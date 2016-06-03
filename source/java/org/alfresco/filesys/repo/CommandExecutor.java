package org.alfresco.filesys.repo;

import java.io.IOException;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.TreeConnection;

/**
 * The Command Executor - executes commands!
 */
public interface CommandExecutor 
{
    /**
     * Execute the command.
     * @param command
     * 
     * @return an object for return or null if there is no return value. 
     * @throws IOException 
     */
    public Object execute(SrvSession sess, TreeConnection tree, Command command) throws IOException;
}
