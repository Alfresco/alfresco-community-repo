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

import static org.alfresco.rest.requests.RuleSettings.IS_INHERITANCE_ENABLED;
import static org.alfresco.utility.constants.UserRole.SiteCollaborator;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

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
public class SetInheritanceTests extends RulesRestTest
{
    private UserModel siteOwner;
    private SiteModel site;

    @BeforeClass (alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create a user, site and folder.");
        siteOwner = dataUser.createRandomTestUser();
        site = dataSite.usingUser(siteOwner).createPrivateRandomSite();
    }

    /** Check we can get the -isInheritanceEnabled- rule setting for the folder. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getIsInherited()
    {
        STEP("Create a folder for the test.");
        FolderModel folder = dataContent.usingUser(siteOwner).usingSite(site).createFolder();

        STEP("Get the -isInheritanceEnabled- rule settings for the folder.");
        RestRuleSettingsModel ruleSettingsModel = restClient.authenticateUser(siteOwner)
                                                            .withPrivateAPI()
                                                            .usingNode(folder)
                                                            .usingIsInheritanceEnabledRuleSetting()
                                                            .retrieveSetting();

        restClient.assertStatusCodeIs(OK);
        RestRuleSettingsModel expected = new RestRuleSettingsModel();
        expected.setKey(IS_INHERITANCE_ENABLED);
        expected.setValue(true);
        ruleSettingsModel.assertThat().isEqualTo(expected);
    }

    /** Check we get an error when trying to get settings from a non-existent folder. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getIsInheritedFromNonExistentFolder()
    {
        STEP("Try to get the -isInheritanceEnabled- rule settings for a fake folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");
        restClient.authenticateUser(siteOwner)
                  .withPrivateAPI()
                  .usingNode(nonExistentFolder)
                  .usingIsInheritanceEnabledRuleSetting()
                  .retrieveSetting();

        restClient.assertLastError().statusCodeIs(NOT_FOUND)
                  .containsSummary("Folder with id fake-id was not found");
    }

    /** Check we get an error when trying to retrieve a non-existent setting. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getNonExistentSetting()
    {
        STEP("Create a folder for the test.");
        FolderModel folder = dataContent.usingUser(siteOwner).usingSite(site).createFolder();

        STEP("Try to get a fake setting from the folder.");
        restClient.authenticateUser(siteOwner).withPrivateAPI().usingNode(folder).usingRuleSetting("-fakeRuleSetting-")
                  .retrieveSetting();

        restClient.assertLastError().statusCodeIs(NOT_FOUND)
                  .containsSummary("Unrecognised rule setting key -fakeRuleSetting-");
    }

    /** Check a user without permission for the folder cannot get the -isInheritanceEnabled- rule setting. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void getIsInheritedWithoutPermission()
    {
        STEP("Create a folder and a user without permission to access it.");
        FolderModel folder = dataContent.usingUser(siteOwner).usingSite(site).createFolder();
        UserModel noPermissionUser = dataUser.createRandomTestUser();

        STEP("Try to get the -isInheritanceEnabled- setting without permission.");
        restClient.authenticateUser(noPermissionUser)
                  .withPrivateAPI()
                  .usingNode(folder)
                  .usingIsInheritanceEnabledRuleSetting()
                  .retrieveSetting();

        restClient.assertLastError().statusCodeIs(FORBIDDEN)
                  .containsSummary("Cannot read from this node");
    }

    /** Check we can change the -isInheritanceEnabled- rule setting for the folder. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateIsInherited()
    {
        STEP("Create a folder for the test.");
        FolderModel folder = dataContent.usingUser(siteOwner).usingSite(site).createFolder();

        STEP("Set -isInheritanceEnabled- to false.");
        RestRuleSettingsModel updateBody = new RestRuleSettingsModel();
        updateBody.setValue(false);

        RestRuleSettingsModel ruleSettingsModel = restClient.authenticateUser(siteOwner)
                                              .withPrivateAPI()
                                              .usingNode(folder)
                                              .usingIsInheritanceEnabledRuleSetting()
                                              .updateSetting(updateBody);

        restClient.assertStatusCodeIs(OK);
        RestRuleSettingsModel expected = new RestRuleSettingsModel();
        expected.setKey(IS_INHERITANCE_ENABLED);
        expected.setValue(false);
        ruleSettingsModel.assertThat().isEqualTo(expected);
    }

    /** Check we get an error when trying to set -isInheritanceEnabled- to something other than a boolean. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateInheritedWithBadValue()
    {
        STEP("Create a folder for the test.");
        FolderModel folder = dataContent.usingUser(siteOwner).usingSite(site).createFolder();

        STEP("Try to set -isInheritanceEnabled- to \"banana\".");
        RestRuleSettingsModel updateBody = new RestRuleSettingsModel();
        updateBody.setValue("banana");

        restClient.authenticateUser(siteOwner).withPrivateAPI().usingNode(folder).usingIsInheritanceEnabledRuleSetting()
                  .updateSetting(updateBody);

        restClient.assertLastError().statusCodeIs(BAD_REQUEST)
                  .containsSummary("Rule setting " + IS_INHERITANCE_ENABLED + " requires a boolean value.");
    }

    /** Check we get an error when the folder is not found. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateInheritedWithNonExistentFolder()
    {
        STEP("Try to set -isInheritanceEnabled- against a fake folder.");
        FolderModel nonExistentFolder = FolderModel.getRandomFolderModel();
        nonExistentFolder.setNodeRef("fake-id");

        RestRuleSettingsModel updateBody = new RestRuleSettingsModel();
        updateBody.setValue(true);

        restClient.authenticateUser(siteOwner).withPrivateAPI().usingNode(nonExistentFolder).usingIsInheritanceEnabledRuleSetting()
                  .updateSetting(updateBody);

        restClient.assertLastError().statusCodeIs(NOT_FOUND)
                  .containsSummary("Folder with id fake-id was not found");
    }

    /** Check we get an error when trying to set a non-existent setting. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateNonExistentSetting()
    {
        STEP("Create a folder for the test.");
        FolderModel folder = dataContent.usingUser(siteOwner).usingSite(site).createFolder();

        STEP("Try to set a fake setting on the folder.");
        RestRuleSettingsModel updateBody = new RestRuleSettingsModel();
        updateBody.setValue(true);

        restClient.authenticateUser(siteOwner).withPrivateAPI().usingNode(folder).usingRuleSetting("-fakeRuleSetting-")
                  .updateSetting(updateBody);

        restClient.assertLastError().statusCodeIs(NOT_FOUND)
                  .containsSummary("Unrecognised rule setting key -fakeRuleSetting-");
    }

    /** Check a user without manage permission cannot update the -isInheritanceEnabled- rule setting. */
    @Test (groups = { TestGroup.REST_API, TestGroup.RULES })
    public void updateIsInheritedWithoutPermission()
    {
        STEP("Create a folder and a collaborator.");
        FolderModel folder = dataContent.usingUser(siteOwner).usingSite(site).createFolder();
        UserModel collaborator = dataUser.createRandomTestUser();
        collaborator.setUserRole(SiteCollaborator);
        restClient.authenticateUser(siteOwner).withCoreAPI().usingSite(site).addPerson(collaborator);

        STEP("Try to update the -isInheritanceEnabled- setting without permission.");
        RestRuleSettingsModel updateBody = new RestRuleSettingsModel();
        updateBody.setValue(true);

        restClient.authenticateUser(collaborator).withPrivateAPI().usingNode(folder).usingIsInheritanceEnabledRuleSetting()
                  .updateSetting(updateBody);

        restClient.assertLastError().statusCodeIs(FORBIDDEN)
                  .containsSummary("Insufficient permissions to manage rules");
    }
}
