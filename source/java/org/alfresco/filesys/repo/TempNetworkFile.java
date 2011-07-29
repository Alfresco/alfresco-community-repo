package org.alfresco.filesys.repo;

import java.io.File;
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
    private FileState fileState;
}
