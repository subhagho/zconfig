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
 * Date: 16/2/19 12:14 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.core.zookeeper;

import com.google.common.base.Strings;
import com.wookler.zconfig.common.LogUtils;
import com.wookler.zconfig.core.ZConfigCoreEnv;
import com.wookler.zconfig.core.ZConfigCoreInstance;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

/**
 * Helper class for ZooKeeper.
 */
public class ZkUtils {
    private static final String ZK_LOCK_PATH = "__LOCKS__";

    /**
     * Get a new instance of the Curator Client. Method will start() the client.
     *
     * @return - Curator Framework Client.
     * @throws ZkException
     */
    public static final CuratorFramework getZkClient() throws ZkException {
        try {
            ZkConnectionConfig config =
                    ZConfigCoreEnv.get().getZkConnectionConfig();
            if (config == null) {
                throw new ZkException(
                        "ZooKeeper Connection configuration not set.");
            }
            RetryPolicy retryPolicy = null;
            if (!Strings.isNullOrEmpty(config.getConnectionString())) {
                Class<?> type = Class.forName(config.getConnectionString());
                Object obj = type.newInstance();
                if (!(obj instanceof RetryPolicy)) {
                    throw new ZkException(
                            String.format("Invalid Retry Policy : [class=%s]",
                                          type.getCanonicalName()));
                }
                retryPolicy = (RetryPolicy) obj;
            }
            CuratorFramework client = CuratorFrameworkFactory
                    .newClient(config.getConnectionString(), retryPolicy);
            client.start();
            return client;
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    /**
     * Get the ZooKeeper root path for this server.
     *
     * @return - ZooKeeper root path.
     * @throws ZkException
     */
    public static final String getServerRootPath() throws ZkException {
        try {
            ZkConnectionConfig config =
                    ZConfigCoreEnv.get().getZkConnectionConfig();
            if (config == null) {
                throw new ZkException(
                        "ZooKeeper Connection configuration not set.");
            }
            String rp = config.getRootPath();
            ZConfigCoreInstance instance = ZConfigCoreEnv.get().getInstance();
            return String.format("%s/%s", rp, instance.getName());
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }

    /**
     * Create a new instance of a distributed ZooKeeper lock with the specified name.
     *
     * @param client - Curator Framework client handle.
     * @param name   - Lock name.
     * @return - Lock instance.
     * @throws ZkException
     */
    public static final InterProcessMutex getZkLock(CuratorFramework client,
                                                    String name)
    throws ZkException {
        String path =
                String.format("%s/%s/%s", getServerRootPath(), ZK_LOCK_PATH, name);

        LogUtils.debug(ZkUtils.class,
                       String.format("Getting ZK Lock : [lock=%s]...", path));

        return new InterProcessMutex(client, path);
    }
}
