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

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.caveat.scheme.CaveatGroup;

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
    private Map<String, CaveatGroup> caveatGroups;

    /**
     * {@inheritDoc} The first call to this method will be cached and returned for every successive call.
     */
    @Override
    public Map<String, CaveatGroup> getCaveatGroups()
    {
        if (caveatGroups == null)
        {
            caveatGroups = caveatDAO.getCaveatGroups();
        }
        return caveatGroups;
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
