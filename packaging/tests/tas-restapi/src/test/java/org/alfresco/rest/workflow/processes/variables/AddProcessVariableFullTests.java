package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddProcessVariableFullTests  extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsProcess, assignee, adminUser;
    private RestProcessModel processModel;
    private RestProcessVariableModel variableModel, processVariable, updatedProcessVariable;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsProcess).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsProcess).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsProcess).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify addProcessVariable by any user for invalid processID with REST API and status code is NOT_FOUND (404)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addProcessVariableWithInvalidProcessId() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processModel.setId("invalidProcessID");

        processVariable = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingProcess(processModel)
                                    .updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidProcessID"));
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify addProcessVariable by any user for empty processID with REST API and status code is NOT_FOUND (404)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addProcessVariableWithEmptyProcessId() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processModel.setId("");

        processVariable = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().usingProcess(processModel)
                                    .updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable in case of having only 'name' field is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addingProcessVariableWithOnlyNameProvided() throws Exception
    {        
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"name\": \"variableName\"}",
                "processes/{processId}/variables/{variableName}", processModel.getId(), "variableName");
        processVariable = restClient.processModel(RestProcessVariableModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("name").is("variableName")
                       .and().field("type").is("d:any")
                       .and().field("value").isNull();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable in case of missing type field is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addingProcessVariableIfMissingValueIsProvided() throws Exception
    {        
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"name\": \"variableName\", \"type\": \"d:text\"}",
                "processes/{processId}/variables/{variableName}", processModel.getId(), "variableName");
        processVariable = restClient.processModel(RestProcessVariableModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("name").is("variableName")
                       .and().field("type").is("d:text")
                       .and().field("value").isNull();
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable in case of missing type field is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addingProcessVariableIfMissingTypeIsProvided() throws Exception
    {        
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"value\": \"variableValue\", \"name\": \"variableName\"}",
                "processes/{processId}/variables/{variableName}", processModel.getId(), "variableValue");
        processVariable = restClient.processModel(RestProcessVariableModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("name").is("variableName")
                       .and().field("type").is("d:text")
                       .and().field("value").is("variableValue");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable is case of empty type value is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addingProcessVariableIfEmptyTypeIsProvided() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("");

        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);

        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("name").is(variableModel.getName())
                       .and().field("type").is("d:text")
                       .and().field("value").is(variableModel.getValue());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable in case of empty body field is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addingProcessVariableWithEmptyBodyProvided() throws Exception
    {        
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("");
        
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{ }",
                "processes/{processId}/variables/{variableName}", processModel.getId(), variableModel.getName());
        processVariable = restClient.processModel(RestProcessVariableModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(RestErrorModel.VARIABLE_NAME_REQUIRED)
                  .containsSummary(RestErrorModel.VARIABLE_NAME_REQUIRED)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Update twice in a row the same variable.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void updateTwiceInARowSameProcessVariable() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();

        processVariable = restClient.withWorkflowAPI().usingProcess(processModel)
                                    .updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("name").is(variableModel.getName())
                       .and().field("type").is(variableModel.getType())
                       .and().field("value").is(variableModel.getValue());
        
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
        .assertThat().entriesListContains("name", variableModel.getName());
          
        updatedProcessVariable = restClient.withWorkflowAPI().usingProcess(processModel)
                                    .updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        updatedProcessVariable.assertThat().field("name").is(variableModel.getName())
                       .and().field("type").is(variableModel.getType())
                       .and().field("value").is(variableModel.getValue());

        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                .assertThat().entriesListContains("name", variableModel.getName());
    }

}
