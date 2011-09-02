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

package org.alfresco.service.cmr.publishing;

import java.util.Set;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 4.0
 */
public interface StatusUpdate
{
    /**
     * @return the status message to be published.
     */
    String getMessage();
    
    /**
     * @return a {@link Set} of String identifiers indicating which {@link Channel} the status update will be published to.
     */
    Set<String> getChannelIds();

    /**
     * Returns a {@link NodeRef}. The returned {@link NodeRef} is one of the {@link NodeRef}s to be published by the associated {@link PublishingEvent}. The status update message will have a URL appended to it which links to the published resource represented by this {@link NodeRef}.
     * @return the {@link NodeRef} to link to.
     */
    NodeRef getNodeToLinkTo();
}
