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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.jcr.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.InvalidItemStateException;
import javax.jcr.InvalidSerializedDataException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.jcr.dictionary.JCRNamespacePrefixResolver;
import org.alfresco.jcr.dictionary.NamespaceRegistryImpl;
import org.alfresco.jcr.dictionary.NodeTypeManagerImpl;
import org.alfresco.jcr.exporter.JCRDocumentXMLExporter;
import org.alfresco.jcr.exporter.JCRSystemXMLExporter;
import org.alfresco.jcr.importer.JCRImportHandler;
import org.alfresco.jcr.item.ItemImpl;
import org.alfresco.jcr.item.ItemResolver;
import org.alfresco.jcr.item.JCRPath;
import org.alfresco.jcr.item.JCRTypeConverter;
import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.jcr.item.ValueFactoryImpl;
import org.alfresco.jcr.repository.RepositoryImpl;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.repo.importer.ImporterComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Alfresco Implementation of a JCR Session
 * 
 * @author David Caruana
 */
public class SessionImpl implements Session
{
    /** Parent Repository */ 
    private RepositoryImpl repository;
    
    /** Transaction Id */
    private String trxId;
    
    /** Session Attributes */
    private Map<String, Object> attributes;
    
    /** Workspace Store Reference */
    private StoreRef workspaceStore;
    
    /** Workspace */
    private WorkspaceImpl workspace = null;
    
    /** Type Converter */
    private JCRTypeConverter typeConverter = null;
    
    /** Session based Namespace Resolver */
    private NamespaceRegistryImpl namespaceResolver;

    /** Type Manager */
    private NodeTypeManagerImpl typeManager = null;
    
    /** Value Factory */
    private ValueFactoryImpl valueFactory = null;
    
    /** Session Proxy */
    private Session proxy = null;

    /** Session Isolation Strategy */
    private SessionIsolation sessionIsolation = null;
    
    /** Authenticated ticket */
    private String ticket = null;
    
    
    /**
     * Construct
     * 
     * @param repository  parent repository
     * @throws NoSuchWorkspaceException
     */
    public SessionImpl(RepositoryImpl repository)
    {
        // intialise session
        this.repository = repository;
        this.typeConverter = new JCRTypeConverter(this);
        this.namespaceResolver = new NamespaceRegistryImpl(true, new JCRNamespacePrefixResolver(repository.getServiceRegistry().getNamespaceService()));
        this.typeManager = new NodeTypeManagerImpl(this, namespaceResolver.getNamespaceService());
        this.valueFactory = new ValueFactoryImpl(this);
    }

    /**
     * Initialise Session
     * 
     * @param ticket  authentication ticket
     * @param workspaceName  workspace name
     * @param attributes  session attributes
     * @throws RepositoryException
     */
    public void init(String ticket, String workspaceName, Map<String, Object> attributes)
        throws RepositoryException
    {
        // create appropriate strategy for handling session isolation
        // TODO: Support full session isolation as described in the JCR specification
        String trxId = AlfrescoTransactionSupport.getTransactionId();
        sessionIsolation = (trxId == null) ? new InnerTransaction() : new OuterTransaction();
        sessionIsolation.begin();

        // initialise the session
        this.ticket = ticket;
        this.attributes = (attributes == null) ? new HashMap<String, Object>() : attributes;
        this.workspaceStore = getWorkspaceStore(workspaceName);
    }
    
    /**
     * Create proxied Session
     * 
     * @return  JCR Session
     */    
    public Session getProxy()
    {
        if (proxy == null)
        {
            proxy = (Session)JCRProxyFactory.create(this, Session.class, this); 
        }
        return proxy;
    }

    /**
     * Get the Repository Impl
     * 
     * @return  repository impl
     */
    public RepositoryImpl getRepositoryImpl()
    {
        return repository;
    }

    /**
     * Get the session Ticket
     * 
     * @return ticket
     */
    public String getTicket()
    {
        return ticket;
    }

    /**
     * Get the associated transaction Id
     * 
     * @return transaction id
     */
    public String getTransactionId()
    {
        return trxId;
    }
    
    /**
     * Get the Type Converter
     *
     * @return the type converter
     */
    public JCRTypeConverter getTypeConverter()
    {
        return typeConverter;
    }
    
    /**
     * Get the Type Manager
     * 
     * @return  the type manager
     */
    public NodeTypeManagerImpl getTypeManager()
    {
        return typeManager;
    }
    
    /**
     * Get the Namespace Resolver
     *
     * @return the session based Namespace Resolver
     */
    public NamespacePrefixResolver getNamespaceResolver()
    {
        return namespaceResolver.getNamespaceService();
    }
    
    /**
     * Get the Workspace Store
     * 
     * @return  the workspace store reference
     */
    public StoreRef getWorkspaceStore()
    {
        return workspaceStore;
    }
    
    
    //
    // JCR Session
    //
    
    /* (non-Javadoc)
     * @see javax.jcr.Session#getRepository()
     */
    public Repository getRepository()
    {
        return repository;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getUserID()
     */
    public String getUserID()
    {
        return getRepositoryImpl().getServiceRegistry().getAuthenticationService().getCurrentUserName();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getAttributeNames()
     */
    public String[] getAttributeNames()
    {
        String[] names = (String[]) attributes.keySet().toArray(new String[attributes.keySet().size()]);
        return names;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getWorkspace()
     */
    public Workspace getWorkspace()
    {
        if (workspace == null)
        {
            workspace = new WorkspaceImpl(this);
        }
        return workspace.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#impersonate(javax.jcr.Credentials)
     */
    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException
    {
        // TODO: Implement when impersonation permission added to Alfresco Repository
        throw new LoginException("Insufficient permission to impersonate");
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getRootNode()
     */
    public Node getRootNode() throws RepositoryException
    {
        NodeRef nodeRef = getRepositoryImpl().getServiceRegistry().getNodeService().getRootNode(workspaceStore);
        NodeImpl nodeImpl = new NodeImpl(this, nodeRef);
        return nodeImpl.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getNodeByUUID(java.lang.String)
     */
    public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException
    {
        NodeRef nodeRef = new NodeRef(workspaceStore, uuid);
        boolean exists = getRepositoryImpl().getServiceRegistry().getNodeService().exists(nodeRef);
        if (exists == false)
        {
            throw new ItemNotFoundException();
        }
        NodeImpl nodeImpl = new NodeImpl(this, nodeRef);
        return nodeImpl.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getItem(java.lang.String)
     */
    public Item getItem(String absPath) throws PathNotFoundException, RepositoryException
    {
        NodeRef nodeRef = getRepositoryImpl().getServiceRegistry().getNodeService().getRootNode(workspaceStore);
        ItemImpl itemImpl = ItemResolver.findItem(this, nodeRef, absPath);
        return itemImpl.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#itemExists(java.lang.String)
     */
    public boolean itemExists(String absPath) throws RepositoryException
    {
        ParameterCheck.mandatoryString("absPath", absPath);
        NodeRef nodeRef = getRepositoryImpl().getServiceRegistry().getNodeService().getRootNode(workspaceStore);
        return ItemResolver.itemExists(this, nodeRef, absPath);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#move(java.lang.String, java.lang.String)
     */
    public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException
    {
        ParameterCheck.mandatoryString("srcAbsPath", srcAbsPath);
        ParameterCheck.mandatoryString("destAbsPath", destAbsPath);
        
        // Find source node
        NodeService nodeService = getRepositoryImpl().getServiceRegistry().getNodeService();
        NodeRef rootRef = nodeService.getRootNode(workspaceStore);
        NodeRef sourceRef = ItemResolver.getNodeRef(this, rootRef, srcAbsPath);
        if (sourceRef == null)
        {
            throw new PathNotFoundException("Source path " + srcAbsPath + " cannot be found.");
        }
        
        // Find dest node
        NodeRef destRef = null;
        QName destName = null;
        Path destPath = new JCRPath(getNamespaceResolver(), destAbsPath).getPath();
        if (destPath.size() == 1)
        {
            destRef = rootRef;
            destName = ((JCRPath.SimpleElement)destPath.get(0)).getQName(); 
        }
        else
        {
            Path destParentPath = destPath.subPath(destPath.size() -2);
            destRef = ItemResolver.getNodeRef(this, rootRef, destParentPath.toPrefixString(getNamespaceResolver()));
            if (destRef == null)
            {
                throw new PathNotFoundException("Destination path " + destParentPath + " cannot be found.");
            }
            destName = ((JCRPath.SimpleElement)destPath.get(destPath.size() -1)).getQName();
        }
        
        // Validate name
        // TODO: Replace with proper name validation
        if (destName.getLocalName().indexOf('[') != -1 || destName.getLocalName().indexOf(']') != -1)
        {
            throw new RepositoryException("Node name '" + destName + "' is invalid");
        }
        
        // Determine child association type for destination
        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(sourceRef);
        
        // Move node
        nodeService.moveNode(sourceRef, destRef, childAssocRef.getTypeQName(), destName);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#save()
     */
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException
    {
        sessionIsolation.commit();
        // Note: start a new transaction for subsequent session changes
        sessionIsolation.begin();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#refresh(boolean)
     */
    public void refresh(boolean keepChanges) throws RepositoryException
    {
        if (keepChanges)
        {
            throw new UnsupportedRepositoryOperationException("Keep changes is not supported.");
        }
        sessionIsolation.rollback();
        // Note: start a new transaction for subsequent session changes
        sessionIsolation.begin();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#hasPendingChanges()
     */
    public boolean hasPendingChanges() throws RepositoryException
    {
        return AlfrescoTransactionSupport.isDirty();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getValueFactory()
     */
    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException
    {
        return valueFactory.getProxy();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#checkPermission(java.lang.String, java.lang.String)
     */
    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException
    {
        // locate noderef for path
        NodeService nodeService = getRepositoryImpl().getServiceRegistry().getNodeService();
        NodeRef rootRef = nodeService.getRootNode(getWorkspaceStore());
        NodeRef nodeRef = ItemResolver.getNodeRef(this, rootRef, absPath);
        if (nodeRef == null)
        {
            throw new AccessControlException("Unable to determine access control for path " + absPath);
        }
        
        // test each of the actions specified
        PermissionService permissionService = getRepositoryImpl().getServiceRegistry().getPermissionService();
        String[] checkActions = actions.split(",");
        for (String checkAction : checkActions)
        {
            checkAction = checkAction.trim();
            AccessStatus accessStatus = null;
            if (checkAction.equals("add_node"))
            {
                accessStatus = permissionService.hasPermission(nodeRef, PermissionService.ADD_CHILDREN);
            }
            else if (checkAction.equals("set_property"))
            {
                accessStatus = permissionService.hasPermission(nodeRef, PermissionService.WRITE_PROPERTIES);
            }
            else if (checkAction.equals("remove"))
            {
                accessStatus = permissionService.hasPermission(nodeRef, PermissionService.DELETE);
            }
            else if (checkAction.equals("read"))
            {
                accessStatus = permissionService.hasPermission(nodeRef, PermissionService.READ);
            }
            else
            {
                // fall-through check for alfresco specific permissions
                accessStatus = permissionService.hasPermission(nodeRef, checkAction);
            }
            
            // abort if permission not granted
            if (accessStatus == AccessStatus.DENIED)
            {
                throw new AccessControlException("Permission " + checkAction + " not granted on path " + absPath);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getImportContentHandler(java.lang.String, int)
     */
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException
    {
        // locate noderef for path
        NodeService nodeService = getRepositoryImpl().getServiceRegistry().getNodeService();
        NodeRef rootRef = nodeService.getRootNode(getWorkspaceStore());
        NodeRef nodeRef = ItemResolver.getNodeRef(this, rootRef, parentAbsPath);
        if (nodeRef == null)
        {
            throw new PathNotFoundException("Parent path " + parentAbsPath + " does not exist.");
        }
        
        // create content handler for import
        JCRImportHandler jcrImportHandler = new JCRImportHandler(this);
        ImporterComponent importerComponent = getRepositoryImpl().getImporterComponent();
        return importerComponent.handlerImport(nodeRef, null, jcrImportHandler, new JCRImportBinding(uuidBehavior), null);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#importXML(java.lang.String, java.io.InputStream, int)
     */
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException
    {
        ContentHandler handler = getImportContentHandler(parentAbsPath, uuidBehavior);

        try
        {
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(handler);
            parser.setFeature("http://xml.org/sax/features/namespaces", true);
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            parser.parse(new InputSource(in));
        }    
        catch (SAXException se)
        {
            // check for wrapped repository exception
            Exception e = se.getException();
            if (e != null && e instanceof AlfrescoRuntimeException)
            {
                throw (AlfrescoRuntimeException) e;
            }
            else
            {
                throw new InvalidSerializedDataException("Failed to import provided xml stream", se);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#exportSystemView(java.lang.String, org.xml.sax.ContentHandler, boolean, boolean)
     */
    public void exportSystemView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException
    {
        JCRSystemXMLExporter exporter = new JCRSystemXMLExporter(this, contentHandler);
        ExporterCrawlerParameters parameters = createExportParameters(absPath, skipBinary, noRecurse);
        ExporterService exporterService = getRepositoryImpl().getServiceRegistry().getExporterService();
        exporterService.exportView(exporter, parameters, null);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#exportSystemView(java.lang.String, java.io.OutputStream, boolean, boolean)
     */
    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException
    {
        JCRSystemXMLExporter exporter = new JCRSystemXMLExporter(this, createExportContentHandler(out));
        ExporterCrawlerParameters parameters = createExportParameters(absPath, skipBinary, noRecurse);
        ExporterService exporterService = getRepositoryImpl().getServiceRegistry().getExporterService();
        exporterService.exportView(exporter, parameters, null);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#exportDocumentView(java.lang.String, org.xml.sax.ContentHandler, boolean, boolean)
     */
    public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException
    {
        JCRDocumentXMLExporter exporter = new JCRDocumentXMLExporter(this, contentHandler);
        ExporterCrawlerParameters parameters = createExportParameters(absPath, skipBinary, noRecurse);
        ExporterService exporterService = getRepositoryImpl().getServiceRegistry().getExporterService();
        exporterService.exportView(exporter, parameters, null);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#exportDocumentView(java.lang.String, java.io.OutputStream, boolean, boolean)
     */
    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException
    {
        JCRDocumentXMLExporter exporter = new JCRDocumentXMLExporter(this, createExportContentHandler(out));
        ExporterCrawlerParameters parameters = createExportParameters(absPath, skipBinary, noRecurse);
        ExporterService exporterService = getRepositoryImpl().getServiceRegistry().getExporterService();
        exporterService.exportView(exporter, parameters, null);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#setNamespacePrefix(java.lang.String, java.lang.String)
     */
    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException
    {
        namespaceResolver.registerNamespace(prefix, uri);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getNamespacePrefixes()
     */
    public String[] getNamespacePrefixes() throws RepositoryException
    {
        return namespaceResolver.getPrefixes();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException
    {
        return namespaceResolver.getURI(prefix);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getNamespacePrefix(java.lang.String)
     */
    public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException
    {
        return namespaceResolver.getPrefix(uri);
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#logout()
     */
    public void logout()
    {
        if (isLive())
        {
            // invalidate authentication
            try
            {
                try
                {
                    getRepositoryImpl().getServiceRegistry().getAuthenticationService().invalidateTicket(getTicket());
                }
                finally
                {
                    try
                    {
                        sessionIsolation.rollback();
                    }
                    catch(RepositoryException e)
                    {
                        // continue execution and force logout
                    }
                }
            }
            finally
            {
                ticket = null;
                repository.deregisterSession();
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#isLive()
     */
    public boolean isLive()
    {
        return ticket != null;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#addLockToken(java.lang.String)
     */
    public void addLockToken(String lt)
    {
        // TODO: UnsupportedRepositoryOperationException
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#getLockTokens()
     */
    public String[] getLockTokens()
    {
        LockService lockService = getRepositoryImpl().getServiceRegistry().getLockService();
        List<NodeRef> nodeRefs = lockService.getLocks(getWorkspaceStore(), LockType.WRITE_LOCK);
        String[] tokens = new String[nodeRefs.size()];
        int i = 0;
        for (NodeRef nodeRef : nodeRefs)
        {
            tokens[i++] = nodeRef.toString();
        }
        return tokens;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Session#removeLockToken(java.lang.String)
     */
    public void removeLockToken(String lt)
    {
        // TODO: UnsupportedRepositoryOperationException
    }

    /**
     * Gets the workspace store reference for the given workspace name
     * 
     * @param workspaceName  the workspace name
     * @return  the store reference
     * @throws NoSuchWorkspaceException
     */
    private StoreRef getWorkspaceStore(String workspaceName)
        throws NoSuchWorkspaceException
    {
        if (workspaceName == null)
        {
            // TODO: Provide a default "Null Workspace" as per JCR specification
            throw new NoSuchWorkspaceException("A default workspace could not be established.");
        }
        
        StoreRef workspace = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, workspaceName);
        NodeService nodeService = getRepositoryImpl().getServiceRegistry().getNodeService();
        boolean exists = false;
        try
        {
            exists = nodeService.exists(workspace);
        }
        catch(org.alfresco.repo.security.permissions.AccessDeniedException e)
        {
            // note: fallthrough - store does not exist
        }
        
        if (!exists)
        {
            throw new NoSuchWorkspaceException("Workspace " + workspaceName + " does not exist.");
        }
        return workspace;
    }

    /**
     * Create a Content Handler that outputs to the specified output stream.
     * 
     * @param output stream the output stream to write to
     * @return  the content handler
     */
    private ContentHandler createExportContentHandler(OutputStream output)
        throws RepositoryException
    {
        // Define output format
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(2);
        format.setEncoding("UTF-8");

        // Construct an XML Writer
        try
        {
            return new XMLWriter(output, format);
        }
        catch (UnsupportedEncodingException e)        
        {
            throw new RepositoryException("Failed to create content handler for export", e);            
        }
    }
    
    /**
     * Create Export Parameters
     * 
     * @param exportPath  path to export from
     * @param skipBinary  skip binary content in export
     * @param noRecurse  do not recurse to children
     * @return  export parameters
     */
    private ExporterCrawlerParameters createExportParameters(String exportPath, boolean skipBinary, boolean noRecurse)
    {
        // construct exporter parameters
        ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
        Location exportFrom = new Location(getWorkspaceStore());
        exportFrom.setPath(exportPath);
        parameters.setExportFrom(exportFrom);
        parameters.setCrawlSelf(true);
        parameters.setCrawlContent(!skipBinary);
        parameters.setCrawlChildNodes(!noRecurse);
        parameters.setCrawlNullProperties(false);
        return parameters;
    }

    /**
     * JCR Session Import Binding
     */
    private class JCRImportBinding implements ImporterBinding
    {
        private ImporterBinding.UUID_BINDING  uuidBinding;

        /**
         * Construct
         * 
         * @param uuidBehaviour   JCR Import UUID Behaviour
         */
        private JCRImportBinding(int uuidBehaviour)
        {
            switch (uuidBehaviour)
            {
                case ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW:
                    uuidBinding = ImporterBinding.UUID_BINDING.CREATE_NEW;
                    break;
                case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING:
                    uuidBinding = ImporterBinding.UUID_BINDING.REMOVE_EXISTING;
                    break;
                case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING:
                    uuidBinding = ImporterBinding.UUID_BINDING.REPLACE_EXISTING;
                    break;
                case ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW:
                    uuidBinding = ImporterBinding.UUID_BINDING.THROW_ON_COLLISION;
                    break;
                 default:
                    throw new ImporterException("Unknown Import UUID Behaviour: " + uuidBehaviour);
            }
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getUUIDBinding()
         */
        public UUID_BINDING getUUIDBinding()
        {
            return uuidBinding;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getValue(java.lang.String)
         */
        public String getValue(String key)
        {
            return null;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#searchWithinTransaction()
         */
        public boolean allowReferenceWithinTransaction()
        {
            return false;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getExcludedClasses()
         */
        public QName[] getExcludedClasses()
        {
            return null;
        }
    }

    //
    // Session / Transaction Management
    //

    /**
     * Strategy for handling Session Isolation
     */
    private interface SessionIsolation
    {
        // Start transaction
        public void begin() throws RepositoryException;
        
        // Commit transaction
        public void commit() throws RepositoryException;
        
        // Rollback transaction
        public void rollback() throws RepositoryException;
    }

    
    /**
     * Implementation of session isolation which relies on an outer transaction
     * to control the isolation boundary.
     * 
     * @author davidc
     */
    private class OuterTransaction implements SessionIsolation
    {
        /* (non-Javadoc)
         * @see org.alfresco.jcr.session.SessionImpl.SessionIsolation#begin()
         */
        public void begin() throws RepositoryException
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.jcr.session.SessionImpl.SessionIsolation#commit()
         */
        public void commit() throws RepositoryException
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.jcr.session.SessionImpl.SessionIsolation#rollback()
         */
        public void rollback() throws RepositoryException
        {
        }
    }
    
    /**
     * Session isolation strategy which uses transactions to control the isolation.
     * 
     * @author davidc
     */
    private class InnerTransaction implements SessionIsolation
    {
        private UserTransaction userTransaction = null;

        /* (non-Javadoc)
         * @see org.alfresco.jcr.session.SessionImpl.SessionIsolation#begin()
         */
        public void begin()
            throws RepositoryException
        {
            try
            {
                UserTransaction trx = repository.getServiceRegistry().getTransactionService().getUserTransaction();
                trx.begin();
                userTransaction = trx;
            }
            catch (NotSupportedException e)
            {
                throw new RepositoryException("Failed to start Repository transaction", e);
            }
            catch (SystemException e)
            {
                throw new RepositoryException("Failed to start Repository transaction", e);
            }
        }
    
        /* (non-Javadoc)
         * @see org.alfresco.jcr.session.SessionImpl.SessionIsolation#commit()
         */
        public void commit()
            throws RepositoryException
        {
            try
            {
                userTransaction.commit();
            }
            catch (HeuristicRollbackException e)
            {
                throw new RepositoryException("Failed to commit Repository transaction", e);
            }
            catch (HeuristicMixedException e)
            {
                throw new RepositoryException("Failed to commit Repository transaction", e);
            }
            catch (RollbackException e)
            {
                throw new RepositoryException("Failed to commit Repository transaction", e);
            }
            catch (SystemException e)
            {
                throw new RepositoryException("Failed to commit Repository transaction", e);
            }
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.jcr.session.SessionImpl.SessionIsolation#rollback()
         */
        public void rollback()
            throws RepositoryException
        {
            try
            {
                userTransaction.rollback();
            }
            catch (SystemException e)
            {
                throw new RepositoryException("Failed to rollback Repository transaction", e);
            }
        }
    }
}