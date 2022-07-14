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
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.alfresco.email.action.access.AccessRestrictionUtil.EXPECTED_ERROR_MESSAGE;
import static org.alfresco.email.action.access.AccessRestrictionUtil.MAIL_ACTION;
import static org.alfresco.email.action.access.AccessRestrictionUtil.createActionWithParameters;
import static org.alfresco.email.action.access.AccessRestrictionUtil.createMailParameters;
import static org.alfresco.email.action.access.AccessRestrictionUtil.getExpectedEmailSendFailureMessage;
import static org.alfresco.email.action.access.AccessRestrictionUtil.mapObjectToJSON;
import static org.hamcrest.Matchers.containsString;

public class V0AdminAccessRestrictionTest extends RestTest {
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
        restClient.configureRequestSpec().setBasePath("");
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body("message", containsString(EXPECTED_ERROR_MESSAGE));
    }

    @Test
    public void adminShouldExecuteMailActionQueue() {
        restClient.authenticateUser(adminUser);

        Action action = createActionWithParameters(MAIL_ACTION, createMailParameters(adminUser, testUser));
        String actionRequestBody = mapObjectToJSON(action);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, actionRequestBody, ACTION_QUEUE_ENDPOINT);
        restClient.configureRequestSpec().setBasePath("");
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body("message", containsString(getExpectedEmailSendFailureMessage(testUser)));
    }
}