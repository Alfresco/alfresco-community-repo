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

import static org.alfresco.utility.report.log.Step.STEP;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;
import java.util.stream.IntStream;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.rest.model.RestRuleModelsCollection;
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
    private UserModel user;
    private SiteModel site;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    /** Check we can create a rule. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.SANITY })
    public void createRule()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        RestRuleModel ruleModel = new RestRuleModel();
        ruleModel.setName("ruleName");

        RestRuleModel rule = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).usingDefaultRuleSet()
                                       .createSingleRule(ruleModel);

        restClient.assertStatusCodeIs(CREATED);

        rule.assertThat().field("id").isNotNull()
            .assertThat().field("name").is("ruleName");
    }

    /** Check we can create several rules. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void createRules()
    {
        STEP("Create a folder in existing site");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create a list of rules in one POST request");
        List<String> ruleNames = List.of("ruleA", "ruleB", "ruleC");
        List<RestRuleModel> ruleModels = ruleNames.stream().map(ruleName ->
        {
            RestRuleModel ruleModel = new RestRuleModel();
            ruleModel.setName(ruleName);
            return ruleModel;
        }).collect(toList());

        RestRuleModelsCollection rules = restClient.authenticateUser(user).withCoreAPI().usingNode(folder).usingDefaultRuleSet()
                                                   .createListOfRules(ruleModels);

        restClient.assertStatusCodeIs(CREATED);

        assertEquals("Unexpected number of rules received in response.", ruleNames.size(), rules.getEntries().size());
        IntStream.range(0, ruleModels.size()).forEach(i ->
                rules.getEntries().get(i).onModel()
                    .assertThat().field("id").isNotNull()
                    .assertThat().field("name").is(ruleNames.get(i)));
    }
}

