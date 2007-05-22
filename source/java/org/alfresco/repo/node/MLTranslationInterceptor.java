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
package org.alfresco.repo.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor to   
 *         -    filter the multilingual nodes to display the documents in the prefered language of teh user
 *                    
 * @author yanipig
 */
public class MLTranslationInterceptor implements MethodInterceptor
{
    private static Log logger = LogFactory
            .getLog(MLTranslationInterceptor.class);

    private NodeService nodeService;
    
    private MultilingualContentService multilingualContentService;

    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        Object ret = null;
        String methodName = invocation.getMethod().getName();
        
        // intercept the methods getChildAssocs and getChildByNames to apply filter.
        if (methodName.equals("getChildAssocs") || methodName.equals("getChildByName"))
        {
            ret = invocation.proceed();
            
            NodeRef parent = (NodeRef) invocation.getArguments()[0];
            
            // all the association returned by the method
            List<ChildAssociationRef> allChildAssoc = (List<ChildAssociationRef>) ret;
            
            // get the user content filter language
            Locale filterLocale = I18NUtil.getContentLocaleOrNull();
            
            if(filterLocale != null 
                    &&  nodeService.getType(parent).equals(ContentModel.TYPE_FOLDER)
                    &&     ret != null 
                    &&     !allChildAssoc.isEmpty()
                    )
            {
            
                // the list of Association to return
                List<ChildAssociationRef> toReturn = new ArrayList();
                // the ml containers found in the folder
                List<NodeRef> mlContainers = new ArrayList();
                
                // construct the list of ML Container
                for (ChildAssociationRef assoc : allChildAssoc)
                {
                    NodeRef child = assoc.getChildRef();
                                    
                    QName type = nodeService.getType(child);
                                        
                    if(type.equals(ContentModel.TYPE_CONTENT) && 
                            nodeService.hasAspect(child, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
                    {                
                        NodeRef container = multilingualContentService.getTranslationContainer(child);

                        if (!mlContainers.contains(container))
                        {
                            mlContainers.add(container);
                        }                        
                    }
                    else
                    {
                        // no specific treatment for folder and non-multilingual document
                        toReturn.add(assoc);
                    }
                }
                
                // for each mlContainer found, choose the unique document to return 
                for(NodeRef container : mlContainers)
                {
                    // get each translation language
                    Set<Locale> locales = multilingualContentService.getTranslations(container).keySet();
                    
                    if(locales != null && locales.size() > 0)
                    {
                        Locale matchedLocal = I18NUtil.getNearestLocale(filterLocale, locales);
                        
                        NodeRef matchedTranslation = null;
                        
                        // if the filter language is not found
                        if(matchedLocal == null)
                        {
                            // get the pivot translation
                            matchedTranslation = multilingualContentService.getPivotTranslation(container);
                        }
                        else
                        {
                            // get the matched translation
                            matchedTranslation = multilingualContentService.getTranslations(container).get(matchedLocal);
                        }
                        
                        toReturn.add(new ChildAssociationRef(null, null, null, matchedTranslation));
                    }
                }
                
                ret = toReturn;
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Filter has found " + 
                            allChildAssoc.size() + " entries, " + 
                            mlContainers.size() + " different ML Container " +
                            "and returns " + toReturn.size() + " nodes");
                }                    
            }
                        
        } else
        {
            ret = invocation.proceed();
        }
            
        return ret;
    }

    public MultilingualContentService getMultilingualContentService()
    {
        return multilingualContentService;
    }

    public void setMultilingualContentService(
            MultilingualContentService multilingualContentService)
    {
        this.multilingualContentService = multilingualContentService;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
}