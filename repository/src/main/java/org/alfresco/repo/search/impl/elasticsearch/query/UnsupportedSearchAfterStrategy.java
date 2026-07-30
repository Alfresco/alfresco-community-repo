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
package org.alfresco.repo.search.impl.elasticsearch.query;

import org.opensearch.client.opensearch._types.query_dsl.Query;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Default implementation used when the Enterprise search_after strategy is not available.
 *
 * <p>
 * Community Edition does not support search_after deep pagination. Any request using {@code searchAfterToken} results in an explicit exception instead of silently falling back to offset paging.
 */
public class UnsupportedSearchAfterStrategy extends SearchExecutionStrategy
{
    @Override
    public ResultSet executeSearch(SearchParameters searchParameters, Query queryWithPermissions)
    {
        throw new UnsupportedSearchAfterException();
    }
}
