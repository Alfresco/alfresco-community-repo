/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.virtual.ref;

import junit.framework.TestCase;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class NodeRefRadixHasherTest extends TestCase
{
    private static Log logger = LogFactory.getLog(NodeRefRadixHasherTest.class);

    @Test
    public void testSupportedStores() throws Exception
    {
        NodeRefRadixHasher h = NodeRefRadixHasher.RADIX_36_HASHER;
        
        String[] storeProtocols = new String[] { StoreRef.PROTOCOL_WORKSPACE, StoreRef.PROTOCOL_ARCHIVE,
                    StoreRef.PROTOCOL_AVM, StoreRef.PROTOCOL_DELETED, VersionService.VERSION_STORE_PROTOCOL };
        String[] storeIds = new String[] { "SpacesStore", VersionModel.STORE_ID, Version2Model.STORE_ID };

        for (int i = 0; i < storeProtocols.length; i++)
        {
            for (int j = 0; j < storeIds.length; j++)
            {
                NodeRef nr = new NodeRef(storeProtocols[i],
                                         storeIds[j],
                                         "0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
                Pair<String, String> nh = h.hash(nr);
                NodeRef nr2 = h.lookup(nh);
                assertEquals("Could match hash-lookup " + nr,
                             nr,
                             nr2);
            }
        }
    }

    @Test
    public void testZeroPaddedNodeId() throws Exception
    {
        NodeRefRadixHasher h = NodeRefRadixHasher.RADIX_36_HASHER;
        NodeRef nr = new NodeRef("workspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
        Pair<String, String> nh = h.hash(nr);
        NodeRef nr2 = h.lookup(nh);
        assertEquals(nr,
                     nr2);
    }

    @Test
    public void testInvalidStoreId() throws Exception
    {
        NodeRefRadixHasher h = NodeRefRadixHasher.RADIX_36_HASHER;
        NodeRef nr = new NodeRef("workspace://ASpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
        try
        {
            h.hash(nr);
            fail("Should not be able to hash invalid store NodeRef " + nr);
        }
        catch (RuntimeException e)
        {
            logger.info("Caught invalid NodeRef " + e.getMessage());
        }
    }

    @Test
    public void testInvalidStoreProtocol() throws Exception
    {
        NodeRefRadixHasher h = NodeRefRadixHasher.RADIX_36_HASHER;
        NodeRef nr = new NodeRef("Xworkspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4b2");
        try
        {
            h.hash(nr);
            fail("Should not be able to hash invalid store NodeRef " + nr);
        }
        catch (RuntimeException e)
        {
            logger.info("Caught invalid NodeRef " + e.getMessage());
        }
    }

    @Test
    public void testInvalidNodeId1() throws Exception
    {
        NodeRefRadixHasher h = NodeRefRadixHasher.RADIX_36_HASHER;
        NodeRef nr = new NodeRef("workspace://SpacesStore/0d3b26ff-c4c1-4680-8622-8608ea7ab4");
        try
        {
            h.hash(nr);
            fail("Should not be able to hash invalid id (length) NodeRef " + nr);
        }
        catch (RuntimeException e)
        {
            logger.info("Caught invalid NodeRef " + e.getMessage());
        }
    }

    @Test
    public void testInvalidNodeId2() throws Exception
    {
        NodeRefRadixHasher h = NodeRefRadixHasher.RADIX_36_HASHER;
        NodeRef nr = new NodeRef("workspace://SpacesStore/0d3b26ff-c4c14680-8622-8608ea7ab4b29");
        try
        {
            h.hash(nr);
            fail("Should not be able to hash invalid id (format) NodeRef " + nr);
        }
        catch (RuntimeException e)
        {
            logger.info("Caught invalid NodeRef " + e.getMessage());
        }
    }
}
