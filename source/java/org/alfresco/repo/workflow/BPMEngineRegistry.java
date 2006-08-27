/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.workflow;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowException;
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
        
        if (logger.isInfoEnabled())
            logger.info("Registered Workflow Component '" + engineId + "' (" + engine.getClass() + ")");
    }

    /**
     * Gets all registered Workflow Components
     * 
     * @return  array of engine ids
     */
    public String[] getWorkflowComponents()
    {
        return workflowComponents.keySet().toArray(new String[workflowComponents.keySet().size()]);
    }

    /**
     * Gets a specific BPM Engine Workflow Component
     * 
     * @param engineId  engine id
     * @return  the Workflow Component
     */
    public WorkflowComponent getWorkflowComponent(String engineId)
    {
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
        
        if (logger.isInfoEnabled())
            logger.info("Registered Task Component '" + engineId + "' (" + engine.getClass() + ")");
    }

    /**
     * Gets all registered Task Components
     * 
     * @return  array of engine ids
     */
    public String[] getTaskComponents()
    {
        return taskComponents.keySet().toArray(new String[taskComponents.keySet().size()]);
    }

    /**
     * Gets a specific BPM Engine Task Component
     * 
     * @param engineId  engine id
     * @return  the Workflow Component
     */
    public TaskComponent getTaskComponent(String engineId)
    {
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
    
}
