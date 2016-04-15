package org.alfresco.service.cmr.action.scheduled;

import java.util.List;

import org.alfresco.service.cmr.action.Action;

/**
 * A service which handles the scheduling of the
 *  execution of persisted actions.
 * It handles registering them with the Quartz
 *  scheduler on repository start, and handles
 *  the edit, creation and deletion of them.
 * 
 * @author Nick Burch
 * @since 3.4
 */
public interface ScheduledPersistedActionService 
{
   /**
    * Creates a new schedule, for the specified Action.
    */
   public ScheduledPersistedAction createSchedule(Action persistedAction);
   
   /**
    * Saves the changes to the schedule to the repository,
    *  and updates the Scheduler with any changed details.
    */
   public void saveSchedule(ScheduledPersistedAction schedule);
   
   /**
    * Removes the schedule for the action, and cancels future
    *  executions of it.
    * The persisted action is unchanged.
    */
   public void deleteSchedule(ScheduledPersistedAction schedule);
   
   /**
    * Returns the schedule for the specified action, or
    *  null if it isn't currently scheduled. 
    */
   public ScheduledPersistedAction getSchedule(Action persistedAction);
   
   /**
    * Returns all currently scheduled actions.
    */
   public List<ScheduledPersistedAction> listSchedules();
}
