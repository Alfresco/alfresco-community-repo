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
 * are overridden by any proxy http header parameters, if present.
 * 
 * @author steveglover
 *
 */
public class ProxyBaseUrlGenerator extends AbstractBaseUrlGenerator
{
    public static final String FORWARDED_HOST_HEADER = "X-Forwarded-Host";
    public static final String FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";
    public static final String HTTPS_SCHEME = "https";
    public static final String HTTP_SCHEME = "http";

	@Override
	protected String getServerPath(HttpServletRequest request)
	{
        String scheme = request.getHeader(FORWARDED_PROTO_HEADER);
        String serverName;
        int serverPort;

        if (!HTTP_SCHEME.equalsIgnoreCase(scheme) && !HTTPS_SCHEME.equalsIgnoreCase(scheme))
        {
            scheme = request.getScheme();
        }

        serverName = request.getServerName();
        serverPort = request.getServerPort();

        String host = request.getHeader(FORWARDED_HOST_HEADER);
        if ((host != null) && (host.length() > 0))
        {
            int index = host.indexOf(':');
            if (index < 0)
            {
                serverName = host;
                serverPort = getDefaultPort(scheme);
            }
            else
            {
                serverName = host.substring(0, index);
                try
                {
                    serverPort = Integer.parseInt(host.substring(index + 1));
                }
                catch (NumberFormatException e)
                {
                    serverPort = getDefaultPort(scheme);
                }
            }
        }

		StringBuilder sb = new StringBuilder();
		sb.append(scheme);
		sb.append("://");
		sb.append(serverName);
		sb.append(":");
		sb.append(serverPort);
		return sb.toString();
	}

    private int getDefaultPort(String scheme)
    {
        if (HTTPS_SCHEME.equalsIgnoreCase(scheme))
        {
            return 443;
        }

        return 80;
    }
    
}
