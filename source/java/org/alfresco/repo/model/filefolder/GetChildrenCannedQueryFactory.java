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
package org.alfresco.repo.model.filefolder;

import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * GetChildren (paged list of FileInfo)
 * 
 * @author janv
 * @since 4.0
 */
public class GetChildrenCannedQueryFactory<FileInfo> extends AbstractCannedQueryFactory<FileInfo>
{
    private NodeService nodeService;
    private MethodSecurityInterceptor methodSecurityInterceptor;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setMethodSecurityInterceptor(MethodSecurityInterceptor methodSecurityInterceptor)
    {
        this.methodSecurityInterceptor = methodSecurityInterceptor;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public CannedQuery<FileInfo> getCannedQuery(CannedQueryParameters parameters)
    {
        String queryExecutionId = super.getQueryExecutionId(parameters);
        return (CannedQuery<FileInfo>) new GetChildrenCannedQuery(nodeService, methodSecurityInterceptor, parameters, queryExecutionId);
    }
}
