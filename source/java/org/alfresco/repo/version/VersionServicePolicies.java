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
