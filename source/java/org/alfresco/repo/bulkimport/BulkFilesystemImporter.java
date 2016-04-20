
package org.alfresco.repo.bulkimport;


/**
 * Interface defining a bulk filesystem importer.
 *
 * @since 4.0
 */
public interface BulkFilesystemImporter
{
    /**
     * Initiates a bulk filesystem import.
     * Note: getStatus().inProgress() must be false prior to calling this method or an Exception will be thrown.
     * 
     * @param bulkImportParameters         The target bulk import parameters.
     * @param nodeImporter         The node importer.
     */
    void bulkImport(BulkImportParameters bulkImportParameters, NodeImporter nodeImporter);

    /**
     * Initiates a bulk filesystem import asynchronously i.e. in a background thread.
     * Note: getStatus().inProgress() must be false prior to calling this method or an Exception will be thrown.
     *
     * @param bulkImportParameters         The target bulk import parameters.
     * @param nodeImporter         The node importer.
     */
    void asyncBulkImport(BulkImportParameters bulkImportParameters, NodeImporter nodeImporter);
    
    /**
     * @return A status object that describes the current state of the bulk filesystem importer.
     */
    BulkImportStatus getStatus();
}
