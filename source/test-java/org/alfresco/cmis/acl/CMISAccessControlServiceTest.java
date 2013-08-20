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
package org.alfresco.cmis.acl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.cmis.CMISAccessControlEntriesGroupedByPrincipalId;
import org.alfresco.cmis.CMISAccessControlEntry;
import org.alfresco.cmis.CMISAccessControlReport;
import org.alfresco.cmis.CMISAccessControlService;
import org.alfresco.cmis.CMISAclCapabilityEnum;
import org.alfresco.cmis.CMISAclPropagationEnum;
import org.alfresco.cmis.CMISConstraintException;
import org.alfresco.cmis.CMISPermissionDefinition;
import org.alfresco.cmis.CMISPermissionMapping;
import org.alfresco.cmis.acl.CMISAccessControlServiceImpl.AccessPermissionComparator;
import org.alfresco.cmis.mapping.BaseCMISTest;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISAccessControlFormatEnum;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public class CMISAccessControlServiceTest extends BaseCMISTest
{
    private NodeRef parent;

    private NodeRef child;

    private NodeRef grandParent;

    public void testAclPropagationMode()
    {
        assertEquals(CMISAclPropagationEnum.PROPAGATE, cmisAccessControlService.getAclPropagation());
    }

    public void testAclCapability()
    {
        assertEquals(CMISAclCapabilityEnum.MANAGE, cmisAccessControlService.getAclCapability());
    }

    public void testPermissions()
    {
        List<CMISPermissionDefinition> allPermissions = cmisAccessControlService.getRepositoryPermissions();
        assertEquals(69, allPermissions.size());
        HashSet<CMISPermissionDefinition> permissionSet = new HashSet<CMISPermissionDefinition>();
        permissionSet.addAll(allPermissions);
        assertEquals(69, permissionSet.size());

    }

    public void testAclReportingCmisPermissionsOnly() throws Exception
    {
        createTestAcls();

        CMISAccessControlReport grandParentReport = cmisAccessControlService.getAcl(grandParent, CMISAccessControlFormatEnum.CMIS_BASIC_PERMISSIONS);
        assertFalse(grandParentReport.isExact());
        assertEquals(10, grandParentReport.getAccessControlEntries().size());
        assertTrue(checkCounts(grandParentReport, PermissionService.ADMINISTRATOR_AUTHORITY, 1, 2));
        assertTrue(checkCounts(grandParentReport, PermissionService.ALL_AUTHORITIES, 1, 0));
        assertTrue(checkCounts(grandParentReport, "ToMask", 1, 0));
        assertTrue(checkCounts(grandParentReport, "Full", 0, 3));
        assertTrue(checkCounts(grandParentReport, "Reader", 1, 0));
        assertTrue(checkCounts(grandParentReport, "Writer", 1, 0));
        assertTrue(checkAbsent(grandParentReport, "SplitRead"));
        assertTrue(checkAbsent(grandParentReport, "SplitWrite"));
        assertTrue(checkAbsent(grandParentReport, "DuplicateRead"));
        assertTrue(checkAbsent(grandParentReport, "Writer2"));
        assertTrue(checkAbsent(grandParentReport, "Multi"));

        CMISAccessControlReport parentReport = cmisAccessControlService.getAcl(parent, CMISAccessControlFormatEnum.CMIS_BASIC_PERMISSIONS);
        assertFalse(parentReport.isExact());
        assertEquals(10, parentReport.getAccessControlEntries().size());
        assertTrue(checkCounts(parentReport, PermissionService.ADMINISTRATOR_AUTHORITY, 0, 3));
        assertTrue(checkCounts(parentReport, PermissionService.ALL_AUTHORITIES, 0, 1));
        assertTrue(checkAbsent(parentReport, "ToMask"));
        assertTrue(checkCounts(parentReport, "Full", 0, 3));
        assertTrue(checkCounts(parentReport, "Reader", 0, 1));
        assertTrue(checkCounts(parentReport, "Writer", 0, 1));
        assertTrue(checkAbsent(parentReport, "SplitRead"));
        assertTrue(checkAbsent(parentReport, "SplitWrite"));
        assertTrue(checkCounts(parentReport, "DuplicateRead", 1, 0));
        assertTrue(checkAbsent(parentReport, "Writer2"));
        assertTrue(checkAbsent(parentReport, "Multi"));

        CMISAccessControlReport childReport = cmisAccessControlService.getAcl(child, CMISAccessControlFormatEnum.CMIS_BASIC_PERMISSIONS);
        assertFalse(childReport.isExact());
        assertEquals(13, childReport.getAccessControlEntries().size());
        assertTrue(checkCounts(childReport, PermissionService.ADMINISTRATOR_AUTHORITY, 0, 3));
        assertTrue(checkCounts(childReport, PermissionService.ALL_AUTHORITIES, 0, 1));
        assertTrue(checkAbsent(childReport, "ToMask"));
        assertTrue(checkCounts(childReport, "Full", 0, 3));
        assertTrue(checkCounts(childReport, "Reader", 0, 1));
        assertTrue(checkCounts(childReport, "Writer", 0, 1));
        assertTrue(checkAbsent(childReport, "SplitRead"));
        assertTrue(checkAbsent(childReport, "SplitWrite"));
        assertTrue(checkCounts(childReport, "DuplicateRead", 1, 0));
        assertTrue(checkCounts(childReport, "Writer2", 1, 0));
        assertTrue(checkCounts(childReport, "Multi", 2, 0));
    }

    private Set<String> getAllPermissions()
    {
        HashSet<String> answer = new HashSet<String>();
        PermissionReference allPermission = permissionModelDao.getPermissionReference(null, PermissionService.ALL_PERMISSIONS);
        Set<PermissionReference> all = permissionModelDao.getAllPermissions();
        for (PermissionReference pr : all)
        {
            answer.add(pr.toString());
        }
        // Add All
        answer.add(allPermission.toString());
        // Add CMIS permissions
        answer.add(CMISAccessControlService.CMIS_ALL_PERMISSION);
        answer.add(CMISAccessControlService.CMIS_READ_PERMISSION);
        answer.add(CMISAccessControlService.CMIS_WRITE_PERMISSION);
        return answer;
    }

    private boolean checkCounts(CMISAccessControlReport report, String key, int direct, int indirect) throws Exception
    {
        // check all permissions are valid

        Set<String> permissionNames = getAllPermissions();

        for (CMISAccessControlEntry entry : report.getAccessControlEntries())
        {
            if (!permissionNames.contains(entry.getPermission()))
            {
                return false;
            }
        }

        // check counts

        for (CMISAccessControlEntriesGroupedByPrincipalId group : report.getAccessControlEntriesGroupedByPrincipalId())
        {
            if (group.getPrincipalId().equals(key))
            {
                if (group.getDirectPermissions().size() != direct)
                {
                    return false;
                }
                if (group.getIndirectPermissions().size() != indirect)
                {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean checkAbsent(CMISAccessControlReport report, String key) throws Exception
    {
        for (CMISAccessControlEntriesGroupedByPrincipalId group : report.getAccessControlEntriesGroupedByPrincipalId())
        {
            if (group.getPrincipalId().equals(key))
            {
                return false;
            }
        }
        return true;
    }

    public void testAclReportingAllPermissions() throws Exception
    {
        createTestAcls();

        CMISAccessControlReport grandParentReport = cmisAccessControlService.getAcl(grandParent, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(grandParentReport.isExact());
        assertEquals(17, grandParentReport.getAccessControlEntries().size());
        assertTrue(checkCounts(grandParentReport, PermissionService.ADMINISTRATOR_AUTHORITY, 1, 3));
        assertTrue(checkCounts(grandParentReport, PermissionService.ALL_AUTHORITIES, 1, 1));
        assertTrue(checkCounts(grandParentReport, "ToMask", 1, 1));
        assertTrue(checkCounts(grandParentReport, "Full", 1, 3));
        assertTrue(checkCounts(grandParentReport, "Reader", 1, 1));
        assertTrue(checkCounts(grandParentReport, "Writer", 1, 1));
        assertTrue(checkAbsent(grandParentReport, "SplitRead"));
        assertTrue(checkAbsent(grandParentReport, "SplitWrite"));
        assertTrue(checkAbsent(grandParentReport, "DuplicateRead"));
        assertTrue(checkAbsent(grandParentReport, "Writer2"));
        assertTrue(checkCounts(grandParentReport, "Multi", 1, 0));

        CMISAccessControlReport parentReport = cmisAccessControlService.getAcl(parent, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(parentReport.isExact());
        assertEquals(20, parentReport.getAccessControlEntries().size());
        assertTrue(checkCounts(parentReport, PermissionService.ADMINISTRATOR_AUTHORITY, 0, 4));
        assertTrue(checkCounts(parentReport, PermissionService.ALL_AUTHORITIES, 0, 2));
        assertTrue(checkAbsent(parentReport, "ToMask"));
        assertTrue(checkCounts(parentReport, "Full", 0, 4));
        assertTrue(checkCounts(parentReport, "Reader", 0, 2));
        assertTrue(checkCounts(parentReport, "Writer", 0, 2));
        assertTrue(checkCounts(parentReport, "SplitRead", 1, 0));
        assertTrue(checkCounts(parentReport, "SplitWrite", 1, 0));
        assertTrue(checkCounts(parentReport, "DuplicateRead", 1, 1));
        assertTrue(checkAbsent(grandParentReport, "Writer2"));
        assertTrue(checkCounts(parentReport, "Multi", 1, 1));

        CMISAccessControlReport childReport = cmisAccessControlService.getAcl(child, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(childReport.isExact());
        assertEquals(29, childReport.getAccessControlEntries().size());
        assertTrue(checkCounts(childReport, PermissionService.ADMINISTRATOR_AUTHORITY, 0, 4));
        assertTrue(checkCounts(childReport, PermissionService.ALL_AUTHORITIES, 0, 2));
        assertTrue(checkAbsent(childReport, "ToMask"));
        assertTrue(checkCounts(childReport, "Full", 0, 4));
        assertTrue(checkCounts(childReport, "Reader", 0, 2));
        assertTrue(checkCounts(childReport, "Writer", 0, 2));
        assertTrue(checkCounts(childReport, "SplitRead", 1, 1));
        assertTrue(checkCounts(childReport, "SplitWrite", 1, 1));
        assertTrue(checkCounts(childReport, "DuplicateRead", 1, 1));
        assertTrue(checkCounts(childReport, "Writer2", 1, 1));
        assertTrue(checkCounts(childReport, "Multi", 3, 4));
        
    }

    private void createTestAcls()
    {
        grandParent = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Parent", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(grandParent, ContentModel.PROP_NAME, "GrandParent");
        parent = nodeService.createNode(grandParent, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Child", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(parent, ContentModel.PROP_NAME, "Parent");
        child = nodeService.createNode(parent, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Child", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(child, ContentModel.PROP_NAME, "Child");
        permissionService.setPermission(grandParent, PermissionService.ADMINISTRATOR_AUTHORITY, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setPermission(grandParent, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
        permissionService.setPermission(grandParent, "ToMask", PermissionService.READ, true);
        permissionService.setPermission(grandParent, "Full", PermissionService.FULL_CONTROL, true);
        permissionService.setPermission(grandParent, "Writer", PermissionService.WRITE, true);
        permissionService.setPermission(grandParent, "Reader", PermissionService.READ, true);
        permissionService.setPermission(grandParent, "Multi", PermissionService.DELETE, true);

        permissionService.setPermission(parent, "ToMask", PermissionService.READ, false);
        permissionService.setPermission(parent, "SplitRead", PermissionService.READ_PROPERTIES, true);
        permissionService.setPermission(parent, "SplitWrite", PermissionService.WRITE_CONTENT, true);
        permissionService.setPermission(parent, "DuplicateRead", PermissionService.READ, true);
        permissionService.setPermission(parent, "Multi", PermissionService.CREATE_CHILDREN, true);

        permissionService.setPermission(child, "SplitRead", PermissionService.READ_CONTENT, true);
        permissionService.setPermission(child, "Writer2", PermissionService.WRITE, true);
        permissionService.setPermission(child, "SplitWrite", PermissionService.WRITE_PROPERTIES, true);
        permissionService.setPermission(child, "DuplicateRead", PermissionService.READ, true);
        permissionService.setPermission(child, "Multi", PermissionService.READ, true);
        permissionService.setPermission(child, "Multi", PermissionService.WRITE, true);
        permissionService.setPermission(child, "Multi", PermissionService.SET_OWNER, true);

    }

    public void testAccessEntryOrdering()
    {
        createTestAcls();

        Set<CMISPermissionDefinition> permDefs = new HashSet<CMISPermissionDefinition>();
        permDefs.addAll(cmisAccessControlService.getRepositoryPermissions());

        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(child);
        ArrayList<AccessPermission> ordered = new ArrayList<AccessPermission>();
        AccessPermissionComparator comparator = new AccessPermissionComparator();
        for (AccessPermission current : permissions)
        {
            int index = Collections.binarySearch(ordered, current, comparator);
            if (index < 0)
            {
                ordered.add(-index - 1, current);
            }
        }
        int i = 0;
        assertEquals(4, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("Full", ordered.get(i).getAuthority());

        i++;
        assertEquals(4, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals(PermissionService.ALL_AUTHORITIES, ordered.get(i).getAuthority());

        i++;
        assertEquals(4, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("Multi", ordered.get(i).getAuthority());

        i++;
        assertEquals(4, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals(PermissionService.ADMINISTRATOR_AUTHORITY, ordered.get(i).getAuthority());

        i++;
        assertEquals(4, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("Reader", ordered.get(i).getAuthority());

        i++;
        assertEquals(4, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("ToMask", ordered.get(i).getAuthority());

        i++;
        assertEquals(4, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("Writer", ordered.get(i).getAuthority());

        i++;
        assertEquals(2, ordered.get(i).getPosition());
        assertEquals(AccessStatus.DENIED, ordered.get(i).getAccessStatus());
        assertEquals("ToMask", ordered.get(i).getAuthority());

        i++;
        assertEquals(2, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("DuplicateRead", ordered.get(i).getAuthority());

        i++;
        assertEquals(2, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("Multi", ordered.get(i).getAuthority());

        i++;
        assertEquals(2, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("SplitRead", ordered.get(i).getAuthority());

        i++;
        assertEquals(2, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("SplitWrite", ordered.get(i).getAuthority());

        i++;
        assertEquals(0, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("DuplicateRead", ordered.get(i).getAuthority());

        i++;
        assertEquals(0, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("Multi", ordered.get(i).getAuthority());

        i++;
        assertEquals(0, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("Multi", ordered.get(i).getAuthority());

        i++;
        assertEquals(0, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("Multi", ordered.get(i).getAuthority());

        i++;
        assertEquals(0, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("SplitRead", ordered.get(i).getAuthority());

        i++;
        assertEquals(0, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("SplitWrite", ordered.get(i).getAuthority());

        i++;
        assertEquals(0, ordered.get(i).getPosition());
        assertEquals(AccessStatus.ALLOWED, ordered.get(i).getAccessStatus());
        assertEquals("Writer2", ordered.get(i).getAuthority());
    }

    public void testApplyAcl() throws Exception
    {
        grandParent = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Parent", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(grandParent, ContentModel.PROP_NAME, "GrandParent");
        parent = nodeService.createNode(grandParent, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Child", namespaceService), ContentModel.TYPE_FOLDER).getChildRef();
        nodeService.setProperty(parent, ContentModel.PROP_NAME, "Parent");
        child = nodeService.createNode(parent, ContentModel.ASSOC_CHILDREN, QName.createQName("cm", "Child", namespaceService), ContentModel.TYPE_CONTENT).getChildRef();
        nodeService.setProperty(child, ContentModel.PROP_NAME, "Child");

        List<CMISAccessControlEntry> acesToAdd = new ArrayList<CMISAccessControlEntry>();
        acesToAdd.add(new CMISAccessControlEntryImpl(PermissionService.ADMINISTRATOR_AUTHORITY, PermissionService.ALL_PERMISSIONS));
        acesToAdd.add(new CMISAccessControlEntryImpl(PermissionService.ALL_AUTHORITIES, PermissionService.READ));
        acesToAdd.add(new CMISAccessControlEntryImpl("ToMask", PermissionService.READ));
        acesToAdd.add(new CMISAccessControlEntryImpl("Full", PermissionService.FULL_CONTROL));
        acesToAdd.add(new CMISAccessControlEntryImpl("Writer", PermissionService.WRITE));
        acesToAdd.add(new CMISAccessControlEntryImpl("Reader", PermissionService.READ));

        CMISAccessControlReport grandParentReport = cmisAccessControlService.applyAcl(grandParent, null, acesToAdd, CMISAclPropagationEnum.PROPAGATE,
                CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(grandParentReport.isExact());
        assertEquals(16, grandParentReport.getAccessControlEntries().size());

        List<CMISAccessControlEntry> acesToRemove = new ArrayList<CMISAccessControlEntry>();
        acesToRemove.add(new CMISAccessControlEntryImpl("ToMask", PermissionService.READ));

        grandParentReport = cmisAccessControlService.applyAcl(grandParent, acesToRemove, null, CMISAclPropagationEnum.PROPAGATE,
                CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(grandParentReport.isExact());
        assertEquals(14, grandParentReport.getAccessControlEntries().size());

        try
        {
            grandParentReport = cmisAccessControlService.applyAcl(grandParent, acesToRemove, null, CMISAclPropagationEnum.PROPAGATE,
                    CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
            fail("A non existent ACE should not be removable");
        }
        catch (CMISConstraintException e)
        {

        }
        acesToAdd = new ArrayList<CMISAccessControlEntry>();
        acesToAdd.add(new CMISAccessControlEntryImpl("SplitRead", permissionModelDao.getPermissionReference(null, PermissionService.READ_PROPERTIES).toString()));
        acesToAdd.add(new CMISAccessControlEntryImpl("SplitWrite", permissionModelDao.getPermissionReference(null, PermissionService.WRITE_CONTENT).toString()));
        acesToAdd.add(new CMISAccessControlEntryImpl("DuplicateRead", permissionModelDao.getPermissionReference(null, PermissionService.READ).toString()));

        CMISAccessControlReport parentReport = cmisAccessControlService.applyAcl(parent, null, acesToAdd, CMISAclPropagationEnum.PROPAGATE,
                CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(parentReport.isExact());
        assertEquals(18, parentReport.getAccessControlEntries().size());

        acesToAdd = new ArrayList<CMISAccessControlEntry>();
        acesToAdd.add(new CMISAccessControlEntryImpl("SplitRead", PermissionService.READ_CONTENT));
        acesToAdd.add(new CMISAccessControlEntryImpl("Writer2", PermissionService.WRITE));
        acesToAdd.add(new CMISAccessControlEntryImpl("SplitWrite", PermissionService.WRITE_PROPERTIES));
        acesToAdd.add(new CMISAccessControlEntryImpl("DuplicateRead", PermissionService.READ));

        CMISAccessControlReport childReport = cmisAccessControlService.applyAcl(child, null, acesToAdd, CMISAclPropagationEnum.PROPAGATE,
                CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(childReport.isExact());
        assertEquals(22, childReport.getAccessControlEntries().size());

        grandParentReport = cmisAccessControlService.getAcl(grandParent, CMISAccessControlFormatEnum.CMIS_BASIC_PERMISSIONS);
        assertFalse(grandParentReport.isExact());
        assertEquals(9, grandParentReport.getAccessControlEntries().size());

        parentReport = cmisAccessControlService.getAcl(parent, CMISAccessControlFormatEnum.CMIS_BASIC_PERMISSIONS);
        assertFalse(parentReport.isExact());
        assertEquals(10, parentReport.getAccessControlEntries().size());

        childReport = cmisAccessControlService.getAcl(child, CMISAccessControlFormatEnum.CMIS_BASIC_PERMISSIONS);
        assertFalse(childReport.isExact());
        assertEquals(11, childReport.getAccessControlEntries().size());

        grandParentReport = cmisAccessControlService.getAcl(grandParent, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(grandParentReport.isExact());
        assertEquals(14, grandParentReport.getAccessControlEntries().size());

        parentReport = cmisAccessControlService.getAcl(parent, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(parentReport.isExact());
        assertEquals(18, parentReport.getAccessControlEntries().size());

        childReport = cmisAccessControlService.getAcl(child, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(childReport.isExact());
        assertEquals(22, childReport.getAccessControlEntries().size());

        acesToAdd = new ArrayList<CMISAccessControlEntry>();
        acesToAdd.add(new CMISAccessControlEntryImpl("CMISReader", CMISAccessControlService.CMIS_READ_PERMISSION));
        acesToAdd.add(new CMISAccessControlEntryImpl("CMISWriter", CMISAccessControlService.CMIS_WRITE_PERMISSION));
        acesToAdd.add(new CMISAccessControlEntryImpl("CMISAll", CMISAccessControlService.CMIS_ALL_PERMISSION));

        childReport = cmisAccessControlService.applyAcl(child, null, acesToAdd, CMISAclPropagationEnum.PROPAGATE, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(childReport.isExact());
        assertEquals(30, childReport.getAccessControlEntries().size());

        childReport = cmisAccessControlService.applyAcl(child, acesToAdd, acesToAdd, CMISAclPropagationEnum.PROPAGATE, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(childReport.isExact());
        assertEquals(30, childReport.getAccessControlEntries().size());

        childReport = cmisAccessControlService.applyAcl(child, acesToAdd, null, CMISAclPropagationEnum.PROPAGATE, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        assertFalse(childReport.isExact());
        assertEquals(22, childReport.getAccessControlEntries().size());

        try
        {
            childReport = cmisAccessControlService.applyAcl(child, acesToAdd, null, CMISAclPropagationEnum.PROPAGATE, CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
            fail("A non existent ACE should not be removable");
        }
        catch (CMISConstraintException e)
        {

        }
    }

    public void testAllowableActionsAndPermissionMapping()
    {
        List<? extends CMISPermissionMapping> mappings = cmisAccessControlService.getPermissionMappings();
        assertEquals(29, mappings.size());
        assertTrue(contains(mappings, "canGetDescendents.Folder", CMISAccessControlService.CMIS_READ_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ReadChildren"));
        // "canGetFolderTree.Folder"
        assertTrue(contains(mappings, "canGetChildren.Folder", CMISAccessControlService.CMIS_READ_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ReadChildren"));
        assertTrue(contains(mappings, "canGetParents.Folder", CMISAccessControlService.CMIS_READ_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"));
        assertTrue(contains(mappings, "canGetFolderParent.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"));
        assertTrue(contains(mappings, "canCreateDocument.Folder", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"));
        assertTrue(contains(mappings, "canCreateFolder.Folder", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"));
        // "canCreateRelationship.Source"
        // "canCreateRelationship.Target"
        assertTrue(contains(mappings, "canGetProperties.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"));
        // "canGetRenditions.Object"
        assertTrue(contains(mappings, "canViewContent.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ReadContent"));
        assertTrue(contains(mappings, "canUpdateProperties.Object", CMISAccessControlService.CMIS_WRITE_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.WriteProperties"));
        assertTrue(contains(mappings, "canMove.Object", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"));
        assertTrue(contains(mappings, "canMove.Target", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"));
        // "canMoveObject.Source"
        assertTrue(contains(mappings, "canDelete.Object", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"));
        // "canDelete.Folder"
        // === SPEC BUG - should really be those below ...
        // "canDeleteObject.Object"
        // "canDeleteObject.Folder"
        assertTrue(contains(mappings, "canSetContent.Document", CMISAccessControlService.CMIS_WRITE_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.WriteContent"));
        assertTrue(contains(mappings, "canDeleteContent.Document", CMISAccessControlService.CMIS_WRITE_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.WriteContent"));
        assertTrue(contains(mappings, "canDeleteTree.Folder", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"));
        assertTrue(contains(mappings, "canAddToFolder.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"));
        assertTrue(contains(mappings, "canAddToFolder.Folder", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.CreateChildren"));
        assertTrue(contains(mappings, "canRemoveFromFolder.Object", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.DeleteNode"));
        // "canRemoveObjectFromFolder.Folder"
        assertTrue(contains(mappings, "canCheckout.Document", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/content/1.0}lockable.CheckOut"));
        assertTrue(contains(mappings, "canCancelCheckout.Document", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/content/1.0}lockable.CancelCheckOut"));
        assertTrue(contains(mappings, "canCheckin.Document", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/content/1.0}lockable.CheckIn"));
        assertTrue(contains(mappings, "canGetAllVersions.VersionSeries", CMISAccessControlService.CMIS_READ_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.Read"));
        // "canGetObjectRelationships.Object"
        assertTrue(contains(mappings, "canAddPolicy.Object", CMISAccessControlService.CMIS_WRITE_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.Write"));
        assertTrue(contains(mappings, "canAddPolicy.Policy", CMISAccessControlService.CMIS_READ_PERMISSION));
        assertTrue(contains(mappings, "canRemovePolicy.Object", CMISAccessControlService.CMIS_WRITE_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.Write"));
        assertTrue(contains(mappings, "canRemovePolicy.Policy", CMISAccessControlService.CMIS_READ_PERMISSION));
        assertTrue(contains(mappings, "canGetAppliedPolicies.Object", CMISAccessControlService.CMIS_READ_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ReadProperties"));
        assertTrue(contains(mappings, "canGetACL.Object", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ReadPermissions"));
        assertTrue(contains(mappings, "canApplyACL.Object", CMISAccessControlService.CMIS_ALL_PERMISSION, "{http://www.alfresco.org/model/system/1.0}base.ChangePermissions"));
    }

    private boolean contains(List<? extends CMISPermissionMapping> mappings, String key, String... entries)
    {
        for (CMISPermissionMapping mapping : mappings)
        {
            if (mapping.getKey().equals(key))
            {
                // check entries are all valid 
                Set<String> permissionNames = getAllPermissions();

                for (String permission : mapping.getPermissions())
                {
                    if (!permissionNames.contains(permission))
                    {
                        return false;
                    }
                }
                if (entries.length > 0)
                {
                    if (mapping.getPermissions().size() == entries.length)
                    {
                        for (String entry : entries)
                        {
                            if (!mapping.getPermissions().contains(entry))
                            {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                else
                {
                    return true;
                }
            }
        }
        return false;
    }
}
