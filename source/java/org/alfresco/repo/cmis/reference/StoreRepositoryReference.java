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
package org.alfresco.repo.cmis.reference;

import org.alfresco.cmis.CMISServices;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * Store Ref Repository Reference
 * 
 * @author davidc
 */
public class StoreRepositoryReference extends AbstractRepositoryReference
{
    private StoreRef storeRef;

    /**
     * Construct
     * 
     * @param cmisServices
     * @param storeRef
     */
    public StoreRepositoryReference(CMISServices cmisServices, StoreRef storeRef)
    {
        super(cmisServices);
        this.storeRef = storeRef;
    }
    
    /**
     * Construct
     * 
     * @param cmisServices
     * @param store  accept storeType://storeId, storeType:storeId, storeId
     */
    public StoreRepositoryReference(CMISServices cmisServices, String store)
    {
        super(cmisServices);
        
        if (store.indexOf(StoreRef.URI_FILLER) != -1)
        {
            storeRef = new StoreRef(store);
        }
        else if (store.indexOf(':') != -1)
        {
            String[] storeParts = store.split(":");
            storeRef = new StoreRef(storeParts[0], storeParts[1]);
        }
        else
        {
            storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, store);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISRepositoryReference#getStoreRef()
     */
    public StoreRef getStoreRef()
    {
        return storeRef;
    }

}
