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

package org.alfresco.service.cmr.publishing.channels;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public interface ChannelService
{
    /**
     * Register a new channel type with the channel service
     * @param channelType The channel type to be registered.
     * @throws IllegalArgumentException if a channel type is already registered that has the same identifier as the supplied one
     */
    @NotAuditable
    void register(AbstractChannelType channelType);
    
    /**
     * Retrieve the channel type that has the specified identifier
     * @param id The identifier of the channel type to be retrieved
     * @return A ChannelType object that represents the channel type with the specified identifier
     */
    @NotAuditable
    ChannelType getChannelType(String id);
    
    /**
     * Retrieve all the registered channel types
     * @return A list of ChannelType objects, each representing a channel type registered with this channel service
     */
    @NotAuditable
    List<ChannelType> getChannelTypes();
    
    /**
     * Create a new channel of the specified channel type on the specified Share site with the specified name and properties.
     * @param channelTypeId The identifier of the channel type that is to be used for the new channel. This must identify a channel type that
     * has been registered with the channel service.
     * @param name The name of the new channel. This must be unique within the specified Share site.
     * @param properties Any additional properties that are to be saved as part of the new channel.
     * @return A Channel object corresponding to the newly created channel.
     */
    @Auditable(parameters={"channelTypeId", "name"})
    Channel createChannel(String channelTypeId, String name, Map<QName, Serializable> properties);
    
    /**
     * Remove the specified channel.
     * @param channel The channel to delete.
     */
    @Auditable
    void deleteChannel(Channel channel);
    
    /**
     * Rename the specified channel
     * @param channel The channel that is to be renamed.
     * @param newName The new name of the channel
     */
    @Auditable(parameters={"newName"})
    void renameChannel(Channel channel, String newName);
    
    /**
     * Update the properties of the specified channel.
     * @param channel The channel that is to be updated.
     * @param properties The properties to set on the channel. These are blended with the current properties
     * on the channel. Any that aren't currently set will be added, others will be updated. 
     */
    @Auditable
    void updateChannel(Channel channel, Map<QName,Serializable> properties);
    
    /**
     * Retrieve all the channels.
     * @return A list of Channel objects, each one representing a channel that exists within the specified Share site.
     */
    @NotAuditable
    List<Channel> getChannels();

    /**
     * Retrieve the channel with the given channel name.
     * @param channelName The name of the channel
     * @return The specified Channel objects or <code>null</code> if the specified channel does not exist.
     */
    @NotAuditable
    Channel getChannelByName(String channelName);
    
    /**
     * Retrieve the channel with the given id.
     * @param id The string value of the channel {@link NodeRef}.
     * @return The specified Channel objects or <code>null</code> if the specified channel does not exist.
     */
    @NotAuditable
    Channel getChannelById(String id);
    
    /**
     * Returns a list of all the channels that are capable of publishing the specified NodeRef.
     * @param nodeToPublish
     * @return
     */
    @NotAuditable
    List<Channel> getRelevantPublishingChannels(NodeRef nodeToPublish);
    
    /**
     * Returns a list of all the channels that are capable of publishing in the specified Share site.
     * @param filterByPublishPermission If true then the returned channels are filtered to include only those
     * to which the authenticated user can publish
     * @return
     */
    @NotAuditable
    List<Channel> getPublishingChannels(boolean filterByPublishPermission);
    
    /**
     * Returns all {@link Channel}s cpaable of performing a status update for the given Share Site.
     * @param filterByPublishPermission If true then the returned channels are filtered to include only those
     * to which the authenticated user can post status updates
     * @return
     */
    @NotAuditable
    List<Channel> getStatusUpdateChannels(boolean filterByPublishPermission);

}
