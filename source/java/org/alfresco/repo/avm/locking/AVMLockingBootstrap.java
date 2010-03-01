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

package org.alfresco.repo.avm.locking;

import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.context.ApplicationEvent;

/**
 * Bootstrap for AVM Locking Service.
 * 
 * @author britt
 */
public class AVMLockingBootstrap extends AbstractLifecycleBean
{
    private AVMLockingService fLockingService;
    private TransactionService transactionService;

    public void setAvmLockingService(AVMLockingService service)
    {
        fLockingService = service;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // Do nothing if the repo is read-only
        if (transactionService.isReadOnly())
        {
            return;
        }
        
        if (fLockingService instanceof AVMLockingServiceImpl)
        {
            ((AVMLockingServiceImpl) fLockingService).init();
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Do nothing.
    }
}
