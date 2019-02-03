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

import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigProviderFactory;
import com.wookler.zconfig.common.ConfigTestConstants;
import com.wookler.zconfig.common.parsers.JSONConfigParser;
import com.wookler.zconfig.common.readers.ConfigFileReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.Properties;

import static com.wookler.zconfig.common.LogUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {
    private static final String BASE_PROPS_FILE =
            "src/test/resources/test-config.properties";
    private static Configuration configuration = null;

    @BeforeAll
    static void init() throws Exception {
        JSONConfigParser parser =
                (JSONConfigParser) ConfigProviderFactory.parser(
                        ConfigProviderFactory.EConfigType.JSON);
        assertNotNull(parser);

        Properties properties = new Properties();
        properties.load(new FileInputStream(BASE_PROPS_FILE));

        String filename = properties.getProperty(
                ConfigTestConstants.PROP_CONFIG_FILE);
        assertFalse(Strings.isNullOrEmpty(filename));
        String vs = properties.getProperty(ConfigTestConstants.PROP_CONFIG_VERSION);
        assertFalse(Strings.isNullOrEmpty(vs));
        Version version = Version.parse(vs);
        assertNotNull(version);

        try (ConfigFileReader reader = new ConfigFileReader(filename)) {

            parser.parse("test-config", reader, version);
            configuration = parser.getConfiguration();
            assertNotNull(configuration);
        }
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
    void update() {
        try {
            String path = "configuration.node_1.node_2.node_3.node_4.TEST_VALUE_LIST";
            AbstractConfigNode node = configuration.find(path);
            assertNotNull(node);
            assertTrue(node instanceof ConfigListValueNode);

            ConfigListValueNode nl = (ConfigListValueNode) node;
            assertEquals(8, nl.size());

        } catch (Throwable e) {
            error(getClass(), e);
            fail(e);
        }
    }

    @Test
    void findProperties() {
        try {
            String path = "configuration.@";
            AbstractConfigNode node = configuration.find(path);
            assertNotNull(node);
            assertTrue(node instanceof ConfigPropertiesNode);

            path = "@PROP_1";
            node = configuration.find(node, path);
            assertTrue(node instanceof ConfigValueNode);
            String param = ((ConfigValueNode) node).getValue();
            assertFalse(Strings.isNullOrEmpty(param));
            debug(getClass(),
                  String.format("[path=%s] property value = %s", path, param));
        } catch (Throwable e) {
            error(getClass(), e);
            fail(e);
        }
    }

    @Test
    void findParameters() {
        try {
            String path = "configuration.node_1.#";
            AbstractConfigNode node = configuration.find(path);
            assertNotNull(node);
            assertTrue(node instanceof ConfigParametersNode);

            path = "#PARAM_1";
            node = configuration.find(node, path);
            assertTrue(node instanceof ConfigValueNode);
            String param = ((ConfigValueNode) node).getValue();
            assertFalse(Strings.isNullOrEmpty(param));
            debug(getClass(),
                  String.format("[path=%s] parameter value = %s", path, param));
        } catch (Throwable e) {
            error(getClass(), e);
            fail(e);
        }
    }

    @Test
    void findForConfigNode() {
        try {
            String path = "configuration.node_1.TEST_ELEMENT_LIST";
            AbstractConfigNode node = configuration.find(path);
            assertNotNull(node);
            assertTrue(node instanceof ConfigListElementNode);
            assertEquals(4, ((ConfigListElementNode) node).size());

            path = "TEST_ELEMENT_LIST.2.string_2";
            node = configuration.find(node, path);
            assertNotNull(node);
            assertTrue(node instanceof ConfigValueNode);
            String value = ((ConfigValueNode) node).getValue();
            assertFalse(Strings.isNullOrEmpty(value));
            debug(getClass(), String.format("[path=%s][value=%s]", path, value));
        } catch (Throwable e) {
            error(getClass(), e);
            fail(e);
        }
    }
}