package org.alfresco.rest.workflow.api;

import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.workflow.api.model.Deployment;

public interface Deployments
{
    public CollectionWithPagingInfo<Deployment> getDeployments(Paging paging);
    
    public Deployment getDeployment(String deploymentId);
}
