package org.alfresco.repo.content.transform;

import org.apache.commons.logging.Log;

/**
 * Interface that gives access to Log entries
 */
interface LogEntries extends Log
{
    /**
     * Returns the log entries.
     * @param n the maximum number of entries to return. All if n is smaller or equal to zero.
     */
    public abstract String[] getEntries(int n);
}