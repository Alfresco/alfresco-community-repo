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

package org.alfresco.repo.publishing;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @author Nick Smith
 *
 */
public class PublishingPackageImpl implements PublishingPackage
{
    private final Map<NodeRef, PublishingPackageEntry> entries;
    private final Set<NodeRef> nodesToPublish;
    private final Set<NodeRef> nodesToUnpublish;
    
    public PublishingPackageImpl(Map<NodeRef, PublishingPackageEntry> entries)
    {
        Set<NodeRef> toPublish = new HashSet<NodeRef>();
        Set<NodeRef> toUnpublish = new HashSet<NodeRef>();
        for (PublishingPackageEntry entry : entries.values())
        {
            NodeRef node = entry.getNodeRef();
            if(entry.isPublish())
            {
                toPublish.add(node);
            }
            else
            {
                toUnpublish.add(node);
            }
        }
        HashMap<NodeRef, PublishingPackageEntry> entryMap = new HashMap<NodeRef, PublishingPackageEntry>(entries);
        this.entries = Collections.unmodifiableMap(entryMap);
        this.nodesToPublish = Collections.unmodifiableSet(toPublish);
        this.nodesToUnpublish = Collections.unmodifiableSet(toUnpublish);
    }
    
    /**
    * {@inheritDoc}
     */
    public Collection<PublishingPackageEntry> getEntries()
    {
        return entries.values();
    }

    public Map<NodeRef,PublishingPackageEntry> getEntryMap()
    {
        return entries;
    }
    
    /**
     * {@inheritDoc}
     */
     public Set<NodeRef> getNodesToPublish()
     {
         return nodesToPublish;
     }

     /**
     * {@inheritDoc}
     */
     @Override
     public Set<NodeRef> getNodesToUnpublish()
     {
         return nodesToUnpublish;
     }

}
