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
public interface ScheduledPersistedAction extends SchedulableAction
{
   /** Get the action which the schedule applies to */
   public Action getAction();
   
   /** Get the persisted {@link NodeRef} of the action this applies to */
   public NodeRef getActionNodeRef();
   

   /** When was this action last run, if ever? */
   public Date getScheduleLastExecutedAt();
   
   
   /**
    * Returns the interval in a form like 1Day (1 day)
    *  or 2Hour (2 hours)
    */
   public String getScheduleInterval();
}
