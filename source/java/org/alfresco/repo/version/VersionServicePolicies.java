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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;

/**
 * Version service policy interfaces
 * 
 * @author Roy Wetherall
 */
public interface VersionServicePolicies
{
	/**
	 * Before create version policy interface.
	 */
	public interface BeforeCreateVersionPolicy extends ClassPolicy
	{
		/**
		 * Called before a new version is created for a version
		 * 
		 * @param versionableNode  reference to the node about to be versioned
		 */
	    public void beforeCreateVersion(NodeRef versionableNode);
	
	}
	
	/**
	 * After create version policy interface
	 *
	 */
	public interface AfterCreateVersionPolicy extends ClassPolicy
	{
		/**
		 * Called after the version has been created 
		 * 
		 * @param versionableNode	the node that has been versioned
		 * @param version			the created version
		 */
		public void afterCreateVersion(NodeRef versionableNode, Version version);
	}
	
	/**
	 * On create version policy interface
	 */
	public interface OnCreateVersionPolicy extends ClassPolicy
	{
		/**
		 * Called during the creation of the version to determine what the versioning policy for a 
		 * perticular type may be.
		 * WARNING: implementing behaviour for this policy effects the versioning behaviour of the 
		 * type the behaviour is registered against.
		 * 
		 * @param classRef
		 * @param versionableNode
		 * @param versionProperties
		 * @param nodeDetails
		 */
		public void onCreateVersion(
				QName classRef,
				NodeRef versionableNode, 
				Map<String, Serializable> versionProperties,
				PolicyScope nodeDetails);
	}
	
	/**
	 * Calculate version lable policy interface
	 */
	public interface CalculateVersionLabelPolicy extends ClassPolicy
	{
		public String calculateVersionLabel(
				QName classRef,
				Version preceedingVersion,
				int versionNumber,
				Map<String, Serializable>verisonProperties);
	}
}
