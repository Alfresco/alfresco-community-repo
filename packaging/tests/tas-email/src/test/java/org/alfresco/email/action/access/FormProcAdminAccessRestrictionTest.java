package org.alfresco.email.action.access;

import org.alfresco.email.EmailTest;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.UserModel;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.alfresco.email.action.access.AccessRestrictionUtil.*;
import static org.junit.Assert.assertEquals;

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
    public void adminShouldCreateAMailForm() {
        restClient.authenticateUser(testUser);

        String body = generateBody(createMailParameters(adminUser, testUser));

        String endpoint = String.format(ACTION_FORM_PROCESSOR_ENDPOINT, MAIL_ACTION);

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, body, endpoint);
        RestResponse response = restClient.process(request);

        assertEquals("500", response.getStatusCode());
        response.assertThat().body("message", org.hamcrest.Matchers.containsString(EXPECTED_ERROR_MESSAGE));
    }

    private String generateBody(Map<String, String> mailParameters) {
        JSONObject json = new JSONObject();
        mailParameters.forEach((key, value) -> json.put("prep_" + key, value));

        return json.toJSONString();
    }

    //TODO implement tests

    //restClient.configureRequestSpec().addHeader("Content-Type", "application/json");
    //required unique command
}