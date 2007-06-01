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
package org.alfresco.repo.security.permissions.impl.model;

import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.AbstractPermissionTest;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.security.permissions.impl.RequiredPermission.On;
import org.alfresco.service.namespace.QName;

public class PermissionModelTest extends AbstractPermissionTest
{
     
    public PermissionModelTest()
    {
        super();
    }

    public void testWoof()
    {
        QName typeQname = nodeService.getType(rootNodeRef);
        Set<QName> aspectQNames = nodeService.getAspects(rootNodeRef);
        PermissionReference ref = permissionModelDAO.getPermissionReference(null, "CheckOut");
        Set<PermissionReference> answer = permissionModelDAO.getRequiredPermissions(ref, typeQname, aspectQNames, On.NODE);
        assertEquals(1, answer.size());
    }
    
    public void testIncludePermissionGroups()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Consumer"));

        assertEquals(8, grantees.size());
    }
    
    public void testIncludePermissionGroups2()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Contributor"));

        assertEquals(14, grantees.size());
    }
    
    public void testIncludePermissionGroups3()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Editor"));

        assertEquals(17, grantees.size());
    }
    
    public void testIncludePermissionGroups4()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Collaborator"));

        assertEquals(24, grantees.size());
    }
    
    public void testIncludePermissionGroups5()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "Coordinator"));

        // NB This has gone from 59 to 63, I believe, because of the for new WCM roles.
        assertEquals(63, grantees.size());
    }
    
    public void testIncludePermissionGroups6()
    {
        Set<PermissionReference> grantees = permissionModelDAO.getGranteePermissions(new SimplePermissionReference(QName.createQName("cm", "cmobject",
                namespacePrefixResolver), "RecordAdministrator"));

        assertEquals(19, grantees.size());
    }
    
    public void testGetGrantingPermissions()
    {
        Set<PermissionReference> granters = permissionModelDAO.getGrantingPermissions(new SimplePermissionReference(QName.createQName("sys", "base",
                namespacePrefixResolver), "ReadProperties"));
        // NB This has gone from 10 to 14 because of the new WCM roles, I believe.
        assertEquals(16, granters.size());
        
        granters = permissionModelDAO.getGrantingPermissions(new SimplePermissionReference(QName.createQName("sys", "base",
                namespacePrefixResolver), "_ReadProperties"));
        // NB 11 to 15 as above.
        assertEquals(17, granters.size());
    }
    
    public void testGlobalPermissions()
    {
        Set<? extends PermissionEntry> globalPermissions = permissionModelDAO.getGlobalPermissionEntries();
        assertEquals(5, globalPermissions.size());
    }
    
}
