package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableCollection;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 2/2/2017.
 */
public class GetProcessVariablesFullTests extends RestTest
{
    private UserModel userWhoStartsProcess, assignee, admin;
    private RestProcessModel processModel;
    private RestProcessVariableCollection variables;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        admin = dataUser.getAdminUser();
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process variables call using admin user. Admin can see the process even if he isn't involved in it.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesUsingAdmin() throws Exception
    {
        variables = restClient.authenticateUser(admin).withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variables.assertThat().entriesListIsNotEmpty();
        variables.getProcessVariableByName("initiator")
                .assertThat().field("name").is("initiator")
                .assertThat().field("type").is("d:noderef")
                .assertThat().field("value").is(userWhoStartsProcess.getUsername());
        variables.getProcessVariableByName("bpm_assignee")
                .assertThat().field("name").is("bpm_assignee")
                .assertThat().field("type").is("cm:person")
                .assertThat().field("value").is(assignee.getUsername());
        variables.getProcessVariableByName("bpm_sendEMailNotifications")
                .assertThat().field("name").is("bpm_sendEMailNotifications")
                .assertThat().field("type").is("d:boolean")
                .assertThat().field("value").is(false);
        variables.getProcessVariableByName("bpm_priority")
                .assertThat().field("name").is("bpm_priority")
                .assertThat().field("type").is("d:int")
                .assertThat().field("value").is(CMISUtil.Priority.Normal.getLevel());
    }

    @Bug(id = "MNT-17438")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process variables with valid skip count parameter applied.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesWithValidSkipCount() throws Exception
    {
        variables = restClient.authenticateUser(admin).withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variables.assertThat().entriesListIsNotEmpty();

        RestProcessVariableCollection variablesSkipped = restClient.authenticateUser(admin).withParams("skipCount=2").withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variablesSkipped
                .assertThat().entriesListDoesNotContain("name", variables.getEntries().get(0).onModel().getName())
                .assertThat().entriesListDoesNotContain("name", variables.getEntries().get(1).onModel().getName())
                .assertThat().entriesListCountIs(variables.getEntries().size()-2)
                .assertThat().paginationField("skipCount").is("2");
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process variables with negative skip count parameter applied.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesWithNegativeSkipCount() throws Exception
    {
        restClient.authenticateUser(admin).withParams("skipCount=-2").withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .containsErrorKey(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process variables with not numeric skip count parameter applied.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesWithNonNumericSkipCount() throws Exception
    {
        restClient.authenticateUser(admin).withParams("skipCount=A").withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "A"));
    }

    @Bug(id = "MNT-17438")
    @TestRail(section = { TestGroup.REST_API,TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process variables with valid maxItems parameter applied.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesWithValidMaxItems() throws Exception
    {
        variables = restClient.authenticateUser(admin).withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variables.assertThat().entriesListIsNotEmpty();

        RestProcessVariableCollection variablesSkipped = restClient.authenticateUser(admin).withParams("maxItems=2").withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variablesSkipped
                .assertThat().entriesListContains("name", variables.getEntries().get(0).onModel().getName())
                .assertThat().entriesListContains("name", variables.getEntries().get(1).onModel().getName())
                .assertThat().entriesListCountIs(2)
                .assertThat().paginationField("maxItems").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process variables with negative maxItems parameter applied.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesWithNegativeMaxItems() throws Exception
    {
        restClient.authenticateUser(admin).withParams("maxItems=-2").withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API,TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process variables with not numeric maxItems parameter applied.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesWithNonNumericMaxItems() throws Exception
    {
        restClient.authenticateUser(admin).withParams("maxItems=A").withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "A"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process variables with properties parameter.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesWithPropertiesParameter() throws Exception
    {
        variables = restClient.authenticateUser(admin).withParams("properties=name").withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variables.assertThat().entriesListIsNotEmpty();
        variables.getProcessVariableByName("initiator")
                .assertThat().field("name").is("initiator")
                .assertThat().field("type").isNull()
                .assertThat().field("value").isNull()
                .assertThat().fieldsCount().is(1);
    }
}
