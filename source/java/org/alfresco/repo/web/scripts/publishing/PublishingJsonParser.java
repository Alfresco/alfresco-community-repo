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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishingJsonParser
{
    /**
     * 
     */
    public static final String STATUS_UPDATE = "statusUpdate";
    public static final String SCHEDULE = "schedule";
    public static final String COMMENT = "comment";
    public static final String CHANNEL_NAME = "channelName";
    public static final String CHANNEL_NAMES = "channelNames";
    public static final String NODE_REF = "nodeRef";
    public static final String MESSAGE = "message";
    public static final String UNPUBLISH_NODES = "unpublishNodes";
    public static final String PUBLISH_NODES = "publishNodes";
    public static final String TIME_ZONE = "timeZone";
    public static final String FORMAT = "format";
    public static final String DATE_TIME = "dateTime";

    public JSONObject getJson(String jsonStr) throws JSONException
    {
        return new JSONObject(new JSONTokener(jsonStr));
    }
    
    public String schedulePublishingEvent(PublishingQueue queue, String jsonStr) throws JSONException, ParseException
    {
        JSONObject json = getJson(jsonStr);
        String channelName = json.optString(CHANNEL_NAME);
        String comment = json.optString(COMMENT);
        Calendar schedule = getCalendar(json.optJSONObject(SCHEDULE));
        PublishingPackage publishingPackage = getPublishingPackage(queue, json);
        StatusUpdate statusUpdate = getStatusUpdate(queue, json.getJSONObject(STATUS_UPDATE));
        return queue.scheduleNewEvent(publishingPackage, channelName, schedule, comment, statusUpdate);
    }
    
    public StatusUpdate getStatusUpdate(PublishingQueue queue, JSONObject json)
    {
        if(json == null)
        {
            return null;
        }
        String message = json.optString(MESSAGE);
        NodeRef nodeToLinkTo = null;
        String nodeStr = json.optString(NODE_REF);
        if(nodeStr!=null)
        {
            nodeToLinkTo = new NodeRef(nodeStr);
        }
        Collection<String> channelNames = toStrings(json.optJSONArray(CHANNEL_NAMES));
        return queue.createStatusUpdate(message, nodeToLinkTo, channelNames);
    }
    
    public PublishingPackage getPublishingPackage(PublishingQueue queue, JSONObject json)
    {
        MutablePublishingPackage pckg = queue.createPublishingPackage();
        List<NodeRef> publishNodes = toNodes(json.optJSONArray(PUBLISH_NODES));
        List<NodeRef> unpublishNodes = toNodes(json.optJSONArray(UNPUBLISH_NODES));
        pckg.addNodesToPublish(publishNodes);
        pckg.addNodesToUnpublish(unpublishNodes);
        return pckg;
    }
    
    public List<NodeRef> toNodes(JSONArray json)
    {
        if(json == null || json.length() == 0)
        {
            return Collections.emptyList();
        }
        ArrayList<NodeRef> results = new ArrayList<NodeRef>(json.length());
        for (int i = 0; i < json.length(); i++)
        {
            String nodeStr = json.optString(i);
            results.add(new NodeRef(nodeStr));
        }
        return results;
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

    public Calendar getCalendar(JSONObject json) throws ParseException
    {
        Date date = getDate(json);
        if(date == null)
        {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        String timeZone = json.optString(TIME_ZONE);
        if(timeZone != null)
        {
            TimeZone zone = TimeZone.getTimeZone(timeZone);
            calendar.setTimeZone(zone);
        }
        calendar.setTime(date);
        return calendar;
    }
    
    public Date getDate(JSONObject json) throws ParseException
    {
        if(json == null)
        {
            return null;
        }
        String dateTime = json.optString(DATE_TIME);
        if(dateTime == null)
        {
            return null;
        }
        String format = json.optString(FORMAT);
        if(format!= null)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.parse(dateTime);
        }
        return ISO8601DateFormat.parse(dateTime);
    }
}
