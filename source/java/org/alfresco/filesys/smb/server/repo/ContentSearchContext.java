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
     * @return Returns a search context with the results of the search
     */
    public static ContentSearchContext search(
            CifsHelper cifsHelper,
            NodeRef searchRootNodeRef,
            String searchStr,
            int attributes)
    {
        // perform the search
        List<NodeRef> results = cifsHelper.getNodeRefs(searchRootNodeRef, searchStr);
        
        // build the search context to store the results
        ContentSearchContext searchCtx = new ContentSearchContext(cifsHelper, results, searchStr);
        
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
            String searchStr)
    {
        super();
        super.setSearchString(searchStr);
        this.cifsHelper = cifsHelper;
        this.results = results;
    }
    
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
        return index < (results.size() -1);
    }

    @Override
    public synchronized boolean nextFileInfo(FileInfo info)
    {
        // check if there is anything else to return
        if (!hasMoreFiles())
        {
            return false;
        }
        // increment the index
        index++;
        // get the next file info
        NodeRef nextNodeRef = results.get(index);
        // get the file info

        try
        {
            FileInfo nextInfo = cifsHelper.getFileInformation(nextNodeRef, "");
            // copy to info handle
            info.copyFrom(nextInfo);
            
            // success
            return true;
        }
        catch (FileNotFoundException e)
        {
            return false;
        }
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
