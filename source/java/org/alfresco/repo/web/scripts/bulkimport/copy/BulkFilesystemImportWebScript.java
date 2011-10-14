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

package org.alfresco.repo.web.scripts.bulkimport.copy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.bulkimport.impl.MultiThreadedBulkFilesystemImporter;
import org.alfresco.repo.bulkimport.impl.StreamingNodeImporterFactory;
import org.alfresco.repo.web.scripts.bulkimport.AbstractBulkFileSystemImportWebScript;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Web Script class that invokes a BulkFilesystemImporter implementation.
 *
 * @since 4.0
 */
public class BulkFilesystemImportWebScript extends AbstractBulkFileSystemImportWebScript
{
    private MultiThreadedBulkFilesystemImporter bulkImporter;
	private StreamingNodeImporterFactory nodeImporterFactory;

	public void setBulkImporter(MultiThreadedBulkFilesystemImporter bulkImporter)
	{
		this.bulkImporter = bulkImporter;
	}

	public void setNodeImporterFactory(StreamingNodeImporterFactory nodeImporterFactory)
	{
		this.nodeImporterFactory = nodeImporterFactory;
	}

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest request, final Status status, final Cache cache)
    {
        Map<String, Object> model  = new HashMap<String, Object>();
        String targetNodeRefStr = null;
        String targetPath = null;
        String sourceDirectoryStr = null;
        String replaceExistingStr = null;
        String batchSizeStr = null;
        String numThreadsStr = null;
        String disableRulesStr = null;

        cache.setNeverCache(true);
        
        try
        {
            if(!bulkImporter.getStatus().inProgress())
            {
                NodeRef targetNodeRef = null;
                File sourceDirectory = null;
                boolean replaceExisting = false;
                int batchSize = bulkImporter.getDefaultBatchSize();
                int numThreads = bulkImporter.getDefaultNumThreads();
                boolean disableRules = false;
                
                // Retrieve, validate and convert parameters
                targetNodeRefStr = request.getParameter(PARAMETER_TARGET_NODEREF);
                targetPath = request.getParameter(PARAMETER_TARGET_PATH);
                sourceDirectoryStr = request.getParameter(PARAMETER_SOURCE_DIRECTORY);
                replaceExistingStr = request.getParameter(PARAMETER_REPLACE_EXISTING);
                batchSizeStr = request.getParameter(PARAMETER_BATCH_SIZE);
                numThreadsStr = request.getParameter(PARAMETER_NUM_THREADS);
                disableRulesStr = request.getParameter(PARAMETER_DISABLE_RULES);

                targetNodeRef = getTargetNodeRef(targetNodeRefStr, targetPath);
                
                if (sourceDirectoryStr == null || sourceDirectoryStr.trim().length() == 0)
                {
                    throw new RuntimeException("Error: mandatory parameter '" + PARAMETER_SOURCE_DIRECTORY + "' was not provided.");
                }
                
                sourceDirectory = new File(sourceDirectoryStr.trim());
                
                if (replaceExistingStr != null && replaceExistingStr.trim().length() > 0)
                {
                    replaceExisting = PARAMETER_VALUE_REPLACE_EXISTING.equals(replaceExistingStr);
                }

                if (disableRulesStr != null && disableRulesStr.trim().length() > 0)
                {
                    disableRules = PARAMETER_VALUE_DISABLE_RULES.equals(disableRulesStr);
                }

                // Initiate the import
                NodeImporter nodeImporter = nodeImporterFactory.getNodeImporter(sourceDirectory);
                BulkImportParameters bulkImportParameters = new BulkImportParameters();
                
                if (numThreadsStr != null && numThreadsStr.trim().length() > 0)
                {
                	try
                	{
                		numThreads = Integer.parseInt(numThreadsStr);
                		if(numThreads < 1)
                		{
                            throw new RuntimeException("Error: parameter '" + PARAMETER_NUM_THREADS + "' must be an integer > 0.");
                		}
                        bulkImportParameters.setNumThreads(numThreads);
                	}
                	catch(NumberFormatException e)
                	{
                        throw new RuntimeException("Error: parameter '" + PARAMETER_NUM_THREADS + "' must be an integer > 0.");
                	}
                }
                
                if (batchSizeStr != null && batchSizeStr.trim().length() > 0)
                {
                	try
                	{
                		batchSize = Integer.parseInt(batchSizeStr);
                		if(batchSize < 1)
                		{
                            throw new RuntimeException("Error: parameter '" + PARAMETER_BATCH_SIZE + "' must be an integer > 0.");
                		}
                        bulkImportParameters.setBatchSize(batchSize);
                	}
                	catch(NumberFormatException e)
                	{
                        throw new RuntimeException("Error: parameter '" + PARAMETER_BATCH_SIZE + "' must be an integer > 0.");
                	}
                }

                bulkImportParameters.setReplaceExisting(replaceExisting);
                bulkImportParameters.setTarget(targetNodeRef);
                bulkImportParameters.setDisableRulesService(disableRules);

                bulkImporter.asyncBulkImport(bulkImportParameters, nodeImporter);

                // redirect to the status Web Script
                status.setCode(Status.STATUS_MOVED_TEMPORARILY);
                status.setRedirect(true);
                status.setLocation(request.getServiceContextPath() + WEB_SCRIPT_URI_BULK_FILESYSTEM_IMPORT_STATUS);
            }
            else
            {
            	model.put(IMPORT_ALREADY_IN_PROGRESS_MODEL_KEY, I18NUtil.getMessage(IMPORT_ALREADY_IN_PROGRESS_ERROR_KEY));
            }
        }
        catch (WebScriptException wse)
        {
        	status.setCode(Status.STATUS_BAD_REQUEST, wse.getMessage());
        	status.setRedirect(true);
        }
        catch (FileNotFoundException fnfe)
        {
        	status.setCode(Status.STATUS_BAD_REQUEST,"The repository path '" + targetPath + "' does not exist !");
        	status.setRedirect(true);
        }
        catch(IllegalArgumentException iae)
        {
        	status.setCode(Status.STATUS_BAD_REQUEST,iae.getMessage());
        	status.setRedirect(true);
        }
        catch (Throwable t)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, buildTextMessage(t), t);
        }
        
        return model;
    }

}
