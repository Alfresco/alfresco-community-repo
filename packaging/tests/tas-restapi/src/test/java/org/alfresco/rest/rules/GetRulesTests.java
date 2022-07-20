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

import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.rest.RestTest;
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

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    /** Check we can get an empty list of rules. */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getEmptyRulesList()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Get the rules that apply to the folder");
        RestRuleModelsCollection rules = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).usingDefaultRuleSet().getListOfRules();

        restClient.assertStatusCodeIs(OK);
        assertTrue("Expected no rules to be present.", rules.isEmpty());
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
}

