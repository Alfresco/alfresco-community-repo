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
