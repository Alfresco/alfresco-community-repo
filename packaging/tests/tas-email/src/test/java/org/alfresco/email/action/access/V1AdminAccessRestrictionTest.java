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
    protected RestWrapper restClientAlfresco;

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
        restClientAlfresco.authenticateUser(testUser)
                          .withCoreAPI()
                          .usingActions()
                          .executeAction("mail", testFolder, createMailParameters(adminUser, testUser));

        restClientAlfresco.assertStatusCodeIs(HttpStatus.INTERNAL_SERVER_ERROR);
        restClientAlfresco.assertLastError().containsSummary(EXPECTED_ERROR_MESSAGE);
        restClientAlfresco.onResponse()
                          .assertThat().body("entry.id", org.hamcrest.Matchers.nullValue());
    }

    @Test
    public void adminShouldExecuteMailAction() throws Exception {
        restClientAlfresco.authenticateUser(adminUser)
                          .withCoreAPI()
                          .usingActions()
                          .executeAction("mail", testFolder, createMailParameters(adminUser, testUser));

        restClientAlfresco.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restClientAlfresco.onResponse()
                          .assertThat().body("entry.id", org.hamcrest.Matchers.notNullValue());
    }
}

