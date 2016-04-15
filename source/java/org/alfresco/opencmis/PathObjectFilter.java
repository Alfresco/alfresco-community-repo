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
package org.alfresco.opencmis;

import java.util.List;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;

public class PathObjectFilter implements ObjectFilter
{
    private String rootPath;
	private NodeService nodeService;
	private NamespaceService namespaceService;
	
	private List<String> excludedPaths;

	public void setExcludedPaths(List<String> excludedPaths)
	{
		this.excludedPaths = excludedPaths;
	}
	
	/**
     * Sets the root path.
     * 
     * @param path
     *            path within default store
     */
    public void setRootPath(String path)
    {
        rootPath = path;
    }

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService)
	{
		this.namespaceService = namespaceService;
	}

	@Override
	public boolean filter(NodeRef nodeRef)
	{
	    try
	    {
    	    Path path = nodeService.getPath(nodeRef);
    	    String s = path.toPrefixString(this.namespaceService);
    	    return filter(s);
	    }
	    catch(AccessDeniedException e)
	    {
	        return true;
	    }
	}
	
	public boolean filter(String path)
	{
		if(path.startsWith(rootPath))
		{
			path = path.substring(rootPath.length());
		}
		return excludedPaths.contains(path);
	}

}
