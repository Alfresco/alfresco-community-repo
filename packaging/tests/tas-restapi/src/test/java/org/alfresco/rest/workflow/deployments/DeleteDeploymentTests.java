package org.alfresco.rest.workflow.deployments;

import java.util.List;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestDeploymentModel;
import org.alfresco.rest.model.RestDeploymentModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.rest.model.RestTaskModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for REST API call DELETE "/deployments/{deploymentId}"
 * Class has priority 100 in order to be executed last in the list of tests classes
 * 
 * @author Cristina Axinte
 *
 */
public class DeleteDeploymentTests extends RestTest
{
    private UserModel adminUser, userModel;
    private RestDeploymentModel deployment;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
    }

    @Bug(id = "REPO-1930")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS },
            executionType = ExecutionType.SANITY, description = "Verify admin user deletes a specific deployment using REST API and status code is successful (204)")
    @Test(groups = { TestGroup.REST_API, TestGroup.DEPLOYMENTS, TestGroup.SANITY, TestGroup.WORKFLOW }, priority = 100)
    public void adminDeletesDeploymentWithSuccess() throws Exception
    {
        dataContent.assertExtensionAmpExists("alfresco-workflow-extension");
        // The deployment with name "customWorkflowExtentionForRest.bpmn" is created by Workflow Extention Point
        RestDeploymentModelsCollection allDeployments = restClient.authenticateUser(adminUser).withWorkflowAPI().getDeployments();
        allDeployments.assertThat().entriesListContains("name", "customWorkflowExtentionForRest.bpmn");
        deployment = allDeployments.getDeploymentByName("customWorkflowExtentionForRest.bpmn");

        RestProcessDefinitionModel processDefinitionAssociated =
                restClient.withWorkflowAPI().getAllProcessDefinitions().getProcessDefinitionById(deployment.getId());

        RestProcessModel addedProcess =
                restClient.withWorkflowAPI().addProcess(processDefinitionAssociated.getName(), adminUser, false, CMISUtil.Priority.Normal);

        List<RestTaskModel> processTasks =
                restClient.withWorkflowAPI().usingProcess(addedProcess).getProcessTasks().getEntries();

        restClient.withWorkflowAPI().usingDeployment(deployment).deleteDeployment();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        //check get deployment returns http status Not Found after deleting the deployment
        restClient.withWorkflowAPI().usingDeployment(deployment).getDeployment();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, deployment.getId()));

        //check that process definitions contained by deployment are deleted
        restClient.withWorkflowAPI().usingProcessDefinitions(processDefinitionAssociated).getProcessDefinition();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, processDefinitionAssociated.getId()));

        //check that history information associated with the deployment is deleted
        restClient.withWorkflowAPI().usingProcess(addedProcess).getProcess();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, addedProcess.getId()));

        for (RestTaskModel processTask: processTasks)
        {
            restClient.withWorkflowAPI().usingTask(processTask).getTask();
            restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                    .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, processTask.getId()));
        }

        //check getProcessDefinitionStartFormModel after deletion of process definition
        restClient.withWorkflowAPI().usingProcessDefinitions(processDefinitionAssociated).getProcessDefinitionStartFormModel();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, processDefinitionAssociated.getId()));

        //check getProcessDefinitionImage after deletion of process definition
        restClient.withWorkflowAPI().usingProcessDefinitions(processDefinitionAssociated).getProcessDefinitionImage();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, processDefinitionAssociated.getId()));

        //delete deployment twice
        restClient.withWorkflowAPI().usingDeployment(deployment).deleteDeployment();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, deployment.getId()))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Bug(id = "REPO-1930")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS },
            executionType = ExecutionType.SANITY, description = "Verify admin user cannot delete an inexistent deployment using REST API and status code is successful (204)")
    @Test(groups = { TestGroup.REST_API, TestGroup.DEPLOYMENTS, TestGroup.SANITY, TestGroup.WORKFLOW }, priority = 100)
    public void adminCannotDeleteInexistentDeployment() throws Exception
    {
        deployment = restClient.authenticateUser(adminUser).withWorkflowAPI().getDeployments().getOneRandomEntry().onModel();
        deployment.setId(String.valueOf(1000));

        restClient.withWorkflowAPI().usingDeployment(deployment).deleteDeployment();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "1000"));
    }

    @Bug(id = "REPO-1930")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS }, executionType = ExecutionType.REGRESSION,
            description = "Verify deleteDeployment is unsupported for empty deployment id with REST API and status code is 404")
    @Test(groups = { TestGroup.REST_API, TestGroup.DEPLOYMENTS, TestGroup.REGRESSION, TestGroup.WORKFLOW })
    public void deleteDeploymentIsUnsupportedForEmptyId() throws Exception
    {
        deployment = restClient.authenticateUser(adminUser).withWorkflowAPI().getDeployments().getOneRandomEntry().onModel();
        deployment.setId("");
        restClient.withWorkflowAPI().usingDeployment(deployment).deleteDeployment();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @Bug(id = "REPO-1930")
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.DEPLOYMENTS }, executionType = ExecutionType.REGRESSION,
            description = "Verify deleteDeployment is forbidden using non admin user or different user than creator with REST API and status code is 403")
    @Test(groups = { TestGroup.REST_API, TestGroup.DEPLOYMENTS, TestGroup.REGRESSION, TestGroup.WORKFLOW })
    public void deleteDeploymentUsingNonAdminUser() throws Exception
    {
        deployment = restClient.authenticateUser(adminUser).withWorkflowAPI().getDeployments().getOneRandomEntry().onModel();
        restClient.authenticateUser(userModel).withWorkflowAPI().usingDeployment(deployment).deleteDeployment();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }
}
