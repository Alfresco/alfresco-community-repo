/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.opencmis.search;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;

public class CMISQueryOptionsTest
{
    @Test
    public void shouldTransferCMISFilterToAFTSFilterAndBack()
    {
        final CMISQueryOptions originCMISQueryOptions = new CMISQueryOptions("SELECT * FROM cmis:document WHERE CONTAINS('*')", StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        originCMISQueryOptions.setQueryFilter(CMISQueryOptions.CMISQueryFilter.propertyEquality("propertyName", "propertyValue"));

        final SearchParameters searchParameters = originCMISQueryOptions.getAsSearchParmeters();
        assertEquals(1, searchParameters.getFilterQueries().size());
        assertEquals("{!afts}=propertyName:\"propertyValue\"", searchParameters.getFilterQueries().getFirst());

        final CMISQueryOptions derivedCMISQueryOptions = CMISQueryOptions.create(searchParameters);
        assertEquals(originCMISQueryOptions.getQueryFilter(), derivedCMISQueryOptions.getQueryFilter());
    }

    @Test
    public void shouldEscapePropertyValue()
    {
        final CMISQueryOptions queryOptions = new CMISQueryOptions("SELECT * FROM cmis:document WHERE CONTAINS('*')", StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        queryOptions.setQueryFilter(CMISQueryOptions.CMISQueryFilter.propertyEquality("propertyName", "property\\Va\"lue\""));

        final SearchParameters searchParameters = queryOptions.getAsSearchParmeters();
        assertEquals(1, searchParameters.getFilterQueries().size());
        assertEquals("{!afts}=propertyName:\"property\\\\Va\\\"lue\\\"\"", searchParameters.getFilterQueries().getFirst());
    }

}
