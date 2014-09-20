/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.search.impl.solr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SpellCheckDecisionManager
{
    private static final Log logger = LogFactory.getLog(SpellCheckDecisionManager.class);

    private static final String COLLATION = "collation";
    private boolean collate;
    private String url;
    private JSONObject spellCheckJsonValue;

    public SpellCheckDecisionManager(JSONObject resultJson, String origURL, JSONObject reguestJsonBody,
                String spellCheckParams)
    {
        try
        {
            List<String> collationQueriesList = new ArrayList<>();
            JSONObject response = resultJson.getJSONObject("response");
            long numberFound = response.getLong("numFound");
            this.url = origURL;

            JSONObject spellcheck = resultJson.getJSONObject("spellcheck");
            JSONArray suggestions = spellcheck.getJSONArray("suggestions");

            for (int key = 0, value = 1, length = suggestions.length(); value < length; key += 2, value += 2)
            {
                String jsonName = suggestions.getString(key);

                if (COLLATION.equals(jsonName))
                {
                    JSONObject valueJsonObject = suggestions.getJSONObject(value);
                    long collationHit = valueJsonObject.getLong("hits");
                    this.collate = numberFound == 0 && collationHit > 0;
                    if (collate)
                    {
                        reguestJsonBody.put("query", valueJsonObject.getString("collationQuery"));
                        spellCheckJsonValue = new JSONObject();
                        spellCheckJsonValue.put("searchInsteadFor", valueJsonObject.getString("collationQueryString"));
                        break;
                    }
                    else if (collationHit > numberFound)
                    {
                        collationQueriesList.add(valueJsonObject.getString("collationQueryString"));

                    }
                }
            }
            if (collate)
            {
                this.url = origURL.replace(spellCheckParams, "");
            }
            else if (collationQueriesList.size() > 0)
            {
                spellCheckJsonValue = new JSONObject();
                JSONArray jsonArray = new JSONArray(collationQueriesList);
                spellCheckJsonValue.put("didYouMean", jsonArray);
            }
            else
            {
                spellCheckJsonValue = new JSONObject();
            }
        }
        catch (Exception e)
        {
            logger.info(e.getMessage());
        }
    }

    /**
     * @return the collate
     */
    public boolean isCollate()
    {
        return this.collate;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return this.url;
    }

    /**
     * @return the spellCheckJsonValue
     */
    public JSONObject getSpellCheckJsonValue()
    {
        return this.spellCheckJsonValue;
    }
}
