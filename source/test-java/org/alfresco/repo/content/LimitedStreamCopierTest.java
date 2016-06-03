package org.alfresco.repo.content;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link LimitedStreamCopier}.
 * 
 * @author Neil Mc Erlean
 * @since Thor
 */
public class LimitedStreamCopierTest
{
    private final static byte[] ZERO_BYTE_ARRAY =        "".getBytes();
    private final static byte[] SHORT_BYTE_ARRAY =       "This string is shorter than the limit".getBytes();
    private final static byte[] LIMIT_SIZED_BYTE_ARRAY = "This string's length exactly equals the limit".getBytes();
    private final static byte[] LONG_BYTE_ARRAY =        "This test string is longer than the limit. Tum te tum te tum te tum.".getBytes();
    
    private final static int SIZE_LIMIT = LIMIT_SIZED_BYTE_ARRAY.length;
    
    private LimitedStreamCopier streamCopier = new LimitedStreamCopier();
    private InputStream in;
    private ByteArrayOutputStream out;
    
    @Before public void initStreamCopier()
    {
        out = new ByteArrayOutputStream();
    }
    
    /**
     * Test copying a simple byte[]
     */
    @Test public void copyStreamSuccessful() throws Exception
    {
        in = new ByteArrayInputStream(SHORT_BYTE_ARRAY);
        
        int bytesCopied = streamCopier.copyStreams(in, out, SIZE_LIMIT);
        
        assertArrayEquals(SHORT_BYTE_ARRAY, out.toByteArray());
        assertEquals(SHORT_BYTE_ARRAY.length, bytesCopied);
    }
    
    /**
     * Test copying an empty byte[]
     */
    @Test public void copyStreamSuccessfulZeroBytes() throws Exception
    {
        in = new ByteArrayInputStream(ZERO_BYTE_ARRAY);
        
        int bytesCopied = streamCopier.copyStreams(in, out, SIZE_LIMIT);
        
        assertArrayEquals(ZERO_BYTE_ARRAY, out.toByteArray());
        assertEquals(ZERO_BYTE_ARRAY.length, bytesCopied);
    }
    
    /**
     * Test copying a byte[] that exceeds the limit.
     */
    @Test (expected=ContentLimitViolationException.class)
    public void copyStreamUnsuccessfulLimitExceeded() throws Exception
    {
        in = new ByteArrayInputStream(LONG_BYTE_ARRAY);
        
        int bytesCopied = streamCopier.copyStreams(in, out, SIZE_LIMIT);
        
        assertArrayEquals(LONG_BYTE_ARRAY, out.toByteArray());
        assertEquals(LONG_BYTE_ARRAY.length, bytesCopied);
    }
    
    /**
     * Test copying a byte[] that equals the limit.
     */
    @Test
    public void copyStreamSuccessfulLimitHit() throws Exception
    {
        in = new ByteArrayInputStream(LIMIT_SIZED_BYTE_ARRAY);
        
        int bytesCopied = streamCopier.copyStreams(in, out, SIZE_LIMIT);
        
        assertArrayEquals(LIMIT_SIZED_BYTE_ARRAY, out.toByteArray());
        assertEquals(LIMIT_SIZED_BYTE_ARRAY.length, bytesCopied);
    }
    
    /**
     * Test copying a simple byte[] with no limit sent.
     */
    @Test public void copyStreamSuccessfulBecauseLimitNotImposed() throws Exception
    {
        in = new ByteArrayInputStream(LONG_BYTE_ARRAY);
        
        int bytesCopied = streamCopier.copyStreams(in, out, -1);
        
        assertArrayEquals(LONG_BYTE_ARRAY, out.toByteArray());
        assertEquals(LONG_BYTE_ARRAY.length, bytesCopied);
    }
}
