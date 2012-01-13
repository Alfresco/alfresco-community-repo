/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.content;


import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Test class for time and byte limits on an AbstractContentReader
 * 
 * @author Alan Davis
 */
public class AbstractContentReaderLimitTest
{
    private static final int K = 1024;
    private static final int M = K*K;
    
    // Normal test file size 5K
    private static final long SIZE = 5*K;

    // Normal test delay between returning bytes, giving a run time of about 5 seconds if not interrupted.
    private static final long MS_PER_BYTE = 1;
    
    // Large test file size 100 MB with no delay takes about a second to read.
    private static final long LARGE_SIZE = 100*M;

    // Top speed to read 1 MB via the DummyAbstractContentReader
    // Assume about 10ms normally per MB, so use half that and a high
    // MARGIN_OF_ERROR_PERCENTAGE_FAST.
    private static final long MS_PER_MB = 5;
    
    // Margins of error when using a DummyAbstractContentReader
    // with or without a delay. Used to make sure different runs
    // don't result in failing tests but at the same time that
    // they will if there is a real problem.
    private static final int MARGIN_OF_ERROR_PERCENTAGE_SLOW = 50;
    private static final int MARGIN_OF_ERROR_PERCENTAGE_FAST = 900;
    
    private DummyAbstractContentReader reader;
    private TransformationOptionLimits limits;
    private TransformerDebug transformerDebug;
    private long minTime;
    private long maxTime;
    private long minLength;
    private long maxLength;

    @Before
    public void setUp() throws Exception
    {
        ApplicationContext ctx = ContentMinimalContextTestSuite.getContext();
        transformerDebug = (TransformerDebug) ctx.getBean("transformerDebug");

        limits = new TransformationOptionLimits();
        reader = new DummyAbstractContentReader(SIZE, MS_PER_BYTE);
        reader.setLimits(limits);
        reader.setTransformerDebug(transformerDebug);
        
        // Without the following, the bytes from the DummyAbstractContentReader are read in 4K blocks
        // so the test to do with timeouts and read limits will not work, as they expect to read 1K per
        // second. Not an issue in the real world as read rates are much higher, so a buffer makes no
        // difference to limit checking. It does make a vast difference to performance when the InputStream
        // is wrapped in a InputStreamReader as is done by a number of transformers.
        reader.setUseBufferedInputStream(false);
    }

    @Test
    public void noLimitTest() throws Exception
    {
        readAndCheck();
    }
    
    @Test(expected=ContentIOException.class)
    public void maxKBytesTest() throws Exception
    {
        limits.setMaxSourceSizeKBytes(1);
        readAndCheck();
    }
    
    @Test(expected=ContentIOException.class)
    public void maxTimeTest() throws Exception
    {
        limits.setTimeoutMs(1000);
        readAndCheck();
    }

    @Test(expected=ContentIOException.class)
    public void maxTimeAndKBytesTest() throws Exception
    {
        limits.setTimeoutMs(1000);
        limits.setMaxSourceSizeKBytes(1);
        readAndCheck();
    }
    
    @Test
    public void limitKBytesTest() throws Exception
    {
        limits.setReadLimitKBytes(1);
        readAndCheck();
    }
    
    @Test
    public void limitTimeTest() throws Exception
    {
        limits.setReadLimitTimeMs(1000);
        readAndCheck();
    }

    @Test
    public void limitTimeAndKBytesTest() throws Exception
    {
        limits.setReadLimitTimeMs(1000);
        limits.setReadLimitKBytes(1);
        readAndCheck();
    }

    @Test
    public void fullSpeedReader() throws Exception
    {
        // Check that we have not slowed down reading of large files.
        reader = new DummyAbstractContentReader(LARGE_SIZE, 0);
        reader.setLimits(limits);
        reader.setTransformerDebug(transformerDebug);
        reader.setUseBufferedInputStream(true);

        readAndCheck();
    }

    private void readAndCheck() throws Exception
    {
        Exception exception = null;
        
        long length = 0;
        long time = System.currentTimeMillis();
        try
        {
            String content = reader.getContentString();
            length = content.length();
        }
        catch(ContentIOException e)
        {
            exception = e;
        }
        time = System.currentTimeMillis() - time;
        
        calcMaxMinValues();

        System.out.printf("Time %04d %04d..%04d length %04d %04d..%04d %s\n",
                time, minTime, maxTime,
                length, minLength, maxLength,
                (exception == null ? "" : exception.getClass().getSimpleName()));
        
        assertTrue("Reader is too fast ("+time+"ms range is "+minTime+"..."+maxTime+"ms)", time >= minTime);
        assertTrue("Reader is too slow ("+time+"ms range is "+minTime+"..."+maxTime+"ms)", time <= maxTime);
        
        if (exception != null)
            throw exception;
        
        assertTrue("Content is too short ("+length+" bytes range is "+minLength+"..."+maxLength+")", length >= minLength);
        assertTrue("Content is too long ("+length+" bytes range is "+minLength+"..."+maxLength+")", length <= maxLength);
    }

    private void calcMaxMinValues()
    {
        long size = reader.size;

        long timeout = limits.getTimePair().getValue();
        assertTrue("The test time value ("+timeout+
            "ms) should be lowered given the file size ("+size+
            ") and the margin of error (of "+marginOfError(timeout)+"ms)",
            timeout <= 0 || msToBytes(timeout+marginOfError(timeout)) <= size);

        long readKBytes = limits.getKBytesPair().getValue();
        long readBytes = readKBytes * K;
        assertTrue("The test KByte value ("+readKBytes+
            "K) should be lowered given the file size ("+size+
            ") and the margin of error (of "+marginOfError(readBytes)+"bytes)",
            readBytes <= 0 || readBytes+marginOfError(readBytes) <= size);

        long bytes = (readBytes > 0) ? readBytes : size;
        long readTime = bytesToMs(bytes);

        minTime = (timeout > 0) ? Math.min(timeout, readTime) : readTime;
        maxTime = minTime + marginOfError(minTime);
        minLength = (timeout > 0) ? msToBytes(minTime-marginOfError(minTime)) : bytes;
        maxLength = (timeout > 0) ? Math.min(msToBytes(maxTime), size) : bytes;
    }
    
    private long msToBytes(long ms)
    {
        return (reader.msPerByte > 0)
            ? ms / reader.msPerByte
            : ms / MS_PER_MB * M;
    }
    
    private long bytesToMs(long bytes)
    {
        return (reader.msPerByte > 0)
            ? bytes * reader.msPerByte
            : bytes * MS_PER_MB / M;
    }
    
    private long marginOfError(long value)
    {
        return
            value *
            ((reader.msPerByte > 0)
             ? MARGIN_OF_ERROR_PERCENTAGE_SLOW
             : MARGIN_OF_ERROR_PERCENTAGE_FAST) /
            100;
    }

    /**
     * A dummy AbstractContentReader that returns a given number of bytes
     * (all 'a') very slowly. There is a configurable delay returning each byte.
     * Used to test timeouts and read limits.
     */
    public static class DummyAbstractContentReader extends AbstractContentReader
    {
        final long size;
        final long msPerByte;
        
        /**
         * @param size of the dummy data
         * @param msPerByte milliseconds between byte reads
         */
        public DummyAbstractContentReader(long size, long msPerByte)
        {
            super("a");
            this.size = size;
            this.msPerByte = msPerByte;
        }
        
        /**
         * @return  Returns an instance of the this class
         */
        @Override
        protected ContentReader createReader() throws ContentIOException
        {
            return new DummyAbstractContentReader(size, msPerByte);
        }

        @Override
        protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
        {
            InputStream is = new InputStream()
            {
                long read = 0;
                long start = 0;
                
                @Override
                public int read() throws IOException
                {
                    if (read >= size)
                        return -1;
                    
                    read++; 

                    if (msPerByte > 0)
                    {
                        long elapse = System.currentTimeMillis() - start;
                        if (read == 1)
                        {
                            start = elapse;
                        }
                        else
                        {
                            // On Windows it is possible to just wait 1 ms per byte but this
                            // does not work on linux hence (end up with a full read taking
                            // 40 seconds rather than 5) the need to wait if elapse time
                            // is too fast.
                            long delay = (read * msPerByte) - elapse;
                            if (delay > 0)
                            {
                                try
                                {
                                    Thread.sleep(delay);
                                }
                                catch (InterruptedException e)
                                {
                                    // ignore
                                }
                            }
                        }
                    }
                    
                    return 'a';
                }

                // Just a way to tell AbstractContentReader not to wrap the ChannelInputStream
                // in a BufferedInputStream
                @Override
                public boolean markSupported()
                {
                    return true;
                }
            };
            return Channels.newChannel(is);
        }

        public boolean exists()
        {
            return true;
        }

        public long getLastModified()
        {
            return 0L;
        }

        public long getSize()
        {
            return size;
        }
    };
}
