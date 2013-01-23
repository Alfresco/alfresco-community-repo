/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extended security service.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ExtendedSecurityService
{
    /**
     * Indicates whether the node has any extended readers set or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if the node has extended readers set, false otherwise
     */
    boolean hasExtendedReaders(NodeRef nodeRef);
    
    /**
     * Gets the set authorities that are extended readers for the given node.
     * 
     * @param nodeRef   node reference
     * @return {@link Set}<{@link String}>  extended readers
     */
    Set<String> getExtendedReaders(NodeRef nodeRef);
    
    /**
     * Set the authorities that are extended readers on the node.  Applies extended readers to
     * file plan parent hierarchy.
     * 
     * @param nodeRef   node reference
     * @param readers   extended readers
     */
    void setExtendedReaders(NodeRef nodeRef, Set<String> readers);
    
    /**
     * Set the authorities that have extended reading permissions on the node.
     * <p>
     * Optionally applies the extended readers to the file plan hierarchy.
     * 
     * @param nodeRef           node reference
     * @param readers           extended readers 
     * @param applyToParents    true if applied to file plan hierarchy, false otherwise
     */
    void setExtendedReaders(NodeRef nodeRef, Set<String> readers, boolean applyToParents);
    
    /**
     * Removes the given authorities from the extended readers set for this node.
     * <p>
     * Applies to file plan hierarchy.
     * 
     * @param nodeRef   node reference
     * @param readers   extended readers
     */
    void removeExtendedReaders(NodeRef nodeRef, Set<String> readers);
    
    /**
     * Removes the given authorities from the extended readers set for this node.
     * <p>
     * Optionally applies the removal to the file plan hierarchy.
     * 
     * @param nodeRef           node reference
     * @param readers           extended readers
     * @param applyToParents    true if applied to the file plan hierarchy, false otherwise
     */
    void removeExtendedReaders(NodeRef nodeRef, Set<String> readers, boolean applyToParents);
    
    /**
     * Removes all extended readers from this node.
     * <p>
     * Applies removal to the file plan hierarchy.
     * 
     * @param nodeRef   node reference
     */
    void removeAllExtendedReaders(NodeRef nodeRef);
    
    /**
     * Removes all extended readers from this node.
     * <p>
     * Optionally applies the removal to the file plan hierarchy.
     * 
     * @param nodeRef           node reference
     * @param applyToParents    true if applied to the file plan hierarchy, false otherwise
     */
    void removeAllExtendedReaders(NodeRef nodeRef, boolean applyToParents);

}
