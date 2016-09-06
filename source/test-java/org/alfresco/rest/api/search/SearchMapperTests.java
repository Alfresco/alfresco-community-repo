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
import static org.alfresco.service.cmr.repository.StoreRef.PROTOCOL_DELETED;
import static org.alfresco.service.cmr.repository.StoreRef.PROTOCOL_TEST;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_CMIS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.alfresco.rest.api.search.impl.SearchMapper;
import org.alfresco.rest.api.search.model.Default;
import org.alfresco.rest.api.search.model.FacetField;
import org.alfresco.rest.api.search.model.FacetFields;
import org.alfresco.rest.api.search.model.FacetQuery;
import org.alfresco.rest.api.search.model.FilterQuery;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.api.search.model.Scope;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.api.search.model.SortDef;
import org.alfresco.rest.api.search.model.Spelling;
import org.alfresco.rest.api.search.model.Template;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchService;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests the SearchMapper class
 *
 * @author Gethin James
 */
public class SearchMapperTests
{

    static SearchMapper searchMapper = new SearchMapper();
    static SerializerTestHelper helper = new SerializerTestHelper();

    @Test(expected = IllegalArgumentException.class)
    public void testMandatory() throws Exception
    {
        SearchParameters searchParameters = searchMapper.toSearchParameters(SearchQuery.EMPTY);
    }

    @Test
    public void toSearchParameters() throws Exception
    {
        SearchParameters searchParameters = searchMapper.toSearchParameters(minimalQuery());
        assertNotNull(searchParameters);

        //Test defaults
        assertEquals("There should be only 1 default store", 1,searchParameters.getStores().size());
        assertEquals("workspaces store is the default", StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, searchParameters.getStores().get(0));
        assertEquals(LimitBy.FINAL_SIZE, searchParameters.getLimitBy());
        assertEquals(100, searchParameters.getMaxItems());

        searchParameters = searchMapper.toSearchParameters(helper.searchQueryFromJson());
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

        try
        {
            searchMapper.fromQuery(searchParameters, q);
            fail();
        } catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("language is a mandatory parameter"));
        }

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
        assertEquals(searchParameters.getMaxItems(),paging.getMaxItems());
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

        searchMapper.fromFilterQuery(searchParameters, Arrays.asList(new FilterQuery("hedgehog", null), new FilterQuery("king", Arrays.asList("not", "used"))));
        assertEquals(2 ,searchParameters.getFilterQueries().size());
        assertEquals("hedgehog" ,searchParameters.getFilterQueries().get(0));
        assertEquals("king" ,searchParameters.getFilterQueries().get(1));

        //tags aren't used at the moment
    }

    @Test
    public void fromFacetQuery() throws Exception
    {
        SearchParameters searchParameters = new SearchParameters();
        //Doesn't error
        searchMapper.fromFacetQuery(searchParameters, null);

        searchMapper.fromFacetQuery(searchParameters, Arrays.asList(new FacetQuery("ping", null), new FacetQuery("pong", "table")));
        assertEquals(2 ,searchParameters.getFacetQueries().size());
        assertEquals("{!afts key='ping'}ping" ,searchParameters.getFacetQueries().get(0));
        assertEquals("{!afts key='table'}pong" ,searchParameters.getFacetQueries().get(1));

        try
        {
            searchMapper.fromFacetQuery(searchParameters, Arrays.asList(new FacetQuery("ping", null),new FacetQuery("{!afts}pang", "tennis")));
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

        searchMapper.fromScope(searchParameters, new Scope(Arrays.asList(
                    new StoreRef(PROTOCOL_TEST, "SpacesStore").toString(),
                    new StoreRef(PROTOCOL_DELETED, "SpacesStore").toString())));
        assertEquals(2 ,searchParameters.getStores().size());
        assertEquals("test://SpacesStore",searchParameters.getStores().get(0).toString());
        assertEquals("deleted://SpacesStore",searchParameters.getStores().get(1).toString());
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
            searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField(null,null,null,null,null,null,null,null,null,null))));
            fail();
        } catch (IllegalArgumentException iae)
        {
            assertTrue(iae.getLocalizedMessage().contains("facetFields facet field is a mandatory parameter"));
        }

        searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield",null,null,null,null,null,null,null,null,null))));
        assertEquals(1 ,searchParameters.getFieldFacets().size());
        FieldFacet ff = searchParameters.getFieldFacets().get(0);

        //Check defaults
        //assertEquals(true, ff.getMissing());
        assertNull(ff.getLimitOrNull());
        assertEquals(0, ff.getOffset());
        assertEquals(0, ff.getMinCount());
        assertEquals(0, ff.getEnumMethodCacheMinDF());

//        assertEquals("{key='myfield'}myfield" ,ff.getField());

        searchParameters = new SearchParameters();
        searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield","mylabel","myprefix",null,null,null,null,null,null,null))));

        ff = searchParameters.getFieldFacets().get(0);
//        assertEquals("{key='mylabel'}myfield" ,ff.getField());
        assertEquals("myprefix" ,ff.getPrefix());

        try
        {
            searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield",null,null,"badsort",null,null,null,null,null,null))));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //Sort is an enum
            assertNotNull(iae);
        }

        try
        {
            searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield",null,null, null,"badmethod",null,null,null,null,null))));
            fail();
        }
        catch (InvalidArgumentException iae)
        {
            //Method is an enum
            assertNotNull(iae);
        }

        searchParameters = new SearchParameters();
        searchMapper.fromFacetFields(searchParameters, new FacetFields(Arrays.asList(new FacetField("myfield",null,null,"INDEX","ENUM",null,null,null,null,null))));
        ff = searchParameters.getFieldFacets().get(0);
        assertEquals("INDEX" ,ff.getSort().toString());
        assertEquals("ENUM" ,ff.getMethod().toString());
    }

    private SearchQuery minimalQuery()
    {
        Query query = new Query("cmis", "foo", "");
        SearchQuery sq = new SearchQuery(query,null, null, null, null, null, null, null, null, null, null);
        return sq;
    }
}
