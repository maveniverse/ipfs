/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.core.internal;

import eu.maveniverse.maven.ipfs.core.IpfsNamespacePublisher;
import io.ipfs.api.IPFS;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.internal.test.util.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
class IpfsNamespacePublisherImplTest {
    public static final DockerImageName IPFS_KUBO_IMAGE = DockerImageName.parse("ipfs/kubo:release");

    public static GenericContainer<?> kubo = new GenericContainer<>(IPFS_KUBO_IMAGE).withExposedPorts(5001);
    public static int kuboPort;

    @BeforeAll
    static void startContainer() {
        kubo.start();
        kuboPort = kubo.getMappedPort(5001);
    }

    @AfterAll
    static void stopContainer() {
        kubo.stop();
    }

    @Test
    void refreshWritePublish() throws IOException {
        IPFS ipfs = new IpfsFactoryImpl().create("/ip4/127.0.0.1/tcp/" + kuboPort);
        try (IpfsNamespacePublisherImpl publisher = new IpfsNamespacePublisherImpl(
                ipfs, "namespace", "filesPrefix", "namespacePrefix", "namespaceKey", true, false, false, null)) {
            publisher.put("test/test.txt", new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));
        }

        // if path does not exist, this below throws IOEx; but we should have just created this path above
        ipfs.files.ls("/filesPrefix/namespace/namespacePrefix/test/test.txt");
    }

    @Test
    void registry() throws IOException {
        IpfsNamespacePublisherRegistryImpl registry = new IpfsNamespacePublisherRegistryImpl(new IpfsFactoryImpl());
        DefaultRepositorySystemSession session = TestUtils.newSession();
        IpfsNamespacePublisher publisher1 = registry.acquire(
                session,
                "/ip4/127.0.0.1/tcp/" + kuboPort,
                "firstNamespace",
                "filesPrefix",
                "namespacePrefix",
                "firstNamespaceKey",
                false,
                false,
                false);
        publisher1.put("test/test.txt", new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

        IpfsNamespacePublisher publisher2 = registry.acquire(
                session,
                "/ip4/127.0.0.1/tcp/" + kuboPort,
                "secondNamespace",
                "filesPrefix",
                "namespacePrefix",
                "firstNamespaceKey",
                false,
                false,
                false);
        publisher2.put("test/test.txt", new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8)));

        // closes all
        registry.closeAll(session);

        // if path does not exist, this below throws IOEx; but we should have just created this path above
        IPFS ipfs = new IpfsFactoryImpl().create("/ip4/127.0.0.1/tcp/" + kuboPort);
        ipfs.files.ls("/filesPrefix/firstNamespace/namespacePrefix/test/test.txt");
        ipfs.files.ls("/filesPrefix/secondNamespace/namespacePrefix/test/test.txt");
    }
}
