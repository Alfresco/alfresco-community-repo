/**
 * package org.alfresco.filesys.repo.rules;
 * @author mrogers
 *
 */
package org.alfresco.filesys.repo.rules;

import java.util.List;

/**
 * EvaluatorContext
 */
public interface EvaluatorContext
{
    public List<ScenarioInstance> getScenarioInstances();
}
