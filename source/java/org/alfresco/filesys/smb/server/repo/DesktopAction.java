/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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

package org.alfresco.filesys.smb.server.repo;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.filesys.DiskSharedDevice;
import org.alfresco.filesys.smb.server.repo.pseudo.LocalPseudoFile;
import org.alfresco.filesys.smb.server.repo.pseudo.PseudoFile;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Desktop Action Class
 *
 * @author gkspencer
 *
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
    
	// Action name
	
	private String m_name;
	
	// Pseudo file details
	
	private PseudoFile m_pseudoFile;
	
	// Desktop action attributes
	
	private int m_attributes;
	
	// Desktop action client side pre-processing control
	
	private int m_clientPreActions;
	
	// Filesystem driver and context
	
	private ContentDiskDriver m_contentDriver;
	private ContentContext m_contentContext;

	// Debug enable flag
	
	private boolean m_debug;
	
	/**
	 * Default constructor
	 */
	public DesktopAction()
	{
	}
	
	/**
	 * Class constructor
	 * 
	 * @param attr int
	 * @param preActions int
	 */
	public DesktopAction(int attr, int preActions)
	{
		setAttributes(attr);
		setPreProcessActions(preActions);
	}
	
	/**
	 * Class constructor
	 *
	 * @param name String
	 */
	public DesktopAction(String name)
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

	/**
	 * Return the content filesystem driver
	 * 
	 * @return ContentDiskDriver
	 */
	public final ContentDiskDriver getDriver()
	{
		return m_contentDriver;
	}
	
	/**
	 * Return the filesystem context
	 * 
	 * @return ContentContext
	 */
	public final ContentContext getContext()
	{
		return m_contentContext;
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
		// Perform standard initialization
		
		standardInitialize(global, config, fileSys);
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
		// Save the filesystem device and I/O handler
		
		if ( fileSys.getDiskInterface() instanceof ContentDiskDriver)
		{
			m_contentDriver = (ContentDiskDriver) fileSys.getDiskInterface();
			m_contentContext = (ContentContext) fileSys.getDiskContext();
		}
		else
			throw new DesktopActionException("Desktop action requires content filesystem driver");
		
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
		
		// Get the local path to the executable
		
		ConfigElement path = findConfigElement("path", global, config);
		if ( path == null || path.getValue() == null || path.getValue().length() == 0)
			throw new DesktopActionException("Desktop action executable path not specified");
		
        // Check that the application exists on the local filesystem
        
        URL appURL = this.getClass().getClassLoader().getResource(path.getValue());
        if ( appURL == null)
            throw new DesktopActionException("Failed to find drag and drop application, " + path.getValue());
        
        // Decode the URL path, it might contain escaped characters
        
        String appURLPath = null;
        try
        {
        	appURLPath = URLDecoder.decode( appURL.getFile(), "UTF-8");
        }
        catch ( UnsupportedEncodingException ex)
        {
        	throw new DesktopActionException("Failed to decode drag/drop path, " + ex.getMessage());
        }

        // Check that the drag/drop file exists
        
        File appFile = new File(appURLPath);
        if ( appFile.exists() == false)
            throw new DesktopActionException("Drag and drop application not found, " + path.getValue());
        
		// Create the pseudo file for the action
		
		PseudoFile pseudoFile = new LocalPseudoFile(name.getValue(), appFile.getAbsolutePath());
		setPseudoFile(pseudoFile);
		
		// Check if confirmations should be switched off for the action
		
		if ( findConfigElement("noConfirm", global, config) != null && hasPreProcessAction(PreConfirmAction))
			setPreProcessActions(getPreProcessActions() - PreConfirmAction);
			
		// Check if debug output is enabled for the action
		
		ConfigElement debug = findConfigElement("debug", global, config);
		if ( debug != null)
			setDebug(true);
		
		// DEBUG
		
		if ( logger.isDebugEnabled() && hasDebug())
			logger.debug("Initialized desktop action " + getName() + ", pseudo name " + name.getValue());
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
     * Return the CIFS helper
     * 
     * @return CifsHelper
     */
    protected final CifsHelper getCifsHelper()
    {
    	return m_contentDriver.getCifsHelper();
    }
    
    /**
     * Return the transaction service
     * 
     * @return TransactionService
     */
    protected final TransactionService getTransactionService()
    {
    	return m_contentDriver.getTransactionService();
    }
    
    /**
     * Return the node service
     * 
     * @return NodeService
     */
    protected final NodeService getNodeService()
    {
    	return m_contentDriver.getNodeService();
    }

    
    /**
     * Return the content service
     * 
     * @return ContentService
     */
    public final ContentService getContentService()
    {
    	return m_contentDriver.getContentService();
    }

    /**
     * Return the namespace service
     * 
     * @return NamespaceService
     */
    public final NamespaceService getNamespaceService()
    {
    	return m_contentDriver.getNamespaceService();
    }
    
    /**
     * Return the search service
     * 
     * @return SearchService
     */
    public final SearchService getSearchService()
    {
    	return m_contentDriver.getSearchService();
    }

    /**
     * Return the check in/out service
     * 
     * @return CheckOutInService
     */
    public final CheckOutCheckInService getCheckInOutService()
    {
    	return m_contentDriver.getCheckInOutService();
    }
    
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
	protected final void setPreProcessActions(int pre)
	{
		m_clientPreActions = pre;
	}
	
	/**
	 * Set the action name
	 * 
	 * @param name String
	 */
	protected final void setName(String name)
	{
		m_name = name;
	}

	/**
	 * Set the associated pseudo file
	 * 
	 * @param pseudoFile PseudoFile
	 */
	protected final void setPseudoFile(PseudoFile pseudoFile)
	{
		m_pseudoFile = pseudoFile;
	}
	
	/**
	 * Enable debug output
	 *
	 * @param ena boolean
	 */
	protected final void setDebug(boolean ena)
	{
		m_debug = ena;
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
