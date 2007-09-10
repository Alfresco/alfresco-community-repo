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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.coci;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionableAspect;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;

/**
 * Version opertaions service implementation
 * 
 * @author Roy Wetherall
 */
public class CheckOutCheckInServiceImpl implements CheckOutCheckInService
{
    /**
     * I18N labels
     */
    private static final String MSG_ERR_BAD_COPY = "coci_service.err_bad_copy";
    private static final String MSG_WORKING_COPY_LABEL = "coci_service.working_copy_label";
    private static final String MSG_ERR_NOT_OWNER = "coci_service.err_not_owner"; 
    private static final String MSG_ERR_ALREADY_WORKING_COPY = "coci_service.err_workingcopy_checkout";
    private static final String MSG_ERR_NOT_AUTHENTICATED = "coci_service.err_not_authenticated";
    private static final String MSG_ERR_WORKINGCOPY_HAS_NO_MIMETYPE = "coci_service.err_workingcopy_has_no_mimetype"; 
    private static final String MSG_ALREADY_CHECKEDOUT = "coci_service.err_already_checkedout";

    /**
     * Extension character, used to recalculate the working copy names
     */
    private static final String EXTENSION_CHARACTER = ".";
    
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * The version service
     */
    private VersionService versionService;
    
    /**
     * The lock service
     */
    private LockService lockService;
    
    /**
     * The copy service
     */
    private CopyService copyService;
    
    /**
     * The search service
     */
    private SearchService searchService;
    
    /**
     * The authentication service
     */
    private AuthenticationService authenticationService;
    
    /**
     * The versionable aspect behaviour implementation
     */
    private VersionableAspect versionableAspect;
    
    /**
     * Set the node service
     * 
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the version service
     * 
     * @param versionService  the version service
     */
    public void setVersionService(VersionService versionService) 
    {
        this.versionService = versionService;
    }
    
    /**
     * Sets the lock service
     * 
     * @param lockService  the lock service
     */
    public void setLockService(LockService lockService) 
    {
        this.lockService = lockService;
    }
    
    /**
     * Sets the copy service
     *  
     * @param copyService  the copy service
     */
    public void setCopyService(
            CopyService copyService) 
    {
        this.copyService = copyService;
    }
    
    /**
     * Sets the authentication service
     * 
     * @param authenticationService  the authentication service
     */
    public void setAuthenticationService(
            AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    /**
     * Set the search service
     * 
     * @param searchService     the search service
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    /**
     * Sets the versionable aspect behaviour implementation
     * 
     * @param versionableAspect     the versionable aspect behaviour implementation
     */
    public void setVersionableAspect(VersionableAspect versionableAspect)
    {
        this.versionableAspect = versionableAspect;
    }
    
    /**
     * Get the working copy label.
     * 
     * @return    the working copy label
     */
    public String getWorkingCopyLabel() 
    {
        return I18NUtil.getMessage(MSG_WORKING_COPY_LABEL);
    }
    
    /**
     * @see org.alfresco.service.cmr.coci.CheckOutCheckInService#checkout(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
     */
    public NodeRef checkout(
            NodeRef nodeRef, 
            NodeRef destinationParentNodeRef,
            QName destinationAssocTypeQName, 
            QName destinationAssocQName) 
    {
    	LockType lockType = this.lockService.getLockType(nodeRef);
    	if (LockType.READ_ONLY_LOCK.equals(lockType) == true)
    	{
    		throw new CheckOutCheckInServiceException(MSG_ALREADY_CHECKEDOUT);
    	}
    	
        // Make sure we are no checking out a working copy node
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == true)
        {
            throw new CheckOutCheckInServiceException(MSG_ERR_ALREADY_WORKING_COPY);
        }
        
        // Apply the lock aspect if required
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == false)
        {
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        }
        
        // Rename the working copy
        String copyName = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        if (this.getWorkingCopyLabel() != null && this.getWorkingCopyLabel().length() != 0)
        {
            if (copyName != null && copyName.length() != 0)
            {
                int index = copyName.lastIndexOf(EXTENSION_CHARACTER);
                if (index > 0)
                {
                    // Insert the working copy label before the file extension
                    copyName = copyName.substring(0, index) + " " + getWorkingCopyLabel() + copyName.substring(index);
                }
                else
                {
                    // Simply append the working copy label onto the end of the existing name
                    copyName = copyName + " " + getWorkingCopyLabel();
                }
            }
            else
            {
                copyName = getWorkingCopyLabel();
            }
        }

        // Make the working copy
        destinationAssocQName = QName.createQName(destinationAssocQName.getNamespaceURI(), QName.createValidLocalName(copyName));
        NodeRef workingCopy = this.copyService.copy(
                nodeRef,
                destinationParentNodeRef,
                destinationAssocTypeQName,
                destinationAssocQName);
        
        // Update the working copy name        
        this.nodeService.setProperty(workingCopy, ContentModel.PROP_NAME, copyName);

        // Get the user 
        String userName = getUserName();
        
        // Apply the working copy aspect to the working copy
        Map<QName, Serializable> workingCopyProperties = new HashMap<QName, Serializable>(1);
        workingCopyProperties.put(ContentModel.PROP_WORKING_COPY_OWNER, userName);
        this.nodeService.addAspect(workingCopy, ContentModel.ASPECT_WORKING_COPY, workingCopyProperties);
        
        // Lock the origional node
        this.lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
        
        // Return the working copy
        return workingCopy;
    }
    
    /**
     * Gets the authenticated users node reference
     * 
     * @return  the users node reference
     */
    private String getUserName()
    {
        String un =  this.authenticationService.getCurrentUserName();
        if (un != null)
        {
           return un;
        }
        else
        {
            throw new CheckOutCheckInServiceException(MSG_ERR_NOT_AUTHENTICATED);
        }
    }

    /**
     * @see org.alfresco.service.cmr.coci.CheckOutCheckInService#checkout(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef checkout(NodeRef nodeRef) 
    {
        // Find the primary parent in order to determine where to put the copy
        ChildAssociationRef childAssocRef = this.nodeService.getPrimaryParent(nodeRef);
        
        // Checkout the working copy to the same destination
        return checkout(nodeRef, childAssocRef.getParentRef(), childAssocRef.getTypeQName(), childAssocRef.getQName());        
    }

    /**
     * @see org.alfresco.repo.version.operations.VersionOperationsService#checkin(org.alfresco.repo.ref.NodeRef, Map<String,Serializable>, java.lang.String, boolean)
     */
    public NodeRef checkin(
            NodeRef workingCopyNodeRef,
            Map<String,Serializable> versionProperties, 
            String contentUrl,
            boolean keepCheckedOut) 
    {
        NodeRef nodeRef = null;
        
        // Check that we have been handed a working copy
        if (this.nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
        {
            // Error since we have not been passed a working copy
            throw new AspectMissingException(ContentModel.ASPECT_WORKING_COPY, workingCopyNodeRef);
        }
        
        // Check that the working node still has the copy aspect applied
        if (this.nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_COPIEDFROM) == true)
        {
            // Disable versionable behaviours since we don't want the auto version policy behaviour to execute when we check-in
            //this.versionableAspect.disableAutoVersion();
            //try
            //{
                Map<QName, Serializable> workingCopyProperties = nodeService.getProperties(workingCopyNodeRef);
                // Try and get the original node reference
                nodeRef = (NodeRef) workingCopyProperties.get(ContentModel.PROP_COPY_REFERENCE);
                if(nodeRef == null)
                {
                    // Error since the original node can not be found
                    throw new CheckOutCheckInServiceException(MSG_ERR_BAD_COPY);                            
                }
                
                try
                {
                    // Release the lock
                    this.lockService.unlock(nodeRef);
                }
                catch (UnableToReleaseLockException exception)
                {
                    throw new CheckOutCheckInServiceException(MSG_ERR_NOT_OWNER, exception);
                }
                
                if (contentUrl != null)
                {
                    ContentData contentData = (ContentData) workingCopyProperties.get(ContentModel.PROP_CONTENT);
                    if (contentData == null)
                    {
                        throw new AlfrescoRuntimeException(MSG_ERR_WORKINGCOPY_HAS_NO_MIMETYPE, new Object[]{workingCopyNodeRef});
                    }
                    else
                    {
                        contentData = new ContentData(
                                contentUrl,
                                contentData.getMimetype(),
                                contentData.getSize(),
                                contentData.getEncoding());
                    }
                    // Set the content url value onto the working copy
                    this.nodeService.setProperty(
                            workingCopyNodeRef, 
                            ContentModel.PROP_CONTENT, 
                            contentData);
                }
                
                // Copy the contents of the working copy onto the original
                this.copyService.copy(workingCopyNodeRef, nodeRef);
                
                if (versionProperties != null && this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
                {
                    // Create the new version
                    this.versionService.createVersion(nodeRef, versionProperties);
                }
                
                if (keepCheckedOut == false)
                {
                    // Delete the working copy
                    this.nodeService.removeAspect(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY);
                    this.nodeService.deleteNode(workingCopyNodeRef);                            
                }
                else
                {
                    // Re-lock the original node
                    this.lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
                }
            //}
            //finally
            //{
            //    this.versionableAspect.enableAutoVersion();
            //}
            
        }
        else
        {
            // Error since the copy aspect is missing
            throw new AspectMissingException(ContentModel.ASPECT_COPIEDFROM, workingCopyNodeRef);
        }
        
        return nodeRef;
    }

    /**
     * @see org.alfresco.service.cmr.coci.CheckOutCheckInService#checkin(org.alfresco.service.cmr.repository.NodeRef, Map, java.lang.String)
     */
    public NodeRef checkin(
            NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties, 
            String contentUrl) 
    {
        return checkin(workingCopyNodeRef, versionProperties, contentUrl, false);
    }

    /**
     * @see org.alfresco.service.cmr.coci.CheckOutCheckInService#checkin(org.alfresco.service.cmr.repository.NodeRef, Map)
     */
    public NodeRef checkin(
            NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties) 
    {
        return checkin(workingCopyNodeRef, versionProperties, null, false);
    }

    /**
     * @see org.alfresco.service.cmr.coci.CheckOutCheckInService#cancelCheckout(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef cancelCheckout(NodeRef workingCopyNodeRef) 
    {
        NodeRef nodeRef = null;
        
        // Check that we have been handed a working copy
        if (this.nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY) == false)
        {
            // Error since we have not been passed a working copy
            throw new AspectMissingException(ContentModel.ASPECT_WORKING_COPY, workingCopyNodeRef);
        }
        
        // Ensure that the node has the copy aspect
        if (this.nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_COPIEDFROM) == true)
        {
            // Get the original node
            nodeRef = (NodeRef)this.nodeService.getProperty(workingCopyNodeRef, ContentModel.PROP_COPY_REFERENCE);
            if (nodeRef == null)
            {
                // Error since the original node can not be found
                throw new CheckOutCheckInServiceException(MSG_ERR_BAD_COPY);
            }            
            
            // Release the lock on the original node
            this.lockService.unlock(nodeRef);
            
            // Delete the working copy
            this.nodeService.removeAspect(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY);
            this.nodeService.deleteNode(workingCopyNodeRef);
        }
        else
        {
            // Error since the copy aspect is missing
            throw new AspectMissingException(ContentModel.ASPECT_COPIEDFROM, workingCopyNodeRef);
        }
        
        return nodeRef;
    }
    
    /**
     * @see org.alfresco.service.cmr.coci.CheckOutCheckInService#getWorkingCopy(org.alfresco.service.cmr.repository.NodeRef)
     */
    public NodeRef getWorkingCopy(NodeRef nodeRef)
    {
        NodeRef workingCopy = null;
        
        // Do a search to find the working copy document
        ResultSet resultSet = null;
        
        try
        {
            resultSet = this.searchService.query(
                    nodeRef.getStoreRef(), 
                    SearchService.LANGUAGE_LUCENE, 
                    "ASPECT:\"" + ContentModel.ASPECT_WORKING_COPY.toString() + "\" +@\\{http\\://www.alfresco.org/model/content/1.0\\}" + ContentModel.PROP_COPY_REFERENCE.getLocalName() + ":\"" + nodeRef.toString() + "\"");
            if (resultSet.getNodeRefs().size() != 0)
            {
                workingCopy = resultSet.getNodeRef(0);
            }
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
        
        return workingCopy;
    }
}
