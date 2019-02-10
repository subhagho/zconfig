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
    private Configuration configuration;
    private ZConfigClientInstance instance;
    private EnvState state = new EnvState();

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

    private void init(AbstractConfigParser parser, String configfile,
                      Version version)
    throws ConfigurationException {
        try {
            LogUtils.info(getClass(), String.format(
                    "Initializing Client Environment : With Configuration file [%s]...",
                    configfile));
            Path path = Paths.get(configfile);
            parser.parse(configfile, ConfigProviderFactory.reader(path.toUri()),
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

    private void dispose() {
        state.dispose();
    }

    public EEnvState getState() {
        return state.getState();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ZConfigClientInstance getInstance() {
        return instance;
    }

    private static final ZConfigClientEnv __ENV__ = new ZConfigClientEnv();

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

    public static void shutdown() {
        synchronized (__ENV__) {
            __ENV__.dispose();
        }
    }

    public static ZConfigClientEnv get() throws ZConfigClientException {
        try {
            __ENV__.state.checkState(EEnvState.Initialized);
            return __ENV__;
        } catch (StateException e) {
            throw new ZConfigClientException(e);
        }
    }
}
