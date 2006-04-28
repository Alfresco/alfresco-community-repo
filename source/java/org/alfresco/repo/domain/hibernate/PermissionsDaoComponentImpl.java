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
package org.alfresco.repo.domain.hibernate;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.DbAuthority;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.node.db.NodeDaoService;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.PermissionsDaoComponent;
import org.alfresco.repo.security.permissions.impl.SimpleNodePermissionEntry;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Support for accessing persisted permission information.
 * 
 * This class maps between persisted objects and the external API defined in the
 * PermissionsDAO interface.
 * 
 * @author andyh
 */
public class PermissionsDaoComponentImpl extends HibernateDaoSupport implements PermissionsDaoComponent
{
    public static final String QUERY_GET_PERMISSION = "permission.GetPermission";
    public static final String QUERY_GET_AC_LIST_FOR_NODE = "permission.GetAccessControlListForNode";
    public static final String QUERY_GET_AC_ENTRIES_FOR_AC_LIST = "permission.GetAccessControlEntriesForAccessControlList";
    public static final String QUERY_GET_AC_ENTRIES_FOR_AUTHORITY = "permission.GetAccessControlEntriesForAuthority";
    public static final String QUERY_GET_AC_ENTRIES_FOR_PERMISSION = "permission.GetAccessControlEntriesForPermission";
    public static final String QUERY_GET_AC_ENTRIES_FOR_AUTHORITY_AND_NODE = "permission.GetAccessControlEntriesForAuthorityAndNode";
    public static final String QUERY_GET_AC_ENTRY_FOR_ALL = "permission.GetAccessControlEntryForAll";
    
    private NodeDaoService nodeDaoService;
    private SimpleCache<NodeRef, SimpleNodePermissionEntry> nullPermissionCache;

    public PermissionsDaoComponentImpl()
    {
        super();
    }

    public void setNodeDaoService(NodeDaoService nodeDaoService)
    {
        this.nodeDaoService = nodeDaoService;
    }

    public void setNullPermissionCache(SimpleCache<NodeRef, SimpleNodePermissionEntry> nullPermissionCache)
    {
        this.nullPermissionCache = nullPermissionCache;
    }

    public NodePermissionEntry getPermissions(NodeRef nodeRef)
    {
        // Create the object if it is not found.
        // Null objects are not cached in hibernate
        // If the object does not exist it will repeatedly query to check its
        // non existence.

        NodePermissionEntry npe = nullPermissionCache.get(nodeRef);
        if (npe != null)
        {
            return npe;
        }
        // get the persisted version
        DbAccessControlList acl = getAccessControlList(nodeRef, false);
        if (acl == null)
        {
            // there isn't an access control list for the node - spoof a null one
            SimpleNodePermissionEntry snpe = new SimpleNodePermissionEntry(
                    nodeRef, true, Collections.<SimplePermissionEntry> emptySet());
            npe = snpe;
            nullPermissionCache.put(nodeRef, snpe);
        }
        else
        {
            npe = createSimpleNodePermissionEntry(acl);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created access control list for node: " + nodeRef);
        }
        return npe;
    }

    /**
     * Get the persisted access control list or create it if required.
     * 
     * @param nodeRef - the node for which to create the list
     * @param create - create the object if it is missing
     * @return Returns the current access control list or null if not found
     */
    private DbAccessControlList getAccessControlList(final NodeRef nodeRef, boolean create)
    {
        // get the access control list for the node
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(PermissionsDaoComponentImpl.QUERY_GET_AC_LIST_FOR_NODE);
                query.setString("storeProtocol", nodeRef.getStoreRef().getProtocol())
                     .setString("storeIdentifier", nodeRef.getStoreRef().getIdentifier())
                     .setString("nodeUuid", nodeRef.getId());
                return query.list();
            }
        };
        @SuppressWarnings("unchecked")
        List<DbAccessControlList> results = (List<DbAccessControlList>) getHibernateTemplate().execute(callback);
        DbAccessControlList acl = null;
        if (results.size() == 0)
        {
            // we'll return null
        }
        else if (results.size() > 0)
        {
            acl = (DbAccessControlList) results.get(0);
        }
        else if (results.size() > 1)
        {
            logger.warn("Duplicate access control lists for node: " + nodeRef);
            acl = (DbAccessControlList) results.get(0);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved access control list: \n" +
                    "   node: " + nodeRef + "\n" +
                    "   list: " + acl);
        }
        return acl;
    }
    
    /**
     * Creates an access control list for the node and removes the entry from
     * the nullPermsionCache.
     */
    private DbAccessControlList createAccessControlList(final NodeRef nodeRef)
    {
        // get the node referenced
        Node node = getNode(nodeRef);
        
        DbAccessControlList acl = new DbAccessControlListImpl();
        acl.setNode(node);
        acl.setInherits(true);
        getHibernateTemplate().save(acl);
        
        nullPermissionCache.remove(nodeRef);
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created Access Control List: \n" +
                    "   node: " + nodeRef + "\n" +
                    "   list: " + acl);
        }
        return acl;
    }

    /**
     * @param nodeRef the node reference
     * @return Returns the node for the given reference, or null
     */
    private Node getNode(NodeRef nodeRef)
    {
        Node node = nodeDaoService.getNode(nodeRef);
        if (node == null)
        {
            throw new InvalidNodeRefException(nodeRef);
        }
        return node;
    }

    public void deletePermissions(NodeRef nodeRef)
    {
        DbAccessControlList acl = getAccessControlList(nodeRef, false);
        if (acl != null)
        {
            // delete the access control list - it will cascade to the entries
            getHibernateTemplate().delete(acl);
        }
    }

    @SuppressWarnings("unchecked")
    public void deletePermissions(final String authority)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                        .getNamedQuery(QUERY_GET_AC_ENTRIES_FOR_AUTHORITY)
                        .setString("authorityRecipient", authority);
                return (Integer) HibernateHelper.deleteQueryResults(session, query);
            }
        };
        Integer deletedCount = (Integer) getHibernateTemplate().execute(callback);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + deletedCount + " entries for authority " + authority);
        }
    }

    public void deletePermissions(final NodeRef nodeRef, final String authority)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                        .getNamedQuery(QUERY_GET_AC_ENTRIES_FOR_AUTHORITY_AND_NODE)
                        .setString("authorityRecipient", authority)
                        .setString("storeProtocol", nodeRef.getStoreRef().getProtocol())
                        .setString("storeIdentifier", nodeRef.getStoreRef().getIdentifier())
                        .setString("nodeUuid", nodeRef.getId());
                return HibernateHelper.deleteQueryResults(session, query);
            }
        };
        Integer deletedCount = (Integer) getHibernateTemplate().execute(callback);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted " + deletedCount + "entries for criteria: \n" +
                    "   node: " + nodeRef + "\n" +
                    "   authority: " + authority);
        }
    }

    /**
     * Deletes all permission entries (access control list entries) that match
     * the given criteria.  Note that the access control list for the node is
     * not deleted.
     */
    public void deletePermission(final NodeRef nodeRef, final String authority, final PermissionReference permission)
    {
        // get the entry
        DbAccessControlEntry entry = getAccessControlEntry(nodeRef, authority, permission);
        if (entry != null)
        {
            getHibernateTemplate().delete(entry);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted entry for criteria: \n" +
                        "   node: " + nodeRef + "\n" +
                        "   authority: " + authority + "\n" +
                        "   permission: " + permission);
            }
        }
    }

    public void setPermission(NodeRef nodeRef, String authority, PermissionReference permission, boolean allow)
    {
        // get the entry
        DbAccessControlEntry entry = getAccessControlEntry(nodeRef, authority, permission);
        if (entry == null)
        {
            // need to create it
            DbAccessControlList dbAccessControlList = getAccessControlList(nodeRef, true);
            DbPermission dbPermission = getPermission(permission, true);
            DbAuthority dbAuthority = getAuthority(authority, true);
            // set persistent objects
            entry = DbAccessControlEntryImpl.create(dbAccessControlList, dbPermission, dbAuthority, allow);
            // save it
            getHibernateTemplate().save(entry);
            // drop the entry from the null cache
            nullPermissionCache.remove(nodeRef);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Created new access control entry: " + entry);
            }
        }
        else
        {
            entry.setAllowed(allow);
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Updated access control entry: " + entry);
            }
        }
    }
    
    /**
     * @param nodeRef the node against which to join
     * @param authority the authority against which to join
     * @param perm the permission against which to join
     * @return Returns all access control entries that match the criteria
     */
    private DbAccessControlEntry getAccessControlEntry(
            final NodeRef nodeRef,
            final String authority,
            final PermissionReference permission)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session
                        .getNamedQuery(QUERY_GET_AC_ENTRY_FOR_ALL)
                        .setString("permissionTypeQName", permission.getQName().toString())
                        .setString("permissionName", permission.getName())
                        .setString("authorityRecipient", authority)
                        .setString("storeProtocol", nodeRef.getStoreRef().getProtocol())
                        .setString("storeIdentifier", nodeRef.getStoreRef().getIdentifier())
                        .setString("nodeUuid", nodeRef.getId());
                return (DbAccessControlEntry) query.uniqueResult();
            }
        };
        DbAccessControlEntry entry = (DbAccessControlEntry) getHibernateTemplate().execute(callback);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("" + (entry == null ? "Did not find" : "Found") + "entry for criteria: \n" +
                    "   node: " + nodeRef + "\n" +
                    "   authority: " + authority + "\n" +
                    "   permission: " + permission);
        }
        return entry;
    }

    /**
     * Utility method to find or create a persisted authority
     */
    private DbAuthority getAuthority(String authority, boolean create)
    {
        DbAuthority entity = (DbAuthority) getHibernateTemplate().get(DbAuthorityImpl.class, authority);
        if ((entity == null) && create)
        {
            entity = new DbAuthorityImpl();
            entity.setRecipient(authority);
            getHibernateTemplate().save(entity);
            return entity;
        }
        else
        {
            return entity;
        }
    }

    /**
     * Utility method to find and optionally create a persisted permission.
     */
    private DbPermission getPermission(PermissionReference permissionRef, final boolean create)
    {
        final QName qname = permissionRef.getQName();
        final String name = permissionRef.getName();
        
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                return DbPermissionImpl.find(session, qname, name);
            }
        };
        DbPermission dbPermission = (DbPermission) getHibernateTemplate().execute(callback);

        // create if necessary
        if ((dbPermission == null) && create)
        {
            dbPermission = new DbPermissionImpl();
            dbPermission.setTypeQname(qname);
            dbPermission.setName(name);
            getHibernateTemplate().save(dbPermission);
        }
        return dbPermission;
    }

    public void setPermission(PermissionEntry permissionEntry)
    {
        setPermission(
                permissionEntry.getNodeRef(),
                permissionEntry.getAuthority(),
                permissionEntry.getPermissionReference(),
                permissionEntry.isAllowed());
    }

    public void setPermission(NodePermissionEntry nodePermissionEntry)
    {
        NodeRef nodeRef = nodePermissionEntry.getNodeRef();
        // get the access control list
        DbAccessControlList acl = getAccessControlList(nodeRef, false);
        if (acl == null)
        {
            // create the access control list
            acl = createAccessControlList(nodeRef);
        }
        else
        {
            // remove entries associated with the list
            int deleted = acl.deleteEntries();
            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted " + deleted + " entries for access control list: \n" +
                        "   acl: " + acl);
            }
            getSession().flush();
        }
        // set attributes
        acl.setInherits(nodePermissionEntry.inheritPermissions());

        // add all entries
        for (PermissionEntry pe : nodePermissionEntry.getPermissionEntries())
        {
            PermissionReference permission = pe.getPermissionReference();
            String authority = pe.getAuthority();
            boolean isAllowed = pe.isAllowed();

            DbPermission permissionEntity = getPermission(permission, true);
            DbAuthority authorityEntity = getAuthority(authority, true);

            DbAccessControlEntryImpl entry = DbAccessControlEntryImpl.create(
                    acl,
                    permissionEntity,
                    authorityEntity,
                    isAllowed);
                                                                                                                                                                                                                                                    getHibernateTemplate().save(entry);
        }
    }

    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions)
    {
        DbAccessControlList acl = getAccessControlList(nodeRef, true);
        acl.setInherits(inheritParentPermissions);
    }
    
    public boolean getInheritParentPermissions(NodeRef nodeRef)
    {
        DbAccessControlList acl = getAccessControlList(nodeRef, false);
        if (acl == null)
        {
            return true;
        }
        else
        {
            return acl.getInherits();
        }
    }

    // Utility methods to create simple detached objects for the outside world
    // We do not pass out the hibernate objects

    private SimpleNodePermissionEntry createSimpleNodePermissionEntry(DbAccessControlList acl)
    {
        if (acl == null)
        {
            ParameterCheck.mandatory("acl", acl);
        }
        List<DbAccessControlEntry> entries = getEntriesForList(acl);
        SimpleNodePermissionEntry snpe = new SimpleNodePermissionEntry(
                acl.getNode().getNodeRef(),
                acl.getInherits(),
                createSimplePermissionEntries(entries));
        return snpe;
    }
    
    /**
     * Executes a query to retrieve the access control list's entries
     * 
     * @param acl the access control list
     * @return Returns a list of the entries
     */
    @SuppressWarnings("unchecked")
    private List<DbAccessControlEntry> getEntriesForList(final DbAccessControlList acl)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_AC_ENTRIES_FOR_AC_LIST);
                query.setLong("accessControlListId", acl.getId());
                return query.list();
            }
        };
        List<DbAccessControlEntry> entries = (List<DbAccessControlEntry>) getHibernateTemplate().execute(callback);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Found " + entries.size() + " entries for access control list " + acl.getId());
        }
        return entries;
    }

    /**
     * @param entries access control entries
     * @return Returns a unique set of entries that can be given back to the outside world
     */
    private Set<SimplePermissionEntry> createSimplePermissionEntries(List<DbAccessControlEntry> entries)
    {
        if (entries == null)
        {
            return null;
        }
        HashSet<SimplePermissionEntry> spes = new HashSet<SimplePermissionEntry>(entries.size(), 1.0f);
        if (entries.size() != 0)
        {
            for (DbAccessControlEntry entry : entries)
            {
                spes.add(createSimplePermissionEntry(entry));
            }
        }
        return spes;
    }

    private static SimplePermissionEntry createSimplePermissionEntry(DbAccessControlEntry ace)
    {
        if (ace == null)
        {
            return null;
        }
        return new SimplePermissionEntry(
                ace.getAccessControlList().getNode().getNodeRef(),
                createSimplePermissionReference(ace.getPermission()),
                ace.getAuthority().getRecipient(),
                ace.isAllowed() ? AccessStatus.ALLOWED : AccessStatus.DENIED);
    }

    private static SimplePermissionReference createSimplePermissionReference(DbPermission perm)
    {
        if (perm == null)
        {
            return null;
        }
        return new SimplePermissionReference(
                perm.getTypeQname(),
                perm.getName());
    }
}
