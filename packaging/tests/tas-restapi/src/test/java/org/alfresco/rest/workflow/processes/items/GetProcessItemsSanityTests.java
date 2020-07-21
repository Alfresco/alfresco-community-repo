package org.alfresco.rest.workflow.processes.items;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestItemModelsCollection;
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
public class GetProcessItemsSanityTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee;
    private RestProcessModel processModel;
    private RestItemModelsCollection items;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Verify that user that started the process gets all process items")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void getProcessItemsUsingTheUserWhoStartedProcess() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        items = restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        items.assertThat().entriesListIsNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Verify that user that is involved in the process gets all process items")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void getProcessItemsUsingUserInvolvedInProcess() throws Exception
    {
        processModel = restClient.authenticateUser(assignee).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        items = restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        items.assertThat().entriesListIsNotEmpty();
    }
}
