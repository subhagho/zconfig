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
 * Date: 18/2/19 8:24 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.core.controller;

import com.codekutter.zconfig.common.ConfigurationException;
import com.codekutter.zconfig.transport.events.AbstractConfigUpdateEvent;
import com.codekutter.zconfig.transport.events.ConfigServerUpdateBatch;
import com.codekutter.zconfig.transport.events.ConfigServerUpdateEvent;
import com.codekutter.zconfig.common.model.Configuration;
import com.codekutter.zconfig.common.model.Version;
import com.codekutter.zconfig.common.utils.IUniqueIDGenerator;
import com.codekutter.zconfig.core.IConfigDAO;
import com.codekutter.zconfig.core.PersistenceException;
import com.codekutter.zconfig.core.ServiceEnvException;
import com.codekutter.zconfig.common.ZConfigCoreEnv;
import com.codekutter.zconfig.core.model.*;
import com.codekutter.zconfig.core.model.nodes.PersistedConfigListValueNode;
import com.codekutter.zconfig.core.model.nodes.PersistedConfigValueNode;
import com.codekutter.zconfig.core.zookeeper.ZkUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

import javax.annotation.Nonnull;
import java.security.Principal;
import java.util.List;

/**
 * Controller class to encapsulate transformation/persistence functions.
 */
public class ZConfigPersistenceController {
    /**
     * Configuration persistence DAO handle.
     */
    private IConfigDAO configDAO = null;

    public ZConfigPersistenceController(@Nonnull IConfigDAO configDAO) {
        this.configDAO = configDAO;
    }

    public void create(@Nonnull ApplicationGroup group, @Nonnull Principal user)
    throws PersistenceException {
        try (CuratorFramework client = ZkUtils.getZkClient()) {
            try {
                String zkPath = ZkUtils.getZkPath(group);
                Stat stat = client.checkExists().forPath(zkPath);
                if (stat != null) {
                    throw new PersistenceException(String.format(
                            "Cannot create Application Group : Path already exists. [path=%s]",
                            zkPath));
                }
                zkPath = client.create().creatingParentsIfNeeded().forPath(zkPath);
                String json = ZConfigCoreEnv.coreEnv().getJsonMapper()
                                            .writeValueAsString(group);
                client.setData().forPath(zkPath, json.getBytes());
            } catch (Exception e) {
                throw new PersistenceException(e);
            }
        }
    }

    public void update(@Nonnull ApplicationGroup group, @Nonnull Principal user)
    throws PersistenceException {
        try (CuratorFramework client = ZkUtils.getZkClient()) {
            try {
                String zkPath = ZkUtils.getZkPath(group);
                Stat stat = client.checkExists().forPath(zkPath);
                if (stat == null) {
                    throw new PersistenceException(String.format(
                            "Cannot update Application Group : Path doesn't exists. [path=%s]",
                            zkPath));
                }
                String json = ZConfigCoreEnv.coreEnv().getJsonMapper()
                                            .writeValueAsString(group);
                client.setData().forPath(zkPath, json.getBytes());
            } catch (Exception e) {
                throw new PersistenceException(e);
            }
        }
    }

    public void create(@Nonnull Application group, @Nonnull Principal user)
    throws PersistenceException {
        try (CuratorFramework client = ZkUtils.getZkClient()) {
            try {
                String zkPath = ZkUtils.getZkPath(group);
                Stat stat = client.checkExists().forPath(zkPath);
                if (stat != null) {
                    throw new PersistenceException(String.format(
                            "Cannot create Application : Path already exists. [path=%s]",
                            zkPath));
                }
                zkPath = client.create().creatingParentsIfNeeded().forPath(zkPath);
                String json = ZConfigCoreEnv.coreEnv().getJsonMapper()
                                            .writeValueAsString(group);
                client.setData().forPath(zkPath, json.getBytes());
            } catch (Exception e) {
                throw new PersistenceException(e);
            }
        }
    }

    public void update(@Nonnull Application group, @Nonnull Principal user)
    throws PersistenceException {
        try (CuratorFramework client = ZkUtils.getZkClient()) {
            try {
                String zkPath = ZkUtils.getZkPath(group);
                Stat stat = client.checkExists().forPath(zkPath);
                if (stat == null) {
                    throw new PersistenceException(String.format(
                            "Cannot update Application : Path doesn't exists. [path=%s]",
                            zkPath));
                }
                String json = ZConfigCoreEnv.coreEnv().getJsonMapper()
                                            .writeValueAsString(group);
                client.setData().forPath(zkPath, json.getBytes());
            } catch (Exception e) {
                throw new PersistenceException(e);
            }
        }
    }

    public void create(@Nonnull Configuration configuration,
                       @Nonnull Principal user)
    throws PersistenceException {

    }

    public int update(@Nonnull ConfigServerUpdateBatch batch,
                      @Nonnull Principal user) throws PersistenceException {
        try {
            if (batch.size() > 0) {
                Version v = Version.parse(batch.getHeader().getPreVersion());
                try (CuratorFramework client = ZkUtils.getZkClient()) {
                    ApplicationGroup appGroup =
                            configDAO.readApplicationGroup(client, batch.getHeader()
                                                                        .getGroup());
                    if (appGroup == null) {
                        throw new PersistenceException(
                                String.format(
                                        "Application Group not found. [group=%s]",
                                        batch.getHeader().getGroup()));
                    }
                    Application app =
                            configDAO
                                    .readApplication(client, appGroup,
                                                     batch.getHeader()
                                                          .getApplication());
                    if (app == null) {
                        throw new PersistenceException(
                                String.format(
                                        "Application not found. [application=%s]",
                                        batch.getHeader().getApplication()));
                    }
                    PersistedConfigNode configNode =
                            configDAO.readConfigHeader(client, app,
                                                       batch.getHeader()
                                                            .getConfigName(), v);
                    if (configNode == null) {
                        throw new PersistenceException(
                                String.format(
                                        "Configuration not found. [configuration=%s][version=%s]",
                                        batch.getHeader().getConfigName(),
                                        v.toString()));
                    }
                    int updateCount = 0;
                    Version updateVersion = new Version(
                            configNode.getCurrentVersion().getMajorVersion(),
                            configNode.getCurrentVersion().getMajorVersion() + 1);

                    for (ConfigServerUpdateEvent event : batch.getEvents()) {
                        if (event.getGroup().compareTo(appGroup.getName()) != 0) {
                            throw new PersistenceException(String.format(
                                    "Invalid Update Event : Application Group doesn't match. [expected=%s][actual=%s]",
                                    appGroup.getName(), event.getGroup()));
                        }
                        if (event.getApplication().compareTo(app.getName()) != 0) {
                            throw new PersistenceException(String.format(
                                    "Invalid Update Event : Application doesn't match. [expected=%s][actual=%s]",
                                    app.getName(), event.getApplication()));
                        }
                        if (event.getConfigName().compareTo(configNode.getName()) !=
                                0) {
                            throw new PersistenceException(String.format(
                                    "Invalid Update Event : Configuration doesn't match. [expected=%s][actual=%s]",
                                    configNode.getName(), event.getConfigName()));
                        }
                        Version ev = Version.parse(event.getPreVersion());
                        if (!ev.equals(v)) {
                            throw new PersistenceException(String.format(
                                    "Invalid Update Event : Version doesn't match. [expected=%s][actual=%s]",
                                    v.toString(), ev.toString()));
                        }
                        boolean ret = false;
                        switch (event.getEventType()) {
                            case Add:
                                ret =
                                        addConfigNode(client, appGroup, app,
                                                      configNode, updateVersion,
                                                      event, user);
                                if (ret) updateCount++;
                                break;
                            case Update:
                                ret =
                                        updateConfigNode(client, appGroup, app,
                                                         configNode, updateVersion,
                                                         event, user);
                                if (ret) updateCount++;
                                break;
                            case Remove:
                                ret =
                                        deleteConfigNode(client, appGroup, app,
                                                         configNode,
                                                         event, user);
                                if (ret) updateCount++;
                                break;
                        }
                    }
                    if (updateCount > 0) {
                        configNode.setCurrentVersion(updateVersion);
                        configDAO.saveConfigHeader(client, configNode, user);
                    }
                    return updateCount;
                }
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
        return 0;
    }

    public Configuration read(@Nonnull String group, @Nonnull String application,
                              @Nonnull String config,
                              @Nonnull Principal user)
    throws PersistenceException {
        return null;
    }

    private boolean addConfigNode(@Nonnull CuratorFramework client,
                                  @Nonnull ApplicationGroup group,
                                  @Nonnull Application application,
                                  @Nonnull PersistedConfigNode configNode,
                                  @Nonnull Version updateVersion,
                                  @Nonnull ConfigServerUpdateEvent event,
                                  @Nonnull Principal user)
    throws PersistenceException {
        try {
            String zkPath = ZkUtils.getZkPath(configNode, event.getPath());
            Stat stat = client.checkExists().forPath(zkPath);
            if (stat != null) {
                byte[] data = client.getData().forPath(zkPath);
                if (data != null && data.length > 0) {
                    throw new PersistenceException(String.format(
                            "Error Adding Config Node : node already exists. [path=%s]",
                            zkPath));
                }
            } else {
                zkPath = client.create().creatingParentsIfNeeded().forPath(zkPath);
            }

            PersistedConfigPathNode node = null;
            if (event.getValue() instanceof String) {
                node = new PersistedConfigValueNode();
                setupNodeInfo(node, configNode, event, user, updateVersion);
                ((PersistedConfigValueNode) node)
                        .setValue((String) event.getValue());
            } else if (event.getValue() instanceof List) {
                node = new PersistedConfigListValueNode();
                setupNodeInfo(node, configNode, event, user, updateVersion);
                List<?> values = (List<?>) event.getValue();
                for (Object value : values) {
                    if (!(value instanceof String)) {
                        throw new PersistenceException(String.format(
                                "Invalid List Value : [path=%s][type=%s]",
                                event.getPath(),
                                value.getClass().getCanonicalName()));
                    }
                    ((PersistedConfigListValueNode) node).addValue((String) value);
                }
            } else {
                throw new ConfigurationException(
                        String.format("Invalid Node Data : [type=%s]",
                                      event.getValue().getClass()
                                           .getCanonicalName()));
            }
            if (node != null) {
                String json = ZConfigCoreEnv.coreEnv().getJsonMapper()
                                            .writeValueAsString(node);
                client.setData().forPath(zkPath, json.getBytes());
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    private void setupNodeInfo(PersistedConfigPathNode node,
                               PersistedConfigNode configNode,
                               ConfigServerUpdateEvent event, Principal user,
                               Version updateVersion) throws ServiceEnvException {
        try {
            IUniqueIDGenerator idGenerator =
                    ZConfigCoreEnv.coreEnv().getIdGenerator();
            ModifiedBy<String> owner = new ModifiedBy<>(user.getName());
            node.setName(event.getName());
            node.setDescription(event.getDescription());
            node.setParent(configNode);
            node.setNodeVersion(updateVersion);
            node.setId(idGenerator.generateStringId(null));
            node.setOwner(owner);
            node.setUpdated(owner);
        } catch (Exception ex) {
            throw new ServiceEnvException(ex);
        }
    }

    private boolean updateConfigNode(@Nonnull CuratorFramework client,
                                     @Nonnull ApplicationGroup group,
                                     @Nonnull Application application,
                                     @Nonnull PersistedConfigNode configNode,
                                     @Nonnull Version updateVersion,
                                     @Nonnull ConfigServerUpdateEvent event,
                                     @Nonnull Principal user)
    throws PersistenceException {
        try {
            PersistedConfigPathNode node =
                    configDAO.readConfigNode(client, configNode, event.getPath());
            if (node == null) {
                throw new PersistenceException(
                        String.format("Node Not Found : Update failed. [path=%s]",
                                      event.getPath()));
            }
            if (event.getValue() instanceof String) {
                ((PersistedConfigValueNode) node)
                        .setValue((String) event.getValue());
            } else if (event.getValue() instanceof List) {
                List<?> values = (List<?>) event.getValue();
                for (Object value : values) {
                    if (!(value instanceof String)) {
                        throw new PersistenceException(String.format(
                                "Invalid List Value : [path=%s][type=%s]",
                                event.getPath(),
                                value.getClass().getCanonicalName()));
                    }
                    ((PersistedConfigListValueNode) node).addValue((String) value);
                }
            } else {
                throw new ConfigurationException(
                        String.format("Invalid Node Data : [type=%s]",
                                      event.getValue().getClass()
                                           .getCanonicalName()));
            }
            if (node != null) {
                String json = ZConfigCoreEnv.coreEnv().getJsonMapper()
                                            .writeValueAsString(node);
                client.setData().forPath(node.getAbsolutePath(), json.getBytes());
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    private boolean deleteConfigNode(@Nonnull CuratorFramework client,
                                     @Nonnull ApplicationGroup group,
                                     @Nonnull Application application,
                                     @Nonnull PersistedConfigNode configNode,
                                     AbstractConfigUpdateEvent event,
                                     Principal user)
    throws PersistenceException {
        return false;
    }
}
