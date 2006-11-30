/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.state;

import java.util.*;

import org.apache.commons.logging.*;

/**
 * File State Table Class
 * 
 * <p>Contains an indexed list of the currently open files/folders.
 * 
 * @author gkspencer
 */
public class FileStateTable
{
    private static final Log logger = LogFactory.getLog(FileStateTable.class);

    // Initial allocation size for the state cache

    private static final int INITIAL_SIZE = 100;

    // File state table, keyed by file path

    private Hashtable<String, FileState> m_stateTable;

    // File state expiry time in seconds

    private long m_cacheTimer = 2 * 60000L; // 2 minutes default

    /**
     * Class constructor
     */
    public FileStateTable()
    {
        m_stateTable = new Hashtable<String, FileState>(INITIAL_SIZE);

        // Start the expired file state checker thread

    }

    /**
     * Get the file state cache timer, in milliseconds
     * 
     * @return long
     */
    public final long getCacheTimer()
    {
        return m_cacheTimer;
    }

    /**
     * Return the number of states in the cache
     * 
     * @return int
     */
    public final int numberOfStates()
    {
        return m_stateTable.size();
    }

    /**
     * Set the default file state cache timer, in milliseconds
     * 
     * @param tmo long
     */
    public final void setCacheTimer(long tmo)
    {
        m_cacheTimer = tmo;
    }

    /**
     * Add a new file state
     * 
     * @param fstate FileState
     */
    public final synchronized void addFileState(FileState fstate)
    {

        // Check if the file state already exists in the cache

        if (logger.isDebugEnabled() && m_stateTable.get(fstate.getPath()) != null)
            logger.debug("***** addFileState() state=" + fstate.toString() + " - ALREADY IN CACHE *****");

        // DEBUG

        if (logger.isDebugEnabled() && fstate == null)
        {
            logger.debug("addFileState() NULL FileState");
            return;
        }

        // Set the file state timeout and add to the cache

        fstate.setExpiryTime(System.currentTimeMillis() + getCacheTimer());
        m_stateTable.put(fstate.getPath(), fstate);
    }

    /**
     * Find the file state for the specified path
     * 
     * @param path String
     * @return FileState
     */
    public final synchronized FileState findFileState(String path)
    {
        return m_stateTable.get(FileState.normalizePath(path));
    }

    /**
     * Find the file state for the specified path, and optionally create a new file state if not
     * found
     * 
     * @param path String
     * @param isdir boolean
     * @param create boolean
     * @return FileState
     */
    public final synchronized FileState findFileState(String path, boolean isdir, boolean create)
    {

        // Find the required file state, if it exists

        FileState state = m_stateTable.get(FileState.normalizePath(path));

        // Check if we should create a new file state

        if (state == null && create == true)
        {

            // Create a new file state

            state = new FileState(path, isdir);

            // Set the file state timeout and add to the cache

            state.setExpiryTime(System.currentTimeMillis() + getCacheTimer());
            m_stateTable.put(state.getPath(), state);
        }

        // Return the file state

        return state;
    }

    /**
     * Update the name that a file state is cached under, and the associated file state
     * 
     * @param oldName String
     * @param newName String
     * @return FileState
     */
    public final synchronized FileState updateFileState(String oldName, String newName)
    {

        // Find the current file state

        FileState state = m_stateTable.remove(FileState.normalizePath(oldName));

        // Rename the file state and add it back into the cache using the new name

        if (state != null)
        {
            state.setPath(newName);
            addFileState(state);
        }

        // Return the updated file state

        return state;
    }

    /**
     * Enumerate the file state cache
     * 
     * @return Enumeration
     */
    public final Enumeration enumerate()
    {
        return m_stateTable.keys();
    }

    /**
     * Remove the file state for the specified path
     * 
     * @param path String
     * @return FileState
     */
    public final synchronized FileState removeFileState(String path)
    {

        // Remove the file state from the cache

        FileState state = m_stateTable.remove(FileState.normalizePath(path));

        // Return the removed file state

        return state;
    }

    /**
     * Rename a file state, remove the existing entry, update the path and add the state back into
     * the cache using the new path.
     * 
     * @param newPath String
     * @param state FileState
     */
    public final synchronized void renameFileState(String newPath, FileState state)
    {

        // Remove the existing file state from the cache, using the original name

        m_stateTable.remove(state.getPath());

        // Update the file state path and add it back to the cache using the new name

        state.setPath(FileState.normalizePath(newPath));
        m_stateTable.put(state.getPath(), state);
    }

    /**
     * Remove all file states from the cache
     */
    public final synchronized void removeAllFileStates()
    {

        // Check if there are any items in the cache

        if (m_stateTable == null || m_stateTable.size() == 0)
            return;

        // Enumerate the file state cache and remove expired file state objects

        Enumeration enm = m_stateTable.keys();

        while (enm.hasMoreElements())
        {

            // Get the file state

            FileState state = m_stateTable.get(enm.nextElement());

            // DEBUG

            if (logger.isDebugEnabled())
                logger.debug("++ Closed: " + state.getPath());
        }

        // Remove all the file states

        m_stateTable.clear();
    }

    /**
     * Remove expired file states from the cache
     * 
     * @return int
     */
    public final int removeExpiredFileStates()
    {

        // Check if there are any items in the cache

        if (m_stateTable == null || m_stateTable.size() == 0)
            return 0;

        // Enumerate the file state cache and remove expired file state objects

        Enumeration enm = m_stateTable.keys();
        long curTime = System.currentTimeMillis();

        int expiredCnt = 0;

        while (enm.hasMoreElements())
        {

            // Get the file state

            FileState state = m_stateTable.get(enm.nextElement());

            if (state != null && state.hasNoTimeout() == false)
            {

                synchronized (state)
                {

                    // Check if the file state has expired and there are no open references to the
                    // file

                    if (state.hasExpired(curTime) && state.getOpenCount() == 0)
                    {

                        // Remove the expired file state

                        m_stateTable.remove(state.getPath());

                        // DEBUG

                        if (logger.isDebugEnabled())
                            logger.debug("Expired file state: " + state);

                        // Update the expired count

                        expiredCnt++;
                    }
                }
            }
        }

        // Return the count of expired file states that were removed

        return expiredCnt;
    }

    /**
     * Dump the state cache entries to the specified stream
     */
    public final void Dump()
    {

        // Dump the file state cache entries to the specified stream

        if (m_stateTable.size() > 0)
            logger.debug("FileStateCache Entries:");

        Enumeration enm = m_stateTable.keys();
        long curTime = System.currentTimeMillis();

        while (enm.hasMoreElements())
        {
            String fname = (String) enm.nextElement();
            FileState state = m_stateTable.get(fname);

            logger.debug("  " + fname + "(" + state.getSecondsToExpire(curTime) + ") : " + state);
        }
    }
}