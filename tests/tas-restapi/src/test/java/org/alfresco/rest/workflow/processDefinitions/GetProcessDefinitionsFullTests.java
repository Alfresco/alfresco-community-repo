package org.alfresco.rest.workflow.processDefinitions;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.rest.model.RestProcessDefinitionModel;
import org.alfresco.rest.model.RestProcessDefinitionModelsCollection;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Claudia Agache on 2/1/2017.
 */
public class GetProcessDefinitionsFullTests extends RestTest
{
    private UserModel adminUserModel;
    private RestProcessDefinitionModelsCollection processDefinitions;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUserModel = dataUser.getAdminUser();
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin gets process definitions with valid skipCount parameter applied using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsWithValidSkipCount() throws Exception
    {
        processDefinitions = restClient.authenticateUser(adminUserModel)
                .withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        RestProcessDefinitionModel firstProcessDefinition = processDefinitions.getEntries().get(0).onModel();
        RestProcessDefinitionModel secondProcessDefinition = processDefinitions.getEntries().get(1).onModel();

        RestProcessDefinitionModelsCollection procDefWithSkipCount = restClient.withParams("skipCount=2").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        procDefWithSkipCount
                .assertThat().entriesListDoesNotContain("name", firstProcessDefinition.getName())
                .assertThat().entriesListDoesNotContain("name", secondProcessDefinition.getName())
                .assertThat().entriesListCountIs(processDefinitions.getEntries().size()-2);
        procDefWithSkipCount.assertThat().paginationField("skipCount").is("2");
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get process definitions with negative skipCount parameter applied using REST API")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsWithNegativeSkipCount() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withParams("skipCount=-1").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .containsErrorKey(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get process definitions with non numeric skipCount parameter applied using REST API")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsWithNonNumericSkipCount() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withParams("skipCount=A").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "A"));
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin gets process definitions with valid maxItems parameter applied using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsWithValidMaxItems() throws Exception
    {
        processDefinitions = restClient.authenticateUser(adminUserModel)
                .withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        RestProcessDefinitionModel firstProcessDefinition = processDefinitions.getEntries().get(0).onModel();
        RestProcessDefinitionModel secondProcessDefinition = processDefinitions.getEntries().get(1).onModel();

        RestProcessDefinitionModelsCollection procDefWithMaxItems = restClient.withParams("maxItems=2").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        procDefWithMaxItems
                .assertThat().entriesListContains("name", firstProcessDefinition.getName())
                .assertThat().entriesListContains("name", secondProcessDefinition.getName())
                .assertThat().entriesListCountIs(2);
        procDefWithMaxItems.assertThat().paginationField("maxItems").is("2");
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get process definitions with negative maxItems parameter applied using REST API")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsWithNegativeMaxItems() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withParams("maxItems=-1").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                .stackTraceIs(RestErrorModel.STACKTRACE)
                .statusCodeIs(HttpStatus.BAD_REQUEST);
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get process definitions with non numeric maxItems parameter applied using REST API")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsWithNonNumericMaxItems() throws Exception
    {
        restClient.authenticateUser(adminUserModel).withParams("maxItems=A").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST).assertLastError()
                .containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "A"));
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify Admin user gets process definitions with properties parameter applied using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsWithValidProperties() throws Exception
    {
        processDefinitions =restClient.authenticateUser(adminUserModel).withParams("properties=name,graphicNotationDefined,version").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processDefinitions.assertThat().entriesListIsNotEmpty();
        processDefinitions.getOneRandomEntry().onModel().assertThat()
                .fieldsCount().is(3).and()
                .field("deploymentId").isNull().and()
                .field("description").isNull().and()
                .field("id").isNull().and()
                .field("startFormResourceKey").isNull().and()
                .field("category").isNull().and()
                .field("title").isNull().and()
                .field("version").isNotEmpty().and()
                .field("graphicNotationDefined").isNotEmpty().and()
                .field("key").isNull().and()
                .field("name").isNotEmpty();
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin gets process definitions ordered by name ascendant using REST API and status code is OK (200)")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsOrderedByNameAsc() throws Exception
    {
        processDefinitions = restClient.authenticateUser(adminUserModel)
                .withParams("orderBy=name ASC").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processDefinitions.assertThat().entriesListIsSortedAscBy("name");
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin doesn't get process definitions when many fields are used for orderBy parameter")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsOrderedByManyFields() throws Exception
    {
        restClient.authenticateUser(adminUserModel)
                .withParams("orderBy=name,id").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
                .assertLastError().containsSummary(RestErrorModel.ONLY_ONE_ORDERBY);
    }

    @TestRail(section = { TestGroup.REST_API,  TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION }, executionType = ExecutionType.REGRESSION,
            description = "Verify admin gets process definitions when where parameter is applied")
    @Test(groups = { TestGroup.REST_API, TestGroup.WORKFLOW, TestGroup.PROCESS_DEFINITION, TestGroup.REGRESSION })
    public void getProcessDefinitionsFilteredByKey() throws Exception
    {
        processDefinitions = restClient.authenticateUser(adminUserModel)
                .withParams("where=(key matches('activitiParallel%'))").withWorkflowAPI().getAllProcessDefinitions();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        processDefinitions.assertThat().entriesListCountIs(2)
                    .assertThat().entriesListContains("key", "activitiParallelReview")
                    .assertThat().entriesListContains("key", "activitiParallelGroupReview");
    }

}
