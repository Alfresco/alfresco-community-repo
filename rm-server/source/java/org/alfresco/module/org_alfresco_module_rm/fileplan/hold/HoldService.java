/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.fileplan.hold;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Hold service interface.
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public interface HoldService
{
    /**
     * Gets the list of all the holds within the holds container in the given file plan
     *
     * @param filePlan The {@link NodeRef} of the file plan
     * @return List of hold node references
     */
    List<NodeRef> getHoldsInFilePlan(NodeRef filePlan);

    /**
     * Gets the list of all the holds within the holds container for the given node reference
     *
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be in the retrieved list of hold(s)
     * @return List of hold node references which has the node reference of the given record / record folder in it
     */
    List<NodeRef> getHoldsForItem(NodeRef nodeRef);

    /**
     * Adds the record to the given hold
     *
     * @param hold The {@link NodeRef} of the hold
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be added to the given hold
     */
    void addToHoldContainer(NodeRef hold, NodeRef nodeRef);

    /**
     * Adds the record to the given list of holds
     *
     * @param holds The list of {@link NodeRef}s of the holds
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be added to the given holds
     */
    void addToHoldContainers(List<NodeRef> holds, NodeRef nodeRef);

    /**
     * Removes the record from the given hold
     *
     * @param hold The {@link NodeRef} of the hold
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be removed from the given hold
     */
    void removeFromHoldContainer(NodeRef hold, NodeRef nodeRef);

    /**
     * Removes the record from the given list of hold
     *
     * @param holds The list {@link NodeRef}s of the holds
     * @param nodeRef The {@link NodeRef} of the record / record folder which will be removed from the given holds
     */
    void removeFromHoldContainers(List<NodeRef> holds, NodeRef nodeRef);
}
