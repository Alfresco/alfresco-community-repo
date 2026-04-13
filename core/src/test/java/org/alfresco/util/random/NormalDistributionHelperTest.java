/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.util.random;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @see NormalDistributionHelper
 * 
 * @author Derek Hulley
 * @since 5.1
 */
public class NormalDistributionHelperTest
{
    private NormalDistributionHelper normalDistribution = new NormalDistributionHelper();
    
    @Test
    public void testGetValue_Fail()
    {
        try
        {
            normalDistribution.getValue(5L, -5L);
            fail("Min-max relation not detected.");
        }
        catch (IllegalArgumentException e)
        {
            // Expected
        }
    }

    @Test
    public void testGetValue_Precise()
    {
        assertEquals(10L, normalDistribution.getValue(10L, 10L));
        assertEquals(0L, normalDistribution.getValue(0L, 0L));
        assertEquals(-10L, normalDistribution.getValue(-10L, -10L));
    }

    @Test
    public void testGetValue_Repeated()
    {
        for (int i = 0; i < 1000; i++)
        {
            long value = normalDistribution.getValue(-1*i, i);
            assertTrue("Min not respected", value >= -1*i);
            assertTrue("Max not respected", value <= i);
        }
    }
}