/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;


/**
 * Workflow Service Implementation Tests
 * 
 * @author davidc
 */
public class WorkflowServiceImplTest extends BaseSpringTest
{
    private final static String USER1 = "WFUser1" + GUID.generate();
    private final static String USER2 = "WFUser2" + GUID.generate();
    private final static String USER3 = "WFUser3" + GUID.generate();
    private final static String GROUP = "WFGroup" + GUID.generate();
    private final static String SUB_GROUP = "WFSubGroup" + GUID.generate();
    
    private WorkflowService workflowService;
    private NodeService nodeService;
    private SearchService searchService;
    private AuthenticationComponent authenticationComponent;
    private MutableAuthenticationService authenticationService;
    private AuthorityService authorityService;
    private PersonService personService;
    private TestPersonManager personManager;

    //@Override
    @SuppressWarnings("deprecation")
    protected void onSetUpInTransaction() throws Exception
    {
        workflowService = (WorkflowService)applicationContext.getBean(ServiceRegistry.WORKFLOW_SERVICE.getLocalName());
        nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
        searchService = (SearchService)applicationContext.getBean(ServiceRegistry.SEARCH_SERVICE.getLocalName());
        authenticationService = (MutableAuthenticationService) applicationContext.getBean(ServiceRegistry.AUTHENTICATION_SERVICE.getLocalName());
        authorityService = (AuthorityService) applicationContext.getBean(ServiceRegistry.AUTHORITY_SERVICE.getLocalName());
        personService = (PersonService) applicationContext.getBean(ServiceRegistry.PERSON_SERVICE.getLocalName());
        
        // authenticate
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // create test users
        personManager = new TestPersonManager(authenticationService, personService, nodeService);
        personManager.createPerson(USER1);
        personManager.createPerson(USER2);
        personManager.createPerson(USER3);

        // create test groups
        createGroup(null, GROUP);
        createGroup(GROUP, SUB_GROUP);
        
        // add users to groups
        addUserToGroup(GROUP, USER1);
        addUserToGroup(SUB_GROUP, USER2);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        super.onTearDownInTransaction();
        
        authenticationComponent.setSystemUserAsCurrentUser();
        deleteGroup(SUB_GROUP);
        deleteGroup(GROUP);
        personManager.clearPeople();
    }

    protected void createGroup(String parentGroupShortName, String groupShortName)
    {
        if (parentGroupShortName != null)
        {
            String parentGroupFullName = authorityService.getName(AuthorityType.GROUP, parentGroupShortName);
            if (authorityService.authorityExists(parentGroupFullName))
            {
                authorityService.createAuthority(AuthorityType.GROUP, groupShortName, groupShortName, null);
                
                String groupFullName = authorityService.getName(AuthorityType.GROUP, groupShortName);
                authorityService.addAuthority(parentGroupFullName, groupFullName);
            }
        }
        else
        {
            authorityService.createAuthority(AuthorityType.GROUP, groupShortName, groupShortName, null);
        }
    }
    
    protected void addUserToGroup(String groupName, String userName)
    {
        // get the full name for the group
        String fullGroupName = this.authorityService.getName(AuthorityType.GROUP, groupName);

        // create group if it does not exist
        if (this.authorityService.authorityExists(fullGroupName) == false)
        {
            this.authorityService.createAuthority(AuthorityType.GROUP, fullGroupName);
        }

        // add the user to the group
        this.authorityService.addAuthority(fullGroupName, userName);
    }
    
    protected void deleteGroup(String groupShortName)
    {
        String groupFullName = authorityService.getName(AuthorityType.GROUP, groupShortName);
        if (authorityService.authorityExists(groupFullName) == true)
        {
            authorityService.deleteAuthority(groupFullName);
        }
    }
    
    protected NodeRef findGroup(String shortGroupName)
    {
        NodeRef group = null;
        
        String query = "+TYPE:\"cm:authorityContainer\" AND @cm\\:authorityName:*" + shortGroupName;
        
        ResultSet results = null;
        try
        {
            results = this.searchService.query(
                    new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"), 
                    SearchService.LANGUAGE_LUCENE, query);
            
            if (results.length() > 0)
            {
                group = results.getNodeRefs().get(0);
            }
        }
        finally
        {
            if (results != null)
            {
                results.close();
            }
        }
        
        return group;
    }

    public void testGetWorkflowDefinitions()
    {
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
    }
    
    public void testStartWorkflow()
    {
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
        WorkflowDefinition workflowDef = workflowDefs.get(0);
        WorkflowPath path = workflowService.startWorkflow(workflowDef.id, null);
        assertNotNull(path);
        assertTrue(path.active);
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(workflowDef.id, path.instance.definition.id);
    }
    
    public void testWorkflowPackage()
    {
        NodeRef nodeRef = workflowService.createPackage(null);
        assertNotNull(nodeRef);
        assertTrue(nodeService.hasAspect(nodeRef, WorkflowModel.ASPECT_WORKFLOW_PACKAGE));
    }
    
    public void testQueryTasks()
    {
        WorkflowTaskQuery filter = new WorkflowTaskQuery();
        filter.setTaskName(QName.createQName("{http://www.alfresco.org/model/wcmworkflow/1.0}submitpendingTask"));
        filter.setTaskState(WorkflowTaskState.COMPLETED);
        Map<QName, Object> taskProps = new HashMap<QName, Object>();
        taskProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}workflowDescription"), "Test5");
        filter.setTaskCustomProps(taskProps);
        filter.setProcessId("jbpm$48");
        filter.setProcessName(QName.createQName("{http://www.alfresco.org/model/wcmworkflow/1.0}submit"));
        Map<QName, Object> procProps = new HashMap<QName, Object>();
        procProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}workflowDescription"), "Test5");
        procProps.put(QName.createQName("companyhome"), new NodeRef("workspace://SpacesStore/3df8a9d0-ff04-11db-98da-a3c3f3149ea5"));
        filter.setProcessCustomProps(procProps);
        filter.setOrderBy(new WorkflowTaskQuery.OrderBy[] { WorkflowTaskQuery.OrderBy.TaskName_Asc, WorkflowTaskQuery.OrderBy.TaskState_Asc });
        List<WorkflowTask> tasks = workflowService.queryTasks(filter);
        System.out.println("Found " + tasks.size() + " tasks.");
        for (WorkflowTask task : tasks)
        {
            System.out.println(task.toString());
        }
    }
    
    public void testAssociateWorkflowPackage()
    {
        // create workflow package
        NodeRef rootRef = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        NodeRef nodeRef = workflowService.createPackage(null);
        assertNotNull(nodeRef);
        assertTrue(nodeService.hasAspect(nodeRef, WorkflowModel.ASPECT_WORKFLOW_PACKAGE));
        ChildAssociationRef childAssoc = nodeService.createNode(rootRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test"), ContentModel.TYPE_CONTENT, null);

        List<WorkflowInstance> exisingInstances = workflowService.getWorkflowsForContent(childAssoc.getChildRef(), true);
        int size = 0;
        if (exisingInstances != null)
        {
            size = exisingInstances.size();
        }
        
        nodeService.addChild(nodeRef, childAssoc.getChildRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test123"));
        
        // start workflow
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
        WorkflowDefinition workflowDef = workflowDefs.get(0);
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(WorkflowModel.ASSOC_PACKAGE, nodeRef);
        WorkflowPath path = workflowService.startWorkflow(workflowDef.id, parameters);
        assertNotNull(path);
        assertTrue(path.active);
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(workflowDef.id, path.instance.definition.id);
        String workflowDefId = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_ID);
        assertEquals(workflowDefId, workflowDef.id);
        String workflowDefName = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME);
        assertEquals(workflowDefName, workflowDef.name);
        String workflowInstanceId = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID);
        assertEquals(workflowInstanceId, path.instance.id);

        // get workflows for content
        List<WorkflowInstance> instances = workflowService.getWorkflowsForContent(childAssoc.getChildRef(), true);
        assertNotNull(instances);
        assertEquals(size + 1, instances.size());

        for (WorkflowInstance instance : instances)
        {
            boolean fNew = true;
            for (WorkflowInstance exisingInstance : exisingInstances)
            {
                if (instance.id.equals(exisingInstance.id))
                {
                    fNew = false;
                    break;
                }
                fNew = true;
                break;
            }

            if (fNew)
            {
                assertEquals(instance.id, path.instance.id);
            }

        }
        
        List<WorkflowInstance> completedInstances = workflowService.getWorkflowsForContent(childAssoc.getChildRef(), false);
        assertNotNull(completedInstances);
        assertEquals(0, completedInstances.size());
    }
    
    public void testGetWorkflowTaskDefinitions()
    {
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
        
        for (WorkflowDefinition workflowDef : workflowDefs)
        {
        	List<WorkflowTaskDefinition> workflowTaskDefs = workflowService.getTaskDefinitions(workflowDef.getId());
            assertNotNull(workflowTaskDefs);
            assertTrue(workflowTaskDefs.size() > 0);
        }
    }
    
    public void testTaskCapabilities()
    {
        // start Adhoc workflow as USER1 and assign to USER2
        personManager.setUser(USER1);
        
        // Get the workflow definition.
        WorkflowDefinition workflowDef = this.workflowService.getDefinitionByName("jbpm$wf:adhoc");
        assertNotNull(workflowDef);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        NodeRef assignee = personService.getPerson(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        String workflowInstanceId = path.getInstance().getId();
        
        // End start task to progress workflow
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask startTask = tasks.get(0);
        String startTaskId = startTask.getId();
        workflowService.endTask(startTaskId, null);
        
        // Fetch start task and check capabilities
        startTask = workflowService.getTaskById(startTaskId);
        assertNotNull(startTask);
        assertEquals(startTask.getState(), WorkflowTaskState.COMPLETED);
        
        // check nothing can be done to the task as its completed
        assertFalse(workflowService.isTaskClaimable(startTask, USER1));
        assertFalse(workflowService.isTaskEditable(startTask, USER1));
        assertFalse(workflowService.isTaskReassignable(startTask, USER1));
        assertFalse(workflowService.isTaskReleasable(startTask, USER1));
        
        // Fetch the current task in the workflow
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
        assertNotNull(paths);
        assertEquals(1, paths.size());
        tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask currentTask = tasks.get(0);
        assertEquals(currentTask.getState(), WorkflowTaskState.IN_PROGRESS);
        assertEquals(currentTask.getProperties().get(ContentModel.PROP_OWNER), USER2);
        
        // check the task is not claimable or releasable by any user as it is not a pooled task
        assertFalse(workflowService.isTaskClaimable(currentTask, USER1));
        assertFalse(workflowService.isTaskClaimable(currentTask, USER2));
        assertFalse(workflowService.isTaskClaimable(currentTask, USER3));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER1));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER2));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER3));
        
        // user1 (initiator) and user2 (owner) should be able to edit and reassign task
        assertTrue(workflowService.isTaskEditable(currentTask, USER1));
        assertTrue(workflowService.isTaskEditable(currentTask, USER2));
        assertTrue(workflowService.isTaskReassignable(currentTask, USER1));
        assertTrue(workflowService.isTaskReassignable(currentTask, USER2));
        
        // user3 should not be able to edit or reassign task
        assertFalse(workflowService.isTaskEditable(currentTask, USER3));
        assertFalse(workflowService.isTaskReassignable(currentTask, USER3));
        
        // cancel the workflow
        workflowService.cancelWorkflow(workflowInstanceId);
        assertNull(workflowService.getWorkflowById(workflowInstanceId));
    }
    
    public void testPooledTaskCapabilities()
    {
        // make admin current user
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // start pooled review and approve workflow
        WorkflowDefinition workflowDef = this.workflowService.getDefinitionByName("jbpm$wf:reviewpooled");
        assertNotNull(workflowDef);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        NodeRef group = findGroup(GROUP);
        assertNotNull(group);
        params.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, group);
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        String workflowInstanceId = path.getInstance().getId();
        
        // End start task to progress workflow
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask startTask = tasks.get(0);
        String startTaskId = startTask.getId();
        workflowService.endTask(startTaskId, null);
        
        // Fetch the current task in the workflow
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
        assertNotNull(paths);
        assertEquals(1, paths.size());
        tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask currentTask = tasks.get(0);
        assertEquals(currentTask.getState(), WorkflowTaskState.IN_PROGRESS);
        assertNull(currentTask.getProperties().get(ContentModel.PROP_OWNER));
        
        // ensure the task is not reassignable by any user
        assertFalse(workflowService.isTaskReassignable(currentTask, USER1));
        assertFalse(workflowService.isTaskReassignable(currentTask, USER2));
        assertFalse(workflowService.isTaskReassignable(currentTask, USER3));
        
        // ensure the task is not releasable by any user
        assertFalse(workflowService.isTaskReleasable(currentTask, USER1));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER2));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER3));
        
        // ensure the task is claimable by the members of the group and sub group
        assertTrue(workflowService.isTaskClaimable(currentTask, USER1));
        assertTrue(workflowService.isTaskClaimable(currentTask, USER2));
        
        // ensure the task is not claimable by members outside of the group
        assertFalse(workflowService.isTaskClaimable(currentTask, USER3));
        
        // ensure the task can be edited 
        assertTrue(workflowService.isTaskEditable(currentTask, USER1));
        assertTrue(workflowService.isTaskEditable(currentTask, USER2));
        assertFalse(workflowService.isTaskEditable(currentTask, USER3));
        
        // claim the task for USER1
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(8);
        properties.put(ContentModel.PROP_OWNER, USER1);
        workflowService.updateTask(currentTask.getId(), properties, null, null);
        currentTask = workflowService.getTaskById(currentTask.getId());
        
        // check flags are correct now USER1 is the owner
        assertFalse(workflowService.isTaskClaimable(currentTask, USER1));
        assertTrue(workflowService.isTaskReleasable(currentTask, USER1));
        assertTrue(workflowService.isTaskEditable(currentTask, USER1));
        assertFalse(workflowService.isTaskReassignable(currentTask, USER1));
        assertFalse(workflowService.isTaskClaimable(currentTask, USER2));
        assertFalse(workflowService.isTaskEditable(currentTask, USER2));
        
        // cancel the workflow
        workflowService.cancelWorkflow(workflowInstanceId);
        assertNull(workflowService.getWorkflowById(workflowInstanceId));
    }
}
