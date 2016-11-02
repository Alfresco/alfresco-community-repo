/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.alfresco.rest.api.search.context.FacetFieldContext;
import org.alfresco.rest.api.search.context.FacetFieldContext.Bucket;
import org.alfresco.rest.api.search.context.SpellCheckContext;
import org.alfresco.rest.api.search.model.Default;
import org.alfresco.rest.api.search.model.FacetField;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.api.search.context.SearchContext;
import org.alfresco.rest.api.search.context.FacetQueryContext;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * Tests json -> SearchQuery deserialization
 *
 * @author Gethin James
 */
public class SearchQuerySerializerTests
{

    private static SerializerTestHelper helper;

    @BeforeClass
    public static void setupTests() throws Exception
    {
        helper = new SerializerTestHelper();
    }

    @Test
    public void testDeserializeQuery() throws IOException
    {
        SearchQuery searchQuery = helper.searchQueryFromJson();
        assertEquals(SearchQuery.class, searchQuery.getClass());
        assertEquals("afts", searchQuery.getQuery().getLanguage());
        assertEquals("g*", searchQuery.getQuery().getQuery());
        assertEquals("great", searchQuery.getQuery().getUserQuery());
        assertEquals(99, searchQuery.getPaging().getMaxItems());
        assertEquals(4, searchQuery.getPaging().getSkipCount());
        assertEquals(2, searchQuery.getInclude().size());
        assertTrue(searchQuery.getInclude().contains("aspectNames"));
        assertTrue(searchQuery.getInclude().contains("properties"));
        assertEquals(1, searchQuery.getSort().size());
        assertEquals(2, searchQuery.getTemplates().size());
        Default defaults = searchQuery.getDefaults();
        assertEquals("namesp",  defaults.getNamespace());
        assertEquals("myfield", defaults.getDefaultFieldName());
        assertEquals("AND",     defaults.getDefaultFTSOperator());
        assertEquals(2, defaults.getTextAttributes().size());
        assertTrue(defaults.getTextAttributes().contains("roy"));
        assertTrue(defaults.getTextAttributes().contains("king"));
        assertEquals(2, searchQuery.getFilterQueries().size());
        assertEquals("myquery",searchQuery.getFilterQueries().get(0).getQuery());
        assertEquals(2, searchQuery.getFilterQueries().get(0).getTags().size());
        assertEquals("myquery2",searchQuery.getFilterQueries().get(1).getQuery());
        assertEquals(1, searchQuery.getFacetQueries().size());
        assertEquals("facquery",searchQuery.getFacetQueries().get(0).getQuery());
        assertEquals("facnoused",searchQuery.getFacetQueries().get(0).getLabel());
        assertEquals("alfrezco", searchQuery.getSpellcheck().getQuery());
        assertEquals(1, searchQuery.getScope().getStores().size());
        assertEquals("workspace://SpacesStore", searchQuery.getScope().getStores().get(0));
        assertEquals(2, searchQuery.getFacetFields().getFacets().size());
        FacetField ff = searchQuery.getFacetFields().getFacets().get(0);
        assertEquals("cm:creator", ff.getField());
        assertEquals("myquery2", ff.getPrefix());
        assertEquals("COUNT", ff.getSort());
        assertEquals(false, ff.getMissing());
        ff = searchQuery.getFacetFields().getFacets().get(1);
        assertEquals("modifier", ff.getField());
        assertEquals("mylabel", ff.getLabel());
        assertEquals("FC", ff.getMethod());
        assertEquals(Integer.valueOf(5), ff.getMincount());
        assertEquals(2000, searchQuery.getLimits().getPermissionEvaluationCount().intValue());
        assertEquals(5000, searchQuery.getLimits().getPermissionEvaluationTime().intValue());
        assertEquals(2, searchQuery.getFields().size());
        assertTrue(searchQuery.getFields().contains("id"));
        assertTrue(searchQuery.getFields().contains("name"));

        //Highlight
        assertEquals("]", searchQuery.getHighlight().getPostfix());
        assertEquals("[", searchQuery.getHighlight().getPrefix());
        assertEquals(20, searchQuery.getHighlight().getSnippetCount().intValue());
        assertEquals(10, searchQuery.getHighlight().getFragmentSize().intValue());
        assertEquals(true, searchQuery.getHighlight().getMergeContiguous());
        assertEquals(40, searchQuery.getHighlight().getMaxAnalyzedChars().intValue());
        assertEquals(true, searchQuery.getHighlight().getUsePhraseHighlighter());

        assertEquals(2, searchQuery.getHighlight().getFields().size());
        FieldHighlightParameters high1 = searchQuery.getHighlight().getFields().get(0);
        assertEquals("my", high1.getField());
        assertEquals("¡", high1.getPostfix());
        assertEquals("?", high1.getPrefix());
        assertEquals(23,  high1.getSnippetCount().intValue());
        assertEquals(5,  high1.getFragmentSize().intValue());
        assertEquals(true,high1.getMergeContiguous());

        FieldHighlightParameters high2 = searchQuery.getHighlight().getFields().get(1);
        assertEquals("your", high2.getField());
        assertEquals(")", high2.getPostfix());
        assertEquals("(", high2.getPrefix());
        assertEquals(3,  high2.getSnippetCount().intValue());
        assertEquals(15,  high2.getFragmentSize().intValue());
        assertEquals(false,high2.getMergeContiguous());
    }

    @Test
    public void testSerializeContext() throws IOException
    {
        ExecutionResult exec1 = new ExecutionResult(new Farmer("180"),null);

        FacetFieldContext ffc = new FacetFieldContext("theLabel", Arrays.asList(new Bucket("b1", 23, "displayText1"), new Bucket("b2", 34, "displayText2")));
        SearchContext searchContext = new SearchContext(23l, Arrays.asList(new FacetQueryContext("f1", 15), new FacetQueryContext("f2", 20)),
                    Arrays.asList(ffc),
                    new SpellCheckContext("aFlag", Arrays.asList("bish", "bash")));
        CollectionWithPagingInfo<ExecutionResult> coll = CollectionWithPagingInfo.asPaged(null, Arrays.asList(exec1), false, 2, null, searchContext);
        String out = helper.writeResponse(coll);
        assertTrue("There must 'context' json output", out.contains("\"context\":{\"consistency\":{\"lastTxId\":23}"));
        assertTrue("There must 'facetQueries' json output", out.contains("\"facetQueries\":"));
        assertTrue("There must 'facetQueries f1' json output", out.contains("{\"label\":\"f1\",\"count\":15}"));
        assertTrue("There must 'facetQueries f2' json output", out.contains("{\"label\":\"f2\",\"count\":20}"));
        assertTrue("There must 'spellCheck' json output", out.contains("\"spellCheck\":{\"type\":\"aFlag\",\"suggestions\":[\"bish\",\"bash\"]}"));
        assertTrue("There must 'facetsFields' json output", out.contains("\"facetsFields\":[{\"label\":\"theLabel\",\"buckets\""));
        assertTrue("There must 'bucket1' json output", out.contains("{\"label\":\"b1\",\"count\":23,\"display\":\"displayText1\"}"));
        assertTrue("There must 'bucket2' json output", out.contains("{\"label\":\"b2\",\"count\":34,\"display\":\"displayText2\"}"));

        searchContext = new SearchContext(-1, null, null, null);
        coll = CollectionWithPagingInfo.asPaged(null, Arrays.asList(exec1), false, 2, null, searchContext);
        out = helper.writeResponse(coll);
        assertTrue("There must NOT BE a 'context' json output", out.contains("\"context\":{}"));
        assertFalse("There must NOT BE a 'facetsFields' json output", out.contains("\"facetsFields\":{}"));

    }


}
