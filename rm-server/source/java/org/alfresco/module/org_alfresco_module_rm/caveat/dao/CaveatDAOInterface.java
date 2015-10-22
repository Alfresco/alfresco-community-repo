/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.module.org_alfresco_module_rm.caveat.dao;

import com.google.common.collect.ImmutableMap;
import org.alfresco.module.org_alfresco_module_rm.caveat.CaveatException.CaveatGroupNotFound;
import org.alfresco.module.org_alfresco_module_rm.caveat.scheme.CaveatGroup;
import org.alfresco.service.namespace.QName;

/**
 * An object responsible for providing access to the configured caveat groups and marks.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public interface CaveatDAOInterface
{
    /**
     * Gets a map of all the available caveat groups keyed by id.
     */
    ImmutableMap<String, CaveatGroup> getCaveatGroups();

    /**
     * Gets the caveat group for a given id.
     *
     * @param groupId The group id to look up.
     * @return The caveat group.
     * @throws CaveatGroupNotFound if the caveat group is not found.
     */
    CaveatGroup getGroupById(String groupId) throws CaveatGroupNotFound;

    /**
     * Gets the property that relates to a {@link CaveatGroup}.
     *
     * @param caveatGroupId
     * @return
     * @throws CaveatGroupNotFound if a matching {@link CaveatGroup} could not be found.
     */
    QName getCaveatGroupProperty(String caveatGroupId);

    /**
     * Gets the {@link CaveatGroup} that relates to a property.
     *
     * @return the matching {@link CaveatGroup} if there is one.
     * @throws CaveatGroupNotFound if there was no matching group.
     */
    CaveatGroup getCaveatGroupFromProperty(QName propertyName);
}
