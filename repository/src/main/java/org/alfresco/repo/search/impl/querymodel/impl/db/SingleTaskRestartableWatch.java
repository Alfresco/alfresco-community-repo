package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.NotThreadSafe;

/*-
 * #%L
 * Alfresco Remote API
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

@NotThreadSafe
public class SingleTaskRestartableWatch
{
    private final String name;

    private long startTimeNanos;
    private long totalTimeNanos;

    public SingleTaskRestartableWatch(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void start()
    {
        startTimeNanos = System.nanoTime();
    }

    public void stop()
    {
        long elapsedNanos = System.nanoTime() - startTimeNanos;
        totalTimeNanos += elapsedNanos;
    }

    public long getTotalTimeNanos()
    {
        return totalTimeNanos;
    }

    public long getTotalTimeMicros()
    {
        return TimeUnit.NANOSECONDS.toMicros(totalTimeNanos);
    }

    public long getTotalTimeMillis()
    {
        return TimeUnit.NANOSECONDS.toMillis(totalTimeNanos);
    }
}
