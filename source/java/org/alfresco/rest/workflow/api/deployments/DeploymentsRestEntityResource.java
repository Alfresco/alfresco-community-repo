/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.deployments;

import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.workflow.api.Deployments;
import org.alfresco.rest.workflow.api.model.Deployment;

@EntityResource(name="deployments", title = "Deployments")
public class DeploymentsRestEntityResource implements EntityResourceAction.Read<Deployment>,  
                                                      EntityResourceAction.ReadById<Deployment> {

    Deployments deployments;
    
    public void setDeployments(Deployments deployments)
    {
        this.deployments = deployments;
    }

    @Override
    public CollectionWithPagingInfo<Deployment> readAll(Parameters params)
    {
        return deployments.getDeployments(params.getPaging());
    }
    
    @Override
	public Deployment readById(String id, Parameters parameters) throws EntityNotFoundException
	{
        return deployments.getDeployment(id);
	}
}
