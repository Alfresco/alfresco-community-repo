package org.alfresco.repo.node.cleanup;

import java.util.List;

/**
 * Interface for classes that implement a snippet of node cleanup.
 * 
 * @author Derek Hulley
 * @since 2.2 SP2
 */
public interface NodeCleanupWorker
{
    /**
     * Perform some work to clean up data.  All errors must be handled and converted
     * to error messages.
     * 
     * @return              Returns a list of informational messages.
     */
    List<String> doClean();
}