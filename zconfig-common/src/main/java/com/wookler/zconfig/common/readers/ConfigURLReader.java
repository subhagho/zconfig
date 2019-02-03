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
 * Date: 2/2/19 10:37 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.readers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Configuration reader to read from a remote URL location.
 */
public class ConfigURLReader extends AbstractConfigReader {
    /**
     * Input stream for this reader instance.
     */
    private BufferedReader inputStream;
    /**
     * Formatted URL location to read from.
     */
    private URL remoteURL;

    /**
     * Create this instance with the specified URL string.
     *
     * @param url - URL String
     */
    public ConfigURLReader(String url) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url));
        try {
            remoteURL = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create this instance with the specified URL.
     *
     * @param url - Remote URL
     */
    public ConfigURLReader(URL url) {
        Preconditions.checkArgument(url != null);
        remoteURL = url;
    }

    /**
     * Open this configuration reader instance.
     *
     * @throws ConfigurationException
     */
    @Override
    public void open() throws ConfigurationException {
        if (!state.isOpen()) {
            try {
                URLConnection conn = remoteURL.openConnection();
                inputStream = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                state.setState(EReaderState.Open);
            } catch (IOException e) {
                state.setError(e);
                throw new ConfigurationException(e);
            }
        }
    }

    /**
     * Get the input stream associated with this reader.
     *
     * @return - Input stream.
     * @throws ConfigurationException
     */
    @Override
    public BufferedReader getInputStream() throws ConfigurationException {
        if (!state.isOpen()) {
            throw new ConfigurationException("Reader is not opened.");
        }
        if (state.hasError()) {
            throw new ConfigurationException(state.getError());
        }
        return inputStream;
    }

    /**
     * Close this configuration reader instance.
     */
    @Override
    public void close() {
        if (state.isOpen()) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    state.setState(EReaderState.Closed);
                } catch (IOException e) {
                    state.setError(e);
                }
            }
        }
    }
}
