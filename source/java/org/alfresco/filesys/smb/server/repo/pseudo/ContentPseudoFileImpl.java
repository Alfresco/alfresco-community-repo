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

package org.alfresco.filesys.smb.server.repo.pseudo;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.DiskDeviceContext;
import org.alfresco.filesys.server.filesys.DiskInterface;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.TreeConnection;
import org.alfresco.filesys.smb.server.repo.ContentContext;
import org.alfresco.filesys.smb.server.repo.ContentDiskDriver;
import org.alfresco.filesys.smb.server.repo.FileState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Content Filesystem Driver Pseudo File Implementation
 *
 * <p>Pseudo file implementation for the content disk driver.
 * 
 * @author gkspencer
 */
public class ContentPseudoFileImpl implements PseudoFileInterface
{
    // Logging
    
    private static final Log logger = LogFactory.getLog(ContentPseudoFileImpl.class);
    
    /**
     * Check if the specified path refers to a pseudo file
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     * @return boolean
     */
    public boolean isPseudoFile(SrvSession sess, TreeConnection tree, String path)
    {
        // Check if the path is for a pseudo file
        
        ContentContext ctx = (ContentContext) tree.getContext();
        boolean isPseudo = false;
        
        String[] paths = FileName.splitPath( path);
        FileState fstate = getStateForPath( ctx, paths[0]);
        
        if ( fstate != null && fstate.hasPseudoFiles())
        {
            // Check if there is a matching pseudo file
            
            PseudoFile pfile = fstate.getPseudoFileList().findFile( paths[1], false);
            if ( pfile != null)
                isPseudo = true;
        }
        
        // Return the pseudo file status
        
        return isPseudo;
    }

    /**
     * Return the pseudo file for the specified path, or null if the path is not a pseudo file
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     * @return PseudoFile
     */
    public PseudoFile getPseudoFile(SrvSession sess, TreeConnection tree, String path)
    {
        // Check if the path is for a pseudo file
        
        ContentContext ctx = (ContentContext) tree.getContext();
        
        String[] paths = FileName.splitPath( path);
        FileState fstate = getStateForPath( ctx, paths[0]);
        
        if ( fstate != null && fstate.hasPseudoFiles())
        {
            // Check if there is a matching pseudo file
            
            PseudoFile pfile = fstate.getPseudoFileList().findFile( paths[1], false);
            if ( pfile != null)
                return pfile;
        }
        
        // Not a pseudo file
        
        return null;
    }

    /**
     * Add pseudo files to a folder so that they appear in a folder search
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     * @return int
     */
    public int addPseudoFilesToFolder(SrvSession sess, TreeConnection tree, String path)
    {
        // Access the device context

        int pseudoCnt = 0;
        ContentContext ctx = (ContentContext) tree.getContext();
        
        if ( ctx.hasDragAndDropApp())
        {
            // If the file state is null create a file state for the path

            FileState fstate = getStateForPath( ctx, path);
            if ( fstate == null)
                ctx.getStateTable().findFileState( path, true, true);
            
            // Check if the folder name starts with 'DRAG', if so then enable the drag and drop pseudo file
            // for this folder
            
            String[] allPaths = FileName.splitAllPaths( path);
            String lastPath = allPaths[allPaths.length - 1].toUpperCase();
            
            if ( lastPath.startsWith("DRAG") && fstate.hasPseudoFiles() == false)
            {
                // Enable the drag and drop pseudo file
                
                fstate.addPseudoFile( ctx.getDragAndDropApp());

                // Update the count of pseudo files added
                
                pseudoCnt++;
                
                // DEBUG
                
                if ( logger.isInfoEnabled())
                    logger.info("Added drag/drop pseudo file for " + path);
            }
        }
        
        // Return the count of pseudo files added
        
        return pseudoCnt;
    }
    
    /**
     * Return the file state for the specified path
     * 
     * @param ctx ContentContext
     * @param path String
     * @return FileState
     */
    private final FileState getStateForPath(ContentContext ctx, String path)
    {
        // Check if there is a cached state for the path
        
        FileState fstate = null;
        
        if ( ctx.hasStateTable())
        {
            // Get the file state for a file/folder
            
            fstate = ctx.getStateTable().findFileState(path);
        }
        
        // Return the file state
        
        return fstate;
    }
}
