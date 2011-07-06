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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class represents an event in a calendar. 
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class CalendarEntryDTO implements CalendarEntry, Serializable {
   private NodeRef nodeRef;
   private String systemName;
   
   private String title;
   private String description;
   private String location;
   private Date start;
   private Date end;
   private List<String> tags;
   
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
    * @return the Tags associated with the event 
    */
   public List<String> getTags()
   {
      // TODO Immutable?
      return tags;
   }
   
   // TODO All Day events
   
   // TODO Doc folder
   
   // TODO Recurrence
   
   // TODO Is Outlook
}
