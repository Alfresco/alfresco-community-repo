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
package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IndexConfigurationInitializerIntegrationTest
{
    private IndexConfigurationInitializer indexConfigurationInitializer;

    @Before
    public void setUp() throws Exception
    {
        indexConfigurationInitializer = new IndexConfigurationInitializer();
    }

    @Test
    public void testLoadConfigurationFromFileOnce() throws IOException
    {
        try (InputStream basicFieldsInputStream = indexConfigurationInitializer.loadConfigurationAsInputStream())
        {
            assertNotNull(basicFieldsInputStream);
        }
    }

    @Test
    public void testLoadConfigurationFromFileTwice() throws IOException
    {
        try (InputStream basicFieldsInputStream1 = indexConfigurationInitializer.loadConfigurationAsInputStream();
                InputStream basicFieldsInputStream2 = indexConfigurationInitializer.loadConfigurationAsInputStream())
        {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> config1 = mapper.readValue(basicFieldsInputStream1, new TypeReference<>() {});
            Map<String, Object> config2 = mapper.readValue(basicFieldsInputStream2, new TypeReference<>() {});
            assertNotNull(config1);
            assertEquals(config1, config2);
        }
    }

    @Test
    public void testPropertyIsIndexed()
    {
        boolean isPropertyIndexed = indexConfigurationInitializer.isPropertyIndexed("ALIVE");
        assertTrue(isPropertyIndexed);
    }

    @Test
    public void testPropertyIsNotIndexed()
    {
        boolean isPropertyIndexed = indexConfigurationInitializer.isPropertyIndexed("FAKE_PROPERTY");
        assertFalse(isPropertyIndexed);
    }

    @Test
    public void testPropertyIsTokenized()
    {
        boolean isPropertyTokenized = indexConfigurationInitializer.isPropertyTokenized("ALIVE");
        assertTrue(isPropertyTokenized);
    }

    @Test
    public void testPropertyIsNotTokenized()
    {
        boolean isPropertyTokenized = indexConfigurationInitializer.isPropertyTokenized("SITE");
        assertFalse(isPropertyTokenized);
    }

}
