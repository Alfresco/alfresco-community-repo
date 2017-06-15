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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_CMIS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.alfresco.rest.api.search.context.SearchRequestContext;
import org.alfresco.rest.api.search.impl.SearchMapper;
import org.alfresco.rest.api.search.impl.StoreMapper;
import org.alfresco.rest.api.search.model.Default;
import org.alfresco.rest.api.search.model.FacetField;
import org.alfresco.rest.api.search.model.FacetFields;
import org.alfresco.rest.api.search.model.FacetQuery;
import org.alfresco.rest.api.search.model.FilterQuery;
import org.alfresco.rest.api.search.model.Limits;
import org.alfresco.rest.api.search.model.Pivot;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.api.search.model.Scope;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.api.search.model.SortDef;
import org.alfresco.rest.api.search.model.Spelling;
import org.alfresco.rest.api.search.model.Template;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.FieldHighlightParameters;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.Interval;
import org.alfresco.service.cmr.search.IntervalParameters;
import org.alfresco.service.cmr.search.IntervalSet;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.StatsRequestParameters;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the SearchMapper class
 *
 * @author Gethin James
 */
public class SearchMapperTests
{

    static SearchMapper searchMapper = new SearchMapper();
    static SerializerTestHelper helper = new SerializerTestHelper();

    @BeforeClass
    public static void setupTests() throws Exception
    {
        searchMapper.setStoreMapper(new StoreMapper());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMandatory() throws Exception
    {
        SearchRequestContext searchRequest = SearchRequestContext.from(SearchQuery.EMPTY);
        SearchParameters searchParameters = searchMapper.toSearchParameters(ResultMapperTests.EMPTY_PARAMS, SearchQuery.EMPTY, searchRequest);
    }

    @Test
    public void toSearchParameters() throws Exception
    {
        SearchRequestContext searchRequest = SearchRequestContext.from(minimalQuery());
        SearchParameters searchParameters = searchMapper.toSearchParameters(ResultMapperTests.EMPTY_PARAMS, minimalQuery(), searchRequest);
        assertNotNull(searchParameters);

        //Test defaults
        assertEquals("There should be only 1 default store", 1,searchParameters.getStores().size());
        assertEquals("workspaces store is the default", StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, searchParameters.getStores().get(0));
        assertEquals(LimitBy.FINAL_SIZE, searchParameters.getLimitBy());
        assertEquals(100, searchParameters.getLimit());

        searchParameters = searchMapper.toSearchParameters(ResultMapperTests.EMPTY_PARAMS, helper.searchQueryFromJson(), searchRequest);
        assertNotNull(searchParameters);
    }

    @Test
    public void fromQuery() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        try
        {
            searchMapper.fromQuery(searchParameters, new Query(null,null, null));
            fail();
        } catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("query is a mandatory parameter"));
        }

        Query q = new Query(null,"hello", null);

        searchMapper.fromQuery(searchParameters, q);
        //Default
        assertEquals(LANGUAGE_FTS_ALFRESCO, searchParameters.getLanguage());

        q = new Query("world", "hello", null);

        try
        {
            searchMapper.fromQuery(searchParameters, q);
            fail();
        } catch (InvalidArgumentException iae)
        {
            assertNotNull(iae);
            //world is not a valid language type
        }

        q = new Query("afts", "hello", null);
        searchMapper.fromQuery(searchParameters, q);
        assertEquals(LANGUAGE_FTS_ALFRESCO, searchParameters.getLanguage());

        q = new Query("cMiS", "hello", null);
        searchMapper.fromQuery(searchParameters, q);
        assertEquals(LANGUAGE_CMIS_ALFRESCO, searchParameters.getLanguage());

        q = new Query("LuCENE", "hello", null);
        searchMapper.fromQuery(searchParameters, q);
        assertEquals(LANGUAGE_LUCENE, searchParameters.getLanguage());

        assertEquals("hello", searchParameters.getQuery());

        q = new Query("LuCENE", "hello", "Heload");
        searchMapper.fromQuery(searchParameters, q);
        assertEquals("Heload", searchParameters.getSearchTerm());
    }

    @Test
    public void fromPaging() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromPaging(searchParameters, null);

        Paging paging = Paging.DEFAULT;
        searchMapper.fromPaging(searchParameters, paging);
        assertEquals(searchParameters.getLimit(),paging.getMaxItems());
        assertEquals(searchParameters.getSkipCount(),paging.getSkipCount());
    }


    @Test
    public void fromSort() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromSort(searchParameters, null);

        try
        {
            searchMapper.fromSort(searchParameters, Arrays.asList(new SortDef("wrongenum", null, false)));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //wrongenum is illegal
            assertNotNull(iae);
        }

        searchMapper.fromSort(searchParameters, Arrays.asList(new SortDef("FIELD", "my", true), new SortDef("SCORE", null, false), new SortDef("DOCUMENT", null, true)));
        assertEquals(3 , searchParameters.getSortDefinitions().size());
        searchParameters.getSortDefinitions().forEach(sortDefinition ->
        {
            switch (sortDefinition.getSortType())
            {
                case FIELD:
                    assertEquals("my", sortDefinition.getField());
                    assertEquals(true, sortDefinition.isAscending());
                    break;
                case SCORE:
                    assertNull(sortDefinition.getField());
                    assertEquals(false, sortDefinition.isAscending());
                    break;
                case DOCUMENT:
                    assertNull(sortDefinition.getField());
                    assertEquals(true, sortDefinition.isAscending());
                    break;
                default:
                    fail("Invalid sortDefinition");
            }
        });

        searchParameters = new SearchParameters();
        searchParameters.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
        try
        {
            searchMapper.fromSort(searchParameters, Arrays.asList(new SortDef("FIELD", null, false)));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //You can't specify SORT when using the CMIS language
            assertNotNull(iae);
        }
    }


    @Test
    public void fromTemplate() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromTemplate(searchParameters, null);

        searchMapper.fromTemplate(searchParameters, Arrays.asList(new Template("hedge", "hog"), new Template("king", "kong"), new Template("bish", "bash")));
        assertEquals(3 ,searchParameters.getQueryTemplates().size());
        assertEquals("hog" ,searchParameters.getQueryTemplates().get("hedge"));
        assertEquals("kong" ,searchParameters.getQueryTemplates().get("king"));
        assertEquals("bash" ,searchParameters.getQueryTemplates().get("bish"));
    }

    @Test
    public void fromDefaults() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromDefault(searchParameters, null);
        searchMapper.fromDefault(searchParameters, new Default(null,null,null,null,null));

        searchMapper.fromDefault(searchParameters, new Default(null,"AND",null,null,null));
        assertEquals("AND", searchParameters.getDefaultFTSOperator().toString());

        searchMapper.fromDefault(searchParameters, new Default(null, null, "or", null,null));
        assertEquals("OR", searchParameters.getDefaultFTSFieldOperator().toString());

        try
        {
            searchMapper.fromDefault(searchParameters, new Default(null, null, "ELSE", null,null));
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            //ELSE is illegal
            assertNotNull(iae);
        }

        searchMapper.fromDefault(searchParameters, new Default(null, null, null, "nspa","dfn"));
        assertEquals("nspa", searchParameters.getNamespace());
        assertEquals("dfn", searchParameters.getDefaultFieldName());

        assertEquals(0 , searchParameters.getTextAttributes().size());
        searchMapper.fromDefault(searchParameters, new Default(Arrays.asList("sausage", "mash"), null, null, null,null));
        assertEquals(2 , searchParameters.getTextAttributes().size());
        assertTrue(searchParameters.getTextAttributes().contains("sausage"));
        assertTrue(searchParameters.getTextAttributes().contains("mash"));
    }

    @Test
    public void validateInclude() throws Exception
    {

        try
        {
            searchMapper.validateInclude(Arrays.asList("sausage"));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
           //Sausage is illegal
           assertNotNull(iae);
        }

        searchMapper.validateInclude(null);

        try
        {
            searchMapper.validateInclude(Arrays.asList("AspectNames"));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //Case sensitive
            assertNotNull(iae);
        }

        searchMapper.validateInclude(Arrays.asList("properties", "aspectNames"));
    }

    @Test
    public void fromFilterQuery() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromFilterQuery(searchParameters, null);

        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("hedgehog", null, null), new FilterQuery("king", null, null)));
        assertEquals(2 ,searchParameters.getFilterQueries().size());
        assertEquals("hedgehog" ,searchParameters.getFilterQueries().get(0));
        assertEquals("king" ,searchParameters.getFilterQueries().get(1));

        searchParameters = new SearchParameters();
        searchParameters.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
        try
        {
            searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("hedgehog", null, null)));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //You can't specify FilterQuery when using the CMIS language
            assertNotNull(iae);
        }

        try
        {
            searchParameters.setLanguage(SearchService.LANGUAGE_SOLR_ALFRESCO);
            searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery(null, null, null)));
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            //You can't specify FilterQuery when using the CMIS language
            assertNotNull(iae);
        }

        searchParameters = new SearchParameters();
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("{!afts}description:xyz", Arrays.asList("desc1", "desc2"), null)));
        assertEquals("{!afts tag=desc1,desc2 }description:xyz" ,searchParameters.getFilterQueries().get(0));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("{!afts}description:xyz", Arrays.asList("desc1"), null)));
        assertEquals("{!afts tag=desc1 }description:xyz" ,searchParameters.getFilterQueries().get(1));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("description:xyz", Arrays.asList("desc1"), null)));
        assertEquals("{!afts tag=desc1 }description:xyz" ,searchParameters.getFilterQueries().get(2));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("{!afts} description:xyz", Arrays.asList("desc1"), null)));
        assertEquals("{!afts tag=desc1 } description:xyz" ,searchParameters.getFilterQueries().get(3));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery(" {!afts cake} description:xyz", Arrays.asList("desc1"), null)));
        assertEquals("{!afts tag=desc1 cake} description:xyz" ,searchParameters.getFilterQueries().get(4));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("{!afts tag=desc1}description:xyz", Arrays.asList("desc1"), null)));
        assertEquals("{!afts tag=desc1}description:xyz" ,searchParameters.getFilterQueries().get(5));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("created:2011", Arrays.asList("desc1"), null)));
        assertEquals("{!afts tag=desc1 }created:2011" ,searchParameters.getFilterQueries().get(6));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("=cm:name:cabbage", Arrays.asList("desc1"), null)));
        assertEquals("{!afts tag=desc1 }=cm:name:cabbage" ,searchParameters.getFilterQueries().get(7));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("{http://www.alfresco.org/model/content/1.0}title:workflow", null, null)));
        assertEquals("{http://www.alfresco.org/model/content/1.0}title:workflow" ,searchParameters.getFilterQueries().get(8));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("{http://www.alfresco.org/model/content/1.0}title:workflow", Arrays.asList("desc1"), null)));
        assertEquals("{!afts tag=desc1 }{http://www.alfresco.org/model/content/1.0}title:workflow" ,searchParameters.getFilterQueries().get(9));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("{!afts} description:xyz", Arrays.asList("desc1", "desc2"), null)));
        assertEquals("{!afts tag=desc1,desc2 }description:xyz" ,searchParameters.getFilterQueries().get(0));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("{ !afts } description:xyz", Arrays.asList("desc1", "desc2"), null)));
        assertEquals("{!afts tag=desc1,desc2 }description:xyz" ,searchParameters.getFilterQueries().get(0));

        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery(null, Arrays.asList("desc1"),  Arrays.asList("cm:name:cabbage", "cm:creator:bob"))));
        assertEquals("{!afts tag=desc1 }cm:name:cabbage OR cm:creator:bob" ,searchParameters.getFilterQueries().get(12));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery(null, null,  Arrays.asList("cm:name:cabbage", "cm:creator:bob"))));
        assertEquals("cm:name:cabbage OR cm:creator:bob" ,searchParameters.getFilterQueries().get(13));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery(null, null,  Arrays.asList("cm:name:cabbage"))));
        assertEquals("cm:name:cabbage" ,searchParameters.getFilterQueries().get(14));
        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("created:2011", null,  Arrays.asList("cm:name:cabbage"))));
        assertEquals("Single query should take precident over multiple queries ORed together.","created:2011" ,searchParameters.getFilterQueries().get(15));
    }

    @Test
    public void fromFacetQuery() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromFacetQuery(searchParameters, null);

        searchMapper.fromFacetQuery(searchParameters, Arrays.asList(new FacetQuery("ping", null,null), new FacetQuery("pong", "table",null)));
        assertEquals(2 ,searchParameters.getFacetQueries().size());
        assertEquals("{!afts key='ping'}ping" ,searchParameters.getFacetQueries().get(0));
        assertEquals("{!afts key='table'}pong" ,searchParameters.getFacetQueries().get(1));

        try
        {
            searchMapper.fromFacetQuery(searchParameters, Arrays.asList(new FacetQuery("ping", null,null),new FacetQuery("{!afts}pang", "tennis",null)));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //Cannot start with afts
            assertNotNull(iae);
        }
    }

    @Test
    public void fromSpelling() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromSpellCheck(searchParameters, null);
        assertFalse(searchParameters.isSpellCheck());

        try
        {
            searchMapper.fromSpellCheck(searchParameters, new Spelling(null));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //Can't be null
            assertNotNull(iae);
        }

        //Now set search term first
        searchParameters.setSearchTerm("fred");
        searchMapper.fromSpellCheck(searchParameters, new Spelling(null));
        assertEquals("fred",searchParameters.getSearchTerm());

        //Now query replaces userQuery (search term)
        searchMapper.fromSpellCheck(searchParameters, new Spelling("favourit"));
        assertEquals("favourit",searchParameters.getSearchTerm());
        assertTrue(searchParameters.isSpellCheck());
    }

    @Test
    public void fromScope() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        searchMapper.setDefaults(searchParameters);

        //Doesn't error, has default store
        searchMapper.fromScope(searchParameters, null);
        assertEquals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,searchParameters.getStores().get(0));

        searchMapper.fromScope(searchParameters, new Scope(null));
        assertEquals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,searchParameters.getStores().get(0));

        try
        {
            searchMapper.fromScope(searchParameters, new Scope(Arrays.asList("nonsense")));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //Must be a valid store ref
            assertNotNull(iae);
        }

        searchMapper.fromScope(searchParameters, new Scope(Arrays.asList(StoreMapper.DELETED, StoreMapper.LIVE_NODES, StoreMapper.VERSIONS)));
        assertEquals(3 ,searchParameters.getStores().size());
        assertEquals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE.toString(),searchParameters.getStores().get(0).toString());
        assertEquals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString(),searchParameters.getStores().get(1).toString());
        assertEquals(StoreMapper.STORE_REF_VERSION2_SPACESSTORE.toString(),searchParameters.getStores().get(2).toString());
    }

    @Test
    public void fromFacetFields() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromFacetFields(searchParameters, null);

        try
        {
            searchMapper.fromFacetFields(searchParameters, new FacetFields(null));
            fail();
        } catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("facetFields facets is a mandatory parameter"));
        }

        try
        {
            searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField(null,null,null,null,null,null,null,null,null,null,null))));
            fail();
        } catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("facetFields facet field is a mandatory parameter"));
        }

        searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield",null,null,null,null,null,null,null,null,null,null))));
        assertEquals(1 ,searchParameters.getFieldFacets().size());
        FieldFacet ff = searchParameters.getFieldFacets().get(0);

        //Check defaults
        //assertEquals(true, ff.getMissing());
        assertNull(ff.getLimitOrNull());
        assertEquals(0, ff.getOffset());
        assertEquals(1, ff.getMinCount());
        assertFalse(ff.isCountDocsMissingFacetField());
        assertEquals(0, ff.getEnumMethodCacheMinDF());

//        assertEquals("{key='myfield'}myfield" ,ff.getField());

        searchParameters = new SearchParameters();
        searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield","mylabel","myprefix",null,null,null,null,null,null,null,null))));

        ff = searchParameters.getFieldFacets().get(0);
//        assertEquals("{key='mylabel'}myfield" ,ff.getField());
        assertEquals("myprefix" ,ff.getPrefix());

        try
        {
            searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield",null,null,"badsort",null,null,null,null,null,null,null))));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //Sort is an enum
            assertNotNull(iae);
        }

        try
        {
            searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield",null,null, null,"badmethod",null,null,null,null,null,null))));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //Method is an enum
            assertNotNull(iae);
        }

        searchParameters = new SearchParameters();
        searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield",null,null,"INDEX","ENUM",null,null,null,null,null,null))));
        ff = searchParameters.getFieldFacets().get(0);
        assertEquals("INDEX" ,ff.getSort().toString());
        assertEquals("ENUM" ,ff.getMethod().toString());
    }

    @Test
    public void fromLimits() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        searchMapper.setDefaults(searchParameters);

        //Doesn't error
        searchMapper.fromLimits(searchParameters, null);
        assertEquals(500, searchParameters.getLimit());
        assertEquals(LimitBy.UNLIMITED, searchParameters.getLimitBy());

        searchMapper.fromLimits(searchParameters, new Limits(null, null));
        assertEquals(LimitBy.UNLIMITED, searchParameters.getLimitBy());
        assertEquals(500, searchParameters.getLimit());

        searchMapper.fromLimits(searchParameters, new Limits(null, 34));
        assertEquals(LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS, searchParameters.getLimitBy());
        assertEquals(34, searchParameters.getMaxPermissionChecks());
        assertEquals(-1, searchParameters.getLimit());
        assertEquals(-1, searchParameters.getMaxPermissionCheckTimeMillis());

        searchParameters = new SearchParameters();
        searchMapper.setDefaults(searchParameters);
        searchMapper.fromLimits(searchParameters, new Limits(1000, null));
        assertEquals(LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS, searchParameters.getLimitBy());
        assertEquals(1000, searchParameters.getMaxPermissionCheckTimeMillis());
        assertEquals(-1, searchParameters.getLimit());
        assertEquals(-1, searchParameters.getMaxPermissionChecks());
    }

    @Test
    public void fromHighlight() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        List<FieldHighlightParameters> fields = Arrays.asList(new FieldHighlightParameters("desc",50,100,false,"@","#"), new FieldHighlightParameters("title",55,105,true,"*","¿"));
        GeneralHighlightParameters highlightParameters = new GeneralHighlightParameters(5, 10, false, "{", "}", 20, true, fields);
        searchMapper.fromHighlight(searchParameters,highlightParameters);
        assertEquals(searchParameters.getHighlight(), highlightParameters);
    }

    @Test
    public void fromStats() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        searchMapper.fromStats(searchParameters, null);

        List<StatsRequestParameters> statsRequestParameters = new ArrayList<>(1);
        statsRequestParameters.add(new StatsRequestParameters(null, null, null, null, null,null, null, null, null,null, null, null,null, null, null, null));

        try
        {
          searchMapper.fromStats(searchParameters, statsRequestParameters);
        }
        catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("field is a mandatory parameter"));
        }

        statsRequestParameters.clear();
        statsRequestParameters.add(new StatsRequestParameters("cm:content", "myLabel", null, null,null, null, null, null,null, null, null, null,null, null, null, null));
        searchMapper.fromStats(searchParameters, statsRequestParameters);
        assertEquals(1 ,searchParameters.getStats().size());

        statsRequestParameters.clear();
        statsRequestParameters.add(new StatsRequestParameters("cm:content", "myLabel", Arrays.asList(3.4f, 12f, 10f), null, null,null, null, null, null,null, null, null,null, null, null, null));
        searchMapper.fromStats(searchParameters, statsRequestParameters);
        assertEquals(1 ,searchParameters.getStats().size());

        statsRequestParameters.clear();
        statsRequestParameters.add(new StatsRequestParameters("cm:content", "myLabel", Arrays.asList(-3.4f), null, null,null, null, null, null,null, null, null,null, null, null, null));

        try
        {
          searchMapper.fromStats(searchParameters, statsRequestParameters);
        }
        catch (IllegalArgumentException iae)
        {
          assertTrue(iae.getLocalizedMessage().contains("Invalid percentile -3.4"));
        }

        statsRequestParameters.clear();
        statsRequestParameters.add(new StatsRequestParameters("cm:content", "myLabel", Arrays.asList(101f),null, null,null, null, null, null,null, null,  null,null, null, null, null));

        try
        {
            searchMapper.fromStats(searchParameters, statsRequestParameters);
        }
        catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("Invalid percentile 101"));
        }

        statsRequestParameters.clear();
        statsRequestParameters.add(new StatsRequestParameters("cm:content", "myLabel", null, null,null, null, null, null,null, null, null, null,null, true, 12f, null));
        try
        {
            searchMapper.fromStats(searchParameters, statsRequestParameters);
        }
        catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("Invalid cardinality accuracy 12.0"));
        }

        statsRequestParameters.clear();
        statsRequestParameters.add(new StatsRequestParameters("cm:content", "myLabel", null, null,null, null, null, null,null, null, null, null,null, null, 12f, null));
        searchMapper.fromStats(searchParameters, statsRequestParameters);
        //cardinality is ignored if not true
        assertEquals(1 ,searchParameters.getStats().size());

    }

    @Test
    public void fromPivot() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        searchMapper.fromPivot(searchParameters, null, null, null, null);

        List<FacetField> facets = new ArrayList<>(1);
        facets.add(new FacetField("myfield",null,null,null,null,null,null,null,null,null,null));
        FacetFields ff = new FacetFields(facets);
        searchMapper.fromFacetFields(searchParameters,ff);
        searchMapper.fromPivot(searchParameters, null, ff, null, null);
        assertEquals(1 ,searchParameters.getFieldFacets().size());
        assertEquals(0 ,searchParameters.getPivots().size());

        //Handle unknown pivot.
        searchParameters = new SearchParameters();

        try
        {
            searchMapper.fromPivot(searchParameters, null, ff, Arrays.asList(new Pivot(null)), null);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            //"bob" doesn't refer to a field facet
            assertNotNull(iae);
        }

        try
        {
            searchMapper.fromPivot(searchParameters, null, ff, Arrays.asList(new Pivot("")), null);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            //"bob" doesn't refer to a field facet
            assertNotNull(iae);
        }

        SearchRequestContext searchRequestContext = SearchRequestContext.from(minimalQuery());

        //"bob" doesn't refer to a field facet but its the last one so lets be kind
        searchMapper.fromPivot(searchParameters, null, ff, Arrays.asList(new Pivot("bob")), searchRequestContext);

        try
        {
            searchMapper.fromPivot(searchParameters, null, ff, Arrays.asList(new Pivot("ken"),new Pivot("bob")), searchRequestContext);
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //"ken" doesn't refer to a field facet and its not the last one
            assertNotNull(iae);
        }

        searchParameters = new SearchParameters();

        searchMapper.fromPivot(searchParameters, null, ff, Arrays.asList(new Pivot("myfield")), searchRequestContext);
        searchMapper.fromFacetFields(searchParameters,ff);
        //Moved from a field facet to a pivot
        assertEquals(0 ,searchParameters.getFieldFacets().size());
        assertEquals(1 ,searchParameters.getPivots().size());
        assertEquals("myfield" ,searchParameters.getPivots().get(0));

    }

    @Test
    public void fromInterval() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        IntervalParameters intervalParameters = new IntervalParameters(null, null);
        try
        {
            searchMapper.fromFacetIntervals(searchParameters, intervalParameters);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            //intervals is required
            assertNotNull(iae);
        }

        List<IntervalSet> intervalSets = Arrays.asList(new IntervalSet(null,"1", null, null, null));
        List<Interval> intervalList = Arrays.asList(new Interval(null, "bob", null));
        intervalParameters = new IntervalParameters(intervalSets,intervalList);

        try
        {
            searchMapper.fromFacetIntervals(searchParameters, intervalParameters);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            //facetIntervals intervals field is required
            assertNotNull(iae);
        }

        intervalList = Arrays.asList(new Interval("aFileld", "bob", null));
        intervalParameters = new IntervalParameters(intervalSets,intervalList);
        try
        {
            searchMapper.fromFacetIntervals(searchParameters, intervalParameters);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            //set start is required
            assertNotNull(iae);
        }

        intervalSets = Arrays.asList(new IntervalSet("1",null, null, null, true));
        intervalParameters = new IntervalParameters(intervalSets,intervalList);

        try
        {
            searchMapper.fromFacetIntervals(searchParameters, intervalParameters);
            fail();
        }
        catch (IllegalArgumentException iae)
        {
            //set end is required
            assertNotNull(iae);
        }

        intervalSets =  new ArrayList<>();
        intervalSets.add(new IntervalSet("0", "3", "bob", null, null));
        intervalSets.add(new IntervalSet("30", "50", "bill", true,true));
        List<IntervalSet> anIntervalSet = new ArrayList<>();
        anIntervalSet.add(new IntervalSet("1", "10", "bert", false, false));
        intervalList = Arrays.asList(new Interval("cm:price", "Price", null), new Interval("cm:price", "Price", anIntervalSet));
        intervalParameters = new IntervalParameters(intervalSets,intervalList);
        searchMapper.fromFacetIntervals(searchParameters, intervalParameters);
        assertEquals(searchParameters.getInterval(), intervalParameters);
    }

    private SearchQuery minimalQuery()
    {
        Query query = new Query("cmis", "foo", "");
        SearchQuery sq = new SearchQuery(query, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        return sq;
    }
    @Test
    public void facetGroup()
    {
        
    }
}
