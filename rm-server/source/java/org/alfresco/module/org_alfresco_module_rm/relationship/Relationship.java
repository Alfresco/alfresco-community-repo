/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.relationship;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface representing the relationship
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public interface Relationship
{
    /**
     * Gets the unique name of the relationship
     *
     * @return The unique name of the relationship
     */
    String getUniqueName();

    /**
     * Gets the source of the relationship
     *
     * @return The source of the relationship
     */
    NodeRef getSource();

    /**
     * Gets the target of the relationship
     *
     * @return The target of the relationship
     */
    NodeRef getTarget();
}
