/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.service.cmr.publishing.channels;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 *
 */
public interface ChannelService
{
    void register(ChannelType channelType);
    ChannelType getChannelType(String id);
    List<ChannelType> getChannelTypes();
    Channel createChannel(String siteId, String channelTypeId, String name, Map<QName, Serializable> properties);
    void deleteChannel(String siteId, String channelName);
    void renameChannel(String siteId, String oldName, String newName);
    void updateChannel(String siteId, String channelName, Map<QName,Serializable> properties);
    List<Channel> getChannels(String siteId);
}
