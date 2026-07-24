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

package org.alfresco.repo.search.impl.elasticsearch.query;

import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_MIME_TYPE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import org.alfresco.repo.search.SearchEngineResultSet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;

// Integration tests for search_after deep pagination over a Point-In-Time (PIT) end-to-end against a real Elasticsearch instance. The index is emptied before every test so a simple cm:name prefix query returns exactly the documents created by the test.
@SuppressWarnings("PMD")
public class SearchAfterPaginationIT extends ElasticsearchBaseQueryIT
{
    private static final String QUERY = "cm:name:paged*";
    // Ascending order of the repeating primary sort key, so the expected order is deterministic.
    private static final String[] MIME_TYPES = {"application/pdf", "image/jpeg", "text/plain"};

    private SearchParameters cursorPage(int pageSize, String cursor)
    {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setLanguage("afts");
        searchParameters.setQuery(QUERY);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLimitBy(LimitBy.FINAL_SIZE);
        searchParameters.setLimit(pageSize);
        searchParameters.addSort("@cm:name", true);
        searchParameters.setSearchAfter(cursor);
        return searchParameters;
    }

    private SearchParameters compoundCursorPage(int pageSize, String cursor)
    {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setLanguage("afts");
        searchParameters.setQuery(QUERY);
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLimitBy(LimitBy.FINAL_SIZE);
        searchParameters.setLimit(pageSize);
        searchParameters.addSort(CONTENT_MIME_TYPE, true);
        searchParameters.addSort("@cm:name", true);
        searchParameters.setSearchAfter(cursor);
        return searchParameters;
    }

    private List<NodeRef> indexPagedDocuments(int count)
    {
        List<NodeRef> refs = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            String name = String.format("paged%03d", i);
            refs.add(indexDocument(name, name));
        }
        return refs;
    }

    private static String nextCursor(ResultSet resultSet)
    {
        return ((SearchEngineResultSet) resultSet).getNextCursor();
    }

    @Test
    public void shouldPageThroughAllResultsWithoutDuplicatesOrGaps()
    {
        List<NodeRef> created = indexPagedDocuments(25);

        List<NodeRef> collected = new ArrayList<>();
        String cursor = ""; // empty cursor requests the first page
        int pages = 0;
        do
        {
            ResultSet page = aftsQueryExecutor.executeQuery(cursorPage(10, cursor));
            pages++;
            collected.addAll(page.getNodeRefs());
            cursor = nextCursor(page);
        } while (cursor != null && pages < 10);

        assertEquals("Expected 3 pages of 10/10/5", 3, pages);
        assertNull("Last page must not return a cursor", cursor);
        assertEquals("Every document should be returned exactly once", 25, collected.size());
        assertEquals("There should be no duplicates across pages", 25, new LinkedHashSet<>(collected).size());
        assertTrue("All created documents should be paged through", collected.containsAll(created));
    }

    @Test
    public void shouldPreserveSortOrderAcrossPages()
    {
        // Several documents share the same mimetype, so the secondary @cm:name sort is required to fully order them.
        List<NodeRef> created = new ArrayList<>();
        List<String> mimeTypes = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < 15; i++)
        {
            String mimeType = MIME_TYPES[i % MIME_TYPES.length];
            String name = String.format("paged%03d", i);
            created.add(indexDocument(name, name, Map.of(CONTENT_MIME_TYPE, mimeType)));
            mimeTypes.add(mimeType);
            names.add(name);
        }

        // Expected order: mimetype ascending, then @cm:name ascending.
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < created.size(); i++)
        {
            order.add(i);
        }
        order.sort(Comparator.<Integer, String> comparing(mimeTypes::get).thenComparing(names::get));
        List<NodeRef> expectedOrder = order.stream().map(created::get).toList();

        List<NodeRef> collected = new ArrayList<>();
        String cursor = "";
        do
        {
            ResultSet page = aftsQueryExecutor.executeQuery(compoundCursorPage(5, cursor));
            collected.addAll(page.getNodeRefs());
            cursor = nextCursor(page);
        } while (cursor != null);

        assertEquals("Cursor paging must preserve mimetype then @cm:name ordering across pages",
                expectedOrder, collected);
    }

    @Test
    public void shouldIsolatePaginationFromDocumentsAddedAfterFirstPage()
    {
        indexPagedDocuments(10); // paged000..paged009

        // The first page opens the Point-In-Time snapshot.
        ResultSet firstPage = aftsQueryExecutor.executeQuery(cursorPage(5, ""));
        String cursor = nextCursor(firstPage);
        assertNotNull("First page should return a cursor", cursor);

        // Insert a document that sorts within the not-yet-returned range, after the PIT was opened.
        NodeRef addedAfterPit = indexDocument("paged007b", "paged007b");

        List<NodeRef> remaining = new ArrayList<>();
        while (cursor != null)
        {
            ResultSet page = aftsQueryExecutor.executeQuery(cursorPage(5, cursor));
            remaining.addAll(page.getNodeRefs());
            cursor = nextCursor(page);
        }
        assertFalse("The PIT snapshot must not surface a document added after pagination started",
                remaining.contains(addedAfterPit));

        // a brand-new pagination opens a fresh PIT and therefore does see the new document.
        List<NodeRef> freshScan = new ArrayList<>();
        String freshCursor = "";
        do
        {
            ResultSet page = aftsQueryExecutor.executeQuery(cursorPage(100, freshCursor));
            freshScan.addAll(page.getNodeRefs());
            freshCursor = nextCursor(page);
        } while (freshCursor != null);
        assertTrue("A fresh search should see the newly added document", freshScan.contains(addedAfterPit));
    }
}
