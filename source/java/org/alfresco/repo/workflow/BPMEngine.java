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
