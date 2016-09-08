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

import java.util.Date;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Disposition action interface.
 * 
 * @author Roy Wetherall
 */
public interface DispositionAction
{    
    /**
     * @return  the node reference
     */
    NodeRef getNodeRef();
    
    /**
     * @return  the disposition action definition
     */
    DispositionActionDefinition getDispositionActionDefinition();
    
    /**
     * @return the id of the action
     */
    String getId();
    
    /**
     * @return the name of the action
     */
    String getName();
    
    /**
     * @return the display label for the action 
     */
    String getLabel();
    
    /**
     * @return  the dispostion action as of eligibility date
     */
    Date getAsOfDate();
    
    /**
     * @return  true if the events are complete (ie: enough events have been completed to make the disposition
     *          action 
     */
    boolean isEventsEligible();
    
    /**
     * @return the user that started the action
     */
    String getStartedBy();
    
    /**
     * @return when the action was started
     */
    Date getStartedAt();
    
    /**
     * @return the user that completed the action
     */
    String getCompletedBy();
    
    /**
     * @return when the action was completed
     */
    Date getCompletedAt();
    
    /**
     * @return List of events that need to be completed for the action
     */
    List<EventCompletionDetails> getEventCompletionDetails();
}
