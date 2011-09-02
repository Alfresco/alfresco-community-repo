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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.publishing.StatusUpdate;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class StatusUpdateImpl implements StatusUpdate
{
    private final String message;
    private final NodeRef nodeToLinkTo;
    private final Set<String> channelNames;
    
    public StatusUpdateImpl(String message, NodeRef nodeToLinkTo, Collection<String> channelNames)
    {
        this.message = message;
        this.nodeToLinkTo = nodeToLinkTo;
        this.channelNames = Collections.unmodifiableSet(new HashSet<String>(channelNames));
    }

    /**
    * {@inheritDoc}
    */
    public String getMessage()
    {
        return message;
    }

    /**
    * {@inheritDoc}
    */
    public Set<String> getChannelIds()
    {
        return channelNames;
    }

    /**
    * {@inheritDoc}
    */
    public NodeRef getNodeToLinkTo()
    {
        return nodeToLinkTo;
    }
}
