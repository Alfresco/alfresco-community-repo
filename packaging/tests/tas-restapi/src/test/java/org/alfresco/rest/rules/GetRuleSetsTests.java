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

import static org.alfresco.rest.requests.RuleSettings.IS_INHERITANCE_ENABLED;
import static org.alfresco.rest.rules.RulesTestsUtils.MOVE_ACTION;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleSetLinkModel;
import org.alfresco.rest.model.RestRuleSetModel;
import org.alfresco.rest.model.RestRuleSetModelsCollection;
import org.alfresco.rest.model.RestRuleSettingsModel;
import org.alfresco.rest.requests.coreAPI.RestCoreAPI;
import org.alfresco.rest.requests.privateAPI.RestPrivateAPI;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for GET /nodes/{nodeId}/rule-sets and /nodes/{nodeId}/rule-sets/{ruleSetId}.
 */
@Test (groups = { TestGroup.RULES })
public class GetRuleSetsTests extends RestTest
{
    private UserModel user;
    private SiteModel site;
    private FolderModel ruleFolder;
    /** A folder with a rule in a private site owned by admin. */
    private FolderModel privateFolder;
    private FolderModel inheritingChildFolder;
    private FolderModel notInheritingChildFolder;
    private RestRuleModel rule;
    private String ruleSetId;

    @BeforeClass (alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user, site and folder.");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create two children of the folder - one that inherits rules and one that doesn't");
        inheritingChildFolder = dataContent.usingUser(user).usingResource(ruleFolder).createFolder();
        notInheritingChildFolder = dataContent.usingUser(user).usingResource(ruleFolder).createFolder();
        RestRuleSettingsModel doesntInherit = new RestRuleSettingsModel();
        doesntInherit.setValue(false);
        restClient.authenticateUser(user).withPrivateAPI().usingNode(notInheritingChildFolder)
                  .usingIsInheritanceEnabledRuleSetting().updateSetting(doesntInherit);

        STEP("Create a rule in the folder.");
        RestRuleModel ruleModel = rulesUtils.createRuleModel("ruleName");
        rule = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                       .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                                                         .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        ruleSetId = ruleSets.getEntries().get(0).onModel().getId();

        STEP("Use admin to create a private site containing a rule in a rule set that can be inherited.");
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        privateFolder = dataContent.usingAdmin().usingSite(privateSite).createFolder();
        privateAPIForAdmin().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(rulesUtils.createRuleModelWithModifiedValues());
    }

    /** Check we can get an empty list of rule sets. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getEmptyRuleSetsList()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Get the rule sets for the folder");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI()
                                                         .usingNode(folder).getListOfRuleSets();

        restClient.assertStatusCodeIs(OK);
        assertTrue("Expected no rule sets to be present.", ruleSets.isEmpty());
    }

    /** Check we can get a list of rule sets. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getRuleSetsList()
    {
        STEP("Get the rule sets for the folder");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                                                         .getListOfRuleSets();

        restClient.assertStatusCodeIs(OK);
        ruleSets.assertThat().entriesListCountIs(1);
        ruleSets.getEntries().get(0).onModel()
                .assertThat().field("id").isNotNull();
    }

    /** Check we get a 404 if trying to load rule sets for a folder that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetsForNonExistentFolder()
    {
        STEP("Try to load rule sets for a non-existent folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(nonExistentFolder).getListOfRuleSets();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check that we get a 403 error when trying to get rule sets for a folder we don't have read access to. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetsWithoutPermission()
    {
        STEP("Check a user cannot list rule sets without read access.");
        privateAPIForUser().usingNode(privateFolder).getListOfRuleSets();
        restClient.assertStatusCodeIs(FORBIDDEN);
    }

    /** Check that we can still list some rule sets if we don't have permission to view them all. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void permissionsAreRespectedWhenListingRuleSets()
    {
        STEP("Create a public site containing a parent and child folder with rule inheritance enabled.");
        SiteModel publicSite = dataSite.usingUser(user).createPublicRandomSite();
        FolderModel parentFolder = dataContent.usingUser(user).usingSite(publicSite).createFolder();
        FolderModel childFolder = dataContent.usingUser(user).usingResource(parentFolder).createFolder();
        RestRuleSettingsModel enabled = new RestRuleSettingsModel();
        enabled.setValue(true);
        privateAPIForUser().usingNode(parentFolder).usingRuleSetting(IS_INHERITANCE_ENABLED).updateSetting(enabled);

        STEP("Link the parent folder to a private rule set.");
        RestRuleSetLinkModel linkModel = new RestRuleSetLinkModel();
        linkModel.setId(privateFolder.getNodeRef());
        privateAPIForAdmin().usingNode(parentFolder).createRuleLink(linkModel);

        STEP("Create a rule on the child folder.");
        privateAPIForUser().usingNode(childFolder).usingDefaultRuleSet().createSingleRule(rulesUtils.createRuleModelWithDefaultValues());

        STEP("Check admin can view both rule sets.");
        RestRuleSetModelsCollection adminViewOfRuleSets = privateAPIForAdmin().usingNode(childFolder).getListOfRuleSets();
        restClient.assertStatusCodeIs(OK);
        RestRuleSetModel parentRuleSet = adminViewOfRuleSets.getEntries().get(0).onModel();
        RestRuleSetModel childRuleSet = adminViewOfRuleSets.getEntries().get(1).onModel();

        STEP("Check the normal user can only view the child rule set.");
        RestRuleSetModelsCollection userViewOfRuleSets = privateAPIForUser().usingNode(childFolder).getListOfRuleSets();
        restClient.assertStatusCodeIs(OK);
        userViewOfRuleSets.assertThat().entriesListContains("id", childRuleSet.getId())
                          .and().entriesListDoesNotContain("id", parentRuleSet.getId());
    }

    /** Check we can get the id of the folder that owns a list of rule sets. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetsAndOwningFolders()
    {
        STEP("Get the rule sets and owning folders");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI()
                                                 .usingNode(ruleFolder)
                                                 .include("owningFolder")
                                                 .getListOfRuleSets();

        restClient.assertStatusCodeIs(OK);
        ruleSets.getEntries().get(0).onModel()
                .assertThat().field("owningFolder").is(ruleFolder.getNodeRef())
                .assertThat().field("id").is(ruleSetId);
        ruleSets.assertThat().entriesListCountIs(1);
    }

    /** Check we can get the reason that a rule set is included in the list. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetsAndOwnedInclusionType()
    {
        STEP("Get the rule sets and inclusion type");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI()
                                                         .usingNode(ruleFolder)
                                                         .include("inclusionType")
                                                         .getListOfRuleSets();

        restClient.assertStatusCodeIs(OK);
        ruleSets.getEntries().get(0).onModel()
                .assertThat().field("inclusionType").is("owned")
                .assertThat().field("id").is(ruleSetId);
        ruleSets.assertThat().entriesListCountIs(1);
    }

    /** Check we can tell that a rule set has been inherited. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetsAndInheritedInclusionType()
    {
        STEP("Get the rule sets and inclusion type");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI()
                                                         .usingNode(inheritingChildFolder)
                                                         .include("inclusionType")
                                                         .getListOfRuleSets();

        restClient.assertStatusCodeIs(OK);
        ruleSets.getEntries().get(0).onModel()
                .assertThat().field("inclusionType").is("inherited")
                .assertThat().field("id").is(ruleSetId);
        ruleSets.assertThat().entriesListCountIs(1);
    }

    /** Check that a rule set is not inherited if inheriting is disabled. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetsWithoutInheriting()
    {
        STEP("Get the rule sets and inclusion type");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI()
                                                         .usingNode(notInheritingChildFolder)
                                                         .getListOfRuleSets();

        restClient.assertStatusCodeIs(OK);
        ruleSets.assertThat().entriesListCountIs(0);
    }

    /** Check we can get a rule set by its id. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getRuleSetById()
    {
        STEP("Get the rule set using its rule set id");
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                                             .getRuleSet(ruleSetId);

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("id").is(ruleSetId)
               // Also check that the optional fields are not included by default.
               .assertThat().field("owningFolder").isNull()
               .assertThat().field("inheritedBy").isNull()
               .assertThat().field("linkedToBy").isNull()
               .assertThat().field("isInherited").isNull()
               .assertThat().field("isLinkedTo").isNull();
    }

    /** Check we can get a rule set using the "-default-" synonym. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getDefaultRuleSetById()
    {
        STEP("Get the default rule set for the folder");
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                                                         .getDefaultRuleSet();

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("id").isNotNull();
    }

    /** Check we get a 404 if trying to load the default rule set for a folder that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getDefaultRuleSetForNonExistentFolder()
    {
        STEP("Try to load a rule set for a non-existent folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(nonExistentFolder).getDefaultRuleSet();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we get 404 for a non-existing rule set id. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetByNonExistingId()
    {
        STEP("Get the rule set using fake rule set id");
        String fakeRuleSetId = "fake-rule-set-id";
        restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).getRuleSet(fakeRuleSetId);
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we can get the id of the folder that owns a rule set. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetAndOwningFolder()
    {
        STEP("Get the rule set and owning folder");
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withPrivateAPI()
                                             .usingNode(ruleFolder)
                                             .include("owningFolder")
                                             .getRuleSet(ruleSetId);

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("owningFolder").is(ruleFolder.getNodeRef())
               .assertThat().field("id").is(ruleSetId);
    }

    /**
     * Check we can find out the id of any folders that inherit a rule set.
     * <p>
     * The test checks several different situations:
     * <pre>
     *   folder --[owns]-> rule set
     *   +- publicFolder --[inherits]-> rule set (user has access)
     *   +- privateFolder --[inherits]-> rule set (user does not have access)
     *      +- publicGrandchild --[inherits]-> rule set (user has access again)
     *   +- nonInheritingFolder (inheritance should be prevented)
     *      +- linkingFolder --[links]-> rule set (not inherited)
     *         +- descendantFolder --[inherits]-> rule set (inherited via link)
     * </pre>
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetAndInheritedBy()
    {
        STEP("Create a site owned by admin and add user as a contributor");
        SiteModel siteModel = dataSite.usingAdmin().createPrivateRandomSite();
        dataUser.addUserToSite(user, siteModel, UserRole.SiteContributor);

        STEP("Create the folder structure");
        FolderModel folder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        FolderModel publicFolder = dataContent.usingUser(user).usingResource(folder).createFolder();
        FolderModel privateFolder = dataContent.usingAdmin().usingResource(folder).createFolder();
        dataContent.usingAdmin().usingResource(privateFolder).setInheritPermissions(false);
        // Create the grandchild with user and use admin to move it under the private folder.
        FolderModel publicGrandchild = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        coreAPIForAdmin().usingActions().executeAction(MOVE_ACTION, publicGrandchild, ImmutableMap.of("destination-folder", "workspace://SpacesStore/" + privateFolder.getNodeRef()));
        // Create the non-inheriting folder.
        FolderModel nonInheritingFolder = dataContent.usingUser(user).usingResource(folder).createFolder();
        RestRuleSettingsModel nonInheriting = new RestRuleSettingsModel();
        nonInheriting.setKey(IS_INHERITANCE_ENABLED);
        nonInheriting.setValue(false);
        privateAPIForUser().usingNode(nonInheritingFolder).usingIsInheritanceEnabledRuleSetting().updateSetting(nonInheriting);
        // Create a child that will link to the rule and a child of that to inherit via the link.
        FolderModel linkingFolder = dataContent.usingUser(user).usingResource(nonInheritingFolder).createFolder();
        FolderModel descendantFolder = dataContent.usingUser(user).usingResource(linkingFolder).createFolder();

        STEP("Create an inheritable rule in the folder and get the rule set id.");
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithModifiedValues();
        privateAPIForUser().usingNode(folder).usingDefaultRuleSet().createSingleRule(ruleModel);
        RestRuleSetModelsCollection ruleSets = privateAPIForUser().usingNode(folder).getListOfRuleSets();
        String ruleSetId = ruleSets.getEntries().get(0).onModel().getId();

        STEP("Create the link to the rule from the linking folder");
        RestRuleSetLinkModel ruleSetLink = new RestRuleSetLinkModel();
        ruleSetLink.setId(folder.getNodeRef());
        privateAPIForUser().usingNode(linkingFolder).createRuleLink(ruleSetLink);

        STEP("Remove the user from  the site");
        dataUser.removeUserFromSite(user, siteModel);

        STEP("Get the rule set and inheriting folders");
        RestRuleSetModel ruleSet = privateAPIForUser().usingNode(folder)
                                                   .include("inheritedBy")
                                                   .getRuleSet(ruleSetId);

        restClient.assertStatusCodeIs(OK);
        List<String> expectedInheritors = List.of(publicFolder.getNodeRef(), descendantFolder.getNodeRef(), publicGrandchild.getNodeRef());
        ruleSet.assertThat().field("inheritedBy").is(expectedInheritors)
               .assertThat().field("id").is(ruleSetId);
    }

    /** Check we can get the folders that link to a rule set and that this respects permissions. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetsAndLinkedToBy()
    {
        STEP("Create a site owned by admin and add user as a contributor");
        SiteModel siteModel = dataSite.usingAdmin().createPrivateRandomSite();
        dataUser.addUserToSite(user, siteModel, UserRole.SiteContributor);

        STEP("Create the folder structure");
        FolderModel ruleFolder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        FolderModel publicFolder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        FolderModel privateFolder = dataContent.usingAdmin().usingSite(siteModel).createFolder();
        dataContent.usingAdmin().usingResource(privateFolder).setInheritPermissions(false);

        STEP("Remove the user from  the site");
        dataUser.removeUserFromSite(user, siteModel);

        STEP("Create a rule in the folder and link to it from the other two.");
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
        privateAPIForUser().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);
        RestRuleSetModelsCollection ruleSets = privateAPIForAdmin().usingNode(ruleFolder).getListOfRuleSets();
        String ruleSetId = ruleSets.getEntries().get(0).onModel().getId();
        RestRuleSetLinkModel ruleSetLink = new RestRuleSetLinkModel();
        ruleSetLink.setId(ruleFolder.getNodeRef());
        privateAPIForUser().usingNode(publicFolder).createRuleLink(ruleSetLink);
        privateAPIForAdmin().usingNode(privateFolder).createRuleLink(ruleSetLink);

        STEP("Get the rule set and linkedToBy field");
        RestRuleSetModel ruleSet = privateAPIForUser().usingNode(ruleFolder)
                                                   .include("linkedToBy")
                                                   .getRuleSet(ruleSetId);

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("linkedToBy").is(List.of(publicFolder.getNodeRef()))
               .assertThat().field("id").is(ruleSetId);
    }

    /** Check that a user can see that a rule set is inherited even if they don't have permission to view the inheriting folder. */
    @Test
    public void getRuleSetAndIsInheritedWithoutPermission()
    {
        STEP("Create a site owned by admin and add user as a contributor");
        SiteModel siteModel = dataSite.usingAdmin().createPrivateRandomSite();
        dataUser.addUserToSite(user, siteModel, UserRole.SiteContributor);

        STEP("Create a folder with a rule set and a private child folder to inherit it");
        FolderModel ruleFolder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        dataContent.usingAdmin().usingResource(ruleFolder).createFolder();
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
        privateAPIForUser().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Remove the user from  the site");
        dataUser.removeUserFromSite(user, siteModel);

        STEP("Get the rule set and isInherited field");
        RestRuleSetModel ruleSet = privateAPIForUser().usingNode(ruleFolder)
                                                   .include("isInherited", "inheritedBy")
                                                   .getDefaultRuleSet();

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("isInherited").is(true)
               .assertThat().field("inheritedBy").isEmpty();

    }

    /** Check that the isInherited field includes rule sets which are only inherited via links. */
    @Test
    public void getRuleSetAndIsInheritedViaLink()
    {
        STEP("Create a site and a folder with a rule");
        SiteModel siteModel = dataSite.usingUser(user).createPublicRandomSite();
        FolderModel ruleFolder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
        privateAPIForUser().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Create a second folder in the site that links to the rule set");
        FolderModel secondFolder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        dataContent.usingUser(user).usingResource(secondFolder).createFolder();
        RestRuleSetLinkModel ruleSetLink = new RestRuleSetLinkModel();
        ruleSetLink.setId(ruleFolder.getNodeRef());
        privateAPIForUser().usingNode(secondFolder).createRuleLink(ruleSetLink);

        STEP("Get the rule set and isInherited field");
        RestRuleSetModel ruleSet = privateAPIForUser().usingNode(ruleFolder)
                                                   .include("isInherited")
                                                   .getDefaultRuleSet();

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("isInherited").is(true);
    }

    /**
     * Check that if a rule set is owned and linked to but not inherited then isInherited returns false.
     */
    @Test
    public void getRuleSetAndIsInheritedCanBeFalse()
    {
        STEP("Create a site and a folder with a rule");
        SiteModel siteModel = dataSite.usingUser(user).createPublicRandomSite();
        FolderModel ruleFolder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
        privateAPIForUser().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Create a second folder in the site that links to the rule set");
        FolderModel secondFolder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        RestRuleSetLinkModel ruleSetLink = new RestRuleSetLinkModel();
        ruleSetLink.setId(ruleFolder.getNodeRef());
        privateAPIForUser().usingNode(secondFolder).createRuleLink(ruleSetLink);

        STEP("Get the rule set and isInherited field");
        RestRuleSetModel ruleSet = privateAPIForUser().usingNode(ruleFolder)
                                                   .include("isInherited")
                                                   .getDefaultRuleSet();

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("isInherited").is(false);
    }


    /** Check that a user can see that a rule set is linked to even if they don't have permission to view the linking folder. */
    @Test
    public void getRuleSetAndIsLinkedToWithoutPermission()
    {
        STEP("Create a site owned by admin and add user as a contributor");
        SiteModel siteModel = dataSite.usingAdmin().createPrivateRandomSite();
        dataUser.addUserToSite(user, siteModel, UserRole.SiteContributor);

        STEP("Create a folder with a rule set");
        FolderModel ruleFolder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
        privateAPIForUser().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Create a private folder linking to the rule set");
        FolderModel linkingFolder = dataContent.usingAdmin().usingSite(siteModel).createFolder();
        RestRuleSetLinkModel linkModel = new RestRuleSetLinkModel();
        linkModel.setId(ruleFolder.getNodeRef());
        privateAPIForAdmin().usingNode(linkingFolder).createRuleLink(linkModel);

        STEP("Remove the user from  the site");
        dataUser.removeUserFromSite(user, siteModel);

        STEP("Get the rule set and isLinkedTo field");
        RestRuleSetModel ruleSet = privateAPIForUser().usingNode(ruleFolder)
                                                   .include("isLinkedTo", "linkedToBy", "owningFolder")
                                                   .getDefaultRuleSet();

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("isLinkedTo").is(true)
               .assertThat().field("linkedToBy").isEmpty();

    }

    /**
     * Check that if a rule set is owned and inherited but not linked to then isLinkedTo returns false.
     */
    @Test
    public void getRuleSetAndIsLinkedToCanBeFalse()
    {
        STEP("Create a site, a folder with a rule and a child folder that inherits it");
        SiteModel siteModel = dataSite.usingUser(user).createPublicRandomSite();
        FolderModel ruleFolder = dataContent.usingUser(user).usingSite(siteModel).createFolder();
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
        privateAPIForUser().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);
        dataContent.usingUser(user).usingResource(ruleFolder).createFolder();

        STEP("Get the rule set and isLinkedTo field");
        RestRuleSetModel ruleSet = privateAPIForUser().usingNode(ruleFolder)
                                                   .include("isLinkedTo")
                                                   .getDefaultRuleSet();

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("isLinkedTo").is(false);
    }

    /** Check that we can only view a rule set if have read permission. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void permissionsChecksForFolderWithPrivateAndPublicRuleSets()
    {
        STEP("Create a public site containing a parent and child folder with rule inheritance enabled.");
        SiteModel publicSite = dataSite.usingUser(user).createPublicRandomSite();
        FolderModel parentFolder = dataContent.usingUser(user).usingSite(publicSite).createFolder();
        FolderModel childFolder = dataContent.usingUser(user).usingResource(parentFolder).createFolder();
        RestRuleSettingsModel enabled = new RestRuleSettingsModel();
        enabled.setValue(true);
        privateAPIForUser().usingNode(parentFolder).usingRuleSetting(IS_INHERITANCE_ENABLED).updateSetting(enabled);

        STEP("Link the parent folder to a private rule set.");
        RestRuleSetLinkModel linkModel = new RestRuleSetLinkModel();
        linkModel.setId(privateFolder.getNodeRef());
        privateAPIForAdmin().usingNode(parentFolder).createRuleLink(linkModel);

        STEP("Create a rule on the child folder.");
        privateAPIForUser().usingNode(childFolder).usingDefaultRuleSet().createSingleRule(rulesUtils.createRuleModelWithDefaultValues());

        STEP("Use the admin user to get both rule sets.");
        RestRuleSetModelsCollection adminViewOfRuleSets = privateAPIForAdmin().usingNode(childFolder).getListOfRuleSets();
        RestRuleSetModel parentRuleSet = adminViewOfRuleSets.getEntries().get(0).onModel();
        RestRuleSetModel childRuleSet = adminViewOfRuleSets.getEntries().get(1).onModel();

        STEP("Check the normal user can only view the child rule set.");
        privateAPIForUser().usingNode(childFolder).getRuleSet(parentRuleSet.getId());
        restClient.assertStatusCodeIs(FORBIDDEN);
        privateAPIForUser().usingNode(childFolder).getRuleSet(childRuleSet.getId());
        restClient.assertStatusCodeIs(OK);
    }

    private RestCoreAPI coreAPIForAdmin()
    {
        return restClient.authenticateUser(dataUser.getAdminUser()).withCoreAPI();
    }

    private RestPrivateAPI privateAPIForUser()
    {
        return restClient.authenticateUser(user).withPrivateAPI();
    }

    private RestPrivateAPI privateAPIForAdmin()
    {
        return restClient.authenticateUser(dataUser.getAdminUser()).withPrivateAPI();
    }
}
