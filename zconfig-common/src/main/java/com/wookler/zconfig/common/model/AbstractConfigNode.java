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
 * Date: 31/12/18 7:12 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.joda.time.DateTime;

/**
 * Abstract base node for defining configuration elements.
 *
 */
public abstract class AbstractConfigNode {
    /**
     * Parent element of this configuration node.
     *
     * Note: Should be null only for the root configuration element.
     */
    private AbstractConfigNode parent = null;
    /**
     * Name of this configuration node.
     *
     * Note: Name must be unique for a path.
     */
    private String name;

    /**
     * Represents the state of this configuration node.
     */
    private NodeState state;

    /**
     * Timestamp this node was created.
     */
    private DateTime createTimeStamp;
    /**
     * Timestamp this node was last updated.
     */
    private DateTime updateTimeStamp;

    /**
     * Incremented version indicator.
     */
    private long nodeVersion = 0;

    /**
     * Get the name of this configuration node.
     *
     * @return - Node name.
     */
    public String getName() {
        return name;
    }

    /**
     * Default constructor - Will initialize the update indicators.
     */
    public AbstractConfigNode() {
        this.createTimeStamp = DateTime.now();
        this.updateTimeStamp = DateTime.now();
        this.nodeVersion = 0;
    }

    /**
     * Set the name of this configuration node.
     *
     * @param name - Configuration node name.
     */
    public void setName(String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        if (name.indexOf('.') >=0 || name.indexOf('/') >= 0) {
            throw new RuntimeException("Invalid name string. Name cannot contain (.) or (/)");
        }
        this.name = name;
    }

    /**
     * Get the parent node for this configuration node.
     *
     * @return - Parent path node or NULL if root node.
     */
    public AbstractConfigNode getParent() {
        return parent;
    }

    /**
     * Set the parent node for this configuration node.
     *
     * @param parent - Parent path node.
     */
    public void setParent(AbstractConfigNode parent) {
        this.parent = parent;
    }

    /**
     * Get the state handle for this node.
     *
     * @return - Node state handle.
     */
    public NodeState getState() {
        return state;
    }

    /**
     * Set the state handle for this node.
     *
     * @param state - Node state handle.
     */
    public void setState(NodeState state) {
        this.state = state;
    }

    /**
     * Get the timestamp this node was created.
     *
     * @return - Created timestamp.
     */
    public DateTime getCreateTimeStamp() {
        return createTimeStamp;
    }

    /**
     * Set the timestamp this node was created.
     *
     * @param createTimeStamp - Created timestamp.
     */
    public void setCreateTimeStamp(DateTime createTimeStamp) {
        Preconditions.checkArgument(createTimeStamp != null);

        this.createTimeStamp = createTimeStamp;
    }

    /**
     * Get the last updated timestamp of this node.
     *
     * @return - Last updated timestamp.
     */
    public DateTime getUpdateTimeStamp() {
        return updateTimeStamp;
    }

    /**
     * Set the last updated timestamp for this node.
     *
     * @param updateTimeStamp - Last updated timestamp
     */
    public void setUpdateTimeStamp(DateTime updateTimeStamp) {
        Preconditions.checkArgument(updateTimeStamp != null);

        this.updateTimeStamp = updateTimeStamp;
    }

    /**
     * Get the record version of this node.
     *
     * @return - Record version.
     */
    public long getNodeVersion() {
        return nodeVersion;
    }

    /**
     * Set the record version of this node.
     *
     * @param nodeVersion - Record version
     */
    public void setNodeVersion(long nodeVersion) {
        Preconditions.checkArgument(nodeVersion >= 0);
        this.nodeVersion = nodeVersion;
    }

    /**
     * Get the path of this node relative to the root node. Path is represented in the Unix
     * path format.
     *
     * @return - Path string.
     */
    public String getAbsolutePath() {
        String path = null;
        if (parent != null) {
            path = parent.getAbsolutePath();
            path = String.format("%s/%s", path, name);
        } else {
            path = String.format("/%s", name);
        }
        return path;
    }

    /**
     * Override default toString().
     *
     * @return - Node name.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Indicate this node has been updated.
     */
    protected void updated() {
        if (state.isSynced()) {
            this.nodeVersion++;
            this.updateTimeStamp = DateTime.now();
            this.state.setState(ENodeState.Updated);
        }
    }

    /**
     * Mark this node as deleted.
     */
    protected void deleted() {
        if (state.isSynced()) {
            this.nodeVersion++;
            this.updateTimeStamp = DateTime.now();
            this.state.setState(ENodeState.Deleted);
        } else if (state.isUpdated()) {
            this.state.setState(ENodeState.Deleted);
        }
    }

    /**
     * Mark this node is being loaded.
     */
    public void loading() {
        if (state == null) {
            state = new NodeState();
        }
        state.setState(ENodeState.Loading);
    }

    /**
     * Mark this node has been synchronized.
     */
    public void synced() {
        Preconditions.checkNotNull(state);
        state.setState(ENodeState.Synced);
    }
}
