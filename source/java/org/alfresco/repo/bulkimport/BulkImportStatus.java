
package org.alfresco.repo.bulkimport;

import java.util.Date;

/**
 * Interface defining which information can be obtained from the Bulk Filesystem Import engine.
 *
 * @since 4.0
 */
public interface BulkImportStatus
{
    // General information
    boolean inProgress();

    int getNumThreads();
    int getBatchSize();

    String getSourceDirectory();
    String getTargetSpace();
    
    Date getStartDate();
    Date getEndDate();
    
    long   getNumberOfBatchesCompleted();

    Long      getDurationInNs();  // Note: java.lang.Long, _not_ primitive long - may be null
    Throwable getLastException();
    String    getLastExceptionAsString();
    

    // Read-side information
    long getNumberOfFoldersScanned();
    long getNumberOfFilesScanned();
    long getNumberOfUnreadableEntries();

    long getNumberOfContentFilesRead();
    long getNumberOfContentBytesRead();
    
    long getNumberOfMetadataFilesRead();
    long getNumberOfMetadataBytesRead();
    
    long getNumberOfContentVersionFilesRead();
    long getNumberOfContentVersionBytesRead();
    
    long getNumberOfMetadataVersionFilesRead();
    long getNumberOfMetadataVersionBytesRead();
    
    // Write-side information
    long getNumberOfSpaceNodesCreated();
    long getNumberOfSpaceNodesReplaced();
    long getNumberOfSpaceNodesSkipped();
    long getNumberOfSpacePropertiesWritten();
    
    long getNumberOfContentNodesCreated();
    long getNumberOfContentNodesReplaced();
    long getNumberOfContentNodesSkipped();
    long getNumberOfContentBytesWritten();
    long getNumberOfContentPropertiesWritten();
    
    long getNumberOfContentVersionsCreated();
    long getNumberOfContentVersionBytesWritten();
    long getNumberOfContentVersionPropertiesWritten();
    
    // Throughput
    public Long getFilesReadPerSecond();
    public Long getBytesReadPerSecond();
    public Long getEntriesScannedPerSecond();
    public Long getBytesWrittenPerSecond();
    public Long getNodesCreatedPerSecond();
}
