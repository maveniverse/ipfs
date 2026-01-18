/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.extension3;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.ipfs.core.IpfsNamespacePublisher;
import eu.maveniverse.maven.ipfs.core.IpfsNamespacePublisherRegistry;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifecycle participant for IPFS.
 */
@Singleton
@Named
public class IpfsLifecycleParticipant extends AbstractMavenLifecycleParticipant {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IpfsNamespacePublisherRegistry registry;

    @Inject
    public IpfsLifecycleParticipant(IpfsNamespacePublisherRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
        try {
            registry.closeAll((ConcurrentMap<String, IpfsNamespacePublisher>) session.getRepositorySession()
                    .getData()
                    .computeIfAbsent(IpfsNamespacePublisherRegistry.class.getName(), ConcurrentHashMap::new));
        } catch (IOException e) {
            throw new MavenExecutionException("Failed Namespace publishing", e);
        }
    }
}
