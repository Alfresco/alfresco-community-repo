package org.alfresco.repo.web.scripts.subscriptions;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class SubscriptionServicePrivateListPut extends SubscriptionServicePrivateListGet
{
    public JSONObject executeImpl(String userId, WebScriptRequest req, WebScriptResponse res) throws IOException,
            ParseException
    {
        JSONObject obj = (JSONObject) JSONValue.parseWithException(req.getContent().getContent());

        Object setPrivate = obj.get("private");

        if (setPrivate != null)
        {
            if (setPrivate.toString().equalsIgnoreCase("true"))
            {
                subscriptionService.setSubscriptionListPrivate(userId, true);
            } else if (setPrivate.toString().equalsIgnoreCase("false"))
            {
                subscriptionService.setSubscriptionListPrivate(userId, false);
            }
        }

        return super.executeImpl(userId, req, res);
    }
}
