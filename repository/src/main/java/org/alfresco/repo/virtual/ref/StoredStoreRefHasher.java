/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 - 2025 Alfresco Software Limited
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

package org.alfresco.repo.virtual.ref;

import org.alfresco.service.cmr.repository.StoreRef;

public class StoredStoreRefHasher implements StoreRefHasher
{
    private final HashStore storeProtocolStore;
    private final HashStore storeIdStore;

    public StoredStoreRefHasher()
    {
        this.storeProtocolStore = HashStoreConfiguration.getInstance().getStoreProtocolStore();
        this.storeIdStore = HashStoreConfiguration.getInstance().getStoreIdStore();
    }

    @Override
    public StoreRef lookup(String storeHash)
    {
        String storeProtocolHash = storeHash.substring(0, 1);
        String storeIdHash = storeHash.substring(1, 2);

        String storeProtocol = storeProtocolStore.lookup(storeProtocolHash);
        String storeId = storeIdStore.lookup(storeIdHash);
        if (storeProtocol == null || storeId == null)
        {
            throw new RuntimeException("Lookup found no protocol or id for " + storeHash);
        }
        return new StoreRef(storeProtocol, storeId);
    }

    @Override
    public String hash(StoreRef storeRef)
    {
        String storeProtocolHash = storeProtocolStore.hash(storeRef.getProtocol());
        String storeIdHash = storeIdStore.hash(storeRef.getIdentifier());
        if (storeProtocolHash == null || storeIdHash == null)
        {
            throw new RuntimeException("Missing hash for " + storeRef);
        }
        return storeProtocolHash + storeIdHash;
    }
}
