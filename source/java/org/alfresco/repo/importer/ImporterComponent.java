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
package org.alfresco.repo.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.importer.view.NodeContext;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.XPathException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.cmr.view.ImporterBinding.UUID_BINDING;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.TransactionHelper;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.util.StringUtils;
import org.xml.sax.ContentHandler;


/**
 * Default implementation of the Importer Service
 *  
 * @author David Caruana
 */
public class ImporterComponent
    implements ImporterService
{
    // Logger
    private static final Log logger = LogFactory.getLog(ImporterComponent.class);
    
    // default importer
    // TODO: Allow registration of plug-in parsers (by namespace)
    private Parser viewParser;

    // supporting services
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private BehaviourFilter behaviourFilter;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private RuleService ruleService;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private AuthenticationContext authenticationContext;
    private OwnableService ownableService;
    private VersionService versionService;

    /**
     * The db node service, used when updating the version store.
     */
    protected NodeService dbNodeService;

    // binding markers    
    private static final String START_BINDING_MARKER = "${";
    private static final String END_BINDING_MARKER = "}"; 
    
    
    /**
     * @param viewParser  the default parser
     */
    public void setViewParser(Parser viewParser)
    {
        this.viewParser = viewParser;
    }
    
    /**
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService the service to perform path searches
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @param contentService  the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @param dictionaryService  the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param namespaceService  the namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param behaviourFilter  policy behaviour filter 
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * TODO: Remove this in favour of appropriate rule disabling
     * 
     * @param ruleService  rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }
    
    /**
     * @param permissionService  permissionService
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * @param authorityService  authorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @param authenticationContext  authenticationContext
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }
    
    /**
     * @param ownableService  ownableService
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    /**
     * @param versionService  versionService
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }
    
    /**
     * Sets the db node service, used when updating the 
     *  versioning information
     *
     * @param nodeService  the node service
     */
    public void setDbNodeService(NodeService nodeService)
    {
        this.dbNodeService = nodeService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterService#importView(java.io.InputStreamReader, org.alfresco.service.cmr.view.Location, java.util.Properties, org.alfresco.service.cmr.view.ImporterProgress)
     */
    public void importView(Reader viewReader, Location location, ImporterBinding binding, ImporterProgress progress)
    {
        NodeRef nodeRef = getNodeRef(location, binding);
        parserImport(nodeRef, location.getChildAssocType(), viewReader, new DefaultStreamHandler(), binding, progress);       
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterService#importView(org.alfresco.service.cmr.view.ImportPackageHandler, org.alfresco.service.cmr.view.Location, org.alfresco.service.cmr.view.ImporterBinding, org.alfresco.service.cmr.view.ImporterProgress)
     */
    public void importView(ImportPackageHandler importHandler, Location location, ImporterBinding binding, ImporterProgress progress) throws ImporterException
    {
        importHandler.startImport();
        Reader dataFileReader = importHandler.getDataStream(); 
        NodeRef nodeRef = getNodeRef(location, binding);
        parserImport(nodeRef, location.getChildAssocType(), dataFileReader, importHandler, binding, progress);
        importHandler.endImport();
    }
    
    /**
     * Get Node Reference from Location
     *  
     * @param location the location to extract node reference from
     * @param binding import configuration
     * @return node reference
     */
    private NodeRef getNodeRef(Location location, ImporterBinding binding)
    {
        ParameterCheck.mandatory("Location", location);
    
        // Establish node to import within
        NodeRef nodeRef = location.getNodeRef();
        if (nodeRef == null)
        {
            // If a specific node has not been provided, default to the root
            nodeRef = nodeService.getRootNode(location.getStoreRef());
        }
        
        // Resolve to path within node, if one specified
        String path = location.getPath();
        if (path != null && path.length() >0)
        {
            // Create a valid path and search
            path = bindPlaceHolder(path, binding);
            path = createValidPath(path);
            List<NodeRef> nodeRefs = searchService.selectNodes(nodeRef, path, null, namespaceService, false);
            if (nodeRefs.size() == 0)
            {
                throw new ImporterException("Path " + path + " within node " + nodeRef + " does not exist - the path must resolve to a valid location");
            }
            if (nodeRefs.size() > 1)
            {
                throw new ImporterException("Path " + path + " within node " + nodeRef + " found too many locations - the path must resolve to one location");
            }
            nodeRef = nodeRefs.get(0);
        }
    
        // TODO: Check Node actually exists
        
        return nodeRef;
    }
    
    /**
     * Bind the specified value to the passed configuration values if it is a place holder
     * 
     * @param value  the value to bind
     * @param binding  the configuration properties to bind to
     * @return  the bound value
     */
    private String bindPlaceHolder(String value, ImporterBinding binding)
    {
        if (binding != null)
        {
            int iStartBinding = value.indexOf(START_BINDING_MARKER);
            while (iStartBinding != -1)
            {
                int iEndBinding = value.indexOf(END_BINDING_MARKER, iStartBinding + START_BINDING_MARKER.length());
                if (iEndBinding == -1)
                {
                    throw new ImporterException("Cannot find end marker " + END_BINDING_MARKER + " within value " + value);
                }
                
                String key = value.substring(iStartBinding + START_BINDING_MARKER.length(), iEndBinding);
                String keyValue = binding.getValue(key);
                if (keyValue == null) {
                    logger.warn("No binding value for placeholder (will default to empty string): " + value);
                }
                value = StringUtils.replace(value, START_BINDING_MARKER + key + END_BINDING_MARKER, keyValue == null ? "" : keyValue);
                iStartBinding = value.indexOf(START_BINDING_MARKER);
            }
        }
        return value;
    }
    
    /**
     * Create a valid qname-based xpath
     * 
     * Note: 
     * - the localname will be truncated to 100 chars
     * - the localname should already be encoded for ISO 9075 (in case of MT bootstrap, the @ sign will be auto-encoded, see below)
     * 
     * Some examples:
     *      /
     *      sys:people/cm:admin
     *      /app:company_home/app:dictionary
     *      ../../cm:people_x0020_folder
     *      sys:people/cm:admin_x0040_test
     *      
     * @param path
     * @return
     */
    private String createValidPath(String path)
    {
        StringBuffer validPath = new StringBuffer(path.length());
        String[] segments = StringUtils.delimitedListToStringArray(path, "/");
        for (int i = 0; i < segments.length; i++)
        {
            if (segments[i] != null && segments[i].length() > 0)
            {
                int colonIndex = segments[i].indexOf(QName.NAMESPACE_PREFIX);
                if (colonIndex == -1)
                {
                    // eg. ".."
                    validPath.append(segments[i]);
                }
                else
                {
                    String[] qnameComponents = QName.splitPrefixedQName(segments[i]);
                    
                    String localName = QName.createValidLocalName(qnameComponents[1]);
                    
                    // MT: bootstrap of "alfrescoUserStore.xml" requires 'sys:people/cm:admin@tenant' to be encoded as 'sys:people/cm:admin_x0040_tenant' (for XPath)
                    localName = localName.replace("@", "_x0040_");
                    
                    QName segmentQName = QName.createQName(qnameComponents[0], localName, namespaceService);
                    validPath.append(segmentQName.toPrefixString());
                }
            }
            if (i < (segments.length -1))
            {
                validPath.append("/");
            }
        }
        return validPath.toString();
    }
    
    /**
     * Perform Import via Parser
     * 
     * @param nodeRef node reference to import under
     * @param childAssocType the child association type to import under
     * @param inputStream the input stream to import from
     * @param streamHandler the content property import stream handler
     * @param binding import configuration
     * @param progress import progress
     */
    public void parserImport(NodeRef nodeRef, QName childAssocType, Reader viewReader, ImportPackageHandler streamHandler, ImporterBinding binding, ImporterProgress progress)
    {
        ParameterCheck.mandatory("Node Reference", nodeRef);
        ParameterCheck.mandatory("View Reader", viewReader);
        ParameterCheck.mandatory("Stream Handler", streamHandler);
        
        Importer nodeImporter = new NodeImporter(nodeRef, childAssocType, binding, streamHandler, progress);
        try
        {
            nodeImporter.start();
            viewParser.parse(viewReader, nodeImporter);
            nodeImporter.end();
        }
        catch(RuntimeException e)
        {
            nodeImporter.error(e);
            throw e;
        }
    }
    
    /**
     * Perform import via Content Handler
     * 
     * @param nodeRef node reference to import under
     * @param childAssocType the child association type to import under
     * @param handler the import content handler
     * @param binding import configuration
     * @param progress import progress
     * @return  content handler to interact with
     */
    public ContentHandler handlerImport(NodeRef nodeRef, QName childAssocType, ImportContentHandler handler, ImporterBinding binding, ImporterProgress progress)
    {
        ParameterCheck.mandatory("Node Reference", nodeRef);

        DefaultContentHandler defaultHandler = new DefaultContentHandler(handler);
        ImportPackageHandler streamHandler = new ContentHandlerStreamHandler(defaultHandler);
        Importer nodeImporter = new NodeImporter(nodeRef, childAssocType, binding, streamHandler, progress);
        defaultHandler.setImporter(nodeImporter);
        return defaultHandler;        
    }

    /**
     * Encapsulate how a node is imported into the repository
     */
    public interface NodeImporterStrategy
    {
        /**
         * Import a node
         * 
         * @param  node to import
         */
        public NodeRef importNode(ImportNode node);
    }
    
    /**
     * Default Importer strategy
     * 
     * @author David Caruana
     */
    private class NodeImporter
        implements Importer
    {
        private NodeRef rootRef;
        private QName rootAssocType;
        private ImporterBinding binding;
        private ImporterProgress progress;
        private ImportPackageHandler streamHandler;
        private NodeImporterStrategy importStrategy;
        private UpdateExistingNodeImporterStrategy updateStrategy;
        private QName[] excludedClasses;

        // Import tracking
        private List<ImportedNodeRef> nodeRefs = new ArrayList<ImportedNodeRef>();

        /**
         * Construct
         * 
         * @param rootRef
         * @param rootAssocType
         * @param binding
         * @param progress
         */
        private NodeImporter(NodeRef rootRef, QName rootAssocType, ImporterBinding binding, ImportPackageHandler streamHandler, ImporterProgress progress)
        {
            this.rootRef = rootRef;
            this.rootAssocType = rootAssocType;
            this.binding = binding;
            this.progress = progress;
            this.streamHandler = streamHandler;
            this.importStrategy = createNodeImporterStrategy(binding == null ? null : binding.getUUIDBinding());
            this.updateStrategy = new UpdateExistingNodeImporterStrategy();

            // initialise list of content models to exclude from import
            if (binding == null || binding.getExcludedClasses() == null)
            {
                this.excludedClasses = new QName[] { ContentModel.ASPECT_REFERENCEABLE };
            }
            else
            {
                this.excludedClasses = binding.getExcludedClasses();
            }
        }

        /**
         * Create Node Importer Strategy
         * 
         * @param uuidBinding  UUID Binding
         * @return  Node Importer Strategy
         */
        private NodeImporterStrategy createNodeImporterStrategy(ImporterBinding.UUID_BINDING uuidBinding)
        {
            if (uuidBinding == null)
            {
                return new CreateNewNodeImporterStrategy(true);
            }
            else if (uuidBinding.equals(UUID_BINDING.CREATE_NEW))
            {
                return new CreateNewNodeImporterStrategy(true);
            }
            else if (uuidBinding.equals(UUID_BINDING.CREATE_NEW_WITH_UUID))
            {
                return new CreateNewNodeImporterStrategy(false);
            }
            else if (uuidBinding.equals(UUID_BINDING.REMOVE_EXISTING))
            {
                return new RemoveExistingNodeImporterStrategy();
            }
            else if (uuidBinding.equals(UUID_BINDING.REPLACE_EXISTING))
            {
                return new ReplaceExistingNodeImporterStrategy();
            }
            else if (uuidBinding.equals(UUID_BINDING.UPDATE_EXISTING))
            {
                return new UpdateExistingNodeImporterStrategy();
            }
            else if (uuidBinding.equals(UUID_BINDING.THROW_ON_COLLISION))
            {
                return new ThrowOnCollisionNodeImporterStrategy();
            }
            else
            {
                return new CreateNewNodeImporterStrategy(true);
            }
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#getRootRef()
         */
        public NodeRef getRootRef()
        {
            return rootRef;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#getRootAssocType()
         */
        public QName getRootAssocType()
        {
            return rootAssocType;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#start()
         */
        public void start()
        {
            reportStarted();
        }
       
        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#importMetaData(java.util.Map)
         */
        public void importMetaData(Map<QName, String> properties)
        {
            // Determine if we're importing a complete repository
            String complexPath = properties.get(QName.createQName(NamespaceService.REPOSITORY_VIEW_1_0_URI, "exportOf"));
            for (String path : complexPath.split(","))
            {
                if (path != null && path.equals("/"))
                {
                    // Only allow complete repository import into root
                    NodeRef storeRootRef = nodeService.getRootNode(rootRef.getStoreRef());
                    if (!storeRootRef.equals(rootRef))
                    {
                        throw new ImporterException("A complete repository package cannot be imported here");
                    }
                }
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#importNode(org.alfresco.repo.importer.ImportNode)
         */
        @SuppressWarnings("unchecked")
        public NodeRef importNode(ImportNode context)
        {
            // import node
            NodeRef nodeRef;
            if (context.isReference())
            {
                nodeRef = linkNode(context);
            }
            else
            {
                nodeRef = importStrategy.importNode(context);
            }
            
            // apply aspects
            for (QName aspect : context.getNodeAspects())
            {
                if (nodeService.hasAspect(nodeRef, aspect) == false)
                {
                    nodeService.addAspect(nodeRef, aspect, null);   // all properties previously added
                    reportAspectAdded(nodeRef, aspect);
                }
            }

            // import content, if applicable
            for (Map.Entry<QName,Serializable> property : context.getProperties().entrySet())
            {
                // filter out content properties (they're imported later)
                DataTypeDefinition valueDataType = context.getPropertyDataType(property.getKey());
                if (valueDataType != null && valueDataType.getName().equals(DataTypeDefinition.CONTENT))
                {
                    // the property may be a single value or a collection - handle both
                    Object objVal = property.getValue();
                    if (objVal instanceof String)
                    {
                       importContent(nodeRef, property.getKey(), (String)objVal);
                    }
                    else if (objVal instanceof Collection)
                    {
                       for (String value : (Collection<String>)objVal)
                       {
                          importContent(nodeRef, property.getKey(), value);
                       }
                    }
                }
            }
            
            // if the node has the versionable aspect applied to it,
            //  create an initial version for it
            if(context.getNodeAspects().contains(ContentModel.ASPECT_VERSIONABLE))
            {
                generateVersioningForVersionableNode(nodeRef);
            }
            
            return nodeRef;
        }
        
        /**
         * Fixes things up for versionable nodes after importing.
         * Because version information is stored in a different store,
         *  the past versions are not included in the ACP. 
         * However, because the node has the versionable aspect applied to 
         *  it, we still need it to have a single version in the version store.
         * This method arranges for that. 
         */
        private void generateVersioningForVersionableNode(final NodeRef nodeRef)
        {
            // Is versioning already turned on?
            if(versionService.getVersionHistory(nodeRef) != null)
            {
                // There is already version history, so we don't need to do anything
                return;
            }
            
            // Take a copy of the version label, as it'll be reset when
            //  we request that versioning occurs
            final String label = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL);
            
            // Have versioning enabled
            Version version = versionService.createVersion(nodeRef, null);
            final NodeRef versionNodeRef = VersionUtil.convertNodeRef(version.getFrozenStateNodeRef());
            
            // Put the version label back how it should be on the main node
            dbNodeService.setProperty(nodeRef, ContentModel.PROP_VERSION_LABEL, label);
            
            // Fix up the versioned version node to be what it should be
            // (The previous version label should be off, and the current label is the new one)
            dbNodeService.setProperty(versionNodeRef, ContentModel.PROP_VERSION_LABEL, null);
            dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_VERSION_LABEL, label);
        }

        /**
         * Link an existing Node
         * 
         * @param context  node to link in
         * @return  node reference of child linked in
         */
        private NodeRef linkNode(ImportNode context)
        {
            ImportParent parentContext = context.getParentContext();
            NodeRef parentRef = parentContext.getParentRef();
            
            // determine the node reference to link to
            String uuid = context.getUUID();
            if (uuid == null || uuid.length() == 0)
            {
                throw new ImporterException("Node reference does not specify a reference to follow.");
            }
            NodeRef referencedRef = new NodeRef(rootRef.getStoreRef(), uuid);

            // Note: do not link references that are defined in the root of the import
            if (!parentRef.equals(getRootRef()))
            {
                // determine child assoc type
                QName assocType = getAssocType(context);
                AssociationDefinition assocDef = dictionaryService.getAssociation(assocType);
                if (assocDef.isChild())
                {
                    // determine child name
                    QName childQName = getChildName(context);
                    if (childQName == null)
                    {
                        String name = (String)nodeService.getProperty(referencedRef, ContentModel.PROP_NAME);
                        if (name == null || name.length() == 0)
                        {
                            throw new ImporterException("Cannot determine node reference child name");
                        }
                        String localName = QName.createValidLocalName(name);
                        childQName = QName.createQName(assocType.getNamespaceURI(), localName);
                    }
                
                    // create the secondary link
                    nodeService.addChild(parentRef, referencedRef, assocType, childQName);
                    reportNodeLinked(referencedRef, parentRef, assocType, childQName);
                }
                else
                {
                    nodeService.createAssociation(parentRef, referencedRef, assocType);
                    reportNodeLinked(parentRef, referencedRef, assocType, null);
                }
            }
            
            // second, perform any specified udpates to the node
            updateStrategy.importNode(context);
            return referencedRef; 
        }
        
        /**
         * Import Node Content.
         * <p>
         * The content URL, if present, will be a local URL.  This import copies the content
         * from the local URL to a server-assigned location.
         *
         * @param nodeRef containing node
         * @param propertyName the name of the content-type property
         * @param contentData the identifier of the content to import
         */
        private void importContent(NodeRef nodeRef, QName propertyName, String importContentData)
        {
            // bind import content data description
            importContentData = bindPlaceHolder(importContentData, binding);
            if (importContentData != null && importContentData.length() > 0)
            {
                DataTypeDefinition dataTypeDef = dictionaryService.getDataType(DataTypeDefinition.CONTENT);
                ContentData contentData = (ContentData)DefaultTypeConverter.INSTANCE.convert(dataTypeDef, importContentData);
                String contentUrl = contentData.getContentUrl();
                if (contentUrl != null && contentUrl.length() > 0)
                {
                    // import the content from the url
                    InputStream contentStream = streamHandler.importStream(contentUrl);
                    ContentWriter writer = contentService.getWriter(nodeRef, propertyName, true);
                    writer.setEncoding(contentData.getEncoding());
                    writer.setMimetype(contentData.getMimetype());
                    writer.putContent(contentStream);
                    reportContentCreated(nodeRef, contentUrl);
                }
            }
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#childrenImported(org.alfresco.service.cmr.repository.NodeRef)
         */
        public void childrenImported(NodeRef nodeRef)
        {
            behaviourFilter.enableBehaviours(nodeRef);
            ruleService.enableRules(nodeRef);
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#resolvePath(java.lang.String)
         */
        public NodeRef resolvePath(String path)
        {
            NodeRef referencedRef = null;
            if (path != null && path.length() > 0)
            {
                referencedRef = resolveImportedNodeRef(rootRef, path);
            }
            return referencedRef;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#isExcludedClass(org.alfresco.service.namespace.QName)
         */
        public boolean isExcludedClass(QName className)
        {
            for (QName excludedClass : excludedClasses)
            {
                if (excludedClass.equals(className))
                {
                    return true;
                }
            }
            return false;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#end()
         */
        @SuppressWarnings("unchecked")
        public void end()
        {
            // Bind all node references to destination space
            for (ImportedNodeRef importedRef : nodeRefs)
            {
                Serializable refProperty = null;
                if (importedRef.value != null)
                {
                    if (importedRef.value instanceof Collection)
                    {
                        Collection<String> unresolvedRefs = (Collection<String>)importedRef.value;
                        List<NodeRef> resolvedRefs = new ArrayList<NodeRef>(unresolvedRefs.size());
                        for (String unresolvedRef : unresolvedRefs)
                        {
                            if (unresolvedRef != null)
                            {
                                NodeRef nodeRef = resolveImportedNodeRef(importedRef.context.getNodeRef(), unresolvedRef);
                                // TODO: Provide a better mechanism for invalid references? e.g. report warning
                                if (nodeRef != null)
                                {
                                    resolvedRefs.add(nodeRef);
                                }
                            }
                        }
                        refProperty = (Serializable)resolvedRefs;
                    }
                    else
                    {
                        refProperty = resolveImportedNodeRef(importedRef.context.getNodeRef(), (String)importedRef.value);
                        // TODO: Provide a better mechanism for invalid references? e.g. report warning
                    }
                }
                
                // Set node reference on source node
                Set<QName> disabledBehaviours = getDisabledBehaviours(importedRef.context);
                try
                {
                    for (QName disabledBehaviour: disabledBehaviours)
                    {
                        behaviourFilter.disableBehaviour(importedRef.context.getNodeRef(), disabledBehaviour);
                    }
                    nodeService.setProperty(importedRef.context.getNodeRef(), importedRef.property, refProperty);
                    if (progress != null)
                    {
                        progress.propertySet(importedRef.context.getNodeRef(), importedRef.property, refProperty);
                    }
                }
                finally
                {
                    behaviourFilter.enableBehaviours(importedRef.context.getNodeRef());
                }
            }
            
            reportCompleted();
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.repo.importer.Importer#error(java.lang.Throwable)
         */
        public void error(Throwable e)
        {
            behaviourFilter.enableAllBehaviours();
            reportError(e);
        }

        /**
         * Get the child name to import node under
         * 
         * @param context  the node
         * @return  the child name
         */
        private QName getChildName(ImportNode context)
        {
            QName assocType = getAssocType(context);
            QName childQName = null;
    
            // Determine child name
            String childName = context.getChildName();
            if (childName != null)
            {
                childName = bindPlaceHolder(childName, binding);
                // <Fix for ETHREEOH-2299>
                if (ContentModel.TYPE_PERSON.equals(context.getTypeDefinition().getName()))
                {
                    childName = childName.toLowerCase();
                }
                // </Fix for ETHREEOH-2299>
                String[] qnameComponents = QName.splitPrefixedQName(childName);
                childQName = QName.createQName(qnameComponents[0], QName.createValidLocalName(qnameComponents[1]), namespaceService); 
            }
            else
            {
                Map<QName, Serializable> typeProperties = context.getProperties();
                
                Serializable nameValue = typeProperties.get(ContentModel.PROP_NAME);

                if(nameValue != null && !String.class.isAssignableFrom(nameValue.getClass()))
                {
                    throw new  ImporterException("Unable to use childName property: "+ ContentModel.PROP_NAME + " is not a string");  
                }
                
                String name = (String)nameValue;
                
                if (name != null && name.length() > 0)
                {
                    name = bindPlaceHolder(name, binding);
                    String localName = QName.createValidLocalName(name);
                    childQName = QName.createQName(assocType.getNamespaceURI(), localName);
                }
            }
            
            return childQName;
        }
        
        /**
         * Get appropriate child association type for node to import under
         * 
         * @param context  node to import
         * @return  child association type name
         */
        private QName getAssocType(ImportNode context)
        {
            QName assocType = context.getParentContext().getAssocType();
            if (assocType != null)
            {
                // return explicitly set association type
                return assocType;
            }
            
            //
            // Derive association type
            //
            
            // build type and aspect list for node
            List<QName> nodeTypes = new ArrayList<QName>();
            nodeTypes.add(context.getTypeDefinition().getName());
            for (QName aspect : context.getNodeAspects())
            {
                nodeTypes.add(aspect);
            }
            
            // build target class types for parent
            Map<QName, QName> targetTypes = new HashMap<QName, QName>();
            QName parentType = nodeService.getType(context.getParentContext().getParentRef());
            ClassDefinition classDef = dictionaryService.getClass(parentType);
            Map<QName, ChildAssociationDefinition> childAssocDefs = classDef.getChildAssociations();
            for (ChildAssociationDefinition childAssocDef : childAssocDefs.values())
            {
                targetTypes.put(childAssocDef.getTargetClass().getName(), childAssocDef.getName());
            }
            Set<QName> parentAspects = nodeService.getAspects(context.getParentContext().getParentRef());
            for (QName parentAspect : parentAspects)
            {
                classDef = dictionaryService.getClass(parentAspect);
                childAssocDefs = classDef.getChildAssociations();
                for (ChildAssociationDefinition childAssocDef : childAssocDefs.values())
                {
                    targetTypes.put(childAssocDef.getTargetClass().getName(), childAssocDef.getName());
                }
            }
            
            // find target class that is closest to node type or aspects
            QName closestAssocType = null;
            int closestHit = 1;
            for (QName nodeType : nodeTypes)
            {
                for (QName targetType : targetTypes.keySet())
                {
                    QName testType = nodeType;
                    int howClose = 1;
                    while (testType != null)
                    {
                        howClose--;
                        if (targetType.equals(testType) && howClose < closestHit)
                        {
                            closestAssocType = targetTypes.get(targetType);
                            closestHit = howClose;
                            break;
                        }
                        ClassDefinition testTypeDef = dictionaryService.getClass(testType);
                        testType = (testTypeDef == null) ? null : testTypeDef.getParentName();
                    }
                }
            }
            
            return closestAssocType;
        }
        
        /**
         * For the given import node, return the behaviours to disable during import
         * 
         * @param context  import node
         * @return  the disabled behaviours
         */
        private Set<QName> getDisabledBehaviours(ImportNode context)
        {
            Set<QName> classNames = new HashSet<QName>();
            
            // disable the type
            TypeDefinition typeDef = context.getTypeDefinition();
            classNames.add(typeDef.getName());

            // disable the aspects imported on the node
            classNames.addAll(context.getNodeAspects());
            
            // note: do not disable default aspects that are not imported on the node.
            //       this means they'll be added on import
            
            return classNames;
        }
        
        /**
         * Bind properties
         * 
         * @param properties
         * @return
         */
        @SuppressWarnings("unchecked")
        private Map<QName, Serializable> bindProperties(ImportNode context)
        {
            Map<QName, Serializable> properties = context.getProperties();
            Map<QName, Serializable> boundProperties = new HashMap<QName, Serializable>(properties.size());
            for (QName property : properties.keySet())
            {
                // get property datatype
                DataTypeDefinition valueDataType = context.getPropertyDataType(property);

                // filter out content properties (they're imported later)
                if (valueDataType != null && valueDataType.getName().equals(DataTypeDefinition.CONTENT))
                {
                    continue;
                }

                // get property value
                Serializable value = properties.get(property);
                
                // bind property value to configuration and convert to appropriate type
                if (value instanceof Collection)
                {
                    List<Serializable> boundCollection = new ArrayList<Serializable>();
                    for (Serializable collectionValue : (Collection<Serializable>)value)
                    {
                        Serializable objValue = bindValue(context, property, valueDataType, collectionValue);
                        boundCollection.add(objValue);
                    }
                    value = (Serializable)boundCollection;
                }
                else
                {
                    value = bindValue(context, property, valueDataType, value);
                }

                // choose to provide property on node creation or at end of import for lazy binding
                if (valueDataType != null && (valueDataType.getName().equals(DataTypeDefinition.NODE_REF) || valueDataType.getName().equals(DataTypeDefinition.CATEGORY)))
                {
                    // record node reference for end-of-import binding
                    ImportedNodeRef importedRef = new ImportedNodeRef(context, property, value);
                    nodeRefs.add(importedRef);
                }
                else
                {
                    // property ready to be set on Node creation / update
                    boundProperties.put(property, value);
                }
            }
            
            return boundProperties;
        }
        
        /**
         * Bind permissions - binds authorities
         * 
         * @param properties
         * @return
         */
        private List<AccessPermission> bindPermissions(List<AccessPermission> permissions)
        {
            List<AccessPermission> boundPermissions = new ArrayList<AccessPermission>(permissions.size());
            
            for (AccessPermission permission : permissions)
            {
                AccessPermission ace = new NodeContext.ACE(permission.getAccessStatus(),
                                                           bindPlaceHolder(permission.getAuthority(), binding),
                                                           permission.getPermission());
                boundPermissions.add(ace);
            }
            
            return boundPermissions;
        }

        /**
         * Bind property value
         * 
         * @param valueType  value type
         * @param value  string form of value
         * @return  the bound value
         */
        private Serializable bindValue(ImportNode context, QName property, DataTypeDefinition valueType, Serializable value)
        {
            Serializable objValue = null;
            if (value != null && valueType != null)
            {
                if (value instanceof String)
                {
                    value = bindPlaceHolder(value.toString(), binding);
                }
                if ((valueType.getName().equals(DataTypeDefinition.NODE_REF) || valueType.getName().equals(DataTypeDefinition.CATEGORY)))
                {
                    objValue = value;
                }
                else
                {
                    objValue = (Serializable) DefaultTypeConverter.INSTANCE.convert(valueType, value);
                }
                
            }
            return objValue;
        }

        /**
         * Resolve imported reference relative to specified node
         *  
         * @param sourceNodeRef  context to resolve within
         * @param importedRef  reference to resolve
         * @return
         */
        private NodeRef resolveImportedNodeRef(NodeRef sourceNodeRef, String importedRef)
        {
            // Resolve path to node reference
            NodeRef nodeRef = null;
            importedRef = bindPlaceHolder(importedRef, binding);
            
            if (importedRef.equals("/"))
            {
                nodeRef = sourceNodeRef;
            }
            else if (importedRef.startsWith("/"))
            {
                String path = createValidPath(importedRef);
                List<NodeRef> nodeRefs = searchService.selectNodes(sourceNodeRef, path, null, namespaceService, false);
                if (nodeRefs.size() > 0)
                {
                    nodeRef = nodeRefs.get(0);
                }
            }
            else
            {
            	// determine if node reference
            	if (NodeRef.isNodeRef(importedRef))
            	{
            		nodeRef = new NodeRef(importedRef);
            	}
            	else
            	{
	                // resolve relative path
	                try
	                {
	                    String path = createValidPath(importedRef);
	                    List<NodeRef> nodeRefs = searchService.selectNodes(sourceNodeRef, path, null, namespaceService, false);
	                    if (nodeRefs.size() > 0)
	                    {
	                        nodeRef = nodeRefs.get(0);
	                    }
	                }
	                catch(XPathException e)
	                {
	                    nodeRef = new NodeRef(importedRef);
	                }
	                catch(AlfrescoRuntimeException e1)
	                {
	                    // Note: Invalid reference format - try path search instead
	                }
            	}
            }
            
            return nodeRef;
        }
        
        /**
         * Helper to report start of import
         */
        private void reportStarted()
        {
            if (progress != null)
            {
                progress.started();
            }
        }
        
        /**
         * Helper to report end of import
         */
        private void reportCompleted()
        {
            if (progress != null)
            {
                progress.completed();
            }
        }
        
        /**
         * Helper to report error
         * 
         * @param e
         */
        private void reportError(Throwable e)
        {
            if (progress != null)
            {
                progress.error(e);
            }
        }
        
        /**
         * Helper to report node created progress
         * 
         * @param progress
         * @param childAssocRef
         */
        private void reportNodeCreated(ChildAssociationRef childAssocRef)
        {
            if (progress != null)
            {
                progress.nodeCreated(childAssocRef.getChildRef(), childAssocRef.getParentRef(), childAssocRef.getTypeQName(), childAssocRef.getQName());
            }
        }

        /**
         * Helper to report node linked progress
         * 
         * @param progress
         * @param childAssocRef
         */
        private void reportNodeLinked(NodeRef childRef, NodeRef parentRef, QName assocType, QName childName)
        {
            if (progress != null)
            {
                progress.nodeLinked(childRef, parentRef, assocType, childName);
            }
        }

        /**
         * Helper to report content created progress
         * 
         * @param progress
         * @param nodeRef
         * @param sourceUrl
         */
        private void reportContentCreated(NodeRef nodeRef, String sourceUrl)
        {
            if (progress != null)
            {
                progress.contentCreated(nodeRef, sourceUrl);
            }
        }
        
        /**
         * Helper to report aspect added progress
         *  
         * @param progress
         * @param nodeRef
         * @param aspect
         */
        private void reportAspectAdded(NodeRef nodeRef, QName aspect)
        {
            if (progress != null)
            {
                progress.aspectAdded(nodeRef, aspect);
            }        
        }

        /**
         * Helper to report property set progress
         * 
         * @param progress
         * @param nodeRef
         * @param properties
         */
        private void reportPropertySet(NodeRef nodeRef, Map<QName, Serializable> properties)
        {
            if (progress != null && properties != null)
            {
                for (QName property : properties.keySet())
                {
                    progress.propertySet(nodeRef, property, properties.get(property));
                }
            }
        }

        /**
         * Helper to report permission set progress
         * 
         * @param nodeRef
         * @param permissions
         */
        private void reportPermissionSet(NodeRef nodeRef, List<AccessPermission> permissions)
        {
            if (progress != null && permissions != null)
            {
                for (AccessPermission permission : permissions)
                {
                    progress.permissionSet(nodeRef, permission);
                }
            }
        }
        
        /**
         * Import strategy where imported nodes are always created regardless of whether a
         * node of the same UUID already exists in the repository
         */
        private class CreateNewNodeImporterStrategy implements NodeImporterStrategy
        {
            // force allocation of new UUID, even if one already specified
            private boolean assignNewUUID;
            
            /**
             * Construct
             * 
             * @param newUUID  force allocation of new UUID
             */
            public CreateNewNodeImporterStrategy(boolean assignNewUUID)
            {
                this.assignNewUUID = assignNewUUID;
            }
            
            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node)
            {
                TypeDefinition nodeType = node.getTypeDefinition();
                NodeRef parentRef = node.getParentContext().getParentRef();
                QName assocType = getAssocType(node);
                QName childQName = getChildName(node);
                if (childQName == null)
                {
                    throw new ImporterException("Cannot determine child name of node (type: " + nodeType.getName() + ")");
                }

                // Create initial node (but, first disable behaviour for the node to be created)
                Set<QName> disabledBehaviours = getDisabledBehaviours(node);
                List<QName> alreadyDisabledBehaviours = new ArrayList<QName>(); 
                for (QName disabledBehaviour: disabledBehaviours)
                {
                    boolean alreadyDisabled = behaviourFilter.disableBehaviour(disabledBehaviour);
                    if (alreadyDisabled)
                    {
                        alreadyDisabledBehaviours.add(disabledBehaviour);
                    }
                }
                disabledBehaviours.removeAll(alreadyDisabledBehaviours);
                
                // Build initial map of properties
                Map<QName, Serializable> initialProperties = bindProperties(node);
                
                // Assign UUID if already specified on imported node
                if (!assignNewUUID && node.getUUID() != null)
                {
                    initialProperties.put(ContentModel.PROP_NODE_UUID, node.getUUID());
                }
                
                // Create Node
                ChildAssociationRef assocRef = nodeService.createNode(parentRef, assocType, childQName, nodeType.getName(), initialProperties);
                NodeRef nodeRef = assocRef.getChildRef();

                // Note: non-admin authorities take ownership of new nodes
                if (!(authenticationContext.isCurrentUserTheSystemUser() || authorityService.hasAdminAuthority()))
                {
                    ownableService.takeOwnership(nodeRef);
                }

                // apply permissions
                List<AccessPermission> permissions = null;
                AccessStatus writePermission = permissionService.hasPermission(nodeRef, PermissionService.CHANGE_PERMISSIONS);
                if (authenticationContext.isCurrentUserTheSystemUser() || writePermission.equals(AccessStatus.ALLOWED))
                {
                    permissions = bindPermissions(node.getAccessControlEntries());
                    
                    for (AccessPermission permission : permissions)
                    {
                        permissionService.setPermission(nodeRef, permission.getAuthority(), permission.getPermission(), permission.getAccessStatus().equals(AccessStatus.ALLOWED));
                    }
                    // note: apply inheritance after setting permissions as this may affect whether you can apply permissions
                    boolean inheritPermissions = node.getInheritPermissions();
                    if (!inheritPermissions)
                    {
                        permissionService.setInheritParentPermissions(nodeRef, false);
                    }
                }
                
                // Disable behaviour for the node until the complete node (and its children have been imported)
                for (QName disabledBehaviour : disabledBehaviours)
                {
                    behaviourFilter.enableBehaviour(disabledBehaviour);
                }
                for (QName disabledBehaviour : disabledBehaviours)
                {
                    behaviourFilter.disableBehaviour(nodeRef, disabledBehaviour);
                }
                // TODO: Replace this with appropriate rule/action import handling
                ruleService.disableRules(nodeRef);

                // Report creation
                reportNodeCreated(assocRef);
                reportPropertySet(nodeRef, initialProperties);
                reportPermissionSet(nodeRef, permissions);

                // return newly created node reference
                return nodeRef;
            }
        }
        
        /**
         * Importer strategy where an existing node (one with the same UUID) as a node being
         * imported is first removed.  The imported node is placed in the location specified
         * at import time. 
         */
        private class RemoveExistingNodeImporterStrategy implements NodeImporterStrategy
        {
            private NodeImporterStrategy createNewStrategy = new CreateNewNodeImporterStrategy(false);
            
            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node)
            {                
                // remove existing node, if node to import has a UUID and an existing node of the same
                // uuid already exists
                String uuid = node.getUUID();
                if (uuid != null && uuid.length() > 0)
                {
                    NodeRef existingNodeRef = new NodeRef(rootRef.getStoreRef(), uuid);
                    if (nodeService.exists(existingNodeRef))
                    {
                        // remove primary parent link forcing deletion
                        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(existingNodeRef);
                        
                        // TODO: Check for root node
                        nodeService.removeChild(childAssocRef.getParentRef(), childAssocRef.getChildRef());
                    }
                }
                
                // import as if a new node into current import parent location
                return createNewStrategy.importNode(node);
            }
        }

        /**
         * Importer strategy where an existing node (one with the same UUID) as a node being
         * imported is first removed.  The imported node is placed under the parent of the removed
         * node.
         */        
        private class ReplaceExistingNodeImporterStrategy implements NodeImporterStrategy
        {
            private NodeImporterStrategy createNewStrategy = new CreateNewNodeImporterStrategy(false);

            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node)
            {                
                // replace existing node, if node to import has a UUID and an existing node of the same
                // uuid already exists
                String uuid = node.getUUID();
                if (uuid != null && uuid.length() > 0)
                {
                    NodeRef existingNodeRef = new NodeRef(rootRef.getStoreRef(), uuid);
                    if (nodeService.exists(existingNodeRef))
                    {
                        // remove primary parent link forcing deletion
                        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(existingNodeRef);
                        nodeService.removeChild(childAssocRef.getParentRef(), childAssocRef.getChildRef());
                        
                        // update the parent context of the node being imported to the parent of the node just deleted
                        node.getParentContext().setParentRef(childAssocRef.getParentRef());
                        node.getParentContext().setAssocType(childAssocRef.getTypeQName());
                    }
                }

                // import as if a new node
                return createNewStrategy.importNode(node);
            }
        }
        
        /**
         * Import strategy where an error is thrown when importing a node that has the same UUID
         * of an existing node in the repository.
         */
        private class ThrowOnCollisionNodeImporterStrategy implements NodeImporterStrategy
        {
            private NodeImporterStrategy createNewStrategy = new CreateNewNodeImporterStrategy(false);

            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node)
            {
                // if node to import has a UUID and an existing node of the same uuid already exists
                // then throw an error
                String uuid = node.getUUID();
                if (uuid != null && uuid.length() > 0)
                {
                    NodeRef existingNodeRef = new NodeRef(rootRef.getStoreRef(), uuid);
                    if (nodeService.exists(existingNodeRef))
                    {
                        throw new InvalidNodeRefException("Node " + existingNodeRef + " already exists", existingNodeRef);
                    }
                }
                
                // import as if a new node
                return createNewStrategy.importNode(node);
            }
        }
        
        /**
         * Import strategy where imported nodes are updated if a node with the same UUID
         * already exists in the repository.
         * 
         * Note: this will only allow incremental update of an existing node - it does not
         *       delete properties or associations.
         */
        private class UpdateExistingNodeImporterStrategy implements NodeImporterStrategy
        {
            private NodeImporterStrategy createNewStrategy = new CreateNewNodeImporterStrategy(false);
            
            /*
             *  (non-Javadoc)
             * @see org.alfresco.repo.importer.ImporterComponent.NodeImporterStrategy#importNode(org.alfresco.repo.importer.ImportNode)
             */
            public NodeRef importNode(ImportNode node)
            {
                // replace existing node, if node to import has a UUID and an existing node of the same
                // uuid already exists
                String uuid = node.getUUID();
                if (uuid != null && uuid.length() > 0)
                {
                    NodeRef existingNodeRef = new NodeRef(rootRef.getStoreRef(), uuid);
                    if (nodeService.exists(existingNodeRef))
                    {
                        // do the update
                        Map<QName, Serializable> existingProperties = nodeService.getProperties(existingNodeRef);
                        Map<QName, Serializable> updateProperties = bindProperties(node);
                        if (updateProperties != null && updateProperties.size() > 0)
                        {
                            existingProperties.putAll(updateProperties);
                            nodeService.setProperties(existingNodeRef, existingProperties);
                        }
                        
                        // Apply permissions
                        List<AccessPermission> permissions = null;
                        AccessStatus writePermission = permissionService.hasPermission(existingNodeRef, PermissionService.CHANGE_PERMISSIONS);
                        if (authenticationContext.isCurrentUserTheSystemUser() || writePermission.equals(AccessStatus.ALLOWED))
                        {
                            boolean inheritPermissions = node.getInheritPermissions();
                            if (!inheritPermissions)
                            {
                                permissionService.setInheritParentPermissions(existingNodeRef, false);
                            }
                            
                            permissions = bindPermissions(node.getAccessControlEntries());
                            
                            for (AccessPermission permission : permissions)
                            {
                                permissionService.setPermission(existingNodeRef, permission.getAuthority(), permission.getPermission(), permission.getAccessStatus().equals(AccessStatus.ALLOWED));
                            }
                        }
                        
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Updating existing node " + existingNodeRef + " at " +
                                    nodeService.getPath(existingNodeRef) + " for " + node.toString());
                        }

                        // report update
                        reportPropertySet(existingNodeRef, updateProperties);
                        reportPermissionSet(existingNodeRef, permissions);
                        
                        return existingNodeRef;
                    }
                }
                
                // import as if a new node
                return createNewStrategy.importNode(node);
            }
        }

    }

    /**
     * Imported Node Reference
     * 
     * @author David Caruana
     */
    private static class ImportedNodeRef
    {
        /**
         * Construct
         * 
         * @param context
         * @param property
         * @param value
         */
        private ImportedNodeRef(ImportNode context, QName property, Serializable value)
        {
            this.context = context;
            this.property = property;
            this.value = value;
        }
        
        private ImportNode context;
        private QName property;
        private Serializable value;
    }

    /**
     * Default Import Stream Handler
     * 
     * @author David Caruana
     */
    private static class DefaultStreamHandler
        implements ImportPackageHandler
    {
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#startImport()
         */
        public void startImport()
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportStreamHandler#importStream(java.lang.String)
         */
        public InputStream importStream(String content)
        {
            ResourceLoader loader = new DefaultResourceLoader();
            Resource resource = loader.getResource(content);
            if (resource.exists() == false)
            {
                throw new ImporterException("Content URL " + content + " does not exist.");
            }
            
            try
            {
                return resource.getInputStream();
            }
            catch(IOException e)
            {
                throw new ImporterException("Failed to retrieve input stream for content URL " + content);
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#getDataStream()
         */
        public Reader getDataStream()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#endImport()
         */
        public void endImport()
        {
        }
    }

    /**
     * Default Import Stream Handler
     * 
     * @author David Caruana
     */
    private static class ContentHandlerStreamHandler
        implements ImportPackageHandler
    {
        private ImportContentHandler handler;

        /**
         * Construct
         * 
         * @param handler
         */
        private ContentHandlerStreamHandler(ImportContentHandler handler)
        {
            this.handler = handler;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#startImport()
         */
        public void startImport()
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportStreamHandler#importStream(java.lang.String)
         */
        public InputStream importStream(String content)
        {
            return handler.importStream(content);
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#getDataStream()
         */
        public Reader getDataStream()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImportPackageHandler#endImport()
         */
        public void endImport()
        {
        }
    }
}
