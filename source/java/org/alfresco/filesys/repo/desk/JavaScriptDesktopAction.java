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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys.repo.desk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.springframework.extensions.config.ConfigElement;
import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.AlfrescoDiskDriver;
import org.alfresco.filesys.alfresco.DesktopAction;
import org.alfresco.filesys.alfresco.DesktopActionException;
import org.alfresco.filesys.alfresco.DesktopParams;
import org.alfresco.filesys.alfresco.DesktopResponse;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.scripts.ScriptException;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.util.ResourceFinder;
import org.springframework.core.io.Resource;

/**
 * Javascript Desktop Action Class
 *
 * <p>Run a server-side script against the target node(s).
 * 
 * @author gkspencer
 */
public class JavaScriptDesktopAction extends DesktopAction {

	// Script service
	
	private ScriptService m_scriptService;
	
	// Script name
	
	private String m_scriptName;
	
	// Script file details
	
	private String m_scriptPath;
	private long m_lastModified;
	
	// Script string
	
	private String m_script;
	
	/**
	 * Class constructor
	 */
	public JavaScriptDesktopAction()
	{
		super( 0, 0);
	}

	/**
	 * Return the confirmation string to be displayed by the client
	 * 
	 * @return String
	 */
	@Override
	public String getConfirmationString()
	{
		return "Run Javascript action";
	}

    /**
     * Perform standard desktop action initialization
     * 
     * @param global ConfigElement
     * @param config ConfigElement
     * @param fileSys DiskSharedDevice
     * @exception DesktopActionException
     */
    @Override
    public void standardInitialize(ConfigElement global, ConfigElement config, DiskSharedDevice fileSys)
        throws DesktopActionException
    {
		// Perform standard initialization
		super.standardInitialize(global, config, fileSys);
		
		// Get the script file name and check that it exists
		
		ConfigElement elem = config.getChild("script");
		if ( elem != null && elem.getValue().length() > 0)
		{
			// Set the script name
			setScriptName(elem.getValue());
		}
		else
			throw new DesktopActionException("Script name not specified");
		
		// check if the desktop action attributes have been specified
		
		elem = config.getChild("attributes");
		if ( elem != null)
		{
			// Check if the attribute string is empty
			
			if ( elem.getValue().length() == 0)
				throw new DesktopActionException("Empty desktop action attributes");
			
			// Parse the attribute string
			setAttributeList(elem.getValue());
		}
		
		// Check if the desktop action pre-processing options have been specified
		
		elem = config.getChild("preprocess");
		if ( elem != null)
		{
		    setPreprocess(elem.getValue());
		}
	}

	@Override
    public void initializeAction(AlfrescoDiskDriver filesysDriver, AlfrescoContext filesysContext)
            throws DesktopActionException
    {
        // Perform standard initialization

	    super.initializeAction(filesysDriver, filesysContext);
        
        // Get the script file name and check that it exists
        
        if ( m_scriptName == null || m_scriptName.length() == 0)
        {
            throw new DesktopActionException("Script name not specified");
        }

        // Check if the script exists on the classpath
        Resource resource = new ResourceFinder().getResource(m_scriptName);
        if (!resource.exists())
        {
            throw new DesktopActionException("Failed to find script on classpath, " + getScriptName());
        }


        // Check that the script file exists
        File scriptFile;
        try
        {
            scriptFile = resource.getFile();
            if (scriptFile.exists() == false)
                throw new DesktopActionException("Script file not found, " + m_scriptName);
        }
        catch (IOException e)
        {
            throw new DesktopActionException("Unable to resolve script as a file, " + resource.getDescription());
        }
        
        m_scriptPath = scriptFile.getAbsolutePath();
        m_lastModified =scriptFile.lastModified();
        
        // Load the script

        try
        {
            loadScript( scriptFile);
        }
        catch ( IOException ex)
        {
            throw new DesktopActionException( "Failed to load script, " + ex.getMessage());
        }
    }

    /**
	 * Run the desktop action
	 * 
	 * @param params DesktopParams
	 * @return DesktopResponse 
	 */
	@Override
	public DesktopResponse runAction(DesktopParams params)
		throws DesktopActionException
	{
		// Check if the script file has been changed
		
		DesktopResponse response = new DesktopResponse(StsSuccess);
		
		File scriptFile = new File(m_scriptPath);
		if ( scriptFile.lastModified() != m_lastModified)
		{
			// Reload the script

			m_lastModified = scriptFile.lastModified();
			
			try
			{
				loadScript( scriptFile);
			}
			catch ( IOException ex)
			{
				response.setStatus(StsError, "Failed to reload script file, " + getScriptName());
				return response;
			}
		}
			
		// Start a transaction
		
		params.getDriver().beginWriteTransaction( params.getSession());

		// Access the script service
		
		if ( getScriptService() != null)
		{
			// Create the objects to be passed to the script
			
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("deskParams", params);
            model.put("out", System.out);
			
            // Add the webapp URL, if valid
            
            if ( hasWebappURL())
            	model.put("webURL", getWebappURL());
            
            // Start a transaction
            
    		params.getDriver().beginWriteTransaction( params.getSession());

    		// Run the script
    		
    		Object result = null;
    		
    		try
    		{
    			// Run the script
    			
    			result = getScriptService().executeScriptString( getScript(), model);
    			
    			// Check the result
    			
    			if ( result != null)
    			{
    				// Check for a full response object
    				
    				if ( result instanceof DesktopResponse)
    				{
    					response = (DesktopResponse) result;
    				}
    				
    				// Status code only response
    				
    				else if ( result instanceof Double)
    				{
    					Double jsSts = (Double) result;
    					response.setStatus( jsSts.intValue(), "");
    				}
    				
    				// Encoded response in the format '<stsCode>,<stsMessage>'
    				
    				else if ( result instanceof String)
    				{
    					String responseMsg = (String) result;
    					
    					// Parse the status message
    					
    					StringTokenizer token = new StringTokenizer( responseMsg, ",");
    					String stsToken = token.nextToken();
    					String msgToken = token.nextToken();
    					
    					int sts = -1;
    					try
    					{
    						sts = Integer.parseInt( stsToken);
    					}
    					catch ( NumberFormatException ex)
    					{
    						response.setStatus( StsError, "Bad response from script");
    					}
    					
    					// Set the response
    					
    					response.setStatus( sts, msgToken != null ? msgToken : "");
    				}
    			}
    		}
    		catch (ScriptException ex)
    		{
    			// Set the error response for the client
    			
    			response.setStatus( StsError, ex.getMessage());
    		}
		}
		else
		{
			// Return an error response, script service not available
			
			response.setStatus( StsError, "Script service not available");
		}
		
		// Return the response
		
		return response;
	}
	
	/**
	 * Get the script service
	 * 
	 * @return ScriptService
	 */
	protected final ScriptService getScriptService()
	{
		// Check if the script service has been initialized
		
		if ( m_scriptService == null)
		{
			// Get the script service
			
			m_scriptService = getServiceRegistry().getScriptService();
		}
		
		// Return the script service
		
		return m_scriptService;
	}
	
	/**
	 * Get the script name
	 * 
	 * @return String
	 */
	public final String getScriptName()
	{
		return m_scriptName;
	}

	/**
	 * Return the script data
	 * 
	 * @return String
	 */
	public final String getScript()
	{
		return m_script;
	}
	
	/**
	 * Set the script name
	 * 
	 * @param name String
	 */
	public final void setScriptName(String name)
	{
		m_scriptName = name;
	}
	
    /**
     * Set the action attributes
     * 
     * @param attributes String
     * @throws DesktopActionException 
     */
    public void setAttributeList(String attributes) throws DesktopActionException
    {
        // Check if the attribute string is empty        
        if ( attributes == null || attributes.length() == 0)
        {
            return;
        }
        // Parse the attribute string
        
        int attr = 0;
        StringTokenizer tokens = new StringTokenizer( attributes, ",");
        
        while ( tokens.hasMoreTokens())
        {
            // Get the current attribute token and validate
            
            String token = tokens.nextToken().trim();
            
            if ( token.equalsIgnoreCase( "targetFiles"))
                attr |= AttrTargetFiles;
            else if ( token.equalsIgnoreCase( "targetFolders"))
                attr |= AttrTargetFolders;
            else if ( token.equalsIgnoreCase( "clientFiles"))
                attr |= AttrClientFiles;
            else if ( token.equalsIgnoreCase( "clientFolders"))
                attr |= AttrClientFolders;
            else if ( token.equalsIgnoreCase( "alfrescoFiles"))
                attr |= AttrAlfrescoFiles;
            else if ( token.equalsIgnoreCase( "alfrescoFolders"))
                attr |= AttrAlfrescoFolders;
            else if ( token.equalsIgnoreCase( "multiplePaths"))
                attr |= AttrMultiplePaths;
            else if ( token.equalsIgnoreCase( "allowNoParams"))
                attr |= AttrAllowNoParams;
            else if ( token.equalsIgnoreCase( "anyFiles"))
                attr |= AttrAnyFiles;
            else if ( token.equalsIgnoreCase( "anyFolders"))
                attr |= AttrAnyFolders;
            else if ( token.equalsIgnoreCase( "anyFilesFolders"))
                attr |= AttrAnyFilesFolders;
            else
                throw new DesktopActionException("Unknown attribute, " + token);
        }
        setAttributes(attr);
    }
	
    /**
     * Set the client side pre-processing actions
     *
     * @param preProcessActions String
     * @throws DesktopActionException 
     */
    public void setPreprocess(String preProcessActions) throws DesktopActionException
    {
        // Check if the pre-process string is empty

        if ( preProcessActions == null || preProcessActions.length() == 0)
        {
            return;
        }
        
        int pre = 0;
        
        // Parse the pre-process string
        
        StringTokenizer tokens = new StringTokenizer( preProcessActions, ",");
        
        while ( tokens.hasMoreTokens())
        {
            // Get the current pre-process token and validate
            
            String token = tokens.nextToken().trim();
            
            if ( token.equalsIgnoreCase( "copyToTarget"))
                pre |= PreCopyToTarget;
            else if ( token.equalsIgnoreCase( "confirm"))
                pre |= PreConfirmAction;
            else if ( token.equalsIgnoreCase( "localToWorkingCopy"))
                pre |= PreLocalToWorkingCopy;
            else
                throw new DesktopActionException("Unknown pre-processing flag, " + token);
        }
        
        // Set the action pre-processing flags
        
        setPreProcessActions( pre);
    }

    /**
	 * Load, or reload, the script
	 * 
	 * @param scriptFile File
	 */
	private final void loadScript(File scriptFile)
		throws IOException
	{
		// Open the script file
		
		BufferedReader scriptIn = new BufferedReader(new FileReader( scriptFile));
		StringBuilder scriptStr = new StringBuilder((int) scriptFile.length() + 256);
		
		String inRec = scriptIn.readLine();
		
		while ( inRec != null)
		{
			scriptStr.append( inRec);
			scriptStr.append( "\n");
			inRec = scriptIn.readLine();
		}
		
		// Close the script file
		
		scriptIn.close();
		
		// Update the script string
		
		m_script = scriptStr.toString();
	}
}
