/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.transport;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.ipfs.core.IpfsNamespacePublisherRegistry;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.transport.Transporter;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.NoTransporterException;
import org.eclipse.aether.util.ConfigUtils;

/**
 * A transporter factory for repositories using the {@code ipfs:namespace[/namespacePrefix]} URIs.
 * <p>
 * It is recommended to have namespace equal to artifacts groupId prefix, for example artifacts with groupId
 * {@code org.apache.maven.plugins} should be published into {@code org.apache} or {@code org.apache.maven} or
 * {@code org.apache.maven.plugins} namespace.
 */
@Named(IpfsTransporterFactory.NAME)
public final class IpfsTransporterFactory implements TransporterFactory {
    public static final String NAME = "ipfs";

    private static final String PROTO = NAME + ":";
    private static final int PROTO_LEN = PROTO.length();

    private float priority;
    private final IpfsNamespacePublisherRegistry registry;

    @Inject
    public IpfsTransporterFactory(IpfsNamespacePublisherRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public float getPriority() {
        return priority;
    }

    /**
     * Sets the priority of this component.
     *
     * @param priority The priority.
     * @return This component for chaining, never {@code null}.
     */
    public IpfsTransporterFactory setPriority(float priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Creates new instance of {@link IpfsTransporter}.
     *
     * @param session The session.
     * @param repository The remote repository.
     */
    @Override
    public Transporter newInstance(RepositorySystemSession session, RemoteRepository repository)
            throws NoTransporterException {
        requireNonNull(session, "session cannot be null");
        requireNonNull(repository, "repository cannot be null");

        String repositoryUrl = repository.getUrl();
        if (repositoryUrl.startsWith(PROTO)) {
            repositoryUrl = repositoryUrl.substring(PROTO_LEN);
            while (repositoryUrl.startsWith("/")) {
                repositoryUrl = repositoryUrl.substring(1);
            }

            if (repositoryUrl.trim().isEmpty()) {
                throw new NoTransporterException(
                        repository,
                        "Invalid IPFS URL; should be ipfs:namespace[/namespacePrefix] where no segment can be empty string");
            }

            String namespace;
            String namespacePrefix;
            if (repositoryUrl.contains("/")) {
                int firstSlash = repositoryUrl.indexOf("/");
                namespace = repositoryUrl.substring(0, firstSlash);
                namespacePrefix = repositoryUrl.substring(firstSlash + 1);
            } else {
                namespace = repositoryUrl;
                namespacePrefix = "";
            }

            String multiaddr = ConfigUtils.getString(
                    session.getConfigProperties(),
                    IpfsTransporterConfigurationKeys.DEFAULT_MULTIADDR,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_MULTIADDR + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_MULTIADDR);
            String filesPrefix = ConfigUtils.getString(
                    session.getConfigProperties(),
                    IpfsTransporterConfigurationKeys.DEFAULT_FILES_PREFIX,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_FILES_PREFIX + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_FILES_PREFIX);
            boolean refreshNamespace = ConfigUtils.getBoolean(
                    session.getConfigProperties(),
                    IpfsTransporterConfigurationKeys.DEFAULT_REFRESH_NAMESPACE,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_REFRESH_NAMESPACE + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_REFRESH_NAMESPACE);
            boolean publishNamespace = ConfigUtils.getBoolean(
                    session.getConfigProperties(),
                    IpfsTransporterConfigurationKeys.DEFAULT_PUBLISH_NAMESPACE,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_PUBLISH_NAMESPACE + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_PUBLISH_NAMESPACE);
            String namespaceKey = ConfigUtils.getString(
                    session.getConfigProperties(),
                    namespace,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_NAMESPACE_KEY + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_NAMESPACE_KEY);
            boolean namespaceKeyCreate = ConfigUtils.getBoolean(
                    session.getConfigProperties(),
                    IpfsTransporterConfigurationKeys.DEFAULT_NAMESPACE_KEY_CREATE,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_NAMESPACE_KEY_CREATE + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_NAMESPACE_KEY_CREATE);

            try {
                return new IpfsTransporter(
                        registry.ipfsNamespacePublisher(
                                multiaddr,
                                namespace,
                                filesPrefix,
                                namespacePrefix,
                                namespaceKey,
                                namespaceKeyCreate,
                                refreshNamespace,
                                publishNamespace),
                        false);
            } catch (IOException e) {
                throw new UncheckedIOException(e); // hard failure, like bad multiaddr or node not running
            }
        }
        throw new NoTransporterException(repository);
    }
}
