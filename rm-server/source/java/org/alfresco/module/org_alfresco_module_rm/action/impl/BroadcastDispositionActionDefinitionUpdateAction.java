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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Action to implement the consequences of a change to the value of the DispositionActionDefinition
 * properties. When these properties are changed on a disposition schedule, then any associated
 * disposition actions may need to be updated as a consequence.
 *
 * @author Neil McErlean
 */
public class BroadcastDispositionActionDefinitionUpdateAction extends RMActionExecuterAbstractBase
{
    /** Logger */
    private static Log logger = LogFactory.getLog(BroadcastDispositionActionDefinitionUpdateAction.class);

    public static final String NAME = "broadcastDispositionActionDefinitionUpdate";
    public static final String CHANGED_PROPERTIES = "changedProperties";

    private BehaviourFilter behaviourFilter;

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (RecordsManagementModel.TYPE_DISPOSITION_ACTION_DEFINITION.equals(nodeService.getType(actionedUponNodeRef)) == false)
        {
            return;
        }

        List<QName> changedProps = (List<QName>)action.getParameterValue(CHANGED_PROPERTIES);

        // Navigate up the containment hierarchy to get the record category grandparent and schedule.
        NodeRef dispositionScheduleNode = nodeService.getPrimaryParent(actionedUponNodeRef).getParentRef();
        NodeRef rmContainer = nodeService.getPrimaryParent(dispositionScheduleNode).getParentRef();
        DispositionSchedule dispositionSchedule = dispositionService.getAssociatedDispositionSchedule(rmContainer);

        behaviourFilter.disableBehaviour();
        try
        {
            List<NodeRef> disposableItems = dispositionService.getDisposableItems(dispositionSchedule);
            for (NodeRef disposableItem : disposableItems)
            {
                updateDisposableItem(dispositionSchedule, disposableItem, actionedUponNodeRef, changedProps);
            }
        }
        finally
        {
            behaviourFilter.enableBehaviour();
        }
    }

    /**
     *
     * @param ds
     * @param disposableItem
     * @param dispositionActionDefinition
     * @param changedProps
     */
    private void updateDisposableItem(DispositionSchedule ds, NodeRef disposableItem, NodeRef dispositionActionDefinition, List<QName> changedProps)
    {
        // We need to check that this folder is under the management of the disposition schedule that
        // has been updated
        DispositionSchedule itemDs = dispositionService.getDispositionSchedule(disposableItem);
        if (itemDs != null &&
            itemDs.getNodeRef().equals(ds.getNodeRef()) == true)
        {
            if (nodeService.hasAspect(disposableItem, ASPECT_DISPOSITION_LIFECYCLE))
            {
                // disposition lifecycle already exists for node so process changes
                processActionDefinitionChanges(dispositionActionDefinition, changedProps, disposableItem);
            }
            else
            {
                // disposition lifecycle does not exist on the node so setup disposition
                dispositionService.updateNextDispositionAction(disposableItem);
            }

            // update rolled up search information
            rollupSearchProperties(disposableItem);
        }
    }

    /**
     * Manually update the rolled up search properties
     *
     * @param disposableItem    disposable item
     */
    private void rollupSearchProperties(NodeRef disposableItem)
    {
        DispositionAction da = dispositionService.getNextDispositionAction(disposableItem);
        if (da != null)
        {
            Map<QName, Serializable> props = nodeService.getProperties(disposableItem);

            props.put(PROP_RS_DISPOSITION_ACTION_NAME, da.getName());
            props.put(PROP_RS_DISPOSITION_ACTION_AS_OF, da.getAsOfDate());
            props.put(PROP_RS_DISPOSITION_EVENTS_ELIGIBLE, nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));

            DispositionActionDefinition daDefinition = da.getDispositionActionDefinition();
            Period period = daDefinition.getPeriod();
            if (period != null)
            {
                props.put(PROP_RS_DISPOSITION_PERIOD, period.getPeriodType());
                props.put(PROP_RS_DISPOSITION_PERIOD_EXPRESSION, period.getExpression());
            }
            else
            {
                props.put(PROP_RS_DISPOSITION_PERIOD, null);
                props.put(PROP_RS_DISPOSITION_PERIOD_EXPRESSION, null);
            }

            List<EventCompletionDetails> events = da.getEventCompletionDetails();
            List<String> list = new ArrayList<String>(events.size());
            for (EventCompletionDetails event : events)
            {
                list.add(event.getEventName());
            }
            props.put(PROP_RS_DISPOSITION_EVENTS, (Serializable)list);

            nodeService.setProperties(disposableItem, props);
        }
    }

    /**
     * Processes all the changes applied to the given disposition
     * action definition node for the given record or folder node.
     *
     * @param dispositionActionDef The disposition action definition node
     * @param changedProps The set of properties changed on the action definition
     * @param recordOrFolder The record or folder the changes potentially need to be applied to
     */
    private void processActionDefinitionChanges(NodeRef dispositionActionDef, List<QName> changedProps, NodeRef recordOrFolder)
    {
        // check that the step being edited is the current step for the folder,
        // if not, the change has no effect on the current step so ignore
        DispositionAction nextAction = dispositionService.getNextDispositionAction(recordOrFolder);
        if (doesChangedStepAffectNextAction(dispositionActionDef, nextAction))
        {
            // the change does effect the nextAction for this node
            // so go ahead and determine what needs updating
            if (changedProps.contains(PROP_DISPOSITION_PERIOD))
            {
                persistPeriodChanges(dispositionActionDef, nextAction);
            }

            if (changedProps.contains(PROP_DISPOSITION_EVENT) || changedProps.contains(PROP_DISPOSITION_EVENT_COMBINATION))
            {
                persistEventChanges(dispositionActionDef, nextAction);
            }

            if (changedProps.contains(PROP_DISPOSITION_ACTION_NAME))
            {
                String action = (String)nodeService.getProperty(dispositionActionDef, PROP_DISPOSITION_ACTION_NAME);
                nodeService.setProperty(nextAction.getNodeRef(), PROP_DISPOSITION_ACTION, action);
            }
        }
    }

    /**
     * Determines whether the disposition action definition (step) being
     * updated has any effect on the given next action
     *
     * @param dispositionActionDef The disposition action definition node
     * @param nextAction The next disposition action
     * @return true if the step change affects the next action
     */
    private boolean doesChangedStepAffectNextAction(NodeRef dispositionActionDef,
                DispositionAction nextAction)
    {
        boolean affectsNextAction = false;

        if (dispositionActionDef != null && nextAction != null)
        {
            // check whether the id of the action definition node being changed
            // is the same as the id of the next action
            String nextActionId = nextAction.getId();
            if (dispositionActionDef.getId().equals(nextActionId))
            {
                affectsNextAction = true;
            }
        }

        return affectsNextAction;
    }

    /**
     * Persists any changes made to the period on the given disposition action
     * definition on the given next action.
     *
     * @param dispositionActionDef The disposition action definition node
     * @param nextAction The next disposition action
     */
    private void persistPeriodChanges(NodeRef dispositionActionDef, DispositionAction nextAction)
    {
        Date newAsOfDate = null;
        Period dispositionPeriod = (Period) nodeService.getProperty(dispositionActionDef, PROP_DISPOSITION_PERIOD);

        if (dispositionPeriod != null)
        {
            // calculate the new as of date as we have been provided a new period
            Date now = new Date();
            newAsOfDate = dispositionPeriod.getNextDate(now);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Set disposition as of date for next action '" + nextAction.getName() +
                        "' (" + nextAction.getNodeRef() + ") to: " + newAsOfDate);
        }

        nodeService.setProperty(nextAction.getNodeRef(), PROP_DISPOSITION_AS_OF, newAsOfDate);
    }

    /**
     * Persists any changes made to the events on the given disposition action
     * definition on the given next action.
     *
     * @param dispositionActionDef The disposition action definition node
     * @param nextAction The next disposition action
     */
    @SuppressWarnings("unchecked")
    private void persistEventChanges(NodeRef dispositionActionDef, DispositionAction nextAction)
    {
        // go through the current events on the next action and remove any that are not present any more
        List<String> stepEvents = (List<String>) nodeService.getProperty(dispositionActionDef, PROP_DISPOSITION_EVENT);
        List<EventCompletionDetails> eventsList = nextAction.getEventCompletionDetails();
        List<String> nextActionEvents = new ArrayList<String>(eventsList.size());
        for (EventCompletionDetails event : eventsList)
        {
            // take note of the event names present on the next action
            String eventName = event.getEventName();
            nextActionEvents.add(eventName);

            // if the event has been removed delete from next action
            if (stepEvents != null && stepEvents.contains(event.getEventName()) == false)
            {
                // remove the child association representing the event
                nodeService.removeChild(nextAction.getNodeRef(), event.getNodeRef());

                if (logger.isDebugEnabled())
                {
                    logger.debug("Removed '" + eventName + "' from next action '" + nextAction.getName() +
                                "' (" + nextAction.getNodeRef() + ")");
                }
            }
        }

        // go through the disposition action definition step events and add any new ones
        if (stepEvents != null)
        {
	        for (String eventName : stepEvents)
	        {
	            if (!nextActionEvents.contains(eventName))
	            {
	                createEvent(recordsManagementEventService.getEvent(eventName), nextAction.getNodeRef());

	                if (logger.isDebugEnabled())
	                {
	                    logger.debug("Added '" + eventName + "' to next action '" + nextAction.getName() +
	                                "' (" + nextAction.getNodeRef() + ")");
	                }
	            }
	        }
        }

        // NOTE: eventsList contains all the events that have been updated!
        // TODO: manually update the search properties for the parent node!

        // finally since events may have changed re-calculate the events eligible flag
        boolean eligible = updateEventEligible(nextAction);

        if (logger.isDebugEnabled())
        {
            logger.debug("Set events eligible flag to '" + eligible + "' for next action '" + nextAction.getName() +
                        "' (" + nextAction.getNodeRef() + ")");
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // Intentionally empty
    }
}
