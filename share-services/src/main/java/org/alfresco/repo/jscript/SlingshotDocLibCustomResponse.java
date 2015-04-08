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
package org.alfresco.repo.jscript;

import org.alfresco.repo.jscript.app.CustomResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

/**
 * Populates DocLib webscript response with custom metadata output
 *
 * @author: mikeh
 */
public final class SlingshotDocLibCustomResponse extends BaseScopableProcessorExtension
{
    private Map<String, Object> customResponses;

    /**
     * Set the custom response beans
     *
     * @param customResponses
     */
    public void setCustomResponses(Map<String, Object> customResponses)
    {
        this.customResponses = customResponses;
    }

    /**
     * Returns a JSON string to be added to the DocLib webscript response.
     *
     * @return The JSON string
     */
    public String getJSON()
    {
        return this.getJSONObj().toString();
    }

    /**
     * Returns a JSON object to be added to the DocLib webscript response.
     *
     * @return The JSON object
     */
    protected Object getJSONObj()
    {
        JSONObject json = new JSONObject();


        for (Map.Entry<String, Object> entry : this.customResponses.entrySet())
        {
            try
            {
                Serializable response = ((CustomResponse) entry.getValue()).populate();
                json.put(entry.getKey(), response == null ? JSONObject.NULL: response);
            }
            catch (JSONException error)
            {
                error.printStackTrace();
            }
        }

        return json;
    }
}
