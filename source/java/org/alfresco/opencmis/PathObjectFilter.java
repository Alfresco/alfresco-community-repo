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
