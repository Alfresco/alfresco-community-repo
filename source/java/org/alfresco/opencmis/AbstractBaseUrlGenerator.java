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
package org.alfresco.opencmis;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.opencmis.CMISDispatcherRegistry.Binding;
import org.alfresco.repo.tenant.TenantUtil;

/**
 * Generates an OpenCMIS base url based on the request, repository id and binding.
 *  
 * @author steveglover
 *
 */
public abstract class AbstractBaseUrlGenerator implements BaseUrlGenerator
{
    private boolean overrideContext;
    private String contextOverride;
    private boolean overrideServletPath;
    private String servletPathOverride;
    private PathGenerator pathGenerator;

	public void setPathGenerator(PathGenerator pathGenerator)
	{
		this.pathGenerator = pathGenerator;
	}

	public void setOverrideContext(boolean overrideContext)
	{
		this.overrideContext = overrideContext;
	}
	
	private String fixup(String urlSegment)
	{
		StringBuilder sb = new StringBuilder();
		int beginIndex = 0;
		int endIndex = urlSegment.length();
		if(urlSegment != null)
		{
			if(!urlSegment.equals("") && !urlSegment.startsWith("/"))
			{
				sb.append("/");
			}
			if(urlSegment.endsWith("/"))
			{
				endIndex -= 1;
			}
		}
		sb.append(urlSegment.substring(beginIndex, endIndex));
		return sb.toString();
	}

	public void setContextOverride(String contextOverride)
	{
		this.contextOverride = fixup(contextOverride);
	}

	public void setOverrideServletPath(boolean overrideServletPath)
	{
		this.overrideServletPath = overrideServletPath;
	}

	public void setServletPathOverride(String servletPathOverride)
	{
		this.servletPathOverride = fixup(servletPathOverride);
	}
    
    protected abstract String getServerPath(HttpServletRequest request);

	public String getContextPath(HttpServletRequest httpReq)
	{
		if(overrideContext)
		{
			return contextOverride;
		}
		else
		{
			return httpReq.getContextPath();
		}
	}

	public String getServletPath(HttpServletRequest req)
	{
		if(overrideServletPath)
		{
			return servletPathOverride;
		}
		else
		{
			return req.getServletPath();
		}
	}
	
	@Override
    public String getRequestURI(HttpServletRequest req, String repositoryId, String operation, String id)
    {
        StringBuilder url = new StringBuilder();

        String contextPath = getContextPath(req);
        if(contextPath != null && !contextPath.equals(""))
        {
    		url.append(contextPath);
        }

        String servletPath = getServletPath(req);
        if(servletPath != null && !servletPath.equals(""))
        {
    		url.append(servletPath);
        	url.append("/");
        }
        
        if(url.length() == 0 || url.charAt(0) != '/')
        {
        	url.append("/");
        }

		if(repositoryId != null)
		{
			url.append(repositoryId == null ? TenantUtil.DEFAULT_TENANT : repositoryId);
			url.append("/");
		}
		
		if(operation != null)
		{
			url.append(operation);
			url.append("/");
		}
		
		if(id != null)
		{
			url.append(id);
		}
		
		int length = url.length();
		if(length > 0 && url.charAt(length - 1) == '/')
		{
			url.deleteCharAt(length - 1);
		}

        return url.toString();
    }

	@Override
    public String getBaseUrl(HttpServletRequest req, String repositoryId, Binding binding)
    {
        StringBuilder url = new StringBuilder();
		String serverPath = getServerPath(req);
		url.append(serverPath);

        String contextPath = getContextPath(req);
        if(contextPath != null && !contextPath.equals(""))
        {
    		url.append(contextPath);
        }

        String servletPath = getServletPath(req);
        if(servletPath != null && !servletPath.equals(""))
        {
    		url.append(servletPath);
        	url.append("/");
        }
        
        if(url.length() > 0 && url.charAt(url.length() - 1) != '/')
        {
        	url.append("/");
        }

        pathGenerator.generatePath(req, url, repositoryId, binding);

        return url.toString();
    }
}
