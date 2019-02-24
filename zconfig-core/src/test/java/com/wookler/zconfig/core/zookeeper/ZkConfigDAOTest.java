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
 * Date: 17/2/19 6:09 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.core.zookeeper;

import com.wookler.zconfig.common.ConfigProviderFactory;
import com.wookler.zconfig.common.ConfigurationException;
import com.wookler.zconfig.common.LogUtils;
import com.wookler.zconfig.common.model.Configuration;
import com.wookler.zconfig.common.model.Version;
import com.wookler.zconfig.common.parsers.AbstractConfigParser;
import com.wookler.zconfig.common.utils.ResourceReaderUtils;
import com.wookler.zconfig.core.IConfigDAO;
import com.wookler.zconfig.core.ZConfigCoreEnv;
import com.wookler.zconfig.core.model.Application;
import com.wookler.zconfig.core.model.ApplicationGroup;
import com.wookler.zconfig.core.model.EPersistedNodeState;
import com.wookler.zconfig.core.model.PersistedConfigNode;
import com.wookler.zconfig.core.test.TestUser;
import org.apache.curator.framework.CuratorFramework;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ZkConfigDAOTest {
    private static final String CONFIG_FILE =
            "src/main/resources/zconfig-server.json";
    private static final String TEST_CONFIG_FILE =
            "src/test/resources/test-config.json";
    private static final String TEST_TEXT_FILE =
            "random-text.txt";
    private static final String TEST_CONFIG_NAME = "test-config";

    @BeforeAll
    static void setup() throws Exception {
        ZConfigCoreEnv.setup(CONFIG_FILE, "0.*");
    }

    @AfterAll
    static void dispose() {
        ZConfigCoreEnv.shutdown();
    }

    @Test
    void saveConfigNode() {
    }

    @Test
    void readZkConfigNode() {
    }

    @Test
    void deleteZkConfigNode() {
    }

    @Test
    void getChildren() {
    }

    @Test
    void saveConfigHeader() {
        try {
            TestUser user = new TestUser();
            user.setName("TEST_" + UUID.randomUUID().toString());

            try (CuratorFramework client = ZkUtils.getZkClient()) {
                IConfigDAO dao = new ZkConfigDAO();

                Configuration configuration = readConfiguration();
                assertNotNull(configuration);

                ApplicationGroup group = dao.readApplicationGroup(client,
                                                                  configuration
                                                                          .getApplicationGroup());
                if (group == null) {
                    group = new ApplicationGroup();
                    group.setId(UUID.randomUUID().toString());
                    group.setName(configuration.getApplicationGroup());
                    group.setDescription(
                            ResourceReaderUtils.readResourceAsText(TEST_TEXT_FILE));
                    group.setChannelName(group.getName());
                    for (int ii = 0; ii < 10; ii++) {
                        group.addProperty(String.format("PROP_NAME_%d", ii),
                                          String.format("PROP_VALUE_%d", ii));
                    }
                    dao.saveApplicationGroup(client, group, user);
                }
                Application application = dao.readApplication(client, group,
                                                              configuration
                                                                      .getApplication());
                if (application == null) {
                    application = new Application();
                    application.setId(UUID.randomUUID().toString());
                    application.setName(configuration.getApplication());
                    application.setDescription(
                            ResourceReaderUtils.readResourceAsText(TEST_TEXT_FILE));
                    application.setGroup(group);
                    application.setState(EPersistedNodeState.Available);

                    dao.saveApplication(client, application, user);
                }

                PersistedConfigNode configNode =
                        dao.saveConfigHeader(client, configuration, new Version(
                                                     configuration.getVersion().getMajorVersion(),
                                                     configuration.getVersion().getMinorVersion() + 1),
                                             user);

                assertNotNull(configNode);

                configNode = dao.readConfigHeader(client, application,
                                                  configuration.getName(),
                                                  configNode.getCurrentVersion());
                assertNotNull(configNode);
                LogUtils.debug(getClass(), configNode);
            }
        } catch (Throwable t) {
            LogUtils.error(getClass(), t);
            fail(t.getLocalizedMessage());
        }
    }

    private static Configuration readConfiguration() throws Exception {
        AbstractConfigParser parser =
                ConfigProviderFactory.parser(TEST_CONFIG_FILE);
        if (parser == null) {
            throw new ConfigurationException(String.format(
                    "Cannot get configuration parser instance. [file=%s]",
                    TEST_CONFIG_FILE));
        }
        LogUtils.info(ZkConfigDAOTest.class, String.format(
                "Reading Test Configuration : With Configuration file [%s]...",
                TEST_CONFIG_FILE));
        Path path = Paths.get(TEST_CONFIG_FILE);
        parser.parse(TEST_CONFIG_NAME, ConfigProviderFactory.reader(path.toUri()),
                     new Version(0, 0));
        Configuration configuration = parser.getConfiguration();
        if (configuration == null) {
            throw new ConfigurationException(String.format(
                    "Error parsing configuration : NULL configuration read. [file=%s]",
                    TEST_CONFIG_FILE));
        }
        return configuration;
    }
}