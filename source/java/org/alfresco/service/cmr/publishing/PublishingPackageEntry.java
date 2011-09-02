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

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Brian
 * @since 4.0
 */
public interface PublishingPackageEntry
{
    /**
     * Retrieve the identifier of the node that this publishing package entry
     * relates to
     * 
     * @return A NodeRef object that identifies the node that this publishing
     *         package entry relates to
     */
    NodeRef getNodeRef();

    /**
     * Retrieve the snapshot of the node that is held as the payload of this
     * publishing package entry. The snapshot is taken when the containing
     * publishing package is placed on the publishing queue IF this is a
     * "publish" entry as opposed to an "unpublish" entry. No snapshot is taken
     * for an unpublish entry.
     * 
     * @return The snapshot of the node that this publishing package entry
     *         relates to if this is a "publish" entry (
     *         <code>null</node> if this is an "unpublish" entry). The snapshot is taken when
     * the containing publishing package is placed on the publishing queue, so if this operation is called before that point
     * then it will return <code>null</code>.
     */
    NodeSnapshot getSnapshot();

    /**
     * Determine if this entry relates to a publish request or an unpublish
     * request
     * 
     * @return <code>true</code> if this entry relates to a publish request and
     *         <code>false</code> if it relates to an unpublish request
     */
    boolean isPublish();
}
