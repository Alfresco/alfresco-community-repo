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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.BPMEngineRegistry;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.AbstractTransactionalSpringContextTests;
import org.springmodules.workflow.jbpm31.JbpmTemplate;

/**
 * JBPMEngine Unit Tests.
 * 
 * @author Nick Smith
 */
public class JBPMEngineUnitTest extends AbstractTransactionalSpringContextTests
{
    private static final String USER_NAME = "admin";

    private static final String TEST_JBPM_ENGINE = "test_jbpm_engine";

    private static final NodeRef companyHome = new NodeRef("for://test/home");

    private JBPMEngine engine = new JBPMEngine();

    private WorkflowDefinition workflowDef;
    
    public void testDeployWorkflow() throws Exception
    {
        ClassPathResource processDef = new ClassPathResource("jbpmresources/test_processdefinition.xml");
        List<WorkflowDefinition> workflowDefs = engine.getDefinitions();
        assertFalse(engine.isDefinitionDeployed(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML));
        assertNotNull(workflowDefs);
        
        int workflowDefCnt = workflowDefs.size();

        deployTestDefinition();
        assertTrue(engine.isDefinitionDeployed(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML));
        workflowDefs = engine.getDefinitions();
        assertNotNull(workflowDefs);
        assertEquals(workflowDefCnt+1, workflowDefs.size());

        assertNotNull(workflowDef);
        assertEquals(TEST_JBPM_ENGINE + "$test", workflowDef.name);
        assertEquals("1", workflowDef.version);

        deployTestDefinition();
        assertTrue(engine.isDefinitionDeployed(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML));
        assertEquals("2", workflowDef.version);
    }

    public void testGetWorkflowInstance() throws Exception
    {
        deployTestDefinition();
        WorkflowPath path = engine.startWorkflow(workflowDef.getId(), null);
        checkPath(path);
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
        { // Do nothing here!
        }

        deployTestDefinition();
        WorkflowPath path = engine.startWorkflow(workflowDef.getId(), null);
        assertNotNull(path);
        assertTrue(path.id.endsWith("-@"));
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(workflowDef.getId(), path.instance.definition.id);
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
        WorkflowPath path = engine.startWorkflow(workflowDef.id, params);
        checkPath(path);
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

    public void testUpdateTask() throws Exception
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

        // context task assignment
        params.put(QName.createQName("", "NodeRef"), new NodeRef("workspace://1/1001"));

        params.put(ContentModel.PROP_OWNER, USER_NAME);

        deployTestDefinition();
        WorkflowPath path = engine.startWorkflow(workflowDef.id, params);
        checkPath(path);
        List<WorkflowTask> tasks1 = engine.getTasksForWorkflowPath(path.id);
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());

        WorkflowTask task = tasks1.get(0);
        assertTrue(task.properties.containsKey(WorkflowModel.PROP_TASK_ID));
        assertTrue(task.properties.containsKey(WorkflowModel.PROP_DUE_DATE));
        assertTrue(task.properties.containsKey(WorkflowModel.PROP_PRIORITY));
        assertTrue(task.properties.containsKey(WorkflowModel.PROP_PERCENT_COMPLETE));
        assertTrue(task.properties.containsKey(ContentModel.PROP_OWNER));

        // update with null parameters
        try
        {
            WorkflowTask taskU1 = engine.updateTask(task.id, null, null, null);
            assertNotNull(taskU1);
        }
        catch (Throwable e)
        {
            fail("Task update failed with null parameters");
        }

        // update property value
        Map<QName, Serializable> updateProperties2 = new HashMap<QName, Serializable>();
        updateProperties2.put(WorkflowModel.PROP_PERCENT_COMPLETE, 100);
        WorkflowTask taskU2 = engine.updateTask(task.id, updateProperties2, null, null);
        assertEquals(100, taskU2.properties.get(WorkflowModel.PROP_PERCENT_COMPLETE));

        // add to assocation
        QName assocName = QName.createQName("", "TestAssoc");
        List<NodeRef> toAdd = new ArrayList<NodeRef>();
        toAdd.add(new NodeRef("workspace://1/1001"));
        toAdd.add(new NodeRef("workspace://1/1002"));
        toAdd.add(new NodeRef("workspace://1/1003"));
        Map<QName, List<NodeRef>> addAssocs = new HashMap<QName, List<NodeRef>>();
        addAssocs.put(assocName, toAdd);
        WorkflowTask taskU3 = engine.updateTask(task.id, null, addAssocs, null);
        assertNotNull(taskU3.properties.get(assocName));
        Object assoc = taskU3.properties.get(assocName);
        assertNotNull(assoc);
        assertEquals(3, ((List<?>) assoc).size());

        // add to assocation again
        List<NodeRef> toAddAgain = new ArrayList<NodeRef>();
        toAddAgain.add(new NodeRef("workspace://1/1004"));
        toAddAgain.add(new NodeRef("workspace://1/1005"));
        Map<QName, List<NodeRef>> addAssocsAgain = new HashMap<QName, List<NodeRef>>();
        addAssocsAgain.put(assocName, toAddAgain);
        WorkflowTask taskU4 = engine.updateTask(task.id, null, addAssocsAgain, null);
        assertNotNull(taskU4.properties.get(assocName));
        assoc = taskU4.properties.get(assocName);
        assertEquals(5, ((List<?>) assoc).size());

        // remove assocation
        List<NodeRef> toRemove = new ArrayList<NodeRef>();
        toRemove.add(new NodeRef("workspace://1/1002"));
        toRemove.add(new NodeRef("workspace://1/1003"));
        Map<QName, List<NodeRef>> removeAssocs = new HashMap<QName, List<NodeRef>>();
        removeAssocs.put(assocName, toRemove);
        WorkflowTask taskU5 = engine.updateTask(task.id, null, null, removeAssocs);
        assertNotNull(taskU5.properties.get(assocName));
        assoc = taskU5.properties.get(assocName);
        assertEquals(3, ((List<?>) assoc).size());
    }

    public void testGetWorkflowInstances() throws Exception
    {
        deployTestDefinition();
        WorkflowPath path1 = engine.startWorkflow(workflowDef.id, null);
        WorkflowPath path2 = engine.startWorkflow(workflowDef.id, null);
        List<WorkflowInstance> instances = engine.getActiveWorkflows(workflowDef.id);
        assertNotNull(instances);
        assertEquals(2, instances.size());

        HashSet<String> ids = new HashSet<String>(2);
        ids.add(path1.instance.id);
        ids.add(path2.instance.id);

        for (WorkflowInstance instance : instances)
        {
            assertEquals(workflowDef.id, instance.definition.id);
            assertTrue(ids.contains(instance.id));
        }
    }

    public void testGetPositions() throws Exception
    {
        deployTestDefinition();
        engine.startWorkflow(workflowDef.id, null);
        List<WorkflowInstance> instances = engine.getActiveWorkflows(workflowDef.id);
        assertNotNull(instances);
        assertEquals(1, instances.size());
        List<WorkflowPath> paths = engine.getWorkflowPaths(instances.get(0).id);
        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertEquals(instances.get(0).id, paths.get(0).instance.id);
        assertTrue(paths.get(0).id.endsWith("-@"));
    }

    private void checkPath(WorkflowPath path)
    {
        assertNotNull(path);
        assertTrue(path.id.endsWith("-@"));
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(workflowDef.id, path.instance.definition.id);
    }

    /*
     * @seeorg.springframework.test.AbstractTransactionalSpringContextTests#
     * onSetUpBeforeTransaction()
     */
    @Override
    protected void onSetUpBeforeTransaction() throws Exception
    {
        super.onSetUpBeforeTransaction();
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
        BPMEngineRegistry engineRegistry = (BPMEngineRegistry) getApplicationContext().getBean(
                    "test_bpm_engineRegistry");

        ConfigurableApplicationContext ctx = getApplicationContext();
        JbpmTemplate jbpmTemplate = (JbpmTemplate) ctx.getBean("test_jbpm_template");
        // Set up the JBPMEngine.
        engine.setJBPMTemplate(jbpmTemplate);
        engine.setTenantService(tenantService);
        engine.setNodeService(nodeService);
        engine.setServiceRegistry(serviceRegistry);
        engine.setNamespaceService(namespaceService);
        engine.setMessageService(mock(MessageService.class));
        engine.setDictionaryService(dictionaryService);
        engine.setEngineId(TEST_JBPM_ENGINE);
        engine.setBPMEngineRegistry(engineRegistry);

        // Need to register JBPMEngine with bean factory so WorflowTaskInstance
        // can load it.
        ConfigurableApplicationContext appContext = getApplicationContext();
        if (!appContext.containsBean(TEST_JBPM_ENGINE))
            appContext.getBeanFactory().registerSingleton(TEST_JBPM_ENGINE, engine);
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

    // deploy test process definition
    private void deployTestDefinition() throws IOException
    {
        ClassPathResource processDef = new ClassPathResource("jbpmresources/test_processdefinition.xml");
        WorkflowDeployment deployment = engine.deployDefinition(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML);
        workflowDef = deployment.definition;
        assertNotNull(workflowDef);
        assertEquals(TEST_JBPM_ENGINE + "$test", workflowDef.name);
    }

    private NamespaceService makeNamespaceService()
    {
        NamespaceServiceMemoryImpl namespace = new NamespaceServiceMemoryImpl();
        namespace.registerNamespace(NamespaceService.DEFAULT_PREFIX, NamespaceService.DEFAULT_URI);
        
        namespace.registerNamespace("wf", "http://www.alfresco.org/model/bpm/1.0");
        namespace.registerNamespace("cm", "http://www.alfresco.org/model/content/1.0");
        namespace.registerNamespace("wcmwf", "http://www.alfresco.org/model/wcmworkflow/1.0");
        namespace.registerNamespace("imwf", "http://www.alfresco.org/model/workflow/invite/moderated/1.0");
        namespace.registerNamespace("inwf", "http://www.alfresco.org/model/workflow/invite/nominated/1.0");
        
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

    /*
     * @see
     * org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations
     * ()
     */
    @Override
    protected String[] getConfigLocations()
    {
        String[] locations = new String[] {
                    "classpath:jbpm-test/test-workflow-context.xml", };
        return locations;
    }
}
