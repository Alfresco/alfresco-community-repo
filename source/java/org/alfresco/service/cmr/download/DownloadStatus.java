package org.alfresco.service.cmr.download;

import java.io.Serializable;

/**
 * Immutable data transfer object representing the status of a download.
 * 
 * Provides the current status and an indication of the progress. Prgress is 
 * measured by comparing done against total. Total gives an indication of the 
 * total work, while done indicates how much has been completed.
 *
 * @author amiller
 */
public class DownloadStatus implements Serializable
{
	private static final long serialVersionUID = 4513872550314507598L;

	public enum Status {
	    PENDING,
	    IN_PROGRESS,
	    DONE,
	    MAX_CONTENT_SIZE_EXCEEDED,
	    CANCELLED
	}
	
	private long done;
	private long total;
	
	private long filesAddedCount;
	private long totalFileCount;

	private Status status;
	
	/**
	 * Default constructor
	 * @param status Current status of the download
	 * @param done Done count
	 * @param total Total to be de done
	 * @param filesAdded Number of files added to the archive
	 * @param totalFiles The number of files that will eventually be added to the archive 
	 */
	public DownloadStatus(Status status, long done, long total, long filesAdded, long totalFiles)
    {
	    this.status = status;
	    this.done = done;
	    this.total = total;
	    this.filesAddedCount = filesAdded;
	    this.totalFileCount = totalFiles;
    }

	/**
	 * @return The percentage complete, calculated by multiplying done by 100 and dividing by total.
	 */
    public long getPercentageComplete() 
    {
	    return (done * 100) / total;
	}
	
    /**
     * @return true if status is DONE, false otherwise.
     */
	public boolean isComplete()
    {
        return status == Status.DONE;
    }

	/**
	 * @return the current status
	 */
    public Status getStatus()
    {
        return status;
    }

    /**
     * @return the current done count
     */
    public long getDone()
    {
        return done;
    }

    /**
     * @return the total, to be done.
     */
    public long getTotal()
    {
        return total;
    }
	
    /**
     * @return the total number of files in the download archive
     */
    public long getTotalFiles()
    {
        return totalFileCount;
    }
	
    /**
     * @return the number of files added to the download archive
     */
    public long getFilesAdded()
    {
        return filesAddedCount;
    }
	
}
