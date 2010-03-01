/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.model.filefolder;

import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An interceptor for the {@link org.alfresco.service.cmr.model.FileFolderService FileFolderService}
 * that marks files or folders with the <b>sys:temporary</b> aspect depending on the
 * name pattern {@link #setFilterRegularExpressions(List) provided}.
 * 
 * @author Derek Hulley
 */
public class TempFileMarkerInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(TempFileMarkerInterceptor.class);
    
    private NodeService nodeService;
    private List<String> filterRegularExpressions;
    
    public TempFileMarkerInterceptor()
    {
        filterRegularExpressions = Collections.emptyList();
    }

    /**
     * @param nodeService the service to use to apply the <b>sys:temporary</b> aspect
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * A list of regular expressions that represent patterns of temporary files.
     * 
     * @param regexps list of regular expressions
     * 
     * @see String#matches(java.lang.String)
     */
    public void setFilterRegularExpressions(List<String> regexps)
    {
        this.filterRegularExpressions = regexps;
    }

    /**
     * Handles <b>rename</b>, <b>move</b>, <b>copy</b>
     */
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        Object ret = invocation.proceed();

        // execute and get the result
        String methodName = invocation.getMethod().getName();
        if (methodName.startsWith("create") ||
                methodName.startsWith("rename") ||
                methodName.startsWith("move") ||
                methodName.startsWith("copy"))
        {
            FileInfo fileInfo = (FileInfo) ret;
            String filename = fileInfo.getName();
            NodeRef nodeRef = fileInfo.getNodeRef();
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Checking filename returned by " + methodName + ": " + filename);
            }
            
            // check against all the regular expressions
            boolean matched = false;
            for (String regexp : filterRegularExpressions)
            {
                if (!filename.matches(regexp))
                {
                    // it is not a match - try next one
                    continue;
                }
                else
                {
                    matched = true;
                    // it matched, so apply the aspect
                    nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                    // no further checking required
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Applied temporary marker: " + fileInfo);
                    }
                    break;
                }
            }
            // If there was NOT a match then the file should not be marked as temporary
            // after any of the operations in question.
            if (!matched && nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY))
            {
                // Remove the aspect
                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_TEMPORARY);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Removed temporary marker: " + fileInfo);
                }
            }
        }
        // done
        return ret;
    }
}
