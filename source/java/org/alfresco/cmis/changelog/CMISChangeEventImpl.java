/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.cmis.changelog;

import java.util.Date;

import org.alfresco.cmis.CMISChangeEvent;
import org.alfresco.cmis.CMISChangeType;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * CMISChangeEvent Implementation
 * 
 * @author Dmitry Velichkevich
 */
public class CMISChangeEventImpl implements CMISChangeEvent
{
    private NodeRef node;
    private CMISChangeType changeType;
    private Date changeTime;

    /**
     * Construct a CMISChangeEvent using fields
     * 
     * @param changeType change type
     * @param node node reference
     * @param changeTime change time
     */
    public CMISChangeEventImpl(CMISChangeType changeType, NodeRef node, Date changeTime)
    {
        this.changeType = changeType;
        this.node = node;
        this.changeTime = changeTime;
    }

    /**
     * @see org.alfresco.cmis.CMISChangeEvent#getChangeType()
     */
    public CMISChangeType getChangeType()
    {
        return changeType;
    }

    /**
     * @see org.alfresco.cmis.CMISChangeEvent#getNode()
     */
    public NodeRef getNode()
    {
        return node;
    }
    
    /**
     * @see org.alfresco.cmis.CMISChangeEvent#getChangeTime()
     */
    public Date getChangeTime()
    {
        return changeTime;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof CMISChangeEvent))
        {
            return false;
        }
        CMISChangeEvent converted = (CMISChangeEvent) obj;
        return same(node, converted.getNode()) && same(changeType, converted.getChangeType()) && same(changeTime, converted.getChangeTime());
    }

    private boolean same(Object left, Object right)
    {
        return (null == left) ? (null == right) : (left.equals(right));
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        int result = (null != node) ? (node.hashCode()) : (31);
        return result * 37 + (null != changeType ? changeType.hashCode() : 31) + (null != changeTime ? changeTime.hashCode() : 31);
    }

}
