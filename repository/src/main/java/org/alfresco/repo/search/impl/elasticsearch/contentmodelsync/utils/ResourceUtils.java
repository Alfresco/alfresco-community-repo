/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.core.io.Resource;

public class ResourceUtils
{
    /**
     * Class argument needed for getResourceAsStream() method (this.getClass() cannot be used from static context)
     * 
     * @param path
     * @param clazz
     * @return
     * @throws IOException
     */
    public static JSONObject readJSONFromFile(Path path, Class clazz) throws IOException
    {
        return readJSONFromFile(path.toString(), clazz);
    }

    public static JSONObject readJSONFromFile(String path, Class clazz) throws IOException
    {
        try (InputStream is = clazz.getResourceAsStream(path))
        {
            if (is == null)
            {
                throw new IllegalArgumentException("Failed to load a file from path: " + path);
            }
            String jsonString = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new JSONObject(jsonString);
        }
        catch (IOException e)
        {
            throw new IOException("Failed to load a JSON from path: " + path, e);
        }
    }

    public static JSONObject[] readJSONsFromResources(Resource[] resources) throws IOException
    {
        List<JSONObject> mergedJSONs = new ArrayList<>();

        if (resources == null)
            return mergedJSONs.toArray(JSONObject[]::new);

        for (Resource resource : resources)
        {
            // check for readability, because empty property, for example 'elasticsearch.index.custom.analyzer.config.files='
            // is treated as a path of "" and is a file and is existing in some Resource implementations
            if (resource.isReadable())
            {
                mergedJSONs.add(readJSONFromResource(resource));
            }
        }
        return mergedJSONs.toArray(JSONObject[]::new);
    }

    public static JSONObject readJSONFromResource(Resource resource) throws IOException
    {
        try (InputStream is = resource.getInputStream())
        {
            String jsonString = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new JSONObject(jsonString);
        }
        catch (IOException e)
        {
            throw new IOException("Failed to load a JSON from the resource file: " + resource.getFilename(), e);
        }
    }
}
