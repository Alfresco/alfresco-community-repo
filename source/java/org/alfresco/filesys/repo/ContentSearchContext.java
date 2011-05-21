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
import java.util.List;

import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileType;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFileList;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
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
{
	// Debug logging
	
    private static final Log logger = LogFactory.getLog(ContentSearchContext.class);

    // Constants
    //
    // Link file size, actual size will be set if/when the link is opened
    
    public final static int LinkFileSize	= 512;
    
    // List of nodes returned from the folder search
    
    private CifsHelper cifsHelper;
    private List<NodeRef> results;
    private int index = -1;

    // Pseudo file list blended into a wildcard folder search
    
    private PseudoFileList pseudoList;
    private boolean donePseudoFiles = false;
    
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
     * @param pseudoList List of pseudo files to be blended into the returned list of files
     * @param relPath Relative path being searched
     */
    protected ContentSearchContext(
            CifsHelper cifsHelper,
            List<NodeRef> results,
            String searchStr,
            PseudoFileList pseudoList,
            String relPath)
    {
        super();
        super.setSearchString(searchStr);
        this.cifsHelper = cifsHelper;
        this.results    = results;
        this.pseudoList = pseudoList;

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
        sb.append(", pseudoList=");
        if ( pseudoList != null)
        	sb.append( pseudoList.numberOfFiles());
        else
        	sb.append("NULL");
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
        // Pseudo files are returned first
        
        if ( donePseudoFiles == false && pseudoList != null && index < (pseudoList.numberOfFiles() - 1))
            return true;
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
            return false;

        // Increment the index and resume id
        
        index++;
        resumeId++;
        
        // If the pseudo file list is valid return the pseudo files first
        
        if ( donePseudoFiles == false && pseudoList != null)
        {
            if ( index < pseudoList.numberOfFiles())
            {
                PseudoFile pfile = pseudoList.getFileAt( index);
                if ( pfile != null)
                {
                    // Get the file information for the pseudo file
                    
                    FileInfo pinfo = pfile.getFileInfo();
                    
                    // Copy the file information to the callers file info
                    
                    info.copyFrom( pinfo);
                    
                	// Generate a file id for the current file
                	
                    if ( info != null && info.getFileId() == -1)
                    {
	                	StringBuilder pathStr = new StringBuilder( m_relPath);
	                	pathStr.append ( info.getFileName());
	                	
	                	info.setFileId( pathStr.toString().hashCode());
                    }
                    
                    // Check if we have finished with the pseudo file list, switch to the normal file list
                    
                    if ( index == (pseudoList.numberOfFiles() - 1))
                    {
                        // Switch to the main file list
                        
                        donePseudoFiles = true;
                        index = -1;
                    }
                    
                    // Indicate that the file information is valid
                    
                    return true;
                }
            }
        }

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

        			// Get the file information and copy across to the callers file info
    	            
		            nextInfo = cifsHelper.getFileInformation(nextNodeRef, "");
		            info.copyFrom(nextInfo);
	        	}
	        	catch ( InvalidNodeRefException ex) {

	        		// Log a warning
	        		
	        		if ( logger.isWarnEnabled())
	        			logger.warn("Noderef " + nextNodeRef + " no longer valid, ignoring");
	        		
	        		// Update the node index, node no longer exists, try the next node in the search
	        		
	        		index++;
	        		resumeId++;
	        	}
        	}
        	
        	// Check if we have finished returning file info
        	
        	if ( nextInfo == null)
        		return false;
        	
        	// Generate a file id for the current file
        	
        	StringBuilder pathStr = new StringBuilder( m_relPath);
        	pathStr.append ( info.getFileName());
        	
            // Set the file id
        	  
            long id = DefaultTypeConverter.INSTANCE.convert(Long.class, cifsHelper.getNodeService().getProperty(nextNodeRef, ContentModel.PROP_NODE_DBID));
            info.setFileId((int) (id & 0xFFFFFFFFL));

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
        
        // If the pseudo file list is valid return the pseudo files first
        
        if ( donePseudoFiles == false && pseudoList != null)
        {
            if ( index < pseudoList.numberOfFiles())
            {
                PseudoFile pfile = pseudoList.getFileAt( index);
                if ( pfile != null)
                {
                    // Get the file information for the pseudo file
                    
                    FileInfo pinfo = pfile.getFileInfo();
                    
                    // Copy the file information to the callers file info
                    
                    return pinfo.getFileName();
                }
            }
            else
            {
                // Switch to the main file list
                
                donePseudoFiles = true;
                index = -1;
                
                if ( results == null || results.size() == 0)
                    return null;
            }
        }

        // Get the next file info from the node search
            
        NodeRef nextNodeRef = results.get(index);
        
        try
        {
            // Get the file information and copy across to the callers file info
            
            FileInfo nextInfo = cifsHelper.getFileInformation(nextNodeRef, "");

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
     * @param resumeId Resume point id.
     * @return true if the search can be restarted, else false.
     */
    public boolean restartAt(FileInfo info)
    {
        //  Check if the resume point is in the pseudo file list

        int resId = 0;
        
        if (pseudoList != null)
        {
            while ( resId < pseudoList.numberOfFiles())
            {
                // Check if the current pseudo file matches the resume file name
                
                PseudoFile pfile = pseudoList.getFileAt(resId);
                if ( pfile.getFileName().equals(info.getFileName()))
                {
                    // Found the restart point
                    
                    donePseudoFiles = false;
                    index = resId - 1;
                    
                    return true;
                }
                else
                    resId++;
            }
        }
        
        // Check if the resume file name is the last file returned
        
        if ( m_lastFileName != null && info.getFileName().equalsIgnoreCase( m_lastFileName)) {

        	// Reset the index/resume id
        	
            index = index - 1;
            resumeId = resumeId - 1;
            donePseudoFiles = true;

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
                    donePseudoFiles = true;
                    
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
     * @param info File to restart the search at.
     * @return true if the search can be restarted, else false.
     */
    public boolean restartAt(int resumeId)
    {
        // Resume ids are one based as zero has special meaning for some protocols, adjust the resume id
        
        resumeId--;
        
        //  Check if the resume point is in the pseudo file list

        if (pseudoList != null)
        {
            if ( resumeId < pseudoList.numberOfFiles())
            {
                // Resume at a pseudo file
                
                index = resumeId;
                donePseudoFiles = false;
                
                return true;
            }
            else
            {
                // Adjust the resume id so that it is an index into the main file list
                
                resumeId -= pseudoList.numberOfFiles();
            }
        }
        
        // Check if the resume point is valid
        
        if ( results != null && resumeId < results.size())
        {
            index = resumeId;
            donePseudoFiles = true;
            
            return true;
        }
        
        // Invalid resume point
        
        return false;
    }
    
    /**
     * Check if the search is returning pseudo files or real file entries
     * 
     * @return boolean
     */
    protected boolean returningPseudoFiles() {
    	return donePseudoFiles ? true : false;
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
    
    /**
     * Return the pseudo file list size
     * 
     * @return int
     */
    protected int getPseudoListSize() {
    	return pseudoList != null ? pseudoList.numberOfFiles() : 0;
    }
}
