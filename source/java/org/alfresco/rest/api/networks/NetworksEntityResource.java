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