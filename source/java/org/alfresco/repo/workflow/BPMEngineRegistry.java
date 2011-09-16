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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * BPM Engine Registry
 * 
 * Responsible for managing the list of registered BPM Engines for the
 * following components:
 * 
 * - Workflow Component
 * - Task Component
 * 
 * @author davidc
 */
public class BPMEngineRegistry
{
    /** ID seperator used in global Ids */
    private static final String ID_SEPERATOR = "$"; 
    private static final String ID_SEPERATOR_REGEX = "\\$"; 
    
    /** Logging support */
    private static Log logger = LogFactory.getLog("org.alfresco.repo.workflow");

    private WorkflowAdminService workflowAdminService;
    
    private Map<String, WorkflowComponent> workflowComponents;
    private Map<String, TaskComponent> taskComponents;

    /**
     * Construct
     */
    public BPMEngineRegistry()
    {
        workflowComponents = new HashMap<String, WorkflowComponent>();
        taskComponents = new HashMap<String, TaskComponent>();
    }
    
    /**
     * Sets the workflow admin service
     * 
     * @param workflowAdminService the workflow admin service
     */
    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService)
    {
        this.workflowAdminService = workflowAdminService;
    }
    
    /**
     * Register a BPM Engine Workflow Component
     * 
     * @param engineId  engine id
     * @param engine  implementing engine
     */
    public void registerWorkflowComponent(String engineId, WorkflowComponent engine)
    {
        if (workflowComponents.containsKey(engineId))
        {
            throw new WorkflowException("Workflow Component already registered for engine id '" + engineId + "'");
        }
        workflowComponents.put(engineId, engine);
        if (logger.isDebugEnabled())
            logger.debug("Registered Workflow Component '" + engineId + "' (" + engine.getClass() + ")");
    }

    /**
     * Gets all registered Workflow Components
     * 
     * @return  array of engine ids
     */
    public String[] getWorkflowComponents()
    {
        return getComponents(workflowComponents.keySet());
    }

    /**
     * Gets a specific BPM Engine Workflow Component
     * 
     * @param engineId  engine id
     * @return  the Workflow Component
     */
    public WorkflowComponent getWorkflowComponent(String engineId)
    {
        if(false == workflowAdminService.isEngineEnabled(engineId))
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Ignoring disabled WorkflowComponent: "+engineId);
            }
            return null;
        }
        return workflowComponents.get(engineId);
    }
    
    /**
     * Register a BPM Engine Task Component
     * 
     * @param engineId  engine id
     * @param engine  implementing engine
     */
    public void registerTaskComponent(String engineId, TaskComponent engine)
    {
        if (taskComponents.containsKey(engineId))
        {
            throw new WorkflowException("Task Component already registered for engine id '" + engineId + "'");
        }
        taskComponents.put(engineId, engine);
        if (logger.isDebugEnabled())
            logger.debug("Registered Task Component '" + engineId + "' (" + engine.getClass() + ")");
    }

    /**
     * Gets all registered Task Components
     * 
     * @return  array of engine ids
     */
    public String[] getTaskComponents()
    {
        return getComponents(taskComponents.keySet());
    }

    private String[] getComponents(Set<String> components)
    {
        List<String> filtered = CollectionUtils.filter(components, new Filter<String>()
        {
            public Boolean apply(String engineId)
            {
                return workflowAdminService.isEngineEnabled(engineId);
            }
        });
        return filtered.toArray(new String[filtered.size()]);
    }

    /**
     * Gets a specific BPM Engine Task Component
     * 
     * @param engineId  engine id
     * @return  the Workflow Component
     */
    public TaskComponent getTaskComponent(String engineId)
    {
        if(false == workflowAdminService.isEngineEnabled(engineId))
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Ignoring disabled TaskComponent: "+engineId);
            }
            return null;
        }
        return taskComponents.get(engineId);
    }

    
    //
    // BPM Engine Id support
    //
    
    /**
     * Construct a global Id
     * 
     * @param engineId  engine id
     * @param localId  engine local id
     * @return  the global id
     */
    public static String createGlobalId(String engineId, String localId)
    {
        return engineId + ID_SEPERATOR + localId;
    }
    
    /**
     * Break apart a global id into its engine and local ids
     * 
     * @param globalId  the global id
     * @return  array containing engine id and global id in that order
     */
    public static String[] getGlobalIdParts(String globalId)
    {
        String[] parts = globalId.split(ID_SEPERATOR_REGEX);
        if (parts.length != 2)
        {
            throw new WorkflowException("Invalid Global Id '" + globalId + "'");
        }
        return parts;
    }
    
    /**
     * Get the engine id from a global id
     * 
     * @param globalId  the global id
     * @return  the engine id
     */
    public static String getEngineId(String globalId)
    {
        return getGlobalIdParts(globalId)[0];
    }
    
    /**
     * Get the local id from a global id
     * 
     * @param globalId  the global id
     * @return  the local id
     */
    public static String getLocalId(String globalId)
    {
        return getGlobalIdParts(globalId)[1];
    }

    /**
     * Returns <code>true</code> if the globalId parameter is a valid global Id
     * for the given engineId.
     * 
     * @param globalId
     * @param engineId
     * @return
     */
    public static boolean isGlobalId(String globalId, String engineId)
    {
        return globalId.startsWith(engineId+ID_SEPERATOR);
    }

}
