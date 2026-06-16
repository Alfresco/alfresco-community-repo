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
package org.alfresco.repo.search.impl.elasticsearch.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class ElasticsearchHttpClientFactoryAwsIamTest
{
    private ElasticsearchHttpClientFactory elasticsearchHttpClientFactory;

    @Before
    public void setUp()
    {
        elasticsearchHttpClientFactory = new ElasticsearchHttpClientFactory();
        configureClientFactory(elasticsearchHttpClientFactory);
    }

    @Test
    public void testGetElasticsearchClientWithUnsupportedAuthMode()
    {
        elasticsearchHttpClientFactory.setAuthMode("digest");

        assertThatThrownBy(() -> elasticsearchHttpClientFactory.getElasticsearchClient())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported elasticsearch.auth.mode");
    }

    @Test
    public void testGetElasticsearchClientWithAwsIamModeRequiresRegion()
    {
        elasticsearchHttpClientFactory.setAuthMode("aws-iam");

        assertThatThrownBy(() -> elasticsearchHttpClientFactory.getElasticsearchClient())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("elasticsearch.aws.region");
    }

    @Test
    public void testGetElasticsearchClientWithAwsIamModeCreatesClient()
    {
        elasticsearchHttpClientFactory.setAuthMode("aws-iam");
        elasticsearchHttpClientFactory.setAwsRegion("us-east-1");
        elasticsearchHttpClientFactory.setAwsService("es");
        elasticsearchHttpClientFactory.setSecureComms("https");
        elasticsearchHttpClientFactory.setPort(443);

        assertNotNull("ElasticsearchClient should not be null",
                elasticsearchHttpClientFactory.getElasticsearchClient());

        elasticsearchHttpClientFactory.destroy();
    }

    @Test
    public void testGetElasticsearchClientWithAwsIamModeRequiresService()
    {
        elasticsearchHttpClientFactory.setAuthMode("aws-iam");
        elasticsearchHttpClientFactory.setAwsRegion("us-east-1");
        elasticsearchHttpClientFactory.setAwsService(" ");
        elasticsearchHttpClientFactory.setSecureComms("https");
        elasticsearchHttpClientFactory.setPort(443);

        assertThatThrownBy(() -> elasticsearchHttpClientFactory.getElasticsearchClient())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("elasticsearch.aws.service");
    }

    @Test
    public void testGetElasticsearchClientWithAwsIamModeRejectsSecureCommsNone()
    {
        elasticsearchHttpClientFactory.setAuthMode("aws-iam");
        elasticsearchHttpClientFactory.setAwsRegion("us-east-1");
        elasticsearchHttpClientFactory.setAwsService("es");
        elasticsearchHttpClientFactory.setSecureComms("none");
        elasticsearchHttpClientFactory.setPort(443);

        assertThatThrownBy(() -> elasticsearchHttpClientFactory.getElasticsearchClient())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("elasticsearch.secureComms")
                .hasMessageContaining("https");
    }

    @Test
    public void testGetElasticsearchClientWithAwsIamModeRejectsMtls()
    {
        elasticsearchHttpClientFactory.setAuthMode("aws-iam");
        elasticsearchHttpClientFactory.setAwsRegion("us-east-1");
        elasticsearchHttpClientFactory.setAwsService("es");
        elasticsearchHttpClientFactory.setSecureComms("mtls");
        elasticsearchHttpClientFactory.setPort(443);

        assertThatThrownBy(() -> elasticsearchHttpClientFactory.getElasticsearchClient())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("elasticsearch.secureComms")
                .hasMessageContaining("https");
    }

    @Test
    public void testGetElasticsearchClientWithAwsIamModeRejectsNonDefaultPort()
    {
        elasticsearchHttpClientFactory.setAuthMode("aws-iam");
        elasticsearchHttpClientFactory.setAwsRegion("us-east-1");
        elasticsearchHttpClientFactory.setAwsService("es");
        elasticsearchHttpClientFactory.setSecureComms("https");
        elasticsearchHttpClientFactory.setPort(9200);

        assertThatThrownBy(() -> elasticsearchHttpClientFactory.getElasticsearchClient())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("elasticsearch.port")
                .hasMessageContaining("443");
    }

    @Test
    public void testGetElasticsearchClientWithAwsIamModeRejectsNonRootBaseUrl()
    {
        elasticsearchHttpClientFactory.setAuthMode("aws-iam");
        elasticsearchHttpClientFactory.setAwsRegion("us-east-1");
        elasticsearchHttpClientFactory.setAwsService("es");
        elasticsearchHttpClientFactory.setSecureComms("https");
        elasticsearchHttpClientFactory.setPort(443);
        elasticsearchHttpClientFactory.setBaseUrl("/alfresco");

        assertThatThrownBy(() -> elasticsearchHttpClientFactory.getElasticsearchClient())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("elasticsearch.baseUrl");
    }

    private static void configureClientFactory(ElasticsearchHttpClientFactory elasticsearchHttpClientFactory)
    {
        elasticsearchHttpClientFactory.setSecureComms("none");
        elasticsearchHttpClientFactory.setHost("localhost");
        elasticsearchHttpClientFactory.setPort(9200);
        elasticsearchHttpClientFactory.setBaseUrl("/");
        elasticsearchHttpClientFactory.setConnectionTimeout(1000);
        elasticsearchHttpClientFactory.setSocketTimeout(30000);
        elasticsearchHttpClientFactory.setResponseTimeout(30000);
        elasticsearchHttpClientFactory.setMaxTotalConnections(30);
        elasticsearchHttpClientFactory.setMaxHostConnections(30);
        elasticsearchHttpClientFactory.setThreadCount(0);
    }
}
