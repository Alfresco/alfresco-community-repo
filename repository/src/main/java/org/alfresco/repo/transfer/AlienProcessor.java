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
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class groups together the business logic for alien nodes which are transferred nodes that contain children from another repository.
 * <p>
 * Alien nodes cannot be deleted through the transfer service, instead they are "pruned"
 * 
 * <p>
 * This class owns the aspect trx:alien (TransferModel.ASPECT_ALIEN)
 */
public interface AlienProcessor
{
    /**
     * Prune the given node of aliens from the specified repositoryId.
     * <p>
     * So any children that are only invaded by the specified repository are deleted.
     * <p>
     * Folders which are invaded by more than one repository will remain.
     * 
     * @param parentNodeRef
     *            the root to prune
     * @param fromRepositoryId
     *            the repositoryId to prune.
     */
    public void pruneNode(NodeRef parentNodeRef, String fromRepositoryId);

    /**
     * Has the node been invaded by aliens ?
     * 
     * @param nodeRef
     *            the node to check
     * @return true the node has been invaded by aliens.
     */
    public boolean isAlien(NodeRef nodeRef);

    /**
     * Called before creating a child of a transferred node.
     * <p>
     * When a new node is created as a child of a Transferred or Alien node then the new node needs to be marked as an alien.
     * <p>
     * Then the tree needs to be walked upwards to mark all parent transferred nodes as alien.
     * 
     * @param childAssocRef
     *            the association ref to the new node
     * @param repositoryId
     *            - the repositoryId of the system who owns the new node.
     * @param isNewNode
     *            - is this a new nide
     */
    public void onCreateChild(ChildAssociationRef childAssocRef, String repositoryId, boolean isNewNode);

    /**
     * Called when an alien node has been moved from one parent to another.
     * <p>
     * If the new parent is transferred or alien may make the new parent an alien.
     * <p>
     * The alien node may also stop being an alien node.
     */
    public void afterMoveAlien(ChildAssociationRef newAssocRef);

    /**
     * Called before deleting an alien node.
     * <p>
     * The tree needs to be walked upwards to take account of the removed alien node.
     * 
     * @param deletedNodeRef
     *            node about to be deleted
     * @param oldRef
     *            null if the deleted node is still "in place" and readable else the old ref prior to the node being moved.
     */
    public void beforeDeleteAlien(NodeRef deletedNodeRef, ChildAssociationRef oldRef);
}
