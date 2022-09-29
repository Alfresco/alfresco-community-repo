/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rules;

import static org.alfresco.rest.rules.RulesTestsUtils.AUDIO_ASPECT;
import static org.alfresco.rest.rules.RulesTestsUtils.createRuleExecutionRequest;
import static org.alfresco.rest.rules.RulesTestsUtils.createRuleModelWithDefaultValues;
import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for POST /nodes/{nodeId}/rule-executions.
 */
@Test(groups = { TestGroup.RULES})
public class ExecuteRulesTests extends RestTest
{
    private UserModel user;
    private SiteModel site;
    private FolderModel ruleFolder;
    private FileModel file;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user, site, folder and file in it");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        file = dataContent.usingUser(user).usingResource(ruleFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Create one rule with add audio aspect action in the folder");
        RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);
    }

    /**
     * Execute one rule with one action trying to add audio aspect to a file
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void executeOneRuleWithOneAction() throws InterruptedException
    {
        STEP("Check if file aspects don't contain Audio one");
        RestNodeModel node = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        node.assertThat().field("aspectNames").notContains(AUDIO_ASPECT);

        STEP("Execute rule");
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).executeRules(createRuleExecutionRequest());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("Check if file contains Audio aspect");
        Utility.sleep(500, 10000, () -> {
            RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(file).getNode();
            restClient.assertStatusCodeIs(HttpStatus.OK);
            fileNode.assertThat().field("aspectNames").contains(AUDIO_ASPECT);
        });
    }

    // TODO add more E2Es. For more see: ACS-3620
}
