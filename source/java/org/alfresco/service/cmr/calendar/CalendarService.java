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

import java.util.Date;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.site.SiteInfo;

/**
 * The Calendar service.
 * 
 * TODO Lucene free querying
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface CalendarService {
   /**
    * Creates a new {@link CalendarEntry} for the given site, but 
    *  doesn't save it to the repository.
    *  
    * @return The newly cre 
    */
   @NotAuditable
   CalendarEntry createCalendarEntry(String siteShortName, String eventTitle,
         String eventDescription, Date from, Date to);
   
   /**
    * Saves a {@link CalendarEntry} in the repository.
    */
   @NotAuditable
   void saveCalendarEntry(CalendarEntry entry);
   
   /**
    * Deletes an existing {@link CalendarEntry} from the repository
    */
   @NotAuditable
   void deleteCalendarEntry(CalendarEntry entry);
   
   /**
    * Retrieves an existing {@link CalendarEntry} from the repository
    */
   @NotAuditable
   CalendarEntry getCalendarEntry(String siteShortName, String entryName);
}
