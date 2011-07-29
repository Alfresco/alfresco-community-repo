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
package org.alfresco.filesys.repo;

import java.io.IOException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * An advice wrapper for an AlfrescoDiskDriver.
 */
public class FilesystemTransactionAdvice implements MethodInterceptor
{
    private boolean readOnly;
    
//    private AlfrescoDiskDriver driver;
    
    private TransactionService transactionService;

    public FilesystemTransactionAdvice()
    {
        readOnly = false;
    }
    
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public Object invoke(final MethodInvocation methodInvocation) throws IOException, Throwable
    {
//        Object[] args = methodInvocation.getArguments();
//        
//        if(args.length == 0 || !(args[0] instanceof SrvSession))
//        {
//            throw new AlfrescoRuntimeException("First argument is not of correct type");
//        }
        
        RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        RetryingTransactionCallback<Object> callback = new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                try
                {
                    return methodInvocation.proceed();
                }
                catch (IOException e)
                {
                    // Ensure original checked IOExceptions get propagated
                    throw new PropagatingException(e);
                }
            }
        };

        if(readOnly)
        {
            // read only transaction
            try
            {
                return tran.doInTransaction(callback, true);
            }
            catch(PropagatingException pe)
            {
                // Unwrap checked exceptions
                throw (IOException) pe.getCause();
            }
        }
        else
        {
            // read/write only transaction
            try
            {
               return tran.doInTransaction(callback);  
            }
            catch(PropagatingException pe)
            {
                // Unwrap checked exceptions
                throw (IOException) pe.getCause();
            }
            
        }
    }

//    public void setDriver(AlfrescoDiskDriver driver)
//    {
//        this.driver = driver;
//    }
//
//    public AlfrescoDiskDriver getDriver()
//    {
//        return driver;
//    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public TransactionService getTransactionService()
    {
        return transactionService;
    }
    
    /**
     * A wrapper for checked exceptions to be passed through the retrying transaction handler.
     */
    protected static class PropagatingException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        /**
         * @param cause
         */
        public PropagatingException(Throwable cause)
        {
            super(cause);
        }        
    }
}
