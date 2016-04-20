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
