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

import static junit.framework.Assert.assertEquals;

import java.util.Calendar;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.MutablePublishingPackage;
import org.alfresco.service.cmr.publishing.NodePublishStatus;
import org.alfresco.service.cmr.publishing.NodePublishStatus.Status;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class EnvironmentImplTest extends AbstractPublishingIntegrationTest
{
    private static final String channel1Name = "Channel1";
    private static final String channel2Name = "Channel2";

    @Resource(name="channelService")
    private ChannelService channelService;
    
    @Test
    public void testCheckPublishStatus()
    {
        NodeRef first = fileFolderService.create(docLib, "first", ContentModel.TYPE_CONTENT).getNodeRef();
        NodeRef second = fileFolderService.create(docLib, "second", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<NodeRef, NodePublishStatus> results = environment.checkPublishStatus(channel1Name, first);
        assertEquals(1, results.size());
        checkNodeStatus(first, Status.NOT_PUBLISHED, results);

        // Schedule first Node for publishing.
        Calendar schedule = Calendar.getInstance();
        schedule.add(Calendar.YEAR, 1);
        MutablePublishingPackage pckg =queue.createPublishingPackage();
        pckg.addNodesToPublish(first);
        queue.scheduleNewEvent(pckg, channel1Name, schedule, null, null);
        
        results = environment.checkPublishStatus(channel1Name, first, second);
        assertEquals(2, results.size());
        checkNodeStatus(first, Status.ON_QUEUE, results);
        checkNodeStatus(second, Status.NOT_PUBLISHED, results);
        
        results = environment.checkPublishStatus(channel2Name, first, second);
        assertEquals(2, results.size());
        checkNodeStatus(first, Status.NOT_PUBLISHED, results);
        checkNodeStatus(second, Status.NOT_PUBLISHED, results);
    }

    private void checkNodeStatus(NodeRef node, Status expStatus, Map<NodeRef, NodePublishStatus> results)
    {
        NodePublishStatus nodeStatus = results.get(node);
        assertEquals(environment, nodeStatus.getEnvironment());
        assertEquals(node, nodeStatus.getNodeRef());
        assertEquals(expStatus, nodeStatus.getStatus());
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        ChannelType channelType = mockChannelType();
        
        channelService.register(channelType);
        channelService.createChannel(siteId, channelTypeId, channel1Name, null);
        channelService.createChannel(siteId, channelTypeId, channel2Name, null);
    }
    
}
