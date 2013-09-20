package org.alfresco.rest.workflow.api.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.DeploymentBuilder;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.rest.api.tests.EnterpriseTestApi;
import org.alfresco.rest.api.tests.RepoService.SiteInformation;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.RepoService.TestPerson;
import org.alfresco.rest.api.tests.RepoService.TestSite;
import org.alfresco.rest.api.tests.TestFixture;
import org.alfresco.rest.api.tests.client.AuthenticatedHttp;
import org.alfresco.rest.api.tests.client.AuthenticationDetailsProvider;
import org.alfresco.rest.api.tests.client.HttpClientProvider;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.UserAuthenticationDetailsProviderImpl;
import org.alfresco.rest.api.tests.client.UserData;
import org.alfresco.rest.api.tests.client.UserDataService;
import org.alfresco.rest.api.tests.client.data.MemberOfSite;
import org.alfresco.rest.workflow.api.model.ProcessInfo;
import org.alfresco.rest.workflow.api.tests.WorkflowApiClient.ProcessesClient;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.util.GUID;
import org.alfresco.util.ISO8601DateFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;

public class EnterpriseWorkflowTestApi extends EnterpriseTestApi
{
    protected ProcessEngine activitiProcessEngine;
    protected WorkflowApiClient publicApiClient;
    protected PersonService personService;
    protected ServiceRegistry serviceRegistry;
    protected NodeService nodeService;
    protected TestNetwork currentNetwork;
    
    @Before
    public void before() throws Exception
    {
        this.applicationContext = getTestFixture().getApplicationContext();
        this.repoService = getTestFixture().getRepoService();
        this.transactionHelper = (RetryingTransactionHelper)applicationContext.getBean("retryingTransactionHelper");
        this.personService = (PersonService) applicationContext.getBean("PersonService");
        this.nodeService = (NodeService) applicationContext.getBean("NodeService");
        this.serviceRegistry = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");
        
        HttpClientProvider httpClientProvider = (HttpClientProvider)applicationContext.getBean("httpClientProvider");
        
        UserDataService userDataService = new UserDataService()
        {
            @Override
            public UserData findUserByUserName(String userName)
            {
                UserData userData = new UserData();
                if(userName.startsWith("admin")) 
                {
                    userData.setUserName(userName);
                    userData.setPassword("admin");
                    userData.setId(userName);
                }
                else
                {
                    TestPerson person = getRepoService().getPerson(userName.toLowerCase());
                    userData.setUserName(person.getId());
                    userData.setPassword(person.getPassword());
                    userData.setId(person.getId());
                }
                return userData;
            }
        };
        AuthenticationDetailsProvider authenticationDetailsProvider = new UserAuthenticationDetailsProviderImpl(userDataService, "admin", "admin");
        AuthenticatedHttp authenticatedHttp = new AuthenticatedHttp(httpClientProvider, authenticationDetailsProvider);
        this.httpClient = new WorkflowApiHttpClient("localhost", TestFixture.PORT, TestFixture.CONTEXT_PATH,
                TestFixture.PUBLIC_API_SERVLET_NAME, authenticatedHttp);
        this.publicApiClient = new WorkflowApiClient(httpClient, userDataService);
        activitiProcessEngine = (ProcessEngine) applicationContext.getBean("activitiProcessEngine");
    }
    
    protected String deployProcessDefinition(String... artifacts) {
        DeploymentBuilder deploymentBuilder = activitiProcessEngine.getRepositoryService().createDeployment();
        boolean firstArtifact = true;
        for (String artifact : artifacts)
        {
            InputStream bpmnInputStream = getClass().getClassLoader().getResourceAsStream(artifact);
            String name = artifact.substring(artifact.lastIndexOf("/") + 1);
            if (firstArtifact)
            {
                deploymentBuilder.name(name);
            }
            deploymentBuilder.addInputStream(name, bpmnInputStream);
        }
        String deploymentId = deploymentBuilder.deploy().getId();
        return deploymentId;
    }
    
    protected RequestContext initApiClientWithTestUser() throws Exception {
        currentNetwork = getTestFixture().getRandomNetwork();
        final String personId = currentNetwork.getPeople().iterator().next().getId();
        RequestContext requestContext = new RequestContext(currentNetwork.getId(), personId);
        publicApiClient.setRequestContext(requestContext);
        return requestContext;
    }
    
    protected TestNetwork getOtherNetwork(String usedNetworkId) throws Exception {
        Iterator<TestNetwork> networkIt = getTestFixture().getNetworksIt();
        while(networkIt.hasNext()) {
            TestNetwork network = networkIt.next();
            if(!usedNetworkId.equals(network.getId())) {
                return network;
            }
        }
        fail("Need more than one network to test permissions");
        return null;
    }
    
    protected TestPerson getOtherPersonInNetwork(String usedPerson, String networkId) throws Exception {
        TestNetwork usedNetwork = getTestFixture().getNetwork(networkId);
        for(TestPerson person : usedNetwork.getPeople()) {
            if(!person.getId().equals(usedPerson)) {
                return person;
            }
        }
        fail("Network doesn't have additonal users, cannot perform test");
        return null;
    }
    
    protected Date parseDate(JSONObject entry, String fieldName) {
       String dateText = (String) entry.get(fieldName);
       if (dateText!=null) {
        try
        {
            return WorkflowApiClient.DATE_FORMAT_ISO8601.parse(dateText);
        }
        catch (Exception e)
        {
            throw new RuntimeException("couldn't parse date "+dateText+": "+e.getMessage(), e);
        }
      }
      return null;
   }
      
   protected String formatDate(Date date) {
       if(date == null)
       {
           return null;
       }
      return WorkflowApiClient.DATE_FORMAT_ISO8601.format(date);
   }
   
   protected ActivitiScriptNode getPersonNodeRef(String name)
   {
       ActivitiScriptNode authority = null;
       if (name != null)
       {
           if (personService.personExists(name))
           {
               authority = new ActivitiScriptNode(personService.getPerson(name), serviceRegistry);
           }
       }
       return authority;
   }
   
   protected void assertErrorSummary(String expectedBriefSummary, HttpResponse response) 
   {
       JSONObject error = (JSONObject) response.getJsonResponse().get("error");
       assertNotNull(error);
       
       String actualBriefSummary = (String) error.get("briefSummary");
       assertNotNull(actualBriefSummary);
       
       // Error starts with exception-number, check if actual message part matches
       assertTrue("Wrong summary of error: " + actualBriefSummary, actualBriefSummary.endsWith(expectedBriefSummary));
   }
   
   protected NodeRef[] createTestDocuments(final RequestContext requestContext) {
       NodeRef[] docNodeRefs = TenantUtil.runAsUserTenant(new TenantRunAsWork<NodeRef[]>()
       {
           @Override
           public NodeRef[] doWork() throws Exception
           {
               String siteName = "site" + GUID.generate();
               SiteInformation siteInfo = new SiteInformation(siteName, siteName, siteName, SiteVisibility.PUBLIC);
               TestSite site = currentNetwork.createSite(siteInfo);
               NodeRef nodeRefDoc1 = getTestFixture().getRepoService().createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc1", "Test Doc1 Title", "Test Doc1 Description", "Test Content");
               NodeRef nodeRefDoc2 = getTestFixture().getRepoService().createDocument(site.getContainerNodeRef("documentLibrary"), "Test Doc2", "Test Doc2 Title", "Test Doc2 Description", "Test Content");
               
               NodeRef[] result = new NodeRef[2];
               result[0] = nodeRefDoc1;
               result[1] = nodeRefDoc2;
               
               return result;
           }
       }, requestContext.getRunAsUser(), requestContext.getNetworkId());
       
       return docNodeRefs;
   }
   
   /**
    * Start an adhoc-process without business key through the public REST-API.
    */
   protected ProcessInfo startAdhocProcess(final RequestContext requestContext, NodeRef[] documentRefs) throws PublicApiException {
       return startAdhocProcess(requestContext, documentRefs, null);
   }
   
   /**
    * Start an adhoc-process with possible business key through the public REST-API.
    */
   @SuppressWarnings("unchecked")
   protected ProcessInfo startAdhocProcess(final RequestContext requestContext, NodeRef[] documentRefs, String businessKey) throws PublicApiException {
       org.activiti.engine.repository.ProcessDefinition processDefinition = activitiProcessEngine
               .getRepositoryService()
               .createProcessDefinitionQuery()
               .processDefinitionKey("@" + requestContext.getNetworkId() + "@activitiAdhoc")
               .singleResult();

       ProcessesClient processesClient = publicApiClient.processesClient();
       
       final JSONObject createProcessObject = new JSONObject();
       createProcessObject.put("processDefinitionId", processDefinition.getId());
       if (businessKey != null)
       {
           createProcessObject.put("businessKey", businessKey);
       }
       final JSONObject variablesObject = new JSONObject();
       variablesObject.put("bpm_priority", 1);
       variablesObject.put("wf_notifyMe", Boolean.FALSE);
       
       TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
       {
           @Override
           public Void doWork() throws Exception
           {
               variablesObject.put("bpm_assignee", requestContext.getRunAsUser());
               return null;
           }
       }, requestContext.getRunAsUser(), requestContext.getNetworkId());
       
       if (documentRefs != null && documentRefs.length > 0)
       {
           final JSONArray itemsObject = new JSONArray();
           for (NodeRef nodeRef : documentRefs)
           {
               itemsObject.add(nodeRef.getId());
           }
           createProcessObject.put("items", itemsObject);
       }
       
       createProcessObject.put("variables", variablesObject);
       
       return processesClient.createProcess(createProcessObject.toJSONString());
   }
   
   /**
    * Start a review pooled process through the public REST-API.
    */
   @SuppressWarnings("unchecked")
   protected ProcessInfo startReviewPooledProcess(final RequestContext requestContext) throws PublicApiException {
       org.activiti.engine.repository.ProcessDefinition processDefinition = activitiProcessEngine
               .getRepositoryService()
               .createProcessDefinitionQuery()
               .processDefinitionKey("@" + requestContext.getNetworkId() + "@activitiReviewPooled")
               .singleResult();

       ProcessesClient processesClient = publicApiClient.processesClient();
       
       final JSONObject createProcessObject = new JSONObject();
       createProcessObject.put("processDefinitionId", processDefinition.getId());
       
       final JSONObject variablesObject = new JSONObject();
       variablesObject.put("bpm_priority", 1);
       variablesObject.put("bpm_workflowDueDate", ISO8601DateFormat.format(new Date()));
       variablesObject.put("wf_notifyMe", Boolean.FALSE);
       
       TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
       {
           @Override
           public Void doWork() throws Exception
           {
               List<MemberOfSite> memberships = getTestFixture().getNetwork(requestContext.getNetworkId()).getSiteMemberships(requestContext.getRunAsUser());
               assertTrue(memberships.size() > 0);
               MemberOfSite memberOfSite = memberships.get(0);
               String group = "GROUP_site_" + memberOfSite.getSiteId() + "_" + memberOfSite.getRole().name();
               variablesObject.put("bpm_groupAssignee", group);
               return null;
           }
       }, requestContext.getRunAsUser(), requestContext.getNetworkId());
       
       createProcessObject.put("variables", variablesObject);
       
       return processesClient.createProcess(createProcessObject.toJSONString());
   }
   
   /**
    * Start a review pooled process through the public REST-API.
    */
   @SuppressWarnings("unchecked")
   protected ProcessInfo startParallelReviewProcess(final RequestContext requestContext) throws PublicApiException {
       org.activiti.engine.repository.ProcessDefinition processDefinition = activitiProcessEngine
               .getRepositoryService()
               .createProcessDefinitionQuery()
               .processDefinitionKey("@" + requestContext.getNetworkId() + "@activitiParallelReview")
               .singleResult();

       ProcessesClient processesClient = publicApiClient.processesClient();
       
       final JSONObject createProcessObject = new JSONObject();
       createProcessObject.put("processDefinitionId", processDefinition.getId());
       
       final JSONObject variablesObject = new JSONObject();
       variablesObject.put("bpm_priority", 1);
       variablesObject.put("wf_notifyMe", Boolean.FALSE);
       
       TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
       {
           @Override
           public Void doWork() throws Exception
           {
               JSONArray assigneeArray = new JSONArray();
               assigneeArray.add(requestContext.getRunAsUser());
               TestPerson otherPerson = getOtherPersonInNetwork(requestContext.getRunAsUser(), requestContext.getNetworkId());
               assigneeArray.add(otherPerson.getId());
               variablesObject.put("bpm_assignees", assigneeArray);
               return null;
           }
       }, requestContext.getRunAsUser(), requestContext.getNetworkId());
       
       createProcessObject.put("variables", variablesObject);
       
       return processesClient.createProcess(createProcessObject.toJSONString());
   }
}
