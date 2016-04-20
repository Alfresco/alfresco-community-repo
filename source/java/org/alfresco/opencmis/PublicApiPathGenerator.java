package org.alfresco.opencmis;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;

/**
 * Cloud generator for OpenCMIS paths based on the repositoryId and binding.
 * 
 * @author steveglover
 *
 */
public class PublicApiPathGenerator implements PathGenerator
{
	public void generatePath(HttpServletRequest req, StringBuilder url, String repositoryId, Binding binding)
	{
		url.append("{repositoryId}");
	    url.append("/");
	    
	    String scope = (String)req.getAttribute("apiScope");
	    String serviceName = (String)req.getAttribute("serviceName");
	    String apiVersion = (String)req.getAttribute("apiVersion");
	    if(scope == null)
	    {
	    	scope = "public";
	    }
	    url.append(scope);
	    url.append("/");
	    url.append(serviceName);
	    url.append("/");
	    url.append("versions");
	    url.append("/");
	    url.append(apiVersion);
	    url.append("/");
	    url.append(binding.toString());
	}

}
