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
package org.alfresco.repo.coci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCancelCheckOut;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCheckIn;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCheckOut;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCancelCheckOut;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckIn;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckOut;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionableAspect;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Check out check in service implementation
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
    private static final String MSG_ERR_CANNOT_RENAME = "coci_service.err_cannot_rename";
    
    /** Class policy delegate's */
    private ClassPolicyDelegate<BeforeCheckOut> beforeCheckOut;
    private ClassPolicyDelegate<OnCheckOut> onCheckOut;
    private ClassPolicyDelegate<BeforeCheckIn> beforeCheckIn;
    private ClassPolicyDelegate<OnCheckIn> onCheckIn;
    private ClassPolicyDelegate<BeforeCancelCheckOut> beforeCancelCheckOut;
    private ClassPolicyDelegate<OnCancelCheckOut> onCancelCheckOut;

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
     * The file folder service
     */
    private FileFolderService fileFolderService;
    
    /** Ownable service */
    private OwnableService ownableService;
    
    /**
     * The search service
     */
    private SearchService searchService;
    
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /**
     * The authentication service
     */
    private AuthenticationService authenticationService;
    
    /** Rule service */
    private RuleService ruleService;
    
    /**
     * The versionable aspect behaviour implementation
     */
    @SuppressWarnings("unused")
    private VersionableAspect versionableAspect;
    
    private BehaviourFilter behaviourFilter;
    
    /**
     * @param behaviourFilter the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
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
     * Set the ownable service
     * @param ownableService	ownable service
     */
    public void setOwnableService(OwnableService ownableService) 
    {
		this.ownableService = ownableService;
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
    public void setCopyService(CopyService copyService) 
    {
        this.copyService = copyService;
    }
    
    /**
     * Sets the authentication service
     * 
     * @param authenticationService  the authentication service
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
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
     * Set the file folder service
     * 
     * @param fileFolderService     the file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
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
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * @param ruleService   rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * Initialise method
     */
    public void init()
    {
        // Register the policies
        beforeCheckOut = policyComponent.registerClassPolicy(CheckOutCheckInServicePolicies.BeforeCheckOut.class);
        onCheckOut = policyComponent.registerClassPolicy(CheckOutCheckInServicePolicies.OnCheckOut.class);
        beforeCheckIn = policyComponent.registerClassPolicy(CheckOutCheckInServicePolicies.BeforeCheckIn.class);
        onCheckIn = policyComponent.registerClassPolicy(CheckOutCheckInServicePolicies.OnCheckIn.class);
        beforeCancelCheckOut = policyComponent.registerClassPolicy(CheckOutCheckInServicePolicies.BeforeCancelCheckOut.class);
        onCancelCheckOut = policyComponent.registerClassPolicy(CheckOutCheckInServicePolicies.OnCancelCheckOut.class);
    }
    
    /**
     * Returns all the classes of a node, including its type and aspects.
     * 
     * @param nodeRef       node reference
     * @return List<QName>  list of classes
     */
    private List<QName> getInvokeClasses(NodeRef nodeRef)
    {
        List<QName> result = new ArrayList<QName>(10);        
        result.add(nodeService.getType(nodeRef));
        Set<QName> aspects = nodeService.getAspects(nodeRef);
        for (QName aspect : aspects)
        {
            result.add(aspect);
        }
        return result;      
    }

    /**
     * Invoke the before check out policy
     * 
     * @param nodeRef
     * @param destinationParentNodeRef
     * @param destinationAssocTypeQName
     * @param destinationAssocQName
     */
    private void invokeBeforeCheckOut(
            NodeRef nodeRef,
            NodeRef destinationParentNodeRef,           
            QName destinationAssocTypeQName, 
            QName destinationAssocQName)
    {
        List<QName> classes = getInvokeClasses(nodeRef);
        for (QName invokeClass : classes)
        {            
            Collection<BeforeCheckOut> policies = beforeCheckOut.getList(invokeClass);
            for (BeforeCheckOut policy : policies) 
            {
                policy.beforeCheckOut(nodeRef, destinationParentNodeRef, destinationAssocTypeQName, destinationAssocQName);
            }
            
        }
    }
    
    /**
     * Invoke on the on check out policy
     * 
     * @param workingCopy
     */
    private void invokeOnCheckOut(NodeRef workingCopy)
    {
        List<QName> classes = getInvokeClasses(workingCopy);
        for (QName invokeClass : classes)
        {            
            Collection<OnCheckOut> policies = onCheckOut.getList(invokeClass);
            for (OnCheckOut policy : policies) 
            {
                policy.onCheckOut(workingCopy);
            }
            
        }
    }
    
    /**
     * Invoke before check in policy
     * 
     * @param workingCopyNodeRef
     * @param versionProperties
     * @param contentUrl
     * @param keepCheckedOut
     */
    private void invokeBeforeCheckIn(
            NodeRef workingCopyNodeRef,
            Map<String,Serializable> versionProperties,
            String contentUrl,
            boolean keepCheckedOut)
    {
        List<QName> classes = getInvokeClasses(workingCopyNodeRef);
        for (QName invokeClass : classes)
        {            
            Collection<BeforeCheckIn> policies = beforeCheckIn.getList(invokeClass);
            for (BeforeCheckIn policy : policies) 
            {
                policy.beforeCheckIn(workingCopyNodeRef, versionProperties, contentUrl, keepCheckedOut);
            }
            
        }
    }
    
    /**
     * Invoke on check in policy
     * 
     * @param nodeRef
     */
    private void invokeOnCheckIn(NodeRef nodeRef)
    {
        List<QName> classes = getInvokeClasses(nodeRef);
        for (QName invokeClass : classes)
        {            
            Collection<OnCheckIn> policies = onCheckIn.getList(invokeClass);
            for (OnCheckIn policy : policies) 
            {
                policy.onCheckIn(nodeRef);
            }
            
        }
    }
    
    /**
     * Invoke before cancel check out
     * 
     * @param workingCopy
     */
    private void invokeBeforeCancelCheckOut(NodeRef workingCopy)
    {
        List<QName> classes = getInvokeClasses(workingCopy);
        for (QName invokeClass : classes)
        {            
            Collection<BeforeCancelCheckOut> policies = beforeCancelCheckOut.getList(invokeClass);
            for (BeforeCancelCheckOut policy : policies) 
            {
                policy.beforeCancelCheckOut(workingCopy);
            }
            
        }
    }
    
    /**
     * Invoke on cancel check out
     * 
     * @param nodeRef
     */
    private void invokeOnCancelCheckOut(NodeRef nodeRef)
    {
        List<QName> classes = getInvokeClasses(nodeRef);
        for (QName invokeClass : classes)
        {            
            Collection<OnCancelCheckOut> policies = onCancelCheckOut.getList(invokeClass);
            for (OnCancelCheckOut policy : policies) 
            {
                policy.onCancelCheckOut(nodeRef);
            }
            
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.coci.CheckOutCheckInService#checkout(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
     */
    public NodeRef checkout(
            final NodeRef nodeRef, 
            final NodeRef destinationParentNodeRef,
            final QName destinationAssocTypeQName, 
            QName destinationAssocQName) 
    {
        LockType lockType = this.lockService.getLockType(nodeRef);
        if (LockType.READ_ONLY_LOCK.equals(lockType) == true || getWorkingCopy(nodeRef) != null)
        {
            throw new CheckOutCheckInServiceException(MSG_ALREADY_CHECKEDOUT);
        }
    
        // Make sure we are no checking out a working copy node
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY) == true)
        {
            throw new CheckOutCheckInServiceException(MSG_ERR_ALREADY_WORKING_COPY);
        }
        
        behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        behaviourFilter.disableBehaviour(destinationParentNodeRef, ContentModel.ASPECT_AUDITABLE);
        try {
            return doCheckout(nodeRef, destinationParentNodeRef, destinationAssocTypeQName, destinationAssocQName);
        }
        finally
        {
            behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            behaviourFilter.enableBehaviour(destinationParentNodeRef, ContentModel.ASPECT_AUDITABLE);
        }
    }

    /**
     * @param nodeRef
     * @param destinationParentNodeRef
     * @param destinationAssocTypeQName
     * @param destinationAssocQName
     * @return
     */
    private NodeRef doCheckout(final NodeRef nodeRef, final NodeRef destinationParentNodeRef,
                final QName destinationAssocTypeQName, QName destinationAssocQName)
    {
        // Apply the lock aspect if required
        if (this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == false)
        {
            this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        }
        
        // Invoke before check out policy
        invokeBeforeCheckOut(nodeRef, destinationParentNodeRef, destinationAssocTypeQName, destinationAssocQName);
        
        // Rename the working copy
        String copyName = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        copyName = createWorkingCopyName(copyName);        

        // Get the user 
        final String userName = getUserName();
        
        NodeRef workingCopy = null;
        ruleService.disableRuleType(RuleType.UPDATE);
        try
        {           
            // Make the working copy
            final QName copyQName = QName.createQName(destinationAssocQName.getNamespaceURI(), QName.createValidLocalName(copyName));
         
            // Find the primary parent
            ChildAssociationRef childAssocRef = this.nodeService.getPrimaryParent(nodeRef);
            
            // If destination parent for working copy is the same as the parent of the source node
            // then working copy should be created even if the user has no permissions to create children in 
            // the parent of the source node 
            if (destinationParentNodeRef.equals(childAssocRef.getParentRef()))
            {
                workingCopy = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
                {
                    public NodeRef doWork() throws Exception
                    {
                        NodeRef copy = copyService.copy(
                                nodeRef, 
                                destinationParentNodeRef, 
                                destinationAssocTypeQName, 
                                copyQName);

                        // Set the owner of the working copy to be the current user
                        ownableService.setOwner(copy, userName);
                        return copy;
                    }
                }, AuthenticationUtil.getSystemUserName());
            }
            else
            {
                workingCopy = copyService.copy(
                        nodeRef, 
                        destinationParentNodeRef, 
                        destinationAssocTypeQName, 
                        copyQName);
            }
        
        
            // Update the working copy name        
            this.nodeService.setProperty(workingCopy, ContentModel.PROP_NAME, copyName);
            
            // Apply the working copy aspect to the working copy
            Map<QName, Serializable> workingCopyProperties = new HashMap<QName, Serializable>(1);
            workingCopyProperties.put(ContentModel.PROP_WORKING_COPY_OWNER, userName);
            this.nodeService.addAspect(workingCopy, ContentModel.ASPECT_WORKING_COPY, workingCopyProperties);
        }
        finally
        {
            ruleService.enableRuleType(RuleType.UPDATE);
        }
        
        // Lock the original node
        this.lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
        
        // Invoke on check out policy
        invokeOnCheckOut(workingCopy);
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
            // Invoke policy
            invokeBeforeCheckIn(workingCopyNodeRef, versionProperties, contentUrl, keepCheckedOut);
            
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
            
            // Handle name change on working copy (only for folders/files)
            if (fileFolderService.getFileInfo(workingCopyNodeRef) != null)
            {
                String origName = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                String name = (String)this.nodeService.getProperty(workingCopyNodeRef, ContentModel.PROP_NAME);
                if (hasWorkingCopyNameChanged(name, origName))
                {
                    // ensure working copy has working copy label in its name to avoid name clash
                    if (!name.contains(" " + getWorkingCopyLabel()))
                    {
                        try
                        {
                            fileFolderService.rename(workingCopyNodeRef, createWorkingCopyName(name));
                        }
                        catch (FileExistsException e)
                        {
                            throw new CheckOutCheckInServiceException(e, MSG_ERR_CANNOT_RENAME, name, createWorkingCopyName(name));
                        }
                        catch (FileNotFoundException e)
                        {
                            throw new CheckOutCheckInServiceException(e, MSG_ERR_CANNOT_RENAME, name, createWorkingCopyName(name));
                        }
                    }
                    try
                    {
                        // rename original to changed working name
                        fileFolderService.rename(nodeRef, getNameFromWorkingCopyName(name));
                    }
                    catch (FileExistsException e)
                    {
                        throw new CheckOutCheckInServiceException(e, MSG_ERR_CANNOT_RENAME, origName, getNameFromWorkingCopyName(name));
                    }
                    catch (FileNotFoundException e)
                    {
                        throw new CheckOutCheckInServiceException(e, MSG_ERR_CANNOT_RENAME, name, getNameFromWorkingCopyName(name));
                    }
                }
            }
            
            if (versionProperties != null && this.nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == true)
            {
                // Create the new version
                this.versionService.createVersion(nodeRef, versionProperties);
            }
            
            if (keepCheckedOut == false)
            {
                // Delete the working copy
                this.nodeService.deleteNode(workingCopyNodeRef);
                
                // Remove the lock aspect (copied from working copy)
                this.nodeService.removeAspect(nodeRef, ContentModel.ASPECT_LOCKABLE);
            }
            else
            {
                // Re-lock the original node
                this.lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
            }
                
           // Invoke policy
           invokeOnCheckIn(nodeRef);
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
            // Invoke policy
            invokeBeforeCancelCheckOut(workingCopyNodeRef);
            
            // Get the original node
            nodeRef = (NodeRef)this.nodeService.getProperty(workingCopyNodeRef, ContentModel.PROP_COPY_REFERENCE);
            if (nodeRef == null)
            {
                // Error since the original node can not be found
                throw new CheckOutCheckInServiceException(MSG_ERR_BAD_COPY);
            }            
            behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            try{
                
                // Release the lock on the original node
                this.lockService.unlock(nodeRef);

                // Delete the working copy
                this.nodeService.deleteNode(workingCopyNodeRef);

                // Invoke policy
                invokeOnCancelCheckOut(nodeRef);
            }
            finally
            {
                behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            }
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
                    "+ASPECT:\"" + ContentModel.ASPECT_WORKING_COPY.toString() + "\" +@\\{http\\://www.alfresco.org/model/content/1.0\\}" + ContentModel.PROP_COPY_REFERENCE.getLocalName() + ":\"" + nodeRef.toString() + "\"");
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

    /**
     * Create working copy name
     * 
     * @param name  name
     * @return  working copy name
     */
    public String createWorkingCopyName(String name)
    {
        if (this.getWorkingCopyLabel() != null && this.getWorkingCopyLabel().length() != 0)
        {
            if (name != null && name.length() != 0)
            {
                int index = name.lastIndexOf(EXTENSION_CHARACTER);
                if (index > 0)
                {
                    // Insert the working copy label before the file extension
                    name = name.substring(0, index) + " " + getWorkingCopyLabel() + name.substring(index);
                }
                else
                {
                    // Simply append the working copy label onto the end of the existing name
                    name = name + " " + getWorkingCopyLabel();
                }
            }
            else
            {
                name = getWorkingCopyLabel();
            }
        }
        return name;
    }
    
    /**
     * Get original name from working copy name
     * 
     * @param workingCopyName
     * @return  original name
     */
    private String getNameFromWorkingCopyName(String workingCopyName)
    {
        String workingCopyLabel = getWorkingCopyLabel();
        String workingCopyLabelRegEx = workingCopyLabel.replaceAll("\\(", "\\\\(");
        workingCopyLabelRegEx = workingCopyLabelRegEx.replaceAll("\\)", "\\\\)");
        if (workingCopyName.contains(" " + workingCopyLabel))
        {
            workingCopyName = workingCopyName.replaceFirst(" " + workingCopyLabelRegEx, "");
        }
        else if (workingCopyName.contains(workingCopyLabel))
        {
            workingCopyName = workingCopyName.replaceFirst(workingCopyLabelRegEx, "");
        }
        return workingCopyName;
    }

    /**
     * Has the working copy name changed compared to the original name
     * 
     * @param name  working copy name
     * @param origName  original name
     * @return  true => if changed
     */
    private boolean hasWorkingCopyNameChanged(String workingCopyName, String origName)
    {
        return !workingCopyName.equals(createWorkingCopyName(origName));
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
    
}
