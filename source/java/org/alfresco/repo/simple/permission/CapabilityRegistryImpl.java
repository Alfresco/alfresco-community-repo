/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.simple.permission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.simple.permission.CapabilityRegistry;

/**
 * Basic implementation of a capability registry.
 * @author britt
 */
public class CapabilityRegistryImpl implements CapabilityRegistry
{
    private Map<Integer, String> fIDToCapability;
    
    private Map<String, Integer> fCapabilityToID;
    
    public CapabilityRegistryImpl()
    {
        fIDToCapability = new HashMap<Integer, String>();
        fCapabilityToID = new HashMap<String, Integer>();
    }
    
    public void setCapabilities(Set<String> capabilities)
    {
        int count = 0;
        for (String cap : capabilities)
        {
            Integer id = count++;
            fIDToCapability.put(id, cap);
            fCapabilityToID.put(cap, id);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.CapabilityRegistry#addCapability(java.lang.String)
     */
    public synchronized void addCapability(String capability)
    {
        // TODO Make this do something in the future.
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.CapabilityRegistry#getAll()
     */
    public synchronized Set<String> getAll()
    {
        return new HashSet<String>(fCapabilityToID.keySet());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.CapabilityRegistry#getCapabilityID(java.lang.String)
     */
    public synchronized int getCapabilityID(String capability)
    {
        Integer id = fCapabilityToID.get(capability);
        if (id == null)
        {
            return -1;
        }
        return id;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.CapabilityRegistry#getCapabilityName(int)
     */
    public synchronized String getCapabilityName(int id)
    {
        return fIDToCapability.get(id);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.simple.permission.CapabilityRegistry#removeCapability(java.lang.String)
     */
    public synchronized void removeCapability(String capability)
    {
        // TODO Make this persistent.
        Integer removed = fCapabilityToID.remove(capability);
        fIDToCapability.remove(removed);
    }
}
