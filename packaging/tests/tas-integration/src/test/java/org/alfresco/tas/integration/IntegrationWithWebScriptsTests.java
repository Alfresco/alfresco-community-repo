package org.alfresco.tas.integration;

import static org.alfresco.utility.report.log.Step.STEP;

import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import java.net.URLDecoder;
import java.util.HashMap;

import java.util.List;
import org.alfresco.rest.core.JsonBodyGenerator;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.model.RestPersonModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.report.Bug;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegrationWithWebScriptsTests extends IntegrationTest
{
    @Test(groups = { TestGroup.INTEGRATION, TestGroup.REST_API, TestGroup.FULL })
    @TestRail(section = { TestGroup.INTEGRATION,
            TestGroup.REST_API }, executionType = ExecutionType.REGRESSION, description = "Verify when importing multiple users via CSV, if the password is not set in the CSV file, user will be disable")
    public void verifyCSVUserImportDisableUserAndGivesRandomPasswordIfItIsMissing() throws Exception
    {
        STEP("1.Upload the CSV File that contains the users.");
        restAPI.authenticateUser(dataUser.getAdminUser()).configureRequestSpec().addMultiPart("filedata",
                Utility.getResourceTestDataFile("userCSV.csv"));
        String fileCreationWebScript = "alfresco/s/api/people/upload";
        RestAssured.basePath = "";
        restAPI.configureRequestSpec().setBasePath(RestAssured.basePath);
        RestRequest request = RestRequest.simpleRequest(HttpMethod.POST, fileCreationWebScript);
        restAPI.authenticateUser(dataUser.getAdminUser()).process(request);
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        STEP("2.Verify that user1np is disabled and user2 is enabled");
        UserModel disabledUserPerson = new UserModel("MNT-171990-user-with-no-password", "user1");
        UserModel enabledUserPerson = new UserModel("MNT-171990-user-with-password", "user2");
        RestPersonModel personModel = restAPI.authenticateUser(dataUser.getAdminUser()).withCoreAPI()
                .usingUser(new UserModel(disabledUserPerson.getUsername(), disabledUserPerson.getPassword())).getPerson();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        personModel.assertThat().field("enabled").is("false");
        personModel = restAPI.authenticateUser(dataUser.getAdminUser()).withCoreAPI()
                .usingUser(new UserModel(enabledUserPerson.getUsername(), enabledUserPerson.getPassword())).getPerson();
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        personModel.assertThat().field("enabled").is("true");

        STEP("3.Activate the disabled user.");
        HashMap<String, String> input = new HashMap<String, String>();
        input.put("enabled", "true");
        String putBody = JsonBodyGenerator.keyValueJson(input);
        restAPI.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingUser(disabledUserPerson).updatePerson(putBody);
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        STEP("4.Verify that the disabled user has a randomly generated password not the same as firstname(DOCS-2755)");
        restAPI.authenticateUser(disabledUserPerson).withCoreAPI()
                .usingUser(new UserModel(enabledUserPerson.getUsername(), enabledUserPerson.getPassword())).getPerson();
        restAPI.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);

        STEP("5.Change the user password and try an Rest API call.");
        input = new HashMap<String, String>();
        input.put("password", "newPassword1");
        putBody = JsonBodyGenerator.keyValueJson(input);
        restAPI.authenticateUser(dataUser.getAdminUser()).withCoreAPI().usingUser(disabledUserPerson).updatePerson(putBody);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        disabledUserPerson = new UserModel(disabledUserPerson.getUsername(), "newPassword1");
        restAPI.authenticateUser(disabledUserPerson).withCoreAPI()
                .usingUser(new UserModel(enabledUserPerson.getUsername(), enabledUserPerson.getPassword())).getPerson();
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        dataUser.usingAdmin().deleteUser(disabledUserPerson);
        dataUser.usingAdmin().deleteUser(enabledUserPerson);
    }

    @Test(groups = { TestGroup.INTEGRATION, TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @Bug (id ="MNT-19514", status=Bug.Status.FIXED)
    @TestRail(section = { TestGroup.INTEGRATION,
            TestGroup.REST_API }, executionType = ExecutionType.REGRESSION, description = "Verify deauthorized users receives HTTP 401 when making api calls")
    public void verifyDeauthorizedUserReceives401() throws Exception
    {
        STEP("1.Create one authorized user");
        UserModel user =dataUser.createRandomTestUser();
        restAPI.authenticateUser(user).withCoreAPI().getSites();
        restAPI.assertStatusCodeIs(HttpStatus.OK);

        STEP("2.Deauthorize the user");
        String deauthorizeUserWebScript = "alfresco/s/api/deauthorize";
        String body = JsonBodyGenerator.defineJSON().add("username", user.getUsername()).build().toString();
        RestAssured.basePath = "";
        restAPI.configureRequestSpec().setBasePath(RestAssured.basePath);
        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, body, deauthorizeUserWebScript);
        restAPI.authenticateUser(dataUser.getAdminUser()).process(request);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        logger.info(String.format("User %s deauthorized successfully", user.getUsername()));

        STEP("3.Assert deauthorized user receives http 401");
        restAPI.authenticateUser(user).withCoreAPI().getSites();
        restAPI.assertStatusCodeIs(HttpStatus.UNAUTHORIZED);

        String briefSummary = restAPI.onResponse().getResponse().jsonPath().get("error.briefSummary").toString();
        String errorMessage = String.format("The user %s could not perform that action", user.getUsername());
        Assert.assertTrue(briefSummary.endsWith(errorMessage));
    }

    @Test(groups = { TestGroup.INTEGRATION, TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @TestRail(section = { TestGroup.INTEGRATION,
            TestGroup.REST_API }, executionType = ExecutionType.REGRESSION, description = "Verify cluster check not blocked by CSRF filter")
    public void verifyClusterCheck() throws Exception
    {
        RestAssured.basePath = "";
        restAPI.configureRequestSpec().setBasePath(RestAssured.basePath);
        String csrfCookieName = "alf-csrftoken";
        String jsessionidCookieName = "JSESSIONID";
        STEP("1. Get alf-csrftoken and JSESSIONID cookies");
        String validateClusterPage = "alfresco/s/enterprise/admin/admin-clustering";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, validateClusterPage);
        Response resp = restAPI.authenticateUser(dataUser.getAdminUser()).process(request).getResponse();
        String jsessionidCookieValue = resp.getCookies().get(jsessionidCookieName);
        String csrfCookieValue = resp.getCookies().get(csrfCookieName);
        Assert.assertNotNull(csrfCookieValue, "CSRF cookie (" + csrfCookieName + ") should be present");
        Assert.assertNotNull(jsessionidCookieValue, "JSESSIONID cookie (" + jsessionidCookieName + ") should be present");

        STEP("2. Make a POST request to admin console cluster validation");
        String validateClusterWebscript = "alfresco/s/enterprise/admin/admin-clustering-test";
        // Post with empty body
        String body = JsonBodyGenerator.defineJSON().build().toString();
        request = RestRequest.requestWithBody(HttpMethod.POST, body, validateClusterWebscript);
        restAPI.configureRequestSpec()
                .addCookie(new Cookie.Builder(csrfCookieName, csrfCookieValue).build())
                .addCookie(new Cookie.Builder(jsessionidCookieName, jsessionidCookieValue).build())
                .addHeader(csrfCookieName, URLDecoder.decode(csrfCookieValue, "UTF-8"))
                .build();
        restAPI.authenticateUser(dataUser.getAdminUser()).process(request);
        restAPI.assertStatusCodeIs(HttpStatus.OK);
        logger.info("Cluster validation triggered successfully");
    }

    @Test(groups = { TestGroup.INTEGRATION, TestGroup.REST_API, TestGroup.FULL, TestGroup.ENTERPRISE})
    @TestRail(section = { TestGroup.INTEGRATION,
            TestGroup.REST_API }, executionType = ExecutionType.REGRESSION, description = "Verify that JSESSIONID remains the same")
    public void verifySessionPersistence() throws Exception
    {
        RestAssured.basePath = "";
        restAPI.configureRequestSpec().setBasePath(RestAssured.basePath);
        String jsessionidCookieName = "JSESSIONID";
        String csrfCookieName = "alf-csrftoken";
        STEP("1. Get JSESSIONID cookie");
        // The URL does not matter, the user needs to authenticated
        UserModel adminUser = dataUser.getAdminUser();
        String authHeader = String.format("%s:%s", adminUser.getUsername(), adminUser.getPassword());
        String authHeaderEncoded = new String(Base64.encodeBase64(authHeader.getBytes()));
        restAPI.configureRequestSpec()
                .addHeader("Authorization", String.format("Basic %s", authHeaderEncoded))
                .build();
        String clusteringPage = "alfresco/s/enterprise/admin/admin-clustering";
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, clusteringPage);
        Response resp = restAPI.process(request).getResponse();
        String jsessionidCookieValue = resp.getCookies().get(jsessionidCookieName);
        String csrfCookieValue = resp.getCookies().get(csrfCookieName);
        Assert.assertNotNull(jsessionidCookieValue, "Cookie " + jsessionidCookieName + " should be present");
        Assert.assertNotNull(csrfCookieValue, "Cookie " + csrfCookieName + " should be present");

        STEP("2. Verify JSESSIONID is not set again");
        restAPI.configureRequestSpec()
                .addCookie(new Cookie.Builder(csrfCookieName, csrfCookieValue).build())
                .addCookie(new Cookie.Builder(jsessionidCookieName, jsessionidCookieValue).build())
                .addHeader(csrfCookieName, URLDecoder.decode(csrfCookieValue, "UTF-8"))
                .addHeader("Authorization", String.format("Basic %s", authHeaderEncoded))
                .build();
        String systemSettingPage = "alfresco/s/enterprise/admin/admin-systemsettings";
        request = RestRequest.simpleRequest(HttpMethod.GET, systemSettingPage);
        resp = restAPI.process(request).getResponse();
        List<String> setCookieHeaderValues = resp.getHeaders().getValues("Set-Cookie");
        Assert.assertFalse(setCookieHeaderValues.stream().anyMatch(s -> s.startsWith("JSESSIONID=")),
                "JSESSIONID cookie should not be set again");
        logger.info("Session persistence was validated successfully");
    }
}
