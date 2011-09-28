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
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Represents a publishing channel
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public interface Channel
{
    /**
     * @return a unique identifier for this {@link Channel}.
     */
    String getId();
    
    /**
     * @return the {@link ChannelType} for this Channel.
     */
    ChannelType getChannelType();
    
    /**
     * Retrieve the node ref of the node that represents this channel object in the repository
     * @return
     */
    NodeRef getNodeRef();
    
    /**
     * Retrieve the name of this channel
     * @return
     */
    String getName();
    
    /**
     * Retrieve the properties defined on this channel.
     * @return
     */
    Map<QName, Serializable> getProperties();
    
    /**
     * Post the specified text onto this channel as a status update.
     * @param status The text of the status update. Note that if the length of this text plus the
     * length of the urlToAppend text is greater than the maximum length permitted as a status
     * update on this channel then this text will be truncated to fit.
     * @param urlToAppend Text that is to be appended to the status update - often a URL to a relevant 
     * piece of content. If this channel can't accept both the status text and the URL then the status text
     * will be truncated in preference to the URL. This argument may be null.
     */
    void sendStatusUpdate(String status, String urlToAppend);
    
    /**
     * Returns the URL for the specified node on this channel.
     * @param The content node whose published URL is being requested.
     * @return a URL for the published content. May return <code>null</code> if the specified node has not
     * been published to this channel.
     */
    String getUrl(NodeRef publishedNode);
    
    /**
     * Has this channel been authorised yet?
     * Typically, when a channel is created in Alfresco the user is sent to the service provider to authorise
     * Alfresco to access their account on their behalf. Once Alfresco has been told that the user has done that then
     * this operation will return true. Until then, this operation will return false.
     * 
     * A channel that is not authorised cannot be used to publish content or status updates to.
     * @return true if this channel has been authorised and is ready for use.
     */
    boolean isAuthorised();
    
    /**
     * Returns <code>true</code> only if the currently authenticated user can publish content to this {@link Channel}.
     * If the {@link ChannelType} does not support publishing, if the {@link Channel} is not authorised or if the 
     * currently authenticated user does not have permission to publish to this {@link Channel} then this 
     * method will return <code>false</code>.
     * @return 
     */
    boolean canPublish();

    /**
     * Returns <code>true</code> only if the currently authenticated user can unpublish content from this {@link Channel}.
     * If the {@link ChannelType} does not support unpublishing, if the {@link Channel} is not authorised or if the 
     * currently authenticated user does not have permission to publish to this {@link Channel} then this method 
     * will return <code>false</code>.
     * @return 
     */
    boolean canUnpublish();

    /**
     * Returns <code>true</code> only if the currently authenticated user can unpublish status updates to this {@link Channel}.
     * If the {@link ChannelType} does not support publishing of status updates, if the {@link Channel} is not authorised 
     * or if the currently authenticated user does not have permission to publish to this {@link Channel} then this method 
     * will return <code>false</code>.
     * @return 
     */
    boolean canPublishStatusUpdates();

}