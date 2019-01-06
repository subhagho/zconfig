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
 * Date: 5/1/19 6:20 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.wookler.zconfig.common.parsers.JSONConfigParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.Properties;

import static com.wookler.zconfig.common.LogUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {
    private static final String JSON_FILE =
            "src/test/resources/test-config.properties";
    private static Configuration configuration = null;

    @BeforeAll
    static void init() throws Exception {
        JSONConfigParser parser = new JSONConfigParser();
        Properties properties = new Properties();
        properties.load(new FileInputStream(JSON_FILE));

        parser.parse("test-config", properties);
        configuration = parser.getConfiguration();
        assertNotNull(configuration);

        debug(ConfigurationTest.class, configuration);
    }

    @Test
    void find() {
        try {
            String path = "configuration.node_1.TEST_ELEMENT_LIST";
            AbstractConfigNode node = configuration.find(path);
            assertNotNull(node);
            assertTrue(node instanceof ConfigListElementNode);
            assertEquals(4, ((ConfigListElementNode) node).size());
        } catch (Throwable e) {
            error(getClass(), e);
            fail(e);
        }
    }

    @Test
    void findForConfigNode() {
        try {

        } catch (Throwable e) {
            error(getClass(), e);
            fail(e);
        }
    }
}