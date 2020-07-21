package org.alfresco.rest.workflow.tasks.variables;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.rest.model.RestVariableModelsCollection;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TaskModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetTaskVariablesTests extends RestTest
{
    private UserModel userWhoStartsTask, assignee;
    private SiteModel siteModel;
    private FileModel fileModel;
    private TaskModel taskModel;
    private RestVariableModelsCollection variableModels;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        fileModel = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Verify that user that started the process gets task variables")
    public void getTaskVariablesByUserWhoStartedProcess() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask)
                .usingSite(siteModel).usingResource(fileModel)
                .createNewTaskAndAssignTo(assignee);
        restClient.authenticateUser(userWhoStartsTask);
        variableModels = restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListIsNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Verify that user that is involved in the process gets task variables")
    public void getTaskVariablesByUserInvolvedInProcess() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask)
                .usingSite(siteModel).usingResource(fileModel)
                .createNewTaskAndAssignTo(assignee);
        restClient.authenticateUser(assignee);
        variableModels = restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListIsNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Verify that user that is not involved in the process gets task variables")
    public void getTaskVariablesUsingAnyUser() throws Exception
    {
        UserModel randomUser = dataUser.createRandomTestUser();
        taskModel = dataWorkflow.usingUser(userWhoStartsTask)
                .usingSite(siteModel).usingResource(fileModel)
                .createNewTaskAndAssignTo(assignee);
        restClient.authenticateUser(randomUser);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Verify that admin is able to  task variables")
    public void getTaskVariablesUsingAdmin() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        taskModel = dataWorkflow.usingUser(userWhoStartsTask)
                .usingSite(siteModel).usingResource(fileModel)
                .createNewTaskAndAssignTo(assignee);
        restClient.authenticateUser(adminUser);
        variableModels = restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListIsNotEmpty();
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify if get task variables request returns status code 404 when invalid taskId is used")
    public void getTaskVariablesUsingInvalidTaskId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask)
                .usingSite(siteModel).usingResource(fileModel)
                .createNewTaskAndAssignTo(assignee);
        taskModel.setId("invalidId");

        restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidId"))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify if get task variables request returns status code 404 when empty taskId is used")
    public void getTaskVariablesUsingEmptyTaskId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assignee);
        taskModel.setId("");

        restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify if get task variables request returns status code 200 after the task is finished.")
    public void getTaskVariablesAfterFinishingTask() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assignee);
        dataWorkflow.usingUser(assignee).taskDone(taskModel);

        variableModels = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListIsNotEmpty();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify if get task variables request returns status code 200 after the process is deleted (Task state is now completed.)")
    public void getTaskVariablesAfterDeletingProcess() throws Exception
    {
        RestProcessModel addedProcess = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        RestTaskModel addedTask = restClient.withWorkflowAPI().getTasks().getTaskModelByProcess(addedProcess);
        restClient.withWorkflowAPI().usingProcess(addedProcess).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        variableModels = restClient.withWorkflowAPI().usingTask(addedTask).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListIsNotEmpty();
    }


    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify user gets task variables that matches a where clause.")
    public void getTaskVariablesWithWhereClauseAsParameter() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assignee);
        variableModels = restClient.authenticateUser(userWhoStartsTask).where("scope='local'")
                .withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListIsNotEmpty()
                .and().entriesListDoesNotContain("scope", "global")
                .and().paginationField("totalItems").is("8");

        variableModels = restClient.authenticateUser(userWhoStartsTask)
                .withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListContains("scope", "global")
                .and().paginationField("totalItems").is("30");
    }

    @Bug(id="MNT-17438")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify user gets task with 'maxItems' parameter")
    public void getTaskVariablesWithMaxItems() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assignee);
        variableModels = restClient.authenticateUser(userWhoStartsTask)
                .withWorkflowAPI().usingParams("maxItems=2").usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListIsNotEmpty()
                .and().paginationField("count").is("2");
    }
    
    @Bug(id="MNT-17438")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify user gets task with 'skipCount' parameter")
    public void getTaskVariablesWithSkipCount() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assignee);
        variableModels = restClient.authenticateUser(userWhoStartsTask)
                .withWorkflowAPI().usingParams("skipCount=10").usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListIsNotEmpty()
                .and().paginationField("count").is("20");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify user cannot get task variables with invalid where clause.")
    public void getTaskVariablesWithInvalidWhereClauseAsParameter() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assignee);
        variableModels = restClient.authenticateUser(userWhoStartsTask).where("scope='fake-where'")
                .withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_WHERE_QUERY, "Invalid value for 'scope' used in query: fake-where."))
                          .containsErrorKey(RestErrorModel.INVALID_QUERY_ERRORKEY)
                          .stackTraceIs(RestErrorModel.STACKTRACE)
                          .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify user gets tasks variables with 'properties' parameter")
    public void getTaskVariablesWithValidProperties() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assignee);
        variableModels = restClient.authenticateUser(userWhoStartsTask).withParams("properties=scope,name")
                .withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.assertThat().entriesListIsNotEmpty();
        variableModels.getOneRandomEntry().onModel().assertThat()
            .fieldsCount().is(2).and()
            .field("type").isNull().and()
            .field("value").isNull().and()
            .field("scope").isNotEmpty().and()
            .field("name").isNotEmpty();
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify user cannot get tasks variables with invalid 'properties' parameter")
    public void getTaskVariablesWithInvalidProperties() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assignee);
        variableModels = restClient.authenticateUser(userWhoStartsTask).withParams("properties=fake")
                .withWorkflowAPI().usingTask(taskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variableModels.getOneRandomEntry().onModel().assertThat()
            .fieldsCount().is(0).and()
            .field("type").isNull().and()
            .field("value").isNull().and()
            .field("scope").isNull().and()
            .field("name").isNull();
    }
}
