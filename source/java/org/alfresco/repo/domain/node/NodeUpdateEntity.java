/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.node;

/**
 * Bean to convey <b>alf_node</b> update data.  It uses the basic node data, but adds
 * information to identify the properties that need updating.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeUpdateEntity extends NodeEntity
{
    private boolean updateStore;
    private boolean updateUuid;
    private boolean updateTypeQNameId;
    private boolean updateAclId;
    private boolean updateDeleted;
    private boolean updateTransaction;
    private boolean updateAuditableProperties;
    
    /**
     * Required default constructor
     */
    public NodeUpdateEntity()
    {
    }
    
    /**
     * Determine if this update represents anything new at all
     */
    public boolean isUpdateAnything()
    {
        return updateAuditableProperties || updateTransaction || updateDeleted
                || updateAclId || updateStore || updateUuid || updateTypeQNameId;
    }

    public boolean isUpdateStore()
    {
        return updateStore;
    }

    public void setUpdateStore(boolean updateStore)
    {
        this.updateStore = updateStore;
    }

    public boolean isUpdateUuid()
    {
        return updateUuid;
    }

    public void setUpdateUuid(boolean updateUuid)
    {
        this.updateUuid = updateUuid;
    }

    public boolean isUpdateTypeQNameId()
    {
        return updateTypeQNameId;
    }

    public void setUpdateTypeQNameId(boolean updateTypeQNameId)
    {
        this.updateTypeQNameId = updateTypeQNameId;
    }

    public boolean isUpdateAclId()
    {
        return updateAclId;
    }

    public void setUpdateAclId(boolean updateAclId)
    {
        this.updateAclId = updateAclId;
    }

    public boolean isUpdateDeleted()
    {
        return updateDeleted;
    }

    public void setUpdateDeleted(boolean updateDeleted)
    {
        this.updateDeleted = updateDeleted;
    }

    public boolean isUpdateTransaction()
    {
        return updateTransaction;
    }

    public void setUpdateTransaction(boolean updateTransaction)
    {
        this.updateTransaction = updateTransaction;
    }

    public boolean isUpdateAuditableProperties()
    {
        return updateAuditableProperties;
    }

    public void setUpdateAuditableProperties(boolean updateAuditableProperties)
    {
        this.updateAuditableProperties = updateAuditableProperties;
    }
}
