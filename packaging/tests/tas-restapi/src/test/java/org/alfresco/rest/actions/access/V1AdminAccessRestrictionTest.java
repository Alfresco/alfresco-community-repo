package org.alfresco.rest.actions.access;

import static org.alfresco.rest.actions.access.AccessRestrictionUtil.ERROR_MESSAGE_ACCESS_RESTRICTED;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.MAIL_ACTION;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.createMailParameters;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
                          .executeAction(MAIL_ACTION, testFolder, createMailParameters(adminUser, testUser));

        restClient.onResponse()
                .assertThat().statusCode(HttpStatus.FORBIDDEN.value())
                .assertThat().body("entry.id", nullValue());
        restClient.assertLastError().containsSummary(ERROR_MESSAGE_ACCESS_RESTRICTED);
    }

    @Test
    public void adminShouldExecuteMailAction() throws Exception {
        restClient.authenticateUser(adminUser)
                          .withCoreAPI()
                          .usingActions()
                          .executeAction(MAIL_ACTION, testFolder, createMailParameters(adminUser, testUser));

        restClient.onResponse()
                .assertThat().statusCode(HttpStatus.ACCEPTED.value())
                .assertThat().body("entry.id", notNullValue());
    }
}
