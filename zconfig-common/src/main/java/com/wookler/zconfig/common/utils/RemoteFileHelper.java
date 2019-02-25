/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Copyright (c) $year
 * Date: 25/2/19 10:11 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.utils;

import com.google.common.base.Preconditions;
import com.wookler.zconfig.common.readers.EReaderType;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Helper class to download/upload remote files.
 */
public class RemoteFileHelper {
    /**
     * Download the specified file from the remote location.
     *
     * @param remoteUri - URI of the HTTP endpoint to download from.
     * @param location  - Local File to create.
     * @return - Number of file bytes read.
     * @throws IOException
     */
    public static long downloadRemoteFile(@Nonnull URI remoteUri,
                                          @Nonnull File location)
    throws IOException {
        Preconditions.checkArgument(remoteUri != null);
        Preconditions.checkArgument(location != null);
        EReaderType type = EReaderType.parseFromUri(remoteUri);
        Preconditions.checkNotNull(type);

        if (type != EReaderType.HTTP) {
            throw new IOException(String.format(
                    "Method should be only called for HTTP channel. [passed channel=%s]",
                    type.name()));
        }
        if (location.exists()) {
            if (!location.delete()) {
                throw new IOException(
                        String.format("Error deleting existing file : [path=%s]",
                                      location.getAbsolutePath()));
            }
        }
        URL url = remoteUri.toURL();
        try (
                ReadableByteChannel remoteChannel = Channels
                        .newChannel(url.openStream())) {
            try (FileOutputStream fos = new FileOutputStream(location)) {
                return fos.getChannel()
                          .transferFrom(remoteChannel, 0, Long.MAX_VALUE);
            }
        }
    }
}
