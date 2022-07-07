package org.alfresco.email.security;

import com.google.common.collect.ImmutableMap;
import org.alfresco.email.EmailTest;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.rules.ActionsOnRule;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.alfresco.utility.testrail.ExecutionType;
import org.alfresco.utility.testrail.annotation.TestRail;
//import org.apache.cxf.common.i18n.Exception;
import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.alfresco.rest.core.RestWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static org.alfresco.rest.core.v0.BaseAPI.NODE_PREFIX;
import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.CREATED;
import static org.testng.Assert.assertTrue;

public class EmailRulesSecurityTests extends EmailTest {
    @Autowired
    protected RestWrapper restClientAlfresco;

    @Autowired
    private RulesAPI rulesAPI;

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
    public void userShouldNotBeAbleToCreateANewRule() {
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule()
                .title("Mail")
                .description("Trying to test mail")
                .actions(Collections.singletonList("mail"));

        rulesAPI.createRule(testUser.getUsername(),
                testUser.getPassword(),
                NODE_PREFIX + testFolder.getNodeRef(),
                ruleDefinition);

        //TODO: Assert that correct status code was returned
        //TODO: Assert that correct message was returned
    }

    @Test
    public void adminUserShouldSuccessfullyCreateANewRule() {
        RuleDefinition ruleDefinition = RuleDefinition.createNewRule()
                .title("Mail")
                .description("Trying to test mail")
                .actions(Collections.singletonList("mail"));

        rulesAPI.createRule(testUser.getUsername(),
                testUser.getPassword(),
                NODE_PREFIX + testFolder.getNodeRef(),
                ruleDefinition);

        //TODO: Assert that correct status code was returned
    }
}