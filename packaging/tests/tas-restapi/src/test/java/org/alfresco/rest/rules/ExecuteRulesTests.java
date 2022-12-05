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

import static org.alfresco.rest.rules.RulesTestsUtils.AUDIO_ASPECT;
import static org.alfresco.rest.rules.RulesTestsUtils.LOCKABLE_ASPECT;
import static org.alfresco.rest.rules.RulesTestsUtils.RULE_NAME_DEFAULT;
import static org.alfresco.utility.report.log.Step.STEP;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.dataprep.CMISUtil;
import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestActionBodyExecTemplateModel;
import org.alfresco.rest.model.RestNodeModel;
import org.alfresco.rest.model.RestRuleExecutionModel;
import org.alfresco.rest.model.RestRuleModel;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for POST /nodes/{nodeId}/rule-executions.
 */
@Test(groups = { TestGroup.RULES})
public class ExecuteRulesTests extends RestTest
{

    private UserModel user;
    private SiteModel site;
    private FolderModel parentFolder;
    private FolderModel childFolder;
    private FileModel parentFolderFile;
    private FileModel childFolderFile;
    private RestRuleModel parentFolderRule;
    private RestRuleModel childFolderRule;

    @BeforeClass(alwaysRun = true)
    public void dataPreparation()
    {
        STEP("Create user and a site");
        user = dataUser.createRandomTestUser();
        site = dataSite.usingUser(user).createPublicRandomSite();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp()
    {
        STEP("Create parent folder, rule folder and file in it");
        parentFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        childFolder = dataContent.usingUser(user).usingResource(parentFolder).createFolder();
        parentFolderFile = dataContent.usingUser(user).usingResource(parentFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        childFolderFile = dataContent.usingUser(user).usingResource(childFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);

        STEP("Create rules for parent and rule folders");
        RestActionBodyExecTemplateModel addLockableAspectAction = rulesUtils.createAddAspectAction(LOCKABLE_ASPECT);
        RestRuleModel ruleModel = rulesUtils.createRuleModel(RULE_NAME_DEFAULT, List.of(addLockableAspectAction));
        ruleModel.setIsInheritable(true);
        parentFolderRule = restClient.authenticateUser(user).withPrivateAPI().usingNode(parentFolder).usingDefaultRuleSet().createSingleRule(ruleModel);
        childFolderRule = restClient.authenticateUser(user).withPrivateAPI().usingNode(childFolder).usingDefaultRuleSet().createSingleRule(rulesUtils.createRuleModelWithDefaultValues());
    }

    /**
     * Execute one rule with one action trying to add audio aspect to a file.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.ACTIONS, TestGroup.SANITY })
    public void executeRules_onlyOwnedRules()
    {
        STEP("Check if file aspects don't contain Audio one");
        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).notContainsAspects(AUDIO_ASPECT);

        STEP("Execute rule");
        RestRuleExecutionModel executionResult = restClient.authenticateUser(user).withPrivateAPI().usingNode(childFolder).executeRules(rulesUtils.createRuleExecutionRequest());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        executionResult.assertThat().field("isEachSubFolderIncluded").is(false);

        STEP("Check if only Audio aspect was added");
        fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).containsAspects(AUDIO_ASPECT);
    }

    /**
     * Execute owned rule adding Audio aspect and inherited rule adding Lockable aspect.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.ACTIONS, TestGroup.SANITY })
    public void executeRules_includeInheritedRules()
    {
        STEP("Check if file aspects don't contain Audio and Lockable ones");
        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).notContainsAspects(AUDIO_ASPECT, LOCKABLE_ASPECT);

        STEP("Execute rules including inherited rules");
        RestRuleExecutionModel ruleExecutionRequest = rulesUtils.createRuleExecutionRequest();
        RestRuleExecutionModel executionResult = restClient.authenticateUser(user).withPrivateAPI().usingNode(childFolder).executeRules(ruleExecutionRequest);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        executionResult.assertThat().field("isEachSubFolderIncluded").is(false);

        STEP("Check if Audio and Lockable aspects were added");
        fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).containsAspects(AUDIO_ASPECT, LOCKABLE_ASPECT);
    }

    /**
     * Execute rules on parent folder (add Lockable aspect) including sub-folder folders (add Audio aspect).
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.ACTIONS, TestGroup.SANITY })
    public void executeRules_includeSubFolderRules()
    {
        STEP("Check if parent folder's file aspects don't contain Audio and Lockable ones");
        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(parentFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).notContainsAspects(AUDIO_ASPECT, LOCKABLE_ASPECT);

        STEP("Check if child folder's file aspects don't contain Audio and Lockable ones");
        fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).notContainsAspects(AUDIO_ASPECT, LOCKABLE_ASPECT);

        STEP("Execute rules on parent folder including sub-folders");
        RestRuleExecutionModel ruleExecutionRequest = rulesUtils.createRuleExecutionRequest();
        ruleExecutionRequest.setIsEachSubFolderIncluded(true);
        RestRuleExecutionModel executionResult = restClient.authenticateUser(user).withPrivateAPI().usingNode(parentFolder).executeRules(ruleExecutionRequest);
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        executionResult.assertThat().field("isEachSubFolderIncluded").is(true);

        STEP("Check if Lockable aspects was added to parent folder's file");
        fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(parentFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode)
            .containsAspects(LOCKABLE_ASPECT)
            .notContainsAspects(AUDIO_ASPECT);

        STEP("Check if Audio and Lockable aspects were added to child folder's file");
        fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode)
            .containsAspects(AUDIO_ASPECT, LOCKABLE_ASPECT);
    }

    /**
     * Try to execute disabled rule and check if nothing changed.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.ACTIONS })
    public void executeRules_disabledRule()
    {
        STEP("Disable child rules");
        RestRuleModel updatedChildRule = rulesUtils.createRuleModelWithDefaultValues();
        updatedChildRule.setIsEnabled(false);
        restClient.authenticateUser(user).withPrivateAPI().usingNode(childFolder).usingDefaultRuleSet().updateRule(childFolderRule.getId(), updatedChildRule);

        STEP("Check if file aspects don't contain Audio one");
        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).notContainsAspects(AUDIO_ASPECT);

        STEP("Execute rule");
        RestRuleExecutionModel executionResult = restClient.authenticateUser(user).withPrivateAPI().usingNode(childFolder).executeRules(rulesUtils.createRuleExecutionRequest());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        executionResult.assertThat().field("isEachSubFolderIncluded").is(false);

        STEP("Check if Audio aspect is still missing");
        fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).notContainsAspects(AUDIO_ASPECT);
    }

    /**
     * Try to execute inherited parent folder's rule which is not inheritable.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.ACTIONS })
    public void executeRules_notInheritableRule()
    {
        STEP("Set parent rule as not inheritable");
        RestRuleModel updatedParentRule = rulesUtils.createRuleModelWithDefaultValues();
        updatedParentRule.setIsInheritable(false);
        restClient.authenticateUser(user).withPrivateAPI().usingNode(parentFolder).usingDefaultRuleSet().updateRule(parentFolderRule.getId(), updatedParentRule);

        STEP("Check if file aspects don't contain Audio and Lockable ones");
        RestNodeModel fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).notContainsAspects(AUDIO_ASPECT, LOCKABLE_ASPECT);

        STEP("Execute child folder rules including inherited rules");
        RestRuleExecutionModel executionResult = restClient.authenticateUser(user).withPrivateAPI().usingNode(childFolder).executeRules(rulesUtils.createRuleExecutionRequest());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);
        executionResult.assertThat().field("isEachSubFolderIncluded").is(false);

        STEP("Check if Audio aspect is present and Lockable is still missing");
        fileNode = restClient.authenticateUser(user).withCoreAPI().usingNode(childFolderFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode)
            .containsAspects(AUDIO_ASPECT)
            .notContainsAspects(LOCKABLE_ASPECT);
    }

    /**
     * Try to execute private folder's rules by user not added to site and receive 403.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.ACTIONS })
    public void executeRules_privateFolderResultsWith403()
    {
        STEP("Using admin create private site, folder and rule");
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingAdmin().usingSite(privateSite).createFolder();
        dataContent.usingAdmin().usingResource(privateFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(dataUser.getAdminUser()).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(rulesUtils.createRuleModelWithDefaultValues());

        STEP("Try to execute private folder's rules by user");
        restClient.authenticateUser(user).withPrivateAPI().usingNode(privateFolder).executeRules(rulesUtils.createRuleExecutionRequest());
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
    }

    /**
     * Try to execute private folder's rules as site contributor and receive 403.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.ACTIONS })
    public void executeRules_privateFolderAsContributorResultsWith403()
    {
        STEP("Using admin create private site, folder, file in it, rule and add user to site as contributor");
        UserModel contributor = dataUser.createRandomTestUser();
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingAdmin().usingSite(privateSite).createFolder();
        dataContent.usingAdmin().usingResource(privateFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(dataUser.getAdminUser()).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(rulesUtils.createRuleModelWithDefaultValues());
        dataUser.usingAdmin().addUserToSite(contributor, privateSite, UserRole.SiteContributor);

        STEP("Try to execute private folder's rules by contributor");
        restClient.authenticateUser(contributor).withPrivateAPI().usingNode(privateFolder).executeRules(rulesUtils.createRuleExecutionRequest());
        restClient.assertStatusCodeIs(HttpStatus.FORBIDDEN);
    }

    /**
     * Execute private folder's rules as site collaborator.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.ACTIONS })
    public void executeRules_privateFolderAsCollaborator()
    {
        STEP("Using admin create private site, folder, file in it, rule and add user to site as collaborator");
        UserModel collaborator = dataUser.createRandomTestUser();
        UserModel admin = dataUser.getAdminUser();
        SiteModel privateSite = dataSite.usingAdmin().createPrivateRandomSite();
        FolderModel privateFolder = dataContent.usingAdmin().usingSite(privateSite).createFolder();
        FileModel privateFile = dataContent.usingAdmin().usingResource(privateFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        restClient.authenticateUser(admin).withPrivateAPI().usingNode(privateFolder).usingDefaultRuleSet().createSingleRule(rulesUtils.createRuleModelWithDefaultValues());
        dataUser.usingAdmin().addUserToSite(collaborator, privateSite, UserRole.SiteCollaborator);

        STEP("Check if file aspects don't contain Audio one");
        RestNodeModel fileNode = restClient.authenticateUser(admin).withCoreAPI().usingNode(privateFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).notContainsAspects(AUDIO_ASPECT);

        STEP("Execute private folder's rules by collaborator");
        restClient.authenticateUser(collaborator).withPrivateAPI().usingNode(privateFolder).executeRules(rulesUtils.createRuleExecutionRequest());
        restClient.assertStatusCodeIs(HttpStatus.CREATED);

        STEP("Check if Audio aspect is present");
        fileNode = restClient.authenticateUser(admin).withCoreAPI().usingNode(privateFile).getNode();
        restClient.assertStatusCodeIs(HttpStatus.OK);
        rulesUtils.assertThat(fileNode).containsAspects(AUDIO_ASPECT);
    }

    /**
     * Try to execute rule with broken action and receive 404 error.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.RULES, TestGroup.ACTIONS })
    public void executeRules_copyActionWithDeletedDestinationFolder()
    {
        FolderModel owningFolder = dataContent.usingUser(user).usingSite(site).createFolder();
        FileModel owningFolderFile = dataContent.usingUser(user).usingResource(owningFolder).createContent(CMISUtil.DocumentType.TEXT_PLAIN);
        FolderModel destinationFolder = dataContent.usingUser(user).usingSite(site).createFolder();

        STEP("Create copy action and rule");
        final Map<String, Serializable> copyParams =
                Map.of("destination-folder", destinationFolder.getNodeRef(), "deep-copy", true);
        final RestActionBodyExecTemplateModel copyAction = rulesUtils.createCustomActionModel("copy", copyParams);
        final RestRuleModel ruleModel = rulesUtils.createRuleModelWithDefaultValues();
        ruleModel.setActions(Arrays.asList(copyAction));

        restClient.authenticateUser(user).withPrivateAPI().usingNode(owningFolder).usingDefaultRuleSet().createSingleRule(ruleModel);

        STEP("Delete destination folder and execute rule");
        restClient.authenticateUser(user).withCoreAPI().usingNode(destinationFolder).deleteNode(destinationFolder.getNodeRef());
        restClient.authenticateUser(user).withPrivateAPI().usingNode(owningFolder).executeRules(rulesUtils.createRuleExecutionRequest());
        restClient.assertStatusCodeIs(HttpStatus.NOT_FOUND);
    }
}
