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
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Undo event action
 *
 * @author Roy Wetherall
 */
public class UndoEventAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_EVENT_NOT_DONE = "rm.action.event-not-undone";

    /** Params */
    public static final String PARAM_EVENT_NAME = "eventName";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        String eventName = (String)action.getParameterValue(PARAM_EVENT_NAME);

        if (this.nodeService.hasAspect(actionedUponNodeRef, ASPECT_DISPOSITION_LIFECYCLE))
        {
            // Get the next disposition action
            DispositionAction da = this.dispositionService.getNextDispositionAction(actionedUponNodeRef);
            if (da != null)
            {
                // Get the disposition event
                EventCompletionDetails event = getEvent(da, eventName);
                if (event != null)
                {
                    // Update the event so that it is undone
                    NodeRef eventNodeRef = event.getNodeRef();
                    Map<QName, Serializable> props = this.nodeService.getProperties(eventNodeRef);
                    props.put(PROP_EVENT_EXECUTION_COMPLETE, false);
                    props.put(PROP_EVENT_EXECUTION_COMPLETED_AT, null);
                    props.put(PROP_EVENT_EXECUTION_COMPLETED_BY, null);
                    this.nodeService.setProperties(eventNodeRef, props);

                    // Check to see if the events eligible property needs to be updated
                    updateEventEigible(da);

                }
                else
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EVENT_NOT_DONE, eventName));
                }
            }
        }
    }

    /**
     * Get the event from the disposition action
     *
     * @param da
     * @param eventName
     * @return
     */
    private EventCompletionDetails getEvent(DispositionAction da, String eventName)
    {
        EventCompletionDetails result = null;
        List<EventCompletionDetails> events = da.getEventCompletionDetails();
        for (EventCompletionDetails event : events)
        {
            if (eventName.equals(event.getEventName()))
            {
                result = event;
                break;
            }
        }
        return result;
    }

    /**
     *
     * @param da
     * @param nodeRef
     */
    private void updateEventEigible(DispositionAction da)
    {
        List<EventCompletionDetails> events = da.getEventCompletionDetails();

        boolean eligible = false;
        if (!da.getDispositionActionDefinition().eligibleOnFirstCompleteEvent())
        {
            eligible = true;
            for (EventCompletionDetails event : events)
            {
                if (!event.isEventComplete())
                {
                    eligible = false;
                    break;
                }
            }
        }
        else
        {
            for (EventCompletionDetails event : events)
            {
                if (event.isEventComplete())
                {
                    eligible = true;
                    break;
                }
            }
        }

        // Update the property with the eligible value
        this.nodeService.setProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE, eligible);
    }


    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // TODO add parameter definitions ....
        // eventName

    }
}
