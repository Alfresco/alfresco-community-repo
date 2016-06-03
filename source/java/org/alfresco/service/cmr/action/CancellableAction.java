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
