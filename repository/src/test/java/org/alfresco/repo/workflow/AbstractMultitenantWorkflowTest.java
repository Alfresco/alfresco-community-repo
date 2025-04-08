/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.workflow;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
@Transactional
@ContextConfiguration({"classpath:tenant/mt-*context.xml"})
public abstract class AbstractMultitenantWorkflowTest extends BaseSpringTest
{
    protected static final String XML = MimetypeMap.MIMETYPE_XML;

    public static final String DEFAULT_ADMIN_PW = "admin";
    private final static QName ADHOC_TASK = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "adhocTask");

    private final String tenant1 = "wfMTTest1" + GUID.generate();
    private final String tenant2 = "wfMTTest2" + GUID.generate();

    private TenantAdminService tenantAdminService;

    protected TenantService tenantService;

    protected ServiceRegistry serviceRegistry;

    protected WorkflowService workflowService;
    protected PersonService personService;

    protected String user1;
    protected String user2;

    private WorkflowTestHelper wfTestHelper;

    @Test
    public void xtestDeployWorkflow() throws Exception
    {
        // Run as User1 so tenant domain 1
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        TenantContextHolder.setTenantDomain(tenant1);

        List<WorkflowDefinition> allDefs = workflowService.getAllDefinitions();
        int allDefsSize = allDefs.size();
        List<WorkflowDefinition> defs = workflowService.getDefinitions();
        int defsSize = defs.size();

        String definitionKey = getTestDefinitionKey();
        // Deploy to tenant1.
        WorkflowDefinition definition = workflowService.getDefinitionByName(definitionKey);
        assertNull(definition);

        // Check definition was successfully deployed on tenant1.
        definition = deployDefinition(getTestDefinitionPath());
        assertNotNull(definition);
        assertEquals(definitionKey, definition.getName());
        assertNotNull(workflowService.getDefinitionById(definition.getId()));
        assertNotNull(workflowService.getDefinitionByName(definitionKey));

        assertEquals(defsSize + 1, workflowService.getDefinitions().size());
        assertEquals(allDefsSize + 1, workflowService.getAllDefinitions().size());

        // Switch to tenant2.
        AuthenticationUtil.setFullyAuthenticatedUser(user2);
        TenantContextHolder.setTenantDomain(tenant2);

        // Check definition not visible on tenant2.
        try
        {
            assertNull(workflowService.getDefinitionById(definition.getId()));
            fail("Should throw Exception here!");
        }
        catch (Exception e)
        {
            // NOOP
        }
        assertNull(workflowService.getDefinitionByName(definitionKey));

        assertEquals(defsSize, workflowService.getDefinitions().size());
        assertEquals(allDefsSize, workflowService.getAllDefinitions().size());

        // Check can deploy same definition to tenant2.
        assertNotNull(deployDefinition(getTestDefinitionPath()));

        // Switch back to tenant1.
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        TenantContextHolder.setTenantDomain(tenant1);

        // Check the definition hasn't changed
        WorkflowDefinition definitionByName = workflowService.getDefinitionByName(definitionKey);
        assertEquals(definition.getId(), definitionByName.getId());
    }

    public void testQueryTasks() throws Exception
    {
        WorkflowTaskQuery query = new WorkflowTaskQuery();

        query.setTaskName(ADHOC_TASK);

        // Run as User1 so tenant domain 1
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
        TenantContextHolder.setTenantDomain(tenant1);

        // Check no tasks to start with
        List<WorkflowTask> tasks = workflowService.queryTasks(query);
        assertEquals(0, tasks.size());

        String adhocKey = getAdhocDefinitionKey();
        WorkflowDefinition adhocDef1 = workflowService.getDefinitionByName(adhocKey);
        // Check Adhoc definition exists
        assertNotNull(adhocDef1);

        // Start Adhoc workflow on tenant1.
        NodeRef assignee1 = personService.getPerson(user1);
        NodeRef pckg1 = workflowService.createPackage(null);
        Map<QName, Serializable> parameters1 = new HashMap<QName, Serializable>();
        parameters1.put(WorkflowModel.ASSOC_PACKAGE, pckg1);
        parameters1.put(WorkflowModel.ASSOC_ASSIGNEE, assignee1);
        WorkflowPath path = workflowService.startWorkflow(adhocDef1.getId(), parameters1);

        // End start task
        String instanceId = path.getInstance().getId();
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        workflowService.endTask(startTask.getId(), null);

        // Should have found the new task.
        tasks = workflowService.queryTasks(query);
        assertEquals(1, tasks.size());

        // Switch to tenant2
        AuthenticationUtil.setFullyAuthenticatedUser(user2);
        TenantContextHolder.setTenantDomain(tenant2);

        // Tenant2 should not find the task
        tasks = workflowService.queryTasks(query);
        assertEquals(0, tasks.size());

        // Start Adhoc workflow on tenant2.
        WorkflowDefinition adhocDef2 = workflowService.getDefinitionByName(adhocKey);
        NodeRef assignee2 = personService.getPerson(user2);
        NodeRef pckg2 = workflowService.createPackage(null);
        Map<QName, Serializable> parameters2 = new HashMap<QName, Serializable>();
        parameters2.put(WorkflowModel.ASSOC_PACKAGE, pckg2);
        parameters2.put(WorkflowModel.ASSOC_ASSIGNEE, assignee2);
        WorkflowPath path2 = workflowService.startWorkflow(adhocDef2.getId(), parameters2);
        String path2Id = path2.getId();

        // End start task
        String instanceId2 = path2.getInstance().getId();
        WorkflowTask startTask2 = workflowService.getStartTask(instanceId2);
        workflowService.endTask(startTask2.getId(), null);

        // Tenant2 should find the new task
        tasks = workflowService.queryTasks(query);
        assertEquals(1, tasks.size());
        assertEquals(path2Id, tasks.get(0).getPath().getId());

    }

    @Before
    public void before() throws Exception
    {
        this.serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        this.tenantService = (TenantService) applicationContext.getBean("tenantService");
        this.tenantAdminService = (TenantAdminService) applicationContext.getBean("tenantAdminService");

        this.workflowService = serviceRegistry.getWorkflowService();
        this.personService = serviceRegistry.getPersonService();

        WorkflowAdminServiceImpl workflowAdminService = (WorkflowAdminServiceImpl) applicationContext.getBean(WorkflowAdminServiceImpl.NAME);
        this.wfTestHelper = new WorkflowTestHelper(workflowAdminService, getEngine(), true);

        AuthenticationUtil.clearCurrentSecurityContext();
        this.user1 = createTenant(tenant1);
        this.user2 = createTenant(tenant2);
    }

    private String createTenant(final String tenantDomain)
    {
        // create tenants (if not already created)
        return AuthenticationUtil.runAsSystem(new RunAsWork<String>() {
            public String doWork() throws Exception
            {
                if (!tenantAdminService.existsTenant(tenantDomain))
                {
                    tenantAdminService.createTenant(tenantDomain, (DEFAULT_ADMIN_PW + " " + tenantDomain).toCharArray(), null); // use default root dir
                }
                return tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            }
        });
    }

    @After
    public void after() throws Exception
    {
        wfTestHelper.tearDown();

        WorkflowSuiteContextShutdownTest.closeContext();
    }

    protected WorkflowDefinition deployDefinition(String resource)
    {
        InputStream input = getInputStream(resource);
        WorkflowDeployment deployment = workflowService.deployDefinition(getEngine(), input, XML);
        WorkflowDefinition definition = deployment.getDefinition();
        return definition;
    }

    protected InputStream getInputStream(String resource)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(resource);
    }

    protected abstract String getTestDefinitionPath();

    protected abstract String getTestDefinitionKey();

    protected abstract String getAdhocDefinitionKey();

    protected abstract String getEngine();

}
