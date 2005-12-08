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
package org.alfresco.repo.content;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Content service policies interface
 * 
 * @author Roy Wetherall
 */
public interface ContentServicePolicies
{
	/**
	 * On content update policy interface
	 */
	public interface OnContentUpdatePolicy extends ClassPolicy
	{
		/**
		 * @param nodeRef	the node reference
		 */
		public void onContentUpdate(NodeRef nodeRef);
	}
}
