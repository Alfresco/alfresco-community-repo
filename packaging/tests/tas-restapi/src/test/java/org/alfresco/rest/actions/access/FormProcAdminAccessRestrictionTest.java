package org.alfresco.rest.actions.access;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.UserModel;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.alfresco.rest.actions.access.AccessRestrictionUtil.ERROR_MESSAGE_ACCESS_RESTRICTED;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.ERROR_MESSAGE_FIELD;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.MAIL_ACTION;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.createMailParameters;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.getExpectedEmailSendFailureMessage;
import static org.hamcrest.Matchers.containsString;

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

    @Before
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

    private String generateBody(Map<String, String> mailParameters) {
        JSONObject json = new JSONObject();
        mailParameters.forEach((key, value) -> json.put(PROPERTY_PREFIX + key, value));

        return json.toJSONString();
    }
}
