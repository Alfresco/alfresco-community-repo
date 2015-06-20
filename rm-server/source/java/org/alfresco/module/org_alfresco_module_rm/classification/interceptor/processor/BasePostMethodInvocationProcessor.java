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

import java.util.Collection;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.ClassificationPostMethodInvocationException.NotSupportedClassTypeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Base class for post method invocation processors
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public abstract class BasePostMethodInvocationProcessor
{
    /** Node service */
    private NodeService nodeService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** Content classification service */
    private ContentClassificationService contentClassificationService;

    /** Post method invocation processor */
    private PostMethodInvocationProcessor postMethodInvocationProcessor;

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
     * @return the postMethodInvocationProcessor
     */
    protected PostMethodInvocationProcessor getPostMethodInvocationProcessor()
    {
        return this.postMethodInvocationProcessor;
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
     * @param postMethodInvocationProcessor the postMethodInvocationProcessor to set
     */
    public void setPostMethodInvocationProcessor(PostMethodInvocationProcessor postMethodInvocationProcessor)
    {
        this.postMethodInvocationProcessor = postMethodInvocationProcessor;
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
    public void register()
    {
        getPostMethodInvocationProcessor().register(this);
    }

    /**
     * Checks if the given object's class is suitable for the processor
     *
     * @param object The object to process
     */
    protected void checkObjectClass(Object object)
    {
        if (!(object.getClass().equals(getClassName())))
        {
            throw new NotSupportedClassTypeException("The object is not an instance of '" + getClassName() + "' but '" + object.getClass() + "'.");
        }
    }

    /**
     * Checks if the given object is a collection
     *
     * @param object Object to check
     * @return <code>true</code> if the code is a collection, <code>false</code> otherwise
     */
    protected <T> boolean isCollection(T object)
    {
        return Collection.class.isAssignableFrom(object.getClass());
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

        return filter;
    }
}
