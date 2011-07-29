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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.filesys.alfresco.AlfrescoContext;
import org.alfresco.filesys.alfresco.DesktopAction;
import org.alfresco.filesys.alfresco.DesktopActionException;
import org.alfresco.filesys.alfresco.DesktopParams;
import org.alfresco.filesys.alfresco.DesktopResponse;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.scripts.ScriptException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ResourceFinder;
import org.springframework.core.io.Resource;
import org.springframework.extensions.config.ConfigElement;

/**
 * Javascript Desktop Action Class
 *
 * <p>Run a server-side script against the target node(s).
 * 
 * @author gkspencer
 */
public class JavaScriptDesktopAction extends DesktopAction {

	// Script name
	
	private String m_scriptName;
	
	// Script file details
	
	private Resource m_scriptResource;
	private Long m_lastModified;
	
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
    public void initializeAction(ServiceRegistry serviceRegistry, AlfrescoContext filesysContext)
            throws DesktopActionException
    {
        // Perform standard initialization

	    super.initializeAction(serviceRegistry, filesysContext);
        
        // Get the script file name and check that it exists
        
        if ( m_scriptName == null || m_scriptName.length() == 0)
        {
            throw new DesktopActionException("Script name not specified");
        }

        // Check if the script exists on the classpath
        m_scriptResource = new ResourceFinder().getResource(m_scriptName);
        if (!m_scriptResource.exists())
        {
            throw new DesktopActionException("Failed to find script on classpath, " + getScriptName());
        }

        // Get the script modification date if it can be resolved to a file
        try
        {
            m_lastModified = m_scriptResource.lastModified();
        }
        catch (IOException e)
        {
            // Don't worry if we can't. Assume it's embedded in a resource.
        }
        
        // Load the script

        try
        {
            loadScript( m_scriptResource);
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
		
        synchronized (this)
        {
            try
            {
                if (m_lastModified != null && m_scriptResource.lastModified() != m_lastModified)
                {
                    // Reload the script if we can

                    m_lastModified = m_scriptResource.lastModified();

                    loadScript(m_scriptResource);
                }
            }
            catch (IOException ex)
            {
                logger.warn("Failed to reload script file, " + m_scriptResource.getDescription(), ex);
            }
        }
			
		// Access the script service
		final ScriptService scriptService = getServiceRegistry().getScriptService();
		
		if ( scriptService != null)
		{
	        // Create the objects to be passed to the script
			
            final Map<String, Object> model = new HashMap<String, Object>();
            model.put("deskParams", params);
            model.put("out", System.out);
			
            // Add the webapp URL, if valid
            
            if ( hasWebappURL())
            {
            	model.put("webURL", getWebappURL());
            }
                
            TransactionService transactionService = this.getServiceRegistry().getTransactionService();
                
            RetryingTransactionHelper tx = transactionService.getRetryingTransactionHelper();

            RetryingTransactionCallback<DesktopResponse> runScriptCB = new RetryingTransactionCallback<DesktopResponse>() {

               @Override
               public DesktopResponse execute() throws Throwable
               {
                    DesktopResponse response = new DesktopResponse(StsSuccess);
                        
                    try
                    {

                        // Run the script

                        Object result = scriptService.executeScriptString(getScript(), model);

                        // Check the result

                        if (result != null)
                        {
                            // Check for a full response object

                            if (result instanceof DesktopResponse)
                            {
                                response = (DesktopResponse) result;
                            }

                            // Status code only response

                            else if (result instanceof Double)
                            {
                                Double jsSts = (Double) result;
                                response.setStatus(jsSts.intValue(), "");
                            }

                            // Encoded response in the format '<stsCode>,<stsMessage>'

                            else if (result instanceof String)
                            {
                                String responseMsg = (String) result;

                                // Parse the status message

                                StringTokenizer token = new StringTokenizer(responseMsg, ",");
                                String stsToken = token.nextToken();
                                String msgToken = token.nextToken();

                                int sts = -1;
                                try
                                {
                                    sts = Integer.parseInt(stsToken);
                                }
                                catch (NumberFormatException ex)
                                {
                                    response.setStatus(StsError, "Bad response from script");
                                }

                                // Set the response

                                response.setStatus(sts, msgToken != null ? msgToken : "");
                            }
                        }
                        // Return the response
    
                        return response;
                    }
                    catch (ScriptException ex)
                    {
                            return new DesktopResponse(StsError, ex.getMessage());
                    }
               }         
           };
                
                
           return tx.doInTransaction(runScriptCB, false, false);
                
                // Compute the response in a retryable write transaction
        }
        else
        {
            // Return an error response, script service not available

            return new DesktopResponse(StsError, "Script service not available");
        }
            
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
    private final void loadScript(Resource scriptResource)
        throws IOException
    {
        // Get resource
        BufferedReader scriptIn = new BufferedReader(new InputStreamReader(scriptResource.getInputStream()));
        StringBuilder scriptStr = new StringBuilder(1024);
        try
        {
            String inRec = scriptIn.readLine();
            while ( inRec != null)
            {
                scriptStr.append( inRec);
                scriptStr.append( "\n");
                inRec = scriptIn.readLine();
            }
        }
        finally
        {
            // Close the script file
            scriptIn.close();
        }
        // Update the script string
        m_script = scriptStr.toString();
    }

}
