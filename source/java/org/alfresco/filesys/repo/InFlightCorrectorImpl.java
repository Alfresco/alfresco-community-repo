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

import java.util.Date;

import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.FileStateCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The in flight corrector corrects search results that have not yet been committed to the
 * repository.
 * 
 * It substitutes the "in flight" valuses from the state cache in place of the values committed to 
 * the repo
 * 
 * @author mrogers
 */
public class InFlightCorrectorImpl implements InFlightCorrector
{
    TreeConnection tree;
    
    private static final Log logger = LogFactory.getLog(InFlightCorrectorImpl.class);
    
    public InFlightCorrectorImpl(TreeConnection tree)
    {
        this.tree = tree;
    }
    public void correct(FileInfo info, String folderPath)
    {
        ContentContext tctx = (ContentContext) tree.getContext();
        
        String path = folderPath + info.getFileName();
        
        if(tctx.hasStateCache())
        {
            FileStateCache cache = tctx.getStateCache();
            FileState fstate = cache.findFileState( path, true);
            
            if(fstate != null)
            {
                logger.debug("correct " + path);
                /*
                 * What about stale file state values here?
                 */
                if(fstate.hasFileSize())
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("replace file size " + info.getSize() + " with " + fstate.getFileSize());
                    }
                    info.setFileSize(fstate.getFileSize());
                }
                if ( fstate.hasAccessDateTime())
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("replace access date " + new Date(info.getAccessDateTime()) + " with " + new Date(fstate.getAccessDateTime()));
                    }
                    info.setAccessDateTime(fstate.getAccessDateTime());
                }
                if ( fstate.hasChangeDateTime())
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("replace change date " + new Date(info.getChangeDateTime()) + " with " + new Date(fstate.getChangeDateTime()));
                    }
                    info.setChangeDateTime(fstate.getChangeDateTime());
                }
                if ( fstate.hasModifyDateTime())
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("replace modified date " + new Date(info.getModifyDateTime()) + " with " + new Date(fstate.getModifyDateTime()));
                    }
                    info.setModifyDateTime(fstate.getModifyDateTime());
                }
                if ( fstate.hasAllocationSize())
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("replace allocation size" + info.getAllocationSize() + " with " + fstate.getAllocationSize());
                    }
                    info.setAllocationSize(fstate.getAllocationSize());
                }
            }           
        }
    }

}
