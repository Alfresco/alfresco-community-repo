/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.domain.hibernate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.AccessControlListDAO;
import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.DbAuthority;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.DbPermissionKey;
import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.NodeStatus;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.security.permissions.impl.PermissionReferenceImpl;
import org.alfresco.repo.security.permissions.impl.PermissionsDaoComponent;
import org.alfresco.repo.security.permissions.impl.SimpleNodePermissionEntry;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.transaction.TransactionalDao;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Support for accessing persisted permission information. This class maps between persisted objects and the external
 * API defined in the PermissionsDAO interface.
 * 
 * @author andyh
 */
public class PermissionsDaoComponentImpl extends HibernateDaoSupport implements PermissionsDaoComponent,
        TransactionalDao
{
    private static final boolean INHERIT_PERMISSIONS_DEFAULT = true;

    public static final String QUERY_GET_PERMISSION = "permission.GetPermission";

    public static final String QUERY_GET_AC_ENTRIES_FOR_AUTHORITY = "permission.GetAccessControlEntriesForAuthority";

    public static final String QUERY_GET_ALL_AC_ENTRIES_FOR_AUTHORITY = "permission.GetAllAccessControlEntriesForAuthority";

    public static final String QUERY_GET_AC_ENTRIES_FOR_PERMISSION = "permission.GetAccessControlEntriesForPermission";

    public static final String QUERY_FIND_NODES_BY_PERMISSION = "permission.FindNodesByPermission";

    private Map<String, AccessControlListDAO> fProtocolToACLDAO;

    private AccessControlListDAO fDefaultACLDAO;

    /** a uuid identifying this unique instance */
    private String uuid;

    /**
     * 
     */
    public PermissionsDaoComponentImpl()
    {
        this.uuid = GUID.generate();
    }

    /**
     * Checks equality by type and uuid
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof PermissionsDaoComponentImpl))
        {
            return false;
        }
        PermissionsDaoComponentImpl that = (PermissionsDaoComponentImpl) obj;
        return this.uuid.equals(that.uuid);
    }

    /**
     * @see #uuid
     */
    public int hashCode()
    {
        return uuid.hashCode();
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
     * Just flushes the session
     */
    public void flush()
    {
        getSession().flush();
    }

    public void setProtocolToACLDAO(Map<String, AccessControlListDAO> map)
    {
        fProtocolToACLDAO = map;
    }

    public void setDefaultACLDAO(AccessControlListDAO defaultACLDAO)
    {
        fDefaultACLDAO = defaultACLDAO;
    }

    public NodePermissionEntry getPermissions(NodeRef nodeRef)
    {
        // Create the object if it is not found.
        // Null objects are not cached in hibernate
        // If the object does not exist it will repeatedly query to check its
        // non existence.
        NodePermissionEntry npe = null;
        DbAccessControlList acl = null;
        try
        {
            acl = getAccessControlList(nodeRef, false);
        }
        catch (InvalidNodeRefException e)
        {
            // Do nothing.
        }
        if (acl == null)
        {
            // there isn't an access control list for the node - spoof a null one
            SimpleNodePermissionEntry snpe = new SimpleNodePermissionEntry(nodeRef, true, Collections
                    .<SimplePermissionEntry> emptySet());
            npe = snpe;
        }
        else
        {
            npe = createSimpleNodePermissionEntry(nodeRef);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created access control list for node: \n" + "   node: " + nodeRef + "\n" + "   acl: " + npe);
        }
        return npe;
    }

    /**
     * Get the persisted access control list or create it if required.
     * 
     * @param nodeRef -
     *            the node for which to create the list
     * @param create -
     *            create the object if it is missing
     * @return Returns the current access control list or null if not found
     */
    private DbAccessControlList getAccessControlList(NodeRef nodeRef, boolean create)
    {
        DbAccessControlList acl = getACLDAO(nodeRef).getAccessControlList(nodeRef);
        if (acl == null && create)
        {
            acl = createAccessControlList(nodeRef);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved access control list: \n" + "   node: " + nodeRef + "\n" + "   list: " + acl);
        }
        return acl;
    }

    /**
     * Creates an access control list for the node and removes the entry from the nullPermsionCache.
     */
    private DbAccessControlList createAccessControlList(NodeRef nodeRef)
    {
        DbAccessControlList acl = new DbAccessControlListImpl();
        acl.setInherits(INHERIT_PERMISSIONS_DEFAULT);
        getHibernateTemplate().save(acl);

        // maintain inverse
        getACLDAO(nodeRef).setAccessControlList(nodeRef, acl);

        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created Access Control List: \n" + "   node: " + nodeRef + "\n" + "   list: " + acl);
        }
        return acl;
    }

    public void deletePermissions(NodeRef nodeRef)
    {
        DbAccessControlList acl = null;
        try
        {
            acl = getAccessControlList(nodeRef, false);
        }
        catch (InvalidNodeRefException e)
        {
            return;
        }
        if (acl != null)
        {
            // maintain referencial integrity
            getACLDAO(nodeRef).setAccessControlList(nodeRef, null);
            // delete the access control list - it will cascade to the entries
            getHibernateTemplate().delete(acl);
        }
    }

    @SuppressWarnings("unchecked")
    public void deletePermissions(final String authority)
    {
        // get the authority
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_AC_ENTRIES_FOR_AUTHORITY).setString("authorityRecipient",
                        authority);
                return (Integer) HibernateHelper.deleteDbAccessControlEntries(session, query);
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
        DbAccessControlList acl = null;
        try
        {
            acl = getACLDAO(nodeRef).getAccessControlList(nodeRef);
        }
        catch (InvalidNodeRefException e)
        {
            return;
        }
        int deletedCount = 0;
        if (acl != null)
        {
            deletedCount = acl.deleteEntriesForAuthority(authority);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted "
                    + deletedCount + "entries for criteria: \n" + "   node: " + nodeRef + "\n" + "   authority: "
                    + authority);
        }
    }

    /**
     * Deletes all permission entries (access control list entries) that match the given criteria. Note that the access
     * control list for the node is not deleted.
     */
    public void deletePermission(NodeRef nodeRef, String authority, PermissionReference permission)
    {
        DbAccessControlList acl = null;
        try
        {
            acl = getACLDAO(nodeRef).getAccessControlList(nodeRef);
        }
        catch (InvalidNodeRefException e)
        {
            return;
        }
        int deletedCount = 0;
        if (acl != null)
        {
            DbPermissionKey permissionKey = new DbPermissionKey(permission.getQName(), permission.getName());
            deletedCount = acl.deleteEntry(authority, permissionKey);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted "
                    + deletedCount + "entries for criteria: \n" + "   node: " + nodeRef + "\n" + "   permission: "
                    + permission + "\n" + "   authority: " + authority);
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
            entry = dbAccessControlList.newEntry(dbPermission, dbAuthority, allow);
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
     * @param nodeRef
     *            the node against which to join
     * @param authority
     *            the authority against which to join
     * @param perm
     *            the permission against which to join
     * @return Returns all access control entries that match the criteria
     */
    private DbAccessControlEntry getAccessControlEntry(NodeRef nodeRef, String authority, PermissionReference permission)
    {
        DbAccessControlList acl = getAccessControlList(nodeRef, false);
        DbAccessControlEntry entry = null;
        if (acl != null)
        {
            DbPermissionKey permissionKey = new DbPermissionKey(permission.getQName(), permission.getName());
            entry = acl.getEntry(authority, permissionKey);
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug(""
                    + (entry == null ? "Did not find" : "Found") + " entry for criteria: \n" + "   node: " + nodeRef
                    + "\n" + "   authority: " + authority + "\n" + "   permission: " + permission);
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
        Session session = getSession();

        DbPermission dbPermission = DbPermissionImpl.find(session, qname, name);

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
        setPermission(permissionEntry.getNodeRef(), permissionEntry.getAuthority(), permissionEntry
                .getPermissionReference(), permissionEntry.isAllowed());
    }

    public void setPermission(NodePermissionEntry nodePermissionEntry)
    {
        NodeRef nodeRef = nodePermissionEntry.getNodeRef();

        // Get the access control list
        // Note the logic here requires to know whether it was created or not
        DbAccessControlList acl = getAccessControlList(nodeRef, false);
        if (acl != null)
        {
            // maintain referencial integrity
            getACLDAO(nodeRef).setAccessControlList(nodeRef, null);
            // drop the list
            getHibernateTemplate().delete(acl);
        }
        // create the access control list
        acl = createAccessControlList(nodeRef);

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

            @SuppressWarnings("unused")
            DbAccessControlEntryImpl entry = acl.newEntry(permissionEntity, authorityEntity, isAllowed);
        }
    }

    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions)
    {
        DbAccessControlList acl = null;
        if (!inheritParentPermissions)
        {
            // Inheritance == true is the default, so only force a create of the ACL if the value false
            acl = getAccessControlList(nodeRef, true);
            acl.setInherits(false);
        }
        else
        {
            acl = getAccessControlList(nodeRef, false);
            if (acl != null)
            {
                acl.setInherits(true);
            }
        }
    }

    public boolean getInheritParentPermissions(NodeRef nodeRef)
    {
        DbAccessControlList acl = null;
        try
        {
            acl = getAccessControlList(nodeRef, false);
        }
        catch (InvalidNodeRefException e)
        {
            return INHERIT_PERMISSIONS_DEFAULT;
        }
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

    private SimpleNodePermissionEntry createSimpleNodePermissionEntry(NodeRef nodeRef)
    {
        DbAccessControlList acl = getACLDAO(nodeRef).getAccessControlList(nodeRef);
        if (acl == null)
        {
            // there isn't an access control list for the node - spoof a null one
            SimpleNodePermissionEntry snpe = new SimpleNodePermissionEntry(nodeRef, true, Collections
                    .<SimplePermissionEntry> emptySet());
            return snpe;
        }
        else
        {
            Set<DbAccessControlEntry> entries = acl.getEntries();
            SimpleNodePermissionEntry snpe = new SimpleNodePermissionEntry(nodeRef, acl.getInherits(),
                    createSimplePermissionEntries(nodeRef, entries));
            return snpe;
        }
    }

    /**
     * @param entries
     *            access control entries
     * @return Returns a unique set of entries that can be given back to the outside world
     */
    private Set<SimplePermissionEntry> createSimplePermissionEntries(NodeRef nodeRef,
            Collection<DbAccessControlEntry> entries)
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
                spes.add(createSimplePermissionEntry(nodeRef, entry));
            }
        }
        return spes;
    }

    private static SimplePermissionEntry createSimplePermissionEntry(NodeRef nodeRef, DbAccessControlEntry ace)
    {
        if (ace == null)
        {
            return null;
        }
        return new SimplePermissionEntry(nodeRef, createSimplePermissionReference(ace.getPermission()), ace
                .getAuthority().getRecipient(), ace.isAllowed() ? AccessStatus.ALLOWED : AccessStatus.DENIED);
    }

    private static SimplePermissionReference createSimplePermissionReference(DbPermission perm)
    {
        if (perm == null)
        {
            return null;
        }
        return new SimplePermissionReference(perm.getTypeQname(), perm.getName());
    }

    /**
     * Helper to choose appropriate NodeService for the given NodeRef
     * 
     * @param nodeRef
     *            The NodeRef to dispatch from.
     * @return The appropriate NodeService.
     */
    private AccessControlListDAO getACLDAO(NodeRef nodeRef)
    {
        AccessControlListDAO ret = fProtocolToACLDAO.get(nodeRef.getStoreRef().getProtocol());
        if (ret == null)
        {
            return fDefaultACLDAO;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public Map<NodeRef, Set<AccessPermission>> getAllSetPermissions(final String authority)
    {
        // get the authority
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_GET_ALL_AC_ENTRIES_FOR_AUTHORITY).setString(
                        "authorityRecipient", authority);

                Map<NodeRef, Set<AccessPermission>> result = new HashMap<NodeRef, Set<AccessPermission>>();

                ScrollableResults entities = query.scroll(ScrollMode.FORWARD_ONLY);
                while (entities.next())
                {
                    DbAccessControlEntry entry = (DbAccessControlEntry) entities.get(0);
                    // DbAccessControlList acl = (DbAccessControlList) entities.get(1);
                    Node node = (Node) entities.get(2);
                    DbPermission dbPermission = entry.getPermission();
                    PermissionReferenceImpl pr = new PermissionReferenceImpl(dbPermission.getTypeQname(), dbPermission
                            .getName());
                    AccessStatus accessStatus = entry.isAllowed() ? AccessStatus.ALLOWED : AccessStatus.DENIED;
                    AccessPermission ap = new AccessPermissionImpl(pr.toString(), accessStatus, entry.getAuthority()
                            .getRecipient());
                    NodeRef nodeRef = node.getNodeRef();
                    Set<AccessPermission> nodeSet = result.get(nodeRef);
                    if (nodeSet == null)
                    {
                        nodeSet = new HashSet<AccessPermission>();
                        result.put(nodeRef, nodeSet);
                    }
                    nodeSet.add(ap);
                }

                return result;
            }
        };
        return (Map<NodeRef, Set<AccessPermission>>) getHibernateTemplate().execute(callback);

    }

    public Set<NodeRef> findNodeByPermission(final String authority, final PermissionReference permission, final boolean allow)
    {
        // get the authority
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(QUERY_FIND_NODES_BY_PERMISSION).setString(
                        "authorityRecipient", authority).setBoolean("allow", allow).setString("permissionName", permission.getName()).setString("permissionTypeQname", permission.getQName().toString());

                Set<NodeRef> result = new HashSet<NodeRef>();

                ScrollableResults entities = query.scroll(ScrollMode.FORWARD_ONLY);
                while (entities.next())
                {
                    DbAccessControlEntry entry = (DbAccessControlEntry) entities.get(0);
                    // DbAccessControlList acl = (DbAccessControlList) entities.get(1);
                    Node node = (Node) entities.get(2);
                    DbPermission dbPermission = entry.getPermission();
                    PermissionReferenceImpl pr = new PermissionReferenceImpl(dbPermission.getTypeQname(), dbPermission
                            .getName());
                    AccessStatus accessStatus = entry.isAllowed() ? AccessStatus.ALLOWED : AccessStatus.DENIED;
                    AccessPermission ap = new AccessPermissionImpl(pr.toString(), accessStatus, entry.getAuthority()
                            .getRecipient());
                    NodeRef nodeRef = node.getNodeRef();
                    result.add(nodeRef);
                }

                return result;
            }
        };
        return (Set<NodeRef>) getHibernateTemplate().execute(callback);
    }
}
