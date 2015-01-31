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
package org.alfresco.repo.cache;


/**
 * Represents a single cache operation type's statistics.
 * For example, the cummalative time spent performing a cache's remove operation
 * and the number of times that the remove was performed.
 * <p>
 * Instances are immutable.
 */
public final class OperationStats
{
    /** Total time spent in operations of this type */
    private final double totalTime;
    /** Count of how many instances of this operation occurred. */
    private final long count;
    
    public OperationStats(double totalTime, long count)
    {
        this.totalTime = totalTime;
        this.count = count;
    }
    
    public OperationStats(OperationStats source, double totalTime, long count)
    {
        if (Double.compare(source.totalTime, Double.NaN) == 0)
        {
            // No previous time to add new time to.
            this.totalTime = totalTime;
        }
        else
        {
            this.totalTime = source.totalTime + totalTime;
        }
        this.count = source.count + count;
    }
    
    public double meanTime()
    {
        return totalTime / count;
    }

    public double getTotalTime()
    {
        return this.totalTime;
    }

    public long getCount()
    {
        return this.count;
    }
}
