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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.thumbnail.FailureHandlingOptions;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.FailedThumbnailInfo;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This action evaluator is specifically created for the {@link ThumbnailService Thumbnail Service}.
 * It is used to evaluate whether a {@link ThumbnailDefinition} should be executed - based on
 * previous failed thumbnail attempts for that definition on that source node, as well as some
 * configuration data.
 * <p/>
 * The behaviour is as follows:
 * <ul>
 * <li>All content nodes are eligible for thumbnailing initially. Of course thumbnails can only ever be
 *     attempted for those content mime types that have at least one registered and active {@link ContentTransformer}.</li>
 * <li>If the first attempt to produce a thumbnail for a node fails, then it may be retried up to a maximum of
 *     {@link FailureHandlingOptions#getRetryCount() system.thumbnail.retryCount} times.</li>
 * <li>These initial retries to produce a thumbnail will occur not more often than every
 *     {@link FailureHandlingOptions#getRetryPeriod() system.thumbnail.retryPeriod} seconds
 *     and will use which ever content transformers the {@link ContentService#getTransformer(String, String) content service} gives.</li>
 * <li>If a thumbnail is not successfully produced for a node after these attempts then it is considered to be
 *     a 'difficult' piece of content with respect to thumbnailing and the assumption is that a thumbnail may
 *     never be available for it. However, in order to allow for the possibility of software upgrades or similiar, which may
 *     make the content thumbnailable at a later date, further attempts will be made, but at a much reduced frequency.</li>
 * <li>Difficult pieces of content will not be attempted more often than every
 *     {@link FailureHandlingOptions#getQuietPeriod() system.thumbnail.quietPeriod} seconds.</li>
 * <li>The attempts to thumbnail difficult pieces of content can be disabled by setting
 *     {@link FailureHandlingOptions#getQuietPeriodRetriesEnabled() system.thumbnail.quietPeriodRetriesEnabled} to false.</li>
 * </ul>
 * At all times, thumbnails will be attempted when a user navigates to a page which needs to show the relevant thumbnail (lazy production).
 * 
 * @author Neil Mc Erlean.
 * @since 3.5.0
 */
public class NodeEligibleForRethumbnailingEvaluator extends ActionConditionEvaluatorAbstractBase 
{
    private static Log logger = LogFactory.getLog(NodeEligibleForRethumbnailingEvaluator.class);

    /**
     * Evaluator constants
     */
    public final static String NAME = "node-eligible-for-rethumbnailing-evaluator";

    public final static String PARAM_THUMBNAIL_DEFINITION_NAME = "thumbnail-definition-name";
    
    public final static String PARAM_RETRY_PERIOD = "retry-period";
    public final static String PARAM_RETRY_COUNT = "retry-count";
    public final static String PARAM_QUIET_PERIOD = "quiet-period";
    public final static String PARAM_QUIET_PERIOD_RETRIES_ENABLED = "quiet-period-retries-enabled";

    protected NodeService nodeService;
    protected ThumbnailService thumbnailService;
    
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }
    
    public void setThumbnailService(ThumbnailService thumbnailService) 
    {
        this.thumbnailService = thumbnailService;
    }
    
    /**
     * Add parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_THUMBNAIL_DEFINITION_NAME, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_THUMBNAIL_DEFINITION_NAME)));
        
        paramList.add(new ParameterDefinitionImpl(PARAM_RETRY_PERIOD, DataTypeDefinition.LONG, true, getParamDisplayLabel(PARAM_RETRY_PERIOD)));
        paramList.add(new ParameterDefinitionImpl(PARAM_RETRY_COUNT, DataTypeDefinition.INT, true, getParamDisplayLabel(PARAM_RETRY_COUNT)));
        
        paramList.add(new ParameterDefinitionImpl(PARAM_QUIET_PERIOD, DataTypeDefinition.LONG, true, getParamDisplayLabel(PARAM_QUIET_PERIOD)));
        paramList.add(new ParameterDefinitionImpl(PARAM_QUIET_PERIOD_RETRIES_ENABLED, DataTypeDefinition.BOOLEAN, true, getParamDisplayLabel(PARAM_QUIET_PERIOD_RETRIES_ENABLED)));
    }

    /**
     * @see ActionConditionEvaluatorAbstractBase#evaluateImpl(ActionCondition, NodeRef)
     */
    public boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef) 
    {
        if (!this.nodeService.exists(actionedUponNodeRef))
        {
            return false;
        }
    
        Serializable paramThumbnailDefnName = actionCondition.getParameterValue(PARAM_THUMBNAIL_DEFINITION_NAME);
        
        Serializable paramRetryPeriod = actionCondition.getParameterValue(PARAM_RETRY_PERIOD);
        Serializable paramRetryCount = actionCondition.getParameterValue(PARAM_RETRY_COUNT);
        Serializable paramQuietPeriod = actionCondition.getParameterValue(PARAM_QUIET_PERIOD);
        
        final Serializable parameterValue = actionCondition.getParameterValue(PARAM_QUIET_PERIOD_RETRIES_ENABLED);
        Serializable paramQuietPeriodRetriesEnabled = parameterValue != null ? parameterValue : FailureHandlingOptions.DEFAULT_QUIET_PERIOD_RETRIES_ENABLED;
        
        
        
        final QName thumbnailDefnQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String)paramThumbnailDefnName);
        
        // If there are no previous failed thumbnail attempts for this thumbnail definition,
        // then the node is always eligible for a first try.
        Map<String, FailedThumbnailInfo> failures = thumbnailService.getFailedThumbnails(actionedUponNodeRef);
        if (failures.isEmpty() || !failures.containsKey(thumbnailDefnQName.getLocalName()))
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Node ").append(actionedUponNodeRef).append(" has no matching ").append(ContentModel.ASSOC_FAILED_THUMBNAIL)
                   .append(" child.");
                logger.debug(msg.toString());
            }
            return true;
        }
        
        
        final FailedThumbnailInfo failedThumbnailInfo = failures.get(thumbnailDefnQName.getLocalName());
        
        // There is a cm:failedThumbnail child, so there has been at least one failed execution of the given
        // thumbnail definition at some point.
        if (failedThumbnailInfo.getMostRecentFailure() == null)
        {
            // The property should never be null like this, but just in case.
            return true;
        }
        
        // So how long ago did the given thumbnail definition fail?
        long nowMs = new Date().getTime();
        long failureTimeMs = failedThumbnailInfo.getMostRecentFailure().getTime();
        final long timeSinceLastFailureMs = nowMs - failureTimeMs;
        
        // And how many failures have there been?
        final int failureCount = failedThumbnailInfo.getFailureCount();
        
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Comparing failure time of ")
               .append(failedThumbnailInfo.getMostRecentFailure()).append(" to now. Difference is ")
               .append(timeSinceLastFailureMs).append(" ms. ").append(failureCount).append(" existing failures.");
            logger.debug(msg.toString());
        }

        if (failureCount >= (Integer)paramRetryCount)
        {
            boolean quietPeriodRetriesEnabled = (Boolean)paramQuietPeriodRetriesEnabled;
            return quietPeriodRetriesEnabled && timeSinceFailureExceedsLimit(paramQuietPeriod, timeSinceLastFailureMs);
        }
        else
        {
            return timeSinceFailureExceedsLimit(paramRetryPeriod, timeSinceLastFailureMs);
        }
    }

    private boolean timeSinceFailureExceedsLimit(Serializable failurePeriod, long timeSinceFailure)
    {
        // Is that time period greater than the specified offset?
        // We'll allow for -ve offset values.
        Long offsetLong = (Long)failurePeriod;
        long absOffset = Math.abs(offsetLong);
        
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Offset is ")
               .append(offsetLong).append(" ms.");
            logger.debug(msg.toString());
        }
        
        return timeSinceFailure > absOffset;
    }
}
