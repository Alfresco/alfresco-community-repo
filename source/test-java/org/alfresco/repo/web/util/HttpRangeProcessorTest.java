package org.alfresco.repo.web.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.repository.ContentReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for the HttpRangeProcessor class.
 * 
 * @author Ray Gauss II
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpRangeProcessorTest
{
    private HttpRangeProcessor httpRangeProcessor;
    private @Mock ContentReader reader;
    
    @Before
    public void setUp() throws Exception
    {
        httpRangeProcessor = new HttpRangeProcessor(null);
        when(reader.getMimetype()).thenReturn("image/jpeg");
        when(reader.getSize()).thenReturn(19133L);
        when(reader.getContentInputStream()).thenReturn(this.getClass().getResourceAsStream("/test.jpg"));
    }

    @Test
    public void testValidRange() throws IOException
    {
        testRange("700-800", HttpServletResponse.SC_PARTIAL_CONTENT);
    }
    
    @Test
    public void testStartOnlyRange() throws IOException
    {
        testRange("19000-", HttpServletResponse.SC_PARTIAL_CONTENT);
    }
    
    @Test
    public void testNegativeRange() throws IOException
    {
        testRange("800-700", HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }
    
    @Test
    public void testBeyondLengthRange() throws IOException
    {
        testRange("20000-", HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }
    
    protected void testRange(String range, int expectedStatus) throws IOException
    {
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        boolean result = httpRangeProcessor.processRange(response, reader, range, null, null, null, null);
        
        assertTrue(result);
        assertEquals(expectedStatus, response.getStatus());
        reader.getContentInputStream().close();
    }
    
}
