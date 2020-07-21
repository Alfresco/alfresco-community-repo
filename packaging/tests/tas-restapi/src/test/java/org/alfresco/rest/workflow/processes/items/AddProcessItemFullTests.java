package org.alfresco.rest.workflow.processes.items;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestItemModel;
import org.alfresco.rest.model.RestItemModelsCollection;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.ProcessModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddProcessItemFullTests  extends RestTest
{
    private FileModel document, document2;
    private SiteModel siteModel;
    private UserModel userWhoStartsProcess, assignee, adminUser;
    private ProcessModel processModel;
    private RestItemModel processItem;
    private RestItemModelsCollection processItems;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(adminUser).createPublicRandomSite();
        document = dataContent.usingUser(adminUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        document2 = dataContent.usingUser(adminUser).usingSite(siteModel).createContent(DocumentType.MSPOWERPOINT);
        dataWorkflow.usingUser(userWhoStartsProcess).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding multiple process items is falling in case of empty process id is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingMultipleProcessItemsIfEmptyProcessIdIsProvided() throws Exception
    {
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();    
        FileModel testDocument = dataContent.usingAdmin().usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        
        processModel.setId("");
        processItems = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItems(testDocument, document);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_WAS_NOT_FOUND, ""));
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process items is falling in case of empty process id is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessItemsIfEmptyProcessIdIsProvided() throws Exception
    {
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();    
        FileModel testDocument = dataContent.usingAdmin().usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        
        processModel.setId("");
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(testDocument);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError()
                .containsSummary(String.format(RestErrorModel.ENTITY_WAS_NOT_FOUND, ""));
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Adding process item is falling in case of empty body item value is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessItemIfEmptyItemBodyIsProvided() throws Exception
    {
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();    
        document.setNodeRef("");
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.REQUIRED_TO_ADD, "itemId"));
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Adding multiple process items is falling in case of empty body item value is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingMultipleProcessItemsIfEmptyItemBodyIsProvided() throws Exception
    {
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        document.setNodeRef("");
        
        processItems = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItems(document, document2);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError().containsSummary(String.format(RestErrorModel.REQUIRED_TO_ADD, "itemId"));
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Add a new process item, update the item and then delete.")
   @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
   public void createDeleteCreateMultipleProcessItems() throws Exception
   {
       processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
       document2 = dataContent.usingAdmin().usingSite(siteModel).createContent(DocumentType.MSPOWERPOINT);
       processItems = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItems(document, document2);
       restClient.assertStatusCodeIs(HttpStatus.CREATED);

       restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItems.getEntries().get(0).onModel());
       
       restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
       restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems()
                 .assertThat().entriesListDoesNotContain("name", processItems.getEntries().get(0).onModel().getName());
       
       processItems = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItems(document);
       restClient.assertStatusCodeIs(HttpStatus.CREATED);      
   }
    
    @TestRail(section = { TestGroup.REST_API,TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Add a new process item, delete the item and create it again.")
   @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
   public void createDeleteCreateProcessItem() throws Exception
   {
       processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
       processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem( document2);
       restClient.assertStatusCodeIs(HttpStatus.CREATED);

       restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem);
       restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
       restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems()
                 .assertThat().entriesListDoesNotContain("name", processItem.getName());
       
       processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
       restClient.assertStatusCodeIs(HttpStatus.CREATED);
       restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems()
       .assertThat().entriesListContains("name", processItem.getName()); 
   }

}
