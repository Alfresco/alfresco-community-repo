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
