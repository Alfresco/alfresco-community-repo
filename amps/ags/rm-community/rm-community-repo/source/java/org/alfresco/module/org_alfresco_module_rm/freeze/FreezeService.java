/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.freeze;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Freeze Service Interface
 *
 * TODO should be deprecated and methods moved to the HoldService with "hold, held, etc" style names
 *
 * @author Roy Wetherall
 * @since 2.0
 */
@AlfrescoPublicApi
public interface FreezeService
{
    /**
     * Indicates whether the passed node reference is frozen.
     *
     * @param nodeRef   node reference
     * @return boolean  true if frozen, false otherwise
     */
    boolean isFrozen(NodeRef nodeRef);

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

   /**
    * @deprecated as of 2.2, use {@link HoldService#isHold(NodeRef)} instead.
    */
    @Deprecated
   boolean isHold(NodeRef nodeRef);

   /**
    * @deprecated as of 2.2, use {@link HoldService#getHeld(NodeRef)} instead.
    */
    @Deprecated
   Set<NodeRef> getFrozen(NodeRef hold);

   /**
    * @deprecated as of 2.2, use {@link HoldService#createHold(NodeRef, String, String, String)} and {@link HoldService#addToHold(NodeRef, NodeRef)} instead.
    */
   @Deprecated
   NodeRef freeze(String reason, NodeRef nodeRef);

   /**
    * @deprecated as of 2.2, use {@link HoldService#addToHold(NodeRef, NodeRef)} instead.
    */
   @Deprecated
   void freeze(NodeRef hold, NodeRef nodeRef);

   /**
    * @deprecated as of 2.2, use {@link HoldService#createHold(NodeRef, String, String, String)} and
    * {@link HoldService#addToHold(NodeRef, List)} instead.
    */
   @Deprecated
   NodeRef freeze(String reason, Set<NodeRef> nodeRefs);

   /**
    * @deprecated as of 2.2, use {@link HoldService#addToHold(NodeRef, List)} instead.
    */
   @Deprecated
   void freeze(NodeRef hold, Set<NodeRef> nodeRefs);

   /**
    * @deprecated as of 2.2, use {@link HoldService#removeFromHold(NodeRef, NodeRef)} instead.
    */
   @Deprecated
   void unFreeze(NodeRef nodeRef);

   /**
    * @deprecated as of 2.2, use {@link HoldService#removeFromHolds(java.util.List, NodeRef)} instead.
    */
   @Deprecated
   void unFreeze(Set<NodeRef> nodeRefs);

   /**
    * @deprecated as of 2.2, use {@link HoldService#deleteHold(NodeRef)} instead.
    */
   @Deprecated
   void relinquish(NodeRef hold);

   /**
    * @deprecated as of 2.2, use {@link HoldService#getHoldReason(NodeRef)} instead.
    */
   @Deprecated
   String getReason(NodeRef hold);

   /**
    * @deprecated as of 2.2, use {@link HoldService#setHoldReason(NodeRef, String)} instead.
    */
   @Deprecated
   void updateReason(NodeRef hold, String reason);

   /**
    * @deprecated as of 2.2, use {@link HoldService#getHolds(NodeRef)} instead.
    */
   @Deprecated
   Set<NodeRef> getHolds(NodeRef filePlan);

    /**
     * Check given node or its children are frozen
     * The node should be record or record folder for retention schedule
     *
     * @param nodeRef
     */
    boolean isFrozenOrHasFrozenChildren(NodeRef nodeRef);
}
