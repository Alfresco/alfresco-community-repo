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
	 * 
	 * @param nodeRef
	 * @return
	 */
    boolean hasExtendedSecurity(NodeRef nodeRef);
    
    /**
     * Gets the set authorities that are extended readers for the given node.
     * 
     * @param nodeRef   node reference
     * @return {@link Set}<{@link String}>  extended readers
     */
    Set<String> getExtendedReaders(NodeRef nodeRef);
    
    /**
     * 
     * @param nodeRef
     * @return
     */
    Set<String> getExtendedWriters(NodeRef nodeRef);

    void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers);
    
    void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents);
    
    void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers);
    
    void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents);
    
    void removeAllExtendedSecurity(NodeRef nodeRef);
    
    void removeAllExtendedSecurity(NodeRef nodeRef, boolean applyToParents);

}
