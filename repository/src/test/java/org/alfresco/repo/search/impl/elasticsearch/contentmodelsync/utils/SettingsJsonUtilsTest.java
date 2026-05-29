/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class SettingsJsonUtilsTest
{

    private JSONObject jsonWithTextIndex = jsonWithTextIndex();
    private JSONObject jsonWithTextQuery = jsonWithTextQuery();
    private JSONObject jsonWithTextQueryAndCharFilter = jsonWithTextQueryAndCharFilter();
    private JSONObject jsonWithTextQueryAndCharFilterAndMoreFilters = jsonWithTextQueryAndCharFilterAndMoreFilters();
    private JSONObject jsonWithLocaleContentEnglish = jsonWithLocaleContentEnglish();
    private JSONObject jsonWithLocaleContentGerman = jsonWithLocaleContentGerman();
    private JSONObject jsonWithLocaleContentPolish = jsonWithLocaleContentPolish();
    private JSONObject emptyJsonObject = new JSONObject();

    @Test
    public void deepMergeOrdinaryCase()
    {
        JSONObject expectedJson = expectedMergedJSONsWithTextAndQuery();

        String result = SettingsJsonUtils.deepMerge(jsonWithTextIndex, jsonWithTextQuery).toString();
        Assert.assertEquals(JsonParser.parseString(expectedJson.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeSourceAndTargetUnchanged()
    {
        SettingsJsonUtils.deepMerge(jsonWithTextIndex, jsonWithTextQuery);

        Assert.assertEquals(JsonParser.parseString(jsonWithTextIndex().toString()), JsonParser.parseString(jsonWithTextIndex.toString()));
        Assert.assertEquals(JsonParser.parseString(jsonWithTextQuery().toString()), JsonParser.parseString(jsonWithTextQuery.toString()));
    }

    @Test
    public void deepMergeFirstJSONIsEmpty()
    {
        String result = SettingsJsonUtils.deepMerge(jsonWithTextIndex, emptyJsonObject).toString();
        Assert.assertEquals(JsonParser.parseString(jsonWithTextIndex.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeSecondJSONIsEmpty()
    {
        String result = SettingsJsonUtils.deepMerge(emptyJsonObject, jsonWithTextIndex).toString();
        Assert.assertEquals(JsonParser.parseString(jsonWithTextIndex.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeTwoEmptyJSONs()
    {
        String result = SettingsJsonUtils.deepMerge(emptyJsonObject, emptyJsonObject).toString();
        Assert.assertEquals(JsonParser.parseString(emptyJsonObject.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeMultipleJSONs()
    {
        JSONObject expectedJson = expectedMergedJSONsWithTextAndQueryAndLocaleContent();

        String result = SettingsJsonUtils.deepMergeMultipleJSONs(
                jsonWithTextQuery, jsonWithLocaleContentEnglish, jsonWithTextIndex).toString();

        Assert.assertEquals(JsonParser.parseString(expectedJson.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeMultipleJSONsOneIsEmpty()
    {
        JSONObject expectedJson = expectedMergedJSONsWithTextAndQuery();

        String result = SettingsJsonUtils.deepMergeMultipleJSONs(
                jsonWithTextQuery, emptyJsonObject, jsonWithTextIndex).toString();

        Assert.assertEquals(JsonParser.parseString(expectedJson.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeMultipleJSONsTwoEmpty()
    {
        JSONObject expectedJson = jsonWithTextIndex();

        String result = SettingsJsonUtils.deepMergeMultipleJSONs(
                emptyJsonObject, emptyJsonObject, jsonWithTextIndex).toString();

        Assert.assertEquals(JsonParser.parseString(expectedJson.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeTwoJSONsTheLastTakesPrecedence()
    {
        JSONObject expectedJson = jsonWithLocaleContentGerman();

        String result = SettingsJsonUtils.deepMerge(
                jsonWithLocaleContentPolish, jsonWithLocaleContentGerman).toString();

        Assert.assertEquals(JsonParser.parseString(expectedJson.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeThreeJSONsTheLastTakesPrecedence()
    {
        JSONObject expectedJson = jsonWithLocaleContentGerman();

        String result = SettingsJsonUtils.deepMergeMultipleJSONs(
                jsonWithLocaleContentPolish, jsonWithLocaleContentEnglish, jsonWithLocaleContentGerman).toString();

        Assert.assertEquals(JsonParser.parseString(expectedJson.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeTwoJSONsKeysNumberDifference()
    {
        JSONObject expectedJson = jsonWithTextQueryAndCharFilter();

        String result = SettingsJsonUtils.deepMerge(
                jsonWithTextQueryAndCharFilter, jsonWithTextQuery).toString();

        Assert.assertEquals(JsonParser.parseString(expectedJson.toString()), JsonParser.parseString(result));
    }

    @Test
    public void deepMergeTwoJSONsDifferenceInArrayElementsWithTheSameKey()
    {
        JSONObject expectedJson = jsonWithTextQueryAndCharFilter();

        String result = SettingsJsonUtils.deepMerge(
                jsonWithTextQueryAndCharFilterAndMoreFilters, jsonWithTextQueryAndCharFilter).toString();

        Assert.assertEquals(JsonParser.parseString(expectedJson.toString()), JsonParser.parseString(result));
    }

    @Test
    public void keyExistsTopLevelKey()
    {
        Assert.assertTrue(SettingsJsonUtils.keyExists(jsonWithLocaleContentGerman, "settings"));
    }

    @Test
    public void keyExistsMidLevelKey()
    {
        Assert.assertTrue(SettingsJsonUtils.keyExists(jsonWithLocaleContentGerman, "analyzer"));
    }

    @Test
    public void keyExistsLowestLevelKey()
    {
        Assert.assertTrue(SettingsJsonUtils.keyExists(jsonWithLocaleContentGerman, "type"));
    }

    @Test
    public void keyDoesNotExist()
    {
        Assert.assertFalse(SettingsJsonUtils.keyExists(jsonWithLocaleContentGerman, "filter"));
    }

    @Test
    public void keyDoesNotExistWhenJsonIsNull()
    {
        Assert.assertFalse(SettingsJsonUtils.keyExists(null, "filter"));
    }

    @Test
    public void keyDoesNotExistWhenJsonIsEmpty()
    {
        Assert.assertFalse(SettingsJsonUtils.keyExists(emptyJsonObject, "filter"));
    }

    private JSONObject expectedMergedJSONsWithTextAndQuery()
    {
        return new JSONObject()
                .put("settings", new JSONObject()
                        .put("analysis", new JSONObject()
                                .put("analyzer", new JSONObject()
                                        .put("locale_cross_text_query", new JSONObject()
                                                .put("tokenizer", "whitespace")
                                                .put("filter", new JSONArray()
                                                        .put("asciifolding")
                                                        .put("custom_word_delimiter_graph")))
                                        .put("locale_cross_text_index", new JSONObject()
                                                .put("tokenizer", "whitespace")
                                                .put("filter", new JSONArray()
                                                        .put("lowercase")
                                                        .put("custom_word_delimiter_graph"))))));
    }

    private JSONObject expectedMergedJSONsWithTextAndQueryAndLocaleContent()
    {
        return new JSONObject()
                .put("settings", new JSONObject()
                        .put("analysis", new JSONObject()
                                .put("analyzer", new JSONObject()
                                        .put("locale_cross_text_query", new JSONObject()
                                                .put("tokenizer", "whitespace")
                                                .put("filter", new JSONArray()
                                                        .put("asciifolding")
                                                        .put("custom_word_delimiter_graph")))
                                        .put("locale_cross_text_index", new JSONObject()
                                                .put("tokenizer", "whitespace")
                                                .put("filter", new JSONArray()
                                                        .put("lowercase")
                                                        .put("custom_word_delimiter_graph")))
                                        .put("locale_content", new JSONObject()
                                                .put("type", "english")))));
    }

    private JSONObject jsonWithTextIndex()
    {
        return new JSONObject()
                .put("settings", new JSONObject()
                        .put("analysis", new JSONObject()
                                .put("analyzer", new JSONObject()
                                        .put("locale_cross_text_index", new JSONObject()
                                                .put("tokenizer", "whitespace")
                                                .put("filter", new JSONArray()
                                                        .put("lowercase")
                                                        .put("custom_word_delimiter_graph"))))));
    }

    private JSONObject jsonWithTextQuery()
    {
        return new JSONObject()
                .put("settings", new JSONObject()
                        .put("analysis", new JSONObject()
                                .put("analyzer", new JSONObject()
                                        .put("locale_cross_text_query", new JSONObject()
                                                .put("tokenizer", "whitespace")
                                                .put("filter", new JSONArray()
                                                        .put("asciifolding")
                                                        .put("custom_word_delimiter_graph"))))));
    }

    private JSONObject jsonWithTextQueryAndCharFilter()
    {
        return new JSONObject()
                .put("settings", new JSONObject()
                        .put("analysis", new JSONObject()
                                .put("analyzer", new JSONObject()
                                        .put("locale_cross_text_query", new JSONObject()
                                                .put("tokenizer", "whitespace")
                                                .put("filter", new JSONArray()
                                                        .put("asciifolding")
                                                        .put("custom_word_delimiter_graph"))
                                                .put("char_filter", new JSONArray()
                                                        .put("html_strip"))))));
    }

    private JSONObject jsonWithTextQueryAndCharFilterAndMoreFilters()
    {
        return new JSONObject()
                .put("settings", new JSONObject()
                        .put("analysis", new JSONObject()
                                .put("analyzer", new JSONObject()
                                        .put("locale_cross_text_query", new JSONObject()
                                                .put("tokenizer", "whitespace")
                                                .put("filter", new JSONArray()
                                                        .put("asciifolding")
                                                        .put("custom_word_delimiter_graph")
                                                        .put("lowercase")
                                                        .put("flatten_graph"))
                                                .put("char_filter", new JSONArray()
                                                        .put("html_strip"))))));
    }

    private JSONObject jsonWithLocaleContentEnglish()
    {
        return new JSONObject()
                .put("settings", new JSONObject()
                        .put("analysis", new JSONObject()
                                .put("analyzer", new JSONObject()
                                        .put("locale_content", new JSONObject()
                                                .put("type", "english")))));
    }

    private JSONObject jsonWithLocaleContentGerman()
    {
        return new JSONObject()
                .put("settings", new JSONObject()
                        .put("analysis", new JSONObject()
                                .put("analyzer", new JSONObject()
                                        .put("locale_content", new JSONObject()
                                                .put("type", "german")))));
    }

    private JSONObject jsonWithLocaleContentPolish()
    {
        return new JSONObject()
                .put("settings", new JSONObject()
                        .put("analysis", new JSONObject()
                                .put("analyzer", new JSONObject()
                                        .put("locale_content", new JSONObject()
                                                .put("type", "polish")))));
    }
}
