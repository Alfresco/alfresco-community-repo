/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import static org.junit.Assert.*;

import static org.alfresco.repo.version.VersionModel.STORE_ID;
import static org.alfresco.service.cmr.repository.StoreRef.*;
import static org.alfresco.service.cmr.repository.StoreRef.PROTOCOL_DELETED;
import static org.alfresco.service.cmr.version.VersionService.VERSION_STORE_PROTOCOL;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Test;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.Pair;

public class NodeRefRadixHasherTest
{
    private final NodeRefRadixHasher nodeRefRadixHasher = NodeRefRadixHasher.RADIX_36_HASHER;

    @Test
    public void testSupportedStoresWithRandomUuids()
    {
        List<String> storeProtocols = List.of(PROTOCOL_WORKSPACE, PROTOCOL_ARCHIVE, PROTOCOL_AVM, PROTOCOL_DELETED, VERSION_STORE_PROTOCOL);
        List<String> storeIds = List.of("SpacesStore", STORE_ID, Version2Model.STORE_ID);
        List<String> uuidNodeIds = Stream.generate(UUID::randomUUID)
                .map(UUID::toString)
                .limit(5)
                .toList();

        for (String storeProtocol : storeProtocols)
        {
            for (String storeId : storeIds)
            {
                for (String uuidNodeId : uuidNodeIds)
                {
                    NodeRef nodeRef = new NodeRef(storeProtocol, storeId, uuidNodeId);
                    Pair<String, String> hash = nodeRefRadixHasher.hash(nodeRef);
                    assertFalse(hash.getSecond().startsWith(NodeRefRadixHasher.NOT_UUID_FORMAT_MARKER));
                    NodeRef actualNodeRef = nodeRefRadixHasher.lookup(hash);
                    assertEquals(nodeRef, actualNodeRef);
                }
            }
        }
    }

    @Test
    public void testSpecificValidNotUuidNodeIds()
    {
        StoreRef storeRef = new StoreRef(PROTOCOL_WORKSPACE, STORE_ID);
        List<String> notUuidNodeIds = List.of(
                "0d3b26ff-c4c1-4680-8622-8608ea7ab4",
                "0d3b26ff-c4c14680-8622-8608ea7ab4b29",
                "wf-email-html-ftl",
                "a",
                NodeRefRadixHasher.NOT_UUID_FORMAT_MARKER,
                "defrobldkfoeirjtuy79dfwwqperfiidoelb");
        for (String notUuidNodeId : notUuidNodeIds)
        {
            NodeRef nodeRef = new NodeRef(storeRef, notUuidNodeId);
            Pair<String, String> hash = nodeRefRadixHasher.hash(nodeRef);
            assertTrue(hash.getSecond().startsWith(NodeRefRadixHasher.NOT_UUID_FORMAT_MARKER));
            NodeRef actualNodeRef = nodeRefRadixHasher.lookup(hash);
            assertEquals(nodeRef, actualNodeRef);
        }
    }

    @Test(expected = RuntimeException.class)
    public void testEmptyNodeId()
    {
        NodeRef nodeRef = new NodeRef("workspace://SpacesStore/");
        nodeRefRadixHasher.hash(nodeRef);
    }
}
