package org.alfresco.rest.workflow.tasks.candidates;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestCandidateModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.GroupModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TaskModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
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
public class GetTaskCandidatesRegressionTests extends  RestTest
{
    private UserModel userModel, userModel1, userModel2;
    private SiteModel siteModel;
    private FileModel fileModel;
    private TaskModel taskModel;
    private GroupModel group;
    private RestCandidateModelsCollection candidateModels;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        fileModel = dataContent.usingUser(userModel).usingSite(siteModel).createContent(DocumentType.TEXT_PLAIN);
        userModel1 = dataUser.createRandomTestUser();
        userModel2 = dataUser.createRandomTestUser();
        group = dataGroup.createRandomGroup();
        dataGroup.addListOfUsersToGroup(group, userModel1, userModel2);
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify get task candidates with invalid task id")
    public void getTaskCandidatesWithInvalidTaskId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        taskModel.setId("invalid-id");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalid-id"))
                .containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify get task candidates with empty task id")
    public void getTaskCandidatesWithEmptyTaskId() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        taskModel.setId("");
        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, ""));
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify get task candidates for completed task")
    public void getTaskCandidatesForCompletedTask() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);

        restClient.authenticateUser(userModel1).withWorkflowAPI().usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        dataWorkflow.usingUser(userModel1).taskDone(taskModel);

        restClient.authenticateUser(userModel1).withWorkflowAPI().usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, taskModel.getId()));

        restClient.authenticateUser(userModel).withWorkflowAPI().usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
                .assertLastError().containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, taskModel.getId()));
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Verify get task candidates by non candidate user")
    public void getTaskCandidatesByNonCandidateUser() throws Exception
    {
        UserModel outsider = dataUser.createRandomTestUser();
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        restClient.authenticateUser(outsider).withWorkflowAPI().usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
                .assertLastError().containsSummary(RestErrorModel.PERMISSION_WAS_DENIED);
    }

    @Bug(id="MNT-17438")
    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Get task candidates with skip count parameter")
    public void getTaskCandidatesWithSkipCount() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        candidateModels = restClient.authenticateUser(userModel).withWorkflowAPI()
                              .usingParams("skipCount=1").usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        candidateModels.assertThat()
            .entriesListIsNotEmpty()
                .and().paginationField("count").is("0");
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Get task candidates with max items parameter set to 0")
    public void getTaskCandidatesWithZeroMaxItems() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        candidateModels = restClient.authenticateUser(userModel).withWorkflowAPI()
                              .usingParams("maxItems=0").usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                              .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                              .stackTraceIs(RestErrorModel.STACKTRACE)
                              .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Get task candidates with max items parameter")
    public void getTaskCandidatesWithMaxItems() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        candidateModels = restClient.authenticateUser(userModel).withWorkflowAPI()
                              .usingParams("maxItems=2").usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        candidateModels.assertThat()
            .entriesListIsNotEmpty()
                .and().paginationField("count").is("1");
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Get task candidates with properties")
    public void getTaskCandidatesWithProperties() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        candidateModels = restClient.authenticateUser(userModel).withWorkflowAPI()
                .usingParams("properties=candidateId").usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        candidateModels.getEntries().get(0).onModel().assertThat()
            .field("candidateId").contains(group.getGroupIdentifier()).and()
            .field("candidateType").isNull();
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Get task candidates with invalid properties")
    public void getTaskCandidatesWithInvalidProperties() throws Exception
    {
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        candidateModels = restClient.authenticateUser(userModel).withWorkflowAPI()
                .usingParams("properties=unknown-prop").usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        candidateModels.getEntries().get(0).onModel().assertThat()
            .fieldsCount().is(0).and();
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Get task candidates by deleted candidate")
    public void getTaskCandidatesByDeletedCandidate() throws Exception
    {
        UserModel newUser = dataUser.createRandomTestUser();
        dataGroup.addListOfUsersToGroup(group, newUser);
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        restClient.authenticateUser(newUser).withWorkflowAPI()
                              .usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        dataUser.usingAdmin().deleteUser(newUser);

        restClient.authenticateUser(newUser).withWorkflowAPI()
            .usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Get task candidates by deleted group")
    public void getTaskCandidatesByDeletedGroup() throws Exception
    {
        GroupModel deletedGroup = dataGroup.createRandomGroup();
        UserModel newUser = dataUser.createRandomTestUser();
        dataGroup.addListOfUsersToGroup(deletedGroup, userModel2, newUser);
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(deletedGroup);
        restClient.authenticateUser(newUser).withWorkflowAPI()
            .usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        dataGroup.usingAdmin().deleteGroup(deletedGroup);

        restClient.authenticateUser(newUser).withWorkflowAPI()
            .usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
            .assertLastError().containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                              .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                              .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.TASKS }, executionType = ExecutionType.REGRESSION,
            description = "Get task candidates by user that was removed from group")
    public void getTaskCandidatesByUserRemovedFromGroup() throws Exception
    {
        UserModel newUser = dataUser.createRandomTestUser();
        dataGroup.addListOfUsersToGroup(group, newUser);
        taskModel = dataWorkflow.usingUser(userModel)
                .usingSite(siteModel)
                .usingResource(fileModel).createPooledReviewTaskAndAssignTo(group);
        restClient.authenticateUser(newUser).withWorkflowAPI()
            .usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        dataGroup.usingAdmin().removeUserFromGroup(group, newUser);

        restClient.authenticateUser(newUser).withWorkflowAPI()
            .usingTask(taskModel).getTaskCandidates();
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN)
            .assertLastError().containsErrorKey(RestErrorModel.PERMISSION_DENIED_ERRORKEY)
                              .containsSummary(RestErrorModel.PERMISSION_WAS_DENIED)
                              .stackTraceIs(RestErrorModel.STACKTRACE);
    }
}