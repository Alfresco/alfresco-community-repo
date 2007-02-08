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
package org.alfresco.jcr.session;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.ItemExistsException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;

import org.alfresco.jcr.item.ItemResolver;
import org.alfresco.jcr.item.JCRPath;
import org.alfresco.jcr.query.QueryManagerImpl;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.xml.sax.ContentHandler;

/**
 * Alfresco implementation of a JCR Workspace
 * 
 * @author David Caruana
 */
public class WorkspaceImpl implements Workspace
{
    
    private SessionImpl session;
    private Workspace proxy = null;
    private QueryManagerImpl queryManager = null;
    
    /**
     * Construct
     * 
     * @param session  the session
     */
    public WorkspaceImpl(SessionImpl session)
    {
        this.session = session;   
    }
    
    /**
     * Get proxied JCR Workspace
     * 
     * @return  proxied JCR Workspace
     */
    public Workspace getProxy()
    {
        if (proxy == null)
        {
            proxy = (Workspace)JCRProxyFactory.create(this, Workspace.class, session);
        }
        return proxy;
    }
    
    
    /* (non-Javadoc)
     * @see javax.jcr.Workspace#getSession()
     */
    public Session getSession()
    {
        return session.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#getName()
     */
    public String getName()
    {
        return session.getWorkspaceStore().getIdentifier();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#copy(java.lang.String, java.lang.String)
     */
    public void copy(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException
    {
        ParameterCheck.mandatoryString("srcAbsPath", srcAbsPath);
        ParameterCheck.mandatoryString("destAbsPath", destAbsPath);
        
        // find source node
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        NodeRef rootRef = nodeService.getRootNode(session.getWorkspaceStore());
        NodeRef sourceRef = ItemResolver.getNodeRef(session, rootRef, srcAbsPath);
        if (sourceRef == null)
        {
            throw new PathNotFoundException("Source path " + srcAbsPath + " cannot be found.");
        }
        
        // find dest node
        NodeRef destRef = null;
        QName destName = null;
        Path destPath = new JCRPath(session.getNamespaceResolver(), destAbsPath).getPath();
        if (destPath.size() == 1)
        {
            destRef = rootRef;
            destName = ((JCRPath.SimpleElement)destPath.get(0)).getQName(); 
        }
        else
        {
            Path destParentPath = destPath.subPath(destPath.size() -2);
            destRef = ItemResolver.getNodeRef(session, rootRef, destParentPath.toPrefixString(session.getNamespaceResolver()));
            if (destRef == null)
            {
                throw new PathNotFoundException("Destination path " + destParentPath + " cannot be found.");
            }
            destName = ((JCRPath.SimpleElement)destPath.get(destPath.size() -1)).getQName();
        }
        
        // validate name
        // TODO: Replace with proper name validation
        if (destName.getLocalName().indexOf('[') != -1 || destName.getLocalName().indexOf(']') != -1)
        {
            throw new RepositoryException("Node name '" + destName + "' is invalid");
        }
        
        // determine child association type for destination
        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(sourceRef);
        
        // copy node
        CopyService copyService = session.getRepositoryImpl().getServiceRegistry().getCopyService();
        copyService.copy(sourceRef, destRef, childAssocRef.getTypeQName(), destName);

        // finally save
        session.save();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#copy(java.lang.String, java.lang.String, java.lang.String)
     */
    public void copy(String srcWorkspace, String srcAbsPath, String destAbsPath) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#clone(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public void clone(String srcWorkspace, String srcAbsPath, String destAbsPath, boolean removeExisting) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#move(java.lang.String, java.lang.String)
     */
    public void move(String srcAbsPath, String destAbsPath) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException
    {
        session.move(srcAbsPath, destAbsPath);
        session.save();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#restore(javax.jcr.version.Version[], boolean)
     */
    public void restore(Version[] versions, boolean removeExisting) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#getQueryManager()
     */
    public QueryManager getQueryManager() throws RepositoryException
    {
        if (queryManager == null)
        {
            queryManager = new QueryManagerImpl(session);
        }
        return queryManager;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#getNamespaceRegistry()
     */
    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException
    {
        return session.getRepositoryImpl().getNamespaceRegistry();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#getNodeTypeManager()
     */
    public NodeTypeManager getNodeTypeManager() throws RepositoryException
    {
        return session.getTypeManager();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#getObservationManager()
     */
    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#getAccessibleWorkspaceNames()
     */
    public String[] getAccessibleWorkspaceNames() throws RepositoryException
    {
        NodeService nodeService = session.getRepositoryImpl().getServiceRegistry().getNodeService();
        List<StoreRef> storeRefs = nodeService.getStores();
        List<String> workspaceStores = new ArrayList<String>();
        for (StoreRef storeRef : storeRefs)
        {
            if (storeRef.getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE))
            {
                workspaceStores.add(storeRef.getIdentifier());
            }
        }
        return workspaceStores.toArray(new String[workspaceStores.size()]);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#getImportContentHandler(java.lang.String, int)
     */
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException
    {
        return session.getImportContentHandler(parentAbsPath, uuidBehavior);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Workspace#importXML(java.lang.String, java.io.InputStream, int)
     */
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException
    {
        session.importXML(parentAbsPath, in, uuidBehavior);
    }

}
