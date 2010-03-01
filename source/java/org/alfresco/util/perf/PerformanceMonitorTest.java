/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.util.perf;

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Enabled vm performance monitoring for <b>performance.summary.vm</b> and
 * <b>performance.PerformanceMonitorTest</b> to check.
 * 
 * @see org.alfresco.util.perf.PerformanceMonitor
 * 
 * @author Derek Hulley
 */
public class PerformanceMonitorTest extends TestCase
{
    private PerformanceMonitor testTimingMonitor;
    
    @Override
    public void setUp() throws Exception
    {
        Method testTimingMethod = PerformanceMonitorTest.class.getMethod("testTiming");
        testTimingMonitor = new PerformanceMonitor("PerformanceMonitorTest", "testTiming");
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(testTimingMonitor);
    }
    
    public synchronized void testTiming() throws Exception
    {
        testTimingMonitor.start();
        
        wait(50);
        
        testTimingMonitor.stop();
    }
}
