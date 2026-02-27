/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.permission;

import org.opensearch.client.opensearch._types.query_dsl.Query;

/**
 * This interface will create a QueryBuilder object including permissions for the current user.
 */
public interface ElasticsearchPermissionQueryFactory
{
    /**
     * 
     * @param query
     *            the query to filter using permissions
     * @param includeGroupsForRoleAdmin
     *            define if groups belongs to an admin should be used or not.
     * @return
     */
    Query getQueryWithPermissionFilter(Query query, boolean includeGroupsForRoleAdmin);

}
