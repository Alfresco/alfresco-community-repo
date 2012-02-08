/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;
import java.util.Properties;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.site.SiteAVMBootstrap;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.WorkflowDeployer;

/**
 * MT Share - update existing tenants (if any)
 */
public class MultiTShareExistingTenantsPatch extends AbstractPatch
{
    private static final String MSG_RESULT = "patch.mtShareExistingTenants.result";
    private static final String MSG_RESULT_NA = "patch.mtShareExistingTenants.result.not_applicable";
    
    private WorkflowDeployer workflowPatchDeployer;
    private List<Properties> workflowDefinitions;
    private TenantService tenantService;
    
    public void setWorkflowDeployer(WorkflowDeployer workflowPatchDeployer)
    {
        this.workflowPatchDeployer = workflowPatchDeployer;
    }
    
    public void setWorkflowDefinitions(List<Properties> workflowDefinitions)
    {
        this.workflowDefinitions = workflowDefinitions;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#checkProperties()
     */
    @Override
    protected void checkProperties()
    {
        super.checkProperties();
    }

    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        if (!tenantService.isEnabled())
        {
            return I18NUtil.getMessage(MSG_RESULT_NA);
        }
        
        if (! tenantService.getCurrentUserDomain().equals(TenantService.DEFAULT_DOMAIN))
        { 
            workflowPatchDeployer.setWorkflowDefinitions(workflowDefinitions);
            workflowPatchDeployer.init();
        }
        
        return I18NUtil.getMessage(MSG_RESULT);
    }
}
