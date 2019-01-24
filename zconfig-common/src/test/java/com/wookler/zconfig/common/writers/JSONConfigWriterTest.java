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
 * Date: 5/1/19 11:01 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.writers;

import com.google.common.base.Strings;
import com.wookler.zconfig.common.ConfigProviderFactory;
import com.wookler.zconfig.common.model.Configuration;
import com.wookler.zconfig.common.parsers.JSONConfigParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static com.wookler.zconfig.common.LogUtils.debug;
import static com.wookler.zconfig.common.LogUtils.error;
import static org.junit.jupiter.api.Assertions.*;

class JSONConfigWriterTest {
    private static final String JSON_FILE =
            "src/test/resources/test-config.properties";
    private static final String TEMP_OUTDIR = "/tmp/zconfig/test/output";

    private static Configuration configuration = null;

    @BeforeAll
    static void init() throws Exception {
        File dir = new File(TEMP_OUTDIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new Exception(String.format(
                        "Error creating test output folder. [directory=%s]",
                        dir.getAbsolutePath()));
            }
        }

        JSONConfigParser parser = (JSONConfigParser) ConfigProviderFactory.parser(
                ConfigProviderFactory.EConfigType.JSON);
        assertNotNull(parser);

        Properties properties = new Properties();
        properties.load(new FileInputStream(JSON_FILE));

        parser.parse("test-config", properties);
        configuration = parser.getConfiguration();
        assertNotNull(configuration);

        debug(JSONConfigWriterTest.class, configuration);
    }

    @Test
    void write() {
        try {
            assertNotNull(configuration);

            JSONConfigWriter writer =
                    (JSONConfigWriter) ConfigProviderFactory.writer(
                            ConfigProviderFactory.EConfigType.JSON);
            assertNotNull(writer);

            String outfile = writer.write(configuration, TEMP_OUTDIR);
            assertFalse(Strings.isNullOrEmpty(outfile));

            File outf = new File(outfile);
            assertTrue(outf.exists());
            debug(getClass(), String.format("Created configuration : file=%s",
                                            outf.getAbsolutePath()));
        } catch (Throwable e) {
            error(getClass(), e);
            fail(e);
        }
    }
}