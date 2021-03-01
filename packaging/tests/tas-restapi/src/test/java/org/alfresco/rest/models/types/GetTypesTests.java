package org.alfresco.rest.models.types;

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

public class GetTypesTests extends RestTest
{

    private UserModel regularUser;

    @BeforeClass(alwaysRun=true)
    public void dataPreparation() throws Exception
    {
        regularUser = dataUser.createRandomTestUser();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Verify user get types and gets status code OK (200)")
    public void getTypes() throws Exception
    {
        RestAbstractClassModelsCollection types = restClient.authenticateUser(regularUser).withModelAPI()
                .getTypes();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        types.assertThat()
            .entriesListCountIs(100)
            .and().entriesListContains("id", "cm:content")
            .and().entriesListContains("id", "cm:systemfolder")
            .and().entriesListContains("id", "cm:folder");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Should filter types using namespace uri and gets status code OK (200)")
    public void getTypeByNamespaceUri() throws Exception
    {
        RestAbstractClassModelsCollection types = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(namespaceUri matches('http://www.alfresco.org/model.*'))")
                .getTypes();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        types.assertThat().entriesListCountIs(100);

        types = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(not namespaceUri matches('http://www.alfresco.org/model.*'))")
                .getTypes();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        types.assertThat().entriesListCountIs(0);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Should filter types using modelId and gets status code OK (200)")
    public void getTypeByModelsIds() throws Exception
    {
        RestAbstractClassModelsCollection types = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(modelId in ('cm:contentmodel', 'smf:smartFolder'))")
                .getTypes();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        types.getPagination().assertThat().fieldsCount().is(5).and()
                .field("totalItems").isLessThan(65).and()
                .field("maxItems").is(100).and()
                .field("skipCount").isGreaterThan(0).and()
                .field("hasMoreItems").is(false);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Should filter types using modelId with subtypes and gets status code OK (200)")
    public void getTypeByModelsIdsWithIncludeSubTypes() throws Exception
    {
        RestAbstractClassModelsCollection types = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(modelId in ('cm:contentmodel INCLUDESUBTYPES', 'smf:smartFolder INCLUDESUBTYPES'))")
                .getTypes();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        types.getPagination().assertThat().fieldsCount().is(5).and()
                .field("totalItems").isGreaterThan(65).and()
                .field("maxItems").is(100).and()
                .field("skipCount").isGreaterThan(0).and()
                .field("hasMoreItems").is(false);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Should filter types using parentId and gets status code OK (200)")
    public void getTypeByParentId() throws Exception
    {
        RestAbstractClassModelsCollection types = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("where=(parentId in ('cm:content'))")
                .getTypes();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        types.getPagination().assertThat().fieldsCount().is(5).and()
                .field("totalItems").isGreaterThan(40).and()
                .field("hasMoreItems").is(false);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section = {TestGroup.REST_API, TestGroup.SITES }, executionType = ExecutionType.REGRESSION,
            description = "Should get Type with association, properties and mandatory types and gets status code OK (200)")
    public void getTypeIncludeParams() throws Exception
    {
        RestAbstractClassModelsCollection types = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("include=properties,mandatoryAspects,associations")
                .getTypes();
        restClient.assertStatusCodeIs(HttpStatus.OK);

        for (RestAbstractClassModel type : types.getEntries())
        {
            type.onModel().assertThat()
                    .field("associations").isNotNull().and()
                    .field("properties").isNotNull().and()
                    .field("mandatoryAspects").isNotNull();
        }
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets types with high skipCount and maxItems parameter applied")
    public void getPaginationParameter() throws Exception
    {
        RestAbstractClassModelsCollection types = restClient.authenticateUser(regularUser)
                .withModelAPI()
                .usingParams("maxItems=10&skipCount=10")
                .getTypes();
        types.assertThat().entriesListCountIs(10);
        types.assertThat().paginationField("hasMoreItems").is("true");
        types.assertThat().paginationField("skipCount").is("10");
        types.assertThat().paginationField("maxItems").is("10");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.SITES, TestGroup.REGRESSION })
    @TestRail(section={TestGroup.REST_API, TestGroup.SITES}, executionType= ExecutionType.REGRESSION,
            description= "Verify if any user gets types with hasMoreItems applied bases on skip count and maxItems")
    public void getHighPaginationQuery() throws Exception
    {
        RestAbstractClassModelsCollection types = restClient.authenticateUser(regularUser).withModelAPI()
                .usingParams("maxItems=10&skipCount=150")
                .getTypes();
        types.assertThat().entriesListCountIs(0);
        types.assertThat().paginationField("hasMoreItems").is("false");
        types.assertThat().paginationField("skipCount").is("150");
        types.assertThat().paginationField("maxItems").is("10");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
}