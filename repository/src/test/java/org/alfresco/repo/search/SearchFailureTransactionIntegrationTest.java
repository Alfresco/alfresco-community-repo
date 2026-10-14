/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.search;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.aopalliance.aop.Advice;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.audit.AuditMethodInterceptor;
import org.alfresco.repo.audit.AuditWriteException;
import org.alfresco.repo.audit.BeanIdentifier;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Reproduces the customer scenario against a real repository: a web script runs a search, swallows the failure, creates a node and returns it.
 *
 * <p>
 * The search is stubbed to fail the way an unreachable Elasticsearch does, but it is advised by the real {@code SearchService_transaction} interceptor, so the rollback rule under test is the shipped one. Everything else - transaction helper, node service, database - is real.
 * </p>
 */
public class SearchFailureTransactionIntegrationTest extends BaseSpringTest
{
    private NodeService nodeService;
    private RetryingTransactionHelper txnHelper;
    private SearchService unreachableSearchService;
    private NodeRef rootNodeRef;

    @Before
    public void setUp()
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        nodeService = serviceRegistry.getNodeService();
        txnHelper = serviceRegistry.getTransactionService().getRetryingTransactionHelper();

        ((AuthenticationComponent) applicationContext.getBean("authenticationComponent")).setSystemUserAsCurrentUser();

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "test-" + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(storeRef);

        unreachableSearchService = searchServiceThatFailsLikeAnUnreachableIndex();
    }

    @Test
    public void searchFailureMustNotDiscardTheCallersWork()
    {
        NodeRef created = txnHelper.doInTransaction(() -> {
            try
            {
                unreachableSearchService.query(new SearchParameters());
                fail("the stubbed search was expected to fail");
            }
            catch (RuntimeException expected)
            {
                // the customer's web script catches the search failure and carries on
            }

            return nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
                    ContentModel.TYPE_CMOBJECT).getChildRef();
        }, false, true);

        boolean persisted = txnHelper.doInTransaction(() -> nodeService.exists(created), true, true);

        assertTrue("The caller was handed " + created + " and saw no error, so that node must exist."
                + " A failed search must not mark the caller's transaction rollback-only; check the"
                + " rollback rule on SearchService_transaction in alfresco/public-services-context.xml",
                persisted);
    }

    @Test
    public void auditWriteFailureMustRollBackTheCallersWork()
    {
        SearchService auditFailingSearchService = searchServiceWhoseAuditWriteFails();

        NodeRef created = txnHelper.doInTransaction(() -> {
            try
            {
                auditFailingSearchService.query(new SearchParameters());
                fail("the stubbed audit write was expected to fail");
            }
            catch (AuditWriteException expected)
            {
                // a caller that swallows this must still not be allowed to commit unrecorded work
            }

            return nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate()),
                    ContentModel.TYPE_CMOBJECT).getChildRef();
        }, false, true);

        boolean persisted = txnHelper.doInTransaction(() -> nodeService.exists(created), true, true);

        assertFalse("Audit deliberately aborts an operation it could not record, so work"
                + " done alongside a failed audit write must not be committed. Check that the rollback"
                + " rule on SearchService_transaction in alfresco/public-services-context.xml still"
                + " excludes " + AuditWriteException.class.getSimpleName(),
                persisted);
    }

    private SearchService searchServiceThatFailsLikeAnUnreachableIndex()
    {
        SearchService target = mock(SearchService.class);
        given(target.query(any(SearchParameters.class)))
                .willThrow(new RuntimeException(new IOException("elasticsearch: Name or service not known")));
        return advised(target);
    }

    /**
     * Uses the real {@link AuditMethodInterceptor} with an audit component that cannot write, so the failure is raised by production code rather than fabricated by the test.
     */
    private SearchService searchServiceWhoseAuditWriteFails()
    {
        AuditComponent failingAudit = mock(AuditComponent.class);
        given(failingAudit.areAuditValuesRequired(anyString())).willReturn(true);
        given(failingAudit.recordAuditValues(anyString(), anyMap()))
                .willThrow(new IllegalStateException("audit table unavailable"));

        BeanIdentifier beanIdentifier = mock(BeanIdentifier.class);
        given(beanIdentifier.getBeanName(any())).willReturn("SearchService");

        AuditMethodInterceptor auditInterceptor = new AuditMethodInterceptor();
        auditInterceptor.setAuditComponent(failingAudit);
        auditInterceptor.setBeanIdentifier(beanIdentifier);

        return advised(mock(SearchService.class), auditInterceptor);
    }

    /** Wraps the target in the production interceptor, so the rollback rule exercised is the shipped one, with any further advice nested inside it as in {@code public-services-context.xml}. */
    private SearchService advised(SearchService target, Advice... nested)
    {
        ProxyFactory factory = new ProxyFactory(target);
        factory.setInterfaces(SearchService.class);
        factory.addAdvice(applicationContext.getBean("SearchService_transaction", TransactionInterceptor.class));
        for (Advice advice : nested)
        {
            factory.addAdvice(advice);
        }
        return (SearchService) factory.getProxy();
    }
}
