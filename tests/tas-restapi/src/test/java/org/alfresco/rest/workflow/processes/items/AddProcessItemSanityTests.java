package org.alfresco.rest.workflow.processes.items;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestItemModel;
import org.alfresco.rest.model.RestItemModelsCollection;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author iulia.cojocea
 */
public class AddProcessItemSanityTests extends RestTest
{
    private FileModel document, document2;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee, adminUser;
    private RestProcessModel processModel;
    private RestItemModel processItem;
    private RestItemModelsCollection processItems;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(adminUser).createPublicRandomSite();
        document = dataContent.usingUser(adminUser).usingSite(siteModel).createContent(DocumentType.XML);
        dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Create non-existing process item")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY})
    public void addProcessItem() throws JsonToModelConversionException, Exception
    {
        document2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "This is a test file");
        dataContent.usingSite(siteModel).createContent(document2);

        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processItem.assertThat().field("createdAt").isNotEmpty()
                   .and().field("size").is(document2.getContent().length())
                   .and().field("createdBy").is(adminUser.getUsername())
                   .and().field("modifiedAt").isNotEmpty()
                   .and().field("name").is(document2.getName())
                   .and().field("modifiedBy").is(adminUser.getUsername())
                   .and().field("id").isNotEmpty()
                   .and().field("mimeType").is(document2.getFileType().mimeType);

        restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems()
                .assertThat().entriesListContains("id", processItem.getId());
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Create non-existing process item")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY})
    public void addMultipleProcessItem() throws JsonToModelConversionException, Exception
    {
        document2 = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "This is a test file");
        dataContent.usingSite(siteModel).createContent(document2);
        document = FileModel.getRandomFileModel(FileType.TEXT_PLAIN, "This is a test file");
        dataContent.usingSite(siteModel).createContent(document);
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processItems = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItems(document2, document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        processItems.getEntries().get(0).onModel()
                    .assertThat().field("createdAt").isNotEmpty()
                    .and().field("size").is(document2.getContent().length())
                    .and().field("createdBy").is(adminUser.getUsername())
                    .and().field("modifiedAt").isNotEmpty()
                    .and().field("name").is(document2.getName())
                    .and().field("modifiedBy").is(adminUser.getUsername())
                    .and().field("id").isNotEmpty()
                    .and().field("mimeType").is(document2.getFileType().mimeType);
        
        processItems.getEntries().get(1).onModel()
                    .assertThat().field("createdAt").isNotEmpty()
                    .and().field("size").is(document.getContent().length())
                    .and().field("createdBy").is(adminUser.getUsername())
                    .and().field("modifiedAt").isNotEmpty()
                    .and().field("name").is(document.getName())
                    .and().field("modifiedBy").is(adminUser.getUsername())
                    .and().field("id").isNotEmpty()
                    .and().field("mimeType").is(document.getFileType().mimeType);

        restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems()
                .assertThat().entriesListContains("id", processItems.getEntries().get(0).onModel().getId())
                .assertThat().entriesListContains("id", processItems.getEntries().get(1).onModel().getId());
    }

    @Bug(id= "REPO-1927")
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Add process item that already exists")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY})
    public void addProcessItemThatAlreadyExists() throws JsonToModelConversionException, Exception
    {
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);

        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processItem.assertThat().field("createdAt").isNotEmpty()
                .and().field("size").is("19")
                .and().field("createdBy").is(adminUser.getUsername())
                .and().field("modifiedAt").isNotEmpty()
                .and().field("name").is(document2.getName())
                .and().field("modifiedBy").is(adminUser.getUsername())
                .and().field("id").isNotEmpty()
                .and().field("mimeType").is(document2.getFileType().mimeType);

        restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems()
                .assertThat().entriesListContains("id", processItem.getId())
                .and().entriesListContains("name", document2.getName());
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }
    
    @Bug(id= "REPO-1927")
    @TestRail(section = {TestGroup.REST_API,TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Add process item that already exists")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY})
    public void addMultipleProcessItemThatAlreadyExists() throws JsonToModelConversionException, Exception
    {
        document2 = dataContent.usingSite(siteModel).createContent(DocumentType.XML);
        document = dataContent.usingSite(siteModel).createContent(DocumentType.XML);

        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processItems = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItems(document2, document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processItems.getEntries().get(0).onModel()
                    .assertThat().field("createdAt").isNotEmpty()
                    .and().field("size").is("19")
                    .and().field("createdBy").is(adminUser.getUsername())
                    .and().field("modifiedAt").isNotEmpty()
                    .and().field("name").is(document2.getName())
                    .and().field("modifiedBy").is(adminUser.getUsername())
                    .and().field("id").isNotEmpty()
                    .and().field("mimeType").is(document2.getFileType().mimeType);
        
        processItems.getEntries().get(1).onModel()
                    .assertThat().field("createdAt").isNotEmpty()
                    .and().field("size").is("19")
                    .and().field("createdBy").is(adminUser.getUsername())
                    .and().field("modifiedAt").isNotEmpty()
                    .and().field("name").is(document.getName())
                    .and().field("modifiedBy").is(adminUser.getUsername())
                    .and().field("id").isNotEmpty()
                    .and().field("mimeType").is(document.getFileType().mimeType);
                
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems()
                .assertThat().entriesListContains("id",  processItems.getEntries().get(0).onModel().getId())
                .and().entriesListContains("name", document2.getName())
                .assertThat().entriesListContains("id",  processItems.getEntries().get(1).onModel().getId())
                .and().entriesListContains("name", document.getName());
                
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItems(document2, document);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
    }
}
