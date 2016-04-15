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
