package org.alfresco.filesys.repo;

import org.alfresco.filesys.repo.rules.Command;

public interface ResultCallback extends Command
{
    /**
     * Call the callback with the result of the operation.
     * @param result the result.
     */
    void execute(Object result);

}
