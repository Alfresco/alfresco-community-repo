 
package org.alfresco.module.org_alfresco_module_rm.transfer;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Transfer Service Interface
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public interface TransferService
{
    /**
     * Indicates whether the given node is a transfer (container) or not.
     *
     * @param nodeRef   node reference
     * @return boolean  true if transfer, false otherwise
     *
     * @since 2.0
     */
    boolean isTransfer(NodeRef nodeRef);

    /**
     * Create the transfer node and link the disposition lifecycle node beneath it
     *
     * @param nodeRef       node reference to transfer
     * @param isAccession   Indicates whether this transfer is an accession or not
     * @return Returns the transfer object node reference
     *
     * @since 2.2
     */
    NodeRef transfer(NodeRef nodeRef, boolean isAccession);

    /**
     * Completes the transfer for the given node.
     *
     * @param nodeRef       node reference to complete the transfer
     * 
     * @since 2.2
     */
    void completeTransfer(NodeRef nodeRef);
}
