/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

package org.alfresco.repo.rendition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.rendition.executer.ImageRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailHelper;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.alfresco.util.test.junitrules.TemporarySites.TestSiteAndMemberInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Neil McErlean
 * @since 3.3
 */
public class RenditionServicePermissionsTest
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RenditionServicePermissionsTest.class);
    
    // JUnit Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // JUnit Rules to create test users.
    public static AlfrescoPerson TEST_USER1 = new AlfrescoPerson(APP_CONTEXT_INIT, "UserOne");
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain staticRuleChain = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                            .around(TEST_USER1);
    
    // A JUnit Rule to manage test nodes use in each test method
    public TemporaryNodes testNodes = new TemporaryNodes(APP_CONTEXT_INIT);
    // A JUnit Rule to create a test site.
    public TemporarySites testSites = new TemporarySites(APP_CONTEXT_INIT);
    
    // A JUnit Rule to run tests as the given user.
    public RunAsFullyAuthenticatedRule runTestsAsUser = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());
    
    // Tie them together in a static Rule Chain
    @Rule public RuleChain nonStaticRuleChain = RuleChain.outerRule(runTestsAsUser)
                                                            .around(testNodes)
                                                            .around(testSites);
    
    private final static QName RESCALE_RENDER_DEFN_NAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
            ImageRenderingEngine.NAME + System.currentTimeMillis());


    private static ContentService            contentService;
    private static NodeService               nodeService;
    private static PermissionService         permissionService;
    private static RenditionService          renditionService;
    private static Repository                repositoryHelper;
    private static RetryingTransactionHelper transactionHelper;
    private static ServiceRegistry           services;
    private static ThumbnailRegistry         thumbnailRegistry;
    
    private NodeRef companyHome;
    private String  testFolderName;
    private NodeRef testFolder;
    
    private NodeRef nodeWithImageContent;
    private TestSiteAndMemberInfo testSiteInfo;
    private NodeRef brokenJpg;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        contentService    = (ContentService)            APP_CONTEXT_INIT.getApplicationContext().getBean("ContentService");
        nodeService       = (NodeService)               APP_CONTEXT_INIT.getApplicationContext().getBean("NodeService");
        permissionService = (PermissionService)         APP_CONTEXT_INIT.getApplicationContext().getBean("PermissionService");
        renditionService  = (RenditionService)          APP_CONTEXT_INIT.getApplicationContext().getBean("renditionService");
        repositoryHelper  = (Repository)                APP_CONTEXT_INIT.getApplicationContext().getBean("repositoryHelper");
        transactionHelper = (RetryingTransactionHelper) APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper");
        services          = (ServiceRegistry)           APP_CONTEXT_INIT.getApplicationContext().getBean("ServiceRegistry");
        thumbnailRegistry = (ThumbnailRegistry)         APP_CONTEXT_INIT.getApplicationContext().getBean("thumbnailRegistry");
    }
    
    @Before public void initNonStaticData() throws Exception
    {
        companyHome = repositoryHelper.getCompanyHome();
        
        // Create the test folder used for these tests
        testFolderName = "Test-folder-"+ System.currentTimeMillis();
        testFolder     = testNodes.createFolder(companyHome, testFolderName, AuthenticationUtil.getAdminUserName());
        
        // Create the node used as a content supplier for one test
        String testImageNodeName = "testImageNode" + System.currentTimeMillis();
        nodeWithImageContent     = testNodes.createQuickFile(MimetypeMap.MIMETYPE_IMAGE_PNG, companyHome, testImageNodeName, AuthenticationUtil.getAdminUserName());
        
        // Create a test site - note that 'admin' is the site creator.
        testSiteInfo = testSites.createTestSiteWithUserPerRole(this.getClass().getSimpleName(),
                                                               "sitePreset",
                                                               SiteVisibility.PRIVATE,
                                                               AuthenticationUtil.getAdminUserName());
        final NodeRef siteDocLib = testSiteInfo.doclib;
        // Put a piece of content in that site - again the creator is admin.
        // This piece of content is malformed and it will not be possible to create thumbnails from it.
        brokenJpg                = testNodes.createQuickFileByName("quickCorrupt.jpg", siteDocLib, AuthenticationUtil.getAdminUserName());
    }
    
    /**
     * This test method uses the RenditionService to render a test document and place the
     * rendition under a folder for which the user does not have write permissions.
     * This should be allowed as all renditions are performed as system.
     */
    @Test public void testRenditionAccessPermissions() throws Exception
    {
        final String normalUser = TEST_USER1.getUsername();

        // As admin, create a user who has read-only access to the testFolder
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Restrict write access to the test folder
                permissionService.setPermission(testFolder, normalUser, PermissionService.CONSUMER, true);
                
                return null;
            }
        });

        // As the user, render a piece of content with the rendition going to testFolder
        final NodeRef rendition = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Set the current security context as a rendition user
                AuthenticationUtil.setFullyAuthenticatedUser(normalUser);
                
                assertFalse("Source node has unexpected renditioned aspect.", nodeService.hasAspect(nodeWithImageContent,
                            RenditionModel.ASPECT_RENDITIONED));

                String path = testFolderName+"/testRendition.png";
                // Create the rendering action.
                RenditionDefinition action = makeRescaleImageAction();
                action.setParameterValue(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE, path);

                // Perform the action with an explicit destination folder
                logger.debug("Creating rendition of: " + nodeWithImageContent);
                ChildAssociationRef renditionAssoc = renditionService.render(nodeWithImageContent, action);
                logger.debug("Created rendition: " + renditionAssoc.getChildRef());
                
                NodeRef renditionNode = renditionAssoc.getChildRef();
                testNodes.addNodeRef(renditionNode);
                
                assertEquals("The parent node was not correct", nodeWithImageContent, renditionAssoc.getParentRef());
                logger.debug("rendition's primary parent: " + nodeService.getPrimaryParent(renditionNode));
                assertEquals("The parent node was not correct", testFolder, nodeService.getPrimaryParent(renditionNode).getParentRef());

                // Now the source content node should have the rn:renditioned aspect
                assertTrue("Source node is missing renditioned aspect.", nodeService.hasAspect(nodeWithImageContent,
                            RenditionModel.ASPECT_RENDITIONED));
                
                return renditionNode;
            }
        });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Set the current security context as admin
                        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

                        final Serializable renditionCreator = nodeService.getProperty(rendition, ContentModel.PROP_CREATOR);
                        assertEquals("Incorrect creator", normalUser, renditionCreator);

                        return null;
                    }
                });
        }

    /** This test case relates to ALF-17797. */
    @Test public void userWithReadOnlyAccessToNodeShouldNotCauseFailedThumbnailProblems() throws Exception
    {
        final String siteConsumer = testSiteInfo.siteConsumer;
        
        // Let's trigger the creation of a doclib thumbnail for the broken JPG node.
        // We know this cannot succeed. We also know the user triggering it does not have write permissions for the node.
        AuthenticationUtil.setFullyAuthenticatedUser(siteConsumer);
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // This is what ScriptNode.createThumbnail does
                ThumbnailDefinition details = thumbnailRegistry.getThumbnailDefinition("doclib");
                Action action = ThumbnailHelper.createCreateThumbnailAction(details, services);
                
                // Queue async creation of thumbnail
                services.getActionService().executeAction(action, brokenJpg, true, true);
                return null;
            }
        });
        
        // FIXME Yuck. Sleeping to wait for the completion of the above async action.
        Thread.sleep(2000);
        
        // The node in question should have no thumbnail/rendition. But it should be marked as having had a failed rendition.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertTrue("Expected an empty list of renditions for brokenJpg.",
                           renditionService.getRenditions(brokenJpg).isEmpty());
                
                assertTrue("Expected brokenJpg to have FailedThumbnailSource aspect.",
                           nodeService.hasAspect(brokenJpg, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE));
                
                List<ChildAssociationRef> failedThumbnailChildren = nodeService.getChildAssocs(brokenJpg, ContentModel.ASSOC_FAILED_THUMBNAIL, RegexQNamePattern.MATCH_ALL);
                assertEquals("Wrong number of failed thumbnail child nodes.", 1, failedThumbnailChildren.size());
                
                NodeRef failedThumbnailChildNode = failedThumbnailChildren.get(0).getChildRef();
                
                assertEquals(1, nodeService.getProperty(failedThumbnailChildNode, ContentModel.PROP_FAILURE_COUNT));
                
                return null;
            }
        });
    }
    
    /**
     * This test case relates to ALF-17886.
     * 
     * The bug is as follows.
     * <ol>
     * <li>A user creates a node and triggers rendition (thumbnail) creation.</li>
     * <li>It all succeeds.</li>
     * <li>Another user (Role SiteColaborator: Only has update, not delete privileges) updates the content with something that cannot be thumbnailed e.g. corrupt document.</li>
     * <li>There should be no failures in executing the DeleteRenditionActionExecuter.</li>
     * </ol>
     */
    @Test public void userWithoutDeleteAccessToNodeShouldNotCauseFailedThumbnailProblemsOnUpdate() throws Exception
    {
        final String siteManager      = testSiteInfo.siteManager;
        final String siteCollaborator = testSiteInfo.siteCollaborator;
        
        // Let's trigger the creation of a doclib thumbnail for a JPG node.
        AuthenticationUtil.setFullyAuthenticatedUser(siteManager);
        
        final NodeRef imgNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                NodeRef imgNode = testNodes.createQuickFile(MimetypeMap.MIMETYPE_IMAGE_JPEG,
                                                            testSiteInfo.doclib,
                                                            "quick.jpg",
                                                            AuthenticationUtil.getFullyAuthenticatedUser());
                
                // This is what ScriptNode.createThumbnail does
                ThumbnailDefinition details = thumbnailRegistry.getThumbnailDefinition("doclib");
                Action action = ThumbnailHelper.createCreateThumbnailAction(details, services);
                
                // Creation of thumbnail
                services.getActionService().executeAction(action, imgNode, true, false);
                
                // The node in question should now have a thumbnail/rendition.
                assertEquals(1, renditionService.getRenditions(imgNode).size());
                
                return imgNode;
            }
        });
        
        
        // Now switch to another user. This user can add/update but cannot delete any content.
        AuthenticationUtil.setFullyAuthenticatedUser(siteCollaborator);
        
        // And we'll update the image node with some broken content.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                final ContentWriter writer = contentService.getWriter(imgNode, ContentModel.PROP_CONTENT, true);
                final ContentReader reader = contentService.getReader(brokenJpg, ContentModel.PROP_CONTENT);
                
                writer.putContent(reader.getContentInputStream());
                
                // Simply updating the content like this should trigger rendition updates, which in this case will fail.
                return null;
            }
        });
        
        // FIXME Yuck. Sleeping to wait for the completion of the above async action.
        Thread.sleep(2000);
        
        // Now to check that the node has no renditions as the previous one should have been deleted.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertTrue(renditionService.getRenditions(imgNode).isEmpty());
                return null;
            }
        });
    }
    
    /**
     * Creates a RenditionDefinition for the RescaleImageActionExecutor.
     * 
     * @return A new RenderingAction.
     */
    private RenditionDefinition makeRescaleImageAction()
    {
        RenditionDefinition result = renditionService.createRenditionDefinition(RESCALE_RENDER_DEFN_NAME,
                    ImageRenderingEngine.NAME);
        result.setParameterValue(ImageRenderingEngine.PARAM_RESIZE_WIDTH, 42);
        return result;
    }
}
