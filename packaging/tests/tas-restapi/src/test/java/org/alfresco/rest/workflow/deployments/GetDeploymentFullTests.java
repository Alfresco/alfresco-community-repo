package org.alfresco.rest.workflow.deployments;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestDeploymentModel;
import org.alfresco.rest.model.RestDeploymentModelsCollection;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 1/31/2017.
 */
public class GetDeploymentFullTests extends RestTest
{
    private UserModel adminUser;
    private RestDeploymentModel expectedDeployment, actualDeployment;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS },
            executionType = ExecutionType.REGRESSION,
            description = "Verify if get deployment request returns all deployments if empty deploymentId is used.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS, TestGroup.REGRESSION })
    public void getNonNetworkDeploymentUsingEmptyDeploymentId() throws Exception
    {
        restClient.authenticateUser(adminUser).withWorkflowAPI();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "deployments/{deploymentId}", "");
        RestDeploymentModelsCollection deployments = restClient.processModels(RestDeploymentModelsCollection.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        deployments.assertThat().entriesListIsNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets non-network deployments with properties parameter applied using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS, TestGroup.REGRESSION})
    public void getNonNetworkDeploymentsWithValidProperties() throws Exception
    {
        expectedDeployment = restClient.authenticateUser(adminUser).withWorkflowAPI().getDeployments().getOneRandomEntry().onModel();
        actualDeployment = restClient.withParams("properties=id,name").withWorkflowAPI().usingDeployment(expectedDeployment).getDeployment();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        actualDeployment.assertThat()
                .fieldsCount().is(2).and()
                .field("id").is(expectedDeployment.getId()).and()
                .field("deployedAt").isNull().and()
                .field("name").is(expectedDeployment.getName());
    }
}
