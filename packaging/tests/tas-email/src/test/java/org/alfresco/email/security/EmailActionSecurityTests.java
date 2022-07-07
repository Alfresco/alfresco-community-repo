package org.alfresco.email.security;

import com.google.common.collect.ImmutableMap;
import org.alfresco.email.EmailTest;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
//import org.apache.cxf.common.i18n.Exception;
import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.core.RestWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertTrue;

public class EmailActionSecurityTests extends EmailTest {
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

