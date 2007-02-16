/*
 * Copyright (C) 2005 Alfresco, Inc.
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
