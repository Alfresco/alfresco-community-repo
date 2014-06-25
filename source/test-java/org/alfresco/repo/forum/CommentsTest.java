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

package org.alfresco.repo.forum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.security.permissions.impl.PermissionServiceImpl;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * Test class for some {@link ForumModel forum model}-related functionality, specifically comments.
 * There is no (fully-featured) "CommentService" or "DiscussionService" and the REST API simply creates the appropriate
 * content structure as required by the forum model.
 * 
 * @author Neil McErlean
 * @since 4.0
 */
public class CommentsTest
{
    private static final ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    public static final String USER_ONE_NAME = "userone";
    public static final AlfrescoPerson TEST_USER1 = new AlfrescoPerson(APP_CONTEXT_INIT, USER_ONE_NAME);
    
    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain STATIC_RULE_CHAIN = RuleChain.outerRule(APP_CONTEXT_INIT)
                                                                    .around(TEST_USER1);
    
    // A JUnit Rule to run all tests as user1
    public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(TEST_USER1);
    
    // A JUnit Rule to manage test nodes use in each test method
    public TemporaryNodes testNodes = new TemporaryNodes(APP_CONTEXT_INIT);
    public TemporarySites testSites = new TemporarySites(APP_CONTEXT_INIT);
    
    // Tie them together in a non-static rule chain.
    @Rule public RuleChain ruleChain = RuleChain.outerRule(runAsRule)
                                                .around(testSites)
                                                .around(testNodes);
    
    // Services
    private static BehaviourFilter behaviourFilter;
    private static ContentService contentService;
    private static NodeService nodeService;
    private static Repository repositoryHelper;
    private static SiteService siteService;
    private static RetryingTransactionHelper transactionHelper;
    private static AuthenticationComponent authenticationComponent;
    private static CommentService commentService;
    private static MutableAuthenticationService authenticationService;
    private static PersonService personService;
    private static ActivityPostDAO postDAO;
    private static PermissionServiceImpl permissionServiceImpl;
    private static ModelDAO permissionModelDAO;

    // These NodeRefs are used by the test methods.
    private static NodeRef COMPANY_HOME;
    private SiteInfo testSite;
    private NodeRef testFolder;
    private List<NodeRef> testDocs;
    
    @BeforeClass public static void initBasicServices() throws Exception
    {
        behaviourFilter = (BehaviourFilter)APP_CONTEXT_INIT.getApplicationContext().getBean("policyBehaviourFilter");
        contentService = (ContentService)APP_CONTEXT_INIT.getApplicationContext().getBean("ContentService");
        nodeService = (NodeService)APP_CONTEXT_INIT.getApplicationContext().getBean("NodeService");
        repositoryHelper = (Repository)APP_CONTEXT_INIT.getApplicationContext().getBean("repositoryHelper");
        siteService = (SiteService)APP_CONTEXT_INIT.getApplicationContext().getBean("SiteService");
        transactionHelper = (RetryingTransactionHelper)APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper");
        
        authenticationComponent = (AuthenticationComponent)APP_CONTEXT_INIT.getApplicationContext().getBean("authenticationComponent");
        commentService = (CommentService)APP_CONTEXT_INIT.getApplicationContext().getBean("commentService");
        authenticationService = (MutableAuthenticationService)APP_CONTEXT_INIT.getApplicationContext().getBean("AuthenticationService");
        personService = (PersonService)APP_CONTEXT_INIT.getApplicationContext().getBean("PersonService");
        postDAO = (ActivityPostDAO)APP_CONTEXT_INIT.getApplicationContext().getBean("postDAO");
        permissionServiceImpl = (PermissionServiceImpl)APP_CONTEXT_INIT.getApplicationContext().getBean("permissionServiceImpl");
        permissionModelDAO = (ModelDAO)APP_CONTEXT_INIT.getApplicationContext().getBean("permissionsModelDAO");
        
        COMPANY_HOME = repositoryHelper.getCompanyHome();
    }
    
    @Before public void createSomeContentForCommentingOn() throws Exception
    {
        // Create some content which we will comment on.
        testSite = testSites.createSite("sitePreset", "testSite", "test site title", "test site description", SiteVisibility.PUBLIC, USER_ONE_NAME);
        final NodeRef doclib = siteService.getContainer(testSite.getShortName(), SiteService.DOCUMENT_LIBRARY);
        testFolder = testNodes.createFolder(doclib, "testFolder", TEST_USER1.getUsername());
        testDocs = new ArrayList<NodeRef>(3);
        for (int i = 0; i < 3; i++)
        {
            NodeRef testNode = testNodes.createQuickFile(MimetypeMap.MIMETYPE_TEXT_PLAIN, COMPANY_HOME, "testDocInFolder" + i, TEST_USER1.getUsername());
            testDocs.add(testNode);
        }
    }
    
    // MNT-11667 "createComment" method creates activity for users who are not supposed to see the file
    @Test public void testMNT11667() throws Exception
    {
        final String userTwo = "usertwo";

        try
        {
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

                    createUser(userTwo);

                    assertTrue(siteService.hasSite(testSite.getShortName()));

                    authenticationComponent.setCurrentUser(USER_ONE_NAME);

                    // invite user to a site with 'Collaborator' role
                    siteService.setMembership(testSite.getShortName(), userTwo, SiteRole.SiteCollaborator.toString());

                    assertEquals(SiteRole.SiteManager.toString(), siteService.getMembersRole(testSite.getShortName(), USER_ONE_NAME));
                    assertEquals(SiteRole.SiteCollaborator.toString(), siteService.getMembersRole(testSite.getShortName(), userTwo));

                    // get container of site
                    NodeRef doclib = siteService.getContainer(testSite.getShortName(), SiteService.DOCUMENT_LIBRARY);

                    // create file in container of site
                    NodeRef testNode = testNodes.createQuickFile(MimetypeMap.MIMETYPE_TEXT_PLAIN, doclib, "testDoc", USER_ONE_NAME);

                    assertTrue(nodeService.exists(testNode));

                    // change permissions
                    permissionServiceImpl.setInheritParentPermissions(testNode, false);
                    permissionServiceImpl.setPermission(testNode, USER_ONE_NAME, PermissionService.ALL_PERMISSIONS, true);

                    // create comment
                    NodeRef comment = commentService.createComment(testNode, "This is the comment title", "This is a Web Script comment", true);

                    assertTrue(nodeService.exists(comment));

                    // get post activity
                    ActivityPostEntity params = new ActivityPostEntity();
                    params.setStatus(ActivityPostEntity.STATUS.PENDING.toString());

                    List<ActivityPostEntity> activityPostList = postDAO.selectPosts(params, -1);

                    String activityNodeRef = null;
                    for (ActivityPostEntity activityPostEntry : activityPostList)
                    {
                        if ("comments".equals(activityPostEntry.getAppTool()))
                        {
                            String activityData = activityPostEntry.getActivityData();
                            JSONObject json = new JSONObject(activityData);
                            activityNodeRef = (String) json.get("nodeRef");
                        }
                    }

                    assertFalse(activityNodeRef == null);

                    NodeRef nodeRef = new NodeRef(activityNodeRef);

                    assertTrue(permissionServiceImpl.hasPermission(nodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
                    assertTrue(permissionServiceImpl.hasPermission(nodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
                    assertTrue(permissionServiceImpl.hasPermission(nodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.ALLOWED);

                    authenticationComponent.setCurrentUser(userTwo);

                    assertTrue(permissionServiceImpl.hasPermission(nodeRef, getPermission(PermissionService.READ)) == AccessStatus.DENIED);
                    assertTrue(permissionServiceImpl.hasPermission(nodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.DENIED);
                    assertTrue(permissionServiceImpl.hasPermission(nodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.DENIED);

                    return null;
                }
            });
        }

        finally
        {
            authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

            if (personService.personExists(userTwo))
            {
                personService.deletePerson(userTwo);
            }

            if (authenticationService.authenticationExists(userTwo))
            {
                authenticationService.deleteAuthentication(userTwo);
            }
        }

    }
    
    private PermissionReference getPermission(String permission)
    {
        return permissionModelDAO.getPermissionReference(null, permission);
    }
    
    private void createUser(String userName)
    {
        // if user with given user name doesn't already exist then create user
        if (authenticationService.authenticationExists(userName) == false)
        {
            // create user
            authenticationService.createAuthentication(userName, "password".toCharArray());
            
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, userName);
            personProps.put(ContentModel.PROP_LASTNAME, userName);
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");
            
            // create person node for user
            personService.createPerson(personProps);
        }
    }
    
    /**
     * This test method comments on some nodes asserting that the commentCount rollup property
     * responds correctly to the changing number of comments.
     */
    @Test public void commentOnDocsCheckingCommentCountRollup() throws Exception
    {
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // All test nodes are uncommented initially.
                    for (NodeRef nr : testDocs)
                    {
                        assertCommentCountIs(nr, 0);
                    }
                    
                    // Comment on each node twice
                    Map<NodeRef, List<NodeRef>> mapDiscussableToComments = new HashMap<NodeRef, List<NodeRef>>();
                        
                    for (NodeRef nr : testDocs)
                    {
                        final ArrayList<NodeRef> comments = new ArrayList<NodeRef>();
                        mapDiscussableToComments.put(nr, comments);
                        
                        comments.add(applyComment(nr, "Test comment 1 " + System.currentTimeMillis()));
                        Thread.sleep(50); // 50 ms sleep so comments aren't simultaneous.
                        
                        comments.add(applyComment(nr, "Test comment 2 " + System.currentTimeMillis()));
                        Thread.sleep(50);
                    }
                    
                    // Check that the rollup comment counts are accurate.
                    for (NodeRef nr : testDocs)
                    {
                        assertCommentCountIs(nr, 2);
                    }
                    
                    // Remove comments
                    for (Map.Entry<NodeRef, List<NodeRef>> entry : mapDiscussableToComments.entrySet())
                    {
                        for (NodeRef commentNode : entry.getValue())
                        {
                            nodeService.deleteNode(commentNode);
                        }
                    }
                    
                    // All test nodes are uncommented again.
                    for (NodeRef nr : testDocs)
                    {
                        assertCommentCountIs(nr, 0);
                    }
                    
                    return null;
                }
            });
    }
    
    /**
     * This test method tests that commented nodes from before Swift have their comment counts correctly rolled up.
     * Nodes that were commented on in prior versions of Alfresco will not have commentCount rollups -
     * neither the aspect nor the property defined within it. Alfresco lazily calculates commentCount rollups for these
     * nodes. So they will appear to have a count of 0 (undefined, really) and will not be given the "(count)" UI decoration.
     * Then when a comment is added (or removed), the comment count should be recalculated from scratch.
     */
    @Test public void testRollupOfPreSwiftNodes() throws Exception
    {
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    assertTrue("Not enough test docs for this test case", testDocs.size() >= 2);
                    NodeRef node1 = testDocs.get(0);
                    NodeRef node2 = testDocs.get(1);
                    
                    // We will simulate pre-Swift commenting by temporarily disabling the behaviours that add the aspect & do the rollups.
                    behaviourFilter.disableBehaviour(ForumModel.TYPE_POST);
                    
                    for (NodeRef nr : new NodeRef[]{node1, node2})
                    {
                        // All test nodes initially do not have the commentsRollup aspect.
                        assertFalse("Test node had comments rollup aspect.", nodeService.hasAspect(nr, ForumModel.ASPECT_COMMENTS_ROLLUP));
                    }
                    
                    // Comment on each node - we need to save one comment noderef in order to delete it later.
                    NodeRef commentOnNode1 = applyComment(node1, "Hello", true);
                    applyComment(node1, "Bonjour", true);
                    applyComment(node2, "Hola", true);
                    applyComment(node2, "Bout ye?", true);
                    
                    // Check that the rollup comment counts are still not present. And re-enable the behaviours after we check.
                    for (NodeRef nr : new NodeRef[]{node1, node2})
                    {
                        assertFalse("Test node had comments rollup aspect.", nodeService.hasAspect(nr, ForumModel.ASPECT_COMMENTS_ROLLUP));
                    }
                    behaviourFilter.enableBehaviour(ForumModel.TYPE_POST);
                    
                    // Now the addition or deletion of a comment, should trigger a recalculation of the comment rollup from scratch.
                    applyComment(node2, "hello again");
                    nodeService.deleteNode(commentOnNode1);
                    assertCommentCountIs(node2, 3);
                    assertCommentCountIs(node1, 1);
                    
                    return null;
                }
            });
    }
    
    /**
     * This test method tests that nodes whose commentCount is set to -1 have their commentCounts recalculated.
     * This feature (see ALF-8498) is to allow customers to set their counts to -1 thus triggering a recount for that document.
     */
    @Test public void testTriggerCommentRecount() throws Exception
    {
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    NodeRef testDoc = testDocs.get(0);
                    applyComment(testDoc, "Hello 1");
                    applyComment(testDoc, "Hello 2");
                    applyComment(testDoc, "Hello 3");
                    
                    assertCommentCountIs(testDoc, 3);
                    
                    // We'll cheat and just set it to an arbitrary value.
                    nodeService.setProperty(testDoc, ForumModel.PROP_COMMENT_COUNT, 42);
                    
                    // It should have that value - even though it's wrong.
                    assertCommentCountIs(testDoc, 42);
                    
                    // Now we'll set it to the trigger value -1.
                    nodeService.setProperty(testDoc, ForumModel.PROP_COMMENT_COUNT, ForumPostBehaviours.COUNT_TRIGGER_VALUE);
                    
                    // It should have the correct, recalculated value.
                    assertCommentCountIs(testDoc, 3);
                    
                    return null;
                }
            });
    }

    
    /**
     * This method asserts that the commentCount (rollup) is as specified for the given node.
     */
    private void assertCommentCountIs(NodeRef discussableNode, int expectedCount)
    {
        final Serializable commentCount = nodeService.getProperty(discussableNode, ForumModel.PROP_COMMENT_COUNT);
        if (expectedCount == 0)
        {
            assertTrue("Uncommented node should have EITHER no commentsRollup aspect OR commentCount of zero.",
                    !nodeService.hasAspect(discussableNode, ForumModel.ASPECT_COMMENTS_ROLLUP) ||
                    (commentCount != null && commentCount.equals(new Integer(0))
                            ));
        }
        else
        {
            assertTrue("Commented node should have discussable aspect.", nodeService.hasAspect(discussableNode, ForumModel.ASPECT_COMMENTS_ROLLUP));
            assertEquals("Wrong comment count", expectedCount, commentCount);
        }
    }
    
    private NodeRef applyComment(NodeRef nr, String comment)
    {
        return applyComment(nr, comment, false);
    }

    /**
     * This method applies the specified comment to the specified node.
     * As there is no CommentService or DiscussionService, we mimic here what the comments REST API does,
     * by manually creating the correct content structure using the nodeService. Behaviours will do some
     * of the work for us. See comments.post.json.js for comparison.
     * @param nr nodeRef to comment on.
     * @param comment the text of the comment.
     * @param suppressRollups if true, commentsRollup aspect will not be added.
     * @return the NodeRef of the fm:post comment node.
     * 
     * @see CommentsTest#testRollupOfPreSwiftNodes() for use of suppressRollups.
     */
    private NodeRef applyComment(NodeRef nr, String comment, boolean suppressRollups)
    {
        // There is no CommentService, so we have to create the node structure by hand.
        // This is what happens within e.g. comment.put.json.js when comments are submitted via the REST API.
        if (!nodeService.hasAspect(nr, ForumModel.ASPECT_DISCUSSABLE))
        {
            nodeService.addAspect(nr, ForumModel.ASPECT_DISCUSSABLE, null);
        }
        if (!nodeService.hasAspect(nr, ForumModel.ASPECT_COMMENTS_ROLLUP) && !suppressRollups)
        {
            nodeService.addAspect(nr, ForumModel.ASPECT_COMMENTS_ROLLUP, null);
        }
        // Forum node is created automatically by DiscussableAspect behaviour.
        NodeRef forumNode = nodeService.getChildAssocs(nr, ForumModel.ASSOC_DISCUSSION, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion")).get(0).getChildRef();
        
        final List<ChildAssociationRef> existingTopics = nodeService.getChildAssocs(forumNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments"));
        NodeRef topicNode = null;
        if (existingTopics.isEmpty())
        {
            topicNode = nodeService.createNode(forumNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments"), ForumModel.TYPE_TOPIC).getChildRef();
        }
        else
        {
            topicNode = existingTopics.get(0).getChildRef();
        }

        NodeRef postNode = nodeService.createNode(topicNode, ContentModel.ASSOC_CONTAINS, QName.createQName("comment" + System.currentTimeMillis()), ForumModel.TYPE_POST).getChildRef();
        nodeService.setProperty(postNode, ContentModel.PROP_CONTENT, new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
        ContentWriter writer = contentService.getWriter(postNode, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(comment);
        
        return postNode;
    }
}
