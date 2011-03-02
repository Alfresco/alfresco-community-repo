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

import org.alfresco.service.cmr.workflow.WorkflowException;
import org.springframework.beans.factory.InitializingBean;


/**
 * Base functionality for a plug-in BPM Engine
 * 
 * @author davidc
 * @author Nick Smith
 */
public class BPMEngine implements InitializingBean
{
    private BPMEngineRegistry registry;
    private String engineId;
 
    /**
     * Sets the BPM Engine Registry
     * 
     * @param registry   the registry
     */
    public void setBPMEngineRegistry(BPMEngineRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Sets the BPM Engine Id
     * 
     * @param engineId   the id
     */
    public void setEngineId(String engineId)
    {
        this.engineId = engineId;
    }

    /**
     * @return the engineId
     */
    protected String getEngineId()
    {
        return engineId;
    }
    
    /**
    * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception
    {
        if (engineId == null || engineId.length() == 0)
        {
            throw new WorkflowException("Engine Id not specified");
        }
        if (this instanceof WorkflowComponent)
        {
            registry.registerWorkflowComponent(engineId, (WorkflowComponent)this);
        }
        if (this instanceof TaskComponent)
        {
            registry.registerTaskComponent(engineId, (TaskComponent)this);
        }
    }

    /**
     * Construct a global Id for use outside of the engine
     *  
     * @param localId  the local engine id
     * @return  the global id
     */
    public String createGlobalId(String localId)
    {
        return BPMEngineRegistry.createGlobalId(engineId, localId);
    }
    
    /**
     * Construct a local Id from a global Id
     * 
     * @param globalId  the global id
     * @return  the local id
     */
    public String createLocalId(String globalId)
    {
        return BPMEngineRegistry.getLocalId(globalId);
    }
    
}
