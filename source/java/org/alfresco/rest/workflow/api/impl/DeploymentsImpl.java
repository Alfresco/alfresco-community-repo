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
package org.alfresco.rest.workflow.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentQuery;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.workflow.api.Deployments;
import org.alfresco.rest.workflow.api.model.Deployment;

public class DeploymentsImpl extends WorkflowRestImpl implements Deployments
{
    @Override
    public CollectionWithPagingInfo<Deployment> getDeployments(Paging paging)
    {
        // Only admin-user is allowed to get deployments
        if(!authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser())) {
            throw new PermissionDeniedException();
        }
        
        DeploymentQuery query = activitiProcessEngine
                .getRepositoryService()
                .createDeploymentQuery()
                .deploymentCategoryNotEquals(WorkflowDeployer.CATEGORY_ALFRESCO_INTERNAL);
        
        if (tenantService.isEnabled() && deployWorkflowsInTenant) 
        {
            query.processDefinitionKeyLike("@" + TenantUtil.getCurrentDomain() + "@%");
        }
        
        query.orderByDeploymenTime().desc();
        List<org.activiti.engine.repository.Deployment> deployments = query.listPage(paging.getSkipCount(), paging.getMaxItems());
        int totalCount = (int) query.count();

        List<Deployment> page = new ArrayList<Deployment>(deployments.size());
        for (org.activiti.engine.repository.Deployment deployment: deployments) 
        {
            page.add(new Deployment(deployment));
        }
          
        return CollectionWithPagingInfo.asPaged(paging, page, page.size() != totalCount, totalCount);
    }
    
    @Override
    public Deployment getDeployment(String deploymentId) 
    {
        // Only admin-user is allowed to get deployments
        if(!authorityService.isAdminAuthority(AuthenticationUtil.getRunAsUser())) {
            throw new PermissionDeniedException();
        }
        
        RepositoryService repositoryService = activitiProcessEngine.getRepositoryService();
        
        DeploymentQuery query = repositoryService
                .createDeploymentQuery()
                .deploymentId(deploymentId);
        
        if (tenantService.isEnabled() && deployWorkflowsInTenant) 
        {
            query.processDefinitionKeyLike("@" + TenantUtil.getCurrentDomain() + "@%");
        }
        
        org.activiti.engine.repository.Deployment deployment = null;
        try 
        {
            deployment = query.singleResult();
        } 
        catch(ActivitiException e) 
        {
            // The next exception will cause a response status 400: Bad request
            throw new InvalidArgumentException("Invalid deployment id: " + deploymentId); 
        }
        
        if (deployment == null) 
        {
            // The next exception will cause a response status 404: Not found
            throw new EntityNotFoundException(deploymentId);
        }

        Deployment deploymentRest = new Deployment(deployment);
        return deploymentRest;
    }
}
