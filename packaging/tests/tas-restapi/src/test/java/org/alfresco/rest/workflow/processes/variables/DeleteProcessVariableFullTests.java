package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableModel;
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

public class DeleteProcessVariableFullTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee;
    private RestProcessModel restProcessModel;
    private ProcessModel processModel;
    private RestProcessVariableModel variableModel;
    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();      
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        processModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createSingleReviewerTaskAndAssignTo(assignee);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete empty process variable")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteEmptyProcessVariable() throws Exception
    {
        variableModel = new RestProcessVariableModel("", "", "bpm:workflowPackage");
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getProcesses()
                                     .getProcessModelByProcessDefId(processModel.getId());
        
        restClient.withWorkflowAPI().usingProcess(restProcessModel)
                  .deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.METHOD_NOT_ALLOWED)
                  .assertLastError()
                  .containsSummary(RestErrorModel.DELETE_EMPTY_ARGUMENT)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .containsErrorKey(RestErrorModel.DELETE_EMPTY_ARGUMENT)
                  .stackTraceIs(RestErrorModel.STACKTRACE);     
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
              description = "Delete process then delete process variables, status OK should be returned")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessVariablesForADeletedProcess() throws Exception
    {
        UserModel userWhoStartsTask = dataUser.createRandomTestUser();
        UserModel assignee = dataUser.createRandomTestUser();

        RestProcessModel processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                                                  .addProcess("activitiAdhoc", assignee, false, Priority.Normal);

        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                                     .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().usingProcess(processModel)
                  .deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, processModel.getId()))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Delete process variable using by the user who started the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessVariableByUserThatStartedTheProcess() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                                 .addProcess("activitiAdhoc", assignee, false, Priority.Normal);
     
        restProcessModel = restClient.withWorkflowAPI().getProcesses()
                                     .getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        
        restClient.withWorkflowAPI().usingProcess(processModel)
                  .deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                  .assertThat()
                  .entriesListDoesNotContain("name", variableModel.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
            description = "Delete process variable using by the user involved in the process.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessVariableByUserInvolvedInTheProcess() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                                 .addProcess("activitiAdhoc", assignee, false, Priority.Normal);

        restProcessModel = restClient.withWorkflowAPI().getProcesses()
                                     .getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(processModel)
                  .deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                  .assertThat()
                  .entriesListDoesNotContain("name", variableModel.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete process variable with invalid type")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessVariableInvalidType() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        variableModel.setType("invalid-type");
        restClient.withWorkflowAPI().usingProcess(restProcessModel).deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                  .assertThat()
                  .entriesListDoesNotContain("name", variableModel.getName());
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete process variable by non assigned user")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessVarialbleByNonAssignedUser() throws Exception
    {
        UserModel nonAssigned = dataUser.createRandomTestUser();
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(nonAssigned).withWorkflowAPI().usingProcess(restProcessModel)
                 .deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.ACCESS_INFORMATION_NOT_ALLOWED, processModel.getId()))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .containsErrorKey(String.format(RestErrorModel.ACCESS_INFORMATION_NOT_ALLOWED, processModel.getId()))
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete process variable by inexistent user")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessVarialbleByInexistentUser() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                                 .addProcess("activitiAdhoc", userWhoStartsTask, false, Priority.Normal);
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(UserModel.getRandomUserModel()).withWorkflowAPI()
                  .usingProcess(restProcessModel)
                  .deleteProcessVariable(variableModel);

        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
}
