/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.core;

import io.ipfs.multihash.Multihash;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * Namespace publisher, that exposes IPFS related methods.
 */
public interface IpfsNamespacePublisher extends Closeable {
    /**
     * The namespace this publisher publishes, never {@code null}.
     */
    String namespace();

    /**
     * Stat response.
     */
    interface Stat {
        /**
         * Returns the CID of node.
         */
        default Multihash hash() {
            return Multihash.decode((String) stat().get("Hash"));
        }

        /**
         * Returns the size of node (makes sense for files).
         */
        default long size() {
            return Long.parseLong(String.valueOf(stat().getOrDefault("Size", "0")));
        }

        /**
         * Returns the cumulative size of node.
         */
        default long cumulativeSize() {
            return Long.parseLong(String.valueOf(stat().getOrDefault("CumulativeSize", "0")));
        }

        /**
         * Returns {@code true} if this instance represents a file.
         */
        default boolean file() {
            return "file".equals(stat().get("Type"));
        }

        /**
         * The "raw" stat map.
         */
        Map<String, Object> stat();
    }

    /**
     * Stat of IPFS MFS path.
     */
    Optional<Stat> stat(String relPath) throws IOException;

    /**
     * Gets the content from IPFS CID.
     */
    Optional<InputStream> get(Multihash multihash) throws IOException;

    /**
     * Puts the content to IPFS MFS path.
     */
    void put(String relPath, InputStream inputStream) throws IOException;
}
