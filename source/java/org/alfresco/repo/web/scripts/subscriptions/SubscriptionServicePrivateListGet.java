package org.alfresco.repo.web.scripts.subscriptions;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class SubscriptionServicePrivateListGet extends AbstractSubscriptionServiceWebScript
{
    @SuppressWarnings("unchecked")
    public JSONObject executeImpl(String userId, WebScriptRequest req, WebScriptResponse res) throws IOException,
            ParseException
    {
        JSONObject obj = new JSONObject();
        obj.put("private", subscriptionService.isSubscriptionListPrivate(userId));

        return obj;
    }
}
