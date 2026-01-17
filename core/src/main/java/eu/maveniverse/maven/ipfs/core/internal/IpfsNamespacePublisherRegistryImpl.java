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
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
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
    private final ConcurrentLinkedDeque<IpfsNamespacePublisher> publishersToPublish;

    @Inject
    public IpfsNamespacePublisherRegistryImpl(IpfsFactory ipfsFactory) {
        this.ipfsFactory = requireNonNull(ipfsFactory);
        this.publishersToPublish = new ConcurrentLinkedDeque<>();
    }

    @Override
    public IpfsNamespacePublisher ipfsNamespacePublisher(
            String multiaddr,
            String namespace,
            String filesPrefix,
            String namespacePrefix,
            String namespaceKey,
            boolean namespaceKeyCreate,
            boolean refreshNamespace,
            boolean publishNamespace)
            throws IOException {
        IpfsNamespacePublisher publisher = new IpfsNamespacePublisherImpl(
                ipfsFactory.create(multiaddr),
                namespace,
                filesPrefix,
                namespacePrefix,
                namespaceKey,
                namespaceKeyCreate);
        if (refreshNamespace) {
            publisher.refreshNamespace();
        }
        if (publishNamespace) {
            publishersToPublish.push(publisher);
        }
        return publisher;
    }

    @Override
    public Collection<String> publishNamespaces() throws IOException {
        ArrayList<String> publishedNamespaces = new ArrayList<>();
        ArrayList<IOException> ioExceptions = new ArrayList<>();
        while (!publishersToPublish.isEmpty()) {
            IpfsNamespacePublisher publisher = publishersToPublish.pop();
            if (publisher.pendingContent()) {
                try {
                    publisher.publishNamespace();
                } catch (IOException e) {
                    ioExceptions.add(e);
                }
                publishedNamespaces.add(publisher.namespace());
            }
        }
        if (!ioExceptions.isEmpty()) {
            IOException ex = new IOException("One or more publishing failed");
            ioExceptions.forEach(ex::addSuppressed);
            throw ex;
        }
        return publishedNamespaces;
    }
}
