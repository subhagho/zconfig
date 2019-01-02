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
 * Date: 1/1/19 9:06 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common;

/**
 * Class defines static global constants.
 */
public class GlobalConstants {
    /**
     * Default date format to parse/print dates.
     */
    public static final String DEFAULT_DATE_FORMAT = "mm.dd.yyyy";
    /**
     * Default data/time format to parse/print date/time.
     */
    public static final String DEFAULT_DATETIME_FORMAT = String.format("%s HH:MM:SS", DEFAULT_DATE_FORMAT);
}
