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

import java.io.FileNotFoundException;
import java.util.List;

import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileType;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapper for simple XPath searche against the node service.  The search is performed statically
 * outside the context instance itself - this class merely maintains the state of the search
 * results across client connections.
 * 
 * @author Derek Hulley
 */
public class ContentSearchContext extends SearchContext 
    implements InFlightCorrectable
{
	// Debug logging
	
    private static final Log logger = LogFactory.getLog(ContentSearchContext.class);

    // Constants
    //
    // Link file size, actual size will be set if/when the link is opened
    
    public final static int LinkFileSize	= 512;
    
    private InFlightCorrector corrector;
    
    public void setInFlightCorrector(InFlightCorrector corrector)
    {
        this.corrector = corrector;
    }
    
    // List of nodes returned from the folder search
    
    private CifsHelper cifsHelper;
    private List<NodeRef> results;
    private int index = -1;

    private boolean lockedFilesAsOffline;
    
    // Resume id
    
    private int resumeId;
    
    // Relative path being searched
    
    private String m_relPath;
    
    // Keep track of the last file name returned for fast restartAt processing
    
    private String m_lastFileName;
    
    /**
     * Class constructor
     * 
     * @param cifsHelper Filesystem helper class
     * @param results List of file/folder nodes that match the search pattern
     * @param searchStr Search path
     * @param relPath Relative path being searched
     */
    protected ContentSearchContext(
            CifsHelper cifsHelper,
            List<NodeRef> results,
            String searchStr,
            String relPath,
            boolean lockedFilesAsOffline)
    {
        super();
        super.setSearchString(searchStr);
        this.cifsHelper = cifsHelper;
        this.results    = results;
        this.lockedFilesAsOffline = lockedFilesAsOffline;

		m_relPath = relPath;
		if ( m_relPath != null && m_relPath.endsWith( FileName.DOS_SEPERATOR_STR) == false)
			m_relPath = m_relPath + FileName.DOS_SEPERATOR_STR;
    }
    
    /**
     * Return the search as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(60);

        sb.append("[ContentSearchContext searchStr=");
        sb.append(getSearchString());
        sb.append(", resultCount=");
        sb.append(results.size());
        sb.append("]");
        
        return sb.toString();
    }

    /**
     * Return the resume id for the current file/directory in the search.
     * 
     * @return int
     */
    public int getResumeId()
    {
        return resumeId;
    }

    /**
     * Determine if there are more files for the active search.
     * 
     * @return boolean
     */
    public boolean hasMoreFiles()
    {
        return index < (results.size() - 1);
    }

    /**
     * Return file information for the next file in the active search. Returns false if the search
     * is complete.
     * 
     * @param info FileInfo to return the file information.
     * @return true if the file information is valid, else false
     */
    public boolean nextFileInfo(FileInfo info)
    {
        // Check if there is anything else to return
        
        if (!hasMoreFiles())
        {
            return false;
        }

        // Increment the index and resume id
        
        index++;
        resumeId++;

        // Return the next available file information for a real file/folder
        
        try
        {
        	// Loop until we get a valid node, might have been deleted since the initial folder search

        	ContentFileInfo nextInfo = null;
        	NodeRef nextNodeRef = null;
        	
        	while ( nextInfo == null && index < results.size())
        	{
        		//	Get the next node from the search
        	
        		nextNodeRef = results.get(index);
            
        		try {

        			// Get the file information and copy across to the caller's file info
    	            
		            nextInfo = cifsHelper.getFileInformation(nextNodeRef, "", false, lockedFilesAsOffline);
		            info.copyFrom(nextInfo);

		            /**
		             * Apply in flight correction
		             */
		            if(corrector != null)
		            {
		                corrector.correct(info, m_relPath);
		            }
		            
	        	}
	        	catch ( InvalidNodeRefException ex) {

	        		// Log a warning
	        		
	        		if ( logger.isWarnEnabled())
	        		{
	        			logger.warn("Noderef " + nextNodeRef + " no longer valid, ignoring");
	        		}
	        		
	        		// Update the node index, node no longer exists, try the next node in the search
	        		
	        		index++;
	        		resumeId++;
	        	}
        	}
        	
        	// Check if we have finished returning file info
        	
        	if ( nextInfo == null)
        	{
        		return false;
        	}
        	
        	// Generate a file id for the current file
        	
        	StringBuilder pathStr = new StringBuilder( m_relPath);
        	pathStr.append ( info.getFileName());
        	
        	// Check if this is a link node
        	if ( nextInfo.isLinkNode())
        	{
        		// Set a dummy file size for the link data that will be generated if/when the file is opened
        		
        		info.setFileSize( LinkFileSize);
        		
        		// Make the link read-only
        		
        		if ( info.isReadOnly() == false)
        			info.setFileAttributes( info.getFileAttributes() + FileAttribute.ReadOnly);
        		
        		// Set the file type to indicate a symbolic link
        		
        		info.setFileType( FileType.SymbolicLink);
        	}
        	else
        		info.setFileType( FileType.RegularFile);
        	
        	// Keep track of the last file name returned
        	
        	m_lastFileName = info.getFileName();
        	
        	// Indicate that the file information is valid
            
            return true;
        }
        catch (FileNotFoundException e)
        {
        }
        
        // File information is not valid
        
        return false;
    }

    /**
     * Return the file name of the next file in the active search. Returns null is the search is
     * complete.
     * 
     * @return String
     */
    public String nextFileName()
    {
        // Check if there is anything else to return
        
        if (!hasMoreFiles())
            return null;

        // Increment the index and resume id
        
        index++;
        resumeId++;

        // Get the next file info from the node search
            
        NodeRef nextNodeRef = results.get(index);
        
        try
        {
            // Get the file information and copy across to the callers file info
            
            FileInfo nextInfo = cifsHelper.getFileInformation(nextNodeRef, "", false, false);

            // Keep track of the last file name returned
            
            m_lastFileName = nextInfo.getFileName();
            
            // Indicate that the file information is valid
            
            return nextInfo.getFileName();
        }
        catch (FileNotFoundException e)
        {
        }
        
        // No more files
        
        return null;
    }

    /**
     * Restart a search at the specified resume point.
     *
     * @param info File to restart the search at.
     * @return true if the search can be restarted, else false.
     */
    public boolean restartAt(FileInfo info)
    {
        int resId = 0;
        
        // Check if the resume file name is the last file returned
        
        if ( m_lastFileName != null && info.getFileName().equalsIgnoreCase( m_lastFileName)) {

        	// Reset the index/resume id
        	
            index = index - 1;
            resumeId = resumeId - 1;

            // DEBUG
        	
        	if ( logger.isDebugEnabled())
        		logger.debug("Fast search restart - " + m_lastFileName);
        	return true;
        }
        
        // Check if the resume file is in the main file list
        
        if ( results != null)
        {
            int idx = 0;
            
            while ( idx < results.size())
            {
                // Get the file name for the node
                
                String fname = cifsHelper.getFileName( results.get( idx));
                if ( fname != null && fname.equals( info.getFileName()))
                {
                    index = idx - 1;
                    resumeId = resId - 1;

                    return true;
                }
                else
                {
                    idx++;
                    resId++;
                }
            }
        }
        
        // Failed to find resume file
        
        return false;
    }

    /**
     * Restart the current search at the specified file.
     * 
     * @return true if the search can be restarted, else false.
     */
    public boolean restartAt(int resumeIdParameter)
    {
        // Resume ids are one based as zero has special meaning for some protocols, adjust the resume id
        
        resumeIdParameter--;
        
        // Check if the resume point is valid
        
        if ( results != null && resumeIdParameter < results.size())
        {
            index = resumeIdParameter;
            resumeId = resumeIdParameter + 1;
            return true;
        }
        
        // Invalid resume point
        
        return false;
    }

    /**
     * Return the relative path that is being searched
     * 
     * @return String
     */
    protected String getRelativePath() {
    	return m_relPath;
    }
    
    /**
     * Return the results array size
     * 
     * @return int
     */
    protected int getResultsSize() {
    	return results != null ? results.size() : 0;
    }
}
