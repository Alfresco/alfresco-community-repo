package org.alfresco.filesys.repo.rules;

import java.util.List;

/**
 * A dependent instance takes account of some other instance.
 */ 
public interface DependentInstance
{
    /**
     * Notifies the scenario that there are conflicting loosing scenarios.
     * <p>
     * The winning scenario may modify its behavior to take account of the loosers.
     * <p>
     * @param results
     * @param command
     * @return the new command
     */
    Command win(List<ScenarioResult> results, Command command);
    
}
