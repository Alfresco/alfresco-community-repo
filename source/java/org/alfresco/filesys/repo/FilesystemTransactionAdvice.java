package org.alfresco.filesys.repo;

import java.io.IOException;

import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * An advice wrapper for an AlfrescoDiskDriver.   Wraps the method call with a 
 * RetryingTransactionHandler.
 * <p>
 * Needs to let the checked exceptions that are specified on the JLAN interfaces through.  
 * In particular must avoid wrapping JLAN's checked exceptions with an AlfrescoRuntimeException 
 * (so must throw IOException etc)
 * <p>
 * @see org.alfresco.jlan.server.filesys.DiskInterface
 * @see org.alfresco.filesys.alfresco.IOControlHandler
 * 
 */
public class FilesystemTransactionAdvice implements MethodInterceptor
{
    private boolean readOnly;
    
    private TransactionService transactionService;

    public FilesystemTransactionAdvice()
    {
        readOnly = false;
    }
    
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public Object invoke(final MethodInvocation methodInvocation) throws IOException, SMBException, Throwable
    {
        
        RetryingTransactionHelper tran = transactionService.getRetryingTransactionHelper();
        
        RetryingTransactionCallback<Object> callback = new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                try
                {
                    return methodInvocation.proceed();
                }
                catch (SMBException e)
                {
                    throw new PropagatingException(e);
                }
                catch (IOControlNotImplementedException e)
                {
                    throw new PropagatingException(e);
                }
                catch (IOException e)
                {
                    // Ensure original checked IOExceptions get propagated
                    throw new PropagatingException(e);
                }
                catch (DeviceContextException e)
                {
                    throw new PropagatingException(e);
                }
            }
        };

        try
        {
            return tran.doInTransaction(callback, readOnly);
        }
        catch(PropagatingException pe)
        {
            Throwable t = pe.getCause();
            if(t != null)
            {
                if(t instanceof IOException)
                {
                    throw (IOException) t;
                }
                if(t instanceof IOControlNotImplementedException)
                {
                    throw (IOControlNotImplementedException) t;
                }
                if(t instanceof SMBException)
                {
                    throw (SMBException)t;
                }
                if(t instanceof DeviceContextException)
                {
                    throw t;
                }
                throw t;
            }
            throw pe;
        }
    }

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
