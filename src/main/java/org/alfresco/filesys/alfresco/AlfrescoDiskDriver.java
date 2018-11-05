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

package org.alfresco.filesys.alfresco;

import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.IOControlNotImplementedException;
import org.alfresco.jlan.server.filesys.IOCtlInterface;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.jlan.util.DataBuffer;
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
public abstract class AlfrescoDiskDriver implements ExtendedDiskInterface {

    // Logging
    
    private static final Log logger = LogFactory.getLog(AlfrescoDiskDriver.class);
    
    // Service registry for desktop actions
    
    private ServiceRegistry m_serviceRegistry;
    
    //  Transaction service
    
    protected TransactionService m_transactionService;

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
     * @param serviceRegistry ServiceRegistry
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
            // Enable a standalone state cache on the filesystem
            
            AlfrescoContext alfCtx = (AlfrescoContext) ctx;
            
            // Initialize the filesystem
            
            alfCtx.initialize(this);
        }
    }
}
