package org.alfresco.rest.workflow.tasks.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestVariableModel;
import org.alfresco.rest.model.RestVariableModelsCollection;
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


public class AddTaskVariablesTestsBulk3 extends RestTest
{
    private UserModel userWhoStartsTask, adminUser;
    private SiteModel siteModel;
    private FileModel fileModel;
    private UserModel assigneeUser;
    private TaskModel taskModel;
    private RestVariableModel restVariablemodel, variableModel, variableModel1;
    private RestVariableModelsCollection restVariableCollection;
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
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Create task variable with name containing symbols")
    public void createTaskVariableWithSymbolsInName() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        variableModel.setName("!@#$%^&*({}<>.,;'=_|");
        restVariablemodel = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restVariablemodel.assertThat()
             .field("scope").is(variableModel.getScope())
             .and().field("name").is(variableModel.getName())
             .and().field("value").is(variableModel.getValue())
             .and().field("type").is(variableModel.getType());
    }
    
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Create multiple task variable with name containing symbols")
    public void createMultipleTaskVariableWithSymbolsInName() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        variableModel.setName("!@#$%^&*({}<>.,;'=_|");
                
        restVariableCollection = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(variableModel,variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restVariableCollection.getEntries().get(0).onModel().assertThat()
                              .field("scope").is(variableModel.getScope())
                              .and().field("name").is(variableModel.getName())
                              .and().field("value").is(variableModel.getValue())
                              .and().field("type").is(variableModel.getType());

        restVariableCollection.getEntries().get(1).onModel().assertThat()
                              .field("scope").is(variableModel1.getScope())
                              .and().field("name").is(variableModel1.getName())
                              .and().field("value").is(variableModel1.getValue())
                              .and().field("type").is(variableModel1.getType());                      
    }
    
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Create task variable with empty name")
    public void createTaskVariableWithEmptyName() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        variableModel.setName("");
        restVariablemodel = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsErrorKey(RestErrorModel.VARIABLE_NAME_REQUIRED)
                              .containsSummary(RestErrorModel.VARIABLE_NAME_REQUIRED)
                              .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                              .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Update task variable with invalid variable name")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void createTaskVariableWithInvalidVariableName() throws Exception
    {            
        restClient.authenticateUser(adminUser).withWorkflowAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"scope\": \"local\",\"names\": \"varName\",\"value\": \"test\","
                + "\"type\": \"d:text\"}", "tasks/{taskId}/variables", taskId);
        restClient.processModel(RestVariableModel.class, request);
                       
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.NO_CONTENT,"Unrecognized field " + "\"names\"")); 
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Create task variable with invalid name")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    public void createTaskVariableWithInvalidName() throws Exception
    {
        restClient.authenticateUser(adminUser).withWorkflowAPI();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, "{\"scope\": \"local\",\"name\": ',\"value\": \"test\","
                + "\"type\": \"d:text\"}", "tasks/{taskId}/variables", taskId);
        restClient.processModel(RestVariableModel.class, request);
        
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
        .assertLastError()
        .containsSummary(String.format(RestErrorModel.NO_CONTENT,"Unexpected character " + "('''"));        
     }
    
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Create task variable with empty name")
    public void failedCreatingMultipleTaskVariableWithEmptyName() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        variableModel.setName("");
        
        restVariableCollection = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(variableModel, variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsErrorKey(RestErrorModel.VARIABLE_NAME_REQUIRED)
                              .containsSummary(RestErrorModel.VARIABLE_NAME_REQUIRED)
                              .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                              .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}
