/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.node;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A component that pre-fetches cached data for the given nodes.  Client code can use
 * this component when a list of <code>NodeRef</code> instances will be processed in
 * a data-intensive manner.
 * 
 * @author Andy Hind
 * @author Derek Hulley
 */
public interface NodeBulkLoader
{
    /**
     * Pre-cache data relevant to the given nodes.  There is no need to split the collection
     * up before calling this method; it is up to the implementations to ensure that batching
     * is done where necessary.
     * 
     * @param nodeRefs          the nodes that will be cached.
     */
    public void cacheNodes(List<NodeRef> nodeRefs);
}
