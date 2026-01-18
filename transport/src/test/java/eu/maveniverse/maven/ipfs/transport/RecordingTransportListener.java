/*
 * Copyright (c) 2023-2024 Maveniverse Org.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 */
package eu.maveniverse.maven.ipfs.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import org.eclipse.aether.spi.connector.transport.TransportListener;
import org.eclipse.aether.transfer.TransferCancelledException;

class RecordingTransportListener extends TransportListener {

    public final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

    public long dataOffset;

    public long dataLength;

    public int startedCount;

    public int progressedCount;

    public boolean cancelStart;

    public boolean cancelProgress;

    @Override
    public void transportStarted(long dataOffset, long dataLength) throws TransferCancelledException {
        startedCount++;
        progressedCount = 0;
        this.dataLength = dataLength;
        this.dataOffset = dataOffset;
        baos.reset();
        if (cancelStart) {
            throw new TransferCancelledException();
        }
    }

    @Override
    public void transportProgressed(ByteBuffer data) throws TransferCancelledException {
        progressedCount++;
        try {
            Channels.newChannel(baos).write(data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (cancelProgress) {
            throw new TransferCancelledException();
        }
    }
}
