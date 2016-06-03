package org.alfresco.service.cmr.transfer;

import java.util.Set;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.repo.transfer.TransferModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Policies raised by the transfer service.
 * @author Brian
 *
 */
public interface TransferServicePolicies 
{
    /**
     * Invoked immediately before processing of a new inbound transfer is started.
     * Reported against the "trx:transferRecord" type ({@link TransferModel#TYPE_TRANSFER_RECORD} 
     */
    public interface BeforeStartInboundTransferPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeStartInboundTransfer");
        
        /**
         * Invoked immediately before processing of a new inbound transfer is started.
         * Reported against the "trx:transferRecord" type ({@link TransferModel#TYPE_TRANSFER_RECORD} 
         */
        public void beforeStartInboundTransfer();
    }
    
    /**
     * Invoked immediately after processing of a new inbound transfer is started. This policy is
     * invoked within the transaction on which the transfer lock is written. 
     * Reported against the "trx:transferRecord" type ({@link TransferModel#TYPE_TRANSFER_RECORD} 
     */
    public interface OnStartInboundTransferPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onStartInboundTransfer");

        /**
         * Invoked immediately after processing of a new inbound transfer is started. This policy is
         * invoked within the transaction on which the transfer lock is written. 
         * Reported against the "trx:transferRecord" type ({@link TransferModel#TYPE_TRANSFER_RECORD} 
         * @param transferId The identifier of the transfer that has been started 
         */
        public void onStartInboundTransfer(String transferId);
    }
    
    /**
     * Invoked immediately after completion of processing of an inbound transfer.
     * Reported against the "trx:transferRecord" type ({@link TransferModel#TYPE_TRANSFER_RECORD} 
     */
    public interface OnEndInboundTransferPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onEndInboundTransfer");

        /**
         * Invoked immediately after completion of processing of an inbound transfer.
         * Reported against the "trx:transferRecord" type ({@link TransferModel#TYPE_TRANSFER_RECORD} 
         * @param transferId The identifier of transfer that has ended
         * @param createdNodes The set of nodes that have been created by this transfer
         * @param updatedNodes The set of nodes that have been updated by this transfer
         * @param deletedNodes The set of nodes that have been deleted by this transfer
         */
        public void onEndInboundTransfer(String transferId, Set<NodeRef> createdNodes, 
                Set<NodeRef> updatedNodes, Set<NodeRef> deletedNodes);
    }
}
