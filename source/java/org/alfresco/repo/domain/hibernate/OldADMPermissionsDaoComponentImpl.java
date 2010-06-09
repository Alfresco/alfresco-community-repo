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
package org.alfresco.repo.domain.hibernate;

import java.util.Collections;

import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support for accessing persisted permission information. This class maps between persisted objects and the external
 * API defined in the PermissionsDAO interface.
 * 
 * @author andyh
 */
public class OldADMPermissionsDaoComponentImpl extends AbstractPermissionsDaoComponentImpl
{
    private static Log logger = LogFactory.getLog(OldADMPermissionsDaoComponentImpl.class);

    /**
     * 
     */
    public OldADMPermissionsDaoComponentImpl()
    {
        super();
    }

    /**
     * Creates an access control list for the node and removes the entry from the nullPermsionCache.
     */
    protected AbstractPermissionsDaoComponentImpl.CreationReport createAccessControlList(NodeRef nodeRef, boolean inherit, DbAccessControlList existing)
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.OLD);
        properties.setInherits(inherit);
        Long id = aclDaoComponent.createAccessControlList(properties);
        DbAccessControlList acl = aclDaoComponent.getDbAccessControlList(id);

        // maintain inverse
        getACLDAO(nodeRef).setAccessControlList(nodeRef, acl);

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created Access Control List: \n" + "   node: " + nodeRef + "\n" + "   list: " + acl);
        }

        AbstractPermissionsDaoComponentImpl.CreationReport report = new AbstractPermissionsDaoComponentImpl.CreationReport(acl, Collections
                .<AclChange> singletonList(new AclDaoComponentImpl.AclChangeImpl(null, id, null, acl.getAclType())));
        return report;

    }

    public void deletePermissions(NodeRef nodeRef)
    {
        DbAccessControlList acl = null;
        try
        {
            acl = getAccessControlList(nodeRef);
        }
        catch (InvalidNodeRefException e)
        {
            return;
        }
        if (acl != null)
        {
            // maintain referencial integrity
            getACLDAO(nodeRef).setAccessControlList(nodeRef, (Long) null);
            aclDaoComponent.deleteAccessControlList(acl.getId());
        }
    }
    
    public static SimpleAccessControlListProperties getDefaultProperties()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.OLD);
        properties.setInherits(true);
        properties.setVersioned(false);
        return properties;
    }
}
