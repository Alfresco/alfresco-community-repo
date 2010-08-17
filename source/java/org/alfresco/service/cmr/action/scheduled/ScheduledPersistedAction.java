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
package org.alfresco.service.cmr.action.scheduled;

import java.util.Date;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The scheduling wrapper around a persisted
 *  action, which is to be executed on a 
 *  scheduled basis.
 *   
 * @author Nick Burch
 * @since 3.4
 */
public interface ScheduledPersistedAction 
{
   /** Get the action which the schedule applies to */
   public Action getAction();
   
   /** Get the persisted {@link NodeRef} of the action this applies to */
   public NodeRef getActionNodeRef();
   
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
   
   
   /**
    * Returns the interval in a form like 1D (1 day)
    *  or 2h (2 hours)
    */
   public String getScheduleInterval();
   
   
   public static enum IntervalPeriod {
      Month ('M'), 
      Week ('W'), 
      Day ('D'), 
      Hour ('h'), 
      Minute ('m'),
      Second ('s');
      
      private final char letter;
      IntervalPeriod(char letter) {
         this.letter = letter;
      }
      public char getLetter() {
         return letter;
      }
   }
}
