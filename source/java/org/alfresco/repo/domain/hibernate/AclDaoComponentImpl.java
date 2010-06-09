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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.DbAccessControlListChangeSet;
import org.alfresco.repo.domain.DbAccessControlListMember;
import org.alfresco.repo.domain.DbAuthority;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.avm.AVMNodeDAO;
import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.permissions.ACEType;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.SimpleAccessControlEntry;
import org.alfresco.repo.security.permissions.SimpleAccessControlEntryContext;
import org.alfresco.repo.security.permissions.SimpleAccessControlList;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.repo.security.permissions.impl.AclDaoComponent;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.alfresco.util.Pair;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate DAO to manage ACL persistence
 * 
 * @author andyh
 */
public class AclDaoComponentImpl extends HibernateDaoSupport implements AclDaoComponent
{
    private static Log logger = LogFactory.getLog(AclDaoComponentImpl.class);

    static String QUERY_GET_PERMISSION = "permission.GetPermission";

    static String QUERY_GET_AUTHORITY = "permission.GetAuthority";

    static String QUERY_GET_ACE_WITH_NO_CONTEXT = "permission.GetAceWithNoContext";

    // static String QUERY_GET_AUTHORITY_ALIAS = "permission.GetAuthorityAlias";

    // static String QUERY_GET_AUTHORITY_ALIASES = "permission.GetAuthorityAliases";

    static String QUERY_GET_ACES_AND_ACLS_BY_AUTHORITY = "permission.GetAcesAndAclsByAuthority";

    static String QUERY_GET_ACES_BY_AUTHORITY = "permission.GetAcesByAuthority";

    static String QUERY_GET_ACES_FOR_ACL = "permission.GetAcesForAcl";

    static String QUERY_LOAD_ACL = "permission.LoadAcl";

    static String QUERY_GET_ACLS_THAT_INHERIT_FROM_THIS_ACL = "permission.GetAclsThatInheritFromThisAcl";

    static String QUERY_GET_AVM_NODES_BY_ACL = "permission.FindAvmNodesByACL";

    static String QUERY_GET_LATEST_ACL_BY_ACLID = "permission.FindLatestAclByGuid";

    /** Access to QName entities */
    private QNameDAO qnameDAO;
    
    /** Access to AVMNode queries */
    private AVMNodeDAO avmNodeDAO;
    
    /** Access to additional Patch queries */
    private PatchDAO patchDAO;

    /** a transactionally-safe cache to be injected */
    private SimpleCache<Long, AccessControlList> aclCache;

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
         * Simlpe copy
         */
        COPY_ONLY, CREATE_AND_INHERIT;
    }

    /**
     *
     */
    public AclDaoComponentImpl()
    {
        super();
        // Wire up for annoying AVM hack to support copy and setting of ACLs as nodes are created
        DbAccessControlListImpl.setAclDaoComponent(this);
    }

    /**
     * Set the DAO for accessing QName entities
     * 
     * @param qnameDAO
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }
    
    public void setAvmNodeDAO(AVMNodeDAO avmNodeDAO)
    {
        this.avmNodeDAO = avmNodeDAO;
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

    public DbAccessControlList getDbAccessControlList(Long id)
    {
        if (id == null)
        {
            return null;
        }
        DbAccessControlList acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, id);
        return acl;
    }

    public Long createAccessControlList(AccessControlListProperties properties)
    {
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
        return createAccessControlListImpl(properties, null, null);
    }

    private Long createAccessControlListImpl(AccessControlListProperties properties, List<AccessControlEntry> aces, Long inherited)
    {
        DbAccessControlListImpl acl = new DbAccessControlListImpl();
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
        case FIXED:
        case GLOBAL:
        case SHARED:
        case DEFINING:
        case LAYERED:
        default:
            if (properties.isVersioned() != null)
            {
                acl.setVersioned(properties.isVersioned());
            }
            else
            {
                acl.setVersioned(Boolean.TRUE);
            }
            break;
        }

        acl.setAclChangeSet(getCurrentChangeSet());
        acl.setRequiresVersion(false);
        Long created = (Long) getHibernateTemplate().save(acl);
        DirtySessionMethodInterceptor.flushSession(getSession(), true);

        if ((aces != null) && aces.size() > 0)
        {
            List<AclChange> changes = new ArrayList<AclChange>();

            List<DbAccessControlEntry> toAdd = new ArrayList<DbAccessControlEntry>(aces.size());
            List<AccessControlEntry> excluded = new ArrayList<AccessControlEntry>(aces.size());
            for (AccessControlEntry ace : aces)
            {

                if ((ace.getPosition() != null) && (ace.getPosition() != 0))
                {
                    throw new IllegalArgumentException("Invalid position");
                }

                // Find authority
                DbAuthority authority = getAuthority(ace.getAuthority(), true);
                DbPermission permission = getPermission(ace.getPermission(), true);

                // Find context

                if (ace.getContext() != null)
                {
                    throw new UnsupportedOperationException();
                }

                // Find ACE
                DbAccessControlEntry entry = getAccessControlEntry(permission, authority, ace, true);

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
            Long toInherit = null;
            if (inherited != null)
            {
                toInherit = getInheritedAccessControlList(inherited);
            }
            getWritable(created, toInherit, excluded, toAdd, toInherit, false, changes, WriteMode.CREATE_AND_INHERIT);
        }

        return created;
    }

    @SuppressWarnings("unchecked")
    private void getWritable(final Long id, final Long parent, List<? extends AccessControlEntry> exclude, List<DbAccessControlEntry> toAdd, Long inheritsFrom, boolean cascade,
            List<AclChange> changes, WriteMode mode)
    {
        List<DbAccessControlEntry> inherited = null;
        List<Integer> positions = null;

        if ((mode == WriteMode.ADD_INHERITED) || (mode == WriteMode.INSERT_INHERITED) || (mode == WriteMode.CHANGE_INHERITED))
        {
            inherited = new ArrayList<DbAccessControlEntry>();
            positions = new ArrayList<Integer>();

            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(QUERY_GET_ACES_FOR_ACL);
                    query.setParameter("id", parent);
                    DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                    return query.list();
                }
            };
            List<DbAccessControlListMember> members = (List<DbAccessControlListMember>) getHibernateTemplate().execute(callback);

            for (DbAccessControlListMember member : members)
            {
                if ((mode == WriteMode.INSERT_INHERITED) && (member.getPosition() == 0))
                {
                    inherited.add(member.getAccessControlEntry());
                    positions.add(member.getPosition());
                }
                else
                {
                    inherited.add(member.getAccessControlEntry());
                    positions.add(member.getPosition());
                }
            }
        }

        getWritable(id, parent, exclude, toAdd, inheritsFrom, inherited, positions, cascade, 0, changes, mode, false);
    }

    /**
     * Make a whole tree of ACLs copy on write if required Includes adding and removing ACEs which cna be optimised
     * slighlty for copy on write (no need to add and then remove)
     * 
     * @param id
     * @param parent
     * @param exclude
     * @param toAdd
     * @param inheritsFrom
     * @param cascade
     * @param depth
     * @param changes
     */
    @SuppressWarnings("unchecked")
    private void getWritable(final Long id, final Long parent, List<? extends AccessControlEntry> exclude, List<DbAccessControlEntry> toAdd, Long inheritsFrom,
            List<DbAccessControlEntry> inherited, List<Integer> positions, boolean cascade, int depth, List<AclChange> changes, WriteMode mode, boolean requiresVersion)
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
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(QUERY_GET_ACLS_THAT_INHERIT_FROM_THIS_ACL);
                    query.setParameter("id", id);
                    DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                    return query.list();
                }
            };
            List<Long> inheritors = (List<Long>) getHibernateTemplate().execute(callback);
            for (Long nextId : inheritors)
            {
                // Check for those that inherit themselves to other nodes ...
                if (nextId != id)
                {
                    getWritable(nextId, current.getAfter(), exclude, toAdd, current.getAfter(), inherited, positions, cascade, depth + 1, changes, mode, cascadeVersion);
                }
            }
        }
    }

    /**
     * COW for an individual ACL
     * 
     * @param id
     * @param parent
     * @param exclude
     * @param toAdd
     * @param inheritsFrom
     * @param depth
     * @return - an AclChange
     */
    @SuppressWarnings("unchecked")
    private AclChange getWritable(final Long id, final Long parent, List<? extends AccessControlEntry> exclude, List<DbAccessControlEntry> toAdd, Long inheritsFrom,
            List<DbAccessControlEntry> inherited, List<Integer> positions, int depth, WriteMode mode, boolean requiresVersion)
    {
        DbAccessControlList acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, id);
        if (!acl.isLatest())
        {
            aclCache.remove(id);
            return new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType());
        }

        if (!acl.isVersioned())
        {
            switch (mode)
            {
            case COPY_UPDATE_AND_INHERIT:
                removeAcesFromAcl(id, exclude, depth);
                addAcesToAcl(acl, toAdd, depth);
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
                addAcesToAcl(acl, toAdd, depth);
                addInherited(acl, inherited, positions, depth);
            case COPY_ONLY:
            default:
                break;
            }
            if (inheritsFrom != null)
            {
                acl.setInheritsFrom(inheritsFrom);
            }
            aclCache.remove(id);
            return new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType());
        }
        else if ((acl.getAclChangeSet() == getCurrentChangeSet()) && (!requiresVersion) && (!acl.getRequiresVersion()))
        {
            switch (mode)
            {
            case COPY_UPDATE_AND_INHERIT:
                removeAcesFromAcl(id, exclude, depth);
                addAcesToAcl(acl, toAdd, depth);
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
                addAcesToAcl(acl, toAdd, depth);
                addInherited(acl, inherited, positions, depth);
            case COPY_ONLY:
            default:
                break;
            }
            if (inheritsFrom != null)
            {
                acl.setInheritsFrom(inheritsFrom);
            }
            aclCache.remove(id);
            return new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType());
        }
        else
        {
            DbAccessControlList newAcl = new DbAccessControlListImpl();
            newAcl.setAclChangeSet(getCurrentChangeSet());
            newAcl.setAclId(acl.getAclId());
            newAcl.setAclType(acl.getAclType());
            newAcl.setAclVersion(acl.getAclVersion() + 1);
            newAcl.setInheritedAclId(-1l);
            newAcl.setInherits(acl.getInherits());
            newAcl.setInheritsFrom((inheritsFrom != null) ? inheritsFrom : acl.getInheritsFrom());
            newAcl.setLatest(Boolean.TRUE);
            newAcl.setVersioned(Boolean.TRUE);
            newAcl.setRequiresVersion(Boolean.FALSE);
            Long created = (Long) getHibernateTemplate().save(newAcl);
            DirtySessionMethodInterceptor.flushSession(getSession(), true);

            // Create new membership entries - excluding those in the given pattern

            // AcePatternMatcher excluder = new AcePatternMatcher(exclude);
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(QUERY_GET_ACES_FOR_ACL);
                    query.setParameter("id", id);
                    DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                    return query.list();
                }
            };
            List<DbAccessControlListMember> members = (List<DbAccessControlListMember>) getHibernateTemplate().execute(callback);

            for (DbAccessControlListMember member : members)
            {
                // if (mode == WriteMode.COPY_UPDATE_AND_INHERIT)
                // {
                // if ((member.getPosition() == depth) && ((excluder == null) || !excluder.matches(member.getACE(),
                // member.getPosition())))
                // {
                // DbAccessControlListMemberImpl newMember = new DbAccessControlListMemberImpl();
                // newMember.setACL(newAcl);
                // newMember.setACE(member.getACE());
                // newMember.setPosition(member.getPosition());
                // getHibernateTemplate().save(newMember);
                // }
                // }

                // TODO: optimise copy cases :-)
                DbAccessControlListMemberImpl newMember = new DbAccessControlListMemberImpl();
                newMember.setAccessControlList(newAcl);
                newMember.setAccessControlEntry(member.getAccessControlEntry());
                newMember.setPosition(member.getPosition());
                getHibernateTemplate().save(newMember);
                DirtySessionMethodInterceptor.flushSession(getSession(), true);

            }

            // add new

            switch (mode)
            {
            case COPY_UPDATE_AND_INHERIT:
                // Done above
                removeAcesFromAcl(newAcl.getId(), exclude, depth);
                addAcesToAcl(newAcl, toAdd, depth);
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
                addAcesToAcl(acl, toAdd, depth);
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
                    DbAccessControlList parentAcl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, writableParentAcl);
                    parentAcl.setInheritedAclId(created);
                }
            }

            // fix up old version
            acl.setLatest(Boolean.FALSE);
            acl.setRequiresVersion(Boolean.FALSE);
            aclCache.remove(id);
            return new AclChangeImpl(id, created, acl.getAclType(), newAcl.getAclType());
        }

    }

    /**
     * Helper to remove ACEs from an ACL
     * 
     * @param id
     * @param exclude
     * @param depth
     */
    @SuppressWarnings("unchecked")
    private void removeAcesFromAcl(final Long id, final List<? extends AccessControlEntry> exclude, final int depth)
    {
        AcePatternMatcher excluder = new AcePatternMatcher(exclude);
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                if (exclude == null)
                {
                    Criteria criteria = session.createCriteria(DbAccessControlListMemberImpl.class, "member");
                    criteria.createAlias("accessControlList", "acl");
                    criteria.add(Restrictions.eq("acl.id", id));
                    criteria.createAlias("accessControlEntry", "ace");
                    criteria.createAlias("ace.authority", "authority");
                    criteria.createAlias("ace.permission", "permission");
                    criteria.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
                    DirtySessionMethodInterceptor.setCriteriaFlushMode(session, criteria);
                    return criteria.list();
                }
                else
                {
                    Criteria criteria = session.createCriteria(DbAccessControlListMemberImpl.class, "member");
                    criteria.createAlias("accessControlList", "acl");
                    criteria.add(Restrictions.eq("acl.id", id));
                    // build or
                    if (exclude.size() == 1)
                    {
                        AccessControlEntry excluded = exclude.get(0);
                        if ((excluded.getPosition() != null) && excluded.getPosition() >= 0)
                        {
                            criteria.add(Restrictions.eq("position", Integer.valueOf(depth)));
                        }
                        if ((excluded.getAccessStatus() != null) || (excluded.getAceType() != null) || (excluded.getAuthority() != null) || (excluded.getPermission() != null))
                        {
                            criteria.createAlias("accessControlEntry", "ace");
                            if (excluded.getAccessStatus() != null)
                            {
                                criteria.add(Restrictions.eq("ace.allowed", excluded.getAccessStatus() == AccessStatus.ALLOWED ? Boolean.TRUE : Boolean.FALSE));
                            }
                            if (excluded.getAceType() != null)
                            {
                                criteria.add(Restrictions.eq("ace.applies", Integer.valueOf(excluded.getAceType().getId())));
                            }
                            if (excluded.getAuthority() != null)
                            {
                                criteria.createAlias("ace.authority", "authority");
                                criteria.add(Restrictions.eq("authority.authority", excluded.getAuthority()));
                            }
                            if (excluded.getPermission() != null)
                            {
                                criteria.createAlias("ace.permission", "permission");
                                criteria.add(Restrictions.eq("permission.name", excluded.getPermission().getName()));
                                // TODO: Add typeQname
                            }
                        }
                    }
                    else
                    {

                        criteria.createAlias("accessControlEntry", "ace");
                        criteria.createAlias("ace.authority", "authority");
                        criteria.createAlias("ace.permission", "permission");
                        List<Criterion> toOr = new LinkedList<Criterion>();
                        LOOP: for (AccessControlEntry excluded : exclude)
                        {
                            List<Criterion> toAnd = new LinkedList<Criterion>();
                            if ((excluded.getPosition() != null) && excluded.getPosition() >= 0)
                            {
                                toAnd.add(Restrictions.eq("position", Integer.valueOf(depth)));
                            }
                            if (excluded.getAccessStatus() != null)
                            {
                                toAnd.add(Restrictions.eq("ace.allowed", excluded.getAccessStatus() == AccessStatus.ALLOWED ? Boolean.TRUE : Boolean.FALSE));
                            }
                            if (excluded.getAceType() != null)
                            {
                                toAnd.add(Restrictions.eq("ace.applies", Integer.valueOf(excluded.getAceType().getId())));
                            }
                            if (excluded.getAuthority() != null)
                            {
                                toAnd.add(Restrictions.eq("authority.authority", excluded.getAuthority()));
                            }
                            if (excluded.getPermission() != null)
                            {
                                toAnd.add(Restrictions.eq("permission.name", excluded.getPermission().getName()));
                                // TODO: Add typeQname
                            }

                            Criterion accumulated = null;
                            for (Criterion current : toAnd)
                            {
                                if (accumulated == null)
                                {
                                    accumulated = current;
                                }
                                else
                                {
                                    accumulated = Restrictions.and(accumulated, current);
                                }
                            }
                            if (accumulated == null)
                            {
                                // matches all
                                toOr = null;
                                break LOOP;
                            }
                            else
                            {
                                toOr.add(accumulated);
                            }
                        }
                        Criterion accumulated = null;
                        for (Criterion current : toOr)
                        {
                            if (accumulated == null)
                            {
                                accumulated = current;
                            }
                            else
                            {
                                accumulated = Restrictions.or(accumulated, current);
                            }
                        }
                        if (accumulated == null)
                        {
                            // no action
                        }
                        else
                        {
                            criteria.add(accumulated);
                        }

                    }

                    criteria.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
                    DirtySessionMethodInterceptor.setCriteriaFlushMode(session, criteria);
                    return criteria.list();

                }
            }
        };

        List<Map<String, Object>> results = (List<Map<String, Object>>) getHibernateTemplate().execute(callback);

        boolean removed = false;
        for (Map<String, Object> result : results)
        {
            DbAccessControlListMember member = (DbAccessControlListMember) result.get("member");
            if ((exclude != null) && excluder.matches(qnameDAO, result, depth))
            {
                getHibernateTemplate().delete(member);
                removed = true;
            }
        }
        if (removed)
        {
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
        }

    }

    /**
     * Helper to add ACEs to an ACL
     * 
     * @param acl
     * @param toAdd
     * @param depth
     */
    private void addAcesToAcl(DbAccessControlList acl, List<DbAccessControlEntry> toAdd, int depth)
    {
        if (toAdd != null)
        {
            for (DbAccessControlEntry add : toAdd)
            {
                DbAccessControlListMemberImpl newMember = new DbAccessControlListMemberImpl();
                newMember.setAccessControlList(acl);
                newMember.setAccessControlEntry(add);
                newMember.setPosition(depth);
                getHibernateTemplate().save(newMember);
            }
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
        }
    }

    private void replaceInherited(Long id, DbAccessControlList acl, List<DbAccessControlEntry> inherited, List<Integer> positions, int depth)
    {
        truncateInherited(id, depth);
        addInherited(acl, inherited, positions, depth);
    }

    @SuppressWarnings("unchecked")
    private void truncateInherited(final Long id, int depth)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_ACES_FOR_ACL);
                query.setParameter("id", id);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<DbAccessControlListMember> members = (List<DbAccessControlListMember>) getHibernateTemplate().execute(callback);

        boolean removed = false;
        for (DbAccessControlListMember member : members)
        {
            if (member.getPosition() > depth)
            {
                getHibernateTemplate().delete(member);
                removed = true;
            }
        }
        if (removed)
        {
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeInherited(final Long id, int depth)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_ACES_FOR_ACL);
                query.setParameter("id", id);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<DbAccessControlListMember> members = (List<DbAccessControlListMember>) getHibernateTemplate().execute(callback);

        boolean changed = false;
        for (DbAccessControlListMember member : members)
        {
            if (member.getPosition() == depth + 1)
            {
                getHibernateTemplate().delete(member);
                changed = true;
            }
            else if (member.getPosition() > (depth + 1))
            {
                member.setPosition(member.getPosition() - 1);
                changed = true;
            }
        }
        if (changed)
        {
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
        }
    }

    private void addInherited(DbAccessControlList acl, List<DbAccessControlEntry> inherited, List<Integer> positions, int depth)
    {
        if (inherited != null)
        {
            for (int i = 0; i < inherited.size(); i++)
            {
                DbAccessControlEntry add = inherited.get(i);
                Integer position = positions.get(i);

                DbAccessControlListMemberImpl newMember = new DbAccessControlListMemberImpl();
                newMember.setAccessControlList(acl);
                newMember.setAccessControlEntry(add);
                newMember.setPosition(position.intValue() + depth + 1);
                getHibernateTemplate().save(newMember);

            }
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
        }
    }

    @SuppressWarnings("unchecked")
    private void insertInherited(final Long id, DbAccessControlList acl, List<DbAccessControlEntry> inherited, List<Integer> positions, int depth)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_ACES_FOR_ACL);
                query.setParameter("id", id);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<DbAccessControlListMember> members = (List<DbAccessControlListMember>) getHibernateTemplate().execute(callback);

        boolean changed = false;
        for (DbAccessControlListMember member : members)
        {
            if (member.getPosition() > depth)
            {
                member.setPosition(member.getPosition() + 1);
                changed = true;
            }
        }
        if (changed)
        {
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
        }

        for (int i = 0; i < inherited.size(); i++)
        {
            DbAccessControlEntry add = inherited.get(i);
            Integer position = positions.get(i);

            DbAccessControlListMemberImpl newMember = new DbAccessControlListMemberImpl();
            newMember.setAccessControlList(acl);
            newMember.setAccessControlEntry(add);
            newMember.setPosition(position.intValue() + depth + 1);
            getHibernateTemplate().save(newMember);

        }
        DirtySessionMethodInterceptor.flushSession(getSession(), true);

    }

    /**
     * Used when deleting a user. No ACL is updated - the user has gone the aces and all related info is deleted.
     */
    @SuppressWarnings("unchecked")
    public List<AclChange> deleteAccessControlEntries(final String authority)
    {
        List<AclChange> acls = new ArrayList<AclChange>();
        Set<Long> aces = new HashSet<Long>();

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_ACES_AND_ACLS_BY_AUTHORITY);
                query.setParameter("authority", authority);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<Object[]> results = (List<Object[]>) getHibernateTemplate().execute(callback);

        // fix up members and extract acls and aces

        for (Object[] ids : results)
        {
            String authorityFound = (String) ids[3];
            if (authorityFound.equals(authority))
            {
                // Delete acl entry
                DbAccessControlListMember member = (DbAccessControlListMember) getHibernateTemplate().get(DbAccessControlListMemberImpl.class, (Long) ids[0]);
                Long aclId = ((Long) ids[1]);
                aclCache.remove(aclId);
                DbAccessControlList list = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, aclId);
                acls.add(new AclChangeImpl(aclId, aclId, list.getAclType(), list.getAclType()));
                getHibernateTemplate().delete(member);
                aces.add((Long) ids[2]);
            }
        }

        // remove ACEs

        for (Long id : aces)
        {
            // Delete acl entry
            DbAccessControlEntry ace = (DbAccessControlEntry) getHibernateTemplate().get(DbAccessControlEntryImpl.class, id);
            getHibernateTemplate().delete(ace);
        }

        // Tidy up any unreferenced ACEs

        callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_ACES_BY_AUTHORITY);
                query.setParameter("authority", authority);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<DbAccessControlEntry> unreferenced = (List<DbAccessControlEntry>) getHibernateTemplate().execute(callback);

        for (DbAccessControlEntry ace : unreferenced)
        {
            getHibernateTemplate().delete(ace);
        }

        // remove authority
        DbAuthority toRemove = getAuthority(authority, false);
        if (toRemove != null)
        {
            getHibernateTemplate().delete(toRemove);
        }

        // TODO: Remove affected ACLs from the cache
        DirtySessionMethodInterceptor.flushSession(getSession(), true);
        return acls;
    }

    @SuppressWarnings("unchecked")
    public void onDeleteAccessControlList(final long id)
    {
        // The acl has gone - remove any members it may have
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_ACES_FOR_ACL);
                query.setParameter("id", id);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<DbAccessControlListMember> members = (List<DbAccessControlListMember>) getHibernateTemplate().execute(callback);

        for (DbAccessControlListMember member : members)
        {
            getHibernateTemplate().delete(member);
        }
        DirtySessionMethodInterceptor.flushSession(getSession(), true);
        aclCache.remove(id);
    }

    @SuppressWarnings("unchecked")
    public List<AclChange> deleteAccessControlList(final Long id)
    {
        if (logger.isDebugEnabled())
        {
            HibernateCallback check = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Criteria criteria = getSession().createCriteria(NodeImpl.class, "node");
                    criteria.createAlias("node.accessControlList", "acl");
                    criteria.add(Restrictions.eq("acl.id", id));
                    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                    DirtySessionMethodInterceptor.setCriteriaFlushMode(session, criteria);
                    return criteria.list();
                }
            };
            List<Node> nodes = (List<Node>) getHibernateTemplate().execute(check);
            for (Node node : nodes)
            {
                logger.debug("Found " + node.getId() + " " + node.getUuid() + " " + node.getAccessControlList());
            }
        }

        List<AclChange> acls = new ArrayList<AclChange>();

        final DbAccessControlList acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, id);
        if (!acl.isLatest())
        {
            throw new UnsupportedOperationException("Old ALC versions can not be updated");
        }
        if (acl.getAclType() == ACLType.SHARED)
        {
            throw new UnsupportedOperationException("Delete is not supported for shared acls - they are deleted with the defining acl");
        }

        if ((acl.getAclType() == ACLType.DEFINING) || (acl.getAclType() == ACLType.LAYERED))
        {
            if ((acl.getInheritedAclId() != null) && (acl.getInheritedAclId() != -1))
            {
                final DbAccessControlList inherited = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, acl.getInheritedAclId());
                // Will remove from the cache
                getWritable(inherited.getId(), acl.getInheritsFrom(), null, null, null, true, acls, WriteMode.REMOVE_INHERITED);
                DbAccessControlList unusedInherited = null;
                for (AclChange change : acls)
                {
                    if (change.getBefore() == inherited.getId())
                    {
                        unusedInherited = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, change.getAfter());
                    }
                }

                final Long newId = unusedInherited.getId();
                HibernateCallback callback = new HibernateCallback()
                {
                    public Object doInHibernate(Session session)
                    {
                        Query query = session.getNamedQuery(QUERY_GET_ACLS_THAT_INHERIT_FROM_THIS_ACL);
                        query.setParameter("id", newId);
                        DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                        return query.list();
                    }
                };
                List<Long> inheritors = (List<Long>) getHibernateTemplate().execute(callback);
                for (Long nextId : inheritors)
                {
                    // Will remove from the cache
                    getWritable(nextId, acl.getInheritsFrom(), null, null, acl.getInheritsFrom(), true, acls, WriteMode.REMOVE_INHERITED);
                }

                callback = new HibernateCallback()
                {
                    public Object doInHibernate(Session session)
                    {
                        Query query = session.getNamedQuery(QUERY_GET_ACES_FOR_ACL);
                        query.setParameter("id", newId);
                        DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                        return query.list();
                    }
                };
                List<DbAccessControlListMember> members = (List<DbAccessControlListMember>) getHibernateTemplate().execute(callback);

                for (DbAccessControlListMember member : members)
                {
                    getHibernateTemplate().delete(member);
                }

                getHibernateTemplate().delete(unusedInherited);
                if (inherited.isVersioned())
                {
                    inherited.setLatest(Boolean.FALSE);
                }
                else
                {
                    getHibernateTemplate().delete(inherited);
                }
            }
        }
        else
        {
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(QUERY_GET_ACLS_THAT_INHERIT_FROM_THIS_ACL);
                    query.setParameter("id", id);
                    DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                    return query.list();
                }
            };
            List<Long> inheritors = (List<Long>) getHibernateTemplate().execute(callback);
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
        }
        else
        {
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(QUERY_GET_ACES_FOR_ACL);
                    query.setParameter("id", id);
                    DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                    return query.list();
                }
            };
            List<DbAccessControlListMember> members = (List<DbAccessControlListMember>) getHibernateTemplate().execute(callback);

            for (DbAccessControlListMember member : members)
            {
                getHibernateTemplate().delete(member);
            }

            getHibernateTemplate().delete(acl);
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
        }

        // remove the deleted acl from the cache
        aclCache.remove(id);
        acls.add(new AclChangeImpl(id, null, acl.getAclType(), null));
        return acls;
    }

    public List<AclChange> deleteLocalAccessControlEntries(Long id)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        SimpleAccessControlEntry pattern = new SimpleAccessControlEntry();
        pattern.setPosition(Integer.valueOf(0));
        // Will remove from the cache
        getWritable(id, null, Collections.singletonList(pattern), null, null, true, changes, WriteMode.COPY_UPDATE_AND_INHERIT);
        return changes;
    }

    public List<AclChange> deleteInheritedAccessControlEntries(Long id)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        SimpleAccessControlEntry pattern = new SimpleAccessControlEntry();
        pattern.setPosition(Integer.valueOf(-1));
        // Will remove from the cache
        getWritable(id, null, Collections.singletonList(pattern), null, null, true, changes, WriteMode.COPY_UPDATE_AND_INHERIT);
        return changes;
    }

    public List<AclChange> deleteAccessControlEntries(Long id, AccessControlEntry pattern)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        // Will remove from the cache
        getWritable(id, null, Collections.singletonList(pattern), null, null, true, changes, WriteMode.COPY_UPDATE_AND_INHERIT);
        return changes;
    }

    /**
     * Search for access control lists
     * 
     * @param pattern
     * @return the ids of the ACLs found
     */
    public Long[] findAccessControlList(AccessControlEntry pattern)
    {
        throw new UnsupportedOperationException();
    }
    
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
     * @param id
     * @return the access control list
     */
    @SuppressWarnings("unchecked")
    public AccessControlList getAccessControlListImpl(final Long id)
    {
        SimpleAccessControlList acl = new SimpleAccessControlList();
        AccessControlListProperties properties = getAccessControlListProperties(id);
        if (properties == null)
        {
            return null;
        }

        acl.setProperties(properties);

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_LOAD_ACL);
                query.setParameter("id", id);
                query.setCacheMode(CacheMode.IGNORE);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };
        List<Object[]> results = (List<Object[]>) getHibernateTemplate().execute(callback);

        List<AccessControlEntry> entries = new ArrayList<AccessControlEntry>(results.size());
        for (Object[] result : results)
        // for (DbAccessControlListMember member : members)
        {
            Boolean aceIsAllowed = (Boolean) result[0];
            Integer aceType = (Integer) result[1];
            String authority = (String) result[2];
            Long permissionId = (Long) result[3];
            Integer position = (Integer) result[4];

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
            DbPermission perm = (DbPermission) getSession().get(DbPermissionImpl.class, permissionId);
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

    public AccessControlListProperties getAccessControlListProperties(Long id)
    {
        DbAccessControlList acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, id);
        if (acl == null)
        {
            return null;
        }
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclId(acl.getAclId());
        properties.setAclType(acl.getAclType());
        properties.setAclVersion(acl.getAclVersion());
        properties.setInherits(acl.getInherits());
        properties.setLatest(acl.isLatest());
        properties.setVersioned(acl.isVersioned());
        properties.setId(id);
        return properties;
    }

    public Long getInheritedAccessControlList(Long id)
    {
        DbAccessControlList acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, id);
        if (acl.getAclType() == ACLType.OLD)
        {
            return null;
        }
        if ((acl.getInheritedAclId() != null) && (acl.getInheritedAclId() != -1))
        {
            return acl.getInheritedAclId();
        }

        if ((acl.getAclType() == ACLType.DEFINING) || (acl.getAclType() == ACLType.LAYERED))
        {
            List<AclChange> changes = new ArrayList<AclChange>();
            // created shared acl
            SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
            properties.setAclType(ACLType.SHARED);
            properties.setInherits(Boolean.TRUE);
            properties.setVersioned(acl.isVersioned());
            Long sharedId = createAccessControlListImpl(properties, null, null);
            @SuppressWarnings("unused")
            DbAccessControlList shared = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, sharedId);
            getWritable(sharedId, id, null, null, id, true, changes, WriteMode.ADD_INHERITED);
            acl.setInheritedAclId(sharedId);
            return sharedId;
        }
        else
        {
            acl.setInheritedAclId(acl.getId());
            return acl.getInheritedAclId();
        }
    }

    public List<AclChange> invalidateAccessControlEntries(final String authority)
    {
        throw new UnsupportedOperationException();
    }

    public List<AclChange> mergeInheritedAccessControlList(Long inherited, Long target)
    {

        // TODO: For now we do a replace - we could do an insert if both inherit from the same acl

        List<AclChange> changes = new ArrayList<AclChange>();

        DbAccessControlList targetAcl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, target);

        DbAccessControlList inheritedAcl = null;
        if (inherited != null)
        {
            inheritedAcl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, inherited);
        }
        else
        {
            // Assume we are just resetting it to inherit as before
            if (targetAcl.getInheritsFrom() != null)
            {
                inheritedAcl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, targetAcl.getInheritsFrom());
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
                        HibernateCallback callback = new HibernateCallback()
                        {
                            public Object doInHibernate(Session session)
                            {
                                Query query = session.getNamedQuery(QUERY_GET_LATEST_ACL_BY_ACLID);
                                query.setParameter("aclId", searchAclId);
                                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                                return query.uniqueResult();
                            }
                        };
                        Long actualInheritor = (Long) getHibernateTemplate().execute(callback);
                        inheritedAcl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, actualInheritor);
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

        DbAccessControlList test = inheritedAcl;
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
                test = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, test.getInheritsFrom());
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

    @SuppressWarnings("unchecked")
    public List<AclChange> setAccessControlEntry(final Long id, final AccessControlEntry ace)
    {
        DbAccessControlList target = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, id);
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
        DbAuthority authority = getAuthority(ace.getAuthority(), true);
        DbPermission permission = getPermission(ace.getPermission(), true);

        // Find context

        if (ace.getContext() != null)
        {
            throw new UnsupportedOperationException();
        }

        // Find ACE
        DbAccessControlEntry entry = getAccessControlEntry(permission, authority, ace, true);

        // Wire up
        // COW and remove any existing matches

        SimpleAccessControlEntry exclude = new SimpleAccessControlEntry();
        // match any access status
        exclude.setAceType(ace.getAceType());
        exclude.setAuthority(ace.getAuthority());
        exclude.setPermission(ace.getPermission());
        exclude.setPosition(0);
        List<DbAccessControlEntry> toAdd = new ArrayList<DbAccessControlEntry>(1);
        toAdd.add(entry);
        // Will remove from the cache
        getWritable(id, null, Collections.singletonList(exclude), toAdd, null, true, changes, WriteMode.COPY_UPDATE_AND_INHERIT);

        return changes;
    }

    private long getCrc(String str)
    {
        try
        {
            CRC32 crc = new CRC32();
            crc.update(str.getBytes("UTF-8"));
            return crc.getValue();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 encoding is not supported");
        }  
    }

    public List<AclChange> enableInheritance(Long id, Long parent)
    {
        List<AclChange> changes = new ArrayList<AclChange>();
        
        DbAccessControlList acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, id);

        switch (acl.getAclType())
        {
        case FIXED:
        case GLOBAL:
            throw new IllegalArgumentException("Fixed and global permissions can not inherit");
        case OLD:
            acl.setInherits(Boolean.TRUE);
            aclCache.remove(id);
            changes.add(new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType()));
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
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
                acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, changes.get(0).getAfter());
                acl.setInherits(Boolean.TRUE);
                DirtySessionMethodInterceptor.flushSession(getSession(), true);
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

    public List<AclChange> disableInheritance(Long id, boolean setInheritedOnAcl)
    {
        DbAccessControlList acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, id);
        List<AclChange> changes = new ArrayList<AclChange>(1);
        switch (acl.getAclType())
        {
        case FIXED:
        case GLOBAL:
            return Collections.<AclChange> singletonList(new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType()));
        case OLD:

            acl.setInherits(Boolean.FALSE);
            aclCache.remove(id);
            changes.add(new AclChangeImpl(id, id, acl.getAclType(), acl.getAclType()));
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
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

    public Long getCopy(Long toCopy, Long toInheritFrom, ACLCopyMode mode)
    {
        DbAccessControlList aclToCopy;
        Long inheritedId;
        DbAccessControlList aclToInheritFrom;
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
            aclToCopy = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, toCopy);
            aclToCopy.setRequiresVersion(true);
            aclCache.remove(toCopy);
            inheritedId = getInheritedAccessControlList(toCopy);
            if ((inheritedId != null) && (!inheritedId.equals(toCopy)))
            {
                DbAccessControlList inheritedAcl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, inheritedId);
                inheritedAcl.setRequiresVersion(true);
                aclCache.remove(inheritedId);
            }
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
            return toCopy;
        case REDIRECT:
            if ((toInheritFrom != null) && (toInheritFrom == toCopy))
            {
                return getInheritedAccessControlList(toInheritFrom);
            }
            aclToCopy = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, toCopy);
            aclToInheritFrom = null;
            if (toInheritFrom != null)
            {
                aclToInheritFrom = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, toInheritFrom);
            }

            switch (aclToCopy.getAclType())
            {
            case DEFINING:
                // This is not called on the redirecting node as only LAYERED change permissins when redirected
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
            aclToCopy = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, toCopy);
            aclToInheritFrom = null;
            if (toInheritFrom != null)
            {
                aclToInheritFrom = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, toInheritFrom);
            }

            switch (aclToCopy.getAclType())
            {
            case DEFINING:
                SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
                properties.setAclType(ACLType.DEFINING);
                properties.setInherits(aclToCopy.getInherits());
                // Accept default versioning
                Long id = createAccessControlList(properties);

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
                DirtySessionMethodInterceptor.flushSession(getSession(), true);
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

    public DbAccessControlList getDbAccessControlListCopy(Long toCopy, Long toInheritFrom, ACLCopyMode mode)
    {
        Long id = getCopy(toCopy, toInheritFrom, mode);
        if (id == null)
        {
            return null;
        }
        DbAccessControlList acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, id);
        return acl;
    }
    
    public List<Long> getAvmNodesByACL(final Long id)
    {

        List<Long> avmNodeIds = avmNodeDAO.getAVMNodesByAclId(id);
        return avmNodeIds;
    }
    
    @SuppressWarnings("unchecked")
    private List<AclChange> disableInheritanceImpl(Long id, boolean setInheritedOnAcl, DbAccessControlList acl)
    {
        List<AclChange> changes = new ArrayList<AclChange>();

        if (!acl.getInherits())
        {
            return Collections.<AclChange> emptyList();
        }
        // Manges caching
        getWritable(id, null, null, null, null, false, changes, WriteMode.COPY_ONLY);
        acl = (DbAccessControlList) getHibernateTemplate().get(DbAccessControlListImpl.class, changes.get(0).getAfter());
        final Long inheritsFrom = acl.getInheritsFrom();
        acl.setInherits(Boolean.FALSE);
        // Keep inherits from so we can reinstate if required
        // acl.setInheritsFrom(-1l);
        // Manges caching
        getWritable(acl.getId(), null, null, null, null, true, changes, WriteMode.TRUNCATE_INHERITED);

        // set Inherited - TODO: UNTESTED

        if ((inheritsFrom != null) && (inheritsFrom != -1) && setInheritedOnAcl)
        {
            HibernateCallback callback = new HibernateCallback()
            {
                public Object doInHibernate(Session session)
                {
                    Query query = session.getNamedQuery(QUERY_GET_ACES_FOR_ACL);
                    query.setParameter("id", inheritsFrom);
                    DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                    return query.list();
                }
            };
            List<DbAccessControlListMember> members = (List<DbAccessControlListMember>) getHibernateTemplate().execute(callback);

            for (DbAccessControlListMember member : members)
            {
                SimpleAccessControlEntry entry = new SimpleAccessControlEntry();
                entry.setAccessStatus(member.getAccessControlEntry().isAllowed() ? AccessStatus.ALLOWED : AccessStatus.DENIED);
                entry.setAceType(member.getAccessControlEntry().getAceType());
                entry.setAuthority(member.getAccessControlEntry().getAuthority().getAuthority());
                if (member.getAccessControlEntry().getContext() != null)
                {
                    SimpleAccessControlEntryContext context = new SimpleAccessControlEntryContext();
                    context.setClassContext(member.getAccessControlEntry().getContext().getClassContext());
                    context.setKVPContext(member.getAccessControlEntry().getContext().getKvpContext());
                    context.setPropertyContext(member.getAccessControlEntry().getContext().getPropertyContext());
                    entry.setContext(context);
                }
                DbPermission perm = member.getAccessControlEntry().getPermission();
                QName permTypeQName = qnameDAO.getQName(perm.getTypeQNameId()).getSecond(); // Has an ID so must exist
                SimplePermissionReference permissionRefernce = SimplePermissionReference.getPermissionReference(permTypeQName, perm.getName());
                entry.setPermission(permissionRefernce);
                entry.setPosition(Integer.valueOf(0));

                setAccessControlEntry(id, entry);
            }
        }
        DirtySessionMethodInterceptor.flushSession(getSession(), true);
        return changes;

    }

    private static final String RESOURCE_KEY_ACL_CHANGE_SET_ID = "hibernate.acl.change.set.id";

    /**
     * Support to get the current ACL change set and bind this to the transaction. So we only make one new version of an
     * ACL per change set. If something is in the current change set we can update it.
     */
    private DbAccessControlListChangeSet getCurrentChangeSet()
    {
        DbAccessControlListChangeSet changeSet = null;
        Serializable changeSetId = (Serializable) AlfrescoTransactionSupport.getResource(RESOURCE_KEY_ACL_CHANGE_SET_ID);
        if (changeSetId == null)
        {
            changeSet = new DbAccessControlListChangeSetImpl();
            changeSetId = getHibernateTemplate().save(changeSet);
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
            changeSet = (DbAccessControlListChangeSetImpl) getHibernateTemplate().get(DbAccessControlListChangeSetImpl.class, changeSetId);
            // bind the id
            AlfrescoTransactionSupport.bindResource(RESOURCE_KEY_ACL_CHANGE_SET_ID, changeSetId);
            if (logger.isDebugEnabled())
            {
                logger.debug("New change set = " + changeSetId);
            }
        }
        else
        {
            changeSet = (DbAccessControlListChangeSet) getHibernateTemplate().get(DbAccessControlListChangeSetImpl.class, changeSetId);
            if (logger.isDebugEnabled())
            {
                logger.debug("Existing change set = " + changeSetId);
            }
        }
        return changeSet;
    }

    private static class AcePatternMatcher
    {
        private List<? extends AccessControlEntry> patterns;

        AcePatternMatcher(List<? extends AccessControlEntry> patterns)
        {
            this.patterns = patterns;
        }

        boolean matches(QNameDAO qnameDAO, Map<String, Object> result, int position)
        {
            if (patterns == null)
            {
                return true;
            }

            DbAccessControlListMember member = (DbAccessControlListMember) result.get("member");
            DbAccessControlEntry entry = (DbAccessControlEntry) result.get("ace");

            for (AccessControlEntry pattern : patterns)
            {
                if (checkPattern(qnameDAO, result, position, member, entry, pattern))
                {
                    return true;
                }
            }
            return false;

        }

        /**
         * @param qnameDAO
         * @param result
         * @param position
         * @param member
         * @param entry
         * @return
         */
        private boolean checkPattern(QNameDAO qnameDAO, Map<String, Object> result, int position, DbAccessControlListMember member, DbAccessControlEntry entry,
                AccessControlEntry pattern)
        {
            if (pattern.getAccessStatus() != null)
            {
                if (pattern.getAccessStatus() != (entry.isAllowed() ? AccessStatus.ALLOWED : AccessStatus.DENIED))
                {
                    return false;
                }
            }

            if (pattern.getAceType() != null)
            {
                if (pattern.getAceType() != entry.getAceType())
                {
                    return false;
                }
            }

            if (pattern.getAuthority() != null)
            {
                DbAuthority authority = (DbAuthority) result.get("authority");
                if ((pattern.getAuthorityType() != AuthorityType.WILDCARD) && !pattern.getAuthority().equals(authority.getAuthority()))
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
                DbPermission permission = (DbPermission) result.get("permission");
                final QName patternQName = pattern.getPermission().getQName();
                final QName permTypeQName = qnameDAO.getQName(permission.getTypeQNameId()).getSecond(); // Has an ID so
                // must exist
                if ((patternQName != null) && (!patternQName.equals(permTypeQName)))
                {
                    return false;
                }
                final String patternName = pattern.getPermission().getName();
                if ((patternName != null) && (!patternName.equals(permission.getName())))
                {
                    return false;
                }
            }

            if (pattern.getPosition() != null)
            {
                if (pattern.getPosition().intValue() >= 0)
                {
                    if (member.getPosition() != position)
                    {
                        return false;
                    }
                }
                else if (pattern.getPosition().intValue() == -1)
                {
                    if (member.getPosition() <= position)
                    {
                        return false;
                    }
                }

            }
            return true;
        }
    }

    /**
     * Does this <tt>Session</tt> contain any changes which must be synchronized with the store?
     * 
     * @return true => changes are pending
     */
    public boolean isDirty()
    {
        // create a callback for the task
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                return session.isDirty();
            }
        };
        // execute the callback
        return ((Boolean) getHibernateTemplate().execute(callback)).booleanValue();
    }

    /**
     * NO-OP
     */
    public void beforeCommit()
    {
    }

    static class AclChangeImpl implements AclChange
    {
        private Long before;

        private Long after;

        private ACLType typeBefore;

        private ACLType typeAfter;

        AclChangeImpl(Long before, Long after, ACLType typeBefore, ACLType typeAfter)
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
     * Get the total number of head nodes in the repository
     * 
     * @return count
     */
    public Long getAVMHeadNodeCount()
    {
        try
        {
            Session session = getSession();
            int isolationLevel = session.connection().getTransactionIsolation();
            try
            {
                session.connection().setTransactionIsolation(1);
                Query query = getSession().getNamedQuery("permission.GetAVMHeadNodeCount");
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                Long answer = (Long) query.uniqueResult();
                return answer;
            }
            finally
            {
                session.connection().setTransactionIsolation(isolationLevel);
            }
        }
        catch (SQLException e)
        {
            throw new AlfrescoRuntimeException("Failed to set TX isolation level", e);
        }

    }

    /**
     * Get the max acl id
     * 
     * @return - max acl id
     */
    public Long getMaxAclId()
    {
        try
        {
            Session session = getSession();
            int isolationLevel = session.connection().getTransactionIsolation();
            try
            {
                session.connection().setTransactionIsolation(1);
                Query query = getSession().getNamedQuery("permission.GetMaxAclId");
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                Long answer = (Long) query.uniqueResult();
                return answer;
            }
            finally
            {
                session.connection().setTransactionIsolation(isolationLevel);
            }
        }
        catch (SQLException e)
        {
            throw new AlfrescoRuntimeException("Failed to set TX isolation level", e);
        }
    }

    /**
     * Does the underlyinf connection support isolation level 1 (dirty read)
     * 
     * @return true if we can do a dirty db read and so track changes (Oracle can not)
     */
    public boolean supportsProgressTracking()
    {
        try
        {
            Session session = getSession();
            return session.connection().getMetaData().supportsTransactionIsolationLevel(1);
        }
        catch (SQLException e)
        {
            return false;
        }

    }

    /**
     * Get the acl count canges so far for progress tracking
     * 
     * @param above
     * @return - the count
     */
    public Long getAVMNodeCountWithNewACLS(Long above)
    {
        try
        {
            Session session = getSession();
            int isolationLevel = session.connection().getTransactionIsolation();
            try
            {
                session.connection().setTransactionIsolation(1);
                Query query = getSession().getNamedQuery("permission.GetAVMHeadNodeCountWherePermissionsHaveChanged");
                query.setParameter("above", above);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                Long answer = (Long) query.uniqueResult();
                return answer;
            }
            finally
            {
                session.connection().setTransactionIsolation(isolationLevel);
            }
        }
        catch (SQLException e)
        {
            throw new AlfrescoRuntimeException("Failed to set TX isolation level", e);
        }
    }

    /**
     * How many nodes are noew in store (approximate)
     * 
     * @return - the number of new nodes - approximate
     */
    public Long getNewInStore()
    {
        Long count = patchDAO.getAVMNodesCountWhereNewInStore();
        return count;
    }

    /**
     * Find layered directories Used to improve performance during patching and cascading the effect of permission
     * changes between layers
     * 
     * @return - layered directories
     */
    public List<Indirection> getLayeredDirectories()
    {
        List<AVMNodeEntity> ldNodeEntities = avmNodeDAO.getAllLayeredDirectories();
        
        ArrayList<Indirection> indirections = new ArrayList<Indirection>(ldNodeEntities.size());
        
        for (AVMNodeEntity ldNodeEntity : ldNodeEntities)
        {

            Long from = ldNodeEntity.getId();
            String to = ldNodeEntity.getIndirection();
            Integer version = ldNodeEntity.getIndirectionVersion();
            indirections.add(new Indirection(from, to, version));
        }
        return indirections;
    }

    /**
     * Find layered files Used to improve performance during patching and cascading the effect of permission changes
     * between layers
     * 
     * @return - layered files
     */
    public List<Indirection> getLayeredFiles()
    {
        List<AVMNodeEntity> lfNodeEntities = avmNodeDAO.getAllLayeredFiles();
        
        ArrayList<Indirection> indirections = new ArrayList<Indirection>(lfNodeEntities.size());
        
        for (AVMNodeEntity lfNodeEntity : lfNodeEntities)
        {
            Long from = lfNodeEntity.getId();
            String to = lfNodeEntity.getIndirection();
            Integer version = lfNodeEntity.getIndirectionVersion();
            indirections.add(new Indirection(from, to, version));
        }
        return indirections;
    }

    public List<Indirection> getAvmIndirections()
    {
        List<Indirection> dirList = getLayeredDirectories();
        List<Indirection> fileList = getLayeredFiles();
        ArrayList<Indirection> answer = new ArrayList<Indirection>(dirList.size() + fileList.size());
        answer.addAll(dirList);
        answer.addAll(fileList);
        return answer;
    }

    public void flush()
    {
        getSession().flush();
    }

    /**
     * Support to describe AVM indirections for permission performance improvements when permissions are set.
     * 
     * @author andyh
     */
    public static class Indirection
    {
        Long from;

        String to;

        Integer toVersion;

        Indirection(Long from, String to, Integer toVersion)
        {
            this.from = from;
            this.to = to;
            this.toVersion = toVersion;
        }

        /**
         * @return - from id
         */
        public Long getFrom()
        {
            return from;
        }

        /**
         * @return - to id
         */
        public String getTo()
        {
            return to;
        }

        /**
         * @return - version
         */
        public Integer getToVersion()
        {
            return toVersion;
        }

    }

    /**
     * How many DM nodes are there?
     * 
     * @return - the count
     */
    public Long getDmNodeCount()
    {
        try
        {
            Session session = getSession();
            int isolationLevel = session.connection().getTransactionIsolation();
            try
            {
                session.connection().setTransactionIsolation(1);
                Query query = getSession().getNamedQuery("permission.GetDmNodeCount");
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                Long answer = (Long) query.uniqueResult();
                return answer;
            }
            finally
            {
                session.connection().setTransactionIsolation(isolationLevel);
            }
        }
        catch (SQLException e)
        {
            throw new AlfrescoRuntimeException("Failed to set TX isolation level", e);
        }

    }

    /**
     * How many DM nodes are three with new ACls (to track patch progress)
     * 
     * @param above
     * @return - the count
     */
    public Long getDmNodeCountWithNewACLS(Long above)
    {
        try
        {
            Session session = getSession();
            int isolationLevel = session.connection().getTransactionIsolation();
            try
            {
                session.connection().setTransactionIsolation(1);
                Query query = getSession().getNamedQuery("permission.GetDmNodeCountWherePermissionsHaveChanged");
                query.setParameter("above", above);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                Long answer = (Long) query.uniqueResult();
                return answer;
            }
            finally
            {
                session.connection().setTransactionIsolation(isolationLevel);
            }
        }
        catch (SQLException e)
        {
            throw new AlfrescoRuntimeException("Failed to set TX isolation level", e);
        }
    }

    public void updateAuthority(String before, String after)
    {
        DbAuthority dbAuthority = getAuthority(before, false);
        // If there is no entry and alias is not required - there is nothing it would match
        if (dbAuthority != null)
        {
            dbAuthority.setAuthority(after);
            dbAuthority.setCrc(getCrc(after));
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
            aclCache.clear();
        }
    }

    private DbAuthority getAuthority(final String authority, boolean create)
    {
        // Find auth
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_AUTHORITY);
                query.setParameter("authority", authority);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.list();
            }
        };

        DbAuthority dbAuthority = null;
        List<DbAuthority> authorities = (List<DbAuthority>) getHibernateTemplate().execute(callback);
        for (DbAuthority found : authorities)
        {
            if (found.getAuthority().equals(authority))
            {
                dbAuthority = found;
                break;
            }
        }
        if (create && (dbAuthority == null))
        {
            dbAuthority = createDbAuthority(authority);
        }
        return dbAuthority;
    }

    private DbPermission getPermission(final PermissionReference permissionReference, boolean create)
    {
        // Find permission

        final QName permissionQName = permissionReference.getQName();
        final String permissionName = permissionReference.getName();
        final Pair<Long, QName> permissionQNamePair = qnameDAO.getOrCreateQName(permissionQName);

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_PERMISSION);
                query.setParameter("permissionTypeQNameId", permissionQNamePair.getFirst());
                query.setParameter("permissionName", permissionName);
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        DbPermission dbPermission = (DbPermission) getHibernateTemplate().execute(callback);
        if (create && (dbPermission == null))
        {
            DbPermissionImpl newPermission = new DbPermissionImpl();
            newPermission.setTypeQNameId(permissionQNamePair.getFirst());
            newPermission.setName(permissionName);
            dbPermission = newPermission;
            getHibernateTemplate().save(newPermission);
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
        }
        return dbPermission;
    }

    private DbAccessControlEntry getAccessControlEntry(final DbPermission permission, final DbAuthority authority, final AccessControlEntry ace, boolean create)
    {

        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_ACE_WITH_NO_CONTEXT);
                query.setParameter("permissionId", permission.getId());
                query.setParameter("authorityId", authority.getId());
                query.setParameter("allowed", (ace.getAccessStatus() == AccessStatus.ALLOWED) ? true : false);
                query.setParameter("applies", ace.getAceType().getId());
                DirtySessionMethodInterceptor.setQueryFlushMode(session, query);
                return query.uniqueResult();
            }
        };
        DbAccessControlEntry entry = (DbAccessControlEntry) getHibernateTemplate().execute(callback);
        if (create && (entry == null))
        {
            DbAccessControlEntryImpl newEntry = new DbAccessControlEntryImpl();
            newEntry.setAceType(ace.getAceType());
            newEntry.setAllowed((ace.getAccessStatus() == AccessStatus.ALLOWED) ? true : false);
            newEntry.setAuthority(authority);
            newEntry.setPermission(permission);
            entry = newEntry;
            getHibernateTemplate().save(newEntry);
            DirtySessionMethodInterceptor.flushSession(getSession(), true);
        }
        return entry;
    }

    public void createAuthority(String authority)
    {
        createDbAuthority(authority);
    }

    public DbAuthority createDbAuthority(String authority)
    {
        DbAuthority dbAuthority = new DbAuthorityImpl();
        dbAuthority.setAuthority(authority);
        dbAuthority.setCrc(getCrc(authority));
        getHibernateTemplate().save(dbAuthority);
        DirtySessionMethodInterceptor.flushSession(getSession(), true);
        return dbAuthority;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.security.permissions.impl.AclDaoComponent#setAccessControlEntries(java.lang.Long,
     *      java.util.List)
     */
    public List<AclChange> setAccessControlEntries(Long id, List<AccessControlEntry> aces)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.security.permissions.impl.AclDaoComponent#createAccessControlList(org.alfresco.repo.security.permissions.AccessControlListProperties,
     *      java.util.List, long)
     */
    public Long createAccessControlList(AccessControlListProperties properties, List<AccessControlEntry> aces, Long inherited)
    {
        return createAccessControlListImpl(properties, aces, inherited);
    }

}
