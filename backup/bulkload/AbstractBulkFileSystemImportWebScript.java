/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.web.scripts.node.bulkload;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * contains common fields and methods for the import web scripts.
 */
public class AbstractBulkFileSystemImportWebScript extends DeclarativeWebScript
{
    protected static final String WEB_SCRIPT_URI_BULK_FILESYSTEM_IMPORT_STATUS = "/bulk/import/filesystem/status";
    
	protected static final String PARAMETER_TARGET_NODEREF = "targetNodeRef";
	protected static final String PARAMETER_TARGET_PATH    = "targetPath";

	protected static final String COMPANY_HOME_NAME = "Company Home";
	protected static final String COMPANY_HOME_PATH = "/" + COMPANY_HOME_NAME;
    
    // Web scripts parameters (common)
	protected static final String PARAMETER_REPLACE_EXISTING        = "replaceExisting";
	protected static final String PARAMETER_VALUE_REPLACE_EXISTING 	= "replaceExisting";
	protected static final String PARAMETER_SOURCE_DIRECTORY       	= "sourceDirectory";
	
	protected static final String IMPORT_ALREADY_IN_PROGRESS_MODEL_KEY = "importInProgress";
	protected static final String IMPORT_ALREADY_IN_PROGRESS_ERROR_KEY ="bfsit.error.importAlreadyInProgress";
	
	protected FileFolderService fileFolderService;
	protected Repository repository;
	   
	protected volatile boolean importInProgress;
    
	protected NodeRef getTargetNodeRef(String targetNodeRefStr, String targetPath) throws FileNotFoundException
	{
		NodeRef targetNodeRef;
		
		if (targetNodeRefStr == null || targetNodeRefStr.trim().length() == 0)
        {
            if (targetPath == null || targetPath.trim().length() == 0)
            {
                throw new WebScriptException("Error: neither parameter '" + PARAMETER_TARGET_NODEREF +
                                           "' nor parameter '" + PARAMETER_TARGET_PATH +
                                           "' was provided, but at least one is required !");
            }
    		targetNodeRef = convertPathToNodeRef(targetPath.trim());
        }
        else
            targetNodeRef = new NodeRef(targetNodeRefStr.trim());
		
		return targetNodeRef;
	}

	protected NodeRef convertPathToNodeRef(String targetPath) throws FileNotFoundException
	{
	    NodeRef result          = null;
	    NodeRef companyHome     = repository.getCompanyHome();
	    String  cleanTargetPath = targetPath.replaceAll("/+", "/");
		
	    if (cleanTargetPath.startsWith(COMPANY_HOME_PATH))
	        cleanTargetPath = cleanTargetPath.substring(COMPANY_HOME_PATH.length());
	    
	    if (cleanTargetPath.startsWith("/"))
	        cleanTargetPath = cleanTargetPath.substring(1);
	    
	    if (cleanTargetPath.endsWith("/"))
	        cleanTargetPath = cleanTargetPath.substring(0, cleanTargetPath.length() - 1);
	    
	    if (cleanTargetPath.length() == 0)
	        result = companyHome;
	    else
	    {
	    	FileInfo info = fileFolderService.resolveNamePath(companyHome, Arrays.asList(cleanTargetPath.split("/")));
	        if(info == null)
	        	throw new WebScriptException("could not determine NodeRef for path :'"+cleanTargetPath+"'");
	        
	        result = info.getNodeRef();
	    }
	    
	    return(result);
	}

	protected String buildTextMessage(Throwable t)
	{
	    StringBuffer result        = new StringBuffer();
	    String       timeOfFailure = (new Date()).toString();
	    String       hostName      = null;
	    String       ipAddress     = null;
	
	    try
	    {
	        hostName  = InetAddress.getLocalHost().getHostName();
	        ipAddress = InetAddress.getLocalHost().getHostAddress();
	    }
	    catch (UnknownHostException uhe)
	    {
	        hostName  = "unknown";
	        ipAddress = "unknown";
	    }
	
	    result.append("\nTime of failure:             " + timeOfFailure);
	    result.append("\nHost where failure occurred: " + hostName + " (" + ipAddress + ")");
	    
	    if (t != null)
	    {
	        result.append("\nRoot exception:");
	        result.append(renderExceptionStackAsText(t));
	    }
	    else
	    {
	        result.append("\nNo exception was provided.");
	    }
	
	    return(result.toString());
	}

	private String renderExceptionStackAsText( Throwable t)
	{
	    StringBuffer result = new StringBuffer();
	
	    if (t != null)
	    {
	        String    message = t.getMessage();
	        Throwable cause   = t.getCause();
	
	        if (cause != null)
	        {
	            result.append(renderExceptionStackAsText(cause));
	            result.append("\nWrapped by:");
	        }
	
	        if (message == null)
	        {
	            message = "";
	        }
	
	        result.append("\n");
	        result.append(t.getClass().getName());
	        result.append(": ");
	        result.append(message);
	        result.append("\n");
	        result.append(renderStackTraceElements(t.getStackTrace()));
	    }
	
	    return(result.toString());
	}

	private String renderStackTraceElements(StackTraceElement[] elements)
	{
	    StringBuffer result = new StringBuffer();
	
	    if (elements != null)
	    {
	        for (int i = 0; i < elements.length; i++)
	        {
	            result.append("\tat " + elements[i].toString() + "\n");
	        }
	    }
	
	    return(result.toString());
	}
	
	// boilerplate setters

	public void setFileFolderService(FileFolderService fileFolderService)
	{
		this.fileFolderService = fileFolderService;
	}

	public void setRepository(Repository repository)
	{
		this.repository = repository;
	}

}