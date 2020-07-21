package org.alfresco.rest.workflow.processDefinitions;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 2/1/2017.
 */
public class GetProcessDefinitionImageFullTests extends RestTest
{
    private UserModel adminUser;
    private RestProcessDefinitionModel randomProcessDefinition;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
    }

    @Bug(id = "REPO-1911")
    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION },
            executionType = ExecutionType.REGRESSION,
            description = "Verify if get process definition image returns status code 404 when empty processDefinitionId is used")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionImageUsingEmptyProcessDefinitionId() throws Exception
    {
        restClient.authenticateUser(adminUser);
        randomProcessDefinition = restClient.withWorkflowAPI().getAllProcessDefinitions().getOneRandomEntry().onModel();
        randomProcessDefinition.setId("");
        restClient.withWorkflowAPI()
                .usingProcessDefinitions(randomProcessDefinition).getProcessDefinitionImage();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }
}
