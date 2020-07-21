/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.alfresco;

import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FileSystem;
import org.alfresco.jlan.server.filesys.SrvDiskInfo;
import org.alfresco.jlan.server.locking.LockManager;
import org.alfresco.jlan.server.locking.OpLockManager;
import org.alfresco.repo.admin.SysAdminParams;

/**
 * Alfresco Filesystem Context Class
 * 
 * <p>Contains per filesystem context.
 * 
 * @author GKSpencer
 */
public abstract class AlfrescoContext extends DiskDeviceContext
{
    private SysAdminParams sysAdminParams;
    
    private boolean isAlfrescoURLEnabled = false;
    private boolean isShareURLEnabled = false;

	// Debug levels
	
	public final static int DBG_FILE		= 0x00000001;	// file/folder create/delete
	public final static int DBG_FILEIO		= 0x00000002;	// file read/write/truncate
	public final static int DBG_SEARCH  	= 0x00000004;	// folder search
	public final static int DBG_INFO        = 0x00000008;	// file/folder information
	public final static int DBG_LOCK        = 0x00000010;	// file byte range locking
	public final static int DBG_PSEUDO      = 0x00000020;	// pseudo files/folders
	public final static int DBG_RENAME      = 0x00000040;	// rename file/folder
	
	// Filesystem debug flag strings
	  
	private static final String m_filesysDebugStr[] = { "FILE", "FILEIO", "SEARCH", "INFO", "LOCK", "PSEUDO", "RENAME" };

    // URL pseudo file web path prefix (server/port/webapp) and link file name
    
    private String m_urlFileName;
    private String m_shareUrlFileName;
    
    // Debug flags
    //
    // Requires the logger to be enabled for debug output
    
    public int m_debug;
    
    public AlfrescoContext()
    {
        // Default the filesystem to look like an 80Gb sized disk with 90% free space
        
        setDiskInformation(new SrvDiskInfo(2560000, 64, 512, 2304000));
        
        // Set parameters
        
        setFilesystemAttributes(FileSystem.CasePreservedNames + FileSystem.UnicodeOnDisk +
                FileSystem.CaseSensitiveSearch);        
    }
    
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }
    
    public SysAdminParams getSysAdminParams()
    {
        return sysAdminParams;
    }

    public void setDisableChangeNotification(boolean disableChangeNotification)
    {
        enableChangeHandler(!disableChangeNotification);
    }
    
    /**
     * Complete initialization by registering with a disk driver
     */
    public void initialize(AlfrescoDiskDriver filesysDriver)
    {
        // no op
    }

    /**
     * Return the filesystem type, either FileSystem.TypeFAT or FileSystem.TypeNTFS.
     * 
     * @return String
     */
    public String getFilesystemType()
    {
        return FileSystem.TypeNTFS;
    }
    
//    /**
//     * Determine if the URL pseudo file is enabled
//     * 
//     * @return boolean
//     */
//    public final boolean hasURLFile()
//    {
//        if (m_urlFileName != null)
//            return true;
//        return false;
//    }
//     
    /**
     * Return the URL pseudo file path prefix
     * @deprecated - does not know about share
     * 
     * @return String
     */
    public final String getURLPrefix()
    {
        return sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":" + sysAdminParams.getAlfrescoPort() + "/" + sysAdminParams.getAlfrescoContext() + "/";
    }
    
    public final String getShareUrlPrefix()
    {
    	return sysAdminParams.getShareProtocol() + "://" + sysAdminParams.getShareHost() + ":" + sysAdminParams.getSharePort() + "/" + sysAdminParams.getShareContext() + "/";
    }

    /**
     * Return the URL pseudo file name
     * 
     * @return String
     */
    public final String getURLFileName()
    {
        return m_urlFileName;
    }
    
    /**
     * Return the Share URL pseudo file name
     * 
     * @return String
     */
    public final String getShareURLFileName()
    {
        return m_shareUrlFileName;
    }

    
    /**
     * Set the URL pseudo file name
     * 
     * @param urlFileName String
     */
    public final void setURLFileName(String urlFileName)
    {
        m_urlFileName = urlFileName;

        // URL file name must end with .url
        if (urlFileName != null)
        {
            if (!urlFileName.endsWith(".url"))
                throw new AlfrescoRuntimeException("URL link file must end with .url, " + urlFileName);
        }
    }
    
    /**
     * Set the Share URL pseudo file name
     * 
     * @param urlFileName String
     */
    public final void setShareURLFileName(String urlFileName)
    {
        m_shareUrlFileName = urlFileName;

        // URL file name must end with .url
        if (urlFileName != null)
        {
            if (!urlFileName.endsWith(".url"))
                throw new AlfrescoRuntimeException("URL Share link file must end with .url, " + urlFileName);
        }
    }
    
    /**
     * Set the debug flags, also requires the logger to be enabled for debug output
     * 
     * @param flagsStr String
     */
    public final void setDebug(String flagsStr)
    {
    	int filesysDbg = 0;
    	
    	if (flagsStr != null)
        {
	        // Parse the flags
	  
	        StringTokenizer token = new StringTokenizer(flagsStr.toUpperCase(), ",");
	  
	        while (token.hasMoreTokens())
	        {
	        	// Get the current debug flag token
  
                String dbg = token.nextToken().trim();
  
                // Find the debug flag name
  
                int idx = 0;
                boolean match = false;
  
                while (idx < m_filesysDebugStr.length && match == false)
                {
                	if ( m_filesysDebugStr[idx].equalsIgnoreCase(dbg) == true)
                		match = true;
                	else
                		idx++;
                }
  
                if (match == false)
                    throw new AlfrescoRuntimeException("Invalid filesystem debug flag, " + dbg);
  
                // Set the debug flag
  
                filesysDbg += 1 << idx;
            }
	        
	        // Set the debug flags
	        
	        m_debug = filesysDbg;
        }
    }
    
    /**
     * Check if a debug flag is enabled
     * 
     * @param flg int
     * @return boolean
     */
    public final boolean hasDebug(int flg)
    {
    	return (m_debug & flg) != 0 ? true : false;
    }
    
    /**
     * Start the filesystem
     * 
     * @param share DiskSharedDevice
     * @exception DeviceContextException
     */
    public void startFilesystem(DiskSharedDevice share)
        throws DeviceContextException {
        
      

        // Call the base class
        
        super.startFilesystem(share);
    }

    LockManager lockManager;

    public void setLockManager(LockManager lockManager)
    {
        this.lockManager = lockManager;
    }

    /**
     * Return the lock manager, if enabled
     *
     * @return LockManager
     */
    public LockManager getLockManager() {
        return lockManager;
    }

    OpLockManager opLockManager;
    
    /**
     * Return the oplock manager, if enabled
     * 
     * @return OpLockManager
     */
    public OpLockManager getOpLockManager() 
    {
        return opLockManager;
    }
    
    public void setOpLockManager(OpLockManager opLockManager) 
    {
        this.opLockManager = opLockManager;
    }

    public void setAlfrescoURLEnabled(boolean isAlfrescoURLEnabled)
    {
        this.isAlfrescoURLEnabled = isAlfrescoURLEnabled;
    }

    public boolean isAlfrescoURLEnabled()
    {
        return isAlfrescoURLEnabled;
    }

    public void setShareURLEnabled(boolean isShareURLEnabled)
    {
        this.isShareURLEnabled = isShareURLEnabled;
    }

    public boolean isShareURLEnabled()
    {
        return isShareURLEnabled;
    }

}
