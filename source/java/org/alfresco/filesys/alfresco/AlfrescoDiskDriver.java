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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys.alfresco;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.server.filesys.IOCtlInterface;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.TransactionalFilesystemInterface;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.jlan.util.DataBuffer;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Alfresco Disk Driver Base Class
 * 
 * <p>Provides common code to the Alfresco filesystem implementations.
 *
 * @author gkspencer
 */
public abstract class AlfrescoDiskDriver implements IOCtlInterface, TransactionalFilesystemInterface, ExtendedDiskInterface {

    // Logging
    
    private static final Log logger = LogFactory.getLog(AlfrescoDiskDriver.class);
    
    // Service registry for desktop actions
    
    private ServiceRegistry m_serviceRegistry;
    
    //  Transaction service
    
    private TransactionService m_transactionService;
    
    // Remember whether the current thread is already in a retrying transaction
    
    private ThreadLocal<Boolean> m_inRetryingTransaction = new ThreadLocal<Boolean>();
    
    /**
     * Return the service registry
     * 
     * @return ServiceRegistry
     */
    public final ServiceRegistry getServiceRegistry()
    {
    	return m_serviceRegistry;
    }

    /**
     * Return the transaction service
     * 
     * @return TransactionService
     */
    public final TransactionService getTransactionService()
    {
        return m_transactionService;
    }
    
    /**
     * Set the service registry
     * 
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	m_serviceRegistry = serviceRegistry;
    }
    
    /**
     * @param transactionService the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        m_transactionService = transactionService;
    }

    /**
     * Process a filesystem I/O control request
     * 
     * @param sess Server session
     * @param tree Tree connection.
     * @param ctrlCode I/O control code
     * @param fid File id
     * @param dataBuf I/O control specific input data
     * @param isFSCtrl true if this is a filesystem control, or false for a device control
     * @param filter if bit0 is set indicates that the control applies to the share root handle
     * @return DataBuffer
     * @exception IOControlNotImplementedException
     * @exception SMBException
     */
    public DataBuffer processIOControl(SrvSession sess, TreeConnection tree, int ctrlCode, int fid, DataBuffer dataBuf,
            boolean isFSCtrl, int filter)
        throws IOControlNotImplementedException, SMBException
    {
        // Validate the file id
        
        NetworkFile netFile = tree.findFile(fid);
        if ( netFile == null || netFile.isDirectory() == false)
            throw new SMBException(SMBStatus.NTErr, SMBStatus.NTInvalidParameter);
        
        // Check if the I/O control handler is enabled
        
        AlfrescoContext ctx = (AlfrescoContext) tree.getContext();
        if ( ctx.hasIOHandler())
            return ctx.getIOHandler().processIOControl( sess, tree, ctrlCode, fid, dataBuf, isFSCtrl, filter);
        else
            throw new IOControlNotImplementedException();
    }
    
    /**
     * Begin a read-only transaction
     * 
     * @param sess SrvSession
     */
    public void beginReadTransaction(SrvSession sess) {
      beginTransaction( sess, true);
    }

    /**
     * Begin a writeable transaction
     * 
     * @param sess SrvSession
     */
    public void beginWriteTransaction(SrvSession sess) {
      beginTransaction( sess, false);
    }
    
    /**
     * Perform a retryable operation in a write transaction
     * <p>
     * WARNING : side effect - that the current transaction, if any, is ended.
     * 
     * 
     * @param sess
     *            the server session
     * @param callback
     *            callback for the retryable operation
     * @return the result of the operation
     * @throws Exception
     */
    public <T> T doInWriteTransaction(SrvSession sess, final CallableIO<T> callback)
            throws IOException
    {
        Boolean wasInRetryingTransaction = m_inRetryingTransaction.get();
        try
        {
            boolean hadTransaction = sess.hasTransaction();
            if (hadTransaction)
            {
                sess.endTransaction();
            }
            m_inRetryingTransaction.set(Boolean.TRUE);
            T result = m_transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionHelper.RetryingTransactionCallback<T>()
                    {
                        public T execute() throws Throwable
                        {
                            try
                            {
                                return callback.call();
                            }
                            catch (IOException e)
                            {
                                // Ensure original checked IOExceptions get propagated
                                throw new PropagatingException(e);
                            }
                        }
                    });
            if (hadTransaction)
            {
                beginReadTransaction(sess);
            }
            return result;
        }
        catch (PropagatingException e)
        {
            // Unwrap checked exceptions
            throw (IOException) e.getCause();
        }
        finally
        {
            m_inRetryingTransaction.set(wasInRetryingTransaction);
        }
    }

    /**
     * End an active transaction
     * 
     * @param sess SrvSession
     * @param tx Object
     */
    public void endTransaction(SrvSession sess, Object tx) {

      // Check that the transaction object is valid
      
      if ( tx == null)
        return;
      
      // Get the filesystem transaction
      
      FilesysTransaction filesysTx = (FilesysTransaction) tx;

      // Check if there is an active transaction
      
      if ( filesysTx != null && filesysTx.hasTransaction())
      {
        // Get the active transaction
        
          UserTransaction ftx = filesysTx.getTransaction();
          
          try
          {
              // Commit or rollback the transaction
              
              if ( ftx.getStatus() == Status.STATUS_MARKED_ROLLBACK ||
            	   ftx.getStatus() == Status.STATUS_ROLLEDBACK ||
            	   ftx.getStatus() == Status.STATUS_ROLLING_BACK)
              {
                  // Transaction is marked for rollback
                  
                  ftx.rollback();
                  
                  // DEBUG
                  
                  if ( logger.isDebugEnabled())
                      logger.debug("End transaction (rollback)");
              }
              else
              {
            	  // Commit the transaction
                  
                  ftx.commit();
                  
                  // DEBUG
                  
                  if ( logger.isDebugEnabled())
                      logger.debug("End transaction (commit)");
              }
          }
          catch ( Exception ex)
          {
        	  if ( logger.isDebugEnabled())
        		  logger.debug("Failed to end transaction, " + ex.getMessage());
//              throw new AlfrescoRuntimeException("Failed to end transaction", ex);
          }
          finally
          {
              // Clear the current transaction
              
              sess.clearTransaction();
          }
        }
    }

    /**
     * Create and start a transaction, if not already active
     * 
     * @param sess SrvSession
     * @param readOnly boolean
     * @exception AlfrescoRuntimeException
     */
    private final void beginTransaction( SrvSession sess, boolean readOnly)
        throws AlfrescoRuntimeException
    {
        // Do nothing if we are already in a retrying transaction
        Boolean inRetryingTransaction = m_inRetryingTransaction.get();
        
        if (inRetryingTransaction != null && inRetryingTransaction)
        {
            return;
        }

        // Initialize the per session thread local that holds the transaction
        
        sess.initializeTransactionObject();

        // Get the filesystem transaction
        
        FilesysTransaction filesysTx = (FilesysTransaction) sess.getTransactionObject().get();
        if ( filesysTx == null)
        {
          filesysTx = new FilesysTransaction();
          sess.getTransactionObject().set( filesysTx);
        }
        
        // If there is an active transaction check that it is the required type

        if ( filesysTx.hasTransaction())
        {
        	// Get the active transaction
          
            UserTransaction tx = filesysTx.getTransaction();
            
            // Check if the current transaction is marked for rollback
            
            try
            {
                if ( tx.getStatus() == Status.STATUS_MARKED_ROLLBACK ||
                     tx.getStatus() == Status.STATUS_ROLLEDBACK ||
                     tx.getStatus() == Status.STATUS_ROLLING_BACK)
                {
                    //  Rollback the current transaction
                    
                    tx.rollback();
                }
            }
            catch ( Exception ex)
            {
            }
            
            // Check if the transaction is a write transaction, if write has been requested
            
            if ( readOnly == false && filesysTx.isReadOnly() == true)
            {
                // Commit the read-only transaction
                
                try
                {
                    tx.commit();
                }
                catch ( Exception ex)
                {
                    throw new AlfrescoRuntimeException("Failed to commit read-only transaction, " + ex.getMessage());
                }
                finally
                {
                    // Clear the active transaction

                	filesysTx.clearTransaction();
                }
            }
        }
        
        // Create the transaction
        
        if ( filesysTx.hasTransaction() == false)
        {
            try
            {
            	// Create a new transaction
            	
            	UserTransaction userTrans = m_transactionService.getUserTransaction(readOnly);
                userTrans.begin();

                // Store the transaction
                
                filesysTx.setTransaction( userTrans, readOnly);
                
                // DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("Created transaction readOnly=" + readOnly);
            }
            catch (Exception ex)
            {
                throw new AlfrescoRuntimeException("Failed to create transaction, " + ex.getMessage());
            }
        }
        
        //  Store the transaction callback
        
        sess.setTransaction( this);
    }

    /**
     * Registers a device context object for this instance
     * of the shared device. The same DeviceInterface implementation may be used for multiple
     * shares. In this base class, we initialize all desktop actions.
     * 
     * @param ctx the context
     * @exception DeviceContextException
     */
    public void registerContext(DeviceContext ctx) throws DeviceContextException
    {
        if (ctx instanceof AlfrescoContext)
        {
            ((AlfrescoContext) ctx).initialize(this);
        }
    }
    
    /**
     * An extended {@link Callable} that throws {@link IOException}s.
     *
     * @param <V>
     */
    public interface CallableIO <V> extends Callable<V>
    {
        public V call() throws IOException;        
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
