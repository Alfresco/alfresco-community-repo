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
public class UpdateTaskVariableTestsBulk2 extends RestTest
{
    private UserModel userModel;
    private SiteModel siteModel;
    private FileModel fileModel;
    private UserModel assigneeUser;
    private TaskModel taskModel;
    private RestVariableModel taskVariable;
    private RestVariableModel variableModel;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        assigneeUser = dataUser.createRandomTestUser();
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Update task variable by user who started the process")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    public void updateTaskVariableByUserWhoStartedProcess() throws Exception
    {
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        taskVariable = restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setValue("new-value");
        variableModel.setName("new-name");
        taskVariable = restClient.authenticateUser(userModel).
                withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        taskVariable.assertThat().field("value").is("new-value")
            .and().field("name").is("new-name");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with symbols in name")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithSymbolsInName() throws Exception
    {
        String symbolName = "<>.,;-'+=%|[]#*&-+";
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        taskVariable = restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setName(symbolName);
        taskVariable = restClient.authenticateUser(userModel).
                withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        taskVariable.assertThat().field("name").is(symbolName);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with invalid task id")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithInvalidTaskId() throws Exception
    {
        TaskModel invalidTask = new TaskModel(userModel.getUsername());
        invalidTask.setId("invalid-task-id");
        taskVariable = restClient.authenticateUser(userModel).
                withWorkflowAPI().usingTask(invalidTask)
                .updateTaskVariable(RestVariableModel.getRandomTaskVariableModel("local", "d:text"));
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, invalidTask.getId()));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with invalid scope")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithInvalidScope() throws Exception
    {
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        taskVariable = restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setScope("invalid-scope");
        taskVariable = restClient.authenticateUser(userModel).
                withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsSummary(String.format(RestErrorModel.ILLEGAL_SCOPE, "invalid-scope"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with invalid type")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithInvalidType() throws Exception
    {
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        taskVariable = restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setType("d:invalidType");
        taskVariable = restClient.authenticateUser(userModel).
                withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsSummary(String.format(RestErrorModel.UNSUPPORTED_TYPE, "d:invalidType"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with symbols in value")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithSymbolsInValue() throws Exception
    {
        String symbolValue = "<>.,;-'+=%|[]#*&-+/\\#!@";
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        taskVariable = restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setValue(symbolValue);
        taskVariable = restClient.authenticateUser(userModel).
                withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        taskVariable.assertThat().field("value").is(symbolValue);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable by non assigned user")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableByNonAssignedUser() throws Exception
    {
        UserModel nonAssigned = dataUser.createRandomTestUser();
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        taskVariable = restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setName("new-name");
        taskVariable = restClient.authenticateUser(nonAssigned)
                .withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
            .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable by inexistent user")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void updateTaskVariableByNonexistentUser() throws Exception
    {
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        taskVariable = restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setName("new-name");
        taskVariable = restClient.authenticateUser(UserModel.getRandomUserModel())
                .withWorkflowAPI().usingTask(taskModel).updateTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

}