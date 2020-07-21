package org.alfresco.rest.people;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.exception.JsonToModelConversionException;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.data.DataUser.ListUserWithRoles;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GetSitesMembershipInformationSanityTests extends RestTest
{
    private SiteModel siteModel;
    private UserModel adminUser;
    private ListUserWithRoles usersWithRoles;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        adminUser = dataUser.getAdminUser();
        siteModel = dataSite.usingUser(adminUser).createPublicRandomSite();
        usersWithRoles = dataUser.addUsersWithRolesToSite(siteModel, UserRole.SiteManager, UserRole.SiteCollaborator, UserRole.SiteConsumer,
                UserRole.SiteContributor);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site manager is able to retrieve sites membership information of another user")
    public void siteManagerIsAbleToRetrieveSitesMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingUser(adminUser).getSitesMembershipInformation()
                .assertThat().entriesListIsNotEmpty().and().paginationExist().and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site collaborator is able to retrieve sites membership information of another user")
    public void siteCollaboratorIsAbleToRetrieveSitesMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(adminUser)
                .getSitesMembershipInformation().assertThat().entriesListIsNotEmpty().and().paginationExist().and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site contributor is able to retrieve sites membership information of another user")
    public void siteContributorIsAbleToRetrieveSitesMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingUser(adminUser)
                .getSitesMembershipInformation().assertThat().entriesListIsNotEmpty().and().paginationExist().and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site consumer is able to retrieve sites membership information of another user")
    public void siteConsumerIsAbleToRetrieveSitesMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingUser(adminUser)
                .getSitesMembershipInformation().assertThat().entriesListIsNotEmpty().and().paginationExist().and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify admin is able to retrieve sites membership information of another user")
    public void siteAdminIsAbleToRetrieveSitesMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).getSitesMembershipInformation()
                .assertThat().entriesListIsNotEmpty().and().paginationExist().and().paginationField("count").isNot("0");
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify that unauthenticated user is not able to retrieve sites membership information")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserCannotRetrieveSitesMembershipInformation() throws JsonToModelConversionException, Exception
    {
        UserModel inexistentUser = new UserModel("inexistent user", "wrong password");
        restClient.authenticateUser(inexistentUser).withCoreAPI().usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager))
                .getSitesMembershipInformation();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
}