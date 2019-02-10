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
 * Date: 10/2/19 8:17 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.client.rmq;

import com.wookler.zconfig.client.ZConfigClientEnv;
import com.wookler.zconfig.common.LogUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RMQConnectionFactoryTest {
    private static final String CONFIG_FILE =
            "src/main/resources/zconfig-client.json";
    private static final String CONFIG_VERSION = "0.0";

    @BeforeAll
    static void setup() throws Exception {
        ZConfigClientEnv.setup(CONFIG_FILE, CONFIG_VERSION);
    }

    @AfterAll
    static void dispose() throws Exception {
        ZConfigClientEnv.shutdown();
    }

    @Test
    void open() {
        try {
            RMQConnectionFactory factory = new RMQConnectionFactory();
            factory.configure(ZConfigClientEnv.get().getConfiguration());
            factory.open("guest", "guest");
        } catch (Exception e) {
            LogUtils.error(getClass(), e);
            fail(e.getLocalizedMessage());
        }
    }
}