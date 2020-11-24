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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import org.springframework.util.StopWatch;

public final class DBStats
{
    public static final ThreadLocal<StopWatch> QUERY_STOPWATCH = new ThreadLocal<StopWatch>();
    public static final ThreadLocal<SingleTaskRestartableWatch> ACL_READ_STOPWATCH = new ThreadLocal<SingleTaskRestartableWatch>();
    public static final ThreadLocal<SingleTaskRestartableWatch> ACL_OWNER_STOPWATCH = new ThreadLocal<SingleTaskRestartableWatch>();
    public static final ThreadLocal<SingleTaskRestartableWatch> HANDLER_STOPWATCH = new ThreadLocal<SingleTaskRestartableWatch>();
    
    private DBStats() {}
    
    public static void resetStopwatches() {
        QUERY_STOPWATCH.set(new StopWatch());
        HANDLER_STOPWATCH.set(new SingleTaskRestartableWatch("tot"));
        ACL_READ_STOPWATCH.set(new SingleTaskRestartableWatch("acl"));
        ACL_OWNER_STOPWATCH.set(new SingleTaskRestartableWatch("own"));
    }
    
    public static StopWatch queryStopWatch() {
        return QUERY_STOPWATCH.get();
    }
    
    public static SingleTaskRestartableWatch aclReadStopWatch() {
        return ACL_READ_STOPWATCH.get();
    }
    
    public static SingleTaskRestartableWatch aclOwnerStopWatch() {
        return ACL_OWNER_STOPWATCH.get();
    }
    
    public static SingleTaskRestartableWatch handlerStopWatch() {
        return HANDLER_STOPWATCH.get();
    }
}


