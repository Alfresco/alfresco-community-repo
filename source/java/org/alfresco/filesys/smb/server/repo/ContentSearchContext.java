/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.smb.server.repo;

import java.io.FileNotFoundException;
import java.util.List;

import org.alfresco.filesys.server.filesys.FileInfo;
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
    private static final Log logger = LogFactory.getLog(ContentSearchContext.class);

    private CifsHelper cifsHelper;
    private List<NodeRef> results;
    private int index = -1;

    // Pseudo file list blended into a wildcard folder search
    
    private PseudoFileList pseudoList;
    private boolean donePseudoFiles = false;
    
    // Resume id
    
    private int resumeId;
    
    /**
     * Class constructor
     * 
     * @param cifsHelper Filesystem helper class
     * @param results List of file/folder nodes that match the search pattern
     * @param searchStr Search path
     * @param pseudoList List of pseudo files to be blended into the returned list of files
     */
    protected ContentSearchContext(
            CifsHelper cifsHelper,
            List<NodeRef> results,
            String searchStr,
            PseudoFileList pseudoList)
    {
        super();
        super.setSearchString(searchStr);
        this.cifsHelper = cifsHelper;
        this.results    = results;
        this.pseudoList = pseudoList;
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
            
            FileInfo nextInfo = cifsHelper.getFileInformation(nextNodeRef, "");
            info.copyFrom(nextInfo);
            
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
