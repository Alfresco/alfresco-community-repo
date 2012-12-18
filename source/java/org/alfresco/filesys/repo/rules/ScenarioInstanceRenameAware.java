package org.alfresco.filesys.repo.rules;

/**
 * The scenario instance wants to be notified about rename.
 * 
 * @author mrogers
 *
 */
public interface ScenarioInstanceRenameAware
{
    /**
     * Notify the scenario of a successful rename operation.
     * 
     * @param operation
     * @param command
     */
    public void notifyRename(Operation operation, Command command);

}
