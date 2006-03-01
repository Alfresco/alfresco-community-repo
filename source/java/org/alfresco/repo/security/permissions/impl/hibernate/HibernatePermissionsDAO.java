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
package org.alfresco.repo.security.permissions.impl.hibernate;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.NodeKey;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.PermissionsDAO;
import org.alfresco.repo.security.permissions.impl.SimpleNodePermissionEntry;
import org.alfresco.repo.security.permissions.impl.SimplePermissionEntry;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.hibernate.ObjectDeletedException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
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
public class HibernatePermissionsDAO extends HibernateDaoSupport implements PermissionsDAO
{
    private SimpleCache<NodeRef, SimpleNodePermissionEntry> nullPermissionCache;

    public HibernatePermissionsDAO()
    {
        super();

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

        npe = createSimpleNodePermissionEntry(getHibernateNodePermissionEntry(nodeRef, false));
        if (npe == null)
        {
            SimpleNodePermissionEntry snpe = new SimpleNodePermissionEntry(nodeRef, true, Collections
                    .<SimplePermissionEntry> emptySet());
            npe = snpe;
            nullPermissionCache.put(nodeRef, snpe);
        }
        return npe;
    }

    /**
     * Get the persisted NodePermissionEntry
     * 
     * @param nodeRef
     * @param create -
     *            create the object if it is missing
     * @return
     */
    private org.alfresco.repo.security.permissions.impl.hibernate.NodePermissionEntry getHibernateNodePermissionEntry(
            NodeRef nodeRef, boolean create)
    {
        // Build the key
        NodeKey nodeKey = getNodeKey(nodeRef);
        try
        {
            Object obj = getHibernateTemplate().get(NodePermissionEntryImpl.class, nodeKey);
            // Create if required
            if ((obj == null) && create)
            {
                NodePermissionEntryImpl entry = new NodePermissionEntryImpl();
                entry.setNodeKey(nodeKey);
                entry.setInherits(true);
                getHibernateTemplate().save(entry);
                nullPermissionCache.remove(nodeRef);
                return entry;
            }
            return (org.alfresco.repo.security.permissions.impl.hibernate.NodePermissionEntry) obj;
        }
        catch (DataAccessException e)
        {
            if (e.contains(ObjectDeletedException.class))
            {
                // the object no loner exists
                if (create)
                {
                    NodePermissionEntryImpl entry = new NodePermissionEntryImpl();
                    entry.setNodeKey(nodeKey);
                    entry.setInherits(true);
                    getHibernateTemplate().save(entry);
                    nullPermissionCache.remove(nodeRef);
                    return entry;
                }
                else
                {
                    return null;
                }
            }
            throw e;
        }
    }

    /**
     * Get a node key from a node reference
     * 
     * @param nodeRef
     * @return
     */
    private NodeKey getNodeKey(NodeRef nodeRef)
    {
        NodeKey nodeKey = new NodeKey(nodeRef.getStoreRef().getProtocol(), nodeRef.getStoreRef().getIdentifier(),
                nodeRef.getId());
        return nodeKey;
    }

    public void deletePermissions(NodeRef nodeRef)
    {
        org.alfresco.repo.security.permissions.impl.hibernate.NodePermissionEntry found = getHibernateNodePermissionEntry(
                nodeRef, false);
        if (found != null)
        {
            deleteHibernateNodePermissionEntry(found);
        }
    }

    private void deleteHibernateNodePermissionEntry(
            org.alfresco.repo.security.permissions.impl.hibernate.NodePermissionEntry hibernateNodePermissionEntry)
    {
        deleteHibernatePermissionEntries(hibernateNodePermissionEntry.getPermissionEntries());
        getHibernateTemplate().delete(hibernateNodePermissionEntry);
    }

    private void deleteHibernatePermissionEntries(
            Set<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry> permissionEntries)
    {
        // Avoid concurrent access problems during deletion
        Set<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry> copy = new HashSet<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry>();
        for (org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry permissionEntry : copy)
        {
            deleteHibernatePermissionEntry(permissionEntry);
        }
    }

    private void deleteHibernatePermissionEntry(
            org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry permissionEntry)
    {
        // Unhook bidirectoinal relationships
        permissionEntry.delete();
        getHibernateTemplate().delete(permissionEntry);
    }

    public void deletePermissions(NodePermissionEntry nodePermissionEntry)
    {
        deletePermissions(nodePermissionEntry.getNodeRef());
    }

    public void deletePermissions(PermissionEntry permissionEntry)
    {
        org.alfresco.repo.security.permissions.impl.hibernate.NodePermissionEntry found = getHibernateNodePermissionEntry(
                permissionEntry.getNodeRef(), false);
        if (found != null)
        {
            Set<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry> deletable = new HashSet<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry>();

            Set<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry> entries = found.getPermissionEntries();
            for (org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry current : entries)
            {
                if (permissionEntry.equals(createSimplePermissionEntry(current)))
                {
                    deletable.add(current);
                }
            }

            for (org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry current : deletable)
            {
                deleteHibernatePermissionEntry(current);
            }
        }
    }

    public void clearPermission(NodeRef nodeRef, String authority)
    {
        org.alfresco.repo.security.permissions.impl.hibernate.NodePermissionEntry found = getHibernateNodePermissionEntry(
                nodeRef, false);
        if (found != null)
        {
            Set<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry> deletable = new HashSet<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry>();

            for (org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry current : found
                    .getPermissionEntries())
            {
                if (createSimplePermissionEntry(current).getAuthority().equals(authority))
                {
                    deletable.add(current);
                }
            }

            for (org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry current : deletable)
            {
                deleteHibernatePermissionEntry(current);
            }
        }
    }

    public void deletePermissions(NodeRef nodeRef, String authority, PermissionReference perm, boolean allow)
    {
        SimplePermissionEntry spe = new SimplePermissionEntry(nodeRef, perm == null ? null
                : new SimplePermissionReference(perm.getQName(), perm.getName()), authority,
                allow ? AccessStatus.ALLOWED : AccessStatus.DENIED);
        deletePermissions(spe);
    }

    public void setPermission(NodeRef nodeRef, String authority, PermissionReference perm, boolean allow)
    {
        deletePermissions(nodeRef, authority, perm, allow);
        PermissionEntryImpl entry = PermissionEntryImpl.create(getHibernateNodePermissionEntry(nodeRef, true),
                getHibernatePermissionReference(perm, true), getHibernateAuthority(authority, true), allow);
        getHibernateTemplate().save(entry);
        nullPermissionCache.remove(nodeRef);
    }

    /**
     * Utility method to find or create a persisted authority
     * 
     * @param authority
     * @param create
     * @return
     */
    private Recipient getHibernateAuthority(String authority, boolean create)
    {
        Recipient key = new RecipientImpl();
        key.setRecipient(authority);

        Recipient found = (Recipient) getHibernateTemplate().get(RecipientImpl.class, key);
        if ((found == null) && create)
        {
            getHibernateTemplate().save(key);
            return key;
        }
        else
        {
            return found;
        }

    }

    /**
     * Utility method to find and optionally create a persisted permission
     * reference.
     * 
     * @param perm
     * @param create
     * @return
     */
    private org.alfresco.repo.security.permissions.impl.hibernate.PermissionReference getHibernatePermissionReference(
            PermissionReference perm, boolean create)
    {
        org.alfresco.repo.security.permissions.impl.hibernate.PermissionReference key = new PermissionReferenceImpl();
        key.setTypeUri(perm.getQName().getNamespaceURI());
        key.setTypeName(perm.getQName().getLocalName());
        key.setName(perm.getName());

        org.alfresco.repo.security.permissions.impl.hibernate.PermissionReference found;

        found = (org.alfresco.repo.security.permissions.impl.hibernate.PermissionReference) getHibernateTemplate().get(
                PermissionReferenceImpl.class, key);
        if ((found == null) && create)
        {
            getHibernateTemplate().save(key);
            return key;
        }
        else
        {
            return found;
        }

    }

    public void setPermission(PermissionEntry permissionEntry)
    {
        setPermission(permissionEntry.getNodeRef(), permissionEntry.getAuthority(), permissionEntry
                .getPermissionReference(), permissionEntry.isAllowed());
    }

    public void setPermission(NodePermissionEntry nodePermissionEntry)
    {
        deletePermissions(nodePermissionEntry);
        NodePermissionEntryImpl entry = new NodePermissionEntryImpl();
        entry.setInherits(nodePermissionEntry.inheritPermissions());
        entry.setNodeKey(getNodeKey(nodePermissionEntry.getNodeRef()));
        getHibernateTemplate().save(entry);
        nullPermissionCache.remove(nodePermissionEntry.getNodeRef());
        for (PermissionEntry pe : nodePermissionEntry.getPermissionEntries())
        {
            setPermission(pe);
        }
    }

    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions)
    {
        getHibernateNodePermissionEntry(nodeRef, true).setInherits(inheritParentPermissions);
    }
    
    public boolean getInheritParentPermissions(NodeRef nodeRef)
    {
        return getHibernateNodePermissionEntry(nodeRef, true).getInherits();
    }

    @SuppressWarnings("unchecked")
    public void deleteAllPermissionsForAuthority(final String authority)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery("permission.GetPermissionsForRecipient");
                query.setString("recipientKey", authority);
                return query.list();
            }
        };
        List<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry> queryResults = (List) getHibernateTemplate().execute(callback);
        for (org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry current : queryResults)
        {
            deleteHibernatePermissionEntry(current);
        }
        
    }

    // Utility methods to create simple detached objects for the outside world
    // We do not pass out the hibernate objects

    private static SimpleNodePermissionEntry createSimpleNodePermissionEntry(
            org.alfresco.repo.security.permissions.impl.hibernate.NodePermissionEntry npe)
    {
        if (npe == null)
        {
            return null;
        }
        SimpleNodePermissionEntry snpe = new SimpleNodePermissionEntry(npe.getNodeRef(), npe.getInherits(),
                createSimplePermissionEntries(npe.getPermissionEntries()));
        return snpe;
    }

    private static Set<SimplePermissionEntry> createSimplePermissionEntries(
            Set<org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry> nes)
    {
        if (nes == null)
        {
            return null;
        }
        HashSet<SimplePermissionEntry> spes = new HashSet<SimplePermissionEntry>(nes.size(), 1.0f);
        if (nes.size() != 0)
        {
            for (org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry pe : nes)
            {
                spes.add(createSimplePermissionEntry(pe));
            }
        }
        return spes;
    }

    private static SimplePermissionEntry createSimplePermissionEntry(
            org.alfresco.repo.security.permissions.impl.hibernate.PermissionEntry pe)
    {
        if (pe == null)
        {
            return null;
        }
        return new SimplePermissionEntry(pe.getNodePermissionEntry().getNodeRef(), createSimplePermissionReference(pe
                .getPermissionReference()), pe.getRecipient().getRecipient(), pe.isAllowed() ? AccessStatus.ALLOWED
                : AccessStatus.DENIED);
    }

    private static SimplePermissionReference createSimplePermissionReference(
            org.alfresco.repo.security.permissions.impl.hibernate.PermissionReference pr)
    {
        if (pr == null)
        {
            return null;
        }
        return new SimplePermissionReference(QName.createQName(pr.getTypeUri(), pr.getTypeName()), pr.getName());
    }

}
