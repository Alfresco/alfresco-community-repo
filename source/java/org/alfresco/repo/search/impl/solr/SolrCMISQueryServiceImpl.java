/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.solr;

import org.alfresco.opencmis.search.CMISQueryOptions;
import org.alfresco.opencmis.search.CMISQueryService;
import org.alfresco.opencmis.search.CMISResultSet;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;

/**
 * @author Andy
 *
 */
public class SolrCMISQueryServiceImpl implements CMISQueryService
{

    /* (non-Javadoc)
     * @see org.alfresco.opencmis.search.CMISQueryService#query(org.alfresco.opencmis.search.CMISQueryOptions)
     */
    @Override
    public CMISResultSet query(CMISQueryOptions options)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.opencmis.search.CMISQueryService#query(java.lang.String, org.alfresco.service.cmr.repository.StoreRef)
     */
    @Override
    public CMISResultSet query(String query, StoreRef storeRef)
    {
        CMISQueryOptions options = new CMISQueryOptions(query, storeRef);
        return query(options);
    }

    public boolean getPwcSearchable()
    {
        return true;
    }

    public boolean getAllVersionsSearchable()
    {
        return false;
    }

    public CapabilityQuery getQuerySupport()
    {
        return CapabilityQuery.BOTHCOMBINED;
    }

    public CapabilityJoin getJoinSupport()
    {
        return CapabilityJoin.NONE;
    }

}
