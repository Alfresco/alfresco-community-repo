/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

package org.alfresco.repo.node.archive;

/**
 * Parameter object for {@link GetArchivedNodesCannedQuery}.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 4.2
 */
public class GetArchivedNodesCannedQueryParams extends ArchivedNodeEntity
{

    /**
     * @param parentNodeId
     * @param assocTypeQNameId
     * @param filter
     * @param filterIgnoreCase
     * @param nameQNameId
     * @param sortOrderAscending
     */
    public GetArchivedNodesCannedQueryParams(Long parentNodeId, Long assocTypeQNameId,
                String filter, Boolean filterIgnoreCase, Long nameQNameId,
                Boolean sortOrderAscending)
    {
        super(parentNodeId, nameQNameId, filter, assocTypeQNameId, sortOrderAscending,
                    filterIgnoreCase);
    }
}
