/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer.DEFAULT_ANALYZER;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchSpringTest;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.field.property.ElasticsearchAnalyzer;

public class ElasticsearchFieldAnalyzersConfigIT extends ElasticsearchSpringTest
{

    private ElasticsearchFieldAnalyzersConfig fieldAnalyzersConfig;

    @Before
    public void setUp() throws Exception
    {
        fieldAnalyzersConfig = elasticsearchContext.getBean(ElasticsearchFieldAnalyzersConfig.class);
    }

    @Test
    public void defaultAnalyzerShouldAlwaysBeAvailable()
    {
        assertTrue("default analyzer should always be available", fieldAnalyzersConfig.isAnalyzerDefinedInElasticsearch(DEFAULT_ANALYZER));
    }

    @Test
    public void analyzerShouldNotExist()
    {
        ElasticsearchAnalyzer someAnalyzer = mock();

        when(someAnalyzer.getName()).thenReturn("aaa_bbb_ccc_ddd");

        assertFalse("some random analyzer should not be available", fieldAnalyzersConfig.isAnalyzerDefinedInElasticsearch(someAnalyzer));
    }

}
