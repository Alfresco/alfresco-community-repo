/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.freeze;

import java.util.Date;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Freeze Service Interface
 * 
 * TODO
 * Implementation used to consolidate freeze behaviours in 2.0. 
 * When implementing consider application of freeze to 'any' node references, not just records and record folders.
 * (Consider implications for security and capabilities)
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public interface FreezeService 
{
   /**
    * Indicates whether the passed node reference is a hold.  A hold is a container for a group of frozen object and contains the freeze
    * reason.
    * 
    * @param nodeRef   hold node reference
    * @return boolean  true if hold, false otherwise
    */
   boolean isHold(NodeRef nodeRef);

   /**
    * Indicates whether the passed node reference is frozen.
    * 
    * @param nodeRef   node reference
    * @return boolean  true if frozen, false otherwise
    */
   boolean isFrozen(NodeRef nodeRef);

   /**
    * Get the 'root' frozen node references in a hold.
    * 
    * @param hold          hold node reference
    * @return Set<NodeRef> frozen node references
    */
   Set<NodeRef> getFrozen(NodeRef hold);

   /**
    * Freezes a node with the provided reason, creating a hold node reference.
    * 
    * @param reason    freeze reason
    * @param nodeRef   node reference
    * @return NodeRef  hold node reference
    */
   NodeRef freeze(String reason, NodeRef nodeRef);

   /**
    * Freezes a node, adding it an existing hold.
    * 
    * @param hold      hold node reference
    * @param nodeRef   node reference
    */
   void freeze(NodeRef hold, NodeRef nodeRef);

   /**
    * Freezes a collection of nodes with the given reason, creating a hold.
    * 
    * @param reason        freeze reason
    * @param nodeRefs  set of nodes to freeze 
    * @return NodeRef      hold node reference
    */
   NodeRef freeze(String reason, Set<NodeRef> nodeRefs);

   /**
    * Freeze a collection of nodes, adding them to an existing hold.
    * 
    * @param hold      hold node reference
    * @param nodeRefs   set of nodes to freeze
    */
   void freeze(NodeRef hold, Set<NodeRef> nodeRefs);

   /**
    * Unfreeze a frozen node.  
    * <p>
    * The unfrozen node is automatically removed from the hold(s) it is in.  If the hold is
    * subsequently empty, the hold is automatically deleted.
    * 
    * @param nodeRef   node reference
    */
   void unFreeze(NodeRef nodeRef);

   /**
    * Unfreeze a collection of nodes.
    * <p>
    * The unfrozen nodes are automatically removed from the hold(s) the are in.  If the hold(s) is
    * subsequently empty, the hold is automatically deleted.
    * 
    * @param nodeRefs  set of nodes to unfreeze
    */
   void unFreeze(Set<NodeRef> nodeRefs);

   /**
    * Unfreezes all nodes within a hold and deletes the hold.
    * 
    * @param hold  hold node reference
    */
   void relinquish(NodeRef hold);

   /**
    * Gets the freeze reason for a hold.
    * 
    * @param hold      hold node reference
    * @return String   freeze reason
    */
   String getReason(NodeRef hold);

   /**
    * Updates the freeze reason for a given hold.
    * 
    * @param hold      hold node reference
    * @param reason    updated reason
    */
   void updateReason(NodeRef hold, String reason);

   /**
    * Gets the hold node references for a given file plan
    * or an empty set if there is not any hold node available
    * 
    * @param filePlan      file plan for which the hold nodes will be retrieved
    * @return Set<NodeRef> hold node references
    */
   Set<NodeRef> getHolds(NodeRef filePlan);

   /**
    * Checks whether or not the given node has frozen children
    * 
    * @param nodeRef The nodeRef for which will be checked if it has frozen children
    * @return true   if the given nodeRef has frozen children, false otherwise
    */
   boolean hasFrozenChildren(NodeRef nodeRef);

   /**
    * Gets the date of the freeze for the given node, null if the node is not frozen
    * 
    * @param nodeRef The nodeRef for which the date check will be performed
    * @return Date   The of the freeze or null 
    */
   Date getFreezeDate(NodeRef nodeRef);

   /**
    * Gets the initiator of the freeze for the given node, null if the node is not frozen
    * 
    * @param nodeRef The nodeRef for which the initiator check will be performed
    * @return String The initiator of the freeze or null
    */
   String getFreezeInitiator(NodeRef nodeRef);
}
