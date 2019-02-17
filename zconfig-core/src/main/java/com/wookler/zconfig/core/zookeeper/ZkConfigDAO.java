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
 * Date: 17/2/19 9:32 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.core.zookeeper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.wookler.zconfig.common.model.*;
import com.wookler.zconfig.common.utils.IUniqueIDGenerator;
import com.wookler.zconfig.core.ServiceEnvException;
import com.wookler.zconfig.core.ZConfigCoreEnv;
import com.wookler.zconfig.core.model.ModifiedBy;
import com.wookler.zconfig.core.model.ZkConfigNode;
import com.wookler.zconfig.core.model.ZkConfigPathNode;
import com.wookler.zconfig.core.model.nodes.ZkConfigValueNode;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

import javax.annotation.Nonnull;
import java.security.Principal;

/**
 * Data Access Object to read/update configuration data from ZooKeeper.
 */
public class ZkConfigDAO {
    public boolean saveConfigNode(@Nonnull CuratorFramework client,
                                  @Nonnull AbstractConfigNode node,
                                  @Nonnull ZkConfigNode configNode,
                                  @Nonnull Principal user,
                                  @Nonnull String changeRequest)
    throws ZkException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(changeRequest));
        try {
            String path = node.getAbsolutePath();
            String zkPath = ZkUtils.getZkPath(configNode, path);
            Stat stat = client.checkExists().forPath(zkPath);
            if (node instanceof ConfigValueNode) {
                return saveValueConfigNode(client, (ConfigValueNode) node,
                                           configNode,
                                           user, changeRequest, zkPath, stat);
            } else if (node instanceof ConfigListValueNode) {
                return saveValueListConfigNode(client, (ConfigListValueNode) node,
                                               configNode,
                                               user, changeRequest, zkPath, stat);
            } else if (node instanceof ConfigParametersNode) {
                return saveParametersConfigNode(client, (ConfigParametersNode) node,
                                                configNode,
                                                user, changeRequest, zkPath, stat);
            } else if (node instanceof ConfigPropertiesNode) {
                return savePropertiesConfigNode(client, (ConfigPropertiesNode) node,
                                                configNode,
                                                user, changeRequest, zkPath, stat);
            }
            return false;
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    private boolean saveValueConfigNode(@Nonnull CuratorFramework client,
                                        @Nonnull ConfigValueNode node,
                                        @Nonnull ZkConfigNode configNode,
                                        @Nonnull Principal user,
                                        @Nonnull String changeRequest,
                                        String zkPath,
                                        Stat stat) throws ZkException {
        try {
            ModifiedBy<String> modifiedBy = new ModifiedBy<>(user.getName());

            ZkConfigValueNode zkNode = null;
            if (stat == null) {
                zkNode = new ZkConfigValueNode();
                setupNewPathNode(zkNode, node, modifiedBy, configNode,
                                 changeRequest);
                zkNode.setValue(node.getValue());
                String path = zkNode.getAbsolutePath();
                path = client.create().creatingParentsIfNeeded().forPath(path);
            } else {
                byte[] data = client.getData().forPath(zkPath);
                if (data == null || data.length <= 0) {
                    zkNode = new ZkConfigValueNode();
                    setupNewPathNode(zkNode, node, modifiedBy, configNode,
                                     changeRequest);
                    zkNode.setValue(node.getValue());
                } else {
                    String json = new String(data);
                    ObjectMapper mapper = ZConfigCoreEnv.get().getJsonMapper();
                    zkNode = mapper.readValue(json, ZkConfigValueNode.class);
                    if (node.getVersion().compareTo(zkNode.getNodeVersion()) != 0) {
                        throw new ZkException(String.format(
                                "Update Failed : Passed node version is stale. [expected=%s][actual=%s]",
                                node.getVersion(), zkNode.getNodeVersion()));
                    }
                    zkNode.setValue(node.getValue());
                    zkNode.setNodeVersion(changeRequest);
                }
            }
            String path = zkNode.getAbsolutePath();
            String json =
                    ZConfigCoreEnv.get().getJsonMapper().writeValueAsString(zkNode);
            client.setData().forPath(path, json.getBytes());

            return true;
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    private void setupNewPathNode(ZkConfigPathNode zkNode,
                                  AbstractConfigNode node, ModifiedBy<String> owner,
                                  ZkConfigNode configNode, String changeRequest)
    throws
    ServiceEnvException {
        zkNode = new ZkConfigValueNode();
        IUniqueIDGenerator idGenerator = ZConfigCoreEnv.get().getIdGenerator();
        zkNode.setId(idGenerator.generateStringId(null));
        zkNode.setName(node.getName());
        zkNode.setDescription(node.getDescription());
        zkNode.setParent(configNode);
        zkNode.setOwner(owner);
        zkNode.setUpdated(owner);
        zkNode.setNodeVersion(changeRequest);
    }

    private boolean saveValueListConfigNode(@Nonnull CuratorFramework client,
                                            @Nonnull ConfigListValueNode node,
                                            @Nonnull ZkConfigNode configNode,
                                            @Nonnull Principal user,
                                            @Nonnull String changeRequest,
                                            String zkPath,
                                            Stat stat) throws ZkException {
        return false;
    }

    private boolean saveParametersConfigNode(@Nonnull CuratorFramework client,
                                             @Nonnull ConfigParametersNode node,
                                             @Nonnull ZkConfigNode configNode,
                                             @Nonnull Principal user,
                                             @Nonnull String changeRequest,
                                             String zkPath,
                                             Stat stat) throws ZkException {
        return false;
    }

    private boolean savePropertiesConfigNode(@Nonnull CuratorFramework client,
                                             @Nonnull ConfigPropertiesNode node,
                                             @Nonnull ZkConfigNode configNode,
                                             @Nonnull Principal user,
                                             @Nonnull String changeRequest,
                                             String zkPath,
                                             Stat stat) throws ZkException {
        return false;
    }
}
