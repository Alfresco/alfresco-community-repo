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
    
    String getId();
    QName getChannelNodeType();

    boolean canPublish();
    boolean canUnpublish();
    boolean canPublishStatusUpdates();
    
    void sendStatusUpdate(Channel channel, String status);
    
    Set<String> getSupportedMimeTypes();
    Set<QName> getSupportedContentTypes();
    
    /**
     * Returns the URL for a piece of content represented by the supplied <code>node</code>.
     * @param node The published content node in the live environment.
     * @return a URL for the published content.
     */
    String getNodeUrl(NodeRef node);
    int getMaximumStatusLength();
    
    String getAuthorisationUrl(Channel channel, String callbackUrl);
    AuthStatus acceptAuthorisationCallback(Channel channel, Map<String, String[]> callbackHeaders, Map<String, String[]> callbackParams);
    
    String getIconFileExtension();
    
    /**
     * Obtain the resource that represents an icon for this channel type.
     * @param size A text representation of the icon size required. "16", "32", etc.
     * @return The resource that represents the requested icon if available. <code>null</code> otherwise.
     */
    Resource getIcon(String size);
}
