package org.alfresco.rest.people.activities;

import org.alfresco.dataprep.CMISUtil.DocumentType;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestActivityModelsCollection;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
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
 * @author Cristina Axinte
 *
 */
public class GetPeopleActivitiesCoreTests extends RestTest
{
    UserModel userModel, adminUser, managerUser;
    SiteModel siteModel1, siteModel2;
    FileModel fileInSite1, fileInSite2;
    FolderModel folderInSite2;
    private RestActivityModelsCollection restActivityModelsCollection;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel1 = dataSite.usingUser(userModel).createPublicRandomSite();
        fileInSite1 = dataContent.usingUser(userModel).usingSite(siteModel1).createContent(DocumentType.TEXT_PLAIN);
        
        siteModel2 = dataSite.usingUser(userModel).createPublicRandomSite();
        folderInSite2 = dataContent.usingUser(userModel).usingSite(siteModel2).createFolder(); 
        fileInSite2 = dataContent.usingAdmin().usingSite(siteModel2).createContent(DocumentType.TEXT_PLAIN);   
        
        managerUser = dataUser.createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel2, UserRole.SiteManager);
        
        // only once the activity list is checked with retry in order not to wait the entire list in each test
        // after repo-4250 a file is created first and then update it with content, so there are more entries than previously
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingMe().getPersonActivitiesUntilEntriesCountIs(6);
        restClient.assertStatusCodeIs(HttpStatus.OK);
        restActivityModelsCollection.assertThat().paginationField("count").is("6");
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user cannot get activities for inexistent user with Rest API and response is 404")
    public void userCannotGetPeopleActivitiesForInexistentPersonId() throws Exception
    {
        UserModel inexistentUserName = new UserModel("inexistent", "password");
        
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingUser(inexistentUserName).getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                                .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, inexistentUserName.getUsername()))
                                .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                                .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets its activities for inexistent siteId with Rest API and response is 404")
    public void userGetItsPeopleActivitiesForInexistentSite() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).usingParams(String.format("siteId=inexistent")).getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND)
            .assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
                            .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "inexistent"))
                            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
                            .stackTraceIs(RestErrorModel.STACKTRACE);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets activities with invald skipCount parameter with Rest API and response is 400")
    public void userGetPeopleActivitiesUsingInvalidSkipCountParameter() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingMe().usingParams("skipCount=-1").getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsErrorKey(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                            .containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT)
                            .stackTraceIs(RestErrorModel.STACKTRACE)
                            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE,TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets activities with invalid maxItems parameter with Rest API and response is 400")
    public void userGetPeopleActivitiesUsingInvalidMaxItemsParameter() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingMe().usingParams("maxItems=0").getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsErrorKey(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                        .containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS)
                        .stackTraceIs(RestErrorModel.STACKTRACE)
                        .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets activities using invalid value for parameter 'who'with Rest API and response is 400")
    public void userGetsPeopleActivitiesUsingInvalidValueForWhoParameter() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).usingParams("who=mee").getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST)
            .assertLastError().containsErrorKey(RestErrorModel.INVALID_PARAMETER_WHO)
                    .containsSummary(RestErrorModel.INVALID_PARAMETER_WHO)
                    .stackTraceIs(RestErrorModel.STACKTRACE)
                    .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER);
    }
    
    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.ACTIVITIES, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API,TestGroup.PEOPLE, TestGroup.ACTIVITIES }, executionType = ExecutionType.REGRESSION, description = "Verify user gets activities successfully using parameter 'who' with 'others' value with Rest API")
    public void userGetsPeopleActivitiesUsingOthersForWhoParameter() throws Exception
    {
        restActivityModelsCollection = restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).usingParams("who=others").getPersonActivities();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        // after repo-4250 a file is created first and then update it with content when using dataprep, so there are more entries than previously
        restActivityModelsCollection.assertThat().paginationField("count").is("3");
        restActivityModelsCollection.assertThat().entriesListDoesNotContain("postPersonId", userModel.getUsername().toLowerCase())
                .and().entriesListContains("postPersonId", adminUser.getUsername().toLowerCase())
                .and().entriesListContains("postPersonId", managerUser.getUsername().toLowerCase());
    }
}
