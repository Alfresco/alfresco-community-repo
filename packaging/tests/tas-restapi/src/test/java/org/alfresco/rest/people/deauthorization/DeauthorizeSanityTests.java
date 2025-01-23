package org.alfresco.rest.people.deauthorization;

import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.RestTest;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;

@Test
public class DeauthorizeSanityTests extends RestTest
{
    private UserModel userModel, adminUser;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        adminUser = dataUser.getAdminUser();
        userModel = dataUser.createRandomTestUser();
    }

    @Test(groups = {TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY})
    @TestRail(section = {TestGroup.REST_API, TestGroup.PEOPLE}, executionType = ExecutionType.SANITY,
            description = "Check if de-authorization is not implemented in Community Edition")
    public void deauthorizationIsNotImplementedInCommunityEdition()
    {
        restClient.authenticateUser(adminUser).withCoreAPI().usingUser(userModel).deauthorizeUser();
        restClient.assertStatusCodeIs(HttpStatus.NOT_IMPLEMENTED);

        restClient.authenticateUser(userModel).withCoreAPI().usingUser(userModel).deauthorizeUser();
        restClient.assertStatusCodeIs(HttpStatus.NOT_IMPLEMENTED);
    }
}
