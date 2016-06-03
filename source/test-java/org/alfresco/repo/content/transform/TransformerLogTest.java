package org.alfresco.repo.content.transform;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for TransformerLog.
 * 
 * @author Alan Davis
 */
public class TransformerLogTest
{
    @Mock
    private TransformerDebug transformerDebug;
    
    @Mock
    private TransformerConfig transformerConfig;
    
    private TransformerLog log;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        log = new TransformerLog();
        log.setTransformerDebug(transformerDebug);
        log.setTransformerConfig(transformerConfig);
    }

    static void assertLogEntriesEquals(String[] expected, String[] actual)
    {
        // Strip the date prefix
        int beginIndex = (TransformerLogger.DATE_FORMAT.format(new Date())+' ').length();
        for (int i=actual.length-1; i >= 0; i--)
        {
            actual[i] = actual[i].substring(beginIndex);
        }
        assertArrayEquals(expected, actual);
    }

    @Test
    public void noEntriesDisabledTest()
    {
        assertArrayEquals(new String[] {"No entries are available. transformer.log.entries must be set to a number between 1 and 1000"}, log.getEntries(10));
    }
    
    @Test
    public void oneEntryTest()
    {
        when(transformerConfig.getProperty("transformer.log.entries")).thenReturn("3");
        log.debug("one");
        
        assertLogEntriesEquals(new String[] {"one"}, log.getEntries(10));
    }

    @Test
    public void fiveEntryTest()
    {
        when(transformerConfig.getProperty("transformer.log.entries")).thenReturn("3");

        log.debug("one");
        log.debug("two");
        log.debug("three");
        log.debug("four");
        log.debug("five");
        
        assertLogEntriesEquals(new String[] {"five", "four", "three"}, log.getEntries(10));
    }
}
