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

import org.alfresco.repo.action.ActionCancelledException;
import org.alfresco.repo.action.executer.ActionExecuter;

/**
 * A marker interface that forms part of the Cancel Action contract.
 * An action that implements this interface commits to periodically 
 *  asking the {@link ActionTrackingService} if a cancel of it has
 *  been requested, and orderly terminating itself if so.
 * 
 * Actions implementing this should, via their
 *  {@link ActionExecuter}, periodically call 
 *  {@link ActionTrackingService#isCancellationRequested(CancellableAction)}
 *  to check if a cancel has been requested for them. 
 * If it has, they should tidy up as much as possible, and then throw
 *  a {@link ActionCancelledException} to indicate to the 
 *  {@link ActionService} that they ceased running due to a
 *  cancel.
 * 
 * @author Nick Burch
 */
public interface CancellableAction extends Action
{
}
