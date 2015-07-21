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

import static java.lang.Boolean.TRUE;
import static org.alfresco.model.ContentModel.TYPE_CONTENT;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.getFullyAuthenticatedUser;
import static org.alfresco.util.GUID.generate;
import static org.alfresco.util.ParameterCheck.mandatory;

import java.lang.reflect.Method;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceBootstrap;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.util.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
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
    /** Key to mark the transaction as processing */
    private static final String KEY_PROCESSING = generate();

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
     * Gets the alfresco transaction support
     *
     * @return The alfresco transaction support
     */
    protected AlfrescoTransactionSupport getAlfrescoTransactionSupport()
    {
        return (AlfrescoTransactionSupport) applicationContext.getBean("rm.alfrescoTransactionSupport");
    }

    /**
     * Gets the retrying transaction helper
     *
     * @return The retrying transaction helper
     */
    protected RetryingTransactionHelper getRetryingTransactionHelper()
    {
        return ((TransactionService) applicationContext.getBean("transactionService")).getRetryingTransactionHelper();
    }

    /**
     * Gets the classification service bootstrap
     *
     * @return The classification service bootstrap
     */
    protected ClassificationServiceBootstrap getClassificationServiceBootstrap()
    {
        return (ClassificationServiceBootstrap) applicationContext.getBean("classificationServiceBootstrap");
    }

    /**
     * is pre-processing enabled?
     *
     * @return boolean <code>true</code> if enabled, <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return (getAlfrescoTransactionSupport().getResource(KEY_PROCESSING) == null);
    }

    /**
     * disable pre-processing for this transaction
     */
    public void disable()
    {
        // mark the transaction as processing a classification check
        getAlfrescoTransactionSupport().bindResource(KEY_PROCESSING, TRUE);
    }

    /**
     * enable pre-processing for this transaction
     */
    public void enable()
    {
        // clear the transaction as processed a classification check
        getAlfrescoTransactionSupport().unbindResource(KEY_PROCESSING);
    }

    /**
     * Checks if the current user is cleared to see the items
     * passed as parameters to the current method invocation.
     *
     * @param invocation The current method invocation
     */
    public void process(final MethodInvocation invocation)
    {
        mandatory("invocation", invocation);

        // do in transaction
        getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @SuppressWarnings("rawtypes")
            public Void execute() throws Throwable
            {
                // ensure classification service has been bootstrapped
                if (getClassificationServiceBootstrap().isInitialised())
                {
                    // if pre-processing is enabled
                    if (isEnabled())
                    {
                        Method method = invocation.getMethod();
                        Class[] params = method.getParameterTypes();

                        int position = 0;
                        for (Class param : params)
                        {
                            // if the param is a node reference
                            if (NodeRef.class.isAssignableFrom(param))
                            {
                                // disable pre-processing
                                disable();
                                try
                                {
                                    // get the value of the parameter
                                    NodeRef testNodeRef = (NodeRef) invocation.getArguments()[position];

                                    // if node exists then see if the current user has clearance
                                    isNodeCleared(testNodeRef, method);
                                }
                                finally
                                {
                                    // re-enable pre-processing
                                    enable();
                                }
                            }

                            position++;
                        }
                    }
                }

                return null;
            }
        }, true);
    }

    /**
     * Checks if the given node exists, if it is a content and if
     * the currently logged in user is cleared to see it.
     *
     * @param nodeRef Node reference to check
     * @param method The invoked method
     */
    private void isNodeCleared(NodeRef nodeRef, Method method)
    {
        if (nodeRef != null &&
                getNodeService().exists(nodeRef) &&
                getDictionaryService().isSubClass(getNodeService().getType(nodeRef), TYPE_CONTENT) &&
                !getContentClassificationService().hasClearance(nodeRef))
        {
            String className = method.getDeclaringClass().getSimpleName();
            String methodName = method.getName();
            String name = className + "." + methodName;

            throw new AccessDeniedException("Access is denied for the user '" + getFullyAuthenticatedUser()
                    + "'  to call the method '" + name + "' for the node '" + nodeRef + "'.");
        }
    }
}
