/*
 * Copyright (C) 2006-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.filesys.repo;

import javax.transaction.UserTransaction;

import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Node Monitor Factory Class
 * 
 * @author gkspencer
 */
public class NodeMonitorFactory {

	// Logging
	
    private static final Log logger = LogFactory.getLog(NodeMonitorFactory.class);
 
    // Services/components
	
	private PolicyComponent m_policyComponent;
	private NodeService m_nodeService;
	private FileFolderService m_fileFolderService;
	private PermissionService m_permissionService;
	private TransactionService m_transService;
	
	/**
	 * Default constructor
	 */
	public NodeMonitorFactory () {
	}

	/**
	 * Create a node monitor
	 * 
	 * @param filesysDriver ContentDiskDriver
	 * @param filesysCtx ContentContext
	 */
	public NodeMonitor createNodeMonitor( ContentDiskDriver filesysDriver, ContentContext filesysCtx) {
		
		// Initialization needs a transaction
		
        UserTransaction tx = m_transService.getUserTransaction(true);
        NodeMonitor nodeMonitor = null;
        
        try {
        	
        	// Start the transaction
        	
        	tx.begin();

        	// Create the node monitor
			
			nodeMonitor = new NodeMonitor( filesysDriver, filesysCtx, m_nodeService, m_policyComponent, m_fileFolderService,
					m_permissionService, m_transService);
			
			// Commit the transaction
			
			tx.commit();
			tx = null;
        }
        catch ( Exception ex) {
        	logger.error(ex);
        }
        finally {
        	
            // If there is an active transaction then roll it back
            
            if ( tx != null)
            {
                try
                {
                    tx.rollback();
                }
                catch (Exception ex)
                {
                    logger.warn("Failed to rollback transaction", ex);
                }
            }
        }
        
        // Return the node monitor
        
        return nodeMonitor;
	}
	
    /**
     * Set the node service
     * 
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        m_nodeService = nodeService;
    }
    
    /**
     * Set the permission service
     * 
     * @param permissionService PermissionService
     */
    public void setPermissionService(PermissionService permissionService)
    {
        m_permissionService = permissionService;
    }
	
    /**
     * Set the file folder service
     * 
     * @param fileService FileFolderService
     */
    public void setFileFolderService(FileFolderService fileService)
    {
    	m_fileFolderService = fileService;
    }
    
    /**
     * Set the policy component
     * 
     * @param policyComponent PolicyComponent
     */
    public void setPolicyComponent(PolicyComponent policyComponent) {
    	m_policyComponent = policyComponent;
    }

    /**
     * Set the transaction service
     * 
     * @param transactionService the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        m_transService = transactionService;
    }

}
