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

package com.wookler.zconfig.core.zookeeper;

import com.wookler.zconfig.common.LogUtils;
import com.wookler.zconfig.common.model.Version;
import com.wookler.zconfig.core.ZConfigCoreEnv;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
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
            CuratorFramework client = ZkUtils.getZkClient();
            assertNotNull(client);
            String id = UUID.randomUUID().toString();
            String path = String.format("%s/_TEST_%s", ZkUtils.getServerRootPath(),
                                        id);
            String p = client.create().forPath(path, id.getBytes());
            LogUtils.debug(getClass(),
                           String.format("Create path response = [%s]", p));
            byte[] data = client.getData().forPath(path);
            assertNotNull(data);
            assertTrue(data.length > 0);
            String nid = new String(data);
            assertEquals(id, nid);
        } catch (Throwable t) {
            LogUtils.error(getClass(), t);
            fail(t.getLocalizedMessage());
        }
    }
}