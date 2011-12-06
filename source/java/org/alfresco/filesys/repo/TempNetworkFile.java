package org.alfresco.filesys.repo;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.NetworkFileStateInterface;
import org.alfresco.jlan.smb.server.disk.JavaNetworkFile;

/**
 * Temporary Java backed network file.
 * 
 * @author mrogers
 */
public class TempNetworkFile extends JavaNetworkFile implements NetworkFileStateInterface
{
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

        super.writeFile(buf, len, pos);
        
        long size = m_io.length();
             
        setFileSize(size);
        if(fileState != null)
        {
            fileState.updateModifyDateTime();
            fileState.updateAccessDateTime();
            fileState.setFileSize(size);
        }
    }
    
    @Override
    public void writeFile(byte[] buffer, int length, int position, long fileOffset)
    throws IOException
    {
        
        super.writeFile(buffer, length, position, fileOffset);
        
        long size = m_io.length();
        setFileSize(size);
        if(fileState != null)
        {
            fileState.updateModifyDateTime();
            fileState.updateAccessDateTime();
            fileState.setFileSize(size);
        }
    }
    
    @Override
    public void truncateFile(long size) throws IOException
    {
        super.truncateFile(size);
        
        setFileSize(size);
        if(fileState != null)
        {
            fileState.updateModifyDateTime();
            fileState.updateAccessDateTime();
            fileState.setFileSize(size);
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
    
    
    private FileState fileState;
}
