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

/**
 * Interface representing the relationship definition
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public interface RelationshipDefinition
{
    /**
     * Gets the unique name of the relationship definition
     *
     * @return The unique name of the relationship definition
     */
    String getUniqueName();

    /**
     * Gets the type of the relationship definition
     *
     * @return The type of the relationship definition
     */
    RelationshipType getType();

    /**
     * Gets the display name of the relationship definition
     *
     * @return The display name of the relationship definition
     */
    RelationshipDisplayName getDisplayName();
}
