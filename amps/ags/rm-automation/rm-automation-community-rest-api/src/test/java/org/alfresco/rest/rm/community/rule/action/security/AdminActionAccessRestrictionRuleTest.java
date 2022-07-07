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
package org.alfresco.rest.rm.community.rule.action.security;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.core.v0.BaseAPI;
import org.alfresco.rest.rm.community.model.rules.RuleDefinition;
import org.alfresco.rest.v0.RulesAPI;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

public class AdminActionAccessRestrictionRuleTest extends RestTest {

    private UserModel adminUser;
    private UserModel testUser;
    private FolderModel testFolder;

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
                BaseAPI.NODE_PREFIX + testFolder.getNodeRef(),
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

        //TODO use admin user here
        rulesAPI.createRule(testUser.getUsername(),
                testUser.getPassword(),
                BaseAPI.NODE_PREFIX + testFolder.getNodeRef(),
                ruleDefinition);

        //TODO: Assert that correct status code was returned
    }
}