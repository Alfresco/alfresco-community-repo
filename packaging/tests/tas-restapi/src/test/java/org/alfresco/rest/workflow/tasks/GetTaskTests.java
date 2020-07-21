package org.alfresco.rest.workflow.tasks;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.rest.model.RestTaskModelsCollection;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.ProcessModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TaskModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * 
 * @author Cristina Axinte
 *
 */
public class GetTaskTests extends RestTest
{
    UserModel userModel, assigneeUser;
    UserModel adminTenantUser, tenantUser, tenantUserAssignee, adminUser;
    SiteModel siteModel;
    FileModel fileModel;
    TaskModel taskModel;
    RestTaskModel restTaskModel, tenantTask;
    RestTaskModelsCollection taskModels;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        assigneeUser = dataUser.createRandomTestUser();   
    }
    
    @BeforeMethod(alwaysRun=true)
    public void createTask() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.SANITY, description = "Verify admin user gets any existing task with Rest API and response is successfull (200)")
    public void adminUserGetsAnyTaskWithSuccess() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("description").is(taskModel.getMessage());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.SANITY, description = "Verify assignee user gets its assigned task with Rest API and response is successfull (200)")
    public void assigneeUserGetsItsTaskWithSuccess() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("description").is(taskModel.getMessage()).and().field("assignee")
                .is(assigneeUser.getUsername());

    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.SANITY, description = "Verify user that started the task gets the started task with Rest API and response is successfull (200)")
    public void starterUserGetsItsTaskWithSuccess() throws Exception
    {
        restTaskModel = restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("description").is(taskModel.getMessage());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify any user with no relation to task id forbidden to get other task with Rest API (403)")
    public void anyUserIsForbiddenToGetOtherTask() throws Exception
    {
        UserModel anyUser = dataUser.createRandomTestUser();

        restTaskModel = restClient.authenticateUser(anyUser).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary("Permission was denied");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.SANITY, description = "Verify candidate user gets its specific task and no other user claimed the task with Rest API and response is successfull (200)")
    public void candidateUserGetsItsTasks() throws Exception
    {
        UserModel userModel1 = dataUser.createRandomTestUser();
        UserModel userModel2 = dataUser.createRandomTestUser();
        GroupModel group = dataGroup.createRandomGroup();
        dataGroup.addListOfUsersToGroup(group, userModel1, userModel2);
        TaskModel taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);

        restTaskModel = restClient.authenticateUser(userModel1).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("description").is(taskModel.getMessage());

    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @Bug(id = "MNT-17051")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.SANITY, description = "Verify involved user in a task without claim it gets the task with Rest API and response is successfull (200)")
    public void involvedUserWithoutClaimTaskGetsTask() throws Exception
    {
        UserModel userModel1 = dataUser.createRandomTestUser();
        UserModel userModel2 = dataUser.createRandomTestUser();
        GroupModel group = dataGroup.createRandomGroup();
        dataGroup.addListOfUsersToGroup(group, userModel1, userModel2);
        TaskModel taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        dataWorkflow.usingUser(userModel1).claimTask(taskModel);

        restTaskModel = restClient.authenticateUser(userModel2).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("description").is(taskModel.getMessage());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, 
        executionType = ExecutionType.REGRESSION, description = "Verify user who started a task gets the task with empty taskId with Rest API")
    public void starterUserGetsTaskWithEmptyTaskId() throws Exception
    {
        UserModel userWhoStartsTask = dataUser.createRandomTestUser();
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userWhoStartsTask);

        restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tasks/{taskId}", "");
        RestTaskModelsCollection tasks = restClient.processModels(RestTaskModelsCollection.class, request);
        
        restClient.assertStatusCodeIs(HttpStatus.OK);
        tasks.assertThat().entriesListIsNotEmpty()
            .and().entriesListCountIs(1)
            .and().entriesListContains("id", taskModel.getId());
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, 
        executionType = ExecutionType.REGRESSION, description = "Verify user who started a task gets the task with properties parameter with Rest API")
    public void starterUserGetsTaskWithPropertiesParameter() throws Exception
    {
        restTaskModel = restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).usingParams("properties=id,assignee,formResourceKey").getTask();
        
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().fieldsCount().is(3)
            .and().field("id").is(taskModel.getId())
            .and().field("assignee").is(taskModel.getAssignee())
            .and().field("formResourceKey").is("wf:adhocTask")
            .and().field("processDefinitionId").isNull()
            .and().field("processId").isNull();
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, 
        executionType = ExecutionType.REGRESSION, description = "Verify user who started a task gets the task after it is deleted with Rest API")
    public void starterUserGetsDeletedTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId());
        
        ProcessModel process = new ProcessModel();
        process.setId(taskModel.getProcessId());
        dataWorkflow.usingUser(userModel).deleteProcess(process);

        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
            .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
            .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, taskModel.getId()))
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
            .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify if using invalid taskId status code 404 is returned.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void invalidTaskId() throws Exception
    {
        taskModel.setId(RandomStringUtils.randomAlphanumeric(20));
        restTaskModel = restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, taskModel.getId()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify if using empty taskId status code 200 is returned.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void emptyTaskId() throws Exception
    {
        restClient.authenticateUser(userModel).withWorkflowAPI();
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, "tasks/{taskId}", "");
        taskModels = restClient.processModels(RestTaskModelsCollection.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        taskModels.assertThat().entriesListIsNotEmpty();
    }
}
