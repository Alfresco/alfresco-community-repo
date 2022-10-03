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

import static org.alfresco.rest.rules.RulesTestsUtils.createRuleModel;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.stream.IntStream;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleSetModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test (groups = { TestGroup.RULES })
public class ReorderRules extends RestTest
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
        List<RestRuleModel> rules = IntStream.range(0, 3).mapToObj(index -> {
            RestRuleModel ruleModel = createRuleModel("ruleName");
            return restClient.authenticateUser(user).withCoreAPI().usingNode(folder).usingDefaultRuleSet().createSingleRule(ruleModel);
        }).collect(toList());

        STEP("Get the default rule set for the folder including the ordered rule ids");
        RestRuleSetModel ruleSet = restClient.authenticateUser(user).withCoreAPI().usingNode(folder)
                                             .include("ruleIds").getDefaultRuleSet();

        List<String> expectedRuleIds = rules.stream().map(RestRuleModel::getId).collect(toList());
        restClient.assertStatusCodeIs(OK);
        ruleSet.assertThat().field("ruleIds").is(expectedRuleIds);
    }
}
