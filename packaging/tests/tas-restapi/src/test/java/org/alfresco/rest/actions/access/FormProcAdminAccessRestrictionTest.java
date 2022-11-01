package org.alfresco.rest.actions.access;

import static org.alfresco.rest.actions.access.AccessRestrictionUtil.ERROR_MESSAGE_ACCESS_RESTRICTED;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.ERROR_MESSAGE_FIELD;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.MAIL_ACTION;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.createMailParameters;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.getExpectedEmailSendFailureMessage;
import static org.hamcrest.Matchers.containsString;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.UserModel;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FormProcAdminAccessRestrictionTest extends RestTest {

    private static final String ACTION_FORM_PROCESSOR_ENDPOINT = "alfresco/service/api/action/%s/formprocessor";

    private static final String PROPERTY_PREFIX = "prop_";

    private UserModel adminUser;
    private UserModel testUser;

    @Autowired
    protected RestWrapper restClient;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception {
        adminUser = dataUser.getAdminUser();
        testUser = dataUser.createRandomTestUser();
    }

    @BeforeMethod(alwaysRun=true)
    public void setup() {
        restClient.configureRequestSpec()
                .setBasePath("")
                .addHeader("Content-Type", "application/json");
    }

    @Test
    public void userShouldNotCreateAMailForm() {
        restClient.authenticateUser(testUser);

        String body = generateBody(createMailParameters(adminUser, testUser));
        String endpoint = String.format(ACTION_FORM_PROCESSOR_ENDPOINT, MAIL_ACTION);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, body, endpoint);
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body(ERROR_MESSAGE_FIELD, containsString(ERROR_MESSAGE_ACCESS_RESTRICTED));
    }

    @Test
    public void adminShouldCreateAMailForm() {
        restClient.authenticateUser(adminUser);

        String body = generateBody(createMailParameters(adminUser, testUser));
        String endpoint = String.format(ACTION_FORM_PROCESSOR_ENDPOINT, MAIL_ACTION);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, body, endpoint);
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body(ERROR_MESSAGE_FIELD, containsString(getExpectedEmailSendFailureMessage(testUser)));
    }

    private String generateBody(Map<String, Serializable> mailParameters) {
        JSONObject json = new JSONObject();
        mailParameters.forEach((key, value) -> json.put(PROPERTY_PREFIX + key, value));

        return json.toJSONString();
    }
}
