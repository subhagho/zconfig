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

package com.codekutter.zconfig.client.factory;

import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.codekutter.zconfig.common.ConfigProviderFactory;
import com.codekutter.zconfig.common.ConfigurationAnnotationProcessor;
import com.codekutter.zconfig.common.ConfigurationException;
import com.codekutter.zconfig.common.model.Configuration;
import com.codekutter.zconfig.common.model.ConfigurationSettings;
import com.codekutter.zconfig.common.model.ESyncMode;
import com.codekutter.zconfig.common.model.Version;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class loads and manages configuration instances and annotated class instances.
 */
public class ConfigurationManager {
    private static class AutowiredType {
        public String relativePath;
        public Class<?> type;
    }

    /**
     * Instance of the configuration loader.
     */
    private ConfigurationLoader loader = new ConfigurationLoader();
    /**
     * Map of Loaded configurations. Only one version of a specific configuration
     * can be loaded per client instance.
     */
    private Map<String, Configuration> loadedConfigs = new HashMap<>();
    /**
     * Map of auto-wired object instances. This is to update these instances
     * upon configuration updates.
     */
    private Map<String, Object> autowiredInstances = new HashMap<>();
    /**
     * Lock to be used for synchronizing configuration loads.
     */
    private ReentrantLock configCacheLock = new ReentrantLock();
    /**
     * Lock to be used to update/add auto-wired instances.
     */
    private ReentrantLock autowireCacheLock = new ReentrantLock();
    /**
     * Lock to be used to synchronize specific configuration updates.
     */
    private Map<String, ReentrantLock> configInstanceLocks = new HashMap<>();
    /**
     * Index map for reading auto-wired instances impacted by a specific update.
     */
    private Multimap<String, AutowiredType> autowiredIndex = HashMultimap.create();
    /**
     * Registered application Groups for which configurations has been loaded.
     */
    private Multimap<String, Configuration> applicationGroups =
            HashMultimap.create();

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
                        postConfigurationLoad(configuration);
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
                        postConfigurationLoad(configuration);
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
     * Update maps based on the loaded configuration.
     *
     * @param configuration - Loaded configuration instance.
     */
    private void postConfigurationLoad(Configuration configuration) {
        if (configuration != null) {
            loadedConfigs.put(configuration.getName(), configuration);
            configInstanceLocks.put(configuration.getName(), new ReentrantLock());
            applicationGroups.put(configuration.getApplicationGroup(),
                                  configuration);
        }
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
     * Get a configuration handle and lock it for updates.
     *
     * @param configName - Configuration name.
     * @return - Configuration handle.
     */
    public Configuration getWithLock(@Nonnull String configName) {
        Configuration config = get(configName);
        if (config != null) {
            ReentrantLock lock = configInstanceLocks.get(configName);
            lock.lock();
        }
        return config;
    }

    /**
     * Unlock the configuration lock handle.
     *
     * @param configName - Configuration name.
     * @return - Was unlocked?
     * @throws ConfigurationException
     */
    public boolean releaseLock(String configName) {
        ReentrantLock lock = configInstanceLocks.get(configName);
        if (lock != null) {
            if (lock.isLocked()) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get all the registered Application Groups.
     *
     * @return - Set of Application Groups.
     */
    public Set<String> getApplicationGroups() {
        return applicationGroups.keySet();
    }

    /**
     * Apply configuration updates to any registered Auto-wired types.
     *
     * @param configName - Configuration name.
     * @param paths      - List of updated paths.
     * @throws ConfigurationException
     */
    public void applyConfigurationUpdates(String configName, List<String> paths)
    throws ConfigurationException {
        Configuration config = loadedConfigs.get(configName);
        if (config != null && config.getSyncMode() == ESyncMode.EVENTS) {
            Map<String, AutowiredType> updated = getUpdatedTypes(configName, paths);
            if (updated != null && !updated.isEmpty()) {
                for (String key : updated.keySet()) {
                    AutowiredType type = updated.get(key);
                    autowireType(type.type, configName, type.relativePath, true);
                }
            }
        }
    }

    /**
     * Resolve the auto-wired types that need to be updated due to the configuration updates.
     *
     * @param configName - Configuration name.
     * @param paths      - List of updated node paths.
     * @return - Map of types needing update.
     */
    private Map<String, AutowiredType> getUpdatedTypes(String configName,
                                                       List<String> paths) {
        Map<String, AutowiredType> map = new HashMap<>();
        for (String path : paths) {
            String indexKey = getTypeIndexKey(path, configName);
            if (autowiredIndex.containsKey(indexKey)) {
                Collection<AutowiredType> types = autowiredIndex.get(indexKey);
                for (AutowiredType type : types) {
                    String key =
                            String.format("%s::%s", type.type.getCanonicalName(),
                                          type.relativePath);
                    if (!map.containsKey(key)) {
                        map.put(key, type);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Create a new instance or Get a cached copy of the specified type. Types are
     * expected to be POJOs with empty constructors.
     *
     * @param type         - Class type to create instance of.
     * @param configName   - Configuration name to autowire from.
     * @param relativePath - Relative Search path to prepend to the search.
     * @param <T>          - Type.
     * @return - Type instance.
     * @throws ConfigurationException
     */
    public <T> T autowireType(@Nonnull Class<? extends T> type,
                              @Nonnull String configName,
                              String relativePath)
    throws ConfigurationException {
        return autowireType(type, configName, relativePath, false);
    }

    /**
     * Create a new instance or Get a cached copy of the specified type. Types are
     * expected to be POJOs with empty constructors.
     *
     * @param type         - Class type to create instance of.
     * @param configName   - Configuration name to autowire from.
     * @param relativePath - Relative Search path to prepend to the search.
     * @param update       - Update the autowired instance.
     * @param <T>          - Type.
     * @return - Type instance.
     * @throws ConfigurationException
     */
    @SuppressWarnings("unchecked")
    private <T> T autowireType(@Nonnull Class<? extends T> type,
                               @Nonnull String configName,
                               String relativePath,
                               boolean update)
    throws ConfigurationException {
        String key = getTypeKey(type, relativePath, configName);
        if (!Strings.isNullOrEmpty(key)) {
            if (!update && autowiredInstances.containsKey(key)) {
                return (T) autowiredInstances.get(key);
            }
            try {
                autowireCacheLock.lock();
                try {
                    if (autowiredInstances.containsKey(key) && !update) {
                        return (T) autowiredInstances.get(key);
                    }
                    Configuration config = loadedConfigs.get(configName);
                    if (config != null) {
                        String path =
                                getSearchPath(type, relativePath);
                        if (!Strings.isNullOrEmpty(path)) {
                            T value = null;
                            if (update) {
                                value = (T) autowiredInstances.get(key);
                            } else
                                value = type.newInstance();
                            ConfigurationAnnotationProcessor
                                    .readConfigAnnotations(type, config, value);
                            autowiredInstances.put(key, value);
                            if (config.getSyncMode() == ESyncMode.EVENTS &&
                                    !update) {
                                AutowiredType at = new AutowiredType();
                                at.relativePath = relativePath;
                                at.type = type;
                                autowiredIndex
                                        .put(getTypeIndexKey(path, configName),
                                             at);
                            }
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
        }
        return null;
    }

    /**
     * Get the index key to index this type instance for updates.
     *
     * @param path       - Search path for this type instance.
     * @param configname - Configuration name.
     * @return - Index Key.
     */
    private String getTypeIndexKey(String path, String configname) {
        return String.format("%s::%s", configname, path);
    }

    /**
     * Get the key to add this autowired instance to.
     *
     * @param type         - Object type.
     * @param reletivePath - Relative path to search from.
     * @param configname   - Configuration instance name.
     * @return - Index Key.
     */
    private String getTypeKey(Class<?> type, String reletivePath,
                              String configname) {
        String path = getSearchPath(type, reletivePath);
        if (!Strings.isNullOrEmpty(path)) {
            return String
                    .format("%s::%s::%s", type.getCanonicalName(), configname,
                            path);
        }
        return null;
    }

    /**
     * Get the search path for this type.
     *
     * @param type         - Type instance to autowire.
     * @param reletivePath - Relative search path.
     * @return - Search path.
     */
    private String getSearchPath(Class<?> type, String reletivePath) {
        String path = ConfigurationAnnotationProcessor
                .hasConfigAnnotation(type);
        if (!Strings.isNullOrEmpty(path)) {
            if (Strings.isNullOrEmpty(reletivePath)) {
                return path;
            } else {
                return String.format("%s.%s", path, reletivePath);
            }
        }
        return null;
    }
}
