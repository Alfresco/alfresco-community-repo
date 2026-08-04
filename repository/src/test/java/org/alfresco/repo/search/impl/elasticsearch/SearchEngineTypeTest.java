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

import org.junit.Test;

/**
 * Unit tests for {@link SearchEngineType}.
 */
public class SearchEngineTypeTest
{
    @Test
    public void opensearchDistributionMapsToOpenSearch()
    {
        assertThat(SearchEngineType.fromDistribution("opensearch")).isEqualTo(SearchEngineType.OPENSEARCH);
    }

    @Test
    public void opensearchDistributionIsCaseInsensitiveAndTrimmed()
    {
        assertThat(SearchEngineType.fromDistribution("OpenSearch")).isEqualTo(SearchEngineType.OPENSEARCH);
        assertThat(SearchEngineType.fromDistribution("  OPENSEARCH  ")).isEqualTo(SearchEngineType.OPENSEARCH);
    }

    @Test
    public void nullDistributionMapsToElasticsearch()
    {
        assertThat(SearchEngineType.fromDistribution(null)).isEqualTo(SearchEngineType.ELASTICSEARCH);
    }

    @Test
    public void blankDistributionMapsToElasticsearch()
    {
        assertThat(SearchEngineType.fromDistribution("")).isEqualTo(SearchEngineType.ELASTICSEARCH);
        assertThat(SearchEngineType.fromDistribution("   ")).isEqualTo(SearchEngineType.ELASTICSEARCH);
    }

    @Test
    public void unrecognisedDistributionMapsToUnknown()
    {
        assertThat(SearchEngineType.fromDistribution("solr")).isEqualTo(SearchEngineType.UNKNOWN);
    }

    @Test
    public void displayNamesAreHumanReadable()
    {
        assertThat(SearchEngineType.OPENSEARCH.getDisplayName()).isEqualTo("OpenSearch");
        assertThat(SearchEngineType.ELASTICSEARCH.getDisplayName()).isEqualTo("Elasticsearch");
        assertThat(SearchEngineType.UNKNOWN.getDisplayName()).isEqualTo("Unknown");
    }
}
