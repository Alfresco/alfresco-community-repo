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

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Utility functions guided by the
 * <a href="http://en.wikipedia.org/wiki/Normal_distribution">Normal Distribution</a>.
 * 
 * @author Derek Hulley
 * @since 5.1
 */
public class NormalDistributionHelper
{
    private final NormalDistribution normalDistribution;
    
    /**
     * Use a simple normal distribution to generate random numbers
     */
    public NormalDistributionHelper()
    {
        this.normalDistribution = new NormalDistribution();
    }
    
    /**
     * Get a random long where a standard deviation of 1.0 corresponds to the
     * min and max values provided.  The sampling is repeated until a value is
     * found within the range given.
     */
    public long getValue(long min, long max)
    {
        if (min > max)
        {
            throw new IllegalArgumentException("Min must less than or equal to max.");
        }
        
        double sample = -2.0;
        // Keep sampling until we get something within bounds of the standard deviation
        while (sample < -1.0 || sample > 1.0)
        {
            sample = normalDistribution.sample();
        }
        long halfRange = (max - min)/2L;
        long mean = min + halfRange;
        long ret = mean + (long) (halfRange * sample);
        // Done
        return ret;
    }
}