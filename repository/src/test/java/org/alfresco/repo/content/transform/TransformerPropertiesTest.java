/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Test class for TransformerProperties. Currently just that The old oooDirect (OpenOffice) transformer settings
 * are aliased to JodConverter settings if they are not set up.
 *
 * @author Alan Davis
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class TransformerPropertiesTest
{
    @Mock
    private ChildApplicationContextFactory subsystem;

    private TransformerProperties transformerProperties;
    private Properties globalProperties;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        globalProperties = new Properties();
        transformerProperties = new TransformerProperties(subsystem, globalProperties);
    }

    @Test
    public void aliasOpenOfficeToJodTest()
    {
        when(subsystem.getProperty(Matchers.any())).thenReturn(null);

        // Should be picked up as normal
        globalProperties.setProperty("content.transformer.complex.JodConverter.Image.priority", "150");

        // JodConverter value should override the OpenOffice value
        globalProperties.setProperty("content.transformer.complex.OpenOffice.Image.extensions.docx.txt.supported", "true");
        globalProperties.setProperty("content.transformer.complex.JodConverter.Image.extensions.docx.txt.supported", "false");

        // Should be picked up as a JodConverter value
        globalProperties.setProperty("content.transformer.complex.OpenOffice.Image.extensions.xlsb.txt.maxSourceSizeKBytes", "1024");

        Set<String> propertyNames = transformerProperties.getPropertyNames();

        assertEquals("The combined settings should be seen as only three entries", 3, propertyNames.size());
        assertTrue(propertyNames.contains("content.transformer.complex.JodConverter.Image.priority"));
        assertTrue(propertyNames.contains("content.transformer.complex.JodConverter.Image.extensions.docx.txt.supported"));
        assertTrue(propertyNames.contains("content.transformer.complex.JodConverter.Image.extensions.xlsb.txt.maxSourceSizeKBytes"));

        assertEquals("150",   transformerProperties.getProperty("content.transformer.complex.JodConverter.Image.priority"));
        assertEquals("false", transformerProperties.getProperty("content.transformer.complex.JodConverter.Image.extensions.docx.txt.supported"));
        assertEquals("1024",  transformerProperties.getProperty("content.transformer.complex.JodConverter.Image.extensions.xlsb.txt.maxSourceSizeKBytes"));
    }
}
