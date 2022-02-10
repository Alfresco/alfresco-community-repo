/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authority.RMAuthority;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.extensions.webscripts.ui.common.StringUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;

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
    static final String ROOT_IPR_GROUP = "INPLACE_RECORD_MANAGEMENT";
    static final String READER_GROUP_PREFIX = ExtendedSecurityService.IPR_GROUP_PREFIX + "R";
    static final String WRITER_GROUP_PREFIX = ExtendedSecurityService.IPR_GROUP_PREFIX + "W";

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
        // run as System on bootstrap
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
                {
                    public Void execute()
                    {
                        // if the root group doesn't exist then create it
                        if (!authorityService.authorityExists(getRootIRPGroup()))
                        {
                            authorityService.createAuthority(AuthorityType.GROUP, ROOT_IPR_GROUP, ROOT_IPR_GROUP,
                                        Collections.singleton(RMAuthority.ZONE_APP_RM));
                        }
                        return null;
                    }
                };
                transactionService.getRetryingTransactionHelper().doInTransaction(callback);

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
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
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#getReaders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getReaders(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        Set<String> result = Collections.EMPTY_SET;
        Pair<String, String> iprGroups = getIPRGroups(nodeRef);
        if (iprGroups != null)
        {
            result = getAuthorities(iprGroups.getFirst());
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#getWriters(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getWriters(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        Set<String> result = Collections.EMPTY_SET;
        Pair<String, String> iprGroups = getIPRGroups(nodeRef);
        if (iprGroups != null)
        {
            result = getAuthorities(iprGroups.getSecond());
        }

        return result;
    }

    /**
     * Helper to get authorities for a given group
     *
     * @param group         group name
     * @return Set<String>  immediate authorities
     */
    private Set<String> getAuthorities(String group)
    {
        Set<String> result = new HashSet<>();
        result.addAll(authorityService.getContainedAuthorities(null, group, true));
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#set(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.util.Pair)
     */
    @Override
    public void set(NodeRef nodeRef, Pair<Set<String>, Set<String>> readersAndWriters)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        set(nodeRef, readersAndWriters.getFirst(), readersAndWriters.getSecond());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#set(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set)
     */
    @Override
    public void set(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // remove existing extended security, assuming there is any
        remove(nodeRef);

        // find groups
        Pair<String, String> iprGroups = createOrFindIPRGroups(readers, writers);

        // assign groups to correct fileplan roles
        NodeRef filePlan = filePlanService.getFilePlan(nodeRef);
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS, iprGroups.getFirst());
        filePlanRoleService.assignRoleToAuthority(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS, iprGroups.getSecond());

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
     * @return Pair<String, String>  where first is the read group and second if the write group, null if none found
     */
    private Pair<String, String> getIPRGroups(NodeRef nodeRef)
    {
        Pair<String, String> result = null;
        String iprReaderGroup = null;
        String iprWriterGroup = null;

        // get all the set permissions
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
        for (AccessPermission permission : permissions)
        {
            // look for the presence of the reader group
            if (permission.getAuthority().startsWith(GROUP_PREFIX + READER_GROUP_PREFIX))
            {
                iprReaderGroup = permission.getAuthority();
            }
            // look for the presence of the writer group
            else if (permission.getAuthority().startsWith(GROUP_PREFIX + WRITER_GROUP_PREFIX))
            {
                iprWriterGroup = permission.getAuthority();
            }
        }

        // assuming the are both present then return
        if (iprReaderGroup != null && iprWriterGroup != null)
        {
            result = new Pair<>(iprReaderGroup, iprWriterGroup);
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
     * When no match is found the groups are created.  Once created
     *
     * @param filePlan              file plan
     * @param readers               authorities with read
     * @param writers               authorities with write
     * @return Pair<String, String> where first is the full name of the read group and
     *                              second is the full name of the write group
     */
    private Pair<String, String> createOrFindIPRGroups(Set<String> readers, Set<String> writers)
    {
        return new Pair<>(
                createOrFindIPRGroup(READER_GROUP_PREFIX, readers),
                createOrFindIPRGroup(WRITER_GROUP_PREFIX, writers));
    }

    /**
     * Create or find an IPR group based on the provided prefix and authorities.
     *
     * @param groupPrefix   group prefix
     * @param authorities   authorities
     * @return String       full group name
     */
    private String createOrFindIPRGroup(String groupPrefix, Set<String> authorities)
    {
        String group = null;

        // find group or determine what the next index is if no group exists or there is a clash
        Pair<String, Integer> groupResult = findIPRGroup(groupPrefix, authorities);

        if (groupResult.getFirst() == null)
        {
            group = createIPRGroup(groupPrefix, authorities, groupResult.getSecond());
        }
        else
        {
            group = groupResult.getFirst();
        }

        return group;
    }

    /**
     * Given a group name prefix and the authorities, finds the exact match existing group.
     * <p>
     * If the group does not exist then the group returned is null and the index shows the next available
     * group index for creation.
     *
     * @param groupPrefix             group name prefix
     * @param authorities             authorities
     * @return Pair<String, Integer>  where first is the name of the found group, null if none found and second
     *                                if the next available create index
     */
    private Pair<String, Integer> findIPRGroup(String groupPrefix, Set<String> authorities)
    {
        String iprGroup = null;
        int nextGroupIndex = 0;
        boolean hasMoreItems = true;
        int pageCount = 0;

        // determine the short name prefix
        String groupShortNamePrefix = getIPRGroupPrefixShortName(groupPrefix, authorities);

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
            for (String group : results.getPage())
            {
                // if exists and matches we have found our group
                if (isIPRGroupTrueMatch(group, authorities))
                {
                    return new Pair<String, Integer>(group, nextGroupIndex);
                }
            }

            // determine if there are any more pages to inspect
            hasMoreItems = results.hasMoreItems();
            pageCount ++;
        }

        return new Pair<>(iprGroup, nextGroupIndex);
    }

    /**
     * Determines whether a group exactly matches a list of authorities.
     *
     * @param authorities           list of authorities
     * @param group                 group
     * @return
     */
    private boolean isIPRGroupTrueMatch(String group, Set<String> authorities)
    {
        //Remove GROUP_EVERYONE for proper comparison as GROUP_EVERYONE is never included in an IPR group
        Set<String> plainAuthorities = new HashSet<String>();
        if (authorities != null)
        {
            plainAuthorities.addAll(authorities);
            plainAuthorities.remove(PermissionService.ALL_AUTHORITIES);
        }
        Set<String> contained =  authorityService.getContainedAuthorities(null, group, true);
        return contained.equals(plainAuthorities);
    }

    /**
     * Get IPR group prefix short name.
     * <p>
     * 'package' scope to help testing.
     *
     * @param prefix        prefix
     * @param authorities   authorities
     * @return String       group prefix short name
     */
    /*package*/ String getIPRGroupPrefixShortName(String prefix, Set<String> authorities)
    {
        StringBuilder builder = new StringBuilder(128)
               .append(prefix)
               .append(getAuthoritySetHashCode(authorities));

        return builder.toString();
    }

    /**
     * Get IPR group short name.
     * <p>
     * Note this excludes the "GROUP_" prefix.
     * <p>
     * 'package' scope to help testing.
     *
     * @param prefix    prefix
     * @param readers   read authorities
     * @param writers   write authorities
     * @param index     group index
     * @return String   group short name
     */
    /*package*/ String getIPRGroupShortName(String prefix, Set<String> authorities, int index)
    {
        return getIPRGroupShortName(prefix, authorities, Integer.toString(index));
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
    private String getIPRGroupShortName(String prefix, Set<String> authorities, String index)
    {
        StringBuilder builder = new StringBuilder(128)
               .append(getIPRGroupPrefixShortName(prefix, authorities))
               .append(index);

        return builder.toString();
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
     * Creates a new IPR group.
     *
     * @param groupNamePrefix   group name prefix
     * @param children          child authorities
     * @param index             group index
     * @return String           full name of created group
     */
    private String createIPRGroup(String groupNamePrefix, Set<String> children, int index)
    {
        ParameterCheck.mandatory("groupNamePrefix", groupNamePrefix);

        // get the group name
        String groupShortName = getIPRGroupShortName(groupNamePrefix, children, index);

        // create group
        String group;
        try
        {
            group = authorityService.createAuthority(AuthorityType.GROUP, groupShortName, groupShortName, Collections.singleton(RMAuthority.ZONE_APP_RM));

            // add root parent
            authorityService.addAuthority(getRootIRPGroup(), group);

            // add children if provided
            if (children != null)
            {
                for (String child : children)
                {
                    if (authorityService.authorityExists(child) && !PermissionService.ALL_AUTHORITIES.equals(child))
                    {
                        authorityService.addAuthority(group, child);
                    }
                }
            }
        }
        catch(DuplicateChildNodeNameException ex)
        {
            // the group was concurrently created
            group = authorityService.getName(AuthorityType.GROUP, groupShortName);
        }

        return group;
    }

    /**
     * Assign IPR groups to a node reference with the correct permissions.
     *
     * @param iprGroups iprGroups, first read and second write
     * @param nodeRef   node reference
     */
    private void assignIPRGroupsToNode(Pair<String, String> iprGroups, NodeRef nodeRef)
    {
        permissionService.setPermission(nodeRef, iprGroups.getFirst(), RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(nodeRef, iprGroups.getSecond(), RMPermissionModel.FILING, true);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService#remove(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void remove(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        Pair<String, String> iprGroups = getIPRGroups(nodeRef);
        if (iprGroups != null)
        {
            // remove any extended security that might be present
            clearPermissions(nodeRef, iprGroups);

            // remove the readers from any renditions of the content
            if (isRecord(nodeRef))
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    clearPermissions(child, iprGroups);
                }
            }
        }
    }

    /**
     * Clear the nodes IPR permissions
     *
     * @param nodeRef   node reference
     */
    private void clearPermissions(NodeRef nodeRef, Pair<String, String> iprGroups)
    {
        // remove group permissions from node
        permissionService.clearPermission(nodeRef, iprGroups.getFirst());
        permissionService.clearPermission(nodeRef, iprGroups.getSecond());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.DeprecatedExtendedSecurityService#getExtendedReaders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override @Deprecated public Set<String> getExtendedReaders(NodeRef nodeRef)
    {
        return getReaders(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.DeprecatedExtendedSecurityService#getExtendedWriters(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override @Deprecated public Set<String> getExtendedWriters(NodeRef nodeRef)
    {
        return getWriters(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.DeprecatedExtendedSecurityService#addExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set)
     */
    @Override @Deprecated public void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        set(nodeRef, readers, writers);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.DeprecatedExtendedSecurityService#addExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set, boolean)
     */
    @Override @Deprecated public void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents)
    {
        set(nodeRef, readers, writers);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.DeprecatedExtendedSecurityService#removeAllExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override @Deprecated public void removeAllExtendedSecurity(NodeRef nodeRef)
    {
        remove(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.DeprecatedExtendedSecurityService#removeExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set)
     */
    @Override @Deprecated public void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers)
    {
        remove(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.DeprecatedExtendedSecurityService#removeExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, java.util.Set, boolean)
     */
    @Override @Deprecated public void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String>writers, boolean applyToParents)
    {
        remove(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.DeprecatedExtendedSecurityService#removeAllExtendedSecurity(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override @Deprecated public void removeAllExtendedSecurity(NodeRef nodeRef, boolean applyToParents)
    {
        remove(nodeRef);
    }
}
