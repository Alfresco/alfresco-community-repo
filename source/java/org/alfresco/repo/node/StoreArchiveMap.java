package org.alfresco.repo.node;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * A map component that maps <b>node stores</b> to their archive <b>stores</b>.
 * 
 * @author Derek Hulley
 */
public class StoreArchiveMap
{
    private Map<StoreRef, StoreRef> storeArchiveMap;
    
    private TenantService tenantService;
    
    public StoreArchiveMap()
    {
        storeArchiveMap = new HashMap<StoreRef, StoreRef>(0);
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
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
    
    public StoreRef get(StoreRef storeRef)
    {
        if (tenantService.isEnabled())
        {
            return tenantService.getName(storeArchiveMap.get(tenantService.getBaseName(storeRef)));
        }
        else
        {
            return storeArchiveMap.get(storeRef);
        }
    }
    
    public void put(StoreRef workStoreRef, StoreRef archiveStoreRef)
    {
        storeArchiveMap.put(workStoreRef, archiveStoreRef);
    }
    
    public void clear()
    {
        storeArchiveMap.clear();
    }
}
