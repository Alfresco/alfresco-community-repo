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
