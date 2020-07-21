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

package org.alfresco.filesys.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Ms Office Content Network File Class
 * 
 * <p>Provides special handling for MS Office files that are written to by the app even though the user does not
 * change the file.
 * 
 * @author gkspencer
 */
public class MSOfficeContentNetworkFile extends ContentNetworkFile {

    private static final Log logger = LogFactory.getLog(MSOfficeContentNetworkFile.class);
	
	// Count of file reads
	
	private int m_readCnt;
	
	// Buffered write list
	
	private List<BufferedWrite> m_writeList;
	
    /**
     * Class constructor
     * 
     * @param nodeService NodeService
     * @param contentService ContentService
     * @param mimetypeService MimetypeService
     * @param nodeRef NodeRef
     * @param name String
     */
    protected MSOfficeContentNetworkFile(
            NodeService nodeService,
            ContentService contentService,
            MimetypeService mimetypeService,
            NodeRef nodeRef,
            String name)
    {
        super(nodeService, contentService, mimetypeService, nodeRef, name);

        // Create the buffered write list
        
        m_writeList = new ArrayList<BufferedWrite>(); 
    }

    /**
     * Return the file read count
     * 
     * @return int
     */
    public final int getReadCount() {
    	return m_readCnt;
    }
    
    /**
     * Read from the file.
     * 
     * @param buffer byte[]
     * @param length int
     * @param position int
     * @param fileOffset long
     * @return Length of data read.
     * @exception IOException
     */
    public int readFile(byte[] buffer, int length, int position, long fileOffset)
    	throws IOException
    {
    	//	Update the read count
    	
    	m_readCnt++;
    	
    	// Chain to the standard read
    	
    	return super.readFile( buffer, length, position, fileOffset);
    }

    /**
     * Write a block of data to the file.
     * 
     * @param buffer byte[]
     * @param length int
     * @param position int
     * @param fileOffset long
     * @exception IOException
     */
    public void writeFile(byte[] buffer, int length, int position, long fileOffset)
    	throws IOException
    {
    	// Check if writes are being buffered
    	
    	if ( m_writeList != null) {
    		
    		// Check if the write should be buffered
    		
    		if ( getReadCount() > 0 && m_writeList.size() < 2) {
    		
	    		// Buffer the write, looks like a file open update. Do not buffer zero length writes.

    			if ( length != 0) {
		    		byte[] data = new byte[ length];
		    		System.arraycopy(buffer, position, data, 0, length);
		    		
		    		BufferedWrite bufWrite = new BufferedWrite( data, fileOffset);
		    		m_writeList.add( bufWrite);
		    		
		    		// DEBUG
		    		
		    		if ( logger.isDebugEnabled())
		    			logger.debug("MSOfficeFile: Buffered write=" + bufWrite + ", cnt=" + m_writeList.size() + ", readCnt=" + getReadCount());
    			}
    			else if ( logger.isDebugEnabled())
    				logger.debug("MSOfficeFile: Ignored zero length write");
    			
	    		return;
	    	}
    	
	    	// Check if there are any buffered writes to be flushed
	    	
	    	if ( m_writeList.size() > 0) {
	    		
	    		// Write out the buffered writes first
	    		
	    		while ( m_writeList.size() > 0) {
	    			
	    			// Get the current buffered write
	    			
	    			BufferedWrite bufWrite = m_writeList.remove( 0);
	    			
	    			try {
	    				
	    				// Write the buffered data to the file
	    				
	    				super.writeFile( bufWrite.getData(), bufWrite.getDataLength(), 0, bufWrite.getOffset());
	    			}
	    			catch ( Exception ex) {
	
	    				// DEBUG
	    				
	    				if ( logger.isDebugEnabled())
	    					logger.debug("MSOfficeFile: Buffered write error, " + ex.getMessage());
	    			}
	    		}
	    		
	    		// DEBUG
	    		
	    		if ( logger.isDebugEnabled())
	    			logger.debug("MSOfficeFile: Buffered writes flushed");
	    		
	    		// Disable any more buffered writes
	    		
	    		m_writeList = null;
	    	}
    	}
    	
    	// Now do the current write
    	
    	super.writeFile(buffer, length, position, fileOffset);
    }

    /**
     * Close the file
     * 
     * @exception IOException
     */
    public void closeFile()
    	throws IOException
    {
    	// DEBUG
    	
    	if ( logger.isDebugEnabled() && m_writeList != null)
    		logger.debug("MSOfficeFile: Discarded buffered writes - " + m_writeList.size());
    	
    	// Chain to the standard close
    	
    	super.closeFile();
    }
}
