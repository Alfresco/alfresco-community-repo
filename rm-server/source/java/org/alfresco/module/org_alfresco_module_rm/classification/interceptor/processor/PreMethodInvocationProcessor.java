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
import static org.alfresco.util.ParameterCheck.mandatory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Pre method invocation processor
 *
 * @author Roy Wetherall
 * @author Tuna Aksoy
 * @since 3.0
 */
public class PreMethodInvocationProcessor implements ApplicationContextAware
{
    /** List of method names to check before invocation */
    private List<String> methodNames = new ArrayList<>();

    /** Application context */
    private ApplicationContext applicationContext;

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    /**
     * Gets the content classification service
     *
     * @return The content classification service
     */
    protected ContentClassificationService getContentClassificationService()
    {
        return (ContentClassificationService) applicationContext.getBean("contentClassificationService");
    }

    /**
     * Gets the node service
     *
     * @return The node service
     */
    protected NodeService getNodeService()
    {
        return (NodeService) applicationContext.getBean("dbNodeService");
    }

    /**
     * Gets the dictionary service
     *
     * @return The dictionary service
     */
    protected DictionaryService getDictionaryService()
    {
        return (DictionaryService) applicationContext.getBean("dictionaryService");
    }

    /**
     * Returns a list of method names to check before invocation
     *
     * @return List of method names to check before invocation
     */
    protected List<String> getMethodNames()
    {
        return this.methodNames;
    }

    /**
     * Init method to populate the list of method names which will be checked before invocation
     */
    public void init()
    {
        getMethodNames().add("NodeService.setProperty");
        getMethodNames().add("NodeService.setProperties");
        getMethodNames().add("NodeService.getProperty");
        //getMethodNames().add("NodeService.getProperties");
        getMethodNames().add("FileFolderService.copy");
        getMethodNames().add("FileFolderService.move");
    }

    /**
     * Checks if the current user is cleared to see the items
     * passed as parameters to the current method invocation.
     *
     * @param invocation The current method invocation
     */
    public void process(MethodInvocation invocation)
    {
        mandatory("invocation", invocation);

        Method method = invocation.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String name = className + "." + methodName;
        Object[] args = invocation.getArguments();

        if (getMethodNames().contains(name))
        {
            for (Object arg : args)
            {
                if (arg != null && NodeRef.class.isAssignableFrom(arg.getClass()))
                {
                    isNodeCleared(((NodeRef) arg), name);
                }
            }
        }
    }

    /**
     * Checks if the given node exists, if it is a content and if
     * the currently logged in user is cleared to see it.
     *
     * @param nodeRef Node reference to check
     * @param name The name of the invoked method
     */
    private void isNodeCleared(NodeRef nodeRef, String name)
    {
        if (getNodeService().exists(nodeRef) &&
                getDictionaryService().isSubClass(getNodeService().getType(nodeRef), TYPE_CONTENT) &&
                !getContentClassificationService().hasClearance(nodeRef))
        {
            throw new ClassificationEnforcementException("The method '" + name  + "' was called, but you are not cleared for the node.");
        }
    }
}
