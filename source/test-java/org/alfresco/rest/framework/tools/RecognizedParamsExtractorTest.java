/*
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
package org.alfresco.rest.framework.tools;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.parameters.InvalidSelectException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.tests.core.ParamsExtender;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test the RecognizedParamsExtractor
 *
 * @author Gethin James
 */
public class RecognizedParamsExtractorTest implements RecognizedParamsExtractor
{


    @Test
    public void getFilterTest()
    {
        BeanPropertiesFilter theFilter  = getFilter(null);
        assertNotNull(theFilter);
        assertTrue("Null passed in so must return the default BeanPropertiesFilter.ALLOW_ALL class", BeanPropertiesFilter.AllProperties.class.equals(theFilter.getClass()));

        theFilter  = getFilter("bob");
        assertNotNull(theFilter);
        assertTrue("Must return the BeanPropertiesFilter class", theFilter instanceof BeanPropertiesFilter);

        theFilter  = getFilter("50,fred,b.z");
        assertNotNull(theFilter);
        assertTrue("Must return the BeanPropertiesFilter class", theFilter instanceof BeanPropertiesFilter);

        theFilter  = getFilter("50,fred,");
        assertNotNull(theFilter);
        assertTrue("Must return the BeanPropertiesFilter class", theFilter instanceof BeanPropertiesFilter);
    }


    @Test
    public void getSortingTest()
    {
        List<SortColumn> theSort  = getSort(null);
        assertNotNull(theSort);
        assertTrue("Null passed in so empty sort list should be returned.", theSort.isEmpty());

        theSort  = getSort("name ASC");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);

        theSort  = getSort("name ");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);

        theSort  = getSort("name DESC");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(!theSort.get(0).asc);  //desc

        theSort  = getSort("name desc");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(!theSort.get(0).asc);  //desc

        theSort  = getSort("name,age desc");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 2);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);
        assertEquals("age", theSort.get(1).column);
        assertTrue(!theSort.get(1).asc);  //desc

        theSort  = getSort(" name, age desc");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 2);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);
        assertEquals("age", theSort.get(1).column);
        assertTrue(!theSort.get(1).asc);  //desc

        theSort  = getSort("name DESC, age desc");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 2);
        assertEquals("name", theSort.get(0).column);
        assertTrue(!theSort.get(0).asc);  //desc
        assertEquals("age", theSort.get(1).column);
        assertTrue(!theSort.get(1).asc);  //desc

        theSort  = getSort("age DeSc, name AsC");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 2);
        assertEquals("age", theSort.get(0).column);
        assertTrue(!theSort.get(0).asc);  //desc
        assertEquals("name", theSort.get(1).column);
        assertTrue(theSort.get(1).asc);
        
        theSort  = getSort("name asc,");  // ok for now, trailing comma is ignored
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);
        
        try
        {
            getSort("age asc, name des");  // invalid, should be desc
            fail("Should throw an InvalidArgumentException");
        }
        catch (InvalidArgumentException error)
        {
            // this is correct
        }
        
        try
        {
            getSort("age asc name");  // invalid, missing comma
            fail("Should throw an InvalidArgumentException");
        }
        catch (InvalidArgumentException error)
        {
            // this is correct
        }
    }

    @Test
    public void getIncludeClauseTest()
    {
        getClauseTest("include");
    }

    @Test
    public void getSelectClauseTest()
    {
        getClauseTest("select");
    }

    // at the moment select and include are parsed the same way, hence common/shared test
    private void getClauseTest(String paramName)
    {
        List<String> theClause = getCorrectClause(paramName, null);
        assertNotNull(theClause);
        assertFalse("Null passed in so nothing in the "+paramName, theClause.size() > 0);

        try
        {
            theClause = getCorrectClause(paramName, ",,,");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }

        try
        {
            theClause = getCorrectClause(paramName, "(,,,");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }

        try
        {
            theClause = getCorrectClause(paramName, "(,,,)");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }

        try
        {
            theClause = getCorrectClause(paramName, "x/,z");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }

        try
        {
            theClause = getCorrectClause(paramName, "/x'n,/z");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }

        try
        {
            theClause = getCorrectClause(paramName, "/foo/0");
            fail("Should throw an InvalidSelectException. Legal identifiers must start with a letter not zero");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }

        try
        {
            theClause = getCorrectClause(paramName, "/");
            fail("Should throw an InvalidSelectException. No identifier specified.");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }

        try
        {
            theClause = getCorrectClause(paramName,  "path, isLink");
            fail("Should throw an InvalidSelectException. No identifier specified.");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }

        theClause = getCorrectClause(paramName, "king/kong");
        assertTrue("has a valid "+paramName, theClause.size() == 1);
        assertEquals("king/kong",theClause.get(0));

        theClause = getCorrectClause(paramName, "x,y");
        assertTrue("has a valid "+paramName, theClause.size() == 2);
        assertEquals("x",theClause.get(0));
        assertEquals("y",theClause.get(1));

        theClause = getCorrectClause(paramName, "x,/z");
        assertTrue("has a valid "+paramName, theClause.size() == 2);
        assertEquals("x",theClause.get(0));
        assertEquals("/z",theClause.get(1));

        theClause = getCorrectClause(paramName, "/b");
        assertTrue("has a valid "+paramName, theClause.size() == 1);
        assertEquals("/b",theClause.get(0));

        theClause = getCorrectClause(paramName, "/be,/he");
        assertTrue("has a valid "+paramName, theClause.size() == 2);
        assertEquals("/be",theClause.get(0));
        assertEquals("/he",theClause.get(1));

        theClause = getCorrectClause(paramName, "/king/kong");
        assertTrue("has a valid "+paramName, theClause.size() == 1);
        assertEquals("/king/kong",theClause.get(0));

        theClause = getCorrectClause(paramName, "/name,/person/age");
        assertTrue("has a valid "+paramName, theClause.size() == 2);
        assertEquals("/name",theClause.get(0));
        assertEquals("/person/age",theClause.get(1));

        theClause = getCorrectClause(paramName, "/foo");
        assertTrue("has a valid select",theClause.size() == 1);
        assertEquals("/foo",theClause.get(0));

        theClause = getCorrectClause(paramName, "/foo/anArray/x");
        assertTrue("has a valid "+paramName, theClause.size() == 1);
        assertEquals("/foo/anArray/x",theClause.get(0));

        theClause = getCorrectClause(paramName, "/foo/anArray/x,/person/age,/eggs/bacon/sausage,/p");
        assertTrue("has a valid "+paramName, theClause.size() == 4);
        assertEquals("/foo/anArray/x",theClause.get(0));
        assertEquals("/person/age",theClause.get(1));
        assertEquals("/eggs/bacon/sausage",theClause.get(2));
        assertEquals("/p",theClause.get(3));

        theClause = getCorrectClause(paramName, "/foo/_bar ");
        assertTrue("has a valid "+paramName, theClause.size() == 1);
        assertEquals("/foo/_bar",theClause.get(0));
    }

    private List<String> getCorrectClause(String paramName, String paramValue)
    {
        if (paramName.equalsIgnoreCase("include"))
        {
            return getIncludeClause(paramValue);
        }
        else if (paramName.equalsIgnoreCase("select"))
        {
            return getSelectClause(paramValue);
        }

        fail("Unexpected clause: "+paramName);
        return null;
    }

    @Test
    public void getRelationFilterTest()
    {
        Map<String, BeanPropertiesFilter> theFilter  = getRelationFilter(null);
        assertNotNull(theFilter);
        assertTrue("Null passed in so nothing to filter.",theFilter.isEmpty());

        theFilter  = getRelationFilter("bob");
        assertNotNull(theFilter);
        assertTrue("Must be a single relationship", theFilter.size() == 1);
        assertTrue("Must be a single relationship called bob", theFilter.containsKey("bob"));
        BeanPropertiesFilter aFilter = theFilter.get("bob");
        assertTrue("No bean properties specified so need a BeanPropertiesFilter.ALLOW_ALL class", BeanPropertiesFilter.AllProperties.class.equals(aFilter.getClass()));

        theFilter  = getRelationFilter("bob,hope");
        assertNotNull(theFilter);
        assertTrue("Must be a two relationships", theFilter.size() == 2);
        assertTrue("Must have hope.", theFilter.containsKey("hope"));
        aFilter = theFilter.get("hope");
        assertTrue("No bean properties specified so need a BeanPropertiesFilter.ALLOW_ALL class", BeanPropertiesFilter.AllProperties.class.equals(aFilter.getClass()));

        theFilter  = getRelationFilter("bob(name),hope");
        assertNotNull(theFilter);
        assertTrue("Must be a two relationships", theFilter.size() == 2);
        assertTrue("Must have bob.", theFilter.containsKey("bob"));
        aFilter = theFilter.get("bob");
        assertTrue("Bean properties specified so must be an BeanPropertiesFilter class", BeanPropertiesFilter.class.equals(aFilter.getClass()));

        theFilter  = getRelationFilter("bob,hope(age,name)");
        assertNotNull(theFilter);
        assertTrue("Must be a two relationships", theFilter.size() == 2);
        aFilter = theFilter.get("bob");
        assertTrue("No bean properties specified so need a BeanPropertiesFilter.ALLOW_ALL class", BeanPropertiesFilter.AllProperties.class.equals(aFilter.getClass()));
        aFilter = theFilter.get("hope");
        assertTrue("Bean properties specified so must be an BeanPropertiesFilter class", BeanPropertiesFilter.class.equals(aFilter.getClass()));


        theFilter  = getRelationFilter("bob(name,age),nohope,hope(height,width)");
        assertNotNull(theFilter);
        assertTrue("Must be a three relationships", theFilter.size() == 3);
        aFilter = theFilter.get("bob");
        assertTrue("Bean properties specified so must be an BeanPropertiesFilter class", BeanPropertiesFilter.class.equals(aFilter.getClass()));
        aFilter = theFilter.get("nohope");
        assertTrue("No bean properties specified so need a ReturnAllBeanProperties class", BeanPropertiesFilter.AllProperties.class.equals(aFilter.getClass()));
        aFilter = theFilter.get("hope");
        assertTrue("Bean properties specified so must be an BeanPropertiesFilter class", BeanPropertiesFilter.class.equals(aFilter.getClass()));

    }


    @Test
    public void findPagingTest()
    {
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("34");
        when(request.getParameter("maxItems")).thenReturn("50");

        Paging pagin = findPaging(request);
        assertNotNull(pagin);
        assertTrue(pagin.getSkipCount() == 34);
        assertTrue(pagin.getMaxItems() == 50);

        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn(null);
        when(request.getParameter("maxItems")).thenReturn(null);
        pagin = findPaging(request);
        assertNotNull(pagin);
        assertTrue(pagin.getSkipCount() == Paging.DEFAULT_SKIP_COUNT);
        assertTrue(pagin.getMaxItems() == Paging.DEFAULT_MAX_ITEMS);

        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("55");
        pagin = findPaging(request);
        assertNotNull(pagin);
        assertTrue(pagin.getSkipCount() == 55);
        assertTrue(pagin.getMaxItems() == Paging.DEFAULT_MAX_ITEMS);

        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn(null);
        when(request.getParameter("maxItems")).thenReturn("45");
        pagin = findPaging(request);
        assertNotNull(pagin);
        assertTrue(pagin.getMaxItems() == 45);
        assertTrue(pagin.getSkipCount() == Paging.DEFAULT_SKIP_COUNT);

        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("apple");
        when(request.getParameter("maxItems")).thenReturn("pear");
        try
        {
            pagin = findPaging(request);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exceptions
        }

        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("0");
        when(request.getParameter("maxItems")).thenReturn("0");
        try
        {
            pagin = findPaging(request);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exceptions
        }

        //Test Case cloud-2198
        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("0");
        when(request.getParameter("maxItems")).thenReturn("a");
        try
        {
            pagin = findPaging(request);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exceptions
        }

        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("s");
        when(request.getParameter("maxItems")).thenReturn("5");
        try
        {
            pagin = findPaging(request);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exceptions
        }

        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("0");
        when(request.getParameter("maxItems")).thenReturn("-2");
        try
        {
            pagin = findPaging(request);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exceptions
        }

        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("-3");
        when(request.getParameter("maxItems")).thenReturn("5");
        try
        {
            pagin = findPaging(request);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exceptions
        }

        request = mock(WebScriptRequest.class);
        when(request.getParameter("maxItems")).thenReturn("5");
        pagin = findPaging(request);
        assertNotNull(pagin);
        assertTrue("skip count defaults to 0", pagin.getSkipCount() == Paging.DEFAULT_SKIP_COUNT);

        //End of Test Case cloud-2198
    }

    @Test
    public void paramsTest()
    {
        Map<String,List<String>> mockParams = new HashMap<String,List<String>>();
        mockParams.put("age", Arrays.asList("23","45"));
        mockParams.put("name", Arrays.asList("fred"));
        WebScriptRequest request = mockRequest(mockParams);
        Map<String, String[]> params = getRequestParameters(request);
        assertNotNull(params);
        Params paramObj = ParamsExtender.valueOf(params);
        assertNotNull(paramObj);
        String aValue = paramObj.getParameter("age");
        assertEquals("23", aValue);

        aValue = paramObj.getParameter("name");
        assertEquals("fred", aValue);

    }

    private WebScriptRequest mockRequest(final Map<String,List<String>> params)
    {
        final String[] paramNames = params.keySet().toArray(new String[]{});
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getParameterNames()).thenReturn(paramNames);
        when(request.getParameterValues(anyString())).thenAnswer(new Answer<String[]>() {
            @Override
            public String[] answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return params.get((String) args[0]).toArray(new String[]{});
            }
        });
        return request;
    }

}
