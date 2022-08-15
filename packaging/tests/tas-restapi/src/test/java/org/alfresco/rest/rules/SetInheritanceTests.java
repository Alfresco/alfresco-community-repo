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

import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestRuleSettingsModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for GET and PUT /nodes/{nodeId}/rule-settings/{ruleSettingKey}.
 */
@Test (groups = { TestGroup.RULES })
public class SetInheritanceTests extends RestTest
{
    private UserModel user;
    private SiteModel site;

    @BeforeClass (alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user, site and folder.");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    /** Check we can get the -isInheritanceEnabled- rule setting for the folder. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getIsInherited()
    {
        STEP("Create a folder for the test.");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Get the -isInheritanceEnabled- rule settings for the folder.");
        RestRuleSettingsModel ruleSettingsModel = restClient.authenticateUser(user)
                                                            .withCoreAPI()
                                                            .usingResource(folder)
                                                            .usingIsInheritanceEnabledRuleSetting()
                                                            .retrieveSetting();

        restClient.assertStatusCodeIs(OK);
        RestRuleSettingsModel expected = new RestRuleSettingsModel();
        expected.setValue(true);
        ruleSettingsModel.assertThat().isEqualTo(expected);
    }

    /** Check we can change the -isInheritanceEnabled- rule setting for the folder. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateIsInherited()
    {
        STEP("Create a folder for the test.");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Set -isInheritanceEnabled- to false.");
        RestRuleSettingsModel updateBody = new RestRuleSettingsModel();
        updateBody.setValue(false);

        RestRuleSettingsModel ruleSettingsModel = restClient.authenticateUser(user)
                                              .withCoreAPI()
                                              .usingResource(folder)
                                              .usingIsInheritanceEnabledRuleSetting()
                                              .updateSetting(updateBody);

        restClient.assertStatusCodeIs(OK);
        RestRuleSettingsModel expected = new RestRuleSettingsModel();
        expected.setValue(false);
        ruleSettingsModel.assertThat().isEqualTo(expected);
    }

    /** Check we get an error when trying to set -isInheritanceEnabled- to something other than a boolean. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateInheritedWithBadValue()
    {
        STEP("Create a folder for the test.");
        FolderModel folder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Try to set -isInheritanceEnabled- to \"banana\".");
        RestRuleSettingsModel updateBody = new RestRuleSettingsModel();
        updateBody.setValue("banana");

        restClient.authenticateUser(user).withCoreAPI().usingResource(folder).usingIsInheritanceEnabledRuleSetting()
                  .updateSetting(updateBody);

        restClient.assertLastError().statusCodeIs(BAD_REQUEST)
                                    .containsSummary("Only boolean values are supported for this key");
    }
}
