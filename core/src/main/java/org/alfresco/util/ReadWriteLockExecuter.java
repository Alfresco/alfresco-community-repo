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
package org.alfresco.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Utility object that wraps read and write methods within the context of a
 * {@link ReentrantReadWriteLock}.  The callback's methods are best-suited
 * to fetching values from a cache or protecting members that need lazy
 * initialization.
 * <p>
 * Client code should construct an instance of this class for each resource
 * (or set of resources) that need to be protected.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public abstract class ReadWriteLockExecuter<T>
{
    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;

    /**
     * Default constructor
     */
    public ReadWriteLockExecuter()
    {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }
    
    /**
     * Execute the read-only part of the work.
     * 
     * @return              Returns a value of interest or <tt>null</tt> if
     *                      the {@link #getWithWriteLock()} method must be
     *                      called
     * @throws Throwable    all checked exceptions are wrapped in a <tt>RuntimeException</tt>
     */
    protected abstract T getWithReadLock() throws Throwable;
    
    /**
     * Execute the write part of the work.
     * <p>
     * <b>NOTE:</b> It is important to perform a double-check on the resource
     * before assuming it is not null; there is a window between the {@link #getWithReadLock()}
     * and the {@link #getWithWriteLock()} during which another thread may have populated
     * the resource of interest.
     * 
     * @return              Returns the value of interest of <tt>null</tt>
     * @throws Throwable    all checked exceptions are wrapped in a <tt>RuntimeException</tt>
     */
    protected abstract T getWithWriteLock() throws Throwable;

    public T execute()
    {
        T ret = null;
        readLock.lock();
        try
        {
            ret = this.getWithReadLock();
            // We do the null check here so that less time is spent outside of the lock
            if (ret != null)
            {
                return ret;
            }
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Exception during 'getWithReadLock'", e);
        }
        finally
        {
            readLock.unlock();
        }
        // If we got here, then we didn't get a result and need to go for the write lock
        writeLock.lock();
        try
        {
            // The return value is not of interest to us
            return this.getWithWriteLock();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            throw new RuntimeException("Exception during 'getWithWriteLock'", e);
        }
        finally
        {
            writeLock.unlock();
        }
    }
}
