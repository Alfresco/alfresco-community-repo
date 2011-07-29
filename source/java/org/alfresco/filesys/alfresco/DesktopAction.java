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

package org.alfresco.filesys.alfresco;

import java.io.IOException;

import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.pseudo.MemoryPseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.ResourceFinder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.extensions.config.ConfigElement;

/**
 * Desktop Action Class
 *
 * @author gkspencer
 */
public abstract class DesktopAction {

    // Logging
    
    protected static final Log logger = LogFactory.getLog(DesktopAction.class);
    
    // Constants
    //
    // Action attributes
    
    public static final int AttrTargetFiles			= 0x0001;	// allow files from the same folder
    public static final int AttrTargetFolders		= 0x0002;	// allow sub-folders from the same folder
    public static final int AttrClientFiles			= 0x0004;	// allow files from a client drive
    															// File(s) will be copied to the target folder
    public static final int AttrClientFolders		= 0x0008;	// allow folders from a client drive
    															// Folder(s) will be copied to the target folder
    public static final int AttrAlfrescoFiles		= 0x0010;	// allow files from another path within the Alfresco share
    public static final int AttrAlfrescoFolders		= 0x0020;	// allow folders from another path within the Alfresco share
    public static final int AttrMultiplePaths		= 0x0040;	// run action using multiple paths
    															// default is to run the action against a single path with the client app calling the action
    															// multiple times
    public static final int AttrAllowNoParams		= 0x0080;	// allow action to run without parameters
    															// used when files/folder parameters are optional
    
    public static final int AttrAnyFiles			= AttrTargetFiles + AttrClientFiles + AttrAlfrescoFiles;
    public static final int AttrAnyFolders			= AttrTargetFolders + AttrClientFolders + AttrAlfrescoFolders;
    public static final int AttrAnyFilesFolders		= AttrAnyFiles + AttrAnyFolders;
    
    // Client side pre-processing actions
    
    public static final int PreCopyToTarget			= 0x0001;	// copy files/folders from another Alfresco folder to the target folder
    public static final int PreConfirmAction		= 0x0002;	// confirm action, allow user to abort
    public static final int PreLocalToWorkingCopy	= 0x0004;	// local files must match a working copy in the target folder

    // Desktop action status codes
    
    public static final int StsSuccess          = 0;
    
    public static final int StsError            = 1;
    public static final int StsFileNotFound     = 2;
    public static final int StsAccessDenied     = 3;
    public static final int StsBadParameter     = 4;
    public static final int StsNotWorkingCopy   = 5;
    public static final int StsNoSuchAction     = 6;
    public static final int StsLaunchURL		= 7;
    public static final int StsCommandLine		= 8;
    public static final int StsAuthTicket		= 9;
    
    // Action name
	
	private String m_name;
	
	// Pseudo file details
	
	private PseudoFile m_pseudoFile;
	
	private String m_filename;
	
	private String m_path;
	
	// Desktop action attributes
	
	private int m_attributes;
	
	// Desktop action client side pre-processing control
	
	private int m_clientPreActions;
	
	// Filesystem driver and context
	
	//private AlfrescoDiskDriver m_filesysDriver;
	private AlfrescoContext m_filesysContext;

	// Webapp URL
	
	private String m_webappURL;
	
	// Debug enable flag
	
	private boolean m_debug;
	
	/**
	 * Default constructor
	 */
	protected DesktopAction()
	{
	}
	
	/**
	 * Class constructor
	 * 
	 * @param attr int
	 * @param preActions int
	 */
	protected DesktopAction(int attr, int preActions)
	{
		setAttributes(attr);
		setPreProcessActions(preActions);
	}
	
	/**
	 * Class constructor
	 *
	 * @param name String
	 */
	protected DesktopAction(String name)
	{
		m_name = name;
	}
	
	/**
	 * Return the desktop action attributes
	 * 
	 * @return int
	 */
	public final int getAttributes()
	{
		return m_attributes;
	}
	
	/**
	 * Check for a specified action attribute
	 * 
	 * @param attr int
	 * @return boolean
	 */
	public final boolean hasAttribute(int attr)
	{
		return ( m_attributes & attr) != 0 ? true : false;
	}
	
	/**
	 * Return the desktop action pore-processing actions
	 * 
	 * @return int
	 */
	public final int getPreProcessActions()
	{
		return m_clientPreActions;
	}
	
	/**
	 * Check for the specified pre-process action
	 * 
	 * @param pre int
	 * @return boolean
	 */
	public final boolean hasPreProcessAction(int pre)
	{
		return (m_clientPreActions & pre) != 0 ? true : false;
	}
	
	/**
	 * Return the action name
	 * 
	 * @return String
	 */
	public final String getName()
	{
		return m_name;
	}

	/**
	 * Check if the action has an associated pseudo file
	 * 
	 * @return boolean
	 */
	public final boolean hasPseudoFile()
	{
		return m_pseudoFile != null ? true : false;
	}
	
	/**
	 * Return the associated pseudo file
	 * 
	 * @return PseudoFile
	 */
	public final PseudoFile getPseudoFile()
	{
		return m_pseudoFile;
	}

//	/**
//	 * Return the filesystem driver
//	 * 
//	 * @return AlfrescoDiskDriver
//	 */
//	public final AlfrescoDiskDriver getDriver()
//	{
//		return m_filesysDriver;
//	}
	
	/**
	 * Return the filesystem context
	 * 
	 * @return AlfrescoContext
	 */
	public final AlfrescoContext getContext()
	{
		return m_filesysContext;
	}
	
	/**
	 * Return the action confirmation string to be displayed by the client application
	 * 
	 * @return String
	 */
	public String getConfirmationString()
	{
		return null;
	}
	
	ServiceRegistry serviceRegistry;
	
	/**
	 * Return the service registry
	 * 
	 * @return ServiceRegistry
	 */
	public final ServiceRegistry getServiceRegistry()
	{
	    return serviceRegistry;
	}
	
	public final void setServiceRegistry (ServiceRegistry serviceRegistry)
	{
	    this.serviceRegistry = serviceRegistry;
	}
	
	/**
	 * Check if debug output is enabled
	 * 
	 * @return boolean
	 */
	public final boolean hasDebug()
	{
		return m_debug;
	}

	/**
	 * Check if the webapp URL is set
	 * 
	 * @return boolean
	 */
	public final boolean hasWebappURL()
	{
		return m_webappURL != null ? true : false;
	}
	
	/**
	 * Return the webapp URL
	 * 
	 * @return String
	 */
	public final String getWebappURL()
	{
		return m_webappURL;
	}
	
	/**
	 * Initialize the desktop action
	 * 
	 * @param global ConfigElement
	 * @param config ConfigElement
	 * @param fileSys DiskSharedDevice
	 * @exception DesktopActionException
	 */
	public void initializeAction(ConfigElement global, ConfigElement config, DiskSharedDevice fileSys)
		throws DesktopActionException
	{
        if ( !(fileSys.getContext() instanceof AlfrescoContext))
            throw new DesktopActionException("Desktop action requires an Alfresco filesystem driver");
		// Perform standard initialization
		
		standardInitialize(global, config, fileSys);
		
		AlfrescoDiskDriver driver = (AlfrescoDiskDriver)fileSys.getDiskInterface();
		
        // Complete initialization
        initializeAction(driver.getServiceRegistry(), (AlfrescoContext) fileSys.getDiskContext());       		
	}
	
	/**
	 * Perform standard desktop action initialization
	 * 
	 * @param global ConfigElement
	 * @param config ConfigElement
	 * @param fileSys DiskSharedDevice
	 * @exception DesktopActionException
	 */
	public void standardInitialize(ConfigElement global, ConfigElement config, DiskSharedDevice fileSys)
		throws DesktopActionException
	{
		// Check for standard config values
		ConfigElement elem = config.getChild("name");
		if ( elem != null && elem.getValue().length() > 0)
		{
			// Set the action name
			
			setName(elem.getValue());
		}
		else
			throw new DesktopActionException("Desktop action name not specified");
		
		// Get the pseudo file name
		
		ConfigElement name = config.getChild("filename");
		if ( name == null || name.getValue() == null || name.getValue().length() == 0)
			throw new DesktopActionException("Desktop action pseudo name not specified");
		setFilename(name.getValue());
		
		// Get the local path to the executable
		
		ConfigElement path = findConfigElement("path", global, config);
		if ( path == null || path.getValue() == null || path.getValue().length() == 0)
			throw new DesktopActionException("Desktop action executable path not specified");
		setPath(path.getValue());
		
		// Check if confirmations should be switched off for the action
		
		if ( findConfigElement("noConfirm", global, config) != null && hasPreProcessAction(PreConfirmAction))
			setPreProcessActions(getPreProcessActions() - PreConfirmAction);
		
		// Check if the webapp URL has been specified
		
		ConfigElement webURL = findConfigElement("webpath", global, config);
		if ( webURL != null && webURL.getValue() != null && webURL.getValue().length() > 0)
		{
		    setWebappURL(webURL.getValue());
		}
		
		// Check if debug output is enabled for the action
		
		ConfigElement debug = findConfigElement("debug", global, config);
		if ( debug != null)
			setDebug(true);
	}

	
    /**
     * Initialize the desktop action
     * 
     * @exception DesktopActionException
     */
    public void initializeAction(ServiceRegistry serviceRegistry, AlfrescoContext filesysContext) throws DesktopActionException
    {
        this.serviceRegistry = serviceRegistry;
        
        // Save the filesystem device and I/O handler
        m_filesysContext = filesysContext;
        
        // Check for standard config values
        
        if ( m_name == null || m_name.length() == 0)
            throw new DesktopActionException("Desktop action name not specified");
        
        // Get the pseudo file name
        if (m_pseudoFile == null)
        {            
            if ( m_filename == null || m_filename.length() == 0)
                throw new DesktopActionException("Desktop action pseudo name not specified");
            
    		// Get the local path to the executable
            if (m_path== null || m_path.length() == 0)
            {
                m_path = m_filesysContext.getGlobalDesktopActionConfig().getPath();
            }
            if ( m_path == null || m_path.length() == 0)
                throw new DesktopActionException("Desktop action executable path not specified");
            
            // Check that the application exists on the local filesystem
            
            Resource resource = new ResourceFinder().getResource(m_path);
            if (!resource.exists())
            {
                throw new DesktopActionException("Failed to find drag and drop application, " + m_path);
            }
            
            PseudoFile pseudoFile = null;
            try
            {
                pseudoFile = new MemoryPseudoFile(m_filename, IOUtils.toByteArray(resource.getInputStream()));
            }
            catch (IOException e)
            {
                throw new DesktopActionException("Drag and drop application resource is invalid, " + resource.getDescription());
            }
            
            setPseudoFile(pseudoFile);
        }
        
        // Check if confirmations should be switched off for the action
        
        if ( m_filesysContext.getGlobalDesktopActionConfig().getNoConfirm() && hasPreProcessAction(PreConfirmAction))
            setPreProcessActions(getPreProcessActions() - PreConfirmAction);
        
        // Check if the webapp URL has been specified
        
        SysAdminParams sysAdminParams = m_filesysContext.getSysAdminParams();
        if (m_webappURL == null || m_webappURL.length() == 0)
        {
            m_webappURL = m_filesysContext.getURLPrefix();
        }
        else
        {
            // Check if the path name contains the local name token
            m_webappURL = sysAdminParams.subsituteHost(m_webappURL);

            if ( !m_webappURL.endsWith("/"))
            {
                m_webappURL = m_webappURL + "/";
            }
        }
        
        // Check if debug output is enabled for the action
        
        if ( m_filesysContext.getGlobalDesktopActionConfig().getDebug())
            setDebug(true);
        
        // DEBUG
        
        if ( logger.isDebugEnabled() && hasDebug())
            logger.debug("Initialized desktop action " + getName() + ", pseudo name " + m_pseudoFile.getFileName());
    }

    /**
	 * Find the required configuration element in the local or global config
	 * 
	 * @param name String
	 * @param global ConfigElement
	 * @param local configElement
	 * @return ConfigElement
	 */
	private final ConfigElement findConfigElement(String name, ConfigElement global, ConfigElement local)
	{
		// Check if the required setting is in the local config
		
		ConfigElement elem = local.getChild(name);
		if ( elem == null && global != null)
			elem = global.getChild(name);
		
		return elem;
	}
	
	/**
	 * Run the desktop action
	 * 
	 * @param params DesktopParams
	 * @return DesktopResponse
	 * @exception 
	 */
	public abstract DesktopResponse runAction(DesktopParams params)
		throws DesktopActionException;

    /**
	 * Set the action attributes
	 * 
	 * @param attr int
	 */
	protected final void setAttributes(int attr)
	{
		m_attributes = attr;
	}
	
	/**
	 * Set the client side pre-processing actions
	 * 
	 * @param pre int
	 */
	public final void setPreProcessActions(int pre)
	{
		m_clientPreActions = pre;
	}
	
	/**
	 * Set the action name
	 * 
	 * @param name String
	 */
	public final void setName(String name)
	{
		m_name = name;
	}

	/**
	 * Set the associated pseudo file
	 * 
	 * @param pseudoFile PseudoFile
	 */
	public final void setPseudoFile(PseudoFile pseudoFile)
	{
		m_pseudoFile = pseudoFile;
	}
	
    /**
     * Set the associated pseudo file name
     * 
     * @param filename the file name
     */
    public void setFilename(String filename)
    {
        this.m_filename = filename;
    }

    /**
     * Set the physical path of the associated pseudo file
     * 
     * @param path the path
     */
    public void setPath(String path)
    {
        this.m_path = path;
    }

    /**
	 * Enable debug output
	 *
	 * @param ena boolean
	 */
	public final void setDebug(boolean ena)
	{
		m_debug = ena;
	}

    /**
	 * Set the webapp URL
	 * 
	 * @param urlStr String
	 */
	public final void setWebappURL(String urlStr)
	{
		m_webappURL = urlStr;
	}
	
	/**
	 * Equality check
	 * 
	 * @param obj Object
	 * @return boolean
	 */
	@Override
	public boolean equals(Object obj)
	{
		if ( obj instanceof DesktopAction)
		{
			DesktopAction action = (DesktopAction) obj;
			return action.getName().equals(getName());
		}
		return false;
	}
	
	/**
	 * Return the desktop action details as a string
	 * 
	 * @return String
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		
		str.append("[");
		str.append(getName());
		str.append(":Attr=0x");
		str.append(Integer.toHexString(getAttributes()));
		str.append(":Pre=0x");
		str.append(Integer.toHexString(getPreProcessActions()));
		
		if ( hasPseudoFile())
		{
			str.append(":Pseudo=");
			str.append(getPseudoFile().getFileName());
		}
		str.append("]");
		
		return str.toString();
	}

}
