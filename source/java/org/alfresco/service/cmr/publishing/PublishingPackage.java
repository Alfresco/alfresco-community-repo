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
package org.alfresco.service.cmr.publishing;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

public interface PublishingPackage
{
    /**
     * Retrieve the collection of publishing package entries contained by this publishing package.
     * @return The collection of publishing package entries. Never <code>null</code>.
     */
    Collection<PublishingPackageEntry> getEntries();
    Map<NodeRef,PublishingPackageEntry> getEntryMap();
    Set<NodeRef> getNodesToPublish();
    Set<NodeRef> getNodesToUnpublish();
}
