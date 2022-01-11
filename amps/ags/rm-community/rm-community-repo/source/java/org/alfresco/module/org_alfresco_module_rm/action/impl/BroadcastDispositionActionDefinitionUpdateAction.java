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

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

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
        if (!RecordsManagementModel.TYPE_DISPOSITION_ACTION_DEFINITION.equals(getNodeService().getType(actionedUponNodeRef)))
        {
            return;
        }

        List<QName> changedProps = (List<QName>)action.getParameterValue(CHANGED_PROPERTIES);

        // Navigate up the containment hierarchy to get the record category grandparent and schedule.
        NodeRef dispositionScheduleNode = getNodeService().getPrimaryParent(actionedUponNodeRef).getParentRef();
        NodeRef rmContainer = getNodeService().getPrimaryParent(dispositionScheduleNode).getParentRef();
        DispositionSchedule dispositionSchedule = getDispositionService().getAssociatedDispositionSchedule(rmContainer);

        behaviourFilter.disableBehaviour();
        try
        {
            List<NodeRef> disposableItems = getDispositionService().getDisposableItems(dispositionSchedule);
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
        DispositionSchedule itemDs = getDispositionService().getDispositionSchedule(disposableItem);
        if (itemDs != null &&
            itemDs.getNodeRef().equals(ds.getNodeRef()))
        {
            if (getNodeService().hasAspect(disposableItem, ASPECT_DISPOSITION_LIFECYCLE))
            {
                // disposition lifecycle already exists for node so process changes
                processActionDefinitionChanges(dispositionActionDefinition, changedProps, disposableItem);
            }
            else
            {
                // disposition lifecycle does not exist on the node so setup disposition
                getDispositionService().updateNextDispositionAction(disposableItem);
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
        DispositionAction da = getDispositionService().getNextDispositionAction(disposableItem);
        if (da != null)
        {
            Map<QName, Serializable> props = getNodeService().getProperties(disposableItem);

            props.put(PROP_RS_DISPOSITION_ACTION_NAME, da.getName());
            props.put(PROP_RS_DISPOSITION_ACTION_AS_OF, da.getAsOfDate());
            props.put(PROP_RS_DISPOSITION_EVENTS_ELIGIBLE, getNodeService().getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));

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
            List<String> list = new ArrayList<>(events.size());
            for (EventCompletionDetails event : events)
            {
                list.add(event.getEventName());
            }
            props.put(PROP_RS_DISPOSITION_EVENTS, (Serializable)list);

            getNodeService().setProperties(disposableItem, props);
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
        DispositionAction nextAction = getDispositionService().getNextDispositionAction(recordOrFolder);
        if (doesChangedStepAffectNextAction(dispositionActionDef, nextAction))
        {
            // the change does effect the nextAction for this node
            // so go ahead and determine what needs updating
            if ((changedProps.contains(PROP_DISPOSITION_PERIOD) || changedProps.contains(PROP_DISPOSITION_PERIOD_PROPERTY))
                    && isNotTrue((Boolean) getNodeService().getProperty(nextAction.getNodeRef(), PROP_MANUALLY_SET_AS_OF)))
            {
                persistPeriodChanges(dispositionActionDef, nextAction);
            }

            if (changedProps.contains(PROP_DISPOSITION_EVENT) || changedProps.contains(PROP_DISPOSITION_EVENT_COMBINATION))
            {
                nextAction.refreshEvents();
            }

            if (changedProps.contains(PROP_DISPOSITION_ACTION_NAME))
            {
                String action = (String)getNodeService().getProperty(dispositionActionDef, PROP_DISPOSITION_ACTION_NAME);
                getNodeService().setProperty(nextAction.getNodeRef(), PROP_DISPOSITION_ACTION, action);
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
    protected void persistPeriodChanges(NodeRef dispositionActionDef, DispositionAction nextAction)
    {
        NodeRef dispositionedNode = getNodeService().getPrimaryParent(nextAction.getNodeRef()).getParentRef();
        DispositionActionDefinition definition = nextAction.getDispositionActionDefinition();
        Date newAsOfDate = getDispositionService().calculateAsOfDate(dispositionedNode, definition);

        if (logger.isDebugEnabled())
        {
            logger.debug("Set disposition as of date for next action '" + nextAction.getName() +
                        "' (" + nextAction.getNodeRef() + ") to: " + newAsOfDate);
        }

        getNodeService().setProperty(nextAction.getNodeRef(), PROP_DISPOSITION_AS_OF, newAsOfDate);
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // Intentionally empty
    }
}
