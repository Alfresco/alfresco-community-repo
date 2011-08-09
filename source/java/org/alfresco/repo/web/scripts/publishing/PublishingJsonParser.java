/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.publishing;

import static org.alfresco.repo.web.scripts.WebScriptUtil.getCalendar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.node.NodeUtils;
import org.alfresco.service.cmr.publishing.PublishingDetails;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.collections.Function;
import org.alfresco.util.collections.JsonUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishingJsonParser implements PublishingWebScriptConstants
{

    public JSONObject getJson(String jsonStr) throws JSONException
    {
        if(jsonStr!=null && jsonStr.isEmpty()==false)
        {
            return new JSONObject(new JSONTokener(jsonStr));
        }
        return new JSONObject();
    }

    public void updateChannel(Channel channel, String jsonStr, ChannelService channelService) throws JSONException
    {
        JSONObject json = getJson(jsonStr);
        String newName = json.optString(NAME);
        if(newName != null && newName.isEmpty() == false)
        {
            channelService.renameChannel(channel, newName);
        }
    }
    
    public String schedulePublishingEvent(PublishingQueue queue, String jsonStr) throws ParseException, JSONException
    {
        JSONObject json = getJson(jsonStr);
        PublishingDetails details = queue.createPublishingDetails()
            .setPublishChannel(json.optString(CHANNEL_ID))
            .setComment(json.optString(COMMENT))
            .setSchedule(getCalendar(json.optJSONObject(SCHEDULED_TIME)))
            .addNodesToPublish(toNodes(json.optJSONArray(PUBLISH_NODES)))
            .addNodesToUnpublish(toNodes(json.optJSONArray(UNPUBLISH_NODES)));

        details = setStatusUpdate(details, json.optJSONObject(STATUS_UPDATE));
        return queue.scheduleNewEvent(details);
    }
    
    public PublishingDetails setStatusUpdate(PublishingDetails details, JSONObject json)
    {
        if(json != null)
        {
            details.setStatusMessage(json.optString(MESSAGE));
            String nodeStr = json.optString(NODE_REF);
            if (nodeStr != null && nodeStr.isEmpty() == false)
            {
                details.setStatusNodeToLinkTo(new NodeRef(nodeStr));
            }
            details.addStatusUpdateChannels(toStrings(json.optJSONArray(CHANNEL_IDS)));
        }
        return details;
    }
    
    public List<NodeRef> toNodes(JSONArray json)
    {
        Function<String, NodeRef> transformer = NodeUtils.toNodeRef();
        return JsonUtils.transform(json, transformer);
    }
    
    private List<String> toStrings(JSONArray json)
    {
        if(json == null || json.length() == 0)
        {
            return Collections.emptyList();
        }
        ArrayList<String> results = new ArrayList<String>(json.length());
        for (int i = 0; i < json.length(); i++)
        {
            results.add(json.optString(i));
        }
        return results;
    }

}
