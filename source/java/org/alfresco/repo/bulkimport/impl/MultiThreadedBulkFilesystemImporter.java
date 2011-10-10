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
package org.alfresco.repo.bulkimport.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.bulkimport.BulkFilesystemImporter;
import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.FilesystemTracker;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Performs a filesystem import into the repository using the {@link BatchProcessor} in multiple threads.
 * 
 * @since 4.0
 *
 */
public abstract class MultiThreadedBulkFilesystemImporter extends AbstractBulkFilesystemImporter
{
	protected final static Log logger = LogFactory.getLog(BulkFilesystemImporter.class);

    protected int defaultBatchSize;
    protected int defaultNumThreads;
    protected int defaultLoggingInterval = 100;

    protected int getLoggingInterval(BulkImportParameters bulkImportParameters)
    {
        return bulkImportParameters.getLoggingInterval() != null ? bulkImportParameters.getLoggingInterval() : defaultLoggingInterval;    	
    }
    
    protected int getBatchSize(BulkImportParameters bulkImportParameters)
    {
        return bulkImportParameters.getBatchSize() != null ? bulkImportParameters.getBatchSize() : defaultBatchSize;    	
    }
    
    protected int getNumThreads(BulkImportParameters bulkImportParameters)
    {
        return bulkImportParameters.getNumThreads() != null ? bulkImportParameters.getNumThreads() : defaultNumThreads;    	
    }

    protected BatchProcessor.BatchProcessWorker<ImportableItem> getWorker(final BulkImportParameters bulkImportParameters, final String lockToken,
    		final NodeImporter nodeImporter, final FilesystemTracker filesystemTracker)
    {
        final int batchSize = bulkImportParameters.getBatchSize() != null ? bulkImportParameters.getBatchSize() : defaultBatchSize;

        BatchProcessor.BatchProcessWorker<ImportableItem> worker = new BatchProcessor.BatchProcessWorker<ImportableItem>()
        {
            public String getIdentifier(ImportableItem importableItem)
            {
                return importableItem.toString();
            }

            public void beforeProcess() throws Throwable
            {
                refreshLock(lockToken, batchSize * 250L);
                // TODO this throws exception txn not started??
                // Disable the auditable aspect's behaviours for this transaction only, to allow creation & modification dates to be set 
            	//behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
            }

            public void afterProcess() throws Throwable
            {
            	importStatus.incrementNumberOfBatchesCompleted();
        		//behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            }
            
            public void process(final ImportableItem importableItem) throws Throwable
            {
            	try
            	{
	            	behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
	            	NodeRef nodeRef = nodeImporter.importImportableItem(importableItem, bulkImportParameters.isReplaceExisting());
	            	filesystemTracker.itemImported(nodeRef, importableItem);
//	            	importableItem.setNodeRef(nodeRef);
            	}
            	finally
            	{
            		behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            	}
            }
        };

        return worker;
    }

    protected BatchProcessor<ImportableItem> getBatchProcessor(final BulkImportParameters bulkImportParameters,
    		final BatchProcessWorkProvider<ImportableItem> workProvider, final int loggingInterval)
    {
        final int numThreads = getNumThreads(bulkImportParameters);
        final int batchSize = getBatchSize(bulkImportParameters);

        importStatus.setNumThreads(numThreads);
        importStatus.setBatchSize(batchSize);

		BatchProcessor<ImportableItem> batchProcessor = new BatchProcessor<ImportableItem>(
                "Bulk Filesystem Import",
                transactionHelper,
                workProvider,
                numThreads, batchSize,
                applicationContext,
                logger, loggingInterval);
		
		return batchProcessor;
    }

	public void setDefaultNumThreads(int defaultNumThreads)
	{
		this.defaultNumThreads = defaultNumThreads;
	}

	public void setDefaultBatchSize(int defaultBatchSize)
	{
		this.defaultBatchSize = defaultBatchSize;
	}

	public int getDefaultNumThreads()
	{
		return defaultNumThreads;
	}

	public int getDefaultBatchSize()
	{
		return defaultBatchSize;
	}

}
