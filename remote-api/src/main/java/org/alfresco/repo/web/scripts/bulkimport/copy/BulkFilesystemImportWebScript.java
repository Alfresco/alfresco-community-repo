/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.repo.web.scripts.bulkimport.copy;

import java.io.File;
import java.util.Map;

import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.bulkimport.impl.MultiThreadedBulkFilesystemImporter;
import org.alfresco.repo.bulkimport.impl.StreamingNodeImporterFactory;
import org.alfresco.repo.web.scripts.bulkimport.AbstractBulkFileSystemImportWebScript;
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
        final MultithreadedImportWebScriptLogic importLogic = new MultithreadedImportWebScriptLogic(bulkImporter,
                () -> createNodeImporter(request), request, status, cache);
        return importLogic.executeImport();
    }

    private NodeImporter createNodeImporter(WebScriptRequest request)
    {
        final String sourceDirectoryStr = request.getParameter(PARAMETER_SOURCE_DIRECTORY);
        if (sourceDirectoryStr == null || sourceDirectoryStr.trim().length() == 0)
        {
            throw new WebScriptException("Error: mandatory parameter '" + PARAMETER_SOURCE_DIRECTORY + "' was not provided.");
        }

        final File sourceDirectory = new File(sourceDirectoryStr.trim());

        return nodeImporterFactory.getNodeImporter(sourceDirectory);
    }

}
