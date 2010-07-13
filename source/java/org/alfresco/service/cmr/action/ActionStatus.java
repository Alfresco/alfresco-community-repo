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
package org.alfresco.service.cmr.action;

import java.io.Serializable;



/**
 * The various states an Action can be in.
 * 
 * @author Nick Burch
 */
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
    *  {@link Action#getExecutionFailureCause()} to find
    *  out why.
    */
   Failed 
   ;
   
   public static ActionStatus valueOf(Serializable s)
   {
      if(s == null) return New;
      return valueOf((String)s);
   }
}
