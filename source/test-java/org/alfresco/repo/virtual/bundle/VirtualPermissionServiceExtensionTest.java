/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.store.VirtualStoreImpl;
import org.alfresco.repo.virtual.store.VirtualUserPermissions;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Test;

public class VirtualPermissionServiceExtensionTest extends VirtualizationIntegrationTest
{
    private PermissionServiceSPI permissionService;

    private String user1;

    private String user2;

    private NodeRef vf1Node2;

    private NodeRef virtualContent;

    private VirtualStoreImpl virtualStore;

    /** original user permissions to be restored on tear down */
    private VirtualUserPermissions savedUserPermissions;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        // we set our own virtual user permissions in order to be context xml
        // independent
        virtualStore = VirtualPermissionServiceExtensionTest.ctx.getBean("virtualStore",
                                                                         VirtualStoreImpl.class);

        permissionService = VirtualPermissionServiceExtensionTest.ctx.getBean("permissionServiceImpl",
                                                                              PermissionServiceSPI.class);

        user1 = "user1";

        user2 = "user2";

        vf1Node2 = nodeService.getChildByName(this.virtualFolder1NodeRef,
                                              ContentModel.ASSOC_CONTAINS,
                                              "Node2");

        virtualContent = createContent(vf1Node2,
                                       "virtualContent").getChildRef();

        this.permissionService.setPermission(this.virtualFolder1NodeRef,
                                             user1,
                                             PermissionService.DELETE_CHILDREN,
                                             true);

        this.permissionService.setPermission(this.virtualFolder1NodeRef,
                                             user2,
                                             PermissionService.DELETE_CHILDREN,
                                             false);

        this.permissionService.setPermission(this.virtualFolder1NodeRef,
                                             user1,
                                             PermissionService.READ_PROPERTIES,
                                             true);

        this.permissionService.setPermission(this.virtualFolder1NodeRef,
                                             user1,
                                             PermissionService.CREATE_CHILDREN,
                                             false);

        this.permissionService.setPermission(this.virtualFolder1NodeRef,
                                             user1,
                                             PermissionService.DELETE,
                                             true);

    }

    protected void setUpTestPermissions()
    {
        // we save the original permissions
        savedUserPermissions = virtualStore.getUserPermissions();

        VirtualUserPermissions testPermissions = new VirtualUserPermissions(savedUserPermissions);
        Set<String> allowVirtualNodes = new HashSet<>(savedUserPermissions.getAllowVirtualNodes());

        // we force create children on virtual nodes
        allowVirtualNodes.add(PermissionService.CREATE_CHILDREN);

        testPermissions.setAllowVirtualNodes(allowVirtualNodes);

        testPermissions.init();

        virtualStore.setUserPermissions(testPermissions);
    }

    @Override
    public void tearDown() throws Exception
    {
        if (savedUserPermissions != null)
        {
            virtualStore.setUserPermissions(savedUserPermissions);
            savedUserPermissions = null;
        }

        super.tearDown();
    }

    private AccessStatus hasPermissionAs(final NodeRef nodeRef, final String permission, String asUser)
    {
        RunAsWork<AccessStatus> hasPermissionAs = new RunAsWork<AccessStatus>()
        {

            @Override
            public AccessStatus doWork() throws Exception
            {
                return permissionService.hasPermission(nodeRef,
                                                       permission);
            }

        };
        return AuthenticationUtil.runAs(hasPermissionAs,
                                        asUser);
    }

    @Test
    public void testHasPermissionAdherence_actualPath() throws Exception
    {

        // virtual nodes should adhere to actual node permission if no filing
        // or the actual path is specified

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(this.virtualFolder1NodeRef,
                                     PermissionService.DELETE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(this.virtualFolder1NodeRef,
                                     PermissionService.DELETE_CHILDREN,
                                     user2));

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(vf1Node2,
                                     PermissionService.DELETE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(virtualContent,
                                     PermissionService.DELETE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(vf1Node2,
                                     PermissionService.DELETE_CHILDREN,
                                     user2));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(virtualContent,
                                     PermissionService.DELETE_CHILDREN,
                                     user2));

        this.permissionService.setPermission(this.virtualFolder1NodeRef,
                                             user1,
                                             PermissionService.DELETE_CHILDREN,
                                             false);

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(vf1Node2,
                                     PermissionService.DELETE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(virtualContent,
                                     PermissionService.DELETE_CHILDREN,
                                     user1));

    }

    @Test
    public void testHasPermissionAdherence_missingFolderPath() throws Exception
    {

        NodeRef virtualFolderT5 = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                          "VirtualFolderT5",
                                                          TEST_TEMPLATE_5_JSON_SYS_PATH);

        NodeRef filingFolderVirtualNodeRef = nodeService.getChildByName(virtualFolderT5,
                                                                        ContentModel.ASSOC_CONTAINS,
                                                                        "FilingFolder_filing_path");

              
      
        
        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     PermissionService.DELETE,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     asTypedPermission(PermissionService.DELETE),
                                     user1) );

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     PermissionService.CREATE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     asTypedPermission(PermissionService.CREATE_CHILDREN),
                                     user1));

    }
    
    @Test
    public void testHasPermissionAdherence_folderPath() throws Exception
    {

        // virtual nodes should adhere to node permission of the node indicated
        // by the filing path is specified -with virtual permission overriding
        // when specified

        NodeRef virtualFolderT5 = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                          "VirtualFolderT5",
                                                          TEST_TEMPLATE_5_JSON_SYS_PATH);

        NodeRef filingFolderVirtualNodeRef = nodeService.getChildByName(virtualFolderT5,
                                                                        ContentModel.ASSOC_CONTAINS,
                                                                        "FilingFolder_filing_path");

        ChildAssociationRef filingFolderChildAssoc = createFolder(rootNodeRef,
                                                                  "FilingFolder");

        NodeRef filingFolderNodeRef = filingFolderChildAssoc.getChildRef();
        
        this.permissionService.setPermission(filingFolderNodeRef,
                                             user1,
                                             PermissionService.CREATE_CHILDREN,
                                             true);
        
        this.permissionService.setPermission(filingFolderNodeRef,
                                             user2,
                                             PermissionService.CREATE_CHILDREN,
                                             false);

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderNodeRef,
                                     PermissionService.DELETE,
                                     user1));

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(filingFolderNodeRef,
                                     PermissionService.CREATE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderNodeRef,
                                     PermissionService.CREATE_CHILDREN,
                                     user2));
        
        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     PermissionService.DELETE,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     asTypedPermission(PermissionService.DELETE),
                                     user1));

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     PermissionService.CREATE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     asTypedPermission(PermissionService.CREATE_CHILDREN),
                                     user1));

        this.permissionService.setPermission(filingFolderNodeRef,
                                             user1,
                                             PermissionService.DELETE_CHILDREN,
                                             true);

        this.permissionService.setPermission(filingFolderNodeRef,
                                             user2,
                                             PermissionService.DELETE_CHILDREN,
                                             false);

        this.permissionService.setPermission(filingFolderNodeRef,
                                             user1,
                                             PermissionService.READ_PROPERTIES,
                                             true);

        this.permissionService.setPermission(filingFolderNodeRef,
                                             user1,
                                             PermissionService.CREATE_CHILDREN,
                                             false);

        this.permissionService.setPermission(filingFolderNodeRef,
                                             user1,
                                             PermissionService.DELETE,
                                             true);

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(filingFolderNodeRef,
                                     PermissionService.DELETE,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderNodeRef,
                                     PermissionService.CREATE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     PermissionService.DELETE,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     asTypedPermission(PermissionService.DELETE),
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     PermissionService.CREATE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(filingFolderVirtualNodeRef,
                                     asTypedPermission(PermissionService.CREATE_CHILDREN),
                                     user1));

    }

    @Test
    public void testHasPermission() throws Exception
    {
        setUpTestPermissions();

        // virtual permission should override actual permissions

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(this.virtualFolder1NodeRef,
                                     PermissionService.DELETE,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(this.virtualFolder1NodeRef,
                                     PermissionService.CREATE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(vf1Node2,
                                     PermissionService.DELETE,
                                     user1));

        assertEquals(AccessStatus.DENIED,
                     hasPermissionAs(vf1Node2,
                                     asTypedPermission(PermissionService.DELETE),
                                     user1));

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(vf1Node2,
                                     PermissionService.CREATE_CHILDREN,
                                     user1));

        assertEquals(AccessStatus.ALLOWED,
                     hasPermissionAs(vf1Node2,
                                     asTypedPermission(PermissionService.CREATE_CHILDREN),
                                     user1));

    }

    @Test
    public void testReadonlyNodeHasPermission() throws Exception
    {

        // virtual permission should override actual permissions
        NodeRef aVFTestTemplate2 = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                           "aVFTestTemplate2",
                                                           TEST_TEMPLATE_2_JSON_SYS_PATH);
        NodeRef vf2Node2 = nodeService.getChildByName(aVFTestTemplate2,
                                                      ContentModel.ASSOC_CONTAINS,
                                                      "Node2");

        final String[] deniedReadOnly = new String[] { PermissionService.UNLOCK, PermissionService.CANCEL_CHECK_OUT,
                    PermissionService.CHANGE_PERMISSIONS, PermissionService.CREATE_CHILDREN, PermissionService.DELETE,
                    PermissionService.WRITE, PermissionService.DELETE_NODE, PermissionService.WRITE_PROPERTIES,
                    PermissionService.WRITE_CONTENT, PermissionService.CREATE_ASSOCIATIONS };

        StringBuilder nonDeniedTrace = new StringBuilder();
        for (int i = 0; i < deniedReadOnly.length; i++)
        {
            AccessStatus accessStatus = hasPermissionAs(vf2Node2,
                                                        deniedReadOnly[i],
                                                        user1);
            if (!AccessStatus.DENIED.equals(accessStatus))
            {
                if (nonDeniedTrace.length() > 0)
                {
                    nonDeniedTrace.append(",");
                }
                nonDeniedTrace.append(deniedReadOnly[i]);
            }
        }

        assertTrue("Non-denied permissions on RO virtual nodes : " + nonDeniedTrace,
                   nonDeniedTrace.length() == 0);
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<? extends PermissionEntry>> mapPermissionsByName(List<? extends PermissionEntry> entries)
    {
        Map<String, List<? extends PermissionEntry>> nameMap = new HashMap<>();
        for (PermissionEntry permissionEntry : entries)
        {
            String name = permissionEntry.getPermissionReference().getName();
            List<PermissionEntry> permissions = (List<PermissionEntry>) nameMap.get(name);
            if (permissions == null)
            {
                permissions = new ArrayList<>();
                nameMap.put(name,
                            permissions);
            }
            permissions.add(permissionEntry);
        }

        return nameMap;
    }

    private Map<String, List<AccessPermission>> mapAccessPermissionsByName(Set<AccessPermission> accessPermissions)
    {
        Map<String, List<AccessPermission>> nameMap = new HashMap<>();
        for (AccessPermission accessPermission : accessPermissions)
        {
            String name = accessPermission.getPermission();
            List<AccessPermission> permissions = (List<AccessPermission>) nameMap.get(name);
            if (permissions == null)
            {
                permissions = new ArrayList<>();
                nameMap.put(name,
                            permissions);
            }
            permissions.add(accessPermission);
        }

        return nameMap;
    }

    /**
     * Asserts that the permission with the given name uniquely found in the
     * given permission entries list has the given access status for the given
     * authority.
     * 
     * @param permissionName
     * @param accessStatus
     * @param authority
     * @param permissionEntries
     */
    protected void assertUniquePermission(String permissionName, AccessStatus accessStatus, String authority,
                List<? extends PermissionEntry> permissionEntries)
    {
        Map<String, List<? extends PermissionEntry>> entriesByName = mapPermissionsByName((List<? extends PermissionEntry>) permissionEntries);
        assertNotNull("Not null permission " + permissionName + " expected.",
                      entriesByName.get(permissionName));
        assertEquals(1,
                     entriesByName.get(permissionName).size());

        PermissionEntry permission = entriesByName.get(permissionName).get(0);
        assertEquals(accessStatus,
                     permission.getAccessStatus());
        assertEquals(authority,
                     permission.getAuthority());
    }

    /**
     * Asserts that the permission with the given name uniquely found in the
     * given permission entries list has the given access status for the given
     * authority.
     * 
     * @param permissionName
     * @param accessStatus
     * @param authority
     * @param permissionEntries
     */
    protected void assertUniqueAccessPermission(String permissionName, AccessStatus accessStatus, String authority,
                Set<AccessPermission> accessPermissions)
    {
        Map<String, List<AccessPermission>> apByName = mapAccessPermissionsByName(accessPermissions);
        assertNotNull("Not null permission " + permissionName + " expected.",
                      apByName.get(permissionName));
        List<AccessPermission> oneAccessPermission = apByName.get(permissionName);
        assertEquals("Expected single AccessPermission but found " + oneAccessPermission,
                     1,
                     oneAccessPermission.size());

        AccessPermission permission = apByName.get(permissionName).get(0);
        assertEquals(accessStatus,
                     permission.getAccessStatus());
        assertEquals(authority,
                     permission.getAuthority());
    }

    @Test
    public void testGetAllSetPermissions() throws Exception
    {
        setUpTestPermissions();

        Set<AccessPermission> allVf1SetPermissions = permissionService.getAllSetPermissions(this.virtualFolder1NodeRef);

        AccessPermission vf1ReadProperties = mapAccessPermissionsByName(allVf1SetPermissions)
                    .get(PermissionService.READ_PROPERTIES)
                        .get(0);

        assertUniqueAccessPermission(PermissionService.DELETE,
                                     AccessStatus.ALLOWED,
                                     user1,
                                     allVf1SetPermissions);
        assertUniqueAccessPermission(PermissionService.CREATE_CHILDREN,
                                     AccessStatus.DENIED,
                                     user1,
                                     allVf1SetPermissions);

        Set<AccessPermission> allNode2SetPermissions = permissionService.getAllSetPermissions(vf1Node2);

        assertUniqueAccessPermission(PermissionService.DELETE,
                                     AccessStatus.DENIED,
                                     PermissionService.ALL_AUTHORITIES,
                                     allNode2SetPermissions);
        assertUniqueAccessPermission(PermissionService.CREATE_CHILDREN,
                                     AccessStatus.ALLOWED,
                                     PermissionService.ALL_AUTHORITIES,
                                     allNode2SetPermissions);
        // adhere to actual node
        assertUniqueAccessPermission(PermissionService.READ_PROPERTIES,
                                     vf1ReadProperties.getAccessStatus(),
                                     vf1ReadProperties.getAuthority(),
                                     allNode2SetPermissions);

    }

    @Test
    public void testGetSetPermissions() throws Exception
    {
        setUpTestPermissions();

        NodePermissionEntry vf1SetPermissions = permissionService.getSetPermissions(this.virtualFolder1NodeRef);
        assertEquals(virtualFolder1NodeRef,
                     vf1SetPermissions.getNodeRef());
        List<? extends PermissionEntry> vf1Entries = vf1SetPermissions.getPermissionEntries();
        assertUniquePermission(PermissionService.DELETE,
                               AccessStatus.ALLOWED,
                               user1,
                               vf1Entries);
        assertUniquePermission(PermissionService.CREATE_CHILDREN,
                               AccessStatus.DENIED,
                               user1,
                               vf1Entries);

        NodePermissionEntry node2SetPermissions = permissionService.getSetPermissions(vf1Node2);

        assertEquals(vf1Node2,
                     node2SetPermissions.getNodeRef());

        List<? extends PermissionEntry> node2Entries = node2SetPermissions.getPermissionEntries();
        assertUniquePermission(PermissionService.DELETE,
                               AccessStatus.DENIED,
                               PermissionService.ALL_AUTHORITIES,
                               node2Entries);
        assertUniquePermission(PermissionService.CREATE_CHILDREN,
                               AccessStatus.ALLOWED,
                               PermissionService.ALL_AUTHORITIES,
                               node2Entries);

    }

    private String asTypedPermission(String perm)
    {
        return virtualStore.getUserPermissions().getPermissionTypeQName() + "." + perm;
    }

}
