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
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.alfresco.service.cmr.transfer.NodeFinder;
import org.alfresco.service.namespace.QName;
import org.springframework.core.io.Resource;

/**
 * @author Brian
 *
 */
public interface ChannelType
{
    String getId();
    QName getChannelNodeType();
    NodeFinder getNodeFinder();
    NodeFilter getNodeFilter();
    void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties);
    void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties);
    void updateStatus(Channel channel, String status, Map<QName, Serializable> properties);
    
    boolean canPublish();
    boolean canUnpublish();
    boolean canPublishStatusUpdates();
    
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
    boolean acceptAuthorisationCallback(Channel channel, Map<String, String[]> callbackHeaders, Map<String, String[]> callbackParams);
    
    String getIconFileExtension();
    Resource getIcon16();
    Resource getIcon32();
}
