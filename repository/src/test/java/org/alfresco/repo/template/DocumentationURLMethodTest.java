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
    public void getDocumentationUrl_shouldReturnBaseUrl()
    {
        assertThat(documentationURLMethod.getDocumentationUrl()).startsWith(BASE_URL);
    }

    @Test
    public void getDocumentationUrl_whenCommunityEditionIsSet_shouldReturnCommunityBaseUrl()
    {
        documentationURLMethod.setEdition(COMMUNITY_EDITION);

        String actual = documentationURLMethod.getDocumentationUrl();

        assertThat(actual).isEqualTo(BASE_URL + COMMUNITY_EDITION_PATH + "/" + VERSION + COMMUNITY_EDITION_PATH);
    }

    @Test
    public void getDocumentationUrl_whenEnterpriseEditionIsSet_shouldReturnEnterpriseBaseUrl() {
        documentationURLMethod.setEdition(ENTERPRISE_EDITION);

        String actual = documentationURLMethod.getDocumentationUrl();

        assertThat(actual).isEqualTo(BASE_URL + ENTERPRISE_EDITION_PATH + "/" + VERSION + ENTERPRISE_EDITION_PATH);
    }

    @Test
    public void getDocumentationUrl_whenGivenNullArgument_shouldReturnBaseUrl() {
        String actual = documentationURLMethod.getDocumentationUrl(null);

        assertThat(actual).isEqualTo(BASE_URL + COMMUNITY_EDITION_PATH + "/" + VERSION + COMMUNITY_EDITION_PATH);
    }

    @Test
    public void getDocumentationUrl_whenUnknownEditionIsSet_shouldReturnBaseUrl() {
        documentationURLMethod.setEdition("director's cut");

        String actual = documentationURLMethod.getDocumentationUrl();

        assertThat(actual).isEqualTo(BASE_URL);
    }

    @Test
    public void getDocumentationUrl_whenGivenPath_shouldReturnUrlToThatPath() {
        String actual = documentationURLMethod.getDocumentationUrl("/road/to/rome");

        assertThat(actual).isEqualTo(BASE_URL + COMMUNITY_EDITION_PATH + "/" + VERSION + COMMUNITY_EDITION_PATH + "/road/to/rome");
    }
}
