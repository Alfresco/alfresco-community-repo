package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class UpdateProcessVariableTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsProcess, assignee;
    private RestProcessModel processModel;
    private RestProcessVariableModel variableModel, updatedVariable;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {        
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsProcess).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsProcess).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsProcess).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Update existing variable using PUT call - invalid type")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void updateProcessVariableInvalidType() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setType("dd");
        
        updatedVariable = restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(String.format(RestErrorModel.UNSUPPORTED_TYPE, "dd"))
                  .containsSummary(String.format(RestErrorModel.UNSUPPORTED_TYPE, "dd"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);        
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Update existing variable using PUT call - invalid type")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void updateProcessVariableTwoUsers() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:any");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
       
        variableModel.setType("d:text");
        updatedVariable = restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(processModel)
                                     .updateProcessVariable(variableModel);    
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedVariable.assertThat().field("type").is("d:text");
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Update existing variable using PUT call - value")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void updateProcessVariableValue() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setValue("newValue");
        
        updatedVariable = restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedVariable.assertThat().field("value").is("newValue");
    }
    
    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Update existing variable using PUT call - name")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void updateProcessVariableName() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setName("newVariableName");
        
        updatedVariable = restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedVariable.assertThat().field("name").is("newVariableName");
    }
    
    @TestRail(section = {TestGroup.REST_API,TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Update existing variable created by the user who started the process using put call with a user that is not involved in the process- value")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void cantUpdateProcessVariableValueCreatedWithAnyUser() throws Exception
    {
        UserModel anotherUser = dataUser.createRandomTestUser();      
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        variableModel.setValue("newValue");
        
        restClient.authenticateUser(anotherUser).withWorkflowAPI().usingProcess(processModel)
                                    .updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
        .assertLastError().containsSummary(String.format(RestErrorModel.ACCESS_INFORMATION_NOT_ALLOWED, processModel.getId()));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Try to add process variable using an invalid processId")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void updateProcessVariableUsingInvalidProcessId() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        processModel.setId("abc");
        
        restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "abc"));
    }
}
