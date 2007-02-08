/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.smb.dcerpc;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Policy Handle Cache Class
 */
public class PolicyHandleCache
{

    // Policy handles

    private Hashtable<String, PolicyHandle> m_cache;

    /**
     * Default constructor
     */
    public PolicyHandleCache()
    {
        m_cache = new Hashtable<String, PolicyHandle>();
    }

    /**
     * Return the number of handles in the cache
     * 
     * @return int
     */
    public final int numberOfHandles()
    {
        return m_cache.size();
    }

    /**
     * Add a handle to the cache
     * 
     * @param name String
     * @param handle PolicyHandle
     */
    public final void addHandle(String name, PolicyHandle handle)
    {
        m_cache.put(name, handle);
    }

    /**
     * Return the handle for the specified index
     * 
     * @param index String
     * @return PolicyHandle
     */
    public final PolicyHandle findHandle(String index)
    {
        return m_cache.get(index);
    }

    /**
     * Delete a handle from the cache
     * 
     * @param index String
     * @return PolicyHandle
     */
    public final PolicyHandle removeHandle(String index)
    {
        return m_cache.remove(index);
    }

    /**
     * Enumerate the handles in the cache
     * 
     * @return Enumeration<PolicyHandle>
     */
    public final Enumeration<PolicyHandle> enumerateHandles()
    {
        return m_cache.elements();
    }

    /**
     * Clear all handles from the cache
     */
    public final void removeAllHandles()
    {
        m_cache.clear();
    }
}
