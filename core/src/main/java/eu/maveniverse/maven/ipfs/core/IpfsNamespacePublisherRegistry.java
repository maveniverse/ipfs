/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.core;

import java.io.IOException;
import java.util.Collection;

/**
 * Namespace publisher registry, that exposes IPFS related methods.
 */
public interface IpfsNamespacePublisherRegistry {
    /**
     * Acquires instance of {@link IpfsNamespacePublisher}, never {@code null}.
     */
    IpfsNamespacePublisher ipfsNamespacePublisher(
            String multiaddr,
            String namespace,
            String filesPrefix,
            String namespacePrefix,
            String namespaceKey,
            boolean namespaceKeyCreate,
            boolean refreshNamespace,
            boolean publishNamespace)
            throws IOException;

    /**
     * Publishes namespaces that has pending content waiting to be published, never {@code null}.
     */
    Collection<String> publishNamespaces() throws IOException;
}
