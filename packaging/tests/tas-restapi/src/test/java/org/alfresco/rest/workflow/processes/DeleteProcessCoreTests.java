package org.alfresco.rest.workflow.processes;

import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DeleteProcessCoreTests extends RestTest
{
    private UserModel userWhoAddsProcess, assignee;
    private RestProcessModel process;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoAddsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.WORKFLOW,
            TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify admin user is able to delete a process started by another user.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessByAdminUser() throws Exception
    {
        process = restClient.authenticateUser(userWhoAddsProcess).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.authenticateUser(dataUser.getAdminUser())
            .withWorkflowAPI().usingProcess(process).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().getProcesses()
            .assertThat().entriesListDoesNotContain("id", process.getId());
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.WORKFLOW,
            TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify User is not able to delete process with invalid id")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessWithInvalidId() throws Exception
    {
        process = restClient.authenticateUser(userWhoAddsProcess).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        process.setId("00001");
        restClient.withWorkflowAPI().usingProcess(process).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "00001"));
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.WORKFLOW,
            TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify User is not able to delete process with empty id")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessWithEmptyId() throws Exception
    {
        process = restClient.authenticateUser(userWhoAddsProcess).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        process.setId("");
        restClient.withWorkflowAPI().usingProcess(process).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
            .assertLastError().containsSummary(RestErrorModel.DELETE_EMPTY_ARGUMENT);
    }
}
