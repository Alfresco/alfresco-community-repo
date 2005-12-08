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
package org.alfresco.repo.security.permissions.impl.hibernate;

import org.alfresco.util.EqualsHelper;

/**
 * Persisted permission entries
 * 
 * @author andyh
 */
public class PermissionEntryImpl implements PermissionEntry
{
    /**
     * The object id
     */
    private long id;
    
    /**
     * The container of this permissions
     */
    private NodePermissionEntry nodePermissionEntry;

    /**
     * The permission to which this applies
     * (non null - all is a special string)
     */
    private PermissionReference permissionReference;

    /**
     * The recipient to which this applies
     * (non null - all is a special string)
     */
    private Recipient recipient;

    /**
     * Is this permission allowed?
     */
    private boolean allowed;

    public PermissionEntryImpl()
    {
        super();
    }
    
    public long getId()
    {
        return id;
    }
    
    // Hibernate
    
    /* package */ void setId(long id)
    {
        this.id = id;
    }

    public NodePermissionEntry getNodePermissionEntry()
    {
        return nodePermissionEntry;
    }

    private void setNodePermissionEntry(NodePermissionEntry nodePermissionEntry)
    {
        this.nodePermissionEntry = nodePermissionEntry;
    }

    public PermissionReference getPermissionReference()
    {
        return permissionReference;
    }

    private void setPermissionReference(PermissionReference permissionReference)
    {
        this.permissionReference = permissionReference;
    }

    public Recipient getRecipient()
    {
        return recipient;
    }

    private void setRecipient(Recipient recipient)
    {
        this.recipient = recipient;
    }

    public boolean isAllowed()
    {
        return allowed;
    }

    public void setAllowed(boolean allowed)
    {
        this.allowed = allowed;
    }


    /**
     * Factory method to create an entry and wire it in to the contained nodePermissionEntry
     * 
     * @param nodePermissionEntry
     * @param permissionReference
     * @param recipient
     * @param allowed
     * @return
     */
    public static PermissionEntryImpl create(NodePermissionEntry nodePermissionEntry, PermissionReference permissionReference, Recipient recipient, boolean allowed)
    {
        PermissionEntryImpl permissionEntry = new PermissionEntryImpl();
        permissionEntry.setNodePermissionEntry(nodePermissionEntry);
        permissionEntry.setPermissionReference(permissionReference);
        permissionEntry.setRecipient(recipient);
        permissionEntry.setAllowed(allowed);
        nodePermissionEntry.getPermissionEntries().add(permissionEntry);
        return permissionEntry;
    }

    /**
     * Unwire 
     */
    public void delete()
    {
        nodePermissionEntry.getPermissionEntries().remove(this);
    }
    
    //
    // Hibernate object pattern
    //

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PermissionEntryImpl))
        {
            return false;
        }
        PermissionEntryImpl other = (PermissionEntryImpl) o;
        return EqualsHelper.nullSafeEquals(this.nodePermissionEntry,
                other.nodePermissionEntry)
                && EqualsHelper.nullSafeEquals(this.permissionReference,
                        other.permissionReference)
                && EqualsHelper.nullSafeEquals(this.recipient, other.recipient)
                && (this.allowed == other.allowed);
    }

    @Override
    public int hashCode()
    {
        int hashCode = nodePermissionEntry.hashCode();
        if (permissionReference != null)
        {
            hashCode = hashCode * 37 + permissionReference.hashCode();
        }
        if (recipient != null)
        {
            hashCode = hashCode * 37 + recipient.hashCode();
        }
        hashCode = hashCode * 37 + (allowed ? 1 : 0);
        return hashCode;
    }

}
