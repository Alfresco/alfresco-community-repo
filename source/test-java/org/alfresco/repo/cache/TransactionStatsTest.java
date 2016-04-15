/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.cache;

import static org.junit.Assert.*;

import org.alfresco.repo.cache.TransactionStats.OpType;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link TransactionStats} class.
 * 
 * @since 5.0
 * @author Matt Ward
 */
public class TransactionStatsTest
{

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void canGetCacheOperationCountsWhenNoOpsTakenPlaceYet()
    {
        TransactionStats stats = new TransactionStats();
        
        // No data has been added yet
        assertTrue(OpType.values().length > 0);
        for (OpType op : OpType.values())
        {            
            assertEquals(0, stats.getCount(op));
        }
    }

    @Test
    public void canRecordSomeOpsAndGetTheirValues()
    {
        TransactionStats stats = new TransactionStats();
        
        // Some hits
        stats.record(0, 1000, OpType.GET_HIT);
        stats.record(2000, 4000, OpType.GET_HIT);
        stats.record(3000, 4000, OpType.GET_HIT);
        // Some misses
        stats.record(0, 1000, OpType.GET_MISS);
        stats.record(0, 2000, OpType.GET_MISS);
        stats.record(0, 3000, OpType.GET_MISS);
        stats.record(8000, 9000, OpType.GET_MISS);
        stats.record(0, 2000, OpType.GET_MISS);
        stats.record(0, 3000, OpType.GET_MISS);
        // Some puts
        stats.record(1500, 2500, OpType.PUT);
        stats.record(100, 2100, OpType.PUT);
        // Some removes
        stats.record(0, 1000, OpType.REMOVE);
        // Some clears
        stats.record(0, 1000, OpType.CLEAR);
        stats.record(0, 2000, OpType.CLEAR);
        stats.record(0, 2000, OpType.CLEAR);
        stats.record(0, 2000, OpType.CLEAR);
        
        // Counts
        assertEquals(3, stats.getCount(OpType.GET_HIT));
        assertEquals(6, stats.getCount(OpType.GET_MISS));
        assertEquals(2, stats.getCount(OpType.PUT));
        assertEquals(1, stats.getCount(OpType.REMOVE));
        assertEquals(4, stats.getCount(OpType.CLEAR));
        
        // Mean operation times
        assertEquals(1333.33, stats.getTimings(OpType.GET_HIT).getMean(), 0.01d);
        assertEquals(2000, stats.getTimings(OpType.GET_MISS).getMean(), 0.01d);
        assertEquals(1500, stats.getTimings(OpType.PUT).getMean(), 0.01d);
        assertEquals(1000, stats.getTimings(OpType.REMOVE).getMean(), 0.01d);
        assertEquals(1750, stats.getTimings(OpType.CLEAR).getMean(), 0.01d);
    }
}
