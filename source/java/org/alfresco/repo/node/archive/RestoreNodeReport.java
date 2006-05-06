/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
        SUCCESS
        {
            @Override
            public boolean isSuccess()
            {
                return true;
            }
            
        },
        FAILURE_INVALID_PARENT
        {
        },
        FAILURE_PERMISSION
        {
        },
        FAILURE_INTEGRITY
        {
        },
        FAILURE_OTHER
        {
        };
        
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
    
    /* package */ RestoreNodeReport(
            RestoreStatus status,
            NodeRef archivedNodeRef,
            NodeRef targetParentNodeRef,
            NodeRef restoredNodeRef,
            Throwable cause)
    {
        this.status = status;
        this.archivedNodeRef = archivedNodeRef;
        this.targetParentNodeRef = targetParentNodeRef;
        this.restoredNodeRef = restoredNodeRef;
        this.cause = cause;
    }

    public NodeRef getArchivedNodeRef()
    {
        return archivedNodeRef;
    }

    public NodeRef getTargetParentNodeRef()
    {
        return targetParentNodeRef;
    }

    public NodeRef getRestoredNodeRef()
    {
        return restoredNodeRef;
    }

    public RestoreStatus getStatus()
    {
        return status;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
