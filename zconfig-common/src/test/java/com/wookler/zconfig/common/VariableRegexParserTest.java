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
 * Date: 1/1/19 11:12 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.wookler.zconfig.common.LogUtils.*;

class VariableRegexParserTest {
    private static final Logger
            LOG = LoggerFactory.getLogger(VariableRegexParserTest.class);

    @Test
    void hasVarable() {
        try {
            String value = "This is a ${test} ${value}";
            assertTrue(VariableRegexParser.hasVarable(value));
        } catch (Throwable t) {
            error(getClass(), t, LOG);
            fail(t);
        }
    }

    @Test
    void getVariables() {
        try {
            String value = "This is a ${test} ${value} which has ${three} variables.";
            List<String> vars = VariableRegexParser.getVariables(value);
            assertNotNull(vars);
            assertTrue(vars.size() == 3);

            for(String var : vars) {
                info(getClass(), String.format("Variable : %s", var));
            }
        } catch (Throwable t) {
            error(getClass(), t, LOG);
            fail(t);
        }
    }
}