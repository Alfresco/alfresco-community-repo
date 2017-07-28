/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.module;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.util.PropertyCheck;
import org.springframework.context.ApplicationEvent;

/**
 * This component is responsible for ensuring that patches are applied
 * at the appropriate time.
 * 
 * @author Derek Hulley
 */
public class ModuleStarter extends AbstractLifecycleBean
{
    private TransactionService transactionService;
    private ModuleService moduleService;

    /**
     * 
     * @param transactionService        provides the retrying transaction
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param moduleService the service that will do the actual work.
     */
    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        PropertyCheck.mandatory(this, "moduleService", moduleService);
        final RetryingTransactionCallback<Object> startModulesCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                moduleService.startModules();
                return null;
            }
        };
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception 
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(startModulesCallback, transactionService.isReadOnly());
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
