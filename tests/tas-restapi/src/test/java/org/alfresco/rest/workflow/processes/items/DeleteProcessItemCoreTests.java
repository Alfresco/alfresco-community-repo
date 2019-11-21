package org.alfresco.rest.workflow.processes.items;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestItemModel;
import org.alfresco.rest.model.RestItemModelsCollection;
import org.alfresco.rest.model.RestProcessModel;
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

public class DeleteProcessItemCoreTests extends RestTest
{
    private FileModel document, secondDoc;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee;
    private RestProcessModel restProcessModel;
    private ProcessModel processModel;
    private RestItemModelsCollection items;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        processModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document)
            .createSingleReviewerTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete process item with invalid id")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemWithInvalidItemId() throws Exception
    {
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        secondDoc = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.MSWORD);
        RestItemModel processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(secondDoc);
        processItem.setId("invalid-id");
        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.PROCESS_ENTITY_NOT_FOUND, "invalid-id"));
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete process item with empty id")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemWithEmptyItemId() throws Exception
    {
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        secondDoc = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.MSWORD);
        RestItemModel processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(secondDoc);
        processItem.setId("");
        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
            .assertLastError().containsSummary(String.format(RestErrorModel.DELETE_EMPTY_ARGUMENT));
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete process item twice")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessItemTwice() throws Exception
    {
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        items = restClient.withWorkflowAPI().usingProcess(restProcessModel).getProcessItems();
        RestItemModel processItem = items.getEntries().get(0);
        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem.onModel());
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem.onModel());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.PROCESS_ENTITY_NOT_FOUND, processItem.onModel().getId()));
    }
}
