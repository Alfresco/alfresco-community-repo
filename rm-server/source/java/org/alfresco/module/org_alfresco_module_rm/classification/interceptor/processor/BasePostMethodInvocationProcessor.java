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
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
     * Registers the post method invocation processors
     */
    @PostConstruct
    private void register()
    {
        getPostMethodInvocationProcessor().register(this);
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
    protected abstract <T extends Object> T process(T object);

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
