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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * OpenOffice Content Network File Class
 * 
 * <p>Provides special handling for OpenOffice file saves that open the file, truncate, close, then open the file
 * again to write the data, as this causes multiple versions to be generated when the file is versionable.
 * 
 * @author gkspencer
 */
public class OpenOfficeContentNetworkFile extends ContentNetworkFile {

    // Debug logging
    
    private static final Log logger = LogFactory.getLog(OpenOfficeContentNetworkFile.class);
    
    // Flag to indicate the last I/O operation was a truncate file to zero size
    
    private boolean m_truncateToZero;
    
    // Delayed file close count
    
    private int m_delayedClose;
    
    /**
     * Class constructor
     * 
     * @param nodeService NodeService
     * @param contentService ContentService
     * @param mimetypeService MimetypeService
     * @param nodeRef NodeRef
     * @param name String
     */
    protected OpenOfficeContentNetworkFile(
            NodeService nodeService,
            ContentService contentService,
            MimetypeService mimetypeService,
            NodeRef nodeRef,
            String name)
    {
        super(nodeService, contentService, mimetypeService, nodeRef, name);
        
        // DEBUG
        
        if (logger.isDebugEnabled())
            logger.debug("Using OpenOffice network file for " + name + ", versionLabel=" + nodeService.getProperty( nodeRef, ContentModel.PROP_VERSION_LABEL));
    }

    /**
     * Return the delayed close count
     * 
     * @return int
     */
    public final int getDelayedCloseCount() {
        return m_delayedClose;
    }
    
    /**
     * Increment the delayed close count
     */
    public final void incrementDelayedCloseCount() {
        m_delayedClose++;
        
        // Clear the truncate to zero status
        
        m_truncateToZero = false;
        
        // DEBUG
        
        if ( logger.isDebugEnabled())
            logger.debug("Increment delayed close count=" + getDelayedCloseCount() + ", path=" + getName());
    }
    
    /**
     * Check if the last file operation was a truncate to zero length
     * 
     * @return boolean
     */
    public final boolean truncatedToZeroLength() {
        return m_truncateToZero;
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
        // Clear the truncate flag
        
        m_truncateToZero = false;
        
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
        // Clear the truncate flag
        
        m_truncateToZero = false;
        
        // Chain to the standard write
        
        super.writeFile( buffer, length, position, fileOffset);
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
        // Chain to the standard truncate
        
        super.truncateFile( size);

        // Check for a truncate to zero length
        
        if ( size == 0L) {
            m_truncateToZero = true;
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("OpenOffice document truncated to zero length, path=" + getName());
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
        // DEBUG
        
        if ( logger.isDebugEnabled()) {
            logger.debug("Close OpenOffice file, " + getName() + ", delayed close count=" + getDelayedCloseCount() + ", writes=" + getWriteCount() +
                    ", modified=" + isModified());
            logger.debug("  Open count=" + getOpenCount() + ", fstate open=" + getFileState().getOpenCount());
        }
        
        // Chain to the standard close
        
        super.closeFile();
    }    
}
