/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rules;

import static org.alfresco.rest.rules.RulesTestsUtils.createRuleModel;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.alfresco.rest.model.RestRuleSetLinkModel;
import org.alfresco.rest.model.RestRuleSetModel;
import org.alfresco.rest.model.RestRuleSetModelsCollection;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for /nodes/{nodeId}/rule-set-links.
 */
@Test (groups = { TestGroup.RULES })
public class RuleSetLinksTests extends RestTest
{
    private UserModel user;
    private SiteModel site;
    private FolderModel ruleFolder;
    private RestRuleModel rule;
    private String ruleSetId;

    @BeforeClass (alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user, site and folder.");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create a rule in the rule folder.");
        RestRuleModel ruleModel = createRuleModel("ruleName");
        rule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                       .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder)
                                                         .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        ruleSetId = ruleSets.getEntries().get(0).onModel().getId();
    }

    /** Check we can link to folder containing a rule set. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void linkToFolderContainingRules()
    {
        STEP("Create another folder in existing site");
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Link to folder");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(ruleFolder.getNodeRef());
        final RestRuleSetLinkModel ruleLink = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).createRuleLink(request);

        STEP("Assert link result");
        restClient.assertStatusCodeIs(CREATED);
        final RestRuleSetLinkModel expectedLink = new RestRuleSetLinkModel();
        expectedLink.setId(ruleSetId);
        ruleLink.assertThat().isEqualTo(expectedLink);

        STEP("Check if folder returns same rules");
        final RestRuleModelsCollection linkedRules = restClient.authenticateUser(user).withCoreAPI()
                .usingNode(folder)
                .usingDefaultRuleSet()
                .getListOfRules();
        linkedRules.assertThat().entriesListCountIs(1);
        linkedRules.getEntries().get(0).onModel().assertThat().isEqualTo(rule);

        STEP("Check if folder returns rule set with linked inclusionType");
        final RestRuleSetModelsCollection linkedRuleSets = restClient.authenticateUser(user).withCoreAPI()
                .usingNode(folder)
                .include("inclusionType")
                .getListOfRuleSets();
        linkedRuleSets.assertThat().entriesListCountIs(1);
        final RestRuleSetModel expectedRuleSet = new RestRuleSetModel();
        expectedRuleSet.setInclusionType("linked");
        linkedRuleSets.getEntries()
                .get(0).onModel().assertThat().isEqualTo(expectedRuleSet, "id", "owningFolder", "model");
    }

    /** Check we can link to a rule set. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void linkToRuleSet()
    {
        STEP("Create another folder in existing site");
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Link to rule set");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(ruleSetId);
        final RestRuleSetLinkModel ruleLink = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).createRuleLink(request);

        STEP("Assert link result");
        restClient.assertStatusCodeIs(CREATED);
        final RestRuleSetLinkModel expectedLink = new RestRuleSetLinkModel();
        expectedLink.setId(ruleSetId);
        ruleLink.assertThat().isEqualTo(expectedLink);

        STEP("Check if folder returns same rules");
        final RestRuleModelsCollection linkedRules = restClient.authenticateUser(user).withCoreAPI()
                .usingNode(folder)
                .usingDefaultRuleSet()
                .getListOfRules();
        linkedRules.assertThat().entriesListCountIs(1);
        linkedRules.getEntries().get(0).onModel().assertThat().isEqualTo(rule);

        STEP("Check if folder returns rule set with linked inclusionType");
        final RestRuleSetModelsCollection likedRuleSets = restClient.authenticateUser(user).withCoreAPI()
                .usingNode(folder)
                .include("inclusionType")
                .getListOfRuleSets();
        likedRuleSets.assertThat().entriesListCountIs(1);
        final RestRuleSetModel expectedRuleSet = new RestRuleSetModel();
        expectedRuleSet.setInclusionType("linked");
        likedRuleSets.getEntries()
                .get(0).onModel().assertThat().isEqualTo(expectedRuleSet, "id", "owningFolder", "model");
    }


    /** Check we get 404 when linking to a non-existing rule set/folder. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void linkToNonExistingRuleSet()
    {
        STEP("Create another folder in existing site");
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Link to non-existing rule set");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId("dummy-rule-set-id");
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder).createRuleLink(request);

        STEP("Assert link result is 404");
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

}
