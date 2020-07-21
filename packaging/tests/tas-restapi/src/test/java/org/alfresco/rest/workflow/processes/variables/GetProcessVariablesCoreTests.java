package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessModelsCollection;
import org.alfresco.rest.model.RestProcessVariableCollection;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetProcessVariablesCoreTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee, admin;
    private RestProcessModel processModel;
    private RestProcessVariableCollection variables;


    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        admin = dataUser.getAdminUser();
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process variables using invalid process ID")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesUsingInvalidProcessId() throws Exception
    {        
        restClient.authenticateUser(userWhoStartsTask).withParams("maxItems=2").withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        RestProcessModelsCollection processes = restClient.authenticateUser(userWhoStartsTask).withParams("maxItems=2").withWorkflowAPI().getProcesses();
        processModel  = processes.assertThat().entriesListIsNotEmpty().when().getOneRandomEntry().onModel();
        
        String id = RandomStringUtils.randomAlphanumeric(10);
        processModel.setId(id);
        variables = restClient.withParams("maxItems=2").withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, id))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process variables using empty process ID")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesUsingEmptyProcessId() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withParams("maxItems=2").withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processModel.setId("");
        variables = restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND).assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete process then get process variables, status OK should be returned")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessVariablesForADeletedProcess() throws Exception
    {
        UserModel userWhoStartsTask = dataUser.createRandomTestUser();
        UserModel assignee = dataUser.createRandomTestUser();

        RestProcessModel processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);

        restClient.authenticateUser(admin).withWorkflowAPI().usingProcess(processModel).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        RestProcessVariableCollection variables = restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        variables.assertThat().entriesListIsNotEmpty();
    }
}
