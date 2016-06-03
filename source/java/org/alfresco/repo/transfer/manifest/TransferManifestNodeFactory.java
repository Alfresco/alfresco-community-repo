package org.alfresco.repo.transfer.manifest;

import org.alfresco.repo.transfer.TransferContext;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferDefinition;

public interface TransferManifestNodeFactory
{
    /**
     * Create an object that represents the specified node in a form that can be used to transfer it elsewhere.
     * Calling this operation is identical to calling {@link TransferManifestNodeFactory#createTransferManifestNode(NodeRef, TransferDefinition, TransferContext, boolean)}
     * specifying <code>false</code> as the value of the <code>forceDelete</code> parameter.
     * @param nodeRef The identifier of the node to be distilled for transfer
     * @param definition The transfer definition against which the node is being transferred
     * @param transferContext internal runtime context of a transfer
     * @return An object that holds a snapshot of the state of the specified node suitable for transfer elsewhere. 
     */
    TransferManifestNode createTransferManifestNode(NodeRef nodeRef, TransferDefinition definition, TransferContext transferContext);

    /**
     * Create an object that represents the specified node in a form that can be used to transfer it elsewhere
     * @param nodeRef The identifier of the node to be distilled for transfer
     * @param definition The transfer definition against which the node is being transferred
     * @param forceDelete If this flag is set then the returned TransferManifestNode object will represent the removal
     * of the specified node, even if the node still exists in this repository. This allows a node to be removed from the
     * target repository even if it hasn't been removed in the source repository.
     * @param transferContext internal runtime context of a transfer
     * @return An object that holds a snapshot of the state of the specified node suitable for transfer elsewhere. 
     */
    TransferManifestNode createTransferManifestNode(NodeRef nodeRef, TransferDefinition definition, TransferContext transferContext, boolean forceDelete);
}
