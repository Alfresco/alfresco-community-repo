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

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.List;

import javax.annotation.Resource;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Nick Smith
 * @since 4.0
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
    
}
