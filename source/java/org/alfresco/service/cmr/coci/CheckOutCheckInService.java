/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.cmr.coci;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Version operations service interface
 * 
 * @author Roy Wetherall
 */
public interface CheckOutCheckInService 
{
	/**
	 * Checks out the given node placing a working copy in the destination specified.
	 * <p>
	 * When a node is checked out a read-only lock is placed on the original node and
	 * a working copy is placed in the destination specified.
	 * <p>
	 * The copy aspect is applied to the working copy so that the original node can be 
	 * identified.
	 * <p>
	 * The working copy aspect is applied to the working copy so that it can be identified
	 * as the working copy of a checked out node.
	 * <p>
	 * The working copy node reference is returned to the caller.
	 * 
	 * @param nodeRef						a reference to the node to checkout
	 * @param destinationParentNodeRef		the destination node reference for the working 
	 * 										copy
	 * @param destinationAssocTypeQName		the destination child assoc type for the working
	 * 										copy
	 * @param destinationAssocQName			the destination child assoc qualified name for 
	 * 										the working copy
	 * @return								node reference to the created working copy
	 */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef", "destinationParentNodeRef", "destinationAssocTypeQName", "destinationAssocQName"})
	public NodeRef checkout(
			NodeRef nodeRef,
			NodeRef destinationParentNodeRef,			
            QName destinationAssocTypeQName, 
            QName destinationAssocQName);
	
	/**
	 * Checks out the working copy of the node into the same parent node with the same child 
	 * associations details.
	 * 
	 * @see CheckOutCheckInService#checkout(NodeRef, NodeRef, QName, QName)
	 * 
	 * @param nodeRef	a reference to the node to checkout
	 * @return			a node reference to the created working copy
	 */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
	public NodeRef checkout(NodeRef nodeRef);
	
	/**
	 * Checks in the working node specified.
	 * <p>
	 * When a working copy is checked in the current state of the working copy is copied to the 
	 * original node.  This will include any content updated in the working node.
	 * <p>
	 * If version properties are provided the original node will be versioned and updated accordingly.
	 * <p>
	 * If a content Url is provided it will be used to update the content of the working node before the
	 * checkin operation takes place.
	 * <p>
	 * Once the operation has completed the read lock applied to the original node during checkout will
	 * be removed and the working copy of the node deleted from the repository, unless the operation is 
	 * instructed to keep the original node checked out.  In which case the lock and the working copy will
	 * remain.
	 * <p>
	 * The node reference to the original node is returned.
	 * 
	 * @param workingCopyNodeRef	the working copy node reference
	 * @param versionProperties		the version properties.  If null is passed then the original node
	 * 								is NOT versioned during the checkin operation.
	 * @param contentUrl			a content url that should be set on the working copy before 
	 * 								the checkin operation takes place.  If null then the current working
	 * 								copy content is copied back to the original node.
	 * @param keepCheckedOut		indicates whether the node should remain checked out after the checkin
	 * 								has taken place.  When the node remains checked out the working node 
	 * 								reference remains the same.
	 * @return						the node reference to the original node, updated with the checked in 
	 * 								state
	 */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"workingCopyNodeRef", "versionProperties", "contentUrl", "keepCheckedOut"})
	public NodeRef checkin(
			NodeRef workingCopyNodeRef,
			Map<String,Serializable> versionProperties,
			String contentUrl,
			boolean keepCheckedOut);
	
	/**
	 * By default the checked in node is not keep checked in.
	 * 
	 * @see VersionOperationsService#checkin(NodeRef, HashMap<String,Serializable>, String, boolean)
	 * 
	 * @param workingCopyNodeRef	the working copy node reference
	 * @param versionProperties		the version properties.  If null is passed then the original node
	 * 								is NOT versioned during the checkin operation.
	 * @param contentUrl			a content url that should be set on the working copy before 
	 * 								the checkin operation takes place.  If null then the current working
	 * 								copy content is copied back to the original node.
	 * @return						the node reference to the original node, updated with the checked in 
	 * 								state
	 */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"workingCopyNodeRef", "versionProperties", "contentUrl"})
	public NodeRef checkin(
			NodeRef workingCopyNodeRef,
			Map<String, Serializable> versionProperties,
			String contentUrl);
	
	/**
	 * If no content url is specified then current content set on the working
	 * copy is understood to be current.
	 * 
	 * @see VersionOperationsService#checkin(NodeRef, HashMap<String,Serializable>, String)
	 *  
	 * @param workingCopyNodeRef	the working copy node reference
	 * @param versionProperties		the version properties.  If null is passed then the original node
	 * 								is NOT versioned during the checkin operation.
	 * @return						the node reference to the original node, updated with the checked in 
	 * 								state
	 */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"workingCopyNodeRef", "versionProperties"})
	public NodeRef checkin(
			NodeRef workingCopyNodeRef,
			Map<String, Serializable> versionProperties);
	
	/**
	 * Cancels the checkout for a given working copy.
	 * <p>
	 * The read-only lock on the original node is removed and the working copy is removed.
	 * <p>
	 * Note that all modification made to the working copy will be lost and the original node
	 * will remain unchanged.
	 * <p>
	 * A reference to the original node reference is returned.
	 * 
	 * @param workingCopyNodeRef	the working copy node reference
	 * @return						the original node reference
	 */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"workingCopyNodeRef"})
	public NodeRef cancelCheckout(NodeRef workingCopyNodeRef);
    
    /**
     * Helper method to retrieve the working copy node reference for a checked out node.
     * <p>
     * A null node reference is returned if the node is not checked out.
     * 
     * @param   nodeRef   a node reference
     * @return            the working copy node reference or null if none.
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = {"nodeRef"})
    public NodeRef getWorkingCopy(NodeRef nodeRef);
}
