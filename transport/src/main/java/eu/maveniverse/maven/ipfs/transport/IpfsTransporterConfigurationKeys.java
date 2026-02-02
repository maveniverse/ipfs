/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.transport;

import org.eclipse.aether.RepositorySystemSession;

/**
 * Configuration for IPFS Transport.
 */
public final class IpfsTransporterConfigurationKeys {
    private IpfsTransporterConfigurationKeys() {}

    private static final String CONFIG_PROPS_PREFIX = "aether.transport." + IpfsTransporterFactory.NAME + ".";

    /**
     * Multiaddress of node to connect to, by default expects local node.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link String}
     * @configurationDefaultValue {@link #DEFAULT_MULTIADDR}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_MULTIADDR = CONFIG_PROPS_PREFIX + "multiaddr";

    public static final String DEFAULT_MULTIADDR = "/ip4/127.0.0.1/tcp/5001";

    /**
     * The prefix to use before namespace.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link String}
     * @configurationDefaultValue {@link #DEFAULT_FILES_PREFIX}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_FILES_PREFIX = CONFIG_PROPS_PREFIX + "filesPrefix";

    public static final String DEFAULT_FILES_PREFIX = "publish";

    /**
     * Whether to refresh namespace IPNS record.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link String}
     * @configurationDefaultValue {@link #DEFAULT_REFRESH_NAMESPACE}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_REFRESH_NAMESPACE = CONFIG_PROPS_PREFIX + "refreshNamespace";

    public static final boolean DEFAULT_REFRESH_NAMESPACE = true;

    /**
     * Whether to publish namespace IPNS record (if it has pending content).
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link String}
     * @configurationDefaultValue {@link #DEFAULT_PUBLISH_NAMESPACE}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_PUBLISH_NAMESPACE = CONFIG_PROPS_PREFIX + "publishNamespace";

    public static final boolean DEFAULT_PUBLISH_NAMESPACE = true;

    /**
     * The name of the key to publish namespace as IPNS record. It has to exist in the current node, or can be created.
     * The default value uses same value as namespace value is.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link String}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_NAMESPACE_KEY = CONFIG_PROPS_PREFIX + "namespaceKey";

    /**
     * Whether to create key if there is no namespace key with given name. If {@code false} publishing will fail if no
     * key found.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link Boolean}
     * @configurationDefaultValue {@link #DEFAULT_NAMESPACE_KEY_CREATE}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_NAMESPACE_KEY_CREATE = CONFIG_PROPS_PREFIX + "namespaceKeyCreate";

    public static final boolean DEFAULT_NAMESPACE_KEY_CREATE = true;

    /**
     * Whether to close publisher when transport is closed. This config is really only to help testing, as in reality
     * it is extension that should close all publishers at the session end.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link Boolean}
     * @configurationDefaultValue {@link #DEFAULT_TRANSPORT_CLOSE_PUBLISHER}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_TRANSPORT_CLOSE_PUBLISHER = CONFIG_PROPS_PREFIX + "transportClosePublisher";

    public static final boolean DEFAULT_TRANSPORT_CLOSE_PUBLISHER = false;
}
