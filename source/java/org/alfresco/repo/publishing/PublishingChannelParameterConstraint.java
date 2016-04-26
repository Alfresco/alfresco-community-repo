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

package org.alfresco.repo.publishing;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.action.constraint.BaseParameterConstraint;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;

/**
 * Action parameter constraint that constrains to list of publishing channels
 * 
 * @see PublishContentActionExecuter
 * @author Brian
 * @since 4.0
 */
public class PublishingChannelParameterConstraint extends BaseParameterConstraint
{
    public static final String NAME = "ac-publishing-channels";

    private ChannelService channelService;

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {
        List<Channel> channels = channelService.getPublishingChannels(false);
        Map<String, String> result = new TreeMap<String, String>();
        for (Channel channel : channels)
        {
            result.put(channel.getId(), channel.getName());
        }
        return result;
    }

}
