/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.transport;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.ipfs.core.IpfsNamespacePublisher;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import org.eclipse.aether.spi.connector.transport.AbstractTransporter;
import org.eclipse.aether.spi.connector.transport.GetTask;
import org.eclipse.aether.spi.connector.transport.PeekTask;
import org.eclipse.aether.spi.connector.transport.PutTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A transporter using {@link IpfsNamespacePublisher} to implement transport features.
 */
final class IpfsTransporter extends AbstractTransporter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IpfsNamespacePublisher publisher;

    IpfsTransporter(IpfsNamespacePublisher publisher) {
        this.publisher = requireNonNull(publisher);
    }

    @Override
    public int classify(Throwable error) {
        if (error instanceof ResourceNotFoundException) {
            return ERROR_NOT_FOUND;
        }
        return ERROR_OTHER;
    }

    @Override
    protected void implPeek(PeekTask task) throws Exception {
        Optional<IpfsNamespacePublisher.Stat> stat = publisher.stat(task.getLocation().getPath());
        if (stat.isEmpty() || !stat.orElseThrow().file()) {
            throw new ResourceNotFoundException();
        }
    }

    @Override
    protected void implGet(GetTask task) throws Exception {
        Optional<IpfsNamespacePublisher.Stat> stat =
                publisher.stat(task.getLocation().getPath());
        if (stat.isPresent() && stat.orElseThrow().file()) {
            IpfsNamespacePublisher.Stat node = stat.orElseThrow();
            Optional<InputStream> nodeContent = publisher.get(node.hash());
            if (nodeContent.isPresent()) {
                try (InputStream content = nodeContent.orElseThrow()) {
                    utilGet(task, content, true, node.size(), false);
                    return;
                }
            }
        }
        throw new ResourceNotFoundException();
    }

    @Override
    protected void implPut(PutTask task) throws Exception {
        try (InputStream inputStream = task.newInputStream()) {
            publisher.put(task.getLocation().getPath(), inputStream);
        }
        utilPut(task, OutputStream.nullOutputStream(), true);
    }

    @Override
    protected void implClose() {}
}
