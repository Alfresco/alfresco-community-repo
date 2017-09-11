/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.heartbeat;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by mmuller on 07/07/2017.
 */
public class HBData
{
    public static String SCHEMA_VERSION = "1.0";

    public static String SCHEMA_VERSION_KEY = "sv";
    public static String SYSTEM_ID_KEY = "sId";
    public static String COLLECTOR_ID_KEY = "cId";
    public static String COLLECTOR_VERSION_KEY = "cv";
    public static String TIMESTAMP_KEY = "t";
    public static String DATA_KEY = "d";

    private String systemId;
    private String collectorId;
    private String collectorVersion;
    private String timestamp;
    private Map<String, Object> data;

    public HBData(String systemId, String collectorId, String collectorVersion, String timestamp)
    {
        this.systemId = systemId;
        this.collectorId = collectorId;
        this.collectorVersion = collectorVersion;
        this.timestamp = timestamp;
    }

    public HBData(String systemId, String collectorId, String collectorVersion, String timestamp, Map<String, Object> data)
    {
        this.systemId = systemId;
        this.collectorId = collectorId;
        this.collectorVersion = collectorVersion;
        this.timestamp = timestamp;
        this.data = data;
    }

    public JSONObject getJSONObject() throws JSONException
    {
        // save data collector properties
        // The LinkedHashMap was choose only because for demoing
        Map<String, String> keyValues = new LinkedHashMap<String, String>();
        keyValues.put(SCHEMA_VERSION_KEY, SCHEMA_VERSION);
        keyValues.put(SYSTEM_ID_KEY, this.systemId);
        keyValues.put(COLLECTOR_ID_KEY, this.collectorId);
        keyValues.put(COLLECTOR_VERSION_KEY, this.collectorVersion);
        keyValues.put(TIMESTAMP_KEY, this.timestamp);

        // save collected data
        JSONObject jsonObject = new JSONObject(keyValues);
        jsonObject.put(DATA_KEY, new JSONObject(this.data));
        return jsonObject;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public void setSystemId(String systemId)
    {
        this.systemId = systemId;
    }

    public String getCollectorId()
    {
        return collectorId;
    }

    public void setCollectorId(String collectorId)
    {
        this.collectorId = collectorId;
    }

    public String getCollectorVersion()
    {
        return collectorVersion;
    }

    public void setCollectorVersion(String collectorVersion)
    {
        this.collectorVersion = collectorVersion;
    }

    public String getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(String timestamp)
    {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getData()
    {
        return data;
    }

    public void setData(Map<String, Object> data)
    {
        this.data = data;
    }

    public static void main(String[] args) throws JSONException
    {
        Map<String, Object> data = new TreeMap<String, Object>();
        data.put("k1","v1");
        data.put("k2",new Integer(2));
        data.put("k3","v3");

        // ISO-8601 same as JavaScript toISOString()
        String timeStamp = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.sss'Z'").format(new Date());

        HBData collectorData = new HBData("99db325c-13c4-4b74-8d1d-f4800b345c89",
                "acs.repository.test",
                "1.0",
                timeStamp);

        collectorData.setData(data);

        String jsonString = collectorData.getJSONObject().toString();
        System.out.println(":O ");
        System.out.println(jsonString);
    }

}
