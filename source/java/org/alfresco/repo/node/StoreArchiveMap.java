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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.node;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * A map component that maps <b>node stores</b> to their archive <b>stores</b>.
 * 
 * @author Derek Hulley
 */
public class StoreArchiveMap
{
    private Map<StoreRef, StoreRef> storeArchiveMap;
    
    public StoreArchiveMap()
    {
        storeArchiveMap = new HashMap<StoreRef, StoreRef>(0);
    }
    
    public Map<StoreRef, StoreRef> getArchiveMap()
    {
        return storeArchiveMap;
    }
    
    public void setArchiveMap(Map<String, String> archiveMap)
    {
        // translate all the entries to references
        for (Map.Entry<String, String> entry : archiveMap.entrySet())
        {
            String storeRefKeyStr = entry.getKey();
            String storeRefValueStr = entry.getValue();
            StoreRef storeRefKey = null;
            StoreRef storeRefValue = null;
            try
            {
                storeRefKey = new StoreRef(storeRefKeyStr);
                storeRefValue = new StoreRef(storeRefValueStr);
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException("Unable create store references from map entry: " + entry);
            }
            storeArchiveMap.put(storeRefKey, storeRefValue);
        }
    }
}
