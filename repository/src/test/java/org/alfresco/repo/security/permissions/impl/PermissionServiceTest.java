/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.security.permissions.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.permissions.ADMAccessControlListDAO;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.Policy;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnGrantLocalPermission;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnInheritPermissionsDisabled;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnInheritPermissionsEnabled;
import org.alfresco.repo.security.permissions.PermissionServicePolicies.OnRevokeLocalPermission;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionContext;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.testing.category.PerformanceTests;
import org.junit.FixMethodOrder;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

@Category(OwnJVMTestsCategory.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PermissionServiceTest extends AbstractPermissionTest
{
    // The number of permissions in the system.
    // See permissionDefinitions.xml and PermissionService interface
    private static final int NUM_OF_PERMISSIONS = 39;

    private SimplePermissionEntry denyAndyAll;

    private SimplePermissionEntry allowAndyAll;

    private SimplePermissionEntry denyAndyRead;

    private SimplePermissionEntry allowAndyRead;

    private SimplePermissionEntry denyAndyReadProperties;

    private SimplePermissionEntry allowAndyReadProperties;

    private SimplePermissionEntry allowAndyReadChildren;

    public PermissionServiceTest()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public void testPublicAccessService()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getOwnerAuthority") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllAuthorities") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllPermission") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getSettablePermissions", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "hasPermission", rootNodeRef, PermissionService.READ_PERMISSIONS) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "hasReadPermission", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getInheritParentPermissions", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAuthorisations") ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getPermissions", rootNodeRef) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllSetPermissions", rootNodeRef) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllPermission") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "deletePermissions", rootNodeRef) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "deletePermission", rootNodeRef, "andy", PermissionService.READ_PERMISSIONS) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "setPermission", rootNodeRef, "andy", PermissionService.READ_PERMISSIONS, true) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "setInheritParentPermissions", rootNodeRef, true) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "clearPermission", rootNodeRef, "andy") ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAuthorisations") ==  AccessStatus.DENIED);

        runAs("admin");
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getOwnerAuthority") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllAuthorities") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllPermission") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getSettablePermissions", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "hasPermission", rootNodeRef, PermissionService.READ_PERMISSIONS) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "hasReadPermission", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getReaders", nodeService.getNodeAclId(rootNodeRef)) == AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getPermissions", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllSetPermissions", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllPermission") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "deletePermissions", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "deletePermission", rootNodeRef, "andy", PermissionService.READ_PERMISSIONS) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "setPermission", rootNodeRef, "andy", PermissionService.READ_PERMISSIONS, true) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "setInheritParentPermissions", rootNodeRef, true) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "clearPermission", rootNodeRef, "andy") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAuthorisations") ==  AccessStatus.DENIED);

        assertTrue(publicServiceAccessService.hasAccess("SiteService", "createSite", "", "", "", "", true) == AccessStatus.ALLOWED);

        runAs("andy");
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getOwnerAuthority") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllAuthorities") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllPermission") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getSettablePermissions", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "hasPermission", rootNodeRef, PermissionService.READ_PERMISSIONS) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "hasReadPermission", rootNodeRef) ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getReaders", nodeService.getNodeAclId(rootNodeRef)) == AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getPermissions", rootNodeRef) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllSetPermissions", rootNodeRef) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAllPermission") ==  AccessStatus.ALLOWED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "deletePermissions", rootNodeRef) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "deletePermission", rootNodeRef, "andy", PermissionService.READ_PERMISSIONS) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "setPermission", rootNodeRef, "andy", PermissionService.READ_PERMISSIONS, true) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "setInheritParentPermissions", rootNodeRef, true) ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "clearPermission", rootNodeRef, "andy") ==  AccessStatus.DENIED);
        assertTrue(publicServiceAccessService.hasAccess("PermissionService", "getAuthorisations") ==  AccessStatus.DENIED);
    }
    
    public void testAnyDenyDeniesAndRead()
    {
        personService.getPerson("andy");
        runAs("admin");

        authorityService.createAuthority(AuthorityType.GROUP, "ONE");
        authorityService.addAuthority("GROUP_ONE", "andy");

        authorityService.createAuthority(AuthorityType.GROUP, "TWO");
        authorityService.addAuthority("GROUP_TWO", "andy");

        authorityService.createAuthority(AuthorityType.GROUP, "THREE");
        authorityService.addAuthority("GROUP_THREE", "andy");

        authorityService.createAuthority(AuthorityType.GROUP, "GEN");
        authorityService.addAuthority("GROUP_GEN", "andy");

        NodeRef one = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(one, "andy", PermissionService.READ, true);
        permissionService.setPermission(one, "GROUP_ONE", PermissionService.READ, true);

        NodeRef two = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(two, "andy", PermissionService.READ, true);
        permissionService.setPermission(two, "GROUP_TWO", PermissionService.READ, true);

        NodeRef three = nodeService.createNode(one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(three, "andy", PermissionService.READ, true);
        permissionService.setPermission(three, "GROUP_THREE", PermissionService.READ, true);

        NodeRef four = nodeService.createNode(three, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}four"), ContentModel.TYPE_FOLDER).getChildRef();

        permissionServiceImpl.setAnyDenyDenies(false);
        try
        {


            runAs("andy");
            assertEquals("andy", authenticationComponent.getCurrentUserName());
            assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.ALLOWED);

            assertTrue(permissionService.hasReadPermission(one) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasReadPermission(three) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasReadPermission(four) == AccessStatus.ALLOWED);

            // Deny group on  one
            runAs("admin");
            permissionService.setPermission(one, "GROUP_ONE", PermissionService.READ, false);

            runAs("andy");
            assertEquals("andy", authenticationComponent.getCurrentUserName());
            assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.ALLOWED);

            assertTrue(permissionService.hasReadPermission(one) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasReadPermission(three) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasReadPermission(four) == AccessStatus.ALLOWED);

        }
        finally
        {
            // ANY DENY DENIES

            permissionServiceImpl.setAnyDenyDenies(true);
        }

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.DENIED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.DENIED);

        runAs("admin");
        permissionService.deletePermission(one, "GROUP_ONE", PermissionService.READ);
        permissionService.setPermission(one, "GROUP_ONE", PermissionService.READ, true);

        // deny andy on one but explicitly allowed on three

        runAs("admin");
        permissionService.setPermission(one, "andy", PermissionService.READ, false);


        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.ALLOWED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.ALLOWED);

        runAs("admin");
        permissionService.deletePermission(one, "andy", PermissionService.READ);
        permissionService.setPermission(one, "andy", PermissionService.READ, true);

        // Deny by all - the underlying allow should win

        runAs("admin");
        permissionService.setPermission(one, "andy", PermissionService.ALL_PERMISSIONS, false);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.ALLOWED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.ALLOWED);

        runAs("admin");
        permissionService.deletePermission(one, "andy", PermissionService.ALL_PERMISSIONS);

        // Deny by all - should win as no underlying mask

        runAs("admin");
        permissionService.setPermission(one, "GROUP_GEN", PermissionService.ALL_PERMISSIONS, false);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.DENIED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.DENIED);

        runAs("admin");
        permissionService.setInheritParentPermissions(three, false);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.ALLOWED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.ALLOWED);

        runAs("admin");
        permissionService.setInheritParentPermissions(three, true);
        permissionService.deletePermission(one, "GROUP_GEN", PermissionService.ALL_PERMISSIONS);

        // direct deny

        runAs("admin");
        permissionService.setPermission(three, "andy", PermissionService.READ, false);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.DENIED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.DENIED);

        runAs("admin");
        permissionService.deletePermission(three, "andy", PermissionService.READ);
        permissionService.setPermission(three, "andy", PermissionService.READ, true);
        
        permissionService.setPermission(one, "GROUP_ONE", PermissionService.READ, false);
        permissionService.setPermission(two, "GROUP_TWO", PermissionService.READ, false);
        permissionService.setPermission(three, "GROUP_THREE", PermissionService.READ, false);
        
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.DENIED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.DENIED);
        
        runAs("admin");
        authorityService.removeAuthority("GROUP_ONE", "andy");
        AlfrescoTransactionSupport.bindResource("MyAuthCache", null);
        
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.DENIED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.DENIED);
        
        runAs("admin");
        authorityService.removeAuthority("GROUP_TWO", "andy");
        AlfrescoTransactionSupport.bindResource("MyAuthCache", null);
        
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.DENIED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.DENIED);
        
        runAs("admin");
        authorityService.removeAuthority("GROUP_THREE", "andy");
        AlfrescoTransactionSupport.bindResource("MyAuthCache", null);
        
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.ALLOWED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.ALLOWED);
        
        runAs("admin");
        permissionService.setPermission(one, PermissionService.ALL_AUTHORITIES, PermissionService.READ, false);
        
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.DENIED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.DENIED);
        
        runAs("admin");
        permissionService.setPermission(rootNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, false);
        
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(two, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(three, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(four, PermissionService.READ) == AccessStatus.DENIED);

        assertTrue(permissionService.hasReadPermission(one) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(two) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(three) == AccessStatus.DENIED);
        assertTrue(permissionService.hasReadPermission(four) == AccessStatus.DENIED);
    }

    /*
     * Tests that the current user is contained in the current authorisations set
     */
    public void testCurrentUserAuthority()
    {
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());

        Set<String> authorisations = permissionService.getAuthorisations();
        assertTrue("", authorisations.contains("andy"));
    }

    public void testMove()
    {
        runAs("admin");
        NodeRef one = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(one, "andy", PermissionService.READ, true);
        NodeRef two = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(two, "andy", PermissionService.WRITE, true);
        NodeRef three = nodeService.createNode(one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();


        NodeRef test = nodeService.createNode(one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"), ContentModel.TYPE_FOLDER).getChildRef();

        // test has shared acl


        // under 1
        // start
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);

        //under 2
        // def parent -> def parent
        runAs("admin");
        nodeService.moveNode(test, two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(two, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.ALLOWED);

        // under 3
        // def parent -> shared parent
        runAs("admin");
        nodeService.moveNode(test, three, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(three, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);

        //under 2
        // shared parent -> def parent
        runAs("admin");
        nodeService.moveNode(test, two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(two, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.ALLOWED);

        //under 1
        // def parent -> def parent
        runAs("admin");
        nodeService.moveNode(test, one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(one, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);


        // test has defining acl

        runAs("admin");
        permissionService.setPermission(test, "andy", PermissionService.CHANGE_PERMISSIONS, true);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        // 2
        runAs("admin");
        nodeService.moveNode(test, two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(two, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        // 3
        runAs("admin");
        nodeService.moveNode(test, three, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(three, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        // 2
        runAs("admin");
        nodeService.moveNode(test, two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(two, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        // 1
        runAs("admin");
        nodeService.moveNode(test, one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(one, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        // 1 inherit

        runAs("admin");
        permissionService.setInheritParentPermissions(test, true);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        runAs("admin");
        permissionService.setInheritParentPermissions(test, false);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        runAs("admin");
        permissionService.setInheritParentPermissions(test, true);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        // 2 inherit

        runAs("admin");
        nodeService.moveNode(test, two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(two, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);


        runAs("admin");
        permissionService.setInheritParentPermissions(test, true);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        runAs("admin");
        permissionService.setInheritParentPermissions(test, false);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        runAs("admin");
        permissionService.setInheritParentPermissions(test, true);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        // 3 inherit

        runAs("admin");
        nodeService.moveNode(test, three, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(three, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        runAs("admin");
        permissionService.setInheritParentPermissions(test, true);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        runAs("admin");
        permissionService.setInheritParentPermissions(test, false);

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        // move to 2 without inherit

        runAs("admin");
        nodeService.moveNode(test, two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(two, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

        // move to 3 without inherit

        runAs("admin");
        nodeService.moveNode(test, three, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        assertEquals(three, nodeService.getPrimaryParent(test).getParentRef());

        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(test, PermissionService.READ) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.WRITE) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(test, PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED);

    }

    public void testChangePersonUid()
    {
        runAs("admin");
        NodeRef one = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(one, "andy", PermissionService.ALL_PERMISSIONS, true);
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.EXECUTE_CONTENT) == AccessStatus.ALLOWED);
        runAs("admin");
        boolean found = false;
        Set<AccessPermission> set = permissionService.getAllSetPermissions(one);
        for (AccessPermission ap : set)
        {
            if (ap.getAuthority().equals("Andy"))
            {
                found = true;
            }
        }
        assertFalse(found);
        NodeRef andy = personService.getPerson("andy");
        nodeService.setProperty(andy, ContentModel.PROP_USERNAME, "Andy");
        runAs("andy");
        assertEquals("Andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.EXECUTE_CONTENT) == AccessStatus.ALLOWED);
        runAs("admin");
        found = false;
        set = permissionService.getAllSetPermissions(one);
        for (AccessPermission ap : set)
        {
            if (ap.getAuthority().equals("Andy"))
            {
                found = true;
            }
        }
        assertTrue(found);

        try
        {
            nodeService.setProperty(andy, ContentModel.PROP_USERNAME, "Bob");
            fail("Chainging uid Andy -> Bob should fail");
        }
        catch (UnsupportedOperationException e)
        {

        }
    }

    public void testChangeGroupUid()
    {
        runAs("admin");
        personService.getPerson("andy");
        NodeRef one = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        authorityService.createAuthority(AuthorityType.GROUP, "ONE");
        authorityService.addAuthority("GROUP_ONE", "andy");
        permissionService.setPermission(one, "GROUP_ONE", PermissionService.ALL_PERMISSIONS, true);
        runAs("andy");
        assertEquals("andy", authenticationComponent.getCurrentUserName());
        assertTrue(permissionService.hasPermission(one, PermissionService.EXECUTE_CONTENT) == AccessStatus.ALLOWED);
        runAs("admin");
        boolean found = false;
        Set<AccessPermission> set = permissionService.getAllSetPermissions(one);
        for (AccessPermission ap : set)
        {
            if (ap.getAuthority().equals("GROUP_One"))
            {
                found = true;
            }
        }
        assertFalse(found);
        NodeRef gONE = authorityDAO.getAuthorityNodeRefOrNull("GROUP_ONE");
        nodeService.setProperty(gONE, ContentModel.PROP_AUTHORITY_NAME, "GROUP_One");
        runAs("andy");
        assertTrue(permissionService.hasPermission(one, PermissionService.EXECUTE_CONTENT) == AccessStatus.ALLOWED);
        runAs("admin");
        found = false;
        set = permissionService.getAllSetPermissions(one);
        for (AccessPermission ap : set)
        {
            if (ap.getAuthority().equals("GROUP_One"))
            {
                found = true;
            }
        }
        assertTrue(found);

        try
        {
            nodeService.setProperty(gONE, ContentModel.PROP_AUTHORITY_NAME, "GROUP_TWO");
            fail("Chainging gid GROUP_One -> GROUP_TWO should fail");
        }
        catch (UnsupportedOperationException e)
        {

        }
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

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        denyAndyAll = new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), "andy", AccessStatus.DENIED);
        allowAndyAll = new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), "andy", AccessStatus.ALLOWED);
        denyAndyRead = new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.DENIED);
        allowAndyRead = new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED);
        denyAndyReadProperties = new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED);
        allowAndyReadProperties = new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED);
        allowAndyReadChildren = new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED);
    }

    public void testDelete()
    {
        if(aclDaoComponent.getDefaultProperties().getAclType() == ACLType.DEFINING)
        {
            runAs(AuthenticationUtil.getAdminUserName());

            NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
            NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();

            assertEquals(0, permissionService.getAllSetPermissions(n1).size());
            assertEquals(0, permissionService.getAllSetPermissions(n2).size());

            permissionService.deletePermissions(n1);
            permissionService.deletePermissions(n2);

            permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(1, permissionService.getAllSetPermissions(n2).size());

            permissionService.deletePermissions(n2);

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(1, permissionService.getAllSetPermissions(n2).size());

            permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.WRITE), "andy", AccessStatus.ALLOWED));

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(2, permissionService.getAllSetPermissions(n2).size());

            permissionService.deletePermissions(n2);

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(1, permissionService.getAllSetPermissions(n2).size());

            permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.WRITE), "andy", AccessStatus.ALLOWED));

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(2, permissionService.getAllSetPermissions(n2).size());

            permissionService.deletePermissions(n1);

            assertEquals(0, permissionService.getAllSetPermissions(n1).size());
            assertEquals(1, permissionService.getAllSetPermissions(n2).size());
        }
        else
        {
            runAs(AuthenticationUtil.getAdminUserName());

            NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
            NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();

            assertEquals(0, permissionService.getAllSetPermissions(n1).size());
            assertEquals(0, permissionService.getAllSetPermissions(n2).size());

            permissionService.deletePermissions(n1);
            permissionService.deletePermissions(n2);

            permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(0, permissionService.getAllSetPermissions(n2).size());

            permissionService.deletePermissions(n2);

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(0, permissionService.getAllSetPermissions(n2).size());

            permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.WRITE), "andy", AccessStatus.ALLOWED));

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(1, permissionService.getAllSetPermissions(n2).size());

            permissionService.deletePermissions(n2);

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(0, permissionService.getAllSetPermissions(n2).size());

            permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.WRITE), "andy", AccessStatus.ALLOWED));

            assertEquals(1, permissionService.getAllSetPermissions(n1).size());
            assertEquals(1, permissionService.getAllSetPermissions(n2).size());

            permissionService.deletePermissions(n1);

            assertEquals(0, permissionService.getAllSetPermissions(n1).size());
            assertEquals(1, permissionService.getAllSetPermissions(n2).size());
        }
    }

    /**
     * This will break when merged forward
     */
    public void testPositionInformation()
    {
        if(aclDaoComponent.getDefaultProperties().getAclType() == ACLType.DEFINING)
        {
            runAs(AuthenticationUtil.getAdminUserName());
            NodeRef one = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
            NodeRef two = nodeService.createNode(one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
            NodeRef three = nodeService.createNode(two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), ContentModel.TYPE_FOLDER).getChildRef();
            NodeRef four = nodeService.createNode(three, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), ContentModel.TYPE_FOLDER).getChildRef();
            permissionService.setPermission(one, "andy", PermissionService.ALL_PERMISSIONS, true);
            permissionService.setPermission(two, "bob", PermissionService.ALL_PERMISSIONS, true);
            permissionService.setPermission(three, "carol", PermissionService.ALL_PERMISSIONS, true);

            Set<AccessPermission> set = permissionService.getAllSetPermissions(one);
            assertEquals(1, set.size());
            for (AccessPermission ap : set)
            {
                if (ap.getAuthority().equals("andy"))
                {
                    assertFalse(ap.isInherited());
                    assertEquals(0, ap.getPosition());
                }
            }

            set = permissionService.getAllSetPermissions(two);
            assertEquals(2, set.size());
            for (AccessPermission ap : set)
            {
                if (ap.getAuthority().equals("andy"))
                {
                    assertTrue(ap.isInherited());
                    assertEquals(2, ap.getPosition());
                }
                if (ap.getAuthority().equals("bob"))
                {
                    assertFalse(ap.isInherited());
                    assertEquals(0, ap.getPosition());
                }
            }

            set = permissionService.getAllSetPermissions(three);
            assertEquals(3, set.size());
            for (AccessPermission ap : set)
            {
                if (ap.getAuthority().equals("andy"))
                {
                    assertTrue(ap.isInherited());
                    assertEquals(4, ap.getPosition());
                }
                if (ap.getAuthority().equals("bob"))
                {
                    assertTrue(ap.isInherited());
                    assertEquals(2, ap.getPosition());
                }
                if (ap.getAuthority().equals("carol"))
                {
                    assertFalse(ap.isInherited());
                    assertEquals(0, ap.getPosition());
                }
            }

            set = permissionService.getAllSetPermissions(four);
            assertEquals(3, set.size());
            for (AccessPermission ap : set)
            {
                if (ap.getAuthority().equals("andy"))
                {
                    assertTrue(ap.isInherited());
                    assertEquals(5, ap.getPosition());
                }
                if (ap.getAuthority().equals("bob"))
                {
                    assertTrue(ap.isInherited());
                    assertEquals(3, ap.getPosition());
                }
                if (ap.getAuthority().equals("carol"))
                {
                    assertTrue(ap.isInherited());
                    assertEquals(1, ap.getPosition());
                }
            }
        }
        else
        {
            runAs(AuthenticationUtil.getAdminUserName());
            NodeRef one = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
            NodeRef two = nodeService.createNode(one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
            NodeRef three = nodeService.createNode(two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), ContentModel.TYPE_FOLDER).getChildRef();
            NodeRef four = nodeService.createNode(three, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), ContentModel.TYPE_FOLDER).getChildRef();
            permissionService.setPermission(one, "andy", PermissionService.ALL_PERMISSIONS, true);
            permissionService.setPermission(two, "bob", PermissionService.ALL_PERMISSIONS, true);
            permissionService.setPermission(three, "carol", PermissionService.ALL_PERMISSIONS, true);

            Set<AccessPermission> set = permissionService.getAllSetPermissions(one);
            assertEquals(1, set.size());
            for (AccessPermission ap : set)
            {
                if (ap.getAuthority().equals("andy"))
                {
                    assertFalse(ap.isInherited());
                    assertEquals(0, ap.getPosition());
                }
            }

            set = permissionService.getAllSetPermissions(two);
            assertEquals(1, set.size());
            for (AccessPermission ap : set)
            {
                if (ap.getAuthority().equals("bob"))
                {
                    assertFalse(ap.isInherited());
                    assertEquals(0, ap.getPosition());
                }
            }

            set = permissionService.getAllSetPermissions(three);
            assertEquals(1, set.size());
            for (AccessPermission ap : set)
            {
                if (ap.getAuthority().equals("carol"))
                {
                    assertFalse(ap.isInherited());
                    assertEquals(0, ap.getPosition());
                }
            }

            set = permissionService.getAllSetPermissions(four);
            assertEquals(0, set.size());
        }

    }

    public void test_AR_2055()
    {
        runAs(AuthenticationUtil.getAdminUserName());
        NodeRef folder = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");
        List<String> pathElements = new ArrayList<String>();
        pathElements.add("monkey");
        try
        {
            FileFolderServiceImpl.makeFolders(serviceRegistry.getFileFolderService(), folder, pathElements, ContentModel.TYPE_FOLDER);
            fail();
        }
        catch (AccessDeniedException ade)
        {

        }

        runAs(AuthenticationUtil.getAdminUserName());
        permissionService.setPermission(folder, "andy", PermissionService.ALL_PERMISSIONS, true);

        FileFolderServiceImpl.makeFolders(serviceRegistry.getFileFolderService(), folder, pathElements, ContentModel.TYPE_FOLDER);

    }

    public void testRunAsRealAndEffectiveUsersWithPriorAuthentication()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        final NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.DENIED);

        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("andy", AuthenticationUtil.getRunAsUser());

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {
            public Object doWork() throws Exception
            {
                assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.ALLOWED);

                assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                        {

                    public Object doWork() throws Exception
                    {
                        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.DENIED);

                        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                        assertEquals("lemur", AuthenticationUtil.getRunAsUser());

                        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                                {

                            public Object doWork() throws Exception
                            {
                                assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.ALLOWED);

                                assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                                        {

                                    public Object doWork() throws Exception
                                    {
                                        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.DENIED);

                                        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                                        assertEquals("andy", AuthenticationUtil.getRunAsUser());

                                        return null;
                                    }
                                        }, "andy");

                                assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                                return null;
                            }
                                }, AuthenticationUtil.getAdminUserName());

                        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                        assertEquals("lemur", AuthenticationUtil.getRunAsUser());

                        return null;
                    }
                        }, "lemur");

                assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                return null;
            }
                }, AuthenticationUtil.getAdminUserName());

        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("andy", AuthenticationUtil.getRunAsUser());

    }

    public void testNestedRunAsRealAndEffectiveUsersWithPriorAuthentication()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        final NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.DENIED);

        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("andy", AuthenticationUtil.getRunAsUser());

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {

            public Object doWork() throws Exception
            {
                assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.ALLOWED);

                assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                        {

                    public Object doWork() throws Exception
                    {
                        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.DENIED);

                        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                        assertEquals("lemur", AuthenticationUtil.getRunAsUser());

                        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                                {

                            public Object doWork() throws Exception
                            {
                                assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.ALLOWED);

                                assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                                        {

                                    public Object doWork() throws Exception
                                    {
                                        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.DENIED);

                                        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                                        assertEquals("andy", AuthenticationUtil.getRunAsUser());

                                        return null;
                                    }
                                        }, "andy");

                                assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                                return null;
                            }
                                }, AuthenticationUtil.getAdminUserName());

                        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                        assertEquals("lemur", AuthenticationUtil.getRunAsUser());

                        return null;
                    }
                        }, "lemur");

                assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                return null;
            }
                }, AuthenticationUtil.getAdminUserName());

        assertEquals("andy", AuthenticationUtil.getFullyAuthenticatedUser());
        assertEquals("andy", AuthenticationUtil.getRunAsUser());

    }

    public void testRunAsRealAndEffectiveUsersWithNoPriorAuthentication()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        final NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        AuthenticationUtil.clearCurrentSecurityContext();

        assertNull(AuthenticationUtil.getFullyAuthenticatedUser());
        assertNull(AuthenticationUtil.getRunAsUser());

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {

            public Object doWork() throws Exception
            {
                assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.ALLOWED);

                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());
                return null;
            }
                }, AuthenticationUtil.getAdminUserName());

        assertNull(AuthenticationUtil.getFullyAuthenticatedUser());
        assertNull(AuthenticationUtil.getRunAsUser());
    }

    public void testNestedRunAsRealAndEffectiveUsersWithNoPriorAuthentication()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        final NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        AuthenticationUtil.clearCurrentSecurityContext();

        assertNull(AuthenticationUtil.getFullyAuthenticatedUser());
        assertNull(AuthenticationUtil.getRunAsUser());

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                {

            public Object doWork() throws Exception
            {
                assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.ALLOWED);

                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                        {

                    public Object doWork() throws Exception
                    {
                        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.DENIED);

                        assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
                        assertEquals("lemur", AuthenticationUtil.getRunAsUser());

                        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                                {

                            public Object doWork() throws Exception
                            {
                                assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.ALLOWED);

                                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
                                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                                AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
                                        {

                                    public Object doWork() throws Exception
                                    {
                                        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.DENIED);

                                        assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
                                        assertEquals("andy", AuthenticationUtil.getRunAsUser());

                                        return null;
                                    }
                                        }, "andy");

                                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
                                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                                return null;
                            }
                                }, AuthenticationUtil.getAdminUserName());

                        assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
                        assertEquals("lemur", AuthenticationUtil.getRunAsUser());

                        return null;
                    }
                        }, "lemur");

                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getFullyAuthenticatedUser());
                assertEquals(AuthenticationUtil.getAdminUserName(), AuthenticationUtil.getRunAsUser());

                return null;
            }
                }, AuthenticationUtil.getAdminUserName());

        assertNull(AuthenticationUtil.getFullyAuthenticatedUser());
        assertNull(AuthenticationUtil.getRunAsUser());

    }

    public void testDefaultModelPermissions()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.DENIED);

        runAs(AuthenticationUtil.getAdminUserName());

        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.CONTRIBUTOR), "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.CONTRIBUTOR)) == AccessStatus.ALLOWED);

    }

    public void testSystemUserPermissions()
    {
        AuthenticationUtil.setRunAsUserSystem();
        try
        {
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.CONSUMER) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.DELETE) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.CANCEL_CHECK_OUT) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.CHECK_OUT) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.COORDINATOR) == AccessStatus.ALLOWED);
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    public void testAdminUserPermissions()
    {
        runAs(AuthenticationUtil.getAdminUserName());
        try
        {
            assertFalse(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.CONSUMER) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.DELETE) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.READ) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.ADD_CHILDREN) == AccessStatus.ALLOWED);
            assertFalse(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.CANCEL_CHECK_OUT) == AccessStatus.ALLOWED);
            assertTrue(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.CHECK_OUT) == AccessStatus.ALLOWED);
            assertFalse(serviceRegistry.getPermissionService().hasPermission(rootNodeRef, PermissionService.COORDINATOR) == AccessStatus.ALLOWED);

        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    public void testWeSetConsumerOnRootIsNotSupportedByHasPermisssionAsItIsTheWrongType()
    {
        runAs("andy");
        assertEquals(0, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.ALLOWED));
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
        assertEquals(permissionService.hasPermission(rootNodeRef, (PermissionService.CONSUMER)), AccessStatus.DENIED);
    }

    public void testEqualBarCaseAuthorities()
    {

        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));   
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "Andy", AccessStatus.ALLOWED));   
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "ANDY", AccessStatus.ALLOWED)); 
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "woof/adobe", AccessStatus.ALLOWED)); 
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "woof/Adobe", AccessStatus.ALLOWED)); 
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "woof/ADOBE", AccessStatus.ALLOWED)); 
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "Woof/Adobe", AccessStatus.ALLOWED)); 
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "WOOF/ADOBE", AccessStatus.ALLOWED)); 

        assertEquals(8, permissionService.getAllSetPermissions(n1).size());
    }


    public void testGetAllSetPermissions()
    {
        runAs("andy");
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.DELETE), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.DELETE), "GROUP_GREEN", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "GROUP_RED", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(systemNodeRef, getPermission(PermissionService.DELETE), "andy", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(systemNodeRef, getPermission(PermissionService.DELETE), "GROUP_GREEN", AccessStatus.DENIED));

        NodeRef current = systemNodeRef;
        Set<AccessPermission> setPermissions = new HashSet<AccessPermission>();
        while (current != null)
        {
            Set<AccessPermission> morePermissions = permissionService.getAllSetPermissions(current);
            for (AccessPermission toTest : morePermissions)
            {
                if (toTest.getAuthorityType() == AuthorityType.GROUP)
                {
                    boolean add = true;
                    for (AccessPermission existing : setPermissions)
                    {
                        if (add && existing.getAuthority().equals(toTest.getAuthority()) && existing.getPermission().equals(toTest.getPermission()))
                        {
                            add = false;
                        }

                    }
                    if (add)
                    {
                        setPermissions.add(toTest);
                    }
                }
            }
            if (permissionService.getInheritParentPermissions(current))
            {
                current = nodeService.getPrimaryParent(current).getParentRef();
            }
            else
            {
                current = null;
            }
        }
        assertEquals(2, setPermissions.size());

    }

    public void testPermissionCacheOnMove()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");

        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.DENIED);

        runAs(AuthenticationUtil.getAdminUserName());

        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        runAs("andy");

        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);

        runAs(AuthenticationUtil.getAdminUserName());
        nodeService.moveNode(n2, rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}oneMoved"));

        runAs("andy");

        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.DENIED);
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

    @SuppressWarnings("unused")
    private void printPermissions(NodeRef nodeRef, String path)
    {
        Long id = nodeDAO.getNodePair(nodeRef).getFirst();
        System.out.println(path + " has " + id);
        for (AccessControlEntry entry : aclDaoComponent.getAccessControlList(id).getEntries())
        {
            System.out.println("\t\t " + id + "  " + entry);
        }
        List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
        for (ChildAssociationRef child : children)
        {
            String newPath = path + "/" + child.getQName();
            printPermissions(child.getChildRef(), newPath);

        }

    }

    public void testSetNodePermissionEntry()
    {
        runAs("andy");
        ArrayList<SimplePermissionEntry> entries = new ArrayList<SimplePermissionEntry>();
        entries.add(new SimplePermissionEntry(rootNodeRef, new SimplePermissionReference(QName.createQName("A", "B"), "C"), "user-one", AccessStatus.ALLOWED));
        entries.add(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), "user-two", AccessStatus.ALLOWED));
        entries.add(new SimplePermissionEntry(rootNodeRef, new SimplePermissionReference(QName.createQName("D", "E"), "F"), permissionService.getAllAuthorities(),
                AccessStatus.ALLOWED));
        entries.add(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), permissionService.getAllAuthorities(), AccessStatus.DENIED));

        SimpleNodePermissionEntry entry = new SimpleNodePermissionEntry(rootNodeRef, false, entries);

        permissionService.setPermission(entry);

        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertFalse(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(4, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
    }

    public void testSetNodePermissionEntry2()
    {
        ArrayList<SimplePermissionEntry> entries = new ArrayList<SimplePermissionEntry>();
        entries.add(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), permissionService.getAllAuthorities(), AccessStatus.ALLOWED));

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

    public void testDoubleSetAllowDeny()
    {
        List<? extends PermissionEntry> permissionEntries = null;
        // add-remove andy-all
        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), true);
        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), false);
        permissionService.deletePermission(rootNodeRef, "andy", permissionService.getAllPermission());
        permissionEntries = permissionService.getSetPermissions(rootNodeRef).getPermissionEntries();
        assertEquals(0, permissionEntries.size());
        // add-remove andy-read
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.READ, true);
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.READ, false);
        permissionService.deletePermission(rootNodeRef, "andy", PermissionService.READ);
        permissionEntries = permissionService.getSetPermissions(rootNodeRef).getPermissionEntries();
        assertEquals(0, permissionEntries.size());
    }

    public void testSetPermissionEntryElements()
    {
        // add andy-all (allow)
        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
        for (PermissionEntry pe : permissionService.getSetPermissions(rootNodeRef).getPermissionEntries())
        {
            assertEquals("andy", pe.getAuthority());
            assertTrue(pe.isAllowed());
            assertTrue(pe.getPermissionReference().getQName().equals(permissionService.getAllPermissionReference().getQName()));
            assertTrue(pe.getPermissionReference().getName().equals(permissionService.getAllPermissionReference().getName()));
            assertEquals(rootNodeRef, pe.getNodeRef());
        }

        // add andy-all (allow)
        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // add other-all (allow)
        permissionService.setPermission(rootNodeRef, "other", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // add andy-all (deny)
        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), false);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // add andy-read (deny)
        permissionService.setPermission(rootNodeRef, "andy", PermissionService.READ, false);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(3, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // remove andy-read
        permissionService.deletePermission(rootNodeRef, "andy", PermissionService.READ);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // remove andy-all
        permissionService.deletePermission(rootNodeRef, "andy", permissionService.getAllPermission());
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // remove other-all
        permissionService.deletePermission(rootNodeRef, "other", permissionService.getAllPermission());
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(0, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
    }

    public void testSetPermissionEntry()
    {
        permissionService.setPermission(allowAndyAll);
        permissionService.setPermission(rootNodeRef, "andy", permissionService.getAllPermission(), true);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
        for (PermissionEntry pe : permissionService.getSetPermissions(rootNodeRef).getPermissionEntries())
        {
            assertEquals("andy", pe.getAuthority());
            assertTrue(pe.isAllowed());
            assertTrue(pe.getPermissionReference().getQName().equals(permissionService.getAllPermissionReference().getQName()));
            assertTrue(pe.getPermissionReference().getName().equals(permissionService.getAllPermissionReference().getName()));
            assertEquals(rootNodeRef, pe.getNodeRef());
        }

        // Set duplicate

        permissionService.setPermission(allowAndyAll);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // Set new

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), "other", AccessStatus.ALLOWED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // Deny

        permissionService.setPermission(denyAndyAll);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // new

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, new SimplePermissionReference(QName.createQName("A", "B"), "C"), "andy", AccessStatus.DENIED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(3, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, new SimplePermissionReference(QName.createQName("A", "B"), "C"), "andy", AccessStatus.DENIED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(2, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(denyAndyAll);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(1, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), "other", AccessStatus.ALLOWED));
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(0, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());

        // delete when we know there's nothing do delete
        permissionService.deletePermission(allowAndyAll);
        assertNotNull(permissionService.getSetPermissions(rootNodeRef));
        assertTrue(permissionService.getSetPermissions(rootNodeRef).inheritPermissions());
        assertEquals(rootNodeRef, permissionService.getSetPermissions(rootNodeRef).getNodeRef());
        assertEquals(0, permissionService.getSetPermissions(rootNodeRef).getPermissionEntries().size());
    }

    public void testGetSettablePermissionsForType()
    {
        Set<String> answer = permissionService.getSettablePermissions(QName.createQName("sys", "base", namespacePrefixResolver));
        assertEquals(NUM_OF_PERMISSIONS, answer.size());

        answer = permissionService.getSettablePermissions(QName.createQName("cm", "ownable", namespacePrefixResolver));
        assertEquals(0, answer.size());

        answer = permissionService.getSettablePermissions(QName.createQName("cm", "content", namespacePrefixResolver));
        assertEquals(5, answer.size());

        answer = permissionService.getSettablePermissions(QName.createQName("cm", "folder", namespacePrefixResolver));
        assertEquals(5, answer.size());

        answer = permissionService.getSettablePermissions(QName.createQName("cm", "monkey", namespacePrefixResolver));
        assertEquals(0, answer.size());
    }

    public void testGetSettablePermissionsForNode()
    {
        QName ownable = QName.createQName("cm", "ownable", namespacePrefixResolver);

        Set<String> answer = permissionService.getSettablePermissions(rootNodeRef);
        assertEquals(NUM_OF_PERMISSIONS, answer.size());

        nodeService.addAspect(rootNodeRef, ownable, null);
        answer = permissionService.getSettablePermissions(rootNodeRef);
        assertEquals(NUM_OF_PERMISSIONS, answer.size());

        nodeService.removeAspect(rootNodeRef, ownable);
        answer = permissionService.getSettablePermissions(rootNodeRef);
        assertEquals(NUM_OF_PERMISSIONS, answer.size());
    }


    public void testSimplePermissionOnStore()
    {
        runAs("andy");

        NodePermissionEntry entry = permissionService.getSetPermissions(testStoreRef);
        assertNotNull(entry);
        assertEquals(0, entry.getPermissionEntries().size());
        assertEquals(0, permissionService.getAllSetPermissions(testStoreRef).size());

        //Test Nulls
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(null, getPermission(PermissionService.READ)));
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(rootNodeRef, (PermissionReference)null));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(new NodeRef(testStoreRef, "I don't exist"), getPermission(PermissionService.READ)));
    }


    public void testSimplePermissionOnRoot()
    {
        runAs("andy");

        assertEquals(NUM_OF_PERMISSIONS, permissionService.getPermissions(rootNodeRef).size());
        assertEquals(0, countGranted(permissionService.getPermissions(rootNodeRef)));
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());

        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");

        assertEquals(NUM_OF_PERMISSIONS, permissionService.getPermissions(rootNodeRef).size());
        assertEquals(2, countGranted(permissionService.getPermissions(rootNodeRef)));

        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
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
        runAs(AuthenticationUtil.getAdminUserName());
        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"), ContentModel.TYPE_CONTENT).getChildRef();

        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CONTENT), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
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
        permissionService.setPermission(allowAndyRead);
        runAs("andy");

        assertEquals(NUM_OF_PERMISSIONS, permissionService.getPermissions(rootNodeRef).size());
        assertEquals(7, countGranted(permissionService.getPermissions(rootNodeRef)));
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());

        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(denyAndyRead);
        runAs("andy");
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(allowAndyRead);
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
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(allowAndyReadProperties);
        runAs("andy");
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        // Changed ny not enfocing READ
        // assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) ==
        // AccessStatus.ALLOWED);
        // assertFalse(permissionService.hasPermission(n1,
        // getPermission(PermissionService.READ_PROPERTIES)) ==
        // AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(allowAndyReadChildren);
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(denyAndyReadProperties);
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(allowAndyReadChildren);
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(allowAndyReadProperties);
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
    }

    public void testPermissionGroupSimpleInheritance()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();

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

        permissionService.setPermission(allowAndyRead);
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n1, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(denyAndyRead);
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

        permissionService.deletePermission(allowAndyRead);
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

        permissionService.setPermission(allowAndyReadProperties);
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.setPermission(denyAndyReadProperties);
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);

        permissionService.deletePermission(allowAndyReadProperties);
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

        permissionService.setPermission(allowAndyRead);
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(denyAndyRead);
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

        permissionService.deletePermission(allowAndyRead);
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

        permissionService.setPermission(allowAndyRead);
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(denyAndyReadProperties);
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(allowAndyReadChildren);
        runAs("andy");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(denyAndyRead);
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

    @Category(PerformanceTests.class)
    public void testPerformance() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());

        // TransactionService transactionService = serviceRegistry.getTransactionService();
        // UserTransaction tx = transactionService.getUserTransaction();
        // tx.begin();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n3 = nodeService.createNode(n2, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}three"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n4 = nodeService.createNode(n3, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}four"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n5 = nodeService.createNode(n4, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}five"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n6 = nodeService.createNode(n5, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}six"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n7 = nodeService.createNode(n6, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}seven"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n8 = nodeService.createNode(n7, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}eight"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n9 = nodeService.createNode(n8, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}nine"), ContentModel.TYPE_FOLDER).getChildRef();
        @SuppressWarnings("unused")
        NodeRef n10 = nodeService.createNode(n9, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}ten"), ContentModel.TYPE_FOLDER).getChildRef();

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        runAs("andy");
        permissionService.hasPermission(n5, PermissionService.READ);
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++)
        {
            permissionService.hasPermission(n5, PermissionService.READ);
        }
        long end = System.nanoTime();
        System.out.println("Can in " + ((end - start) / 1e9f / 10000));
        System.out.println("Can per second " + (1 / ((end - start) / 1e9f / 10000)));

    }

    public void testPerf() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());

        // TransactionService transactionService = serviceRegistry.getTransactionService();
        // UserTransaction tx = transactionService.getUserTransaction();
        // tx.begin();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n3 = nodeService.createNode(n2, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}three"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n4 = nodeService.createNode(n3, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}four"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n5 = nodeService.createNode(n4, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}five"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n6 = nodeService.createNode(n5, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}six"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n7 = nodeService.createNode(n6, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}seven"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n8 = nodeService.createNode(n7, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}eight"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n9 = nodeService.createNode(n8, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}nine"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n10 = nodeService.createNode(n9, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}ten"), ContentModel.TYPE_FOLDER).getChildRef();

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        // permissionService.setPermission(new SimplePermissionEntry(n9,
        // getPermission(PermissionService.READ),
        // "andy", AccessStatus.ALLOWED));
        // permissionService.setPermission(new SimplePermissionEntry(n10,
        // getPermission(PermissionService.READ),
        // "andy", AccessStatus.ALLOWED));

        long start;
        long end;
        long time = 0;
        //        for (int i = 0; i < 1000; i++)
        //        {
        //            getSession().flush();
        //            // getSession().clear();
        //            start = System.nanoTime();
        //            assertTrue(permissionService.hasPermission(n10, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        //            end = System.nanoTime();
        //            time += (end - start);
        //        }
        //        System.out.println("Time is " + (time / 1000000000.0));
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

        // tx.rollback();
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

        permissionService.setPermission(allowAndyAll);
        assertEquals(1, permissionService.getAllSetPermissions(rootNodeRef).size());
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.ALL_PERMISSIONS)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, PermissionServiceImpl.OLD_ALL_PERMISSIONS_REFERENCE) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(denyAndyRead);
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

        permissionService.setPermission(denyAndyAll);
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
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

        permissionService.setPermission(denyAndyRead);
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

        permissionService.setPermission(denyAndyAll);
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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), ROLE_AUTHENTICATED, AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), ROLE_AUTHENTICATED, AccessStatus.DENIED));
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

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), ROLE_AUTHENTICATED, AccessStatus.ALLOWED));
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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), permissionService.getAllAuthorities(), AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), permissionService.getAllAuthorities(), AccessStatus.DENIED));
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

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), permissionService.getAllAuthorities(),
                AccessStatus.ALLOWED));
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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), permissionService.getAllAuthorities(),
                AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.WRITE)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), permissionService.getAllAuthorities(), AccessStatus.DENIED));
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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, permissionService.getAllPermissionReference(), permissionService.getAllAuthorities(),
                AccessStatus.DENIED));
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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), ROLE_AUTHENTICATED, AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.DENIED));
        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.DENIED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.DENIED);
        runAs("lemur");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testGroupAndUserInteractionAnyAllowAllows()
    {

        permissionServiceImpl.setAnyDenyDenies(false);
        try
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

            permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
            runAs("andy");
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
            runAs("lemur");
            assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

            permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), ROLE_AUTHENTICATED, AccessStatus.ALLOWED));
            runAs("andy");
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
            runAs("lemur");
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

            permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.DENIED));
            runAs("andy");
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
            runAs("lemur");
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

            permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
            runAs("andy");
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
            runAs("lemur");
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

            permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.DENIED));
            runAs("andy");
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
            runAs("lemur");
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
            assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        }
        finally
        {
            permissionServiceImpl.setAnyDenyDenies(true);
        }
    }

    public void testInheritPermissions()
    {
        runAs(AuthenticationUtil.getAdminUserName());
        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();

        Long aclID = nodeService.getNodeAclId(n1);
        PermissionContext context = new PermissionContext(QName.createQName("{namespace}one"));
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(aclID, context, PermissionService.ALL_PERMISSIONS));

        runAs("andy");

        //Pass null aclid
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(null, context, PermissionService.ALL_PERMISSIONS));
        context.setStoreAcl(3455l);
        //Gets further but should now fail
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(null, context, PermissionService.ALL_PERMISSIONS));

        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

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
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

    }

    public void testAncestorRequirementAndInheritance()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();

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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CONTENT), "andy", AccessStatus.ALLOWED));

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

        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.DENIED));
        permissionService.setInheritParentPermissions(n2, false);

        runAs("andy");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setInheritParentPermissions(n2, true);

        runAs("andy");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        // Changed by removing permission read parents access
        // assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) ==
        // AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(n2, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
    }

    public void testPermissionCase()
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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "Andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "ANDY", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CONTENT), "AnDy", AccessStatus.ALLOWED));

        permissionService.getAllSetPermissions(rootNodeRef);

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

        // permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
        // getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        // permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
        // getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        // permissionService.setPermission(new SimplePermissionEntry(rootNodeRef,
        // getPermission(PermissionService.READ_CONTENT), "andy", AccessStatus.ALLOWED));
        //
        //        
        // runAs("andy");
        // assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) ==
        // AccessStatus.ALLOWED);
        // assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) ==
        // AccessStatus.ALLOWED);
        // assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) ==
        // AccessStatus.ALLOWED);
        // assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) ==
        // AccessStatus.ALLOWED);
        // runAs("lemur");
        // assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) ==
        // AccessStatus.ALLOWED);
        // assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) ==
        // AccessStatus.ALLOWED);
        // assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) ==
        // AccessStatus.ALLOWED);
        // assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) ==
        // AccessStatus.ALLOWED);

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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CONTENT), "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

    }

    public void testContentPermissions()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(n1, ContentModel.ASSOC_CONTAINS, QName.createQName("{namespace}two"), ContentModel.TYPE_CONTENT).getChildRef();

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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));

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

        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CONTENT), "andy", AccessStatus.ALLOWED));

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

        // printPermissions(rootNodeRef, "/");

        permissionService.deletePermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.deletePermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));
        permissionService.deletePermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CONTENT), "andy", AccessStatus.ALLOWED));

        // printPermissions(rootNodeRef, "/");

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

        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.FULL_CONTROL), "andy", AccessStatus.ALLOWED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
        runAs("lemur");
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertFalse(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.FULL_CONTROL), "andy", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES), "andy", AccessStatus.ALLOWED));

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

        permissionService.deletePermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.FULL_CONTROL), "andy", AccessStatus.DENIED));

        runAs("andy");
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_PROPERTIES)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CHILDREN)) == AccessStatus.ALLOWED);
        assertTrue(permissionService.hasPermission(rootNodeRef, getPermission(PermissionService.READ_CONTENT)) == AccessStatus.ALLOWED);
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

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));

        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.DELETE), "andy", AccessStatus.ALLOWED));

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

        permissionService.setPermission(new SimplePermissionEntry(systemNodeRef, getPermission(PermissionService.DELETE), "andy", AccessStatus.DENIED));

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
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "lemur", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "lemur", AccessStatus.ALLOWED));
        assertEquals(4, permissionService.getAllSetPermissions(rootNodeRef).size());

        permissionService.clearPermission(rootNodeRef, "andy");
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.clearPermission(rootNodeRef, "lemur");
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());

    }

    @Category(PerformanceTests.class)
    public void xtestAclInsertionPerformanceShared()
    {
        NodeRef parent = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        for(int i = 0; i < 10000; i++)
        {
            nodeService.createNode(parent, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}child"+i), ContentModel.TYPE_FOLDER).getChildRef();
        }
        long start = System.nanoTime();
        permissionService.setPermission(new SimplePermissionEntry(parent, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.ALLOWED));
        long end = System.nanoTime();

        assertTrue("Time was "+(end - start)/1000000000.0f, end == start);
    }

    @Category(PerformanceTests.class)
    public void xtestAclInsertionPerformanceDefining()
    {
        NodeRef parent = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        for(int i = 0; i < 10000; i++)
        {
            NodeRef created = nodeService.createNode(parent, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}child"+i), ContentModel.TYPE_FOLDER).getChildRef();
            permissionService.setPermission(new SimplePermissionEntry(created, getPermission(PermissionService.CONSUMER), "bob", AccessStatus.ALLOWED));
        }
        long start = System.nanoTime();
        permissionService.setPermission(new SimplePermissionEntry(parent, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.ALLOWED));
        long end = System.nanoTime();

        assertTrue("Time was "+(end - start)/1000000000.0f, end == start);
    }

    @Category(PerformanceTests.class)
    public void xtestAclInsertionPerformanceMixed()
    {
        NodeRef parent = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        for(int i = 0; i < 10000; i++)
        {
            NodeRef created = nodeService.createNode(parent, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}child"+i), ContentModel.TYPE_FOLDER).getChildRef();
            if(i % 2 == 0)
            {
                permissionService.setPermission(new SimplePermissionEntry(created, getPermission(PermissionService.CONSUMER), "bob", AccessStatus.ALLOWED));
            }
        }
        long start = System.nanoTime();
        permissionService.setPermission(new SimplePermissionEntry(parent, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.ALLOWED));
        long end = System.nanoTime();

        assertTrue("Time was "+(end - start)/1000000000.0f, end == start);
    }

    public void testClearPermissionNullAuthority()
    {
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        assertEquals(2, permissionService.getAllSetPermissions(rootNodeRef).size());
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ), "lemur", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(rootNodeRef, getPermission(PermissionService.READ_CHILDREN), "lemur", AccessStatus.ALLOWED));
        assertEquals(4, permissionService.getAllSetPermissions(rootNodeRef).size());

        permissionService.clearPermission(rootNodeRef, null);
        assertEquals(0, permissionService.getAllSetPermissions(rootNodeRef).size());
    }

    public void testGetAllSetPermissionsFromAllNodes()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n4 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}four"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n5 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}five"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n6 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}six"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n7 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}seven"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n8 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}eight"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n9 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}nine"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n10 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}ten"), ContentModel.TYPE_FOLDER).getChildRef();

        // assertEquals(0, permissionService.getAllSetPermissionsForCurrentUser().size());
        // assertEquals(0, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).size());
        // assertEquals(0, permissionService.getAllSetPermissionsForAuthority("andy").size());

        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ_CONTENT), AuthenticationUtil.getAdminUserName(), AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n3, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n4, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n5, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n6, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n7, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n8, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n9, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n10, getPermission(PermissionService.READ_CHILDREN), AuthenticationUtil.getAdminUserName(), AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n10, getPermission(PermissionService.READ_CHILDREN), "andy", AccessStatus.ALLOWED));

        // assertEquals(10, permissionService.getAllSetPermissionsForCurrentUser().size());
        // assertEquals(10, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).size());
        // assertEquals(2, permissionService.getAllSetPermissionsForAuthority("andy").size());
        // assertNull(permissionService.getAllSetPermissionsForCurrentUser().get(rootNodeRef));
        // assertNull(permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(rootNodeRef));
        // assertNull(permissionService.getAllSetPermissionsForAuthority("andy").get(rootNodeRef));
        // assertEquals(2, permissionService.getAllSetPermissionsForCurrentUser().get(n1).size());
        // assertEquals(2, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n1).size());
        // assertNull(permissionService.getAllSetPermissionsForAuthority("andy").get(n1));
        // assertEquals(1, permissionService.getAllSetPermissionsForCurrentUser().get(n2).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n2).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority("andy").get(n2).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForCurrentUser().get(n3).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n3).size());
        // assertNull(permissionService.getAllSetPermissionsForAuthority("andy").get(n3));
        // assertEquals(1, permissionService.getAllSetPermissionsForCurrentUser().get(n4).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n4).size());
        // assertNull(permissionService.getAllSetPermissionsForAuthority("andy").get(n4));
        // assertEquals(1, permissionService.getAllSetPermissionsForCurrentUser().get(n5).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n5).size());
        // assertNull(permissionService.getAllSetPermissionsForAuthority("andy").get(n5));
        // assertEquals(1, permissionService.getAllSetPermissionsForCurrentUser().get(n6).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n6).size());
        // assertNull(permissionService.getAllSetPermissionsForAuthority("andy").get(n6));
        // assertEquals(1, permissionService.getAllSetPermissionsForCurrentUser().get(n7).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n7).size());
        // assertNull(permissionService.getAllSetPermissionsForAuthority("andy").get(n7));
        // assertEquals(1, permissionService.getAllSetPermissionsForCurrentUser().get(n8).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n8).size());
        // assertNull(permissionService.getAllSetPermissionsForAuthority("andy").get(n8));
        // assertEquals(1, permissionService.getAllSetPermissionsForCurrentUser().get(n9).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n9).size());
        // assertNull(permissionService.getAllSetPermissionsForAuthority("andy").get(n9));
        // assertEquals(1, permissionService.getAllSetPermissionsForCurrentUser().get(n10).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority(AuthenticationUtil.getAdminUserName()).get(n10).size());
        // assertEquals(1, permissionService.getAllSetPermissionsForAuthority("andy").get(n10).size());

    }

    @Category(PerformanceTests.class)
    public void test_DefiningShared_AclUpdatePerformance()
    {
        runAs("admin");
        NodeRef one = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(one, "andy", PermissionService.READ, true);
        NodeRef two = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(two, "andy", PermissionService.WRITE, true);
        NodeRef test = nodeService.createNode(one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"), ContentModel.TYPE_FOLDER).getChildRef();

        // test has shared acl


        long start = System.nanoTime();
        for(int i = 0; i < 1000; i++)
        {
            nodeService.moveNode(test, two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
            nodeService.moveNode(test, one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        }  
        long end = System.nanoTime();

        //assertTrue("Time was "+(end - start)/1000000000.0f, end == start);
    }


    @Category(PerformanceTests.class)
    public void test_DefiningDefining_AclUpdatePerformance()
    {
        runAs("admin");
        NodeRef one = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(one, "andy", PermissionService.READ, true);
        permissionService.setPermission(one, "bob", PermissionService.READ, true);
        permissionService.setPermission(one, "cat", PermissionService.READ, true);
        permissionService.setPermission(one, "dog", PermissionService.READ, true);
        permissionService.setPermission(one, "emu", PermissionService.READ, true);
        permissionService.setPermission(one, "fox", PermissionService.READ, true);
        NodeRef two = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(two, "andy", PermissionService.WRITE, true);
        NodeRef test = nodeService.createNode(one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"), ContentModel.TYPE_FOLDER).getChildRef();
        permissionService.setPermission(test, "andy", PermissionService.CHANGE_PERMISSIONS, true);

        // test has shared acl


        long start = System.nanoTime();
        for(int i = 0; i < 1000; i++)
        {
            nodeService.moveNode(test, two, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
            nodeService.moveNode(test, one, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}test"));
        }  
        long end = System.nanoTime();

        //assertTrue("Time was "+(end - start)/1000000000.0f, end == start);

    }

    @Category(PerformanceTests.class)
    public void testAclInsertionPerformanceShared()
    {
        NodeRef parent = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        for(int i = 0; i < 1000; i++)
        {
            nodeService.createNode(parent, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}child"+i), ContentModel.TYPE_FOLDER).getChildRef();
        }
        long start = System.nanoTime();
        permissionService.setPermission(new SimplePermissionEntry(parent, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.ALLOWED));
        long end = System.nanoTime();

        //assertTrue("Time was "+(end - start)/1000000000.0f, end == start);
    }

    @Category(PerformanceTests.class)
    public void testAclInsertionPerformanceDefining()
    {
        NodeRef parent = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        for(int i = 0; i < 1000; i++)
        {
            NodeRef created = nodeService.createNode(parent, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}child"+i), ContentModel.TYPE_FOLDER).getChildRef();
            permissionService.setPermission(new SimplePermissionEntry(created, getPermission(PermissionService.CONSUMER), "bob", AccessStatus.ALLOWED));
        }
        long start = System.nanoTime();
        permissionService.setPermission(new SimplePermissionEntry(parent, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.ALLOWED));
        long end = System.nanoTime();

        //assertTrue("Time was "+(end - start)/1000000000.0f, end == start);
    }

    @Category(PerformanceTests.class)
    public void testAclInsertionPerformanceMixed()
    {
        NodeRef parent = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        for(int i = 0; i < 1000; i++)
        {
            NodeRef created = nodeService.createNode(parent, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}child"+i), ContentModel.TYPE_FOLDER).getChildRef();
            if(i % 2 == 0)
            {
                permissionService.setPermission(new SimplePermissionEntry(created, getPermission(PermissionService.CONSUMER), "bob", AccessStatus.ALLOWED));
            }
        }
        long start = System.nanoTime();
        permissionService.setPermission(new SimplePermissionEntry(parent, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.ALLOWED));
        long end = System.nanoTime();

        //assertTrue("Time was "+(end - start)/1000000000.0f, end == start);
    }
    
    public void testPreserveAuditableData()
    {
        ADMAccessControlListDAO accessControlListDao = (ADMAccessControlListDAO) applicationContext.getBean("admNodeACLDAO");
        boolean preserveAuditableData = accessControlListDao.isPreserveAuditableData();
        
        runAs("admin");
        
        personService.getPerson("andy");
        personService.getPerson("userTwo");
        
        NodeRef folder = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}Folder"), ContentModel.TYPE_FOLDER).getChildRef();;
        NodeRef content1 = nodeService.createNode(folder, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}content1"), ContentModel.TYPE_CONTENT).getChildRef();;
        NodeRef content2 = nodeService.createNode(folder, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}content2"), ContentModel.TYPE_CONTENT).getChildRef();;
        
        try
        {
            permissionService.setPermission(folder, "andy", PermissionService.COORDINATOR, true);
            
            assertEquals("admin", nodeService.getProperty(content1, ContentModel.PROP_MODIFIER));
        
            accessControlListDao.setPreserveAuditableData(true);
        
            runAs("andy");
            permissionService.setPermission(content1, "userTwo", PermissionService.COORDINATOR, true);
            assertEquals("admin", nodeService.getProperty(content1, ContentModel.PROP_MODIFIER));
        
            accessControlListDao.setPreserveAuditableData(false);
        
            permissionService.setPermission(content2, "userTwo", PermissionService.COORDINATOR, true);
            assertEquals("andy", nodeService.getProperty(content2, ContentModel.PROP_MODIFIER));
        }
        finally
        {
            accessControlListDao.setPreserveAuditableData(preserveAuditableData);
            if (folder != null)
            {
                nodeService.deleteNode(folder);
            }
        }
    }


    public void xtestFindNodesByPermission()
    {
        runAs(AuthenticationUtil.getAdminUserName());

        // StoreRef storeRef = rootNodeRef.getStoreRef();

        NodeRef n1 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}one"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n2 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}two"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n3 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}three"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n4 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}four"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n5 = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}five"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n6 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}six"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n7 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}seven"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n8 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}eight"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n9 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}nine"), ContentModel.TYPE_FOLDER).getChildRef();
        NodeRef n10 = nodeService.createNode(n1, ContentModel.ASSOC_CHILDREN, QName.createQName("{namespace}ten"), ContentModel.TYPE_FOLDER).getChildRef();

        personService.getPerson("andy");
        String groupAuth = authorityService.createAuthority(AuthorityType.GROUP, "G");
        authorityService.addAuthority(groupAuth, "andy");

        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser("Consumer",
        // true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser("Consumer",
        // false, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(), "Consumer", true,
        // false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(), "Consumer", false,
        // false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy", "Consumer", true,
        // false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy", "Consumer", false,
        // false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth, "Consumer", true,
        // false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth, "Consumer", false,
        // false, false), storeRef).size());

        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.CONSUMER), AuthenticationUtil.getAdminUserName(), AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n1, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n6, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n7, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n8, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n9, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n9, getPermission(PermissionService.CONSUMER), groupAuth, AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n10, getPermission(PermissionService.CONSUMER), groupAuth, AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n10, getPermission(PermissionService.CONSUMER), "andy", AccessStatus.DENIED));
        permissionService.setPermission(new SimplePermissionEntry(n2, getPermission(PermissionService.CONTRIBUTOR), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n3, getPermission(PermissionService.READ), "andy", AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n3, getPermission(PermissionService.READ_CONTENT), groupAuth, AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n4, getPermission(PermissionService.READ_CHILDREN), groupAuth, AccessStatus.ALLOWED));
        permissionService.setPermission(new SimplePermissionEntry(n5, getPermission(PermissionService.READ_CONTENT), groupAuth, AccessStatus.ALLOWED));

        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONSUMER,
        // true, false, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONSUMER,
        // false, false, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONSUMER, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONSUMER, false, false, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONSUMER, true, false, false), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONSUMER, false, false, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONSUMER, true, false, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONSUMER, false, false, false), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONTRIBUTOR,
        // true, false, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONTRIBUTOR,
        // false, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONTRIBUTOR, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONTRIBUTOR, false, false, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONTRIBUTOR, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONTRIBUTOR, false, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONTRIBUTOR, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONTRIBUTOR, false, false, false), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ, true,
        // false, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ, false,
        // false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ, false, false, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ, false, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ, false, false, false), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CONTENT,
        // true, false, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CONTENT,
        // false, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CONTENT, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CONTENT, false, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CONTENT, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CONTENT, false, false, false), storeRef).size());
        // assertEquals(2, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CONTENT, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CONTENT, false, false, false), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CHILDREN,
        // true, false, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CHILDREN,
        // false, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CHILDREN, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CHILDREN, false, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CHILDREN, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CHILDREN, false, false, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CHILDREN, true, false, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CHILDREN, false, false, false), storeRef).size());

        // Include groups for exact match

        // for (NodeRef nodeRef :
        // permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONSUMER, true, true, false))
        // {
        // System.out.println("Found " + nodeService.getPath(nodeRef));
        // }

        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONSUMER,
        // true, true, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONSUMER,
        // false, true, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONSUMER, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONSUMER, false, true, false), storeRef).size());
        // assertEquals(2, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONSUMER, true, true, false), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONSUMER, false, true, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONSUMER, true, true, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONSUMER, false, true, false), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONTRIBUTOR,
        // true, true, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONTRIBUTOR,
        // false, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONTRIBUTOR, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONTRIBUTOR, false, true, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONTRIBUTOR, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONTRIBUTOR, false, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONTRIBUTOR, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONTRIBUTOR, false, true, false), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ, true,
        // true, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ, false,
        // true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ, false, true, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ, false, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ, false, true, false), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CONTENT,
        // true, true, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CONTENT,
        // false, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CONTENT, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CONTENT, false, true, false), storeRef).size());
        // assertEquals(2, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CONTENT, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CONTENT, false, true, false), storeRef).size());
        // assertEquals(2, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CONTENT, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CONTENT, false, true, false), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CHILDREN,
        // true, true, false), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CHILDREN,
        // false, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CHILDREN, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CHILDREN, false, true, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CHILDREN, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CHILDREN, false, true, false), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CHILDREN, true, true, false), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CHILDREN, false, true, false), storeRef).size());

        // Include inexact permission

        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONSUMER,
        // true, false, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONSUMER,
        // false, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONSUMER, true, false, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONSUMER, false, false, true), storeRef).size());
        // assertEquals(2, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONSUMER, true, false, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONSUMER, false, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONSUMER, true, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONSUMER, false, false, true), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONTRIBUTOR,
        // true, false, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONTRIBUTOR,
        // false, false, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONTRIBUTOR, true, false, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONTRIBUTOR, false, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONTRIBUTOR, true, false, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONTRIBUTOR, false, false, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONTRIBUTOR, true, false, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONTRIBUTOR, false, false, true), storeRef).size());
        //
        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ, true,
        // false, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ, false,
        // false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ, true, false, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ, false, false, true), storeRef).size());
        // assertEquals(3, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ, true, false, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ, false, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ, true, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ, false, false, true), storeRef).size());
        //
        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CONTENT,
        // true, false, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CONTENT,
        // false, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CONTENT, true, false, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CONTENT, false, false, true), storeRef).size());
        // assertEquals(3, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CONTENT, true, false, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CONTENT, false, false, true), storeRef).size());
        // assertEquals(3, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CONTENT, true, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CONTENT, false, false, true), storeRef).size());
        //
        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CHILDREN,
        // true, false, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CHILDREN,
        // false, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CHILDREN, true, false, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CHILDREN, false, false, true), storeRef).size());
        // assertEquals(3, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CHILDREN, true, false, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CHILDREN, false, false, true), storeRef).size());
        // assertEquals(2, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CHILDREN, true, false, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CHILDREN, false, false, true), storeRef).size());

        // Inexact for all

        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONSUMER,
        // true, true, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONSUMER,
        // false, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONSUMER, true, true, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONSUMER, false, true, true), storeRef).size());
        // assertEquals(3, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONSUMER, true, true, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONSUMER, false, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONSUMER, true, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONSUMER, false, true, true), storeRef).size());
        //
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONTRIBUTOR,
        // true, true, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.CONTRIBUTOR,
        // false, true, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONTRIBUTOR, true, true, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.CONTRIBUTOR, false, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONTRIBUTOR, true, true, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.CONTRIBUTOR, false, true, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONTRIBUTOR, true, true, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.CONTRIBUTOR, false, true, true), storeRef).size());
        //
        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ, true,
        // true, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ, false,
        // true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ, true, true, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ, false, true, true), storeRef).size());
        // assertEquals(4, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ, true, true, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ, false, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ, true, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ, false, true, true), storeRef).size());
        //
        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CONTENT,
        // true, true, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CONTENT,
        // false, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CONTENT, true, true, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CONTENT, false, true, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CONTENT, true, true, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CONTENT, false, true, true), storeRef).size());
        // assertEquals(3, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CONTENT, true, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CONTENT, false, true, true), storeRef).size());
        //
        // assertEquals(1,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CHILDREN,
        // true, true, true), storeRef).size());
        // assertEquals(0,
        // filterForStore(permissionService.findNodesByAssignedPermissionForCurrentUser(PermissionService.READ_CHILDREN,
        // false, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CHILDREN, true, true, true), storeRef).size());
        // assertEquals(0, filterForStore(permissionService.findNodesByAssignedPermission(AuthenticationUtil.getAdminUserName(),
        // PermissionService.READ_CHILDREN, false, true, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CHILDREN, true, true, true), storeRef).size());
        // assertEquals(5, filterForStore(permissionService.findNodesByAssignedPermission("andy",
        // PermissionService.READ_CHILDREN, false, true, true), storeRef).size());
        // assertEquals(2, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CHILDREN, true, true, true), storeRef).size());
        // assertEquals(1, filterForStore(permissionService.findNodesByAssignedPermission(groupAuth,
        // PermissionService.READ_CHILDREN, false, true, true), storeRef).size());

    }

    // private Set<NodeRef> filterForStore(Set<NodeRef> set, StoreRef storeRef)
    // {
    // Set<NodeRef> toRemove = new HashSet<NodeRef>();
    // for (NodeRef node : set)
    // {
    // if (!node.getStoreRef().equals(storeRef))
    // {
    // toRemove.add(node);
    // }
    // }
    // set.removeAll(toRemove);
    // return set;
    // }

    // TODO: Test permissions on missing nodes

    /**
     * Test that the PermissionServicePolicies are invoked whenever a local permission is granted/revoked, or permission inheritance is enabled/disabled
     */
    public void testPermissionPolicies()
    {
        //test that OnGrantLocalPermission is invoked when a local permission is granted
        OnGrantLocalPermission onGrantLocalPermission = createClassPolicy(OnGrantLocalPermission.class, OnGrantLocalPermission.QNAME, ContentModel.TYPE_BASE);

        permissionService.setPermission(rootNodeRef, USER1_ANDY, PermissionService.CONTRIBUTOR, true);

        verify(onGrantLocalPermission).onGrantLocalPermission(rootNodeRef, USER1_ANDY, PermissionService.CONTRIBUTOR);

        
        //test that OnRevokeLocalPermission is invoked when a local permission is revoked
        OnRevokeLocalPermission onRevokeLocalPermission = createClassPolicy(OnRevokeLocalPermission.class, OnRevokeLocalPermission.QNAME, ContentModel.TYPE_BASE);

        permissionService.setPermission(rootNodeRef, USER1_ANDY, PermissionService.CONTRIBUTOR, false);

        verify(onRevokeLocalPermission).onRevokeLocalPermission(rootNodeRef, USER1_ANDY, PermissionService.CONTRIBUTOR);
        
        
        //test that OnRevokeLocalPermission is invoked when a local permission is deleted
        onRevokeLocalPermission = createClassPolicy(OnRevokeLocalPermission.class, OnRevokeLocalPermission.QNAME, ContentModel.TYPE_BASE);
        
        permissionService.deletePermission(rootNodeRef, USER1_ANDY, PermissionService.CONTRIBUTOR);

        verify(onRevokeLocalPermission).onRevokeLocalPermission(rootNodeRef, USER1_ANDY, PermissionService.CONTRIBUTOR);
        
        //test that OnRevokeLocalPermission is invoked when all local permissions are revoked
        onRevokeLocalPermission = createClassPolicy(OnRevokeLocalPermission.class, OnRevokeLocalPermission.QNAME, ContentModel.TYPE_BASE);
        
        permissionService.deletePermissions(rootNodeRef);

        verify(onRevokeLocalPermission).onRevokeLocalPermission(rootNodeRef, null, null);
        
        //test that OnInheritPermissionsEnabled is invoked when permission inheritance is disabled
        OnInheritPermissionsDisabled onInheritPermissionsDisabled = createClassPolicy(OnInheritPermissionsDisabled.class, OnInheritPermissionsDisabled.QNAME, ContentModel.TYPE_BASE);
        
        permissionService.setInheritParentPermissions(rootNodeRef, false);

        verify(onInheritPermissionsDisabled).onInheritPermissionsDisabled(rootNodeRef, false);
        
        //test that OnInheritPermissionsEnabled is invoked when permission inheritance is disabled with async=false
        onInheritPermissionsDisabled = createClassPolicy(OnInheritPermissionsDisabled.class, OnInheritPermissionsDisabled.QNAME, ContentModel.TYPE_BASE);
        
        permissionService.setInheritParentPermissions(rootNodeRef, false, false);

        verify(onInheritPermissionsDisabled).onInheritPermissionsDisabled(rootNodeRef, false);
        
        //test that OnInheritPermissionsEnabled is invoked when permission inheritance is disabled with async=true
        onInheritPermissionsDisabled = createClassPolicy(OnInheritPermissionsDisabled.class, OnInheritPermissionsDisabled.QNAME, ContentModel.TYPE_BASE);
        
        permissionService.setInheritParentPermissions(rootNodeRef, false, true);

        verify(onInheritPermissionsDisabled).onInheritPermissionsDisabled(rootNodeRef, false);
        
        //test that OnInheritPermissionsEnabled is invoked when permission inheritance is enabled
        OnInheritPermissionsEnabled onInheritPermissionsEnabled = createClassPolicy(OnInheritPermissionsEnabled.class, OnInheritPermissionsEnabled.QNAME, ContentModel.TYPE_BASE);
        
        permissionService.setInheritParentPermissions(rootNodeRef, true);

        verify(onInheritPermissionsEnabled).onInheritPermissionsEnabled(rootNodeRef);
    }
    
    /**
     * Test that the revoke permission works on every cases.
     */
    public void testDeletePermissions() {

            runAs(AuthenticationUtil.getAdminUserName());

            // test that OnRevokeLocalPermission is invoked when a local permission
            // is revoked
            OnRevokeLocalPermission onRevokeLocalPermission = createClassPolicy(OnRevokeLocalPermission.class,
                            OnRevokeLocalPermission.QNAME, ContentModel.TYPE_BASE);

            SiteInfo siteInfo = siteService.createSite("TEST_SITE", "TEST_SITE", "TEST_SITE", "TEST_SITE",
                            SiteVisibility.PUBLIC);
            permissionService.setPermission(siteInfo.getNodeRef(), USER1_ANDY, PermissionService.CONTRIBUTOR, true);
            permissionService.setPermission(siteInfo.getNodeRef(), USER1_ANDY, PermissionService.WRITE, true);
            permissionService.setPermission(siteInfo.getNodeRef(), USER1_ANDY, PermissionService.CHANGE_PERMISSIONS, true);

            //case1 delete a specific permission
            permissionService.deletePermission(siteInfo.getNodeRef(), USER1_ANDY, PermissionService.CONTRIBUTOR);
            verify(onRevokeLocalPermission).onRevokeLocalPermission(siteInfo.getNodeRef(), USER1_ANDY,
                            PermissionService.CONTRIBUTOR);

            //case2 delete all permissions for an authority
            permissionService.deletePermission(siteInfo.getNodeRef(), USER1_ANDY, null);
            verify(onRevokeLocalPermission).onRevokeLocalPermission(siteInfo.getNodeRef(), USER1_ANDY, null);

            permissionService.setPermission(siteInfo.getNodeRef(), USER1_ANDY, PermissionService.CONTRIBUTOR, true);
            permissionService.setPermission(siteInfo.getNodeRef(), USER3_PAUL, PermissionService.CONTRIBUTOR, true);
           
            //case3 entries for all authorities that have a specific permission
            permissionService.deletePermission(siteInfo.getNodeRef(), null, PermissionService.CONTRIBUTOR);
            verify(onRevokeLocalPermission).onRevokeLocalPermission(siteInfo.getNodeRef(), null,
                            PermissionService.CONTRIBUTOR);

            permissionService.setPermission(siteInfo.getNodeRef(), USER1_ANDY, PermissionService.CONTRIBUTOR, true);
            permissionService.setPermission(siteInfo.getNodeRef(), USER1_ANDY, PermissionService.WRITE, true);
            permissionService.setPermission(siteInfo.getNodeRef(), USER1_ANDY, PermissionService.CHANGE_PERMISSIONS, true);
            permissionService.setPermission(siteInfo.getNodeRef(), USER3_PAUL, PermissionService.CONTRIBUTOR, true);
           
            //case4 delete all permissions set for the node
            permissionService.deletePermission(siteInfo.getNodeRef(), null, null);
            verify(onRevokeLocalPermission).onRevokeLocalPermission(siteInfo.getNodeRef(), null, null);

    }
    
    private <T extends Policy> T createClassPolicy(Class<T> policyInterface, QName policyQName, QName triggerOnClass)
    {
        T policy = mock(policyInterface);
        policyComponent.bindClassBehaviour(
                    policyQName, 
                    triggerOnClass, 
                    new JavaBehaviour(policy, policyQName.getLocalName()));
        return policy;
    }    
}