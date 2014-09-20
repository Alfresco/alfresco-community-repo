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

package org.alfresco.repo.search.impl.lucene;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.service.cmr.search.SuggesterResult;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SolrSuggesterResult implements SuggesterResult
{
    private static final Log logger = LogFactory.getLog(SolrSuggesterResult.class);

    private Long numberFound;
    private List<Pair<String, Integer>> suggestions = new ArrayList<>();

    public SolrSuggesterResult()
    {
    }

    public SolrSuggesterResult(JSONObject jsonObject)
    {
        try
        {
            processJson(jsonObject);
        }
        catch (Exception e)
        {
            logger.info(e.getMessage());
        }
    }

    /**
     * Parses the json returned from the suggester
     * 
     * @param json the JSON object
     * @throws JSONException
     */
    @SuppressWarnings("rawtypes")
    protected void processJson(JSONObject json) throws JSONException
    {
        ParameterCheck.mandatory("json", json);

        if (logger.isDebugEnabled())
        {
            logger.debug("Suggester JSON response: " + json);
        }

        JSONObject suggest = json.getJSONObject("suggest");
        for (Iterator suggestIterator = suggest.keys(); suggestIterator.hasNext(); /**/)
        {
            String dictionary = (String) suggestIterator.next();

            JSONObject dictionaryJsonObject = suggest.getJSONObject(dictionary);
            for (Iterator dicIterator = dictionaryJsonObject.keys(); dicIterator.hasNext(); /**/)
            {
                String termStr = (String) dicIterator.next();

                JSONObject termJsonObject = dictionaryJsonObject.getJSONObject(termStr);
                // number found
                this.numberFound = termJsonObject.getLong("numFound");

                // the suggested terms
                JSONArray suggestion = termJsonObject.getJSONArray("suggestions");
                for (int i = 0, length = suggestion.length(); i < length; i++)
                {
                    JSONObject data = suggestion.getJSONObject(i);
                    this.suggestions.add(new Pair<String, Integer>(data.getString("term"), data.getInt("weight")));
                }
            }
        }
    }

    @Override
    public long getNumberFound()
    {
        return this.numberFound;
    }

    @Override
    public List<Pair<String, Integer>> getSuggestions()
    {
        return this.suggestions;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(250);
        builder.append("SolrSuggesterResult [numberFound=").append(this.numberFound).append(", suggestions=")
                    .append(this.suggestions).append("]");
        return builder.toString();
    }
}
