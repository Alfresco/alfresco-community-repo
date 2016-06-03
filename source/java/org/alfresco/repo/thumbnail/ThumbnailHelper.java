package org.alfresco.repo.thumbnail;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.thumbnail.conditions.NodeEligibleForRethumbnailingEvaluator;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;

/**
 * A simple helper class for constructing create-thumbnail actions decorated with the correct
 * condition and compensating actions.
 * 
 * @author Neil Mc Erlean
 * @since 3.5.0
 */
public class ThumbnailHelper
{
    public static Action createCreateThumbnailAction(ThumbnailDefinition thumbnailDef, ServiceRegistry services)
    {
        ActionService actionService = services.getActionService();
        
        Action action = actionService.createAction(CreateThumbnailActionExecuter.NAME);
        action.setParameterValue(CreateThumbnailActionExecuter.PARAM_THUMBANIL_NAME, thumbnailDef.getName());
        
        decorateAction(thumbnailDef, action, actionService);
        
        return action;
    }
    

    private static void decorateAction(ThumbnailDefinition thumbnailDef, Action action, ActionService actionService)
    {
        final FailureHandlingOptions failureOptions = thumbnailDef.getFailureHandlingOptions();
        long retryPeriod = failureOptions == null ? FailureHandlingOptions.DEFAULT_PERIOD : failureOptions.getRetryPeriod() * 1000l;
        int retryCount = failureOptions == null ? FailureHandlingOptions.DEFAULT_RETRY_COUNT : failureOptions.getRetryCount();
        long quietPeriod = failureOptions == null ? FailureHandlingOptions.DEFAULT_PERIOD : failureOptions.getQuietPeriod() * 1000l;
        boolean quietPeriodRetriesEnabled = failureOptions == null ?
                FailureHandlingOptions.DEFAULT_QUIET_PERIOD_RETRIES_ENABLED : failureOptions.getQuietPeriodRetriesEnabled();
        
        // The thumbnail/action should only be run if it is eligible.
        Map<String, Serializable> failedThumbnailConditionParams = new HashMap<String, Serializable>();
        failedThumbnailConditionParams.put(NodeEligibleForRethumbnailingEvaluator.PARAM_THUMBNAIL_DEFINITION_NAME, thumbnailDef.getName());
        failedThumbnailConditionParams.put(NodeEligibleForRethumbnailingEvaluator.PARAM_RETRY_PERIOD, retryPeriod);
        failedThumbnailConditionParams.put(NodeEligibleForRethumbnailingEvaluator.PARAM_RETRY_COUNT, retryCount);
        failedThumbnailConditionParams.put(NodeEligibleForRethumbnailingEvaluator.PARAM_QUIET_PERIOD, quietPeriod);
        failedThumbnailConditionParams.put(NodeEligibleForRethumbnailingEvaluator.PARAM_QUIET_PERIOD_RETRIES_ENABLED, quietPeriodRetriesEnabled);
        
        ActionCondition thumbnailCondition = actionService.createActionCondition(NodeEligibleForRethumbnailingEvaluator.NAME, failedThumbnailConditionParams);
        

        // If it is run and if it fails, then we want a compensating action to run which will mark
        // the source node as having failed to produce a thumbnail.
        Action applyBrokenThumbnail = actionService.createAction("add-failed-thumbnail");
        applyBrokenThumbnail.setParameterValue(AddFailedThumbnailActionExecuter.PARAM_THUMBNAIL_DEFINITION_NAME, thumbnailDef.getName());
        applyBrokenThumbnail.setParameterValue(AddFailedThumbnailActionExecuter.PARAM_FAILURE_DATETIME, new Date());

        action.addActionCondition(thumbnailCondition);
        action.setCompensatingAction(applyBrokenThumbnail);
    }
}
