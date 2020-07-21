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

import java.util.List;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.FileStateCache;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFileList;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cache Lookup Search Context Class
 * 
 *  <p>Use the file state cache to check for current timestamp values for file information being returned in
 *  the current search.
 * 
 * @author gkspencer
 */
public class CacheLookupSearchContext extends DotDotContentSearchContext {

	// Debug logging
	
    private static final Log logger = LogFactory.getLog(CacheLookupSearchContext.class);
    
	// File state cache
	
	private FileStateCache m_stateCache;
	
    /**
     * Class constructor
     * 
     * @param cifsHelper Filesystem helper class
     * @param results List of file/folder nodes that match the search pattern
     * @param searchStr Search path
     * @param pseudoList List of pseudo files to be blended into the returned list of files
     * @param relPath Relative path being searched
     * @param stateCache File state cache
     */
    protected CacheLookupSearchContext(
            CifsHelper cifsHelper,
            List<NodeRef> results,
            String searchStr,
            PseudoFileList pseudoList,
            String relPath,
            FileStateCache stateCache,
            boolean lockedFilesAsOffline)
    {
        super(cifsHelper, results, searchStr, pseudoList, relPath, lockedFilesAsOffline);
        super.setSearchString(searchStr);
        
        m_stateCache = stateCache;
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
    	// Get the file information for the next file
    	
    	if ( super.nextFileInfo( info) == false)
    		return false;
    	else if ( returningPseudoFiles() == true || m_stateCache == null)
    		return true;
    	
    	// We have a real file entry, check if there is a cache entry
    	
    	StringBuilder relPath = new StringBuilder( getRelativePath());
    	relPath.append( info.getFileName());
    	
    	FileState fstate = m_stateCache.findFileState( relPath.toString());
    	
    	if ( fstate != null)
    	{
    		// Check if there are current timestamps that can be used to override the database values
    		
    		if ( fstate.hasAccessDateTime())
    			info.setAccessDateTime( fstate.getAccessDateTime());
    		
    		if ( fstate.hasModifyDateTime())
    			info.setModifyDateTime( fstate.getModifyDateTime());
    		
            // File used/allocation size
            
    		if ( fstate.hasFileSize())
    			info.setFileSize( fstate.getFileSize());
    		
            if ( fstate.hasAllocationSize() && fstate.getAllocationSize() > info.getSize())
                info.setAllocationSize( fstate.getAllocationSize());
    		
    		// DEBUG
    		
    		if ( logger.isDebugEnabled())
    			logger.debug("Search timestamps from cache, path=" + info.getFileName());
    	}
    	
    	// Indicate that the file information is valid
    	
    	return true;
    }
    
     
    /**
     * Return the search as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(60);

        sb.append("[CacheLookupSearchContext searchStr=");
        sb.append(getSearchString());
        sb.append(", resultCount=");
        sb.append(getResultsSize());
        sb.append(", pseudoList=");
        if ( getPseudoListSize() != 0)
        	sb.append( getPseudoListSize());
        else
        	sb.append("NULL");
        sb.append(",cache=");
        sb.append(m_stateCache);
        
//        if ( super.getDotInfo(finfo)
//                != null)
//        	sb.append(",Dot");
//        if ( m_dotDotInfo != null)
//        	sb.append(",DotDot");
        sb.append("]");
        
        return sb.toString();
    }
}
