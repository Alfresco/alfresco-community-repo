/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.filesys.state;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * File State Reaper Class
 * 
 * <p>FileStateTable objects register with the file state reaper to periodically check for expired file states.
 *
 * @author gkspencer
 */
public class FileStateReaper implements Runnable {

	// Logging
	
    private static final Log logger = LogFactory.getLog(FileStateReaper.class);

    // Default expire check thread interval

    private static final long DEFAULT_EXPIRECHECK = 15000;

    // Wakeup interval for the expire file state checker thread

    private long m_expireInterval = DEFAULT_EXPIRECHECK;
    
    //	File state checker thread
    
    private Thread m_thread;
    
    //	Shutdown request flag
    
    private boolean m_shutdown;

    // List of file state tables to be scanned for expired file states

    private Hashtable<String, FileStateTable> m_stateTables;
    
    /**
     * Default constructor
     */
    public FileStateReaper()
    {
    	// Create the reaper thread
    	
        m_thread = new Thread(this);
        m_thread.setDaemon(true);
        m_thread.setName("FileStateReaper");
        m_thread.start();
        
        // Create the file state table list
        
        m_stateTables = new Hashtable<String, FileStateTable>();
    }
    
    /**
     * Return the expired file state checker interval, in milliseconds
     * 
     * @return long
     */
    public final long getCheckInterval()
    {
        return m_expireInterval;
    }

    /**
     * Set the expired file state checker interval, in milliseconds
     * 
     * @param chkIntval long
     */
    public final void setCheckInterval(long chkIntval)
    {
        m_expireInterval = chkIntval;
    }

    /**
     * Add a file state table to the reaper list
     * 
     * @param filesysName String
     * @param stateTable FileStateTable
     */
    public final void addStateTable( String filesysName, FileStateTable stateTable)
    {
    	// DEBUG
    	
    	if ( logger.isDebugEnabled())
    		logger.debug( "Added file state table for " + filesysName);
    	
    	m_stateTables.put( filesysName, stateTable);
    }
    
    /**
     * Remove a state table from the reaper list
     * 
     * @param filesysName String
     */
    public final void removeStateTable( String filesysName)
    {
    	m_stateTables.remove( filesysName);

    	// DEBUG
    	
    	if ( logger.isDebugEnabled())
    		logger.debug( "Removed file state table for " + filesysName);
    	
    }
    
    /**
     * Expired file state checker thread
     */
    public void run()
    {
        // Loop forever

    	m_shutdown = false;
    	
        while ( m_shutdown == false)
        {

            // Sleep for the required interval

            try
            {
                Thread.sleep(getCheckInterval());
            }
            catch (InterruptedException ex)
            {
            }

            //	Check for shutdown
            
            if ( m_shutdown == true)
            {
            	//	Debug
            	
            	if ( logger.isDebugEnabled())
            		logger.debug("FileStateReaper thread closing");

            	return;
            }
            
            // Check if there are any state tables registered
            
            if ( m_stateTables != null && m_stateTables.size() > 0)
            {
	            try
	            {
	            	// Loop through the registered file state tables and remove expired file states
	            	
	            	Enumeration<String> filesysNames = m_stateTables.keys();
	            	
	            	while ( filesysNames.hasMoreElements())
	            	{
	            		// Get the current filesystem name and associated state table
	            		
	            		String filesysName = filesysNames.nextElement();
	            		FileStateTable stateTable = m_stateTables.get( filesysName);
	            		
		                // Check for expired file states
		
		                int cnt = stateTable.removeExpiredFileStates();
		
		                // Debug
		
		                if (logger.isDebugEnabled() && cnt > 0)
		                    logger.debug("Expired " + cnt + " file states for " + filesysName + ", cache=" + stateTable.numberOfStates());
	            	}
	            }
	            catch (Exception ex)
	            {
	            	// Log errors if not shutting down
	            	
	            	if ( m_shutdown == false)
	            		logger.debug(ex);
	            }
            }
        }
    }

	/**
	 * Request the file state checker thread to shutdown
	 */
	public final void shutdownRequest() {
		m_shutdown = true;
		
		if ( m_thread != null)
		{
			try {
				m_thread.interrupt();
			}
			catch (Exception ex) {
			}
		}
	}
}
