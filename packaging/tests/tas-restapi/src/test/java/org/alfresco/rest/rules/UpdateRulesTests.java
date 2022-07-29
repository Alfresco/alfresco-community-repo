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

import static org.alfresco.utility.constants.UserRole.SiteCollaborator;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.fail;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Map;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestActionBodyExecTemplateModel;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleSetModel;
import org.alfresco.utility.constants.UserRole;
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

    /** Check we can update a rule. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void updateRule()
    {
        RestRuleModel rule = createRule("Rule name");

        STEP("Try to update the rule.");
        RestRuleModel updatedRuleModel = createRule("Updated rule name");
        RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                       .updateRule(rule.getId(), updatedRuleModel);

        restClient.assertStatusCodeIs(OK);
        updatedRule.assertThat().field("id").is(rule.getId())
                   .assertThat().field("name").is("Updated rule name");
    }

    /** Check we get a 404 if trying to update a rule in a folder that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateRuleForNonExistentFolder()
    {
        RestRuleModel rule = createRule("Rule name");

        STEP("Try to update a rule in a non-existent folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");

        RestRuleModel updatedRuleModel = new RestRuleModel();
        updatedRuleModel.setName("Updated rule name");
        restClient.authenticateUser(user).withCoreAPI().usingNode(nonExistentFolder).usingDefaultRuleSet()
                  .updateRule(rule.getId(), updatedRuleModel);

        restClient.assertLastError().statusCodeIs(NOT_FOUND)
                                    .containsSummary("fake-id was not found");
    }

    /** Check we get a 404 if trying to update a rule in a rule set that doesn't exist. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateRuleForNonExistentRuleSet()
    {
        RestRuleModel rule = createRule("Rule name");

        STEP("Try to update a rule in a non-existent rule set.");
        RestRuleModel updatedRuleModel = new RestRuleModel();
        updatedRuleModel.setName("Updated rule name");
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingRuleSet("fake-id")
                  .updateRule(rule.getId(), updatedRuleModel);

        restClient.assertLastError().statusCodeIs(NOT_FOUND)
                  .containsSummary("fake-id was not found");
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
        RestRuleModel rule = createRule("Rule name");

        STEP("Try to update the rule to have no name.");
        RestRuleModel updatedRuleModel = new RestRuleModel();
        updatedRuleModel.setName("");
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().updateRule(rule.getId(), updatedRuleModel);

        restClient.assertLastError().statusCodeIs(BAD_REQUEST)
                                    .containsSummary("Rule name is a mandatory parameter");
    }

    /** Check that updates to the rule's id are ignored. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void tryToUpdateRuleId()
    {
        RestRuleModel rule = createRule("Rule name");

        STEP("Try to update the rule id and check it isn't changed.");
        RestRuleModel updatedRuleModel = createRule("Rule name");
        updatedRuleModel.setId("new-rule-id");
        RestRuleModel updatedRule = restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                                              .updateRule(rule.getId(), updatedRuleModel);

        updatedRule.assertThat().field("id").is(rule.getId());
    }

    /**
     * Create a rule.
     *
     * @param name The name for the rule.
     * @return The created rule.
     */
    private RestRuleModel createRule(String name)
    {
        STEP("Create a rule model called " + name);
        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName(name);
        ruleModel.setActions(List.of(createAction()));
        return restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
            .createSingleRule(ruleModel);
    }

    /**
     * Create a rule's action.
     *
     * @return The created action.
     */
    private RestActionBodyExecTemplateModel createAction() {
        RestActionBodyExecTemplateModel restActionModel = new RestActionBodyExecTemplateModel();
        restActionModel.setActionDefinitionId("add-features");
        restActionModel.setParams(Map.of("aspect-name", "{http://www.alfresco.org/model/audio/1.0}audio", "actionContext", "rule"));
        return restActionModel;
    }
}
