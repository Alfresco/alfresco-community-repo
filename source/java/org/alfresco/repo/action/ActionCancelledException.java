package org.alfresco.repo.action;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.action.CancellableAction;

/**
 * The Exception thrown when a {@link CancellableAction} detects
 *  that a cancel was requested, and needs the transaction it
 *  is in to be wound back as part of the cancellation.
 */
public class ActionCancelledException extends AlfrescoRuntimeException 
{
   public ActionCancelledException(CancellableAction action)
   {
      super(action.toString());
   }
}
