package org.alfresco.email.action.access;

import org.alfresco.rest.RestTest;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.core.RestWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import static org.alfresco.email.action.access.AccessRestrictionUtil.EXPECTED_ERROR_MESSAGE;
import static org.alfresco.email.action.access.AccessRestrictionUtil.createMailParameters;

public class V1AdminAccessRestrictionTest extends RestTest {

    private UserModel adminUser;
    private UserModel testUser;
    private FolderModel testFolder;

    @Autowired
    protected RestWrapper restClient;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception {
        adminUser = dataUser.getAdminUser();

        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser)
                           .createPublicRandomSite();
        testFolder = dataContent.usingUser(testUser)
                                .usingSite(testSite)
                                .createFolder();
    }

    @Test
    public void userShouldNotExecuteMailAction() throws Exception {
        restClient.authenticateUser(testUser)
                          .withCoreAPI()
                          .usingActions()
                          .executeAction("mail", testFolder, createMailParameters(adminUser, testUser));

        restClient.onResponse()
                .assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body("entry.id", org.hamcrest.Matchers.nullValue());
        restClient.assertLastError().containsSummary(EXPECTED_ERROR_MESSAGE);
    }

    @Test
    public void adminShouldExecuteMailAction() throws Exception {
        restClient.authenticateUser(adminUser)
                          .withCoreAPI()
                          .usingActions()
                          .executeAction("mail", testFolder, createMailParameters(adminUser, testUser));

        restClient.onResponse()
                .assertThat().statusCode(HttpStatus.ACCEPTED.value())
                .assertThat().body("entry.id", org.hamcrest.Matchers.notNullValue());

    }
}

