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
 * Date: 15/2/19 6:24 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.codekutter.zconfig.core.model;

import com.codekutter.zconfig.core.utils.EntityUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.codekutter.zconfig.common.Context;
import com.codekutter.zconfig.common.utils.IUniqueIDGenerator;
import org.springframework.lang.NonNull;

import javax.annotation.Nonnull;

/**
 * Class represents an instance of an application definition.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY,
              property = "@class")
public class Application extends BaseEntity<String, Application>
        implements IZkNode {
    /**
     * Name of this application group.
     */
    private String name;
    /**
     * Description of this application group.
     */
    private String description;

    /**
     * Application Group this application belongs to.
     */
    private ApplicationGroup group;
    /**
     * State of this application instance.
     */
    private EPersistedNodeState state;

    /**
     * Get the Application Group name.
     *
     * @return - Application Group name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the Application Group name. Application group names
     * are expected to be unique per instance.
     *
     * @param name - Application Group name.
     */
    public void setName(@NonNull String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        this.name = name;
    }

    /**
     * Get the description of this Application Group.
     *
     * @return - Application Group description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of this Application Group.
     *
     * @param description - Application Group description
     */
    public void setDescription(@NonNull String description) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(description));

        this.description = description;
    }

    /**
     * Get the Application Group.
     *
     * @return - Application Group.
     */
    public ApplicationGroup getGroup() {
        return group;
    }

    /**
     * Set the Application Group.
     *
     * @param group - Application Group.
     */
    public void setGroup(ApplicationGroup group) {
        this.group = group;
    }

    /**
     * Get the state of this application instance.
     *
     * @return - Application instance state.
     */
    public EPersistedNodeState getState() {
        return state;
    }

    /**
     * Set the state of this application instance.
     *
     * @param state - Application instance state.
     */
    public void setState(@Nonnull EPersistedNodeState state) {
        this.state = state;
    }

    /**
     * Compare this entity instance's key with the passed source.
     *
     * @param source - Source instance to compare with.
     * @return - (<0 key < source.key) (0 key == source.key) (>0 key > source.key)
     */
    @Override
    public int compareKey(PersistedEntity<String, Application> source) {
        return getId().compareTo(source.getId());
    }

    /**
     * Get the computed hash code for this entity instance.
     *
     * @return - Hash Code.
     */
    @Override
    public int getHashCode() {
        return EntityUtils.getStringHashCode(getId());
    }

    /**
     * Copy the attribute values from the passed source entity to the current.
     *
     * @param source - Source entity to copy from.
     * @throws EntityException
     */
    @Override
    public void copyChanges(Application source) throws EntityException {
        EntityUtils.copyChanges(source, this);
    }

    /**
     * Provide a custom clone interface for entities to be cloned.
     *
     * @param params - Additional parameters.
     * @return - Cloned entity
     * @throws EntityException
     */
    @Override
    public Application clone(Object... params) throws EntityException {
        if (params != null && params.length > 0) {
            Object obj = params[0];
            if (obj instanceof IUniqueIDGenerator) {
                IUniqueIDGenerator idgen = (IUniqueIDGenerator) obj;
                Context ctx = null;
                if (params.length > 1) {
                    if (params[1] instanceof Context) {
                        ctx = (Context) params[1];
                    }
                }
                String id = idgen.generateStringId(ctx);
                Preconditions.checkState(!Strings.isNullOrEmpty(id));
                Application app = new Application();
                app.setId(id);
                app.copyChanges(this);

                return app;
            } else {
                throw new EntityException(
                        "Invalid argument : Expected arg[1] to be Unique ID Generator.");
            }
        } else {
            throw new EntityException(
                    "Missing mandatory argument : Unique ID Generator not specified.");
        }
    }

    /**
     * Get the path name of this node.
     *
     * @return - Path node name.
     */
    @Override
    @JsonIgnore
    public String getPath() {
        return name;
    }

    /**
     * Get the absolute path of this node element.
     *
     * @return - Absolute Path.
     */
    @Override
    @JsonIgnore
    public String getAbsolutePath() {
        if (group != null && !Strings.isNullOrEmpty(name)) {
            return String.format("%s/%s", group.getAbsolutePath(), getPath());
        }
        return null;
    }
}
