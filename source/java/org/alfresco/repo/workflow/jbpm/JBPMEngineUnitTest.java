/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.workflow.jbpm;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.transaction.TransactionDefinition.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import junit.framework.TestCase;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.NamespaceServiceMemoryImpl;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.transaction.SpringAwareUserTransaction;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springmodules.workflow.jbpm31.JbpmTemplate;

/**
 * JBPMEngine Unit Tests.
 * 
 * @author Nick Smith
 */
public class JBPMEngineUnitTest extends TestCase
{
    private static final NodeRef companyHome = new NodeRef("for://test/home");

    private static final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] { "classpath:test/alfresco/test-database-context.xml",
                            "classpath:test/alfresco/test-workflow-context.xml", });

    private JBPMEngine engine = new JBPMEngine();

    private WorkflowDefinition testWorkflowDef;

    private SpringAwareUserTransaction transaction;

    public void testStartWorkflowWithoutPackage() throws Exception
    {
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        engine.startWorkflow(testWorkflowDef.getId(), params);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // Mock up various services.
        NodeService nodeService = mock(NodeService.class);
        TenantService tenantService = makeTenantService();
        NamespaceService namespaceService = makeNamespaceService();
        DictionaryService dictionaryService = makeDictionaryService();

        // Add services to ServiceRegistry
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        when(serviceRegistry.getNodeService()).thenReturn(nodeService);
        when(serviceRegistry.getNamespaceService()).thenReturn(namespaceService);
        when(serviceRegistry.getDictionaryService()).thenReturn(dictionaryService);

        JbpmTemplate jbpmTemplate = (JbpmTemplate) ctx.getBean("test_jbpm_template");
        // Set up the JBPMEngine.
        engine.setJBPMTemplate(jbpmTemplate);
        engine.setTenantService(tenantService);
        engine.setNodeService(nodeService);
        engine.setServiceRegistry(serviceRegistry);
        engine.setNamespaceService(namespaceService);
        engine.setMessageService(mock(MessageService.class));
        engine.setDictionaryService(dictionaryService);
        engine.setEngineId("jbpm_test");

        // Need to register JBPMEngine with bean factory so WorflowTaskInstance
        // can load it.
        ctx.getBeanFactory().registerSingleton("test_jbpm_engine", engine);

        // Deploy test workflow process definition to JBPM.
        startTransaction();
        deployTestDefinition();
    }

    @SuppressWarnings("unchecked")
    private DictionaryService makeDictionaryService()
    {
        DictionaryService service = mock(DictionaryService.class);

        // DictionaryService.getType(QName) always returns a mock
        // TypeDefinition.
        TypeDefinition typeDef = mock(TypeDefinition.class);
        when(service.getType((QName) any())).thenReturn(typeDef);

        // DictionaryService.getAnonymousType(QName, Collection<QName>)
        // always returns a mock TypeDefinition
        when(service.getAnonymousType((QName) any(),//
                    (Collection<QName>) any()))//
                    .thenReturn(typeDef);
        return service;
    }

    private void startTransaction() throws NotSupportedException, SystemException
    {
        PlatformTransactionManager transactionManager = (PlatformTransactionManager) ctx
                    .getBean("testTransactionManager");
        transaction = new SpringAwareUserTransaction(transactionManager, false, ISOLATION_DEFAULT,
                    PROPAGATION_REQUIRED, 1000);
        transaction.begin();
    }

    // deploy test process definition
    private void deployTestDefinition() throws IOException
    {
        ClassPathResource processDef = new ClassPathResource(
                    "jbpmresources/test_processdefinition.xml");
        assertFalse(engine.isDefinitionDeployed(processDef.getInputStream(),
                    MimetypeMap.MIMETYPE_XML));
        WorkflowDeployment deployment = engine.deployDefinition(processDef.getInputStream(),
                    MimetypeMap.MIMETYPE_XML);
        testWorkflowDef = deployment.definition;
        assertNotNull(testWorkflowDef);
        assertEquals("jbpm_test$test", testWorkflowDef.name);
        assertEquals("1", testWorkflowDef.version);
        assertTrue(engine.isDefinitionDeployed(processDef.getInputStream(),
                    MimetypeMap.MIMETYPE_XML));
    }

    private NamespaceService makeNamespaceService()
    {
        NamespaceServiceMemoryImpl namespace = new NamespaceServiceMemoryImpl();
        namespace.registerNamespace(NamespaceService.DEFAULT_PREFIX, NamespaceService.DEFAULT_URI);
        namespace.registerNamespace("wf", "http://www.alfresco.org/model/bpm/1.0");
        return namespace;
    }

    private TenantService makeTenantService()
    {
        TenantService tenantService = mock(TenantService.class);

        // Tenant Service.isEnabled() returns true.
        when(tenantService.isEnabled()).thenReturn(true);

        // TenantService.getRootNode always returns companyHome.
        when(tenantService.getRootNode((NodeService) any(),//
                    (SearchService) any(),//
                    (NamespaceService) any(),//
                    anyString(),//
                    (NodeRef) any()))//
                    .thenReturn(companyHome);
        // Tenant Service.getName(String) will return the input param.
        when(tenantService.getName(anyString())).thenAnswer(new Answer<String>()
        {
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                return (String) invocation.getArguments()[0];
            }
        });
        when(tenantService.getBaseName(anyString())).thenAnswer(new Answer<String>()
        {
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                return (String) invocation.getArguments()[0];
            }
        });
        return tenantService;
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        try
        {
            transaction.rollback();
        }
        // To prevent rollback exceptions hiding other exceptions int he unit
        // test.
        catch (Throwable t)
        {
            System.out.println(t.getStackTrace());
        }
    }
}
