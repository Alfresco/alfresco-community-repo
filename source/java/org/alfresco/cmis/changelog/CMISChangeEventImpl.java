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
    private CMISChangeType changeType;
    private Date changeTime;
    private NodeRef changedNode;
    private String objectId;


    /**
     * Instantiates a new CMIS change event.
     * 
     * @param changeType
     *            the change type
     * @param changeTime
     *            the change time
     * @param changedNode
     *            the changed node
     * @param objectId
     *            the object id
     */
    public CMISChangeEventImpl(CMISChangeType changeType, Date changeTime, NodeRef changedNode, String objectId)
    {
        this.changeType = changeType;
        this.changeTime = changeTime;
        this.changedNode = changedNode;
        this.objectId = objectId;
    }

    /**
     * @see org.alfresco.cmis.CMISChangeEvent#getChangeType()
     */
    public CMISChangeType getChangeType()
    {
        return changeType;
    }

    /**
     * @see org.alfresco.cmis.CMISChangeEvent#getChangeTime()
     */
    public Date getChangeTime()
    {
        return changeTime;
    }

    
    /**
     * @see org.alfresco.cmis.CMISChangeEvent#getChangedNode()
     */
    public NodeRef getChangedNode()
    {
        return changedNode;
    }

    /**
     * @see org.alfresco.cmis.CMISChangeEvent#getObjectId()
     */
    public String getObjectId()
    {
        return objectId;
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
        return same(changedNode, converted.getChangedNode()) && same(changeType, converted.getChangeType()) && same(changeTime, converted.getChangeTime());
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
        int result = (null != changedNode) ? (changedNode.hashCode()) : (31);
        return result * 37 + (null != changeType ? changeType.hashCode() : 31) + (null != changeTime ? changeTime.hashCode() : 31);
    }

}
