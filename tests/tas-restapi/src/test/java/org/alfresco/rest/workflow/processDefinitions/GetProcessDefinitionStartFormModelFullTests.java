package org.alfresco.rest.workflow.processDefinitions;

import java.util.Arrays;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestFormModelsCollection;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 1/31/2017.
 */
public class GetProcessDefinitionStartFormModelFullTests extends RestTest
{
    private UserModel adminUser;
    private RestProcessDefinitionModel activitiAdhoc;
    private RestFormModelsCollection returnedResponse;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        activitiAdhoc = restClient.authenticateUser(adminUser).withWorkflowAPI().getAllProcessDefinitions().getProcessDefinitionByDeploymentId("1");
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION },
            executionType = ExecutionType.REGRESSION,
            description = "Verify admin gets a model of the start form type definition for specific process definition using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void adminGetsStartFormModelForActivitiAdhocProcessDefinition() throws Exception
    {
        returnedResponse = restClient.authenticateUser(adminUser).withWorkflowAPI()
                .usingProcessDefinitions(activitiAdhoc).getProcessDefinitionStartFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedResponse.assertThat().entriesListContains("qualifiedName", "{http://www.alfresco.org/model/bpm/1.0}workflowPriority");
        returnedResponse.getStartFormModelByQualifiedName("{http://www.alfresco.org/model/bpm/1.0}workflowPriority")
                .assertThat().field("defaultValue").is("2")
                .and().field("dataType").is("d:int")
                .and().field("name").is("bpm_workflowPriority")
                .and().field("title").is("Workflow Priority")
                .and().field("required").is("false")
                .and().field("allowedValues").is(Arrays.asList("1", "2", "3"));
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION },
            executionType = ExecutionType.REGRESSION,
            description = "Verify admin gets a model of the start form type definition with properties parameter applied using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void adminGetsStartFormModelWithPropertiesParameterApplied() throws Exception
    {
        returnedResponse = restClient.authenticateUser(adminUser).withParams("properties=qualifiedName,dataType,title").withWorkflowAPI()
                .usingProcessDefinitions(activitiAdhoc).getProcessDefinitionStartFormModel();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        returnedResponse.assertThat().entriesListContains("qualifiedName", "{http://www.alfresco.org/model/bpm/1.0}workflowPriority");
        returnedResponse.getStartFormModelByQualifiedName("{http://www.alfresco.org/model/bpm/1.0}workflowPriority")
                .assertThat().fieldsCount().is(3)
                .and().field("qualifiedName").is("{http://www.alfresco.org/model/bpm/1.0}workflowPriority")
                .and().field("defaultValue").isNull()
                .and().field("dataType").is("d:int")
                .and().field("name").isNull()
                .and().field("title").is("Workflow Priority")
                .and().field("required").isNull()
                .and().field("allowedValues").isNull();
    }
}
