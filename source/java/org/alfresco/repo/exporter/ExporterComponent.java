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
package org.alfresco.repo.exporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.view.ExportPackageHandler;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.cmr.view.ReferenceType;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.extensions.surf.util.ParameterCheck;


/**
 * Default implementation of the Exporter Service.
 * 
 * @author David Caruana
 */
public class ExporterComponent
    implements ExporterService
{
    // Supporting services
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private DescriptorService descriptorService;
    private AuthenticationService authenticationService;
    private PermissionService permissionService;
    

    /** Indent Size */
    private int indentSize = 2;
    
    
    /**
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param searchService  the service to perform path searches
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
     * @param descriptorService  the descriptor service
     */
    public void setDescriptorService(DescriptorService descriptorService)
    {
        this.descriptorService = descriptorService;
    }

    /**
     * @param authenticationService  the authentication service
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService; 
    }
    
    /**
     * @param permissionService  the permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExporterService#exportView(java.io.OutputStream, org.alfresco.service.cmr.view.ExporterCrawlerParameters, org.alfresco.service.cmr.view.Exporter)
     */
    public void exportView(OutputStream viewWriter, ExporterCrawlerParameters parameters, Exporter progress)
    {
        ParameterCheck.mandatory("View Writer", viewWriter);
        
        // Construct a basic XML Exporter
        Exporter xmlExporter = createXMLExporter(viewWriter, parameters.getReferenceType());

        // Export
        exportView(xmlExporter, parameters, progress);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExporterService#exportView(org.alfresco.service.cmr.view.ExportPackageHandler, org.alfresco.service.cmr.view.ExporterCrawlerParameters, org.alfresco.service.cmr.view.Exporter)
     */
    public void exportView(ExportPackageHandler exportHandler, ExporterCrawlerParameters parameters, Exporter progress)
    {
        ParameterCheck.mandatory("Stream Handler", exportHandler);

        // create exporter around export handler
        exportHandler.startExport();
        OutputStream dataFile = exportHandler.createDataStream();
        Exporter xmlExporter = createXMLExporter(dataFile, parameters.getReferenceType());
        URLExporter urlExporter = new URLExporter(xmlExporter, exportHandler);

        // export        
        exportView(urlExporter, parameters, progress);
        
        // end export
        exportHandler.endExport();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ExporterService#exportView(org.alfresco.service.cmr.view.Exporter, org.alfresco.service.cmr.view.ExporterCrawler, org.alfresco.service.cmr.view.Exporter)
     */
    public void exportView(Exporter exporter, ExporterCrawlerParameters parameters, Exporter progress)
    {
        ParameterCheck.mandatory("Exporter", exporter);
        
        ChainedExporter chainedExporter = new ChainedExporter(new Exporter[] {exporter, progress});
        DefaultCrawler crawler = new DefaultCrawler();
        crawler.export(parameters, chainedExporter);
    }
    
    /**
     * Create an XML Exporter that exports repository information to the specified
     * output stream in xml format.
     * 
     * @param viewWriter  the output stream to write to
     * @param referenceType  the format of references to export
     * @return  the xml exporter
     */
    private Exporter createXMLExporter(OutputStream viewWriter, ReferenceType referenceType)
    {
        // Define output format
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(indentSize);
        format.setEncoding("UTF-8");

        // Construct an XML Exporter
        try
        {
            XMLWriter writer = new XMLWriter(viewWriter, format);
            ViewXMLExporter exporter = new ViewXMLExporter(namespaceService, nodeService, searchService, dictionaryService, permissionService, writer);
            exporter.setReferenceType(referenceType);
            return exporter;
        }
        catch (UnsupportedEncodingException e)        
        {
            throw new ExporterException("Failed to create XML Writer for export", e);            
        }
        catch (Exception e)        
        {
            throw new ExporterException("Failed to create XML Writer for export", e);            
        }
    }
    
    
    /**
     * Responsible for navigating the Repository from specified location and invoking
     * the provided exporter call-back for the actual export implementation.
     * 
     * @author David Caruana
     */
    private class DefaultCrawler implements ExporterCrawler
    {
        private ExporterContextImpl context;
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterCrawler#export(org.alfresco.service.cmr.view.Exporter)
         */
        public void export(ExporterCrawlerParameters parameters, Exporter exporter)
        {
            // Initialise Crawler
            context = new ExporterContextImpl(parameters);
            exporter.start(context);

            //
            // Export Nodes
            //
            
            while (context.canRetrieve())
            {
                // determine if root repository node
                NodeRef nodeRef = context.getExportOf();
                if (parameters.isCrawlSelf())
                {
                    // export root node of specified export location
                    walkStartNamespaces(parameters, exporter);
                    boolean rootNode = nodeService.getRootNode(nodeRef.getStoreRef()).equals(nodeRef);
                    walkNode(nodeRef, parameters, exporter, rootNode);
                    walkEndNamespaces(parameters, exporter);
                }
                else if (parameters.isCrawlChildNodes())
                {
                    // export child nodes only
                    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);
                    for (ChildAssociationRef childAssoc : childAssocs)
                    {
                        walkStartNamespaces(parameters, exporter);
                        walkNode(childAssoc.getChildRef(), parameters, exporter, false);
                        walkEndNamespaces(parameters, exporter);
                    }
                }

                context.setNextValue();
            }

            //
            // Export associations between nodes
            //
            context.resetContext();
            while (context.canRetrieve())
            {
                Set<NodeRef> nodesWithSecondaryLinks = context.getNodesWithSecondaryLinks();
                if (nodesWithSecondaryLinks != null)
                {
                    //
                    // Export Secondary Links between Nodes
                    //
                    for (NodeRef nodeWithAssociations : nodesWithSecondaryLinks)
                    {
                        walkStartNamespaces(parameters, exporter);
                        walkNodeSecondaryLinks(nodeWithAssociations, parameters, exporter);
                        walkEndNamespaces(parameters, exporter);
                    }
                }

                Set<NodeRef> nodesWithAssociations = context.getNodesWithAssociations();
                if (nodesWithAssociations != null)
                {
                    //
                    // Export Associations between Nodes
                    //
                    for (NodeRef nodeWithAssociations : nodesWithAssociations)
                    {
                        walkStartNamespaces(parameters, exporter);
                        walkNodeAssociations(nodeWithAssociations, parameters, exporter);
                        walkEndNamespaces(parameters, exporter);
                    }
                }
                
                context.setNextValue();
            }

            exporter.end();
        }
        
        /**
         * Call-backs for start of Namespace scope
         */
        private void walkStartNamespaces(ExporterCrawlerParameters parameters, Exporter exporter)
        {
            Collection<String> prefixes = namespaceService.getPrefixes();
            for (String prefix : prefixes)
            {
                if (!prefix.equals("xml"))
                {
                    String uri = namespaceService.getNamespaceURI(prefix);
                    exporter.startNamespace(prefix, uri);
                }
            }
        }
        
        /**
         * Call-backs for end of Namespace scope
         */
        private void walkEndNamespaces(ExporterCrawlerParameters parameters, Exporter exporter)
        {
            Collection<String> prefixes = namespaceService.getPrefixes();
            for (String prefix : prefixes)
            {
                if (!prefix.equals("xml"))
                {
                    exporter.endNamespace(prefix);
                }
            }
        }
        
        /**
         * Navigate a Node.
         * 
         * @param nodeRef  the node to navigate
         */
        private void walkNode(NodeRef nodeRef, ExporterCrawlerParameters parameters, Exporter exporter, boolean exportAsRef)
        {
            // Export node (but only if it's not excluded from export)
            QName type = nodeService.getType(nodeRef);
            if (isExcludedURI(parameters.getExcludeNamespaceURIs(), type.getNamespaceURI()))
            {
                return;
            }
            
            // explicitly included ?
            if (parameters.getIncludedPaths() != null)
            {
            	String nodePathPrefixString = nodeService.getPath(nodeRef).toPrefixString(namespaceService);
            	if (! (isIncludedPath(parameters.getIncludedPaths(), nodePathPrefixString)))
                {
                    return;
                }
            }

            // export node as reference to node, or as the actual node
            if (exportAsRef)
            {
                exporter.startReference(nodeRef, null);
            }
            else
            {
                exporter.startNode(nodeRef);
            }

            // Export node aspects
            exporter.startAspects(nodeRef);
            Set<QName> aspects = nodeService.getAspects(nodeRef);
            for (QName aspect : aspects)
            {
                if (isExcludedURI(parameters.getExcludeNamespaceURIs(), aspect.getNamespaceURI()))
                {
                    continue;
                }
                else if (isExcludedAspect(parameters.getExcludeAspects(), aspect))
                {
                    continue;
                }
                else
                {
                    exporter.startAspect(nodeRef, aspect);
                    exporter.endAspect(nodeRef, aspect);
                }
            }
            exporter.endAspects(nodeRef);
            
            // Export node permissions
            AccessStatus readPermission = permissionService.hasPermission(nodeRef, PermissionService.READ_PERMISSIONS);
            if (authenticationService.isCurrentUserTheSystemUser() || readPermission.equals(AccessStatus.ALLOWED))
            {
                Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
                boolean inheritPermissions = permissionService.getInheritParentPermissions(nodeRef);
                if (permissions.size() > 0 || !inheritPermissions)
                {
                    exporter.startACL(nodeRef);
                    for (AccessPermission permission : permissions)
                    {
                        if(permission.isSetDirectly())
                        {
                            exporter.permission(nodeRef, permission);
                        }
                    }
                    exporter.endACL(nodeRef);
                }
            }
            
            // Export node properties
            exporter.startProperties(nodeRef);
            boolean aware = MLPropertyInterceptor.setMLAware(true);
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
            MLPropertyInterceptor.setMLAware(aware);
            for (QName property : properties.keySet())
            {
                // filter out properties whose namespace is excluded
                if (isExcludedURI(parameters.getExcludeNamespaceURIs(), property.getNamespaceURI()))
                {
                    continue;
                }
                if (isExcludedAspectProperty(parameters.getExcludeAspects(), property))
                {
                    continue;
                }
                
                // filter out properties whose value is null, if not required
                Object value = properties.get(property);
                if (!parameters.isCrawlNullProperties() && value == null)
                {
                    continue;
                }
                
                // start export of property
                exporter.startProperty(nodeRef, property);

                if (value instanceof Collection)
                {
                    exporter.startValueCollection(nodeRef, property);
                    int index = 0;
                    for (Object valueInCollection : (Collection)value)
                    {
                        walkProperty(nodeRef, property, valueInCollection, index, parameters, exporter);
                        index++;
                    }
                    exporter.endValueCollection(nodeRef, property);
                }
                else
                {
                    if (value instanceof MLText)
                    {
                        MLText valueMLT = (MLText) value;
                        Set<Locale> locales = valueMLT.getLocales();
                        for (Locale locale : locales)
                        {
                            String localeValue = valueMLT.getValue(locale);
                            if (localeValue == null)
                            {
                                walkProperty(nodeRef, property, localeValue, -1, parameters, exporter);
                                continue;
                            }
                            exporter.startValueMLText(nodeRef, locale);
                            walkProperty(nodeRef, property, localeValue, -1, parameters, exporter);
                            exporter.endValueMLText(nodeRef);
                        }
                    }
                    else
                    {
                        walkProperty(nodeRef, property, value, -1, parameters, exporter);
                    }
                }

                // end export of property
                exporter.endProperty(nodeRef, property);
            }
            exporter.endProperties(nodeRef);
            
            // Export node children
            if (parameters.isCrawlChildNodes())
            {
            	// sort associations into assoc type buckets filtering out unneccessary associations
                Map<QName, List<ChildAssociationRef>> assocTypes = new HashMap<QName, List<ChildAssociationRef>>();
                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);
                for (ChildAssociationRef childAssoc : childAssocs)
                {
                    QName childAssocType = childAssoc.getTypeQName();
                    if (isExcludedURI(parameters.getExcludeNamespaceURIs(), childAssocType.getNamespaceURI()))
                    {
                        continue;
                    }
                    if (isExcludedChildAssoc(parameters.getExcludeChildAssocs(), childAssocType))
                    {
                        continue;
                    }
                    if (isExcludedAspectAssociation(parameters.getExcludeAspects(), childAssocType))
                    {
                        continue;
                    }
                    if (childAssoc.isPrimary() == false)
                    {
                        context.recordSecondaryLink(nodeRef);
                        continue;
                    }
                    if (isExcludedURI(parameters.getExcludeNamespaceURIs(), childAssoc.getQName().getNamespaceURI()))
                    {
                    	continue;
                    }
                    
                    List<ChildAssociationRef> assocRefs = assocTypes.get(childAssocType);
                    if (assocRefs == null)
                    {
                        assocRefs = new ArrayList<ChildAssociationRef>();
                        assocTypes.put(childAssocType, assocRefs);
                    }
                    assocRefs.add(childAssoc);
                }
                	
                // output each association type bucket
                if (assocTypes.size() > 0)
                {
                    exporter.startAssocs(nodeRef);
                    for (Map.Entry<QName, List<ChildAssociationRef>> assocType : assocTypes.entrySet())
                    {
                        List<ChildAssociationRef> assocRefs = assocType.getValue();
                        if (assocRefs.size() > 0)
                        {
                            exporter.startAssoc(nodeRef, assocType.getKey());
                            for (ChildAssociationRef assocRef : assocRefs)
                            {
                                walkNode(assocRef.getChildRef(), parameters, exporter, false);
                            }
                            exporter.endAssoc(nodeRef, assocType.getKey());
                        }
                    }
                    exporter.endAssocs(nodeRef);
                }
            }
            
            // Export node associations
            if (parameters.isCrawlAssociations())
            {
                List<AssociationRef> associations = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
                if (associations.size() > 0)
                {
                    context.recordAssociation(nodeRef);
                }
            }
            
            // Signal end of node
            // export node as reference to node, or as the actual node
            if (exportAsRef)
            {
                exporter.endReference(nodeRef);
            }
            else
            {
                exporter.endNode(nodeRef);
            }            
        }
        
        /**
         * Export Property
         * 
         * @param nodeRef  
         * @param property
         * @param value
         * @param index
         * @param parameters
         * @param exporter
         */
        private void walkProperty(NodeRef nodeRef, QName property, Object value, int index, ExporterCrawlerParameters parameters, Exporter exporter)
        {
            // determine data type of value
            PropertyDefinition propDef = dictionaryService.getProperty(property);
            DataTypeDefinition dataTypeDef = (propDef == null) ? null : propDef.getDataType();
            QName valueDataType = null;
            if (dataTypeDef == null || dataTypeDef.getName().equals(DataTypeDefinition.ANY))
            {
                dataTypeDef = (value == null) ? null : dictionaryService.getDataType(value.getClass());
                if (dataTypeDef != null)
                {
                    valueDataType = dataTypeDef.getName();
                }
            }
            else
            {
                valueDataType = dataTypeDef.getName();
            }

            if (valueDataType == null || !valueDataType.equals(DataTypeDefinition.CONTENT))
            {
                // Export non content data types
                try
                {
                    exporter.value(nodeRef, property, value, index);
                }
                catch(TypeConversionException e)
                {
                    exporter.warning("Value of property " + property + " could not be converted to xml string");
                    exporter.value(nodeRef, property, (value == null ? null : value.toString()), index);
                }
            }
            else
            {
                // export property of datatype CONTENT
                ContentReader reader = contentService.getReader(nodeRef, property);
                if (!parameters.isCrawlContent() || reader == null || reader.exists() == false)
                {
                    // export an empty url for the content
                    ContentData contentData = (ContentData)value;
                    ContentData noContentURL = null;
                    if (contentData == null)
                    {
                        noContentURL = new ContentData("", null, 0L, "UTF-8");
                    }
                    else
                    {
                        noContentURL = new ContentData("", contentData.getMimetype(), contentData.getSize(), contentData.getEncoding());
                    }
                    exporter.content(nodeRef, property, null, noContentURL, index);
                    exporter.warning("Skipped content for property " + property + " on node " + nodeRef);
                }
                else
                {
                    InputStream inputStream = reader.getContentInputStream();
                    try
                    {
                        exporter.content(nodeRef, property, inputStream, reader.getContentData(), index);
                    }
                    finally
                    {
                        try
                        {
                            inputStream.close();
                        }
                        catch(IOException e)
                        {
                            throw new ExporterException("Failed to export node content for node " + nodeRef, e);
                        }
                    }
                }
            }
        }
        
        /**
         * Export Secondary Links
         * 
         * @param nodeRef
         * @param parameters
         * @param exporter
         */
        private void walkNodeSecondaryLinks(NodeRef nodeRef, ExporterCrawlerParameters parameters, Exporter exporter)
        {
        	// sort associations into assoc type buckets filtering out unneccessary associations
            Map<QName, List<ChildAssociationRef>> assocTypes = new HashMap<QName, List<ChildAssociationRef>>();
            List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef childAssoc : childAssocs)
            {
                // determine if child association should be exported
                QName childAssocType = childAssoc.getTypeQName();
                if (isExcludedURI(parameters.getExcludeNamespaceURIs(), childAssocType.getNamespaceURI()))
                {
                    continue;
                }
                if (isExcludedChildAssoc(parameters.getExcludeChildAssocs(), childAssocType))
                {
                    continue;
                }
                if (isExcludedAspectAssociation(parameters.getExcludeAspects(), childAssocType))
                {
                    continue;
                }
                if (childAssoc.isPrimary())
                {
                    continue;
                }
                if (!isWithinExport(childAssoc.getChildRef(), parameters))
                {
                    continue;
                }
                
                List<ChildAssociationRef> assocRefs = assocTypes.get(childAssocType);
                if (assocRefs == null)
                {
                    assocRefs = new ArrayList<ChildAssociationRef>();
                    assocTypes.put(childAssocType, assocRefs);
                }
                assocRefs.add(childAssoc);
            }
            	
            // output each association type bucket
            if (assocTypes.size() > 0)
            {
                exporter.startReference(nodeRef, null);
                exporter.startAssocs(nodeRef);
                for (Map.Entry<QName, List<ChildAssociationRef>> assocType : assocTypes.entrySet())
                {
                    List<ChildAssociationRef> assocRefs = assocType.getValue();
                    if (assocRefs.size() > 0)
                    {
                        exporter.startAssoc(nodeRef, assocType.getKey());
                        for (ChildAssociationRef assocRef : assocRefs)
                        {
                            exporter.startReference(assocRef.getChildRef(), assocRef.getQName());
                            exporter.endReference(assocRef.getChildRef());
                        }
                        exporter.endAssoc(nodeRef, assocType.getKey());
                    }
                }
                exporter.endAssocs(nodeRef);
                exporter.endReference(nodeRef);
            }
        }
        
        /**
         * Export Node Associations
         * 
         * @param nodeRef
         * @param parameters
         * @param exporter
         */
        private void walkNodeAssociations(NodeRef nodeRef, ExporterCrawlerParameters parameters, Exporter exporter)
        {
        	// sort associations into assoc type buckets filtering out unneccessary associations
            Map<QName, List<AssociationRef>> assocTypes = new HashMap<QName, List<AssociationRef>>();
            List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
            for (AssociationRef assoc : assocs)
            {
                QName assocType = assoc.getTypeQName();
                if (isExcludedURI(parameters.getExcludeNamespaceURIs(), assocType.getNamespaceURI()))
                {
                    continue;
                }
                if (!isWithinExport(assoc.getTargetRef(), parameters))
                {
                    continue;
                }
                
                List<AssociationRef> assocRefs = assocTypes.get(assocType);
                if (assocRefs == null)
                {
                    assocRefs = new ArrayList<AssociationRef>();
                    assocTypes.put(assocType, assocRefs);
                }
                assocRefs.add(assoc);
            }
            	
            // output each association type bucket
            if (assocTypes.size() > 0)
            {
                exporter.startReference(nodeRef, null);
                exporter.startAssocs(nodeRef);
                for (Map.Entry<QName, List<AssociationRef>> assocType : assocTypes.entrySet())
                {
                    List<AssociationRef> assocRefs = assocType.getValue();
                    if (assocRefs.size() > 0)
                    {
                        exporter.startAssoc(nodeRef, assocType.getKey());
                        for (AssociationRef assocRef : assocRefs)
                        {
                            exporter.startReference(assocRef.getTargetRef(), null);
                            exporter.endReference(assocRef.getTargetRef());
                        }
                        exporter.endAssoc(nodeRef, assocType.getKey());
                    }
                }
                exporter.endAssocs(nodeRef);
                exporter.endReference(nodeRef);
            }
        }

        /**
         * Is the specified URI an excluded URI?
         * 
         * @param uri  the URI to test
         * @return  true => it's excluded from the export
         */
        private boolean isExcludedURI(String[] excludeNamespaceURIs, String uri)
        {
            for (String excludedURI : excludeNamespaceURIs)
            {
                if (uri.equals(excludedURI))
                {
                    return true;
                }
            }
            return false;
        }
        
        private boolean isIncludedPath(String[] includedPaths, String path)
        {
            for (String includePath : includedPaths)
            {
                // note: allow parents or children - e.g. if included path is /a/b/c then /, /a, /a/b, /a/b/c, /a/b/c/d, /a/b/c/d/e are all included         	 
                if (includePath.startsWith(path) || path.startsWith(includePath))
                {
                    return true;
                }
            }

            return false;
        }
        
        
        /**
         * Is the aspect unexportable?
         * 
         * @param aspectQName           the aspect name
         * @return                      <tt>true</tt> if the aspect can't be exported
         */
        private boolean isExcludedAspect(QName[] excludeAspects, QName aspectQName)
        {
            if (aspectQName.equals(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT) ||
                    aspectQName.equals(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
            {
                return true;
            }
            else
            {
                for (QName excludeAspect : excludeAspects)
                {
                    if (aspectQName.equals(excludeAspect))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Is the child association unexportable?
         * 
         * @param childAssocQName           the child assoc name
         * @return                      <tt>true</tt> if the aspect can't be exported
         */
        private boolean isExcludedChildAssoc(QName[] excludeChildAssocs, QName childAssocQName)
        {
            for (QName excludeChildAssoc : excludeChildAssocs)
            {
                if (childAssocQName.equals(excludeChildAssoc))
                {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Is the property unexportable?
         */
        private boolean isExcludedAspectProperty(QName[] excludeAspects, QName propertyQName)
        {
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
            if (propDef == null)
            {
                return false;
            }
            
            ClassDefinition classDef = propDef.getContainerClass();
            if (classDef == null || !classDef.isAspect())
            {
                return false;
            }
            
            return isExcludedAspect(excludeAspects, classDef.getName());
        }

        /**
         * Is the association unexportable?
         */
        private boolean isExcludedAspectAssociation(QName[] excludeAspects, QName associationQName)
        {
            AssociationDefinition assocDef = dictionaryService.getAssociation(associationQName);
            if (assocDef == null)
            {
                return false;
            }
            
            ClassDefinition classDef = assocDef.getSourceClass();
            if (classDef == null || !classDef.isAspect())
            {
                return false;
            }
            
            return isExcludedAspect(excludeAspects, classDef.getName());
        }
        
        /**
         * Determine if specified Node Reference is within the set of nodes to be exported
         * 
         * @param nodeRef  node reference to check
         * @return  true => node reference is within export set
         */
        private boolean isWithinExport(NodeRef nodeRef, ExporterCrawlerParameters parameters)
        {
            boolean isWithin = false;
            
            // Current strategy is to determine if node is a child of the root exported node
            for (NodeRef exportRoot : context.getExportList())
            {
                if (nodeRef.equals(exportRoot) && parameters.isCrawlSelf() == true)
                {
                    // node to export is the root export node (and root is to be exported)
                    isWithin = true;
                }
                else
                {
                    // locate export root in primary parent path of node
                    Path nodePath = nodeService.getPath(nodeRef);
                    for (int i = nodePath.size() -1; i >= 0; i--)
                    {
                        Path.ChildAssocElement pathElement = (Path.ChildAssocElement)nodePath.get(i);
                        if (pathElement.getRef().getChildRef().equals(exportRoot))
                        {
                            isWithin = true;
                            break;
                        }
                    }
                }
            }
            return isWithin;
        }
    }


    /**
     * Exporter Context
     */
    private class ExporterContextImpl implements ExporterContext
    {
        private NodeRef[] exportList;
        private NodeRef[] parentList;
        private String exportedBy;
        private Date exportedDate;
        private String exporterVersion;
        
        private Map<Integer, Set<NodeRef>> nodesWithSecondaryLinks = new HashMap<Integer, Set<NodeRef>>();
        private Map<Integer, Set<NodeRef>> nodesWithAssociations = new HashMap<Integer, Set<NodeRef>>();
        
        private int index;
        
        
        /**
         * Construct
         * 
         * @param parameters  exporter crawler parameters
         */
        public ExporterContextImpl(ExporterCrawlerParameters parameters)
        {
            index = 0;
            
            // get current user performing export
            String currentUserName = authenticationService.getCurrentUserName();
            exportedBy = (currentUserName == null) ? "unknown" : currentUserName;

            // get current date
            exportedDate = new Date(System.currentTimeMillis());

            // get list of exported nodes
            exportList = (parameters.getExportFrom() == null) ? null : parameters.getExportFrom().getNodeRefs();
            if (exportList == null)
            {
                // multi-node export
                exportList = new NodeRef[1];
                NodeRef exportOf = getNodeRef(parameters.getExportFrom());
                exportList[0] = exportOf;
            }
            parentList = new NodeRef[exportList.length];
            for (int i = 0; i < exportList.length; i++)
            {
                parentList[i] = getParent(exportList[i], parameters.isCrawlSelf());
            }
            
            // get exporter version
            exporterVersion = descriptorService.getServerDescriptor().getVersion();
        }
        
        public boolean canRetrieve()
        {
            return index < exportList.length;
        }
        
        public int setNextValue()
        {
            return ++index;
        }
        
        public void resetContext()
        {
            index = 0;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportedBy()
         */
        public String getExportedBy()
        {
            return exportedBy;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportedDate()
         */
        public Date getExportedDate()
        {
            return exportedDate;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExporterVersion()
         */
        public String getExporterVersion()
        {
            return exporterVersion;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportOf()
         */
        public NodeRef getExportOf()
        {
            if (canRetrieve())
            {
                return exportList[index];
            }
            return null;
        }
        
        /*
         * (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportParent()
         */
        public NodeRef getExportParent()
        {
            if (canRetrieve())
            {
                return parentList[index];
            }
            return null;
        }
        
        /*
         * (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportList()
         */
        public NodeRef[] getExportList()
        {
            return exportList;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ExporterContext#getExportParentList()
         */
        public NodeRef[] getExportParentList()
        {
            return parentList;
        }

        /**
         * Record that associations exist for node
         * 
         * @param nodeRef
         */
        public void recordAssociation(NodeRef nodeRef)
        {
            Set<NodeRef> nodes = nodesWithAssociations.get(index);
            if (nodes == null)
            {
                nodes = new HashSet<NodeRef>();
                nodesWithAssociations.put(index, nodes);
            }
            nodes.add(nodeRef);
        }

        /**
         * Gets nodes that have been recorded with associations
         * 
         * @return
         */
        public Set<NodeRef> getNodesWithAssociations()
        {
            Set<NodeRef> nodes = nodesWithAssociations.get(index);
            if (nodes != null)
            {
                return nodes;
            }
            return null;
        }

        /**
         * Record that secondary links exist for node
         * 
         * @param nodeRef
         */
        public void recordSecondaryLink(NodeRef nodeRef)
        {
            Set<NodeRef> nodes = nodesWithSecondaryLinks.get(index);
            if (nodes == null)
            {
                nodes = new HashSet<NodeRef>();
                nodesWithSecondaryLinks.put(index, nodes);
            }
            nodes.add(nodeRef);
        }

        /**
         * Gets nodes that have been recorded with secondary links
         * 
         * @return
         */
        public Set<NodeRef> getNodesWithSecondaryLinks()
        {
            Set<NodeRef> nodes = nodesWithSecondaryLinks.get(index);
            if (nodes != null)
            {
                return nodes;
            }
            return null;
        }
        
        /**
         * Get the Node Ref from the specified Location
         * 
         * @param location  the location
         * @return  the node reference
         */
        private NodeRef getNodeRef(Location location)
        {
            ParameterCheck.mandatory("Location", location);
        
            // Establish node to export from
            NodeRef nodeRef = (location == null) ? null : location.getNodeRef();
            if (nodeRef == null)
            {
                // If a specific node has not been provided, default to the root
                nodeRef = nodeService.getRootNode(location.getStoreRef());
            }
        
            // Resolve to path within node, if one specified
            String path = (location == null) ? null : location.getPath();
            if (path != null && path.length() >0)
            {
                // Create a valid path and search
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
         * Gets the parent node of the items to be exported
         * 
         * @param exportOf
         * @param exportSelf
         * @return
         */
        private NodeRef getParent(NodeRef exportOf, boolean exportSelf)
        {
            NodeRef parent = null;
            
            if (exportSelf)
            {
                NodeRef rootNode = nodeService.getRootNode(exportOf.getStoreRef());
                if (rootNode.equals(exportOf))
                {
                    parent = exportOf;
                }
                else
                {
                    ChildAssociationRef parentRef = nodeService.getPrimaryParent(exportOf);
                    parent = parentRef.getParentRef();
                }
            }
            else
            {
                parent = exportOf;
            }
            
            return parent;
        }

    }
    
}
