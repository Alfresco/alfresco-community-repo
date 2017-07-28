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
