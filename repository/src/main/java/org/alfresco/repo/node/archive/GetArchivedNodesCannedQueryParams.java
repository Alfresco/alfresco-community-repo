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

package org.alfresco.repo.node.archive;

/**
 * Parameter object for {@link GetArchivedNodesCannedQuery}.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 4.2
 */
public class GetArchivedNodesCannedQueryParams extends ArchivedNodeEntity
{

    private int limit;

    /**
     *
     * @param parentNodeId Long
     * @param assocTypeQNameId Long
     * @param filter String
     * @param filterIgnoreCase Boolean
     * @param nameQNameId Long
     * @param sortOrderAscending Boolean
     */
    public GetArchivedNodesCannedQueryParams(Long parentNodeId, Long assocTypeQNameId,
                String filter, Boolean filterIgnoreCase, Long nameQNameId,
                Boolean sortOrderAscending)
    {
        super(parentNodeId, nameQNameId, filter, assocTypeQNameId, sortOrderAscending,
                    filterIgnoreCase);
    }

    /**
     * @param parentNodeId
     * @param assocTypeQNameId
     * @param filter
     * @param filterIgnoreCase
     * @param nameQNameId
     * @param sortOrderAscending
     * @param limit
     */
    public GetArchivedNodesCannedQueryParams(Long parentNodeId, Long assocTypeQNameId,
            String filter, Boolean filterIgnoreCase, Long nameQNameId, Boolean sortOrderAscending,
            int limit)
    {
        this(parentNodeId, assocTypeQNameId, filter, filterIgnoreCase, nameQNameId,
                sortOrderAscending);
        this.setLimit(limit);
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

}
