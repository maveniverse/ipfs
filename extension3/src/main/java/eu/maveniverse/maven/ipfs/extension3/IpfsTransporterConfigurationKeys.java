/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.extension3;

import org.eclipse.aether.RepositorySystemSession;

/**
 * Configuration for IPFS Transport.
 */
public final class IpfsTransporterConfigurationKeys {
    private IpfsTransporterConfigurationKeys() {}

    static final String CONFIG_PROPS_PREFIX = "aether.transport." + IpfsTransporterFactory.NAME + ".";

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
     * Whether to refresh IPNS record before deployment.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link String}
     * @configurationDefaultValue {@link #DEFAULT_REFRESH_IPNS}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_REFRESH_IPNS = CONFIG_PROPS_PREFIX + "refreshIpns";

    public static final boolean DEFAULT_REFRESH_IPNS = true;

    /**
     * Whether to publish IPNS record for deployment. In Resolver 1.9.x this is tricky, as it has no notion of
     * "session end", hence, this option is usable (will behave as expected) only if transport is used in deployment
     * with "deploy at end" feature. Because of that, default is {@code false}.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link String}
     * @configurationDefaultValue {@link #DEFAULT_PUBLISH_IPNS}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_PUBLISH_IPNS = CONFIG_PROPS_PREFIX + "publishIpns";

    public static final boolean DEFAULT_PUBLISH_IPNS = false;

    /**
     * The name of the key to publish IPNS record. It has to exist in the current node, or can be created. The default
     * value uses same value as namespace value is.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link String}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_PUBLISH_IPNS_KEY_NAME = CONFIG_PROPS_PREFIX + "publishIpnsKeyName";

    /**
     * Whether to create key if there is no key with given name. If {@code false} publishing will fail if no key found.
     *
     * @configurationSource {@link RepositorySystemSession#getConfigProperties()}
     * @configurationType {@link Boolean}
     * @configurationDefaultValue {@link #DEFAULT_PUBLISH_IPNS_KEY_CREATE}
     * @configurationRepoIdSuffix Yes
     */
    public static final String CONFIG_PROP_PUBLISH_IPNS_KEY_CREATE = CONFIG_PROPS_PREFIX + "publishIpnsKeyCreate";

    public static final boolean DEFAULT_PUBLISH_IPNS_KEY_CREATE = true;
}
