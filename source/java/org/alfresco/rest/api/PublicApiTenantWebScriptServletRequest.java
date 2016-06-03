package org.alfresco.rest.api;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.web.scripts.TenantWebScriptServletRequest;
import org.springframework.extensions.config.ServerProperties;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Runtime;

public class PublicApiTenantWebScriptServletRequest extends TenantWebScriptServletRequest
{
	public static final String NETWORKS_PATH = "networks";
	public static final String NETWORK_PATH = "network";
	
	public PublicApiTenantWebScriptServletRequest(Runtime container, HttpServletRequest req, Match serviceMatch, ServerProperties serverProperties)
	{
		super(container, req, serviceMatch, serverProperties);
	}

	@Override
    protected void parse()
    {
        String realPathInfo = getRealPathInfo();

        if(realPathInfo.equals("") || realPathInfo.equals("/"))
        {
        	// no tenant - "index" request
        	tenant = TenantUtil.DEFAULT_TENANT;
            pathInfo = NETWORKS_PATH;
        }
        else
        {
        	// optimisation - don't need to lowercase the whole path
        	if(realPathInfo.substring(0, 5).toLowerCase().equals("/cmis"))
        	{
        		// cmis service document, pass through as is and set tenant to "-default-".
            	tenant = TenantUtil.DEFAULT_TENANT;
                pathInfo = realPathInfo;
        	}
        	else
        	{
                int idx = realPathInfo.indexOf('/', 1);

	            // remove tenant
	        	tenant = realPathInfo.substring(1, idx == -1 ? realPathInfo.length() : idx);
	            pathInfo = realPathInfo.substring(tenant.length() + 1);
        	}
        }
    }
}
