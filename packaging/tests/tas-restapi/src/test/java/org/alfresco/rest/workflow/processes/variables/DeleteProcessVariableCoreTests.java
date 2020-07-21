package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
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

/**
 * 
 * @author bogdan.bocancea
 *
 */
public class DeleteProcessVariableCoreTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee, adminUser;
    private RestProcessModel restProcessModel;
    private ProcessModel processModel;
    private RestProcessVariableModel variableModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();     
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        processModel = dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document)
            .createSingleReviewerTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete invalid process variable")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteInvalidProcessVariable() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("x:InvalidVar");
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, variableModel.getName()))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Delete process variable twice")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessVariableTwice() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withWorkflowAPI().usingProcess(restProcessModel).deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(restProcessModel).deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, variableModel.getName()));
    }

    @TestRail(section = {TestGroup.REST_API,TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Remove process variables with empty processId")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessVariableEmptyProcessId() throws JsonToModelConversionException, Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restProcessModel.setId("");
        restClient.withWorkflowAPI().usingProcess(restProcessModel).deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                  .assertLastError()
                  .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""))
                  .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION, 
              description = "Delete process variable with admin.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void deleteProcessVariableWithAdmin() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        restProcessModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI()
                .getProcesses().getProcessModelByProcessDefId(processModel.getId());
        restClient.withWorkflowAPI().usingProcess(restProcessModel).addProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.authenticateUser(adminUser).withWorkflowAPI().usingProcess(processModel)
                  .deleteProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);
        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                  .assertThat().entriesListDoesNotContain("name", variableModel.getName());
    }

}
