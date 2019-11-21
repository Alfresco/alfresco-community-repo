package org.alfresco.rest.workflow.processes;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessModelsCollection;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * 
 * @author Cristina Axinte
 *
 */
public class GetProcessesFullTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel adminUser, userWhoStartsTask, assignee;
    private TaskModel task1, task2;
    private ProcessModel process3;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        task1 = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);  
        task2 = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(adminUser);
        process3 = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createSingleReviewerTaskAndAssignTo(assignee);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user gets processes when skipCount parameter is applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessesWithSkipCountParameter() throws Exception
    {
        RestProcessModelsCollection processes = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processes.assertThat().paginationField("count").is("3");
        RestProcessModel process1 = processes.getEntries().get(0).onModel();
        RestProcessModel process2 = processes.getEntries().get(1).onModel();
        RestProcessModel process3 = processes.getEntries().get(2).onModel();

        processes = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingParams("skipCount=2").getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processes.assertThat().paginationField("count").is("1");
        processes.assertThat().paginationField("skipCount").is("2");
        processes.assertThat().entriesListDoesNotContain("id", process1.getId());
        processes.assertThat().entriesListDoesNotContain("id", process2.getId());
        processes.getEntries().get(0).onModel().assertThat().field("id").is(process3.getId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user gets processes when maxItems parameter is applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessesWithMaxItemsParameter() throws Exception
    {
        RestProcessModelsCollection processes = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processes.assertThat().paginationField("count").is("3");
        RestProcessModel process1 = processes.getEntries().get(0).onModel();
        RestProcessModel process2 = processes.getEntries().get(1).onModel();
        RestProcessModel process3 = processes.getEntries().get(2).onModel();

        processes = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingParams("maxItems=2").getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processes.assertThat().paginationField("count").is("2");
        processes.assertThat().paginationField("maxItems").is("2");
        processes.assertThat().entriesListDoesNotContain("id", process3.getId());
        processes.getEntries().get(0).onModel().assertThat().field("id").is(process1.getId());
        processes.getEntries().get(1).onModel().assertThat().field("id").is(process2.getId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user gets processes when properties parameter is applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessesWithPropertiesParameter() throws Exception
    {
        RestProcessModelsCollection processes = restClient.authenticateUser(userWhoStartsTask).withParams("properties=id")
                .withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        List<RestProcessModel> processesList = processes.getEntries();
        processesList.get(0).onModel().assertThat().fieldsCount().is(1)
            .and().field("id").isNotEmpty()
            .and().field("processDefinitionId").isNull()
            .and().field("startUserId").isNull();
        processesList.get(1).onModel().assertThat().fieldsCount().is(1)
            .and().field("id").isNotEmpty()
            .and().field("processDefinitionId").isNull()
            .and().field("startUserId").isNull();
        processesList.get(2).onModel().assertThat().fieldsCount().is(1)
            .and().field("id").isNotEmpty()
            .and().field("processDefinitionId").isNull()
            .and().field("startUserId").isNull();
        processes.assertThat().entriesListIsNotEmpty()
            .and().entriesListContains("id", process3.getId())
            .and().entriesListContains("id", task2.getProcessId())
            .and().entriesListContains("id", task1.getProcessId());
    }

    @Bug(id = "REPO-1958")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user cannot get processes when using an invalid orderBy parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessesWithInvalidOrderByParameter() throws Exception
    {
        restClient.authenticateUser(userWhoStartsTask).withParams("orderBy=test")
                .withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
            .containsErrorKey(String.format(RestErrorModel.INVALID_ORDERBY, "test", "processDefinitionId, startUserId, startActivityId,startedAt, id, completed, processDefinitionKey"))
            .containsSummary(String.format(RestErrorModel.INVALID_ORDERBY, "test", "processDefinitionId, startUserId, startActivityId,startedAt, id, completed, processDefinitionKey"))
            .stackTraceIs(RestErrorModel.STACKTRACE)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user cannot get processes when using an invalid parameter in where clause")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessesWithInvalidWhereParameter() throws Exception
    {
        restClient.authenticateUser(userWhoStartsTask).where("startUserIdd='" + userWhoStartsTask.getUsername() + "'")
                .withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
            .containsErrorKey(RestErrorModel.INVALID_PROPERTY_ERRORKEY)
            .containsSummary(String.format(RestErrorModel.PROPERTY_IS_NOT_SUPPORTED_EQUALS, "startUserIdd", userWhoStartsTask.getUsername()))
            .stackTraceIs(RestErrorModel.STACKTRACE)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user cannot get processes when using an invalid where clause expression")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessesWithInvalidWhereClauseExpression() throws Exception
    {
        restClient.authenticateUser(userWhoStartsTask).where("startUserId AND '" + userWhoStartsTask.getUsername() + "'")
                .withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
            .containsErrorKey(RestErrorModel.INVALID_QUERY_ERRORKEY)
            .containsSummary(String.format(RestErrorModel.INVALID_WHERE_QUERY, "(startUserId AND '" + userWhoStartsTask.getUsername() + "')"))
            .stackTraceIs(RestErrorModel.STACKTRACE)
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
}
