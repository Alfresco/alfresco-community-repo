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

import static org.alfresco.utility.constants.UserRole.SiteConsumer;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.dataprep.CMISUtil;
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
public class RuleSetLinksTests extends RulesRestTest
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
        RestRuleModel ruleModel = rulesUtils.createRuleModel("ruleName");
        RestRuleModel rule = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        final RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        final String ruleSetId = ruleSets.getEntries().get(0).onModel().getId();

        STEP("Link to a rule folder");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(ruleFolder.getNodeRef());
        final RestRuleSetLinkModel ruleLink = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).createRuleLink(request);

        STEP("Assert link result");
        restClient.assertStatusCodeIs(CREATED);
        final RestRuleSetLinkModel expectedLink = new RestRuleSetLinkModel();
        expectedLink.setId(ruleSetId);
        ruleLink.assertThat().isEqualTo(expectedLink);

        STEP("Check if folder returns same rules");
        final RestRuleModelsCollection linkedRules = restClient.authenticateUser(user).withPrivateAPI()
                .usingNode(folder)
                .usingDefaultRuleSet()
                .getListOfRules();
        linkedRules.assertThat().entriesListCountIs(1);
        linkedRules.getEntries().get(0).onModel().assertThat().isEqualTo(rule);

        STEP("Check if folder returns rule set with linked inclusionType");
        final RestRuleSetModelsCollection linkedRuleSets = restClient.authenticateUser(user).withPrivateAPI()
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
        RestRuleModel ruleModel = rulesUtils.createRuleModel("ruleName");
        RestRuleModel rule = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        final RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        final String ruleSetId = ruleSets.getEntries().get(0).onModel().getId();

        STEP("Link to a rule set");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(ruleSetId);
        final RestRuleSetLinkModel ruleLink = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).createRuleLink(request);

        STEP("Assert link result");
        restClient.assertStatusCodeIs(CREATED);
        final RestRuleSetLinkModel expectedLink = new RestRuleSetLinkModel();
        expectedLink.setId(ruleSetId);
        ruleLink.assertThat().isEqualTo(expectedLink);

        STEP("Check if folder returns same rules");
        final RestRuleModelsCollection linkedRules = restClient.authenticateUser(user).withPrivateAPI()
                .usingNode(folder)
                .usingDefaultRuleSet()
                .getListOfRules();
        linkedRules.assertThat().entriesListCountIs(1);
        linkedRules.getEntries().get(0).onModel().assertThat().isEqualTo(rule);

        STEP("Check if folder returns rule set with linked inclusionType");
        final RestRuleSetModelsCollection likedRuleSets = restClient.authenticateUser(user).withPrivateAPI()
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
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).createRuleLink(request);

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
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder1).createRuleLink(request);

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
        RestRuleModel ruleModel1 = rulesUtils.createRuleModel("ruleName1");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder1).usingDefaultRuleSet()
                .createSingleRule(ruleModel1);
        RestRuleModel ruleModel2 = rulesUtils.createRuleModel("ruleName2");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder2).usingDefaultRuleSet()
                .createSingleRule(ruleModel2);

        STEP("Link from a folder with rules");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(folder2.getNodeRef());
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder1).createRuleLink(request);

        STEP("Assert link result is 400");
        restClient.assertStatusCodeIs(BAD_REQUEST)
                .assertLastError().containsSummary(
                "Unable to link to a rule set because the folder has pre-existing rules or is already linked to a rule set.");
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
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).createRuleLink(request);

        STEP("Assert link result is 400");
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
        RestRuleModel ruleModel = rulesUtils.createRuleModel("ruleName");
        RestRuleModel rule = restClient.authenticateUser(user).withPrivateAPI().usingNode(parentFolder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        final RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI().usingNode(parentFolder)
                .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        final String ruleSetId = ruleSets.getEntries().get(0).onModel().getId();

        STEP("Link to the parent folder");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(parentFolder.getNodeRef());
        final RestRuleSetLinkModel ruleLink = restClient.authenticateUser(user).withPrivateAPI().usingNode(childFolder).createRuleLink(request);

        STEP("Assert link result");
        restClient.assertStatusCodeIs(CREATED);
        final RestRuleSetLinkModel expectedLink = new RestRuleSetLinkModel();
        expectedLink.setId(ruleSetId);
        ruleLink.assertThat().isEqualTo(expectedLink);

        STEP("Check if child folder returns same rules");
        final RestRuleModelsCollection linkedRules = restClient.authenticateUser(user).withPrivateAPI()
                .usingNode(childFolder)
                .usingDefaultRuleSet()
                .getListOfRules();
        linkedRules.assertThat().entriesListCountIs(1);
        linkedRules.getEntries().get(0).onModel().assertThat().isEqualTo(rule);

        STEP("Check if child folder returns rule set with linked inclusionType");
        final RestRuleSetModelsCollection linkedRuleSets = restClient.authenticateUser(user).withPrivateAPI()
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

    /**
     * Check we get an error when trying to link to a rule set that we can't view.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void linkToRuleSetWithoutPermission()
    {
        STEP("Use admin to create a private site with a folder containing a rule.");
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingAdmin().usingSite(privateSite).createFolder();
        restClient.authenticateUser(dataUser.getAdminUser()).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet()
                  .createSingleRule(rulesUtils.createRuleModelWithDefaultValues());

        STEP("Use a normal user to try to link to the rule.");
        FolderModel publicFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(privateFolder.getNodeRef());
        restClient.authenticateUser(user).withPrivateAPI().usingNode(publicFolder).createRuleLink(request);

        restClient.assertStatusCodeIs(FORBIDDEN);
    }

    /**
     * Check we are able to link to a rule set with only read permission.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void linkToRuleSetWithOnlyReadPermission()
    {
        STEP("Use admin to create a private site with a folder containing a rule.");
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingAdmin().usingSite(privateSite).createFolder();
        restClient.authenticateUser(dataUser.getAdminUser()).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet()
                  .createSingleRule(rulesUtils.createRuleModelWithDefaultValues());

        STEP("Add the normal user as a consumer.");
        dataUser.usingAdmin().addUserToSite(user, privateSite, SiteConsumer);

        STEP("Use a normal user to try to link to the rule.");
        FolderModel publicFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(privateFolder.getNodeRef());
        restClient.authenticateUser(user).withPrivateAPI().usingNode(publicFolder).createRuleLink(request);

        restClient.assertStatusCodeIs(CREATED);
    }

    /**
     * Check we can DELETE/unlink a ruleset
     *
     * DELETE /nodes/{folderNodeId}/rule-set-links/{ruleSetId}.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void unlinkRuleSet()
    {
        STEP("Create folders in existing site");
        final FolderModel ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create a rule in the rule folder.");
        RestRuleModel ruleModel = rulesUtils.createRuleModel("ruleName");
        RestRuleModel rule = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        final RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        final String ruleSetId = ruleSets.getEntries().get(0).onModel().getId();

        STEP("Link to a rule folder");
        final RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(ruleFolder.getNodeRef());
        final RestRuleSetLinkModel ruleLink = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).createRuleLink(request);

        STEP("Unlink the rule set");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).unlinkRuleSet(ruleSetId);

        STEP("Assert unlink result");
        restClient.assertStatusCodeIs(NO_CONTENT);

        STEP("GET the rule set and isLinkedTo field.");
        RestRuleSetModel ruleSet =  restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                .include("isLinkedTo", "linkedToBy", "owningFolder")
                .getDefaultRuleSet();

        STEP("Assert linkedTo is false.");
        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("isLinkedTo").is(false)
                .assertThat().field("linkedToBy").isEmpty();
    }

    /**
     * Check a 400 is thrown when using folder/content id instead of a ruleSetId.
     *
     * DELETE /nodes/{folderNodeId}/rule-set-links/{ruleSetId}
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void unlinkUsingDocumentId()
    {
        STEP("Create folders in existing site");
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Attempt to unlink the rule set");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).unlinkRuleSet(folder.getNodeRef());

        STEP("Assert unlink result");
        restClient.assertStatusCodeIs(BAD_REQUEST)
                  .assertLastError().containsSummary("NodeId of a rule set is expected!");
    }

    /**
     * Check a 404 is thrown when using non-existent id instead of a ruleSetId.
     *
     * DELETE /nodes/{folderNodeId}/rule-set-links/{ruleSetId}
     */
    //TODO This test may need to be modified once ACS-3616 is done
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void unlinkUsingRandomId()
    {
        STEP("Create folders in existing site");
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Attempt to unlink the rule set");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).unlinkRuleSet("non-existent-id");

        STEP("Assert unlink result");
        restClient.assertStatusCodeIs(NOT_FOUND)
                .assertLastError().containsSummary("Rule set with id non-existent-id was not found");
    }

    /**
     * Check we cannot unlink from a rule set that we can't view.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void unlinkFromRuleSetWithoutPermission()
    {
        STEP("Use admin to create a private site with a folder containing a rule.");
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingAdmin().usingSite(privateSite).createFolder();
        restClient.authenticateUser(dataUser.getAdminUser()).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet()
                  .createSingleRule(rulesUtils.createRuleModelWithDefaultValues());

        STEP("Add the user as a consumer.");
        dataUser.usingAdmin().addUserToSite(user, privateSite, SiteConsumer);

        STEP("Use the consumer to create a folder with a link to the private rule set.");
        FolderModel publicFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(privateFolder.getNodeRef());
        restClient.authenticateUser(user).withPrivateAPI().usingNode(publicFolder).createRuleLink(request);
        restClient.assertStatusCodeIs(CREATED);

        STEP("Remove the user from the private site.");
        dataUser.usingAdmin().removeUserFromSite(user, privateSite);

        STEP("Use the user to try to unlink from the rule set.");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(publicFolder).unlinkRuleSet("-default-");

        restClient.assertStatusCodeIs(FORBIDDEN);
    }

    /**
     * Check we can unlink from a rule set if we only have read permission for it.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void unlinkFromRuleSetWithOnlyReadPermission()
    {
        STEP("Use admin to create a private site with a folder containing a rule.");
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingAdmin().usingSite(privateSite).createFolder();
        restClient.authenticateUser(dataUser.getAdminUser()).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet()
                  .createSingleRule(rulesUtils.createRuleModelWithDefaultValues());

        STEP("Add the user as a consumer.");
        dataUser.usingAdmin().addUserToSite(user, privateSite, SiteConsumer);

        STEP("Use the consumer to create a folder with a link to the private rule set.");
        FolderModel publicFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        RestRuleSetLinkModel request = new RestRuleSetLinkModel();
        request.setId(privateFolder.getNodeRef());
        restClient.authenticateUser(user).withPrivateAPI().usingNode(publicFolder).createRuleLink(request);
        restClient.assertStatusCodeIs(CREATED);

        STEP("Use the consumer to try to unlink from the rule set.");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(publicFolder).unlinkRuleSet("-default-");

        restClient.assertStatusCodeIs(NO_CONTENT);
    }
}
