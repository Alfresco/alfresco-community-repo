/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor;

import static org.alfresco.model.ContentModel.TYPE_CONTENT;

import javax.annotation.PostConstruct;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearanceService;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for post method invocation processors
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public abstract class BasePostMethodInvocationProcessor
{
    /** Node service */
    @Autowired
    private NodeService nodeService;

    /** Dictionary service */
    @Autowired
    private DictionaryService dictionaryService;

    /** Content classification service */
    @Autowired
    private ContentClassificationService contentClassificationService;

    /** Security Clearance Service */
    @Autowired
    private SecurityClearanceService securityClearanceService;

    /** Post method invocation processor */
    @Autowired
    private PostMethodInvocationProcessor postMethodInvocationProcessor;

    /** Cache to hold the filtered node information */
    @Autowired
    private SimpleCache<Pair<String, NodeRef>, Pair<Boolean, NodeRef>> basePostMethodInvocationProcessorCache;

    /**
     * @return the nodeService
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * @return the dictionaryService
     */
    protected DictionaryService getDictionaryService()
    {
        return this.dictionaryService;
    }

    /**
     * @return the contentClassificationService
     */
    protected ContentClassificationService getContentClassificationService()
    {
        return this.contentClassificationService;
    }

    /**
     * @return the securityClearanceService
     */
    protected SecurityClearanceService getSecurityClearanceService()
    {
        return this.securityClearanceService;
    }

    /**
     * @return the postMethodInvocationProcessor
     */
    protected PostMethodInvocationProcessor getPostMethodInvocationProcessor()
    {
        return this.postMethodInvocationProcessor;
    }

    /**
     * @return the cache
     */
    protected SimpleCache<Pair<String, NodeRef>, Pair<Boolean, NodeRef>> getCache()
    {
        return this.basePostMethodInvocationProcessorCache;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param contentClassificationService the contentClassificationService to set
     */
    public void setContentClassificationService(ContentClassificationService contentClassificationService)
    {
        this.contentClassificationService = contentClassificationService;
    }

    /**
     * @param securityClearanceService the securityClearanceService to set
     */
    public void setSecurityClearanceService(SecurityClearanceService securityClearanceService)
    {
        this.securityClearanceService = securityClearanceService;
    }

    /**
     * @param postMethodInvocationProcessor the postMethodInvocationProcessor to set
     */
    public void setPostMethodInvocationProcessor(PostMethodInvocationProcessor postMethodInvocationProcessor)
    {
        this.postMethodInvocationProcessor = postMethodInvocationProcessor;
    }

    /**
     * @param cache the cache to set
     */
    public void setCache(SimpleCache<Pair<String, NodeRef>, Pair<Boolean, NodeRef>> cache)
    {
        this.basePostMethodInvocationProcessorCache = cache;
    }

    /**
     * Gets the class name
     *
     * @return The class name
     */
    protected abstract Class<?> getClassName();

    /**
     * Performs checks on the given object and throws exception if not all checks pass
     *
     * @param object The object to check
     * @return The given object
     */
    public abstract <T extends Object> T process(T object);

    /**
     * Registers the post method invocation processors
     */
    @PostConstruct
    public void register()
    {
        getPostMethodInvocationProcessor().register(this);
    }

    /**
     * Filters the node if the give node reference exist and it is a
     * content but the logged in user is not cleared to see the it.
     *
     * @param nodeRef Node reference
     * @return <code>null</code> if the give node reference has been
     * filtered, the node reference itself otherwise
     */
    protected NodeRef filter(NodeRef nodeRef)
    {
        NodeRef filter = nodeRef;

        if (getNodeService().exists(nodeRef) &&
                getDictionaryService().isSubClass(getNodeService().getType(nodeRef), TYPE_CONTENT) &&
                !getContentClassificationService().hasClearance(nodeRef))
        {
            filter = null;
        }

//        if (filter != null)
//        {
//            String uniqueCacheKey = getFullyAuthenticatedUser() /*+ userClearance?*/;
//
//            Pair<String, NodeRef> cacheKey = new Pair<String, NodeRef>(uniqueCacheKey, filter);
//            Pair<Boolean, NodeRef> cacheValue = getCache().get(cacheKey);
//
//            if (cacheValue == null || !cacheValue.getFirst().booleanValue())
//            {
//                if (getNodeService().exists(nodeRef) &&
//                        getDictionaryService().isSubClass(getNodeService().getType(nodeRef), TYPE_CONTENT) &&
//                        !getContentClassificationService().hasClearance(nodeRef))
//                {
//                    filter = null;
//                }
//                getCache().put(new Pair<String, NodeRef>(uniqueCacheKey, nodeRef), new Pair<Boolean, NodeRef>(true, filter));
//            }
//            else
//            {
//                filter = getCache().get(cacheKey).getSecond();
//            }
//        }

        return filter;
    }
}
