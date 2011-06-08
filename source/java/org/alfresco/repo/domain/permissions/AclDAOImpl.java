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
package org.alfresco.repo.domain.permissions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.ACEType;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.SimpleAccessControlEntry;
import org.alfresco.repo.security.permissions.SimpleAccessControlList;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DAO to manage ACL persistence
 * 
 * Note: based on earlier AclDaoComponentImpl
 * 
 * @author Andy Hind, janv
 * @since 3.4
 */
public class AclDAOImpl implements AclDAO
{
    private static Log logger = LogFactory.getLog(AclDAOImpl.class);

    private QNameDAO qnameDAO;
    private AclCrudDAO aclCrudDAO;
    private NodeDAO nodeDAO;
    private TenantService tenantService;
    private SimpleCache<Long, AccessControlList> aclCache;
    private SimpleCache<Serializable, Set<String>> readersCache;

    private enum WriteMode
    {
        /**
         * Remove inherited ACEs after that set
         */
        TRUNCATE_INHERITED,
        /**
         * Add inherited ACEs
         */
        ADD_INHERITED,
        /**
         * The source of inherited ACEs is changing
         */
        CHANGE_INHERITED,
        /**
         * Remove all inherited ACEs
         */
        REMOVE_INHERITED,
        /**
         * Insert inherited ACEs
         */
        INSERT_INHERITED,
        /**
         * Copy ACLs and update ACEs and inheritance
         */
        COPY_UPDATE_AND_INHERIT,
        /**
         * Simple copy
         */
        COPY_ONLY, CREATE_AND_INHERIT;
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setAclCrudDAO(AclCrudDAO aclCrudDAO)
    {
        this.aclCrudDAO = aclCrudDAO;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * Set the ACL cache
     * 
     * @param aclCache
     */
    public void setAclCache(SimpleCache<Long, AccessControlList> aclCache)
    {
        this.aclCache = aclCache;
    }

    /**
     * @param readersCache the readersCache to set
     */
    public void setReadersCache(SimpleCache<Serializable, Set<String>> readersCache)
    {
        this.readersCache = readersCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long createAccessControlList()
    {
        return createAccessControlList(getDefaultProperties()).getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessControlListProperties getDefaultProperties()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setInherits(true);
        properties.setVersioned(false);
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Acl createAccessControlList(AccessControlListProperties properties)
    {
        if (properties == null)
        {
            throw new IllegalArgumentException("Properties cannot be null");
        }

        if (properties.getAclType() == null)
        {
            throw new IllegalArgumentException("ACL Type must be defined");
        }
        switch (properties.getAclType())
        {
        case OLD:
            if (properties.isVersioned() == Boolean.TRUE)
            {
                throw new IllegalArgumentException("Old acls can not be versioned");
            }
            break;
        case SHARED:
            throw new IllegalArgumentException("Can not create shared acls direct - use get inherited");
        case DEFINING:
        case LAYERED:
            break;
        case FIXED:
            if (properties.getInherits() == Boolean.TRUE)
            {
                throw new IllegalArgumentException("Fixed ACLs can not inherit");
            }
        case GLOBAL:
            if (properties.getInherits() == Boolean.TRUE)
            {
                throw new IllegalArgumentException("Fixed ACLs can not inherit");
            }
        default:
            break;
        }
        return createAccessControlList(properties, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Acl createAccessControlList(AccessControlListProperties properties, List<AccessControlEntry> aces, Long inherited)
    {
        if (properties == null)
        {
            throw new IllegalArgumentException("Properties cannot be null");
        }

        AclEntity acl = new AclEntity();
        if (properties.getAclId() != null)
        {
            acl.setAclId(properties.getAclId());
        }
        else
        {
            acl.setAclId(GUID.generate());
        }
        acl.setAclType(properties.getAclType());
        acl.setAclVersion(Long.valueOf(1l));

        switch (properties.getAclType())
        {
        case FIXED:
        case GLOBAL:
            acl.setInherits(Boolean.FALSE);
        case OLD:
        case SHARED:
        case DEFINING:
        case LAYERED:
        default:
            if (properties.getInherits() != null)
            {
                acl.setInherits(properties.getInherits());
            }
            else
            {
                acl.setInherits(Boolean.TRUE);
            }
            break;
        }
        acl.setLatest(Boolean.TRUE);

        switch (properties.getAclType())
        {
        case OLD:
            acl.setVersioned(Boolean.FALSE);
            break;
        case LAYERED:
            if (properties.isVersioned() != null)
            {
                acl.setVersioned(properties.isVersioned());
            }
            else
            {
                acl.setVersioned(Boolean.TRUE);
            }
            break;
        case FIXED:
        case GLOBAL:
        case SHARED:
        case DEFINING:
        default:
            if (properties.isVersioned() != null)
            {
                acl.setVersioned(properties.isVersioned());
            }
            else
            {
                acl.setVersioned(Boolean.FALSE);
            }
            break;
        }

        acl.setAclChangeSetId(getCurrentChangeSetId());
        acl.setRequiresVersion(false);

        Acl createdAcl = (AclEntity)aclCrudDAO.createAcl(acl);
        long created = createdAcl.getId();

        List<Ace> toAdd = new ArrayList<Ace>();
        List<AccessControlEntry> excluded = new ArrayList<AccessControlEntry>();
        List<AclChange> changes = new ArrayList<AclChange>();
        if ((aces != null) && aces.size() > 0)
        {
            for (AccessControlEntry ace : aces)
            {
                if ((ace.getPosition() != null) && (ace.getPosition() != 0))
                {
                    throw new IllegalArgumentException("Invalid position");
                }

                // Find authority
                Authority authority = aclCrudDAO.getOrCreateAuthority(ace.getAuthority());
                Permission permission = aclCrudDAO.getOrCreatePermission(ace.getPermission());

                // Find context
                if (ace.getContext() != null)
                {
                    throw new UnsupportedOperationException();
                }

                // Find ACE
                Ace entry = aclCrudDAO.getOrCreateAce(permission, authority, ace.getAceType(), ace.getAccessStatus());

                // Wire up
                // COW and remove any existing matches

                SimpleAccessControlEntry exclude = new SimpleAccessControlEntry();
                // match any access status
                exclude.setAceType(ace.getAceType());
                exclude.setAuthority(ace.getAuthority());
                exclude.setPermission(ace.getPermission());
                exclude.setPosition(0);

                toAdd.add(entry);
                excluded.add(exclude);
                // Will remove from the cache
            }
        }
        Long toInherit = null;
        if (inherited != null)
        {
            toInherit = getInheritedAccessControlList(inherited);
        }
        getWritable(created, toInherit, excluded, toAdd, toInherit, false, changes, WriteMode.CREATE_AND_INHERIT);


        return createdAcl;
    }

    private void getWritable(
            final Long id, final Long parent,
            List<? extends AccessControlEntry> exclude, List<Ace> toAdd,
            Long inheritsFrom, boolean cascade,
            List<AclChange> changes, WriteMode mode)
    {
        List<Ace> inherited = null;
        List<Integer> positions = null;

        if ((mode == WriteMode.ADD_INHERITED) || (mode == WriteMode.INSERT_INHERITED) || (mode == WriteMode.CHANGE_INHERITED) || (mode == WriteMode.CREATE_AND_INHERIT ))
        {
            inherited = new ArrayList<Ace>();
            positions = new ArrayList<Integer>();

            // get aces for acl (via acl member)
            List<AclMember> members;
            if(parent != null)
            {
                members = aclCrudDAO.getAclMembersByAcl(parent);
            }
            else
            {
                members = Collections.<AclMember>emptyList(); 
            }

            for (AclMember member : members)
            {
                Ace aceEntity = aclCrudDAO.getAce(member.getAceId());

                if ((mode == WriteMode.INSERT_INHERITED) && (member.getPos() == 0))
                {
                    inherited.add(aceEntity);
                    positions.add(member.getPos());
                }
                else
                {
                    inherited.add(aceEntity);
                    positions.add(member.getPos());
                }
            }
        }

        getWritable(id, parent, exclude, toAdd, inheritsFrom, inherited, positions, cascade, 0, changes, mode, false);
    }

    /**
     * Make a whole tree of ACLs copy on write if required Includes adding and removing ACEs which can be optimised
     * slightly for copy on write (no need to add and then remove)
     */
    private void getWritable(
            final Long id, final Long parent,
            List<? extends AccessControlEntry> exclude, List<Ace> toAdd, Long inheritsFrom,
            List<Ace> inherited, List<Integer> positions,
            boolean cascade, int depth, List<AclChange> changes, WriteMode mode, boolean requiresVersion)
    {
        AclChange current = getWritable(id, parent, exclude, toAdd, inheritsFrom, inherited, positions, depth, mode, requiresVersion);
        changes.add(current);

        boolean cascadeVersion = requiresVersion;
        if (!cascadeVersion)
        {
            cascadeVersion = !current.getBefore().equals(current.getAfter());
        }

        if (cascade)
        {
            List<Long> inheritors = aclCrudDAO.getAclsThatInheritFromAcl(id);
            for (Long nextId : inheritors)
            {
                // Check for those that inherit themselves to other nodes ...
                if (!nextId.equals(id))
                {
                    getWritable(nextId, current.getAfter(), exclude, toAdd, current.getAfter(), inherited, positions, cascade, depth + 1, changes, mode, cascadeVersion);
                }
            }
        }
    }

    /**
     * COW for an individual ACL
     * @return - an AclChange
     */
    private AclChange getWritable(
            final Long id, final Long parent,
            List<? extends AccessControlEntry> exclude, List<Ace> acesToAdd, Long inheritsFrom,
            List<Ace> inherited, List<Integer> positions, int depth, WriteMode mode, boolean requiresVersion)
    {
        AclUpdateEntity acl = aclCrudDAO.getAclForUpdate(id);
        if (!acl.isLatest())
        {
            aclCache.remove(id);
            readersCache.remove(id);
            return new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType());
        }

        List<Long> toAdd = new ArrayList<Long>(0);
        if (acesToAdd != null)
        {
            for (Ace ace : acesToAdd)
            {
                toAdd.add(ace.getId());
            }
        }

        if (!acl.isVersioned())
        {
            switch (mode)
            {
            case COPY_UPDATE_AND_INHERIT:
                removeAcesFromAcl(id, exclude, depth);
                aclCrudDAO.addAclMembersToAcl(acl.getId(), toAdd, depth);
                break;
            case CHANGE_INHERITED:
                replaceInherited(id, acl, inherited, positions, depth);
                break;
            case ADD_INHERITED:
                addInherited(acl, inherited, positions, depth);
                break;
            case TRUNCATE_INHERITED:
                truncateInherited(id, depth);
                break;
            case INSERT_INHERITED:
                insertInherited(id, acl, inherited, positions, depth);
                break;
            case REMOVE_INHERITED:
                removeInherited(id, depth);
                break;
            case CREATE_AND_INHERIT:
                aclCrudDAO.addAclMembersToAcl(acl.getId(), toAdd, depth);
                addInherited(acl, inherited, positions, depth);
            case COPY_ONLY:
            default:
                break;
            }
            if (inheritsFrom != null)
            {
                acl.setInheritsFrom(inheritsFrom);
            }
            aclCrudDAO.updateAcl(acl);
            aclCache.remove(id);
            readersCache.remove(id);
            return new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType());
        }
        else if ((acl.getAclChangeSetId() == getCurrentChangeSetId()) && (!requiresVersion) && (!acl.getRequiresVersion()))
        {
            switch (mode)
            {
            case COPY_UPDATE_AND_INHERIT:
                removeAcesFromAcl(id, exclude, depth);
                aclCrudDAO.addAclMembersToAcl(acl.getId(), toAdd, depth);
                break;
            case CHANGE_INHERITED:
                replaceInherited(id, acl, inherited, positions, depth);
                break;
            case ADD_INHERITED:
                addInherited(acl, inherited, positions, depth);
                break;
            case TRUNCATE_INHERITED:
                truncateInherited(id, depth);
                break;
            case INSERT_INHERITED:
                insertInherited(id, acl, inherited, positions, depth);
                break;
            case REMOVE_INHERITED:
                removeInherited(id, depth);
                break;
            case CREATE_AND_INHERIT:
                aclCrudDAO.addAclMembersToAcl(acl.getId(), toAdd, depth);
                addInherited(acl, inherited, positions, depth);
            case COPY_ONLY:
            default:
                break;
            }
            if (inheritsFrom != null)
            {
                acl.setInheritsFrom(inheritsFrom);
            }
            aclCrudDAO.updateAcl(acl);
            aclCache.remove(id);
            readersCache.remove(id);
            return new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType());
        }
        else
        {
            AclEntity newAcl = new AclEntity();
            newAcl.setAclChangeSetId(getCurrentChangeSetId());
            newAcl.setAclId(acl.getAclId());
            newAcl.setAclType(acl.getAclType());
            newAcl.setAclVersion(acl.getAclVersion() + 1);
            newAcl.setInheritedAcl(-1l);
            newAcl.setInherits(acl.getInherits());
            newAcl.setInheritsFrom((inheritsFrom != null) ? inheritsFrom : acl.getInheritsFrom());
            newAcl.setLatest(Boolean.TRUE);
            newAcl.setVersioned(Boolean.TRUE);
            newAcl.setRequiresVersion(Boolean.FALSE);

            AclEntity createdAcl = (AclEntity)aclCrudDAO.createAcl(newAcl);
            long created = createdAcl.getId();

            // Create new membership entries - excluding those in the given pattern

            // AcePatternMatcher excluder = new AcePatternMatcher(exclude);

            // get aces for acl (via acl member)
            List<AclMember> members = aclCrudDAO.getAclMembersByAcl(id);

            if (members.size() > 0)
            {
                List<Pair<Long,Integer>> aceIdsWithDepths = new ArrayList<Pair<Long,Integer>>(members.size());

                for (AclMember member : members)
                {
                    aceIdsWithDepths.add(new Pair<Long, Integer>(member.getAceId(), member.getPos()));
                }

                // copy acl members to new acl
                aclCrudDAO.addAclMembersToAcl(newAcl.getId(), aceIdsWithDepths);
            }

            // add new

            switch (mode)
            {
            case COPY_UPDATE_AND_INHERIT:
                // Done above
                removeAcesFromAcl(newAcl.getId(), exclude, depth);
                aclCrudDAO.addAclMembersToAcl(newAcl.getId(), toAdd, depth);
                break;
            case CHANGE_INHERITED:
                replaceInherited(newAcl.getId(), newAcl, inherited, positions, depth);
                break;
            case ADD_INHERITED:
                addInherited(newAcl, inherited, positions, depth);
                break;
            case TRUNCATE_INHERITED:
                truncateInherited(newAcl.getId(), depth);
                break;
            case INSERT_INHERITED:
                insertInherited(newAcl.getId(), newAcl, inherited, positions, depth);
                break;
            case REMOVE_INHERITED:
                removeInherited(newAcl.getId(), depth);
                break;
            case CREATE_AND_INHERIT:
                aclCrudDAO.addAclMembersToAcl(acl.getId(), toAdd, depth);
                addInherited(acl, inherited, positions, depth);
            case COPY_ONLY:
            default:
                break;
            }

            // Fix up inherited ACL if required
            if (newAcl.getAclType() == ACLType.SHARED)
            {
                if (parent != null)
                {
                    Long writableParentAcl = getWritable(parent, null, null, null, null, null, null, 0, WriteMode.COPY_ONLY, false).getAfter();
                    AclUpdateEntity parentAcl = aclCrudDAO.getAclForUpdate(writableParentAcl);
                    parentAcl.setInheritedAcl(created);
                    aclCrudDAO.updateAcl(parentAcl);
                }
            }

            // fix up old version
            acl.setLatest(Boolean.FALSE);
            acl.setRequiresVersion(Boolean.FALSE);
            aclCrudDAO.updateAcl(acl);
            aclCache.remove(id);
            readersCache.remove(id);
            return new AclChangeImpl(id, created, acl.getAclType(), newAcl.getAclType());
        }
    }

    /**
     * Helper to remove ACEs from an ACL
     */
    private void removeAcesFromAcl(final Long id, final List<? extends AccessControlEntry> exclude, final int depth)
    {
        if (exclude == null)
        {
            // cascade delete all acl members - no exclusion
            aclCrudDAO.deleteAclMembersByAcl(id);
        }
        else
        {
            AcePatternMatcher excluder = new AcePatternMatcher(exclude);

            List<Map<String, Object>> results = aclCrudDAO.getAcesAndAuthoritiesByAcl(id);
            List<Long> memberIds = new ArrayList<Long>(results.size());

            for (Map<String, Object> result : results)
            {
                Long result_aclmemId = (Long) result.get("aclmemId");

                if ((exclude != null) && excluder.matches(aclCrudDAO, result, depth))
                {
                    memberIds.add(result_aclmemId);
                }
            }

            // delete list of acl members
            aclCrudDAO.deleteAclMembers(memberIds);
        }
    }

    private void replaceInherited(Long id, Acl acl, List<Ace> inherited, List<Integer> positions, int depth)
    {
        truncateInherited(id, depth);
        addInherited(acl, inherited, positions, depth);
    }

    private void truncateInherited(final Long id, int depth)
    {
        List<AclMember> members = aclCrudDAO.getAclMembersByAcl(id);

        List<Long> membersToDelete = new ArrayList<Long>(members.size());
        for (AclMember member : members)
        {
            if (member.getPos() > depth)
            {
                membersToDelete.add(member.getId());
            }
        }

        if (membersToDelete.size() > 0)
        {
            // delete list of acl members
            aclCrudDAO.deleteAclMembers(membersToDelete);
        }
    }

    private void removeInherited(final Long id, int depth)
    {
        List<AclMemberEntity> members = aclCrudDAO.getAclMembersByAclForUpdate(id);

        List<Long> membersToDelete = new ArrayList<Long>(members.size());
        for (AclMemberEntity member : members)
        {
            if (member.getPos() == depth + 1)
            {
                membersToDelete.add(member.getId());
            }
            else if (member.getPos() > (depth + 1))
            {
                member.setPos(member.getPos() - 1);
                aclCrudDAO.updateAclMember(member);
            }
        }

        if (membersToDelete.size() > 0)
        {
            // delete list of acl members
            aclCrudDAO.deleteAclMembers(membersToDelete);
        }
    }

    private void addInherited(Acl acl, List<Ace> inherited, List<Integer> positions, int depth)
    {
        if ((inherited != null) && (inherited.size() > 0))
        {
            List<Pair<Long,Integer>> aceIdsWithDepths = new ArrayList<Pair<Long,Integer>>(inherited.size());
            for (int i = 0; i < inherited.size(); i++)
            {
                Ace add = inherited.get(i);
                Integer position = positions.get(i);
                aceIdsWithDepths.add(new Pair<Long, Integer>(add.getId(), position.intValue() + depth + 1));
            }
            aclCrudDAO.addAclMembersToAcl(acl.getId(), aceIdsWithDepths);
        }
    }

    private void insertInherited(final Long id, AclEntity acl, List<Ace> inherited, List<Integer> positions, int depth)
    {
        // get aces for acl (via acl member)
        List<AclMemberEntity> members = aclCrudDAO.getAclMembersByAclForUpdate(id);

        for (AclMemberEntity member : members)
        {
            if (member.getPos() > depth)
            {
                member.setPos(member.getPos() + 1);
                aclCrudDAO.updateAclMember(member);
            }
        }

        addInherited(acl, inherited, positions, depth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> deleteAccessControlEntries(final String authority)
    {
        List<AclChange> acls = new ArrayList<AclChange>();

        // get authority
        Authority authEntity = aclCrudDAO.getAuthority(authority);
        if (authEntity == null)
        {
            return acls;
        }

        List<Long> aces = new ArrayList<Long>();

        List<AclMember> members = aclCrudDAO.getAclMembersByAuthority(authority);

        boolean leaveAuthority = false;
        if (members.size() > 0)
        {
            List<Long> membersToDelete = new ArrayList<Long>(members.size());

            // fix up members and extract acls and aces
            for (AclMember member : members)
            {
                // Delete acl entry
                Long aclMemberId = member.getId();
                Long aclId = member.getAclId();
                Long aceId = member.getAceId();

                boolean hasAnotherTenantNodes = false;
                if (AuthenticationUtil.isMtEnabled())
                {
                    // ALF-3563

                    // Retrieve dependent nodes
                    List<Long> nodeIds = aclCrudDAO.getADMNodesByAcl(aclId, -1);
                    nodeIds.addAll(aclCrudDAO.getAVMNodesByAcl(aclId, -1));

                    if (nodeIds.size() > 0)
                    {
                        for (Long nodeId : nodeIds)
                        {
                            Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeId);
                            if (nodePair == null)
                            {
                                logger.warn("Node does not exist: " + nodeId);
                            }
                            NodeRef nodeRef = nodePair.getSecond();

                            try
                            {
                                // Throws AlfrescoRuntimeException in case of domain mismatch
                                tenantService.checkDomain(nodeRef.getStoreRef().getIdentifier());
                            }
                            catch (AlfrescoRuntimeException e)
                            {
                                hasAnotherTenantNodes = true;
                                leaveAuthority = true;
                                break;
                            }
                        }
                    }
                }

                if (!hasAnotherTenantNodes)
                {
                    aclCache.remove(aclId);
                    readersCache.remove(aclId);

                    Acl list = aclCrudDAO.getAcl(aclId);
                    acls.add(new AclChangeImpl(aclId, aclId, list.getAclType(), list.getAclType()));
                    membersToDelete.add(aclMemberId);
                    aces.add((Long)aceId);
                }
            }

            // delete list of acl members
            aclCrudDAO.deleteAclMembers(membersToDelete);
        }

        if (!leaveAuthority)
        {
            // remove ACEs
            aclCrudDAO.deleteAces(aces);

            // Tidy up any unreferenced ACEs

            // get aces by authority
            List<Ace> unreferenced = aclCrudDAO.getAcesByAuthority(authEntity.getId());

            if (unreferenced.size() > 0)
            {
                List<Long> unrefencedAcesToDelete = new ArrayList<Long>(unreferenced.size());
                for (Ace ace : unreferenced)
                {
                    unrefencedAcesToDelete.add(ace.getId());
                }
                aclCrudDAO.deleteAces(unrefencedAcesToDelete);
            }

            // remove authority
            if (authEntity != null)
            {
                aclCrudDAO.deleteAuthority(authEntity.getId());
            }
        }

        return acls;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAclForNode(long aclId, boolean isAVMNode)
    {
        Acl dbAcl = getAcl(aclId);
        if (dbAcl.getAclType() == ACLType.DEFINING)
        {
            // delete acl members & acl
            aclCrudDAO.deleteAclMembersByAcl(aclId);
            aclCrudDAO.deleteAcl(aclId);

            aclCache.remove(aclId);
            readersCache.remove(aclId);
        }
        if (dbAcl.getAclType() == ACLType.SHARED)
        {
            // check unused
            Long defining = dbAcl.getInheritsFrom();
            if (aclCrudDAO.getAcl(defining) == null)
            {
                if (! isAVMNode)
                {
                    // ADM
                    if (getADMNodesByAcl(aclId, 1).size() == 0)
                    {
                        // delete acl members & acl
                        aclCrudDAO.deleteAclMembersByAcl(aclId);
                        aclCrudDAO.deleteAcl(aclId);

                        aclCache.remove(aclId);
                        readersCache.remove(aclId);
                    }
                }
                else
                {
                    // TODO: AVM
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> deleteAccessControlList(final Long id)
    {
        if (logger.isDebugEnabled())
        {
            // debug only
            int maxForDebug = 11;
            List<Long> nodeIds = getADMNodesByAcl(id, maxForDebug);

            for (Long nodeId : nodeIds)
            {
                logger.debug("deleteAccessControlList: Found nodeId=" + nodeId + ", aclId=" + id);
            }
        }

        List<AclChange> acls = new ArrayList<AclChange>();

        final AclUpdateEntity acl = aclCrudDAO.getAclForUpdate(id);
        if (!acl.isLatest())
        {
            throw new UnsupportedOperationException("Old ACL versions can not be updated");
        }
        if (acl.getAclType() == ACLType.SHARED)
        {
            throw new UnsupportedOperationException("Delete is not supported for shared acls - they are deleted with the defining acl");
        }

        if ((acl.getAclType() == ACLType.DEFINING) || (acl.getAclType() == ACLType.LAYERED))
        {
            if ((acl.getInheritedAcl() != null) && (acl.getInheritedAcl() != -1))
            {
                final Acl inherited = aclCrudDAO.getAcl(acl.getInheritedAcl());

                // Will remove from the cache
                getWritable(inherited.getId(), acl.getInheritsFrom(), null, null, null, true, acls, WriteMode.REMOVE_INHERITED);
                Acl unusedInherited = null;
                for (AclChange change : acls)
                {
                    if (change.getBefore() == inherited.getId())
                    {
                        unusedInherited = aclCrudDAO.getAcl(change.getAfter());
                    }
                }

                final Long newId = unusedInherited.getId();
                List<Long> inheritors = aclCrudDAO.getAclsThatInheritFromAcl(newId);
                for (Long nextId : inheritors)
                {
                    // Will remove from the cache
                    getWritable(nextId, acl.getInheritsFrom(), null, null, acl.getInheritsFrom(), true, acls, WriteMode.REMOVE_INHERITED);
                }

                // delete acl members
                aclCrudDAO.deleteAclMembersByAcl(newId);

                // delete 'unusedInherited' acl
                aclCrudDAO.deleteAcl(unusedInherited.getId());

                if (inherited.isVersioned())
                {
                    AclUpdateEntity inheritedForUpdate = aclCrudDAO.getAclForUpdate(inherited.getId());
                    if (inheritedForUpdate != null)
                    {
                        inheritedForUpdate.setLatest(Boolean.FALSE);
                        aclCrudDAO.updateAcl(inheritedForUpdate);
                    }
                }
                else
                {
                    // delete 'inherited' acl 
                    aclCrudDAO.deleteAcl(inherited.getId());
                }
            }
        }
        else
        {
            List<Long> inheritors = aclCrudDAO.getAclsThatInheritFromAcl(id);
            for (Long nextId : inheritors)
            {
                // Will remove from the cache
                getWritable(nextId, acl.getInheritsFrom(), null, null, null, true, acls, WriteMode.REMOVE_INHERITED);
            }
        }

        // delete
        if (acl.isVersioned())
        {
            acl.setLatest(Boolean.FALSE);
            aclCrudDAO.updateAcl(acl);
        }
        else
        {
            // delete acl members & acl
            aclCrudDAO.deleteAclMembersByAcl(id);
            aclCrudDAO.deleteAcl(acl.getId());
        }

        // remove the deleted acl from the cache
        aclCache.remove(id);
        readersCache.remove(id);
        acls.add(new AclChangeImpl(id, null, acl.getAclType(), null));
        return acls;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> deleteLocalAccessControlEntries(Long id)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        SimpleAccessControlEntry pattern = new SimpleAccessControlEntry();
        pattern.setPosition(Integer.valueOf(0));
        // Will remove from the cache
        getWritable(id, null, Collections.singletonList(pattern), null, null, true, changes, WriteMode.COPY_UPDATE_AND_INHERIT);
        return changes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> deleteInheritedAccessControlEntries(Long id)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        SimpleAccessControlEntry pattern = new SimpleAccessControlEntry();
        pattern.setPosition(Integer.valueOf(-1));
        // Will remove from the cache
        getWritable(id, null, Collections.singletonList(pattern), null, null, true, changes, WriteMode.COPY_UPDATE_AND_INHERIT);
        return changes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> deleteAccessControlEntries(Long id, AccessControlEntry pattern)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        // Will remove from the cache
        getWritable(id, null, Collections.singletonList(pattern), null, null, true, changes, WriteMode.COPY_UPDATE_AND_INHERIT);
        return changes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Acl getAcl(Long id)
    {
        return aclCrudDAO.getAcl(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessControlListProperties getAccessControlListProperties(Long id)
    {
        ParameterCheck.mandatory("id", id);                 // Prevent unboxing failures
        return aclCrudDAO.getAcl(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessControlList getAccessControlList(Long id)
    {
        AccessControlList acl = aclCache.get(id);
        if (acl == null)
        {
            acl = getAccessControlListImpl(id);
            aclCache.put(id, acl);
        }
        else
        {
            // System.out.println("Used cache for "+id);
        }
        return acl;
    }

    /**
     * @return the access control list
     */
    private AccessControlList getAccessControlListImpl(final Long id)
    {
        SimpleAccessControlList acl = new SimpleAccessControlList();
        AccessControlListProperties properties = getAccessControlListProperties(id);
        if (properties == null)
        {
            return null;
        }

        acl.setProperties(properties);

        List<Map<String, Object>> results = aclCrudDAO.getAcesAndAuthoritiesByAcl(id);

        List<AccessControlEntry> entries = new ArrayList<AccessControlEntry>(results.size());
        for (Map<String, Object> result : results)
            // for (AclMemberEntity member : members)
        {
            Boolean aceIsAllowed = (Boolean) result.get("allowed");
            Integer aceType = (Integer) result.get("applies");
            String authority = (String) result.get("authority");
            Long permissionId = (Long) result.get("permissionId");
            Integer position = (Integer) result.get("pos");
            //Long result_aclmemId = (Long) result.get("aclmemId"); // not used here

            SimpleAccessControlEntry sacEntry = new SimpleAccessControlEntry();
            sacEntry.setAccessStatus(aceIsAllowed ? AccessStatus.ALLOWED : AccessStatus.DENIED);
            sacEntry.setAceType(ACEType.getACETypeFromId(aceType));
            sacEntry.setAuthority(authority);
            // if (entry.getContext() != null)
            // {
            // SimpleAccessControlEntryContext context = new SimpleAccessControlEntryContext();
            // context.setClassContext(entry.getContext().getClassContext());
            // context.setKVPContext(entry.getContext().getKvpContext());
            // context.setPropertyContext(entry.getContext().getPropertyContext());
            // sacEntry.setContext(context);
            // }
            Permission perm = aclCrudDAO.getPermission(permissionId);
            QName permTypeQName = qnameDAO.getQName(perm.getTypeQNameId()).getSecond(); // Has an ID so must exist
            SimplePermissionReference permissionRefernce = SimplePermissionReference.getPermissionReference(permTypeQName, perm.getName());
            sacEntry.setPermission(permissionRefernce);
            sacEntry.setPosition(position);
            entries.add(sacEntry);
        }

        Collections.sort(entries);
        acl.setEntries(entries);

        return acl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getInheritedAccessControlList(Long id)
    {
        aclCache.remove(id);
        AclUpdateEntity acl = aclCrudDAO.getAclForUpdate(id);
        if (acl.getAclType() == ACLType.OLD)
        {
            return null;
        }
        if ((acl.getInheritedAcl() != null) && (acl.getInheritedAcl() != -1))
        {
            return acl.getInheritedAcl();
        }

        Long inheritedAclId = null;

        if ((acl.getAclType() == ACLType.DEFINING) || (acl.getAclType() == ACLType.LAYERED))
        {
            List<AclChange> changes = new ArrayList<AclChange>();
            // created shared acl
            SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
            properties.setAclType(ACLType.SHARED);
            properties.setInherits(Boolean.TRUE);
            properties.setVersioned(acl.isVersioned());
            Long sharedId = createAccessControlList(properties, null, null).getId();
            getWritable(sharedId, id, null, null, id, true, changes, WriteMode.ADD_INHERITED);
            acl.setInheritedAcl(sharedId);
            inheritedAclId = sharedId;
        }
        else
        {
            acl.setInheritedAcl(acl.getId());
            inheritedAclId = acl.getId();
        }

        aclCrudDAO.updateAcl(acl);
        return inheritedAclId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> mergeInheritedAccessControlList(Long inherited, Long target)
    {
        // TODO: For now we do a replace - we could do an insert if both inherit from the same acl

        List<AclChange> changes = new ArrayList<AclChange>();

        Acl targetAcl = aclCrudDAO.getAcl(target);

        Acl inheritedAcl = null;
        if (inherited != null)
        {
            inheritedAcl = aclCrudDAO.getAcl(inherited);
        }
        else
        {
            // Assume we are just resetting it to inherit as before
            if (targetAcl.getInheritsFrom() != null)
            {
                inheritedAcl = aclCrudDAO.getAcl(targetAcl.getInheritsFrom());
                if (inheritedAcl == null)
                {
                    // TODO: Try previous versions
                    throw new IllegalStateException("No old inheritance definition to use");
                }
                else
                {
                    // find the latest version of the acl
                    if (!inheritedAcl.isLatest())
                    {
                        final String searchAclId = inheritedAcl.getAclId();

                        Long actualInheritor = (Long)aclCrudDAO.getLatestAclByGuid(searchAclId);

                        inheritedAcl = aclCrudDAO.getAcl(actualInheritor);
                        if (inheritedAcl == null)
                        {
                            // TODO: Try previous versions
                            throw new IllegalStateException("No ACL found");
                        }
                    }
                }
            }
            else
            {
                // There is no inheritance to set
                return changes;
            }
        }

        // recursion test
        // if inherited already inherits from the target

        Acl test = inheritedAcl;
        while (test != null)
        {
            if (test.getId() == target)
            {
                throw new IllegalStateException("Cyclical ACL detected");
            }
            Long parent = test.getInheritsFrom();
            if ((parent == null) || (parent == -1l))
            {
                test = null;
            }
            else
            {
                test = aclCrudDAO.getAcl(test.getInheritsFrom());
            }
        }

        if ((targetAcl.getAclType() != ACLType.DEFINING) && (targetAcl.getAclType() != ACLType.LAYERED))
        {
            throw new IllegalArgumentException("Only defining ACLs can have their inheritance set");
        }

        if (!targetAcl.getInherits())
        {
            return changes;
        }

        Long actualInheritedId = inheritedAcl.getId();

        if ((inheritedAcl.getAclType() == ACLType.DEFINING) || (inheritedAcl.getAclType() == ACLType.LAYERED))
        {
            actualInheritedId = getInheritedAccessControlList(actualInheritedId);
        }
        // Will remove from the cache
        getWritable(target, actualInheritedId, null, null, actualInheritedId, true, changes, WriteMode.CHANGE_INHERITED);

        return changes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> setAccessControlEntry(final Long id, final AccessControlEntry ace)
    {
        Acl target = aclCrudDAO.getAcl(id);
        if (target.getAclType() == ACLType.SHARED)
        {
            throw new IllegalArgumentException("Shared ACLs are immutable");
        }

        List<AclChange> changes = new ArrayList<AclChange>();

        if ((ace.getPosition() != null) && (ace.getPosition() != 0))
        {
            throw new IllegalArgumentException("Invalid position");
        }

        // Find authority
        Authority authority = aclCrudDAO.getOrCreateAuthority(ace.getAuthority());
        Permission permission = aclCrudDAO.getOrCreatePermission(ace.getPermission());

        // Find context
        if (ace.getContext() != null)
        {
            throw new UnsupportedOperationException();
        }

        // Find ACE
        Ace entry = aclCrudDAO.getOrCreateAce(permission, authority, ace.getAceType(), ace.getAccessStatus());

        // Wire up
        // COW and remove any existing matches

        SimpleAccessControlEntry exclude = new SimpleAccessControlEntry();
        // match any access status
        exclude.setAceType(ace.getAceType());
        exclude.setAuthority(ace.getAuthority());
        exclude.setPermission(ace.getPermission());
        exclude.setPosition(0);
        List<Ace> toAdd = new ArrayList<Ace>(1);
        toAdd.add(entry);
        // Will remove from the cache
        getWritable(id, null, Collections.singletonList(exclude), toAdd, null, true, changes, WriteMode.COPY_UPDATE_AND_INHERIT);

        return changes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> enableInheritance(Long id, Long parent)
    {
        List<AclChange> changes = new ArrayList<AclChange>();

        AclUpdateEntity acl = aclCrudDAO.getAclForUpdate(id);

        switch (acl.getAclType())
        {
        case FIXED:
        case GLOBAL:
            throw new IllegalArgumentException("Fixed and global permissions can not inherit");
        case OLD:
            acl.setInherits(Boolean.TRUE);
            aclCrudDAO.updateAcl(acl);
            aclCache.remove(id);
            readersCache.remove(id);
            changes.add(new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType()));
            return changes;
        case SHARED:
            // TODO support a list of children and casacade if given
            throw new IllegalArgumentException(
            "Shared acls should be replace by creating a definig ACL, wiring it up for inhertitance, and then applying inheritance to any children. It can not be done by magic ");
        case DEFINING:
        case LAYERED:
        default:
            if (!acl.getInherits())
            {
                // Will remove from the cache
                getWritable(id, null, null, null, null, false, changes, WriteMode.COPY_ONLY);
                acl = aclCrudDAO.getAclForUpdate(changes.get(0).getAfter());
                acl.setInherits(Boolean.TRUE);
                aclCrudDAO.updateAcl(acl);
            }
            else
            {
                // Will remove from the cache
                getWritable(id, null, null, null, null, false, changes, WriteMode.COPY_ONLY);
            }

            List<AclChange> merged = mergeInheritedAccessControlList(parent, changes.get(0).getAfter());
            changes.addAll(merged);
            return changes;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChange> disableInheritance(Long id, boolean setInheritedOnAcl)
    {
        aclCache.remove(id);
        AclUpdateEntity acl = aclCrudDAO.getAclForUpdate(id);
        List<AclChange> changes = new ArrayList<AclChange>(1);
        switch (acl.getAclType())
        {
        case FIXED:
        case GLOBAL:
            return Collections.<AclChange> singletonList(new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType()));
        case OLD:
            acl.setInherits(Boolean.FALSE);
            aclCrudDAO.updateAcl(acl);
            aclCache.remove(id);
            readersCache.remove(id);
            changes.add(new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType()));
            return changes;
        case SHARED:
            // TODO support a list of children and casacade if given
            throw new IllegalArgumentException("Shared ACL must inherit");
        case DEFINING:
        case LAYERED:
        default:
            return disableInheritanceImpl(id, setInheritedOnAcl, acl);
        }
    }

    private Long getCopy(Long toCopy, Long toInheritFrom, ACLCopyMode mode)
    {
        AclUpdateEntity aclToCopy;
        Long inheritedId;
        Acl aclToInheritFrom;
        switch (mode)
        {
        case INHERIT:
            if (toCopy.equals(toInheritFrom))
            {
                return getInheritedAccessControlList(toCopy);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        case COW:
            aclToCopy = aclCrudDAO.getAclForUpdate(toCopy);
            aclToCopy.setRequiresVersion(true);
            aclCrudDAO.updateAcl(aclToCopy);
            aclCache.remove(toCopy);
            readersCache.remove(toCopy);
            inheritedId = getInheritedAccessControlList(toCopy);
            if ((inheritedId != null) && (!inheritedId.equals(toCopy)))
            {
                AclUpdateEntity inheritedAcl = aclCrudDAO.getAclForUpdate(inheritedId);
                inheritedAcl.setRequiresVersion(true);
                aclCrudDAO.updateAcl(inheritedAcl);
                aclCache.remove(inheritedId);
                readersCache.remove(inheritedId);
            }
            return toCopy;
        case REDIRECT:
            if ((toInheritFrom != null) && (toInheritFrom == toCopy))
            {
                return getInheritedAccessControlList(toInheritFrom);
            }
            aclToCopy = aclCrudDAO.getAclForUpdate(toCopy);
            aclToInheritFrom = null;
            if (toInheritFrom != null)
            {
                aclToInheritFrom = aclCrudDAO.getAcl(toInheritFrom);
            }

            switch (aclToCopy.getAclType())
            {
            case DEFINING:
                // This is not called on the redirecting node as only LAYERED change permissions when redirected
                // So this needs to make a copy in the same way layered does
            case LAYERED:
                if (toInheritFrom == null)
                {
                    return toCopy;
                }
                // manages cache clearing beneath
                List<AclChange> changes = mergeInheritedAccessControlList(toInheritFrom, toCopy);
                for (AclChange change : changes)
                {
                    if (change.getBefore().equals(toCopy))
                    {
                        return change.getAfter();
                    }
                }
                throw new UnsupportedOperationException();
            case SHARED:
                if (aclToInheritFrom != null)
                {
                    return getInheritedAccessControlList(toInheritFrom);
                }
                else
                {
                    throw new UnsupportedOperationException();
                }
            case FIXED:
            case GLOBAL:
            case OLD:
                return toCopy;
            default:
                throw new UnsupportedOperationException();
            }
        case COPY:
            aclToCopy = aclCrudDAO.getAclForUpdate(toCopy);
            aclToInheritFrom = null;
            if (toInheritFrom != null)
            {
                aclToInheritFrom = aclCrudDAO.getAcl(toInheritFrom);
            }

            switch (aclToCopy.getAclType())
            {
            case DEFINING:
                SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
                properties.setAclType(ACLType.DEFINING);
                properties.setInherits(aclToCopy.getInherits());
                properties.setVersioned(true);

                Long id = createAccessControlList(properties).getId();

                AccessControlList indirectAcl = getAccessControlList(toCopy);
                for (AccessControlEntry entry : indirectAcl.getEntries())
                {
                    if (entry.getPosition() == 0)
                    {
                        setAccessControlEntry(id, entry);
                    }
                }
                if (aclToInheritFrom != null)
                {
                    mergeInheritedAccessControlList(toInheritFrom, id);
                }
                return id;
            case SHARED:
                if (aclToInheritFrom != null)
                {
                    return getInheritedAccessControlList(toInheritFrom);
                }
                else
                {
                    return null;
                }
            case FIXED:
            case GLOBAL:
            case LAYERED:
            case OLD:
                return toCopy;
            default:
                throw new UnsupportedOperationException();
            }
        default:
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Acl getAclCopy(Long toCopy, Long toInheritFrom, ACLCopyMode mode)
    {
        return getAclEntityCopy(toCopy, toInheritFrom, mode);
    }

    private Acl getAclEntityCopy(Long toCopy, Long toInheritFrom, ACLCopyMode mode)
    {
        Long id = getCopy(toCopy, toInheritFrom, mode);
        if (id == null)
        {
            return null;
        }
        return aclCrudDAO.getAcl(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> getAVMNodesByAcl(long aclEntityId, int maxResults)
    {
        return aclCrudDAO.getAVMNodesByAcl(aclEntityId, maxResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> getADMNodesByAcl(long aclEntityId, int maxResults)
    {
        return aclCrudDAO.getADMNodesByAcl(aclEntityId, maxResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Acl createLayeredAcl(Long indirectedAcl)
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.LAYERED);

        Acl acl = createAccessControlList(properties);
        long id = acl.getId();

        if (indirectedAcl != null)
        {
            mergeInheritedAccessControlList(indirectedAcl, id);
        }
        return acl;
    }

    private List<AclChange> disableInheritanceImpl(Long id, boolean setInheritedOnAcl, AclEntity aclIn)
    {
        List<AclChange> changes = new ArrayList<AclChange>();

        if (!aclIn.getInherits())
        {
            return Collections.<AclChange> emptyList();
        }

        // Manages caching
        getWritable(id, null, null, null, null, false, changes, WriteMode.COPY_ONLY);
        AclUpdateEntity acl = aclCrudDAO.getAclForUpdate(changes.get(0).getAfter());
        final Long inheritsFrom = acl.getInheritsFrom();
        acl.setInherits(Boolean.FALSE);
        aclCrudDAO.updateAcl(acl);

        // Keep inherits from so we can reinstate if required
        // acl.setInheritsFrom(-1l);

        // Manages caching
        getWritable(acl.getId(), null, null, null, null, true, changes, WriteMode.TRUNCATE_INHERITED);

        // set Inherited - TODO: UNTESTED

        if ((inheritsFrom != null) && (inheritsFrom != -1) && setInheritedOnAcl)
        {
            // get aces for acl (via acl member)
            List<AclMember> members = aclCrudDAO.getAclMembersByAcl(inheritsFrom);

            for (AclMember member : members)
            {
                // TODO optimise
                Ace ace = aclCrudDAO.getAce(member.getAceId());
                Authority authority = aclCrudDAO.getAuthority(ace.getAuthorityId());

                SimpleAccessControlEntry entry = new SimpleAccessControlEntry();
                entry.setAccessStatus(ace.isAllowed() ? AccessStatus.ALLOWED : AccessStatus.DENIED);
                entry.setAceType(ace.getAceType());
                entry.setAuthority(authority.getAuthority());

                /* NOTE: currently unused - intended for possible future enhancement
                if (ace.getContextId() != null)
                {
                    AceContext aceContext = aclCrudDAO.getAceContext(ace.getContextId());

                    SimpleAccessControlEntryContext context = new SimpleAccessControlEntryContext();
                    context.setClassContext(aceContext.getClassContext());
                    context.setKVPContext(aceContext.getKvpContext());
                    context.setPropertyContext(aceContext.getPropertyContext());
                    entry.setContext(context);
                }
                 */

                Permission perm = aclCrudDAO.getPermission(ace.getPermissionId());
                QName permTypeQName = qnameDAO.getQName(perm.getTypeQNameId()).getSecond(); // Has an ID so must exist
                SimplePermissionReference permissionRefernce = SimplePermissionReference.getPermissionReference(permTypeQName, perm.getName());
                entry.setPermission(permissionRefernce);
                entry.setPosition(Integer.valueOf(0));

                setAccessControlEntry(id, entry);
            }
        }
        return changes;
    }

    private static final String RESOURCE_KEY_ACL_CHANGE_SET_ID = "acl.change.set.id";

    /**
     * Support to get the current ACL change set and bind this to the transaction. So we only make one new version of an
     * ACL per change set. If something is in the current change set we can update it.
     */
    private long getCurrentChangeSetId()
    {
        Long changeSetId = (Long)AlfrescoTransactionSupport.getResource(RESOURCE_KEY_ACL_CHANGE_SET_ID);
        if (changeSetId == null)
        {
            changeSetId = aclCrudDAO.createAclChangeSet();

            // bind the id
            AlfrescoTransactionSupport.bindResource(RESOURCE_KEY_ACL_CHANGE_SET_ID, changeSetId);
            if (logger.isDebugEnabled())
            {
                logger.debug("New change set = " + changeSetId);
            }
        }
        return changeSetId;
    }

    private static class AcePatternMatcher
    {
        private List<? extends AccessControlEntry> patterns;

        AcePatternMatcher(List<? extends AccessControlEntry> patterns)
        {
            this.patterns = patterns;
        }

        boolean matches(AclCrudDAO aclCrudDAO, Map<String, Object> result, int position)
        {
            if (patterns == null)
            {
                return true;
            }

            for (AccessControlEntry pattern : patterns)
            {
                if (checkPattern(aclCrudDAO, result, position, pattern))
                {
                    return true;
                }
            }
            return false;
        }

        private boolean checkPattern(AclCrudDAO aclCrudDAO, Map<String, Object> result, int position, AccessControlEntry pattern)
        {
            Boolean result_aceIsAllowed = (Boolean) result.get("allowed");
            Integer result_aceType = (Integer) result.get("applies");
            String result_authority = (String) result.get("authority");
            Long result_permissionId = (Long) result.get("permissionId");
            Integer result_position = (Integer) result.get("pos");
            //Long result_aclmemId = (Long) result.get("aclmemId"); // not used

            if (pattern.getAccessStatus() != null)
            {
                if (pattern.getAccessStatus() != (result_aceIsAllowed ? AccessStatus.ALLOWED : AccessStatus.DENIED))
                {
                    return false;
                }
            }

            if (pattern.getAceType() != null)
            {
                if (pattern.getAceType() != ACEType.getACETypeFromId(result_aceType))
                {
                    return false;
                }
            }

            if (pattern.getAuthority() != null)
            {
                if ((pattern.getAuthorityType() != AuthorityType.WILDCARD) && !pattern.getAuthority().equals(result_authority))
                {
                    return false;
                }
            }

            if (pattern.getContext() != null)
            {
                throw new IllegalArgumentException("Context not yet supported");
            }

            if (pattern.getPermission() != null)
            {
                Long permId = aclCrudDAO.getPermission(pattern.getPermission()).getId();
                if (!permId.equals(result_permissionId))
                {
                    return false;
                }
            }

            if (pattern.getPosition() != null)
            {
                if (pattern.getPosition().intValue() >= 0)
                {
                    if (result_position != position)
                    {
                        return false;
                    }
                }
                else if (pattern.getPosition().intValue() == -1)
                {
                    if (result_position <= position)
                    {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    static class AclChangeImpl implements AclChange
    {
        private Long before;
        private Long after;
        private ACLType typeBefore;
        private ACLType typeAfter;

        public AclChangeImpl(Long before, Long after, ACLType typeBefore, ACLType typeAfter)
        {
            this.before = before;
            this.after = after;
            this.typeAfter = typeAfter;
            this.typeBefore = typeBefore;
        }

        public Long getAfter()
        {
            return after;
        }

        public Long getBefore()
        {
            return before;
        }

        /**
         * @param after
         */
        public void setAfter(Long after)
        {
            this.after = after;
        }

        /**
         * @param before
         */
        public void setBefore(Long before)
        {
            this.before = before;
        }

        public ACLType getTypeAfter()
        {
            return typeAfter;
        }

        /**
         * @param typeAfter
         */
        public void setTypeAfter(ACLType typeAfter)
        {
            this.typeAfter = typeAfter;
        }

        public ACLType getTypeBefore()
        {
            return typeBefore;
        }

        /**
         * @param typeBefore
         */
        public void setTypeBefore(ACLType typeBefore)
        {
            this.typeBefore = typeBefore;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("(").append(getBefore()).append(",").append(getTypeBefore()).append(")");
            builder.append(" - > ");
            builder.append("(").append(getAfter()).append(",").append(getTypeAfter()).append(")");
            return builder.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renameAuthority(String before, String after)
    {
        aclCrudDAO.renameAuthority(before, after);
        aclCache.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fixSharedAcl(Long shared, Long defining)
    {
        if (defining == null)
        {
            throw new IllegalArgumentException("Null defining acl");
        }
        
        if (shared == null)
        {
            throw new IllegalArgumentException("Null shared acl");
        }
        List<AclChange> changes = new ArrayList<AclChange>();
        getWritable(shared, defining, null, null, defining, true, changes, WriteMode.CHANGE_INHERITED);
    }
}
