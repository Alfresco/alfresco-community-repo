/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Checking filename returned by " + methodName + ": " + filename);
            }
            
            // check against all the regular expressions
            for (String regexp : filterRegularExpressions)
            {
                if (!filename.matches(regexp))
                {
                    // it is not a match - try next one
                    continue;
                }
                else
                {
                    // it matched, so apply the aspect
                    NodeRef nodeRef = fileInfo.getNodeRef();
                    nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
                    // no further checking required
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Applied temporary marker: " + fileInfo);
                    }
                    break;
                }
            }
        }
        // done
        return ret;
    }
}
