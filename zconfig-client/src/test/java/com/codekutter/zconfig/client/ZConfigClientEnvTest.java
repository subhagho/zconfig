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
 * Date: 4/3/19 5:48 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.client;

import com.codekutter.zconfig.common.ConfigProviderFactory;
import com.codekutter.zconfig.common.LogUtils;
import com.codekutter.zconfig.common.ZConfigClientEnv;
import com.codekutter.zconfig.common.model.Configuration;
import com.codekutter.zconfig.common.model.Version;
import com.codekutter.zconfig.common.utils.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ZConfigClientEnvTest {
    private static final String CONFIG_FILE =
            "src/main/resources/zconfig-client.json";
    private static final String CONFIG_VERSION = "0.*";
    private static final String CONFIG_NAME = "zconfig-client";
    private static final String CONFIG_BASE_PATH = "/tmp/zconfig/configs";

    @Test
    void postInit() {
        try {
            ZConfigClientEnv.setup(CONFIG_FILE, CONFIG_VERSION);
        } catch (Throwable t) {
            LogUtils.error(getClass(), t);
            fail(t.getLocalizedMessage());
        }
    }

    @Test
    void getConfiguration() {
        try {
            ZConfigClientEnv.setup(CONFIG_FILE, CONFIG_VERSION);
            File dir = new File(CONFIG_BASE_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File source = new File(CONFIG_FILE);
            String path = ZConfigClientEnv.clientEnv()
                                          .getRelativeConfigurationPath(CONFIG_NAME,
                                                                        Version.parse(
                                                                                CONFIG_VERSION),
                                                                        ConfigProviderFactory.EConfigType.JSON);
            File destDir =
                    new File(String.format("%s/%s", dir.getAbsolutePath(), path));
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            IOUtils.copyFile(source, destDir);
            Configuration configuration = ZConfigClientEnv.clientEnv().getConfiguration(
                    CONFIG_NAME, Version.parse(
                            CONFIG_VERSION),
                    ConfigProviderFactory.EConfigType.JSON);
            assertNotNull(configuration);
            LogUtils.debug(getClass(), configuration);
        } catch (Throwable t) {
            LogUtils.error(getClass(), t);
            fail(t.getLocalizedMessage());
        }
    }
}