/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.copy.query;

import java.util.List;

import org.alfresco.query.AbstractCannedQuery;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CopyService.CopyInfo;

/**
 * Factory producing queries for the {@link CopyService}
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class GetCopiedCannedQueryFactory extends AbstractCopyCannedQueryFactory<CopyInfo>
{
    @Override
    public CannedQuery<CopyInfo> getCannedQuery(CannedQueryParameters parameters)
    {
        throw new UnsupportedOperationException();
    }
    
    private class GetCopiedCannedQuery extends AbstractCannedQuery<CopyInfo>
    {
        private GetCopiedCannedQuery(CannedQueryParameters parameters)
        {
            super(parameters);
        }
        
        @Override
        protected List<CopyInfo> queryAndFilter(CannedQueryParameters parameters)
        {
            throw new UnsupportedOperationException();
        }
    }
}