/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.rest.api.tests.util;

import static org.junit.Assert.assertNotNull;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class JacksonUtil
{
    private JacksonHelper jsonHelper;

    public JacksonUtil(JacksonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public <T> List<T> parseEntries(JSONObject jsonObject, Class<T> clazz) throws IOException
    {
        assertNotNull(jsonObject);
        assertNotNull(clazz);

        List<T> models = new ArrayList<>();

        JSONObject jsonList = (JSONObject) jsonObject.get("list");
        assertNotNull(jsonList);

        JSONArray jsonEntries = (JSONArray) jsonList.get("entries");
        assertNotNull(jsonEntries);

        for (Object entry : jsonEntries)
        {
            JSONObject jsonEntry = (JSONObject) entry;
            T pojoModel = parseEntry(jsonEntry, clazz);
            models.add(pojoModel);
        }

        return models;
    }

    public <T> T parseEntry(JSONObject jsonObject, Class<T> clazz) throws IOException
    {
        assertNotNull(jsonObject);
        assertNotNull(clazz);

        JSONObject entry = (JSONObject) jsonObject.get("entry");
        T pojoModel = jsonHelper.construct(new StringReader(entry.toJSONString()), clazz);
        assertNotNull(pojoModel);

        return pojoModel;
    }
}
