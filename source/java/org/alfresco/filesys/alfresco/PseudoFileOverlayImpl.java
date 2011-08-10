/*
 * Copyright (C) 2007-2010 Alfresco Software Limited.
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
package org.alfresco.filesys.alfresco;

import java.util.Enumeration;

import org.alfresco.filesys.repo.ContentDiskDriver2;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.pseudo.MemoryPseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFile;
import org.alfresco.jlan.server.filesys.pseudo.PseudoFileList;
import org.alfresco.jlan.util.WildCard;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Re-implementation of PseudoFiles for ContentDiskDriver2
 * 
 * Overlays "desktop actions"
 * 
 * @author mrogers
 * */
public class PseudoFileOverlayImpl implements PseudoFileOverlay
{
    private AlfrescoContext context;
    private NodeService nodeService;
    
    private static final Log logger = LogFactory.getLog(PseudoFileOverlayImpl.class);
    
    PseudoFileList pl = new PseudoFileList();
    
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", getNodeService());
        PropertyCheck.mandatory(this, "context", context);
    
        DesktopActionTable actions = context.getDesktopActions();

        if(actions != null)
        {
            Enumeration<String> actionNames = actions.enumerateActionNames();
  
            while(actionNames.hasMoreElements())
            {
                // Get the current desktop action      
                String name = actionNames.nextElement();
                DesktopAction action = actions.getAction(name);
      
                // Add the pseudo file for the desktop action
      
                if ( action.hasPseudoFile())
                {
                    PseudoFile file = action.getPseudoFile();
                    pl.addFile(file);
                }
            }
        }
    }
    
    private PseudoFile generateURLShortcut(NodeRef nodeRef)
    {
        if ( context.hasURLFile())
        {
            // Make sure the state has the associated node details
      
            // Build the URL file data
 
            StringBuilder urlStr = new StringBuilder();
      
            urlStr.append("[InternetShortcut]\r\n");
            urlStr.append("URL=");
            urlStr.append(context.getURLPrefix());
            urlStr.append("navigate/browse/workspace/SpacesStore/");
            urlStr.append( nodeRef.getId());
            urlStr.append("\r\n");

            // Create the in memory pseudo file for the URL link
          
            byte[] urlData = urlStr.toString().getBytes();
          
            MemoryPseudoFile urlFile = new MemoryPseudoFile( context.getURLFileName(), urlData);
            return urlFile;
        }
        return null;
    }
   
    /**
     * 
     */
    public boolean isPseudoFile(NodeRef parentDir, String name)
    {
        if ( parentDir == null) 
        {
            return false;
        }

        if(getPseudoFile(parentDir, name) != null)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Get the pseudo file
     * @param path
     * @param nodeRef
     * @return the pseudoFile or null if there is no pseudo file
     */
    public PseudoFile getPseudoFile(NodeRef parentDir, String fname)
    {       
        if ( parentDir == null)
        {
            return null;
        }
        
        if(context.hasURLFile())
        {
            if(context.getURLFileName().equals(fname))
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("returning URL pseudo file");
                }
                return generateURLShortcut(parentDir);
            }
        }
        
        PseudoFile file = pl.findFile(fname, false);
        return file;
    }
    
    /**
     * 
     */
    public PseudoFileList searchPseudoFiles(NodeRef parentDir, String name)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("searchPseudoFile parentDir:" + parentDir +", name:" + name);
        }
        //return pseudo files matching the path/pattern
                
        if ( parentDir == null || name == null || name.length() == 0 || name.equals("\\"))
        {
            return null;
        }
        
        String fname = name;
        
        if ( fname.equals( "*.*"))
        {
            fname = "*";
        }
        
        if ( WildCard.containsWildcards(fname))
        {
            // does contain wildcards
            
            // Check if the wildcard is for all files or a subset
           
            if ( fname.equals( "*"))
            {
                // Match all pseudo files
                PseudoFileList filterList = new PseudoFileList();
                
                // copy desktop actions  which do not depend on parentDir
                for ( int i = 0; i < pl.numberOfFiles(); i++)
                {
                    PseudoFile pseudoFile = pl.getFileAt(i);
                    filterList.addFile(pseudoFile);
                }
                
                // The URL file is dependent upon the parent dir
                if(context.hasURLFile())
                {
                    filterList.addFile(generateURLShortcut(parentDir));
                }
                    
                return filterList;
            }
            else
            {
                // Generate a subset of pseudo files that match the wildcard search pattern
                
                WildCard wildCard = new WildCard( fname, false);
                PseudoFileList filterList = new PseudoFileList();
                
                for ( int i = 0; i < pl.numberOfFiles(); i++)
                {
                    PseudoFile pseudoFile = pl.getFileAt( i);
                    if ( wildCard.matchesPattern( pseudoFile.getFileName()))
                    {
                        // Add the pseudo file to the filtered list        
                        filterList.addFile( pseudoFile);
                    }
                }
                
                // The URL file is dependent upon the parent dir
                if(context.hasURLFile())
                {
                    if(wildCard.matchesPattern(context.getURLFileName()))
                    {
                        filterList.addFile(generateURLShortcut(parentDir));
                    }
                }
                
                return filterList;
                // Use the filtered pseudo file list, or null if there were no matches
            }
        }
        else 
        {
             // does not contain wild cards  
            PseudoFileList filterList = new PseudoFileList();
            PseudoFile file = getPseudoFile(parentDir, fname);
            
            if(file != null)
            {
               filterList.addFile(file);       
            }
            
            return filterList;
        }
    }
   
    //
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setContext(AlfrescoContext context)
    {
        this.context = context;
    }

    public AlfrescoContext getContext()
    {
        return context;
    }
}
