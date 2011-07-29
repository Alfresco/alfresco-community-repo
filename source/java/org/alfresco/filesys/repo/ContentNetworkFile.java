/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.filesys.repo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.AccessDeniedException;
import org.alfresco.jlan.server.filesys.DiskFullException;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.smb.SeekType;
import org.alfresco.jlan.smb.server.SMBSrvSession;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.AbstractContentReader;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.ContentAccessor;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Implementation of the <tt>NetworkFile</tt> for direct interaction
 * with the channel repository.
 * <p>
 * This provides the interaction with the Alfresco Content Model file/folder structure.
 * 
 * @author Derek Hulley
 */
public class ContentNetworkFile extends NodeRefNetworkFile
{
    private static final Log logger = LogFactory.getLog(ContentNetworkFile.class);
    
    // Services
    
    private NodeService nodeService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    
    // File channel to file content
    
    private FileChannel channel;
    
    // File content
    
    private ContentAccessor content;
    private String preUpdateContentURL;
    
    // Indicate if file has been written to or truncated/resized
    
    private boolean modified;
    
    // Flag to indicate if the file channel is writable
    
    private boolean writableChannel;

    /**
     * Helper method to create a {@link NetworkFile network file} given a node reference.
     */
    public static ContentNetworkFile createFile( NodeService nodeService, ContentService contentService, MimetypeService mimetypeService,
            CifsHelper cifsHelper, NodeRef nodeRef, FileOpenParams params, SrvSession sess)
    {
        String path = params.getPath();
        
        // Create the file
        
        ContentNetworkFile netFile = null;
        
        if ( isMSOfficeSpecialFile(path, sess, nodeService, nodeRef)) {
        	
        	// Create a file for special processing for Excel
        	
        	netFile = new MSOfficeContentNetworkFile( nodeService, contentService, mimetypeService, nodeRef, path);
        }
        else if ( isOpenOfficeSpecialFile( path, sess, nodeService, nodeRef)) {
            
            // Create a file for special processing
            
            netFile = new OpenOfficeContentNetworkFile( nodeService, contentService, mimetypeService, nodeRef, path);
        }        
        else {
        	
        	// Create a normal content file
        
        	netFile = new ContentNetworkFile(nodeService, contentService, mimetypeService, nodeRef, path);
        }
        
        // Set relevant parameters
        
        if (params.isReadOnlyAccess())
        {
            netFile.setGrantedAccess(NetworkFile.READONLY);
        }
        else
        {
            netFile.setGrantedAccess(NetworkFile.READWRITE);
        }
        
        // Check the type
        
        FileInfo fileInfo;
        try
        {
            fileInfo = cifsHelper.getFileInformation(nodeRef, "", false, false);
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException("File not found when creating network file: " + nodeRef, e);
        }
        
        if (fileInfo.isDirectory())
        {
            netFile.setAttributes(FileAttribute.Directory);
        }
        else
        {
            // Set the current size
            
            netFile.setFileSize(fileInfo.getSize());
        }
        
        // Set the file timestamps
        
        if ( fileInfo.hasCreationDateTime())
            netFile.setCreationDate( fileInfo.getCreationDateTime());
        
        if ( fileInfo.hasModifyDateTime())
            netFile.setModifyDate(fileInfo.getModifyDateTime());
        
        if ( fileInfo.hasAccessDateTime())
            netFile.setAccessDate(fileInfo.getAccessDateTime());
        
        // Set the file attributes
        
        netFile.setAttributes(fileInfo.getFileAttributes());

        // Set the owner process id
        
        netFile.setProcessId( params.getProcessId());
        
        // If the file is read-only then only allow read access
        
        if ( netFile.isReadOnly())
            netFile.setGrantedAccess(NetworkFile.READONLY);
        
        // DEBUG
        
        if (logger.isDebugEnabled())
            logger.debug("Create file node=" + nodeRef + ", param=" + params + ", netfile=" + netFile);

        // Return the network file
        
        return netFile;
    }

    /**
     * Class constructor
     * 
     * @param transactionService TransactionService
     * @param nodeService NodeService
     * @param contentService ContentService
     * @param nodeRef NodeRef
     * @param name String
     */
    protected ContentNetworkFile(
            NodeService nodeService,
            ContentService contentService,
            MimetypeService mimetypeService,
            NodeRef nodeRef,
            String name)
    {
        super(name, nodeRef);
        setFullName(name);
        this.nodeService = nodeService;
        this.contentService = contentService;
        this.mimetypeService = mimetypeService;
    }

    /**
     * Return the file details as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append( "[");
        str.append(getFullName());
        str.append(",");
        str.append( getNodeRef().getId());
        str.append( ",channel=");
        str.append( channel);
        if ( channel != null)
        	str.append( writableChannel ? "(Write)" : "(Read)");
        if ( modified)
        	str.append( ",modified");
        str.append( "]");

        return str.toString();
    }
    
    /**
     * @return Returns true if the channel should be writable
     * 
     * @see NetworkFile#getGrantedAccess()
     * @see NetworkFile#READONLY
     * @see NetworkFile#WRITEONLY
     * @see NetworkFile#READWRITE
     */
    private boolean isWritable()
    {
        // Check that we are allowed to write
    	
        int access = getGrantedAccess();
        return (access == NetworkFile.READWRITE || access == NetworkFile.WRITEONLY);
    }

    /**
     * Determine if the file content data has been opened
     * 
     * @return boolean
     */
    public final boolean hasContent()
    {
        return content != null ? true : false;
    }
    
    /**
     * Opens the channel for reading or writing depending on the access mode.
     * <p>
     * If the channel is already open, it is left.
     * 
     * @param write true if the channel must be writable
     * @param trunc true if the writable channel does not require the previous content data
     * @throws AccessDeniedException if this network file is read only
     * @throws AlfrescoRuntimeException if this network file represents a directory
     *
     * @see NetworkFile#getGrantedAccess()
     * @see NetworkFile#READONLY
     * @see NetworkFile#WRITEONLY
     * @see NetworkFile#READWRITE
     */
    protected void openContent(boolean write, boolean trunc)
    	throws AccessDeniedException, AlfrescoRuntimeException
    {
    	// Check if the file is a directory
    	
        if (isDirectory())
        {
            throw new AlfrescoRuntimeException("Unable to open channel for a directory network file: " + this);
        }
        
        // Check if write access is required and the current channel is read-only
        
        else if ( write && writableChannel == false && channel != null)
        {
            // Close the existing read-only channel
            
            try
            {
                channel.close();
                channel = null;
            }
            catch (IOException ex)
            {
                logger.error("Error closing read-only channel", ex);
            }
            
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Switching to writable channel for " + getName());
        }
        else if (channel != null)
        {
            // Already have channel open
        	
            return;
        }
        
        // We need to create the channel
        
        if (write && !isWritable())
        {
            throw new AccessDeniedException("The network file was created for read-only: " + this);
        }

        content = null;
        preUpdateContentURL = null;
        if (write)
        {
        	// Get a writeable channel to the content, along with the original content
            if(logger.isDebugEnabled())
            {
                logger.debug("get writer for content property");
            }
        	
            content = contentService.getWriter( getNodeRef(), ContentModel.PROP_CONTENT, false);
                        
            // Keep the original content for later comparison
            
            ContentData preUpdateContentData = (ContentData) nodeService.getProperty( getNodeRef(), ContentModel.PROP_CONTENT);
            if (preUpdateContentData != null)
            {
                preUpdateContentURL = preUpdateContentData.getContentUrl();
            }
            
            // Indicate that we have a writable channel to the file
            
            writableChannel = true;
            
            // Get the writable channel, do not copy existing content data if the file is to be truncated
            
            channel = ((ContentWriter) content).getFileChannel( trunc);
        }
        else
        {
        	// Get a read-only channel to the content
            if(logger.isDebugEnabled())
            {
                logger.debug("get reader for content property");
            }
        	
            content = contentService.getReader( getNodeRef(), ContentModel.PROP_CONTENT);
            
            // Ensure that the content we are going to read is valid
            
            content = FileContentReader.getSafeContentReader(
                    (ContentReader) content,
                    I18NUtil.getMessage(FileContentReader.MSG_MISSING_CONTENT),
                    getNodeRef(), content);
            
            // Indicate that we only have a read-only channel to the data
            
            writableChannel = false;
            
            // Get the read-only channel
            
            channel = ((ContentReader) content).getFileChannel();
        }
        
        // Update the current file size
        
        if ( channel != null) 
        {
            try 
            {
                setFileSize(channel.size());
            }
            catch (IOException ex) 
            {
                logger.error( ex);
            }
            
            // Indicate that the file is open
            
            setClosed( false);
        }
    }

    /**
     * Close the file
     * 
     * @exception IOException
     */
    public void closeFile()
    	throws IOException
    {
    	// Check if this is a directory
    	if(logger.isDebugEnabled())
    	{
    	    logger.debug("closeFile");
    	}
    	
        if (isDirectory())
        {
        	// Nothing to do
            if(logger.isDebugEnabled())
            {
                logger.debug("file is a directory - nothing to do");
            }
        	
            setClosed( true);
            return;
        }
        else if (!hasContent())
        {
        	// File was not read/written so channel was not opened
            if(logger.isDebugEnabled())
            {
                logger.debug("no content to write - nothing to do");
            }
        	
            setClosed( true);
            return;
        }
        
        // Check if the file has been modified
        
        if (modified)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("content has been modified");
            }
            NodeRef contentNodeRef = getNodeRef();
            ContentWriter writer = (ContentWriter)content;
            
            // We may be in a retry block, in which case this section will already have executed and channel will be null
            if (channel != null)
            {
                // Do we need the mimetype guessing for us when we're done?
                if (content.getMimetype() == null || content.getMimetype().equals(MimetypeMap.MIMETYPE_BINARY) )
                {
                    String filename = (String) nodeService.getProperty(contentNodeRef, ContentModel.PROP_NAME);
                    writer.guessMimetype(filename);
                }
                
                // We always want the encoding guessing
                writer.guessEncoding();
                
                // Close the channel
                channel.close();
                channel = null;
            }
            
            // Retrieve the content data and stop the content URL from being 'eagerly deleted', in case we need to
            // retry the transaction

            final ContentData contentData = content.getContentData();

            // Update node properties, but only if the binary has changed (ETHREEOH-1861)
            
            ContentReader postUpdateContentReader = writer.getReader();

            RunAsWork<ContentReader> getReader = new RunAsWork<ContentReader>()
            {
                public ContentReader doWork() throws Exception
                {
                	return preUpdateContentURL == null ? null : contentService.getRawReader(preUpdateContentURL);
                }
            };
            ContentReader preUpdateContentReader = AuthenticationUtil.runAs(getReader, AuthenticationUtil.getSystemUserName());
            
            boolean contentChanged = preUpdateContentURL == null
                    || !AbstractContentReader.compareContentReaders(preUpdateContentReader,
                            postUpdateContentReader);
            
            if (contentChanged)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("content has changed - remove ASPECT_NO_CONTENT");
                }
                nodeService.removeAspect(contentNodeRef, ContentModel.ASPECT_NO_CONTENT);
                try
                {
                    nodeService.setProperty( contentNodeRef, ContentModel.PROP_CONTENT, contentData);
                }
                catch (ContentQuotaException qe)
                {
                    throw new DiskFullException(qe.getMessage());
                }
            }

            // Tidy up after ourselves after a successful commit. Otherwise leave things to allow a retry. 
            AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter()
            {
                @Override
                public void afterCommit()
                {
                    content = null;
                    preUpdateContentURL = null;
                    
                    setClosed( true);
                }
            });
        }
        else if (channel != null)
        {
            // Close it - it was not modified
            if(logger.isDebugEnabled())
            {
                logger.debug("content not modified - simply close the channel");
            }
        	
            channel.close();
            channel = null;
        }
    }

    /**
     * Truncate or extend the file to the specified length
     * 
     * @param size long
     * @exception IOException
     */
    public void truncateFile(long size)
    	throws IOException
    {
        logger.debug("truncate file");
    	try 
    	{
        	// If the content data channel has not been opened yet and the requested size is zero
            // then this is an open for overwrite so the existing content data is not copied
            
            if ( hasContent() == false && size == 0L)
            {
                // Open content for overwrite, no need to copy existing content data
                
                openContent(true, true);
            }
            else
            {
                // Normal open for write
                
                openContent(true, false);

                // Truncate or extend the channel
                
                channel.truncate(size);
            }
    	}
    	catch ( ContentIOException ex) {
    		
    		// DEBUG
    		
    		if ( logger.isDebugEnabled())
    			logger.debug("Error opening file " + getFullName() + " for write", ex);
    		
    		// Convert to a file server I/O error
    		
    		throw new DiskFullException("Failed to open " + getFullName() + " for write");
    	}
        
        // Set modification flag
        
        modified = true;
        
        // Set the new file size
        
        setFileSize( size);
        
        // Update the modification date/time
        
        if ( getFileState() != null)
        	getFileState().updateModifyDateTime();
        
        // DEBUG
        
        if (logger.isDebugEnabled())
            logger.debug("Truncate file=" + this + ", size=" + size);
    }

    /**
     * Write a block of data to the file.
     * 
     * @param buf byte[]
     * @param len int
     * @param pos int
     * @param fileOff long
     * @exception IOException
     */
    public void writeFile(byte[] buffer, int length, int position, long fileOffset)
    	throws IOException
    {
    	try {
	        // Open the channel for writing
	        
	        openContent(true, false);
    	}
    	catch ( ContentIOException ex) {
    		
    		// DEBUG
    		
    		if ( logger.isDebugEnabled())
    			logger.debug("Error opening file " + getFullName() + " for write", ex);
    		
    		// Convert to a file server I/O error
    		
    		throw new DiskFullException("Failed to open " + getFullName() + " for write");
    	}
        
        // Write to the channel
        
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, position, length);
        int count = channel.write(byteBuffer, fileOffset);
        
        // Set modification flag
        
        modified = true;
        incrementWriteCount();

        // Update the current file size
        
        setFileSize(channel.size());
        
        // Update the modification date/time
        
        if ( getFileState() != null)
        	getFileState().updateModifyDateTime();

        // DEBUG
        
        if (logger.isDebugEnabled())
            logger.debug("Write file=" + this + ", size=" + count);
    }

    /**
     * Read from the file.
     * 
     * @param buf byte[]
     * @param len int
     * @param pos int
     * @param fileOff long
     * @return Length of data read.
     * @exception IOException
     */
    public int readFile(byte[] buffer, int length, int position, long fileOffset)
    	throws IOException
    {
        // Open the channel for reading
        
        openContent(false, false);
        
        // Read from the channel
        
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, position, length);
        int count = channel.read(byteBuffer, fileOffset);
        if (count < 0)
        {
            count = 0;  // doesn't obey the same rules, i.e. just returns the bytes read
        }
        
        // Update the access date/time
        
        if ( getFileState() != null)
        	getFileState().updateAccessDateTime();

        // DEBUG
        
        if (logger.isDebugEnabled())
            logger.debug("Read file=" + this + " read=" + count);
        
        // Return the actual count of bytes read
        
        return count;
    }
    
    /**
     * Open the file
     * 
     * @param createFlag boolean
     * @exception IOException
     */
    @Override
    public void openFile(boolean createFlag)
    	throws IOException
    {
    	// Wait for read/write before opening the content channel
    }

    /**
     * Seek to a new position in the file
     * 
     * @param pos long
     * @param typ int
     * @return long
     */
    @Override
    public long seekFile(long pos, int typ)
    	throws IOException
    {
        //  Open the file, if not already open

        openContent( false, false);

        //  Check if the current file position is the required file position

        long curPos = channel.position();
        
        switch (typ) {

          //  From start of file

          case SeekType.StartOfFile :
            if (curPos != pos)
              channel.position( pos);
            break;

            //  From current position

          case SeekType.CurrentPos :
            channel.position( curPos + pos);
            break;

            //  From end of file

          case SeekType.EndOfFile :
            {
              long newPos = channel.size() + pos;
              channel.position(newPos);
            }
            break;
        }

        // Update the access date/time
        
        if ( getFileState() != null)
        	getFileState().updateAccessDateTime();

        // DEBUG
        
        if (logger.isDebugEnabled())
            logger.debug("Seek file=" + this + ", pos=" + pos + ", type=" + typ);

        //  Return the new file position

        return channel.position();
    }

    /**
     * Flush and buffered data for this file
     * 
     * @exception IOException
     */
    @Override
    public void flushFile()
    	throws IOException
    {
        // Open the channel for writing
    	
        openContent(true, false);
        
        // Flush the channel - metadata flushing is not important
        
        channel.force(false);
        
        // Update the access date/time
        
        if ( getFileState() != null)
        	getFileState().updateAccessDateTime();

        // DEBUG
        
        if (logger.isDebugEnabled())
            logger.debug("Flush file=" + this);
    }
    
    /**
     * Return the modified status
     * 
     * @return boolean
     */
    public final boolean isModified() {
        return modified;
    }
    
    /**
     * Check if the file is an MS Office document type that needs special processing
     * 
     * @param path String
     * @param sess SrvSession
     * @param nodeService NodeService
     * @param nodeRef NodeRef
     * @return boolean
     */
    private static final boolean isMSOfficeSpecialFile( String path, SrvSession sess, NodeService nodeService, NodeRef nodeRef) {
    	
    	// Check if the file extension indicates a problem MS Office format

    	path = path.toLowerCase();
    	
    	if ( path.endsWith( ".xls") && sess instanceof SMBSrvSession) {
    	    
            // Check if the file is versionable
            
            if ( nodeService.hasAspect( nodeRef, ContentModel.ASPECT_VERSIONABLE))
                return true;
    	}
    	return false;
    }

    /**
     * Check if the file is an OpenOffice document type that needs special processing
     * 
     * @param path String
     * @param sess SrvSession
     * @param nodeService NodeService
     * @param nodeRef NodeRef
     * @return boolean
     */
    private static final boolean isOpenOfficeSpecialFile( String path, SrvSession sess, NodeService nodeService, NodeRef nodeRef) {
        
        // Check if the file extension indicates a problem OpenOffice format

        path = path.toLowerCase();
        
        if ( path.endsWith( ".odt") && sess instanceof SMBSrvSession) {
            
            // Check if the file is versionable
            
            if ( nodeService.hasAspect( nodeRef, ContentModel.ASPECT_VERSIONABLE))
                return true;
        }
        return false;
    }
}
