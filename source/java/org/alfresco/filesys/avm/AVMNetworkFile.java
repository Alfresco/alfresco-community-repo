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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys.avm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.AlfrescoNetworkFile;
import org.alfresco.jlan.server.filesys.AccessDeniedException;
import org.alfresco.jlan.server.filesys.DiskFullException;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.smb.SeekType;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AVM Network File Class
 * 
 * <p>Holds the details of an open file, and provides access to the file data.
 *
 * @author GKSpencer
 */
public class AVMNetworkFile extends AlfrescoNetworkFile {

	// Logging
	
	private static final Log logger = LogFactory.getLog(AVMNetworkFile.class);

	// Node Service

	private NodeService m_nodeService;
	
	// AVM service
	
	private AVMService m_avmService;
	
	// AVM path to the file/folder and store version
	
	private String m_avmPath;
	private int m_avmVersion;
	
	// Flag to indicate if the file has been modified
	
	private boolean m_modified;
	
	// Access to the file data, flag to indicate if the file channel is writable
	
	private FileChannel m_channel;
	private ContentWriter m_content;

	private boolean m_writable;
	
	// Mime type, if a writer is opened
	
	private String m_mimeType;
	
	/**
	 * Class constructor
	 * 
	 * @param details AVMNodeDescriptor
	 * @param avmPath String
	 * @param avmVersion int
	 * @param nodeService NodeService
	 * @param avmService AVMService
	 */
	public AVMNetworkFile( AVMNodeDescriptor details, String avmPath, int avmVersion, NodeService nodeService, AVMService avmService)
	{
		super( details.getName());
	
		// Save the service, apth and version
		
		m_nodeService = nodeService;
		m_avmService = avmService;
		m_avmPath    = avmPath;
		m_avmVersion = avmVersion;
		
		// Copy the file details
		
		setAccessDate( details.getAccessDate());
		setCreationDate( details.getCreateDate());
		setModifyDate( details.getModDate());
		
		if ( details.isFile())
			setFileSize( details.getLength());
		else
			setFileSize( 0L);
		
		int attr = 0;
		
		if ( details.isDirectory())
			attr += FileAttribute.Directory;
		
		if ( avmVersion != AVMContext.VERSION_HEAD)
			attr += FileAttribute.ReadOnly;
		
		setAttributes( attr);
	}

	/**
	 * Check if there is an open file channel to the content
	 * 
	 * @return boolean
	 */
	public final boolean hasContentChannel()
	{
		return m_channel != null ? true : false;
	}
	
	/**
	 * Return the mime type
	 * 
	 * @return String
	 */
	public final String getMimeType()
	{
		return m_mimeType;
	}
	
	/**
	 * Set the mime type
	 * 
	 * @param mimeType String
	 */
	public final void setMimeType(String mimeType)
	{
		m_mimeType = mimeType;
	}
	
	/**
     * Open the file
     * 
     * @param createFlag boolean
     * @exception IOException
     */
    public void openFile(boolean createFlag)
    	throws IOException
    {
    	// Nothing to do, content is opened on first read/write
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
    public int readFile(byte[] buf, int len, int pos, long fileOff)
    	throws java.io.IOException
    {
    	// DEBUG
    	
    	if ( logger.isDebugEnabled())
    		logger.debug("Read file " + getName() + ", len=" + len + ", offset=" + fileOff);
    	
        // Open the channel for reading
        
        openContent(false, false);
        
        // Read from the channel
        
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf, pos, len);
        int count = m_channel.read(byteBuffer, fileOff);
        if (count < 0)
        {
        	// Return a zero count at end of file
        	
            count = 0;
        }
        
        // Return the length of data read
        
        return count;
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
    public void writeFile(byte[] buf, int len, int pos, long fileOff)
    	throws java.io.IOException
    {
    	// DEBUG
    	
    	if ( logger.isDebugEnabled())
    		logger.debug("Write file " + getName() + ", len=" + len + ", offset=" + fileOff);
    	
        // Open the channel for writing
        
        openContent(true, false);
        
        // Write to the channel
        
        ByteBuffer byteBuffer = ByteBuffer.wrap(buf, pos, len);
        m_channel.write(byteBuffer, fileOff);
        
        // Set modification flag
        
        m_modified = true;
        incrementWriteCount();
        
        // Update the current file size
        
        setFileSize( m_channel.size());
    }

    /**
     * Seek to the specified file position.
     * 
     * @param pos long
     * @param typ int
     * @return int
     * @exception IOException
     */
    public long seekFile(long pos, int typ)
    	throws IOException
    {
        //  Open the file, if not already open

        openContent( false, false);

        //  Check if the current file position is the required file position

        long curPos = m_channel.position();
        
        switch (typ) {

          //  From start of file

          case SeekType.StartOfFile :
            if (curPos != pos)
              m_channel.position( pos);
            break;

            //  From current position

          case SeekType.CurrentPos :
            m_channel.position( curPos + pos);
            break;

            //  From end of file

          case SeekType.EndOfFile :
            {
              long newPos = m_channel.size() + pos;
              m_channel.position(newPos);
            }
            break;
        }

        //  Return the new file position

        return m_channel.position();
    }

    /**
     * Flush any buffered output to the file
     * 
     * @throws IOException
     */
    public void flushFile()
    	throws IOException
    {
    	// If the file channel is open for write then flush the channel
    	
    	if ( m_channel != null && m_writable)
    		m_channel.force( false);
    }
    
    /**
     * Truncate the file to the specified file size
     * 
     * @param siz long
     * @exception IOException
     */
    public void truncateFile(long siz)
    	throws IOException
    {
    	// DEBUG
    	
    	if ( logger.isDebugEnabled())
    		logger.debug("Truncate file " + getName() + ", size=" + siz);
    	
        // If the content data channel has not been opened yet and the requested size is zero
        // then this is an open for overwrite so the existing content data is not copied
        
        if ( m_channel == null && siz == 0L)
        {
            // Open content for overwrite, no need to copy existing content data
            
            openContent(true, true);
        }
        else
        {
            // Normal open for write
            
            openContent(true, false);

            // Truncate or extend the channel
            
            m_channel.truncate(siz);
        }
        
        // Set modification flag
        
        m_modified = true;
        incrementWriteCount();
    }

    /**
     * Close the database file
     */
    public void closeFile()
    	throws IOException
    {
    	// If the file is a directory or the file channel has not been opened then there is nothing to do
    	
    	if ( isDirectory() || m_channel == null && m_content == null)
    		return;
    	
        // We may be in a retry block, in which case this section will already have executed and channel will be null
        if (m_channel != null)
        {
            // Close the file channel
            
            try
            {
                m_channel.close();
                m_channel = null;
            }
            catch ( IOException ex)
            {
                if (RetryingTransactionHelper.extractRetryCause(ex) != null)
                {
                    throw ex;
                }
                logger.error("Failed to close file channel for " + getName(), ex);
            }

        }

        if (m_content != null)
        {
            // Retrieve the content data and stop the content URL from being 'eagerly deleted', in case we need to
            // retry the transaction

            final ContentData contentData = m_content.getContentData();

            try
            {
                NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, m_avmPath);                
                m_nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, contentData);
            }
            catch (ContentQuotaException qe)
            {
                throw new DiskFullException(qe.getMessage());
            }

            // Tidy up after ourselves after a successful commit. Otherwise leave things to allow a
            AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter()
            {
                @Override
                public void afterCommit()
                {
                    m_content = null;
                }
            });
        }
    }
    
    /**
     * Open a file channel to the file content, switching to a writable file channel if required.
     * 
     * @param write boolean
     * @param trunc boolean
     * @throws AccessDeniedException If this network file is read only
     * @throws AlfrescoRuntimeException If this network file represents a directory
     */
    private void openContent(boolean write, boolean trunc)
    	throws AccessDeniedException, AlfrescoRuntimeException
    {
    	// Check if this network file is a directory, no content to open
    	
        if ( isDirectory())
            throw new AlfrescoRuntimeException("Unable to open channel for a directory network file: " + this);
        
        // Check if write access is required and the current channel is read-only

        long curPos = 0L;
        
        if ( write && m_writable == false && m_channel != null)
        {
            // Close the existing read-only channel
            
            try
            {
            	// Save the current file position
            	
            	curPos = m_channel.position();
            	
            	// Close the read-only file channel
            	
                m_channel.close();
                m_channel = null;
            }
            catch (IOException ex)
            {
                logger.error("Error closing read-only channel", ex);
            }
            
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Switching to writable channel for " + getName());
        }
        else if ( m_channel != null)
        {
            // File channel already open
        	
            return;
        }
        
        // We need to create the channel
        
        if (write && getGrantedAccess() == NetworkFile.READONLY)
            throw new AccessDeniedException("The network file was created for read-only: " + this);

        // Access the content data and get a file channel to the data
        
        if ( write )
        {
        	// Access the content data for write

        	m_content = null;
        	
        	try {
        		
        		// Create a writer to access the file data
        		
        	    m_content = m_avmService.getContentWriter(m_avmPath, false);
        		
        		// Set the mime-type

        	    m_content.setMimetype( getMimeType());
        	}
        	catch (Exception ex) {
        		logger.debug( ex);
        		ex.printStackTrace();
        		
        		// Rethrow exception, convert to access denied
        		
        		throw new AccessDeniedException("Failed to open file for write access, " + m_avmPath);
        	}

        	// Indicate that we have a writable channel to the file
            
            m_writable = true;
            
            // Get the writable channel, do not copy existing content data if the file is to be truncated
            
            m_channel = m_content.getFileChannel( trunc);
            
            // Reset the file position to match the read-only file channel position, unless we truncated the file
            
            if ( curPos != 0L && trunc == false)
            {
            	try
            	{
            		m_channel.position( curPos);
            	}
            	catch (IOException ex)
            	{
            		logger.error("Failed to set file position for " + getName(), ex);
            	}
            }
        }
        else
        {
        	// Access the content data for read
        	
            ContentReader cReader = m_avmService.getContentReader( m_avmVersion, m_avmPath);
            
            // Indicate that we only have a read-only channel to the data
            
            m_writable = false;
            
            // Get the read-only channel
            
            m_channel = cReader.getFileChannel();
        }
    }
    
    /**
     * Return the writable state of the content channel
     * 
     * @return boolean
     */
    public final boolean isWritable()
    {
    	return m_writable;
    }
    
    /**
     * Return the network file details as a string
     * 
     * @return String
     */
    public String toString()
    {
    	StringBuilder str = new StringBuilder();
    	
    	str.append( "[");
    	str.append( getName());
    	str.append( ":");
    	str.append( isDirectory() ? "Dir," : "File,");
    	str.append( getFileSize());
    	str.append( "-Channel=");
    	str.append( m_channel);
    	str.append( m_writable ? ",Write" : ",Read");
    	str.append( m_modified ? ",Modified" : "");
    	str.append( "]");
    	
    	return str.toString();
    }
}
