package org.alfresco.rest.workflow.tasks.variables;

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


public class AddTaskVariablesTestsBulk2 extends RestTest
{
    private UserModel userWhoStartsTask, adminUser;
    private SiteModel siteModel;
    private FileModel fileModel;
    private UserModel assigneeUser;
    private TaskModel taskModel;
    private RestVariableModel invalidVariableModel, variableModel, variableModel1;
    
    private String taskId;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsTask = dataUser.createRandomTestUser();
        assigneeUser = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(adminUser).createPublicRandomSite();
        fileModel = dataContent.usingUser(adminUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);

        taskId = taskModel.getId();
        restClient.authenticateUser(adminUser);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Adding task variable is falling in case invalid variableBody is provided")
    public void failedAddingTaskVariableIfInvalidBodyIsProvided() throws Exception
    {
        invalidVariableModel = RestVariableModel.getRandomTaskVariableModel("instance", "d:char");
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(invalidVariableModel);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Illegal value for variable scope: 'instance'.");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
     description = "Adding multiple task variable is falling in case invalid variableBody is provided")
    public void failedAddingMultipleTaskVariableIfInvalidBodyIsProvided() throws Exception
    {               
        variableModel = RestVariableModel.getRandomTaskVariableModel("instance", "d:char");
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(variableModel, variableModel1);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Illegal value for variable scope: 'instance'.");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Adding task variable is falling in case empty body type is provided")
    public void failedAddingTaskVariableIfEmptyBodyIsProvided() throws Exception
    {
        RestVariableModel invalidVariableModel = RestVariableModel.getRandomTaskVariableModel("", "");
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(invalidVariableModel);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary("Variable scope is required and can only be 'local' or 'global'.");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Adding multiple task variable is falling in case empty body type is provided")
    public void failedAddingMultipleTaskVariableIfEmptyBodyIsProvided() throws Exception
    {
        RestVariableModel invalidVariableModel = RestVariableModel.getRandomTaskVariableModel("", "");
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(invalidVariableModel, variableModel1);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary("Variable scope is required and can only be 'local' or 'global'.");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Adding task variable is falling in case incomplete body type is provided")
    public void failedAddingTaskVariableIfIncompleteBodyIsProvided() throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"name\": \"missingVariableScope\",\"value\": \"test\",\"type\": \"d:text\"}",
                "tasks/{taskId}/variables", taskId);
        restClient.processModel(RestVariableModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary("Variable scope is required and can only be 'local' or 'global'.");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Adding task variable is falling in case incomplete body - missing required: name type is provided")
    public void failedAddingTaskVariableIfIncompleteRequiredBodyIsProvided() throws Exception
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"scope\": \"local\",\"value\": \"missingVariableName\",\"type\": \"d:text\"}",
                "tasks/{taskId}/variables", taskId);
        restClient.processModel(RestVariableModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Variable name is required.");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Adding task variable is falling in case invalid type is provided")
    public void failedAddingTaskVariableIfInvalidTypeIsProvided() throws Exception
    {
        RestVariableModel invalidVariableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:char");
        taskModel.setId(taskId);
        
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(invalidVariableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Unsupported type of variable: 'd:char'.");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
      description = "Adding multiple task variable is falling in case invalid type is provided")
    public void failedAddingMultipleTaskVariableIfInvalidTypeIsProvided() throws Exception
    {
        RestVariableModel invalidVariableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:char");
        taskModel.setId(taskId);
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(invalidVariableModel, variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Unsupported type of variable: 'd:char'.");
    }

    @Bug(id = "ACE-5674")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Adding task variable is falling in case invalid type prefix is provided")
    public void failedAddingTaskVariableIfInvalidTypePrefixIsProvided() throws Exception
    {
        RestVariableModel invalidVariableModel = RestVariableModel.getRandomTaskVariableModel("local", "ddm:text");
        taskModel.setId(taskId);
        
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(invalidVariableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Namespace prefix ddm is not mapped to a namespace URI");
    }
    
    @Bug(id = "ACE-5674")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Adding multiple task variable is falling in case invalid type prefix is provided")
    public void failedAddingMultipleTaskVariableIfInvalidTypePrefixIsProvided() throws Exception
    {
        RestVariableModel invalidVariableModel = RestVariableModel.getRandomTaskVariableModel("local", "ddm:text");
        taskModel.setId(taskId);
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(invalidVariableModel,variableModel1, variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Namespace prefix ddm is not mapped to a namespace URI");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Adding task variable is falling in case invalid scope is provided")
    public void failedAddingTaskVariableIfInvalidScopeIsProvided() throws Exception
    {
        RestVariableModel invalidVariableModel = RestVariableModel.getRandomTaskVariableModel("instance", "d:text");
        taskModel.setId(taskId);
        
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(invalidVariableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Illegal value for variable scope: 'instance'.");
    }    

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Adding multiple task variable is falling in case invalid scope is provided")
    public void failedAddingMultipleTaskVariableIfInvalidScopeIsProvided() throws Exception
    {
        RestVariableModel invalidVariableModel = RestVariableModel.getRandomTaskVariableModel("instance", "d:text");
        taskModel.setId(taskId);
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(invalidVariableModel, variableModel, variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("Illegal value for variable scope: 'instance'.");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Adding task variable is falling in case invalid task id is provided")
    public void failedAddingTaskVariableIfInvalidTaskIdIsProvided() throws Exception
    {
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        taskModel.setId(taskModel.getId() + "TEST");
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, taskModel.getId()));
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
      description = "Adding multiple task variable is falling in case invalid task id is provided")
    public void failedAddingMultipleTaskVariableIfInvalidTaskIdIsProvided() throws Exception
    {
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        taskModel.setId(taskModel.getId() + "TEST");
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(variableModel, variableModel1);

        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, taskModel.getId()));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, description = "Adding task variable is falling in case invalid task id is provided")
    public void failedAddingTaskVariableIfInvalidValueIsProvided() throws Exception
    {
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:int");
        variableModel.setValue("invalidValue");

        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("For input string: \"invalidValue\"");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Adding multiple task variable is falling in case invalid task id is provided")
    public void failedAddingMultipleTaskVariableIfInvalidValueIsProvided() throws Exception
    {
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:int");
        variableModel.setValue("invalidValue");
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");

        restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(variableModel, variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary("For input string: \"invalidValue\"");
    }
}
