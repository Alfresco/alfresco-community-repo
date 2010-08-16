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
package org.alfresco.repo.action.scheduled;

import java.util.Date;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;

/**
 * The scheduling wrapper around a persisted
 *  action, which is to be executed on a 
 *  scheduled basis.
 *   
 * @author Nick Burch
 * @since 3.4
 */
public class ScheduledPersistedActionImpl implements ScheduledPersistedAction 
{
   private Action action;
   private Date scheduleStart;
   private Integer intervalCount;
   private IntervalPeriod intervalPeriod;
   
   public ScheduledPersistedActionImpl(Action action) 
   {
      this.action = action;
   }
   
   
   /** Get the action which the schedule applies to */
   public Action getAction() 
   {
      return action;
   }
   
   /** 
    * Get the first date that the action should be run
    *  on or after, or null if it should start shortly
    *  after each startup.  
    */
   public Date getScheduleStart() 
   {
      return scheduleStart;
   }
   
   /**
    * Sets the first date that the action should be
    *  run on or after. Set to null if the action
    *  should be run shortly after each startup.
    */
   public void setScheduleStart(Date startDate) 
   {
      this.scheduleStart = startDate;
   }

   
   /**
    * How many {@link #getScheduleIntervalPeriod()} periods
    *  should we wait between executions?
    * Will be null if the action isn't scheduled to
    *  be repeated.
    */
   public Integer getScheduleIntervalCount() 
   {
      return intervalCount;
   }
   
   /**
    * Sets how many periods should be waited between
    *  each execution, or null if it shouldn't be
    *  repeated. 
    */
   public void setScheduleIntervalCount(Integer count) 
   {
      this.intervalCount = count;
   }

   
   /**
    * How long are {@link #getScheduleIntervalCount()} counts
    *  measured in?
    */
   public IntervalPeriod getScheduleIntervalPeriod() 
   {
      return intervalPeriod;
   }
   
   /**
    * Sets the interval period
    */
   public void setScheduleIntervalPeriod(IntervalPeriod period) {
      this.intervalPeriod = period;
   }
   
   
   /**
    * Returns the interval in a form like 1D (1 day)
    *  or 2h (2 hours), or null if a period+count
    *  hasn't been set
    */
   public String getScheduleInterval() 
   {
      if(intervalCount == null || intervalPeriod == null) 
      {
         return null;
      }
      return intervalCount.toString() + intervalPeriod.getLetter();
   }
}
