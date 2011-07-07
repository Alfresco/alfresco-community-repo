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

package org.alfresco.repo.web.scripts;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class WebScriptUtil
{
    // General Keys
    public static final String DATA_KEY = "data";

    // NodeRef Keys
    public static final String STORE_PROTOCOL = "store_protocol";
    public static final String STORE_ID = "store_id";
    public static final String NODE_ID = "node_id";

    //Date/Calendar Keys
    public static final String DATE_TIME= "dateTime";
    public static final String FORMAT= "format";
    public static final String TIME_ZONE= "timeZone";
    public static final String ISO8601 = "ISO8601";

    public static String getContent(WebScriptRequest request) throws IOException
    {
        Content content = request.getContent();
        return content.getContent();
    }

    public static Map<String, Object> buildCalendarModel(Calendar calendar)
    {
        Map<String, Object> model = buildDateModel(calendar.getTime());
        model.put(TIME_ZONE, calendar.getTimeZone().getID());
        return model;
    }

    public static Map<String, Object> buildDateModel(Date dateTime)
    {
        String dateStr = ISO8601DateFormat.format(dateTime);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(DATE_TIME, dateStr);
        model.put(FORMAT, ISO8601);
        return model;
    }
    
    public static Calendar getCalendar(JSONObject json) throws ParseException
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
    
    public static Date getDate(JSONObject json) throws ParseException
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
        if(format!= null && ISO8601.equals(format) == false)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.parse(dateTime);
        }
        return ISO8601DateFormat.parse(dateTime);
    }
    

    public static Map<String, Object> createBaseModel(Map<String, Object> result)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(DATA_KEY, result);
        return model;
    }
    
    public static Map<String, Object> createBaseModel(List<Map<String, Object>> results)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(DATA_KEY, results);
        return model;
    }
    
    public static NodeRef getNodeRef(Map<String, String> params)
    {
        String protocol = params.get(STORE_PROTOCOL);
        String storeId= params.get(STORE_ID);
        String nodeId= params.get(NODE_ID);
        if(protocol == null || storeId == null || nodeId==null )
        {
            return null;
        }
        return new NodeRef(protocol, storeId, nodeId);
    }


}
