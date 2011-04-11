/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.tagging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TagDetails;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * This class is an interceptor of the NodeService that converts the content of the tagScopeCache property 
 * into a pseudo, multi-value text property (cm:tagScopeSummary) 
 * with each value of the spoofed property taking the form "<tag name>=<tag count>".
 * This interceptor can be enabled by calling its 
 * static {@link TagScopePropertyMethodInterceptor#setEnabled(Boolean)} method. It is enabled by default. When enabled, 
 * a call to getProperties
 * for a node that has a cm:tagScopeCache property will include the calculated cm:tagScopeSummary property. A call to 
 * getProperty specifying cm:tagScopeSummary as the property name will return the calculated property value or null
 * if the node has no cm:tagScopeCache property value. 
 * 
 * @author Brian Remmington
 *
 */
public class TagScopePropertyMethodInterceptor implements MethodInterceptor
{
    private static ThreadLocal<Boolean> enabled = new ThreadLocal<Boolean>() 
    {
        @Override
        protected Boolean initialValue()
        {
            return Boolean.TRUE;
        }
    };
    
    private ContentService contentService;
    private NodeService nodeService;
    private SimpleCache<String, List<String>> cache;
    

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setCache(SimpleCache<String, List<String>> cache)
    {
        this.cache = cache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        Object ret;
        
        //If we're not enabled then exit here
        if (Boolean.FALSE.equals(getEnabled()))
        {
            return invocation.proceed();
        }
        
        String methodName = invocation.getMethod().getName();

        if ("getProperty".equals(methodName))
        {
            Object[] args = invocation.getArguments();
            NodeRef nodeRef = (NodeRef) args[0];
            QName propertyQName = (QName) args[1];
            //Is this a request for the calculated cm:tagScopeSummary property?
            if (ContentModel.PROP_TAGSCOPE_SUMMARY.equals(propertyQName))
            {
                ret = getTagSummary(nodeRef, null); 
            }
            else
            {
                ret = invocation.proceed();
            }
        }
        else if ("getProperties".equals(methodName))
        {
            ret = invocation.proceed();
            if (Map.class.isAssignableFrom(ret.getClass()))
            {
                Map<QName, Serializable> retMap = (Map<QName, Serializable>)ret;
                NodeRef nodeRef = (NodeRef) invocation.getArguments()[0];
                List<String> tagSummary = getTagSummary(nodeRef, retMap);
                if (tagSummary != null)
                {
                    retMap.put(ContentModel.PROP_TAGSCOPE_SUMMARY, (Serializable)tagSummary);
                }
            }
        }
        else
        {
            ret = invocation.proceed();
        }
        return ret;
    }
    
    /**
     * Given a NodeRef and, optionally, the property map of that node, this operation establishes whether
     * the node is a TagScope node, and returns the appropriate value of the cm:tagScopeSummary property. 
     * @param nodeRef
     * @param allNodeProperties Optional. If the caller has a current property map for the node being queried
     * then supplying it here saves a little time. This argument is allowed to be null.
     * @return
     */
    protected List<String> getTagSummary(NodeRef nodeRef, Map<QName, Serializable> allNodeProperties)
    {
        List<String> tagSummary = null;
        ContentData tagScopeCache = null;
        if (allNodeProperties != null)
        {
            tagScopeCache = (ContentData) allNodeProperties.get(ContentModel.PROP_TAGSCOPE_CACHE);
        }
        else
        {
            tagScopeCache = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_TAGSCOPE_CACHE);
        }
        if (tagScopeCache != null)
        {
            String contentUrl = tagScopeCache.getContentUrl();
            tagSummary = cache.get(contentUrl);
            if (tagSummary == null)
            {
                ContentReader contentReader = contentService.getRawReader(contentUrl);
                if (contentReader != null && contentReader.exists())
                {
                    List<TagDetails> tagDetails = TaggingServiceImpl.readTagDetails(contentReader.getContentInputStream());
                    tagSummary = new ArrayList<String>(tagDetails.size());
                    for (TagDetails tagDetail : tagDetails)
                    {
                        tagSummary.add(tagDetail.getName() + "=" + tagDetail.getCount());
                    }
                    //Push into the cache
                    tagSummary = Collections.unmodifiableList(tagSummary);
                    cache.put(contentUrl, tagSummary);
                }
            }
        }
        return tagSummary;
    }

    public static final Boolean getEnabled()
    {
        return enabled.get();
    }
    
    /**
     * Allows the functionality of this interceptor to be enabled and disabled on a thread-by-thread basis.
     * The caller should ensure that the value is reset to its prior setting once it has finished using the
     * thread of execution.
     * @param enable
     * @return The setting prior to invoking this operation.
     */
    public static final Boolean setEnabled(Boolean enable)
    {
        Boolean oldSetting = enabled.get();
        enabled.set(enable);
        return oldSetting;
    }
}
