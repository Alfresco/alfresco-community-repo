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
package org.alfresco.filesys.repo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskFullException;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.quota.QuotaManager;
import org.alfresco.jlan.server.filesys.quota.QuotaManagerException;
import org.alfresco.jlan.util.MemorySize;
import org.alfresco.jlan.util.StringList;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Content Quota Manager Class
 * 
 * <p>Implementation of JLAN QuotaManager interface for the Alfresco repository.
 * <p>Keeps an in memory quota for each active user.    After a configurable length of 
 * time quotas are removed from memory.  
 * 
 * @author gkspencer
 *
 */
public class ContentQuotaManager implements QuotaManager, Runnable {

    // Debug logging
    
    private static final Log logger = LogFactory.getLog(ContentQuotaManager.class);
    
    // User details idle check interval
    
    private static final long   UserQuotaCheckInterval  =   1 * 60 * 1000;  // 1 minute
    private static final long   UserQuotaExpireInterval =   5 * 60 * 1000;  // 5 minutes
    
    // Associated filesystem driver
    
    private DiskInterface m_filesys;
    
    // Content usage service
    
    private ContentUsageService m_usageService;
    
    private ContentService contentService;
    
    // Track live usage of users that are writing files
    
    private HashMap<String, UserQuotaDetails> m_liveUsage;
    private Object m_addDetailsLock = new Object();
    
    // User details inactivity checker thread
    
    private Thread m_thread;
    private boolean m_shutdown;
    
    public void init()
    {
        PropertyCheck.mandatory(this, "contentService", getContentService());
        PropertyCheck.mandatory(this, "contentUsageService", m_usageService);
    }

    /**
     * Get the usage service
     * 
     * @return ContentUsageService
     */
    public final ContentUsageService getUsageService() {
        return m_usageService;
    }
    
    /**
     * Set the usage service
     * 
     * @param usageService ContentUsageService
     */
    public final void setUsageService(ContentUsageService usageService) {
        m_usageService = usageService;
    }
    
    /**
     * Return the free space available in bytes
     * 
     * @return long
     */
	public long getAvailableFreeSpace() {

		// Get the live free space value from the content store, if supported
		
		long freeSpace = contentService.getStoreFreeSpace();
		if ( freeSpace == -1L) {
			
			// Content store does not support sizing, return a large dummy value
		
			freeSpace = ContentDiskDriver.DiskFreeDefault;
		}
		
		return freeSpace;
	}

    /**
     * Return the free space available to the specified user/session
     *
     * @param sess SrvSession
     * @param tree TreeConnection
     * @return long
     */
	public long getUserFreeSpace(SrvSession sess, TreeConnection tree) {
	    
	    // Check if content usage is enabled
	    
	    if ( m_usageService.getEnabled() == false)
	        return 0L;
	    
	    // Check if there is a live usage record for the user
	    
	    UserQuotaDetails userQuota = getQuotaDetails(sess, true);
	    if ( userQuota != null)
	    {
	        return userQuota.getAvailableSpace();
	    }
	    // No quota details available
	    
		return 0L;
	}

    /**
     * Allocate space on the filesystem.
     *
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param file NetworkFile
     * @param alloc long requested allocation size
     * @return long granted allocation size
     * @exception IOException
     */
    public long allocateSpace(SrvSession sess, TreeConnection tree, NetworkFile file, long alloc)
        throws IOException {

        // Check if content usage is enabled
        
        if ( m_usageService.getEnabled() == false)
            return alloc;
        
        // Check if there is a live usage record for the user
        
        UserQuotaDetails userQuota = getQuotaDetails(sess, true);
        long allowedAlloc = 0L;
        
        if ( userQuota != null) {
            
            // Check if the user has a usage quota
            
            if ( userQuota.hasUserQuota()) {
                
                synchronized ( userQuota) {
                    
                    // Check if the user has enough free space allocation
                    
                    if ( alloc > 0 && userQuota.getAvailableSpace() >= alloc) {
                        userQuota.addToCurrentUsage( alloc);
                        allowedAlloc = alloc;
                    }
                }
            }
            else {
                
                // Update the live usage
                
                synchronized ( userQuota) {
                    userQuota.addToCurrentUsage( alloc);
                    allowedAlloc = alloc;
                }
            }
        }
        else if ( logger.isDebugEnabled())
            logger.debug("Failed to allocate " + alloc + " bytes for sess " + sess.getUniqueId());
        
        // Check if the allocation was allowed
        
        if ( allowedAlloc < alloc) {
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Allocation failed userQuota=" + userQuota);

            throw new DiskFullException();
        }
        else if ( logger.isDebugEnabled())
            logger.debug("Allocated " + alloc + " bytes, userQuota=" + userQuota);
        
        // Return the allocation size
        
        return allowedAlloc;
    }

    /**
     * Release space to the free space for the filesystem.
     *
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param fid int
     * @param path String
     * @param alloc long
     * @exception IOException
     */
	public void releaseSpace(SrvSession sess, TreeConnection tree, int fid, String path, long alloc)
	    throws IOException {

        // Check if content usage is enabled
        
        if ( m_usageService.getEnabled() == false)
            return;
        
        // Check if there is a live usage record for the user
        
        UserQuotaDetails userQuota = getQuotaDetails(sess, true);
        
        if ( userQuota != null) {
            
            synchronized ( userQuota) {
                
                // Release the space from the live usage value
                
                userQuota.subtractFromCurrentUsage( alloc);
            }
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Released " + alloc + " bytes, userQuota=" + userQuota);
        }
        else if ( logger.isDebugEnabled())
            logger.debug("Failed to release " + alloc + " bytes for sess " + sess.getUniqueId());
	}

    /**
     * Start the quota manager.
     * 
     * @param disk DiskInterface
     * @param ctx DiskDeviceContext
     * @exception QuotaManagerException
     */
	public void startManager(DiskInterface disk, DiskDeviceContext ctx)
		throws QuotaManagerException 
	{

	    if(logger.isDebugEnabled())
	    {
	        logger.debug("Start Quota Manager");
	    }
	    
	    // Save the filesystem driver details
	    m_filesys = disk;
	    
	    // Allocate the live usage table
	    
	    m_liveUsage = new HashMap<String, UserQuotaDetails>();
	    
        // Create the inactivity checker thread        
        m_thread = new Thread(this);
        m_thread.setDaemon(true);
        m_thread.setName("ContentQuotaManagerChecker");
        m_thread.start();
	}

    /**
     * Stop the quota manager
     * 
     * @param disk DiskInterface
     * @param ctx DiskDeviceContext
     * @exception QuotaManagerException
     */
	public void stopManager(DiskInterface disk, DiskDeviceContext ctx)
		throws QuotaManagerException 
    {
	    
	    if(logger.isDebugEnabled())
	    {
	        logger.debug("Stop Quota Manager");
	    }

	    // Clear out the live usage details
	    
	    m_liveUsage.clear();
	    
	    // Shutdown the checker thread
	    
	    m_shutdown = true;
	    m_thread.interrupt();
	}
	
	/**
	 * Get the usage details for the session/user
	 * 
	 * @param sess SrvSession
	 * @param loadDetails boolean
	 * @return UserQuotaDetails or null
	 */
	private UserQuotaDetails getQuotaDetails(SrvSession sess, boolean loadDetails) {
	    
        UserQuotaDetails userQuota = null;
        
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        
        if ( sess != null && userName != null) 
        {        
            // Get the live usage values

            userQuota = m_liveUsage.get(userName);
            
            if ( userQuota == null && loadDetails == true) 
            {
                // User is not in the live tracking table, load details for the user
                try 
                {
                    logger.debug("user is not in cache - load details");
                    userQuota = loadUsageDetails(userName);
                }
                catch ( QuotaManagerException ex) 
                {
                    if ( logger.isDebugEnabled())
                    {
                        logger.debug("Unable to load usage details", ex);
                    }
                }
            }
        }
        
        // Return the user quota details
        
        return userQuota;
	}
	
	/**
	 * Load the user quota details
	 * 
	 * @param user - name of the user.
	 * @return UserQuotaDetails
	 * @throws QuotaManagerException
	 */
	private UserQuotaDetails loadUsageDetails(String userName)
	    throws QuotaManagerException {
	    
	    // Check if the user name is available
	    
	    UserQuotaDetails quotaDetails = null;
   
	    try 
	    {
	        if ( userName == null || userName.length() == 0)
	        {
	            logger.debug("user name is null or empty - throw QuotaManagerException");
	            throw new QuotaManagerException("No user name for client");
	        }
	        	        
	        // Get the usage quota and current usage values for the user
	        
	        long userQuota = m_usageService.getUserQuota( userName);
	        long userUsage = m_usageService.getUserUsage( userName);
	        
	        // Create the user quota details for live tracking
	        
	        quotaDetails = new UserQuotaDetails( userName, userQuota);
	        if ( userUsage > 0L)
	        {
	            quotaDetails.setCurrentUsage( userUsage);
	        }
	        
	        // Add the details to the live tracking table
	        
	        synchronized ( m_addDetailsLock) {
	            
	            // Check if another thread has added the details
	            
	            UserQuotaDetails details = m_liveUsage.get( userName);
	            if ( details != null)
	            {
	                quotaDetails = details;
	            }
	            else
	            {
	                m_liveUsage.put( userName, quotaDetails);
	            }
            }
	        
	        // DEBUG
	        
	        if ( logger.isDebugEnabled())
	        {
	            logger.debug( "Added live usage tracking " + quotaDetails);
	        }
	    }
	    catch ( Exception ex) 
	    {

	        // Log the error
	        
	        if ( logger.isDebugEnabled())
	        {
	            logger.debug("Failed to load usage for" + userName, ex);
	        }
	        // Failed to load usage details
	        
	        throw new QuotaManagerException("Failed to load usage for " + userName + ", " + ex);
	    }
	    
	    // Return the user usage details
	    
	    return quotaDetails;
	}
	
	/**
	 * Inactivity checker, run in a seperate thread
	 */
	public void run() {
	    
	    // DEBUG
	    
	    if ( logger.isDebugEnabled())
	        logger.debug("Content quota manager checker thread starting");
	    
        // Loop forever

	    StringList removeNameList = new StringList();
	    
        m_shutdown = false;
        
        while ( m_shutdown == false)
        {

            // Sleep for the required interval

            try
            {
                Thread.sleep( UserQuotaCheckInterval);
            }
            catch (InterruptedException ex)
            {
            }

            //  Check for shutdown
            
            if ( m_shutdown == true)
            {
                //  Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Content quota manager checker thread closing");

                return;
            }
            
            // Check if there are any user quota details to check
            
            if ( m_liveUsage != null && m_liveUsage.size() > 0)
            {
                try
                {
                    // Timestamp to check if the quota details is inactive
                    
                    long checkTime = System.currentTimeMillis() - UserQuotaExpireInterval;
                    
                    // Loop through the user quota details

                    removeNameList.remoteAllStrings();
                    Iterator<String> userNames = m_liveUsage.keySet().iterator();
                    
                    while ( userNames.hasNext()) {
                        
                        // Get the user quota details and check if it has been inactive in the last check interval
                        
                        String userName = userNames.next();
                        UserQuotaDetails quotaDetails =  m_liveUsage.get( userName);
                        
                        if ( quotaDetails.getLastUpdated() < checkTime) {
                            
                            // Add the user name to the remove list, inactive
                            
                            removeNameList.addString( userName);
                        }                            
                    }
                    
                    // Remove inactive records from the live quota tracking
                    
                    while ( removeNameList.numberOfStrings() > 0) 
                    {
                        
                        // Get the current user name and remove the record
                        
                        String userName = removeNameList.removeStringAt( 0);
                        UserQuotaDetails quotaDetails = m_liveUsage.remove( userName);
                        
                        // DEBUG
                        
                        if ( logger.isDebugEnabled())
                            logger.debug("Removed inactive usage tracking, " + quotaDetails);
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

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public ContentService getContentService()
    {
        return contentService;
    }
}
