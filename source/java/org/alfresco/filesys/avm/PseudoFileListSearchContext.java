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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.filesys.avm;

import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.server.pseudo.PseudoFile;
import org.alfresco.filesys.server.pseudo.PseudoFileList;
import org.alfresco.filesys.util.WildCard;

/**
 * Pseudo File List Search Context Class
 * 
 * <p>Returns files from a pseudo file list for a wildcard search.
 *
 * @author gkspencer
 */
public class PseudoFileListSearchContext extends SearchContext {

	// Pseudo file list and current index
	
	private PseudoFileList m_fileList;
	private int m_fileIdx;
	
	// File attributes
	
	private int m_attrib;
	
	// Optional wildcard filter
	
	private WildCard m_filter;
	
	// Mark all files/folders as read-only
	
	private boolean m_readOnly;
	
	/**
	 * Class constructor
	 * 
	 * @param fileList PseudoFileList
	 * @param attrib int
	 * @param filter WildCard
	 * @param readOnly boolean
	 */
	public PseudoFileListSearchContext( PseudoFileList fileList, int attrib, WildCard filter, boolean readOnly)
	{
		m_attrib   = attrib;
		m_filter   = filter;
		m_fileList = fileList;
		m_readOnly = readOnly;
	}
	
    /**
     * Determine if there are more files for the active search.
     * 
     * @return boolean
     */
    public boolean hasMoreFiles()
    {
    	return m_fileIdx < m_fileList.numberOfFiles() ? true : false;
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
    	// Check if there is another file record to return
    	
    	if ( m_fileIdx >= m_fileList.numberOfFiles())
    		return false;

    	// Search for the next valid file
    	
        boolean foundMatch = false;
    	PseudoFile curFile = null;
    	
        while (foundMatch == false && m_fileIdx < m_fileList.numberOfFiles())
        {
        	// 	Get the next file from the list
        	
        	curFile = m_fileList.getFileAt( m_fileIdx++);
        	
        	//	Check if the file name matches the search pattern
  				
  			if ( m_filter != null)
  			{
  				// Check if the current file matches the wildcard pattern
  				
  				if ( m_filter.matchesPattern(curFile.getFileName()) == true)
  				{
	  				//  Check if the file matches the search attributes
	  	
	  				if (FileAttribute.hasAttribute(m_attrib, FileAttribute.Directory) &&
	  					curFile.isDirectory())
	  				{
	  	
	  					//  Found a match
	  	
	  					foundMatch = true;
	  				}
	  				else if ( curFile.isFile())
	  				{
	  	
	  					//  Found a match
	  	
	  					foundMatch = true;
	  				}
	
	  				//	Check if we found a match
	  				
	  				if ( foundMatch == false)
	  				{
	  					
	  					//  Get the next file from the list
	
	  					if ( ++m_fileIdx < m_fileList.numberOfFiles())
	  			        	curFile = m_fileList.getFileAt( m_fileIdx);
	  				}
  				}
  			}
  			else
  				foundMatch = true;
        }

        // If we found a match then fill in the file information

        if ( foundMatch)
        {
        	// Fill in the file information
        	
        	info.setFileName( curFile.getFileName());
        	
        	// Get the file information from the pseudo file
        	
        	FileInfo pfInfo = curFile.getFileInfo();
        	
        	if ( curFile.isFile())
        	{
        		info.setFileSize( pfInfo.getSize());
        		info.setAllocationSize( pfInfo.getAllocationSize());
        	}
        	else
        		info.setFileSize( 0L);

        	info.setAccessDateTime( pfInfo.getAccessDateTime());
        	info.setCreationDateTime( pfInfo.getCreationDateTime());
        	info.setModifyDateTime( pfInfo.getModifyDateTime());

        	// Build the file attributes
        	
        	int attr = pfInfo.getFileAttributes();
        	
        	if ( pfInfo.isHidden() == false &&
        			curFile.getFileName().startsWith( ".") ||
        			curFile.getFileName().equalsIgnoreCase( "Desktop.ini") ||
        			curFile.getFileName().equalsIgnoreCase( "Thumbs.db"))
        		attr += FileAttribute.Hidden;

        	// Check if the file/folder should be marked as read-only
        	
        	if ( m_readOnly && pfInfo.isReadOnly() == false)
        		attr += FileAttribute.ReadOnly;
        	
        	info.setFileAttributes( attr);
        	info.setFileId( pfInfo.getFileId());
        }
        
        // Indicate if the file information is valid
        
    	return foundMatch;
    }

    /**
     * Return the file name of the next file in the active search. Returns null is the search is
     * complete.
     * 
     * @return String
     */
    public String nextFileName()
    {
    	// Check if there is another file record to return
    	
    	//	Find the next matching file name
    	
    	while ( m_fileIdx < m_fileList.numberOfFiles()) {
    		
    		//	Check if the current file name matches the search pattern
    		
    		String fname = m_fileList.getFileAt( m_fileIdx++).getFileName();
    		
    		if ( m_filter.matchesPattern(fname))
    			return fname;
    	}
    	
    	// No more matching file names
    	
    	return null;
    }

    /**
     * Return the total number of file entries for this search if known, else return -1
     * 
     * @return int
     */
    public int numberOfEntries()
    {
        return m_fileList.numberOfFiles();
    }

    /**
     * Return the resume id for the current file/directory in the search.
     * 
     * @return int
     */
    public int getResumeId()
    {
    	return m_fileIdx;
    }
    
    /**
     * Restart a search at the specified resume point.
     * 
     * @param resumeId Resume point id.
     * @return true if the search can be restarted, else false.
     */
    public boolean restartAt(int resumeId)
    {
    	// Range check the resume id
    	
    	int resId = resumeId - 1;
    	
    	if ( resId < 0 || resId >= m_fileList.numberOfFiles())
    		return false;
    	
    	// Reset the current file index
    	
    	m_fileIdx = resId;
    	return true;
    }

    /**
     * Restart the current search at the specified file.
     * 
     * @param info File to restart the search at.
     * @return true if the search can be restarted, else false.
     */
    public boolean restartAt(FileInfo info)
    {
    	// Search backwards from the current file
    	
    	int curFileIdx = m_fileIdx;

        if (m_fileIdx >= m_fileList.numberOfFiles())
        {
            m_fileIdx = m_fileList.numberOfFiles() - 1;
        }
        
    	while ( m_fileIdx > 0) {
    		
    		// Check if the current file is the required search restart point
    		
    		if ( m_fileList.getFileAt( m_fileIdx).getFileName().equals( info.getFileName()))
    			return true;
    		else
    			m_fileIdx--;
    	}
    	
    	// Failed to find the restart file
    	
    	m_fileIdx = curFileIdx;
    	return false;
    }
}
