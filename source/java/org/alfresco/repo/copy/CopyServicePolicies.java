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
package org.alfresco.repo.copy;

import java.util.Map;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public interface CopyServicePolicies 
{
	/**
	 * Policy invoked when a <b>node</b> is copied
	 */
	public interface OnCopyNodePolicy extends ClassPolicy
	{
        /**
         * @param classRef              the type of node being copied
         * @param sourceNodeRef         node being copied
         * @param destinationStoreRef   the destination store reference
         * @param copyToNewNode         indicates whether we are copying to a new node or not 
         * @param copyDetails           modifiable <b>node</b> details
         */
		public void onCopyNode(
				QName classRef,
				NodeRef sourceNodeRef,
                StoreRef destinationStoreRef,
                boolean copyToNewNode,
				PolicyScope copyDetails);
	}
	
	/**
	 * Policy invoked when the copy operation invoked on a <b>node</b> is complete.
	 * <p>
	 * The copy map contains all the nodes created during the copy, this helps to re-map
	 * any potentially relative associations.
	 */
	public interface OnCopyCompletePolicy extends ClassPolicy
	{
		/**
		 * @param classRef			the type of the node that was copied
		 * @param sourceNodeRef		the origional node
		 * @param destinationRef	the destination node
		 * @param copyMap			a map containing all the nodes that have been created during the copy
		 */
		public void onCopyComplete(
				QName classRef,
				NodeRef sourceNodeRef,
				NodeRef destinationRef,
                boolean copyToNewNode,
				Map<NodeRef, NodeRef> copyMap);
	}
}
