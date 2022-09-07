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

import static org.alfresco.rest.rules.RulesTestsUtils.*;
import static org.alfresco.utility.constants.UserRole.SiteCollaborator;
import static org.alfresco.utility.constants.UserRole.SiteConsumer;
import static org.alfresco.utility.constants.UserRole.SiteContributor;
import static org.alfresco.utility.constants.UserRole.SiteManager;
import static org.alfresco.utility.model.FileModel.getRandomFileModel;
import static org.alfresco.utility.model.FileType.TEXT_PLAIN;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestActionBodyExecTemplateModel;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for POST /nodes/{nodeId}/rule-sets/{ruleSetId}/rules.
 */
@Test(groups = {TestGroup.RULES})
public class CreateRulesTests extends RestTest
{
    private static final String IGNORE_ID = "id";
    private static final String IGNORE_IS_SHARED = "isShared";
    private UserModel user;
    private SiteModel site;
    private FolderModel ruleFolder;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
    }

    /**
     * Check we can create a rule.
     * <p>
     * Also check that the isShared field is not returned when not requested.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void createRule()
    {
        RestRuleModel ruleModel = createRuleModelWithModifiedValues();

        RestRuleModel rule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                       .createSingleRule(ruleModel);

        RestRuleModel expectedRuleModel = createRuleModelWithModifiedValues();
        expectedRuleModel.setActions(addActionContextParams(expectedRuleModel.getActions()));
        expectedRuleModel.setConditions(createEmptyConditionModel());
        restClient.assertStatusCodeIs(CREATED);
        rule.assertThat().isEqualTo(expectedRuleModel, IGNORE_ID, IGNORE_IS_SHARED)
                .assertThat().field("id").isNotNull()
                .assertThat().field("isShared").isNull();
    }

    /** Check creating a rule in a non-existent folder returns an error. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRuleInNonExistentFolder()
    {
        STEP("Try to create a rule in non-existent folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");

        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("ruleName");

        restClient.authenticateUser(user).withCoreAPI().usingNode(nonExistentFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(NOT_FOUND);
        restClient.assertLastError().containsSummary("fake-id was not found");
    }

    /** Check creating a rule in a non-existent rule set returns an error. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRuleInNonExistentRuleSet()
    {
        STEP("Try to create a rule in non-existent rule set.");
        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("ruleName");

        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingRuleSet("fake-id").createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(NOT_FOUND);
        restClient.assertLastError().containsSummary("fake-id was not found");
    }

    /** Try to create a rule without a name and check the error. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRuleWithEmptyName()
    {
        RestRuleModel ruleModel = createRuleModel("");

        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(BAD_REQUEST);
        restClient.assertLastError().containsSummary("Rule name is a mandatory parameter");
    }

    /** Check we can create two rules with the same name. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void duplicateRuleNameIsAcceptable()
    {
        RestRuleModel ruleModel = createRuleModel("duplicateRuleName");

        STEP("Create two identical rules");
        RestRuleModel ruleA = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);
        RestRuleModel ruleB = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        // Check that the names are the same but the ids are different.
        ruleA.assertThat().field("name").is(ruleB.getName());
        ruleA.assertThat().field("id").isNot(ruleB.getId());
    }

    /** Check that a user without permission to view the folder cannot create a rule in it. */
    public void requireReadPermissionToCreateRule()
    {
        STEP("Create a user and use them to create a private site containing a folder");
        UserModel privateUser = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(privateUser).createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingUser(privateUser).usingSite(privateSite).createFolder();

        STEP("Try to use a different user to create a rule in the private folder");
        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("ruleName");

        restClient.authenticateUser(user).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(FORBIDDEN);
        restClient.assertLastError().containsSummary("Insufficient permissions to manage rules");
    }

    /** Check that a Collaborator cannot create a rule in a private folder. */
    public void siteCollaboratorCannotCreateRule()
    {
        testRolePermissionsWith(SiteCollaborator);

        restClient.assertStatusCodeIs(FORBIDDEN);
        restClient.assertLastError().containsSummary("Insufficient permissions to manage rules");
    }

    /** Check that a Contributor cannot create a rule in a private folder. */
    public void siteContributorCannotCreateRule()
    {
        testRolePermissionsWith(SiteContributor);

        restClient.assertStatusCodeIs(FORBIDDEN);
        restClient.assertLastError().containsSummary("Insufficient permissions to manage rules");
    }

    /** Check that a Consumer cannot create a rule in a private folder. */
    public void siteConsumerCannotCreateRule()
    {
        testRolePermissionsWith(SiteConsumer);

        restClient.assertStatusCodeIs(FORBIDDEN);
        restClient.assertLastError().containsSummary("Insufficient permissions to manage rules");
    }

    /** Check that a siteManager can create a rule in a private folder. */
    public void siteManagerCanCreateRule()
    {
        testRolePermissionsWith(SiteManager)
                .assertThat().field("id").isNotNull()
                .assertThat().field("name").is("testRule");
        restClient.assertStatusCodeIs(CREATED);
    }

    /** Check we can't create a rule under a document node. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void tryToCreateRuleUnderDocument()
    {
        STEP("Create a document.");
        FileModel fileModel = dataContent.usingUser(user).usingSite(site).createContent(getRandomFileModel(TEXT_PLAIN));

        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("ruleName");

        restClient.authenticateUser(user).withCoreAPI().usingNode(fileModel).usingDefaultRuleSet().createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(BAD_REQUEST);
        restClient.assertLastError().containsSummary("folder is expected");
    }

    /** Check we can create several rules. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRules()
    {
        STEP("Create a list of rules in one POST request");
        List<String> ruleNames = List.of("ruleA", "ruleB", "ruleC");
        List<RestRuleModel> ruleModels = ruleNames.stream().map(RulesTestsUtils::createRuleModel).collect(toList());

        RestRuleModelsCollection rules = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                                   .createListOfRules(ruleModels);

        restClient.assertStatusCodeIs(CREATED);

        assertEquals("Unexpected number of rules received in response.", ruleNames.size(), rules.getEntries().size());
        IntStream.range(0, ruleModels.size()).forEach(i ->
                rules.getEntries().get(i).onModel()
                    .assertThat().field("id").isNotNull()
                    .assertThat().field("name").is(ruleNames.get(i)));
    }

    /** Try to create several rules with an error in one of them. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRulesWithOneError()
    {
        STEP("Try to create a three rules but the middle one has an error.");
        RestRuleModel ruleA = createRuleModel("ruleA");
        RestRuleModel ruleB = createRuleModel("");
        // Don't set a name for Rule B.
        RestRuleModel ruleC = createRuleModel("ruleC");
        List<RestRuleModel> ruleModels = List.of(ruleA, ruleB, ruleC);

        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().createListOfRules(ruleModels);

        restClient.assertStatusCodeIs(BAD_REQUEST);
        restClient.assertLastError().containsSummary("Rule name is a mandatory parameter");
    }

    /** Check we can create a rule without description. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRuleWithoutDescription()
    {
        RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        UserModel admin = dataUser.getAdminUser();

        RestRuleModel rule = restClient.authenticateUser(admin).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
            .createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(CREATED);
        rule.assertThat().field("id").isNotNull()
            .assertThat().field("name").is(RULE_NAME_DEFAULT)
            .assertThat().field("description").isNull();
    }

    /** Check we can create a rule without specifying triggers but with the default "inbound" value. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRuleWithoutTriggers()
    {
        RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        UserModel admin = dataUser.getAdminUser();

        RestRuleModel rule = restClient.authenticateUser(admin).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
            .createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(CREATED);
        rule.assertThat().field("id").isNotNull()
            .assertThat().field("name").is(RULE_NAME_DEFAULT)
            .assertThat().field("triggers").is(List.of("inbound"));
    }

    /** Check we can create a rule without error script. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRuleWithoutErrorScript()
    {
        RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        UserModel admin = dataUser.getAdminUser();

        RestRuleModel rule = restClient.authenticateUser(admin).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
            .createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(CREATED);
        rule.assertThat().field("id").isNotNull()
            .assertThat().field("name").is(RULE_NAME_DEFAULT)
            .assertThat().field("errorScript").isNull();
    }

    /** Check we can create a rule with irrelevant isShared flag, and it doesn't have impact to the process. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRuleWithSharedFlag()
    {
        RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        ruleModel.setIsShared(true);
        UserModel admin = dataUser.getAdminUser();

        RestRuleModel rule = restClient.authenticateUser(admin).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
            .createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(CREATED);
        rule.assertThat().field("id").isNotNull()
            .assertThat().field("name").is(RULE_NAME_DEFAULT)
            .assertThat().field("isShared").isNull();
    }

    /** Check we can create a rule. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void createRuleAndIncludeFieldsInResponse()
    {
        RestRuleModel ruleModel = createRuleModel("ruleName");

        RestRuleModel rule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                       .include("isShared")
                                       .createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(CREATED);
        rule.assertThat().field("isShared").isNotNull();
    }

    public RestRuleModel testRolePermissionsWith(UserRole userRole)
    {
        STEP("Create a user and use them to create a private site containing a folder");
        SiteModel privateSite = dataSite.usingUser(user).createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingUser(user).usingSite(privateSite).createFolder();

        STEP(String.format("Add a user with '%s' role in the private site's folder", userRole.toString()));
        UserModel userWithRole = dataUser.createRandomTestUser();
        dataUser.addUserToSite(userWithRole, privateSite, userRole);
        RestRuleModel ruleModel = createRuleModel("testRule", List.of(createDefaultActionModel()));

        return restClient.authenticateUser(userWithRole).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(ruleModel);
    }

    /**
     * Check we can create a rule with several actions.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void createRuleWithActions()
    {
        final Map<String, Serializable> copyParams =
                Map.of("destination-folder", "dummy-folder-node", "deep-copy", true);
        final RestActionBodyExecTemplateModel copyAction = createCustomActionModel("copy", copyParams);
        final Map<String, Serializable> checkOutParams =
                Map.of("destination-folder", "dummy-folder-node", "assoc-name", "cm:checkout", "assoc-type",
                        "cm:contains");
        final RestActionBodyExecTemplateModel checkOutAction = createCustomActionModel("check-out", checkOutParams);
        final Map<String, Serializable> scriptParams = Map.of("script-ref", "dummy-script-node-id");
        final RestActionBodyExecTemplateModel scriptAction = createCustomActionModel("script", scriptParams);
        final RestRuleModel ruleModel = createRuleModelWithDefaultValues();
        ruleModel.setActions(Arrays.asList(copyAction, checkOutAction, scriptAction));

        final UserModel admin = dataUser.getAdminUser();

        final RestRuleModel rule = restClient.authenticateUser(admin).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);

        final RestRuleModel expectedRuleModel = createRuleModelWithDefaultValues();
        expectedRuleModel.setActions(addActionContextParams(Arrays.asList(copyAction, checkOutAction, scriptAction)));
        expectedRuleModel.setConditions(createEmptyConditionModel());
        expectedRuleModel.setTriggers(List.of("inbound"));

        restClient.assertStatusCodeIs(CREATED);
        rule.assertThat().isEqualTo(expectedRuleModel, IGNORE_ID, IGNORE_IS_SHARED)
                .assertThat().field("isShared").isNull();
    }
}
