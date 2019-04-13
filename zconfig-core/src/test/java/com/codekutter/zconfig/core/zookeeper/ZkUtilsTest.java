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
 * Date: 16/2/19 12:47 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.core.zookeeper;

import com.codekutter.zconfig.common.LogUtils;
import com.codekutter.zconfig.common.ZConfigCoreEnv;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.utils.ZKPaths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ZkUtilsTest {
    private static final String CONFIG_FILE =
            "src/main/resources/zconfig-server.json";

    @BeforeAll
    static void setup() throws Exception {
        ZConfigCoreEnv.setup(CONFIG_FILE, "0.*");
    }

    @AfterAll
    static void dispose() {
        ZConfigCoreEnv.shutdown();
    }

    @Test
    void getZkClient() {
        try {
            try (CuratorFramework client = ZkUtils.getZkClient()) {
                assertNotNull(client);
                String id = UUID.randomUUID().toString();
                String path =
                        String.format("/_TEST_%s", id);
                LogUtils.debug(getClass(), String.format("Root Path = [%s]",
                                                         ZkUtils.getServerRootPath()));
                String p = ZKPaths.makePath(ZkUtils.getServerRootPath(), path);
                p = client.create().creatingParentsIfNeeded().forPath(p);
                client.setData().forPath(p, id.getBytes());

                LogUtils.debug(getClass(),
                               String.format("Create path response = [%s]", p));
                byte[] data = client.getData().forPath(p);
                assertNotNull(data);
                assertTrue(data.length > 0);
                String nid = new String(data);
                assertEquals(id, nid);
            }
        } catch (Throwable t) {
            LogUtils.error(getClass(), t);
            fail(t.getLocalizedMessage());
        }
    }

    @Test
    void getZkLock() {
        try {
            try (CuratorFramework client = ZkUtils.getZkClient()) {
                assertNotNull(client);
                String id = UUID.randomUUID().toString();
                InterProcessMutex lock = ZkUtils.getZkLock(client, id);
                assertNotNull(lock);
                lock.acquire();
                try {
                    LogUtils.debug(getClass(), "Acquired ZK lock...");
                } finally {
                    lock.release();
                }
            }
        } catch (Throwable t) {
            LogUtils.error(getClass(), t);
            fail(t.getLocalizedMessage());
        }
    }
}