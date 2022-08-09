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

import static org.alfresco.rest.rules.RulesTestsUtils.createRuleModel;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.alfresco.rest.model.RestRuleSetModel;
import org.alfresco.rest.model.RestRuleSetModelsCollection;
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
    private RestRuleModel rule;
    private String ruleSetId;

    @BeforeClass (alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user, site and folder.");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create a rule in the folder.");
        RestRuleModel ruleModel = createRuleModel("ruleName");
        rule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                       .createSingleRule(ruleModel);

        STEP("Get the rule sets for the folder and find the rule set id");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder)
                                                         .getListOfRuleSets();
        ruleSets.assertThat().entriesListCountIs(1);
        ruleSetId = ruleSets.getEntries().get(0).onModel().getId();
    }

    /** Check we can get an empty list of rule sets. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getEmptyRuleSetsList()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Get the rule sets for the folder");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withCoreAPI()
                                                         .usingNode(folder).getListOfRuleSets();

        restClient.assertStatusCodeIs(OK);
        assertTrue("Expected no rule sets to be present.", ruleSets.isEmpty());
    }

    /** Check we can get a list of rule sets. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getRuleSetsList()
    {
        STEP("Get the rule sets for the folder");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder)
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
        restClient.authenticateUser(user).withCoreAPI().usingNode(nonExistentFolder).getListOfRuleSets();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we can get the id of the folder that owns a list of rule sets. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetsAndOwningFolders()
    {
        STEP("Get the rule sets and owning folders");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withCoreAPI()
                                                 .usingNode(ruleFolder)
                                                 .usingParams("include=owningFolder")
                                                 .getListOfRuleSets();

        restClient.assertStatusCodeIs(OK);
        ruleSets.getEntries().get(0).onModel()
                .assertThat().field("owningFolder").is(ruleFolder.getNodeRef())
                .assertThat().field("id").is(ruleSetId);
        ruleSets.assertThat().entriesListCountIs(1);
    }

    /** Check we can get a rule set by its id. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getRuleSetById()
    {
        STEP("Get the rule set using its rule set id");
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder)
                                             .getRuleSet(ruleSetId);

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("id").is(ruleSetId);
    }

    /** Check we can get a rule set using the "-default-" synonym. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getDefaultRuleSetById()
    {
        STEP("Get the default rule set for the folder");
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder)
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
        restClient.authenticateUser(user).withCoreAPI().usingNode(nonExistentFolder).getDefaultRuleSet();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we get 404 for a non-existing rule set id. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetByNonExistingId()
    {
        STEP("Get the rule set using fake rule set id");
        String fakeRuleSetId = "fake-rule-set-id";
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).getRuleSet(fakeRuleSetId);
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we can get the id of the folder that owns a rule set. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRuleSetAndOwningFolder()
    {
        STEP("Get the rule set and owning folder");
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withCoreAPI()
                                             .usingNode(ruleFolder)
                                             .usingParams("include=owningFolder")
                                             .getRuleSet(ruleSetId);

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("owningFolder").is(ruleFolder.getNodeRef())
               .assertThat().field("id").is(ruleSetId);
    }
}
