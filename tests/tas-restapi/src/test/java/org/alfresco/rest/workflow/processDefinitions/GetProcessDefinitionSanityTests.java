package org.alfresco.rest.workflow.processDefinitions;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/13/2016.
 */
public class GetProcessDefinitionSanityTests extends RestTest
{
    private UserModel testUser;
    private RestProcessDefinitionModel randomProcessDefinition, firstRandomProcessDefinition, returnedProcessDefinition;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        testUser = dataUser.createRandomTestUser();
        restClient.authenticateUser(dataUser.getAdminUser());
        randomProcessDefinition = restClient.withWorkflowAPI().getAllProcessDefinitions().getOneRandomEntry().onModel();
        firstRandomProcessDefinition = restClient.withWorkflowAPI().getAllProcessDefinitions().getProcessDefinitionByDeploymentId("1");
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION },
            executionType = ExecutionType.SANITY,
            description = "Verify Admin user gets a specific process definition for non-network deployments using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.SANITY })
    public void adminGetsProcessDefinition() throws Exception
    {
        returnedProcessDefinition = restClient.withWorkflowAPI().usingProcessDefinitions(randomProcessDefinition).getProcessDefinition();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedProcessDefinition.assertThat().field("name").is(randomProcessDefinition.getName());
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION },
            executionType = ExecutionType.SANITY,
            description = "Verify Any user gets a specific process definition for non-network deployments using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.SANITY })
    public void anyUserGetsProcessDefinition() throws Exception
    {
        restClient.authenticateUser(testUser);
        returnedProcessDefinition = restClient.withWorkflowAPI().usingProcessDefinitions(firstRandomProcessDefinition).getProcessDefinition();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedProcessDefinition.assertThat()        
                .field("name").is(firstRandomProcessDefinition.getName()).and()
                .field("deploymentId").is(firstRandomProcessDefinition.getDeploymentId()).and()
                .field("description").is(firstRandomProcessDefinition.getDescription()).and()
                .field("id").is(firstRandomProcessDefinition.getId()).and()
                .field("startFormResourceKey").is(firstRandomProcessDefinition.getStartFormResourceKey()).and()
                .field("category").is(firstRandomProcessDefinition.getCategory()).and()
                .field("title").is(firstRandomProcessDefinition.getTitle()).and()
                .field("version").is(firstRandomProcessDefinition.getVersion()).and()
                .field("graphicNotationDefined").is(firstRandomProcessDefinition.getGraphicNotationDefined()).and()
                .field("key").is(firstRandomProcessDefinition.getKey());
    }
}
