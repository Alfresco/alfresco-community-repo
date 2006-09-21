/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.smb.server.repo.desk;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.NotifyChange;
import org.alfresco.filesys.smb.server.repo.DesktopAction;
import org.alfresco.filesys.smb.server.repo.DesktopParams;
import org.alfresco.filesys.smb.server.repo.DesktopResponse;
import org.alfresco.filesys.smb.server.repo.DesktopTarget;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

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

		// check if there are any files/folders to process
		
		if ( params.numberOfTargetNodes() == 0)
			return new DesktopResponse(StsSuccess);
		
		// Start a transaction
		
		params.getSession().beginTransaction(getTransactionService(), false);
		
		// Process the list of target nodes

		DesktopResponse response = new DesktopResponse(StsSuccess);
		
		for ( int idx = 0; idx < params.numberOfTargetNodes(); idx++)
		{
			// Get the current target node
			
			DesktopTarget target = params.getTarget(idx);
			
			// Check if the node is a working copy
			
            if ( getNodeService().hasAspect( target.getNode(), ContentModel.ASPECT_WORKING_COPY))
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
                	
                	if ( getNodeService().hasAspect( target.getNode(), ContentModel.ASPECT_LOCKABLE)) {
                	
                		// Get the lock type
                		
                		String lockTypeStr = (String) getNodeService().getProperty( target.getNode(), ContentModel.PROP_LOCK_TYPE);
                		if ( lockTypeStr != null) {
                			response.setStatus(StsError, "Checkout failed, file is locked");
                			return response;
                		}
                	}
                	
                    // Check out the file
                    
                    NodeRef workingCopyNode = getCheckInOutService().checkout( target.getNode());

                    // Get the working copy file name
                    
                    String workingCopyName = (String) getNodeService().getProperty( workingCopyNode, ContentModel.PROP_NAME);
                    
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
}
