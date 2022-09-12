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

import static org.alfresco.rest.rules.RulesTestsUtils.createRuleModelWithModifiedValues;
import static org.alfresco.utility.report.log.Step.STEP;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.alfresco.rest.model.RestRuleSetModelsCollection;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for GET /nodes/{nodeId}/rule-sets/{ruleSetId}/rules with rule inheritance.
 */
@Test(groups = {TestGroup.RULES})
public class GetInheritedRulesTests extends RestTest
{
    private UserModel user;
    private SiteModel site;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user and site");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    /**
     * Check we can get all the rules for the folder by providing the different rule set ids.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getInheritedRules()
    {
        STEP("Create a parent and child folder, each with inheriting rules");
        FolderModel parent = dataContent.usingUser(user).usingSite(site).createFolder();
        FolderModel child = dataContent.usingUser(user).usingResource(parent).createFolder();
        RestRuleModel parentRule = createRuleModelWithModifiedValues();
        parentRule = restClient.authenticateUser(user).withCoreAPI().usingNode(parent).usingDefaultRuleSet().createSingleRule(parentRule);
        RestRuleModel childRule = createRuleModelWithModifiedValues();
        childRule = restClient.authenticateUser(user).withCoreAPI().usingNode(child).usingDefaultRuleSet().createSingleRule(childRule);

        STEP("Get the rules in the default rule set for the child folder");
        RestRuleModelsCollection rules = restClient.authenticateUser(user).withCoreAPI().usingNode(child).usingDefaultRuleSet().getListOfRules();
        rules.assertThat().entriesListContains("id", childRule.getId())
             .and().entriesListCountIs(3);

        STEP("Get the rules in the inherited rule set for the child folder");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withCoreAPI().usingNode(child).include("inclusionType").getListOfRuleSets();
        String inheritedRuleSetId = ruleSets.getEntries().stream()
                                            .filter(ruleSet -> ruleSet.onModel().getInclusionType().equals("inherited"))
                                            .findFirst().get().onModel().getId();
        RestRuleModelsCollection inheritedRules = restClient.authenticateUser(user).withCoreAPI().usingNode(child).usingRuleSet(inheritedRuleSetId).getListOfRules();
        inheritedRules.assertThat().entriesListContains("id", parentRule.getId())
                      .and().entriesListCountIs(1);
    }
}
