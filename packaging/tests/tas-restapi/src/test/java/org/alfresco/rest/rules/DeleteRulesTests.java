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

import static org.alfresco.utility.constants.UserRole.SiteCollaborator;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.stream.Stream;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for DELETE /nodes/{nodeId}/rule-sets/{ruleSetId}/rules/{ruleId}.
 */
@Test(groups = {TestGroup.RULES})
public class DeleteRulesTests extends RestTest
{
    private static final String FAKE_NODE_REF = "fake-node-id";

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
        createdRules = Stream.of("ruleA", "ruleB")
                .map(ruleName -> {
                    RestRuleModel ruleModel = new RestRuleModel();
                    ruleModel.setName(ruleName);
                    return restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet()
                            .createSingleRule(ruleModel);
                })
                .collect(toList());
        createdRuleA = createdRules.get(0);
    }

    /**
     * Delete a rule by its id.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY})
    public void deleteSingleRuleAndGet204()
    {
        STEP("Attempt delete the rule ");
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().deleteRule(createdRuleA.getId());
        restClient.assertStatusCodeIs(NO_CONTENT);
    }

    /**
     * Try to delete a rule in a non-existing folder and get 404.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void deleteRuleInNonExistingFolderAndGet404()
    {
        STEP("Create a non-existing folder model");
        final FolderModel nonExistingFolder = new FolderModel();
        nonExistingFolder.setNodeRef(FAKE_NODE_REF);

        STEP("Attempt delete the rule in non-existing folder");
        restClient.authenticateUser(user).withCoreAPI().usingNode(nonExistingFolder).usingDefaultRuleSet().deleteRule(createdRuleA.getId());

        restClient.assertLastError().statusCodeIs(NOT_FOUND);
    }

    /**
     * Try to delete a rule in a non-existing rule set and get 404.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES})
    public void deleteRuleInNonExistingRuleSetAndGet404()
    {

        STEP("Attempt delete the rule in non-existing rule set");
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingRuleSet(FAKE_NODE_REF).deleteRule(createdRuleA.getId());

        restClient.assertLastError().statusCodeIs(NOT_FOUND);
    }

    /**
     * Try to delete a non-existing rule and get 404.
     */
    @Test(groups = {TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY})
    public void deleteNonExistingRuleAndGet404()
    {
        STEP("Attempt delete non-existing rule");
        restClient.authenticateUser(user).withCoreAPI().usingNode(ruleFolder).usingDefaultRuleSet().deleteRule(FAKE_NODE_REF);

        restClient.assertLastError().statusCodeIs(NOT_FOUND);
    }

    /**
     * Check that a user without write permission on folder cannot delete a rule inside it.
     */
    public void deleteSingleRuleWithoutWritePermissionAndGet403()
    {
        STEP("Create a user and use them to create a private site containing a folder with a rule");
        UserModel privateUser = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(privateUser).createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingUser(privateUser).usingSite(privateSite).createFolder();
        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("Private site rule");
        restClient.authenticateUser(privateUser).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Try to delete the rule with another user");
        restClient.authenticateUser(user).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().deleteRule(ruleModel.getId());

        restClient.assertLastError().statusCodeIs(FORBIDDEN);
    }

    /**
     * Check that a user with write permission on folder can delete a rule inside it.
     */
    public void deleteSingleRuleWithWritePermissionAndGet204()
    {
        STEP("Create a user and use them to create a private site containing a folder with a rule");
        UserModel privateUser = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingUser(privateUser).createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingUser(privateUser).usingSite(privateSite).createFolder();
        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("Private site rule");
        restClient.authenticateUser(privateUser).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Create a collaborator in the private site");
        UserModel collaborator = dataUser.createRandomTestUser();
        collaborator.setUserRole(SiteCollaborator);
        restClient.authenticateUser(privateUser).withCoreAPI().usingSite(privateSite).addPerson(collaborator);

        STEP("Check the collaborator can delete the rule");
        restClient.authenticateUser(collaborator).withCoreAPI().usingNode(privateFolder).usingDefaultRuleSet().deleteRule(ruleModel.getId());

        restClient.assertStatusCodeIs(NO_CONTENT);
    }
}
