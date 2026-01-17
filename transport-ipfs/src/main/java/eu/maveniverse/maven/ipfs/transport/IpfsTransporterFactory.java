/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.transport;

import static java.util.Objects.requireNonNull;

import io.ipfs.api.IPFS;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Named;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.transport.Transporter;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.NoTransporterException;
import org.eclipse.aether.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private float priority;
    private final ConcurrentMap<String, IpfsNamespacePublisher> ongoingPublishing = new ConcurrentHashMap<>();

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
            boolean refreshIpns = ConfigUtils.getBoolean(
                    session.getConfigProperties(),
                    IpfsTransporterConfigurationKeys.DEFAULT_REFRESH_IPNS,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_REFRESH_IPNS + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_REFRESH_IPNS);
            boolean publishIpns = ConfigUtils.getBoolean(
                    session.getConfigProperties(),
                    IpfsTransporterConfigurationKeys.DEFAULT_PUBLISH_IPNS,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_PUBLISH_IPNS + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_PUBLISH_IPNS);
            String publishIpnsKeyName = ConfigUtils.getString(
                    session.getConfigProperties(),
                    namespace,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_PUBLISH_IPNS_KEY_NAME + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_PUBLISH_IPNS_KEY_NAME);
            boolean publishIpnsKeyCreate = ConfigUtils.getBoolean(
                    session.getConfigProperties(),
                    IpfsTransporterConfigurationKeys.DEFAULT_PUBLISH_IPNS_KEY_CREATE,
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_PUBLISH_IPNS_KEY_CREATE + "." + repository.getId(),
                    IpfsTransporterConfigurationKeys.CONFIG_PROP_PUBLISH_IPNS_KEY_CREATE);

            IPFS ipfs = connect(multiaddr);
            IpfsNamespacePublisher publisher = ongoingPublishing.computeIfAbsent(repository.getId(), k -> {
                IpfsNamespacePublisher pub = new IpfsNamespacePublisher(
                        ipfs, namespace, filesPrefix, namespacePrefix, publishIpnsKeyName, publishIpnsKeyCreate);
                if (refreshIpns) {
                    try {
                        if (!pub.refreshNamespace()) {
                            logger.warn("IPNS refresh unsuccessful, see logs above for reasons");
                        }
                    } catch (IOException e) {
                        logger.warn("IPNS refresh failed", e);
                    }
                }
                return pub;
            });
            return new IpfsTransporter(publisher, publishIpns);
        }
        throw new NoTransporterException(repository);
    }

    @SuppressWarnings("rawtypes")
    private IPFS connect(String multiaddr) {
        try {
            IPFS ipfs = new IPFS(multiaddr);
            Map id = ipfs.id();
            logger.debug("Connected to IPFS w/ ID={} node at '{}'", id.get("ID"), multiaddr);
            return ipfs;
        } catch (IOException e) {
            // this is user error: bad multiaddr or daemon does not run; hard failure
            throw new UncheckedIOException(e);
        }
    }
}
