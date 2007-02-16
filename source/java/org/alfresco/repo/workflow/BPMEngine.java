/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow;

import org.alfresco.service.cmr.workflow.WorkflowException;
import org.springframework.beans.factory.InitializingBean;


/**
 * Base functionality for a plug-in BPM Engine
 * 
 * @author davidc
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

    /*
     *  (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
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
    protected String createGlobalId(String localId)
    {
        return BPMEngineRegistry.createGlobalId(engineId, localId);
    }
    
    /**
     * Construct a local Id from a global Id
     * 
     * @param globalId  the global id
     * @return  the local id
     */
    protected String createLocalId(String globalId)
    {
        return BPMEngineRegistry.getLocalId(globalId);
    }
}
