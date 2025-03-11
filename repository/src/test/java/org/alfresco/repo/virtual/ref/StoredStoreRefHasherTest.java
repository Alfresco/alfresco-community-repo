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

import static org.junit.Assert.assertEquals;

import static org.alfresco.repo.version.VersionModel.STORE_ID;
import static org.alfresco.service.cmr.repository.StoreRef.*;
import static org.alfresco.service.cmr.version.VersionService.VERSION_STORE_PROTOCOL;

import java.util.List;

import org.junit.Test;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.repository.StoreRef;

public class StoredStoreRefHasherTest
{
    private final StoreRefHasher storeRefHasher = new StoredStoreRefHasher();

    @Test
    public void testSupportedStores()
    {
        List<String> storeProtocols = List.of(PROTOCOL_WORKSPACE, PROTOCOL_ARCHIVE, PROTOCOL_AVM, PROTOCOL_DELETED, VERSION_STORE_PROTOCOL);
        List<String> storeIds = List.of("SpacesStore", STORE_ID, Version2Model.STORE_ID);

        for (String storeProtocol : storeProtocols)
        {
            for (String storeId : storeIds)
            {
                StoreRef storeRef = new StoreRef(storeProtocol, storeId);
                String hash = storeRefHasher.hash(storeRef);
                StoreRef actualStoreRef = storeRefHasher.lookup(hash);
                assertEquals(storeRef, actualStoreRef);
            }
        }
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidStoreId()
    {
        StoreRef storeRef = new StoreRef(PROTOCOL_WORKSPACE, "ASpacesStore");

        storeRefHasher.hash(storeRef);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidStoreProtocol()
    {
        StoreRef storeRef = new StoreRef("Xworkspace", STORE_ID);

        storeRefHasher.hash(storeRef);
    }
}
