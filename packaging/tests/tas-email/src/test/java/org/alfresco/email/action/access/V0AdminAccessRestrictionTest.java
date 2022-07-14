package org.alfresco.email.action.access;

import org.alfresco.email.action.access.pojo.Action;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.email.action.access.AccessRestrictionUtil.*;
import static org.junit.Assert.assertEquals;

public class V0AdminAccessRestrictionTest extends RestTest {
    //TODO implement
    private static final String ACTION_QUEUE_ENDPOINT = "alfresco/service/api/actionQueue?async=false";
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
    public void userShouldNotExecuteMailActionQueue() {
        restClient.authenticateUser(testUser);

        Action action = createActionWithParameters(MAIL_ACTION, createMailParameters(adminUser, testUser));

        String actionRequestBody = mapObjectToJSON(action);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, actionRequestBody, ACTION_QUEUE_ENDPOINT);
        RestResponse response = restClient.process(request);

        assertEquals("500", response.getStatusCode());
        response.assertThat().body("message", org.hamcrest.Matchers.containsString(EXPECTED_ERROR_MESSAGE));
    }

    @Test
    public void adminShouldExecuteMailActionQueue() {
        restClient.authenticateUser(adminUser);

        Action action = createActionWithParameters(MAIL_ACTION, createMailParameters(adminUser, testUser));

        String actionRequestBody = mapObjectToJSON(action);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, actionRequestBody, ACTION_QUEUE_ENDPOINT);
        RestResponse response = restClient.process(request);

        assertEquals("200", response.getStatusCode());
    }
}