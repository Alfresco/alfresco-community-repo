package org.alfresco.rest.workflow.processes;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestProcessModelsCollection;
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
 * Created by Claudia Agache on 10/11/2016.
 */
public class GetProcessesSanityTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee, anotherUser;
    private TaskModel task;
    private RestProcessModelsCollection allProcesses;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        anotherUser = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        task = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY, 
            description = "Verify User gets all processes started by him using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void getProcessesByUserWhoStartedProcess() throws Exception
    {
        allProcesses = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        allProcesses.assertThat().entriesListContains("id", task.getProcessId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY, 
            description = "Verify User gets all processes assigned to him using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void getProcessesByAssignedUser() throws Exception
    {
        allProcesses = restClient.authenticateUser(assignee).withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        allProcesses.assertThat().entriesListContains("id", task.getProcessId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY, 
            description = "Verify User that is not involved in a process can not get that process using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void getProcessesByAnotherUser() throws Exception
    {
        allProcesses = restClient.authenticateUser(anotherUser).withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        allProcesses.assertThat().entriesListDoesNotContain("id", task.getProcessId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY, 
            description = "Verify Admin gets all processes, even if he isn't involved in a process, using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void getProcessesByAdmin() throws Exception
    {
        allProcesses = restClient.authenticateUser(dataUser.getAdminUser()).withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        allProcesses.assertThat().entriesListContains("id", task.getProcessId());
    }
}
