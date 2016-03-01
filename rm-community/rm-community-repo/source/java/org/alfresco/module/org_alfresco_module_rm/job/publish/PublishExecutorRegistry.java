 
package org.alfresco.module.org_alfresco_module_rm.job.publish;

import java.util.HashMap;
import java.util.Map;

/**
 * Publish executor register.
 * 
 * @author Roy Wetherall
 */
public class PublishExecutorRegistry
{
    /** Map of publish executors */
    private Map<String, PublishExecutor> publishExectors = new HashMap<String, PublishExecutor>(3);
    
    /**
     * Register a publish executor
     * 
     * @param publishExecutor   publish executor
     */
    public void register(PublishExecutor publishExecutor)
    {
        publishExectors.put(publishExecutor.getName(), publishExecutor);
    }
    
    /**
     * Get registered publish executor by name.
     * 
     * @param name  name
     * @return {@link PublishExecutor}]
     */
    public PublishExecutor get(String name)
    {
        return publishExectors.get(name);
    }
}
