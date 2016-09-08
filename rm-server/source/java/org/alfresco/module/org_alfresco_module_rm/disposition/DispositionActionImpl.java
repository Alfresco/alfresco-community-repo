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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * @author Roy Wetherall
 */
public class DispositionActionImpl implements DispositionAction, 
                                              RecordsManagementModel
{
    private RecordsManagementServiceRegistry services;
    private NodeRef dispositionNodeRef;
    private DispositionActionDefinition dispositionActionDefinition;    
    
    /**
     * Constructor 
     * 
     * @param services
     * @param dispositionActionNodeRef
     */
    public DispositionActionImpl(RecordsManagementServiceRegistry services, NodeRef dispositionActionNodeRef)    
    {
        this.services = services;
        this.dispositionNodeRef = dispositionActionNodeRef;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getDispositionActionDefinition()
     */
    public DispositionActionDefinition getDispositionActionDefinition()
    {
        if (this.dispositionActionDefinition == null)
        {
            // Get the current action
            String id = (String)services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_ID);
            
            // Get the disposition instructions for the owning node
            NodeRef recordNodeRef = this.services.getNodeService().getPrimaryParent(this.dispositionNodeRef).getParentRef();
            if (recordNodeRef != null)
            {
                DispositionSchedule ds = this.services.getDispositionService().getDispositionSchedule(recordNodeRef);
            
                if (ds != null)
                {
                    // Get the disposition action definition
                    this.dispositionActionDefinition = ds.getDispositionActionDefinition(id);
                }
            }
        }
        
        return this.dispositionActionDefinition;
        
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getNodeRef()
     */
    public NodeRef getNodeRef()
    {
       return this.dispositionNodeRef;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getLabel()
     */
    public String getLabel()
    {
        String name = getName();
        String label = name;
        
        // get the disposition action from the RM action service
        RecordsManagementAction action = this.services.getRecordsManagementActionService().getDispositionAction(name);
        if (action != null)
        {
            label = action.getLabel();
        }
        
        return label;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getId()
     */
    public String getId()
    {
        return (String)this.services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_ID);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getName()
     */
    public String getName()
    {
        return (String)this.services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getAsOfDate()
     */
    public Date getAsOfDate()
    {
        return (Date)this.services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_AS_OF);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#isEventsEligible()
     */
    public boolean isEventsEligible()
    {
        return ((Boolean)this.services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_EVENTS_ELIGIBLE)).booleanValue();
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getCompletedAt()
     */
    public Date getCompletedAt()
    {
        return (Date)this.services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_COMPLETED_AT);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getCompletedBy()
     */
    public String getCompletedBy()
    {
        return (String)this.services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_COMPLETED_BY);
    }

    /*
     * @see org.alfresco.module.org_alfresco_module_rm.DispositionAction#getStartedAt()
     */
    public Date getStartedAt()
    {
        return (Date)this.services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_STARTED_AT);
    }

    /*
     * @see org.alfresco.module.org_alfresco_module_rm.DispositionAction#getStartedBy()
     */
    public String getStartedBy()
    {
        return (String)this.services.getNodeService().getProperty(this.dispositionNodeRef, PROP_DISPOSITION_ACTION_STARTED_BY);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction#getEventCompletionDetails()
     */
    public List<EventCompletionDetails> getEventCompletionDetails()
    {
        List<ChildAssociationRef> assocs = this.services.getNodeService().getChildAssocs(
                                                        this.dispositionNodeRef, 
                                                        ASSOC_EVENT_EXECUTIONS, 
                                                        RegexQNamePattern.MATCH_ALL);
        List<EventCompletionDetails> result = new ArrayList<EventCompletionDetails>(assocs.size());
        for (ChildAssociationRef assoc : assocs)
        {
            Map<QName, Serializable> props = this.services.getNodeService().getProperties(assoc.getChildRef()); 
            String eventName = (String)props.get(PROP_EVENT_EXECUTION_NAME); 
            EventCompletionDetails ecd = new EventCompletionDetails(
                    assoc.getChildRef(), eventName, 
                    this.services.getRecordsManagementEventService().getEvent(eventName).getDisplayLabel(),
                    getBooleanValue(props.get(PROP_EVENT_EXECUTION_AUTOMATIC), false),
                    getBooleanValue(props.get(PROP_EVENT_EXECUTION_COMPLETE), false),
                    (Date)props.get(PROP_EVENT_EXECUTION_COMPLETED_AT),
                    (String)props.get(PROP_EVENT_EXECUTION_COMPLETED_BY));
            result.add(ecd);
        }
        
        return result;
    }
    
    /**
     * Helper method to deal with boolean values
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    private boolean getBooleanValue(Object value, boolean defaultValue)
    {
        boolean result = defaultValue;
        if (value != null && value instanceof Boolean)
        {
            result = ((Boolean)value).booleanValue();
        }
        return result;
    }

}
