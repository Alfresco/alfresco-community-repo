/*
 * Copyright (C) 2013 Alfresco Software Limited.
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
package org.alfresco.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for LogTee.
 * 
 * @author Alan Davis
 */
public class LogTeeTest
{
    @Mock
    Log log1;
    
    @Mock
    Log log2;
    
    LogTee tee;
    
    Throwable throwable;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        tee = new LogTee(log1, log2) { };
        throwable = new Exception();
    }

    @Test
    public void traceTest()
    {
        tee.trace("");
        tee.trace("", throwable);
        verify(log1).trace("", null);
        verify(log1).trace("", throwable);
        verify(log2).trace("", null);
        verify(log2).trace("", throwable);
        
        when(log1.isTraceEnabled()).thenReturn(true);
        assertTrue("", tee.isTraceEnabled());
        
        when(log2.isTraceEnabled()).thenReturn(true);
        assertTrue("", tee.isTraceEnabled());

        when(log1.isTraceEnabled()).thenReturn(false);
        assertTrue("", tee.isTraceEnabled());
        
        when(log2.isTraceEnabled()).thenReturn(false);
        assertFalse("", tee.isTraceEnabled());

        when(log2.isTraceEnabled()).thenReturn(true);
        assertTrue("", tee.isTraceEnabled());
    }

    @Test
    public void debugTest()
    {
        tee.debug("");
        tee.debug("", throwable);
        verify(log1).debug("", null);
        verify(log1).debug("", throwable);
        verify(log2).debug("", null);
        verify(log2).debug("", throwable);
        
        
        when(log1.isDebugEnabled()).thenReturn(true);
        assertTrue("", tee.isDebugEnabled());
        
        when(log2.isDebugEnabled()).thenReturn(true);
        assertTrue("", tee.isDebugEnabled());

        when(log1.isDebugEnabled()).thenReturn(false);
        assertTrue("", tee.isDebugEnabled());
        
        when(log2.isDebugEnabled()).thenReturn(false);
        assertFalse("", tee.isDebugEnabled());

        when(log2.isDebugEnabled()).thenReturn(true);
        assertTrue("", tee.isDebugEnabled());
    }

    @Test
    public void infoTest()
    {
        tee.info("");
        tee.info("", throwable);
        verify(log1).info("", null);
        verify(log1).info("", throwable);
        verify(log2).info("", null);
        verify(log2).info("", throwable);
        
        
        when(log1.isInfoEnabled()).thenReturn(true);
        assertTrue("", tee.isInfoEnabled());
        
        when(log2.isInfoEnabled()).thenReturn(true);
        assertTrue("", tee.isInfoEnabled());

        when(log1.isInfoEnabled()).thenReturn(false);
        assertTrue("", tee.isInfoEnabled());
        
        when(log2.isInfoEnabled()).thenReturn(false);
        assertFalse("", tee.isInfoEnabled());

        when(log2.isInfoEnabled()).thenReturn(true);
        assertTrue("", tee.isInfoEnabled());
    }

    @Test
    public void warnTest()
    {
        tee.warn("");
        tee.warn("", throwable);
        verify(log1).warn("", null);
        verify(log1).warn("", throwable);
        verify(log2).warn("", null);
        verify(log2).warn("", throwable);
        
        
        when(log1.isWarnEnabled()).thenReturn(true);
        assertTrue("", tee.isWarnEnabled());
        
        when(log2.isWarnEnabled()).thenReturn(true);
        assertTrue("", tee.isWarnEnabled());

        when(log1.isWarnEnabled()).thenReturn(false);
        assertTrue("", tee.isWarnEnabled());
        
        when(log2.isWarnEnabled()).thenReturn(false);
        assertFalse("", tee.isWarnEnabled());

        when(log2.isWarnEnabled()).thenReturn(true);
        assertTrue("", tee.isWarnEnabled());
    }

    @Test
    public void errorTest()
    {
        tee.error("");
        tee.error("", throwable);
        verify(log1).error("", null);
        verify(log1).error("", throwable);
        verify(log2).error("", null);
        verify(log2).error("", throwable);
        
        
        when(log1.isErrorEnabled()).thenReturn(true);
        assertTrue("", tee.isErrorEnabled());
        
        when(log2.isErrorEnabled()).thenReturn(true);
        assertTrue("", tee.isErrorEnabled());

        when(log1.isErrorEnabled()).thenReturn(false);
        assertTrue("", tee.isErrorEnabled());
        
        when(log2.isErrorEnabled()).thenReturn(false);
        assertFalse("", tee.isErrorEnabled());

        when(log2.isErrorEnabled()).thenReturn(true);
        assertTrue("", tee.isErrorEnabled());
    }

    @Test
    public void fatalTest()
    {
        tee.fatal("");
        tee.fatal("", throwable);
        verify(log1).fatal("", null);
        verify(log1).fatal("", throwable);
        verify(log2).fatal("", null);
        verify(log2).fatal("", throwable);
        
        
        when(log1.isFatalEnabled()).thenReturn(true);
        assertTrue("", tee.isFatalEnabled());
        
        when(log2.isFatalEnabled()).thenReturn(true);
        assertTrue("", tee.isFatalEnabled());

        when(log1.isFatalEnabled()).thenReturn(false);
        assertTrue("", tee.isFatalEnabled());
        
        when(log2.isFatalEnabled()).thenReturn(false);
        assertFalse("", tee.isFatalEnabled());

        when(log2.isFatalEnabled()).thenReturn(true);
        assertTrue("", tee.isFatalEnabled());
    }
}
