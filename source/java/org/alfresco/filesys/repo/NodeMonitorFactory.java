
package org.alfresco.filesys.repo;

import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
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
	 * @param filesysCtx ContentContext
	 */
	public NodeMonitor createNodeMonitor(final ContentContext filesysCtx) 
	{
		
		// Initialization needs a transaction
		
	    RetryingTransactionHelper tran = m_transService.getRetryingTransactionHelper();
	    
        RetryingTransactionCallback<NodeMonitor> initialiseCB = new RetryingTransactionCallback<NodeMonitor>() {

            @Override
            public NodeMonitor execute() throws Throwable
            {                
                NodeMonitor nodeMonitor = new NodeMonitor(
                        filesysCtx, m_nodeService, m_policyComponent, m_fileFolderService,
                        m_permissionService, m_transService);
                
                return nodeMonitor;
            }
        };
	    
	    
        return tran.doInTransaction(initialiseCB, true); 

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
