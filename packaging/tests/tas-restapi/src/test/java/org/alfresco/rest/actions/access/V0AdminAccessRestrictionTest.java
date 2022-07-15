package org.alfresco.rest.actions.access;

import org.alfresco.rest.actions.access.pojo.Action;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.alfresco.rest.actions.access.AccessRestrictionUtil.ERROR_MESSAGE_ACCESS_RESTRICTED;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.ERROR_MESSAGE_FIELD;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.MAIL_ACTION;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.createActionWithParameters;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.createMailParameters;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.getExpectedEmailSendFailureMessage;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.mapObjectToJSON;
import static org.hamcrest.Matchers.containsString;

public class V0AdminAccessRestrictionTest extends RestTest {

    private static final String ACTION_QUEUE_ENDPOINT = "alfresco/service/api/actionQueue?async=false";

    private UserModel adminUser;
    private UserModel testUser;

    @Autowired
    protected RestWrapper restClient;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception {
        adminUser = dataUser.getAdminUser();

        testUser = dataUser.createRandomTestUser();
        testSite = dataSite.usingUser(testUser)
                .createPublicRandomSite();
    }

    @BeforeMethod(alwaysRun=true)
    public void setup() {
        restClient.configureRequestSpec().setBasePath("");
    }

    @Test
    public void userShouldNotExecuteMailActionQueue() {
        restClient.authenticateUser(testUser);

        Action action = createActionWithParameters(MAIL_ACTION, createMailParameters(adminUser, testUser));
        String actionRequestBody = mapObjectToJSON(action);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, actionRequestBody, ACTION_QUEUE_ENDPOINT);
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body(ERROR_MESSAGE_FIELD, containsString(ERROR_MESSAGE_ACCESS_RESTRICTED));
    }

    @Test
    public void adminShouldExecuteMailActionQueue() {
        restClient.authenticateUser(adminUser);

        Action action = createActionWithParameters(MAIL_ACTION, createMailParameters(adminUser, testUser));
        String actionRequestBody = mapObjectToJSON(action);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, actionRequestBody, ACTION_QUEUE_ENDPOINT);
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body(ERROR_MESSAGE_FIELD, containsString(getExpectedEmailSendFailureMessage(testUser)));
    }
}
