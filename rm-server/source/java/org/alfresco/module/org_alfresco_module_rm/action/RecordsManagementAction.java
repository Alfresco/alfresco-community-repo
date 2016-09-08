/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Record Management Action
 * 
 * @author Roy Wetherall
 */
public interface RecordsManagementAction
{
    /**
     * Get the name of the action
     * 
     * @return  String  action name
     */
    public String getName();
    
    /**
     * Get the label of the action
     * 
     * @return  String  action label
     */
    public String getLabel();
    
    /**
     * Get the description of the action
     * 
     * @return  String  action description 
     */
    public String getDescription();
    
    /**
     * Indicates whether this is a disposition action or not
     * 
     * @return  boolean     true if a disposition action, false otherwise
     */
    boolean isDispositionAction();
    
    /**
     * Execution of the action
     * 
     * @param filePlanComponent     file plan component the action is executed upon
     * @param parameters            action parameters
     */
    public RecordsManagementActionResult execute(NodeRef filePlanComponent, Map<String, Serializable> parameters);
    
    
    /**
     * Can this action be executed?
     * Does it meet all of its entry requirements - EXCEPT permission checks.
     * 
     * @param filePlanComponent     file plan component the action is executed upon
     * @param parameters            action parameters
     * @return
     */
    public boolean isExecutable(NodeRef filePlanComponent, Map<String, Serializable> parameters);
    
    
    /**
     * Get a set of properties that should only be updated via this or other action.
     * These properties will be rejected by updates via the generic public services, such as the NodeService.
     * 
     * @return the set of protected properties
     */
    public Set<QName> getProtectedProperties();
    
    /**
     * Get a set of aspects that should be updated via this or other actions.
     * The aspect can not be added via public services, such as the NodeService.
     * @return
     */
    public Set<QName> getProtectedAspects();

    /**
     * Some admin-related rmActions execute against a target nodeRef which is not provided
     * by the calling code, but is instead an implementation detail of the action.
     * 
     * @return the target nodeRef
     */
    public NodeRef getImplicitTargetNodeRef();
}
