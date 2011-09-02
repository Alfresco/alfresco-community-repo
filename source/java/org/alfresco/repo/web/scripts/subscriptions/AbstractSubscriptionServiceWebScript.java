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
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.subscriptions.PrivateSubscriptionListException;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.service.cmr.subscriptions.SubscriptionsDisabledException;
import org.alfresco.util.ISO8601DateFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public abstract class AbstractSubscriptionServiceWebScript extends AbstractWebScript
{
    protected SubscriptionService subscriptionService;
    protected NodeService nodeService;
    protected PersonService personService;

    public void setSubscriptionService(SubscriptionService subscriptionService)
    {
        this.subscriptionService = subscriptionService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        if (!subscriptionService.isActive())
        {
            res.setStatus(404);
            return;
        }

        try
        {
            String userId = req.getServiceMatch().getTemplateVars().get("userid");
            Object obj = executeImpl(userId, req, res);

            if (obj instanceof JSONObject || obj instanceof JSONArray)
            {
                res.setContentEncoding(Charset.defaultCharset().displayName());

                Writer writer = res.getWriter();
                if (obj instanceof JSONObject)
                {
                    ((JSONObject) obj).writeJSONString(writer);
                } else
                {
                    ((JSONArray) obj).writeJSONString(writer);
                }
                writer.flush();
            } else
            {
                res.setStatus(204);
            }
        } catch (SubscriptionsDisabledException sde)
        {
            throw new WebScriptException(404, "Subscription service is disabled!", sde);
        } catch (NoSuchPersonException nspe)
        {
            throw new WebScriptException(404, "Unknown user '" + nspe.getUserName() + "'!", nspe);
        } catch (PrivateSubscriptionListException psle)
        {
            throw new WebScriptException(403, "Subscription list is private!", psle);
        } catch (ParseException pe)
        {
            throw new WebScriptException(400, "Unable to parse JSON!", pe);
        } catch (ClassCastException cce)
        {
            throw new WebScriptException(400, "Unable to parse JSON!", cce);
        } catch (IOException ioe)
        {
            throw new WebScriptException(500, "Unable to serialize JSON!", ioe);
        }
    }

    public abstract Object executeImpl(String userId, WebScriptRequest req, WebScriptResponse res) throws IOException,
            ParseException;

    protected int parseNumber(String name, String number, int def)
    {
        if (number != null && number.length() > 0)
        {
            try
            {
                return Integer.parseInt(number);

            } catch (NumberFormatException e)
            {
                throw new WebScriptException(400, name + " is not a number!", e);
            }
        } else
        {
            return def;
        }
    }

    protected PagingRequest createPagingRequest(WebScriptRequest req)
    {
        int skipCount = parseNumber("skipCount", req.getParameter("skipCount"), 0);
        int maxItems = parseNumber("maxItems", req.getParameter("maxItems"), -1);

        PagingRequest result = new PagingRequest(skipCount, maxItems, null);
        result.setRequestTotalCountMax(Integer.MAX_VALUE);

        return result;
    }

    @SuppressWarnings("unchecked")
    protected JSONObject getUserDetails(String username)
    {
        NodeRef node = personService.getPerson(username);

        JSONObject result = new JSONObject();
        result.put("userName", username);
        result.put("firstName", nodeService.getProperty(node, ContentModel.PROP_FIRSTNAME));
        result.put("lastName", nodeService.getProperty(node, ContentModel.PROP_LASTNAME));
        result.put("jobtitle", nodeService.getProperty(node, ContentModel.PROP_JOBTITLE));
        result.put("organization", nodeService.getProperty(node, ContentModel.PROP_ORGANIZATION));

        String status = (String) nodeService.getProperty(node, ContentModel.PROP_USER_STATUS);
        if (status != null)
        {
            result.put("userStatus", status);
        }

        Date statusTime = (Date) nodeService.getProperty(node, ContentModel.PROP_USER_STATUS_TIME);
        if (statusTime != null)
        {
            JSONObject statusTimeJson = new JSONObject();
            statusTimeJson.put("iso8601", ISO8601DateFormat.format(statusTime));
            result.put("userStatusTime", statusTimeJson);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    protected JSONArray getUserArray(List<String> usernames)
    {
        JSONArray result = new JSONArray();

        if (usernames != null)
        {
            for (String username : usernames)
            {
                result.add(getUserDetails(username));
            }
        }

        return result;
    }
}
