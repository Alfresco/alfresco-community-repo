/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.PathInfo.ElementInfo;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.NodeBinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Centralises access to file/folder/node services and maps between representations.
 * 
 * @author steveglover
 * @author janv
 * @author Jamal Kaabi-Mofrad
 * 
 * @since publicapi1.0
 */
public class NodesImpl implements Nodes
{
    private static enum Type
    {
        // Note: ordered
        DOCUMENT, FOLDER;
    };

    private final static String PATH_ROOT = "-root-";
    private final static String PATH_MY = "-my-";
    private final static String PATH_SHARED = "-shared-";

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private FileFolderService fileFolderService;
    private NamespaceService namespaceService;
    private PermissionService permissionService;
    private Repository repositoryHelper;
    private ServiceRegistry sr;
    private Set<String> defaultIgnoreTypes;
    private Set<QName> ignoreTypeQNames;

    public void init()
    {
        if (defaultIgnoreTypes != null)
        {
            ignoreTypeQNames = new HashSet<>(defaultIgnoreTypes.size());
            for (String type : defaultIgnoreTypes)
            {
                ignoreTypeQNames.add(createQName(type));
            }
        }
    }

    public void setServiceRegistry(ServiceRegistry sr) {
        this.sr = sr;

        this.namespaceService = sr.getNamespaceService();
        this.fileFolderService = sr.getFileFolderService();
        this.nodeService = sr.getNodeService();
        this.permissionService = sr.getPermissionService();
        this.dictionaryService = sr.getDictionaryService();
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    public void setIgnoreTypes(Set<String> ignoreTypes)
    {
        this.defaultIgnoreTypes = ignoreTypes;
    }

    private static final List<QName> EXCLUDED_ASPECTS = Arrays.asList(
            ContentModel.ASPECT_REFERENCEABLE,
            ContentModel.ASPECT_LOCALIZED);

    private static final List<QName> EXCLUDED_PROPS = Arrays.asList(
            // top-level basic info
            ContentModel.PROP_NAME,
            ContentModel.PROP_MODIFIER,
            ContentModel.PROP_MODIFIED,
            ContentModel.PROP_CREATOR,
            ContentModel.PROP_CREATED,
            ContentModel.PROP_CONTENT,
            // sys:localized
            ContentModel.PROP_LOCALE,
            // sys:referenceable
            ContentModel.PROP_NODE_UUID,
            ContentModel.PROP_STORE_IDENTIFIER,
            ContentModel.PROP_STORE_PROTOCOL,
            ContentModel.PROP_NODE_DBID,
            // other - TBC
            ContentModel.PROP_INITIAL_VERSION,
            ContentModel.PROP_AUTO_VERSION_PROPS,
            ContentModel.PROP_AUTO_VERSION);

    private final static String PARAM_ISFOLDER = "isFolder";
    private final static String PARAM_NAME = "name";
    private final static String PARAM_CREATEDAT = "createdAt";
    private final static String PARAM_MODIFIEDAT = "modifiedAt";
    private final static String PARAM_CREATEBYUSER = "createdByUser";
    private final static String PARAM_MODIFIEDBYUSER = "modifiedByUser";
    private final static String PARAM_MIMETYPE = "mimeType";
    private final static String PARAM_SIZEINBYTES = "sizeInBytes";
    private final static String PARAM_NODETYPE = "nodeType";

    private final static Map<String,QName> MAP_PARAM_QNAME;
    static {
        Map<String,QName> aMap = new HashMap<>(9);

        aMap.put(PARAM_ISFOLDER, GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER);
        aMap.put(PARAM_NAME, ContentModel.PROP_NAME);
        aMap.put(PARAM_CREATEDAT, ContentModel.PROP_CREATED);
        aMap.put(PARAM_MODIFIEDAT, ContentModel.PROP_MODIFIED);
        aMap.put(PARAM_CREATEBYUSER, ContentModel.PROP_CREATOR);
        aMap.put(PARAM_MODIFIEDBYUSER, ContentModel.PROP_MODIFIER);
        aMap.put(PARAM_MIMETYPE, GetChildrenCannedQuery.SORT_QNAME_CONTENT_MIMETYPE);
        aMap.put(PARAM_SIZEINBYTES, GetChildrenCannedQuery.SORT_QNAME_CONTENT_SIZE);
        aMap.put(PARAM_NODETYPE, GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE);

        MAP_PARAM_QNAME = Collections.unmodifiableMap(aMap);
    }

    private final static Set<String> LIST_FOLDER_CHILDREN_EQUALS_QUERY_PROPERTIES =
            new HashSet<>(Arrays.asList(new String[] {PARAM_ISFOLDER}));

    /*
     * Note: assumes workspace://SpacesStore
     */
    public NodeRef validateNode(String nodeId)
    {
        return validateNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
    }

    public NodeRef validateNode(StoreRef storeRef, String nodeId)
    {
        String versionLabel = null;

        int idx = nodeId.indexOf(";");
        if (idx != -1)
        {
            versionLabel = nodeId.substring(idx + 1);
            nodeId = nodeId.substring(0, idx);
            if (versionLabel.equals("pwc"))
            {
                // TODO correct exception?
                throw new EntityNotFoundException(nodeId);
            }
        }

        NodeRef nodeRef = new NodeRef(storeRef, nodeId);
        return validateNode(nodeRef);
    }

    public NodeRef validateNode(NodeRef nodeRef)
    {
        if (!nodeService.exists(nodeRef))
        {
            throw new EntityNotFoundException(nodeRef.getId());
        }

        return nodeRef;
    }

    public boolean nodeMatches(NodeRef nodeRef, Set<QName> expectedTypes, Set<QName> excludedTypes)
    {
        if (!nodeService.exists(nodeRef))
        {
            throw new EntityNotFoundException(nodeRef.getId());
        }

        return typeMatches(nodeService.getType(nodeRef), expectedTypes, excludedTypes);
    }

    protected boolean typeMatches(QName type, Set<QName> expectedTypes, Set<QName> excludedTypes)
    {
        Set<QName> allExpectedTypes = new HashSet<>();
        if (expectedTypes != null)
        {
            for (QName expectedType : expectedTypes)
            {
                allExpectedTypes.addAll(dictionaryService.getSubTypes(expectedType, true));
            }
        }

        Set<QName> allExcludedTypes = new HashSet<>();
        if (excludedTypes != null)
        {
            for (QName excludedType : excludedTypes)
            {
                allExcludedTypes.addAll(dictionaryService.getSubTypes(excludedType, true));
            }
        }

        boolean inExpected = allExpectedTypes.contains(type);
        boolean excluded = allExcludedTypes.contains(type);
        return (inExpected && !excluded);
    }

    /**
     * @deprecated review usage (backward compat')
     */
    public Node getNode(String nodeId)
    {
        NodeRef nodeRef = validateNode(nodeId);

        return new Node(nodeRef, null, nodeService.getProperties(nodeRef), sr);
    }

    /**
     * @deprecated review usage (backward compat')
     */
    public Node getNode(NodeRef nodeRef)
    {
        return new Node(nodeRef, null, nodeService.getProperties(nodeRef), sr);
    }

    private Type getType(NodeRef nodeRef)
    {
        return getType(nodeService.getType(nodeRef));
    }

    private Type getType(QName type)
    {
        boolean isContainer = Boolean.valueOf((dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true
                    && !dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER)));
        return isContainer ? Type.FOLDER : Type.DOCUMENT;
    }

    /**
     * @deprecated note: currently required for backwards compat' (Favourites API)
     */
    public Document getDocument(NodeRef nodeRef)
    {
        Type type = getType(nodeRef);
        if (type.equals(Type.DOCUMENT))
        {
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            Document doc = new Document(nodeRef, getParentNodeRef(nodeRef), properties, sr);

            doc.setVersionLabel((String) properties.get(ContentModel.PROP_VERSION_LABEL));
            ContentData cd = (ContentData) properties.get(ContentModel.PROP_CONTENT);
            if (cd != null)
            {
                doc.setSizeInBytes(BigInteger.valueOf(cd.getSize()));
                doc.setMimeType((cd.getMimetype()));
            }

            setCommonProps(doc, nodeRef, properties);
            return doc;
        }
        else
        {
            throw new InvalidArgumentException("Node is not a file");
        }
    }

    private void setCommonProps(Node node, NodeRef nodeRef, Map<QName,Serializable> properties)
    {
        node.setGuid(nodeRef);
        node.setTitle((String)properties.get(ContentModel.PROP_TITLE));
        node.setDescription((String)properties.get(ContentModel.PROP_TITLE));
        node.setModifiedBy((String)properties.get(ContentModel.PROP_MODIFIER));
        node.setCreatedBy((String)properties.get(ContentModel.PROP_CREATOR));
    }

    /**
     * @deprecated note: currently required for backwards compat' (Favourites API)
     */
    public Folder getFolder(NodeRef nodeRef)
    {
        Type type = getType(nodeRef);
        if (type.equals(Type.FOLDER))
        {
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            Folder folder = new Folder(nodeRef, getParentNodeRef(nodeRef), properties, sr);
            setCommonProps(folder, nodeRef, properties);
            return folder;
        }
        else
        {
            throw new InvalidArgumentException("Node is not a folder");
        }
    }

    private NodeRef getParentNodeRef(final NodeRef nodeRef) {

        if (repositoryHelper.getCompanyHome().equals(nodeRef))
        {
            return null; // note: does not make sense to return parent above C/H
        }

        return nodeService.getPrimaryParent(nodeRef).getParentRef();
    }

    private NodeRef validateOrLookupNode(String nodeId, String path)
    {
        NodeRef parentNodeRef;

        if (nodeId.equals(PATH_ROOT))
        {
            parentNodeRef = repositoryHelper.getCompanyHome();
        }
        else if (nodeId.equals(PATH_SHARED))
        {
            parentNodeRef = repositoryHelper.getSharedHome();
        }
        else if (nodeId.equals(PATH_MY))
        {
            NodeRef person = repositoryHelper.getPerson();
            if (person == null)
            {
                throw new InvalidArgumentException("Unexpected: cannot use " + PATH_MY);
            }
            parentNodeRef = repositoryHelper.getUserHome(person);
            if (parentNodeRef == null)
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else
        {
            parentNodeRef = validateNode(nodeId);
        }

        if (path != null)
        {
            // resolve path relative to current nodeId
            parentNodeRef = resolveNodeByPath(parentNodeRef, path, true);
        }

        return parentNodeRef;
    }

    protected NodeRef resolveNodeByPath(final NodeRef parentNodeRef, String path, boolean checkForCompanyHome)
    {
        final List<String> pathElements = new ArrayList<>(0);

        if ((path != null) && (! path.isEmpty())) {

            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            if (! path.isEmpty()) {
                pathElements.addAll(Arrays.asList(path.split("/")));

                if (checkForCompanyHome)
                {
                /*
                if (nodeService.getRootNode(parentNodeRef.getStoreRef()).equals(parentNodeRef)) {
                    // special case
                    NodeRef chNodeRef = repositoryHelper.getCompanyHome();
                    String chName = (String)nodeService.getProperty(chNodeRef, ContentModel.PROP_NAME);
                    if (chName.equals(pathElements.get(0))) {
                        pathElements = pathElements.subList(1, pathElements.size());
                        parentNodeRef = chNodeRef;
                    }
                }
                */
                }
            }
        }

        FileInfo fileInfo = null;
        try {
            if (pathElements.size() != 0) {
                fileInfo = fileFolderService.resolveNamePath(parentNodeRef, pathElements);
            }
            else
            {
                fileInfo = fileFolderService.getFileInfo(parentNodeRef);
                if (fileInfo == null)
                {
                    throw new FileNotFoundException(parentNodeRef);
                }
            }
        }
        catch (FileNotFoundException fnfe) {
            // convert checked exception
            throw new InvalidNodeRefException(fnfe.getMessage()+" ["+path+"]", parentNodeRef);
        }

        return fileInfo.getNodeRef();
    }

    public Node getFolderOrDocument(String nodeId, Parameters parameters)
    {
        String path = parameters.getParameter("path");
        NodeRef nodeRef = validateOrLookupNode(nodeId, path);

        QName typeQName = nodeService.getType(nodeRef);
         List<QName> requestedProperties = createQNames(parameters.getSelectedProperties());
        return getFolderOrDocument(nodeRef, getParentNodeRef(nodeRef), typeQName, requestedProperties, false);
    }

    private Node getFolderOrDocument(final NodeRef nodeRef, NodeRef parentNodeRef, QName typeQName, List<QName> selectedProperties, boolean minimalnfo)
    {
        PathInfo pathInfo = null;
        if (!minimalnfo)
        {
            pathInfo = lookupPathInfo(nodeRef);
        }

        Type type = getType(typeQName);

        Node node;
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        if (type.equals(Type.DOCUMENT))
        {
            node = new Document(nodeRef, parentNodeRef, properties, sr);
        }
        else if (type.equals(Type.FOLDER))
        {
            // container/folder
            node = new Folder(nodeRef, parentNodeRef, properties, sr);
        }
        else
        {
            throw new InvalidArgumentException("Node is not a folder or file");
        }

        if (!minimalnfo)
        {
            node.setProperties(mapProperties(properties, selectedProperties));
            node.setAspectNames(mapAspects(nodeService.getAspects(nodeRef)));
        }

        node.setNodeType(typeQName.toPrefixString(namespaceService));
        node.setPath(pathInfo);

        return node;
    }

    protected PathInfo lookupPathInfo(NodeRef nodeRefIn)
    {
        // TODO which implementation?
        return getPathInfo(nodeRefIn);

        // List<PathInfo.ElementInfo> elements = new ArrayList<>(5);
        //
        // NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
        // boolean isComplete = true;
        //
        // NodeRef pNodeRef = nodeRefIn;
        // while (pNodeRef != null)
        // {
        // if (pNodeRef.equals(companyHomeNodeRef))
        // {
        // pNodeRef = null;
        // }
        // else {
        // pNodeRef = nodeService.getPrimaryParent(pNodeRef).getParentRef();
        //
        // if (pNodeRef == null)
        // {
        // // belts-and-braces - is it even possible to get here ?
        // isComplete = false;
        // }
        // else
        // {
        // if (permissionService.hasPermission(pNodeRef, PermissionService.READ)
        // == AccessStatus.ALLOWED)
        // {
        // String name = (String) nodeService.getProperty(pNodeRef,
        // ContentModel.PROP_NAME);
        // elements.add(0, new ElementInfo(pNodeRef, name));
        // }
        // else
        // {
        // isComplete = false;
        // pNodeRef = null;
        // }
        // }
        // }
        // }
        //
        // StringBuilder sb = new StringBuilder();
        // for (PathInfo.ElementInfo e : elements)
        // {
        // sb.append("/").append(e.getName());
        // }
        //
        // return new PathInfo(sb.toString(), isComplete, elements);
    }

    private PathInfo getPathInfo(NodeRef nodeRef)
    {
        final Path nodePath = nodeService.getPath(nodeRef);

        List<ElementInfo> pathElements = new ArrayList<>();
        Boolean isComplete = Boolean.TRUE;
        // 2 => as we don't want to include the given node in the path as well.
        for (int i = nodePath.size() - 2; i >= 0; i--)
        {
            Element element = nodePath.get(i);
            if (element instanceof Path.ChildAssocElement)
            {
                ChildAssociationRef elementRef = ((Path.ChildAssocElement) element).getRef();
                if (elementRef.getParentRef() != null)
                {
                    NodeRef childNodeRef = elementRef.getChildRef();
                    if (permissionService.hasPermission(childNodeRef, PermissionService.READ) == AccessStatus.ALLOWED)
                    {
                        Serializable nameProp = nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);
                        pathElements.add(0, new ElementInfo(childNodeRef, nameProp.toString()));
                    }
                    else
                    {
                        // Just return the pathInfo up to the location where the user has access
                        isComplete = Boolean.FALSE;
                        break;
                    }
                }
            }
        }

        String pathStr = null;
        if(pathElements.size() > 0)
        {
            StringBuilder sb = new StringBuilder(120);
            for (PathInfo.ElementInfo e : pathElements)
            {
                sb.append("/").append(e.getName());
            }
            pathStr = sb.toString();
        }
        else
        {
            // There is no path element, so set it to null in order to be
            // ignored by Jackson during serialisation
            isComplete = null;
        }
        return new PathInfo(pathStr, isComplete, pathElements);
    }

    protected Map<String, Object> mapProperties(Map<QName, Serializable> nodeProps, List<QName> selectedProperties)
    {
        Map<String, Object> props = null;
        if (!selectedProperties.isEmpty())
        {
            props = new HashMap<>(selectedProperties.size());
            for (QName qName : selectedProperties)
            {
                Serializable value = nodeProps.get(qName);
                if (value != null)
                {
                    props.put(qName.toPrefixString(namespaceService), value);
                }
            }
            if (props.isEmpty())
            {
                props = null; // set to null so it doesn't show up as an empty object in the JSON response.
            }
        }

        return props;
    }

    protected List<String> mapAspects(Set<QName> nodeAspects)
    {
        List<String> aspectNames = new ArrayList<>(nodeAspects.size());

        for (QName aspectName : nodeAspects)
        {
            if (! EXCLUDED_ASPECTS.contains(aspectName))
            {
                aspectNames.add(aspectName.toPrefixString(namespaceService));
            }
        }

        if (aspectNames.size() == 0)
        {
            aspectNames = null; // no aspects to return
        }

        return aspectNames;
    }

    public CollectionWithPagingInfo<Node> getChildren(String parentFolderNodeId, Parameters parameters)
    {
        // TODO do we want to support path with list folder children ?
        String path = null;
        // String path = parameters.getParameter("path");

        final NodeRef parentNodeRef = validateOrLookupNode(parentFolderNodeId, path);

        // TODO
        // map - where (filter) properties - including isFolder
        // map - orderBy (sort) properties - including isFolder

        // TODO refactor & fix !
        final boolean minimalnfo = (parameters.getSelectedProperties().size() == 0);
        final List<QName> requestedProperties = createQNames(parameters.getSelectedProperties());

        boolean includeFolders = true;
        boolean includeFiles = true;

        Query q = parameters.getQuery();

        if (q != null)
        {
            // TODO confirm list of filter props - what about custom props (+ across types/aspects) ?
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(LIST_FOLDER_CHILDREN_EQUALS_QUERY_PROPERTIES, null);
            QueryHelper.walk(q, propertyWalker);

            Boolean b = propertyWalker.getProperty(PARAM_ISFOLDER, WhereClauseParser.EQUALS, Boolean.class);
            if (b != null)
            {
                includeFiles = !b;
                includeFolders = b;
            }
        }

        List<SortColumn> sortCols = parameters.getSorting();
        List<Pair<QName, Boolean>> sortProps = null;
        if ((sortCols != null) && (sortCols.size() > 0))
        {
            sortProps = new ArrayList<>(sortCols.size());
            for (SortColumn sortCol : sortCols)
            {
                QName propQname = MAP_PARAM_QNAME.get(sortCol.column);
                if (propQname == null)
                {
                    propQname = createQName(sortCol.column);
                }

                if (propQname != null)
                {
                    sortProps.add(new Pair<>(propQname, sortCol.asc));
                }
            }
        }
        else
        {
            // default sort order
            sortProps = new ArrayList<>(Arrays.asList(
                    new Pair<>(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, Boolean.FALSE),
                    new Pair<>(ContentModel.PROP_NAME, true)));
        }

        Paging paging = parameters.getPaging();

        if (! nodeMatches(parentNodeRef, Collections.singleton(ContentModel.TYPE_FOLDER), null))
        {
            throw new InvalidArgumentException("NodeId of folder is expected");
        }

        PagingRequest pagingRequest = Util.getPagingRequest(paging);

        final PagingResults<FileInfo> pagingResults = fileFolderService.list(parentNodeRef, includeFiles, includeFolders, ignoreTypeQNames, sortProps, pagingRequest);

        final List<FileInfo> page = pagingResults.getPage();
        List<Node> nodes = new AbstractList<Node>()
        {
            @Override
            public Node get(int index)
            {
                FileInfo fInfo = page.get(index);

                // basic info by default (unless "select"ed otherwise)
                return getFolderOrDocument(fInfo.getNodeRef(), parentNodeRef, fInfo.getType(), requestedProperties, minimalnfo);
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        return CollectionWithPagingInfo.asPaged(paging, nodes, pagingResults.hasMoreItems(), pagingResults.getTotalResultCount().getFirst());
    }

    public void deleteNode(String nodeId)
    {
        NodeRef nodeRef = validateNode(nodeId);
        fileFolderService.delete(nodeRef);
    }

    // TODO should we able to specify content properties (eg. mimeType ... or use extension for now, or encoding)
	public Node createNode(String parentFolderNodeId, Node nodeInfo, Parameters parameters)
	{
        // check that requested parent node exists and it's type is a (sub-)type of folder
        final NodeRef parentNodeRef = validateOrLookupNode(parentFolderNodeId, null);

		if (! nodeMatches(parentNodeRef, Collections.singleton(ContentModel.TYPE_FOLDER), null))
		{
			throw new InvalidArgumentException("NodeId of folder is expected: "+parentNodeRef);
		}

        String nodeName = nodeInfo.getName();
        if ((nodeName == null) || nodeName.isEmpty())
        {
            throw new InvalidArgumentException("Node name is expected: "+parentNodeRef);
        }

        String nodeType = nodeInfo.getNodeType();
        if ((nodeType == null) || nodeType.isEmpty())
        {
            throw new InvalidArgumentException("Node type is expected: "+parentNodeRef+","+nodeName);
        }

        // check that requested type is a (sub-) type of folder or content
        QName nodeTypeQName = createQName(nodeType);

        Set<QName> contentAndFolders = new HashSet<>(Arrays.asList(ContentModel.TYPE_FOLDER, ContentModel.TYPE_CONTENT));
        if (! typeMatches(nodeTypeQName, contentAndFolders, null)) {
            throw new InvalidArgumentException("Type of folder or content is expected: "+ nodeType);
        }

        boolean isContent = typeMatches(nodeTypeQName, Collections.singleton(ContentModel.TYPE_CONTENT), null);

        Map<QName, Serializable> props = new HashMap<>(10);

        if (nodeInfo.getProperties() != null)
        {
            for (Entry<String, Object> entry : nodeInfo.getProperties().entrySet())
            {
                QName propQName = QName.createQName((String)entry.getKey(), namespaceService);
                props.put(propQName, (Serializable)entry.getValue());
            }
        }

        props.put(ContentModel.PROP_NAME, nodeName);

        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(nodeName));
        NodeRef nodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, nodeTypeQName, props).getChildRef();

        if (isContent) {
            // add empty file
            ContentWriter writer = sr.getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            String mimeType = sr.getMimetypeService().guessMimetype(nodeName);
            writer.setMimetype(mimeType);
            writer.putContent("");
        }

        return getFolderOrDocument(nodeRef.getId(), parameters);
	}

    public Node updateNode(String nodeId, Node nodeInfo, Parameters parameters)
    {

        final NodeRef nodeRef = validateNode(nodeId);

        final Set<QName> fileOrFolder = new HashSet<>(Arrays.asList(ContentModel.TYPE_FOLDER, ContentModel.TYPE_CONTENT));
        if (! nodeMatches(nodeRef, fileOrFolder, null))
        {
            throw new InvalidArgumentException("NodeId of file or folder is expected");
        }

        Map<QName, Serializable> props = new HashMap<>(10);

        if (nodeInfo.getProperties() != null)
        {
            for (Entry<String, Object> entry : nodeInfo.getProperties().entrySet())
            {
                QName propQName = QName.createQName((String)entry.getKey(), namespaceService);
                props.put(propQName, (Serializable)entry.getValue());
            }
        }

        String name = nodeInfo.getName();
        if ((name != null) && (! name.isEmpty()))
        {
            // note: this is equivalent of a rename within target folder
            props.put(ContentModel.PROP_NAME, name);
        }

        if (props.size() > 0)
        {
            nodeService.addProperties(nodeRef, props);
        }

        return getFolderOrDocument(nodeRef.getId(), parameters);
    }

    @Override
    public BinaryResource getContent(String fileNodeId, Parameters parameters)
    {
        final NodeRef nodeRef = validateNode(fileNodeId);

        if (! nodeMatches(nodeRef, Collections.singleton(ContentModel.TYPE_CONTENT), null))
        {
            throw new InvalidArgumentException("NodeId of content is expected: "+nodeRef);
        }

        // TODO attachment header - update (or extend ?) REST fwk
        return new NodeBinaryResource(nodeRef, ContentModel.PROP_CONTENT);
    }

    @Override
    public void updateContent(String fileNodeId, BasicContentInfo contentInfo, InputStream stream, Parameters parameters)
    {
        final NodeRef nodeRef = validateNode(fileNodeId);

        if (! nodeMatches(nodeRef, Collections.singleton(ContentModel.TYPE_CONTENT), null))
        {
            throw new InvalidArgumentException("NodeId of content is expected: "+nodeRef);
        }

        ContentWriter writer = sr.getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

        String mimeType = contentInfo.getMimeType();
        if (mimeType == null)
        {
            String fileName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            writer.guessMimetype(fileName);
        }
        else
        {
            writer.setMimetype(mimeType);
        }

        writer.guessEncoding();

        writer.putContent(stream);

        // TODO - hmm - we may wish to return json info !!
        return;
    }

    /**
     * Helper to create a QName from either a fully qualified or short-name QName string
     *
     * @param qnameStr Fully qualified or short-name QName string
     * @return QName
     */
    protected QName createQName(String qnameStr)
    {
        try
        {
            QName qname;
            if (qnameStr.indexOf(QName.NAMESPACE_BEGIN) != -1)
            {
                qname = QName.createQName(qnameStr);
            }
            else
            {
                qname = QName.createQName(qnameStr, namespaceService);
            }
            return qname;
        }
        catch (Exception ex)
        {
            String msg = ex.getMessage();
            if (msg == null)
            {
                msg = "";
            }
            throw new InvalidArgumentException(qnameStr + " isn't a valid QName. " + msg);
        }
    }

    /**
     * Helper to create a QName from either a fully qualified or short-name QName string
     *
     * @param qnameStrList list of fully qualified or short-name QName string
     * @return a list of {@code QName} objects
     */
    protected List<QName> createQNames(List<String> qnameStrList)
    {
        List<QName> result = new ArrayList<>(qnameStrList.size());
        for (String str : qnameStrList)
        {
            str = str.replaceFirst("_", ":"); // FIXME remove this when we have fixed the framework.
            QName name = createQName(str);
            if (!EXCLUDED_PROPS.contains(name))
            {
                result.add(name);
            }
        }
        return result;
    }

}
