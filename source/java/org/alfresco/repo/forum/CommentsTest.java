/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test class for some {@link ForumModel forum model}-related functionality, specifically comments.
 * There is no "CommentService" or "DiscussionService" and the REST API simply creates the appropriate
 * content structure as required by the forum model.
 * 
 * @author Neil McErlean
 * @since 4.0
 */
public class CommentsTest
{
    private static final ApplicationContext testContext = ApplicationContextHelper.getApplicationContext();
    
    // Services
    private static BehaviourFilter behaviourFilter;
    private static ContentService contentService;
    private static NodeService nodeService;
    private static Repository repositoryHelper;
    private static RetryingTransactionHelper transactionHelper;

    // These NodeRefs are used by the test methods.
    private NodeRef testFolder;
    private List<NodeRef> testDocs;
    
    /**
     * Initialise various services required by the test.
     */
    @BeforeClass public static void initTestsContext() throws Exception
    {
        behaviourFilter = (BehaviourFilter)testContext.getBean("policyBehaviourFilter");
        contentService = (ContentService)testContext.getBean("ContentService");
        nodeService = (NodeService)testContext.getBean("NodeService");
        repositoryHelper = (Repository)testContext.getBean("repositoryHelper");
        transactionHelper = (RetryingTransactionHelper)testContext.getBean("retryingTransactionHelper");
        
        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }
    
    /**
     * Create some content that can be commented on.
     */
    @Before public void initIndividualTestContext() throws Exception
    {
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Create some content which we will comment on.
                NodeRef companyHome = repositoryHelper.getCompanyHome();
                
                testFolder = createNode(companyHome, "testFolder", ContentModel.TYPE_FOLDER);
                testDocs = new ArrayList<NodeRef>(3);
                for (int i = 0; i < 3; i++)
                {
                    NodeRef testNode = createNode(testFolder, "testDocInFolder", ContentModel.TYPE_CONTENT);
                    testDocs.add(testNode);
                }
                
                return null;
            }
        });
    }
    
    /**
     * This method deletes any nodes which were created during test execution.
     */
    @After public void tidyUpTestNodes() throws Exception
    {
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                    
                    for (NodeRef nr : testDocs)
                    {
                        if (nodeService.exists(nr)) nodeService.deleteNode(nr);
                    }
                    
                    return null;
                }
            });
    }
    
    /**
     * Create a node of the specified content type, under the specified parent node with the specified cm:name.
     */
    private NodeRef createNode(NodeRef parentNode, String name, QName type)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        String fullName = name + "-" + GUID.generate();
        props.put(ContentModel.PROP_NAME, fullName);
        QName docContentQName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, fullName);
        NodeRef node = nodeService.createNode(parentNode,
                    ContentModel.ASSOC_CONTAINS,
                    docContentQName,
                    type,
                    props).getChildRef();
        return node;
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
