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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.bulkimport.BulkImportStatus;
import org.alfresco.repo.bulkimport.ImportableItem;

/**
 * Thread-safe implementation of Bulk Import Status.
 *
 * @since 4.0
 * 
 * @see org.alfresco.extension.bulkfilesystemimport.BulkImportStatus
 */
public class BulkImportStatusImpl implements BulkImportStatus
{
    public enum NodeState
    {
    	SKIPPED,
    	CREATED,
    	REPLACED
    };
    
    // General information
    private int numThreads;
    private int batchSize;
    private AtomicBoolean inProgress                              = new AtomicBoolean();
    private String        sourceDirectory                         = null;
    private String        targetSpace                             = null;
    private Date          startDate                               = null;
    private Date          endDate                                 = null;
    private Long          startNs                                 = null;
    private Long          endNs                                   = null;
    private Throwable     lastException                           = null;
    private AtomicLong    numberOfBatchesCompleted                = new AtomicLong();
    
    // Read-side information
    private AtomicLong    numberOfFoldersScanned                  = new AtomicLong();
    private AtomicLong    numberOfFilesScanned                    = new AtomicLong();
    private AtomicLong    numberOfUnreadableEntries               = new AtomicLong(); 
    
    private AtomicLong    numberOfContentFilesRead                = new AtomicLong();
    private AtomicLong    numberOfContentBytesRead                = new AtomicLong();
    
    private AtomicLong    numberOfMetadataFilesRead               = new AtomicLong();
    private AtomicLong    numberOfMetadataBytesRead               = new AtomicLong();
    
    private AtomicLong    numberOfContentVersionFilesRead         = new AtomicLong();
    private AtomicLong    numberOfContentVersionBytesRead         = new AtomicLong();
    
    private AtomicLong    numberOfMetadataVersionFilesRead        = new AtomicLong();
    private AtomicLong    numberOfMetadataVersionBytesRead        = new AtomicLong();
    
    // Write-side information
    private AtomicLong    numberOfSpaceNodesCreated               = new AtomicLong();
    private AtomicLong    numberOfSpaceNodesReplaced              = new AtomicLong();
    private AtomicLong    numberOfSpaceNodesSkipped               = new AtomicLong();
    private AtomicLong    numberOfSpacePropertiesWritten          = new AtomicLong();
    
    private AtomicLong    numberOfContentNodesCreated             = new AtomicLong();
    private AtomicLong    numberOfContentNodesReplaced            = new AtomicLong();
    private AtomicLong    numberOfContentNodesSkipped             = new AtomicLong();
    private AtomicLong    numberOfContentBytesWritten             = new AtomicLong();
    private AtomicLong    numberOfContentPropertiesWritten        = new AtomicLong();
    
    private AtomicLong    numberOfContentVersionsCreated          = new AtomicLong();
    private AtomicLong    numberOfContentVersionBytesWritten      = new AtomicLong();
    private AtomicLong    numberOfContentVersionPropertiesWritten = new AtomicLong();

    public BulkImportStatusImpl()
    {
        inProgress.set(false);
    }
    
    // General information
    public String getSourceDirectory() { return(sourceDirectory); }
    public String getTargetSpace()     { return(targetSpace); }
    public Date getStartDate()       { return(copyDate(startDate)); }
    public Date getEndDate()         { return(copyDate(endDate)); }
    
    public Long getDurationInNs()
    {
        Long result = null;
        
        if (startNs != null)
        {
            if (endNs != null)
            {
                result = new Long(endNs - startNs);
            }
            else
            {
                result = new Long(System.nanoTime() - startNs);
            }
        }
        
        return(result);
    }
    
    public Long getDuration()
    {
        long duration = 0;
        Long durationNS = getDurationInNs();
        
        if(durationNS != null)
        {
        	duration = durationNS / (1000 * 1000 * 1000);
        	if(duration == 0)
        	{
        		return null;
        	}
        }
        
        return new Long(duration);
    }
    
    public Throwable getLastException()
    {
    	return(lastException);
    }
    
    public String getLastExceptionAsString()
    {
        String result = null;
        
        if (lastException != null)
        {
            StringWriter sw = new StringWriter();
            PrintWriter  pw = new PrintWriter(sw, true);
            
            lastException.printStackTrace(pw);
            
            pw.flush();
            sw.flush();
            
            result = sw.toString();
        }
        
        return(result);
    }
    
    public boolean inProgress()
    {
    	return(inProgress.get());
	}
    
    public long getNumberOfBatchesCompleted()
    {
    	return(numberOfBatchesCompleted.get());
    }
    
    public void incrementNumberOfBatchesCompleted()
    {
    	numberOfBatchesCompleted.incrementAndGet();
    }
    
    public void startImport(final String sourceDirectory, final String targetSpace)
    {
        if (!inProgress.compareAndSet(false, true))
        {
            throw new AlfrescoRuntimeException("Import already in progress.");
        }
        
        // General information
        this.sourceDirectory           = sourceDirectory;
        this.targetSpace               = targetSpace;
        this.startDate                 = new Date();
        this.endDate                   = null;
        this.lastException             = null;
        this.numberOfBatchesCompleted.set(0);
        
        // Read-side information
        this.numberOfFoldersScanned.set(1);   // We set this to one to count the initial starting directory (which doesn't otherwise get counted)
        this.numberOfFilesScanned.set(0);
        this.numberOfUnreadableEntries.set(0);
        
        this.numberOfContentFilesRead.set(0);
        this.numberOfContentBytesRead.set(0);
        
        this.numberOfMetadataFilesRead.set(0);
        this.numberOfMetadataBytesRead.set(0);
        
        this.numberOfContentVersionFilesRead.set(0);
        this.numberOfContentVersionBytesRead.set(0);
        
        this.numberOfMetadataVersionFilesRead.set(0);
        this.numberOfMetadataVersionBytesRead.set(0);
        
        // Write-side information
        this.numberOfSpaceNodesCreated.set(0);
        this.numberOfSpaceNodesReplaced.set(0);
        this.numberOfSpaceNodesSkipped.set(0);
        this.numberOfSpacePropertiesWritten.set(0);
        
        this.numberOfContentNodesCreated.set(0);
        this.numberOfContentNodesReplaced.set(0);
        this.numberOfContentNodesSkipped.set(0);
        this.numberOfContentBytesWritten.set(0);
        this.numberOfContentPropertiesWritten.set(0);
        
        this.numberOfContentVersionsCreated.set(0);
        this.numberOfContentVersionBytesWritten.set(0);
        this.numberOfContentVersionPropertiesWritten.set(0);
        
        this.startNs = System.nanoTime();
        this.endNs   = null;
    }
    
    public void stopImport()
    {
        if (!inProgress.compareAndSet(true, false))
        {
            throw new RuntimeException("Import not in progress.");
        }
        
        endNs = System.nanoTime();
        endDate = new Date();
    }
    
    public void stopImport(final Throwable lastException)
    {
        stopImport();
        this.lastException = lastException;
    }
    
    // Read-side information
    public long getNumberOfFoldersScanned()              { return(numberOfFoldersScanned.longValue()); }
    public long getNumberOfFilesScanned()                { return(numberOfFilesScanned.longValue()); }
    public long getNumberOfUnreadableEntries()           { return(numberOfUnreadableEntries.longValue()); }
    
    public long getNumberOfContentFilesRead()            { return(numberOfContentFilesRead.longValue()); }
    public long getNumberOfContentBytesRead()            { return(numberOfContentBytesRead.longValue()); }
    
    public long getNumberOfMetadataFilesRead()           { return(numberOfMetadataFilesRead.longValue()); }
    public long getNumberOfMetadataBytesRead()           { return(numberOfMetadataBytesRead.longValue()); }
    
    public long getNumberOfContentVersionFilesRead()     { return(numberOfContentVersionFilesRead.longValue()); }
    public long getNumberOfContentVersionBytesRead()     { return(numberOfContentVersionBytesRead.longValue()); }
    
    public long getNumberOfMetadataVersionFilesRead()    { return(numberOfMetadataVersionFilesRead.longValue()); }
    public long getNumberOfMetadataVersionBytesRead()    { return(numberOfMetadataVersionBytesRead.longValue()); }
    
    public void incrementImportableItemsRead(final ImportableItem importableItem, final boolean isDirectory)
    {
        if (importableItem.getHeadRevision().contentFileExists())
        {
            if (!isDirectory)
            {
                numberOfContentFilesRead.incrementAndGet();
                numberOfContentBytesRead.addAndGet(importableItem.getHeadRevision().getContentFileSize());
            }
        }
        
        if (importableItem.getHeadRevision().metadataFileExists())
        {
            numberOfMetadataFilesRead.incrementAndGet();
            numberOfMetadataBytesRead.addAndGet(importableItem.getHeadRevision().getMetadataFileSize());
        }
        
        if (!isDirectory && importableItem.hasVersionEntries())
        {
            for (final ImportableItem.ContentAndMetadata versionEntry : importableItem.getVersionEntries())
            {
                if (versionEntry.contentFileExists())
                {
                    numberOfContentVersionFilesRead.incrementAndGet();
                    numberOfContentVersionBytesRead.addAndGet(versionEntry.getContentFileSize());
                }
                
                if (versionEntry.metadataFileExists())
                {
                    numberOfMetadataVersionFilesRead.incrementAndGet();
                    numberOfMetadataVersionBytesRead.addAndGet(versionEntry.getMetadataFileSize());
                }
            }
        }
    }
    
    public void incrementNumberOfFilesScanned()
    {
        numberOfFilesScanned.incrementAndGet();
    }
    
    public void incrementNumberOfFoldersScanned()
    {
        numberOfFoldersScanned.incrementAndGet();
    }
    
    public void incrementNumberOfUnreadableEntries()
    {
        numberOfUnreadableEntries.incrementAndGet();
    }
    
    public void incrementImportableItemsSkipped(final ImportableItem importableItem, final boolean isDirectory)
    {
        if (importableItem.getHeadRevision().contentFileExists())
        {
            long ignored = isDirectory ? numberOfSpaceNodesSkipped.incrementAndGet() : numberOfContentNodesSkipped.incrementAndGet();
        }
        
        // We don't track the number of properties or version entries skipped
    }
    
    
    
    // Write-side information
    public long getNumberOfSpaceNodesCreated()               { return(numberOfSpaceNodesCreated.longValue()); }
    public long getNumberOfSpaceNodesReplaced()              { return(numberOfSpaceNodesReplaced.longValue()); }
    public long getNumberOfSpaceNodesSkipped()               { return(numberOfSpaceNodesSkipped.longValue()); }
    public long getNumberOfSpacePropertiesWritten()          { return(numberOfSpacePropertiesWritten.longValue()); }
    
    public long getNumberOfContentNodesCreated()             { return(numberOfContentNodesCreated.longValue()); }
    public long getNumberOfContentNodesReplaced()            { return(numberOfContentNodesReplaced.longValue()); }
    public long getNumberOfContentNodesSkipped()             { return(numberOfContentNodesSkipped.longValue()); }
    public long getNumberOfContentBytesWritten()             { return(numberOfContentBytesWritten.longValue()); }
    public long getNumberOfContentPropertiesWritten()        { return(numberOfContentPropertiesWritten.longValue()); }
    
    public long getNumberOfContentVersionsCreated()          { return(numberOfContentVersionsCreated.longValue()); }
    public long getNumberOfContentVersionBytesWritten()      { return(numberOfContentVersionBytesWritten.longValue()); }
    public long getNumberOfContentVersionPropertiesWritten() { return(numberOfContentVersionPropertiesWritten.longValue()); }

    public void incrementContentBytesWritten(final ImportableItem importableItem, final boolean isSpace,
    		final NodeState nodeState)
    {
        if (importableItem.getHeadRevision().contentFileExists())
        {
            switch (nodeState)
            {
                case CREATED:
                    numberOfContentBytesWritten.addAndGet(importableItem.getHeadRevision().getContentFileSize());
                    break;
                    
                case REPLACED:
                    numberOfContentBytesWritten.addAndGet(importableItem.getHeadRevision().getContentFileSize());
                    break;
            }
        }
    }

    public void incrementNodesWritten(final ImportableItem importableItem,
                                      final boolean        isSpace,
                                      final NodeState      nodeState,
                                      final long           numProperties,
                                      final long           numVersionProperties)
    {
        long ignored;

        if (importableItem.getHeadRevision().contentFileExists())
        {
            switch (nodeState)
            {
                case SKIPPED:
                    ignored = isSpace ? numberOfSpaceNodesSkipped.incrementAndGet() : numberOfContentNodesSkipped.incrementAndGet();
                    break;
                    
                case CREATED:
                    ignored = isSpace ? numberOfSpaceNodesCreated.incrementAndGet() : numberOfContentNodesCreated.incrementAndGet();
                    break;
                    
                case REPLACED:
                    ignored = isSpace ? numberOfSpaceNodesReplaced.incrementAndGet() : numberOfContentNodesReplaced.incrementAndGet();
                    break;
            }
        }

        switch (nodeState)
        {
            case SKIPPED:
                // We don't track the number of properties skipped
                break;
                
            case CREATED:
            case REPLACED:
                ignored = isSpace ? numberOfSpacePropertiesWritten.addAndGet(numProperties) : numberOfContentPropertiesWritten.addAndGet(numProperties);
                break;
        }

        if (!isSpace && importableItem.hasVersionEntries())
        {
            numberOfContentVersionPropertiesWritten.addAndGet(numVersionProperties);
            
            for (final ImportableItem.ContentAndMetadata versionEntry : importableItem.getVersionEntries())
            {
                if (versionEntry.contentFileExists())
                {
                    switch (nodeState)
                    {
                        case SKIPPED:
                            // We only track the number of items skipped on the read side
                            break;
                            
                        case CREATED:
                        case REPLACED:
                            numberOfContentVersionsCreated.incrementAndGet();
                            numberOfContentVersionBytesWritten.addAndGet(versionEntry.getContentFileSize());
                            break;
                    }
                }
            }
        }
    }
    
    public Long getFilesReadPerSecond()
    {
    	Long duration = getDuration();
    	if(duration != null)
    	{
	    	long totalFilesRead = numberOfContentFilesRead.longValue() + numberOfMetadataFilesRead.longValue() +
	    		numberOfContentVersionFilesRead.longValue() + numberOfMetadataVersionFilesRead.longValue();    	
	    	long filesReadPerSecond = totalFilesRead / duration;
	    	return filesReadPerSecond;
    	}
    	else
    	{
    		return null;
    	}
	}

    public Long getBytesReadPerSecond()
    {
    	Long duration = getDuration();
    	if(duration != null)
    	{
	    	long totalDataRead = numberOfContentBytesRead.longValue() + numberOfMetadataBytesRead.longValue() +
	    		numberOfContentVersionBytesRead.longValue() + numberOfMetadataVersionBytesRead.longValue();    	
	    	long bytesReadPerSecond = totalDataRead / duration;
	    	return bytesReadPerSecond;
    	}
    	else
    	{
    		return null;
    	}
    }

    public Long getEntriesScannedPerSecond()
    {
    	Long duration = getDuration();
    	if(duration != null)
    	{
	    	long entriesScannedPerSecond = (numberOfFilesScanned.longValue() + numberOfFoldersScanned.longValue()) / duration;
	    	return entriesScannedPerSecond;
    	}
    	else
    	{
    		return null;
    	}
    }
    
    public Long getBytesWrittenPerSecond()
    {
    	Long duration = getDuration();
    	if(duration != null)
    	{
	    	long totalDataWritten = numberOfContentBytesWritten.longValue() + numberOfContentVersionBytesWritten.longValue();    	
	    	long bytesWrittenPerSecond = totalDataWritten / duration;
	    	return bytesWrittenPerSecond;
    	}
    	else
    	{
    		return null;
    	}
    }

    public Long getNodesCreatedPerSecond()
    {
    	Long duration = getDuration();
    	if(duration != null)
    	{
	    	// Note: we count versions as a node
	    	long totalNodesWritten = numberOfSpaceNodesCreated.longValue() + numberOfSpaceNodesReplaced.longValue() +
	    		numberOfContentNodesCreated.longValue() + numberOfContentNodesReplaced.longValue() + numberOfContentVersionsCreated.longValue();
	    	long nodesCreatedPerSecond = totalNodesWritten / duration;
	    	return nodesCreatedPerSecond;
		}
		else
		{
			return null;
		}
    }

    // Private helper methods
    private final Date copyDate(final Date date)
    {
        // Defensively copy the date.
        Date result = null;
        
        if (date != null)
        {
            result = new Date(date.getTime());
        }
        
        return(result);
    }
    
    public int getNumThreads()
    {
    	return numThreads;
    }

    public int getBatchSize()
    {
    	return batchSize;
    }

    public void setNumThreads(int numThreads)
    {
    	this.numThreads = numThreads;
    }

    public void setBatchSize(int batchSize)
    {
    	this.batchSize = batchSize;
    }

    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Bulk Import Status\n");
    	sb.append("Source directory: ");
    	sb.append(getSourceDirectory());
    	sb.append("\nTarget space: ");
    	sb.append(getTargetSpace());
    	sb.append("\nStart date : ");
    	sb.append(getStartDate());
    	sb.append("\nEnd date : ");
    	sb.append(getEndDate());
    	sb.append("\nNum threads : ");
    	sb.append(getNumThreads());
    	sb.append("\nBatch size : ");
    	sb.append(getBatchSize());

    	if(inProgress())
    	{
    		sb.append("\n\nIn progress");

    	}
    	else
    	{
    		sb.append("\n\nNot in progress");
    	}
    	
    	sb.append("\nBytes written/sec : ");
    	sb.append(getBytesWrittenPerSecond());
    	sb.append("\nBytes read/sec : ");
    	sb.append(getBytesReadPerSecond());
    	sb.append("\nEntries scanned/sec : ");
    	sb.append(getEntriesScannedPerSecond());
    	sb.append("\nFiles read/sec : ");
    	sb.append(getFilesReadPerSecond());
    	sb.append("\nNodes created/sec : ");
    	sb.append(getNodesCreatedPerSecond());
    	sb.append("\nNumber of files scanned : ");
    	sb.append(getNumberOfFilesScanned());
    	sb.append("\nNumber of folders scanned : ");
    	sb.append(getNumberOfFoldersScanned());
    	sb.append("\nNumber of content files read : ");
    	sb.append(getNumberOfContentFilesRead());
    	sb.append("\nNumber of content version files read : ");
    	sb.append(getNumberOfContentVersionFilesRead());
    	sb.append("\nNumber of metadata files read : ");
    	sb.append(getNumberOfMetadataFilesRead());
    	sb.append("\nNumber of metadata version files read : ");
    	sb.append(getNumberOfMetadataVersionFilesRead());
    	sb.append("\nNumber of unreadable entries : ");
    	sb.append(getNumberOfUnreadableEntries());
    	
    	sb.append("\nNumber of content nodes created : ");
    	sb.append(getNumberOfContentNodesCreated());
    	sb.append("\nNumber of content nodes replaced : ");
    	sb.append(getNumberOfContentNodesReplaced());
    	sb.append("\nNumber of content nodes skipped : ");
    	sb.append(getNumberOfContentNodesSkipped());
    	sb.append("\nNumber of content properties written : ");
    	sb.append(getNumberOfContentPropertiesWritten());
    	sb.append("\nNumber of content version properties written : ");
    	sb.append(getNumberOfContentVersionPropertiesWritten());
    	sb.append("\nNumber of content versions created : ");
    	sb.append(getNumberOfContentVersionsCreated());
    	sb.append("\nNumber of space nodes created : ");
    	sb.append(getNumberOfSpaceNodesCreated());
    	sb.append("\nNumber of space nodes replaced : ");
    	sb.append(getNumberOfSpaceNodesReplaced());
    	sb.append("\nNumber of space nodes skipped : ");
    	sb.append(getNumberOfSpaceNodesSkipped());
    	sb.append("\nNumber of space properties written : ");
    	sb.append(getNumberOfSpacePropertiesWritten());
    	
    	sb.append("\nNumber of bytes read : ");
    	sb.append(getNumberOfContentBytesRead());
    	sb.append("\nNumber of metadata bytes read: ");
    	sb.append(getNumberOfMetadataBytesRead());
    	sb.append("\nNumber of content version bytes read : ");
    	sb.append(getNumberOfContentVersionBytesRead());
    	sb.append("\nNumber of metadata bytes read : ");
    	sb.append(getNumberOfMetadataBytesRead());
    	sb.append("\nNumber of metadata version bytes read : ");
    	sb.append(getNumberOfMetadataVersionBytesRead());
    	
    	sb.append("\nNumber of batches completed : ");
    	sb.append(getNumberOfBatchesCompleted());

    	sb.append("\nNumber of bytes written : ");
    	sb.append(getNumberOfContentBytesWritten());
    	sb.append("\nNumber of content version bytes written : ");
    	sb.append(getNumberOfContentVersionBytesWritten());    	

    	return sb.toString();
    }
}
