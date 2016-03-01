 
package org.alfresco.module.org_alfresco_module_rm.job.publish;

/**
 * Base publish executor implementation
 * 
 * @author Roy Wetherall
 */
public abstract class BasePublishExecutor implements PublishExecutor
{
    /** Publish executor registry */
    public PublishExecutorRegistry registry;
    
    /**
     * Set publish executor registry
     * @param registry  publish executor registry
     */
    public void setPublishExecutorRegistry(PublishExecutorRegistry registry)
    {
        this.registry = registry;
    }
    
    /**
     * Init method
     */
    public void init()
    {
        registry.register(this);
    }    
}
