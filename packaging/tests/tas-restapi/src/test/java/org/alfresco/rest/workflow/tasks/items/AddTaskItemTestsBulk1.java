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
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class AddTaskItemTestsBulk1 extends RestTest
{
    private UserModel userModel, userWhoStartsTask, assigneeUser;
    private SiteModel siteModel; 
    private FileModel fileModel, document2, document3;
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

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create non-existing task item")
    public void createTaskItem() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        taskItem.assertThat().field("createdAt").is(taskItem.getCreatedAt())
            .assertThat().field("size").is(taskItem.getSize())
            .assertThat().field("createdBy").is(taskItem.getCreatedBy())
            .assertThat().field("modifiedAt").is(taskItem.getModifiedAt())
            .assertThat().field("name").is(taskItem.getName())
            .assertThat().field("modifiedBy").is(taskItem.getModifiedBy())
            .assertThat().field("id").is(taskItem.getId())
            .assertThat().field("mimeType").is(taskItem.getMimeType());    
   }
    
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.SANITY })
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.SANITY,
            description = "Create multiple non-existing task item")
    public void createMultipleTaskItem() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
                
        taskItems = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItems(document2, fileModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        taskItems.getEntries().get(0).onModel()
                 .assertThat().field("createdAt").is(taskItems.getEntries().get(0).onModel().getCreatedAt())
                 .assertThat().field("size").is(taskItems.getEntries().get(0).onModel().getSize())
                 .assertThat().field("createdBy").is(taskItems.getEntries().get(0).onModel().getCreatedBy())
                 .assertThat().field("modifiedAt").is(taskItems.getEntries().get(0).onModel().getModifiedAt())
                 .assertThat().field("name").is(taskItems.getEntries().get(0).onModel().getName())
                 .assertThat().field("modifiedBy").is(taskItems.getEntries().get(0).onModel().getModifiedBy())
                 .assertThat().field("id").is(taskItems.getEntries().get(0).onModel().getId())
                 .assertThat().field("mimeType").is(taskItems.getEntries().get(0).onModel().getMimeType());
        
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

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @Bug(id = "MNT-16966")
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that in case task item exists the request fails")
    public void createTaskItemThatAlreadyExists() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        document3 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document3);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        taskItem.assertThat().field("createdAt").is(taskItem.getCreatedAt())
            .assertThat().field("size").is(taskItem.getSize())
            .and().field("createdBy").is(taskItem.getCreatedBy())
            .and().field("modifiedAt").is(taskItem.getModifiedAt())
            .and().field("name").is(taskItem.getName())
            .and().field("modifiedBy").is(taskItem.getModifiedBy())
            .and().field("id").is(taskItem.getId())
            .and().field("mimeType").is(taskItem.getMimeType());
        
        taskItem = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItem(document3);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }
    
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @Bug(id = "MNT-16966")
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify that in case task item exists the request fails")
    public void createMultipleTaskItemThatAlreadyExists() throws Exception
    {
        UserModel adminUser = dataUser.getAdminUser();
        restClient.authenticateUser(adminUser);
        document3 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        
        taskItems = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItems(document3, document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        taskItems.getEntries().get(0).onModel()
                 .assertThat().field("createdAt").is(taskItems.getEntries().get(0).onModel().getCreatedAt())
                 .assertThat().field("size").is(taskItems.getEntries().get(0).onModel().getSize())
                 .assertThat().field("createdBy").is(taskItems.getEntries().get(0).onModel().getCreatedBy())
                 .assertThat().field("modifiedAt").is(taskItems.getEntries().get(0).onModel().getModifiedAt())
                 .assertThat().field("name").is(taskItems.getEntries().get(0).onModel().getName())
                 .assertThat().field("modifiedBy").is(taskItems.getEntries().get(0).onModel().getModifiedBy())
                 .assertThat().field("id").is(taskItems.getEntries().get(0).onModel().getId())
                 .assertThat().field("mimeType").is(taskItems.getEntries().get(0).onModel().getMimeType());
        
        taskItems.getEntries().get(1).onModel()
                 .assertThat().field("createdAt").is(taskItems.getEntries().get(1).onModel().getCreatedAt())
                 .assertThat().field("size").is(taskItems.getEntries().get(1).onModel().getSize())
                 .assertThat().field("createdBy").is(taskItems.getEntries().get(1).onModel().getCreatedBy())
                 .assertThat().field("modifiedAt").is(taskItems.getEntries().get(1).onModel().getModifiedAt())
                 .assertThat().field("name").is(taskItems.getEntries().get(1).onModel().getName())
                 .assertThat().field("modifiedBy").is(taskItems.getEntries().get(1).onModel().getModifiedBy())
                 .assertThat().field("id").is(taskItems.getEntries().get(1).onModel().getId())
                 .assertThat().field("mimeType").is(taskItems.getEntries().get(1).onModel().getMimeType());  
                       
        taskItems = restClient.withWorkflowAPI().usingTask(taskModel).addTaskItems(document3, document2);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }
}