/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.security;

import static org.alfresco.service.cmr.security.PermissionService.GROUP_PREFIX;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authority.RMAuthority;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.extensions.webscripts.ui.common.StringUtils;

import com.google.gdata.util.common.base.Pair;

/**
 * Extended security service implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ExtendedSecurityServiceImpl extends ServiceBaseImpl
                                         implements ExtendedSecurityService,
                                                    RecordsManagementModel,
                                                    ApplicationListener<ContextRefreshedEvent>
{
    /** ipr group names */
    private static final String ROOT_IPR_GROUP = "INPLACE_RECORD_MANAGEMENT";
    private static final String READER_GROUP_PREFIX = ExtendedSecurityService.IPR_GROUP_PREFIX + "R";
    private static final String WRITER_GROUP_PREFIX = ExtendedSecurityService.IPR_GROUP_PREFIX + "W";
    
    /** max page size for authority query */
    private static final int MAX_ITEMS = 50;
    
    /** File plan service */
    private FilePlanService filePlanService;

    /** File plan role service */
    private FilePlanRoleService filePlanRoleService;
    
    /** authority service */
    private AuthorityService authorityService;
    
    /** permission service */
    private PermissionService permissionService;
    
    /** transaction service */
    private TransactionService transactionService;

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }
    
    /**
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * @param transactionService    transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Application context refresh event handler
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // if the root group doesn't exist then create it
                if (!authorityService.authorityExists(getRootIRPGroup()))
                {
                    authorityService.createAuthority(AuthorityType.GROUP, ROOT_IPR_GROUP, ROOT_IPR_GROUP, Collections.singleton(RMAuthority.ZONE_APP_RM));
                }
                
                return null;
            }
        });
    }
    
    /**
     * Get root IPR group name
     */
    private String getRootIRPGroup()
    {
        return GROUP_PREFIX + ROOT_IPR_GROUP;
    }

	/**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#hasExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean hasExtendedSecurity(NodeRef nodeRef)
    {
        return (getIPRGroups(nodeRef) != null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getExtendedReaders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getExtendedReaders(NodeRef nodeRef)
    {
        Set<String> result = null;
        
        Pair<String, String> iprGroups = getIPRGroups(nodeRef);
        if (iprGroups != null)
        {
            result = authorityService.getContainedAuthorities(null, iprGroups.first, true);
            result.remove(iprGroups.second);
        }
        else
        {
            result = Collections.EMPTY_SET;
        }
        
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#getExtendedWriters(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getExtendedWriters(NodeRef nodeRef)
    {
        Set<String> result = null;
        
        Pair<String, String> iprGroups = getIPRGroups(nodeRef);
        if (iprGroups != null)
        {
            result = authorityService.getContainedAuthorities(null, iprGroups.second, true);
        }
        else
        {
            result = Collections.EMPTY_SET;
        }
        
        return result;
        
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#addExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set)
     */
    @Override
    public void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (nodeRef != null)
        {
            addExtendedSecurityImpl(nodeRef, readers, writers);

            // add to the extended security roles
            addExtendedSecurityRoles(nodeRef, readers, writers);
        }
    }

    /**
     * Add extended security implementation method
     *
     * @param nodeRef
     * @param readers
     * @param writers
     * @param applyToParents
     */
    private void addExtendedSecurityImpl(final NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        // find groups
        Pair<String, String> iprGroups = createOrFindIPRGroups(readers, writers);
        
        // assign groups to node
        assignIPRGroupsToNode(iprGroups, nodeRef);
        
        // apply the readers to any renditions of the content
        if (isRecord(nodeRef))
        {
            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                NodeRef child = assoc.getChildRef();
                assignIPRGroupsToNode(iprGroups, child);
            }
        }
    }
    
    /**
     * Get the IPR groups associated with a given node reference.
     * <p>
     * Return null if none found.
     * 
     * @param nodeRef                node reference
     * @return Pair<String, String>  where first if the read group and second if the write group, null if none found
     */
    private Pair<String, String> getIPRGroups(NodeRef nodeRef)
    {
        Pair<String, String> result = null;
        String iprReaderGroup = null;
        String iprWriterGroup = null;
        
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
        for (AccessPermission permission : permissions)
        {
            if (permission.getAuthority().startsWith(GROUP_PREFIX + READER_GROUP_PREFIX))
            {
                iprReaderGroup = permission.getAuthority();
            }   
            else if (permission.getAuthority().startsWith(GROUP_PREFIX + WRITER_GROUP_PREFIX))
            {
                iprWriterGroup = permission.getAuthority();
            }
        }
        
        if (iprReaderGroup != null && iprWriterGroup != null)
        {
            result = new Pair<String, String>(iprReaderGroup, iprWriterGroup);
        }
            
        return result;
    }
    
    /**
     * Given a set of readers and writers find or create the appropriate IPR groups.
     * <p>
     * The IPR groups are named with hashes of the authority lists in order to reduce 
     * the set of groups that require exact match.  A further index is used to handle
     * a situation where there is a hash clash, but a difference in the authority lists.
     * <p>
     * When no match is found the groups are created.
     * 
     * @param readers               authorities with read
     * @param writers               authorities with write
     * @return Pair<String, String> where first is the full name of the read group and 
     *                              second is the full name of the write group
     */
    private Pair<String, String> createOrFindIPRGroups(Set<String> readers, Set<String> writers)
    {
        Pair<String, String> result = null;
                    
        // find read group or determine what the next index is if no group exists or there is a clash            
        Pair<String, Integer> readGroupResult = findIPRGoup(READER_GROUP_PREFIX, readers, writers); 
        
        if (readGroupResult.first == null)
        {
            // create inplace record reader and writer groups
            result = createIPRGroups(readers, writers, readGroupResult.second);
        }
        else
        {
            // set result
            result = new Pair<String, String>(readGroupResult.first, 
                                              getIRPWriteGroupNameFromReadGroupName(readGroupResult.first, readers, writers));
        }
 
        return result;
    }
    
    /**
     * Give a group name prefix and the read/write authorities, finds the exact match existing read group
     * (containing the exact match write group).
     * <p>
     * If the group does not exist then the group returned is null and the index shows the next available 
     * group index for creation.
     * 
     * @param groupPrefix             group name prefix
     * @param readers                 authorities with read
     * @param writers                 authorities with write
     * @return Pair<String, Integer>  where first is the name of the found group, null if none found and second
     *                                if the next available create index
     */
    private Pair<String, Integer> findIPRGoup(String groupPrefix, Set<String> readers, Set<String> writers)
    {
        String iprGroup = null;
        int nextGroupIndex = 0;
        boolean hasMoreItems = true;
        int pageCount = 0;
        
        // determine the short name prefix
        String groupShortNamePrefix = getIPRGroupPrefixShortName(groupPrefix, readers, writers);
        
        // iterate over the authorities to find a match
        while (hasMoreItems == true)
        {        
            // get matching authorities
            PagingResults<String> results = authorityService.getAuthorities(AuthorityType.GROUP, 
                        RMAuthority.ZONE_APP_RM, 
                        groupShortNamePrefix,
                        false, 
                        false, 
                        new PagingRequest(MAX_ITEMS*pageCount, MAX_ITEMS));
            
            // record the total count
            nextGroupIndex = nextGroupIndex + results.getPage().size();
            
            // see if any of the matching groups exactly match
            for (String readGroup : results.getPage())
            {
                // get the corresponding write group name
                String writeGroup = getIRPWriteGroupNameFromReadGroupName(readGroup, readers, writers);
                
                // check for existence
                if (!authorityService.authorityExists(writeGroup))
                {
                    throw new AlfrescoRuntimeException("Missing inplace writer group for reader group " + readGroup);
                }
                  
                // if exists and matches we have found our groups
                if (isIPRGroupTrueMatch(readGroup, readers, writeGroup) &&
                    isIPRGroupTrueMatch(writeGroup, writers, null))
                {
                    iprGroup = readGroup;
                    break;
                } 
            }
            
            // determine if there are any more pages to inspect
            hasMoreItems = results.hasMoreItems();
            pageCount ++;
        }
        
        return new Pair<String, Integer>(iprGroup, nextGroupIndex);
    }
    
    /**
     * Determines whether a group exactly matches a list of authorities.
     * 
     * @param authorities           list of authorities
     * @param group                 group 
     * @param excludeAuthority      authority to exclude from comparision
     * @return
     */
    private boolean isIPRGroupTrueMatch(String group, Set<String> authorities, String excludeAuthority)
    {
        Set<String> contained = authorityService.getContainedAuthorities(null, group, true);
        if (excludeAuthority != null)
        {
            contained.remove(excludeAuthority);            
        }        
        return contained.equals(authorities);
    }
    
    /**
     * Get IPR group prefix short name.
     * 
     * @param prefix        prefix
     * @param authorities   read authorities
     * @param shortName     write authorities
     * @return String       group prefix short name
     */
    private String getIPRGroupPrefixShortName(String prefix, Set<String> readers, Set<String> writers)
    {
        StringBuilder builder = new StringBuilder(128)
               .append(prefix)
               .append(getAuthoritySetHashCode(readers))
               .append(getAuthoritySetHashCode(writers));
        
        return builder.toString();
    }
    
    /**
     * Get IPR group short name.
     * <p>
     * Note this excludes the "GROUP_" prefix.
     * 
     * @param prefix    prefix
     * @param readers   read authorities
     * @param writers   write authorities
     * @param index     group index
     * @return String   group short name
     */
    private String getIPRGroupShortName(String prefix, Set<String> readers, Set<String> writers, int index)
    {
        return getIPRGroupShortName(prefix, readers, writers, Integer.toString(index));
    }
    
    /**
     * Get IPR group short name.
     * <p>
     * Note this excludes the "GROUP_" prefix.
     * 
     * @param prefix    prefix
     * @param readers   read authorities
     * @param writers   write authorities
     * @param index     group index
     * @return String   group short name
     */
    private String getIPRGroupShortName(String prefix, Set<String> readers, Set<String> writers, String index)
    {
        StringBuilder builder = new StringBuilder(128)
               .append(prefix)
               .append(getAuthoritySetHashCode(readers))
               .append(getAuthoritySetHashCode(writers))
               .append(index);
        
        return builder.toString();
    }
    
    /**
     * Get the IPR write group name from the read group name.
     * <p>
     * Note this doesn't test for existence of the group, instead determines the name based on the index and 
     * authorities.
     * <p>
     * Note this excludes the "GROUP_" prefix
     * 
     * @param readGroupShortName    read group short name
     * @param readers               read authorities
     * @param writers               write authorities
     * @return String               write group name
     */
    private String getIRPWriteGroupNameFromReadGroupName(String readGroupnName, Set<String> readers, Set<String> writers)
    {
        String index = readGroupnName.substring(readGroupnName.length() - 1);
        return PermissionService.GROUP_PREFIX + getIPRGroupShortName(WRITER_GROUP_PREFIX, readers, writers, index);        
    }
    
    /**
     * Gets the hashcode value of a set of authorities.
     * 
     * @param authorities   set of authorities
     * @return int          hash code
     */
    private int getAuthoritySetHashCode(Set<String> authorities)
    {
        int result = 0;
        if (authorities != null && !authorities.isEmpty())
        {
            result = StringUtils.join(authorities.toArray(), "").hashCode();   
        }
        return result;
    }
    
    /**
     * Creates new IPR groups.
     * 
     * @param readers               read authorities
     * @param writers               write authorities
     * @param index                 index
     * @return Pair<String, String> where first if the full read group name and second is the full write group name
     */
    private Pair<String, String> createIPRGroups(Set<String> readers, Set<String> writers, int index)
    {
        String iprReaderGroup = createIPRGroup(getIPRGroupShortName(READER_GROUP_PREFIX, readers, writers, index), getRootIRPGroup(), readers); 
        String iprWriterGroup = createIPRGroup(getIPRGroupShortName(WRITER_GROUP_PREFIX, readers, writers, index), iprReaderGroup, writers);
        return new Pair<String, String>(iprReaderGroup, iprWriterGroup);
    }
    
    /**
     * Creates a new IPR group.
     * 
     * @param groupShortName    group short name
     * @param parent            parent group, null if none
     * @param children          child authorities
     * @return String           full name of created group
     */
    private String createIPRGroup(String groupShortName, String parent, Set<String> children)
    {
        ParameterCheck.mandatory("groupShortName", groupShortName);
        
        // create group
        String group = authorityService.createAuthority(AuthorityType.GROUP, groupShortName, groupShortName, Collections.singleton(RMAuthority.ZONE_APP_RM)); 
        
        // add parent if provided
        if (parent != null)
        {
            authorityService.addAuthority(parent, group);
        }
        
        // add children if provided
        if (children != null)
        {
            for (String child : children)
            {
                if (!PermissionService.ALL_AUTHORITIES.equals(child))
                {
                    authorityService.addAuthority(group, child);
                }
            }
        }
        
        return group;
    }
    
    /**
     * Assign IPR groups to a node reference with the appropraite permissions.
     * 
     * @param iprGroups iprGroups, first read and second write
     * @param nodeRef   node reference
     */
    private void assignIPRGroupsToNode(Pair<String, String> iprGroups, NodeRef nodeRef)
    {
        permissionService.setPermission(nodeRef, iprGroups.first, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(nodeRef, iprGroups.second, RMPermissionModel.FILING, true);
    }

    /**
     * Add authorities to extended security roles.
     * 
     * @param nodeRef   node reference
     * @param readers   read authorities
     * @param writers   write authorities
     */
    private void addExtendedSecurityRoles(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        NodeRef filePlan = filePlanService.getFilePlan(nodeRef);

        addExtendedSecurityRolesImpl(filePlan, readers, FilePlanRoleService.ROLE_EXTENDED_READERS);
        addExtendedSecurityRolesImpl(filePlan, writers, FilePlanRoleService.ROLE_EXTENDED_WRITERS);
    }

    /**
     * Add extended security roles implementation
     *
     * @param filePlan      file plan
     * @param authorities   authorities
     * @param roleName      role name
     */
    private void addExtendedSecurityRolesImpl(NodeRef filePlan, Set<String> authorities, String roleName)
    {
        if (authorities != null)
        {
            for (String authority : authorities)
            {
                if ((!authority.equals(PermissionService.ALL_AUTHORITIES) && !authority.equals(PermissionService.OWNER_AUTHORITY)))
                {
                    // add the authority to the role
                    filePlanRoleService.assignRoleToAuthority(filePlan, roleName, authority);
                }
            }
        }
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#removeAllExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeAllExtendedSecurity(NodeRef nodeRef)
    {
        if (hasExtendedSecurity(nodeRef))
        {
            removeExtendedSecurityImpl(nodeRef);

            // remove the readers from any renditions of the content
            if (isRecord(nodeRef))
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    removeExtendedSecurityImpl(child);
                }
            }
        }
    }

    /**
     * Remove all extended security from node.
     * 
     * @param nodeRef   node reference
     */
    private void removeExtendedSecurityImpl(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        
        Pair<String, String> iprGroups = getIPRGroups(nodeRef);
        if (iprGroups != null)
        {
            // remove group permissions from node
            permissionService.clearPermission(nodeRef, iprGroups.first);
            permissionService.clearPermission(nodeRef, iprGroups.second);
            
            // TODO delete the groups if they are no longer in use (easier said than done perhaps!)
        }
    }        
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#addExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set, boolean)
     */
    @Override @Deprecated public void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents)
    {
        addExtendedSecurity(nodeRef, readers, writers);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#removeExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set)
     */
    @Override @Deprecated public void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        removeAllExtendedSecurity(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#removeExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set, boolean)
     */
    @Override @Deprecated public void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String>writers, boolean applyToParents)
    {
        removeAllExtendedSecurity(nodeRef);        
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#removeAllExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override @Deprecated public void removeAllExtendedSecurity(NodeRef nodeRef, boolean applyToParents)
    {
        removeAllExtendedSecurity(nodeRef); 
    }
}
