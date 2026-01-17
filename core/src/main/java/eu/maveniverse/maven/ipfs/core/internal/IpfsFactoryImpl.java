/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.core.internal;

import eu.maveniverse.maven.ipfs.core.IpfsFactory;
import io.ipfs.api.IPFS;
import java.io.IOException;
import java.util.Map;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named
public class IpfsFactoryImpl implements IpfsFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("rawtypes")
    @Override
    public IPFS create(String multiaddr) throws IOException {
        IPFS ipfs = new IPFS(multiaddr);
        Map id = ipfs.id();
        logger.debug("Connected to IPFS node ID={} at '{}'", id.get("ID"), multiaddr);
        return ipfs;
    }
}
