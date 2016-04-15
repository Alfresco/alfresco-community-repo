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
package org.alfresco.repo.bulkimport.impl;

import java.io.File;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.repo.bulkimport.NodeImporter;

/**
 * A multi threaded bulk importer that imports by striping across filesystem levels.
 * 
 * @since 4.0
 *
 */
public class StripingBulkFilesystemImporter extends MultiThreadedBulkFilesystemImporter
{
	/**
     * Method that does the work of importing a filesystem using the BatchProcessor.
     * 
     * @param bulkImportParameters  The bulk import parameters to apply to this bulk import.
     * @param nodeImporter          The node importer implementation that will import each node.
     * @param lockToken             The lock token to use during the bulk import.
     */
    @Override
    protected void bulkImportImpl(final BulkImportParameters bulkImportParameters, final NodeImporter nodeImporter, final String lockToken)
    {
        super.bulkImportImpl(bulkImportParameters, nodeImporter, lockToken);

    	final File sourceFolder = nodeImporter.getSourceFolder();
        final int batchSize = getBatchSize(bulkImportParameters);
        final int loggingInterval = getLoggingInterval(bulkImportParameters);
    	final StripingFilesystemTracker tracker = new StripingFilesystemTracker(directoryAnalyser, bulkImportParameters.getTarget(), sourceFolder, batchSize);
        final BatchProcessor<ImportableItem> batchProcessor = getBatchProcessor(bulkImportParameters, tracker.getWorkProvider(), loggingInterval);
        final BatchProcessor.BatchProcessWorker<ImportableItem> worker = getWorker(bulkImportParameters, lockToken, nodeImporter, tracker);

		do
		{
			batchProcessor.process(worker, true);
			if(batchProcessor.getLastError() != null)
			{
				throw new AlfrescoRuntimeException(batchProcessor.getLastError());
			}
		}
		while(tracker.moreLevels());
    }
}
