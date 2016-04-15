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
