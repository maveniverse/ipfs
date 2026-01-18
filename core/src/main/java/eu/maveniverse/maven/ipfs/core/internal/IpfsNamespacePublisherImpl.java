/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.core.internal;

import static java.util.Objects.requireNonNull;

import eu.maveniverse.maven.ipfs.core.IpfsNamespacePublisher;
import io.ipfs.api.IPFS;
import io.ipfs.api.KeyInfo;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named
public class IpfsNamespacePublisherImpl implements IpfsNamespacePublisher {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IPFS ipfs;
    private final String nsRoot;
    private final String root;
    private final String namespace;
    private final String namespaceKey;
    private final boolean namespaceKeyCreate;
    private final boolean publishNamespace;
    private final AtomicBoolean pendingContent;

    public IpfsNamespacePublisherImpl(
            IPFS ipfs,
            String namespace,
            String filesPrefix,
            String namespacePrefix,
            String namespaceKey,
            boolean namespaceKeyCreate,
            boolean refreshNamespace,
            boolean publishNamespace)
            throws IOException {
        this.ipfs = requireNonNull(ipfs);
        this.nsRoot = URI.create("ipfs:///")
                .resolve(requireNonNull(filesPrefix) + "/")
                .resolve(requireNonNull(namespace) + "/")
                .normalize()
                .getPath();
        this.root = namespacePrefix.isBlank()
                ? this.nsRoot
                : URI.create("ipfs:///")
                        .resolve(requireNonNull(filesPrefix) + "/")
                        .resolve(requireNonNull(namespace) + "/")
                        .resolve(requireNonNull(namespacePrefix) + "/")
                        .normalize()
                        .getPath();
        this.namespace = requireNonNull(namespace);
        this.namespaceKey = requireNonNull(namespaceKey);
        this.namespaceKeyCreate = namespaceKeyCreate;
        this.publishNamespace = publishNamespace;
        this.pendingContent = new AtomicBoolean(false);

        if (refreshNamespace) {
            refreshNamespace();
        }
    }

    @Override
    public String namespace() {
        return namespace;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Optional<Stat> stat(String relPath) throws IOException {
        try {
            Map stat = ipfs.files.stat(root + relPath);
            return Optional.of(() -> stat);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException && e.getMessage().contains("\"Message\":\"file does not exist\"")) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public Optional<InputStream> get(Multihash multihash) throws IOException {
        try {
            return Optional.of(ipfs.catStream(multihash));
        } catch (RuntimeException e) {
            // TODO: what happens here if non-existent CID?
            Throwable cause = e.getCause();
            if (cause instanceof IOException && e.getMessage().contains("\"Message\":\"file does not exist\"")) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public void put(String relPath, InputStream inputStream) throws IOException {
        ipfs.files.write(root + relPath, new NamedStreamable.InputStreamWrapper(inputStream), true, true);
        pendingContent.set(true);
    }

    @Override
    public void close() throws IOException {
        if (publishNamespace && pendingContent.get()) {
            publishNamespace();
        }
    }

    @SuppressWarnings("rawtypes")
    private Optional<Multihash> resolveName(String name) throws IOException {
        try {
            Map res = ipfs.dag.resolve("/ipns/" + name);
            return Optional.of(Multihash.decode((String) ((Map) res.get("Cid")).get("/")));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private Optional<KeyInfo> getOrCreateKey(String keyName, boolean create) throws IOException {
        Optional<KeyInfo> keyInfoOptional = ipfs.key.list().stream()
                .filter(k -> Objects.equals(keyName, k.name))
                .findAny();
        if (create && keyInfoOptional.isEmpty()) {
            keyInfoOptional = Optional.of(ipfs.key.gen(keyName, Optional.empty(), Optional.empty()));
        }
        return keyInfoOptional;
    }

    private void refreshNamespace() throws IOException {
        logger.info("Refreshing IPNS {} at {}...", namespace, nsRoot);
        Optional<Multihash> res = resolveName(namespace);
        if (res.isPresent()) {
            Multihash namespaceCid = res.orElseThrow();
            try {
                ipfs.files.rm(nsRoot, true, true);
                ipfs.files.cp("/ipfs/" + namespaceCid.toBase58(), nsRoot, true);
                ipfs.pin.add(namespaceCid);
                ipfs.pin.verify(false, false);
                logger.info("Refreshed IPNS {} at {} to {}...", namespace, nsRoot, namespaceCid);
            } catch (Exception e) {
                // not yet published?; ignore
                logger.debug("Could not refresh IPNS {}", namespaceCid);
            }
        } else {
            logger.info("Not refreshed: key '{}' not available and not allowed to create it", namespaceKey);
        }
    }

    @SuppressWarnings("rawtypes")
    private void publishNamespace() throws IOException {
        logger.info("Publishing IPNS {} at {}...", namespace, nsRoot);
        Optional<Stat> stat = stat(nsRoot);
        if (stat.isPresent()) {
            Multihash cid = stat.orElseThrow().hash();
            Optional<KeyInfo> keyInfo = getOrCreateKey(namespaceKey, namespaceKeyCreate);
            if (keyInfo.isPresent()) {
                ipfs.pin.add(cid);
                Map publish = ipfs.name.publish(cid, Optional.of(keyInfo.orElseThrow().name));
                logger.info("Published IPNS {} (pointing to {})", publish.get("Name"), publish.get("Value"));
            } else {
                logger.info("Not published: key '{}' not available nor allowed to create it", namespaceKey);
            }
        }
    }
}
