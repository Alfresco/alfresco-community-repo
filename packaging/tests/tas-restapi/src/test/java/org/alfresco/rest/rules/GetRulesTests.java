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
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
public class GetRulesTests extends RulesRestTest
{
    private UserModel user;
    private SiteModel site;
    private FolderModel ruleFolder;
    private List<RestRuleModel> createdRules;
    private RestRuleModel createdRuleA;
    private static final String IGNORE_ID = "id";
    private static final String IGNORE_IS_SHARED = "isShared";
    private static final String ACTIONS = "actions";
    private static final String CONDITIONS = "conditions";

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user, site and folder");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create rules in the folder");
        createdRules = Stream.of("ruleA", "ruleB").map(ruleName -> {
            RestRuleModel ruleModel = rulesUtils.createRuleModel(ruleName);
            return restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);
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
        RestRuleModelsCollection rules = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).usingDefaultRuleSet().getListOfRules();

        restClient.assertStatusCodeIs(NOT_FOUND);
        assertTrue("Expected no rules to be present.", rules.isEmpty());
    }

    /**
     * Check we can get all the rules for a folder.
     * <p>
     * Also check that the isShared field is not returned when not requested.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getRulesList()
    {
        STEP("Get the rules that apply to the folder");
        RestRuleModelsCollection rules = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet().getListOfRules();

        restClient.assertStatusCodeIs(OK);
        rules.assertThat().entriesListCountIs(createdRules.size());
        IntStream.range(0, createdRules.size()).forEach(i ->
                rules.getEntries().get(i).onModel()
                     .assertThat().field("id").is(createdRules.get(i).getId())
                     .assertThat().field("name").is(createdRules.get(i).getName())
                     .assertThat().field("isShared").isNull());
    }

    /** Check we get a 404 if trying to load rules for a folder that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRulesForNonExistentFolder()
    {
        STEP("Try to load rules for a non-existent folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(nonExistentFolder).usingDefaultRuleSet().getListOfRules();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we get a 404 if trying to load rules with a rule set id that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getRulesFromNonExistentRuleSet()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        STEP("Try to load rules for a non-existent rule set.");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).usingRuleSet("fake-id").getListOfRules();
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we can get all the rules for a folder along with the extra "include" and "other" fields. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getRulesListWithIncludedFields()
    {
        STEP("Get the rules that apply to the folder");
        RestRuleModelsCollection rules = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                                   .include("isShared")
                                                   .getListOfRules();

        rules.assertThat().entriesListCountIs(createdRules.size());
        IntStream.range(0, createdRules.size()).forEach(i ->
                rules.getEntries().get(i).onModel()
                     .assertThat().field("isShared").isNotNull()
                        .assertThat().field("description").isNull()
                        .assertThat().field("isEnabled").is(true)
                        .assertThat().field("isInheritable").is(false)
                        .assertThat().field("isAsynchronous").is(false)
                        .assertThat().field("errorScript").isNull()
                        .assertThat().field("isShared").is(false)
                        .assertThat().field("triggers").is("[inbound]"));
    }

    /**
     * Check we can get a rule by its id.
     * <p>
     * Also check that the isShared field is not returned when not requested.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getSingleRule()
    {
        STEP("Load a particular rule");
        RestRuleModel rule = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet().getSingleRule(createdRuleA.getId());

        restClient.assertStatusCodeIs(OK);

        rule.assertThat().field("id").is(createdRuleA.getId())
            .assertThat().field("name").is(createdRuleA.getName())
            .assertThat().field("isShared").isNull();
    }

    /** Check we can get rule's other fields */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getRulesOtherFieldsModified()
    {
        STEP("Create a rule with all other fields default values modified");
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithModifiedValues();
        ruleModel.setTriggers(List.of("update"));
        UserModel admin = dataUser.getAdminUser();
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        RestRuleModel rule = restClient.authenticateUser(admin).withPrivateAPI().usingNode(folder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        RestRuleModel expectedRuleModel = rulesUtils.createRuleModelWithModifiedValues();
        expectedRuleModel.setTriggers(List.of("update"));

        restClient.assertStatusCodeIs(CREATED);
        rule.assertThat().isEqualTo(expectedRuleModel, IGNORE_ID, IGNORE_IS_SHARED)
                .assertThat().field("id").isNotNull()
                .assertThat().field("isShared").isNull();

    }

    /** Check we can get rule's "other" fields */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getRulesDefaultFields()
    {
        STEP("Create a rule with all other fields default values");
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
        UserModel admin = dataUser.getAdminUser();
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        RestRuleModel rule = restClient.authenticateUser(admin).withPrivateAPI().usingNode(folder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        RestRuleModel expectedRuleModel = rulesUtils.createRuleModelWithDefaultValues();
        expectedRuleModel.setTriggers(List.of("inbound"));

        restClient.assertStatusCodeIs(CREATED);

        restClient.assertStatusCodeIs(CREATED);
        rule.assertThat().isEqualTo(expectedRuleModel, IGNORE_ID, IGNORE_IS_SHARED)
                .assertThat().field("id").isNotNull()
                .assertThat().field("isShared").isNull();
    }

    /** Check we get a 404 if trying to load a rule from a folder that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getSingleRuleFromNonExistentFolder()
    {
        STEP("Try to load a rule from a non-existent folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(nonExistentFolder).usingDefaultRuleSet().getSingleRule("fake-rule-id");
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we get a 404 if trying to load a rule with a rule set id that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getSingleRuleFromNonExistentRuleSet()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        STEP("Try to load rules for a non-existent rule set.");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).usingRuleSet("fake-id").getSingleRule("fake-rule-id");
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we get a 404 if trying to load an existing rule providing a wrong but existing folder */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getSingleRuleFromWrongFolder()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        STEP("Try to load a rule for a wrong but existing folder.");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).usingDefaultRuleSet().getSingleRule(createdRuleA.getId());
        restClient.assertStatusCodeIs(NOT_FOUND);
    }

    /** Check we can get a rule by its id along with any included fields. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getSingleRuleWithIncludedFields()
    {
        STEP("Load a particular rule");
        RestRuleModel rule = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                       .include("isShared")
                                       .getSingleRule(createdRuleA.getId());

        rule.assertThat().field("isShared").isNotNull();
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
        restClient.authenticateUser(privateUser).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Try to get the rule with another user");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet().getListOfRules();

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
        RestRuleModel ruleModel = rulesUtils.createRuleModel("Private site rule");
        restClient.authenticateUser(privateUser).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Create a collaborator in the private site");
        UserModel collaborator = dataUser.createRandomTestUser();
        collaborator.setUserRole(SiteCollaborator);
        restClient.authenticateUser(privateUser).withCoreAPI().usingSite(privateSite).addPerson(collaborator);

        STEP("Check the collaborator can view the rule");
        RestRuleModelsCollection rules = restClient.authenticateUser(collaborator).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet().getListOfRules();

        restClient.assertStatusCodeIs(OK);
        rules.assertThat().entriesListContains("name", "Private site rule");
    }

    /**
     * Check we can GET Rule's actions.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void getRuleActions()
    {
        STEP("Create a rule with a few actions");
        final FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        final RestRuleModel ruleWithVariousActions = rulesUtils.createRuleWithVariousActions();
        final UserModel admin = dataUser.getAdminUser();
        final RestRuleModel rule = restClient.authenticateUser(admin).withPrivateAPI().usingNode(folder).usingDefaultRuleSet()
                .createSingleRule(ruleWithVariousActions);

        STEP("Retrieve the created rule via the GET endpoint");
        final RestRuleModel getRuleBody = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).usingDefaultRuleSet().getSingleRule(rule.getId());

        STEP("Assert that actions are returned as expected from the GET endpoint");
        restClient.assertStatusCodeIs(OK);
        getRuleBody.assertThat().field(ACTIONS).contains("actionDefinitionId=copy")
                   .assertThat().field(ACTIONS).contains("destination-folder=" + rulesUtils.getCopyDestinationFolder().getNodeRef())
                   .assertThat().field(ACTIONS).contains("deep-copy=true")
                   .assertThat().field(ACTIONS).contains("actionDefinitionId=check-out")
                   .assertThat().field(ACTIONS).contains("destination-folder=" + rulesUtils.getCheckOutDestinationFolder().getNodeRef())
                   .assertThat().field(ACTIONS).contains("assoc-name=cm:checkout");
    }

    /**
     * Check we can GET rule's conditions.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void getRulesConditions()
    {
        STEP("Create a rule with several conditions");
        RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
        ruleModel.setConditions(rulesUtils.createVariousConditions());

        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        RestRuleModel rule = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        STEP("Retrieve the created rule via the GET endpoint");
        final RestRuleModel getRuleBody = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).usingDefaultRuleSet().getSingleRule(rule.getId());

        STEP("Assert that conditions are retrieved using the GET endpoint");
        restClient.assertStatusCodeIs(OK);
        getRuleBody.assertThat().field(CONDITIONS).contains("comparator=ends")
                   .assertThat().field(CONDITIONS).contains("field=cm:creator")
                   .assertThat().field(CONDITIONS).contains("parameter=ski")
                   .assertThat().field(CONDITIONS).contains("comparator=begins")
                   .assertThat().field(CONDITIONS).contains("field=cm:modelVersion")
                   .assertThat().field(CONDITIONS).contains("parameter=1.");
    }
}
