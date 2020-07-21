package org.alfresco.rest.workflow.processes.items;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestItemModelsCollection;
import org.alfresco.rest.model.RestProcessModel;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 2/2/2017.
 */
public class GetProcessItemsFullTests extends RestTest
{
    private FileModel document1, document2, document3;
    private SiteModel siteModel;
    private UserModel userWhoStartsTask, assignee;
    private RestProcessModel processModel;
    private RestItemModelsCollection items;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        userWhoStartsTask = dataUser.createRandomTestUser();
        assignee = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userWhoStartsTask).createPublicRandomSite();
        document1 = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        document2 = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        document3 = dataContent.usingUser(userWhoStartsTask).usingSite(siteModel).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process items for process without items")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessItemsForProcessWithoutItems() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        items = restClient.withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        items.assertThat().entriesListIsEmpty();
    }

    @Bug(id = "REPO-1989")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process items for process with valid skipCount parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessItemsWithValidSkipCount() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document3);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        items = restClient.authenticateUser(assignee).withParams("skipCount=2").withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        items.assertThat().entriesListContains("name", document3.getName())
                .assertThat().entriesListDoesNotContain("name", document1.getName())
                .assertThat().entriesListDoesNotContain("name", document2.getName())
                .assertThat().entriesListCountIs(1)
                .assertThat().paginationField("skipCount").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process items for process with negative skipCount")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessItemsWithNegativeSkipCount() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        restClient.authenticateUser(assignee).withParams("skipCount=-2").withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .containsErrorKey(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process items for process with non numeric skipCount")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessItemsWithNonNumericSkipCount() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        restClient.authenticateUser(assignee).withParams("skipCount=A").withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "A"));
    }

    @Bug(id = "MNT-17438")
    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process items for process with valid maxItems parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessItemsWithValidMaxItems() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document2);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document3);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        items = restClient.authenticateUser(assignee).withParams("maxItems=2").withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        items.assertThat().entriesListDoesNotContain("name", document3.getName())
                .assertThat().entriesListContains("name", document1.getName())
                .assertThat().entriesListContains("name", document2.getName())
                .assertThat().entriesListCountIs(2)
                .assertThat().paginationField("maxItems").is("2");
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process items for process with negative maxItems")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessItemsWithNegativeMaxItems() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        restClient.authenticateUser(assignee).withParams("maxItems=-2").withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Get process items for process with non numeric maxItems")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION})
    public void getProcessItemsWithNonNumericMaxItems() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        restClient.authenticateUser(assignee).withParams("maxItems=A").withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "A"));
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process items with properties parameter.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessItemsWithPropertiesParameter() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        items = restClient.authenticateUser(assignee).withParams("properties=createdBy,name,mimeType,size").withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        items.assertThat().entriesListIsNotEmpty();
        items.getProcessItemByName(document1.getName())
                .assertThat().field("createdBy").is(userWhoStartsTask.getUsername())
                .assertThat().field("name").is(document1.getName())
                .assertThat().field("mimeType").is(document1.getFileType().mimeType)
                .assertThat().field("size").isNotNull()
                .assertThat().field("modifiedAt").isNull()
                .assertThat().field("modifiedBy").isNull()
                .assertThat().field("id").isNull()
                .assertThat().field("createdAt ").isNull()
                .assertThat().fieldsCount().is(4);
    }

    @TestRail(section = { TestGroup.REST_API, TestGroup.PROCESSES }, executionType = ExecutionType.REGRESSION,
            description = "Verify get all process items after process is deleted.")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESSES, TestGroup.REGRESSION })
    public void getProcessItemsAfterDeletingProcess() throws Exception
    {
        processModel = restClient.authenticateUser(userWhoStartsTask).withWorkflowAPI().addProcess("activitiAdhoc", assignee, false, CMISUtil.Priority.Normal);
        restClient.withWorkflowAPI().usingProcess(processModel).addProcessItem(document1);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        restClient.withWorkflowAPI().usingProcess(processModel).deleteProcess();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        items = restClient.authenticateUser(assignee).withWorkflowAPI().usingProcess(processModel).getProcessItems();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        items.assertThat().entriesListIsNotEmpty();
        items.getProcessItemByName(document1.getName())
                .assertThat().field("createdBy").is(userWhoStartsTask.getUsername())
                .assertThat().field("name").is(document1.getName())
                .assertThat().field("mimeType").is(document1.getFileType().mimeType)
                .assertThat().field("size").isNotNull()
                .assertThat().field("modifiedAt").isNotNull()
                .assertThat().field("modifiedBy").is(userWhoStartsTask.getUsername())
                .assertThat().field("id").is(document1.getNodeRefWithoutVersion())
                .assertThat().field("createdAt ").isNotNull();
    }
}
