/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import static org.alfresco.rest.rules.RulesTestsUtils.createRuleModel;
import static org.alfresco.utility.constants.UserRole.SiteCollaborator;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for GET /nodes/{nodeId}/rule-sets/{ruleSetId}/rules and GET /nodes/{nodeId}/rule-sets/{ruleSetId}/rules/{ruleId}.
 */
@Test(groups = {TestGroup.RULES})
public class GetRulesTests extends RestTest
{
    private UserModel user;
    private SiteModel site;
    private FolderModel ruleFolder;
    private List<RestRuleModel> createdRules;
    private RestRuleModel createdRuleA;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user, site and folder");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create rules in the folder");
        createdRules = Stream.of("ruleA", "ruleB").map(ruleName -> {
            RestRuleModel ruleModel = createRuleModel(ruleName);
            return restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);
        }).collect(toList());
        createdRuleA = createdRules.get(0);
    }

    /** Check we can get an empty list of rules. */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getEmptyRulesList()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Get the rules that apply to the folder");
        RestRuleModelsCollection rules = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).usingDefaultRuleSet().getListOfRules();

        restClient.assertStatusCodeIs(NOT_FOUND);
        assertTrue("Expected no rules to be present.", rules.isEmpty());
    }

    /** Check we can get all the rules for a folder. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getRulesList()
    {
        STEP("Get the rules that apply to the folder");
        RestRuleModelsCollection rules = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().getListOfRules();

        restClient.assertStatusCodeIs(OK);
        rules.assertThat().entriesListCountIs(createdRules.size());
        IntStream.range(0, createdRules.size()).forEach(i ->
                rules.getEntries().get(i).onModel()
                         .assertThat().field("id").is(createdRules.get(i).getId())
                         .assertThat().field("name").is(createdRules.get(i).getName()));
    }

    /** Check we get a 404 if trying to load rules for a folder that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRulesForNonExistentFolder()
    {
        STEP("Try to load rules for a non-existent folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");
        restClient.authenticateUser(user).withCoreAPI().usingNode(nonExistentFolder).usingDefaultRuleSet().getListOfRules();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we get a 404 if trying to load rules with a rule set id that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRulesFromNonExistentRuleSet()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        STEP("Try to load rules for a non-existent rule set.");
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder).usingRuleSet("fake-id").getListOfRules();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we can get a rule by its id. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getSingleRule()
    {
        STEP("Load a particular rule");
        RestRuleModel rule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().getSingleRule(createdRuleA.getId());

        restClient.assertStatusCodeIs(OK);

        rule.assertThat().field("id").is(createdRuleA.getId())
            .assertThat().field("name").is(createdRuleA.getName());
    }

    /** Check we get a 404 if trying to load a rule from a folder that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getSingleRuleFromNonExistentFolder()
    {
        STEP("Try to load a rule from a non-existent folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");
        restClient.authenticateUser(user).withCoreAPI().usingNode(nonExistentFolder).usingDefaultRuleSet().getSingleRule("fake-rule-id");
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we get a 404 if trying to load a rule with a rule set id that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getSingleRuleFromNonExistentRuleSet()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        STEP("Try to load rules for a non-existent rule set.");
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder).usingRuleSet("fake-id").getSingleRule("fake-rule-id");
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we get a 404 if trying to load an existing rule providing a wrong but existing folder */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getSingleRuleFromWrongFolder()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        STEP("Try to load a rule for a wrong but existing folder.");
        restClient.authenticateUser(user).withCoreAPI().usingNode(folder).usingDefaultRuleSet().getSingleRule(createdRuleA.getId());
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check that a user without read permission cannot view the folder rules. */
    public void requireReadPermissionToGetRule()
    {
        STEP("Create a user and use them to create a private site containing a folder with a rule");
        UserModel privateUser = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(privateUser).createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingUser(privateUser).usingSite(privateSite).createFolder();
        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("Private site rule");
        restClient.authenticateUser(privateUser).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Try to get the rule with another user");
        restClient.authenticateUser(user).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().getListOfRules();

        restClient.assertLastError()
                  .statusCodeIs(FORBIDDEN)
                  .containsSummary("Cannot read from this node");
    }

    /** Check that a user with only read permission can view the folder rules. */
    public void dontRequireWritePermissionToGetRule()
    {
        STEP("Create a user and use them to create a private site containing a folder with a rule");
        UserModel privateUser = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(privateUser).createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingUser(privateUser).usingSite(privateSite).createFolder();
        RestRuleModel ruleModel = createRuleModel("Private site rule");
        restClient.authenticateUser(privateUser).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Create a collaborator in the private site");
        UserModel collaborator = dataUser.createRandomTestUser();
        collaborator.setUserRole(SiteCollaborator);
        restClient.authenticateUser(privateUser).withCoreAPI().usingSite(privateSite).addPerson(collaborator);

        STEP("Check the collaborator can view the rule");
        RestRuleModelsCollection rules = restClient.authenticateUser(collaborator).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().getListOfRules();

        restClient.assertStatusCodeIs(OK);
        rules.assertThat().entriesListContains("name", "Private site rule");
    }
}
