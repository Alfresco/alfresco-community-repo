/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.springframework.context.ApplicationContext;


/**
 * Integration tests for Alfresco Rules. This class does not test the internals of the {@link RuleService}
 * but rather sets up and runs common or previously problematic rules use cases and ensures they are correct.
 * 
 * @author Neil Mc Erlean
 * @since 4.1.3
 */
public class MiscellaneousRulesTest
{
    private static final Log log = LogFactory.getLog(MiscellaneousRulesTest.class);
    
    // Static JUnit Rules
    public static ApplicationContextInit APP_CTXT_INIT = new ApplicationContextInit();
    public static AlfrescoPerson         TEST_USER     = new AlfrescoPerson(APP_CTXT_INIT);
    
    // Rule chain to ensure they run in the right order
    @ClassRule public static RuleChain STATIC_RULE_CHAIN = RuleChain.outerRule(APP_CTXT_INIT)
                                                                       .around(TEST_USER);
    
    // Non-static JUnit Rules
    public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(TEST_USER);
    public TemporarySites              testSites = new TemporarySites(APP_CTXT_INIT);
    public TemporaryNodes              testNodes = new TemporaryNodes(APP_CTXT_INIT);
    
    // Rule chain to ensure they run in the right order
    @Rule public RuleChain ruleChain = RuleChain.outerRule(runAsRule)
                                                   .around(testSites)
                                                   .around(testNodes);
    
    private static ActionService             ACTION_SERVICE;
    private static CopyService               COPY_SERVICE;
    private static NodeService               NODE_SERVICE;
    private static RetryingTransactionHelper TRANSACTION_HELPER;
    private static RuleService               RULE_SERVICE;
    private static SiteService               SITE_SERVICE;
    
    private SiteInfo testSite;
    
    @BeforeClass public static void initSpringBeans() throws Exception
    {
        final ApplicationContext appCtxt = APP_CTXT_INIT.getApplicationContext();
        
        ACTION_SERVICE     = appCtxt.getBean("ActionService", ActionService.class);
        COPY_SERVICE       = appCtxt.getBean("CopyService", CopyService.class);
        NODE_SERVICE       = appCtxt.getBean("NodeService", NodeService.class);
        TRANSACTION_HELPER = appCtxt.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        RULE_SERVICE       = appCtxt.getBean("RuleService", RuleService.class);
        SITE_SERVICE       = appCtxt.getBean("SiteService", SiteService.class);
    }
    
    @Before public void createTestData() throws Exception
    {
        testSite = testSites.createSite("sitePreset", "testSiteName", "testSiteTitle", "test site description",
                             SiteVisibility.PUBLIC, TEST_USER.getUsername());
        
    }
    
    @Test public void alf14568() throws Exception
    {
        final NodeRef testSiteDocLib = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return SITE_SERVICE.getContainer(testSite.getShortName(), SiteService.DOCUMENT_LIBRARY);
            }
        });
        assertNotNull("Null doclib", testSiteDocLib);
        
        // Create four folders - the first (zero'th) will not be used.
        final NodeRef[] folders = new NodeRef[4];
        for (int i : new int[] {0, 1, 2, 3})
        {
            folders[i] = testNodes.createFolder(testSiteDocLib, "folder" + i, TEST_USER.getUsername());
        }
        
        // Create an inbound rule for Folder1 - copy to Folder2.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Clashes with the JUnit annotation @Rule
                org.alfresco.service.cmr.rule.Rule rule = new org.alfresco.service.cmr.rule.Rule();
                rule.setRuleType(RuleType.INBOUND);
                rule.applyToChildren(false);
                rule.setRuleDisabled(false);
                rule.setTitle("Copy to folder2");
                rule.setExecuteAsynchronously(false);
                
                Map<String, Serializable> params = new HashMap<String, Serializable>();
                params.put(CopyActionExecuter.PARAM_DESTINATION_FOLDER, folders[2]);
                Action copyAction = ACTION_SERVICE.createAction("copy", params);
                rule.setAction(copyAction);
                
                RULE_SERVICE.saveRule(folders[1], rule);
                
                return null;
            }
        });
        
        // Create an update rule for Folder2 - copy to Folder3.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                org.alfresco.service.cmr.rule.Rule rule = new org.alfresco.service.cmr.rule.Rule();
                rule.setRuleType(RuleType.UPDATE);
                rule.applyToChildren(false);
                rule.setRuleDisabled(false);
                rule.setTitle("Copy to folder3");
                rule.setExecuteAsynchronously(false);
                
                Map<String, Serializable> params = new HashMap<String, Serializable>();
                params.put(CopyActionExecuter.PARAM_DESTINATION_FOLDER, folders[3]);
                Action copyAction = ACTION_SERVICE.createAction("copy", params);
                rule.setAction(copyAction);
                
                RULE_SERVICE.saveRule(folders[2], rule);
                
                return null;
            }
        });
        
        // Now put a file in Folder1. - don't need transaction as one is included within this call
        testNodes.createQuickFile(MimetypeMap.MIMETYPE_TEXT_PLAIN, folders[1], "quick.txt", TEST_USER.getUsername());
        
        // Which folders is the file in?
        final Set<NodeRef> foldersContainingFile = new HashSet<NodeRef>();
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                for (NodeRef folder : folders)
                {
                    NodeRef child = NODE_SERVICE.getChildByName(folder, ContentModel.ASSOC_CONTAINS, "quick.txt");
                    if (child != null)
                    {
                        foldersContainingFile.add(folder);
                    }
                }
                
                return null;
            }
        });
        
        // Now disable all the rules - I don't think this should be necessary, but if we don't do this, the teardown
        // parts of the JUnit Rules cause problems in the repo.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                for (NodeRef folder : folders)
                {
                    RULE_SERVICE.removeAllRules(folder);
                }
                
                return null;
            }
        });
        
        final Set<NodeRef> expectedFolders = new HashSet<NodeRef>();
        expectedFolders.add(folders[1]);
        expectedFolders.add(folders[2]);
        
        assertEquals(expectedFolders, foldersContainingFile);
    }
    
    /**
     * ALF-14568 doesn't explicitly say so, but there is a related problem on top of the
     * 'creating cm:original assoc triggers rule' bug. It is the related 'deleting cm:original assoc triggers rule' bug.
     * This test case validates the fix for that issue.
     */
    @Test public void alf14568_supplementary() throws Exception
    {
        final NodeRef testSiteDocLib = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return SITE_SERVICE.getContainer(testSite.getShortName(), SiteService.DOCUMENT_LIBRARY);
            }
        });
        assertNotNull("Null doclib", testSiteDocLib);
        
        // Create a folder to put our Alfresco Rules on - but don't put any rules on it yet.
        final NodeRef ruleFolder = testNodes.createFolder(testSiteDocLib, "ruleFolder", TEST_USER.getUsername());
        
        // Create a piece of content outside our Rules folder.
        final NodeRef originalContent = testNodes.createQuickFile(MimetypeMap.MIMETYPE_TEXT_PLAIN,
                                                                  testSiteDocLib,
                                                                  "original.txt",
                                                                  TEST_USER.getUsername());
        
        // Now copy that node into the Ruled folder, which will create a cm:original assoc.
        final NodeRef copyNode = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return COPY_SERVICE.copy(originalContent, ruleFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
            }
        });
        
        final QName exifAspectQName = QName.createQName("{http://www.alfresco.org/model/exif/1.0}exif");
        
        // Only now do we create the update rule on our folder - put a marker aspect on the node.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Clashes with the JUnit annotation @Rule
                org.alfresco.service.cmr.rule.Rule rule = new org.alfresco.service.cmr.rule.Rule();
                rule.setRuleType(RuleType.UPDATE);
                rule.applyToChildren(false);
                rule.setRuleDisabled(false);
                rule.setTitle("Put EXIF aspect on changed nodes");
                rule.setExecuteAsynchronously(false);
                
                Map<String, Serializable> params = new HashMap<String, Serializable>();
                params.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, exifAspectQName);
                Action addAspectAction = ACTION_SERVICE.createAction("add-features", params);
                rule.setAction(addAspectAction);
                
                RULE_SERVICE.saveRule(ruleFolder, rule);
                
                return null;
            }
        });
        
        // Now delete the original node.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                NODE_SERVICE.deleteNode(originalContent);
                
                return null;
            }
        });
        
        // The removal of the cm:original association should NOT have triggered the rule.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertFalse("Rule executed when it shouldn't have.", NODE_SERVICE.hasAspect(copyNode, exifAspectQName));
                
                return null;
            }
        });
    }
    
    @Test public void alf13192_rulesFromFirstFolderMoveToSecondWhenDeleteFirstFolder() throws Exception
    {
        final NodeRef testSiteDocLib = TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return SITE_SERVICE.getContainer(testSite.getShortName(), SiteService.DOCUMENT_LIBRARY);
            }
        });
        assertNotNull("Null doclib", testSiteDocLib);
        
        final NodeRef folder1 = testNodes.createFolder(testSiteDocLib, "folder 1", TEST_USER.getUsername());
        final NodeRef folder2 = testNodes.createFolder(testSiteDocLib, "folder 2", TEST_USER.getUsername());
        
        // Put a rule on folder1.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Clashes with the JUnit annotation @Rule
                org.alfresco.service.cmr.rule.Rule rule = new org.alfresco.service.cmr.rule.Rule();
                rule.setRuleType(RuleType.OUTBOUND);
                rule.applyToChildren(false);
                rule.setRuleDisabled(false);
                rule.setTitle("Copy to folder2");
                rule.setExecuteAsynchronously(false);
                
                Map<String, Serializable> params = new HashMap<String, Serializable>();
                params.put(CopyActionExecuter.PARAM_DESTINATION_FOLDER, folder2);
                Action copyAction = ACTION_SERVICE.createAction("copy", params);
                rule.setAction(copyAction);
                
                RULE_SERVICE.saveRule(folder1, rule);
                
                // While we're here, let's log some information about the rules.
                List<ChildAssociationRef> ruleFolders = NODE_SERVICE.getChildAssocs(folder1, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER);
                assertEquals(1, ruleFolders.size());
                log.debug("Rule SystemFolder noderef is " + ruleFolders.get(0).getChildRef());
                
                return null;
            }
        });
        
        // Now delete folder1.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                log.debug("About to delete the ruled folder: " + folder1);
                
                NODE_SERVICE.deleteNode(folder1);
                
                return null;
            }
        });
        
        
        // folder2 should have no rules-related elements
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertFalse(RULE_SERVICE.hasRules(folder2));
                assertFalse(NODE_SERVICE.hasAspect(folder2, RuleModel.ASPECT_RULES));
                
                return null;
            }
        });
    }
}
