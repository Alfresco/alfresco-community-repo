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
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UpdateTaskVariableTestsBulk3 extends RestTest
{
    private SiteModel siteModel;
    private FileModel fileModel;
    private TaskModel taskModel;
    private UserModel userModel;
    private RestVariableModel taskVariable, updatedTaskVariable;  

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        taskModel = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(userModel);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with invalid name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithInvalidVariableName() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"scope\": \"local\",\"names\": \"varName\",\"value\": \"test\","
                + "\"type\": \"d:text\"}",
                              "tasks/{taskId}/variables/{variableName}", taskModel.getId(), taskVariable.getName());
        restClient.processModel(RestVariableModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.NO_CONTENT,"Unrecognized field " + "\"names\"")); 
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with invalid name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithInvalidVariableValue() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"scope\": \"local\",\"name\": \"varName\",\"values\": \"test\","
                + "\"type\": \"d:text\"}",
                              "tasks/{taskId}/variables/{variableName}", taskModel.getId(), taskVariable.getName());
        restClient.processModel(RestVariableModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.NO_CONTENT,"Unrecognized field " + "\"values\"")); 
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with invalid name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithEmptyVariableName() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"scope\": \"local\",\"\": \"varName\",\"value\": \"test\","
                + "\"type\": \"d:text\"}",
                              "tasks/{taskId}/variables/{variableName}", taskModel.getId(), taskVariable.getName());
        restClient.processModel(RestVariableModel.class, request);     
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.NO_CONTENT,"Unrecognized field " + "\"\"")); 
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with empty name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithEmptyName() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        taskVariable.setName("");        
        restClient.withWorkflowAPI().usingTask(taskModel).updateTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                  .assertLastError().containsErrorKey(RestErrorModel.PUT_EMPTY_ARGUMENT)
                  .containsSummary(RestErrorModel.PUT_EMPTY_ARGUMENT)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with empty name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithInvalidName() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"scope\": \"local\",\"name\": ',\"value\": \"test\","
                + "\"type\": \"d:text\"}",
                              "tasks/{taskId}/variables/{variableName}", taskModel.getId(), taskVariable.getName());
        restClient.processModel(RestVariableModel.class, request); 
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.NO_CONTENT,"Unexpected character " + "('''")); 
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with empty name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithInvalidValue() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"scope\": \"local\",\"name\": \"varName\",\"value\"::,"
                + "\"type\": \"d:text\"}",
                              "tasks/{taskId}/variables/{variableName}", taskModel.getId(), taskVariable.getName());
        restClient.processModel(RestVariableModel.class, request); 
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.NO_CONTENT,"Unexpected character " + "(':'"));      
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with empty name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithMissingType() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"scope\": \"local\",\"name\": \"varName\",\"value\": \"test\"}",
                              "tasks/{taskId}/variables/{variableName}", taskModel.getId(), taskVariable.getName());
        restClient.processModel(RestVariableModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        taskVariable.assertThat().field("scope").is(taskVariable.getScope())
                    .and().field("name").is(taskVariable.getName())
                    .and().field("type").is("d:text")
                    .and().field("value").is(taskVariable.getValue());   
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with empty name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithMissingTypeAndValue() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"scope\": \"local\",\"name\": \"varName\"}",
                              "tasks/{taskId}/variables/{variableName}", taskModel.getId(), taskVariable.getName());
        updatedTaskVariable = restClient.processModel(RestVariableModel.class, request);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedTaskVariable.assertThat().field("scope").is(updatedTaskVariable.getScope())
                    .and().field("name").is(updatedTaskVariable.getName())
                    .and().field("type").is("d:any");                   
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with invalid name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithEmptyBody() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{}",
                              "tasks/{taskId}/variables/{variableName}", taskModel.getId(), taskVariable.getName());
        restClient.processModel(RestVariableModel.class, request);        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError().containsErrorKey(RestErrorModel.VARIABLE_NAME_REQUIRED)
                  .containsSummary(RestErrorModel.VARIABLE_NAME_REQUIRED)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with invalid name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTaskVariableWithInvalidBody() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel)
                .withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"scope\": \"local\",\"name\": \"varName\",\"value\": \"test\","
                + "\"type\": \"d:text\", \"errorKey\": \"invalidBody\"}",
                              "tasks/{taskId}/variables/{variableName}", taskModel.getId(), taskVariable.getName());
        restClient.processModel(RestVariableModel.class, request);        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.NO_CONTENT,"Unrecognized field " + "\"errorKey\"")); 
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with empty name - PUT call")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void updateTwiceInARowSameTaskVariable() throws Exception
    {
        taskVariable = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).addTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        taskVariable.setName("newName");    
        taskVariable.setScope("global");
        updatedTaskVariable = restClient.withWorkflowAPI().usingTask(taskModel).updateTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedTaskVariable.assertThat().field("scope").is("global");
        updatedTaskVariable.assertThat().field("name").is("newName");
        
        updatedTaskVariable = restClient.withWorkflowAPI().usingTask(taskModel).updateTaskVariable(taskVariable);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedTaskVariable.assertThat().field("scope").is("global");
        updatedTaskVariable.assertThat().field("name").is("newName");        
    }
}
