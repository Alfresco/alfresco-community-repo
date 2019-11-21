package org.alfresco.rest.workflow.tasks;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.rest.model.RestVariableModelsCollection;
import org.alfresco.utility.model.*;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.json.JsonObject;
import java.util.HashMap;

public class UpdateTaskTests extends RestTest
{
    UserModel owner, anyUser;
    SiteModel siteModel;
    FileModel fileModel;
    UserModel assigneeUser;
    TaskModel taskModel;
    RestTaskModel restTaskModel;
    UserModel adminUser;
    RestProcessDefinitionModel activitiReviewProcessDefinition;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        owner = dataUser.createRandomTestUser();
        anyUser = dataUser.createRandomTestUser();
        assigneeUser = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(adminUser).createPublicRandomSite();
        fileModel = dataContent.usingUser(adminUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        activitiReviewProcessDefinition = restClient.authenticateUser(adminUser).withWorkflowAPI().getAllProcessDefinitions().getProcessDefinitionByKey("activitiReviewPooled");
    }

    @BeforeMethod(alwaysRun = true)
    public void createTask() throws Exception
    {
        taskModel = dataWorkflow.usingUser(owner).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY, description = "Verify admin user updates task with Rest API and response is successfull (200)")
    public void adminUserUpdatesAnyTaskWithSuccess() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("description").is(taskModel.getMessage());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY, description = "Verify assignee user updates its assigned task with Rest API and response is successfull (200)")
    public void assigneeUserUpdatesItsTaskWithSuccess() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("description").is(taskModel.getMessage());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY, description = "Verify user that started the task updates the started task with Rest API and response is successfull (200)")
    public void starterUserUpdatesItsTaskWithSuccess() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify any user with no relation to task is forbidden to update other task with Rest API (403)")
    public void anyUserIsForbiddenToUpdateOtherTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(anyUser).withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary("Permission was denied");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY, description = "Verify candidate user updates its specific task and no other user claimed the task with Rest API and response is successfull (200)")
    public void candidateUserUpdatesItsTasks() throws Exception
    {
        UserModel userModel1 = dataUser.createRandomTestUser();
        UserModel userModel2 = dataUser.createRandomTestUser();
        GroupModel group = dataGroup.createRandomGroup();
        dataGroup.addListOfUsersToGroup(group, userModel1, userModel2);
        TaskModel taskModel = dataWorkflow.usingUser(owner).usingSite(siteModel).usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);

        restTaskModel = restClient.authenticateUser(userModel1).withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat()
                .field("dueAt").isNotEmpty()
                .and().field("processDefinitionId").is(activitiReviewProcessDefinition.getId())
                .and().field("processId").is(taskModel.getProcessId())
                .and().field("name").is("Review Task")
                .and().field("description").is(taskModel.getMessage())
                .and().field("startedAt").isNotEmpty()
                .and().field("id").is(taskModel.getId())
                .and().field("state").is("unclaimed")
                .and().field("activityDefinitionId").is("reviewTask")
                .and().field("priority").is(taskModel.getPriority().getLevel())
                .and().field("formResourceKey").is("wf:activitiReviewTask");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner can update twice task with status resolve successfully (200)")
    public void taskOwnerCanUpdateTaskWithResolveStateTwice() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("resolved");

        restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task asignee cannot update twice task with status resolve - Forbidden (403)")
    public void taskAssigneeCanNotUpdateTaskWithResolveStateTwice() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("resolved");

        restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);

    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task cannot be update using a invalid json body property - Bad Request (400)")
    public void taskCanNotBeUpdatedWithInvalidJsonBodyProperty() throws Exception
    {
        String invalidJsonProperty= "states";
        restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel);
        String postBody = JsonBodyGenerator.keyValueJson(invalidJsonProperty, "completed");
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restClient.processModel(RestTaskModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.NO_CONTENT, "Unrecognized field \""+invalidJsonProperty+"\""));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task cannot be update using a invalid state - Bad Request (400)")
    public void taskCanNotBeUpdatedWithInvalidState() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("continued");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.INVALID_VALUE, "task state", "continued"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task cannot be update using a invalid property - Bad Request (400)")
    public void taskCanNotBeUpdatedWithInvalidSelectProperty() throws Exception
    {
        String invalidProperty= "states";
        restClient.authenticateUser(assigneeUser)
                .withParams("select=" + invalidProperty).withWorkflowAPI().usingTask(taskModel);
        String postBody = JsonBodyGenerator.keyValueJson("state", "completed");
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restClient.processModel(RestTaskModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.PROPERTY_DOES_NOT_EXIST, invalidProperty));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task can be update using multiple select values successfully (200)")
    public void taskCanBeUpdatedWithMultipleSelectValues() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "resolved")
                .add("assignee", "admin").build();

        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("resolved");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Verify task assignee can complete task successfully (200)")
    public void taskAssigneeCanCompleteTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("completed");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Verify task owner can complete task successfully (200))")
    public void taskOwnerCanCompleteTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("completed");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot complete task - Forbidden (403))")
    public void anyUserCannotCompleteTask() throws Exception
    {
        UserModel anyUser = dataUser.createRandomTestUser();

        restTaskModel = restClient.authenticateUser(anyUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @Bug(id = "REPO-2062")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner cannot complete task with invalid name - Bad Request(400))")
    public void taskOwnerCannotCompleteTaskWithInvalidName() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "completed")
                .add("variables", JsonBodyGenerator.defineJSONArray()
                        .add(JsonBodyGenerator.defineJSON()
                                .add("name", "bpmx_priorityx")
                                .add("type", "d:int")
                                .add("value", 1)
                                .add("scope", "global").build())
                ).build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,variables").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.UNSUPPORTED_TYPE,"bpm_priorityx"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner cannot complete task with invalid Type - Bad Request(400))")
    public void taskOwnerCannotCompleteTaskWithInvalidType() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "completed")
                .add("variables", JsonBodyGenerator.defineJSONArray()
                        .add(JsonBodyGenerator.defineJSON()
                                .add("name", "bpmx_priority")
                                .add("type", "d_int")
                                .add("value", 1)
                                .add("scope", "global").build())
                ).build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,variables").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.UNSUPPORTED_TYPE,"d_int"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @Bug(id = "REPO-2062")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner cannot complete task with invalid value - Bad Request(400))")
    public void taskOwnerCannotCompleteTaskWithInvalidValue() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "completed")
                .add("variables", JsonBodyGenerator.defineJSONArray()
                        .add(JsonBodyGenerator.defineJSON()
                                .add("name", "bpmx_priority")
                                .add("type", "d:int")
                                .add("value", "text")
                                .add("scope", "global").build())
                ).build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,variables").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.UNSUPPORTED_TYPE, "text"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner cannot complete task with invalid scope - Bad Request(400))")
    public void taskOwnerCannotCompleteTaskWithInvalidScope() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "completed")
                .add("variables", JsonBodyGenerator.defineJSONArray()
                        .add(JsonBodyGenerator.defineJSON()
                                .add("name", "bpmx_priority")
                                .add("type", "d:int")
                                .add("value", 1)
                                .add("scope", "globalscope").build())
                ).build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,variables").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(String.format(RestErrorModel.ILLEGAL_SCOPE, "globalscope"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task assignee can claim task successfully (200)")
    public void taskAssigneeCanClaimTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task assignee can unclaim task successfully (200)")
    public void taskAssigneeCanUnclaimTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("unclaimed");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task assignee can delegate task successfully (200)")
    public void taskAssigneeCanDelegateTask() throws Exception
    {
        UserModel delagateToUser = dataUser.createRandomTestUser();

        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "delegated")
                .add("assignee", delagateToUser.getUsername()).build();

        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("delegated")
                .and().field("assignee").is(delagateToUser.getUsername());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner can delegate task to same assignee successfully (200)")
    public void taskOwnerCanDelegateTaskToSameAssignee() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "delegated")
                .add("assignee", assigneeUser.getUsername()).build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("delegated")
                .and().field("assignee").is(assigneeUser.getUsername());
    }


    @Bug(id = "REPO-2063")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task assignee cannot delegate task to task owner -  (400)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskAssigneeCannotDelegateTaskToTaskOwner() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "delegated")
                .add("assignee", owner.getUsername()).build();

        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @Bug(id = "REPO-2063")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task assignee cannot delegate task to invalid assignee - (400)")
    public void taskAssigneeCannotDelegateTaskToInvalidAssignee() throws Exception
    {
        UserModel invalidAssignee = new UserModel("invalidAssignee", "password");

        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "delegated")
                .add("assignee", invalidAssignee.getUsername()).build();

        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task assignee can update task with status resolve")
    public void resolveTaskAsCurrentAssignee() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("resolved");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify update task using invalid taskId")
    public void updateTaskUsingInvalidTaskId() throws Exception
    {
        taskModel.setId("invalidId");

        restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidId"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify update task using another name")
    public void updateTaskUsingAnotherName() throws Exception
    {
        restClient.authenticateUser(owner).withParams("select=name").withWorkflowAPI().usingTask(taskModel);
        String postBody = "{\"name\":\"newNameTask\"}";
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("name").is("newNameTask");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify update task using another description")
    public void updateTaskUsingAnotherDescription() throws Exception
    {
        restClient.authenticateUser(owner).withParams("select=description").withWorkflowAPI().usingTask(taskModel);
        String postBody = "{\"description\":\"newDescription\"}";
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("description").is("newDescription");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify update task using another priority")
    public void updateTaskUsingAnotherPriority() throws Exception
    {
        restClient.authenticateUser(owner).withParams("select=priority").withWorkflowAPI().usingTask(taskModel);
        String postBody = "{\"priority\":3}";
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("priority").is(CMISUtil.Priority.Low.getLevel());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify update task using another priority")
    public void updateTaskUsingAnotherOwner() throws Exception
    {
        UserModel newOwner = dataUser.createRandomTestUser();

        restClient.authenticateUser(owner).withParams("select=owner").withWorkflowAPI().usingTask(taskModel);
        String postBody = "{\"owner\":\""+newOwner.getUsername()+"\"}";
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("owner").is(newOwner.getUsername());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify owner can not update task from completed to claimed and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanNotUpdateTaskFromCompletedToClaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("completed");
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError()
                .containsSummary(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify owner can not update task from completed to unclaimed and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanNotUpdateTaskFromCompletedToUnclaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("completed");
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError()
                .containsSummary(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify owner can not update task from completed to completed and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanNotUpdateTaskWithCompleteStateTwice() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("completed");
        restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError()
                .containsSummary(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify owner can update task from claimed to completed and response is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanUpdateTaskFromClaimedToCompleted() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("claimed");
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("completed");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify user can update task from claimed to claimed and response is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void userCanUpdateTaskWithClaimedStateTwice() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("claimed");
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("claimed");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify owner can update task from unclaimed to completed and response is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanUpdateTaskFromUnclaimedToCompleted() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("completed");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify owner can not update task from unclaimed to unclaimed and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanNotUpdateTaskWithUnclaimedStateTwice() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
    }

    @Bug(id = "REPO-1924")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify owner can not update task from delegated to completed and response is 422")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanNotUpdateTaskFromDelegatedToCompleted() throws Exception
    {
        restClient.authenticateUser(owner).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel);
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("state", "delegated");
        body.put("assignee", assigneeUser.getUsername());
        String postBody = JsonBodyGenerator.keyValueJson(body);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(),
                restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated").and().field("assignee")
                .is(assigneeUser.getUsername());

        restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError().containsSummary(RestErrorModel.DELEGATED_TASK_CAN_NOT_BE_COMPLETED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify owner can not update task from delegated to delegated and response is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanNotUpdateTaskWithDelegatedStateTwice() throws Exception
    {
        restClient.authenticateUser(owner).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel);
        HashMap<String, String> body = new HashMap<String, String>();
        body.put("state", "delegated");
        body.put("assignee", assigneeUser.getUsername());
        String postBody = JsonBodyGenerator.keyValueJson(body);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(),
                restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated").and().field("assignee")
                .is(assigneeUser.getUsername());

        request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated").and().field("assignee")
                .is(assigneeUser.getUsername());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,
            TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Verify owner can update task from resolved to completed and response is 200")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanUpdateTaskFromResolvedToCompleted() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("resolved");

        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("completed");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify owner cannot update task from status completed to delegated and response is 404")
    public void ownerCannotUpdateTaskFromCompletedToDelegated() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("completed");

        restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("delegated");
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .containsSummary(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee cannot update task from status completed to delegated and response is 404")
    public void assigneeCannotUpdateTaskFromCompletedToDelegated() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("completed");

        restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("delegated");
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .containsSummary(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify owner cannot update task from status completed to resolved and response is 404")
    public void ownerCannotUpdateTaskFromCompletedToResolved() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("completed");

        restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .containsSummary(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee cannot update task from status completed to resolved and response is 404")
    public void assigneeCannotUpdateTaskFromCompletedToResolved() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("completed");

        restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .containsSummary(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify owner cannot update task from status completed to completed and response is 404")
    public void ownerCannotUpdateTaskFromCompletedToCompleted() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("completed");

        restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .containsSummary(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee cannot update task from status completed to completed and response is 404")
    public void assigneeCannotUpdateTaskFromCompletedToCompleted() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("completed");

        restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .containsSummary(String.format(RestErrorModel.TASK_ALREADY_COMPLETED, taskModel.getId()))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify owner can update task from status claimed to unclaimed and response is 200")
    public void ownerCanUpdateTaskFromClaimedToUnclaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("unclaimed");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee can update task from status claimed to unclaimed and response is 200")
    public void assigneeCanUpdateTaskFromClaimedToUnclaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");

        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("unclaimed");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify owner can update task from status claimed to delegated and response is 200")
    public void ownerCanUpdateTaskFromClaimedToDelegated() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");

        UserModel newAssignee = dataUser.createRandomTestUser();
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "delegated")
                .add("assignee", newAssignee.getUsername()).build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("delegated")
                .and().field("assignee").is(newAssignee.getUsername());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee can update task from status claimed to delegated and response is 200")
    public void assigneeCanUpdateTaskFromClaimedToDelegated() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");

        UserModel newAssignee = dataUser.createRandomTestUser();
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "delegated")
                .add("assignee", newAssignee.getUsername()).build();

        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("delegated")
                .and().field("assignee").is(newAssignee.getUsername());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify owner can update task from status claimed to resolved and response is 200")
    public void ownerCanUpdateTaskFromClaimedToResolved() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("resolved");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee can update task from status claimed to resolved and response is 200")
    public void assigneeCanUpdateTaskFromClaimedToResolved() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");

        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("resolved");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify owner cannot update task from status claimed to claimed and response is 409")
    public void ownerCannotUpdateTaskFromClaimedToClaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT).assertLastError()
                .containsErrorKey(RestErrorModel.TASK_ALREADY_CLAIMED)
                .containsSummary(RestErrorModel.TASK_ALREADY_CLAIMED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify assignee can update task from status claimed to claimed and response is 200")
    public void assigneeCanUpdateTaskFromClaimedToClaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(taskModel).getTask();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");

        restTaskModel = restClient.authenticateUser(assigneeUser)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId())
                .and().field("state").is("claimed");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner can complete task with valid variables and response is 200")
    public void taskOwnerCanCompleteTaskWithValidVariables() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "completed")
                .add("variables", JsonBodyGenerator.defineJSONArray()
                        .add(JsonBodyGenerator.defineJSON()
                                .add("name", "bpm_priority")
                                .add("type", "d:int")
                                .add("value", 3)
                                .add("scope", "global").build())
                ).build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,variables").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("priority").is(1)
                .and().field("state").is("completed")
                .and().field("assignee").is(taskModel.getAssignee());
        RestVariableModelsCollection variables = restClient.authenticateUser(owner).withWorkflowAPI().usingTask(restTaskModel).getTaskVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variables.getVariableByName("bpm_priority").assertThat().field("scope").is("global")
                .and().field("name").is("bpm_priority")
                .and().field("type").is("d:int")
                .and().field("value").is(3);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner cannot complete task with empty variables array and response is 200")
    public void taskOwnerCannotUpdateTaskWithEmptyVariablesArray() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "completed")
                .add("variables", JsonBodyGenerator.defineJSONArray()
                ).build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,variables").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("priority").is(1)
                .and().field("state").is("completed")
                .and().field("assignee").is(taskModel.getAssignee());
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner cannot complete task with empty variables json body and response is 200")
    public void taskOwnerCannotUpdateTaskWithEmptyVariablesJsonBody() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "completed")
                .add("variables", JsonBodyGenerator.defineJSONArray()
                        .add(JsonBodyGenerator.defineJSON().build())
                ).build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,variables").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(RestErrorModel.VARIABLE_NAME_REQUIRED)
                .containsSummary(RestErrorModel.VARIABLE_NAME_REQUIRED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user with no relation to task is forbidden to delegate other task with Rest API (403)")
    public void anyUserIsForbiddenToDelegateOtherTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(anyUser).withWorkflowAPI().usingTask(taskModel).updateTask("delegated");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary("Permission was denied");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user with no relation to task is forbidden to resolve other task with Rest API (403)")
    public void anyUserIsForbiddenToResolveOtherTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(anyUser).withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary("Permission was denied");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user with no relation to task is forbidden to claim other task with Rest API (403)")
    public void anyUserIsForbiddenToClaimOtherTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(anyUser).withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary("Permission was denied");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user with no relation to task is forbidden to unclaim other task with Rest API (403)")
    public void anyUserIsForbiddenToUnclaimOtherTask() throws Exception
    {
        restTaskModel = restClient.authenticateUser(anyUser).withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary("Permission was denied");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify user cannot delegate task with emty assignee name and response is 400")
    public void updateTaskWithEmptyAssigneeValue() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "delegated")
                .add("assignee", "")
                .build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(RestErrorModel.DELEGATING_ASSIGNEE_PROVIDED)
                .containsSummary(RestErrorModel.DELEGATING_ASSIGNEE_PROVIDED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify owner can resolve task when assignee is the owner and response is 200")
    public void taskOwnerUpdateTaskResolveStateAndOwnerAssignee() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("state", "resolved")
                .add("assignee", owner.getUsername())
                .build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("state").is("resolved")
                .and().field("assignee").isNull();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify owner cannot update task after it was deleted and response is 404")
    public void updateTaskAfterItWasDeleted() throws Exception
    {
        ProcessModel process = new ProcessModel();
        process.setId(taskModel.getProcessId());
        dataWorkflow.usingUser(owner).deleteProcess(process);

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .containsSummary(String.format(RestErrorModel.ENTITY_WAS_NOT_FOUND, taskModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify task owner cannot update task with empty input body and response is 400")
    public void taskOwnerCannotUpdateTaskWithEmptyInputBody() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON().build();

        restTaskModel = restClient.authenticateUser(owner)
                .withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.TASK_INVALID_STATE, "null"))
                .containsSummary(String.format(RestErrorModel.TASK_INVALID_STATE, "null"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using another name")
    public void anyUserCannotUpdateTaskUsingAnotherName() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("name", "Review Task-updated")
                .build();

        restClient.authenticateUser(anyUser).withParams("select=name").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using another description")
    public void anyUserCannotUpdateTaskUsingAnotherDescription() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("description", "description updated")
                .build();

        restClient.authenticateUser(anyUser).withParams("select=description").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using another dueAt")
    public void anyUserCannotUpdateTaskUsingAnotherDueAt() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("dueAt", "2025-01-01T11:57:32.000+0000")
                .build();

        restClient.authenticateUser(anyUser).withParams("select=dueAt").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using another priority")
    public void anyUserCannotUpdateTaskUsingAnotherPriority() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("priority", 3)
                .build();

        restClient.authenticateUser(anyUser).withParams("select=priority").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using another priority")
    public void anyUserCannotUpdateTaskUsingAnotherOwner() throws Exception
    {
        UserModel newOwner = dataUser.createRandomTestUser();
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("owner", newOwner.getUsername())
                .build();


        restClient.authenticateUser(anyUser).withParams("select=owner").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using invalid name")
    public void anyUserCannotUpdateTaskUsingInvalidName() throws Exception
    {
        restClient.authenticateUser(owner).withParams("select=name").withWorkflowAPI().usingTask(taskModel);
        String postBody = "{\"name\":\"invalid-\"a\"}";
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "Unexpected character ('a'"))
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "Unexpected character ('a'"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using invalid description")
    public void anyUserCannotUpdateTaskUsingInvalidDescription() throws Exception
    {
        restClient.authenticateUser(owner).withParams("select=description").withWorkflowAPI().usingTask(taskModel);
        String postBody = "{\"description\":\"invalid-\"a\"}";
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "Unexpected character ('a'"))
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "Unexpected character ('a'"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using invalid dueAt")
    public void anyUserCannotUpdateTaskUsingInvalidDueAt() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("dueAt", "invalid-date")
                .build();

        restClient.authenticateUser(anyUser).withParams("select=dueAt").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using invalid priority")
    public void anyUserCannotUpdateTaskUsingInvalidPriority() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON()
                .add("priority", "a")
                .build();

        restClient.authenticateUser(anyUser).withParams("select=priority").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify any user cannot update task using invalid priority")
    public void anyUserCannotUpdateTaskUsingInvalidOwner() throws Exception
    {
        restClient.authenticateUser(owner).withParams("select=owner").withWorkflowAPI().usingTask(taskModel);
        String postBody = "{\"owner\":\"invalid-\"a\"}";
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, postBody, "tasks/{taskId}?{parameters}", taskModel.getId(), restClient.getParameters());
        restTaskModel = restClient.processModel(RestTaskModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "Unexpected character ('a'"))
                .containsSummary(String.format(RestErrorModel.NO_CONTENT, "Unexpected character ('a'"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from unclaimed to claimed by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCanUpdateTaskFromUnclaimedToClaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("claimed");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task cannot be updated from unclaimed to claimed by a regular user not connected to the task")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void regularUserCannotUpdateTaskFromUnclaimedToClaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from unclaimed to delegated by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCanUpdateTaskFromUnclaimedToDelegated() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");

        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", owner.getUsername()).build();

        restTaskModel = restClient.authenticateUser(owner).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task cannot be updated from unclaimed to delegated by a regular user not connected to the task")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void regularUserCannotUpdateTaskFromUnclaimedToDelegated() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");

        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();

        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from unclaimed to resolved by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCanUpdateTaskFromUnclaimedToResolved() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("resolved");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from unclaimed to resolved by a regular user not connected to the task")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void regularCanNotUpdateTaskFromUnclaimedToResolved() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from unclaimed to unclaimed by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCanUpdateTaskFromUnclaimedToUnclaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from unclaimed to unclaimed by a regular user not connected to the task")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void regularUserCannotUpdateTaskFromUnclaimedToUnclaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "REPO-1982")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from delegated to unclaimed by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCanUpdateTaskFromDelegatedToUnclaimed() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");

        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
    }

    @Bug(id = "REPO-1982")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from delegated to unclaimed by task assignee")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskAssigneeCanUpdateTaskFromDelegatedToUnclaimed() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");

        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("unclaimed");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task cannot be updated from delegated to claimed since it is already claimed by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCannotUpdateTaskFromDelegatedToClaimed() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");

        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT).assertLastError()
                .containsErrorKey(RestErrorModel.TASK_ALREADY_CLAIMED)
                .containsSummary(RestErrorModel.TASK_ALREADY_CLAIMED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from delegated to claimed since it is already claimed by task assignee")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskAssigneeCanUpdateTaskFromDelegatedToClaimed() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");

        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");
    }

    @Bug(id = "REPO-1924")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from delegated to completed by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCanUpdateTaskFromDelegatedToCompleted() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");

        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError()
                .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                .containsSummary(RestErrorModel.DELEGATED_TASK_CAN_NOT_BE_COMPLETED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "REPO-1924")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from delegated to completed by task assignee")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskAssigneeCanUpdateTaskFromDelegatedToCompleted() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");

        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("completed");
        restClient.assertStatusCodeIs(HttpStatus.UNPROCESSABLE_ENTITY).assertLastError()
                .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                .containsSummary(RestErrorModel.DELEGATED_TASK_CAN_NOT_BE_COMPLETED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from delegated to resolved")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCanUpdateTaskFromDelegatedToResolved() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");

        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("resolved");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from delegated to resolved by task assignee")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskAssigneeCanUpdateTaskFromDelegatedToResolved() throws Exception
    {
        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");

        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("resolved");
    }

    @Bug(id = "REPO-1982")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from resolved to unclaimed by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCanUpdateTaskFromResolvedToUnclaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");

        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("resolved");
    }

    @Bug(id = "REPO-1982")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task cannot be updated from resolved to unclaimed by task assignee")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskAssigneeCannotUpdateTaskFromResolvedToUnclaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");

        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("unclaimed");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "REPO-1982")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task cannot be updated from resolved to claimed by task owner")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskOwnerCanUpdateTaskFromResolvedToClaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");

        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("resolved");
    }

    @Bug(id = "REPO-1982")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task cannot be updated from resolved to claimed by task assignee")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskAssigneeCanUpdateTaskFromResolvedToClaimed() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");

        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("claimed");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "REPO-1982")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from resolved to delegated by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCannotUpdateTaskFromResolvedToDelegated() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");

        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("delegated");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from resolved to delegated by task assignee")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskAssigneeCannotUpdateTaskFromResolvedToDelegated() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");

        JsonObject inputJson = JsonBodyGenerator.defineJSON().add("state", "delegated").add("assignee", assigneeUser.getUsername()).build();
        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state,assignee").withWorkflowAPI().usingTask(taskModel).updateTask(inputJson);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "REPO-1982")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from resolved to resolved by task creator")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskCreatorCannotUpdateTaskFromResolvedToResolved() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");

        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restTaskModel.assertThat().field("id").is(taskModel.getId()).and().field("state").is("resolved");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that task can be updated from resolved to resolved by task assignee")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void taskAssigneeCannotUpdateTaskFromResolvedToResolved() throws Exception
    {
        restTaskModel = restClient.authenticateUser(adminUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");

        restTaskModel = restClient.authenticateUser(assigneeUser).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError()
                .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "REPO-1982")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task by providing empty select value")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskByProvidingEmptySelectValue() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withParams("select=").withWorkflowAPI().usingTask(taskModel).updateTask("resolved");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(RestErrorModel.INVALID_SELECT_ERRORKEY)
                .containsSummary(RestErrorModel.INVALID_SELECT)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task by providing empty state value")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskByProvidingEmptyStateValue() throws Exception
    {
        restTaskModel = restClient.authenticateUser(owner).withParams("select=state").withWorkflowAPI().usingTask(taskModel).updateTask(" ");
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsErrorKey(String.format(RestErrorModel.TASK_INVALID_STATE, " "))
                .containsSummary(String.format(RestErrorModel.TASK_INVALID_STATE, " "))
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}
