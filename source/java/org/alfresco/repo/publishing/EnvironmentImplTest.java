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

import java.util.Calendar;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.NodePublishStatus;
import org.alfresco.service.cmr.publishing.NodePublishStatus.Status;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.junit.Test;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class EnvironmentImplTest extends AbstractPublishingIntegrationTest
{
    private Channel channel1;
    private Channel channel2;
    
    @Resource(name="channelService")
    private ChannelService channelService;
    
    @Test
    public void testCheckPublishStatus()
    {
        NodeRef first = fileFolderService.create(docLib, "first", ContentModel.TYPE_CONTENT).getNodeRef();
        NodeRef second = fileFolderService.create(docLib, "second", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<NodeRef, NodePublishStatus> results = environment.checkPublishStatus(channel1.getId(), first);
        assertEquals(1, results.size());
        checkNodeStatus(first, Status.NOT_PUBLISHED, results);

        // Schedule first Node for publishing.
        Calendar schedule = Calendar.getInstance();
        schedule.add(Calendar.YEAR, 1);
        MutablePublishingPackage pckg =queue.createPublishingPackage();
        pckg.addNodesToPublish(first);
        queue.scheduleNewEvent(pckg, channel1.getId(), schedule, null, null);
        
        results = environment.checkPublishStatus(channel1.getId(), first, second);
        assertEquals(2, results.size());
        checkNodeStatus(first, Status.ON_QUEUE, results);
        checkNodeStatus(second, Status.NOT_PUBLISHED, results);
        
        results = environment.checkPublishStatus(channel2.getId(), first, second);
        assertEquals(2, results.size());
        checkNodeStatus(first, Status.NOT_PUBLISHED, results);
        checkNodeStatus(second, Status.NOT_PUBLISHED, results);
    }

    private void checkNodeStatus(NodeRef node, Status expStatus, Map<NodeRef, NodePublishStatus> results)
    {
        NodePublishStatus nodeStatus = results.get(node);
        assertEquals(node, nodeStatus.getNodeRef());
        assertEquals(expStatus, nodeStatus.getStatus());
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        channelService = (ChannelServiceImpl) getApplicationContext().getBean("channelService");

        ChannelType channelType = mockChannelType();
        if (channelService.getChannelType(channelType.getId()) == null)
        {
            channelService.register(channelType);
        }
        this.channel1 = channelService.createChannel(siteId, channelTypeId, GUID.generate(), null);
        this.channel2 = channelService.createChannel(siteId, channelTypeId, GUID.generate(), null);
    }
    
}
