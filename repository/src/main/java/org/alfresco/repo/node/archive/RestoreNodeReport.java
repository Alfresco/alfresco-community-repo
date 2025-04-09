/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node.archive;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A simple record of an attempt to restore a node from an archive store.
 * 
 * @author Derek Hulley
 */
public class RestoreNodeReport implements Serializable
{
    private static final long serialVersionUID = 7375879981852517364L;

    /**
     * Represents the state of a restore process.
     */
    public static enum RestoreStatus
    {
        /** the operation was a success */
        SUCCESS
        {
            @Override
            public boolean isSuccess()
            {
                return true;
            }

        },
        /** the node to restore was missing */
        FAILURE_INVALID_ARCHIVE_NODE
        {},
        /** the destination parent of the restore operation was missing */
        FAILURE_INVALID_PARENT
        {},
        /** the permissions required for either reading or writing were invalid */
        FAILURE_PERMISSION
        {},
        /** there was an integrity failure after the node was restored */
        FAILURE_INTEGRITY
        {},
        /** duplicate child name not allowed **/
        FAILURE_DUPLICATE_CHILD_NODE_NAME
        {},
        /** the problem was not well-recognized */
        FAILURE_OTHER
        {};

        /**
         * 
         * @return Returns <tt>true</tt> if the status represents a successful state
         */
        public boolean isSuccess()
        {
            return false;
        }
    }

    private NodeRef archivedNodeRef;
    private NodeRef targetParentNodeRef;
    private NodeRef restoredNodeRef;
    private RestoreStatus status;
    private Throwable cause;

    /* package */ RestoreNodeReport(NodeRef archivedNodeRef)
    {
        this.archivedNodeRef = archivedNodeRef;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(100);
        sb.append("RestoreNodeReport")
                .append("[ archived=").append(archivedNodeRef)
                .append(", restored=").append(restoredNodeRef)
                .append(", parent=").append(targetParentNodeRef)
                .append(", status=").append(status)
                .append(", err=").append((cause == null ? "<none>" : cause.getMessage()));
        return sb.toString();
    }

    public NodeRef getArchivedNodeRef()
    {
        return archivedNodeRef;
    }

    public NodeRef getTargetParentNodeRef()
    {
        return targetParentNodeRef;
    }

    /* package */ void setTargetParentNodeRef(NodeRef targetParentNodeRef)
    {
        this.targetParentNodeRef = targetParentNodeRef;
    }

    public NodeRef getRestoredNodeRef()
    {
        return restoredNodeRef;
    }

    /* package */ void setRestoredNodeRef(NodeRef restoredNodeRef)
    {
        this.restoredNodeRef = restoredNodeRef;
    }

    public RestoreStatus getStatus()
    {
        return status;
    }

    /* package */ void setStatus(RestoreStatus status)
    {
        this.status = status;
    }

    public Throwable getCause()
    {
        return cause;
    }

    /* package */ void setCause(Throwable cause)
    {
        this.cause = cause;
    }
}
