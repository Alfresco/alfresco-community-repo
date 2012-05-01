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
package org.alfresco.module.org_alfresco_module_rm.capability;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Capability Interface.
 * 
 * @author andyh
 */
public interface Capability
{
    /**
     * Does this capability apply to this nodeRef?
     * @param nodeRef
     * @return
     */
    AccessStatus hasPermission(NodeRef nodeRef);
    
    /**
     * 
     * @param nodeRef
     * @return
     */
    int hasPermissionRaw(NodeRef nodeRef);

    /**
     * Evaluates the capability.
     * 
     * @param nodeRef
     * @return
     */
    int evaluate(NodeRef nodeRef);
    
    /**
     * 
     * @param source
     * @param target
     * @return
     */
    int evaluate(NodeRef source, NodeRef target);
    
    /**
     * Indicates whether this is a private capability or not.  Private capabilities are used internally, otherwise
     * they are made available to the user to assign to roles.
     * 
     * @return
     */
    boolean isPrivate();
    
    /**
     * Get the name of the capability
     * @return
     */
    String getName();

    /**
     * Get the name of optional actions tied to this capability
     * @return
     */
    List<String> getActionNames();

    /**
     * 
     * @return
     */
    List<RecordsManagementAction> getActions();       
}
