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

/**
 * Generates an OpenCMIS base url based on the request, repository id and binding. The url scheme, host and port
 * are overridden by a property from repository.properties or in an override file.
 * 
 * @author steveglover
 *
 */
public class DefaultBaseUrlGenerator extends AbstractBaseUrlGenerator
{
    private boolean overrideServer;
    private String serverOverride;

	public DefaultBaseUrlGenerator()
	{
	}

	public void setOverrideServer(boolean overrideServer)
	{
		this.overrideServer = overrideServer;
	}

	public void setServerOverride(String serverOverride)
	{
		this.serverOverride = serverOverride;
	}
	
	protected String getServerPath(HttpServletRequest request)
	{
		if(overrideServer)
		{
			return serverOverride;
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(request.getScheme());
			sb.append("://");
			sb.append(request.getServerName());
			sb.append(":");
			sb.append(request.getServerPort());
			return sb.toString();
		}
	}

}
