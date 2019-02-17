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
 * Date: 13/2/19 10:19 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
 * Abstract base class for defining the operating environment.
 */
public abstract class ZConfigEnv {
    /**
     * Name of this configuration instance.
     */
    private String configName;
    /**
     * Parsed configuration handle.
     */
    private Configuration configuration;
    /**
     * Client instance state.
     */
    private EnvState state = new EnvState();

    protected ZConfigEnv(@Nonnull String configName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(configName));
        this.configName = configName;
    }

    /**
     * Initialize this client environment from the specified configuration file and version.
     *
     * @param configfile - Configuration file path.
     * @param version    - Configuration version (expected)
     * @throws ConfigurationException
     */
    protected void init(String configfile, Version version)
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
    protected void init(String configfile, ConfigProviderFactory.EConfigType type,
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
    protected void init(AbstractConfigParser parser, String configfile,
                        Version version)
    throws ConfigurationException {
        try {
            LogUtils.info(getClass(), String.format(
                    "Initializing Client Environment : With Configuration file [%s]...",
                    configfile));
            Path path = Paths.get(configfile);
            parser.parse(configName, ConfigProviderFactory.reader(path.toUri()),
                         version);
            configuration = parser.getConfiguration();
            if (configuration == null) {
                throw new ConfigurationException(String.format(
                        "Error parsing configuration : NULL configuration read. [file=%s]",
                        configfile));
            }

            LogUtils.info(getClass(),
                          "Client environment successfully initialized...");
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    protected void setupInstance(@Nonnull Class<? extends ZConfigInstance> type,
                                 @Nonnull ZConfigInstance instance)
    throws ConfigurationException {
        instance.setId(UUID.randomUUID().toString());
        instance.setStartTime(DateTime.now());
        ConfigurationAnnotationProcessor
                .readConfigAnnotations(type,
                                       getConfiguration(),
                                       instance);
        InetAddress addr = NetUtils.getIpAddress();
        if (addr != null) {
            instance.setIp(addr.getHostAddress());
            instance.setHostname(addr.getCanonicalHostName());
        }
    }

    /**
     * Update the state of this instance.
     *
     * @param state - State to update to.
     */
    protected void updateState(EEnvState state) {
        this.state.setState(state);
    }

    /**
     * Check the state of this instance.
     *
     * @param state - Expected state.
     * @throws StateException - Exception will be raised if state is not as expected.
     */
    protected void checkState(EEnvState state) throws StateException {
        this.state.checkState(state);
    }

    /**
     * Disposed this client environment instance.
     */
    protected void dispose() {
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
     * Get a new instance of the JSON Object mapper.
     *
     * @return - JSON Object mapper.
     */
    public ObjectMapper getJsonMapper() {
        ObjectMapper mapper = new ObjectMapper();

        return mapper;
    }
}
