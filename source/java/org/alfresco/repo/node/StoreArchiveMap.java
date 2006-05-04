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
