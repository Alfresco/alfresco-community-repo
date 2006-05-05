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

import org.alfresco.jcr.item.NodeRefNodeIteratorImpl;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Alfresco implementation of a JCR Version
 * 
 * @author David Caruana
 */
public class VersionImpl implements Version
{
    private VersionHistoryImpl versionHistoryImpl;
    private org.alfresco.service.cmr.version.Version version; 
    private Version proxy;
    
    
    /**
     * Construct
     * 
     * @param versionHistoryImpl
     * @param version
     */
    public VersionImpl(VersionHistoryImpl versionHistoryImpl, org.alfresco.service.cmr.version.Version version)
    {
        this.versionHistoryImpl = versionHistoryImpl;
        this.version = version;
    }

    /**
     * Get Version Proxy
     * 
     * @return
     */
    public Version getProxy()
    {
        if (proxy == null)
        {
            proxy = (Version)JCRProxyFactory.create(this, Version.class, versionHistoryImpl.getSessionImpl()); 
        }
        return proxy;
    }
    
    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.Version#getContainingHistory()
     */    
    public VersionHistory getContainingHistory() throws RepositoryException
    {
        return versionHistoryImpl.getProxy();
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.Version#getCreated()
     */
    public Calendar getCreated() throws RepositoryException
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(version.getCreatedDate());
        return calendar;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.Version#getSuccessors()
     */
    public Version[] getSuccessors() throws RepositoryException
    {
        Collection<org.alfresco.service.cmr.version.Version> successors = versionHistoryImpl.getVersionHistoryImpl().getSuccessors(version);
        Version[] versions = new Version[successors.size()];
        int i = 0;
        for (org.alfresco.service.cmr.version.Version sucessor : successors)
        {
            versions[i++] = new VersionImpl(versionHistoryImpl, sucessor).getProxy();
        }
        return versions;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.Version#getPredecessors()
     */
    public Version[] getPredecessors() throws RepositoryException
    {
        org.alfresco.service.cmr.version.Version predecessor = versionHistoryImpl.getVersionHistoryImpl().getPredecessor(version);
        Version[] versions = new Version[1];
        versions[0] = new VersionImpl(versionHistoryImpl, predecessor).getProxy();
        return versions;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.Node#hasNodes()
     */ 
    public boolean hasNodes() throws RepositoryException
    {
        return true;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.Node#getNodes()
     */
    public NodeIterator getNodes() throws RepositoryException
    {
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
        nodeRefs.add(version.getFrozenStateNodeRef());
        return new NodeRefNodeIteratorImpl(versionHistoryImpl.getSessionImpl(), nodeRefs);
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
        return version.getVersionLabel();
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
