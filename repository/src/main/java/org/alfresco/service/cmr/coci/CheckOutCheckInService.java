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
package org.alfresco.service.cmr.coci;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Version operations service interface
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface CheckOutCheckInService
{
    /**
     * Checks out the given node placing a working copy in the destination specified.
     * <p>
     * When a node is checked out a read-only lock is placed on the original node and a working copy is placed in the destination specified.
     * <p>
     * The copy aspect is applied to the working copy so that the original node can be identified.
     * <p>
     * The working copy aspect is applied to the working copy so that it can be identified as the working copy of a checked out node.
     * <p>
     * The working copy node reference is returned to the caller.
     * 
     * @param nodeRef
     *            a reference to the node to checkout
     * @param destinationParentNodeRef
     *            the destination node reference for the working copy
     * @param destinationAssocTypeQName
     *            the destination child assoc type for the working copy
     * @param destinationAssocQName
     *            the destination child assoc qualified name for the working copy
     * @return node reference to the created working copy
     */
    @Auditable(parameters = {"nodeRef", "destinationParentNodeRef", "destinationAssocTypeQName", "destinationAssocQName"})
    public NodeRef checkout(
            NodeRef nodeRef,
            NodeRef destinationParentNodeRef,
            QName destinationAssocTypeQName,
            QName destinationAssocQName);

    /**
     * Checks out the working copy of the node into the same parent node with the same child associations details.
     * 
     * @see CheckOutCheckInService#checkout(NodeRef, NodeRef, QName, QName)
     * 
     * @param nodeRef
     *            a reference to the node to checkout
     * @return a node reference to the created working copy
     */
    @Auditable(parameters = {"nodeRef"})
    public NodeRef checkout(NodeRef nodeRef);

    /**
     * Checks in the working node specified.
     * <p>
     * When a working copy is checked in the current state of the working copy is copied to the original node. This will include any content updated in the working node.
     * <p>
     * If versioning is not enabled on a node (the versionable aspect is not present on the node), the check in overwrites the existing node and releases the lock unless the keepCheckedOut flag is used. With versioning enabled on the node, a new version is always created.
     * <p>
     * If a content Url is provided it will be used to update the content of the working node before the checkin operation takes place.
     * <p>
     * Once the operation has completed the read lock applied to the original node during checkout will be removed and the working copy of the node deleted from the repository, unless the operation is instructed to keep the original node checked out. In which case the lock and the working copy will remain.
     * <p>
     * The node reference to the original node is returned.
     * 
     * @param workingCopyNodeRef
     *            the working copy node reference
     * @param versionProperties
     *            the version properties. If null is passed then the original node is NOT versioned during the checkin operation.
     * @param contentUrl
     *            a content url that should be set on the working copy before the checkin operation takes place. If null then the current working copy content is copied back to the original node.
     * @param keepCheckedOut
     *            indicates whether the node should remain checked out after the checkin has taken place. When the node remains checked out the working node reference remains the same.
     * @return the node reference to the original node, updated with the checked in state
     */
    @Auditable(parameters = {"workingCopyNodeRef", "versionProperties", "contentUrl", "keepCheckedOut"})
    public NodeRef checkin(
            NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties,
            String contentUrl,
            boolean keepCheckedOut);

    /**
     * By default the checked in node is not keep checked in.
     * 
     *
     * @param workingCopyNodeRef
     *            the working copy node reference
     * @param versionProperties
     *            the version properties. If null is passed then the original node is NOT versioned during the checkin operation.
     * @param contentUrl
     *            a content url that should be set on the working copy before the checkin operation takes place. If null then the current working copy content is copied back to the original node.
     * @return the node reference to the original node, updated with the checked in state
     */
    @Auditable(parameters = {"workingCopyNodeRef", "versionProperties", "contentUrl"})
    public NodeRef checkin(
            NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties,
            String contentUrl);

    /**
     * If no content url is specified then current content set on the working copy is understood to be current.
     * 
     *
     * @param workingCopyNodeRef
     *            the working copy node reference
     * @param versionProperties
     *            the version properties. If null is passed then the original node is NOT versioned during the checkin operation.
     * @return the node reference to the original node, updated with the checked in state
     */
    @Auditable(parameters = {"workingCopyNodeRef", "versionProperties"})
    public NodeRef checkin(
            NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties);

    /**
     * Cancels the checkout for a given working copy.
     * <p>
     * The read-only lock on the original node is removed and the working copy is removed.
     * <p>
     * Note that all modification made to the working copy will be lost and the original node will remain unchanged.
     * <p>
     * A reference to the original node reference is returned.
     * 
     * @param workingCopyNodeRef
     *            the working copy node reference
     * @return the original node reference
     */
    @Auditable(parameters = {"workingCopyNodeRef"})
    public NodeRef cancelCheckout(NodeRef workingCopyNodeRef);

    /**
     * Helper method to retrieve the working copy node reference for a checked out node.
     * <p>
     * A null node reference is returned if the node is not checked out.
     * 
     * @param nodeRef
     *            a node reference
     * @return the working copy node reference or null if none.
     */
    @Auditable(parameters = {"nodeRef"})
    public NodeRef getWorkingCopy(NodeRef nodeRef);

    /**
     * Helper method to retrieve the original node (check-out source) for a working copy.
     * <p/>
     * A <tt>null</tt> node reference is returned if the node is not a working copy.
     * 
     * @param nodeRef
     *            the (potential) working copy
     * @return the original (source) node or <tt>null</tt> if it is not a working copy
     */
    @Auditable(parameters = {"nodeRef"})
    public NodeRef getCheckedOut(NodeRef nodeRef);

    /**
     * Determine if a node is a working copy or not
     * 
     * @param nodeRef
     *            the (potential) working copy
     * @return <tt>true</tt> if the node is a working copy otherwise <tt>false</tt>
     */
    @Auditable(parameters = {"nodeRef"})
    public boolean isWorkingCopy(NodeRef nodeRef);

    /**
     * Determine if a node has been checked out or not
     * 
     * @param nodeRef
     *            the (potentially) checked out node
     * @return <tt>true</tt> if the node has been checked out otherwise <tt>false</tt>
     */
    @Auditable(parameters = {"nodeRef"})
    public boolean isCheckedOut(NodeRef nodeRef);
}
