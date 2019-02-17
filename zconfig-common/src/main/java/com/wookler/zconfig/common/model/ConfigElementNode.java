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
 * Date: 3/1/19 5:14 PM
 * Subho Ghosh (subho dot ghosh at outlook.com)
 *
 */

package com.wookler.zconfig.common.model;

import com.google.common.base.Preconditions;

public abstract class ConfigElementNode extends AbstractConfigNode {

    /**
     * Node creation info.
     */
    private ModifiedBy createdBy;
    /**
     * Node updation info.
     */
    private ModifiedBy updatedBy;

    /**
     * Get the node creation info.
     *
     * @return - Created By info.
     */
    public ModifiedBy getCreatedBy() {
        return createdBy;
    }

    /**
     * Set the node creation info.
     *
     * @param createdBy - Created timestamp.
     */
    public void setCreatedBy(ModifiedBy createdBy) {
        Preconditions.checkArgument(createdBy != null);

        this.createdBy = createdBy;
    }

    /**
     * Get node last updation info.
     *
     * @return - Last updation info.
     */
    public ModifiedBy getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the last updation info for this node.
     *
     * @param updatedBy - Last updated timestamp
     */
    public void setUpdatedBy(ModifiedBy updatedBy) {
        Preconditions.checkArgument(updatedBy != null);

        this.updatedBy = updatedBy;
    }
}
