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
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named
public class IpfsNamespacePublisherRegistryImpl implements IpfsNamespacePublisherRegistry {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IpfsFactory ipfsFactory;

    @Inject
    public IpfsNamespacePublisherRegistryImpl(IpfsFactory ipfsFactory) {
        this.ipfsFactory = requireNonNull(ipfsFactory);
    }

    @Override
    public IpfsNamespacePublisher acquire(
            ConcurrentMap<String, IpfsNamespacePublisher> sessionPublishers,
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
            return sessionPublishers.computeIfAbsent(namespaceKey, k -> {
                try {
                    return new IpfsNamespacePublisherImpl(
                            ipfsFactory.create(multiaddr),
                            namespace,
                            filesPrefix,
                            namespacePrefix,
                            namespaceKey,
                            namespaceKeyCreate,
                            refreshNamespace,
                            publishNamespace);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public void closeAll(ConcurrentMap<String, IpfsNamespacePublisher> sessionPublishers) throws IOException {
        ArrayList<IOException> ioExceptions = new ArrayList<>();
        for (IpfsNamespacePublisher publisher : sessionPublishers.values()) {
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
}
