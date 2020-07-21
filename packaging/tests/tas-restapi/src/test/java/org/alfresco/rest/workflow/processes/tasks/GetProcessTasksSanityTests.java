package org.alfresco.rest.workflow.processes.tasks;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestTaskModelsCollection;
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

/**
 * Tests for GET "/processes/{processId}/tasks" REST API call
 *
 * @author Cristina Axinte
 */
public class GetProcessTasksSanityTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userModel, assignee1, assignee2, assignee3;
    private ProcessModel process;
    private RestTaskModelsCollection processTasks;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        assignee1 = dataUser.createRandomTestUser();
        assignee2 = dataUser.createRandomTestUser();
        assignee3 = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        document = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        process = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(document)
                .createMoreReviewersWorkflowAndAssignTo(assignee1, assignee2, assignee3);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY, 
            description = "Verify user who started the process gets all tasks of started process with Rest API and response is successfull (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void userWhoStartedProcessCanGetProcessTasks() throws JsonToModelConversionException, Exception
    {
        processTasks = restClient.authenticateUser(userModel).withWorkflowAPI().usingProcess(process).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processTasks.assertThat()
            .entriesListCountIs(3).and()
            .entriesListContains("assignee", assignee1.getUsername()).and()
            .entriesListContains("assignee", assignee2.getUsername()).and()
            .entriesListContains("assignee", assignee3.getUsername());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY, 
            description = "Verify any assignee user of the process gets all tasks of the process with Rest API and response is successfull (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void assigneeUserCanGetAllProcessTasks() throws JsonToModelConversionException, Exception
    {
        processTasks = restClient.authenticateUser(assignee1).withWorkflowAPI().usingProcess(process).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processTasks.assertThat()
            .entriesListContains("assignee", assignee1.getUsername()).and()
            .entriesListContains("assignee", assignee2.getUsername()).and()
            .entriesListContains("assignee", assignee3.getUsername());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.SANITY, 
            description = "Verify any assignee user of the process gets all tasks of the process with Rest API and response is successfull (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void involvedUserCanGetAllProcessTasks() throws JsonToModelConversionException, Exception
    {
        ProcessModel process = dataWorkflow.usingUser(userModel).usingSite(siteModel).usingResource(document)
                .createMoreReviewersWorkflowAndAssignTo(assignee1, assignee2, assignee3);
        dataWorkflow.usingUser(assignee1).approveTask(process);

        processTasks = restClient.authenticateUser(assignee2).withWorkflowAPI().usingProcess(process).getProcessTasks();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processTasks.assertThat()
            .entriesListIsNotEmpty().assertThat()
            .entriesListContains("assignee", userModel.getUsername());
    }
}
