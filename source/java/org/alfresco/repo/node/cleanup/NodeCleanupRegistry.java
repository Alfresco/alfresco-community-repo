package org.alfresco.repo.node.cleanup;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.StackTraceUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link NodeCleanupWorker worker} that aggregates any number of
 * {@link #register(NodeCleanupWorker) registered} workers.
 * 
 * @author Derek Hulley
 * @since 2.2 SP2
 */
public class NodeCleanupRegistry implements NodeCleanupWorker
{
    private static Log logger = LogFactory.getLog(NodeCleanupRegistry.class);
    
    private List<NodeCleanupWorker> cleanupWorkers;
    
    public NodeCleanupRegistry()
    {
        cleanupWorkers = new ArrayList<NodeCleanupWorker>(5);
    }
    
    public void register(NodeCleanupWorker cleanupWorker)
    {
        cleanupWorkers.add(cleanupWorker);
    }

    /**
     * Calls all registered cleaners in order, without transactions or authentication.
     * The return messages are aggregated.
     */
    public List<String> doClean()
    {
        List<String> results = new ArrayList<String>(100);
        for (NodeCleanupWorker cleanupWorker : cleanupWorkers)
        {
            try
            {
                results.addAll(cleanupWorker.doClean());
            }
            catch (Throwable e)
            {
                // This failed.  The cleaner should be handling this, but we can't guarantee it.
                logger.error(
                        "NodeCleanupWork doesn't handle all exception conditions: " +
                        cleanupWorker.getClass().getName());
                StringBuilder sb = new StringBuilder(1024);
                StackTraceUtil.buildStackTrace(
                    "Node cleanup failed: " +
                    "   Worker: " + cleanupWorker.getClass().getName() + "\n" +
                    "   Error:  " + e.getMessage(),
                    e.getStackTrace(),
                    sb,
                    0);
                results.add(sb.toString());
            }
        }
        return results;
    }
}