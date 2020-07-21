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

/**
 * 
 * @author Cristina Axinte
 *
 */
public class DeleteProcessFullTests extends RestTest
{
    private UserModel userWhoAddsProcess, assignee;
    private RestProcessModel process;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoAddsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user is NOT able to delete process started by him twice using REST API and status code is NOT FOUND (404)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessByUserWhoStartedProcessTwice() throws Exception
    {
        process = restClient.authenticateUser(userWhoAddsProcess).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withWorkflowAPI().usingProcess(process).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restClient.withWorkflowAPI().usingProcess(process).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
            .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
            .containsSummary(String.format(RestErrorModel.ENTITY_WAS_NOT_FOUND, process.getId()))
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
            .stackTraceIs(RestErrorModel.STACKTRACE);

        restClient.withWorkflowAPI().getProcesses().assertThat().entriesListDoesNotContain("id", process.getId());
    }
}
