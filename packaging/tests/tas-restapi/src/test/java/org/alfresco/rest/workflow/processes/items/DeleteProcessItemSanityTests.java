package org.alfresco.rest.workflow.processes.items;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestItemModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
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
public class DeleteProcessItemSanityTests extends RestTest
{
    private FileModel document, document2;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee, adminUser;
    private RestProcessModel processModel;
    private RestItemModel processItem;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(adminUser).createPublicRandomSite();
        document = dataContent.usingUser(adminUser).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API,TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Delete existing process item")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void deleteProcessItem() throws Exception
    {
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        document2 = dataContent.usingAdmin().usingSite(siteModel).createContent(DocumentType.MSWORD);
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems()
                .assertThat().entriesListDoesNotContain("id", processItem.getId());
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Try to delete existing process item using invalid processId")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void deleteProcessItemUsingInvalidProcessId() throws Exception
    {
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        document2 = dataContent.usingAdmin().usingSite(siteModel).createContent(DocumentType.MSPOWERPOINT);
        processItem = restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processModel.setId("incorrectProcessId");
        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessItem(processItem);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }
}
