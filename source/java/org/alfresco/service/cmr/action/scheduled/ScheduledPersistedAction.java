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
