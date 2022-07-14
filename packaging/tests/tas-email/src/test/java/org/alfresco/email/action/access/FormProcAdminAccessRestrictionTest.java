package org.alfresco.email.action.access;

import org.alfresco.email.EmailTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.UserModel;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.alfresco.email.action.access.AccessRestrictionUtil.EXPECTED_ERROR_MESSAGE;
import static org.alfresco.email.action.access.AccessRestrictionUtil.MAIL_ACTION;
import static org.alfresco.email.action.access.AccessRestrictionUtil.createMailParameters;
import static org.alfresco.email.action.access.AccessRestrictionUtil.getExpectedEmailSendFailureMessage;
import static org.hamcrest.Matchers.containsString;

public class FormProcAdminAccessRestrictionTest extends EmailTest {

    private static final String ACTION_FORM_PROCESSOR_ENDPOINT = "alfresco/service/api/action/%s/formprocessor";

    private UserModel adminUser;
    private UserModel testUser;

    @Autowired
    protected RestWrapper restClient;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation() throws Exception {
        adminUser = dataUser.getAdminUser();
        testUser = dataUser.createRandomTestUser();
    }
    @Test
    public void userShouldNotCreateAMailForm() {
        restClient.authenticateUser(testUser);

        String body = generateBody(createMailParameters(adminUser, testUser));
        String endpoint = String.format(ACTION_FORM_PROCESSOR_ENDPOINT, MAIL_ACTION);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, body, endpoint);
        restClient.configureRequestSpec().addHeader("Content-Type", "application/json")
                                         .setBasePath("");
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body("message", containsString(EXPECTED_ERROR_MESSAGE));
    }

    @Test
    public void adminShouldCreateAMailForm() {
        restClient.authenticateUser(adminUser);

        String body = generateBody(createMailParameters(adminUser, testUser));
        String endpoint = String.format(ACTION_FORM_PROCESSOR_ENDPOINT, MAIL_ACTION);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, body, endpoint);
        restClient.configureRequestSpec().addHeader("Content-Type", "application/json")
                                         .setBasePath("");
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body("message", containsString(getExpectedEmailSendFailureMessage(testUser)));
    }

    private String generateBody(Map<String, String> mailParameters) {
        JSONObject json = new JSONObject();
        mailParameters.forEach((key, value) -> json.put("prop_" + key, value));

        return json.toJSONString();
    }
}