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

import static org.alfresco.rest.rules.RulesTestsUtils.ID;
import static org.alfresco.rest.rules.RulesTestsUtils.INBOUND;
import static org.alfresco.rest.rules.RulesTestsUtils.INVERTED;
import static org.alfresco.rest.rules.RulesTestsUtils.IS_SHARED;
import static org.alfresco.rest.rules.RulesTestsUtils.RULE_ASYNC_DEFAULT;
import static org.alfresco.rest.rules.RulesTestsUtils.RULE_CASCADE_DEFAULT;
import static org.alfresco.rest.rules.RulesTestsUtils.RULE_ENABLED_DEFAULT;
import static org.alfresco.rest.rules.RulesTestsUtils.createCompositeCondition;
import static org.alfresco.rest.rules.RulesTestsUtils.createCustomActionModel;
import static org.alfresco.rest.rules.RulesTestsUtils.createDefaultActionModel;
import static org.alfresco.rest.rules.RulesTestsUtils.createRuleModel;
import static org.alfresco.rest.rules.RulesTestsUtils.createRuleModelWithDefaultValues;
import static org.alfresco.rest.rules.RulesTestsUtils.createRuleModelWithModifiedValues;
import static org.alfresco.rest.rules.RulesTestsUtils.createSimpleCondition;
import static org.alfresco.rest.rules.RulesTestsUtils.createVariousConditions;
import static org.alfresco.utility.constants.UserRole.SiteCollaborator;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestActionBodyExecTemplateModel;
import org.alfresco.rest.model.RestCompositeConditionDefinitionModel;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for PUT /nodes/{nodeId}/rule-sets/{ruleSetId}/rules.
 */
@Test (groups = { TestGroup.RULES })
public class UpdateRulesTests extends RestTest
{
    private UserModel user;
    private SiteModel site;
    private FolderModel ruleFolder;

    @BeforeClass (alwaysRun = true)
    public void dataPreparation()
    {
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
        ruleFolder = dataContent.usingUser(user).usingSite(site).createFolder();
    }

    /**
     * Check we can update a rule.
     * <p>
     * Also check that the isShared field is not returned when not requested.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRule()
    {
        RestRuleModel rule = createAndSaveRule("Rule name");

        STEP("Try to update the rule.");
        RestRuleModel updatedRuleModel = createRuleModel("Updated rule name");
        RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                       .updateRule(rule.getId(), updatedRuleModel);

        restClient.assertStatusCodeIs(OK);
        updatedRule.assertThat().field(ID).is(rule.getId())
                   .assertThat().field("name").is("Updated rule name")
                   .assertThat().field(IS_SHARED).isNull();
    }

    /** Check we get a 404 if trying to update a rule in a folder that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateRuleForNonExistentFolder()
    {
        RestRuleModel rule = createAndSaveRule("Rule name");

        STEP("Try to update a rule in a non-existent folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");

        RestRuleModel updatedRuleModel = new RestRuleModel();
        updatedRuleModel.setName("Updated rule name");
        restClient.authenticateUser(user).withCoreAPI().usingNode(nonExistentFolder).usingDefaultRuleSet()
                  .updateRule(rule.getId(), updatedRuleModel);

        restClient.assertLastError().statusCodeIs(NOT_FOUND)
                                    .containsSummary("Folder with id fake-id was not found");
    }

    /** Check we get a 404 if trying to update a rule in a rule set that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateRuleForNonExistentRuleSet()
    {
        RestRuleModel rule = createAndSaveRule("Rule name");

        STEP("Try to update a rule in a non-existent rule set.");
        RestRuleModel updatedRuleModel = new RestRuleModel();
        updatedRuleModel.setName("Updated rule name");
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingRuleSet("fake-id")
                  .updateRule(rule.getId(), updatedRuleModel);

        restClient.assertLastError().statusCodeIs(NOT_FOUND)
                  .containsSummary("Rule set with id fake-id was not found");
    }

    /** Check we get a 404 if trying to update a rule that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateRuleForNonExistentRuleId()
    {
        STEP("Try to update a rule that doesn't exist.");
        RestRuleModel updatedRuleModel = new RestRuleModel();
        updatedRuleModel.setName("Updated rule name");
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                  .updateRule("fake-id", updatedRuleModel);

        restClient.assertLastError().statusCodeIs(NOT_FOUND)
                  .containsSummary("fake-id was not found");
    }

    /** Check that a user without permission cannot update a rule. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void requirePermissionToUpdateRule()
    {
        STEP("Create a user and use them to create a private site containing a folder");
        UserModel privateUser = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(privateUser).createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingUser(privateUser).usingSite(privateSite).createFolder();

        STEP("Create a collaborator and check they don't have permission to create a rule");
        UserModel collaborator = dataUser.createRandomTestUser();
        dataUser.addUserToSite(collaborator, privateSite, SiteCollaborator);
        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("ruleName");
        restClient.authenticateUser(user).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(FORBIDDEN);
        restClient.assertLastError().containsSummary("Insufficient permissions to manage rules");
    }

    /** Check we get an error trying to update a rule to have no name. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateRuleToHaveEmptyName()
    {
        RestRuleModel rule = createAndSaveRule("Rule name");

        STEP("Try to update the rule to have no name.");
        RestRuleModel updatedRuleModel = createRuleModel("");
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().updateRule(rule.getId(), updatedRuleModel);

        restClient.assertLastError().statusCodeIs(BAD_REQUEST)
                                    .containsSummary("Rule name is a mandatory parameter");
    }

    /** Check that updates to the rule's id are ignored. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void tryToUpdateRuleId()
    {
        RestRuleModel rule = createAndSaveRule("Rule name");

        STEP("Try to update the rule id and check it isn't changed.");
        RestRuleModel updatedRuleModel = createRuleModel("Rule name");
        updatedRuleModel.setId("new-rule-id");
        RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                              .updateRule(rule.getId(), updatedRuleModel);

        updatedRule.assertThat().field(ID).is(rule.getId());
    }

    /** Check we can update a rule and get the included fields. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleWithIncludedFields()
    {
        RestRuleModel rule = createAndSaveRule("Rule name");

        STEP("Try to update the rule.");
        RestRuleModel updatedRuleModel = createRuleModel("Updated rule name");
        RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                              .include(IS_SHARED)
                                              .updateRule(rule.getId(), updatedRuleModel);

        updatedRule.assertThat().field(IS_SHARED).isNotNull();
    }

    /**
     * Check we get error when attempt to update a rule to one without any actions.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void updateRuleWithoutActionsShouldFail()
    {
        RestRuleModel rule = createAndSaveRule("Rule name");

        STEP("Try to update the rule - set no actions.");
        rule.setActions(null);
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .include(IS_SHARED)
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(BAD_REQUEST);
        restClient.assertLastError().containsSummary("A rule must have at least one action");
    }

    /**
     * Check we get error when attempt to update a rule to one with invalid action.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void updateRuleWithInvalidActionDefinitionShouldFail()
    {
        RestRuleModel rule = createAndSaveRule("Rule name");

        STEP("Try to update the rule - set no actions.");
        final RestActionBodyExecTemplateModel invalidAction = new RestActionBodyExecTemplateModel();
        final String actionDefinitionId = "invalid-definition-value";
        invalidAction.setActionDefinitionId(actionDefinitionId);
        invalidAction.setParams(Map.of("dummy-key", "dummy-value"));
        rule.setActions(List.of(invalidAction));
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .include(IS_SHARED)
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(NOT_FOUND);
        restClient.assertLastError().containsSummary(actionDefinitionId);
    }

    /** Check we can use the POST response to create the new rule. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateCopyRuleWithResponseFromPOST()
    {
        FolderModel destination = dataContent.usingUser(user).usingSite(site).createFolder();

        RestActionBodyExecTemplateModel copyAction = new RestActionBodyExecTemplateModel();
        copyAction.setActionDefinitionId("copy");
        copyAction.setParams(ImmutableMap.of("destination-folder", destination.getNodeRef()));
        RestRuleModel rule = createAndSaveRule("Rule name", List.of(copyAction));

        STEP("Try to update the rule.");
        rule.setName("Updated rule name");
        RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                              .include(IS_SHARED)
                                              .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(OK);
        updatedRule.assertThat().field("name").is("Updated rule name")
                   .assertThat().field("actions.actionDefinitionId").is(List.of("copy"))
                   .assertThat().field("actions.params").is(List.of(ImmutableMap.of("destination-folder", destination.getNodeRef())));
    }

    /** Check we can use the POST response and update rule fields. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleFields()
    {
        final RestRuleModel rule = createAndSaveRule(createRuleModelWithModifiedValues());

        STEP("Try to update the rule fields.");
        rule.setName("Updated rule name");
        rule.setTriggers(List.of(INBOUND));
        final String updatedDescription = "Updated description";
        rule.setDescription(updatedDescription);
        rule.setIsEnabled(!RULE_ENABLED_DEFAULT);
        rule.setIsInheritable(!RULE_CASCADE_DEFAULT);
        rule.setIsAsynchronous(!RULE_ASYNC_DEFAULT);
        final String updatedErrorScript = "updated-error-script";
        rule.setErrorScript(updatedErrorScript);
        final RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(OK);
        updatedRule.assertThat().isEqualTo(rule, ID)
                .assertThat().field(ID).isNotNull();
    }

    /** Check we can use the POST response and update rule by adding conditions. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleAddConditions()
    {
        final RestRuleModel rule = createAndSaveRule(createRuleModelWithModifiedValues());

        STEP("Try to update the rule and add conditions.");
        rule.setConditions(createVariousConditions());

        final RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(OK);
        updatedRule.assertThat().isEqualTo(rule, ID)
                .assertThat().field(ID).isNotNull();
    }

    /** Check we can use the POST response and update a rule rule without any conditions by adding null conditions. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleAddNullConditions()
    {
        final RestRuleModel rule = createAndSaveRule(createRuleModelWithModifiedValues());

        STEP("Try to update the rule and add null conditions.");
        rule.setConditions(null);

        final RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(OK);
        updatedRule.assertThat().isEqualTo(rule, ID)
                .assertThat().field(ID).isNotNull();
    }

    /** Check we can use the POST response and update rule by modifying conditions. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleModifyConditions()
    {
        final RestRuleModel ruleModelWithInitialValues = createRuleModelWithModifiedValues();
        ruleModelWithInitialValues.setConditions(createVariousConditions());
        final RestRuleModel rule = createAndSaveRule(ruleModelWithInitialValues);

        STEP("Try to update the rule and modify conditions.");
        final RestCompositeConditionDefinitionModel compositeCondition = createCompositeCondition(
                List.of(createCompositeCondition(false, List.of(createSimpleCondition("tag", "equals", "sample_tag")))));
        rule.setConditions(compositeCondition);

        final RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(OK);
        updatedRule.assertThat().isEqualTo(rule, ID)
                .assertThat().field(ID).isNotNull();
    }

    /** Check we can use the POST response and update rule by removing all conditions. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleRemoveAllConditions()
    {
        final RestRuleModel ruleModelWithInitialValues = createRuleModelWithModifiedValues();
        ruleModelWithInitialValues.setConditions(createVariousConditions());
        final RestRuleModel rule = createAndSaveRule(ruleModelWithInitialValues);

        STEP("Try to update the rule and remove all conditions.");
        rule.setConditions(null);

        final RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(OK);
        updatedRule.assertThat().isEqualTo(rule, ID)
                .assertThat().field(ID).isNotNull();
    }

    /** Check we get a 400 error when using the POST response and update rule by adding condition with invalid category. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleWithInvalidCategoryInConditionAndFail()
    {
        final RestRuleModel ruleModelWithInitialValues = createRuleModelWithModifiedValues();
        ruleModelWithInitialValues.setConditions(createVariousConditions());
        final RestRuleModel rule = createAndSaveRule(ruleModelWithInitialValues);

        STEP("Try to update the rule with invalid condition.");
        final RestCompositeConditionDefinitionModel conditions = createCompositeCondition(
                List.of(createCompositeCondition(!INVERTED, List.of(createSimpleCondition("category", "equals", "fake-category-id")))));
        rule.setConditions(conditions);

        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(BAD_REQUEST);
        restClient.assertLastError().containsSummary("Category in condition is invalid");
    }

    /** Check we get a 400 error when using the POST response and update rule by adding condition without comparator when it is required. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleWithConditionWithoutComparatorAndFail()
    {
        final RestRuleModel ruleModelWithInitialValues = createRuleModelWithModifiedValues();
        ruleModelWithInitialValues.setConditions(createVariousConditions());
        final RestRuleModel rule = createAndSaveRule(ruleModelWithInitialValues);

        STEP("Try to update the rule with invalid condition (null comparator when required non-null).");
        final RestCompositeConditionDefinitionModel conditions = createCompositeCondition(
                List.of(createCompositeCondition(!INVERTED, List.of(createSimpleCondition("size", null, "65500")))));
        rule.setConditions(conditions);

        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(BAD_REQUEST);
        restClient.assertLastError().containsSummary("Comparator in condition must not be blank");
    }

    /** Check we get a 400 error when using the POST response and update rule by adding condition without field. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleWithConditionWithoutFieldAndFail()
    {
        final RestRuleModel ruleModelWithInitialValues = createRuleModelWithModifiedValues();
        ruleModelWithInitialValues.setConditions(createVariousConditions());
        final RestRuleModel rule = createAndSaveRule(ruleModelWithInitialValues);

        STEP("Try to update the rule with invalid condition (null field).");
        final RestCompositeConditionDefinitionModel conditions = createCompositeCondition(
                List.of(createCompositeCondition(!INVERTED, List.of(createSimpleCondition(null, "greater_than", "65500")))));
        rule.setConditions(conditions);

        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(BAD_REQUEST);
        restClient.assertLastError().containsSummary("Field in condition must not be blank");
    }

    /** Check we get a 400 error when using the POST response and update rule by adding condition without parameter value. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRuleWithConditionWithoutParamValueAndFail()
    {
        final RestRuleModel ruleModelWithInitialValues = createRuleModelWithModifiedValues();
        ruleModelWithInitialValues.setConditions(createVariousConditions());
        final RestRuleModel rule = createAndSaveRule(ruleModelWithInitialValues);

        STEP("Try to update the rule with invalid condition (null parameter).");
        final RestCompositeConditionDefinitionModel conditions = createCompositeCondition(
                List.of(createCompositeCondition(!INVERTED, List.of(createSimpleCondition("size", "greater_than", "")))));
        rule.setConditions(conditions);

        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(BAD_REQUEST);
        restClient.assertLastError().containsSummary("Parameter in condition must not be blank");
    }

    /**
     * Check we can update a rule by adding several actions.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void updateRuleAddActions()
    {
        final RestRuleModel rule = createAndSaveRule(createRuleModelWithModifiedValues());

        STEP("Try to update the rule by adding several actions");
        final Map<String, Serializable> copyParams =
                Map.of("destination-folder", "dummy-folder-node", "deep-copy", true);
        final RestActionBodyExecTemplateModel copyAction = createCustomActionModel("copy", copyParams);
        final Map<String, Serializable> scriptParams = Map.of("script-ref", "dummy-script-node-id");
        final RestActionBodyExecTemplateModel scriptAction = createCustomActionModel("script", scriptParams);
        rule.setActions(Arrays.asList(copyAction, scriptAction));

        final RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(OK);
        updatedRule.assertThat().isEqualTo(rule, ID)
                .assertThat().field(ID).isNotNull();
    }

    /**
     * Check we get a 400 error when attempting to update a rule by adding action with not allowed parameter.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void updateRuleAddCheckoutActionForOutboundShouldFail()
    {
        final RestRuleModel rule = createAndSaveRule(createRuleModelWithModifiedValues());

        STEP("Try to update the rule by adding checkout action");
        final Map<String, Serializable> checkOutParams =
                Map.of("destination-folder", "dummy-folder-node", "assoc-name", "cm:checkout", "assoc-type",
                        "cm:contains");
        final RestActionBodyExecTemplateModel checkOutAction = createCustomActionModel("check-out", checkOutParams);
        final Map<String, Serializable> scriptParams = Map.of("script-ref", "dummy-script-node-id");
        rule.setActions(List.of(checkOutAction));

        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(BAD_REQUEST);
        restClient.assertLastError().containsSummary("Check out action cannot be performed for the rule type outbound!");
    }

    /**
     * Check we get a 500 error when attempting to update a rule by adding action with parameter with non existing namespace in value.
     * In near future we need to fix this kind of negative path to return a 4xx error.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void updateRuleAddActionWithInvalidParamShouldFail()
    {
        final RestRuleModel rule = createAndSaveRule(createRuleModelWithModifiedValues());

        STEP("Try to update the rule by adding action with invalid parameter (non-existing namespace in value)");
        final RestActionBodyExecTemplateModel action = new RestActionBodyExecTemplateModel();
        action.setActionDefinitionId("add-features");
        action.setParams(Map.of("aspect-name", "dummy:dummy"));
        rule.setActions(List.of(action));

        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .updateRule(rule.getId(), rule);

        restClient.assertStatusCodeIs(INTERNAL_SERVER_ERROR);
        restClient.assertLastError().containsSummary("Namespace prefix dummy is not mapped to a namespace URI");
    }

    private RestRuleModel createAndSaveRule(String name)
    {
        return createAndSaveRule(name, List.of(createDefaultActionModel()));
    }

    /**
     * Create a rule for folder and store it.
     *
     * @param name The name for the rule.
     * @param restActionModels Rule's actions.
     * @return The created rule.
     */
    private RestRuleModel createAndSaveRule(String name, List<RestActionBodyExecTemplateModel> restActionModels)
    {
        STEP("Create a rule called " + name + ", containing actions: " + restActionModels);
        RestRuleModel ruleModel = createRuleModel(name, restActionModels);
        return restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
            .createSingleRule(ruleModel);
    }

    /**
     * Create a rule for folder and store it.
     *
     * @param ruleModel RuleModel used as create request
     * @return The created rule.
     */
    private RestRuleModel createAndSaveRule(final RestRuleModel ruleModel)
    {
        STEP("Create a rule: " + ruleModel);
        return restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                .createSingleRule(ruleModel);
    }
}
