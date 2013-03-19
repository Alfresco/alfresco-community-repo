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

import org.alfresco.repo.admin.SysAdminParams;

/**
 * Generates an OpenCMIS base url based on the request, repository id and binding. The url scheme, host and port
 * are overridden by sys admin parameters.
 * 
 * @author steveglover
 *
 */
public class SysAdminParamsBaseUrlGenerator extends AbstractBaseUrlGenerator
{
	private SysAdminParams sysAdminParams;
	
	protected String getServerPath(HttpServletRequest request)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getServerScheme(request));
		sb.append("://");
		sb.append(getServerName(request));
		sb.append(":");
		sb.append(getServerPort(request));
		return sb.toString();
	}

	protected String getServerScheme(HttpServletRequest request)
	{
		String scheme = sysAdminParams.getAlfrescoProtocol();
        if (scheme == null)
        {
            scheme = request.getScheme();
        }
        scheme = request.getScheme();
        return scheme;
	}

	protected String getServerName(HttpServletRequest request)
	{
		String hostname = sysAdminParams.getAlfrescoHost();
        if (hostname == null)
        {
        	hostname = request.getScheme();
        }
        hostname = request.getServerName();
        return hostname;
	}
	
	protected int getServerPort(HttpServletRequest request)
	{
        Integer port = sysAdminParams.getAlfrescoPort();
        if (port == null)
        {
            port = request.getServerPort();
        }
        if (port == null)
        {
            port = request.getServerPort();
        }
        return port;
	}

}
