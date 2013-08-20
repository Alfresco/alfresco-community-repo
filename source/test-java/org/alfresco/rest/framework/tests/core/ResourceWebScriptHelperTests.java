package org.alfresco.rest.framework.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.parameters.InvalidSelectException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.alfresco.rest.framework.tests.api.mocks.Goat;
import org.alfresco.rest.framework.tests.api.mocks.Grass;
import org.alfresco.rest.framework.tests.api.mocks.UniqueIdMethodButNoSetter;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Tests methods on ResourceWebScriptHelper
 *
 * @author Gethin James
 */
public class ResourceWebScriptHelperTests
{

    
    @Test
    public void getFilterTest()
    {
        BeanPropertiesFilter theFilter  = ResourceWebScriptHelper.getFilter(null);
        assertNotNull(theFilter);
        assertTrue("Null passed in so must return the default BeanPropertiesFilter.ALLOW_ALL class", BeanPropertiesFilter.AllProperties.class.equals(theFilter.getClass()));
        
        theFilter  = ResourceWebScriptHelper.getFilter("bob");
        assertNotNull(theFilter);
        assertTrue("Must return the BeanPropertiesFilter class", theFilter instanceof BeanPropertiesFilter);
        
        theFilter  = ResourceWebScriptHelper.getFilter("50,fred,b.z");
        assertNotNull(theFilter);
        assertTrue("Must return the BeanPropertiesFilter class", theFilter instanceof BeanPropertiesFilter);
        
        theFilter  = ResourceWebScriptHelper.getFilter("50,fred,");
        assertNotNull(theFilter);
        assertTrue("Must return the BeanPropertiesFilter class", theFilter instanceof BeanPropertiesFilter);
    }

    
    @Test
    public void getSortingTest()
    {
        List<SortColumn> theSort  = ResourceWebScriptHelper.getSort(null);
        assertNotNull(theSort);
        assertTrue("Null passed in so empty sort list should be returned.", theSort.isEmpty());
               
        theSort  = ResourceWebScriptHelper.getSort("name ASC");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);
        
        theSort  = ResourceWebScriptHelper.getSort("name ");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);
        
        theSort  = ResourceWebScriptHelper.getSort("name DESC");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(!theSort.get(0).asc);  //desc

        theSort  = ResourceWebScriptHelper.getSort("name desc");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(!theSort.get(0).asc);  //desc
        
        theSort  = ResourceWebScriptHelper.getSort("name,age desc");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 2);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);
        assertEquals("age", theSort.get(1).column);
        assertTrue(!theSort.get(1).asc);  //desc
        
        theSort  = ResourceWebScriptHelper.getSort(" name, age desc");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 2);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);
        assertEquals("age", theSort.get(1).column);
        assertTrue(!theSort.get(1).asc);  //desc
        
        theSort  = ResourceWebScriptHelper.getSort("name DESC, age desc");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 2);
        assertEquals("name", theSort.get(0).column);
        assertTrue(!theSort.get(0).asc);  //desc
        assertEquals("age", theSort.get(1).column);
        assertTrue(!theSort.get(1).asc);  //desc

        theSort  = ResourceWebScriptHelper.getSort("age Desc, name Asc");
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 2);
        assertEquals("age", theSort.get(0).column);
        assertTrue(!theSort.get(0).asc);  //desc
        assertEquals("name", theSort.get(1).column);
        assertTrue(theSort.get(1).asc);
        
        theSort  = ResourceWebScriptHelper.getSort("name des");  //invalid, should be desc
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);  //Defaults to ascending because the sort order was invalid

        theSort  = ResourceWebScriptHelper.getSort("name asc,");  //invalid, should be desc
        assertNotNull(theSort);
        assertTrue("Must have a value for column: NAME", !theSort.isEmpty());
        assertTrue(theSort.size() == 1);
        assertEquals("name", theSort.get(0).column);
        assertTrue(theSort.get(0).asc);
    }

    @Test
    public void getSelectClauseTest()
    {
        List<String> theSelect = ResourceWebScriptHelper.getSelectClause(null);
        assertNotNull(theSelect);
        assertFalse("Null passed in so nothing in the Select.",theSelect.size() > 0);
        
        try
        {
            theSelect  = ResourceWebScriptHelper.getSelectClause(",,,");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }
        
        try
        {
            theSelect  = ResourceWebScriptHelper.getSelectClause("(,,,");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }
        
        try
        {
            theSelect  = ResourceWebScriptHelper.getSelectClause("(,,,)");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidSelectException error)
        {
            //this is correct
        }
        
        try
        {
            theSelect  = ResourceWebScriptHelper.getSelectClause("x/,z");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }
        
        try
        {
            theSelect  = ResourceWebScriptHelper.getSelectClause("/x'n,/z");
            fail("Should throw an InvalidSelectException");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        } 
      
        try
        {
            theSelect  = ResourceWebScriptHelper.getSelectClause("/foo/0");
            fail("Should throw an InvalidSelectException. Legal identifiers must start with a letter not zero");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }        
        
        try
        {
            theSelect  = ResourceWebScriptHelper.getSelectClause("/");
            fail("Should throw an InvalidSelectException. No identifier specified.");
        }
        catch (InvalidArgumentException error)
        {
            //this is correct
        }              
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("king/kong");
        assertTrue("has a valid select",theSelect.size() == 1);
        assertEquals("king/kong",theSelect.get(0));
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("x,y");
        assertTrue("has a valid select",theSelect.size() == 2);
        assertEquals("x",theSelect.get(0));
        assertEquals("y",theSelect.get(1));
    
        theSelect  = ResourceWebScriptHelper.getSelectClause("x,/z");
        assertTrue("has a valid select",theSelect.size() == 2);
        assertEquals("x",theSelect.get(0));
        assertEquals("/z",theSelect.get(1));
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("/b");
        assertTrue("has a valid select",theSelect.size() == 1);
        assertEquals("/b",theSelect.get(0));
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("/be,/he");
        assertTrue("has a valid select",theSelect.size() == 2);
        assertEquals("/be",theSelect.get(0));
        assertEquals("/he",theSelect.get(1));
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("/king/kong");
        assertTrue("has a valid select",theSelect.size() == 1);
        assertEquals("/king/kong",theSelect.get(0));
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("/name,/person/age");
        assertTrue("has a valid select",theSelect.size() == 2);
        assertEquals("/name",theSelect.get(0));
        assertEquals("/person/age",theSelect.get(1));
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("/foo");
        assertTrue("has a valid select",theSelect.size() == 1);
        assertEquals("/foo",theSelect.get(0));
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("/foo/anArray/x");
        assertTrue("has a valid select",theSelect.size() == 1);
        assertEquals("/foo/anArray/x",theSelect.get(0));
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("/foo/anArray/x,/person/age,/eggs/bacon/sausage,/p");
        assertTrue("has a valid select",theSelect.size() == 4);
        assertEquals("/foo/anArray/x",theSelect.get(0));
        assertEquals("/person/age",theSelect.get(1));
        assertEquals("/eggs/bacon/sausage",theSelect.get(2));
        assertEquals("/p",theSelect.get(3));
        
        theSelect  = ResourceWebScriptHelper.getSelectClause("/foo/_bar ");
        assertTrue("has a valid select",theSelect.size() == 1);
        assertEquals("/foo/_bar",theSelect.get(0));
    }
 
    @Test
    public void getRelationFilterTest()
    {
        Map<String, BeanPropertiesFilter> theFilter  = ResourceWebScriptHelper.getRelationFilter(null);
        assertNotNull(theFilter);
        assertTrue("Null passed in so nothing to filter.",theFilter.isEmpty());
        
        theFilter  = ResourceWebScriptHelper.getRelationFilter("bob");
        assertNotNull(theFilter);
        assertTrue("Must be a single relationship", theFilter.size() == 1);
        assertTrue("Must be a single relationship called bob", theFilter.containsKey("bob"));
        BeanPropertiesFilter aFilter = theFilter.get("bob");
        assertTrue("No bean properties specified so need a BeanPropertiesFilter.ALLOW_ALL class", BeanPropertiesFilter.AllProperties.class.equals(aFilter.getClass()));
        
        theFilter  = ResourceWebScriptHelper.getRelationFilter("bob,hope");
        assertNotNull(theFilter);
        assertTrue("Must be a two relationships", theFilter.size() == 2);
        assertTrue("Must have hope.", theFilter.containsKey("hope"));
        aFilter = theFilter.get("hope");
        assertTrue("No bean properties specified so need a BeanPropertiesFilter.ALLOW_ALL class", BeanPropertiesFilter.AllProperties.class.equals(aFilter.getClass()));
        
        theFilter  = ResourceWebScriptHelper.getRelationFilter("bob(name),hope");
        assertNotNull(theFilter);
        assertTrue("Must be a two relationships", theFilter.size() == 2);
        assertTrue("Must have bob.", theFilter.containsKey("bob"));
        aFilter = theFilter.get("bob");
        assertTrue("Bean properties specified so must be an BeanPropertiesFilter class", BeanPropertiesFilter.class.equals(aFilter.getClass()));
   
        theFilter  = ResourceWebScriptHelper.getRelationFilter("bob,hope(age,name)");
        assertNotNull(theFilter);
        assertTrue("Must be a two relationships", theFilter.size() == 2);
        aFilter = theFilter.get("bob");
        assertTrue("No bean properties specified so need a BeanPropertiesFilter.ALLOW_ALL class", BeanPropertiesFilter.AllProperties.class.equals(aFilter.getClass()));
        aFilter = theFilter.get("hope");
        assertTrue("Bean properties specified so must be an BeanPropertiesFilter class", BeanPropertiesFilter.class.equals(aFilter.getClass()));   
 

        theFilter  = ResourceWebScriptHelper.getRelationFilter("bob(name,age),nohope,hope(height,width)");
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
    public void setUniqueIdTest()
    {
        Farmer f = new Farmer("1234");
        ResourceWebScriptHelper.setUniqueId(f, "2345");
        assertEquals("2345",f.getId());
     
        Goat g = new Goat();
        ResourceWebScriptHelper.setUniqueId(g, "Gruff");
        assertEquals("Gruff",g.getName());

        Grass grass = new Grass("56");
        ResourceWebScriptHelper.setUniqueId(grass, "No chance");
        assertNotNull("There should not be an error, errors should be swallowed up.",grass);
        
        UniqueIdMethodButNoSetter invalidbj = new UniqueIdMethodButNoSetter();
        ResourceWebScriptHelper.setUniqueId(invalidbj, "error");
        assertNotNull("There should not be an error, errors should be swallowed up.",invalidbj);
    }
    
    @Test
    public void findPagingTest()
    {
        WebScriptRequest request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("34");
        when(request.getParameter("maxItems")).thenReturn("50");
        
        Paging pagin = ResourceWebScriptHelper.findPaging(request); 
        assertNotNull(pagin);
        assertTrue(pagin.getSkipCount() == 34);
        assertTrue(pagin.getMaxItems() == 50);
        
        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn(null);
        when(request.getParameter("maxItems")).thenReturn(null);
        pagin = ResourceWebScriptHelper.findPaging(request);
        assertNotNull(pagin);
        assertTrue(pagin.getSkipCount() == Paging.DEFAULT_SKIP_COUNT);
        assertTrue(pagin.getMaxItems() == Paging.DEFAULT_MAX_ITEMS);
        
        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("55");
        pagin = ResourceWebScriptHelper.findPaging(request);
        assertNotNull(pagin);
        assertTrue(pagin.getSkipCount() == 55);
        assertTrue(pagin.getMaxItems() == Paging.DEFAULT_MAX_ITEMS);
        
        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn(null);
        when(request.getParameter("maxItems")).thenReturn("45");
        pagin = ResourceWebScriptHelper.findPaging(request);
        assertNotNull(pagin);
        assertTrue(pagin.getMaxItems() == 45);
        assertTrue(pagin.getSkipCount() == Paging.DEFAULT_SKIP_COUNT);
        
        request = mock(WebScriptRequest.class);
        when(request.getParameter("skipCount")).thenReturn("apple");
        when(request.getParameter("maxItems")).thenReturn("pear");
        try
        {
            pagin = ResourceWebScriptHelper.findPaging(request);
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
            pagin = ResourceWebScriptHelper.findPaging(request);
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
            pagin = ResourceWebScriptHelper.findPaging(request);
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
            pagin = ResourceWebScriptHelper.findPaging(request);
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
            pagin = ResourceWebScriptHelper.findPaging(request);
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
            pagin = ResourceWebScriptHelper.findPaging(request);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exceptions
        }
        
        request = mock(WebScriptRequest.class);
        when(request.getParameter("maxItems")).thenReturn("5");
        pagin = ResourceWebScriptHelper.findPaging(request);
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
        Map<String, String[]> params = ResourceWebScriptHelper.getRequestParameters(request);
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
