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

import static java.util.stream.Collectors.toList;
import static org.alfresco.utility.constants.UserRole.SiteCollaborator;
import static org.alfresco.utility.constants.UserRole.SiteContributor;
import static org.alfresco.utility.constants.UserRole.SiteManager;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for DELETE /nodes/{nodeId}/rule-sets/{ruleSetId}/rules/{ruleId}.
 */
@Test(groups = {TestGroup.RULES})
public class DeleteRulesTests extends RulesRestTest
{
    private static final String FAKE_NODE_REF = "fake-node-id";

    private UserModel user;
    private SiteModel site;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a Contributor user and a public site");
        user = dataUser.createRandomTestUser();
        user.setUserRole(SiteContributor);
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    /**
     * Delete previously created rule by its id (as Contributor).
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY})
    public void deleteSingleRuleAndGet204()
    {
        STEP("Create a few rules in the folder");
        final FolderModel ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        final List<RestRuleModel> createdRules = Stream.of("ruleA", "ruleB", "ruleC")
                .map(ruleName -> {
                    RestRuleModel ruleModel = rulesUtils.createRuleModel(ruleName);
                    return restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                            .createSingleRule(ruleModel);
                })
                .collect(toList());

        STEP("Attempt delete one rule");
        final RestRuleModel ruleA = createdRules.get(0);
        restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet().deleteRule(ruleA.getId());
        restClient.assertStatusCodeIs(NO_CONTENT);

        STEP("Get and check the rules from the folder after deleting one of them");
        final RestRuleModelsCollection rulesAfterDeletion =
                restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet().getListOfRules();
        restClient.assertStatusCodeIs(OK);
        rulesAfterDeletion.assertThat().entriesListCountIs(createdRules.size() - 1);
        Assert.assertTrue(rulesAfterDeletion.getEntries()
                .stream()
                .noneMatch(r -> r.onModel().getId().equals(ruleA.getId()))
        );
        final Set<String> ruleIdsThatShouldBeLeft = createdRules.stream()
                .filter(r -> !r.getName().equals("ruleA"))
                .map(RestRuleModel::getId)
                .collect(Collectors.toSet());
        final Set<String> ruleIdsAfterDeletion = rulesAfterDeletion.getEntries().stream()
                .map(r -> r.onModel().getId())
                .collect(Collectors.toSet());
        Assert.assertEquals(ruleIdsThatShouldBeLeft, ruleIdsAfterDeletion);
    }

    /**
     * Try to delete a rule in a non-existing folder and get 404.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void deleteRuleInNonExistingFolderAndGet404()
    {
        final FolderModel ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        final RestRuleModel testRule = createRule(ruleFolder);

        STEP("Create a non-existing folder model");
        final FolderModel nonExistingFolder = new FolderModel();
        nonExistingFolder.setNodeRef(FAKE_NODE_REF);

        STEP("Attempt delete the rule in non-existing folder");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(nonExistingFolder).usingDefaultRuleSet().deleteRule(testRule.getId());

        restClient.assertLastError().statusCodeIs(NOT_FOUND);
    }

    /**
     * Try to delete a rule in a non-existing rule set and get 404.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void deleteRuleInNonExistingRuleSetAndGet404()
    {
        final FolderModel ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        final RestRuleModel testRule = createRule(ruleFolder);

        STEP("Attempt delete the rule in non-existing rule set");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingRuleSet(FAKE_NODE_REF).deleteRule(testRule.getId());

        restClient.assertLastError().statusCodeIs(NOT_FOUND);
    }

    /**
     * Try to delete a non-existing rule and get 404.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY})
    public void deleteNonExistingRuleAndGet404()
    {
        final FolderModel ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        STEP("Attempt delete non-existing rule");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet().deleteRule(FAKE_NODE_REF);

        restClient.assertLastError().statusCodeIs(NOT_FOUND);
    }

    /**
     * Try to delete an existing rule passing a wrong but existing folder and get 404.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY})
    public void deleteExistingRuleFromWrongFolderAndGet404()
    {
        final FolderModel ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        final RestRuleModel testRule = createRule(ruleFolder);

        STEP("Create a second folder in the site");
        final FolderModel anotherFolder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Attempt delete an existing rule from a wrong but existing (second) folder");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(anotherFolder).usingDefaultRuleSet().deleteRule(testRule.getId());

        restClient.assertLastError().statusCodeIs(NOT_FOUND);
    }

    /**
     * Check that a user without write permission on folder cannot delete a rule inside it.
     */
    public void deleteSinglePrivateRuleWithoutPermissionAndGet403()
    {
        STEP("Create a user and use them to create a private site containing a folder with a rule");
        final UserModel privateUser = dataUser.createRandomTestUser();
        final SiteModel privateSite = dataSite.usingUser(privateUser).createPrivateRandomSite();
        final FolderModel privateFolder = dataContent.usingUser(privateUser).usingSite(privateSite).createFolder();
        final RestRuleModel ruleModel = rulesUtils.createRuleModel("Private site rule");
        final RestRuleModel createdRule =
                restClient.authenticateUser(privateUser).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet()
                        .createSingleRule(ruleModel);

        STEP("Try to delete the rule with another user");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet().deleteRule(createdRule.getId());

        restClient.assertLastError().statusCodeIs(FORBIDDEN);
    }

    /**
     * Check that a user with SiteCollaborator permissions on folder can delete a rule inside it.
     */
    public void deleteSinglePublicRuleAsCollaboratorAndGet403()
    {
        STEP("Create a user and use them to create a private site containing a folder with a rule");
        final FolderModel ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        final RestRuleModel testRule = createRule(ruleFolder);

        STEP("Create a manager in the private site");
        final UserModel siteCollaborator = dataUser.createRandomTestUser();
        siteCollaborator.setUserRole(SiteCollaborator);
        restClient.authenticateUser(user).withCoreAPI().usingSite(site).addPerson(siteCollaborator);

        STEP("Check the manager can delete the rule");
        restClient.authenticateUser(siteCollaborator).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .deleteRule(testRule.getId());

        restClient.assertLastError().statusCodeIs(FORBIDDEN);
    }

    /**
     * Check that a user with SiteManager permissions on folder can delete a rule inside it.
     */
    public void deleteSinglePrivateRuleAsSiteManagerAndGet204()
    {
        STEP("Create a user and use them to create a private site containing a folder with a rule");
        final UserModel privateUser = dataUser.createRandomTestUser();
        final SiteModel privateSite = dataSite.usingUser(privateUser).createPrivateRandomSite();
        final FolderModel privateFolder = dataContent.usingUser(privateUser).usingSite(privateSite).createFolder();
        final RestRuleModel ruleModel = rulesUtils.createRuleModel("Private site rule");
        final RestRuleModel createdRule =
                restClient.authenticateUser(privateUser).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet()
                        .createSingleRule(ruleModel);

        STEP("Create a manager in the private site");
        final UserModel siteManager = dataUser.createRandomTestUser();
        siteManager.setUserRole(SiteManager);
        restClient.authenticateUser(privateUser).withCoreAPI().usingSite(privateSite).addPerson(siteManager);

        STEP("Check the manager can delete the rule");
        restClient.authenticateUser(siteManager).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet()
                .deleteRule(createdRule.getId());

        restClient.assertStatusCodeIs(NO_CONTENT);
    }

    private RestRuleModel createRule(FolderModel ruleFolder)
    {
        STEP("Create a rule in the folder");
        final RestRuleModel ruleModel = rulesUtils.createRuleModel("Test rule");
        return restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);
    }
}
