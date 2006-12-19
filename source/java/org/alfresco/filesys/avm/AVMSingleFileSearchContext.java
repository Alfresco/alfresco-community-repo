/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

package org.alfresco.filesys.avm;

import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;

/**
 * AVM Filesystem Single File Search Context Class
 * 
 * <p>Contains the details of a non-wildcard file/folder search, where there is only one result to return.
 *
 * @author GKSpencer
 */
public class AVMSingleFileSearchContext extends SearchContext {

	// Details of the single file/folder
	
	private AVMNodeDescriptor m_fileDetails;

	// Flag to indicate file details have been returned
	
	private boolean m_endOfSearch;
	
	// Relative path to the file/folder
	
	private String m_relativePath;
	
	/**
	 * Class constructor
	 * 
	 * @param fileDetails AVMNodeDescriptor
	 * @param relPath String
	 */
	public AVMSingleFileSearchContext( AVMNodeDescriptor fileDetails, String relPath)
	{
		m_fileDetails  = fileDetails;
		m_relativePath = relPath;
	}
	
    /**
     * Determine if there are more files for the active search.
     * 
     * @return boolean
     */
    public boolean hasMoreFiles()
    {
    	return m_endOfSearch == false ? true : false;
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
    	// Check if the file details have been returned
    	
    	if ( m_endOfSearch == true)
    		return false;

    	// Fill in the file information details
    	
    	info.setFileName( m_fileDetails.getName());
    	
    	if ( m_fileDetails.isFile())
    	{
    		info.setFileSize( m_fileDetails.getLength());
    		info.setAllocationSize((m_fileDetails.getLength() + 512L) & 0xFFFFFFFFFFFFFE00L);
    	}
    	else
    		info.setFileSize( 0L);

    	info.setAccessDateTime( m_fileDetails.getAccessDate());
    	info.setCreationDateTime( m_fileDetails.getCreateDate());
    	info.setModifyDateTime( m_fileDetails.getModDate());

    	// Build the file attributes
    	
    	int attr = 0;
    	
    	if ( m_fileDetails.isDirectory())
    		attr += FileAttribute.Directory;
    	
    	if ( m_fileDetails.getName().startsWith( ".") ||
    			m_fileDetails.getName().equalsIgnoreCase( "Desktop.ini") ||
    			m_fileDetails.getName().equalsIgnoreCase( "Thumbs.db"))
    		attr += FileAttribute.Hidden;
    	
    	info.setFileAttributes( attr);
    	info.setFileId( m_relativePath.hashCode());
    	
    	// Set the end of search flag, indicate that the file informatin is valid
    	
    	m_endOfSearch = true;
    	return true;
    }

    /**
     * Return the file name of the next file in the active search. Returns null is the search is
     * complete.
     * 
     * @return String
     */
    public String nextFileName()
    {
    	// Check if the file details have been returned
    	
    	if ( m_endOfSearch == true)
    		return null;

    	// Return the file/folder name, set the end of search flag
    	
    	m_endOfSearch = true;
    	return m_fileDetails.getName();
    }

    /**
     * Return the total number of file entries for this search if known, else return -1
     * 
     * @return int
     */
    public int numberOfEntries()
    {
        return 1;
    }

    /**
     * Return the resume id for the current file/directory in the search.
     * 
     * @return int
     */
    public int getResumeId()
    {
    	return 1;
    }
    
    /**
     * Restart a search at the specified resume point.
     * 
     * @param resumeId Resume point id.
     * @return true if the search can be restarted, else false.
     */
    public boolean restartAt(int resumeId)
    {
    	// Validate the resume id and clear the end of search flag
    	
    	if ( resumeId == 1)
    		m_endOfSearch = false;
    	else
    		return false;
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
    	return true;
    }
}
