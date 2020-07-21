package org.alfresco.rest.workflow.processes.items;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.EmptyRestModelCollectionException;
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

public class DeleteProcessItemFullTests extends RestTest
{
    private FileModel document, document2;
    private SiteModel siteModel;
    private UserModel userWhoStartsProcess, assignee, adminUser, anotherUser;
    private ProcessModel processModel;
    private RestItemModelsCollection items;
    private RestItemModel processItem;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsProcess).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsProcess).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
              description = "Try to delete existing process item using empty processId")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemUsingEmptyProcessId() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI()
                .addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        document2 = dataContent.usingAdmin().usingSite(siteModel).createContent(DocumentType.MSPOWERPOINT);
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        processModel.setId("");
        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "The entity with id: "))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
             description = "Add a new process item, update the item and then delete.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void createUpdateDeleteProcessItem() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI()
                                 .addProcess("activitiAdhoc", assignee, false, Priority.Normal);  
        document2 = dataContent.usingAdmin().usingSite(siteModel).createContent(DocumentType.MSPOWERPOINT);
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        processItem.setName("newItemName");
        processItem.assertThat().field("name").is("newItemName");

        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems()
                  .assertThat().entriesListDoesNotContain("name", processItem.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
              description = "Delete process item using any user.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemByAnyUser() throws Exception
    {              
        anotherUser = dataUser.createRandomTestUser();

        document2 = dataContent.usingAdmin().usingSite(siteModel).createContent(DocumentType.HTML);
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(anotherUser).withWorkflowAPI().usingProcess(processModel)
                 .deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.ACCESS_INFORMATION_NOT_ALLOWED, processModel.getId()))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .containsErrorKey(String.format(RestErrorModel.ACCESS_INFORMATION_NOT_ALLOWED, processModel.getId()))
                  .stackTraceIs(RestErrorModel.STACKTRACE);;
    } 

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
             description = "Delete process item with admin.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemWithAdmin() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI()
                .addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        
        document2 = dataContent.usingAdmin().usingSite(siteModel).createContent(DocumentType.HTML);
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(adminUser).withWorkflowAPI().usingProcess(processModel)
                  .deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                  .assertThat().entriesListDoesNotContain("name", processItem.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
              description = "Delete process item by the user who is involved in the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemByUserInvolvedInTheProcess() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI()
                .addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(processModel)
                  .deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                  .assertThat().entriesListDoesNotContain("name", processItem.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
              description = "Delete process item by the user who started the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemByUserThatStartedTheProcess() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI()
                .addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingProcess(processModel)
                  .deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                  .assertThat().entriesListDoesNotContain("name", processItem.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
              description = "Delete process item for a deleted process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemsForDeletedProcess() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI()
                .addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, processModel.getId()))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
              description = "Delete process item by inexistent user.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemByInexistentUser() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI()
                .addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(UserModel.getRandomUserModel()).withWorkflowAPI().usingProcess(processModel)
                 .deleteProcessItem(processItem);

        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
              description = "Delete process item for process without items.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION}, 
          expectedExceptions = EmptyRestModelCollectionException.class)
    public void deleteProcessItemsForProcessWithoutItems() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI()
                .addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        
        items = restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        items.assertThat().entriesListIsEmpty();

        restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingProcess(processModel)
                  .deleteProcessItem(items.getOneRandomEntry());
    }
}
