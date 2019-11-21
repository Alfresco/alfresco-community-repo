package org.alfresco.rest.workflow.processes.tasks;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTaskModelsCollection;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 2/3/2017.
 */
public class GetProcessTasksFullTests extends RestTest
{
    private FileModel document;
    private SiteModel publicSite;
    private UserModel adminUser, userWhoStartsProcess, assignee1, assignee2, assignee3;
    private ProcessModel processWithSingleTask, processWithMultipleTasks;
    private RestTaskModelsCollection processTasks;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee1 = dataUser.createRandomTestUser("A_1");
        assignee2 = dataUser.createRandomTestUser("B_2");
        assignee3 = dataUser.createRandomTestUser("C_3");
        publicSite = dataSite.usingUser(userWhoStartsProcess).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsProcess).usingSite(publicSite).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        processWithSingleTask = dataWorkflow.usingUser(userWhoStartsProcess).usingSite(publicSite).usingResource(document)
                .createSingleReviewerTaskAndAssignTo(assignee1);
        processWithMultipleTasks = dataWorkflow.usingUser(userWhoStartsProcess).usingSite(publicSite).usingResource(document)
                .createMoreReviewersWorkflowAndAssignTo(assignee1, assignee2, assignee3);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin user is able to getProcessTasks even if he wasn't involved in process. Check status code is OK")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessTasksWithAdmin() throws Exception
    {
        processTasks = restClient.authenticateUser(adminUser).withWorkflowAPI()
                .usingProcess(processWithSingleTask).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processTasks.assertThat().entriesListCountIs(1)
                .and().entriesListContains("assignee", assignee1.getUsername());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process tasks with valid skipCount parameter applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessTasksWithValidSkipCount() throws Exception
    {
        processTasks = restClient.authenticateUser(userWhoStartsProcess)
                .withParams("orderBy=assignee ASC&skipCount=2").withWorkflowAPI()
                .usingProcess(processWithMultipleTasks).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processTasks.assertThat().entriesListContains("assignee", assignee3.getUsername())
                .assertThat().entriesListDoesNotContain("assignee", assignee1.getUsername())
                .assertThat().entriesListDoesNotContain("assignee", assignee2.getUsername())
                .assertThat().entriesListCountIs(1)
                .assertThat().paginationField("skipCount").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process tasks with negative skipCount parameter applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessTasksWithNegativeSkipCount() throws Exception
    {
        restClient.authenticateUser(userWhoStartsProcess)
                .withParams("skipCount=-2").withWorkflowAPI()
                .usingProcess(processWithMultipleTasks).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .containsErrorKey(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process tasks with non numeric skipCount parameter applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessTasksWithNonNumericSkipCount() throws Exception
    {
        restClient.authenticateUser(userWhoStartsProcess)
                .withParams("skipCount=A").withWorkflowAPI()
                .usingProcess(processWithMultipleTasks).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "A"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process tasks with valid maxItems parameter applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessTasksWithValidMaxItems() throws Exception
    {
        processTasks = restClient.authenticateUser(userWhoStartsProcess)
                .withParams("orderBy=assignee ASC&maxItems=2").withWorkflowAPI()
                .usingProcess(processWithMultipleTasks).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processTasks.assertThat().entriesListDoesNotContain("assignee", assignee3.getUsername())
                .assertThat().entriesListContains("assignee", assignee1.getUsername())
                .assertThat().entriesListContains("assignee", assignee2.getUsername())
                .assertThat().entriesListCountIs(2)
                .assertThat().paginationField("maxItems").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process tasks with negative maxItems parameter applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessTasksWithNegativeMaxItems() throws Exception
    {
        restClient.authenticateUser(userWhoStartsProcess)
                .withParams("maxItems=-2").withWorkflowAPI()
                .usingProcess(processWithMultipleTasks).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process tasks with non numeric maxItems parameter applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessTasksWithNonNumericMaxItems() throws Exception
    {
        restClient.authenticateUser(userWhoStartsProcess)
                .withParams("maxItems=A").withWorkflowAPI()
                .usingProcess(processWithMultipleTasks).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "A"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify getProcessTasks with properties parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessTasksWithPropertiesParameter() throws Exception
    {
        processTasks = restClient.authenticateUser(adminUser)
                .withParams("properties=name,assignee,state").withWorkflowAPI()
                .usingProcess(processWithSingleTask).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processTasks.assertThat().entriesListCountIs(1);
        processTasks.getOneRandomEntry().onModel().assertThat()
                .field("dueAt").isNull().and()
                .field("processDefinitionId").isNull().and()
                .field("processId").isNull().and()
                .field("description").isNull().and()
                .field("startedAt").isNull().and()
                .field("id").isNull().and()
                .field("activityDefinitionId").isNull().and()
                .field("priority").isNull().and()
                .field("formResourceKey").isNull().and()
                .field("name").is("Review Task").and()
                .field("state").is("claimed").and()
                .field("assignee").is(assignee1.getUsername());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin gets Process Tasks ordered by assignee ascendant using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessTasksOrderedByAssignee() throws Exception
    {
        processTasks = restClient.authenticateUser(adminUser)
                .withParams("orderBy=assignee").withWorkflowAPI()
                .usingProcess(processWithMultipleTasks).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processTasks.assertThat().entriesListIsSortedAscBy("assignee");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get Process Tasks ordered by many fields")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessTasksOrderedByMultipleFields() throws Exception
    {
        restClient.authenticateUser(adminUser)
                .withParams("orderBy=assignee,state").withWorkflowAPI()
                .usingProcess(processWithMultipleTasks).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary("Only one order by parameter is supported");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process tasks after process is deleted.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessTasksAfterDeletingProcess() throws Exception
    {
        ProcessModel processModel = dataWorkflow.usingUser(userWhoStartsProcess).usingSite(publicSite).usingResource(document)
                .createSingleReviewerTaskAndAssignTo(assignee1);

        restClient.authenticateUser(adminUser).withWorkflowAPI().usingProcess(processModel).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        processTasks = restClient.authenticateUser(adminUser).withWorkflowAPI()
                .usingProcess(processModel).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processTasks.assertThat().entriesListIsEmpty();
    }

}
