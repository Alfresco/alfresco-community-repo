/*
 * Copyright (C) 2009-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.job;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Records management job executer base class.
 * 
 * @author Roy Wetherall
 */
public abstract class RecordsManagementJobExecuter  extends AbstractLifecycleBean
                                                    implements RecordsManagementModel                       
{
    /** Retrying transaction helper */
    protected RetryingTransactionHelper retryingTransactionHelper;
    
    /** Indicates whether the application bootstrap is complete or not */
    protected boolean bootstrapComplete = false;
    
    /**
     * @param retryingTransactionHelper retrying transaction helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    /**
     * Executes the jobs work.
     */
    public void execute()
    {
        // jobs not allowed to execute unless bootstrap is complete
        if (bootstrapComplete == true)
        {
            executeImpl();
        }
    }
    
    /**
     * Jobs work implementation.
     */
    public abstract void executeImpl();
    
    /**
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent arg0)
    {
        // record that the bootstrap has complete
        bootstrapComplete = true;
    }
    
    /**
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent arg0)
    {
        // no implementation
    }
    
    /**
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ApplicationEvent arg0)
    {
        // no implementation
    }
}
