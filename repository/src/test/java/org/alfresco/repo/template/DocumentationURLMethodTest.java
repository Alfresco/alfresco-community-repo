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

import org.junit.Before;
import org.junit.Test;
import static org.alfresco.repo.template.DocumentationURLMethod.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentationURLMethodTest
{
    public static final String BASE_URL = "baseUrl";
    public static final String VERSION = "majestic_unicorn";
    DocumentationURLMethod documentationURLMethod;

    @Before
    public void setUp() throws Exception
    {
        documentationURLMethod = new DocumentationURLMethod();
        documentationURLMethod.setEdition(COMMUNITY_EDITION);
        documentationURLMethod.setVersion(VERSION);
        documentationURLMethod.setDocumentationBaseUrl(BASE_URL);
    }

    @Test
    public void testGetDocumentationUrl_shouldReturnBaseUrl()
    {
        assertThat(documentationURLMethod.getDocumentationUrl()).startsWith(BASE_URL);
    }

    @Test
    public void testGetDocumentationUrl_whenCommunityEditionIsSet_shouldReturnCommunityBaseUrl()
    {
        documentationURLMethod.setEdition(COMMUNITY_EDITION);

        String actual = documentationURLMethod.getDocumentationUrl();

        assertThat(actual).isEqualTo(BASE_URL + COMMUNITY_EDITION_PATH + "/" + VERSION + COMMUNITY_EDITION_PATH);
    }

    @Test
    public void testGetDocumentationUrl_whenEnterpriseEditionIsSet_shouldReturnEnterpriseBaseUrl() {
        documentationURLMethod.setEdition(ENTERPRISE_EDITION);

        String actual = documentationURLMethod.getDocumentationUrl();

        assertThat(actual).isEqualTo(BASE_URL + ENTERPRISE_EDITION_PATH + "/" + VERSION + ENTERPRISE_EDITION_PATH);
    }

    @Test
    public void testGetDocumentationUrl_whenGivenNullArgument_shouldReturnBaseUrl() {
        String actual = documentationURLMethod.getDocumentationUrl(null);

        assertThat(actual).isEqualTo(BASE_URL + COMMUNITY_EDITION_PATH + "/" + VERSION + COMMUNITY_EDITION_PATH);
    }

    @Test
    public void testGetDocumentationUrl_whenUnknownEditionIsSet_shouldReturnBaseUrl() {
        documentationURLMethod.setEdition("director's cut");

        String actual = documentationURLMethod.getDocumentationUrl();

        assertThat(actual).isEqualTo(BASE_URL);
    }

    @Test
    public void testGetDocumentationUrl_whenGivenPath_shouldReturnUrlToThatPath() {
        String actual = documentationURLMethod.getDocumentationUrl("/road/to/rome");

        assertThat(actual).isEqualTo(BASE_URL + COMMUNITY_EDITION_PATH + "/" + VERSION + COMMUNITY_EDITION_PATH + "/road/to/rome");
    }
}
