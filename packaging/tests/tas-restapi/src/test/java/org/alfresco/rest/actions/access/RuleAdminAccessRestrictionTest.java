package org.alfresco.rest.actions.access;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.actions.access.pojo.Rule;
import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestResponse;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
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
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.createMailParameters;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.createRuleWithAction;
import static org.alfresco.rest.actions.access.AccessRestrictionUtil.mapObjectToJSON;
import static org.hamcrest.Matchers.containsString;

public class RuleAdminAccessRestrictionTest extends RestTest {

    private static final String CREATE_RULE_ENDPOINT = "alfresco/service/api/node/workspace/SpacesStore/%s/ruleset/rules";

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

    @BeforeMethod(alwaysRun=true)
    public void setup() {
        restClient.configureRequestSpec().setBasePath("");
    }

    @Test
    public void userShouldNotBeAbleToCreateANewRule() {
        restClient.authenticateUser(testUser);

        Rule rule = createRuleWithAction(MAIL_ACTION, createMailParameters(adminUser, testUser));
        String ruleRequestBody = mapObjectToJSON(rule);
        String ruleEndpoint = String.format(CREATE_RULE_ENDPOINT, testFolder.getNodeRef());

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, ruleRequestBody, ruleEndpoint);
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body(ERROR_MESSAGE_FIELD, containsString(ERROR_MESSAGE_ACCESS_RESTRICTED));
    }

    @Test
    public void adminShouldBeAbleToCreateANewRule() {
        restClient.authenticateUser(adminUser);

        Rule rule = createRuleWithAction(MAIL_ACTION, createMailParameters(adminUser, testUser));
        String ruleRequestBody = mapObjectToJSON(rule);
        String ruleEndpoint = String.format(CREATE_RULE_ENDPOINT, testFolder.getNodeRef());

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, ruleRequestBody, ruleEndpoint);
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.OK.value());
    }

    @Test
    public void userShouldAddAFileToFolderWithMailRule() {
        restClient.authenticateUser(adminUser);

        Rule rule = createRuleWithAction(MAIL_ACTION, createMailParameters(adminUser, testUser));
        String ruleRequestBody = mapObjectToJSON(rule);
        String ruleEndpoint = String.format(CREATE_RULE_ENDPOINT, testFolder.getNodeRef());

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, ruleRequestBody, ruleEndpoint);
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.OK.value());

        dataContent.usingUser(testUser)
                .usingSite(testSite)
                .usingResource(testFolder)
                .createContent(FileModel.getRandomFileModel(FileType.TEXT_PLAIN));
    }
}
