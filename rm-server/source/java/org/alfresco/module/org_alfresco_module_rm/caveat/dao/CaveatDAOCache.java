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

import java.util.Map;

/**
 * A cache that ensures the underlying caveat DAO is only executed once per query.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatDAOCache implements CaveatDAOInterface
{
    /** The wrapped caveat DAO. */
    private CaveatDAOInterface caveatDAO;
    /** A cache of the system caveat groups. */
    private ImmutableMap<String, CaveatGroup> caveatGroups;

    /**
     * {@inheritDoc} The first call to this method will be cached and returned for every successive call.
     */
    @Override
    public ImmutableMap<String, CaveatGroup> getCaveatGroups()
    {
        ensureCachePopulated();
        return caveatGroups;
    }

    /**
     * {@inheritDoc} The first call to this method will populate the cache for future calls.
     */
    @Override
    public CaveatGroup getGroupById(String groupId) throws CaveatGroupNotFound
    {
        ensureCachePopulated();
        CaveatGroup caveatGroup = caveatGroups.get(groupId);
        if (caveatGroup == null)
        {
            throw new CaveatGroupNotFound(groupId);
        }
        return caveatGroup;
    }

    @Override public QName getCaveatGroupProperty(String caveatGroupId)
    {
        ensureCachePopulated();

        return getGroupById(caveatGroupId).getModelProperty();
    }

    @Override public CaveatGroup getCaveatGroupFromProperty(QName propertyName)
    {
        ensureCachePopulated();

        CaveatGroup matchingGroup = null;
        for (Map.Entry<String, CaveatGroup> entry : caveatGroups.entrySet())
        {
            final CaveatGroup potentialMatch = entry.getValue();
            if (propertyName.equals(getCaveatGroupProperty(potentialMatch.getId())))
            {
                matchingGroup = potentialMatch;
                break;
            }
        }
        if (matchingGroup == null)
        {
            throw new CaveatGroupNotFound("No group found for property '" + propertyName + "'");
        }
        else
        {
            return matchingGroup;
        }
    }

    /** The first call to this method will populate the cache, subsequent calls will do nothing. */
    private void ensureCachePopulated()
    {
        if (caveatGroups == null)
        {
            caveatGroups = caveatDAO.getCaveatGroups();
        }
    }

    /**
     * Set the caveat DAO to be wrapped.
     *
     * @param caveatDAO The caveat DAO to be wrapped.
     */
    public void setCaveatDAO(CaveatDAOInterface caveatDAO)
    {
        this.caveatDAO = caveatDAO;
    }
}
