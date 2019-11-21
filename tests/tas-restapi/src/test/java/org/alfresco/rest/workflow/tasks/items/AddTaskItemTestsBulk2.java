package org.alfresco.rest.workflow.tasks.items;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestVariableModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TaskModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddTaskItemTestsBulk2 extends RestTest
{
    private UserModel userModel, assigneeUser, anotherUser;
    private SiteModel siteModel;
    private FileModel fileModel, fileModel1;
    private TaskModel taskModel;
    private String taskId;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        fileModel1 = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        assigneeUser = dataUser.createRandomTestUser();
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);

        taskId = taskModel.getId();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Add task item using random user.")
    public void addTaskItemByRandomUser() throws Exception
    {
        anotherUser = dataUser.createRandomTestUser();
       restClient.authenticateUser(anotherUser).withWorkflowAPI().usingTask(taskModel).addTaskItem(fileModel);

        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                  .assertLastError()
                  .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                  .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
     }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Add multiple task item using random user.")
    public void addMultipleTaskItemByRandomUser() throws Exception
    {
        anotherUser = dataUser.createRandomTestUser();
        restClient.authenticateUser(anotherUser).withWorkflowAPI().usingTask(taskModel)
                             .addTaskItems(fileModel,fileModel1);

        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                  .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                  .containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);;
    }

    @Bug(id = "ACE-5683")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Adding task item, is falling in case invalid itemBody is provided")
    public void failedAddingTaskItemIfInvalidItemBodyIsProvided() throws Exception
    {
        fileModel.setNodeRef("invalidNodeRef");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskItem(fileModel);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidNodeRef"))
                  .containsErrorKey(RestErrorModel.NOT_FOUND_ERRORKEY)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @Bug(id = "ACE-5683")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
     description = "Adding multiple task item, is falling in case invalid itemBody is provided")
    public void failedAddingMultipleTaskItemIfInvalidItemBodyIsProvided() throws Exception
    {
        fileModel.setNodeRef("invalidNodeRef");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel)
                              .addTaskItems(fileModel, fileModel1);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidNodeRef"))
                  .containsErrorKey(RestErrorModel.NOT_FOUND_ERRORKEY)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);;
    }

    @Bug(id = "ACE-5675")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Adding task item is falling in case empty item body is provided")
    public void failedAddingTaskItemIfEmptyItemBodyIsProvided() throws Exception
    {
        fileModel.setNodeRef("");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskItem(fileModel);

        // TODO - expected error message to be added
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("");
    }
    
    @Bug(id = "ACE-5675")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
       description = "Adding multiple task item is falling in case empty item body is provided")
    public void failedAddingMultipleTaskItemIfEmptyItemBodyIsProvided() throws Exception
    {
        fileModel.setNodeRef("");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskItems(fileModel, fileModel1);

        // TODO - expected error message to be added
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Adding task item is falling in case invalid task id is provided")
    public void failedAddingTaskItemIfInvalidTaskIdIsProvided() throws Exception
    {
        taskModel.setId("invalidTaskId");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskItem(fileModel);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidTaskId"))
                  .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);;
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Adding task item is falling in case invalid task id is provided")
    public void failedAddingMultipleTaskItemIfInvalidTaskIdIsProvided() throws Exception
    {
        taskModel.setId("invalidTaskId");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskItems(fileModel,fileModel1);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidTaskId"))
                  .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "ACE-5675")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Adding task item is falling in case incomplete body type is provided")
    public void failedAddingTaskVariableIfIncompleteBodyIsProvided() throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{}", "tasks/{taskId}/items", taskId);
        restClient.processModel(RestVariableModel.class, request);

        // TODO - expected error message to be added
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("");
    }    
}
