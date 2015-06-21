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

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    protected ContentClassificationService getContentClassificationService()
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

//    /** Transaction service */
//    private TransactionService transactionService;
//
//    /** Classification service bootstrap */
//    private ClassificationServiceBootstrap classificationServiceBootstrap;
//
//    /** Alfresco transaction support */
//    private AlfrescoTransactionSupport alfrescoTransactionSupport;
//
//    /** Node service */
//    private NodeService nodeService;
//
//    /** Dictionary service */
//    private DictionaryService dictionaryService;
//
//    /** Content classification service */
//    private ContentClassificationService contentClassificationService;
//
//    /**
//     * @return the transactionService
//     */
//    protected TransactionService getTransactionService()
//    {
//        return this.transactionService;
//    }
//
//    /**
//     * @return the classificationServiceBootstrap
//     */
//    protected ClassificationServiceBootstrap getClassificationServiceBootstrap()
//    {
//        return this.classificationServiceBootstrap;
//    }
//
//    /**
//     * @return the alfrescoTransactionSupport
//     */
//    protected AlfrescoTransactionSupport getAlfrescoTransactionSupport()
//    {
//        return this.alfrescoTransactionSupport;
//    }
//
//    /**
//     * @return the nodeService
//     */
//    protected NodeService getNodeService()
//    {
//        return this.nodeService;
//    }
//
//    /**
//     * @return the dictionaryService
//     */
//    protected DictionaryService getDictionaryService()
//    {
//        return this.dictionaryService;
//    }
//
//    /**
//     * @return the contentClassificationService
//     */
//    protected ContentClassificationService getContentClassificationService()
//    {
//        return this.contentClassificationService;
//    }
//
//    /**
//     * @param transactionService the transactionService to set
//     */
//    public void setTransactionService(TransactionService transactionService)
//    {
//        this.transactionService = transactionService;
//    }
//
//    /**
//     * @param classificationServiceBootstrap the classificationServiceBootstrap to set
//     */
//    public void setClassificationServiceBootstrap(ClassificationServiceBootstrap classificationServiceBootstrap)
//    {
//        this.classificationServiceBootstrap = classificationServiceBootstrap;
//    }
//
//    /**
//     * @param alfrescoTransactionSupport the alfrescoTransactionSupport to set
//     */
//    public void setAlfrescoTransactionSupport(AlfrescoTransactionSupport alfrescoTransactionSupport)
//    {
//        this.alfrescoTransactionSupport = alfrescoTransactionSupport;
//    }
//
//    /**
//     * @param nodeService the nodeService to set
//     */
//    public void setNodeService(NodeService nodeService)
//    {
//        this.nodeService = nodeService;
//    }
//
//    /**
//     * @param dictionaryService the dictionaryService to set
//     */
//    public void setDictionaryService(DictionaryService dictionaryService)
//    {
//        this.dictionaryService = dictionaryService;
//    }
//
//    /**
//     * @param contentClassificationService the contentClassificationService to set
//     */
//    public void setContentClassificationService(ContentClassificationService contentClassificationService)
//    {
//        this.contentClassificationService = contentClassificationService;
//    }

    /**
     * Checks if the current user is cleared to see the items
     * passed as parameters to the current method invocation.
     *
     * @param invocation The current method invocation
     * @return <code>true</code> if the user is cleared to see the items, <code>false</code> otherwise
     */
    public boolean process(final MethodInvocation invocation)
    {
        mandatory("invocation", invocation);

        // do in transaction
        return /*getTransactionService().*/getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>()
        {
            @SuppressWarnings("rawtypes")
            public Boolean execute() throws Throwable
            {
                Boolean result = true;

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
                                getAlfrescoTransactionSupport().bindResource(KEY_PROCESSING, TRUE);
                                try
                                {
                                    // get the value of the parameter
                                    NodeRef testNodeRef = (NodeRef) invocation.getArguments()[position];

                                    // if node exists then see if the current user has clearance
                                    result = isNodeCleared(testNodeRef);
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

                return result;
            }
        }, true);
    }

    /**
     * Checks if the given node exists, if it is a content and if
     * the currently logged in user is cleared to see it.
     *
     * @param nodeRef Node reference to check
     * @return <code>true</code> if the node passes the checks, <code>false</code> otherwise
     */
    private boolean isNodeCleared(NodeRef nodeRef)
    {
        boolean result = true;

        if (getNodeService().exists(nodeRef) &&
                getDictionaryService().isSubClass(getNodeService().getType(nodeRef), TYPE_CONTENT) &&
                !getContentClassificationService().hasClearance(nodeRef))
        {
            result = false;
        }

        return result;
    }
}
