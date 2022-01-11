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

package org.alfresco.module.org_alfresco_module_rm.disposition;

import java.util.Date;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Disposition action interface.
 * 
 * @author Roy Wetherall
 * @since 1.0
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
    
    /**
     * Get the event completion details for a named event.
     * 
     * @param eventName event name
     * @return {@link EventCompletionDetails}   event completion details
     * @since 2.2
     */
    EventCompletionDetails getEventCompletionDetails(String eventName);
    
    /**
     * Add new completion details to the disposition action based on the provided 
     * event.
     * 
     * @param event records management event
     * @since 2.2
     */
    void addEventCompletionDetails(RecordsManagementEvent event);
    
    /**
     * Complete an event.
     * <p>
     * If null is provided, the complete at date will be take as 'now' and the completed by user
     * as the fully authenticated user.
     * 
     * @param eventName     event name
     * @param completedAt   completed at 'date', now if null
     * @param completedBy   completed by user, authenticated user if null
     * @since 2.2
     */
    void completeEvent(String eventName, Date completedAt, String completedBy);
    
    /**
     * Undo the completion of an event.
     * 
     * @param eventName event name
     * @since 2.2
     */
    void undoEvent(String eventName);
    
    /**
     * Refresh events against current disposition action definition.
     * <p>
     * Called when disposition action definition has changed.
     * 
     * @since 2.2
     */
    void refreshEvents();
}
