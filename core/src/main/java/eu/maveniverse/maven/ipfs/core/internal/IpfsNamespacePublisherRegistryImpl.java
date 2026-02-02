/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.core.internal;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.ipfs.core.IpfsFactory;
import eu.maveniverse.maven.ipfs.core.IpfsNamespacePublisher;
import eu.maveniverse.maven.ipfs.core.IpfsNamespacePublisherRegistry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.aether.RepositorySystemSession;

@Singleton
@Named
public class IpfsNamespacePublisherRegistryImpl implements IpfsNamespacePublisherRegistry {
    private final IpfsFactory ipfsFactory;

    @Inject
    public IpfsNamespacePublisherRegistryImpl(IpfsFactory ipfsFactory) {
        this.ipfsFactory = requireNonNull(ipfsFactory);
    }

    @Override
    public IpfsNamespacePublisher acquire(
            RepositorySystemSession session,
            String multiaddr,
            String namespace,
            String filesPrefix,
            String namespacePrefix,
            String namespaceKey,
            boolean namespaceKeyCreate,
            boolean refreshNamespace,
            boolean publishNamespace)
            throws IOException {
        try {
            ConcurrentMap<String, IpfsNamespacePublisher> sessionPublishers = sessionPublishers(session);
            return sessionPublishers.computeIfAbsent(namespace, k -> {
                try {
                    return new IpfsNamespacePublisherImpl(
                            ipfsFactory.create(multiaddr),
                            namespace,
                            filesPrefix,
                            namespacePrefix,
                            namespaceKey,
                            namespaceKeyCreate,
                            refreshNamespace,
                            publishNamespace,
                            () -> {
                                sessionPublishers.remove(namespace);
                            });
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public void closeAll(RepositorySystemSession session) throws IOException {
        ArrayList<IOException> ioExceptions = new ArrayList<>();
        // we close all; but the map will be modified by onClose callback, so copy first
        for (IpfsNamespacePublisher publisher :
                List.copyOf(sessionPublishers(session).values())) {
            try {
                publisher.close();
            } catch (IOException e) {
                ioExceptions.add(e);
            }
        }
        if (!ioExceptions.isEmpty()) {
            IOException ex = new IOException("One or more publishing failed");
            ioExceptions.forEach(ex::addSuppressed);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private ConcurrentMap<String, IpfsNamespacePublisher> sessionPublishers(RepositorySystemSession session) {
        return (ConcurrentMap<String, IpfsNamespacePublisher>) session.getData()
                .computeIfAbsent(IpfsNamespacePublisherRegistry.class.getName(), ConcurrentHashMap::new);
    }
}
