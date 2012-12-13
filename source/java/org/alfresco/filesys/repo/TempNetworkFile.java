package org.alfresco.filesys.repo;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.alfresco.filesys.alfresco.NetworkFileLegacyReferenceCount;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.NetworkFileStateInterface;
import org.alfresco.jlan.smb.server.disk.JavaNetworkFile;

/**
 * Temporary Java backed network file.
 * 
 * @author mrogers
 */
public class TempNetworkFile extends JavaNetworkFile 
    implements NetworkFileStateInterface,
    NetworkFileLegacyReferenceCount
{
    private boolean changed = false;
    boolean modificationDateSetDirectly = false;
    
 
    
    /**
     * Create a new temporary file with no existing content.
     * 
     * @param file the underlying File
     * @param netPath where in the repo this file is going.
     */
    public TempNetworkFile(File file, String netPath)
    {
        super(file, netPath);
        setFullName(netPath);
        setAttributes(FileAttribute.NTNormal);
        setClosed(false);
    }
    
    /**
     * A new temporary network file with some existing content.
     * @param file
     * @param netPath
     * @param existingContent
     */
    public TempNetworkFile(File file, String netPath, Reader existingContent)
    {
        super(file, netPath);
        setFullName(netPath);
        setAttributes(FileAttribute.NTNormal);
        setClosed(false);
    }
    
    /**
     * Access to the underlying file.
     * @return the file.
     */
    public File getFile()
    {
        return m_file;
    } 
    
    public String toString()
    {
        return "TempNetworkFile:" + getFullName() + " path: " + m_file.getAbsolutePath();
    }
    
    @Override
    public int readFile(byte[] buf, int len, int pos, long fileOff)
    throws java.io.IOException
    {
        if(fileState != null)
        {
            fileState.updateAccessDateTime();
        }
        return super.readFile(buf, len, pos, fileOff);
    }
    
    @Override
    public void writeFile(byte[] buf, int len, int pos) throws IOException
    {
        changed = true;

        super.writeFile(buf, len, pos);
        
        long size = m_io.length();
             
        setFileSize(size);
        if(fileState != null)
        {
            fileState.updateModifyDateTime();
            fileState.updateAccessDateTime();
            fileState.setFileSize(size);
            fileState.setAllocationSize((size + 512L) & 0xFFFFFFFFFFFFFE00L);
        }
    }
    
    @Override
    public void writeFile(byte[] buffer, int length, int position, long fileOffset)
    throws IOException
    {
        changed = true;
        
        super.writeFile(buffer, length, position, fileOffset);
        
        long size = m_io.length();
        setFileSize(size);
        if(fileState != null)
        {
            fileState.updateModifyDateTime();
            fileState.updateAccessDateTime();
            fileState.setFileSize(size);
            fileState.setAllocationSize((size + 512L) & 0xFFFFFFFFFFFFFE00L);
        }
    }
    
    @Override
    public void truncateFile(long size) throws IOException
    {
        super.truncateFile(size);
        
        if(size == 0)
        {
            changed = true;
        }
        
        setFileSize(size);
        if(fileState != null)
        {
            fileState.updateModifyDateTime();
            fileState.updateAccessDateTime();
            fileState.setFileSize(size);
            fileState.setAllocationSize((size + 512L) & 0xFFFFFFFFFFFFFE00L);
        }
    }
    
    // For JLAN file state lock manager
    public void setFileState(FileState fileState)
    {
        this.fileState = fileState;
    }

    @Override
    public FileState getFileState()
    {
        return fileState;
    }
    
    /**
     * Tell JLAN it needs to call disk.closeFile rather than short cutting.
     * @return
     */
    public boolean allowsOpenCloseViaNetworkFile() {
        return false;
    }
    
    
    public void setChanged(boolean changed)
    {
        this.changed = changed;
    }

    public boolean isChanged()
    {
        return changed;
    }
    
    public boolean isModificationDateSetDirectly()
    {
        return modificationDateSetDirectly;
    }
    
    public void setModificationDateSetDirectly(boolean  modificationDateSetDirectly)
    {
        this.modificationDateSetDirectly =  modificationDateSetDirectly;
    }

    private int legacyOpenCount = 0;
    
    /**
     * Increment the legacy file open count
     * 
     * @return int
     */
    public synchronized final int incrementLegacyOpenCount() {
        legacyOpenCount++;
        return legacyOpenCount;
    }
    
    /**
     * Decrement the legacy file open count
     * 
     * @return int
     */
    public synchronized final int decrementLagacyOpenCount() {
        legacyOpenCount--;
        return legacyOpenCount;
    }
    
    /**
     * Return the legacy open file count
     * 
     * @return int
     */
    public final int getLegacyOpenCount() {
        return legacyOpenCount;
    }


    private FileState fileState;
}
