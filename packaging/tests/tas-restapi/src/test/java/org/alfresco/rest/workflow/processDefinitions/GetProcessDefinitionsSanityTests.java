package org.alfresco.rest.workflow.processDefinitions;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestProcessDefinitionModelsCollection;
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
public class GetProcessDefinitionsSanityTests extends RestTest
{
    private UserModel adminUserModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
        restClient.authenticateUser(adminUserModel);
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION },
            executionType = ExecutionType.SANITY, description = "Verify Admin user gets process definitions for non-network deployments using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.SANITY })
    public void nonNetworkAdminGetsProcessDefinitions() throws Exception
    {
        RestProcessDefinitionModelsCollection processDefinitions = restClient.authenticateUser(adminUserModel)
                .withWorkflowAPI()
                .getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processDefinitions.assertThat().entriesListIsNotEmpty();
        processDefinitions.getProcessDefinitionByDeploymentId("1").assertThat()
                .field("name").is("Adhoc Activiti Process").and()
                .field("description").is("Assign a new task to yourself or a colleague").and()
                .field("id").is("activitiAdhoc:1:4").and()
                .field("startFormResourceKey").is("wf:submitAdhocTask").and()
                .field("category").is("http://alfresco.org").and()
                .field("title").is("New Task").and()
                .field("version").is("1").and()
                .field("graphicNotationDefined").is("true").and()
                .field("key").is("activitiAdhoc");
    }
}
