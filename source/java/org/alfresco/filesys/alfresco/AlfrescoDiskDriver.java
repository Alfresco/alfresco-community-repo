/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.filesys.alfresco;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.server.filesys.IOCtlInterface;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.TransactionalFilesystemInterface;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.jlan.util.DataBuffer;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.state.FileStateReaper;
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
public class AlfrescoDiskDriver implements IOCtlInterface, TransactionalFilesystemInterface {

    // Logging
    
    private static final Log logger = LogFactory.getLog(AlfrescoDiskDriver.class);
    
    // Service registry for desktop actions
    
    private ServiceRegistry m_serviceRegistry;
    
    // File state reaper
    
    private FileStateReaper m_stateReaper;

    //  Transaction service
    
    private TransactionService m_transactionService;
    
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
     * Return the file state reaper
     * 
     * @return FileStateReaper
     */
    public final FileStateReaper getStateReaper()
    {
    	return m_stateReaper;
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
     * Set the file state reaper
     * 
     * @param stateReaper FileStateReaper
     */
    public final void setStateReaper(FileStateReaper stateReaper)
    {
    	m_stateReaper = stateReaper;
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
     * End an active transaction
     * 
     * @param sess SrvSession
     * @param tx ThreadLocal<Object>
     */
    public void endTransaction(SrvSession sess, ThreadLocal<Object> tx) {

      // Check that the transaction object is valid
      
      if ( tx == null)
        return;
      
      // Get the filesystem transaction
      
      FilesysTransaction filesysTx = (FilesysTransaction) tx.get();

      // Check if there is an active transaction
      
      if ( filesysTx != null && filesysTx.hasTransaction())
      {
        // Get the active transaction
        
          UserTransaction ftx = filesysTx.getTransaction();
          
          try
          {
              // Commit or rollback the transaction
              
              if ( ftx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
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
              throw new AlfrescoRuntimeException("Failed to end transaction", ex);
          }
          finally
          {
              // Clear the current transaction
              
              filesysTx.clearTransaction();
          }
        }
      
        // Clear the active transaction interface, leave the transaction object as we will reuse it
      
        sess.setTransaction( null);
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
}
