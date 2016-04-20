package org.alfresco.service.cmr.action;

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * The various states an Action can be in.
 * 
 * @author Nick Burch
 */
@AlfrescoPublicApi
public enum ActionStatus
{
   /**
    * A new Action, which has never been run 
    */
   New,
   /**
    * An Action which has been scheduled for
    *  Asynchronous execution, but not yet run.
    */
   Pending, 
   /**
    * Indicates that the Action is currently being
    *  executed by the {@link ActionService}
    */
   Running, 
   /**
    * The Action was run without error
    */
   Completed, 
   /**
    * The Action, which must implement 
    *  {@link CancellableAction}, detected that a
    *  cancellation was requested and cancelled itself.
    */
   Cancelled, 
   /**
    * The Action failed to run to completion. Call 
    *  {@link Action#getExecutionFailureMessage()} to find
    *  out why.
    */
   Failed,
   /**
    * The Action failed with a transient exception. Call 
    *  {@link Action#getExecutionFailureMessage()} to find
    *  out why.
    *  @since 4.0.1
    */
   Declined 
   ;
   
   public static ActionStatus valueOf(Serializable s)
   {
      if(s == null) return New;
      return valueOf((String)s);
   }
}
