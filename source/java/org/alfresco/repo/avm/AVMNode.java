/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.repo.avm;

import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.namespace.QName;

/**
 * The Interface for versionable objects.
 * @author britt
 */
public interface AVMNode
{
    /**
     * Set the ancestor of this node.
     * @param ancestor The ancestor to set.
     */
    public void setAncestor(AVMNode ancestor);

    /**
     * Change the ancestor of a node.
     * @param ancestor The ancestor node that should be set.
     */
    public void changeAncestor(AVMNode ancestor);

    /**
     * Get the ancestor of this node.
     * @return The ancestor of this node.
     */
    public AVMNode getAncestor();

    /**
     * Set the merged from node.
     * @param mergedFrom The merged from node.
     */
    public void setMergedFrom(AVMNode mergedFrom);

    /**
     * Get the node this was merged from.
     * @return The node this was merged from.
     */
    public AVMNode getMergedFrom();

    /**
     * Get the version number.
     * @return The version number.
     */
    public int getVersionID();

    /**
     * Set the version number.
     * @param version The version number to set.
     */
    public void setVersionID(int version);

    /**
     * Possibly copy ourselves.
     * @param lPath The Lookup for this node.
     * @return A copy of ourself or null if no copy was necessary.
     */
    public AVMNode copy(Lookup lPath);

    /**
     * Get the type of this node.
     */
    public int getType();

    /**
     * Get the descriptor for this node.
     * @param lPath The Lookup.
     * @param name The name of this in the current context.
     * @return The descriptor for this node.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath, String name);

    /**
     * Get the descriptor for this node.
     * @param lPath The Lookup.
     * @return The descriptor for this node.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath);

    /**
     * Get a node descriptor for this node.
     * @param parentPath The parent path.
     * @param name The name looked up as.
     * @param parentIndirection The indirection of the parent.
     * @param parentIndirectionVersion The indirection version of the parent.
     * @return The descriptor for this node.
     */
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection, int parentIndirectionVersion);

    /**
     * Get the object id.
     * @return The object id.
     */
    public long getId();

    /**
     * Get the newnews.
     * @return Whether the node is new.
     */
    public boolean getIsNew();

    /**
     * Get a string representation for debugging.
     * @param lPath The Lookup.
     * @return A String representation.
     */
    public String toString(Lookup lPath);

    /**
     * Set whether this node to be a root of a AVMStore
     * @param isRoot
     */
    public void setIsRoot(boolean isRoot);

    /**
     * Get whether this node is a root of a AVMStore.
     * @return Whether this node is a root.
     */
    public boolean getIsRoot();

    /**
     * Update the modification time of this node.
     */
    public void updateModTime();

    /**
     * Set a property.
     * @param qname      the QName
     * @param value      The value to set.
     */
    public void setProperty(QName qname, PropertyValue value);

    /**
     * Set a collection of properties on this node.
     * @param properties The Map of QNames to PropertyValues.
     */
    public void setProperties(Map<QName, PropertyValue> properties);

    /**
     * Add properties to those that already exist.
     * @param properties The properties to add.
     */
    public void addProperties(Map<QName, PropertyValue> properties);

    /**
     * Get a property by name.
     * @param name The name of the property to get.
     * @return A PropertyValue
     */
    public PropertyValue getProperty(QName name);

    /**
     * Get all the properties associated with this node.
     * @return A Map of QNames to PropertyValues.
     */
    public Map<QName, PropertyValue> getProperties();

    /**
     * Delete a property from this node.
     * @param qnameEntityId         the ID of the QName to delete
     */
    public void deleteProperty(QName qname);

    /**
     * Delete all properties from this node.
     */
    public void deleteProperties();

    /**
     * Set an ACL on this node.
     * @param acl The ACL to set.
     */
    public void setAcl(Acl acl);

    /**
     * Get the ACL on this node.
     * @return The ACL on this node.
     */
    public Acl getAcl();

    /**
     * Set the store that we are new in.
     * @param store The store we are new in.
     */
    public void setStoreNew(AVMStore store);

    /**
     * Get the possibly null store that we're new in.
     * @return The store that we're new in.
     */
    public AVMStore getStoreNew();
    
    /**
     * Copy ACL from another node.
     * 
     * @param other
     * @param mode
     */
    public void copyACLs(AVMNode other, ACLCopyMode mode);
    
    public void copyACLs(Acl otherAcl, Acl parentAcl, ACLCopyMode mode);
    
    /**
     * Copy metadata from another node.
     * @param other The other node.
     */
    public void copyMetaDataFrom(AVMNode other, Long parentAcl);

    /**
     * Get the GUID associated with this version.
     * @return The GUID.
     */
    public String getGuid();

    /**
     * Set the GUID associated with this version.
     * @param guid
     */
    public void setGuid(String guid);

    /**
     * Get the Aspects that this node has.
     * @return A Set of Aspects IDs.
     */
    public Set<QName> getAspects();
    
    public void addAspect(QName aspectQName);
    
    public void removeAspect(QName aspectQName);

    /**
     * Get the Basic Attributes on this node.
     * @return
     */
    public BasicAttributes getBasicAttributes();
}
