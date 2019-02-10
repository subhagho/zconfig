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
import com.wookler.zconfig.common.model.Configuration;
import com.wookler.zconfig.common.model.Version;
import com.wookler.zconfig.common.parsers.AbstractConfigParser;
import com.wookler.zconfig.common.utils.NetUtils;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Singleton class to define and expose environment settings.
 */
public class ZConfigClientEnv {
    /**
     * Configuration name for ZConfig Client configurations.
     */
    public static final String CONFIG_NAME = "zconfig-client";

    /**
     * Parsed configuration handle.
     */
    private Configuration configuration;
    /**
     * Client instance handle.
     */
    private ZConfigClientInstance instance;
    /**
     * Client instance state.
     */
    private EnvState state = new EnvState();

    /**
     * Initialize this client environment from the specified configuration file and version.
     *
     * @param configfile - Configuration file path.
     * @param version    - Configuration version (expected)
     * @throws ConfigurationException
     */
    private void init(String configfile, Version version)
    throws ConfigurationException {
        try {
            AbstractConfigParser parser = ConfigProviderFactory.parser(configfile);
            if (parser == null) {
                throw new ConfigurationException(String.format(
                        "Cannot get configuration parser instance. [file=%s]",
                        configfile));
            }
            init(parser, configfile, version);
        } catch (Exception e) {
            state.setError(e);
            throw new ConfigurationException(e);
        }
    }

    /**
     * Initialize this client environment from the specified configuration file and version.
     *
     * @param configfile - Configuration file path.
     * @param type       - Configuration file type (in-case file type cannot be deciphered).
     * @param version    - Configuration version (expected)
     * @throws ConfigurationException
     */
    private void init(String configfile, ConfigProviderFactory.EConfigType type,
                      Version version)
    throws ConfigurationException {
        try {
            AbstractConfigParser parser = ConfigProviderFactory.parser(type);
            if (parser == null) {
                throw new ConfigurationException(String.format(
                        "Cannot get configuration parser instance. [file=%s]",
                        configfile));
            }
            init(parser, configfile, version);
        } catch (Exception e) {
            state.setError(e);
            throw new ConfigurationException(e);
        }
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
    private void init(AbstractConfigParser parser, String configfile,
                      Version version)
    throws ConfigurationException {
        try {
            LogUtils.info(getClass(), String.format(
                    "Initializing Client Environment : With Configuration file [%s]...",
                    configfile));
            Path path = Paths.get(configfile);
            parser.parse(CONFIG_NAME, ConfigProviderFactory.reader(path.toUri()),
                         version);
            configuration = parser.getConfiguration();
            if (configuration == null) {
                throw new ConfigurationException(String.format(
                        "Error parsing configuration : NULL configuration read. [file=%s]",
                        configfile));
            }
            instance = new ZConfigClientInstance();
            instance.setId(UUID.randomUUID().toString());
            instance.setStartTime(DateTime.now());
            ConfigurationAnnotationProcessor
                    .readConfigAnnotations(ZConfigClientInstance.class,
                                           configuration,
                                           instance);
            InetAddress addr = NetUtils.getIpAddress();
            if (addr != null) {
                instance.setIp(addr.getHostAddress());
                instance.setHostname(addr.getCanonicalHostName());
            }
            LogUtils.debug(getClass(), instance);


            state.setState(EEnvState.Initialized);
            LogUtils.info(getClass(),
                          "Client environment successfully initialized...");
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Disposed this client environment instance.
     */
    private void dispose() {
        state.dispose();
    }

    /**
     * Get the state of this client environment.
     *
     * @return - Current state.
     */
    public EEnvState getState() {
        return state.getState();
    }

    /**
     * Get configuration loaded for this client environment.
     *
     * @return - Configuration handle.
     */
    public Configuration getConfiguration() {
        return configuration;
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
                if (__ENV__.state.getState() != EEnvState.Initialized) {
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
                if (__ENV__.state.getState() != EEnvState.Initialized) {
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
            __ENV__.state.checkState(EEnvState.Initialized);
            return __ENV__;
        } catch (StateException e) {
            throw new ZConfigClientException(e);
        }
    }
}
