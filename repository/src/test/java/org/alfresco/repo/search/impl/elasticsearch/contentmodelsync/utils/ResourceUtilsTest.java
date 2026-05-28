/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
import java.nio.file.Path;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ResourceUtilsTest
{
    private final String CUSTOM_GERMAN_ANALYZER_PATH = "/org/alfresco/repo/search/impl/elasticsearch/contentmodelsync/testCustomGermanAnalyzerSettings.json";
    private final String MALFROMED_JSON_PATH = "/org/alfresco/repo/search/impl/elasticsearch/contentmodelsync/malformed.json";

    @Test
    public void readJSONFromFileReadGermanAnalyzer() throws IOException
    {
        Path path = Path.of(CUSTOM_GERMAN_ANALYZER_PATH);
        JSONObject jsonObject = ResourceUtils.readJSONFromFile(path, getClass());

        Assert.assertTrue(SettingsJsonUtils.keyExists(jsonObject, "locale_content"));
    }

    @Test
    public void readJSONFromResourceReadGermanAnalyzer() throws IOException
    {
        Resource resource = new ClassPathResource(CUSTOM_GERMAN_ANALYZER_PATH);

        JSONObject jsonObject = ResourceUtils.readJSONFromResource(resource);
        Assert.assertTrue(SettingsJsonUtils.keyExists(jsonObject, "locale_content"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readJSONFromResourceFileNotExist() throws IOException
    {
        Path path = Path.of("/path/to/non/existing/file");

        JSONObject jsonObject = ResourceUtils.readJSONFromFile(path, getClass());
        Assert.assertTrue(SettingsJsonUtils.keyExists(jsonObject, "locale_content"));
    }

    @Test(expected = IOException.class)
    public void readJSONFromResourceMalformedJSON() throws IOException
    {
        Resource resource = new ClassPathResource(MALFROMED_JSON_PATH);

        JSONObject jsonObject = ResourceUtils.readJSONFromResource(resource);
        Assert.assertTrue(SettingsJsonUtils.keyExists(jsonObject, "locale_content"));
    }
}
