/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.virtual.template;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.SearchParameters;

public class BasicConstraint implements VirtualQueryConstraint
{
    public static final BasicConstraint INSTANCE = new BasicConstraint();

    private BasicConstraint()
    {

    }

    @Override
    public SearchParameters apply(ActualEnvironment environment, VirtualQuery query) throws VirtualizationException
    {
        SearchParameters searchParameters = new SearchParameters();

        String storeRefString = query.getStoreRef();
        if (storeRefString != null)
        {
            searchParameters.addStore(new StoreRef(storeRefString));
        }
        else
        {
            searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        }

        searchParameters.setLanguage(query.getLanguage());
        searchParameters.setQuery(query.getQueryString());
        searchParameters.setQueryConsistency(QueryConsistency.TRANSACTIONAL_IF_POSSIBLE);

        return searchParameters;
    }

}
