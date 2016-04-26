package org.alfresco.repo.transfer.requisite;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Processor for transfer requsite file
 * @author mrogers
 *
 */
public interface TransferRequsiteProcessor
{
    /**
     * Called at the start of a transfer requsite
     */
    public void startTransferRequsite();
    
    /**
     * Called at the end of a transfer requsite 
     */
    public void endTransferRequsite();

    /**
     * Called when a missing content property is found
     * @param node
     * @param qname
     * @param name
     */
    public void missingContent(NodeRef node, QName qname, String name);
 

}
