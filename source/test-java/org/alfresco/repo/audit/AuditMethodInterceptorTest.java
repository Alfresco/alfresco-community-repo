/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionServiceImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;

/**
 * Tests AuditMethodInterceptor
 * 
 * @see AuditMethodInterceptor
 * @author alex.mukha
 * @since 4.2.3
 */
public class AuditMethodInterceptorTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuditModelRegistryImpl auditModelRegistry;
    private TransactionServiceImpl transactionServiceImpl;
    private NodeService nodeService;
    private ServiceRegistry serviceRegistry;
    private AuditComponent auditComponent;
    private AuditService auditService;
    private TransactionService transactionService;

    private NodeRef nodeRef;

    private static String APPLICATION_NAME = "alfresco-mnt-11072";
    private static final Log logger = LogFactory.getLog(AuditMethodInterceptorTest.class);

    @Override
    public void setUp() throws Exception
    {
        auditModelRegistry = (AuditModelRegistryImpl) ctx.getBean("auditModel.modelRegistry");
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        auditComponent = (AuditComponent) ctx.getBean("auditComponent");
        auditService = serviceRegistry.getAuditService();
        transactionService = serviceRegistry.getTransactionService();
        transactionServiceImpl = (TransactionServiceImpl) ctx.getBean("transactionService");
        nodeService = serviceRegistry.getNodeService();

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        nodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        // Register the model
        URL modelUrl = ResourceUtils.getURL("classpath:alfresco/testaudit/alfresco-audit-test-mnt-11072.xml");
        auditModelRegistry.registerModel(modelUrl);
        auditModelRegistry.loadAuditModels();
    }

    @Override
    public void tearDown()
    {
        auditService.clearAudit(APPLICATION_NAME, null, null);
        auditModelRegistry.destroy();
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * Test for <a href="https://issues.alfresco.com/jira/browse/MNT-11072">MNT-11072</a> <br>
     * Use NodeService, as it is wrapped by the AuditMethodInterceptor, to get node props in read-only server mode.
     * 
     * @throws Exception
     */
    public void testAuditInReadOnly() throws Exception
    {
        // Run as admin
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        QName veto = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "TestVeto");
        transactionServiceImpl.setAllowWrite(false, veto);
        try
        {
            // Access the node in read-only transaction
            Map<QName, Serializable> props = transactionService.getRetryingTransactionHelper()
                    .doInTransaction(new RetryingTransactionCallback<Map<QName, Serializable>>()
            {

                @Override
                public Map<QName, Serializable> execute() throws Throwable
                {
                    return nodeService.getProperties(nodeRef);
                }

            }, true, false);

            assertNotNull("The props should exsist.", props);

            // Search for audit
            final StringBuilder sb = new StringBuilder();
            final MutableInt rowCount = new MutableInt();
            AuditQueryCallback callback = new AuditQueryCallback()
            {
                @Override
                public boolean valuesRequired()
                {
                    return true;
                }

                @Override
                public boolean handleAuditEntry(Long entryId, String applicationName,
                        String user, long time, Map<String, Serializable> values)
                {
                    assertNotNull(applicationName);
                    assertNotNull(user);

                    sb.append("Row: ").append(entryId).append(" | ")
                            .append(applicationName).append(" | ")
                            .append(user).append(" | ")
                            .append(new Date(time)).append(" | ")
                            .append(values).append(" | ").append("\n");
                    rowCount.setValue(rowCount.intValue() + 1);
                    return true;
                }

                @Override
                public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
                {
                    throw new AlfrescoRuntimeException(errorMsg, error);
                }
            };

            AuditQueryParameters params = new AuditQueryParameters();
            params.setForward(true);
            params.setUser(AuthenticationUtil.getAdminUserName());
            params.setApplicationName(APPLICATION_NAME);

            rowCount.setValue(0);
            auditComponent.auditQuery(callback, params, -1);

            assertEquals("There should be one audit entry.", 1, rowCount.intValue());
            assertTrue("The requested nodeRef should be in the audit entry.",
                    sb.toString().contains(nodeRef.toString()));
            if (logger.isDebugEnabled())
            {
                logger.debug(sb.toString());
            }
        }
        finally
        {
            transactionServiceImpl.setAllowWrite(true, veto);
        }
    }
}
