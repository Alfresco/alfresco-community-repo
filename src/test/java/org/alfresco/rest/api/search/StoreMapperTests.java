/*-
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.api.search;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import org.alfresco.rest.api.search.impl.StoreMapper;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Test;

/**
 * Tests the StoreMapper class
 *
 * @author Gethin James
 */
public class StoreMapperTests
{
    static StoreMapper storeMapper = new StoreMapper();

    @Test(expected = InvalidArgumentException.class)
    public void testGetStoreErrors() throws Exception
    {
        storeMapper.getStoreRef(null);
    }

    @Test(expected = InvalidArgumentException.class)
    public void testGetStoreWithEmpty() throws Exception
    {
        storeMapper.getStoreRef("");
    }

    @Test(expected = InvalidArgumentException.class)
    public void testInvalidStoreName() throws Exception
    {
        storeMapper.getStoreRef("bob");
    }

    @Test
    public void testGetStoreRef() throws Exception
    {
        assertEquals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, storeMapper.getStoreRef("nodes"));
        assertEquals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, storeMapper.getStoreRef("Nodes"));
        assertEquals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, storeMapper.getStoreRef("NODES"));

        assertEquals(StoreMapper.STORE_REF_VERSION2_SPACESSTORE, storeMapper.getStoreRef("Versions"));
        assertEquals(StoreMapper.STORE_REF_VERSION2_SPACESSTORE, storeMapper.getStoreRef("versions"));
        assertEquals(StoreMapper.STORE_REF_VERSION2_SPACESSTORE, storeMapper.getStoreRef("VERSIONS"));

        assertEquals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, storeMapper.getStoreRef("Deleted-nodes"));
        assertEquals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, storeMapper.getStoreRef("deleted-nodes"));
        assertEquals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, storeMapper.getStoreRef("DELETED-NODES"));
    }

    @Test
    public void testGetStore() throws Exception
    {
        assertEquals(storeMapper.LIVE_NODES, storeMapper.getStore(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "not interested")));
        assertEquals(storeMapper.VERSIONS, storeMapper.getStore(new NodeRef(StoreMapper.STORE_REF_VERSION2_SPACESSTORE, "not interested")));
        assertEquals(storeMapper.DELETED, storeMapper.getStore(new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, "not interested")));

        assertNull(storeMapper.getStore(null));
    }
}
