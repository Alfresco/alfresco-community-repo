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
package org.alfresco.encryption;

import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;

/**
 * The EncryptionChecker checks the state of the repository's encryption system. In particular it checks:
 * <ul>
 * <li>that the keystore exists and, if not, creates one.
 * <li>that the encryption keys have not been changed. If so, the bootstrap will be halted.
 * </ul>
 * 
 * @since 4.0
 *
 */
public class EncryptionChecker extends AbstractLifecycleBean
{
    private TransactionService transactionService;
    private KeyStoreChecker keyStoreChecker;

    public void setKeyStoreChecker(KeyStoreChecker keyStoreChecker)
    {
        this.keyStoreChecker = keyStoreChecker;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setForceWritable(true); // Force write in case server is read-only

        txnHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
            public Void execute() throws Throwable
            {
                try
                {
                    keyStoreChecker.validateKeyStores();
                }
                catch (Throwable e)
                {
                    // Just throw as a runtime exception
                    throw new AlfrescoRuntimeException("Keystores are invalid", e);
                }

                return null;
            }
        });
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {

    }
}
