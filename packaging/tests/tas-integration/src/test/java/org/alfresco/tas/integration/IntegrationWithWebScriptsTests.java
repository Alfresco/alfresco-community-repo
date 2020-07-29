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
}
