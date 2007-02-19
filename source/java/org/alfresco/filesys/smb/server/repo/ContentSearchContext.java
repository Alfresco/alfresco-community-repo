/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb.server.repo;

import java.io.FileNotFoundException;
import java.util.List;

import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.FileType;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.server.pseudo.PseudoFile;
import org.alfresco.filesys.server.pseudo.PseudoFileList;
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
        sb.append("ContentSearchContext")
          .append("[ searchStr=").append(getSearchString())
          .append(", resultCount=").append(results.size())
          .append("]");
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

        // Get the next file info from the node search
            
        NodeRef nextNodeRef = results.get(index);
        
        try
        {
            // Get the file information and copy across to the callers file info
            
            ContentFileInfo nextInfo = cifsHelper.getFileInformation(nextNodeRef, "");
            info.copyFrom(nextInfo);
            
        	// Generate a file id for the current file
        	
        	StringBuilder pathStr = new StringBuilder( m_relPath);
        	pathStr.append ( info.getFileName());
        	
        	info.setFileId( pathStr.toString().hashCode());

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
}
