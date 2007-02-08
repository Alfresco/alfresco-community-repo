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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
    public void started();
    
    public void completed();
    
    public void error(Throwable e);
    
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
     * Report creation of a node link.
     * 
     * @param nodeRef  the node ref
     * @param parentRef  the parent ref
     * @param assocName  the child association type name
     * @param childName  the child association name
     */
    public void nodeLinked(NodeRef nodeRef, NodeRef parentRef, QName assocName, QName childName);
    
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
