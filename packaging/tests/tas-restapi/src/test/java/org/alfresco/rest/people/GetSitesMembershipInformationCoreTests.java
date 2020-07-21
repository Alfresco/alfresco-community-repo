package org.alfresco.rest.people;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetSitesMembershipInformationCoreTests extends RestTest
{
    private SiteModel publicSiteModel;
    private SiteModel moderatedSiteModel;
    private SiteModel privateSiteModel;
    private UserModel userModel;
    UserModel leaveSiteUserModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        userModel = dataUser.createRandomTestUser();
        leaveSiteUserModel = dataUser.createRandomTestUser();
        publicSiteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        moderatedSiteModel = dataSite.usingUser(userModel).createModeratedRandomSite();
        privateSiteModel = dataSite.usingUser(userModel).createPrivateRandomSite();
        dataUser.addUserToSite(leaveSiteUserModel, publicSiteModel, UserRole.SiteCollaborator);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager is not able to retrieve site membership information for a personId that does not exist")
    public void siteManagerCantRetrieveSiteMembershipInformationForAPersonIdThatDoesNotExist() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(new UserModel("invalidPersonId", "password")).getSitesMembershipInformation()
                .assertThat().entriesListIsEmpty();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        restClient.assertLastError().containsErrorKey(RestErrorModel.ENTITY_NOT_FOUND_ERRORKEY)
            .containsSummary(String.format(RestErrorModel.ENTITY_NOT_FOUND, "invalidPersonId"))
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
            .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify if get site membership information request returns status code 400 when invalid maxItems parameter is used")
    public void getSiteMembershipInformationRequestReturns400ForInvalidMaxItemsParameter() throws Exception
    {
        restClient.authenticateUser(userModel).withParams("maxItems=0").withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat()
                .entriesListIsEmpty();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);

        restClient.withParams("maxItems=-1").withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat().entriesListIsEmpty();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError().containsSummary(RestErrorModel.ONLY_POSITIVE_VALUES_MAXITEMS);

        restClient.withParams("maxItems=test").withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat().entriesListIsEmpty();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError().containsSummary(String.format(RestErrorModel.INVALID_MAXITEMS, "test"));
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify if get site membership information request returns status code 200 for valid maxItems parameter")
    public void getSiteMembershipInformationRequestReturns200ForValidMaxItemsParameter() throws Exception
    {
        restClient.authenticateUser(userModel).withParams("maxItems=5").withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat()
                .entriesListIsNotEmpty().getPagination().assertThat().field("maxItems").is("5");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify if get site membership information request returns status code 400 when invalid skipCount parameter is used")
    public void getSiteMembershipInformationRequestReturns400ForInvalidSkipCountParameter() throws Exception
    {
        restClient.authenticateUser(userModel).withParams("skipCount=-1").withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat()
                .entriesListIsEmpty();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError().containsSummary(RestErrorModel.NEGATIVE_VALUES_SKIPCOUNT);

        restClient.withParams("skipCount=test").withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat().entriesListIsEmpty();
        restClient.assertStatusCodeIs(HttpStatus.BAD_REQUEST);
        restClient.assertLastError().containsErrorKey(String.format(RestErrorModel.INVALID_SKIPCOUNT, "test"))
            .containsSummary(String.format(RestErrorModel.INVALID_SKIPCOUNT, "test"))
            .descriptionURLIs(RestErrorModel.RESTAPIEXPLORER)
            .stackTraceIs(RestErrorModel.STACKTRACE);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify if get site membership information request returns status code 200 for valid skipCount parameter")
    public void getSiteMembershipInformationRequestReturns200ForValidSkipCountParameter() throws Exception
    {
        restClient.authenticateUser(userModel).withParams("skipCount=1").withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat()
                .entriesListIsNotEmpty().getPagination().assertThat().field("skipCount").is("1");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager is able to retrieve public sites")
    public void siteManagerCheckThatPublicSitesAreRetrieved() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat()
                .entriesListContains("id", publicSiteModel.getId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager is able to retrieve moderated sites")
    public void siteManagerCheckThatModeratedSitesAreRetrieved() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat()
                .entriesListContains("id", moderatedSiteModel.getId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site manager is able to retrieve private sites")
    public void siteManagerCheckThatPrivateSitesAreRetrieved() throws Exception
    {
        restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat()
                .entriesListContains("id", privateSiteModel.getId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify site member is able to leave site")
    public void siteMemberIsAbleToLeaveSite() throws Exception
    {
        restClient.authenticateUser(leaveSiteUserModel).withCoreAPI().usingAuthUser().deleteSiteMember(publicSiteModel);
        restClient.authenticateUser(leaveSiteUserModel).withCoreAPI().usingAuthUser().getSitesMembershipInformation().assertThat().entriesListIsEmpty();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }
}
