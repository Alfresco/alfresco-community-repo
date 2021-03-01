package org.alfresco.rest.models.aspects;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestAbstractClassModel;
import org.alfresco.rest.model.RestAbstractClassModelsCollection;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetAspectsTests  extends RestTest
{

    private UserModel regularUser;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.MODEL }, executionType = ExecutionType.REGRESSION,
            description = "Verify user get aspects and gets status code OK (200)")
    public void getAspects() throws Exception
    {
        RestAbstractClassModelsCollection aspects = restClient.authenticateUser(regularUser).withModelAPI()
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        aspects.assertThat()
            .entriesListCountIs(100)
            .and().entriesListContains("id", "cm:classifiable")
            .and().entriesListContains("id", "cm:author")
            .and().entriesListContains("id", "cm:checkedOut");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.MODEL }, executionType = ExecutionType.REGRESSION,
            description = "Should filter aspects using namespace uri and gets status code OK (200)")
    public void getAspectByNamespaceUri() throws Exception
    {
        RestAbstractClassModelsCollection aspects = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(namespaceUri matches('http://www.alfresco.org/model.*'))")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        aspects.assertThat().entriesListCountIs(100);

        aspects = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(not namespaceUri matches('http://www.alfresco.org/model.*'))")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        aspects.assertThat().entriesListCountIs(0);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.MODEL }, executionType = ExecutionType.REGRESSION,
            description = "Should filter aspects using modelId and gets status code OK (200)")
    public void getAspectByModelsIds() throws Exception
    {
        RestAbstractClassModelsCollection aspects = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(modelId in ('cm:contentmodel', 'smf:smartFolder'))")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        aspects.getPagination().assertThat().fieldsCount().is(5).and()
                .field("totalItems").isLessThan(65).and()
                .field("maxItems").is(100).and()
                .field("skipCount").isGreaterThan(0).and()
                .field("hasMoreItems").is(false);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.MODEL }, executionType = ExecutionType.REGRESSION,
            description = "Should filter aspects using modelId with subaspects and gets status code OK (200)")
    public void getAspectByModelsIdsWithIncludeSubAspects() throws Exception
    {
        RestAbstractClassModelsCollection aspects = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(modelId in ('cm:contentmodel INCLUDESUBASPECTS', 'smf:smartFolder INCLUDESUBASPECTS'))")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        aspects.getPagination().assertThat().fieldsCount().is(5).and()
                .field("totalItems").isGreaterThan(65).and()
                .field("maxItems").is(100).and()
                .field("skipCount").isGreaterThan(0).and()
                .field("hasMoreItems").is(false);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.MODEL }, executionType = ExecutionType.REGRESSION,
            description = "Should filter aspects using parentId and gets status code OK (200)")
    public void getAspectByParentId() throws Exception
    {
        RestAbstractClassModelsCollection aspects = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(parentId in ('cm:titled'))")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        aspects.getPagination().assertThat().fieldsCount().is(5).and()
                .field("totalItems").is(5).and()
                .field("hasMoreItems").is(false);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.MODEL }, executionType = ExecutionType.REGRESSION,
            description = "Should Aspects association, properties and mandatory aspects and gets status code OK (200)")
    public void getAspectIncludeParams() throws Exception
    {
        RestAbstractClassModelsCollection aspects = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("include=properties,mandatoryAspects,associations")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        for (RestAbstractClassModel aspect : aspects.getEntries())
        {
            aspect.onModel().assertThat()
                    .field("associations").isNotNull().and()
                    .field("properties").isNotNull().and()
                    .field("mandatoryAspects").isNotNull();
        }
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.MODEL }, executionType = ExecutionType.REGRESSION,
            description = "Should verify the query errors with possible options")
    public void verifyAspectsQueryError()
    {
        restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(modelId in (' ')")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);

        restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(modelId in ('cm:contentmodel INCLUDESUBASPECTS',))")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);

        restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(modelId in ('cm:contentmodel INCLUDESUBTYPES'))")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);

        restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(parentId in (' ')")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);

        restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(parentId in ('cm:content',))")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);

        restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(parentId in ('cm:content',))&include=properties")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);

        restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(namespaceUri matches('*'))")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);

        restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(parentId in ('cm:content'))&include=properties")
                .getAspects();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.MODEL}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets aspects with high skipCount and maxItems parameter applied")
    public void getPaginationParameter() throws Exception
    {
        RestAbstractClassModelsCollection aspects = restClient.authenticateUser(regularUser)
                .withModelAPI()
                .usingParams("maxItems=10&skipCount=10")
                .getAspects();
        aspects.assertThat().entriesListCountIs(10);
        aspects.assertThat().paginationField("hasMoreItems").is("true");
        aspects.assertThat().paginationField("skipCount").is("10");
        aspects.assertThat().paginationField("maxItems").is("10");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.MODEL, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.MODEL}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets aspects with hasMoreItems applied bases on skip count and maxItems")
    public void getHighPaginationQuery() throws Exception
    {
        RestAbstractClassModelsCollection aspects = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("maxItems=10&skipCount=150")
                .getAspects();
        aspects.assertThat().entriesListCountIs(0);
        aspects.assertThat().paginationField("hasMoreItems").is("false");
        aspects.assertThat().paginationField("skipCount").is("150");
        aspects.assertThat().paginationField("maxItems").is("10");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
}