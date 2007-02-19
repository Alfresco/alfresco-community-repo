/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.importer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.namespace.QName;


/**
 * Description of node to import.
 * 
 * @author David Caruana
 *
 */
public interface ImportNode
{
    /**
     * @return  the parent context
     */
    public ImportParent getParentContext();

    /**
     * @return  the type definition
     */
    public TypeDefinition getTypeDefinition();
    
    /**
     * @return  is this a node reference
     */
    public boolean isReference();
    
    /**
     * @return  the node ref
     */
    public NodeRef getNodeRef();
    
    /**
     * @return  node uuid to create node with
     */
    public String getUUID();

    /**
     * @return  the child name
     */
    public String getChildName();
    
    /**
     * Gets all properties for the node
     * 
     * @return the properties
     */
    public Map<QName,Serializable> getProperties();

    /**
     * Gets the property data type
     * 
     * @param propertyName  name of property
     * @return  data type of named property
     */
    public DataTypeDefinition getPropertyDataType(QName propertyName);
    
    /**
     * @return  the aspects of this node
     */
    public Set<QName> getNodeAspects();
    
    /**
     * @return  true => the node inherits permissions from its parent
     */
    public boolean getInheritPermissions();
    
    /**
     * @return  the permissions applied to this node
     */
    public List<AccessPermission> getAccessControlEntries();
    
}
