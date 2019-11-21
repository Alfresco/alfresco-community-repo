package org.alfresco.rest.workflow.tasks.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestVariableModel;
import org.alfresco.rest.model.RestVariableModelsCollection;
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
 * @author iulia.cojocea
 */

public class AddTaskVariablesTestsBulk1 extends RestTest
{
    private UserModel userModel, userWhoStartsTask;
    private SiteModel siteModel;
    private FileModel fileModel;
    private UserModel assigneeUser;
    private TaskModel taskModel;
    private RestVariableModel restVariablemodel;
    private RestVariableModelsCollection restVariableCollection;
    private RestVariableModel variableModel, variableModel1;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        userWhoStartsTask = dataUser.createRandomTestUser();
        assigneeUser = dataUser.createRandomTestUser();
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel).createNewTaskAndAssignTo(assigneeUser);
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create non-existing task variable with admin")
    public void createTaskVariableWithAdmin() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
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
            description = "Create non-existing task variable with admin")
    public void createMultipleTaskVariablesWithAdmin() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        
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

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create non-existing task variable with user involved in the process")
    public void createTaskVariableWithInvolvedUser() throws Exception
    {
        restClient.authenticateUser(assigneeUser);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restVariablemodel = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restVariablemodel.assertThat().field("scope").is(variableModel.getScope())
             .and().field("name").is(variableModel.getName())
             .and().field("value").is(variableModel.getValue())
             .and().field("type").is(variableModel.getType());
        
        restClient.withWorkflowAPI().usingTask(taskModel).getTaskVariables().assertThat().entriesListContains("name", variableModel.getName());
    }
    
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Create multiple non-existing task variable with involved user")
    public void createMultipleTaskVariableWithInvolvedUser() throws Exception
    {
        restClient.authenticateUser(assigneeUser);
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        
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

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create non-existing task variable with task owner")
    public void createTaskVariableWithTaskOwner() throws Exception
    {
        restClient.authenticateUser(userWhoStartsTask);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restVariablemodel = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restVariablemodel.assertThat().field("scope").is(variableModel.getScope())
             .and().field("name").is(variableModel.getName())
             .and().field("value").is(variableModel.getValue())
             .and().field("type").is(variableModel.getType());
    }
    
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Create multiple non-existing task variable with task owner")
    public void createMultipleTaskVariableWithTaskOwner() throws Exception
    {
        restClient.authenticateUser(userWhoStartsTask);
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        
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
            description = "Create non-existing task variable with any user")
    public void createTaskVariableWithRandomUser() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        restClient.authenticateUser(userModel);
        RestVariableModel variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        
        restVariablemodel = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary("Permission was denied");
    }
    
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Create multiple non-existing task variable with any user")
    public void createMultipleTaskVariableWithRandomUser() throws Exception
    {
        restClient.authenticateUser(userModel);
        variableModel = RestVariableModel.getRandomTaskVariableModel("local", "d:text");
        variableModel1 = RestVariableModel.getRandomTaskVariableModel("global", "d:text");
        
        restVariableCollection = restClient.withWorkflowAPI().usingTask(taskModel).addTaskVariables(variableModel,variableModel1);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN).assertLastError().containsSummary("Permission was denied");
    }
    
}
