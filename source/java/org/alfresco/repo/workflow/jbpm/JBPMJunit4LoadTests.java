package org.alfresco.repo.workflow.jbpm;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springmodules.workflow.jbpm31.JbpmCallback;
import org.springmodules.workflow.jbpm31.JbpmTemplate;

/**
 * This test shows a performance benefit from a usage of direct queries
 * instead of creating required classes like WorkflowTask in a loop with collecting
 * required properties from different services.
 * 
 * @author arsenyko
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/application-context.xml" })
public class JBPMJunit4LoadTests
{
    
    private static String WORKFLOW_NAME = "jbpm$wf:adhoc";
    private static String WORKFLOW_NODE_NAME = "workflow-test-19243cbb-c58a-485e-bcd9-2e2be030dfb9.txt";
    
    private static int WORKFLOW_COUNT = 2000;
    
    @Autowired
    protected ApplicationContext applicationContext;
    protected ServiceRegistry serviceRegistry;
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected NodeService nodeService;
    protected WorkflowService workflowService;
    protected FileFolderService fileFolderService;
    
    protected JBPMEngine jbpmEngine;
    
    protected AuthenticationComponent authenticationComponent;
    
    private NodeRef companyHomeNodeRef;
    
    @Before
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        serviceRegistry.getAuthenticationService().authenticate("admin", "admin".toCharArray());

        retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();
        fileFolderService = serviceRegistry.getFileFolderService();
        workflowService = serviceRegistry.getWorkflowService();
        nodeService = serviceRegistry.getNodeService();
        
        BPMEngineRegistry registry = (BPMEngineRegistry)applicationContext.getBean("bpm_engineRegistry");
        jbpmEngine = (JBPMEngine) registry.getWorkflowComponent("jbpm");
        
        NodeRef storeRootNodeRef = nodeService.getRootNode(new StoreRef("workspace://SpacesStore"));
        companyHomeNodeRef = serviceRegistry.getSearchService().selectNodes(storeRootNodeRef, "/app:company_home", null, serviceRegistry.getNamespaceService(), false).get(0);
        System.out.println(" -------------- ");
        createWorkflowStuff();
    }
    
    public void createWorkflowStuff() throws Exception
    {
        System.out.println(" [createWorkflowStuff] Started at " + new Date().toString());
        
        NodeRef workflowNode = nodeService.getChildByName(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, WORKFLOW_NODE_NAME);
        
        if (workflowNode == null)
        {
            RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>(){
    
                @Override
                public Void execute() throws Throwable
                {
                    FileInfo fileInfo = fileFolderService.create(companyHomeNodeRef, WORKFLOW_NODE_NAME, ContentModel.TYPE_CONTENT);
                    ContentWriter writer = serviceRegistry.getContentService().getWriter(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
                    writer.setMimetype("text/plain");
                    writer.setEncoding("UTF-8");
                    writer.putContent("many workflows many workflows many workflows many workflows many workflows many workflows many workflows many workflows");
                    System.out.println(" [createWorkflowStuff] Workflow node '" + WORKFLOW_NODE_NAME + "' has been created");
                    
                    //WorkflowDefinition wfDef = workflowService.getDefinitionByName(WORKFLOW_NAME);
                    WorkflowDefinition wfDef = jbpmEngine.getDefinitionByName(WORKFLOW_NAME);
                    long startTime = new Date().getTime();
                    for (Integer i = 0; i < WORKFLOW_COUNT; i++)
                    {
                        // We are creating workflows in usual way, but with new persistent objects.
                        // There is a some performance issue with sesssion.flash() in each iteration,
                        // but this was made to avoid a lot of changes in a logic related to org.alfresco.service.cmr.workflow.* 
                        // classes.
                        workflowService.startWorkflow(wfDef.id, prepareWorkflowProperties(fileInfo.getNodeRef(), i.toString()));
                        // jbpmEngine.startWorkflow_ALF1787(wfDef.id, prepareWorkflowProperties(fileInfo.getNodeRef(), i.toString()));
                    }
                    long endTime = new Date().getTime();
                    System.out.println(" [createWorkflowStuff] Execution time (ms): " + (endTime - startTime));
                    return null;
                }
                
            };
            retryingTransactionHelper.setMaxRetries(1);
            retryingTransactionHelper.doInTransaction(callback);
            System.out.println(" [createWorkflowStuff] Finished at " + new Date().toString());
        }
        else
        {
            System.out.println(" [createWorkflowStuff] Workflow node '" + WORKFLOW_NODE_NAME + "' already exists");
        }
    }
    
    @SuppressWarnings("unchecked")
    //@Test
    public void testQuery1() throws Exception
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                JbpmTemplate jbpmTemplate = (JbpmTemplate) applicationContext.getBean("jbpm_template");
                List<Object[]> result = (List<Object[]>) jbpmTemplate.execute(new JbpmCallback()
                {
                    public List<Object[]> doInJbpm(JbpmContext context)
                    {
                        Session session = context.getSession();
                        Query query = session.getNamedQuery("org.alfresco.repo.workflow.findTaskInstancesByActorId");
                        return query.setString("actorId", "admin").list();
                    }
                });
                for(Object[] ti : result)
                {
                    System.out.println(Arrays.toString(ti));
                }
                System.out.println(result.size());
                return null;
            }
        };
        retryingTransactionHelper.setMaxRetries(1);
        retryingTransactionHelper.doInTransaction(callback);
    }
    
    @Test
    public void testGetAssignedTasks_NEW() throws Exception
    {
        final int RUN_COUNT = 7;
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                Date beginTime = new Date();
                System.out.println(" [testGetAssignedTasks_NEW] Started at " + beginTime.toString());
                List<WorkflowTask> tasks = workflowService.getAssignedTasks("admin", WorkflowTaskState.IN_PROGRESS);
                Date endTime = new Date();
                System.out.println(" [testGetAssignedTasks_NEW] Retrieved tasks: " + tasks.size() + " in " + (endTime.getTime() - beginTime.getTime()) + " ms");
                System.out.println(" [testGetAssignedTasks_NEW] Finished at " + endTime.toString());
                return null;
            }
        };
        retryingTransactionHelper.setMaxRetries(1);
        for(int i=0; i<RUN_COUNT; i++)
        {
            retryingTransactionHelper.doInTransaction(callback);
        }
    }
    /*
    @Test
    public void testGetAssignedTasks_OLD() throws Exception
    {
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>(){

            @Override
            public Void execute() throws Throwable
            {
                Date beginTime = new Date();
                System.out.println(" [testGetAssignedTasks_OLD] Started at " + beginTime.toString());
                List<WorkflowTask> tasks = jbpmEngine.getAssignedTasks_OLD("admin", WorkflowTaskState.IN_PROGRESS);
                Date endTime = new Date();
                System.out.println(" [testGetAssignedTasks_OLD] Retrieved tasks: " + tasks.size() + " in " + (endTime.getTime() - beginTime.getTime()) + " ms");
                System.out.println(" [testGetAssignedTasks_OLD] Finished at " + new Date().toString());
                return null;
            }
        };
        retryingTransactionHelper.setMaxRetries(1);
        retryingTransactionHelper.doInTransaction(callback);
    }
    */
    
    @After
    public void tearDown() throws Exception
    {
        System.out.println(" -------------- ");
    }
    
    private Map<QName, Serializable> prepareWorkflowProperties(NodeRef nodeRef, String id)
    {
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        
        parameters.put(WorkflowModel.ASSOC_PACKAGE, nodeRef);
        parameters.put(WorkflowModel.ASSOC_ASSIGNEE, "admin");
        parameters.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "Test workflow '" + id + "'");
        parameters.put(WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME, "test_workflow_" + id);
        
        return parameters;

    }

}
