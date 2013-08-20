
package org.alfresco.rest.framework.tests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.rest.api.model.Comment;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.ReturnAllBeanProperties;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper.Writer;
import org.alfresco.rest.framework.jacksonextensions.RestJsonModule;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests Jackson Json logic
 * 
 * @author Gethin James
 */
public class JsonJacksonTests
{

    static JacksonHelper jsonHelper = null;

    public static final String FARMER_JSON = "{\"name\":\"Giles\",\"created\":\"2012-03-23T15:56:18.552+0000\",\"age\":54,\"id\":\"1234A3\",\"farm\":\"LARGE\"}";
    public static final String COLLECTION_START = "[ ";
    public static final String COLLECTION_END = " ]";
    public static final String FARMERS_COLLECTION_JSON = COLLECTION_START + FARMER_JSON + "," + FARMER_JSON + "," + FARMER_JSON + COLLECTION_END;
    
    
    @BeforeClass
    public static void setupTests() throws Exception
    {
        jsonHelper = new JacksonHelper();
        RestJsonModule module = new RestJsonModule();
        jsonHelper.setModule(module);
        jsonHelper.afterPropertiesSet();
    }
    
    @Test
    public void testDeserializeFarmer() throws IOException
    {
        Farmer aFarmer = jsonHelper.construct(new StringReader(FARMER_JSON), Farmer.class);
        assertEquals(Farmer.class, aFarmer.getClass());
        assertEquals("Giles", aFarmer.getName());
        assertEquals(54, aFarmer.getAge());
        assertEquals(Farmer.size.LARGE, aFarmer.getFarm());
        
        try
        {
            aFarmer = jsonHelper.construct(new StringReader(""), Farmer.class);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exception
        }
    }
    
    @Test
    public void testDeserializeFarmersAsList() throws IOException
    {
        List<Farmer> aFarmer = jsonHelper.constructList(new StringReader(COLLECTION_START+FARMER_JSON+COLLECTION_END), Farmer.class);
        assertTrue(Collection.class.isAssignableFrom(aFarmer.getClass()));
        assertEquals(1, aFarmer.size());
        
        aFarmer = jsonHelper.constructList(new StringReader(FARMERS_COLLECTION_JSON), Farmer.class);
        assertTrue(Collection.class.isAssignableFrom(aFarmer.getClass()));
        assertEquals(3, aFarmer.size());
        
        aFarmer = jsonHelper.constructList(new StringReader(FARMER_JSON), Farmer.class);
        assertTrue(Collection.class.isAssignableFrom(aFarmer.getClass()));
        assertEquals(1, aFarmer.size());
        
        try
        {
            aFarmer = jsonHelper.constructList(new StringReader(""), Farmer.class);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exception
        }
        
        try
        {
            aFarmer = jsonHelper.constructList(new StringReader("rubbish"), Farmer.class);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exception
        }
        
        try
        {
            aFarmer = jsonHelper.constructList(new StringReader("[]"), Farmer.class);
            fail("Should not get here.");
        }
        catch (InvalidArgumentException iae)
        {
            assertNotNull(iae); // Must throw this exception
        }
        
    }
    
    @Test
    public void testDeserializeComment() throws IOException
    {
        String json = "{\"title\":\"fred\", \"content\":\"lots of noise\"}";
        Comment aComment = jsonHelper.construct(new StringReader(json), Comment.class);
        assertEquals(Comment.class, aComment.getClass());
        assertEquals("fred", aComment.getTitle());
        assertEquals("lots of noise", aComment.getContent());
    }

    @Test
    public void testSerializeComment() throws IOException
    {
        final Comment aComment = new Comment();
        aComment.setContent("<b>There it is</b>");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonHelper.withWriter(out, new Writer()
        {
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                        throws JsonGenerationException, JsonMappingException, IOException
            {
                FilterProvider fp = new SimpleFilterProvider().addFilter(
                            JacksonHelper.DEFAULT_FILTER_NAME, new ReturnAllBeanProperties());
                objectMapper.writer(fp).writeValue(generator, aComment);
            }
        });
        assertTrue(out.toString().contains("{\"content\":\"<b>There it is</b>\""));
    }

    @Test
    public void testSerializeMultipleObjects() throws IOException
    {
        final Collection<Comment> allComments = new ArrayList<Comment>();
        Comment aComment = new Comment();
        aComment.setContent("<b>There it is</b>");
        allComments.add(aComment);
        aComment = new Comment();
        aComment.setContent("<p>I agree with the author</p>");
        allComments.add(aComment);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonHelper.withWriter(out, new Writer()
        {
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                        throws JsonGenerationException, JsonMappingException, IOException
            {
                FilterProvider fp = new SimpleFilterProvider().addFilter(
                            JacksonHelper.DEFAULT_FILTER_NAME, new ReturnAllBeanProperties());
                objectMapper.writer(fp).writeValue(generator, allComments);
            }
        });
        assertTrue(out.toString().contains("content\":\"<b>There it is</b>"));
        assertTrue(out.toString().contains("content\":\"<p>I agree with the author</p>"));
    }

    @Test
    public void testNullInComment() throws IOException
    {
        final Comment aComment = new Comment();
        aComment.setContent(null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        jsonHelper.withWriter(out, new Writer()
        {
            @Override
            public void writeContents(JsonGenerator generator, ObjectMapper objectMapper)
                        throws JsonGenerationException, JsonMappingException, IOException
            {
                FilterProvider fp = new SimpleFilterProvider().addFilter(
                            JacksonHelper.DEFAULT_FILTER_NAME, new ReturnAllBeanProperties());
                objectMapper.writer(fp).writeValue(generator, aComment);
            }
        });
        assertEquals("Null values should not be output.", "{\"canEdit\":false,\"canDelete\":false}",
                    out.toString());
    }
}
