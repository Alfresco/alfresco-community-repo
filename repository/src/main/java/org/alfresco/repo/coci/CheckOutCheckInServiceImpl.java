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
package org.alfresco.repo.coci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCancelCheckOut;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCheckIn;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCheckOut;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCancelCheckOut;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckIn;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckOut;
import org.alfresco.repo.coci.traitextender.CheckOutCheckInServiceExtension;
import org.alfresco.repo.coci.traitextender.CheckOutCheckInServiceTrait;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AspectMissingException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.AJProxyTrait;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;

/**
 * Check out check in service implementation
 * 
 * @author Roy Wetherall
 */
public class CheckOutCheckInServiceImpl implements CheckOutCheckInService, Extensible
{
    /**
     * I18N labels
     */
    private static final String MSG_ERR_BAD_COPY = "coci_service.err_bad_copy";
    private static final String MSG_WORKING_COPY_LABEL = "coci_service.working_copy_label";
    private static final String MSG_ERR_NOT_OWNER = "coci_service.err_not_owner";
    private static final String MSG_ERR_ALREADY_WORKING_COPY = "coci_service.err_workingcopy_checkout";
    private static final String MSG_ERR_NOT_AUTHENTICATED = "coci_service.err_not_authenticated";
    private static final String MSG_ERR_WORKINGCOPY_HAS_NO_CONTENT = "coci_service.err_workingcopy_has_no_content";
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

    private static Log logger = LogFactory.getLog(CheckOutCheckInServiceImpl.class);

    private NodeService nodeService;
    private VersionService versionService;
    private LockService lockService;
    private CopyService copyService;
    private FileFolderService fileFolderService;
    private OwnableService ownableService;
    private PolicyComponent policyComponent;
    private AuthenticationService authenticationService;
    private RuleService ruleService;

    /** Component to determine which behaviours are active and which not */
    private BehaviourFilter behaviourFilter;
    private final ExtendedTrait<CheckOutCheckInServiceTrait> checkOutCheckInServiceTrait;

    public CheckOutCheckInServiceImpl()
    {
        this.checkOutCheckInServiceTrait = new ExtendedTrait<CheckOutCheckInServiceTrait>(AJProxyTrait.create(this, CheckOutCheckInServiceTrait.class));
    }

    /**
     * @param behaviourFilter
     *            the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * Set the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the version service
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    /**
     * Set the ownable service
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    /**
     * Sets the lock service
     */
    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }

    /**
     * Sets the copy service
     */
    public void setCopyService(CopyService copyService)
    {
        this.copyService = copyService;
    }

    /**
     * Sets the authentication service
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Set the file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @param policyComponent
     *            policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param ruleService
     *            rule service
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
     * @param nodeRef
     *            node reference
     * @return List<QName> list of classes
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
     *            the node to be checked out
     * @param destinationParentNodeRef
     *            the parent of the working copy
     * @param destinationAssocTypeQName
     *            the working copy's primary association type
     * @param destinationAssocQName
     *            the working copy's primary association name
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
     *            the new working copy
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
     *            the current working copy to check in
     * @param versionProperties
     *            Map<String,Serializable>
     * @param contentUrl
     *            String
     * @param keepCheckedOut
     *            boolean
     */
    private void invokeBeforeCheckIn(
            NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties,
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
     *            the node being checked in
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
     *            the working copy that will be destroyed
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
     *            the working copy that will be destroyed
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

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public NodeRef checkout(NodeRef nodeRef)
    {
        // Find the primary parent in order to determine where to put the copy
        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(nodeRef);

        // Checkout the working copy to the same destination
        return checkout(nodeRef, childAssocRef.getParentRef(), childAssocRef.getTypeQName(), childAssocRef.getQName());
    }

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public NodeRef checkout(
            final NodeRef nodeRef,
            final NodeRef destinationParentNodeRef,
            final QName destinationAssocTypeQName,
            QName destinationAssocQName)
    {
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CHECKED_OUT))
        {
            throw new CheckOutCheckInServiceException(MSG_ALREADY_CHECKEDOUT);
        }

        // Make sure we are not checking out a working copy node
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            throw new CheckOutCheckInServiceException(MSG_ERR_ALREADY_WORKING_COPY);
        }

        // It is not enough to check LockUtils.isLockedOrReadOnly in case when the same user does offline and online edit (for instance in two open browsers). In this case we get
        // set ContentModel.ASPECT_LOCKABLE and LockType.WRITE_LOCK. So, here we have to check following
        LockStatus lockStatus = lockService.getLockStatus(nodeRef);
        if (lockStatus != LockStatus.NO_LOCK && lockStatus != LockStatus.LOCK_EXPIRED)
        {
            throw new NodeLockedException(nodeRef);
        }

        behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        behaviourFilter.disableBehaviour(destinationParentNodeRef, ContentModel.ASPECT_AUDITABLE);
        try
        {
            return doCheckout(nodeRef, destinationParentNodeRef, destinationAssocTypeQName, destinationAssocQName);
        }
        finally
        {
            behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            behaviourFilter.enableBehaviour(destinationParentNodeRef, ContentModel.ASPECT_AUDITABLE);
        }
    }

    private NodeRef doCheckout(
            final NodeRef nodeRef,
            final NodeRef destinationParentNodeRef,
            final QName destinationAssocTypeQName,
            QName destinationAssocQName)
    {
        // Invoke before check out policy
        invokeBeforeCheckOut(nodeRef, destinationParentNodeRef, destinationAssocTypeQName, destinationAssocQName);

        // Apply the lock aspect if required
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE) == false)
        {
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_LOCKABLE, null);
        }

        // Get the user
        final String userName = getUserName();

        NodeRef workingCopy = null;
        ruleService.disableRuleType(RuleType.UPDATE);
        try
        {
            // Rename the working copy
            String copyName = (String) this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            String workingCopyLabel = getWorkingCopyLabel();
            copyName = createWorkingCopyName(copyName, workingCopyLabel);

            // Make the working copy
            final QName copyQName = QName.createQName(destinationAssocQName.getNamespaceURI(), QName.createValidLocalName(copyName));

            // Find the primary parent
            ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(nodeRef);

            // If destination parent for working copy is the same as the parent of the source node
            // then working copy should be created even if the user has no permissions to create children in
            // the parent of the source node
            if (destinationParentNodeRef.equals(childAssocRef.getParentRef()))
            {
                workingCopy = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {
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
            nodeService.setProperty(workingCopy, ContentModel.PROP_NAME, copyName);

            // Apply the working copy aspect to the working copy
            Map<QName, Serializable> workingCopyProperties = new HashMap<QName, Serializable>(1);
            workingCopyProperties.put(ContentModel.PROP_WORKING_COPY_OWNER, userName);
            workingCopyProperties.put(ContentModel.PROP_WORKING_COPY_LABEL, workingCopyLabel);
            nodeService.addAspect(workingCopy, ContentModel.ASPECT_WORKING_COPY, workingCopyProperties);
            nodeService.addAspect(workingCopy, ContentModel.ASPECT_LOCKABLE, null);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_CHECKED_OUT, null);
            nodeService.createAssociation(nodeRef, workingCopy, ContentModel.ASSOC_WORKING_COPY_LINK);
            nodeService.removeAspect(workingCopy, ContentModel.ASPECT_CMIS_CREATED_CHECKEDOUT);
        }
        finally
        {
            ruleService.enableRuleType(RuleType.UPDATE);
        }

        // Lock the original node
        lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);

        // Invoke on check out policy
        invokeOnCheckOut(workingCopy);
        return workingCopy;
    }

    /**
     * Gets the authenticated users node reference
     * 
     * @return the users node reference
     */
    private String getUserName()
    {
        String un = this.authenticationService.getCurrentUserName();
        if (un != null)
        {
            return un;
        }
        else
        {
            throw new CheckOutCheckInServiceException(MSG_ERR_NOT_AUTHENTICATED);
        }
    }

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public NodeRef checkin(
            NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties)
    {
        return checkin(workingCopyNodeRef, versionProperties, null, false);
    }

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public NodeRef checkin(
            NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties,
            String contentUrl)
    {
        return checkin(workingCopyNodeRef, versionProperties, contentUrl, false);
    }

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public NodeRef checkin(
            NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties,
            String contentUrl,
            boolean keepCheckedOut)
    {
        // Check that we have been handed a working copy
        if (!nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            // Error since we have not been passed a working copy
            throw new AspectMissingException(ContentModel.ASPECT_WORKING_COPY, workingCopyNodeRef);
        }

        // Get the checked out node
        NodeRef nodeRef = getCheckedOut(workingCopyNodeRef);
        if (nodeRef == null)
        {
            // Error since the original node can not be found
            throw new CheckOutCheckInServiceException(MSG_ERR_BAD_COPY);
        }

        // Invoke policy
        invokeBeforeCheckIn(workingCopyNodeRef, versionProperties, contentUrl, keepCheckedOut);

        try
        {
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
            {
                // Release the lock on the original node
                lockService.unlock(nodeRef, false, true);
            }
        }
        catch (UnableToReleaseLockException exception)
        {
            throw new CheckOutCheckInServiceException(MSG_ERR_NOT_OWNER, exception);
        }

        if (contentUrl != null)
        {
            ContentData contentData = (ContentData) nodeService.getProperty(workingCopyNodeRef, ContentModel.PROP_CONTENT);
            if (contentData == null)
            {
                throw new AlfrescoRuntimeException(MSG_ERR_WORKINGCOPY_HAS_NO_CONTENT, new Object[]{workingCopyNodeRef});
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
            nodeService.setProperty(
                    workingCopyNodeRef,
                    ContentModel.PROP_CONTENT,
                    contentData);
        }

        // Copy the contents of the working copy onto the original
        this.copyService.copy(workingCopyNodeRef, nodeRef);

        // Handle name change on working copy (only for folders/files)
        if (fileFolderService.getFileInfo(workingCopyNodeRef) != null)
        {
            String origName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            String name = (String) nodeService.getProperty(workingCopyNodeRef, ContentModel.PROP_NAME);
            String wcLabel = (String) this.nodeService.getProperty(workingCopyNodeRef, ContentModel.PROP_WORKING_COPY_LABEL);
            if (wcLabel == null)
            {
                wcLabel = getWorkingCopyLabel();
            }

            if (hasWorkingCopyNameChanged(name, origName, wcLabel))
            {
                // ensure working copy has working copy label in its name to avoid name clash
                if (!name.contains(" " + wcLabel))
                {
                    try
                    {
                        fileFolderService.rename(workingCopyNodeRef, createWorkingCopyName(name, wcLabel));
                    }
                    catch (FileExistsException e)
                    {
                        throw new CheckOutCheckInServiceException(e, MSG_ERR_CANNOT_RENAME, name, wcLabel);
                    }
                    catch (FileNotFoundException e)
                    {
                        throw new CheckOutCheckInServiceException(e, MSG_ERR_CANNOT_RENAME, name, wcLabel);
                    }
                }
                String newName = getNameFromWorkingCopyName(name, wcLabel);
                try
                {
                    // rename original to changed working name
                    fileFolderService.rename(nodeRef, newName);
                }
                catch (FileExistsException e)
                {
                    throw new CheckOutCheckInServiceException(e, MSG_ERR_CANNOT_RENAME, origName, newName);
                }
                catch (FileNotFoundException e)
                {
                    throw new CheckOutCheckInServiceException(e, MSG_ERR_CANNOT_RENAME, name, newName);
                }
            }
        }

        if (versionProperties != null &&
                nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) &&
                !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CMIS_CREATED_CHECKEDOUT))
        {
            // Create the new version
            this.versionService.createVersion(nodeRef, versionProperties);
        }

        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_CMIS_CREATED_CHECKEDOUT);

        if (keepCheckedOut == false)
        {
            // Delete the working copy
            // Disable cm:auditable aspect
            // See MNT-8789
            behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            behaviourFilter.disableBehaviour(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY);
            try
            {
                // Clean up original node
                // Note: Lock has already been removed. So no lockService.unlock(nodeRef);
                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_CHECKED_OUT);

                // Delete the working copy
                nodeService.deleteNode(workingCopyNodeRef);
            }
            finally
            {
                // Just for symmetry; the node is gone
                behaviourFilter.enableBehaviour(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY);
                behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            }
        }
        else
        {
            // Re-lock the original node
            lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
        }

        // Invoke policy
        invokeOnCheckIn(nodeRef);

        return nodeRef;
    }

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public NodeRef cancelCheckout(NodeRef workingCopyNodeRef)
    {
        // Check that we have been handed a working copy
        if (!nodeService.hasAspect(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            // Error since we have not been passed a working copy
            throw new AspectMissingException(ContentModel.ASPECT_WORKING_COPY, workingCopyNodeRef);
        }

        // Get the checked out node
        NodeRef nodeRef = getCheckedOut(workingCopyNodeRef);
        if (nodeRef == null)
        {
            // Error since the original node can not be found
            throw new CheckOutCheckInServiceException(MSG_ERR_BAD_COPY);
        }

        // Invoke policy
        invokeBeforeCancelCheckOut(workingCopyNodeRef);

        behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        behaviourFilter.disableBehaviour(workingCopyNodeRef, ContentModel.ASPECT_WORKING_COPY);
        behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_CMIS_CREATED_CHECKEDOUT);
        try
        {
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
            {
                // Release the lock on the original node
                lockService.unlock(nodeRef, false, true);
            }
            nodeService.removeAspect(nodeRef, ContentModel.ASPECT_CHECKED_OUT);

            // Delete the working copy
            nodeService.deleteNode(workingCopyNodeRef);

            // Invoke policy
            invokeOnCancelCheckOut(nodeRef);

            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CMIS_CREATED_CHECKEDOUT))
            {
                nodeService.deleteNode(nodeRef);
            }
        }
        catch (UnableToReleaseLockException exception)
        {
            throw new CheckOutCheckInServiceException(MSG_ERR_NOT_OWNER, exception);
        }
        finally
        {
            behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_CMIS_CREATED_CHECKEDOUT);
        }

        return nodeRef;
    }

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public NodeRef getWorkingCopy(NodeRef nodeRef)
    {
        NodeRef workingCopy = null;
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CHECKED_OUT))
        {
            List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);
            // It is a 1:1 relationship
            if (assocs.size() == 0)
            {
                logger.warn("Found node with cm:checkedOut aspect but no association.  Current node state: " + nodeService.getNodeStatus(nodeRef));
            }
            else if (assocs.size() > 1)
            {
                logger.warn("Found multiple " + ContentModel.ASSOC_WORKING_COPY_LINK + " association from node: " + nodeRef);
            }
            else
            {
                workingCopy = assocs.get(0).getTargetRef();
            }
        }

        return workingCopy;
    }

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public NodeRef getCheckedOut(NodeRef nodeRef)
    {
        NodeRef original = null;
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            List<AssociationRef> assocs = nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);
            // It is a 1:1 relationship
            if (assocs.size() == 0)
            {
                logger.warn("Found node with cm:workingcopy aspect but no association.  Current node state: " + nodeService.getNodeStatus(nodeRef));
            }
            else if (assocs.size() > 1)
            {
                logger.warn("Found multiple " + ContentModel.ASSOC_WORKING_COPY_LINK + " association to node: " + nodeRef);
            }
            else
            {
                original = assocs.get(0).getSourceRef();
            }
        }

        return original;
    }

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public boolean isWorkingCopy(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);
    }

    @Override
    @Extend(traitAPI = CheckOutCheckInServiceTrait.class, extensionAPI = CheckOutCheckInServiceExtension.class)
    public boolean isCheckedOut(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ContentModel.ASPECT_CHECKED_OUT);
    }

    /**
     * Create a working copy name using the given fileName and workingCopyLabel. The label will be inserted before the file extension (if present), or else appended to the name (in either case a space is prepended to the workingCopyLabel).
     * <p>
     * Examples, where workingCopyLabel is "wc":
     * <p>
     * "Myfile.txt" becomes "Myfile wc.txt", "Myfile" becomes "Myfile wc".
     * <p>
     * In the event that fileName is empty or null, the workingCopyLabel is used for the new working copy name
     * <p>
     * Example: "" becomes "wc".
     * 
     * @param name
     *            String
     * @param workingCopyLabel
     *            String
     * @return String
     */
    public static String createWorkingCopyName(String name, final String workingCopyLabel)
    {
        if (workingCopyLabel != null && workingCopyLabel.length() != 0)
        {
            if (name != null && name.length() != 0)
            {
                int index = name.lastIndexOf(EXTENSION_CHARACTER);
                if (index > 0)
                {
                    // Insert the working copy label before the file extension
                    name = name.substring(0, index) + " " + workingCopyLabel + name.substring(index);
                }
                else
                {
                    // Simply append the working copy label onto the end of the existing name
                    name = name + " " + workingCopyLabel;
                }
            }
            else
            {
                name = workingCopyLabel;
            }
        }
        else
        {
            throw new IllegalArgumentException("workingCopyLabel is null or empty");
        }

        return name;
    }

    /**
     * Get original name from the working copy name and the cm:workingCopyLabel that was used to create it.
     * 
     * @param workingCopyName
     *            String
     * @param workingCopyLabel
     *            String
     * @return original name
     */
    private String getNameFromWorkingCopyName(String workingCopyName, String workingCopyLabel)
    {
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
     * @param workingCopyName
     *            working copy name
     * @param origName
     *            original name
     * @param wcLabel
     *            that was inserted into origName to create the original working copy name
     * @return true => if changed
     */
    private boolean hasWorkingCopyNameChanged(String workingCopyName, String origName, String wcLabel)
    {
        String origWorkingCopyName = createWorkingCopyName(origName, wcLabel);
        return !workingCopyName.equals(origWorkingCopyName);
    }

    /**
     * Get the working copy label.
     * 
     * @return the working copy label
     */
    public static String getWorkingCopyLabel()
    {
        return I18NUtil.getMessage(MSG_WORKING_COPY_LABEL);
    }

    @Override
    public <T extends Trait> ExtendedTrait<T> getTrait(Class<? extends T> traitAPI)
    {
        return (ExtendedTrait<T>) checkOutCheckInServiceTrait;
    }
}
