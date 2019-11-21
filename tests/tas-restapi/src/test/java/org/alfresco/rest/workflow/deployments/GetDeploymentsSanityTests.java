package org.alfresco.rest.workflow.deployments;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestDeploymentModelsCollection;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/4/2016.
 */
public class GetDeploymentsSanityTests extends RestTest
{
    private UserModel adminUserModel;
    private RestDeploymentModelsCollection deployments;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        restClient.authenticateUser(adminUserModel);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS }, executionType = ExecutionType.SANITY, 
        description = "Verify Admin user gets non-network deployments using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS, TestGroup.SANITY})
    public void getNonNetworkDeploymentsWithAdmin() throws JsonToModelConversionException, Exception
    {
        deployments = restClient.authenticateUser(adminUserModel).withWorkflowAPI().getDeployments();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        deployments.assertThat().entriesListIsNotEmpty();
        deployments.getOneRandomEntry().onModel().assertThat()
                .fieldsCount().is(3).and()
                .field("id").isNotEmpty().and()
                .field("deployedAt").isNotEmpty().and()
                .field("name").isNotEmpty();
    }

}
