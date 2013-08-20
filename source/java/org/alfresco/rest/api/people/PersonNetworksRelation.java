package org.alfresco.rest.api.people;

import org.alfresco.rest.api.Networks;
import org.alfresco.rest.api.model.PersonNetwork;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author steveglover
 *
 */
@RelationshipResource(name = "networks", entityResource = PeopleEntityResource.class, title = "Person Networks")
public class PersonNetworksRelation implements RelationshipResourceAction.Read<PersonNetwork>, RelationshipResourceAction.ReadById<PersonNetwork>, InitializingBean
{
    private static final Log logger = LogFactory.getLog(PersonNetworksRelation.class);

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
    @WebApiDescription(title = "A paged list of the person's network memberships.")
    public CollectionWithPagingInfo<PersonNetwork> readAll(String personId, Parameters parameters)
	{
        return networks.getNetworks(personId, parameters.getPaging());
	}

	@Override
    @WebApiDescription(title = "Network membership for person 'personId' in network 'networkId'.")
	public PersonNetwork readById(String personId, String networkId, Parameters parameters)
	{
        return networks.getNetwork(personId, networkId);
	}

}
