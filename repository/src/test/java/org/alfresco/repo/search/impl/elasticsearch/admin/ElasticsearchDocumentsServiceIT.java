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
package org.alfresco.repo.search.impl.elasticsearch.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.search.impl.elasticsearch.query.ElasticsearchBaseQueryIT;
import org.alfresco.service.cmr.repository.NodeRef;

public class ElasticsearchDocumentsServiceIT extends ElasticsearchBaseQueryIT
{

    private static final int ALIVE_DOCUMENTS = 10;

    private ElasticsearchDocumentsService elasticsearchDocumentsService;
    private List<String> ids = new ArrayList<>();

    @Before
    public void initStatService()
    {
        elasticsearchDocumentsService = (ElasticsearchDocumentsService) elasticsearchContext.getBean(
                "elasticsearchDocumentsService");
    }

    @Before
    public void initDocuments()
    {
        for (int i = 0; i < ALIVE_DOCUMENTS; ++i)
        {
            NodeRef nodeRef = indexDocument("-alive-", "content", new HashMap<>() {
                {
                    put("CONTENT_INDEXING_LAST_UPDATE", 1000L);
                    put("METADATA_INDEXING_LAST_UPDATE", 2000L);
                }
            });

            ids.add(nodeRef.getId());
        }
    }

    @Test
    public void shouldRetrieveAllLastUpdateTimestamps()
    {
        List<String> sourceFields = Arrays.asList("CONTENT_INDEXING_LAST_UPDATE", "METADATA_INDEXING_LAST_UPDATE");

        List<ElasticsearchDocument> documents = elasticsearchDocumentsService.getDocuments(ids, sourceFields, 10000);

        assertEquals("Unexpected number of documents retrieved from service.",
                ALIVE_DOCUMENTS, documents.size());
        assertEquals("Unexpected document ids retrieved from service.",
                ids, documents.stream().map(ElasticsearchDocument::getId).toList());
        assertTrue("Unexpected document content indexing timestamps retrieved from service.",
                documents.stream().allMatch(esDocument -> esDocument.getContentIndexingLastUpdate() == 1000L));
        assertTrue("Unexpected document metadata indexing timestamps retrieved from service.",
                documents.stream().allMatch(esDocument -> esDocument.getMetadataIndexingLastUpdate() == 2000L));
    }

    @Test
    public void shouldRetrieveOnlyMetadataLastUpdateTimestamp()
    {
        List<String> sourceFields = Arrays.asList("METADATA_INDEXING_LAST_UPDATE");

        List<ElasticsearchDocument> documents = elasticsearchDocumentsService.getDocuments(ids, sourceFields, 10000);

        assertEquals("Unexpected number of documents retrieved from service.",
                ALIVE_DOCUMENTS, documents.size());
        assertEquals("Unexpected document ids retrieved from service.",
                ids, documents.stream().map(ElasticsearchDocument::getId).toList());
        assertTrue("Unexpected document content indexing timestamps retrieved from service.",
                documents.stream().allMatch(esDocument -> esDocument.getContentIndexingLastUpdate() == 0L));
        assertTrue("Unexpected document metadata indexing timestamps retrieved from service.",
                documents.stream().allMatch(esDocument -> esDocument.getMetadataIndexingLastUpdate() == 2000L));
    }
}
