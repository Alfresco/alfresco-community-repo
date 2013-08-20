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
