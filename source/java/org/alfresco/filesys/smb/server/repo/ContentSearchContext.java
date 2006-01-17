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
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.SearchContext;
import org.alfresco.filesys.smb.server.repo.pseudo.PseudoFile;
import org.alfresco.filesys.smb.server.repo.pseudo.PseudoFileList;
import org.alfresco.filesys.util.WildCard;
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
    
    /**
     * Performs a search against the direct children of the given node.
     * <p>
     * Wildcard characters are acceptable, and the search may either be for
     * a specific file or directory, or any file or directory.
     * 
     * @param serviceRegistry used to gain access the the repository
     * @param cifsHelper caches path query results
     * @param searchRootNodeRef the node whos children are to be searched
     * @param searchStr the search string relative to the search root node
     * @param attributes the search attributes, e.g. searching for folders, etc
     * @param searchFolderState File state of the folder being searched
     * @return Returns a search context with the results of the search
     */
    public static ContentSearchContext search(
            CifsHelper cifsHelper,
            NodeRef searchRootNodeRef,
            String searchStr,
            int attributes,
            FileState searchFolderState)
    {
        // Perform the search
        
        List<NodeRef> results = cifsHelper.getNodeRefs(searchRootNodeRef, searchStr);

        // Check if there are any pseudo files for the folder being searched.
        
        PseudoFileList pseudoList = null;
        
        if ( searchFolderState != null && searchFolderState.hasPseudoFiles())
        {
            // If it is a wildcard search use all pseudo files
            
            if ( WildCard.containsWildcards(searchStr))
            {
                // Check if the folder has any associated pseudo files
                
                pseudoList = searchFolderState.getPseudoFileList();
            }
            else if ( results == null || results.size() == 0)
            {
                // Check if the required file is in the pseudo file list
                
                String fname = searchStr;
                if ( fname.indexOf(FileName.DOS_SEPERATOR) != -1)
                {
                    String[] paths = FileName.splitPath( searchStr);
                    fname = paths[1];
                }
                
                if ( fname != null)
                {
                    // Search for a matching pseudo file
                    
                    PseudoFile pfile = searchFolderState.getPseudoFileList().findFile( fname, true);
                    if ( pfile != null)
                    {
                        // Create a file list with the required file
                        
                        pseudoList = new PseudoFileList();
                        pseudoList.addFile( pfile);
                    }
                }
            }
        }
        
        // Build the search context to store the results
        
        ContentSearchContext searchCtx = new ContentSearchContext(cifsHelper, results, searchStr, pseudoList);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Search context created: \n" +
                    "   search root: " + searchRootNodeRef + "\n" +
                    "   search context: " + searchCtx);
        }
        return searchCtx;
    }
    
    /**
     * @see ContentSearchContext#search(FilePathCache, NodeRef, String, int)
     */
    private ContentSearchContext(
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

    @Override
    public synchronized int getResumeId()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean hasMoreFiles()
    {
        // Pseudo files are returned first
        
        if ( pseudoList != null && index < (pseudoList.numberOfFiles() - 1))
            return true;
        return index < (results.size() -1);
    }

    @Override
    public synchronized boolean nextFileInfo(FileInfo info)
    {
        // check if there is anything else to return
        
        if (!hasMoreFiles())
            return false;

        // Increment the index
        
        index++;
        
        // If the pseudo file list is valid return the pseudo files first
        
        if ( pseudoList != null)
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
                    return true;
                }
            }
            else
            {
                // Switch to the main file list
                
                pseudoList = null;
                index = 0;
                
                if ( results == null || results.size() == 0)
                    return false;
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

    @Override
    public synchronized String nextFileName()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean restartAt(FileInfo info)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean restartAt(int resumeId)
    {
        throw new UnsupportedOperationException();
    }
}
