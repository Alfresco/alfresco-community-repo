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
import org.alfresco.filesys.config.ServerConfigurationBean;
import org.alfresco.filesys.repo.ContentContext;
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
public abstract class AlfrescoDiskDriver implements IOCtlInterface, ExtendedDiskInterface {

    // Logging
    
    private static final Log logger = LogFactory.getLog(AlfrescoDiskDriver.class);
    
    // Service registry for desktop actions
    
    private ServiceRegistry m_serviceRegistry;
    
    //  Transaction service
    
    protected TransactionService m_transactionService;
    
    protected IOControlHandler ioControlHandler;
    
    public void setIoControlHandler(IOControlHandler ioControlHandler)
    {
        this.ioControlHandler = ioControlHandler;
    }

    public IOControlHandler getIoControlHandler()
    {
        return ioControlHandler;
    }
        
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
        // Not enabled for AVM
        
        if(tree.getContext() instanceof ContentContext)
        {
            ContentContext ctx = (ContentContext) tree.getContext();
        
            if(ioControlHandler != null)
            {
                return ioControlHandler.processIOControl(sess, tree, ctrlCode, fid, dataBuf, isFSCtrl, filter, this, ctx);
            }
            else
            {
                throw new IOControlNotImplementedException();
            }
        }
        return null;
    }
    
    /**
     * Registers a device context object for this instance
     * of the shared device. The same DeviceInterface implementation may be used for multiple
     * shares. In this base class, we initialize all desktop actions.
     * 
     * @param ctx the context
     * @param serverConfig ServerConfigurationBean
     * @exception DeviceContextException
     */
    public void registerContext(DeviceContext ctx, ServerConfigurationBean serverConfig) throws DeviceContextException
    {
        if (ctx instanceof AlfrescoContext)
        {
            // Enable a standalone state cache on the filesystem
            
            AlfrescoContext alfCtx = (AlfrescoContext) ctx;
            
            if ( serverConfig != null) 
            {
                alfCtx.enableStateCache(serverConfig, true);
            }

            // Initialize the filesystem
            
            alfCtx.initialize(this);
        }
    }
}
