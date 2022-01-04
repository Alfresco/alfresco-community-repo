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

package org.alfresco.module.org_alfresco_module_rm.hold;

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hold service interface.
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
@AlfrescoPublicApi
public interface HoldService
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
     * Gets the list of all the holds within the holds container in the given file plan
     *
     * @param filePlan The {@link NodeRef} of the file plan
     * @return List of hold node references
     */
    List<NodeRef> getHolds(NodeRef filePlan);

    /**
     * Gets the node reference for the hold with the given name in the given file plan
     *
     * @param name {@link String} The name of the hold
     * @return {@link NodeRef} of the hold with the given name
     */
    NodeRef getHold(NodeRef filePlan, String name);

    /**
     * Gets the list of all the holds within the holds container for the given node reference
     *
     * @param nodeRef The {@link NodeRef} of the record / record folder /active content
     * @param includedInHold <code>true</code> to retrieve the list of hold node references which will include the node reference
     * <code>false</code> to get a list of node references which will not have the given node reference
     * @return List of hold node references
     */
    List<NodeRef> heldBy(NodeRef nodeRef, boolean includedInHold);

    /**
     * Gets the list of item node references which are in the given hold
     *
     * @param hold {@link NodeRef} of the hold
     * @return Lost of item {@link NodeRef}s which are in the given hold
     */
    List<NodeRef> getHeld(NodeRef hold);

    /**
     * Creates a hold with the given name, reason and description for the given file plan
     *
     * @param filePlan The {@link NodeRef} of the file plan
     * @param name {@link String} The name of the hold
     * @param reason {@link String} The reason of the hold
     * @param description {@link String} The description of the hold
     * @return The {@link NodeRef} of the created hold
     */
    NodeRef createHold(NodeRef filePlan, String name, String reason, String description);

    /**
     * Gets the hold reason for the given hold node reference
     *
     * @param hold The {@link NodeRef} of the hold
     * @return {@link String} The reason of the hold
     */
    String getHoldReason(NodeRef hold);

    /**
     * Sets the hold reason
     *
     * @param hold The {@link NodeRef} of the hold
     * @param reason {@link String} The reason for the hold
     */
    void setHoldReason(NodeRef hold, String reason);

    /**
     * Deletes the hold
     *
     * @param hold The {@link NodeRef} of the hold
     */
    void deleteHold(NodeRef hold);

    /**
     * Adds the item to the given hold
     *
     * @param hold    The {@link NodeRef} of the hold
     * @param nodeRef The {@link NodeRef} of the record / record folder / active content which will be added to the given hold
     */
    void addToHold(NodeRef hold, NodeRef nodeRef);

    /**
     * Adds the items to the the given hold
     *
     * @param hold The {@link NodeRef} of the hold to which the items will be added
     * @param nodeRefs The item {@link NodeRef}s which will be added to the hold
     */
    void addToHold(NodeRef hold, List<NodeRef> nodeRefs);

    /**
     * Adds the item to the given list of holds
     *
     * @param holds The list of {@link NodeRef}s of the holds
     * @param nodeRef The {@link NodeRef} of the record / record folder / active content which will be added to the given holds
     */
    void addToHolds(List<NodeRef> holds, NodeRef nodeRef);

    /**
     * Adds the given items to the given list of holds
     *
     * @param holds List of holds to which the given items will be added
     * @param nodeRefs The list of items which will be added to the given holds
     */
    void addToHolds(List<NodeRef> holds, List<NodeRef> nodeRefs);

    /**
     * Removes the record from the given hold
     *
     * @param hold The {@link NodeRef} of the hold
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be removed from the given hold
     */
    void removeFromHold(NodeRef hold, NodeRef nodeRef);

    /**
     * Removes the given items from the given hold
     *
     * @param hold The hold {@link NodeRef} from which the given items will be removed
     * @param nodeRefs The list of items which will be removed from the given holds
     */
    void removeFromHold(NodeRef hold, List<NodeRef> nodeRefs);

    /**
     * Removes the item from the given list of hold
     *
     * @param holds The list {@link NodeRef}s of the holds
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be removed from the given holds
     */
    void removeFromHolds(List<NodeRef> holds, NodeRef nodeRef);

    /**
     * Removes the items from the given holds
     *
     * @param holds List of hold {@link NodeRef}s from which the items will be removed
     * @param nodeRefs List of item {@link NodeRef}s which will be removed from the given holds
     */
    void removeFromHolds(List<NodeRef> holds, List<NodeRef> nodeRefs);

    /**
     * Removes the given {@link NodeRef} from all the holds
     *
     * @param nodeRef The {@link NodeRef} of item which will be removed from all the holds
     */
    void removeFromAllHolds(NodeRef nodeRef);

    /**
     * Removes the given list of {@link NodeRef}s from all the holds
     *
     * @param nodeRefs The list of item {@link NodeRef}s which will be removed from all the holds
     */
    void removeFromAllHolds(List<NodeRef> nodeRefs);
}
