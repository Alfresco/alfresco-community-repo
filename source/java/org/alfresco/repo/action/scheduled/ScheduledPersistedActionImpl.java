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
package org.alfresco.repo.action.scheduled;

import java.util.Date;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.quartz.DateIntervalTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

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
   private NodeRef persistedAtNodeRef;
   private Action action;
   private Date lastExecutedAt;
   private Date scheduleStart;
   private Date scheduleEnd;
   private Integer intervalCount;
   private IntervalPeriod intervalPeriod;
   
   protected ScheduledPersistedActionImpl(Action action) 
   {
      this.action = action;
   }
   
   
   /**
    * Get the persisted nodeRef for this schedule
    */
   protected NodeRef getPersistedAtNodeRef()
   {
      return persistedAtNodeRef;
   }
   
   /** 
    * Record where this schedule is persisted
    */
   protected void setPersistedAtNodeRef(NodeRef nodeRef)
   {
      this.persistedAtNodeRef = nodeRef;
   }
   
   /** Get the action which the schedule applies to */
   public Action getAction() 
   {
      return action;
   }
   
   /** Get where the action lives */
   public NodeRef getActionNodeRef()
   {
      return action == null ? null : action.getNodeRef();
   }
   
   /** When was this action last run, if ever? */
   public Date getScheduleLastExecutedAt()
   {
      return lastExecutedAt;
   }
   
   /** Record when the scheduled action was last run */ 
   public void setScheduleLastExecutedAt(Date executedAt)
   {
      this.lastExecutedAt = executedAt;
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
    * Not yet publicly available - get the date after
    *  which the action should no longer be run.
    */
   protected Date getScheduleEnd()
   {
      return scheduleEnd;
   }
   
   /**
    * Not yet publicly available - set the date after
    *  which the action should no longer be run.
    */
   protected void setScheduleEnd(Date endDate)
   {
      this.scheduleEnd = endDate;
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
    * Returns the interval in a form like 1Day (1 day)
    *  or 2Hour (2 hours), or null if a period+count
    *  hasn't been set
    */
   public String getScheduleInterval() 
   {
      if(intervalCount == null || intervalPeriod == null) 
      {
         return null;
      }
      return intervalCount.toString() + intervalPeriod.name();
   }
   
   /**
    * Returns a Quartz trigger definition based on the current
    *  scheduling details.
    * May only be called once this object has been persisted
    */
   public Trigger asTrigger()
   {
      if(persistedAtNodeRef == null)
         throw new IllegalStateException("Must be persisted first");
      
      // Use our nodeRef as the unique title
      String triggerName = persistedAtNodeRef.toString();
      
      
      // If they don't want it to repeat, and didn't set any
      //  dates, then we can't schedule it!
      if(getScheduleInterval() == null && scheduleStart == null)
      {
         return null;
      }
      
      // If the end date is in the past, don't schedule
      if(scheduleEnd != null && scheduleEnd.getTime() < System.currentTimeMillis())
      {
         return null;
      }
      
      
      // Decide what to use as the start time
      // This will be based on the requested start time, when
      //  the schedule last ran (if ever), and the interval
      Date startAt = scheduleStart;
      Date endAt = scheduleEnd;
      if(startAt == null)
      {
         // No start time specified, so start immediately
         startAt = new Date();
      }
      else
      {
         // A start time was given. Is it still to come?
         if(startAt.getTime() >= System.currentTimeMillis())
         {
            // The start date hasn't happened yet
            // This means we can just use it as-is
         }
         else
         {
            // The start date has passed. Have we ever run this?
            if(lastExecutedAt == null)
            {
               // The schedule has never run
               // Tell Quartz the requested start time, so it runs
               //  immediately, and repeats are correctly calculated
               startAt = scheduleStart;
            }
            else if(lastExecutedAt.getTime() < startAt.getTime())
            {
               // The previous run time is before the scheduled
               //  start time, so has probably been edited, and
               //  both of these dates is in the past.
               // Tell Quartz the requested start time, so it runs
               //  immediately, and repeats are correctly calculated
               startAt = scheduleStart;
            }
            else
            {
               // The start date is in the past, and we have
               //  previously run the job
               
               if(getScheduleInterval() == null)
               {
                  // There is no schedule setup, so this is
                  //  probably just an old job on startup/edit
                  // So, don't run it
                  return null;
               }
               
               // Based on the start time, when would it next be
               //  due to fire?
               DateIntervalTrigger testT = buildDateIntervalTrigger("TEST", scheduleStart, null);
               Date nextFireFromNow = testT.getFireTimeAfter(new Date());
               Date nextFireFromLast = testT.getFireTimeAfter(lastExecutedAt);
               
               // If the next fire time from the last is before the
               //  next due date, then we missed one
               if(nextFireFromLast.getTime() < nextFireFromNow.getTime())
               {
                  // We missed one!
                  // Tell Quartz the requested start time, so it runs
                  //  immediately, and repeats are correctly calculated
                  startAt = scheduleStart;
               }
               else
               {
                  // The last run time was largely when due
                  // So, don't run until the next time
                  startAt = nextFireFromNow;
               }
            }
         }
      }
      
      
      // If they don't want it to repeat, just use a simple interval
      if(getScheduleInterval() == null)
      {
         SimpleTrigger trigger = new SimpleTrigger(
               triggerName, null,
               startAt
         ); 
         trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
         return trigger;
      }
      
      return buildDateIntervalTrigger(triggerName, startAt, endAt);
   }
   
   private DateIntervalTrigger buildDateIntervalTrigger(String triggerName, Date startAt, Date endAt)
   {
      // There are some repeating rules
      // Create a Date Interval trigger
      DateIntervalTrigger.IntervalUnit quartzInterval = 
         DateIntervalTrigger.IntervalUnit.valueOf(
               intervalPeriod.toString().toUpperCase()
         );
      
      // Create the interval
      DateIntervalTrigger trigger = new DateIntervalTrigger(
               triggerName, null,
               startAt, endAt,
               quartzInterval, intervalCount
      );
      trigger.setMisfireInstruction( DateIntervalTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW );
      return trigger;
   }
}
