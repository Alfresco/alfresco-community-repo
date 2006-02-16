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
package org.alfresco.repo.security.permissions.impl;

import java.util.HashSet;
import java.util.Set;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

public class PermissionServiceTest extends AbstractPermissionTest
{
    public PermissionServiceTest()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public void testAuthenticatedRoleIsPresent()
    {
        runAs("andy");
        Authentication auth = authenticationComponent.getCurrentAuthentication();
        for (GrantedAuthority authority : auth.getAuthorities())
        {
            if (authority.getAuthority().equals(ROLE_AUTHENTICATED))
            {
                return;
            }
        }
        fail("Missing role ROLE_AUTHENTICATED ");
    }

 
    
    public void testSetInheritFalse()
    {
        runAs("andy");
        permissionService.setInheritParentPermissions(rootNodeRef, false);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertFalse(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(0, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
    }

    public void testSetInheritTrue()
    {
        runAs("andy");
        permissionService.setInheritParentPermissions(rootNodeRef, true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(0, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermissions(permissionService.getSetPermissions(rootNodeRef));
    }

    public void testAlterInherit()
    {
        runAs("andy");
        testSetInheritFalse();
        testSetInheritTrue();
        testSetInheritFalse();
        testSetInheritTrue();

        permissionService.deletePermissions(rootNodeRef);
        // testUnset();
    }

    public void testSetNodePermissionEntry()
    {
        runAs("andy");
        Set<SimplePermissionEntry> entries = new HashSet<SimplePermissionEntry>();
        entries.add(new SimplePermissionEntry(rootNodeRef, new SimplePermissionReference(QName.createQName("A", "B"),
                "C"), "user-one", AccessStatus.ALLOWED));
        entries.add(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), "user-two",
                AccessStatus.ALLOWED));
        entries.add(new SimplePermissionEntry(rootNodeRef, new SimplePermissionReference(QName.createQName("D", "E"),
                "F"), permissionService.getAllAuthorities(), AccessStatus.ALLOWED));
        entries.add(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(),
                permissionService.getAllAuthorities(), AccessStatus.DENIED));

        SimpleNodePermissionEntry entry = new SimpleNodePermissionEntry(rootNodeRef, false, entries);

        permissionService.setPermission(entry);

        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertFalse(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(4, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
    }

    public void testSetNodePermissionEntry2()
    {
        Set<SimplePermissionEntry> entries = new HashSet<SimplePermissionEntry>();
        entries.add(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(),
                permissionService.getAllAuthorities(), AccessStatus.ALLOWED));

        SimpleNodePermissionEntry entry = new SimpleNodePermissionEntry(rootNodeRef, false, entries);

        permissionService.setPermission(entry);

        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertFalse(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
    }

    public void testAlterNodePermissions()
    {
        testSetNodePermissionEntry();
        testSetNodePermissionEntry2();
        testSetNodePermissionEntry();
        testSetNodePermissionEntry2();
    }

    public void testSetPermissionEntryElements()
    {
        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
        for (PermissionEntry pe : permissionService.getSetPermissions(rootNodeRef).getPermissionEntries())
        {
            assertEquals("andy", pe.getAuthority());
            assertTrue(pe.isAllowed());
            assertTrue(pe.getPermissionReference().getQName().equals(
                    permissionService.getAllPermissionReference().getQName()));
            assertTrue(pe.getPermissionReference().getName().equals(
                    permissionService.getAllPermissionReference().getName()));
            assertEquals(rootNodeRef, pe.getNodeRef());
        }

        // Set duplicate

        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // Set new

        permissionService.setPermission(rootNodeRef, "other", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // Add deny

        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), false);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(3, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // new

        permissionService.setPermission(rootNodeRef, "andy", PermissionService.READ, false);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(4, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // delete

        permissionService.deletePermission(rootNodeRef, "andy", PermissionService.READ, false);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(3, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(rootNodeRef, "andy", permissionService.getAllPermission(), false);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(rootNodeRef, "other", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(rootNodeRef, "andy", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(0, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

    }

    public void testSetPermissionEntry()
    {
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
        for (PermissionEntry pe : permissionService.getSetPermissions(rootNodeRef).getPermissionEntries())
        {
            assertEquals("andy", pe.getAuthority());
            assertTrue(pe.isAllowed());
            assertTrue(pe.getPermissionReference().getQName().equals(
                    permissionService.getAllPermissionReference().getQName()));
            assertTrue(pe.getPermissionReference().getName().equals(
                    permissionService.getAllPermissionReference().getName()));
            assertEquals(rootNodeRef, pe.getNodeRef());
        }

        // Set duplicate

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "andy", AccessStatus.ALLOWED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // Set new

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "other", AccessStatus.ALLOWED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // Deny

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "andy", AccessStatus.DENIED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(3, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // new

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, new SimplePermissionReference(QName
                .createQName("A", "B"), "C"), "andy", AccessStatus.DENIED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(4, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, new SimplePermissionReference(QName
                .createQName("A", "B"), "C"), "andy", AccessStatus.DENIED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(3, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "andy", AccessStatus.DENIED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "other", AccessStatus.ALLOWED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "andy", AccessStatus.ALLOWED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(0, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
    }

    public void testGetSettablePermissionsForType()
    {
        Set<String> answer = permissionService.getSettablePermissions(QName.createQName("sys", "base",
                namespacePrefixResolver));
        assertEquals(17, answer.size());

        answer = permissionService.getSettablePermissions(QName.createQName("cm", "ownable", namespacePrefixResolver));
        assertEquals(0, answer.size());

        answer = permissionService.getSettablePermissions(QName.createQName("cm", "content", namespacePrefixResolver));
        assertEquals(5, answer.size());

        answer = permissionService.getSettablePermissions(QName.createQName("cm", "folder", namespacePrefixResolver));
        assertEquals(5, answer.size());
    }

    public void testGetSettablePermissionsForNode()
    {
        QName ownable = QName.createQName("cm", "ownable", namespacePrefixResolver);

        Set<String> answer = permissionService.getSettablePermissions(rootNodeRef);
        assertEquals(21, answer.size());

        nodeService.addAspect(rootNodeRef, ownable, null);
        answer = permissionService.getSettablePermissions(rootNodeRef);
        assertEquals(21, answer.size());

        nodeService.removeAspect(rootNodeRef, ownable);
        answer = permissionService.getSettablePermissions(rootNodeRef);
        assertEquals(21, answer.size());
    }

    public void testSimplePermissionOnRoot()
    {
        runAs("andy");

        assertEquals(21, permissionService.getPermissions(rootNodeRef).size());
        assertEquals(0, countGranted(permissionService.getPermissions(rootNodeRef)));
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());

        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");

        assertEquals(21, permissionService.getPermissions(rootNodeRef).size());
        assertEquals(1, countGranted(permissionService.getPermissions(rootNodeRef)));

        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
    }

    private int countGranted(Set<AccessPermission> permissions)
    {
        int count = 0;
        for (AccessPermission ap : permissions)
        {
            if (ap.getAccessStatus() == AccessStatus.ALLOWED)
            {
                count++;
            }
        }
        return count;
    }

    public void testGlobalPermissionsForAdmin()
    {
        runAs("admin");
        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"),
                ContentModel.TYPE_CONTENT).getChildRef();

        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "admin", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "admin", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "admin", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CONTENT), "admin", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.ALL_PERMISSIONS), "admin", AccessStatus.DENIED));
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testPermissionGroupOnRoot()
    {
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        runAs("andy");

        assertEquals(21, permissionService.getPermissions(rootNodeRef).size());
        assertEquals(3, countGranted(permissionService.getPermissions(rootNodeRef)));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());

        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.DENIED));
        runAs("andy");
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.DENIED));
        runAs("andy");
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), "andy", AccessStatus.DENIED));
        runAs("andy");
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("andy");
    }

    public void testSimplePermissionSimpleInheritance()
    {
        runAs("admin");

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        // Changed ny not enfocing READ
        //assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        assertEquals(3, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        assertEquals(3, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        assertEquals(3, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        assertEquals(3, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
    }

    public void testPermissionGroupSimpleInheritance()
    {
        runAs("admin");

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), "andy", AccessStatus.DENIED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testDenySimplePermisionOnRootNode()
    {
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
    }

    public void testDenyPermissionOnRootNOde()
    {

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), "andy", AccessStatus.DENIED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testComplexDenyOnRootNode()
    {

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testPerf() throws Exception
    {
        runAs("admin");

        //TransactionService transactionService = serviceRegistry.getTransactionService();
        //UserTransaction tx = transactionService.getUserTransaction();
        //tx.begin();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n3 = nodeService.createNode(n2, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}three"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n4 = nodeService.createNode(n3, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}four"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n5 = nodeService.createNode(n4, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}five"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n6 = nodeService.createNode(n5, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}six"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n7 = nodeService.createNode(n6, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}seven"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n8 = nodeService.createNode(n7, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}eight"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n9 = nodeService.createNode(n8, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}nine"),
                ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n10 = nodeService.createNode(n9, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}ten"),
                ContentModel.TYPE_FOLDER).getChildRef();

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        // permissionService.setPermission(new SimplePermissionEntry(n9,
        // getPermission(PermissionService.READ),
        // "andy", AccessStatus.ALLOWED));
        // permissionService.setPermission(new SimplePermissionEntry(n10,
        // getPermission(PermissionService.READ),
        // "andy", AccessStatus.ALLOWED));

        long start;
        long end;
        long time = 0;
        for (int i = 0; i < 1000; i++)
        {
            getSession().flush();
            //getSession().clear();
            start = System.nanoTime();
            assertTrue(permissionService.hasPermission(n10, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            end = System.nanoTime();
            time += (end - start);
        }
        System.out.println("Time is " + (time / 1000000000.0));
        // assertTrue((time / 1000000000.0) < 60.0);

        time = 0;
        for (int i = 0; i < 1000; i++)
        {
            start = System.nanoTime();
            assertTrue(permissionService.hasPermission(n10, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            end = System.nanoTime();
            time += (end - start);
        }
        System.out.println("Time is " + (time / 1000000000.0));
        // assertTrue((time / 1000000000.0) < 2.0);

        //tx.rollback();
    }

    public void testAllPermissions()
    {
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "andy", AccessStatus.ALLOWED));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.DENIED));
        runAs("andy");
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "andy", AccessStatus.DENIED));
        assertEquals(3, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

    }
    
    
    public void testOldAllPermissions()
    {
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE, "andy", AccessStatus.ALLOWED));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.DENIED));
        runAs("andy");
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), "andy", AccessStatus.DENIED));
        assertEquals(3, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

    }
    

    public void testAuthenticatedAuthority()
    {

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ROLE_AUTHENTICATED, AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ROLE_AUTHENTICATED, AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), ROLE_AUTHENTICATED, AccessStatus.DENIED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), ROLE_AUTHENTICATED, AccessStatus.ALLOWED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testAllAuthorities()
    {

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                permissionService.getAllAuthorities(), AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                permissionService.getAllAuthorities(), AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), permissionService.getAllAuthorities(), AccessStatus.DENIED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ), permissionService.getAllAuthorities(), AccessStatus.ALLOWED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testAllPermissionsAllAuthorities()
    {

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), permissionService.getAllAuthorities(), AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                permissionService.getAllAuthorities(), AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService
                .getAllPermissionReference(), permissionService.getAllAuthorities(), AccessStatus.DENIED));
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testGroupAndUserInteraction()
    {

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                ROLE_AUTHENTICATED, AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.DENIED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testInheritPermissions()
    {
        runAs("admin");
        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"),
                ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "andy",
                AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setInheritParentPermissions(n2, false);

        runAs("andy");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setInheritParentPermissions(n2, true);

        runAs("andy");
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

    }

    public void testAncestorRequirementAndInheritance()
    {
        runAs("admin");

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"),
                ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ_CHILDREN),
                "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_PROPERTIES),
                "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ_CHILDREN),
                "andy", AccessStatus.DENIED));
        permissionService.setInheritParentPermissions(n2, false);

        runAs("andy");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setInheritParentPermissions(n2, true);

       
        
        runAs("andy");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        // Changed by removing permission read parents access
        //assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testEffectiveComposite()
    {

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

    }

    public void testContentPermissions()
    {
        runAs("admin");

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN,
                QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"),
                ContentModel.TYPE_CONTENT).getChildRef();

        runAs("andy");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ_CHILDREN),
                "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CHILDREN),
                "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_PROPERTIES),
                "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CONTENT),
                "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(n2,
                getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.deletePermission(new SimplePermissionEntry(n2,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        permissionService.deletePermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CONTENT),
                "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ), "andy",
                AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

    }

    public void testAllPermissionSet()
    {
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.FULL_CONTROL), "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.FULL_CONTROL), "andy", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.FULL_CONTROL), "andy", AccessStatus.DENIED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

    }

    public void testChildrenRequirements()
    {
        if (!personService.createMissingPeople())
        {
            assertEquals(1, nodeService.getChildAssocs(rootNodeRef).size());
        }
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_NODE)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_NODE)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.DELETE),
                "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_NODE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_NODE)) == AccessStatus.ALLOWED);

        runAs("andy");
        assertTrue(permissionService.hasPermission(systemNodeRef, getPermission(PermissionService.DELETE_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(systemNodeRef, getPermission(PermissionService.DELETE_NODE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(systemNodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(systemNodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(systemNodeRef, getPermission(PermissionService.DELETE_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(systemNodeRef, getPermission(PermissionService.DELETE_NODE)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(systemNodeRef,
                getPermission(PermissionService.DELETE), "andy", AccessStatus.DENIED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_CHILDREN)) == AccessStatus.ALLOWED);
        // The following are now true as we have no cascade delete check
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_NODE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.DELETE_NODE)) == AccessStatus.ALLOWED);

    }

    public void testClearPermission()
    {
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ),
                "lemur", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
                getPermission(PermissionService.READ_CHILDREN), "lemur", AccessStatus.ALLOWED));
        assertEquals(4, permissionService.getAllSetPermissions(rootNodeRef).size());

        permissionService.clearPermission(rootNodeRef, "andy");
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.clearPermission(rootNodeRef, "lemur");
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());

    }


    // TODO: Test permissions on missing nodes
    
   
}
