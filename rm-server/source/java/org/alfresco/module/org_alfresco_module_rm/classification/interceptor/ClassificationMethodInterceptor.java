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
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor;

import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import java.lang.reflect.Method;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationServiceBootstrap;
import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.util.AlfrescoTransactionSupport;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Classification method interceptor
 *
 * @author Roy Wetherall
 * @since 3.0
 */
public class ClassificationMethodInterceptor implements MethodInterceptor, ApplicationContextAware
{
    private static final String KEY_PROCESSING = GUID.generate();

    /** application context */
    private ApplicationContext applicationContext;

    /**
     * @param applicationContext   application context
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    protected AuthenticationUtil getAuthenticationUtil()
    {
        return (AuthenticationUtil)applicationContext.getBean("rm.authenticationUtil");
    }

    /**
     * @return {@link ContentClassificationService} content classification service
     */
    protected ContentClassificationService getContentClassificaitonService()
    {
        return (ContentClassificationService)applicationContext.getBean("contentClassificationService");
    }

    protected AlfrescoTransactionSupport getAlfrescoTransactionSupport()
    {
        return (AlfrescoTransactionSupport)applicationContext.getBean("rm.alfrescoTransactionSupport");
    }

    protected RetryingTransactionHelper getRetryingTransactionHelper()
    {
        return ((TransactionService)applicationContext.getBean("transactionService")).getRetryingTransactionHelper();
    }

    protected ClassificationServiceBootstrap getClassificationServiceBootstrap()
    {
        return (ClassificationServiceBootstrap)applicationContext.getBean("classificationServiceBootstrap");
    }

    protected NodeService getNodeService()
    {
        return (NodeService)applicationContext.getBean("dbNodeService");
    }

    protected DictionaryService getDictionaryService()
    {
        return (DictionaryService)applicationContext.getBean("dictionaryService");
    }

    /**
     * Check that the current user is cleared to see the items passed as parameters to the current
     * method invocation.
     *
     * @param invocation    method invocation
     */
    @SuppressWarnings("rawtypes")
    public void checkClassification(final MethodInvocation invocation)
    {
        // do in transaction
        getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // ensure classification service has been bootstrapped
                if (getClassificationServiceBootstrap().isInitialised())
                {
                    // check that we are not already processing a classification check
                    Object value = getAlfrescoTransactionSupport().getResource(KEY_PROCESSING);
                    if (value == null)
                    {
                        Method method = invocation.getMethod();
                        Class[] params = method.getParameterTypes();

                        int position = 0;
                        for (Class param : params)
                        {
                            // if the param is a node reference
                            if (NodeRef.class.isAssignableFrom(param))
                            {
                                // mark the transaction as processing a classification check
                                getAlfrescoTransactionSupport().bindResource(KEY_PROCESSING, Boolean.TRUE);
                                try
                                {
                                    // get the value of the parameter
                                    NodeRef testNodeRef = (NodeRef) invocation.getArguments()[position];

                                    // if node exists then see if the current user has clearance
                                    checkNode(testNodeRef);
                                }
                                finally
                                {
                                    // clear the transaction as processed a classification check
                                    getAlfrescoTransactionSupport().unbindResource(KEY_PROCESSING);
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

    private boolean validUser()
    {
        boolean result = false;

        // check that we have an authenticated user and that they aren't "system"
        if (isNotBlank(getAuthenticationUtil().getFullyAuthenticatedUser()) &&
            !getAuthenticationUtil().isRunAsUserTheSystemUser())
        {
            result = true;
        }

        return result;
    }

    private void checkNode(NodeRef testNodeRef)
    {
        if (getNodeService().exists(testNodeRef) &&
                getDictionaryService().isSubClass(getNodeService().getType(testNodeRef), ContentModel.TYPE_CONTENT) &&
                !getContentClassificaitonService().hasClearance(testNodeRef))
        {
            // throw exception
            throw new AccessDeniedException("You do not have clearance!");
        }
    }

    /**
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        boolean isValidUser = validUser();

        if (isValidUser)
        {
            // pre method invocation check
            checkClassification(invocation);
        }

        // method proceed
        Object result = invocation.proceed();

        if (isValidUser)
        {
            // post method invocation processing
         // TODO
        }

        return result;
    }
}
