/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions.impl;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;

/**
 * Extended Permission Service Interface used in RM.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ExtendedPermissionService extends PermissionService
{
    Set<String> getWriters(Long aclId);
    
    /**
     * Get the readers and writers for a given node.
     * <p>
     * The writers list includes the owner for the node.
     * 
     * @param nodeRef                           node reference
     * @return Pair<Set<String>, Set<String>>   first is a set containing all the authorities that have read permission on the 
     *                                          document and second is a set containing all the authorities that have write
     *                                          permission on the document, including the owner.
     *                                          
     * @since 2.5
     */
    Pair<Set<String>, Set<String>> getReadersAndWriters(NodeRef nodeRef);
}
