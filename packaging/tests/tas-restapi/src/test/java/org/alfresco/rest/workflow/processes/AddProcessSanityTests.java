package org.alfresco.rest.workflow.processes;

import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessModelsCollection;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 10/18/2016.
 */
public class AddProcessSanityTests extends RestTest
{
    private UserModel userWhoStartsProcess, assignee;
    private RestProcessModel addedProcess;
    private RestProcessModelsCollection processes;

    @TestRail(section = { TestGroup.REST_API,TestGroup.WORKFLOW,
            TestGroup.PROCESSES }, executionType = ExecutionType.SANITY, 
            description = "Verify non network user is able to start new process using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void nonNetworkUserStartsNewProcess() throws JsonToModelConversionException, Exception
    {
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();

        addedProcess = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        addedProcess.assertThat().field("id").is(addedProcess.getId())
                    .and().field("startUserId").is(addedProcess.getStartUserId());

        processes = restClient.withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processes.assertThat().entriesListContains("id", addedProcess.getId());
        
    }

}
