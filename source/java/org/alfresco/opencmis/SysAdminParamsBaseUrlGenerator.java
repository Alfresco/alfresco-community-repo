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
