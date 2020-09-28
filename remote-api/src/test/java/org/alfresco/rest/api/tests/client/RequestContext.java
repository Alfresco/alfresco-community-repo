/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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
