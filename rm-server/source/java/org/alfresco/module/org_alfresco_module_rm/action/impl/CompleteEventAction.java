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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Complete event action
 * 
 * @author Roy Wetherall
 */
public class CompleteEventAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_EVENT_NO_DISP_LC = "rm.action.event-no-disp-lc";
    
    public static final String PARAM_EVENT_NAME = "eventName";
    public static final String PARAM_EVENT_COMPLETED_BY = "eventCompletedBy";
    public static final String PARAM_EVENT_COMPLETED_AT = "eventCompletedAt";
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        String eventName = (String)action.getParameterValue(PARAM_EVENT_NAME);
        String eventCompletedBy = (String)action.getParameterValue(PARAM_EVENT_COMPLETED_BY);
        Date eventCompletedAt = (Date)action.getParameterValue(PARAM_EVENT_COMPLETED_AT);
        
        if (this.nodeService.hasAspect(actionedUponNodeRef, ASPECT_DISPOSITION_LIFECYCLE) == true)
        {
            // Get the next disposition action
            DispositionAction da = this.dispositionService.getNextDispositionAction(actionedUponNodeRef);
            if (da != null)
            {
                // Get the disposition event
                EventCompletionDetails event = getEvent(da, eventName);
                if (event != null)
                {
                    // Update the event so that it is complete
                    NodeRef eventNodeRef = event.getNodeRef();
                    Map<QName, Serializable> props = this.nodeService.getProperties(eventNodeRef);
                    props.put(PROP_EVENT_EXECUTION_COMPLETE, true);
                    props.put(PROP_EVENT_EXECUTION_COMPLETED_AT, eventCompletedAt);
                    props.put(PROP_EVENT_EXECUTION_COMPLETED_BY, eventCompletedBy);
                    this.nodeService.setProperties(eventNodeRef, props);
                    
                    // Check to see if the events eligible property needs to be updated
                    updateEventEligible(da);
                    
                }
                else
                {
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EVENT_NO_DISP_LC, eventName));
                }
            }
        }
    }
    
    /**
     * Get the event from the dispostion action
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
            if (eventName.equals(event.getEventName()) == true)
            {
                result = event;
                break;
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // TODO add parameter definitions ....
        // eventId, executeBy, executedAt

    }

    @Override
    public Set<QName> getProtectedProperties()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(PROP_EVENT_EXECUTION_COMPLETE);
        qnames.add(PROP_EVENT_EXECUTION_COMPLETED_AT);
        qnames.add(PROP_EVENT_EXECUTION_COMPLETED_BY);
        return qnames;
    }

    
    
    @Override
    public Set<QName> getProtectedAspects()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(ASPECT_DISPOSITION_LIFECYCLE);
        return qnames;
    }

    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
        String eventName = null;
        if(parameters != null)
        {
            eventName = (String) parameters.get(PARAM_EVENT_NAME);
        }

        if (this.nodeService.hasAspect(filePlanComponent, ASPECT_DISPOSITION_LIFECYCLE))
        {
            // Get the next disposition action
            DispositionAction da = this.dispositionService.getNextDispositionAction(filePlanComponent);
            if (da != null)
            {
                // Get the disposition event
                if(parameters != null)
                {
                    EventCompletionDetails event = getEvent(da, eventName);
                    if (event != null)
                    {
                        return true;
                    }
                    else
                    {
                        if (throwException)
                        {
                            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EVENT_NO_DISP_LC, eventName));
                        }
                        else
                        {
                            return false;
                        }
                    }
                }
                else
                {
                    return true;
                }
            }
        }
        return false;
    }

}
