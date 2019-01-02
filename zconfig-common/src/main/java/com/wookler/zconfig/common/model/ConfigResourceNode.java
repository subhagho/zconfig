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
 * Date: 2/1/19 8:02 AM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.google.common.base.Preconditions;

import java.net.URI;

/**
 * Node denotes a resource URI. Resources specified will be downloaded (if required) and made accessible
 * via the configuration handle.
 *
 * XML:
 * <resource name="name" URI="uri" type="type" />
 *
 * JSON:
 * resource : {
 *     name : "name",
 *     URI : "uri",
 *     type : "type",
 *     ...
 * }
 */
public abstract class ConfigResourceNode extends AbstractConfigNode {
    /**
     * Resource type of this node.
     */
    private EResourceType type;
    /**
     * Resource location of the resource pointed to by this node.
     */
    private URI location;

    /**
     * Get the resource type of this node.
     *
     * @return - Resource type.
     */
    public EResourceType getType() {
        return type;
    }

    /**
     * Set the resource type for this node.
     *
     * @param type - Resource type.
     */
    public void setType(EResourceType type) {
        Preconditions.checkArgument(type != null);
        this.type = type;
    }

    /**
     * Get the URI to the location of the resource.
     *
     * @return - Location URI.
     */
    public URI getLocation() {
        return location;
    }

    /**
     * Set the URI to the location of the resource.
     *
     * @param location - Location URI.
     */
    public void setLocation(URI location) {
        Preconditions.checkArgument(location != null);
        this.location = location;
    }

    /**
     * Check if this node if the terminal value specified in the path.
     *
     * @param path  - Tokenized Path array.
     * @param index - Current index in the path array to search for.
     * @return
     */
    @Override
    public AbstractConfigNode find(String[] path, int index) {
        String key = path[index];
        if (getName().compareTo(key) == 0 && (index == path.length - 1)) {
            return this;
        }
        return null;
    }
}
