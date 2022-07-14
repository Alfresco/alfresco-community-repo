/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.email.action.access;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.email.action.access.pojo.Rule;
import org.alfresco.rest.RestTest;
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
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.alfresco.email.action.access.AccessRestrictionUtil.EXPECTED_ERROR_MESSAGE;
import static org.alfresco.email.action.access.AccessRestrictionUtil.MAIL_ACTION;
import static org.alfresco.email.action.access.AccessRestrictionUtil.createMailParameters;
import static org.alfresco.email.action.access.AccessRestrictionUtil.createRuleWithAction;
import static org.alfresco.email.action.access.AccessRestrictionUtil.mapObjectToJSON;
import static org.junit.Assert.assertEquals;

public class RuleAdminAccessRestrictionTest extends RestTest {

    private static final String CREATE_RULE_ENDPOINT = "alfresco/service/api/node/workspace/SpacesStore/%s/ruleset/rules";

    private UserModel adminUser;
    private UserModel testUser;
    private FolderModel testFolder;

    private FileModel testFile;

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
    public void userShouldNotBeAbleToCreateANewRule() {
        restClient.authenticateUser(testUser);

        Rule rule = createRuleWithAction(MAIL_ACTION, createMailParameters(adminUser, testUser));
        String ruleRequestBody = mapObjectToJSON(rule);
        String ruleEndpoint = String.format(CREATE_RULE_ENDPOINT, testFolder.getNodeRef());

        RestRequest request = RestRequest.requestWithBody(HttpMethod.POST, ruleRequestBody, ruleEndpoint);
        RestResponse response = restClient.process(request);

        response.assertThat().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .assertThat().body("message", org.hamcrest.Matchers.containsString(EXPECTED_ERROR_MESSAGE));
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