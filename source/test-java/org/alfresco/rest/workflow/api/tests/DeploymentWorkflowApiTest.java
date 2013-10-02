/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.tests;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.tests.AbstractTestFixture;
import org.alfresco.rest.api.tests.RepoService.TestNetwork;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.workflow.api.model.Deployment;
import org.alfresco.rest.workflow.api.tests.WorkflowApiClient.DeploymentsClient;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.springframework.http.HttpStatus;

/**
 * Rest api tests using http client to communicate with the rest apis in the repository.
 * 
 * Note: currently certain tests work only with lucene search subsystem e.g. tags.
 * 
 * @author steveglover
 *
 */
public class DeploymentWorkflowApiTest extends EnterpriseWorkflowTestApi
{   
    protected static HashSet<String> alfrescoPublicDeploymentNames = new HashSet<String>(Arrays.asList(new String[]{
            "review-pooled.bpmn20.xml",
            "review.bpmn20.xml",
            "parallel-review-group.bpmn20.xml",
            "parallel-review.bpmn20.xml",
            "adhoc.bpmn20.xml"}));
    
    protected static HashSet<String> alfrescoPublicProcessDefinitionKeys = new HashSet<String>(Arrays.asList(new String[]{
            "activitiReviewPooled",
            "activitiReview",
            "activitiParallelGroupReview",
            "activitiParallelReview",
            "activitiAdhoc"}));
    
    @Test
    public void testGetDeploymentsWithNonAdminUser() throws Exception
    {
        // deployments-get#1 
        initApiClientWithTestUser();
        DeploymentsClient deploymentsClient = publicApiClient.deploymentsClient();
        
        try {
            deploymentsClient.getDeployments();
            fail("Exception expected");
        } catch(PublicApiException expected) {
            assertEquals(HttpStatus.FORBIDDEN.value(), expected.getHttpResponse().getStatusCode());
            assertErrorSummary("Permission was denied", expected.getHttpResponse());
        }
    }
    
    
    @Test
    public void testGetDeployments() throws Exception
    {
        // testGetDeployments#1: Getting deployments with admin-user
        RequestContext requestContext = initApiClientWithTestUser();
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        publicApiClient.setRequestContext(new RequestContext(TenantUtil.DEFAULT_TENANT, tenantAdmin));

        DeploymentsClient deploymentsClient = publicApiClient.deploymentsClient();

        ListResponse<Deployment> deploymentResponse = deploymentsClient.getDeployments();
        Map<String, Deployment> deploymentMap = new HashMap<String, Deployment>();
        for (Deployment deployment : deploymentResponse.getList())
        {
            deploymentMap.put(deployment.getName(), deployment);
        }
        assertEquals(5, deploymentResponse.getList().size());
        
        assertTrue(deploymentMap.containsKey("review-pooled.bpmn20.xml"));
        assertTrue(deploymentMap.containsKey("review.bpmn20.xml"));
        assertTrue(deploymentMap.containsKey("parallel-review-group.bpmn20.xml"));
        assertTrue(deploymentMap.containsKey("parallel-review.bpmn20.xml"));
        assertTrue(deploymentMap.containsKey("adhoc.bpmn20.xml"));
        
        // testGetDeployments#2: Check all deployment fields in resulting deployment
        org.activiti.engine.repository.Deployment activitiDeployment = activitiProcessEngine.getRepositoryService()
            .createDeploymentQuery()
            .deploymentName("adhoc.bpmn20.xml")
            .processDefinitionKey("@" + requestContext.getNetworkId() + "@activitiAdhoc")
            .singleResult();
        
        assertNotNull(activitiDeployment);
        Deployment adhocDeployment = deploymentMap.get("adhoc.bpmn20.xml");
        
        assertEquals(activitiDeployment.getId(), adhocDeployment.getId());
        assertEquals(activitiDeployment.getCategory(), adhocDeployment.getCategory());
        assertEquals(activitiDeployment.getName(), adhocDeployment.getName());
        assertEquals(activitiDeployment.getDeploymentTime(), adhocDeployment.getDeployedAt());
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("maxItems", "2");
        JSONObject deploymentsListObject = deploymentsClient.getDeploymentsWithRawResponse(params);
        assertNotNull(deploymentsListObject);
        JSONObject paginationJSON = (JSONObject) deploymentsListObject.get("pagination");
        assertEquals(2l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(0l, paginationJSON.get("skipCount"));
        assertEquals(true, paginationJSON.get("hasMoreItems"));
        
        params = new HashMap<String, String>();
        deploymentsListObject = deploymentsClient.getDeploymentsWithRawResponse(params);
        assertNotNull(deploymentsListObject);
        paginationJSON = (JSONObject) deploymentsListObject.get("pagination");
        assertEquals(5l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(0l, paginationJSON.get("skipCount"));
        assertEquals(false, paginationJSON.get("hasMoreItems"));
        
        params = new HashMap<String, String>();
        params.put("skipCount", "2");
        params.put("maxItems", "2");
        deploymentsListObject = deploymentsClient.getDeploymentsWithRawResponse(params);
        assertNotNull(deploymentsListObject);
        paginationJSON = (JSONObject) deploymentsListObject.get("pagination");
        assertEquals(2l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(2l, paginationJSON.get("skipCount"));
        assertEquals(true, paginationJSON.get("hasMoreItems"));
        
        params = new HashMap<String, String>();
        params.put("skipCount", "2");
        params.put("maxItems", "5");
        deploymentsListObject = deploymentsClient.getDeploymentsWithRawResponse(params);
        assertNotNull(deploymentsListObject);
        paginationJSON = (JSONObject) deploymentsListObject.get("pagination");
        assertEquals(3l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(2l, paginationJSON.get("skipCount"));
        assertEquals(true, paginationJSON.get("hasMoreItems"));
        
        params = new HashMap<String, String>();
        params.put("skipCount", "0");
        params.put("maxItems", "7");
        deploymentsListObject = deploymentsClient.getDeploymentsWithRawResponse(params);
        assertNotNull(deploymentsListObject);
        paginationJSON = (JSONObject) deploymentsListObject.get("pagination");
        assertEquals(5l, paginationJSON.get("count"));
        assertEquals(5l, paginationJSON.get("totalItems"));
        assertEquals(0l, paginationJSON.get("skipCount"));
        assertEquals(false, paginationJSON.get("hasMoreItems"));
    }
    
    @Test
    public void testGetDeploymentsEmpty() throws Exception
    {
        // Create a new test-network, not added to the test-fixture to prevent being used
        // in other tests
        String networkName = AbstractTestFixture.TEST_DOMAIN_PREFIX + "999";
        final TestNetwork testNetwork = repoService.createNetworkWithAlias(networkName, true);
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                testNetwork.create();

                return null;
            }
        }, false, true);
        
        // Delete all deployments in the network
        List<org.activiti.engine.repository.Deployment> deployments = activitiProcessEngine.getRepositoryService()
            .createDeploymentQuery()
            .processDefinitionKeyLike("@" + testNetwork.getId() + "@%")
            .list();
        
        for(org.activiti.engine.repository.Deployment deployment : deployments) 
        {
            activitiProcessEngine.getRepositoryService().deleteDeployment(deployment.getId(), true);
        }
        
        // Fetch deployments using tenant-admin
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + testNetwork.getId();
        publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), tenantAdmin));

        DeploymentsClient deploymentsClient = publicApiClient.deploymentsClient();

        ListResponse<Deployment> deploymentResponse = deploymentsClient.getDeployments();
        assertEquals(0, deploymentResponse.getList().size());
    }
    
    @Test
    public void testGetDeploymentById() throws Exception
    {
        // Use admin-user for tenant
        RequestContext requestContext = initApiClientWithTestUser();
        
        String tenantAdmin = AuthenticationUtil.getAdminUserName() + "@" + requestContext.getNetworkId();
        publicApiClient.setRequestContext(new RequestContext(TenantUtil.DEFAULT_TENANT, tenantAdmin));
        
        // Fetch the actual deployment from activiti
        org.activiti.engine.repository.Deployment activitiDeployment = activitiProcessEngine.getRepositoryService()
            .createDeploymentQuery()
            .deploymentName("adhoc.bpmn20.xml")
            .processDefinitionKey("@" + requestContext.getNetworkId() + "@activitiAdhoc")
            .singleResult();
        
        assertNotNull(activitiDeployment);

        // Do the actual API-call
        DeploymentsClient deploymentsClient = publicApiClient.deploymentsClient();
        Deployment deployment = deploymentsClient.findDeploymentById(activitiDeployment.getId());
        
        assertNotNull(deployment);
        
        assertEquals(activitiDeployment.getId(), deployment.getId());
        assertEquals(activitiDeployment.getCategory(), deployment.getCategory());
        assertEquals(activitiDeployment.getName(), deployment.getName());
        assertEquals(activitiDeployment.getDeploymentTime(), deployment.getDeployedAt());
        
        try
        {
            deploymentsClient.findDeploymentById("fakeid");
            fail("Expected exception");
        }
        catch (PublicApiException e)
        {
            assertEquals(404, e.getHttpResponse().getStatusCode());
        }
        
        // get deployment with default user
        try
        {
            publicApiClient.setRequestContext(requestContext);
            deploymentsClient.findDeploymentById(activitiDeployment.getId());
            fail("Expected exception");
        }
        catch (PublicApiException e)
        {
            assertEquals(403, e.getHttpResponse().getStatusCode());
        }
    }

    protected String createProcessDefinitionKey(String key, RequestContext requestContext) {
        return "@" + requestContext.getNetworkId() + "@" + key;
    }
}
