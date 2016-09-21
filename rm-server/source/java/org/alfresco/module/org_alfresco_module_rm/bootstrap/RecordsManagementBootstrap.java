/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.SplitEmailAction;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigService;
import org.alfresco.module.org_alfresco_module_rm.email.CustomEmailMappingService;
import org.alfresco.repo.action.parameter.NodeParameterSuggesterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;


/**
 * RM module bootstrap
 *
 * @author janv
 */
public class RecordsManagementBootstrap extends AbstractLifecycleBean
{
    private TransactionService transactionService;
    private RMCaveatConfigService caveatConfigService;
    private CustomEmailMappingService customEmailMappingService;
    private RecordsManagementAdminService adminService;
    private NodeParameterSuggesterBootstrap suggesterBootstrap;

    public NodeParameterSuggesterBootstrap getSuggesterBootstrap() 
    {
        return suggesterBootstrap;
    }

    public void setSuggesterBootstrap(NodeParameterSuggesterBootstrap suggesterBootstrap) 
    {
        this.suggesterBootstrap = suggesterBootstrap;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setCaveatConfigService(RMCaveatConfigService caveatConfigService)
    {
        this.caveatConfigService = caveatConfigService;
    }

    public void setCustomEmailMappingService(CustomEmailMappingService customEmailMappingService)
    {
        this.customEmailMappingService = customEmailMappingService;
    }

    public void setRecordsManagementAdminService(RecordsManagementAdminService adminService)
    {
		this.adminService = adminService;
	}

    public CustomEmailMappingService getCustomEmailMappingService()
    {
        return customEmailMappingService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // run as System on bootstrap
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
                {
                    public Void execute()
                    {
                        // initialise caveat config
                        caveatConfigService.init();

                        // Initialise the custom model
                        adminService.initialiseCustomModel();
                        
                        // Initialize the suggester after the model
                        // in case it contains namespaces from custom models
                        suggesterBootstrap.init();

                        // Initialise the SplitEmailAction
                        SplitEmailAction action = (SplitEmailAction)getApplicationContext().getBean("splitEmail");
                        action.bootstrap();

                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(callback);

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
}

