package org.alfresco.opencmis;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.alfresco.repo.tenant.TenantUtil;

/**
 * Default generator for OpenCMIS paths based on the repositoryId and binding.
 * 
 * @author steveglover
 *
 */
public class DefaultPathGenerator implements PathGenerator
{
	public void generatePath(HttpServletRequest req, StringBuilder url, String repositoryId, Binding binding)
	{
	    url.append(binding.toString());
	    url.append("/");
	    if(repositoryId != null)
	    {
	        url.append(repositoryId);
	    }
	    else
	    {
	        url.append(TenantUtil.DEFAULT_TENANT);
	    }
	}

}
