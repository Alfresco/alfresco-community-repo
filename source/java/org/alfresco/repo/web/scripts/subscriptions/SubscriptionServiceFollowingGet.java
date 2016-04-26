package org.alfresco.repo.web.scripts.subscriptions;

import java.io.IOException;

import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class SubscriptionServiceFollowingGet extends AbstractSubscriptionServiceWebScript
{
    @SuppressWarnings("unchecked")
    public JSONObject executeImpl(String userId, WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        PagingFollowingResults result = subscriptionService.getFollowing(userId, createPagingRequest(req));

        JSONObject obj = new JSONObject();
        obj.put("people", getUserArray(result.getPage()));
        obj.put("hasMoreItems", result.hasMoreItems());
        if (result.getTotalResultCount() != null)
        {
            obj.put("totalCount", result.getTotalResultCount().getFirst());
        }

        return obj;
    }
}
