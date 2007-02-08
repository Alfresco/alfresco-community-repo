/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.jcr.version;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.MergeException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.JCRProxyFactory;


/**
 * Alfresco Implementation of a JCR Version History
 * 
 * @author David Caruana
 */
public class VersionHistoryImpl implements VersionHistory
{
    private SessionImpl session;
    private org.alfresco.service.cmr.version.VersionHistory versionHistory;
    private VersionHistory proxy = null;
    

    /**
     * Construct
     * 
     * @param context
     * @param versionHistory
     */
    public VersionHistoryImpl(SessionImpl context, org.alfresco.service.cmr.version.VersionHistory versionHistory)
    {
        this.session = context;
        this.versionHistory = versionHistory;
    }

    /**
     * Get Version History Proxy
     * 
     * @return  version history proxy
     */
    public VersionHistory getProxy()
    {
        if (proxy == null)
        {
            proxy = (VersionHistory)JCRProxyFactory.create(this, VersionHistory.class, session); 
        }
        return proxy;
    }

    /**
     * Get Session
     * 
     * @return  session impl
     */
    /*package*/ SessionImpl getSessionImpl()
    {
        return session;
    }
    
    /**
     * Get Version History impl
     * 
     * @return  version history impl
     */
    /*package*/ org.alfresco.service.cmr.version.VersionHistory getVersionHistoryImpl()
    {
        return versionHistory;
    }
    
    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.VersionHistory#getVersionableUUID()
     */
    public String getVersionableUUID() throws RepositoryException
    {
        return versionHistory.getRootVersion().getVersionedNodeRef().getId();
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.VersionHistory#getRootVersion()
     */
    public Version getRootVersion() throws RepositoryException
    {
        return new VersionImpl(this, versionHistory.getRootVersion()).getProxy();
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.VersionHistory#getAllVersions()
     */
    public VersionIterator getAllVersions() throws RepositoryException
    {
        Collection<org.alfresco.service.cmr.version.Version> versions = versionHistory.getAllVersions();
        List<org.alfresco.service.cmr.version.Version> versionsList = new ArrayList<org.alfresco.service.cmr.version.Version>(versions);
        return new VersionListIterator(this, versionsList);
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.VersionHistory#getVersion(java.lang.String)
     */
    public Version getVersion(String versionName) throws VersionException, RepositoryException
    {
        org.alfresco.service.cmr.version.Version version = versionHistory.getVersion(versionName);
        return new VersionImpl(this, version).getProxy();
    }

    public Version getVersionByLabel(String label) throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void addVersionLabel(String versionName, String label, boolean moveLabel) throws VersionException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void removeVersionLabel(String label) throws VersionException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean hasVersionLabel(String label) throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean hasVersionLabel(Version version, String label) throws VersionException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public String[] getVersionLabels() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public String[] getVersionLabels(Version version) throws VersionException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void removeVersion(String versionName) throws ReferentialIntegrityException, AccessDeniedException, UnsupportedRepositoryOperationException, VersionException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    
    // Node implementation
    // TODO: To support this set of methods will require the projection of all the JCR Version nodes.
    //       That's not simple.
    
    public Node addNode(String relPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Node addNode(String relPath, String primaryNodeTypeName) throws ItemExistsException, PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void orderBefore(String srcChildRelPath, String destChildRelPath) throws UnsupportedRepositoryOperationException, VersionException, ConstraintViolationException, ItemNotFoundException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, Value value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, Value value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, Value[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, Value[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, String[] values) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, String[] values, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, String value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, String value, int type) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, InputStream value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, boolean value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, double value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, long value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, Calendar value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property setProperty(String name, Node value) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Node getNode(String relPath) throws PathNotFoundException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeIterator getNodes() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeIterator getNodes(String namePattern) throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public PropertyIterator getProperties() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public PropertyIterator getProperties(String namePattern) throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public int getIndex() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public PropertyIterator getReferences() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean hasNode(String relPath) throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean hasProperty(String relPath) throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean hasNodes() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean hasProperties() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeType getPrimaryNodeType() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean isNodeType(String nodeTypeName) throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void addMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void removeMixin(String mixinName) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean canAddMixin(String mixinName) throws NoSuchNodeTypeException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeDefinition getDefinition() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Version checkin() throws VersionException, UnsupportedRepositoryOperationException, InvalidItemStateException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void checkout() throws UnsupportedRepositoryOperationException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void doneMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void cancelMerge(Version version) throws VersionException, InvalidItemStateException, UnsupportedRepositoryOperationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void update(String srcWorkspaceName) throws NoSuchWorkspaceException, AccessDeniedException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws NoSuchWorkspaceException, AccessDeniedException, MergeException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException, NoSuchWorkspaceException, AccessDeniedException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean isCheckedOut() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void restore(String versionName, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void restore(Version version, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void restore(Version version, String relPath, boolean removeExisting) throws PathNotFoundException, ItemExistsException, VersionException, ConstraintViolationException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, ItemExistsException, UnsupportedRepositoryOperationException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Version getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Lock getLock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void unlock() throws UnsupportedRepositoryOperationException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean holdsLock() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean isLocked() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public String getPath() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public String getName() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Item getAncestor(int depth) throws ItemNotFoundException, AccessDeniedException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public int getDepth() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public Session getSession() throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean isNode()
    {
        return true;
    }

    public boolean isNew()
    {
        return false;
    }

    public boolean isModified()
    {
        return false;
    }

    public boolean isSame(Item otherItem) throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void accept(ItemVisitor visitor) throws RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    public void remove() throws VersionException, LockException, ConstraintViolationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

}
