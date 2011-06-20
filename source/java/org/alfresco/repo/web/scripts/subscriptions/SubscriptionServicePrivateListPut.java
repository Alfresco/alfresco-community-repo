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
package org.alfresco.repo.web.scripts.subscriptions;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class SubscriptionServicePrivateListPut extends SubscriptionServicePrivateListGet
{
    public JSONObject executeImpl(String userId, WebScriptRequest req, WebScriptResponse res) throws IOException,
            JSONException
    {
        JSONObject obj = new JSONObject(req.getContent().getContent());

        String setPrivate = obj.getString("private");

        if (setPrivate != null)
        {
            if (setPrivate.equalsIgnoreCase("true"))
            {
                subscriptionService.setSubscriptionListPrivate(userId, true);
            } else if (setPrivate.equalsIgnoreCase("false"))
            {
                subscriptionService.setSubscriptionListPrivate(userId, false);
            }
        }

        return super.executeImpl(userId, req, res);
    }
}
