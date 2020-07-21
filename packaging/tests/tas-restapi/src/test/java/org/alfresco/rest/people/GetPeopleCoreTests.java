package org.alfresco.rest.people;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestErrorModel;
import org.alfresco.utility.constants.UserRole;
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
public class GetPeopleCoreTests extends RestTest
{
    UserModel userModel;
    SiteModel siteModel;
    UserModel searchedUser;
    UserModel adminUser, managerUser;
    UserModel inexistentUser;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
        siteModel = dataSite.usingUser(userModel).createPublicRandomSite();
        searchedUser = dataUser.createRandomTestUser();
        managerUser = dataUser.usingAdmin().createRandomTestUser();
        dataUser.usingUser(userModel).addUserToSite(managerUser, siteModel, UserRole.SiteManager);
        inexistentUser = new UserModel("inexistentUser", "password");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify inexistent user cannot get a person with Rest API and response is 401")
    public void inexistentUserIsUnauthorizedToGetPerson() throws Exception
    {
        restClient.authenticateUser(inexistentUser).withCoreAPI().usingUser(searchedUser).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.REGRESSION })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.REGRESSION, description = "Verify user cannot get a person that doesn't exists with Rest API and response is 404")
    public void userCannotGetInexistentPerson() throws Exception
    {
        restClient.authenticateUser(managerUser).withCoreAPI().usingUser(inexistentUser).getPerson();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
        restClient.assertLastError().containsSummary(String.format(inexistentUser.getUsername(), RestErrorModel.ENTITY_NOT_FOUND));
    }
}
