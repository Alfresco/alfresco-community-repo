package org.alfresco.rest.workflow.processes.variables;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestProcessVariableModel;
import org.alfresco.utility.data.RandomData;
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
public class AddProcessVariableSanityTests extends RestTest
{
    private FileModel document;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee, adminUser;
    private RestProcessModel processModel;
    private RestProcessVariableModel variableModel, processVariable;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        dataWorkflow.usingUser(userWhoStartsTask).usingSite(siteModel).usingResource(document).createNewTaskAndAssignTo(assignee);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Create non-existing variable")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void addProcessVariable() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();

        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("name").is(variableModel.getName())
                       .and().field("type").is(variableModel.getType())
                       .and().field("value").is(variableModel.getValue());

        restClient.withWorkflowAPI().usingProcess(processModel).getProcessVariables()
                .assertThat().entriesListContains("name", variableModel.getName());
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Update existing variable")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void updateExistingProcessVariable() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("d:text");
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("name").is(variableModel.getName())
                        .and().field("type").is(variableModel.getType())
                        .and().field("value").is(variableModel.getValue());

        String newValue = RandomData.getRandomName("value");
        variableModel.setValue(newValue);
        processVariable = restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processVariable.assertThat().field("value").is(newValue);
    }

    @TestRail(section = {TestGroup.REST_API, TestGroup.WORKFLOW,TestGroup.PROCESSES }, executionType = ExecutionType.SANITY,
            description = "Adding process variable is falling in case invalid variableBody is provided")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.SANITY })
    public void failedAddingProcessVariableIfInvalidBodyIsProvided() throws Exception
    {
        variableModel = RestProcessVariableModel.getRandomProcessVariableModel("incorrect type");
        processModel = restClient.authenticateUser(adminUser).withWorkflowAPI().getProcesses().getOneRandomEntry().onModel();
        
        restClient.withWorkflowAPI().usingProcess(processModel).updateProcessVariable(variableModel);
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                  .assertLastError()
                  .containsErrorKey(String.format(RestErrorModel.UNSUPPORTED_TYPE, "incorrect type"))
                  .containsSummary(String.format(RestErrorModel.UNSUPPORTED_TYPE, "incorrect type"))
                  .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                  .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}
