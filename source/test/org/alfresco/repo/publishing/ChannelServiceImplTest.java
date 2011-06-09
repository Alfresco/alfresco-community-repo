/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.publishing;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;
import static org.alfresco.repo.publishing.PublishingModel.*;
    
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.alfresco.repo.transfer.AbstractNodeFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.alfresco.service.cmr.transfer.NodeFinder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
/**
 * @author Nick Smith
 * @since 4.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test/alfresco/test-web-publishing-context.xml"})
public class ChannelServiceImplTest
{
    @Resource
    ChannelServiceImpl channelService;

    @Resource
    NodeService nodeService;
    
    @Resource
    ServiceRegistry serviceRegistry;
    
    @Resource
    MockChannelType mockChannelType;
    
    @Test
    public void testRegister()
    {
        List<ChannelType> types = channelService.getChannelTypes();
        
        // Check the mock channel type is registered through Spring.
        assertTrue(types.contains(mockChannelType));
        channelService.getChannelType(MockChannelType.ID);
        try
        {
            channelService.register(null);
            fail("Exception expected when calling register(null)");
        }
        catch(IllegalArgumentException e)
        {
            //NOOP
        }
        try
        {
            channelService.register(mockChannelType);
            fail("Exception expected when trying to register the same ChannelType twice");
        }
        catch(IllegalArgumentException e)
        {
            //NOOP
        }
    }
    
    @Test
    public void testGetChannelDependancyNodeFinder() throws Exception
    {
        when(serviceRegistry.getNodeService()).thenReturn(nodeService);
        NodeRef node = new NodeRef("test://foo/bar");
        NodeRef parent = new NodeRef("test://foo/barParent");
        ChildAssociationRef assoc = new ChildAssociationRef(null, parent, null, node);
        
        NodeFinder nodeFinder = channelService.getChannelDependancyNodeFinder();
        assertNotNull(nodeFinder);

        // Initialize Node Finders
        ChannelDependancyNodeFinder cdnf = (ChannelDependancyNodeFinder) nodeFinder;
        cdnf.setServiceRegistry(serviceRegistry);
        cdnf.init();
        
        // Need to call afterPropertiesSet() again to pick up nodeService.
        mockChannelType.afterPropertiesSet();
        
        // Check no nodes found if NodeRef does not have a channel type.
        Set<NodeRef> results = nodeFinder.findFrom(node);
        assertTrue(results.isEmpty());
        
        // Check no nodes found if NodeRef has an unregistered channel type.
        when(nodeService.getProperty(node, PROP_CHANNEL_TYPE))
            .thenReturn("Foo");
        results = nodeFinder.findFrom(node);
        assertTrue(results.isEmpty());
        
        // Check returns parent if MockChannelType found.
        when(nodeService.getProperty(node, PROP_CHANNEL_TYPE))
            .thenReturn(MockChannelType.ID);
        when(nodeService.getPrimaryParent(node))
            .thenReturn(assoc);
        results = nodeFinder.findFrom(node);
        assertEquals(1, results.size());
        assertTrue(results.contains(parent));
    }
    
    @Test
    public void testGetChannelDependancyNodeFilter() throws Exception
    {
        when(serviceRegistry.getNodeService()).thenReturn(nodeService);
        NodeRef node = new NodeRef("test://foo/bar");
        
        NodeFilter nodeFinder = channelService.getChannelDependancyNodeFilter();
        assertNotNull(nodeFinder);
        
        // Initialize Node Finders
        ChannelDependancyNodeFilter cdnf = (ChannelDependancyNodeFilter) nodeFinder;
        cdnf.setServiceRegistry(serviceRegistry);
        cdnf.init();
        
        // Check no nodes filtered if NodeRef does not have a channel type.
        assertTrue(nodeFinder.accept(node));
        
        // Check no nodes filtered if NodeRef has an unregistered channel type.
        when(nodeService.getProperty(node, PROP_CHANNEL_TYPE))
            .thenReturn("Foo");
        assertTrue(nodeFinder.accept(node));
        
        // TODO Test other NodeFilter behaviour when added.
    }
}
