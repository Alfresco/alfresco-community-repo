/**
 * package org.alfresco.filesys.repo.rules;
 * @author mrogers
 *
 */
package org.alfresco.filesys.repo.rules;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EvaluatorContext
 */
public interface EvaluatorContext
{
    /**
     * Get the current scenario instances for this context
     * @return a list of the curent scenario instances
     */
    public List<ScenarioInstance> getScenarioInstances();
    
    /**
     * Get the session state for this context.
     * @return the session state for this context.
     */
    public Map<String, Object> getSessionState();
    
    
}
