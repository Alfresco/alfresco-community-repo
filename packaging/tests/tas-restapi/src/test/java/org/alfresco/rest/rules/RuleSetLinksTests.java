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
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.alfresco.rest.model.RestRuleSetLinkModel;
import org.alfresco.rest.model.RestRuleSetModel;
import org.alfresco.rest.model.RestRuleSetModelsCollection;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for /nodes/{nodeId}/rule-set-links.
 */
@Test(groups = {TestGroup.RULES})
public class RuleSetLinksTests extends RestTest
{
    private UserModel user;
    private SiteModel site;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user and site.");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    /**
     * Check we can link to folder containing a rule set.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void linkToFolderContainingRules()
    {
        STEP("Create folders in existing site");
        final FolderModel ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create a rule in the rule folder.");
        RestRuleModel ruleModel = createRuleModel("ruleName");
        RestRuleModel rule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        final RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder)
                .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        final String ruleSetId = ruleSets.getEntries().get(0).onModel().getId();

        STEP("Link to a rule folder");
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
        expectedRuleSet.setId(ruleSetId);
        expectedRuleSet.setInclusionType("linked");
        linkedRuleSets.getEntries()
                .get(0).onModel().assertThat().isEqualTo(expectedRuleSet);
    }

    /**
     * Check we can link to a rule set.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void linkToRuleSet()
    {
        STEP("Create folders in existing site");
        final FolderModel ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create a rule in the rule folder.");
        RestRuleModel ruleModel = createRuleModel("ruleName");
        RestRuleModel rule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        final RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder)
                .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        final String ruleSetId = ruleSets.getEntries().get(0).onModel().getId();

        STEP("Link to a rule set");
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
        expectedRuleSet.setId(ruleSetId);
        expectedRuleSet.setInclusionType("linked");
        likedRuleSets.getEntries()
                .get(0).onModel().assertThat().isEqualTo(expectedRuleSet);
    }


    /**
     * Check we get 404 when linking to a non-existing rule set/folder.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void linkToNonExistingRuleSet()
    {
        STEP("Create a folder in existing site");
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Link to non-existing rule set");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId("dummy-rule-set-id");
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder).createRuleLink(request);

        STEP("Assert link result is 404");
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /**
     * Check we get bad request error when linking to a folder without rules.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void linkToFolderWithoutRules()
    {
        STEP("Create 2 folders without rules in existing site");
        final FolderModel folder1 = dataContent.usingUser(user).usingSite(site).createFolder();
        final FolderModel folder2 = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Link to a folder without rules");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(folder2.getNodeRef());
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder1).createRuleLink(request);

        STEP("Assert link result is 400");
        restClient.assertStatusCodeIs(BAD_REQUEST)
                .assertLastError().containsSummary("The target node has no rules to link.");
    }

    /**
     * Check we get bad request error when linking from a folder which already has rules.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void linkFromFolderWithRules()
    {
        STEP("Create folders in existing site");
        final FolderModel folder1 = dataContent.usingUser(user).usingSite(site).createFolder();
        final FolderModel folder2 = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create rules in both folders.");
        RestRuleModel ruleModel1 = createRuleModel("ruleName1");
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder1).usingDefaultRuleSet()
                .createSingleRule(ruleModel1);
        RestRuleModel ruleModel2 = createRuleModel("ruleName2");
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder2).usingDefaultRuleSet()
                .createSingleRule(ruleModel2);

        STEP("Link from a folder with rules");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(folder2.getNodeRef());
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder1).createRuleLink(request);

        STEP("Assert link result is 400");
        restClient.assertStatusCodeIs(BAD_REQUEST)
                .assertLastError().containsSummary(
                "Unable to link to a ruleset because the folder has pre-existing rules or is already linked to a ruleset.");
    }

    /**
     * Check we get bad request error when linking to a file node.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void linkToFileNode()
    {
        STEP("Create a folder in existing site");
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        final FileModel fileContent = dataContent.usingUser(user).usingSite(site).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Link to a file node");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(fileContent.getNodeRef());
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder).createRuleLink(request);

        STEP("Assert link result is 404");
        restClient.assertStatusCodeIs(BAD_REQUEST)
                .assertLastError().containsSummary("NodeId of a folder is expected!");
    }

    /**
     * Check we can link to a parent folder with rules.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void linkToParentNodeWithRules()
    {
        STEP("Create parent/child folders in existing site");
        final FolderModel parentFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        final FolderModel childFolder = dataContent.usingUser(user).usingSite(site).usingResource(parentFolder).createFolder();

        STEP("Create a rule in the parent folder.");
        RestRuleModel ruleModel = createRuleModel("ruleName");
        RestRuleModel rule = restClient.authenticateUser(user).withCoreAPI().usingNode(parentFolder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        final RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withCoreAPI().usingNode(parentFolder)
                .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        final String ruleSetId = ruleSets.getEntries().get(0).onModel().getId();

        STEP("Link to the parent folder");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(parentFolder.getNodeRef());
        final RestRuleSetLinkModel ruleLink = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolder).createRuleLink(request);

        STEP("Assert link result");
        restClient.assertStatusCodeIs(CREATED);
        final RestRuleSetLinkModel expectedLink = new RestRuleSetLinkModel();
        expectedLink.setId(ruleSetId);
        ruleLink.assertThat().isEqualTo(expectedLink);

        STEP("Check if child folder returns same rules");
        final RestRuleModelsCollection linkedRules = restClient.authenticateUser(user).withCoreAPI()
                .usingNode(childFolder)
                .usingDefaultRuleSet()
                .getListOfRules();
        linkedRules.assertThat().entriesListCountIs(1);
        linkedRules.getEntries().get(0).onModel().assertThat().isEqualTo(rule);

        STEP("Check if child folder returns rule set with linked inclusionType");
        final RestRuleSetModelsCollection linkedRuleSets = restClient.authenticateUser(user).withCoreAPI()
                .usingNode(childFolder)
                .include("inclusionType")
                .getListOfRuleSets();
        linkedRuleSets.assertThat().entriesListCountIs(1);
        final RestRuleSetModel expectedRuleSet = new RestRuleSetModel();
        expectedRuleSet.setId(ruleSetId);
        expectedRuleSet.setInclusionType("linked");
        linkedRuleSets.getEntries()
                .get(0).onModel().assertThat().isEqualTo(expectedRuleSet);
    }

}
