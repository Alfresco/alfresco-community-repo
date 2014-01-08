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
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Records management action service interface
 * 
 * @author Roy Wetherall
 */
public interface RecordsManagementActionService
{
    /**
     * Get a list of the available records management actions
     * 
     * @return  List<RecordsManagementAction>    records management actions
     */
    List<RecordsManagementAction> getRecordsManagementActions();
    
    /**
     * 
     * @return
     * @since 2.1
     */
    List<RecordsManagementActionCondition> getRecordsManagementActionConditions();
    
    /**
     * Get a list of the available disposition actions.  A disposition action is a records
     * management action that can be used when defining disposition instructions.
     * 
     * @return List<RecordsManagementAction>     disposition actions
     */
    List<RecordsManagementAction> getDispositionActions();
    
    /**
     * Gets the named records management action
     * 
     * @param name The name of the RM action to retrieve
     * @return The RecordsManagementAction or null if it doesn't exist
     */
    RecordsManagementAction getRecordsManagementAction(String name);
    
    /**
     * Gets the named disposition action
     * 
     * @param name The name of the disposition action to retrieve
     * @return The RecordsManagementAction or null if it doesn't exist
     */
    RecordsManagementAction getDispositionAction(String name);

    /**
     * Execute a records management action
     * 
     * @param nodeRef     node reference to a rm container, rm folder or record
     * @param name        action name
     */
    RecordsManagementActionResult executeRecordsManagementAction(NodeRef nodeRef, String name);
    
    /**
     * Execute a records management action against several nodes
     * 
     * @param nodeRefs  node references to rm containers, rm folders or records
     * @param name      action name
     */
    Map<NodeRef, RecordsManagementActionResult> executeRecordsManagementAction(List<NodeRef> nodeRefs, String name);
    
    /**
     * Execute a records management action
     * 
     * @param nodeRef     node reference to a rm container, rm folder or record
     * @param name        action name
     * @param parameters  action parameters
     */
    RecordsManagementActionResult executeRecordsManagementAction(NodeRef nodeRef, String name, Map<String, Serializable> parameters);

    /**
     * Execute a records management action against several nodes
     * 
     * @param nodeRefs      node references to rm containers, rm folders or records
     * @param name          action name
     * @param parameters    action parameters
     */
    Map<NodeRef, RecordsManagementActionResult> executeRecordsManagementAction(List<NodeRef> nodeRefs, String name, Map<String, Serializable> parameters);
    
    /**
     * Execute a records management action. The nodeRef against which the action is to be
     * executed must be provided by the RecordsManagementAction implementation.
     * 
     * @param name        action name
     * @param parameters  action parameters
     */
    RecordsManagementActionResult executeRecordsManagementAction(String name, Map<String, Serializable> parameters);

    /**
     * Register records management action
     * 
     * @param rmAction  records management action
     */
    void register(RecordsManagementAction rmAction);
    
    /**
     * 
     * @param rmCondition
     * @since 2.1
     */
    void register(RecordsManagementActionCondition rmCondition);
}
