package org.alfresco.rest.workflow.tasks.items;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestItemModel;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author bogdan.bocancea
 */
public class RemoveTaskItemTests extends RestTest
{
    private UserModel adminUser, userWhoStartsTask, assigneeUser;
    private SiteModel siteModel;
    private FileModel fileModel, document2;
    private TaskModel taskModel;
    private RestItemModel taskItem;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsTask = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        fileModel = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        assigneeUser = dataUser.createRandomTestUser();
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Delete existing task item")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    public void deleteTaskItem() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(adminUser);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);

        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskItems()
                .assertThat().entriesListDoesNotContain("id", taskItem.getId()).and()
                .entriesListDoesNotContain("name", document2.getName());
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Try to Delete existing task item using invalid taskId")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemUsingInvalidTaskId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(adminUser);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);

        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        taskModel.setId("invalidTaskId");
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidTaskId"));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Try to Delete existing task item using invalid itemId")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemUsingInvalidItemId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(adminUser);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        taskItem.setId("incorrectItemId");
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.PROCESS_ENTITY_NOT_FOUND, "incorrectItemId"));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete existing task item with empty task id")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemEmptyTaskId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(userWhoStartsTask);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        taskModel.setId("");
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete existing task item with empty item id")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemEmptyItemId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(userWhoStartsTask);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        taskItem.setId("");
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                .assertLastError().containsSummary(RestErrorModel.DELETE_EMPTY_ARGUMENT);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete existing task item for completed task")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemForCompletedTask() throws Exception
    {
        TaskModel completedTask = dataWorkflow.usingUser(userWhoStartsTask)
                .usingSite(siteModel)
                .usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(userWhoStartsTask);
        document2 = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.withWorkflowAPI().usingTask(completedTask).addTaskItem(document2);
        dataWorkflow.usingUser(assigneeUser).taskDone(completedTask);
        dataWorkflow.usingUser(userWhoStartsTask).taskDone(completedTask);
        restClient.withWorkflowAPI().usingTask(completedTask).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, completedTask.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Delete existing task item with candidate user")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    public void deleteTaskItemWithCandidateUser() throws Exception
    {
        GroupModel group = dataGroup.createRandomGroup();
        dataGroup.addListOfUsersToGroup(group, assigneeUser);
        TaskModel groupTask = dataWorkflow.usingUser(userWhoStartsTask)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);

        restClient.authenticateUser(userWhoStartsTask);
        document2 = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.withWorkflowAPI().usingTask(groupTask).addTaskItem(document2);
        restClient.authenticateUser(assigneeUser)
                .withWorkflowAPI().usingTask(groupTask).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete existing task item with unauthorized user")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemWithUnauthorizedUser() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        UserModel unauthorizedUser = dataUser.createRandomTestUser();
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        restClient.authenticateUser(unauthorizedUser).withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Delete existing task item with inexistent user")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void deleteTaskItemWithInexistentUser() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        restClient.authenticateUser(UserModel.getRandomUserModel()).withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete existing task item for deleted task")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemFromDeletedProcess() throws Exception
    {
        ProcessModel deletedProcess = dataWorkflow.usingUser(userWhoStartsTask)
                .usingSite(siteModel)
                .usingResource(fileModel).createMoreReviewersWorkflowAndAssignTo(assigneeUser);
        TaskModel task = restClient.authenticateUser(userWhoStartsTask)
                .withWorkflowAPI().usingProcess(deletedProcess).getProcessTasks().getEntries().get(0).onModel();

        document2 = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingTask(task).addTaskItem(document2);
        dataWorkflow.usingUser(userWhoStartsTask).deleteProcess(deletedProcess);
        restClient.withWorkflowAPI().usingTask(task).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, task.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task item twice")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemTwice() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(userWhoStartsTask);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);

        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.PROCESS_ENTITY_NOT_FOUND, document2.getNodeRefWithoutVersion()))
                              .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                              .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                              .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task item with locked document")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemWithLockedDocument() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(userWhoStartsTask);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);

        dataContent.usingUser(userWhoStartsTask).usingResource(document2).checkOutDocument();
        
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.CONFLICT)
            .assertLastError().containsSummary(String.format(RestErrorModel.LOCKED_NODE_OPERATION, document2.getNodeRefWithoutVersion()))
                              .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                              .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                              .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task item with deleted document")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskItemWithDeletedDocument() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(userWhoStartsTask);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);

        dataContent.usingUser(userWhoStartsTask).usingResource(document2).deleteContent();
        
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.PROCESS_ENTITY_NOT_FOUND, document2.getNodeRefWithoutVersion()))
                              .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                              .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                              .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}