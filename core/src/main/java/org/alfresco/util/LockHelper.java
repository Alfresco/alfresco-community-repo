/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Helper to make trying for read-write locks simpler
 * 
 * @author Derek Hulley
 * @since 4.1.7
 */
public class LockHelper
{
    /**
     * Exception generated when a lock try is unsuccessful
     * 
     * @author Derek Hulley
     * @since 4.1.7
     */
    public static class LockTryException extends RuntimeException
    {
        private static final long serialVersionUID = -3629889029591630609L;

        public LockTryException(String msg)
        {
            super(msg);
        }
    }
    
    /**
     * Try to get a lock in the given number of milliseconds or get an exception
     * 
     * @param lock                          the lock to try
     * @param timeoutMs                     the number of milliseconds to try
     * @param useCase                       {@link String} value which specifies description of use case when lock is needed
     * @throws LockTryException    the exception if the time is exceeded or the thread is interrupted
     */
    public static void tryLock(Lock lock, long timeoutMs, String useCase) throws LockTryException
    {
        boolean gotLock = false;
        try
        {
            gotLock = lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            // Handled
        }
        if (!gotLock)
        {
            throw new LockTryException("Failed to get lock " + lock.getClass().getSimpleName() + " for " + useCase + " in " + timeoutMs + "ms.");
        }
    }
}
