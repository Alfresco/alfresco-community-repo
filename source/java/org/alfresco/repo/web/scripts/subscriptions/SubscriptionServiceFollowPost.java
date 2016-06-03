package org.alfresco.repo.web.scripts.subscriptions;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class SubscriptionServiceFollowPost extends AbstractSubscriptionServiceWebScript
{
    public JSONObject executeImpl(String userId, WebScriptRequest req, WebScriptResponse res) throws IOException,
            ParseException
    {
        JSONArray jsonUsers = (JSONArray) JSONValue.parseWithException(req.getContent().getContent());

        for (Object o : jsonUsers)
        {
            String user = (o == null ? null : o.toString());
            if (user != null)
            {
                subscriptionService.follow(userId, user);
            }
        }

        return null;
    }
}
