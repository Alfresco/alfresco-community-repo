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
package org.alfresco.repo.search.impl.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchVersionInfo;
import org.opensearch.client.opensearch.core.InfoResponse;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;

/**
 * Unit tests for {@link SearchEngineInfoImpl}.
 */
public class SearchEngineInfoImplTest
{
    private final ElasticsearchHttpClientFactory clientFactory = mock(ElasticsearchHttpClientFactory.class);

    private final SearchEngineInfoImpl searchEngineInfo = new SearchEngineInfoImpl(clientFactory);

    @Test
    public void shouldReportUnknownBeforeDetection()
    {
        assertThat(searchEngineInfo.isDetected()).isFalse();
        assertThat(searchEngineInfo.getType()).isEqualTo(SearchEngineType.UNKNOWN);
        assertThat(searchEngineInfo.getVersion()).isNull();
        assertThat(searchEngineInfo.getDistribution()).isNull();
    }

    @Test
    public void shouldDetectOpenSearch() throws Exception
    {
        stubInfo("opensearch", "2.13.0");

        searchEngineInfo.detect();

        assertThat(searchEngineInfo.getType()).isEqualTo(SearchEngineType.OPENSEARCH);
        assertThat(searchEngineInfo.getVersion()).isEqualTo("2.13.0");
        assertThat(searchEngineInfo.getDistribution()).isEqualTo("opensearch");
        assertThat(searchEngineInfo.isDetected()).isTrue();
    }

    @Test
    public void shouldDetectElasticsearchWhenNoDistributionReported() throws Exception
    {
        stubInfo(null, "7.10.2");

        searchEngineInfo.detect();

        assertThat(searchEngineInfo.getType()).isEqualTo(SearchEngineType.ELASTICSEARCH);
        assertThat(searchEngineInfo.getVersion()).isEqualTo("7.10.2");
        assertThat(searchEngineInfo.getDistribution()).isNull();
        assertThat(searchEngineInfo.isDetected()).isTrue();
    }

    @Test
    public void shouldFallBackToUnknownWhenDetectionFails() throws Exception
    {
        OpenSearchClient client = mock(OpenSearchClient.class);
        when(clientFactory.getElasticsearchClient()).thenReturn(client);
        when(client.info()).thenThrow(new IOException("connection refused"));

        searchEngineInfo.detect();

        assertThat(searchEngineInfo.getType()).isEqualTo(SearchEngineType.UNKNOWN);
        assertThat(searchEngineInfo.getVersion()).isNull();
        assertThat(searchEngineInfo.getDistribution()).isNull();
        assertThat(searchEngineInfo.isDetected()).isFalse();
    }

    private void stubInfo(String distribution, String number) throws IOException
    {
        OpenSearchClient client = mock(OpenSearchClient.class);
        InfoResponse info = mock(InfoResponse.class);
        OpenSearchVersionInfo versionInfo = mock(OpenSearchVersionInfo.class);

        when(clientFactory.getElasticsearchClient()).thenReturn(client);
        when(client.info()).thenReturn(info);
        when(info.version()).thenReturn(versionInfo);
        when(versionInfo.distribution()).thenReturn(distribution);
        when(versionInfo.number()).thenReturn(number);
    }
}
