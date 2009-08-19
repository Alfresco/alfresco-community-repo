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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
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
    private static final String TEST_JBPM_ENGINE = "test_jbpm_engine";

    private static final NodeRef companyHome = new NodeRef("for://test/home");

    private static final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] { "classpath:test/alfresco/test-database-context.xml",
                            "classpath:test/alfresco/test-workflow-context.xml", });

    private JBPMEngine engine = new JBPMEngine();

    private WorkflowDefinition testWorkflowDef;

    private SpringAwareUserTransaction transaction;

    public void testDeployWorkflow() throws Exception
    {
        ClassPathResource processDef = new ClassPathResource(
                    "jbpmresources/test_processdefinition.xml");
        List<WorkflowDefinition> workflowDefs = engine.getDefinitions();
        assertFalse(engine.isDefinitionDeployed(processDef.getInputStream(),
                    MimetypeMap.MIMETYPE_XML));
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() == 0);

        deployTestDefinition();
        assertTrue(engine.isDefinitionDeployed(processDef.getInputStream(),
                    MimetypeMap.MIMETYPE_XML));
        workflowDefs = engine.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() == 1);

        assertNotNull(testWorkflowDef);
        assertEquals("jbpm_test$test", testWorkflowDef.name);
        assertEquals("1", testWorkflowDef.version);

        deployTestDefinition();
        assertTrue(engine.isDefinitionDeployed(processDef.getInputStream(),
                    MimetypeMap.MIMETYPE_XML));
        assertEquals("2", testWorkflowDef.version);
    }

    public void testGetWorkflowInstance() throws Exception
    {
        deployTestDefinition();
        WorkflowPath path = engine.startWorkflow(testWorkflowDef.getId(), null);
        assertNotNull(path);
        assertTrue(path.id.endsWith("-@"));
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(testWorkflowDef.id, path.instance.definition.id);
        WorkflowInstance instance = engine.getWorkflowById(path.instance.id);
        assertNotNull(instance);
        assertEquals(path.instance.id, instance.id);
    }

    public void testStartWorkflowWithoutPackage() throws Exception
    {
        try
        {
            engine.startWorkflow("norfolknchance", null);
            fail("Failed to catch invalid definition id");
        }
        catch (WorkflowException e)
        {
        }

        deployTestDefinition();
        WorkflowPath path = engine.startWorkflow(testWorkflowDef.getId(), null);
        assertNotNull(path);
        assertTrue(path.id.endsWith("-@"));
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(testWorkflowDef.getId(), path.instance.definition.id);
    }

    public void testStartWorkflowParameters() throws Exception
    {
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();

        // protected - shouldn't be written
        params.put(WorkflowModel.PROP_TASK_ID, 3);

        // task instance field
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());

        params.put(WorkflowModel.PROP_PRIORITY, 1); // task instance field
        params.put(WorkflowModel.PROP_PERCENT_COMPLETE, 10); // context variable

        // context variable outside of task definition
        params.put(QName.createQName("", "Message"), "Hello World");

        // context variable outside of task definition
        params.put(QName.createQName("", "Array"), new String[] { "one", "two" });

        // context variable outside of task definition
        params.put(QName.createQName("", "NodeRef"), new NodeRef("workspace://1/1001"));

        params.put(ContentModel.PROP_OWNER, "Owner"); // task assignment

        deployTestDefinition();
        WorkflowPath path = engine.startWorkflow(testWorkflowDef.id, params);
        assertNotNull(path);
        assertTrue(path.id.endsWith("-@"));
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(testWorkflowDef.id, path.instance.definition.id);
        List<WorkflowTask> tasks1 = engine.getTasksForWorkflowPath(path.id);
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());

        WorkflowTask task = tasks1.get(0);
        assertTrue(task.properties.containsKey(WorkflowModel.PROP_TASK_ID));
        assertTrue(task.properties.containsKey(WorkflowModel.PROP_DUE_DATE));
        assertTrue(task.properties.containsKey(WorkflowModel.PROP_PRIORITY));
        assertTrue(task.properties.containsKey(WorkflowModel.PROP_PERCENT_COMPLETE));
        assertTrue(task.properties.containsKey(ContentModel.PROP_OWNER));

        // NodeRef initiator = path.instance.initiator;
        // String initiatorUsername = (String)
        // nodeService.getProperty(initiator,
        // ContentModel.PROP_USERNAME);
        // assertEquals(AuthenticationUtil.getAdminUserName(),
        // initiatorUsername);
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
        if (!ctx.containsBean(TEST_JBPM_ENGINE))
            ctx.getBeanFactory().registerSingleton(TEST_JBPM_ENGINE, engine);

        // Deploy test workflow process definition to JBPM.
        startTransaction();
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
        WorkflowDeployment deployment = engine.deployDefinition(processDef.getInputStream(),
                    MimetypeMap.MIMETYPE_XML);
        testWorkflowDef = deployment.definition;
        assertNotNull(testWorkflowDef);
        assertEquals("jbpm_test$test", testWorkflowDef.name);
    }

    private NamespaceService makeNamespaceService()
    {
        NamespaceServiceMemoryImpl namespace = new NamespaceServiceMemoryImpl();
        namespace.registerNamespace(NamespaceService.DEFAULT_PREFIX, NamespaceService.DEFAULT_URI);
        namespace.registerNamespace("wf", "http://www.alfresco.org/model/bpm/1.0");
        namespace.registerNamespace("cm", "http://www.alfresco.org/model/content/1.0");
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
