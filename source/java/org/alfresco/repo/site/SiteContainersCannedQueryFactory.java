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
package org.alfresco.repo.site;

import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * A canned query factory for constructing canned queries to fetch site containers.
 * 
 * @author steveglover
 *
 */
public class SiteContainersCannedQueryFactory extends AbstractCannedQueryFactory<FileInfo>
{
	private FileFolderService fileFolderService;
	private NodeService nodeService;
    private MethodSecurityBean<FileInfo> methodSecurity;

	public void setMethodSecurity(MethodSecurityBean<FileInfo> methodSecurity)
	{
		this.methodSecurity = methodSecurity;
	}

	public void setFileFolderService(FileFolderService fileFolderService)
	{
		this.fileFolderService = fileFolderService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	@Override
    public CannedQuery<FileInfo> getCannedQuery(CannedQueryParameters parameters)
    {
    	Object parameterBean = parameters.getParameterBean();
    	CannedQuery<FileInfo> cq = null;
    	if(parameterBean instanceof SiteContainersCannedQueryParams)
    	{
    		cq = new SiteContainersCannedQuery(fileFolderService, nodeService, parameters, methodSecurity);
    	}
        return (CannedQuery<FileInfo>)cq;
    }

}
