/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.transport;

import java.io.IOException;

/**
 * Special exception type used for "not found" errors.
 */
class ResourceNotFoundException extends IOException {
    ResourceNotFoundException() {
        super("IPFS MFS path not found");
    }
}
