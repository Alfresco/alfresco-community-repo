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
    * Creates a new {@link CalendarEntry} in the repository for the
    *  specified site.
    *  
    * @return The System Assigned Name for the new entry 
    */
   @NotAuditable
   String createCalendarEntry(SiteInfo site, CalendarEntry entry);
   
   /**
    * Updates an existing {@link CalendarEntry} in the repository
    */
   @NotAuditable
   void updateCalendarEntry(SiteInfo site, CalendarEntry entry);
   
   /**
    * Deletes an existing {@link CalendarEntry} from the repository
    */
   @NotAuditable
   void deleteCalendarEntry(SiteInfo site, CalendarEntry entry);
   
   /**
    * Retrieves an existing {@link CalendarEntry} from the repository
    */
   @NotAuditable
   CalendarEntry getCalendarEntry(SiteInfo site, String name);
}
