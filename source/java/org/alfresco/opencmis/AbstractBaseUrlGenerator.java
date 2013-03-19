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
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

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

	public void setContextOverride(String contextOverride)
	{
		this.contextOverride = contextOverride;
	}

	public void setOverrideServletPath(boolean overrideServletPath)
	{
		this.overrideServletPath = overrideServletPath;
	}

	public void setServletPathOverride(String servletPathOverride)
	{
		this.servletPathOverride = servletPathOverride;
	}
    
    protected abstract String getServerPath(HttpServletRequest request);

	protected String getContextPath(HttpServletRequest httpReq)
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

	protected String getServletPath(HttpServletRequest req)
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
    public UrlBuilder getBaseUrl(HttpServletRequest req, String repositoryId, Binding binding)
    {
        UrlBuilder url = new UrlBuilder(getServerPath(req));

        String contextPath = getContextPath(req);
        if(contextPath != null && !contextPath.equals(""))
        {
        	url.addPathSegment(contextPath);
        }

        String servletPath = getServletPath(req);
        if(servletPath != null && !servletPath.equals(""))
        {
        	url.addPathSegment(servletPath);
        }

        pathGenerator.generatePath(req, url, repositoryId, binding);
        
        return url;
    }
}
