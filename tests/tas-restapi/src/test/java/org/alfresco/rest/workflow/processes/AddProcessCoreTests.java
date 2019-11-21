package org.alfresco.rest.workflow.processes;

import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.rest.model.RestProcessDefinitionModelsCollection;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessModelsCollection;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * 
 * @author Cristina Axinte
 *
 */
public class AddProcessCoreTests extends RestTest
{
    private UserModel assignee, adminUser;
    private RestProcessModel addedProcess;
    private RestProcessModelsCollection processes;
    private RestProcessDefinitionModel processDefinition;

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, 
            executionType = ExecutionType.REGRESSION, description = "Verify non network admin is able to start new process using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void nonNetworkAdminUserStartsNewProcess() throws JsonToModelConversionException, Exception
    {
        adminUser = dataUser.getAdminUser();
        assignee = dataUser.createRandomTestUser();

        addedProcess = restClient.authenticateUser(adminUser).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        addedProcess.assertThat().field("id").is(addedProcess.getId())
                    .and().field("startUserId").is(adminUser.getUsername());

        processDefinition = restClient.authenticateUser(adminUser).withWorkflowAPI().getAllProcessDefinitions().getProcessDefinitionByKey("activitiAdhoc");
        processes = restClient.withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processes.assertThat().entriesListContains("id", addedProcess.getId())
            .assertThat().entriesListContains("processDefinitionId", processDefinition.getId())
            .assertThat().entriesListContains("startUserId", adminUser.getUsername())
            .assertThat().entriesListContains("startActivityId", "start")
            .assertThat().entriesListContains("completed", "false")
            .assertThat().entriesListContains("processDefinitionKey", "activitiAdhoc");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, 
            executionType = ExecutionType.REGRESSION, description = "Verify start new process with empty request body using REST API returns status code is Bad Request (400)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void startNewProcessWithEmptyProcessBody() throws JsonToModelConversionException, Exception
    {
        adminUser = dataUser.getAdminUser();
        assignee = dataUser.createRandomTestUser();

        restClient.authenticateUser(adminUser).withWorkflowAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "", "processes");
        restClient.processModel(RestProcessModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @Bug(id = "REPO-1936")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, 
            executionType = ExecutionType.REGRESSION, description = "Verify start new process with invalid request body using REST API returns status code is Bad Request (400)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void startNewProcessWithInvalidProcessDefInProcessBody() throws JsonToModelConversionException, Exception
    {
        adminUser = dataUser.getAdminUser();
        assignee = dataUser.createRandomTestUser();

        restClient.authenticateUser(adminUser).withWorkflowAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"processDefinitionKey\":\"activitiAdhoc\"}", "processes");
        restClient.processModel(RestProcessModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, 
            executionType = ExecutionType.REGRESSION, description = "Verify start new process with invalid request body using REST API returns status code is Bad Request (400)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void startNewProcessWithInvalidVariablesInProcessBody1() throws JsonToModelConversionException, Exception
    {
        adminUser = dataUser.getAdminUser();
        assignee = dataUser.createRandomTestUser();

        restClient.authenticateUser(adminUser).withWorkflowAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"variables\":{\"bpm_sendEMailNotifications\":false}}", "processes");
        restClient.processModel(RestProcessModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsSummary("Either processDefinitionId or processDefinitionKey is required");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, 
            executionType = ExecutionType.REGRESSION, description = "Verify start new process with invalid request body using REST API returns status code is Bad Request (400)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void startNewProcessWithInvalidVariablesInProcessBody2() throws JsonToModelConversionException, Exception
    {
        adminUser = dataUser.getAdminUser();
        assignee = dataUser.createRandomTestUser();

        restClient.authenticateUser(adminUser).withWorkflowAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"variables\":{\"bpm_assignee\":\"admin\"}}", "processes");
        restClient.processModel(RestProcessModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsSummary("Either processDefinitionId or processDefinitionKey is required");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, 
            executionType = ExecutionType.REGRESSION, description = "Verify start new process with invalid request body using REST API returns status code is Bad Request (400)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void startNewProcessWithInvalidVariablesInProcessBody3() throws JsonToModelConversionException, Exception
    {
        adminUser = dataUser.getAdminUser();
        assignee = dataUser.createRandomTestUser();

        restClient.authenticateUser(adminUser).withWorkflowAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"variables\":{\"bpm_workflowPriority\":2}}", "processes");
        restClient.processModel(RestProcessModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsSummary("Either processDefinitionId or processDefinitionKey is required");
    }
}
