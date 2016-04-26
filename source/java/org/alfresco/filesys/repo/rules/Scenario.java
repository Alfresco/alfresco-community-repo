package org.alfresco.filesys.repo.rules;

import java.util.List;

/**
 * A scenario is a factory for scenario instances.
 * 
 */
public interface Scenario
{
    /**
     * Create a new ScenarioInstance
     * <p>
     * If the scenario is interested in the specified operation then 
     * return a new scenario instance.
     * @param ctx EvaluatorContext.
     * @param operation the operation to be performed
     * @return the scenario instance or null if a new instance is not required.
     */
    ScenarioInstance createInstance(EvaluatorContext ctx, Operation operation);
      
    
}
