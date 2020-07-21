package org.alfresco.rest.people;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.alfresco.rest.RestTest;
import org.alfresco.utility.Utility;
import org.alfresco.utility.exception.DataPreparationException;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.ValidatableResponse;

public class AvatarPeopleSanityTest extends RestTest
{

    private UserModel userModel;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws DataPreparationException
    {
        userModel = dataUser.createRandomTestUser();
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY, TestGroup.RENDITIONS })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Update Avatar for People")
    public void updateGetAvatarForPeople() throws Exception
    {
        File avatarFile = Utility.getResourceTestDataFile("avatar.jpg");
        restClient.authenticateUser(userModel);
        ValidatableResponse response = restClient.withCoreAPI().usingAuthUser()
                .uploadAvatarContent(restProperties.envProperty().getFullServerUrl(), avatarFile).statusCode(200);
        // Renditions are async
        Utility.sleep(500, 60000, () ->
        {
            restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().downloadAvatarContent();
            restClient.assertStatusCodeIs(HttpStatus.OK);
        });
        assertNotNull(response.extract().body().asByteArray());
        assertTrue(response.extract().body().asByteArray().length > 0, "Avatar Image not uploaded");
    }

    @Test(groups = { TestGroup.REST_API, TestGroup.PEOPLE, TestGroup.SANITY, TestGroup.RENDITIONS })
    @TestRail(section = { TestGroup.REST_API, TestGroup.PEOPLE }, executionType = ExecutionType.SANITY, description = "Remove Avatar for People")
    public void removeGetAvatarForPeople() throws Exception
    {
        File avatarFile = Utility.getResourceTestDataFile("avatar.jpg");
        restClient.authenticateUser(userModel);
        restClient.withCoreAPI().usingAuthUser().uploadAvatarContent(restProperties.envProperty().getFullServerUrl(), avatarFile).statusCode(200);

        restClient.authenticateUser(userModel);
        restClient.withCoreAPI().usingAuthUser().resetAvatarImageRequest();
        restClient.assertStatusCodeIs(HttpStatus.NO_CONTENT);

        restClient.authenticateUser(userModel).withCoreAPI().usingAuthUser().downloadAvatarContent();
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }

}
