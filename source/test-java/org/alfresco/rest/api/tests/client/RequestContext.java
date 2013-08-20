package org.alfresco.rest.api.tests.client;

import org.apache.chemistry.opencmis.client.api.OperationContext;

public class RequestContext
{
	private String networkId;
	private String runAsUser;
	private String password;
	private OperationContext cmisOperationCtxOverride = null;
	
	public RequestContext(String runAsUser)
	{
		super();
		this.runAsUser = runAsUser;
	}

	public RequestContext(String networkId, String runAsUser)
	{
		this(runAsUser);
		this.networkId = networkId;
		this.password = null;
	}
	
	public RequestContext(String networkId, String runAsUser, OperationContext cmisOperationCtxOverride)
	{
		this(runAsUser);
		this.networkId = networkId;
		this.password = null;
		this.cmisOperationCtxOverride = cmisOperationCtxOverride;
	}

	public RequestContext(String networkId, String runAsUser, String password)
	{
		this(runAsUser);
		this.networkId = networkId;
		this.password = password;
	}

	public void setOperationContext(OperationContext ctx)
	{
		this.cmisOperationCtxOverride = ctx;
	}
	
	public OperationContext getCmisOperationCtxOverride()
	{
		return cmisOperationCtxOverride;
	}

	public String getPassword()
	{
		return password;
	}

	public String getNetworkId()
	{
		return networkId;
	}

	public String getRunAsUser()
	{
		return runAsUser;
	}

	public void setNetworkId(String networkId)
	{
		this.networkId = networkId;
	}

	public void setRunAsUser(String runAsUser)
	{
		this.runAsUser = runAsUser;
	}
	
}
