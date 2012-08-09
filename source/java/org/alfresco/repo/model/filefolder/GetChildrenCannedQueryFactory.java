/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * GetChidren canned query factory for files and folders. 
 * 
 * @since 4.1.1
 * @author steveglover
 *
 */
public class GetChildrenCannedQueryFactory extends org.alfresco.repo.node.getchildren.GetChildrenCannedQueryFactory
{
    private HiddenAspect hiddenAspect;
    
    public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
		this.hiddenAspect = hiddenAspect;
	}

    @Override
    public CannedQuery<NodeRef> getCannedQuery(CannedQueryParameters parameters)
    {
        NodePropertyHelper nodePropertyHelper = new NodePropertyHelper(dictionaryService, qnameDAO, localeDAO, contentDataDAO);
        
        return (CannedQuery<NodeRef>) new GetChildrenCannedQuery(nodeDAO, qnameDAO, cannedQueryDAO, nodePropertyHelper, tenantService, methodSecurity, parameters, hiddenAspect);
    }
}
