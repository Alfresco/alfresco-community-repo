/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.audit;

import org.alfresco.repo.audit.model.AuditModelRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.context.ApplicationEvent;

/**
 * Starts all the necessary audit functionality once the repository has started.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditBootstrap extends AbstractLifecycleBean
{
    private TransactionService transactionService;
    private AuditModelRegistry auditModelRegistry;
    private boolean isActive = true;
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setAuditModelRegistry(AuditModelRegistry registry)
    {
        this.auditModelRegistry = registry;
    }
    
    public void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }

    /**
     * @see AuditModelRegistry#loadAuditModels()
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // Don't bootstrap if the system is read-only
        if (transactionService.isReadOnly())
        {
            return;
        }
        
        // Don't start if we've been configured out
        if (!this.isActive)
        {
            return;
        }

        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                auditModelRegistry.loadAuditModels();
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, transactionService.isReadOnly());
    }

    /**
     * No-op
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
}
