package org.alfresco.repo.transfer.script;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferService;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;

/**
 * Java Script Transfer Service.   Adapts the Java Transfer Service to
 * Java Script.
 *
 * @author Mark Rogers
 */
public class ScriptTransferService extends BaseScopableProcessorExtension
{
    private TransferService transferService;

    /**
     * @param transferService
     */
    public void setTransferService(TransferService transferService)
    {
        this.transferService = transferService;
    }

    /**
     * 
     * @return
     */
    public TransferService getTransferService()
    {
        return transferService;
    }
    
    /**
     * create a transfer target
     */
    
    /**
     * Transfer a set of nodes, with no callback
     * @param targetName 
     * @param nodes
     * 
     * @return node ref of transfer report.  
     */
    public NodeRef transfer(String targetName, Scriptable nodes)
    {
        return null;
    }
    
}
