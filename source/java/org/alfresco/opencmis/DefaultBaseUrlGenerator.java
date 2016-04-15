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
