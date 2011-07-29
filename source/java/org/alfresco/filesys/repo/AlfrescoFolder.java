package org.alfresco.filesys.repo;

import java.io.IOException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.NetworkFileStateInterface;

/**
 * Object returned to JLAN if the repository object is a folder.
 */
public class AlfrescoFolder extends NetworkFile implements NetworkFileStateInterface
{
    public AlfrescoFolder(String path, FileInfo fileInfo, boolean readOnly)
    {
        super(path);
        setFullName(path);
        
        // Set the file timestamps
        
        if ( fileInfo.hasCreationDateTime())
            setCreationDate( fileInfo.getCreationDateTime());
        
        if ( fileInfo.hasModifyDateTime())
            setModifyDate(fileInfo.getModifyDateTime());
        
        if ( fileInfo.hasAccessDateTime())
            setAccessDate(fileInfo.getAccessDateTime());
        
        // Set the file attributes
        setAttributes(fileInfo.getFileAttributes());
    }

    @Override
    public void openFile(boolean createFlag) throws IOException
    {
        throw new AlfrescoRuntimeException("Unable to open channel for a directory network file: " + this);        
    }

    @Override
    public int readFile(byte[] buf, int len, int pos, long fileOff)
            throws IOException
    {
        throw new AlfrescoRuntimeException("Unable to open channel for a directory network file: " + this);
    }

    @Override
    public void writeFile(byte[] buf, int len, int pos, long fileOff)
            throws IOException
    {
        throw new AlfrescoRuntimeException("Unable to open channel for a directory network file: " + this);        
    }

    @Override
    public long seekFile(long pos, int typ) throws IOException
    {
        return 0;
    }

    @Override
    public void flushFile() throws IOException
    {
        // Do nothing.
    }

    @Override
    public void truncateFile(long siz) throws IOException
    {
        throw new AlfrescoRuntimeException("Unable to open channel for a directory network file: " + this);
    }

    @Override
    public void closeFile() throws IOException
    {
        setClosed(true);
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
