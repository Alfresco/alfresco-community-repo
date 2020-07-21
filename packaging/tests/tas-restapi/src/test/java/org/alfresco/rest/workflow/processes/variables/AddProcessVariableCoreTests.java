package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddProcessVariableCoreTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsProcess, assignee, adminUser, anotherUser;
    private RestProcessModel processModel;
    private RestProcessVariableModel variableModel, processVariable;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        anotherUser = dataUser.createRandomTestUser();
        userWhoStartsProcess = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsProcess).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsProcess).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsProcess).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Add process variable using by the user who started the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addProcessVariableByUserThatStartedTheProcess() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsProcess).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);

        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("name").is(variableModel.getName())
                       .and().field("type").is(variableModel.getType())
                       .and().field("value").is(variableModel.getValue());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Add process variable using by a random user.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void addProcessVariableByAnyUser() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(anotherUser).withWorkflowAPI().addProcess("activitiAdhoc", anotherUser, false, Priority.Normal);

        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("name").is(variableModel.getName())
                       .and().field("type").is(variableModel.getType())
                       .and().field("value").is(variableModel.getValue());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable is falling in case invalid type is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfInvalidTypeIsProvided() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:textarea");

        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(String.format(RestErrorModel.UNSUPPORTED_TYPE, "d:textarea"))
                  .containsSummary(String.format(RestErrorModel.UNSUPPORTED_TYPE, "d:textarea"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }
   
    @Bug(id = "REPO-1938")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable is falling in case invalid type prefix is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfInvalidTypePrefixIsProvided() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("ddt:text");

        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                  .containsSummary(String.format(RestErrorModel.INVALID_NAMEPACE_PREFIX, "ddt"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable is falling in case invalid value is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfInvalidValueIsProvided() throws Exception
    {
        RestProcessVariableModel variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:int");
        variableModel.setValue("invalidValue");

        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(RestErrorModel.API_DEFAULT_ERRORKEY)
                  .containsSummary(String.format(RestErrorModel.FOR_INPUT_STRING, "invalidValue"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable is falling in case missing required variable body (name) is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfMissingRequiredVariableNameBodyIsProvided() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();

        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"value\": \"missingVariableName\",\"type\": \"d:text\"}",
                "processes/{processId}/variables/{variableName}", processModel.getId(), variableModel.getName());
        restClient.processModel(RestProcessVariableModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(RestErrorModel.VARIABLE_NAME_REQUIRED)
                  .containsSummary(RestErrorModel.VARIABLE_NAME_REQUIRED)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Adding process variable is falling in case invalid variableBody (adding extra parameter in body) is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfInvalidBodyIsProvided() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();

        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, "{\"scope\": \"local\",\"value\": \"testing\",\"type\": \"d:text\"}",
                "processes/{processId}/variables/{variableName}", processModel.getId(), variableModel.getName());
        restClient.processModel(RestProcessVariableModel.class, request);

        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(String.format(RestErrorModel.NO_CONTENT, "Unrecognized field \"scope\""))
                  .containsSummary(String.format(RestErrorModel.NO_CONTENT, "Unrecognized field \"scope\""))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Adding process variable is falling in case empty name is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void failedAddingProcessVariableIfEmptyNameIsProvided() throws Exception
    {
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, Priority.Normal);
        RestProcessVariableModel variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        variableModel.setName("");

        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);

        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                  .assertLastError()
                  .containsErrorKey(RestErrorModel.PUT_EMPTY_ARGUMENT)
                  .containsSummary(RestErrorModel.PUT_EMPTY_ARGUMENT)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

}
