/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.workflow.activiti;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.variable.ScriptNodeVariableType;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @since 3.4.e
 * @author Nick Smith
 * @author Frederik Heremans
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-database-context.xml",
            "classpath:activiti/test-activiti-component-context.xml",
            "classpath:alfresco/activiti-context.xml"})
@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=true)
@Transactional
public class AbstractActivitiComponentTest
{
    protected static final String TEST_GROUP = "GROUP_testGroup";
    protected static final String TEST_USER = "testUser";
    protected static final String TEST_TASK_DEF = "activiti/testTransaction.bpmn20.xml";
    protected static final String TEST_TASK_KEY = "testTask";
    protected static final String TEST_ADHOC_DEF = "activiti/testAdhoc.bpmn20.xml";
    protected static final String TEST_SIGNALLING_DEF = "activiti/testSignalling.bpmn20.xml";
    protected static final String TEST_REVIEW_DEF = "activiti/testReview.bpmn20.xml";
    protected static final String TEST_ADHOC_KEY = "testAdhoc";
    protected static final String TEST_JOB_KEY = "testAdhoc";
    protected static final String TEST_JOB_DEF = "activiti/testJob.bpmn20.xml";
    protected static final String TEST_DIAGRAM_DEF = "activiti/testDiagram.bpmn20.xml";
    
    protected static final String XML = MimetypeMap.MIMETYPE_XML;

    @Autowired
    protected ProcessEngine processEngine;

    @Resource(name="activitiWorkflowEngine")
    protected ActivitiWorkflowEngine workflowEngine;
    @Resource(name="activitiRuntimeService")
    protected RuntimeService runtime;
    @Resource(name="activitiRepositoryService")
    protected RepositoryService repo;
    @Resource(name="activitiTaskService")
    protected TaskService taskService;
    @Resource(name="activitiHistoryService")
    protected HistoryService historyService;
    @Resource(name="activitiManagementService")
    protected ManagementService managementService;
    
    @Resource
    protected MessageService messageService;

    @Resource
    protected TenantService tenantService;
    
    @Resource(name="NamespaceService")
    protected NamespaceService namespaceService;

    @Resource(name="DictionaryService")
    protected DictionaryService dictionaryService;

    @Resource(name="NodeService")
    protected NodeService nodeService;
    
    @Resource(name="searchService")
    protected SearchService unprotectedSearchService;
    
    @Resource
    protected PermissionService permissionService;
    
    @Resource(name="PersonService")
    protected PersonService personService;
    
    @Resource
    protected AuthorityDAO authorityDAO;
    
    @Resource
    protected ServiceRegistry serviceRegistry;
    
    protected static final NodeRef rootNode = new NodeRef("workspace://root/");
    protected static final NodeRef companyHomeNode = new NodeRef("workspace://companyHome/");
    protected static final NodeRef adminPersonNode = new NodeRef("workspace://admin/");
    protected static final NodeRef adminHomeNode = new NodeRef("workspace://admin-home/");
    protected static final NodeRef testUserNode = new NodeRef("workspace://testUser/");
    protected static final NodeRef testGroupNode = new NodeRef("workspace://testGroup/");
    protected static final NodeRef testWorkflowPackage = new NodeRef("workspace://testPackage/");
    protected static final NodeRef testWorkflowContext = new NodeRef("workspace://testContext/");
    
    protected WorkflowDefinition deployTestTaskDefinition()
    {
        return deployDefinition(TEST_TASK_DEF);
    }

    protected WorkflowDefinition deployTestAdhocDefinition()
    {
        return deployDefinition(TEST_ADHOC_DEF);
    }
    
    protected WorkflowDefinition deployTestSignallingDefinition()
    {
        return deployDefinition(TEST_SIGNALLING_DEF);
    }
    
    protected WorkflowDefinition deployTestJobDefinition()
    {
        return deployDefinition(TEST_JOB_DEF);
    }
    
    protected WorkflowDefinition deployTestDiagramDefinition()
    {
        return deployDefinition(TEST_DIAGRAM_DEF);
    }
    
    protected WorkflowDefinition deployDefinition(String resource)
    {
        InputStream input = getInputStream(resource);
        WorkflowDeployment deployment = workflowEngine.deployDefinition(input, XML);
        WorkflowDefinition definition = deployment.getDefinition();
        return definition;
    }

    protected InputStream getInputStream(String resource)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input= classLoader.getResourceAsStream(resource);
        return input;
    }


    @Before
    public void setUp() throws Exception
    {
        mockTenantService();
        mockNamespaceService();
        mockDictionaryService();
        mockNodeService();
        mockSearchService();
        mockPermissionService();
        mockPersonService();
        mockAuthorityDAO();
        mockServiceRegistry();

        workflowEngine.setCompanyHomeStore("workspace://SpacesStore");
        workflowEngine.setCompanyHomePath("spaces.company_home.childname");
        
        // Also add custom type
        // TODO: Should come from configuration
        ScriptNodeVariableType variableType = new ScriptNodeVariableType();
        variableType.setServiceRegistry(serviceRegistry);
        
//        ((ProcessEngineImpl)processEngine).getProcessEngineConfiguration().getVariableTypes().addType(variableType, 1);
        
        // Use util to set current user to admin 
        AuthenticationUtil.setFullyAuthenticatedUser("admin");
        
        workflowEngine.afterPropertiesSet();
    }

    /**
     * 
     */
    private void mockServiceRegistry()
    {
        // Set all services on the mocked Serviceregistry, injected by spring
        when(serviceRegistry.getNodeService()).thenReturn(nodeService);
        when(serviceRegistry.getDictionaryService()).thenReturn(dictionaryService);
        when(serviceRegistry.getPermissionService()).thenReturn(permissionService);
    }
    
    private void mockAuthorityDAO()
    {
       when(authorityDAO.authorityExists(TEST_USER)).thenReturn(true);
       when(authorityDAO.authorityExists(TEST_GROUP)).thenReturn(true);
    
       // We will return dummy node refs to authorities testUser and testGroup
       when(authorityDAO.getAuthorityNodeRefOrNull(TEST_USER)).thenReturn(testUserNode);
       when(authorityDAO.getAuthorityNodeRefOrNull(TEST_GROUP)).thenReturn(testGroupNode);
    }

    private void mockPersonService()
    {
        // Checking if admin exists
        when(personService.personExists("admin")).thenReturn(true);
        
        // Return reference to Admin person
        when(personService.getPerson("admin")).thenReturn(adminPersonNode);
        
        // Check if test-user exists
        when(personService.personExists(TEST_USER)).thenReturn(true);
        when(personService.getPerson(TEST_USER)).thenReturn(testUserNode);
    }

    private void mockPermissionService()
    {
        // Allow permission on all nodes
        when(permissionService.hasPermission((NodeRef) any(), anyString())).thenReturn(AccessStatus.ALLOWED);
    }

    private void mockNodeService()
    {
        // Return root store
        when(nodeService.getRootNode(new StoreRef("workspace://SpacesStore"))).thenReturn(rootNode);
        
        // Return company home and it's type
        when(nodeService.exists(companyHomeNode)).thenReturn(true);
        when(nodeService.getType((NodeRef) any())).thenReturn(QName.createQName("cm:folder"));
        
        // Return admin's home property
        when(nodeService.getProperty(adminPersonNode, ContentModel.PROP_HOMEFOLDER)).thenReturn(adminHomeNode);
        
        // Return testUser and testGroup types
        when(nodeService.getType(testUserNode)).thenReturn(ContentModel.TYPE_PERSON);
        when(nodeService.getType(testGroupNode)).thenReturn(ContentModel.TYPE_AUTHORITY);
    }
    
    private void mockSearchService()
    {
        // When searching for company home, return single node
        when(unprotectedSearchService.selectNodes(rootNode, "spaces.company_home.childname", null, namespaceService, false)).thenReturn(Arrays.asList(companyHomeNode));
    }

    /**
     * @return
     */
    private void mockDictionaryService()
    {
        Mockito.reset(dictionaryService);
        when(dictionaryService.getType((QName)any())).thenAnswer(new Answer<TypeDefinition>()
        {
            @Override
            public TypeDefinition answer(InvocationOnMock invocation) throws Throwable
            {
                QName name = (QName) invocation.getArguments()[0];
                TypeDefinition type = mock(TypeDefinition.class);
           
                when(type.getName()).thenReturn(name);
                return type;
            }
        });
        
        when(dictionaryService.getAnonymousType((QName)any())).thenAnswer(new Answer<TypeDefinition>()
                    {
                        @Override
                        public TypeDefinition answer(InvocationOnMock invocation) throws Throwable
                        {
                            QName name = (QName) invocation.getArguments()[0];
                            TypeDefinition type = mock(TypeDefinition.class);
                       
                            when(type.getName()).thenReturn(name);
                            
                            // Add a default value
                            Map<QName, PropertyDefinition> props = new HashMap<QName, PropertyDefinition>();
                            QName qname = QName.createQName("http://test", "myProp");
                            
                            
                            DataTypeDefinition qNameDef = mock(DataTypeDefinition.class);
                            when(qNameDef.getName()).thenReturn(DataTypeDefinition.QNAME);
                            when(qNameDef.getJavaClassName()).thenReturn(QName.class.getName());
                            
                            // Create dummy property type
                            DataTypeDefinition def = mock(DataTypeDefinition.class);
                            when(def.getName()).thenReturn(DataTypeDefinition.TEXT);
                            when(def.getJavaClassName()).thenReturn(String.class.getName());
                            
                            // Create dummy property definition
                            PropertyDefinition prop = mock(PropertyDefinition.class);
                            when(prop.getName()).thenReturn(qname);
                            when(prop.getDefaultValue()).thenReturn("Default value");
                            when(prop.getDataType()).thenReturn(def);
                            
                            // Also add description
                            PropertyDefinition description = mock(PropertyDefinition.class);
                            when(description.getName()).thenReturn(WorkflowModel.PROP_DESCRIPTION);
                            when(description.getDataType()).thenReturn(def);
                            
                            // Add outcome property name
                            PropertyDefinition outcomePropertyName = mock(PropertyDefinition.class);
                            when(outcomePropertyName.getName()).thenReturn(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
                            when(outcomePropertyName.getDataType()).thenReturn(qNameDef);
                            when(outcomePropertyName.getDefaultValue()).thenReturn("{http://test}testOutcome");
                            
                           // Add outcome property
                            PropertyDefinition outcomeProperty = mock(PropertyDefinition.class);
                            when(outcomeProperty.getName()).thenReturn(QName.createQName("http://test", "testOutcome"));
                            when(outcomeProperty.getDataType()).thenReturn(def);
                            
                            props.put(qname, prop);
                            props.put(WorkflowModel.PROP_DESCRIPTION, description);
                            props.put(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME, outcomePropertyName);
                            props.put(QName.createQName("http://test", "testOutcome"), outcomeProperty);
                            
                            when(type.getProperties()).thenReturn(props);
                            return type;
                        }
                    });
        
        // Mock type inheritance for person nodes
        when(dictionaryService.isSubClass(ContentModel.TYPE_PERSON, ContentModel.TYPE_PERSON)).thenReturn(true);
    }

    private void mockNamespaceService()
    {
        namespaceService.registerNamespace(NamespaceService.BPM_MODEL_PREFIX, NamespaceService.BPM_MODEL_1_0_URI);
        namespaceService.registerNamespace(NamespaceService.DEFAULT_PREFIX, NamespaceService.DEFAULT_URI);
        namespaceService.registerNamespace(NamespaceService.WORKFLOW_MODEL_PREFIX, NamespaceService.WORKFLOW_MODEL_1_0_URI);
        namespaceService.registerNamespace("test", "http://test");
    }

    private void mockTenantService()
    {
        when(tenantService.getBaseName(anyString())).thenAnswer(new Answer<String>()
        {
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                Object arg= invocation.getArguments()[0];
                return (String) arg;
            }
        });
    }
    
    @After
    public void tearDown()
    {
        List<ProcessDefinition> defs = repo.createProcessDefinitionQuery()
            .processDefinitionKey(TEST_TASK_KEY)
            .list();
        HashSet<String> deployments = new HashSet<String>(defs.size());
        for (ProcessDefinition def : defs)
        {
            deployments.add(def.getDeploymentId());
        }
        for (String deployment : deployments)
        {
            List<ProcessDefinition> definitions = repo.createProcessDefinitionQuery()
                .deploymentId(deployment)
                .list();
            for (ProcessDefinition def : definitions)
            {
                killInstances(def);
            }
            repo.deleteDeployment(deployment);
        }
    }
    
        public String mapQNameToName(QName name)
    {
        // NOTE: Map names using old conversion scheme (i.e. : -> _) as well as
        // new scheme (i.e. } -> _)
        // NOTE: Use new scheme
        String nameStr = name.toPrefixString(this.namespaceService);
        if (nameStr.indexOf('_') != -1 && nameStr.indexOf('_') < nameStr.indexOf(':'))
        {
            return nameStr.replace(':', '}');
        }
        return nameStr.replace(':', '_');
    }
    

    private void killInstances(ProcessDefinition def)
    {
        List<ProcessInstance> instances = runtime.createProcessInstanceQuery()
            .processDefinitionId(def.getId())
            .list();
        for (ProcessInstance instance : instances)
        {
            runtime.deleteProcessInstance(instance.getId(), "For test");
        }
    }
    
}
