package org.alfresco.rest.people.deauthorization.community;

import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestAuthKeyModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;

/**
 * Verifies API behavior in community edition. Should be excluded in enterprise edition.
 */
@Test
public class ReauthorizeSanityTests extends RestTest
{
    private UserModel userModel;
    private UserModel adminUser;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY})
    @TestRail(section = {TestGroup.REST_API, TestGroup.PEOPLE}, executionType = ExecutionType.SANITY,
            description = "Check if reauthorization is not implemented in Community Edition")
    public void reauthorizationIsNotImplementedInCommunityEdition()
    {
        // given
        var key = new RestAuthKeyModel();
        key.setAuthorizationKey("am9obnRlc3RAMTIzNDU=");

        // when admin invokes API
        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(userModel).reauthorizeUser(key);
        // then
        restClient.assertStatusCodeIs(HttpStatus.NOT_IMPLEMENTED);

        // when user invokes API
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).reauthorizeUser(key);
        // then
        restClient.assertStatusCodeIs(HttpStatus.NOT_IMPLEMENTED);
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY})
    @TestRail(section = {TestGroup.REST_API, TestGroup.PEOPLE}, executionType = ExecutionType.SANITY,
            description = "Check if the reauthorization code is not implemented in Community Edition")
    public void reauthorizationCodeIsNotImplementedInCommunityEdition()
    {
        // when admin invokes API
        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(userModel).getReauthorizationCode();
        // then
        restClient.assertStatusCodeIs(HttpStatus.NOT_IMPLEMENTED);

        // when user invokes API
        restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).getReauthorizationCode();
        // then
        restClient.assertStatusCodeIs(HttpStatus.NOT_IMPLEMENTED);
    }
}
