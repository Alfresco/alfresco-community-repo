package org.alfresco.rest.workflow.processes;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessModelsCollection;
import org.alfresco.utility.model.*;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

/**
 * 
 * @author Cristina Axinte
 *
 */
public class GetProcessesCoreTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel adminUser, userWhoStartsTask, assignee ;
    private TaskModel task1, task2;
    private ProcessModel process3;
    private RestProcessDefinitionModel adhocProcessDefinition;
    private RestProcessDefinitionModel activitiReviewProcessDefinition;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        task1 = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);  
        task2 = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(adminUser);
        process3 = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createSingleReviewerTaskAndAssignTo(assignee);
        adhocProcessDefinition = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getAllProcessDefinitions().getProcessDefinitionByKey("activitiAdhoc");
        activitiReviewProcessDefinition = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getAllProcessDefinitions().getProcessDefinitionByKey("activitiReview");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Verify user gets all processes started by him ordered descending by id")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessesOrderedByIdDESC() throws Exception
    {
        RestProcessModelsCollection processes = restClient.authenticateUser(userWhoStartsTask).withParams("orderBy=id DESC")
                .withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processes.assertThat().entriesListIsNotEmpty();
        List<RestProcessModel> processesList = processes.getEntries();
        processesList.get(0).onModel().assertThat().field("id").is(process3.getId());
        processesList.get(1).onModel().assertThat().field("id").is(task2.getProcessId());
        processesList.get(2).onModel().assertThat().field("id").is(task1.getProcessId());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user gets processes that matches a where clause")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessesWithWhereClauseAsParameter() throws JsonToModelConversionException, Exception
    {       
        RestProcessModelsCollection processes = restClient.authenticateUser(userWhoStartsTask).where("processDefinitionKey='activitiReview'")
                .withWorkflowAPI().getProcesses();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        
        processes.assertThat().entriesListIsNotEmpty().and().entriesListContains("processDefinitionId", activitiReviewProcessDefinition.getId())
                .and().entriesListDoesNotContain("processDefinitionId", adhocProcessDefinition.getId());
    }
}
