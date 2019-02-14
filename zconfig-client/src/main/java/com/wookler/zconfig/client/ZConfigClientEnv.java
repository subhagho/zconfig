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
 * Date: 10/2/19 5:48 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.*;
import com.wookler.zconfig.common.model.Version;
import com.wookler.zconfig.common.parsers.AbstractConfigParser;
import com.wookler.zconfig.common.utils.NetUtils;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Singleton class to define and expose environment settings.
 */
public class ZConfigClientEnv extends ZConfigEnv {
    /**
     * Configuration name for ZConfig Client configurations.
     */
    public static final String CONFIG_NAME = "zconfig-client";

    /**
     * Client instance handle.
     */
    private ZConfigClientInstance instance;

    /**
     * Default constructor - Sets the name of the config.
     */
    public ZConfigClientEnv() {
        super(CONFIG_NAME);
    }

    /**
     * Initialize this client environment from the specified configuration file and version
     * using the configuration parser.
     *
     * @param parser     - Configuration parser to use.
     * @param configfile - Configuration file path.
     * @param version    - Configuration version (expected)
     * @throws ConfigurationException
     */
    @Override
    protected void init(AbstractConfigParser parser, String configfile,
                        Version version)
    throws ConfigurationException {
        try {
            super.init(parser, configfile, version);
            instance = new ZConfigClientInstance();
            setupInstance(ZConfigClientInstance.class, instance);
            LogUtils.debug(getClass(), instance);


            updateState(EEnvState.Initialized);
            LogUtils.info(getClass(),
                          "Client environment successfully initialized...");
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Get the instance header for this client.
     *
     * @return - Client instance header.
     */
    public ZConfigClientInstance getInstance() {
        return instance;
    }

    /**
     * Client environment singleton.
     */
    private static final ZConfigClientEnv __ENV__ = new ZConfigClientEnv();

    /**
     * Setup the client environment using the passed configuration file.
     *
     * @param configfile - Configuration file (path) to read from.
     * @param version    - Configuration version (expected)
     * @throws ConfigurationException
     */
    public static void setup(@Nonnull String configfile, @Nonnull String version)
    throws ConfigurationException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(configfile));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(version));

        synchronized (__ENV__) {
            try {
                if (__ENV__.getState() != EEnvState.Initialized) {
                    __ENV__.init(configfile, Version.parse(version));
                }
            } catch (ValueParseException e) {
                throw new ConfigurationException(e);
            }
        }
    }

    /**
     * Setup the client environment using the passed configuration file.
     * Method to be used in-case the configuration type cannot be deciphered using
     * the file extension.
     *
     * @param configfile - Configuration file (path) to read from.
     * @param type       - Configuration type.
     * @param version    - Configuration version (expected)
     * @throws ConfigurationException
     */
    public static void setup(@Nonnull String configfile,
                             @Nonnull ConfigProviderFactory.EConfigType type,
                             @Nonnull String version)
    throws ConfigurationException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(configfile));
        Preconditions.checkArgument(type != null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(version));

        synchronized (__ENV__) {
            try {
                if (__ENV__.getState() != EEnvState.Initialized) {
                    __ENV__.init(configfile, type, Version.parse(version));
                }
            } catch (ValueParseException e) {
                throw new ConfigurationException(e);
            }
        }
    }

    /**
     * Shutdown this client environment.
     */
    public static void shutdown() {
        synchronized (__ENV__) {
            __ENV__.dispose();
        }
    }

    /**
     * Get a handle to the client environment singleton.
     *
     * @return - Client Environment handle.
     * @throws ZConfigClientException
     */
    public static ZConfigClientEnv get() throws ZConfigClientException {
        try {
            __ENV__.checkState(EEnvState.Initialized);
            return __ENV__;
        } catch (StateException e) {
            throw new ZConfigClientException(e);
        }
    }
}
