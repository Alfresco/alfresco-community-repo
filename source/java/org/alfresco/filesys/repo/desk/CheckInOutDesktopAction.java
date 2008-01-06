/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.repo.desk;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.filesys.alfresco.DesktopAction;
import org.alfresco.filesys.alfresco.DesktopParams;
import org.alfresco.filesys.alfresco.DesktopResponse;
import org.alfresco.filesys.alfresco.DesktopTarget;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.NotifyChange;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;

/**
 * Check In/Out Desktop Action Class
 * 
 * <p>Provides check in/out functionality via CIFS using the desktop action interface.
 * 
 * @author gkspencer
 */
public class CheckInOutDesktopAction extends DesktopAction {

	// Check in/out service
	
	private CheckOutCheckInService m_checkInOutService;
	
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
	public DesktopResponse runAction(DesktopParams params) {

		// Check if there are any files/folders to process
		
		if ( params.numberOfTargetNodes() == 0)
			return new DesktopResponse(StsSuccess);
		
		// Get required services
		
		NodeService nodeService = getServiceRegistry().getNodeService();
		
		// Start a transaction
		
		params.getDriver().beginWriteTransaction( params.getSession());
		
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
                    getCheckInOutService().checkin( target.getNode(), versionProperties, null, false);

                    // Check if there are any file/directory change notify requests active

                    if ( getContext().hasChangeHandler()) {
                        
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
                        
                        getContext().getChangeHandler().notifyFileChanged(NotifyChange.ActionRemoved, fileName);
                    }
                }
                catch (Exception ex)
                {
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
                    
                    NodeRef workingCopyNode = getCheckInOutService().checkout( target.getNode());

                    // Get the working copy file name
                    
                    String workingCopyName = (String) nodeService.getProperty( workingCopyNode, ContentModel.PROP_NAME);
                    
                    // Check out was successful, pack the working copy name

                    response.setStatus(StsSuccess, "Checked out working copy " + workingCopyName);
                    
                    // Check if there are any file/directory change notify requests active

                    if ( getContext().hasChangeHandler()) {
                        
                        // Build the relative path to the checked in file

                        String fileName = FileName.buildPath( params.getFolder().getFullName(), null, workingCopyName, FileName.DOS_SEPERATOR);
                        
                        // Queue a file added change notification
                        
                        getContext().getChangeHandler().notifyFileChanged(NotifyChange.ActionAdded, fileName);
                    }
                }
                catch (Exception ex)
                {
                	// Dump the error
                	
                	if ( logger.isErrorEnabled())
                		logger.error("Desktop action error", ex);
                	
                    // Return an error status and message

                	response.setStatus(StsError, "Failed to checkout " + target.getTarget() + ", " + ex.getMessage());
                }
            }
		}
		
		// Return a success status for now
		
		return response; 
	}
	
	/**
	 * Get the check in/out service
	 * 
	 * @return CheckOutCheckInService
	 */
	protected final CheckOutCheckInService getCheckInOutService()
	{
		// Check if the service has been cached
		
		if ( m_checkInOutService == null)
		{
			m_checkInOutService = getServiceRegistry().getCheckOutCheckInService();
		}
		
		// Return the check in/out service
		
		return m_checkInOutService;
	}
}
