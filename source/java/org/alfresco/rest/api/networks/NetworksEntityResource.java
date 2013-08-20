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
package org.alfresco.rest.api.networks;

import org.alfresco.rest.api.Networks;
import org.alfresco.rest.api.model.Network;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

@EntityResource(name="networks", title = "Networks entity")
public class NetworksEntityResource implements EntityResourceAction.ReadById<Network>, InitializingBean
{
	public static final String NAME = "networks";

	private Networks networks;

	public void setNetworks(Networks networks)
	{
		this.networks = networks;
	}

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("networks", this.networks);
    }

    @Override
    @WebApiDescription(title = "Get Network Information", description = "Get information for the network with id 'networkId'")
    @WebApiParam(name = "networkId", title = "The network name")
    public Network readById(final String networkId, Parameters parameters)
    {
    	return networks.getNetwork(networkId);
    }
}