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
package org.alfresco.repo.template;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class DocumentationURLMethodTest {

    public static final String BASE_URL = "baseUrl";
    public static final String VERSION = "majestic_unicorn";
    public static final String DEFAULT_DOCUMENTATION_URL = "defaultDocumentationUrl";
    DocumentationURLMethod documentationURLMethod;

    @Before
    public void setUp() throws Exception
    {
        documentationURLMethod = new DocumentationURLMethod();
        documentationURLMethod.setVersion(VERSION);
        documentationURLMethod.setDocumentationBaseUrl(BASE_URL);
        documentationURLMethod.setDefaultDocumentationUrl(DEFAULT_DOCUMENTATION_URL);
    }

    @Test
    public void testGetDocumentationUrl_NoArgs() throws TemplateModelException
    {
        Object result = documentationURLMethod.exec(Collections.emptyList());
        assertEquals("defaultDocumentationUrl", result);
    }

    @Test
    public void testGetDocumentationUrl_EmptyTopicUidAndUrlComponent() throws TemplateModelException
    {
        Object result = documentationURLMethod.exec(Arrays.asList(
                new SimpleScalar(""),
                new SimpleScalar("")
        ));
        assertEquals("defaultDocumentationUrl", result);
    }

    @Test
    public void testGetDocumentationUrl_WithTopicUid() throws TemplateModelException
    {
        Object result = documentationURLMethod.exec(Collections.singletonList(new SimpleScalar("/topic")));
        assertEquals(BASE_URL + "/topic" + VERSION, result);
    }

    @Test
    public void testGetDocumentationUrl_WithTopicUidAndUrlComponent() throws TemplateModelException
    {
        Object result = documentationURLMethod.exec(Arrays.asList(
                new SimpleScalar("/topic"),
                new SimpleScalar("urlComponent")
        ));
        assertEquals(BASE_URL + "/topic" + VERSION + "urlComponent", result);
    }

    @Test
    public void testGetDocumentationUrl_WithAllArgsAndPropertyValue() throws TemplateModelException
    {
        Object result = documentationURLMethod.exec(Arrays.asList(
                new SimpleScalar("/topic"),
                new SimpleScalar("urlComponent"),
                new SimpleScalar("See docs at {0}")
        ));
        assertEquals("See docs at " + BASE_URL + "/topic" + VERSION + "urlComponent", result);
    }
}
