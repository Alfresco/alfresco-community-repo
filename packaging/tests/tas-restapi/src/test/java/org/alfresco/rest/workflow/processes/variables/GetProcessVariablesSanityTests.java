package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableCollection;
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
public class GetProcessVariablesSanityTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee;
    private RestProcessModel processModel;
    private RestProcessVariableCollection variables;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Verify that user that started the process gets all process variables")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void getProcessVariablesUsingTheUserWhoStartedProcess() throws JsonToModelConversionException, Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        variables = restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variables.assertThat().entriesListIsNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Verify get all process variables call using a user that is involved in the process")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void getProcessVariablesUsingUserInvolvedInProcess() throws JsonToModelConversionException, Exception
    {
        processModel = restClient.authenticateUser(assignee).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        variables = restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variables.assertThat().entriesListIsNotEmpty();
    }
}
