package org.alfresco.rest.workflow.tasks.items;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestItemModel;
import org.alfresco.rest.model.RestItemModelsCollection;
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

public class AddTaskItemTestsBulk3 extends RestTest
{
    private UserModel userModel,userWhoStartsTask, assigneeUser;
    private SiteModel siteModel; 
    private FileModel fileModel, fileModel1, document1, document2,document3;
    private TaskModel taskModel;
    private RestItemModelsCollection taskItems;
    private RestItemModel taskItem;

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

    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Add task item using random user.")
    public void addTaskItemByTheUserThatStartedTheProcess() throws Exception
    {
        taskItem = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingTask(taskModel).addTaskItem(fileModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        taskItem.assertThat().field("createdAt").is(taskItem.getCreatedAt())
                .and().field("size").is(taskItem.getSize())
                .and().field("createdBy").is(taskItem.getCreatedBy())
                .and().field("modifiedAt").is(taskItem.getModifiedAt())
                .and().field("name").is(taskItem.getName())
                .and().field("modifiedBy").is(taskItem.getModifiedBy())
                .and().field("id").is(taskItem.getId())
                .and().field("mimeType").is(taskItem.getMimeType());
     }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION, 
    description = "Add multiple task item using random user.")
    public void addMultipleTaskItemByTheUserThatStartedTheProcess() throws Exception
    {
        fileModel1 = dataContent.usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        taskItems = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingTask(taskModel)
                             .addTaskItems(fileModel, fileModel1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        taskItems.getEntries().get(0).onModel()
                 .assertThat().field("createdAt").is(taskItems.getEntries().get(0).onModel().getCreatedAt())
                 .and().field("size").is(taskItems.getEntries().get(0).onModel().getSize())
                 .and().field("createdBy").is(taskItems.getEntries().get(0).onModel().getCreatedBy())
                 .and().field("modifiedAt").is(taskItems.getEntries().get(0).onModel().getModifiedAt())
                 .and().field("name").is(taskItems.getEntries().get(0).onModel().getName())
                 .and().field("modifiedBy").is(taskItems.getEntries().get(0).onModel().getModifiedBy())
                 .and().field("id").is(taskItems.getEntries().get(0).onModel().getId())
                 .and().field("mimeType").is(taskItems.getEntries().get(0).onModel().getMimeType());
        
        taskItems.getEntries().get(1).onModel()
                 .assertThat().field("createdAt").is(taskItems.getEntries().get(1).onModel().getCreatedAt())
                 .assertThat().field("size").is(taskItems.getEntries().get(1).onModel().getSize())
                 .assertThat().field("createdBy").is(taskItems.getEntries().get(1).onModel().getCreatedBy())
                 .assertThat().field("modifiedAt").is(taskItems.getEntries().get(1).onModel().getModifiedAt())
                 .assertThat().field("name").is(taskItems.getEntries().get(1).onModel().getName())
                 .assertThat().field("modifiedBy").is(taskItems.getEntries().get(1).onModel().getModifiedBy())
                 .assertThat().field("id").is(taskItems.getEntries().get(1).onModel().getId())
                 .assertThat().field("mimeType").is(taskItems.getEntries().get(1).onModel().getMimeType());    
     }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW,TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete task item then create it again")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION})
    public void deleteTaskItemThenCreateItAgain() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel)
                                 .createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(userWhoStartsTask);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItem);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Delete multiple task item then create it again")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION})
    public void deleteMultipleTaskItemsThenCreateThemAgain() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(fileModel)
                                 .createNewTaskAndAssignTo(assigneeUser);
        restClient.authenticateUser(userWhoStartsTask);
        document1 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        document3 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        taskItems = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItems(document2, document1, document3);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItems.getEntries().get(0).onModel());
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItems.getEntries().get(1).onModel());
        restClient.withWorkflowAPI().usingTask(taskModel).deleteTaskItem(taskItems.getEntries().get(2).onModel());
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        
        restClient.withWorkflowAPI().usingTask(taskModel).addTaskItems(document2, document1, document3);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
    }
}
