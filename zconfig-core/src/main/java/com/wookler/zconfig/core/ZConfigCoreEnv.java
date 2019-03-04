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
 * Date: 9/2/19 10:09 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.*;
import com.wookler.zconfig.common.model.Version;
import com.wookler.zconfig.common.parsers.AbstractConfigParser;
import com.wookler.zconfig.common.utils.DefaultUniqueIDGenerator;
import com.wookler.zconfig.common.utils.IUniqueIDGenerator;
import com.wookler.zconfig.core.zookeeper.ZkConnectionConfig;

import javax.annotation.Nonnull;

/**
 * Singleton instance for setting up the core operating environment.
 */
public class ZConfigCoreEnv extends ZConfigEnv {
    /**
     * Configuration name for ZConfig Core configurations.
     */
    public static final String CONFIG_NAME = "zconfig-core";

    private ZConfigCoreInstance instance;
    private ZkConnectionConfig zkConnectionConfig;
    private IUniqueIDGenerator idGenerator = new DefaultUniqueIDGenerator();

    /**
     * Default constructor - Sets the name of the config.
     */
    public ZConfigCoreEnv() {
        super(CONFIG_NAME);
    }

    /**
     * Perform post-initialisation tasks if any.
     *
     * @throws ConfigurationException
     */
    @Override
    public void postInit() throws ConfigurationException {
        instance = new ZConfigCoreInstance();
        setupInstance(ZConfigCoreInstance.class, instance);
        LogUtils.debug(getClass(), instance);

        zkConnectionConfig = new ZkConnectionConfig();
        ConfigurationAnnotationProcessor
                .readConfigAnnotations(ZkConnectionConfig.class, getConfiguration(),
                                       zkConnectionConfig);
        LogUtils.debug(getClass(), zkConnectionConfig);
        LogUtils.info(getClass(),
                      "Core environment successfully initialized...");
    }

    /**
     * Client environment singleton.
     */
    public ZConfigCoreInstance getInstance() {
        return instance;
    }

    /**
     * Get the ZooKeeper connection configuration.
     *
     * @return - ZooKeeper connection configuration.
     */
    public ZkConnectionConfig getZkConnectionConfig() {
        return zkConnectionConfig;
    }

    /**
     * Get the Unique ID Generator handle.
     *
     * @return - Unique ID Generator
     */
    public IUniqueIDGenerator getIdGenerator() {
        return idGenerator;
    }

    private static final ZConfigCoreEnv __ENV__ = new ZConfigCoreEnv();

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
     * @throws ServiceEnvException
     */
    public static ZConfigCoreEnv get() throws ServiceEnvException {
        try {
            __ENV__.checkState(EEnvState.Initialized);
            return __ENV__;
        } catch (StateException e) {
            throw new ServiceEnvException(e);
        }
    }
}
