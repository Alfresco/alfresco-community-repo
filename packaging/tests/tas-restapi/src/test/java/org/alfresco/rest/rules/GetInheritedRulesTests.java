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

import static org.alfresco.rest.requests.RuleSettings.IS_INHERITANCE_ENABLED;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
import org.alfresco.rest.model.RestRuleSetLinkModel;
import org.alfresco.rest.model.RestRuleSetModel;
import org.alfresco.rest.model.RestRuleSetModelsCollection;
import org.alfresco.rest.model.RestRuleSettingsModel;
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
        RestRuleModel parentRule = rulesUtils.createInheritableRuleModel();
        parentRule = restClient.authenticateUser(user).withPrivateAPI().usingNode(parent).usingDefaultRuleSet().createSingleRule(parentRule);
        RestRuleSettingsModel enabled = new RestRuleSettingsModel();
        enabled.setValue(true);
        restClient.authenticateUser(user).withPrivateAPI().usingNode(child).usingRuleSetting(IS_INHERITANCE_ENABLED).updateSetting(enabled);
        RestRuleModel childRule = rulesUtils.createRuleModelWithDefaultValues();
        childRule = restClient.authenticateUser(user).withPrivateAPI().usingNode(child).usingDefaultRuleSet().createSingleRule(childRule);

        STEP("Get the rules in the default rule set for the child folder");
        RestRuleModelsCollection rules = restClient.authenticateUser(user).withPrivateAPI().usingNode(child).usingDefaultRuleSet().getListOfRules();
        rules.assertThat().entriesListContains("id", childRule.getId())
             .and().entriesListCountIs(1);

        STEP("Get the rules in the inherited rule set for the child folder");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI().usingNode(child).include("inclusionType").getListOfRuleSets();
        String inheritedRuleSetId = ruleSets.getEntries().stream()
                                            .filter(ruleSet -> ruleSet.onModel().getInclusionType().equals("inherited"))
                                            .findFirst().get().onModel().getId();
        RestRuleModelsCollection inheritedRules = restClient.authenticateUser(user).withPrivateAPI().usingNode(child).usingRuleSet(inheritedRuleSetId).getListOfRules();
        inheritedRules.assertThat().entriesListContains("id", parentRule.getId())
                      .and().entriesListCountIs(1);
    }

    /**
     * Check we only get the owned rules and no inherited rules in the child folder when parent rule isn't inheritable.
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void getInheritedRules_parentRuleNotInheritable()
    {
        STEP("Create a parent and child folder, each with inheriting rules");
        FolderModel parent = dataContent.usingUser(user).usingSite(site).createFolder();
        FolderModel child = dataContent.usingUser(user).usingResource(parent).createFolder();
        RestRuleModel parentRule = rulesUtils.createRuleModelWithDefaultValues();
        restClient.authenticateUser(user).withPrivateAPI().usingNode(parent).usingDefaultRuleSet().createSingleRule(parentRule);
        RestRuleSettingsModel enabled = new RestRuleSettingsModel();
        enabled.setValue(true);
        restClient.authenticateUser(user).withPrivateAPI().usingNode(child).usingRuleSetting(IS_INHERITANCE_ENABLED).updateSetting(enabled);
        RestRuleModel childRule = rulesUtils.createRuleModelWithDefaultValues();
        childRule = restClient.authenticateUser(user).withPrivateAPI().usingNode(child).usingDefaultRuleSet().createSingleRule(childRule);

        STEP("Get the rules in the default rule set for the child folder");
        RestRuleModelsCollection rules = restClient.authenticateUser(user).withPrivateAPI().usingNode(child).usingDefaultRuleSet().getListOfRules();
        rules.assertThat().entriesListContains("id", childRule.getId())
                .and().entriesListCountIs(1);

        STEP("The inherited rule set for the child folder shouldn't return any rules");
        RestRuleSetModelsCollection ruleSets = restClient.authenticateUser(user).withPrivateAPI().usingNode(child).include("inclusionType").getListOfRuleSets();
        String inheritedRuleSetId = ruleSets.getEntries().stream()
                .filter(ruleSet -> ruleSet.onModel().getInclusionType().equals("inherited"))
                .findFirst().get().onModel().getId();
        RestRuleModelsCollection inheritedRules = restClient.authenticateUser(user).withPrivateAPI().usingNode(child).usingRuleSet(inheritedRuleSetId).getListOfRules();
        inheritedRules.assertThat().entriesListIsEmpty();
    }

    /**
     * Check that we only get each rule once with linking and inheritance, and the order is correct.
     * <p>
     * The folder structure for this test is as follows:
     * <pre>
     *      A --[links]-> DRuleSet
     *      +-B --[owns]-> BRuleSet
     *        +-C --[owns]-> CRuleSet
     *          +-D --[owns]--> DRuleSet
     * </pre>
     */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void rulesReturnedAreUnique()
    {
        STEP("Create four folders with rules");
        FolderModel folderA = dataContent.usingUser(user).usingSite(site).createFolder();
        FolderModel folderB = dataContent.usingUser(user).usingResource(folderA).createFolder();
        FolderModel folderC = dataContent.usingUser(user).usingResource(folderB).createFolder();
        FolderModel folderD = dataContent.usingUser(user).usingResource(folderC).createFolder();
        RestRuleModel ruleB = restClient.authenticateUser(user).withPrivateAPI().usingNode(folderB).usingDefaultRuleSet().createSingleRule(rulesUtils.createInheritableRuleModel());
        RestRuleModel ruleC = restClient.authenticateUser(user).withPrivateAPI().usingNode(folderC).usingDefaultRuleSet().createSingleRule(rulesUtils.createInheritableRuleModel());
        RestRuleModel ruleD = restClient.authenticateUser(user).withPrivateAPI().usingNode(folderD).usingDefaultRuleSet().createSingleRule(rulesUtils.createRuleModelWithDefaultValues());
        RestRuleSettingsModel enabled = new RestRuleSettingsModel();
        enabled.setValue(true);
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folderC).usingRuleSetting(IS_INHERITANCE_ENABLED).updateSetting(enabled);
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folderD).usingRuleSetting(IS_INHERITANCE_ENABLED).updateSetting(enabled);

        STEP("Link folderA to ruleSetD");
        RestRuleSetLinkModel linkModel = new RestRuleSetLinkModel();
        linkModel.setId(folderD.getNodeRef());
        restClient.authenticateUser(user).withPrivateAPI().usingNode(folderA).createRuleLink(linkModel);

        STEP("Get the rule sets for the folderD");
        List<RestRuleSetModel> ruleSets = restClient.authenticateUser(user).withPrivateAPI().usingNode(folderD).getListOfRuleSets().getEntries();

        STEP("Check the rules for each rule set are as expected");
        List<RestRuleModel> expectedRuleIds = List.of(ruleD, ruleB, ruleC);
        IntStream.range(0, 2).forEach(index -> {
            String ruleSetId = ruleSets.get(index).onModel().getId();
            List<RestRuleModel> rules = restClient.authenticateUser(user)
                                                  .withPrivateAPI()
                                                  .usingNode(folderD)
                                                  .usingRuleSet(ruleSetId)
                                                  .getListOfRules()
                                                  .getEntries()
                                                  .stream()
                                                  .map(RestRuleModel::onModel)
                                                  .collect(Collectors.toList());
            assertEquals(rules, List.of(expectedRuleIds.get(index)), "Unexpected rules found for rule set " + ruleSetId);
        });
        assertEquals(ruleSets.size(), 3, "Expected three unique rule sets to be returned but got " + ruleSets);
    }
}
