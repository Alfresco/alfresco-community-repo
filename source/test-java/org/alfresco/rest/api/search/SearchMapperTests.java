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
import static org.junit.Assert.assertNull;
import org.alfresco.rest.api.search.impl.SearchMapper;
import org.alfresco.rest.api.search.model.Default;
import org.alfresco.rest.api.search.model.FacetQuery;
import org.alfresco.rest.api.search.model.FilterQuery;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.api.search.model.SortDef;
import org.alfresco.rest.api.search.model.Template;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.SearchParameters;
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
        searchMapper.fromSort(searchParameters, Arrays.asList(new SortDef("FIELD", "createdAt", true)));
        assertEquals(1 , searchParameters.getSortDefinitions().size());
        assertEquals("{http://www.alfresco.org/model/content/1.0}created", searchParameters.getSortDefinitions().get(0).getField());

        searchParameters = new SearchParameters();
        searchMapper.fromSort(searchParameters, Arrays.asList(new SortDef("FIELD", "cm:created", true)));
        assertEquals("cm:created", searchParameters.getSortDefinitions().get(0).getField());

        searchParameters = new SearchParameters();
        searchMapper.fromSort(searchParameters, Arrays.asList(new SortDef("FIELD", "modifiedByUser", true)));
        assertEquals("{http://www.alfresco.org/model/content/1.0}modifier", searchParameters.getSortDefinitions().get(0).getField());

        searchParameters = new SearchParameters();
        searchMapper.fromSort(searchParameters, Arrays.asList(new SortDef("FIELD", "nodeType", true)));
        assertEquals("{}TYPE", searchParameters.getSortDefinitions().get(0).getField());

        searchParameters = new SearchParameters();
        searchParameters.setLanguage(SearchMapper.CMIS);
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

        searchMapper.fromFacetQuery(searchParameters, Arrays.asList(new FacetQuery("ping", null), new FacetQuery("pong", "table"), new FacetQuery("pang", "tennis")));
        assertEquals(3 ,searchParameters.getFacetQueries().size());
        assertEquals("ping" ,searchParameters.getFacetQueries().get(0));
        assertEquals("pong" ,searchParameters.getFacetQueries().get(1));
        assertEquals("pang" ,searchParameters.getFacetQueries().get(2));

        //label isn't used at the moment
    }


    private SearchQuery minimalQuery()
    {
        Query query = new Query("cmis", "foo", "");
        SearchQuery sq = new SearchQuery(query,null, null, null, null, null, null, null);
        return sq;
    }
}
