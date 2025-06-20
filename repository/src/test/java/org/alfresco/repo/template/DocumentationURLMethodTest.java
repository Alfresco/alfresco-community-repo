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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import org.junit.Before;
import org.junit.Test;

public class DocumentationURLMethodTest
{

    public static final String BASE_URL = "baseUrl";
    public static final String ACS_VERSION = "_acs_";
    public static final String SEARCH_VERSION = "_solr_";
    public static final String SEARCH_ENTERPRISE_VERSION = "_es_";
    public static final String DEFAULT_DOCUMENTATION_URL = "defaultDocumentationUrl";
    DocumentationURLMethod documentationURLMethod;

    @Before
    public void setUp() throws Exception
    {
        documentationURLMethod = new DocumentationURLMethod();
        documentationURLMethod.setAcsVersion(ACS_VERSION);
        documentationURLMethod.setAlfrescoSearchVersion(SEARCH_VERSION);
        documentationURLMethod.setAlfrescoSearchEnterpriseVersion(SEARCH_ENTERPRISE_VERSION);
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
                new SimpleScalar("")));
        assertEquals("defaultDocumentationUrl", result);
    }

    @Test
    public void testGetDocumentationUrl_WithTopicUid_AcsVersion() throws TemplateModelException
    {
        Object result = documentationURLMethod.exec(Collections.singletonList(new SimpleScalar("/topic")));
        assertEquals(BASE_URL + "/topic" + ACS_VERSION, result);
    }

    @Test
    public void testGetDocumentationUrl_WithTopicUidAndUrlComponent_AcsVersion() throws TemplateModelException
    {
        Object result = documentationURLMethod.exec(Arrays.asList(
                new SimpleScalar("/topic"),
                new SimpleScalar("urlComponent")));
        assertEquals(BASE_URL + "/topic" + ACS_VERSION + "urlComponent", result);
    }

    @Test
    public void testGetDocumentationUrl_WithSolrVersion() throws TemplateModelException
    {
        Object result = documentationURLMethod.exec(Arrays.asList(
                new SimpleScalar("/topic"),
                new SimpleScalar("urlComponent"),
                new SimpleScalar("solr")));
        assertEquals(BASE_URL + "/topic" + SEARCH_VERSION + "urlComponent", result);
    }

    @Test
    public void testGetDocumentationUrl_WithElasticSearchVersion() throws TemplateModelException
    {
        Object result = documentationURLMethod.exec(Arrays.asList(
                new SimpleScalar("/topic"),
                new SimpleScalar("urlComponent"),
                new SimpleScalar("elasticsearch")));
        assertEquals(BASE_URL + "/topic" + SEARCH_ENTERPRISE_VERSION + "urlComponent", result);
    }

}
