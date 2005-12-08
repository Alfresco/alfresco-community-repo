/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
