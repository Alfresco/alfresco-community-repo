/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.alfresco;

import java.util.Enumeration;

import org.alfresco.filesys.server.filesys.*;
import org.alfresco.filesys.server.pseudo.PseudoFileImpl;
import org.alfresco.filesys.server.pseudo.PseudoFileInterface;
import org.alfresco.filesys.server.state.FileStateReaper;
import org.alfresco.filesys.server.state.FileStateTable;

/**
 * Alfresco Filesystem Context Class
 * 
 * <p>Contains per filesystem context.
 * 
 * @author GKSpencer
 */
public abstract class AlfrescoContext extends DiskDeviceContext
{
    // File state table and associated file state reaper
    
    private FileStateTable m_stateTable;
    private FileStateReaper m_stateReaper;
    
    // URL pseudo file web path prefix (server/port/webapp) and link file name
    
    private String m_urlPathPrefix;
    private String m_urlFileName;
    
    // Pseudo file interface
    
    private PseudoFileInterface m_pseudoFileInterface;

    // Desktop actions
    
    private DesktopActionTable m_desktopActions;
    
    // I/O control handler
    
    private IOControlHandler m_ioHandler;
    
    /**
     * Class constructor
     *
     * @param filesysName String
     * @param devName String
     */
    public AlfrescoContext(String filesysName, String devName)
    {
        super(filesysName, devName);
        
        // Default the filesystem to look like an 80Gb sized disk with 90% free space
        
        setDiskInformation(new SrvDiskInfo(2560000, 64, 512, 2304000));
        
        // Set parameters
        
        setFilesystemAttributes(FileSystem.CasePreservedNames + FileSystem.UnicodeOnDisk +
        		FileSystem.CaseSensitiveSearch);
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
    
    /**
     * Determine if the file state table is enabled
     * 
     * @return boolean
     */
    public final boolean hasStateTable()
    {
        return m_stateTable != null ? true : false;
    }
    
    /**
     * Return the file state table
     * 
     * @return FileStateTable
     */
    public final FileStateTable getStateTable()
    {
        return m_stateTable;
    }
    
    /**
     * Enable/disable the file state table
     * 
     * @param ena boolean
     * @param stateReaper FileStateReaper
     */
    public final void enableStateTable(boolean ena, FileStateReaper stateReaper)
    {
        if ( ena == false)
        {
        	// Remove the state table from the reaper
        	
        	stateReaper.removeStateTable( getFilesystemName());
            m_stateTable = null;
        }
        else if ( m_stateTable == null)
        {
        	// Create the file state table

            m_stateTable = new FileStateTable();
            
            // Register with the file state reaper
            
            stateReaper.addStateTable( getFilesystemName(), m_stateTable);
        }
        
        // Save the reaper, for deregistering when the filesystem is closed
        
        m_stateReaper = stateReaper;
    }
    
    /**
     * Determine if the pseudo file interface is enabled
     * 
     * @return boolean
     */
    public final boolean hasPseudoFileInterface()
    {
    	return m_pseudoFileInterface != null ? true : false;
    }
    
    /**
     * Return the pseudo file interface
     * 
     * @return PseudoFileInterface
     */
    public final PseudoFileInterface getPseudoFileInterface()
    {
    	return m_pseudoFileInterface;
    }

    /**
     * Enable the pseudo file interface for this filesystem
     */
    public final void enabledPseudoFileInterface()
    {
    	if ( m_pseudoFileInterface == null)
    		m_pseudoFileInterface = new PseudoFileImpl();
    }
    
    /**
     * Determine if there are desktop actins configured
     * 
     * @return boolean
     */
    public final boolean hasDesktopActions()
    {
    	return m_desktopActions != null ? true : false;
    }
    
    /**
     * Return the desktop actions table
     * 
     * @return DesktopActionTable
     */
    public final DesktopActionTable getDesktopActions()
    {
    	return m_desktopActions;
    }
    
    /**
     * Return the count of desktop actions
     * 
     * @return int
     */
    public final int numberOfDesktopActions()
    {
    	return m_desktopActions != null ? m_desktopActions.numberOfActions() : 0;
    }

    /**
     * Add a desktop action
     * 
     * @param action DesktopAction
     * @return boolean
     */
    public final boolean addDesktopAction(DesktopAction action)
    {
    	// Check if the desktop actions table has been created
    	
    	if ( m_desktopActions == null)
    	{
    		m_desktopActions = new DesktopActionTable();
    		
    		// Enable pseudo files
    		
    		enabledPseudoFileInterface();
    	}
    	
    	// Add the action
    	
    	return m_desktopActions.addAction(action);
    }

    /**
     * Determine if custom I/O control handling is enabled for this filesystem
     * 
     * @return boolean
     */
    public final boolean hasIOHandler()
    {
    	return m_ioHandler != null ? true : false;
    }
    
    /**
     * Return the custom I/O control handler
     * 
     * @return IOControlHandler
     */
    public final IOControlHandler getIOHandler()
    {
    	return m_ioHandler;
    }
    
    /**
     * Determine if the URL pseudo file is enabled
     * 
     * @return boolean
     */
    public final boolean hasURLFile()
    {
        if ( m_urlPathPrefix != null && m_urlFileName != null)
            return true;
        return false;
    }
    
    /**
     * Return the URL pseudo file path prefix
     * 
     * @return String
     */
    public final String getURLPrefix()
    {
        return m_urlPathPrefix;
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
     * Set the URL path prefix
     * 
     * @param urlPrefix String
     */
    public final void setURLPrefix(String urlPrefix)
    {
        m_urlPathPrefix = urlPrefix;
        
        if ( urlPrefix != null)
        	enabledPseudoFileInterface();
    }
    
    /**
     * Set the URL pseudo file name
     * 
     * @param urlFileName String
     */
    public final void setURLFileName(String urlFileName)
    {
        m_urlFileName = urlFileName;
        
        if ( urlFileName != null)
        	enabledPseudoFileInterface();
    }

    /**
     * Set the desktop actions
     * 
     * @param desktopActions DesktopActionTable
     * @param filesysDriver DiskInterface
     */
    public final void setDesktopActions(DesktopActionTable desktopActions, DiskInterface filesysDriver)
    {
    	// Enumerate the desktop actions and add to this filesystem
    	
    	Enumeration<String> names = desktopActions.enumerateActionNames();
    	
    	while ( names.hasMoreElements())
    	{
    		addDesktopAction( desktopActions.getAction(names.nextElement()));
    	}
    	
    	// If there are desktop actions then create the custom I/O control handler
    	
    	if ( numberOfDesktopActions() > 0)
    	{
    		// Create the custom I/O control handler
    	
    		m_ioHandler = createIOHandler( filesysDriver);
    		if ( m_ioHandler != null)
    			m_ioHandler.initialize(( AlfrescoDiskDriver) filesysDriver, this);
    	}
    }
    
    /**
     * Create the I/O control handler for this filesystem type
     * 
     * @param filesysDriver DiskInterface
     * @return IOControlHandler
     */
    protected abstract IOControlHandler createIOHandler( DiskInterface filesysDriver);
    
    /**
     * Set the I/O control handler
     * 
     * @param ioctlHandler IOControlHandler
     */
    protected void setIOHandler( IOControlHandler ioctlHandler)
    {
    	m_ioHandler = ioctlHandler;
    }
    
    /**
     * Close the filesystem context
     */
	public void CloseContext() {
		
		//	Deregister the file state table from the reaper
		
		if ( m_stateTable != null)
			enableStateTable( false, m_stateReaper);
		
		//	Call the base class
		
		super.CloseContext();
	}
}
