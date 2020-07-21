package org.alfresco.rest.workflow.processes;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestProcessDefinitionModel;
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
public class GetProcessFullTests extends RestTest
{
    private UserModel userWhoStartsProcess, assignee;
    private RestProcessModel addedProcess, process;
    private RestProcessDefinitionModel adhocProcessDefinition;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        addedProcess = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.High);
        adhocProcessDefinition = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getAllProcessDefinitions().getProcessDefinitionByKey("activitiAdhoc");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is able to get the process with properties parameter applied using REST API")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessWithPropertiesParameter() throws Exception
    {
        process = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingParams("properties=startUserId,id").usingProcess(addedProcess).getProcess();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        process.assertThat().fieldsCount().is(2)
            .and().field("startUserId").is(addedProcess.getStartUserId())
            .and().field("id").is(addedProcess.getId())
            .and().field("processDefinitionKey").isNull();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user is able to get a process that was deleted, but it has 'deleted through REST API call' deleteReason")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getDeletedProcess() throws Exception
    {
        addedProcess = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.High);

        process = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingProcess(addedProcess).getProcess();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        process.assertThat()
            .field("processDefinitionId").is(adhocProcessDefinition.getId())
            .and().field("startUserId").is(addedProcess.getStartUserId())
            .and().field("startActivityId").is("start")
            .and().field("startedAt").isNotEmpty()
            .and().field("id").is(addedProcess.getId())
            .and().field("completed").is(false)
            .and().field("processDefinitionKey").is("activitiAdhoc");
        
        restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingProcess(addedProcess).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        process = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingProcess(addedProcess).getProcess();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        process.assertThat()
            .field("processDefinitionId").is(adhocProcessDefinition.getId())
            .and().field("durationInMs").isNotNull()
            .and().field("startUserId").is(addedProcess.getStartUserId())
            .and().field("startActivityId").is("start")
            .and().field("endedAt").isNotEmpty()
            .and().field("startedAt").isNotEmpty()
            .and().field("id").is(addedProcess.getId())
            .and().field("completed").is(true)
            .and().field("deleteReason").is("deleted through REST API call")
            .and().field("processDefinitionKey").is("activitiAdhoc");

        restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().assertThat().entriesListDoesNotContain("id", process.getId());
    }
}
