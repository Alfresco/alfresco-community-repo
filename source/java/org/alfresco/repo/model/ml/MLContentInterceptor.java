/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.model.ml;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Replaces content readers according to the empty translation status.
 * 
 * @see ContentService#getReader(NodeRef, QName)
 * @see FileFolderService#getReader(NodeRef)
 * @since 2.1
 * @author Derek Hulley
 */
public class MLContentInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory.getLog(MLContentInterceptor.class);
    
    /** Direct access to the NodeService */
    private NodeService nodeService;
    /** Direct access to the ContentService */
    private ContentService contentService;
    /** Direct access to the ML Content Service */
    private MultilingualContentService multilingualContentService;
    
    public void setNodeService(NodeService bean)
    {
        this.nodeService = bean;
    }
    
    public void setContentService(ContentService directContentService)
    {
        this.contentService = directContentService;
    }

    public void setMultilingualContentService(MultilingualContentService directMultilingualContentService)
    {
        this.multilingualContentService = directMultilingualContentService;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        String methodName = invocation.getMethod().getName();
        Object[] args = invocation.getArguments();

        Object ret = null;
        
        if (methodName.equals("getReader"))
        {
            NodeRef nodeRef = (NodeRef) args[0];
            
            // Shortcut it if the node is not an empty translation
            if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
            {
                return invocation.proceed();
            }
            
            // Get the content property required
            QName propertyQName = null;
            if (args.length == 1)
            {
                // Assume that the default cm:content is required
                propertyQName = ContentModel.PROP_CONTENT;
            }
            else
            {
                // The request is specific
                propertyQName = (QName) args[1];
            }
            // Get the pivot translation
            NodeRef pivotNodeRef = multilingualContentService.getPivotTranslation(nodeRef);
            // Get the reader from that
            ContentReader pivotContentReader = contentService.getReader(pivotNodeRef, propertyQName);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Converted reader for empty translation: \n" +
                        "   Empty Translation: " + nodeRef + "\n" +
                        "   Pivot Translation: " + pivotNodeRef + "\n" +
                        "   Reader:            " + pivotContentReader);
            }
            ret = pivotContentReader;
        }
        else
        {
            ret = invocation.proceed();
        }
        // done
        return ret;
    }
}
