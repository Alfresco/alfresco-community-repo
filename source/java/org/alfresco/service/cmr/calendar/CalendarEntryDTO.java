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
package org.alfresco.service.cmr.calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class represents an event in a calendar. 
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarEntryDTO implements CalendarEntry, Serializable 
{
   private static final long serialVersionUID = -7997650453677545845L;
   private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
   
   private NodeRef nodeRef;
   private NodeRef containerNodeRef;
   private String systemName;
   
   private String title;
   private String description;
   private String location;
   private Date start;
   private Date end;
   private String recurrenceRule;
   private Date lastRecurrence;
   private String sharePointDocFolder;
   private boolean isOutlook = false;
   private String outlookUID;
   private Date createdAt;
   private Date modifiedAt;
   private List<String> tags = new ArrayList<String>();
   
   /**
    * Creates an empty {@link CalendarEntry}, which can be populated
    *  with set calls.
    */
   public CalendarEntryDTO()
   {}
   
   /**
    * Creates a {@link CalendarEntry} with common properties.
    */
   public CalendarEntryDTO(String title, String description, 
         String location, Date start, Date end)
   {
      this.title = title;
      this.description = description;
      this.location = location;
      this.start = start;
      this.end = end;
   }
   
   /**
    * @return the NodeRef of the underlying calendar entry
    */
   public NodeRef getNodeRef() 
   {
      return nodeRef;
   }
   
   /**
    * @return the NodeRef of the calendar's container in the site
    */
   public NodeRef getContainerNodeRef() 
   {
      return containerNodeRef;
   }
   
   /**
    * @return the System generated name for the event
    */
   public String getSystemName() 
   {
      return systemName;
   }
   
   /**
    * @return the Title ("what") of the event
    */
   public String getTitle() 
   {
      return title;
   }
   
   /**
    * Sets the Title ("what") of the event
    */
   public void setTitle(String title)
   {
      this.title = title;
   }
   
   /**
    * @return the Description of the event
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * Sets the Description of the event
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * @return the Location of the event
    */
   public String getLocation()
   {
      return location;
   }

   /**
    * Sets the Location of the event
    */
   public void setLocation(String location)
   {
      this.location = location;
   }
   
   /**
    * @return the Start date and time
    */
   public Date getStart()
   {
      return start;
   }
   
   /**
    * Sets the event start date and time
    */
   public void setStart(Date start)
   {
      this.start = start;
   }
   
   /**
    * @return the End date and time
    */
   public Date getEnd()
   {
      return end;
   }

   /**
    * Sets the event end date and time
    */
   public void setEnd(Date end)
   {
      this.end = end;
   }
   
   /**
    * Gets the event recurrence rule.
    */
   public String getRecurrenceRule()
   {
      return recurrenceRule;
   }
   
   /**
    * Sets the event recurrence rule
    */
   public void setRecurrenceRule(String recurrenceRule)
   {
      this.recurrenceRule = recurrenceRule;
   }
   
   /**
    * Gets the date of the last instance of this recurring event
    */
   public Date getLastRecurrence()
   {
      return lastRecurrence;
   }
   
   /**
    * Sets the date of the last instance of this recurring event
    */
   public void setLastRecurrence(Date lastRecurrence)
   {
      this.lastRecurrence = lastRecurrence;
   }
   
   /**
    * Is this an outlook based event?
    */
   public boolean isOutlook()
   {
      return isOutlook;
   }
   
   /**
    * Sets if this is an outlook based event or not
    */
   public void setOutlook(boolean outlook)
   {
      this.isOutlook = outlook;
   }
   
   /**
    * Gets the UID used by Outlook for this event.
    * See {@link CalendarEntry#isOutlook()}
    */
   public String getOutlookUID()
   {
      return outlookUID;
   }
   
   /**
    * Sets the UID used by Outlook for this event.
    * When a UID is set, normally the isOutlook flag is set too.
    */
   public void setOutlookUID(String outlookUID)
   {
      this.outlookUID = outlookUID;
   }

   /**
    * Gets the SharePoint "Doc Folder" for the event. 
    * Only used for SharePoint based events
    */
   public String getSharePointDocFolder() 
   {
      return sharePointDocFolder;
   }

   /**
    * Sets the SharePoint "Doc Folder" for the event. 
    * Only used for SharePoint based events
    */
   public void setSharePointDocFolder(String docFolder) 
   {
      this.sharePointDocFolder = docFolder;
   }

   /**
    * @return the Tags associated with the event 
    */
   public List<String> getTags()
   {
      return tags;
   }

   /**
    * Gets when this entry was created
    */
   public Date getCreatedAt() 
   {
      return createdAt;
   }
   public void setCreatedAt(Date createdAt)
   {
      this.createdAt = createdAt;
   }

   /**
    * Gets when this entry was modified
    */
   public Date getModifiedAt() 
   {
      return modifiedAt;
   }
   public void setModifiedAt(Date modifiedAt)
   {
      this.modifiedAt = modifiedAt;
   }

   /**
    * Does the given {@link CalendarEntry} define an all-day
    *  event?
    * An All Day Event is defined as one starting at midnight
    *  on a day, and ending at midnight.
    *  
    * For a single day event, the start and end dates should be
    *  the same, and the times for both are UTC midnight.
    * For a multi day event, the start and end times are UTC midnight,
    *  for the first and last days respectively.
    */
   public static boolean isAllDay(CalendarEntry entry)
   {
      if (entry.getStart() == null || entry.getEnd() == null)
      {
         // One or both dates is missing
         return false;
      }
      
      // As of 4.0, all-day events use UTC midnight for consistency
      Calendar startUTC = Calendar.getInstance();
      Calendar endUTC = Calendar.getInstance();
      startUTC.setTime(entry.getStart());
      endUTC.setTime(entry.getEnd());
      startUTC.setTimeZone(UTC);
      endUTC.setTimeZone(UTC);
      
      // Pre-4.0, the midnights were local time...
      Calendar startLocal = Calendar.getInstance();
      Calendar endLocal = Calendar.getInstance();
      startLocal.setTime(entry.getStart());
      endLocal.setTime(entry.getEnd());
      
      // Check for midnight, first in UTC then again in Server Local Time
      Calendar[] starts = new Calendar[] { startUTC, startLocal };
      Calendar[] ends = new Calendar[] { endUTC, endLocal };
      for(int i=0; i<starts.length; i++)
      {
         Calendar start = starts[i];
         Calendar end = ends[i];
         if (start.get(Calendar.HOUR_OF_DAY) == 0 &&
               start.get(Calendar.MINUTE) == 0 &&
               start.get(Calendar.SECOND) == 0 &&
               end.get(Calendar.HOUR_OF_DAY) == 0 &&
               end.get(Calendar.MINUTE) == 0 &&
               end.get(Calendar.SECOND) == 0)
         {
            // Both at midnight, counts as all day
            return true;
         }
      }
      
      // In any other case, it isn't an all-day
      return false;
   }
}
