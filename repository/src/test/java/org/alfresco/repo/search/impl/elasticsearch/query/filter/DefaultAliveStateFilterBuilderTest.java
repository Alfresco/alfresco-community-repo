/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search.impl.elasticsearch.query.filter;

import static org.junit.Assert.assertEquals;

import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.ALIVE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;

import org.alfresco.service.cmr.search.SearchParameters;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAliveStateFilterBuilderTest
{
    private final DefaultAliveStateFilterBuilder aliveStateFilterBuilder = new DefaultAliveStateFilterBuilder();

    @Mock
    private SearchParameters searchParameters;

    @Test
    public void aliveStateFilter_alwaysReturnsAliveTrue()
    {
        Query filter = aliveStateFilterBuilder.getAliveStateFilter(searchParameters);

        assertEquals("term", filter._kind().jsonValue());
        TermQuery termQuery = filter.term();
        assertEquals(ALIVE, termQuery.field());
        assertEquals("true", termQuery.value()._get().toString());
    }
}
