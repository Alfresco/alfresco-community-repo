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

import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.core.io.Resource;

/**
 * @author Brian
 * @since 4.0
 */
public interface ChannelType
{
    enum  AuthStatus {AUTHORISED, RETRY, UNAUTHORISED}
    
    /**
     * Returns the unique identifier of this channel type
     * @return
     */
    String getId();
    
    /**
     * Returns the title (display name) of this channel type.
     * The title may be localised, but this is implementation specific
     * @return
     */
    String getTitle();
    
    /**
     * Each channel is stored in the repository as a node. This operation returns
     * the qualified name of the type of that node.
     * @return
     */
    QName getChannelNodeType();

    /**
     * Does this channel type support publishing content?
     * @return
     */
    boolean canPublish();
    
    /**
     * Does this channel type support unpublishing content? That is to say, once content has been published
     * to a channel of this type, can it later be removed from that channel?
     * @return
     */
    boolean canUnpublish();
    
    /**
     * Does this channel type support status updates?
     * @return
     */
    boolean canPublishStatusUpdates();
    
    /**
     * Send the specified status update to the specified channel
     * @param channel
     * @param status
     */
    void sendStatusUpdate(Channel channel, String status);
    
    /**
     * Returns the set of MIME types supported by channels of this type.
     * @return The set of MIME types supported by channels of this type or an empty set
     * if content of any MIME type can be published.
     */
    Set<String> getSupportedMimeTypes();

    /**
     * Returns the set of content types supported by channels of this type.
     * @return The set of content types supported by channels of this type or an empty set
     * if content of any content type can be published.
     */
    Set<QName> getSupportedContentTypes();
    
    /**
     * Returns the URL for a piece of content represented by the supplied <code>node</code>.
     * @param node The published content node in the live environment.
     * @return a URL for the published content.
     */
    String getNodeUrl(NodeRef node);
    
    /**
     * If this channel type supports status updates then this operation returns the maximum permitted 
     * length of those status updates. 
     * @return The maximum length of status updates on channels of this type. A value of zero indicates that there 
     * is no maximum.
     */
    int getMaximumStatusLength();
    
    /**
     * When creating a new channel of this type, this operation is called to find out where the user should be taken
     * in order to authorise Alfresco to publish content / status updates to that channel.
     * @param channel The channel that needs to be authorised.
     * @param callbackUrl Where the service provider represented by this channel type should redirect the user to once 
     * the authorisation procedure is complete.
     * @return The URL that the user should be taken to in order to authorise access to Alfresco for the specified channel.
     */
    String getAuthorisationUrl(Channel channel, String callbackUrl);
    
    /**
     * This operation is called after the service provider represented by this channel type has redirected the user
     * back to Alfresco. The HTTP headers and parameters received from the service provider are included, as they are likely 
     * to contain essential information such as tokens, error codes, and so on.
     * @param channel The channel related to this authorisation callback.
     * @param callbackHeaders All the HTTP headers received in the callback.
     * @param callbackParams All the HTTP parameters received in the callback.
     * @return A value indicating whether the authorisation was successful or not. If not, there is an indication as to
     * whether the user may try again or not. 
     */
    AuthStatus acceptAuthorisationCallback(Channel channel, Map<String, String[]> callbackHeaders, Map<String, String[]> callbackParams);
    
    /**
     * Obtain the resource that represents an icon for this channel type.
     * @param size A text representation of the icon size required. "16", "32", etc.
     * @return The resource that represents the requested icon if available. <code>null</code> otherwise.
     */
    Resource getIcon(String size);
}
