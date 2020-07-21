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

public class GetSiteMembershipSanityTests extends RestTest
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
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site manager is able to retrieve site membership information of another user")
    public void siteManagerCanRetrieveSiteMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).withCoreAPI().usingUser(adminUser).getSiteMembership(siteModel)
                .assertThat().field("role").is(UserRole.SiteManager).and().field("id").is(siteModel.getId()).and().field("site").isNotEmpty();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site collaborator is able to retrieve site membership information of another user")
    public void siteCollaboratorCanRetrieveSiteMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteCollaborator)).withCoreAPI().usingUser(adminUser)
                .getSiteMembership(siteModel).onSite().assertThat().field("role").is(UserRole.SiteCollaborator).and().field("id").is(siteModel.getId());
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site contributor is able to retrieve site membership information of another user")
    public void siteContributorCanRetrieveSiteMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteContributor)).withCoreAPI().usingUser(adminUser)
                .getSiteMembership(siteModel).assertThat().field("role").is(UserRole.SiteManager).and().field("id").is(siteModel.getId()).and().field("site")
                .isNotEmpty();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify site consumer is able to retrieve site membership information of another user")
    public void siteConsumerCanRetrieveSiteMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(usersWithRoles.getOneUserWithRole(UserRole.SiteConsumer)).withCoreAPI().usingUser(adminUser).getSiteMembership(siteModel)
                .assertThat().field("role").is(UserRole.SiteManager).and().field("id").is(siteModel.getId()).and().field("site").isNotEmpty();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify admin user is able to retrieve site membership information of another user")
    public void adminCanRetrieveSiteMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).getSiteMembership(siteModel)
                .assertThat().field("role").is(UserRole.SiteManager).and().field("id").is(siteModel.getId()).and().field("site").isNotEmpty();
        restClient.assertStatusCodeIs(HttpStatus.OK);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Verify unauthenticated user is not able to retrieve site membership information of another user")
//    @Bug(id="MNT-16904", description = "It fails only on environment with tenants")
    public void unauthenticatedUserCannotRetrieveSiteMembershipInformation() throws JsonToModelConversionException, Exception
    {
        restClient.authenticateUser(new UserModel("random user", "random password")).withCoreAPI()
                .usingUser(usersWithRoles.getOneUserWithRole(UserRole.SiteManager)).getSiteMembership(siteModel);
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }
}