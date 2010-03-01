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
package org.alfresco.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Utility to convert JSON to Freemarker-compatible data model
 * 
 * @author janv
 */
public final class JSONtoFmModel
{
    public static String ROOT_ARRAY = "root";
    
    // note: current format is dependent on ISO8601DateFormat.parser, eg. YYYY-MM-DDThh:mm:ss.sssTZD
    private static String REGEXP_ISO8061 = "^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})(.([0-9]){3})?(Z|[\\+\\-]([0-9]{2}):([0-9]{2}))$";
    private static Pattern matcherISO8601 = Pattern.compile(REGEXP_ISO8061);
    
    public static boolean autoConvertISO8601 = true;
    
    /**
     * Convert JSON Object string to Freemarker-compatible data model
     * 
     * @param jsonString
     * @return model
     * @throws JSONException
     */
    public static Map<String, Object> convertJSONObjectToMap(String jsonString) throws JSONException
    {
        JSONObject jo = new JSONObject(new JSONTokener(jsonString));
        return convertJSONObjectToMap(jo);
    }
    
    /**
     * JSONObject is an unordered collection of name/value pairs -> convert to Map (equivalent to Freemarker "hash")
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertJSONObjectToMap(JSONObject jo) throws JSONException
    {
        Map<String, Object> model = new HashMap<String, Object>();
        
        Iterator<String> itr = (Iterator<String>)jo.keys();
        while (itr.hasNext())
        {
            String key = (String)itr.next();
            
            Object o = jo.get(key);
            if (o instanceof JSONObject)
            {
                model.put(key, convertJSONObjectToMap((JSONObject)o));
            }
            else if (o instanceof JSONArray)
            {
                model.put(key, convertJSONArrayToList((JSONArray)o));
            }
            else if (o == JSONObject.NULL)
            {
                model.put(key, null); // note: http://freemarker.org/docs/dgui_template_exp.html#dgui_template_exp_missing
            }
            else
            {
                if ((o instanceof String) && autoConvertISO8601 && (matcherISO8601.matcher((String)o).matches()))
                {
                    o = ISO8601DateFormat.parse((String)o);
                }
                
                model.put(key, o);
            }
        }
       
        return model;
    }
   
    /**
     * Convert JSON Array string to Freemarker-compatible data model
     * 
     * @param jsonString
     * @return model
     * @throws JSONException
     */
    public static Map<String, Object> convertJSONArrayToMap(String jsonString) throws JSONException
    {
        Map<String, Object> model = new HashMap<String, Object>();
        JSONArray ja = new JSONArray(new JSONTokener(jsonString));
        model.put(ROOT_ARRAY, convertJSONArrayToList(ja));
        return model;
    }
    
    /**
     * JSONArray is an ordered sequence of values -> convert to List (equivalent to Freemarker "sequence")
     */
    public static List<Object> convertJSONArrayToList(JSONArray ja) throws JSONException
    {
        List<Object> model = new ArrayList<Object>();
       
        for (int i = 0; i < ja.length(); i++)
        {
            Object o = ja.get(i);
            
            if (o instanceof JSONArray)
            {
                model.add(convertJSONArrayToList((JSONArray)o));
            }
            else if (o instanceof JSONObject)
            {
                model.add(convertJSONObjectToMap((JSONObject)o));
            }
            else if (o == JSONObject.NULL)
            {
                model.add(null);
            }
            else
            {
                if ((o instanceof String) && autoConvertISO8601 && (matcherISO8601.matcher((String)o).matches()))
                {
                    o = ISO8601DateFormat.parse((String)o);
                }
                
                model.add(o);
            }
        }
       
        return model;
    }
   
    // for debugging only
    public static String toString(Map<String, Object> map)
    {
        return JSONtoFmModel.toStringBuffer(map, 0).toString();
    }
    
    @SuppressWarnings("unchecked")
    private static StringBuffer toStringBuffer(Map<String, Object> unsortedMap, int indent)
    {      
        StringBuffer tabs = new StringBuffer();
        for (int i = 0; i < indent; i++)
        {
            tabs.append("\t");
        }
        
        StringBuffer sb = new StringBuffer();
        
        SortedMap<String, Object> map = new TreeMap<String, Object>();
        map.putAll(unsortedMap);
        
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if (entry.getValue() instanceof Map)
            {
                sb.append(tabs).append(entry.getKey()).append(":").append(entry.getValue().getClass()).append("\n");
                sb.append(JSONtoFmModel.toStringBuffer((Map<String, Object>)entry.getValue(), indent+1));
            }
            else if (entry.getValue() instanceof List)
            {
                sb.append(tabs).append("[\n");
                List l = (List)entry.getValue();
                for (int i = 0; i < l.size(); i++)
                {
                    sb.append(tabs).append(l.get(i)).append(":").append((l.get(i) != null) ? l.get(i).getClass() : "null").append("\n");
                }
                sb.append(tabs).append("]\n");
            }
            else
            {
                sb.append(tabs).append(entry.getKey()).append(":").append(entry.getValue()).append(":").append((entry.getValue() != null ? entry.getValue().getClass() : "null")).append("\n");         
            }
        }
        
        return sb;
    }
}
