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
