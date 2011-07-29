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
package org.alfresco.filesys.repo.desk;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.DesktopAction;
import org.alfresco.filesys.alfresco.DesktopParams;
import org.alfresco.filesys.alfresco.DesktopResponse;
import org.alfresco.filesys.alfresco.DesktopTarget;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.NotifyChange;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.FileStateCache;
import org.alfresco.jlan.smb.server.notify.NotifyChangeHandler;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;

/**
 * Check In/Out Desktop Action Class
 * 
 * <p>Provides check in/out functionality via CIFS using the desktop action interface.
 * 
 * @author gkspencer
 */
public class CheckInOutDesktopAction extends DesktopAction {

	/**
	 * Class constructor
	 */
	public CheckInOutDesktopAction()
	{
		super( DesktopAction.AttrAnyFiles, DesktopAction.PreConfirmAction + DesktopAction.PreCopyToTarget + DesktopAction.PreLocalToWorkingCopy);
	}
	
	/**
	 * Return the confirmation string to be displayed by the client
	 * 
	 * @return String
	 */
	@Override
	public String getConfirmationString() {
		return "Run check in/out action";
	}

	/**
	 * Run the desktop action
	 * 
	 * @param params DesktopParams
	 * @return DesktopResponse 
	 */
	@Override
	public DesktopResponse runAction(final DesktopParams params) {

		// Check if there are any files/folders to process
		
		if ( params.numberOfTargetNodes() == 0)
		{
			return new DesktopResponse(StsSuccess);
		}
		
		TransactionService transactionService = this.getServiceRegistry().getTransactionService();

		RetryingTransactionHelper tx = transactionService.getRetryingTransactionHelper();
		
        final List<Pair<Integer, String>> fileChanges = new LinkedList<Pair<Integer,String>>();

		RetryingTransactionCallback<DesktopResponse> runCOCICB = new RetryingTransactionCallback<DesktopResponse>() {

		    @Override
		    public DesktopResponse execute() throws Throwable
		    {


		        // Get required services

		        ServiceRegistry serviceRegistry = getServiceRegistry();
		        NodeService nodeService = serviceRegistry.getNodeService();
		        CheckOutCheckInService checkOutCheckInService = serviceRegistry.getCheckOutCheckInService(); 

		        // Process the list of target nodes

		        DesktopResponse response = new DesktopResponse(StsSuccess);

		        for ( int idx = 0; idx < params.numberOfTargetNodes(); idx++)
		        {
		            // Get the current target node

		            DesktopTarget target = params.getTarget(idx);

		            // Check if the node is a working copy

		            if ( nodeService.hasAspect( target.getNode(), ContentModel.ASPECT_WORKING_COPY))
		            {
		                try
		                {
		                    // Check in the file, pass an empty version properties so that veriosnable nodes create a new version

		                    Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
		                    checkOutCheckInService.checkin( target.getNode(), versionProperties, null, false);

		                    // Check if there are any file/directory change notify requests active

		                    if ( getContext().hasFileServerNotifications()) {

		                        // Build the relative path to the checked in file

		                        String fileName = null;

		                        if ( target.getTarget().startsWith(FileName.DOS_SEPERATOR_STR))
		                        {
		                            // Path is already relative to filesystem root

		                            fileName = target.getTarget();
		                        }
		                        else
		                        {
		                            // Build a root relative path for the file

		                            fileName = FileName.buildPath( params.getFolder().getFullName(), null, target.getTarget(), FileName.DOS_SEPERATOR);
		                        }

		                        // Queue a file deleted change notification
		                        fileChanges.add(new Pair<Integer, String>(NotifyChange.ActionRemoved, fileName));
		                    }
		                }
		                catch (Exception ex)
		                {
		                    // Propagate retryable errors. Log the rest.
		                    if (RetryingTransactionHelper.extractRetryCause(ex) != null)
		                    {
		                        if (ex instanceof RuntimeException)
		                        {
		                            throw (RuntimeException)ex;
		                        }
		                        else
		                        {
		                            throw new AlfrescoRuntimeException("Desktop action error", ex);
		                        }
		                    }

		                    // Dump the error

		                    if ( logger.isErrorEnabled())
		                        logger.error("Desktop action error", ex);

		                    // Return an error status and message

		                    response.setStatus(StsError, "Checkin failed for " + target.getTarget() + ", " + ex.getMessage());
		                }
		            }
		            else
		            {
		                try
		                {
		                    // Check if the file is locked

		                    if ( nodeService.hasAspect( target.getNode(), ContentModel.ASPECT_LOCKABLE)) {

		                        // Get the lock type

		                        String lockTypeStr = (String) nodeService.getProperty( target.getNode(), ContentModel.PROP_LOCK_TYPE);
		                        if ( lockTypeStr != null) {
		                            response.setStatus(StsError, "Checkout failed, file is locked");
		                            return response;
		                        }
		                    }

		                    // Check out the file

		                    NodeRef workingCopyNode = checkOutCheckInService.checkout( target.getNode());

		                    // Get the working copy file name

		                    String workingCopyName = (String) nodeService.getProperty( workingCopyNode, ContentModel.PROP_NAME);

		                    // Check out was successful, pack the working copy name

		                    response.setStatus(StsSuccess, "Checked out working copy " + workingCopyName);

		                    // Build the relative path to the checked out file

		                    String fileName = FileName.buildPath( params.getFolder().getFullName(), null, workingCopyName, FileName.DOS_SEPERATOR);

		                    // Update cached state for the working copy to indicate the file exists

		                    FileStateCache stateCache = getContext().getStateCache();
		                    if ( stateCache != null) {

		                        // Update any cached state for the working copy file

		                        FileState fstate = stateCache.findFileState( fileName);
		                        if ( fstate != null)
		                            fstate.setFileStatus( FileStatus.FileExists);
		                    }

		                    // Check if there are any file/directory change notify requests active

		                    if ( getContext().hasChangeHandler()) {

		                        // Build the relative path to the checked in file

		                        // Queue a file added change notification
		                        fileChanges.add(new Pair<Integer, String>(NotifyChange.ActionAdded, fileName));
		                    }
		                }
		                catch (Exception ex)
		                {
		                    // Propagate retryable errors. Log the rest.
		                    if (RetryingTransactionHelper.extractRetryCause(ex) != null)
		                    {
		                        if (ex instanceof RuntimeException)
		                        {
		                            throw (RuntimeException)ex;
		                        }
		                        else
		                        {
		                            throw new AlfrescoRuntimeException("Desktop action error", ex);
		                        }
		                    }

		                    // Dump the error

		                    if ( logger.isErrorEnabled())
		                    {
		                        logger.error("Desktop action error", ex);
		                    }
		                        
		                    // Return an error status and message

		                    response.setStatus(StsError, "Failed to checkout " + target.getTarget() + ", " + ex.getMessage());
		                }
		            }
		        } // end of for loop
		        
		        return response;
		    }
		};
		
	    RetryingTransactionCallback<Void> notifyCB = new RetryingTransactionCallback<Void>() {

	        @Override
	        public Void execute() throws Throwable
	        {
	            NotifyChangeHandler notifyChangeHandler = getContext().getChangeHandler();
	            for (Pair<Integer, String> fileChange : fileChanges)
	            {
	                notifyChangeHandler.notifyFileChanged(fileChange.getFirst(), fileChange.getSecond());
	            }
	            return null;
	        }
	    };
        
		DesktopResponse response = tx.doInTransaction(runCOCICB, false, false);
	
		/**
		 * Now do the notification in a separate transaction
		 */
		if(fileChanges.size() > 0)
		{
		    tx.doInTransaction(notifyCB, false, false);
		}
  		
        return response;		
	}	
	
	
}
