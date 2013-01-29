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
package org.alfresco.module.org_alfresco_module_rm.fileplan;

import org.alfresco.service.cmr.repository.NodeRef;

import com.hazelcast.impl.Node;

/**
 * File plan service interface.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface FilePlanService
{
    /**
     * Indicates whether the given node is file plan node or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if node is a file plan node
     */
    boolean isFilePlan(NodeRef nodeRef);
    
    /**
     * Indicates whether the unfiled container exists for a given file plan or not.
     * 
     * @param filePlan  file plan
     * @return boolean  true if unfiled container exists, false otherwise
     */
    boolean existsUnfiledContainer(NodeRef filePlan);
    
    /**
     * Gets the unfiled container for a given file plan.  Returns null if
     * none.
     * 
     * @param filePlan          file plan
     * @return {@link NodeRef}  unfiled container, null if none
     */
    NodeRef getUnfiledContainer(NodeRef filePlan);
    
    /**
     * Creates, and returns, a unfiled container for a given file plan.
     * 
     * @param filePlan      file plan
     * @return {@link Node} unfiled container
     */
    NodeRef createUnfiledContainer(NodeRef filePlan);

}
