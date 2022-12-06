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
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleSetModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test (groups = { TestGroup.RULES })
public class ReorderRulesTests extends RulesRestTest
{
    private UserModel user;
    private SiteModel site;

    @BeforeClass (alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user and site.");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    /** Check we can get the ordered list of rules in a rule set. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getOrderedRuleIds()
    {
        STEP("Create a folder containing three rules in the existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        List<RestRuleModel> rules = createRulesInFolder(folder, user);

        STEP("Get the default rule set for the folder including the ordered rule ids");
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder)
                                             .include("ruleIds").getDefaultRuleSet();

        List<String> expectedRuleIds = rules.stream().map(RestRuleModel::getId).collect(toList());
        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("ruleIds").is(expectedRuleIds);
    }

    /** Check that a user can view the order of the rules in a rule set if they only have read permission. */
    @Test
    public void getRuleSetAndRuleIdsWithReadOnlyPermission()
    {
        STEP("Create a site owned by admin and add user as a consumer");
        SiteModel siteModel = dataSite.usingAdmin().createPrivateRandomSite();
        dataUser.addUserToSite(user, siteModel, UserRole.SiteConsumer);

        STEP("Use admin to create a folder with a rule set and three rules in it");
        FolderModel ruleFolder = dataContent.usingAdmin().usingSite(siteModel).createFolder();
        dataContent.usingAdmin().usingResource(ruleFolder).createFolder();
        List<RestRuleModel> rules = createRulesInFolder(ruleFolder, dataUser.getAdminUser());

        STEP("Get the rule set with the ordered list of rules");
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                                             .include("ruleIds").getDefaultRuleSet();

        restClient.assertStatusCodeIs(OK);
        List<String> ruleIds = rules.stream().map(RestRuleModel::getId).collect(toList());
        ruleSet.assertThat().field("ruleIds").is(ruleIds);
    }

    /** Check we can reorder the rules in a rule set. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void reorderRules()
    {
        STEP("Create a folder containing three rules in the existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        List<RestRuleModel> rules = createRulesInFolder(folder, user);

        STEP("Reverse the order of the rules within the rule set");
        List<String> reversedRuleIds = Lists.reverse(rules.stream().map(RestRuleModel::getId).collect(toList()));
        RestRuleSetModel ruleSetBody = new RestRuleSetModel();
        ruleSetBody.setId("-default-");
        ruleSetBody.setRuleIds(reversedRuleIds);
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder)
                                             .include("ruleIds").updateRuleSet(ruleSetBody);

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("ruleIds").is(reversedRuleIds);
    }

    /** Check we can reorder the rules in a rule set by editing the response from the GET. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void reorderRulesUsingResponseFromGET()
    {
        STEP("Create a folder containing three rules in the existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();
        List<RestRuleModel> rules = createRulesInFolder(folder, user);

        STEP("Get the rule set with its id.");
        RestRuleSetModel ruleSetResponse = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder)
                                                     .include("ruleIds").getDefaultRuleSet();

        STEP("Reverse the order of the rules within the rule set");
        ruleSetResponse.setRuleIds(Lists.reverse(ruleSetResponse.getRuleIds()));
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withPrivateAPI().usingNode(folder)
                                             .include("ruleIds").updateRuleSet(ruleSetResponse);

        restClient.assertStatusCodeIs(OK);
        List<String> reversedRuleIds = Lists.reverse(rules.stream().map(RestRuleModel::getId).collect(toList()));
        ruleSet.assertThat().field("ruleIds").is(reversedRuleIds);
    }

    /** Check that a user cannot reorder the rules in a rule set if they only have read permission. */
    @Test
    public void reorderRulesWithoutPermission()
    {
        STEP("Create a site owned by admin and add user as a consumer");
        SiteModel siteModel = dataSite.usingAdmin().createPrivateRandomSite();
        dataUser.addUserToSite(user, siteModel, UserRole.SiteContributor);

        STEP("Use admin to create a folder with a rule set and three rules in it");
        FolderModel ruleFolder = dataContent.usingAdmin().usingSite(siteModel).createFolder();
        dataContent.usingAdmin().usingResource(ruleFolder).createFolder();
        List<RestRuleModel> rules = createRulesInFolder(ruleFolder, dataUser.getAdminUser());

        STEP("Try to reorder the rules as the contributor");
        List<String> reversedRuleIds = Lists.reverse(rules.stream().map(RestRuleModel::getId).collect(toList()));
        RestRuleSetModel ruleSetBody = new RestRuleSetModel();
        ruleSetBody.setId("-default-");
        ruleSetBody.setRuleIds(reversedRuleIds);
        restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                  .include("ruleIds").updateRuleSet(ruleSetBody);

        restClient.assertStatusCodeIs(FORBIDDEN);
    }

    /** Check that a user can reorder the rules in a rule set if they have write permission. */
    @Test
    public void reorderRulesWithPermission()
    {
        STEP("Create a site owned by admin and add user as a collaborator");
        SiteModel siteModel = dataSite.usingAdmin().createPrivateRandomSite();
        dataUser.addUserToSite(user, siteModel, UserRole.SiteCollaborator);

        STEP("Use admin to create a folder with a rule set and three rules in it");
        FolderModel ruleFolder = dataContent.usingAdmin().usingSite(siteModel).createFolder();
        dataContent.usingAdmin().usingResource(ruleFolder).createFolder();
        List<RestRuleModel> rules = createRulesInFolder(ruleFolder, dataUser.getAdminUser());

        STEP("Try to reorder the rules as the contributor");
        List<String> reversedRuleIds = Lists.reverse(rules.stream().map(RestRuleModel::getId).collect(toList()));
        RestRuleSetModel ruleSetBody = new RestRuleSetModel();
        ruleSetBody.setId("-default-");
        ruleSetBody.setRuleIds(reversedRuleIds);
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withPrivateAPI().usingNode(ruleFolder)
                                             .include("ruleIds").updateRuleSet(ruleSetBody);

        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("ruleIds").is(reversedRuleIds);
    }

    /** Create three rules in the given folder. */
    private List<RestRuleModel> createRulesInFolder(FolderModel folder, UserModel user)
    {
        return IntStream.range(0, 3).mapToObj(index ->
        {
            RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
            return restClient.authenticateUser(user).withPrivateAPI().usingNode(folder).usingDefaultRuleSet().createSingleRule(ruleModel);
        }).collect(toList());
    }
}
