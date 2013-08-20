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
package org.alfresco.repo.thumbnail.conditions;

import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.FailureHandlingOptions;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.thumbnail.FailedThumbnailInfo;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * This class tests {@link NodeEligibleForRethumbnailingEvaluator}.
 * 
 * @author Neil Mc Erlean
 * @since 3.5.0
 */
public class NodeEligibleForRethumbnailingEvaluatorTest extends BaseSpringTest
{
    private final QName thumbnailDef1 = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "thumbDef1");
    
    private NodeService nodeService;
    private ThumbnailService thumbnailService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    
    private FailureHandlingOptions failureHandlingOptions;

    /**
     * A node with no thumbnails on it & no previous attempts to produce thumbnails.
     */
    private NodeRef newUnthumbnailedNodeRef;
    
    /**
     * No thumbnails. 1 failed attempt 2 seconds ago.
     */
    private NodeRef recentlyFailedNodeRef;
    
    /**
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        final Date now = new Date();
        final Date twoSecondsAgo = new Date(now.getTime() - 2000);
        
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        this.thumbnailService = (ThumbnailService)this.applicationContext.getBean("thumbnailService");
        this.failureHandlingOptions = (FailureHandlingOptions) this.applicationContext.getBean("standardFailureOptions");

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // We'll fake this. We won't actually run any thumbnails - just set up the various aspect and child-node
        // conditions we need to test the evaluator.
        this.newUnthumbnailedNodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}newUnthumbnailedNodeRef"),
                ContentModel.TYPE_CONTENT).getChildRef();

        this.recentlyFailedNodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName("{test}recentlyFailedNodeRef"),
                ContentModel.TYPE_CONTENT).getChildRef();
        nodeService.addAspect(recentlyFailedNodeRef, ContentModel.ASPECT_FAILED_THUMBNAIL_SOURCE, null);
        NodeRef thumbDef1FailureNode = nodeService.createNode(recentlyFailedNodeRef,
                                                             ContentModel.ASSOC_FAILED_THUMBNAIL,
                                                             thumbnailDef1,
                                                             ContentModel.TYPE_FAILED_THUMBNAIL).getChildRef();
        nodeService.setProperty(thumbDef1FailureNode, ContentModel.PROP_FAILED_THUMBNAIL_TIME, twoSecondsAgo);
        nodeService.setProperty(thumbDef1FailureNode, ContentModel.PROP_FAILURE_COUNT, 1);
    }
    
    @Override
    public void onTearDownInTransaction()
    {
        nodeService.deleteStore(testStoreRef);
    }

    @SuppressWarnings("deprecation")
    public void testNodeWithNoFailedThumbnails()
    {
        // Such a node is always eligible for thumbnailing.
        ActionCondition condition = new ActionConditionImpl(GUID.generate(), NodeEligibleForRethumbnailingEvaluator.NAME);
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_THUMBNAIL_DEFINITION_NAME, thumbnailDef1.getLocalName());
        // Offset values don't matter here.
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_RETRY_PERIOD, 0L);
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_RETRY_COUNT,
                failureHandlingOptions.getRetryCount());
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_QUIET_PERIOD, 0L);
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_QUIET_PERIOD_RETRIES_ENABLED, true);
        
        NodeEligibleForRethumbnailingEvaluator evaluator =
            (NodeEligibleForRethumbnailingEvaluator)this.applicationContext.getBean(NodeEligibleForRethumbnailingEvaluator.NAME);
        
        assertTrue(evaluator.evaluate(condition, newUnthumbnailedNodeRef));
    }

    public void testNodeWithFailedThumbnails()
    {
        // A "non-difficult" node is one which is not yet known to be difficult to thumbnail.
        // In other words it is one which has previously failed to thumbnail, but which has not yet
        // hit the retryCount limit for initial retries.
        
        NodeEligibleForRethumbnailingEvaluator evaluator =
            (NodeEligibleForRethumbnailingEvaluator)this.applicationContext.getBean(NodeEligibleForRethumbnailingEvaluator.NAME);
        
        // Evaluate the thumbnail definition which has failed.
        //
        // 1. A node that has failed once - and more recently than the limit.
        ActionCondition condition = new ActionConditionImpl(GUID.generate(), NodeEligibleForRethumbnailingEvaluator.NAME);
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_THUMBNAIL_DEFINITION_NAME, thumbnailDef1.getLocalName());
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_RETRY_PERIOD,
                failureHandlingOptions.getRetryPeriod() * 1000);
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_RETRY_COUNT,
                failureHandlingOptions.getRetryCount());
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_QUIET_PERIOD,
                                     failureHandlingOptions.getQuietPeriod() * 1000);
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_QUIET_PERIOD_RETRIES_ENABLED, true);
        
        assertFalse(evaluator.evaluate(condition, recentlyFailedNodeRef));

        // 2. A node that has failed once - but longer ago than the lower limit.
        Map<String, FailedThumbnailInfo> failures = thumbnailService.getFailedThumbnails(recentlyFailedNodeRef);
        assertFalse(failures.isEmpty());
        final FailedThumbnailInfo failedThumbnailInfo = failures.get(thumbnailDef1.getLocalName());
        
        final long timeBeforeTheLimit = new Date().getTime() - (failureHandlingOptions.getRetryPeriod() * 1000l) - 5000l;
        nodeService.setProperty(failedThumbnailInfo.getFailedThumbnailNode(), ContentModel.PROP_FAILED_THUMBNAIL_TIME, timeBeforeTheLimit);
        
        assertTrue(evaluator.evaluate(condition, recentlyFailedNodeRef));
        
        
        
        // 3. If the same node had failed retryCount times, it would not be eligible.
        // At this point it would be a "difficult" document.
        nodeService.setProperty(failedThumbnailInfo.getFailedThumbnailNode(), ContentModel.PROP_FAILURE_COUNT, failureHandlingOptions.getRetryCount());
        
        assertFalse(evaluator.evaluate(condition, recentlyFailedNodeRef));
        

        // 4. If it had failed retryCount times, but its last failure time was more than
        //    quietPeriod seconds ago, then it would be eligible.
        final long timeBeforeTheLongLimit = new Date().getTime() - (failureHandlingOptions.getQuietPeriod() * 1000l) - 5000l;
        nodeService.setProperty(failedThumbnailInfo.getFailedThumbnailNode(), ContentModel.PROP_FAILED_THUMBNAIL_TIME, timeBeforeTheLongLimit);

        assertTrue(evaluator.evaluate(condition, recentlyFailedNodeRef));
        
        // 5. Unless the retries during the quiet period are disabled...
        condition.setParameterValue(NodeEligibleForRethumbnailingEvaluator.PARAM_QUIET_PERIOD_RETRIES_ENABLED, false);

        assertFalse(evaluator.evaluate(condition, recentlyFailedNodeRef));
    }
}
