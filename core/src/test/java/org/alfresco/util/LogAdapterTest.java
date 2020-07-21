/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test class for LogAdapter.
 * 
 * @author Alan Davis
 */
public class LogAdapterTest
{
    @Mock
    Log log;
    
    LogAdapter adapter;
    
    Throwable throwable;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        
        adapter = new LogAdapter(log) { };
        throwable = new Exception();
    }

    @Test
    public void traceTest()
    {
        adapter.trace("");
        adapter.trace("", throwable);
        verify(log).trace("", null);
        verify(log).trace("", throwable);
        
        when(log.isTraceEnabled()).thenReturn(true);
        assertTrue("", adapter.isTraceEnabled());
        
        when(log.isTraceEnabled()).thenReturn(false);
        assertFalse("", adapter.isTraceEnabled());
    }

    @Test
    public void debugTest()
    {
        adapter.debug("");
        adapter.debug("", throwable);
        verify(log).debug("", null);
        verify(log).debug("", throwable);
        
        when(log.isDebugEnabled()).thenReturn(true);
        assertTrue("", adapter.isDebugEnabled());
        
        when(log.isDebugEnabled()).thenReturn(false);
        assertFalse("", adapter.isDebugEnabled());
    }

    @Test
    public void infoTest()
    {
        adapter.info("");
        adapter.info("", throwable);
        verify(log).info("", null);
        verify(log).info("", throwable);
        
        when(log.isInfoEnabled()).thenReturn(true);
        assertTrue("", adapter.isInfoEnabled());
        
        when(log.isInfoEnabled()).thenReturn(false);
        assertFalse("", adapter.isInfoEnabled());
    }

    @Test
    public void warnTest()
    {
        adapter.warn("");
        adapter.warn("", throwable);
        verify(log).warn("", null);
        verify(log).warn("", throwable);
        
        when(log.isWarnEnabled()).thenReturn(true);
        assertTrue("", adapter.isWarnEnabled());
        
        when(log.isWarnEnabled()).thenReturn(false);
        assertFalse("", adapter.isWarnEnabled());
    }

    @Test
    public void errorTest()
    {
        adapter.error("");
        adapter.error("", throwable);
        verify(log).error("", null);
        verify(log).error("", throwable);
        
        when(log.isErrorEnabled()).thenReturn(true);
        assertTrue("", adapter.isErrorEnabled());
        
        when(log.isErrorEnabled()).thenReturn(false);
        assertFalse("", adapter.isErrorEnabled());
    }

    @Test
    public void fatalTest()
    {
        adapter.fatal("");
        adapter.fatal("", throwable);
        verify(log).fatal("", null);
        verify(log).fatal("", throwable);
        
        when(log.isFatalEnabled()).thenReturn(true);
        assertTrue("", adapter.isFatalEnabled());
        
        when(log.isFatalEnabled()).thenReturn(false);
        assertFalse("", adapter.isFatalEnabled());
    }
    
    @Test
    public void nullTest()
    {
        adapter = new LogAdapter(null) { };

        adapter.trace("");
        adapter.trace("", throwable);
        adapter.debug("");
        adapter.debug("", throwable);
        adapter.info("");
        adapter.info("", throwable);
        adapter.warn("");
        adapter.warn("", throwable);
        adapter.error("");
        adapter.error("", throwable);
        adapter.fatal("");
        adapter.fatal("", throwable);
        verify(log, times(0)).trace("", null);
        verify(log, times(0)).trace("", throwable);
        verify(log, times(0)).debug("", null);
        verify(log, times(0)).debug("", throwable);
        verify(log, times(0)).info("", null);
        verify(log, times(0)).info("", throwable);
        verify(log, times(0)).warn("", null);
        verify(log, times(0)).warn("", throwable);
        verify(log, times(0)).error("", null);
        verify(log, times(0)).error("", throwable);
        verify(log, times(0)).fatal("", null);
        verify(log, times(0)).fatal("", throwable);
        
        when(log.isTraceEnabled()).thenReturn(true);
        assertFalse("", adapter.isTraceEnabled());
        
        when(log.isTraceEnabled()).thenReturn(false);
        assertFalse("", adapter.isTraceEnabled());
        
        when(log.isDebugEnabled()).thenReturn(true);
        assertFalse("", adapter.isDebugEnabled());
        
        when(log.isDebugEnabled()).thenReturn(false);
        assertFalse("", adapter.isDebugEnabled());
        
        when(log.isInfoEnabled()).thenReturn(true);
        assertFalse("", adapter.isInfoEnabled());
        
        when(log.isInfoEnabled()).thenReturn(false);
        assertFalse("", adapter.isInfoEnabled());
        
        when(log.isWarnEnabled()).thenReturn(true);
        assertFalse("", adapter.isWarnEnabled());
        
        when(log.isWarnEnabled()).thenReturn(false);
        assertFalse("", adapter.isWarnEnabled());
        
        when(log.isErrorEnabled()).thenReturn(true);
        assertFalse("", adapter.isErrorEnabled());
        
        when(log.isErrorEnabled()).thenReturn(false);
        assertFalse("", adapter.isErrorEnabled());
        
        when(log.isFatalEnabled()).thenReturn(true);
        assertFalse("", adapter.isFatalEnabled());
        
        when(log.isFatalEnabled()).thenReturn(false);
        assertFalse("", adapter.isFatalEnabled());
    }
}
