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
 * Date: 26/2/19 9:48 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.client.factory;

import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigProviderFactory;
import com.wookler.zconfig.common.ConfigurationAnnotationProcessor;
import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.model.Configuration;
import com.wookler.zconfig.common.model.ConfigurationSettings;
import com.wookler.zconfig.common.model.Version;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class loads and manages configuration instances and annotated class instances.
 */
public class ConfigurationManager {
    private ConfigurationLoader loader = new ConfigurationLoader();
    private Map<String, Configuration> loadedConfigs = new HashMap<>();
    private Map<Class<?>, Object> autowiredInstances = new HashMap<>();
    private ReentrantLock configCacheLock = new ReentrantLock();
    private ReentrantLock autowireCacheLock = new ReentrantLock();

    /**
     * Load configuration from the specified URI.
     *
     * @param configName - Configuration name.
     * @param configUri  - Configuration URI string.
     * @param configType - Configuration Parser type.
     * @param version    - Configuration URI String.
     * @param settings   - Configuration Settings.
     * @return - Loaded Configuration instance.
     * @throws ConfigurationException
     */
    public Configuration load(@Nonnull String configName,
                              @Nonnull String configUri,
                              @Nonnull
                                      ConfigProviderFactory.EConfigType configType,
                              @Nonnull
                                      Version version,
                              ConfigurationSettings settings)
    throws ConfigurationException {
        Configuration configuration = loadedConfigs.get(configName);
        if (configuration == null) {
            configCacheLock.lock();
            try {
                configuration = loadedConfigs.get(configName);
                if (configuration == null) {
                    configuration = loader
                            .load(configName, configUri, configType, version,
                                  settings);
                    if (configuration != null) {
                        loadedConfigs.put(configName, configuration);
                    } else {
                        throw new ConfigurationException(String.format(
                                "Configuration not found : [name=%s][uri=%s][version=%s]",
                                configName, configUri, version.toString()));
                    }
                }
            } finally {
                configCacheLock.unlock();
            }
        }
        return configuration;
    }

    /**
     * Load configuration from the specified URI.
     *
     * @param configName - Configuration name.
     * @param configUri  - Configuration URI string.
     * @param configType - Configuration Parser type.
     * @param version    - Configuration URI String.
     * @return - Loaded Configuration instance.
     * @throws ConfigurationException
     */
    public Configuration load(@Nonnull String configName,
                              @Nonnull String configUri,
                              @Nonnull
                                      ConfigProviderFactory.EConfigType configType,
                              @Nonnull
                                      Version version)
    throws ConfigurationException {
        return load(configName, configUri, configType, version, null);
    }

    /**
     * Load configuration from the specified filename.
     *
     * @param configName - Configuration name.
     * @param filename   - Configuration filename.
     * @param version    - Configuration URI String.
     * @param settings   - Configuration Settings.
     * @return - Loaded Configuration instance.
     * @throws ConfigurationException
     */
    public Configuration load(@Nonnull String configName,
                              @Nonnull String filename,
                              @Nonnull
                                      Version version,
                              ConfigurationSettings settings)
    throws ConfigurationException {
        Configuration configuration = loadedConfigs.get(configName);
        if (configuration == null) {
            configCacheLock.lock();
            try {
                configuration = loadedConfigs.get(configName);
                if (configuration == null) {
                    configuration = loader
                            .load(configName, filename, version, settings);
                    if (configuration != null) {
                        loadedConfigs.put(configName, configuration);
                    } else {
                        throw new ConfigurationException(String.format(
                                "Configuration not found : [name=%s][file=%s][version=%s]",
                                configName, filename, version.toString()));
                    }
                }
            } finally {
                configCacheLock.unlock();
            }
        }
        return configuration;
    }

    /**
     * Load configuration from the specified filename.
     *
     * @param configName - Configuration name.
     * @param filename   - Configuration filename.
     * @param version    - Configuration URI String.
     * @return - Loaded Configuration instance.
     * @throws ConfigurationException
     */
    public Configuration load(@Nonnull String configName,
                              @Nonnull String filename,
                              @Nonnull
                                      Version version)
    throws ConfigurationException {
        return load(configName, filename, version, null);
    }

    /**
     * Get a cached configuration handle.
     *
     * @param configName - Configuration name.
     * @return - Cached handle or NULL if not in cache.
     */
    public Configuration get(@Nonnull String configName) {
        if (loadedConfigs.containsKey(configName)) {
            return loadedConfigs.get(configName);
        }
        return null;
    }

    /**
     * Create a new instance or Get a cached copy of the specified type. Types are
     * expected to be POJOs with empty constructors.
     *
     * @param type       - Class type to create instance of.
     * @param configName - Configuration name to autowire from.
     * @param <T>        - Type.
     * @return - Type instance.
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    public <T> T autowireType(Class<? extends T> type, String configName)
    throws ConfigurationException {
        if (autowiredInstances.containsKey(type)) {
            return (T) autowiredInstances.get(type);
        }
        try {
            autowireCacheLock.lock();
            try {
                if (autowiredInstances.containsKey(type)) {
                    return (T) autowiredInstances.get(type);
                }
                Configuration config = loadedConfigs.get(configName);
                if (config != null) {
                    String path =
                            ConfigurationAnnotationProcessor
                                    .hasConfigAnnotation(type);
                    if (!Strings.isNullOrEmpty(path)) {
                        T value = type.newInstance();
                        ConfigurationAnnotationProcessor
                                .readConfigAnnotations(type, config, value);
                        autowiredInstances.put(type, value);
                        return value;
                    }
                } else {
                    throw new ConfigurationException(String.format(
                            "Specified configuration not found. [name=%s]",
                            configName));
                }
            } finally {
                autowireCacheLock.unlock();
            }
        } catch (IllegalAccessException e) {
            throw new ConfigurationException(e);
        } catch (InstantiationException e) {
            throw new ConfigurationException(e);
        }
        return null;
    }
}
