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
package org.alfresco.repo.security.authentication;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Transaction listener that re-generates the hash of a users password in
 * the afterCommit callback.
 *
 * @author Gavin Cornwell
 */
public class HashPasswordTransactionListener implements TransactionListener
{
    private static Log logger = LogFactory.getLog(HashPasswordTransactionListener.class);
    
    private final String username;
    private final char[] password;
    
    private TransactionService transactionService;
    private MutableAuthenticationDao authenticationDao;
    
    public HashPasswordTransactionListener(final String username, final char[] password)
    {
        this.username = username;
        this.password = password;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setAuthenticationDao(MutableAuthenticationDao authenticationDao)
    {
        this.authenticationDao = authenticationDao;
    }
    
    @Override
    public void flush()
    {
        // nothing to do
    }

    @Override
    public void beforeCommit(boolean readOnly)
    {
        // nothing to do
    }

    @Override
    public void beforeCompletion()
    {
        // nothing to do
    }

    @Override
    public void afterCommit()
    {
        // get transaction helper and force it to be writable in case system is in read only mode
        RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
        txHelper.setForceWritable(true);
        txHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationUtil.pushAuthentication();
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                try
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Re-hashing password for user: " + username);
                    }
                    
                    // update the users password to force a new hash to be generated
                    authenticationDao.updateUser(username, password);
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Password for user '" + username + "' has been re-hashed following login");
                    }
                    
                    return null;
                }
                finally
                {
                    AuthenticationUtil.popAuthentication();
                }
            }
        }, false, true);
    }

    @Override
    public void afterRollback()
    {
        // nothing to do
    }
}
