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
package org.alfresco.service.cmr.view;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.namespace.QName;


/**
 * Callback interface for monitoring progress of an import.
 * 
 * @author David Caruana
 *
 */
public interface ImporterProgress
{
    /**
     * Report creation of a node.
     * 
     * @param nodeRef  the node ref
     * @param parentRef  the parent ref
     * @param assocName  the child association type name
     * @param childName  the child association name
     */
    public void nodeCreated(NodeRef nodeRef, NodeRef parentRef, QName assocName, QName childName);

    /**
     * Report creation of content
     * 
     * @param nodeRef  the node ref
     * @param sourceUrl  the source location of the content
     */
    public void contentCreated(NodeRef nodeRef, String sourceUrl);
    
    /**
     * Report setting of a property
     * 
     * @param nodeRef  the node ref
     * @param property  the property name
     * @param value  the property value
     */
    public void propertySet(NodeRef nodeRef, QName property, Serializable value);
    
    /**
     * Report setting of a permission
     *
     * @param nodeRef  the node ref
     * @param permission  the permission
     */
    public void permissionSet(NodeRef nodeRef, AccessPermission permission);
    
    /**
     * Report addition of an aspect
     * 
     * @param nodeRef  the node ref
     * @param aspect  the aspect
     */
    public void aspectAdded(NodeRef nodeRef, QName aspect);
}
