/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * CMIS Services
 * 
 * @author davidc
 */
public interface CMISServices
{
    /**
     * Gets the supported CMIS Version
     * 
     * @return  version of CMIS specification supported
     */
    public String getCMISVersion();

    /**
     * Gets the supported CMIS Specification Title
     * 
     * @return  CMIS pecification Title
     */
    public String getCMISSpecTitle();
    
    /**
     * Gets the default root node path
     * 
     * @return  root node path
     */
    public String getDefaultRootPath();
    
    /**
     * Gets the default root node ref
     *  
     * @return  root node ref
     */
    public NodeRef getDefaultRootNodeRef();

    /**
     * Gets the default store ref
     * 
     * @return  store ref
     */
    public StoreRef getDefaultRootStoreRef();
    
    /**
     * Finds a NodeRef given a repository reference
     * 
     * @param referenceType  node, path
     * @param reference  node => id, path => path
     * @return  nodeRef (or null, if not found)
     */
    public NodeRef getNode(String referenceType, String[] reference);
    
    /**
     * Gets a map of node attributes relating to renditions.
     * 
     * @param nodeRef
     *            the node ref
     * @param renditionFilter
     *            the rendition filter
     * @return the attribute map
     */
    public Map<String, Object> getRenditions(NodeRef nodeRef, String renditionFilter);
    
    /**
     * Query for node children
     * 
     * @param parent  node to query children for
     * @param typesFilter  types filter
     * @return  children of node
     */
    public NodeRef[] getChildren(NodeRef parent, CMISTypesFilterEnum typesFilter);

    /**
     * Query for checked out items
     *  
     * @param username  for user
     * @param folder  (optional) within folder
     * @param includeDescendants  true => include descendants of folder, false => only children of folder
     * @return  checked out items
     */
    public NodeRef[] getCheckedOut(String username, NodeRef folder, boolean includeDescendants);

    /**
     * Query for relationship
     * 
     * @param relDef
     * @param source
     * @param target
     * @return  relationship
     */
    public AssociationRef getRelationship(CMISTypeDefinition relDef, NodeRef source, NodeRef target);
    
    /**
     * Query for relationships
     * 
     * @param item  node to query relationships for
     * @param relDef  type of relationship to query (or null, for all relationships)
     * @param includeSubTypes  
     * @param direction  limit direction of relationships to query (or null, for both directions)
     * @return  relationships
     */
    public AssociationRef[] getRelationships(NodeRef node, CMISTypeDefinition relDef, boolean includeSubTypes, CMISRelationshipDirectionEnum direction);
    
    /**
     * Get a single property for a node
     * 
     * @param nodeRef
     * @param propertyName
     * @return value
     */
    public Serializable getProperty(NodeRef nodeRef, String propertyName);

    /**
     * Get a single property for an association
     * 
     * @param assocRef
     * @param propertyName
     * @return value
     */
    public Serializable getProperty(AssociationRef assocRef, String propertyName);
    
    /**
     * Get all properties
     * 
     * @param nodeRef
     * @return
     */
    public Map<String, Serializable> getProperties(NodeRef nodeRef);
    
    /**
     * Set a single property
     * 
     * @param nodeRef
     * @param propertyName
     * @param value
     */
    public void setProperty(NodeRef nodeRef, String propertyName, Serializable value);
}
