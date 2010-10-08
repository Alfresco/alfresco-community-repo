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
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.cmis.CMISAccessControlEntry;
import org.alfresco.cmis.CMISAccessControlFormatEnum;
import org.alfresco.cmis.CMISAccessControlReport;
import org.alfresco.cmis.CMISAccessControlService;
import org.alfresco.cmis.CMISAclCapabilityEnum;
import org.alfresco.cmis.CMISAclPropagationEnum;
import org.alfresco.cmis.CMISAclSupportedPermissionEnum;
import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.cmis.CMISConstraintException;
import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISPermissionDefinition;
import org.alfresco.cmis.CMISPermissionMapping;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * @author andyh
 */
public class CMISAccessControlServiceImpl implements CMISAccessControlService
{
    private CMISAclCapabilityEnum aclCapabilityEnum;
    
    private CMISAclSupportedPermissionEnum aclSupportedPermissionEnum;

    private CMISAclPropagationEnum aclPropagationEnum;

    private ModelDAO permissionModelDao;

    private PermissionService permissionService;

    private CMISMapping cmisMapping;

    private NodeService nodeService;

    private CMISDictionaryService cmisDictionaryService;

    /**
     * @param aclCapabilityEnum
     *            the aclCapabilityEnum to set
     */
    public void setAclCapabilityEnum(CMISAclCapabilityEnum aclCapabilityEnum)
    {
        this.aclCapabilityEnum = aclCapabilityEnum;
    }
    
    /**
     * Sets the acl supported permission enum.
     * 
     * @param aclSupportedPermissionEnum
     *            the aclSupportedPermissionEnum to set
     */
    public void setAclSupportedPermissionEnum(CMISAclSupportedPermissionEnum aclSupportedPermissionEnum)
    {
        this.aclSupportedPermissionEnum = aclSupportedPermissionEnum;
    }

    /**
     * @param aclPropagationEnum
     *            the aclPropagationEnum to set
     */
    public void setAclPropagationEnum(CMISAclPropagationEnum aclPropagationEnum)
    {
        this.aclPropagationEnum = aclPropagationEnum;
    }

    /**
     * @param permissionModelDao
     *            the permissionModelDao to set
     */
    public void setPermissionModelDao(ModelDAO permissionModelDao)
    {
        this.permissionModelDao = permissionModelDao;
    }

    /**
     * @param permissionService
     *            the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Sets the cmis mapping.
     * 
     * @param cmisMapping
     *            the cmis mapping
     */
    public void setCMISMapping(CMISMapping cmisMapping)
    {
        this.cmisMapping = cmisMapping;
    }

    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param cmisDictionaryService
     *            the cmisDictionaryService to set
     */
    public void setCMISDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlService#applyAcl(org.alfresco.service.cmr.repository.NodeRef,
     * java.util.List)
     */
    public CMISAccessControlReport applyAcl(NodeRef nodeRef, List<CMISAccessControlEntry> acesToApply) throws CMISConstraintException
    {
        Set<CMISAccessControlEntry> acesToAdd = new LinkedHashSet<CMISAccessControlEntry>(acesToApply);
        List<? extends CMISAccessControlEntry> acesExisting = getAcl(nodeRef,
                CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS).getAccessControlEntries();
        List<CMISAccessControlEntry> acesToRemove = new ArrayList<CMISAccessControlEntry>(acesExisting.size());
        for (CMISAccessControlEntry accessControlEntry : acesExisting)
        {
            // Only pay attention to existing direct entries
            if (accessControlEntry.getDirect() && !acesToAdd.remove(accessControlEntry))
            {
                acesToRemove.add(accessControlEntry);
            }
        }
        return applyAcl(nodeRef, acesToRemove, new ArrayList<CMISAccessControlEntry>(acesToAdd),
                CMISAclPropagationEnum.PROPAGATE,
                CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.CMISAccessControlService#applyAcl(org.alfresco.service.cmr.repository.NodeRef,
     *      java.util.List, java.util.List, org.alfresco.cmis.CMISAclPropagationEnum)
     */
    public CMISAccessControlReport applyAcl(NodeRef nodeRef, List<CMISAccessControlEntry> acesToRemove, List<CMISAccessControlEntry> acesToAdd, CMISAclPropagationEnum propagation,
            CMISAccessControlFormatEnum format) throws CMISConstraintException
    {
        if (propagation == CMISAclPropagationEnum.OBJECT_ONLY)
        {
            throw new CMISConstraintException("Unsupported ACL propagation mode: " + propagation);
        }
        // Check controllable ACL
        QName type = nodeService.getType(nodeRef);
        CMISTypeDefinition cmisType = cmisDictionaryService.findTypeForClass(type);
        if (false == cmisType.isControllableACL())
        {
            throw new CMISConstraintException("ACLs are not supported for type: " + cmisType.getDisplayName());
        }
        // TODO: Check valid permissions. We do not check this internally. Ignore for now ...

        if (acesToRemove != null)
        {
            Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
            for (CMISAccessControlEntry entry : acesToRemove)
            {
                String alfrescoPermission = cmisMapping.getSetPermission(compressPermission(entry.getPermission()));
                AccessPermission toCheck = new AccessPermissionImpl(alfrescoPermission, AccessStatus.ALLOWED, entry.getPrincipalId(), 0);
                if (false == permissions.contains(toCheck))
                {
                    throw new CMISConstraintException("No matching ACE found to delete");
                }
                permissionService.deletePermission(nodeRef, entry.getPrincipalId(), alfrescoPermission);
            }
        }
        if (acesToAdd != null)
        {
            for (CMISAccessControlEntry entry : acesToAdd)
            {
                String alfrescoPermission = cmisMapping.getSetPermission(compressPermission(entry.getPermission()));
                permissionService.setPermission(nodeRef, entry.getPrincipalId(), alfrescoPermission, true);
            }
        }
        return getAcl(nodeRef, format);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.CMISAccessControlService#getAcl(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.cmis.CMISAccessControlFormatEnum)
     */
    public CMISAccessControlReport getAcl(NodeRef nodeRef, CMISAccessControlFormatEnum format)
    {
        CMISAccessControlReportImpl merge = new CMISAccessControlReportImpl();
        // Need to compact deny to mask correctly
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
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

        for (AccessPermission entry : ordered)
        {
            if (entry.getAccessStatus() == AccessStatus.ALLOWED)
            {
                //answer.addEntry(new CMISAccessControlEntryImpl(entry.getAuthority(), expandPermission(cmisMapping.getReportedPermission(getPermission(entry.getPermission()),
                //        format)), entry.getPosition()));
                merge.addEntry(new CMISAccessControlEntryImpl(entry.getAuthority(), entry.getPermission(), entry.getPosition()));
            }
            else if (entry.getAccessStatus() == AccessStatus.DENIED)
            {
                //answer.removeEntry(new CMISAccessControlEntryImpl(entry.getAuthority(), expandPermission(cmisMapping.getReportedPermission(getPermission(entry.getPermission()),
                //        format)), entry.getPosition()));
                merge.removeEntry(new CMISAccessControlEntryImpl(entry.getAuthority(), entry.getPermission(), entry.getPosition()));
            }
        }
        
        CMISAccessControlReportImpl answer = new CMISAccessControlReportImpl();
        for(CMISAccessControlEntry entry : merge.getAccessControlEntries())
        {
            CMISAccessControlEntryImpl impl = (CMISAccessControlEntryImpl)entry;
            PermissionReference permissionReference = permissionModelDao.getPermissionReference(null, impl.getPermission());
            Set<PermissionReference> longForms = permissionModelDao.getGranteePermissions(permissionReference);
            HashSet<String> shortForms = new HashSet<String>();
            for(PermissionReference longForm : longForms)
            {
                shortForms.add(getPermission(longForm));
            }
            for(Pair<String, Boolean> toAdd : cmisMapping.getReportedPermissions(impl.getPermission(), shortForms, permissionModelDao.hasFull(permissionReference), impl.getDirect(), format))
            {
                answer.addEntry(new CMISAccessControlEntryImpl(impl.getPrincipalId(), expandPermission(toAdd.getFirst()), impl.getPosition(), toAdd.getSecond()));
            }
            
        }
        return answer;
    }

    private String getPermission(PermissionReference permissionReference)
    {
        if (permissionModelDao.isUnique(permissionReference))
        {
            return permissionReference.getName();
        }
        else
        {
            return permissionReference.toString();
        }
    }

    private String expandPermission(String permission)
    {
        if (permission.equals(CMIS_ALL_PERMISSION))
        {
            return permission;
        }
        else if (permission.equals(CMIS_READ_PERMISSION))
        {
            return permission;
        }
        else if (permission.equals(CMIS_WRITE_PERMISSION))
        {
            return permission;

        }
        else if (permission.startsWith("{"))
        {
            return permission;
        }
        else
        {
            PermissionReference permissionReference = permissionModelDao.getPermissionReference(null, permission);
            return permissionReference.toString();
        }
    }

    private String compressPermission(String permission) {
        int sepIndex;
        if (permission.equals(CMIS_ALL_PERMISSION) || permission.equals(CMIS_READ_PERMISSION)
                || permission.equals(CMIS_WRITE_PERMISSION) || !permission.startsWith("{")
                || (sepIndex = permission.lastIndexOf('.')) == -1) {
            return permission;
        }
        return permission.substring(sepIndex + 1);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.CMISAccessControlService#getAclCapability()
     */
    public CMISAclCapabilityEnum getAclCapability()
    {
        return aclCapabilityEnum;
    }

    /**
     * Set the acl capability enum.
     * 
     * @param aclCapabilityEnum
     */
    public void setAclCapability(CMISAclCapabilityEnum aclCapabilityEnum)
    {
        this.aclCapabilityEnum = aclCapabilityEnum;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlService#getSupportedPermissions()
     */
    public CMISAclSupportedPermissionEnum getSupportedPermissions()
    {
        return this.aclSupportedPermissionEnum;
    }

    /**
     * Sets the supported permissions.
     * 
     * @param aclSupportedPermissionEnum
     *            the supported permissions
     */
    public void setSupportedPermissions(CMISAclSupportedPermissionEnum aclSupportedPermissionEnum)
    {
        this.aclSupportedPermissionEnum = aclSupportedPermissionEnum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.CMISAccessControlService#getAclPropagation()
     */
    public CMISAclPropagationEnum getAclPropagation()
    {
        return aclPropagationEnum;
    }

    /**
     * Set the acl propagation enum.
     * 
     * @param aclPropagationEnum
     */
    public void setAclPropagation(CMISAclPropagationEnum aclPropagationEnum)
    {
        this.aclPropagationEnum = aclPropagationEnum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.CMISAccessControlService#getPermissionMappings()
     */
    public List<? extends CMISPermissionMapping> getPermissionMappings()
    {
        ArrayList<CMISPermissionMappingImpl> mappings = new ArrayList<CMISPermissionMappingImpl>();
        for(CMISAllowedActionEnum e : EnumSet.allOf(CMISAllowedActionEnum.class))
        {
            Map<String, List<String>> enumMappings = e.getPermissionMapping();
            for(String key : enumMappings.keySet())
            {
                List<String> list = enumMappings.get(key);
                CMISPermissionMappingImpl mapping = new CMISPermissionMappingImpl(key, list);
                mappings.add(mapping);
            }
        }
        return mappings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.CMISAccessControlService#getRepositoryPermissions()
     */
    public List<CMISPermissionDefinition> getRepositoryPermissions()
    {
        ArrayList<CMISPermissionDefinition> answer = new ArrayList<CMISPermissionDefinition>();
        PermissionReference allPermission = permissionModelDao.getPermissionReference(null, PermissionService.ALL_PERMISSIONS);
        Set<PermissionReference> all = permissionModelDao.getAllExposedPermissions();
        for (PermissionReference pr : all)
        {
            addPermissionDefinition(answer, pr);
        }
        // Add All
        addPermissionDefinition(answer, allPermission);
        // Add CMIS permissions
        answer.add(new CMISPermissionDefinitionImpl(CMIS_ALL_PERMISSION));
        answer.add(new CMISPermissionDefinitionImpl(CMIS_READ_PERMISSION));
        answer.add(new CMISPermissionDefinitionImpl(CMIS_WRITE_PERMISSION));
        return answer;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlService#getPrincipalAnonymous()
     */
    public String getPrincipalAnonymous()
    {
        return AuthenticationUtil.getGuestUserName();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.cmis.CMISAccessControlService#getPrincipalAnyone()
     */
    public String getPrincipalAnyone()
    {
        return PermissionService.ALL_AUTHORITIES;
    }

    private void addPermissionDefinition(ArrayList<CMISPermissionDefinition> list, PermissionReference pr)
    {
        CMISPermissionDefinitionImpl def = new CMISPermissionDefinitionImpl(getPermissionString(pr));
        list.add(def);
    }

    private String getPermissionString(PermissionReference pr)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(pr.getQName().toString());
        builder.append(".");
        builder.append(pr.getName());
        return builder.toString();
    }
    
    public static class AccessPermissionComparator implements Comparator<AccessPermission>
    {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(AccessPermission left, AccessPermission right)
        {
            if (left.getPosition() != right.getPosition())
            {
                return right.getPosition() - left.getPosition();
            }
            else
            {
                if (left.getAccessStatus() != right.getAccessStatus())
                {
                    return (left.getAccessStatus() == AccessStatus.DENIED) ? -1 : 1;
                }
                else
                {
                    int compare = left.getAuthority().compareTo(right.getAuthority());
                    if (compare != 0)
                    {
                        return compare;
                    }
                    else
                    {
                        return (left.getPermission().compareTo(right.getPermission()));
                    }

                }

            }
        }

    }

}
