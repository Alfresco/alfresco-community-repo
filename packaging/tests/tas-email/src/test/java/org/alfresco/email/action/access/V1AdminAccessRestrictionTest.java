package org.alfresco.email.action.access;

import com.google.common.collect.ImmutableMap;
import org.alfresco.rest.RestTest;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.core.RestWrapper;
import org.springframework.beans.factory.annotation.Autowired;

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
        ImmutableMap<String, String> mail = ImmutableMap.of("from", adminUser.getEmailAddress(),
                                                            "to", testUser.getEmailAddress(),
                                                            "cc", testUser.getEmailAddress(),
                                                            "subject", "User email subject",
                                                            "text", "User email sample text");

        restClientAlfresco.authenticateUser(testUser)
                          .withCoreAPI()
                          .usingActions()
                          .executeAction("mail", testFolder, mail);

        // TODO: check proper status code and error message later with Marcin's implementation
        restClientAlfresco.assertStatusCodeIs(HttpStatus.OK);
        restClientAlfresco.onResponse()
                          .assertThat().body("entry.id", org.hamcrest.Matchers.nullValue());
    }

    @Test
    public void adminShouldExecuteMailAction() throws Exception {
        ImmutableMap<String, String> mail = ImmutableMap.of("from", adminUser.getEmailAddress(),
                                                            "to", testUser.getEmailAddress(),
                                                            "cc", testUser.getEmailAddress(),
                                                            "subject", "Admin email subject",
                                                            "text", "Admin email sample text");

        restClientAlfresco.authenticateUser(adminUser)
                          .withCoreAPI()
                          .usingActions()
                          .executeAction("mail", testFolder, mail);

        restClientAlfresco.assertStatusCodeIs(HttpStatus.ACCEPTED);
        restClientAlfresco.onResponse()
                          .assertThat().body("entry.id", org.hamcrest.Matchers.notNullValue());
    }
}

