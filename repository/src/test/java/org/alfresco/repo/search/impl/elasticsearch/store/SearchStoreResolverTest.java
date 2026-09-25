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
package org.alfresco.repo.search.impl.elasticsearch.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;

/** Unit tests for {@link SearchStoreResolver}. */
@RunWith(MockitoJUnitRunner.class)
public class SearchStoreResolverTest
{
    private static final StoreRef WORKSPACE_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    private static final StoreRef ARCHIVE_STORE = new StoreRef(StoreRef.PROTOCOL_ARCHIVE, "SpacesStore");
    private static final String INDEX_NAME = "alfresco";

    @Mock
    private ElasticsearchHttpClientFactory httpClientFactory;

    private SearchStoreResolver resolver;

    @Before
    public void setUp()
    {
        resolver = new SearchStoreResolver(httpClientFactory);
    }

    @Test
    public void resolveStore_returnsSingleStore()
    {
        assertEquals(WORKSPACE_STORE, resolver.resolveStore(List.of(WORKSPACE_STORE)));
        assertEquals(ARCHIVE_STORE, resolver.resolveStore(List.of(ARCHIVE_STORE)));
    }

    @Test
    public void resolveStore_rejectsEmptyStoreList()
    {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolveStore(List.of()));
    }

    @Test
    public void resolveStore_rejectsMultipleStores()
    {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolveStore(List.of(WORKSPACE_STORE, ARCHIVE_STORE)));
    }

    @Test
    public void resolveStore_rejectsNullStoreList()
    {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolveStore((List<StoreRef>) null));
    }

    @Test
    public void resolveIndex_workspaceStoreUsesUnifiedIndex()
    {
        when(httpClientFactory.getIndexName()).thenReturn(INDEX_NAME);
        assertEquals(INDEX_NAME, resolver.resolveIndex(List.of(WORKSPACE_STORE)));
    }

    @Test
    public void resolveIndex_archiveStoreUsesUnifiedIndex()
    {
        when(httpClientFactory.getIndexName()).thenReturn(INDEX_NAME);
        assertEquals(INDEX_NAME, resolver.resolveIndex(List.of(ARCHIVE_STORE)));
    }

    @Test
    public void resolveIndex_rejectsUnsupportedProtocol()
    {
        StoreRef unsupported = new StoreRef("unsupported", "SpacesStore");
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> resolver.resolveIndex(List.of(unsupported)));
        assertTrue(exception.getMessage().contains("is not supported"));
    }

    @Test
    public void isArchiveScope_trueForArchiveStore()
    {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(ARCHIVE_STORE);
        assertTrue(resolver.isArchiveScope(searchParameters));
    }

    @Test
    public void isArchiveScope_falseForWorkspaceStore()
    {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(WORKSPACE_STORE);
        assertFalse(resolver.isArchiveScope(searchParameters));
    }
}
