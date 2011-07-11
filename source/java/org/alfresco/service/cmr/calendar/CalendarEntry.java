/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.service.cmr.calendar;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class represents an event in a calendar. 
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface CalendarEntry extends Serializable, PermissionCheckValue {
   /**
    * @return the NodeRef of the underlying calendar entry
    */
   NodeRef getNodeRef();
   
   /**
    * @return the System generated name for the event
    */
   String getSystemName();
   
   /**
    * @return the Title ("what") of the event
    */
   String getTitle();
   
   /**
    * Sets the Title ("what") of the event
    */
   void setTitle(String title);
   
   /**
    * @return the Description of the event
    */
   String getDescription();
   
   /**
    * Sets the Description of the event
    */
   void setDescription(String description);
   
   /**
    * @return the Location of the event
    */
   String getLocation();

   /**
    * Sets the Location of the event
    */
   void setLocation(String location);
   
   /**
    * @return the Start date and time
    */
   Date getStart();
   
   /**
    * Sets the event start date and time
    */
   void setStart(Date start);
   
   /**
    * @return the End date and time
    */
   Date getEnd();

   /**
    * Sets the event end date and time
    */
   void setEnd(Date end);
   
   /**
    * Gets the event recurrence rule.
    * This needs to be in the SharePoint/Outlook format of
    *  FREQ=...;INTERVAL=...;BY...=...;COUNT=...
    */
   String getRecurrenceRule();
   
   /**
    * Sets the event recurrence rule, in SharePoint/Outlook format
    */
   void setRecurrenceRule(String recurrenceRule);
   
   /**
    * Gets the date of the last instance of this recurring event,
    * i.e. the last date up until which recurrances will occur.
    */
   Date getLastRecurrence();
   
   /**
    * Sets the date of the last instance of this recurring event
    */
   void setLastRecurrence(Date lastRecurrence);
   
   /**
    * Is this an outlook based event?
    */
   boolean isOutlook();
   
   /**
    * Sets if this is an outlook based event or not
    */
   void setOutlook(boolean outlook);
   
   /**
    * Gets the UID used by Outlook for this event.
    * See {@link CalendarEntry#isOutlook()}
    */
   String getOutlookUID();
   
   /**
    * Sets the UID used by Outlook for this event.
    * When a UID is set, normally the isOutlook flag is set too.
    */
   void setOutlookUID(String outlookUID);
   
   /**
    * Gets the SharePoint "Doc Folder" for the event. 
    * Only used for SharePoint based events
    */
   String getSharePointDocFolder();
   
   /**
    * Sets the SharePoint "Doc Folder" for the event. 
    * Only used for SharePoint based events
    */
   void setSharePointDocFolder(String docFolder);
   
   /**
    * @return the Tags associated with the event 
    */
   List<String> getTags();
}
