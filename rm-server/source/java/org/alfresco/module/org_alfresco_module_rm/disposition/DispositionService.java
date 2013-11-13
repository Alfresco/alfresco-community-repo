/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.disposition.property.DispositionProperty;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Disposition service interface.
 *
 * @author Roy Wetherall
 * @since 2.0
 */
public interface DispositionService
{
    /** ========= Disposition Property Methods ========= */

    /**
     * Register a disposition property.
     *
     * @param dispositionProperty   disposition property
     */
    void registerDispositionProperty(DispositionProperty dispositionProperty);

    /**
     * Returns the list of disposition period properties that apply given the context provided.
     *
     * @return filtered list of disposition period properties
     */
    Collection<DispositionProperty> getDispositionProperties(boolean isRecordLevel, String dispositionAction);
    Collection<DispositionProperty> getDispositionProperties();


    /** ========= Disposition Schedule Methods ========= */

    /**
     * Get the disposition schedule for a given record management node.  Traverses the hierarchy to
     * find the first disposition schedule in the primary hierarchy.
     *
     * @param nodeRef   node reference to record category, record folder or record
     * @return {@link DispositionSchedule}  disposition schedule
     */
    DispositionSchedule getDispositionSchedule(NodeRef nodeRef);

    // Gets all the disposition schedules, not just the first in the primary parent path.
    // TODO List<DispositionSchedule> getAllDispositionSchedules(NodeRef nodeRef);

    /**
     * Get the disposition schedule directly associated with the node specified.  Returns
     * null if none.
     *
     * @param nodeRef   node reference
     * @return {@link DispositionSchedule}  disposition schedule directly associated with the node reference, null if none
     */
    DispositionSchedule getAssociatedDispositionSchedule(NodeRef nodeRef);

    /**
     * Gets the records management container that is directly associated with the disposition schedule.
     *
     * @param dispositionSchedule   disposition schedule
     * @return {@link NodeRef}  node reference of the associated container
     */
    NodeRef getAssociatedRecordsManagementContainer(DispositionSchedule dispositionSchedule);

    /**
     * Indicates whether a disposition schedule has any disposable items under its management
     *
     * @param dispositionSchdule	disposition schedule
     * @return boolean	true if there are disposable items being managed by, false otherwise
     */
    boolean hasDisposableItems(DispositionSchedule dispositionSchdule);

    /**
     * Gets a list of all the disposable items (records, record folders) that are under the control of
     * the disposition schedule.
     *
     * @param dispositionSchedule   disposition schedule
     * @return {@link List}<{@link NodeRef}>    list of disposable items
     */
    List<NodeRef> getDisposableItems(DispositionSchedule dispositionSchedule);

    /**
     * Indicates whether the node is a disposable item or not (ie is under the control of a disposition schedule)
     *
     * @param nodeRef   node reference
     * @return boolean  true if node is a disposable item, false otherwise
     */
    boolean isDisposableItem(NodeRef nodeRef);

    /**
     * Creates a disposition schedule on the given record category.
     *
     * @param recordCategory
     * @param props
     * @return {@link DispositionSchedule}
     */
    DispositionSchedule createDispositionSchedule(NodeRef recordCategory, Map<QName, Serializable> props);

    // TODO DispositionSchedule updateDispositionSchedule(DispositionScedule, Map<QName, Serializable> props)

    // TODO void removeDispositionSchedule(NodeRef nodeRef); - can only remove if no disposition items

    /** ========= Disposition Action Definition Methods ========= */

    /**
     * Adds a new disposition action definition to the given disposition schedule.
     *
     * @param schedule The DispositionSchedule to add to
     * @param actionDefinitionParams Map of parameters to use to create the action definition
     */
    DispositionActionDefinition addDispositionActionDefinition(
                DispositionSchedule schedule,
                Map<QName, Serializable> actionDefinitionParams);

    /**
     * Removes the given disposition action definition from the given disposition
     * schedule.
     *
     * @param schedule The DispositionSchedule to remove from
     * @param actionDefinition The DispositionActionDefinition to remove
     */
    void removeDispositionActionDefinition(
                DispositionSchedule schedule,
                DispositionActionDefinition actionDefinition);

    /**
     * Updates the given disposition action definition belonging to the given disposition
     * schedule.
     *
     * @param actionDefinition The DispositionActionDefinition to update
     * @param actionDefinitionParams Map of parameters to use to update the action definition
     * @return The updated DispositionActionDefinition
     */
    DispositionActionDefinition updateDispositionActionDefinition(
                DispositionActionDefinition actionDefinition,
                Map<QName, Serializable> actionDefinitionParams);


    /** ========= Disposition Action Methods ========= */

    /**
     * Indicates whether the next disposition action is eligible or not.
     *
     * @param nodeRef   node reference to disposable item
     * @return boolean  true if next disposition action is eligible, false otherwise
     */
    boolean isNextDispositionActionEligible(NodeRef nodeRef);

    /**
     * Gets the next disposition action for a given node
     *
     * @param nodeRef               node reference to disposable item
     * @return DispositionAction    next disposition action, null if none
     */
    DispositionAction getNextDispositionAction(NodeRef nodeRef);

    // TODO void startNextDispositionAction(NodeRef nodeRef);

    // TODO void completeNextDispositionAction(NodeRef nodeRef);


    /** ========= Disposition Action History Methods ========= */

    /**
     * Gets a list of all the completed disposition action in the order they occured.
     *
     * @param nodeRef                       record/record folder
     * @return List<DispositionAction>      list of completed disposition actions
     */
    List<DispositionAction> getCompletedDispositionActions(NodeRef nodeRef);

    /**
     * Helper method to get the last completed disposition action.  Returns null
     * if there is none.
     *
     * @param nodeRef               record/record folder
     * @return DispositionAction    last completed disposition action, null if none
     */
    DispositionAction getLastCompletedDispostionAction(NodeRef nodeRef);

    /**
     * Indicates whether the item is cutoff or not.
     *
     * @param nodeRef   node reference
     * @return boolean  true if the item is cutoff, false otherwise
     *
     * @since 2.0
     */
    boolean isCutoff(NodeRef nodeRef);
}
