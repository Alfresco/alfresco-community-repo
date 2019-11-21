package org.alfresco.rest.workflow.tasks.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestVariableModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TaskModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author bogdan.bocancea
 */
public class DeleteTaskVariableTests extends RestTest
{
    private UserModel userModel, assigneeUser, adminUser;
    private SiteModel siteModel;
    private FileModel fileModel;
    private TaskModel taskModel;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        assigneeUser = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Delete existing task variable")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    public void deleteTaskVariable() throws Exception
    {
        restClient.authenticateUser(adminUser);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");

        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables()
                .assertThat().entriesListDoesNotContain("name", variableModel.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Try to delete existing task variable using invalid task id")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void tryToDeleteTaskVariableUsingInvalidTaskId() throws Exception
    {
        restClient.authenticateUser(adminUser);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");

        restClient.withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        taskModel.setId("incorrectTaskId");
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Delete task variable with any user")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    public void deleteTaskVariableByAnyUser() throws Exception
    {
        restClient.authenticateUser(userModel);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");

        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables()
                .assertThat().entriesListDoesNotContain("name", variableModel.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task variable with invalid type")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskVariableInvalidType() throws Exception
    {
        restClient.authenticateUser(userModel);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        variableModel.setType("invalid-type");
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables()
                .       assertThat().entriesListDoesNotContain("name", variableModel.getName());
    }

//    The reason the test is not valid is because of `/` malforming the path so that it actually can't land on the URL of the webscript. The request with <>.,;|-+=% (without /) actually is parsed and 404 is thrown (no entity with id  <>.,;|-+=% is found)
//    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
//            description = "Delete task variable with invalid name")
//    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
//    public void deleteTaskVariableInvalidName() throws Exception
//    {
//        restClient.authenticateUser(userModel);
//        RestVariableModel variableModel = new RestVariableModel("local", "<>.,;/|-+=%", "d:text", "invalid name");
//        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
//        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
//    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Create, update, delete task variable with any user")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void createUpdateDeleteTaskVariableByAnyUser() throws Exception
    {
        restClient.authenticateUser(userModel);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");

        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        variableModel.setName("new-variable");
        restClient.withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables()
                .assertThat().entriesListDoesNotContain("name", variableModel.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task variable by non assigned user")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskVariableByNonAssignedUser() throws Exception
    {
        UserModel nonAssigned = dataUser.createRandomTestUser();
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");

        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.authenticateUser(nonAssigned).withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task variable by inexistent user")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void deleteTaskVariableByInexistentUser() throws Exception
    {
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.authenticateUser(UserModel.getRandomUserModel())
                .withWorkflowAPI()
                .usingTask(taskModel)
                .deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task variable twice")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskVariableTwice() throws Exception
    {
        restClient.authenticateUser(userModel);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables()
            .assertThat().entriesListDoesNotContain("name", variableModel.getName());
        
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
              .assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, variableModel.getName()))
                                .stackTraceIs(RestErrorModel.STACKTRACE)
                                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task variable with empty variable name")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskEmptyVariableName() throws Exception
    {
        restClient.authenticateUser(userModel);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");

        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        variableModel.setName("");
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
              .assertLastError().containsErrorKey(RestErrorModel.DELETE_EMPTY_ARGUMENT)
                                .containsSummary(RestErrorModel.DELETE_EMPTY_ARGUMENT)
                                .stackTraceIs(RestErrorModel.STACKTRACE)
                                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task variable with empty variable scope")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskEmptyVariableScope() throws Exception
    {
        restClient.authenticateUser(userModel);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");

        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        variableModel.setScope("");
        variableModel.setType("");
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables()
            .assertThat().entriesListDoesNotContain("name", variableModel.getName());
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task variable with invalid variable name")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void deleteTaskInvalidVariableName() throws Exception
    {
        restClient.authenticateUser(userModel);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");

        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        variableModel.setName("invalid-name");
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
              .assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalid-name"))
                                .stackTraceIs(RestErrorModel.STACKTRACE)
                                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
}
