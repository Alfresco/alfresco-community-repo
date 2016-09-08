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
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Complete event action
 * 
 * @author Roy Wetherall
 */
public class CompleteEventAction extends RMActionExecuterAbstractBase
{
    public static final String NAME = "completeEvent";
	public static final String PARAM_EVENT_NAME = "eventName";
    public static final String PARAM_EVENT_COMPLETED_BY = "eventCompletedBy";
    public static final String PARAM_EVENT_COMPLETED_AT = "eventCompletedAt";
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_EVENT_NAME, DataTypeDefinition.TEXT, true,
                getParamDisplayLabel(PARAM_EVENT_NAME), false, "rm-ac-manual-events"));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) == true &&
            freezeService.isFrozen(actionedUponNodeRef) == false)
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
                        if (eventCompletedAt == null)
                        {
                            eventCompletedAt = new Date();
                        }
    
                        if (eventCompletedBy == null)
                        {
                            eventCompletedBy = AuthenticationUtil.getRunAsUser();
                        }
    
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
                        // RM-695: Commenting error handling out. If the current disposition stage does not define the event being completed nothing should happen.
                        // throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_EVENT_NO_DISP_LC, eventName));
                    }
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
}
