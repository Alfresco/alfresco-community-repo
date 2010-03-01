/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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