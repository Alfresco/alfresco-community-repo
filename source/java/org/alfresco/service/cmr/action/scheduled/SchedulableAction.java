/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.action.scheduled;

import java.util.Date;

/**
 * The scheduling details for an action, normally used
 *  via {@link ScheduledPersistedAction}
 *   
 * @author Nick Burch
 * @since 3.4
 */
public interface SchedulableAction 
{
   /** 
    * Get the first date that the action should be run
    *  on or after, or null if it should start shortly
    *  after each startup.  
    */
   public Date getScheduleStart();
   
   /**
    * Sets the first date that the action should be
    *  run on or after. Set to null if the action
    *  should be run shortly after each startup.
    */
   public void setScheduleStart(Date startDate);

   
   /**
    * How many {@link #getScheduleIntervalPeriod()} periods
    *  should we wait between executions?
    * Will be null if the action isn't scheduled to
    *  be repeated.
    */
   public Integer getScheduleIntervalCount();
   
   /**
    * Sets how many periods should be waited between
    *  each execution, or null if it shouldn't be
    *  repeated. 
    */
   public void setScheduleIntervalCount(Integer count);

   
   /**
    * How long are {@link #getScheduleIntervalCount()} counts
    *  measured in?
    */
   public IntervalPeriod getScheduleIntervalPeriod();
   
   /**
    * Sets the interval period
    */
   public void setScheduleIntervalPeriod(IntervalPeriod period);
   
   
   public static enum IntervalPeriod {
      Month, 
      Week, 
      Day, 
      Hour, 
      Minute,
      Second;
   }
}
