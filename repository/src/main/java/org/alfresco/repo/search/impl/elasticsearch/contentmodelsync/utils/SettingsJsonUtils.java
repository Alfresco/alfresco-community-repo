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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.json.JSONObject;

public class SettingsJsonUtils
{

    /**
     * Simple check if the key exists, no matter on which level
     * 
     * @param jsonWithAnalyzersSettings
     * @param mandatoryAttributes
     * @throws IOException
     */
    public static void validate(JSONObject jsonWithAnalyzersSettings, List<String> mandatoryAttributes) throws IOException
    {
        for (String mandatoryAttribute : mandatoryAttributes)
        {
            if (!keyExists(jsonWithAnalyzersSettings, mandatoryAttribute))
            {
                throw new IOException("Analyzer settings not configured properly. Missing attribute: " + mandatoryAttribute);
            }
        }
    }

    public static boolean keyExists(JSONObject jsonToSearch, String searchedKey)
    {
        if (jsonToSearch == null)
            return false;

        Queue<JSONObject> jsonObjectsToTraverse = new LinkedList<>();
        jsonObjectsToTraverse.add(jsonToSearch);

        while (!jsonObjectsToTraverse.isEmpty())
        {
            JSONObject jsonObject = jsonObjectsToTraverse.poll();
            if (jsonObject.has(searchedKey))
            {
                return true;
            }
            else
            {
                for (String key : jsonObject.keySet())
                {
                    if (jsonObject.get(key) instanceof JSONObject)
                    {
                        jsonObjectsToTraverse.add(jsonObject.getJSONObject(key));
                    }
                }
            }
        }

        return false;
    }

    /**
     * The last argument takes precedence according to the rules mentioned in the deepMerge(JSONObject source, JSONObject target) method.
     * 
     * @param toMerge
     * @return
     */
    public static JSONObject deepMergeMultipleJSONs(JSONObject... toMerge)
    {
        return Arrays.stream(toMerge).reduce(new JSONObject(), SettingsJsonUtils::deepMerge);
    }

    /**
     * Performs a deep merge of JSONObjects, source and target JSONObjects are not modified in the result. If a key already exists in the target, it won't be replaced with a key from the source. If a key already exists, but has different elements number, values will be merged from both maps, for example: A: { keys: { key1:value1, key2:value2 } }
     *
     * B: { keys: { key2:value3 key3:value3 } }
     *
     * Result of deepMerge(A,B) would be: { keys: { key1:value1, key2:value3, key3:value3 } }
     *
     * The only exceptions are JSONArrays. JSONArrays with the same key won't be replaced or merged if there is an element number or value mismatch (target still takes precedence) For Example: A: { keys: [ value1, value2 ] }
     *
     * B: { keys: [ value3, value4 ] }
     *
     * Result of deepMerge(A,B) would be: { keys: [ value3, value4 ] }
     *
     * @param firstMergeObject
     * @param secondMergeObject
     * @return new JSONObject as a result of merging source and target JSONs
     */

    static JSONObject deepMerge(JSONObject firstMergeObject, JSONObject secondMergeObject)
    {
        JSONObject result = new JSONObject(firstMergeObject.toString()); // TODO deep copy through parsing, probably there's a ton better way to do this
        Queue<MergeReferences> queue = new LinkedList<>();

        MergeReferences rootMergeReferences = new MergeReferences(result, secondMergeObject);
        queue.addAll(merge(rootMergeReferences));
        while (!queue.isEmpty())
        {
            queue.addAll(merge(queue.poll()));
        }

        return result;
    }

    private static List<MergeReferences> merge(MergeReferences mergeReferences)
    {
        List<MergeReferences> newMergeReferences = new ArrayList<>();

        JSONObject target = mergeReferences.targetReference;
        JSONObject toMerge = mergeReferences.toMergeReference;

        Set<String> targetKeySet = target.keySet();
        Iterator<String> toMergeKeys = toMerge.keys();

        while (toMergeKeys.hasNext())
        {
            String toMergeKey = toMergeKeys.next();

            if (targetKeySet.contains(toMergeKey)
                    && target.get(toMergeKey) instanceof JSONObject
                    && toMerge.get(toMergeKey) instanceof JSONObject)
            {
                newMergeReferences.add(new MergeReferences(target.getJSONObject(toMergeKey), toMerge.getJSONObject(toMergeKey)));
            }
            else
            {
                // TODO possibly a check is needed for when one of them is a JSONObject, while the other isn't, for once in life a xor could be used :D
                target.put(toMergeKey, toMerge.get(toMergeKey));
            }
        }

        return newMergeReferences;
    }

    private static class MergeReferences
    {
        private JSONObject targetReference;
        private JSONObject toMergeReference;

        public MergeReferences(JSONObject targetReference, JSONObject toMergeReference)
        {
            this.targetReference = targetReference;
            this.toMergeReference = toMergeReference;
        }
    }
}
