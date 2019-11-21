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

public class AddProcessVariablesCoreTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsProcess, assignee;
    private RestProcessModel processModel;
    private RestProcessVariableModel variableModel, processVariable, variableModel1;


    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsProcess).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsProcess).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsProcess).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }    

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify addProcessVariable by any user for invalid processID with REST API and status code is NOT_FOUND (404)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addProcessVariableForInvalidProcessIdIsNotFound() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processModel.setId("invalidProcessID");

        processVariable = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingProcess(processModel)
                                    .addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidProcessID"));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify addProcessVariable by any user for empty processID with REST API and status code is NOT_FOUND (404)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addProcessVariableForEmptyProcessIdIsEmpty() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processModel.setId("");

        processVariable = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingProcess(processModel)
                                    .addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding multiple process variables is falling in case invalid process id is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addMultipleProcessVariablesInvalidProcessId() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:int");
        variableModel1 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
       
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();       
        processModel.setId("invalidProcessID");
        
       restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(processModel)           
                                              .addProcessVariables(variableModel1, variableModel); 
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidProcessID"));
    }
    
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding multiple process variables is falling in case empty process id is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addMultipleProcessVariablesEmptyProcessId() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:int");
        variableModel1 = RestProcessVariableModel.getRandomProcessVariableModel("d:text");       
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();       
        processModel.setId("");
        
        restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(processModel)           
                                              .addProcessVariables(variableModel1, variableModel); 
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }    
}
