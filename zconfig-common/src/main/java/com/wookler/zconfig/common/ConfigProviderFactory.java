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
 * Date: 24/1/19 12:17 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common;

import com.wookler.zconfig.common.parsers.AbstractConfigParser;
import com.wookler.zconfig.common.parsers.JSONConfigParser;
import com.wookler.zconfig.common.writers.AbstractConfigWriter;
import com.wookler.zconfig.common.writers.JSONFileConfigWriter;

/**
 * Factory class to provide config parser and config reader instances.
 */
public class ConfigProviderFactory {
    /**
     * Enum to define the configuration type.
     */
    public static enum EConfigType {
        /**
         * JSON configuration file.
         */
        JSON,
        /**
         * XML configuration file.
         */
        XML
    }

    /**
     * Get a new configuration parser instance of the parser for the specified configuration type.
     *
     * @param type - Configuration type.
     * @return - Parser instance.
     */
    public static final AbstractConfigParser parser(EConfigType type) {
        switch (type) {
            case JSON:
                return new JSONConfigParser();
            case XML:
                return null;
            default:
                return null;
        }
    }

    /**
     * Get a new configuration writer instance of the parser for the specified configuration type.
     *
     * @param type - Configuration type.
     * @return - Writer instance.
     */
    public static final AbstractConfigWriter writer(EConfigType type) {
        switch (type) {
            case JSON:
                return new JSONFileConfigWriter();
            case XML:
                return null;
            default:
                return null;
        }
    }
}
