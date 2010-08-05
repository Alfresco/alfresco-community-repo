package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class groups together the business logic for alien nodes which are 
 * transferred nodes that contain children from another repository.
 * <p>
 * Alien nodes cannot be deleted through the transfer service, instead they are
 * "pruned"
 * 
 * <p>
 * This class owns the aspect trx:alien (TransferModel.ASPECT_ALIEN)
 */
public interface AlienProcessor
{
    /**
     * Prune the given node of aliens from the specified repositoryId
     * @param parentNodeRef the root to prune
     * @param fromRepositoryId the repositoryId to prune.
     */
    public void pruneNode(NodeRef parentNodeRef, String fromRepositoryId);

    /**
     * Has the node been invaded by aliens ?
     * @param nodeRef the node to check
     * @return true the node has been invaded by aliens.
     */
    public boolean isAlien(NodeRef nodeRef);
    
    /**
     * Called before deleting an alien node.
     * 
     * @param nodeBeingDeleted node about to be deleted
     */
    public void beforeDeleteAlien(NodeRef deletedNodeRef);
 
    /**
     * Called before creating a child of a transferred node.
     * 
     * When a new node is created as a child of a Transferred or Alien node then
     * the new node needs to be marked as an alien. 
     * <p>
     * Then the tree needs to be walked upwards to mark all parent 
     * transferred nodes as alien.
     * 
     * @param childAssocRef the association ref to the new node
     * @param repositoryId - the repositoryId of the system who owns the new node.
     */
    public void onCreateChild(ChildAssociationRef childAssocRef, String repositoryId);
 
}
