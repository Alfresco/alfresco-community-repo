package org.alfresco.repo.transfer.manifest;

/**
 * Manifest Processor
 * 
 * Interface called when parsing the transfer manifest file
 * 
 * When Parsing the manifest file, the startTransferManifest will be called first, then 
 * processHeader, then mulpiple calls of processTransferManifestNode, one for each node,
 * then endTransferManifest
 *
 * @author Mark Rogers
 */
public interface TransferManifestProcessor
{
    /**
     * Signals the start of a transfer manifest
     */
    public void startTransferManifest();
    
    /**
     * Gives the header to be proceessed
     * @param header the header
     */
    public void processTransferManifiestHeader(TransferManifestHeader header);
        
    /**
     * Gives a manifest node to be processed
     * @param node the node
     */
    public void processTransferManifestNode(TransferManifestNormalNode node); 
    
    /**
     * Gives a deleted manifest node to be processed
     * @param node the node
     */
    public void processTransferManifestNode(TransferManifestDeletedNode node); 
    
    /**
     * Signals the end of a transfer manifest
     */
    public void endTransferManifest();
    
}
