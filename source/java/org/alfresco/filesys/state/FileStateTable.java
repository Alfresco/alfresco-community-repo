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
package org.alfresco.filesys.state;

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

    // File state listener, can veto expiring of file states
    
    private FileStateListener m_stateListener;
    
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
    	FileState fstate = m_stateTable.get(FileState.normalizePath(path));
    	
    	if ( fstate != null)
    		fstate.updateAccessDateTime();
    	
    	return fstate;
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
            
            // DEBUG
            
            if ( logger.isDebugEnabled() && state.getPath().length() > 0 && state.getPath().indexOf("\\") == -1) {
            	logger.debug("*** File state path is not relative - " + state.getPath() + " ***");
            	Thread.dumpStack();
            }
        }

        // Update the access date/time if valid
        
        if ( state != null)
        	state.updateAccessDateTime();
        
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
            
            // Update the access date/time
            
            state.updateAccessDateTime();
        }

        // Return the updated file state

        return state;
    }

    /**
     * Enumerate the file state cache
     * 
     * @return Enumeration<String>
     */
    public final Enumeration<String> enumerate()
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
        
        // Updaet the access date/time
        
        state.updateAccessDateTime();
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

        Enumeration<String> enm = m_stateTable.keys();

        while (enm.hasMoreElements())
        {

            // Get the file state

            FileState state = m_stateTable.get(enm.nextElement());

			//	Check if there is a state listener
			
			if ( m_stateListener != null)
				m_stateListener.fileStateClosed(state);
			
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

        Enumeration<String> enm = m_stateTable.keys();
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
                    	// Check with the state listener before removing the file state, if enabled
                    	
                    	if ( hasStateListener() == false || m_stateListener.fileStateExpired( state) == true)
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

        Enumeration<String> enm = m_stateTable.keys();
        long curTime = System.currentTimeMillis();

        while (enm.hasMoreElements())
        {
            String fname = enm.nextElement();
            FileState state = m_stateTable.get(fname);

            logger.debug("  " + fname + "(" + state.getSecondsToExpire(curTime) + ") : " + state);
        }
    }
    
    /**
     * Add a file state listener
     * 
     * @param l FileStateListener
     */
    public final void addStateListener(FileStateListener l) {
    	m_stateListener = l;
    }
    
    /**
     * Remove a file state listener
     * 
     * @param l FileStateListener
     */
    public final void removeStateListener(FileStateListener l) {
  		if ( m_stateListener == l)
    		m_stateListener = null;
    }
    
    /**
     * Check if the file state listener is set
     * 
     * @return boolean
     */
    public final boolean hasStateListener() {
    	return m_stateListener != null ? true : false;
    }
}