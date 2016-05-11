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

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.ContentLimitViolationException;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.ContentInfo;
import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.PathInfo.ElementInfo;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RequestEntityTooLargeException;
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
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Centralises access to file/folder/node services and maps between representations.
 *
 * Note:
 * This class was originally used for returning some basic node info when listing Favourites.
 * It has now been re-purposed and extended to implement the new File Folder (RESTful) API.
 * 
 * @author steveglover
 * @author janv
 * @author Jamal Kaabi-Mofrad
 * 
 * @since publicapi1.0
 */
public class NodesImpl implements Nodes
{
    private static final Log logger = LogFactory.getLog(NodesImpl.class);

    private static enum Type
    {
        // Note: ordered
        DOCUMENT, FOLDER
    }

    private final static String PARAM_RELATIVE_PATH = "relativePath"; // TODO wip

    private final static String PARAM_SELECT_PROPERTIES = "properties";
    private final static String PARAM_SELECT_PATH = "path";
    private final static String PARAM_SELECT_ASPECTNAMES = "aspectNames";
    private final static String PARAM_SELECT_ISLINK = "isLink";

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private FileFolderService fileFolderService;
    private NamespaceService namespaceService;
    private PermissionService permissionService;
    private MimetypeService mimetypeService;
    private ContentService contentService;
    private ActionService actionService;
    private VersionService versionService;
    private QuickShareService quickShareService;
    private Repository repositoryHelper;
    private ServiceRegistry sr;
    private Set<String> defaultIgnoreTypes;
    private Set<QName> ignoreTypeQNames;

    private Set<String> nonAttachContentTypes = Collections.emptySet(); // pre-configured whitelist, eg. images & pdf

    public void setNonAttachContentTypes(Set<String> nonAttachWhiteList)
    {
        this.nonAttachContentTypes = nonAttachWhiteList;
    }

    public void init()
    {
        this.namespaceService = sr.getNamespaceService();
        this.fileFolderService = sr.getFileFolderService();
        this.nodeService = sr.getNodeService();
        this.permissionService = sr.getPermissionService();
        this.dictionaryService = sr.getDictionaryService();
        this.mimetypeService = sr.getMimetypeService();
        this.contentService = sr.getContentService();
        this.actionService = sr.getActionService();
        this.versionService = sr.getVersionService();

        if (defaultIgnoreTypes != null)
        {
            ignoreTypeQNames = new HashSet<>(defaultIgnoreTypes.size());
            for (String type : defaultIgnoreTypes)
            {
                ignoreTypeQNames.add(createQName(type));
            }
        }
    }

    public void setServiceRegistry(ServiceRegistry sr)
    {
        this.sr = sr;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    public void setQuickShareService(QuickShareService quickShareService)
    {
        this.quickShareService = quickShareService;
    }

    public void setIgnoreTypes(Set<String> ignoreTypes)
    {
        this.defaultIgnoreTypes = ignoreTypes;
    }

    // excluded namespaces (aspects and properties)
    private static final List<String> EXCLUDED_NS = Arrays.asList(NamespaceService.SYSTEM_MODEL_1_0_URI);

    // excluded aspects
    private static final List<QName> EXCLUDED_ASPECTS = Arrays.asList();

    // excluded properties
    private static final List<QName> EXCLUDED_PROPS = Arrays.asList(
            // top-level minimal info
            ContentModel.PROP_NAME,
            ContentModel.PROP_MODIFIER,
            ContentModel.PROP_MODIFIED,
            ContentModel.PROP_CREATOR,
            ContentModel.PROP_CREATED,
            ContentModel.PROP_CONTENT,
            // other - TBC
            ContentModel.PROP_INITIAL_VERSION,
            ContentModel.PROP_AUTO_VERSION_PROPS,
            ContentModel.PROP_AUTO_VERSION);

    private static final List<QName> PROPS_USERLOOKUP = Arrays.asList(
            ContentModel.PROP_CREATOR,
            ContentModel.PROP_MODIFIER,
            ContentModel.PROP_OWNER,
            ContentModel.PROP_LOCK_OWNER,
            ContentModel.PROP_WORKING_COPY_OWNER);

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
    static
    {
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

        return new Node(nodeRef, null, nodeService.getProperties(nodeRef), null, sr);
    }

    /**
     * @deprecated review usage (backward compat')
     */
    public Node getNode(NodeRef nodeRef)
    {
        return new Node(nodeRef, null, nodeService.getProperties(nodeRef), null, sr);
    }

    private Type getType(NodeRef nodeRef)
    {
        return getType(nodeService.getType(nodeRef), nodeRef);
    }

    private Type getType(QName typeQName, NodeRef nodeRef)
    {
        // quick check for common types
        if (typeQName.equals(ContentModel.TYPE_FOLDER) || typeQName.equals(ApplicationModel.TYPE_FOLDERLINK))
        {
            return Type.FOLDER;
        }
        else if (typeQName.equals(ContentModel.TYPE_CONTENT) || typeQName.equals(ApplicationModel.TYPE_FILELINK))
        {
            return Type.DOCUMENT;
        }

        // further checks

        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_LINK))
        {
            if (dictionaryService.isSubClass(typeQName, ApplicationModel.TYPE_FOLDERLINK))
            {
                return Type.FOLDER;
            }
            else if (dictionaryService.isSubClass(typeQName, ApplicationModel.TYPE_FILELINK))
            {
                return Type.DOCUMENT;
            }

            NodeRef linkNodeRef = (NodeRef)nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
            if (linkNodeRef != null)
            {
                try
                {
                    typeQName = nodeService.getType(linkNodeRef);
                    // drop-through to check type of destination
                    // note: edge-case - if link points to another link then we will return null
                }
                catch (InvalidNodeRefException inre)
                {
                    // ignore
                }
            }
        }

        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER))
        {
            if (! dictionaryService.isSubClass(typeQName, ContentModel.TYPE_SYSTEM_FOLDER))
            {
                return Type.FOLDER;
            }
            return null; // unknown
        }
        else if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            return Type.DOCUMENT;
        }

        return null; // unknown
    }

    /**
     * @deprecated note: currently required for backwards compat' (Favourites API)
     */
    public Document getDocument(NodeRef nodeRef)
    {
        Type type = getType(nodeRef);
        if ((type != null) && type.equals(Type.DOCUMENT))
        {
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            Document doc = new Document(nodeRef, getParentNodeRef(nodeRef), properties, null, sr);

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
        if ((type != null) && type.equals(Type.FOLDER))
        {
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            Folder folder = new Folder(nodeRef, getParentNodeRef(nodeRef), properties, null, sr);
            setCommonProps(folder, nodeRef, properties);
            return folder;
        }
        else
        {
            throw new InvalidArgumentException("Node is not a folder");
        }
    }

    private NodeRef getParentNodeRef(NodeRef nodeRef)
    {
        if (repositoryHelper.getCompanyHome().equals(nodeRef))
        {
            return null; // note: does not make sense to return parent above C/H
        }

        return nodeService.getPrimaryParent(nodeRef).getParentRef();
    }

    protected NodeRef validateOrLookupNode(String nodeId, String path)
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

        if ((path != null) && (! path.isEmpty()))
        {

            if (path.startsWith("/"))
            {
                path = path.substring(1);
            }

            if (! path.isEmpty())
            {
                pathElements.addAll(Arrays.asList(path.split("/")));

                if (checkForCompanyHome)
                {
                /*
                if (nodeService.getRootNode(parentNodeRef.getStoreRef()).equals(parentNodeRef))
                {
                    // special case
                    NodeRef chNodeRef = repositoryHelper.getCompanyHome();
                    String chName = (String)nodeService.getProperty(chNodeRef, ContentModel.PROP_NAME);
                    if (chName.equals(pathElements.get(0)))
                    {
                        pathElements = pathElements.subList(1, pathElements.size());
                        parentNodeRef = chNodeRef;
                    }
                }
                */
                }
            }
        }

        FileInfo fileInfo = null;
        try
        {
            if (pathElements.size() != 0)
            {
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
        catch (FileNotFoundException fnfe)
        {
            // convert checked exception
            throw new EntityNotFoundException(fnfe.getMessage()+" ["+parentNodeRef+","+path+"]");
        }

        return fileInfo.getNodeRef();
    }

    public Node getFolderOrDocument(String nodeId, Parameters parameters)
    {
        String path = parameters.getParameter(PARAM_RELATIVE_PATH);
        NodeRef nodeRef = validateOrLookupNode(nodeId, path);

        QName typeQName = nodeService.getType(nodeRef);

        return getFolderOrDocumentFullInfo(nodeRef, getParentNodeRef(nodeRef), typeQName, parameters);
    }

    private Node getFolderOrDocumentFullInfo(NodeRef nodeRef, NodeRef parentNodeRef, QName nodeTypeQName, Parameters parameters)
    {
        List<String> selectParam = new ArrayList<>();
        selectParam.addAll(parameters.getSelectedProperties());

        // Add basic info for single get (above & beyond minimal that is used for listing collections)
        selectParam.add(PARAM_SELECT_ASPECTNAMES);
        selectParam.add(PARAM_SELECT_PROPERTIES);

        return getFolderOrDocument(nodeRef, parentNodeRef, nodeTypeQName, selectParam, null);
    }

    private Node getFolderOrDocument(final NodeRef nodeRef, NodeRef parentNodeRef, QName nodeTypeQName, List<String> selectParam, Map<String,UserInfo> mapUserInfo)
    {
        if (mapUserInfo == null)
        {
            mapUserInfo = new HashMap<>(2);
        }

        PathInfo pathInfo = null;
        if (selectParam.contains(PARAM_SELECT_PATH))
        {
            pathInfo = lookupPathInfo(nodeRef);
        }

        Type type = getType(nodeTypeQName, nodeRef);

        Node node;
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        if (type == null)
        {
            // unknown type
            node = new Document(nodeRef, parentNodeRef, properties, mapUserInfo, sr);
            node.setIsFolder(null);
        }
        else if (type.equals(Type.DOCUMENT))
        {
            node = new Document(nodeRef, parentNodeRef, properties, mapUserInfo, sr);
        }
        else if (type.equals(Type.FOLDER))
        {
            // container/folder
            node = new Folder(nodeRef, parentNodeRef, properties, mapUserInfo, sr);
        }
        else
        {
            throw new RuntimeException("Unexpected - should not reach here");
        }

        if (selectParam.size() > 0)
        {
            node.setProperties(mapFromNodeProperties(properties, selectParam, mapUserInfo));
        }

        if (selectParam.contains(PARAM_SELECT_ASPECTNAMES))
        {
            node.setAspectNames(mapFromNodeAspects(nodeService.getAspects(nodeRef)));
        }

        if (selectParam.contains(PARAM_SELECT_ISLINK))
        {
            boolean isLink = typeMatches(nodeTypeQName, Collections.singleton(ContentModel.TYPE_LINK), null);
            node.setIsLink(isLink);
        }
        
        node.setNodeType(nodeTypeQName.toPrefixString(namespaceService));
        node.setPath(pathInfo);

        return node;
    }
    
    protected PathInfo lookupPathInfo(NodeRef nodeRefIn)
    {
        final Path nodePath = nodeService.getPath(nodeRefIn);

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
        if (pathElements.size() > 0)
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
    
    protected Set<QName> mapToNodeAspects(List<String> aspectNames)
    {
    	Set<QName> nodeAspects = new HashSet<>(aspectNames.size());

        for (String aspectName : aspectNames)
        {
            QName aspectQName = createQName(aspectName);

            AspectDefinition ad = dictionaryService.getAspect(aspectQName);
            if (ad != null)
            {
                nodeAspects.add(aspectQName);
            }
            else 
            {
            	throw new InvalidArgumentException("Unknown aspect: "+aspectName);
            }
        }

        return nodeAspects;
    }

    protected Map<QName, Serializable> mapToNodeProperties(Map<String, Object> props)
    {
        Map<QName, Serializable> nodeProps = new HashMap<>(props.size());

        for (Entry<String, Object> entry : props.entrySet())
        {
        	String propName = entry.getKey();
            QName propQName = createQName(propName);

            PropertyDefinition pd = dictionaryService.getProperty(propQName);
            if (pd != null)
            {
                Serializable value;
                if (pd.getDataType().getName().equals(DataTypeDefinition.NODE_REF))
                {
                    String nodeRefString = (String) entry.getValue();
                    if (! NodeRef.isNodeRef(nodeRefString))
                    {
                        value = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeRefString);
                    }
                    else
                    {
                        value = new NodeRef(nodeRefString);
                    }
                }
                else
                {
                    value = (Serializable)entry.getValue();
                }
                nodeProps.put(propQName, value);
            }
            else 
            {
            	throw new InvalidArgumentException("Unknown property: "+propName);
            }
        }

        return nodeProps;
    }

    protected Map<String, Object> mapFromNodeProperties(Map<QName, Serializable> nodeProps, List<String> selectParam, Map<String,UserInfo> mapUserInfo)
    {
        List<QName> selectedProperties;

        if ((selectParam.size() == 0) || selectParam.contains(PARAM_SELECT_PROPERTIES))
        {
            // return all properties
            selectedProperties = new ArrayList<>(nodeProps.size());
            for (QName propQName : nodeProps.keySet())
            {
                if ((! EXCLUDED_NS.contains(propQName.getNamespaceURI())) && (! EXCLUDED_PROPS.contains(propQName)))
                {
                    selectedProperties.add(propQName);
                }
            }
        }
        else
        {
            // return selected properties
            selectedProperties = createQNames(selectParam);
        }

        Map<String, Object> props = null;
        if (!selectedProperties.isEmpty())
        {
            props = new HashMap<>(selectedProperties.size());

            for (QName qName : selectedProperties)
            {
                Serializable value = nodeProps.get(qName);
                if (value != null)
                {
                    if (PROPS_USERLOOKUP.contains(qName))
                    {
                        value = Node.lookupUserInfo((String)value, mapUserInfo, sr.getPersonService());
                    }
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

    protected List<String> mapFromNodeAspects(Set<QName> nodeAspects)
    {
        List<String> aspectNames = new ArrayList<>(nodeAspects.size());

        for (QName aspectQName : nodeAspects)
        {
            if ((! EXCLUDED_NS.contains(aspectQName.getNamespaceURI())) && (! EXCLUDED_ASPECTS.contains(aspectQName)))
            {
                aspectNames.add(aspectQName.toPrefixString(namespaceService));
            }
        }

        if (aspectNames.size() == 0)
        {
            aspectNames = null; // no aspects to return
        }

        return aspectNames;
    }

    public CollectionWithPagingInfo<Node> listChildren(String parentFolderNodeId, Parameters parameters)
    {
        final NodeRef parentNodeRef = validateOrLookupNode(parentFolderNodeId, null);

        final List<String> selectParam = parameters.getSelectedProperties();

        boolean includeFolders = true;
        boolean includeFiles = true;

        Query q = parameters.getQuery();

        if (q != null)
        {
            // TODO confirm list of filter props - what about custom props (+ across types/aspects) ? What about VF extension ?
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

        final Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        final List<FileInfo> page = pagingResults.getPage();
        List<Node> nodes = new AbstractList<Node>()
        {
            @Override
            public Node get(int index)
            {
                FileInfo fInfo = page.get(index);

                // minimal info by default (unless "select"ed otherwise)
                return getFolderOrDocument(fInfo.getNodeRef(), parentNodeRef, fInfo.getType(), selectParam, mapUserInfo);
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

        // node name - mandatory
        String nodeName = nodeInfo.getName();
        if ((nodeName == null) || nodeName.isEmpty())
        {
            throw new InvalidArgumentException("Node name is expected: "+parentNodeRef);
        }

        // node type - check that requested type is a (sub-) type of folder or content
        String nodeType = nodeInfo.getNodeType();
        if ((nodeType == null) || nodeType.isEmpty())
        {
            throw new InvalidArgumentException("Node type is expected: "+parentNodeRef+","+nodeName);
        }

        QName nodeTypeQName = createQName(nodeType);

        Set<QName> contentAndFolders = new HashSet<>(
                Arrays.asList(ContentModel.TYPE_FOLDER, ContentModel.TYPE_CONTENT, ContentModel.TYPE_LINK));

        if (! typeMatches(nodeTypeQName, contentAndFolders, null))
        {
            throw new InvalidArgumentException("Type of cm:folder cm:content or cm:link is expected: "+ nodeType);
        }

        boolean isContent = typeMatches(nodeTypeQName, Collections.singleton(ContentModel.TYPE_CONTENT), null);

        Map<QName, Serializable> props = new HashMap<>(1);

        if (nodeInfo.getProperties() != null)
        {
            // node properties - set any additional properties
            props = mapToNodeProperties(nodeInfo.getProperties());
        }

        // Create the node
        NodeRef nodeRef = createNodeImpl(parentNodeRef, nodeName, nodeTypeQName, props);

        List<String> aspectNames = nodeInfo.getAspectNames();
        if (aspectNames != null)
        {
            // node aspects - set any additional aspects
        	Set<QName> aspectQNames = mapToNodeAspects(aspectNames);
            for (QName aspectQName : aspectQNames)
            {
                if (EXCLUDED_ASPECTS.contains(aspectQName) || aspectQName.equals(ContentModel.ASPECT_AUDITABLE))
                {
                    continue; // ignore
                }

                nodeService.addAspect(nodeRef, aspectQName, null);
            }
        }

        if (isContent)
        {
            // add empty file
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            String mimeType;
            ContentInfo contentInfo = nodeInfo.getContent();
            if (contentInfo != null && contentInfo.getMimeType() != null)
            {
                mimeType = contentInfo.getMimeType();
            }
            else
            {
                mimeType = mimetypeService.guessMimetype(nodeName);
            }
            writer.setMimetype(mimeType);
            writer.putContent("");
        }

        return getFolderOrDocument(nodeRef.getId(), parameters);
    }

    private NodeRef createNodeImpl(NodeRef parentNodeRef, String nodeName, QName nodeTypeQName, Map<QName, Serializable> props)
    {
        if (props == null)
        {
            props = new HashMap<>(1);
        }
        props.put(ContentModel.PROP_NAME, nodeName);

        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(nodeName));
        try
        {
            return nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, nodeTypeQName, props).getChildRef();
        }
        catch (DuplicateChildNodeNameException dcne)
        {
            // duplicate - name clash
            throw new ConstraintViolatedException(dcne.getMessage());
        }
    }

    public Node updateNode(String nodeId, Node nodeInfo, Parameters parameters)
    {
        final NodeRef nodeRef = validateNode(nodeId);

        QName nodeTypeQName = nodeService.getType(nodeRef);

        final Set<QName> fileOrFolder = new HashSet<>(Arrays.asList(ContentModel.TYPE_FOLDER, ContentModel.TYPE_CONTENT));
        if (! typeMatches(nodeTypeQName, fileOrFolder, null))
        {
            throw new InvalidArgumentException("NodeId of file or folder is expected");
        }

        Map<QName, Serializable> props = new HashMap<>(0);

        if (nodeInfo.getProperties() != null)
        {
            props = mapToNodeProperties(nodeInfo.getProperties());
        }

        String name = nodeInfo.getName();
        if ((name != null) && (! name.isEmpty()))
        {
            // update node name if needed - note: if the name is different than existing then this is equivalent of a rename (within parent folder)
            props.put(ContentModel.PROP_NAME, name);
        }

        String nodeType = nodeInfo.getNodeType();
        if ((nodeType != null) && (! nodeType.isEmpty()))
        {
            // update node type - ensure that we are performing a specialise (we do not support generalise)
            QName destNodeTypeQName = createQName(nodeType);

            if ((! destNodeTypeQName.equals(nodeTypeQName)) && dictionaryService.isSubClass(destNodeTypeQName, nodeTypeQName))
            {
                nodeService.setType(nodeRef, destNodeTypeQName);
            }
            else
            {
                throw new InvalidArgumentException("Failed to change (specialise) node type - from "+nodeTypeQName+" to "+destNodeTypeQName);
            }
        }

        NodeRef parentNodeRef = nodeInfo.getParentId();
        if (parentNodeRef != null)
        {
            // move/rename - with exception mapping
            move(nodeRef, parentNodeRef, name);
        }

        List<String> aspectNames = nodeInfo.getAspectNames();
        if (aspectNames != null)
        {
            // update aspects - note: can be empty (eg. to remove existing aspects+properties) but not cm:auditable, sys:referencable, sys:localized
        	
            Set<QName> aspectQNames = mapToNodeAspects(aspectNames);

            Set<QName> existingAspects = nodeService.getAspects(nodeRef);

            Set<QName> aspectsToAdd = new HashSet<>(3);
            Set<QName> aspectsToRemove = new HashSet<>(3);

            for (QName aspectQName : aspectQNames)
            {
                if (EXCLUDED_NS.contains(aspectQName.getNamespaceURI()) || EXCLUDED_ASPECTS.contains(aspectQName) || aspectQName.equals(ContentModel.ASPECT_AUDITABLE))
                {
                    continue; // ignore
                }

                if (! existingAspects.contains(aspectQName))
                {
                    aspectsToAdd.add(aspectQName);
                }
            }

            for (QName existingAspect : existingAspects)
            {
                if (EXCLUDED_NS.contains(existingAspect.getNamespaceURI()) || EXCLUDED_ASPECTS.contains(existingAspect) || existingAspect.equals(ContentModel.ASPECT_AUDITABLE))
                {
                    continue; // ignore
                }

                if (! aspectQNames.contains(existingAspect))
                {
                    aspectsToRemove.add(existingAspect);
                }
            }

            // Note: for now, if aspectNames are sent then all that are required should be sent (to avoid properties from other existing aspects being removed)
            // TODO: optional PATCH mechanism to add one new new aspect (with some related aspect properties) without affecting existing aspects/properties
            for (QName aQName : aspectsToRemove)
            {
                // in future, this could/should be part of QuickShareService aspect "behaviour"
                if (aQName.equals(QuickShareModel.ASPECT_QSHARE))
                {
                    String qShareId = (String)nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
                    if (qShareId != null)
                    {
                        quickShareService.unshareContent(qShareId);
                    }
                }

                nodeService.removeAspect(nodeRef, aQName);
            }

            for (QName aQName : aspectsToAdd)
            {
                // in future, this could/should be part of QuickShareService aspect "behaviour"
                if (aQName.equals(QuickShareModel.ASPECT_QSHARE))
                {
                    quickShareService.shareContent(nodeRef);
                }

                nodeService.addAspect(nodeRef, aQName, null);
            }
        }

        if (props.size() > 0)
        {
            try
            {
                // update node properties - note: null will unset the specified property
                nodeService.addProperties(nodeRef, props);
            }
            catch (DuplicateChildNodeNameException dcne)
            {
                throw new ConstraintViolatedException(dcne.getMessage());
            }
        }

        return getFolderOrDocument(nodeRef.getId(), parameters);
    }

    private void move(NodeRef nodeRef, NodeRef parentNodeRef, String name)
    {
        NodeRef currentParentNodeRef = getParentNodeRef(nodeRef);
        if (! currentParentNodeRef.equals(parentNodeRef))
        {
            try
            {
                // updating "parentId" means moving primary parent !
                // note: in the future (as and when we support secondary parent/child assocs) we may also
                // wish to select which parent to "move from" (in case where the node resides in multiple locations)
                fileFolderService.move(nodeRef, parentNodeRef, name);
            }
            catch (InvalidNodeRefException inre)
            {
                throw new EntityNotFoundException(inre.getMessage()+" ["+nodeRef+","+parentNodeRef+"]");
            }
            catch (FileNotFoundException fnfe)
            {
                // convert checked exception
                throw new EntityNotFoundException(fnfe.getMessage()+" ["+nodeRef+","+parentNodeRef+"]");
            }
            catch (FileExistsException fee)
            {
                // duplicate - name clash
                throw new ConstraintViolatedException(fee.getMessage());
            }
            catch (FileFolderServiceImpl.InvalidTypeException ite)
            {
                throw new InvalidArgumentException("Expect target parentId to be a folder: "+parentNodeRef);
            }
        }
    }

    @Override
    public BinaryResource getContent(String fileNodeId, Parameters parameters)
    {
        final NodeRef nodeRef = validateNode(fileNodeId);

        if (! nodeMatches(nodeRef, Collections.singleton(ContentModel.TYPE_CONTENT), null))
        {
            throw new InvalidArgumentException("NodeId of content is expected: "+nodeRef);
        }

        Map<QName, Serializable> nodeProps = nodeService.getProperties(nodeRef);
        ContentData cd = (ContentData)nodeProps.get(ContentModel.PROP_CONTENT);
        String name = (String)nodeProps.get(ContentModel.PROP_NAME);

        org.alfresco.rest.framework.resource.content.ContentInfo ci = null;
        String mimeType = null;
        if (cd != null)
        {
            mimeType = cd.getMimetype();
            ci = new org.alfresco.rest.framework.resource.content.ContentInfoImpl(mimeType, cd.getEncoding(), cd.getSize(), cd.getLocale());
        }

        // By default set attachment header (with filename) unless attachment=false *and* content type is pre-configured as non-attach
        boolean attach = true;
        String attachment = parameters.getParameter("attachment");
        if (attachment != null)
        {
            Boolean a = Boolean.valueOf(attachment);
            if (!a)
            {
                if (nonAttachContentTypes.contains(mimeType))
                {
                    attach = false;
                }
                else
                {
                    logger.warn("Ignored attachment=false for "+fileNodeId+" since "+mimeType+" is not in the whitelist for non-attach content types");
                }
            }
        }
        String attachFileName = (attach ? name : null);

        return new NodeBinaryResource(nodeRef, ContentModel.PROP_CONTENT, ci, attachFileName);
    }

    @Override
    public Node updateContent(String fileNodeId, BasicContentInfo contentInfo, InputStream stream, Parameters parameters)
    {
        final NodeRef nodeRef = validateNode(fileNodeId);

        if (!nodeMatches(nodeRef, Collections.singleton(ContentModel.TYPE_CONTENT), null))
        {
            throw new InvalidArgumentException("NodeId of content is expected: " + nodeRef);
        }

        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

        String mimeType = contentInfo.getMimeType();
        if (mimeType == null)
        {
            String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            writer.guessMimetype(fileName);
        }
        else
        {
            writer.setMimetype(mimeType);
        }
        writer.guessEncoding();
        writer.putContent(stream);

        return getFolderOrDocumentFullInfo(nodeRef,
                    getParentNodeRef(nodeRef),
                    nodeService.getType(nodeRef),
                    parameters);
    }

    @Override
    public Node upload(String parentFolderNodeId, FormData formData, Parameters parameters)
    {
        if (formData == null || !formData.getIsMultiPart())
        {
            throw new InvalidArgumentException("The request content-type is not multipart");
        }

        final NodeRef parentNodeRef = validateOrLookupNode(parentFolderNodeId, null);
        if (Type.DOCUMENT == getType(parentNodeRef))
        {
            throw new InvalidArgumentException(parentFolderNodeId + " is not a folder.");
        }

        String fileName = null;
        Content content = null;
        boolean autoRename = false;
        QName nodeTypeQName = null;
        boolean overwrite = false; // If a fileName clashes for a versionable file
        Map<String, Object> qnameStrProps = new HashMap<>();
        Map<QName, Serializable> properties = null;

        for (FormData.FormField field : formData.getFields())
        {
            switch (field.getName().toLowerCase())
            {
                case "filename":
                    fileName = getStringOrNull(field.getValue());
                    break;

                case "filedata":
                    if (field.getIsFile())
                    {
                        fileName = fileName != null ? fileName : field.getFilename();
                        content = field.getContent();
                    }
                    break;

                case "autorename":
                    autoRename = Boolean.valueOf(field.getValue());
                    break;

                case "nodetype":
                    nodeTypeQName = createQName(getStringOrNull(field.getValue()));
                    if (!dictionaryService.isSubClass(nodeTypeQName, ContentModel.TYPE_CONTENT))
                    {
                        throw new InvalidArgumentException("Can only upload type of cm:content: " + nodeTypeQName);
                    }
                    break;

                // case "overwrite":
                // overwrite = Boolean.valueOf(field.getValue());
                // break;

                default:
                {
                    final String propName = field.getName();
                    if (propName.indexOf(QName.NAMESPACE_PREFIX) > -1)
                    {
                        qnameStrProps.put(propName, field.getValue());
                    }
                }
            }
        }

        // MNT-7213 When alf_data runs out of disk space, Share uploads
        // result in a success message, but the files do not appear.
        if (formData.getFields().length == 0)
        {
            throw new ConstraintViolatedException(" No disk space available");
        }
        // Ensure mandatory file attributes have been located. Need either
        // destination, or site + container or updateNodeRef
        if ((fileName == null || content == null))
        {
            throw new InvalidArgumentException("Required parameters are missing");
        }

        try
        {
            // Map the given properties, if any.
            if (qnameStrProps.size() > 0)
            {
                properties = mapToNodeProperties(qnameStrProps);
            }

            /*
             * Existing file handling
             */
            NodeRef existingFile = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, fileName);
            if (existingFile != null)
            {
                // File already exists, decide what to do
                if (autoRename)
                {
                    fileName = findUniqueName(parentNodeRef, fileName);
                }
                // TODO uncomment when we decide on uploading a new version vs overwriting
                // else if (overwrite && nodeService.hasAspect(existingFile, ContentModel.ASPECT_VERSIONABLE))
                // {
                //     // Upload component was configured to overwrite files if name clashes
                //     write(existingFile, content, fileName, false, true);
                //
                //     // Extract the metadata (The overwrite policy controls
                //     // which if any parts of the document's properties are updated from this)
                //     extractMetadata(existingFile);
                //
                //     // Do not clean formData temp files to
                //     // allow for retries. Temp files will be deleted later
                //     // when GC call DiskFileItem#finalize() method or by temp file cleaner.
                //     return createUploadResponse(parentNodeRef, existingFile);
                // }
                else
                {
                    throw new ConstraintViolatedException(fileName + " already exists.");
                }
            }

            // Create a new file.
            return createNewFile(parentNodeRef, fileName, nodeTypeQName, content, properties, parameters);

            // Do not clean formData temp files to allow for retries.
            // Temp files will be deleted later when GC call DiskFileItem#finalize() method or by temp file cleaner.
        }
        catch (ApiException apiEx)
        {
            // As this is an public API fwk exception, there is no need to convert it, so just throw it.
            throw apiEx;
        }
        catch (AccessDeniedException ade)
        {
            throw new PermissionDeniedException();
        }
        catch (ContentQuotaException cqe)
        {
            throw new RequestEntityTooLargeException();
        }
        catch (ContentLimitViolationException clv)
        {
            throw new ConstraintViolatedException();
        }
        catch (Exception ex)
        {
            /*
             * NOTE: Do not clean formData temp files to allow for retries. It's
             * possible for a temp file to remain if max retry attempts are
             * made, but this is rare, so leave to usual temp file cleanup.
             */

            throw new ApiException("Unexpected error occurred during upload of new content.", ex);
        }
    }

    /**
     * Helper to create a new node and writes its content to the repository.
     */
    private Node createNewFile(NodeRef parentNodeRef, String fileName, QName nodeType, Content content, Map<QName, Serializable> props, Parameters params)
    {
        if (nodeType == null)
        {
            nodeType = ContentModel.TYPE_CONTENT;
        }
        NodeRef newFile = createNodeImpl(parentNodeRef, fileName, nodeType, props);

        // Write content
        write(newFile, content, fileName, true, true);

        // Ensure the file is versionable (autoVersion = true, autoVersionProps = false)
        ensureVersioningEnabled(newFile, true, false);

        // Extract the metadata
        extractMetadata(newFile);

        // Create the response
        return getFolderOrDocumentFullInfo(newFile, parentNodeRef, nodeType, params);
    }

    private String getStringOrNull(String value)
    {
        if (StringUtils.isNotEmpty(value))
        {
            return value.equalsIgnoreCase("null") ? null : value;
        }
        return null;
    }

    /**
     * Writes the content to the repository.
     *
     * @param nodeRef       the reference to the node having a content property
     * @param content       the content
     * @param fileName      the uploaded file name
     * @param applyMimeType If true, apply the mimeType from the Content object,
     *                      else leave the original mimeType (if the original type is null, then guess the mimeType)
     * @param guessEncoding If true, guess the encoding from the underlying
     *                      input stream, else use encoding set in the Content object as supplied
     */
    protected void write(NodeRef nodeRef, Content content, String fileName, boolean applyMimeType, boolean guessEncoding)
    {
        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        String mimeType = content.getMimetype();
        // Per RA-637 requirement the mimeType provided by the client takes precedence, however,
        // if the mimeType is null, then it will be retrieved or guessed.
        if (mimeType == null || !applyMimeType)
        {
            ContentData existingContentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            if (existingContentData != null)
            {
                mimeType = existingContentData.getMimetype();
            }
            else
            {
                mimeType = mimetypeService.guessMimetype(fileName);
            }
        }
        writer.setMimetype(mimeType);

        if (guessEncoding)
        {
            writer.guessEncoding();
        }
        else
        {
            writer.setEncoding(content.getEncoding());
        }
        writer.putContent(content.getInputStream());
    }

    /**
     * Ensures the given node has the {@code cm:versionable} aspect applied to it, and
     * that it has the initial version in the version store.
     *
     * @param nodeRef          the reference to the node to be checked
     * @param autoVersion      If the {@code cm:versionable} aspect is applied, should auto
     *                         versioning be requested?
     * @param autoVersionProps If the {@code cm:versionable} aspect is applied, should
     *                         auto versioning of properties be requested?
     */
    protected void ensureVersioningEnabled(NodeRef nodeRef, boolean autoVersion, boolean autoVersionProps)
    {
        Map<QName, Serializable> props = new HashMap<>(2);
        props.put(ContentModel.PROP_AUTO_VERSION, autoVersion);
        props.put(ContentModel.PROP_AUTO_VERSION_PROPS, autoVersionProps);

        versionService.ensureVersioningEnabled(nodeRef, props);
    }

    /**
     * Extracts the given node metadata asynchronously.
     */
    private void extractMetadata(NodeRef nodeRef)
    {
        final String actionName = "extract-metadata";
        ActionDefinition actionDef = actionService.getActionDefinition(actionName);
        if (actionDef != null)
        {
            Action action = actionService.createAction(actionName);
            actionService.executeAction(action, nodeRef);
        }
    }

    /**
     * Creates a unique file name, if the upload component was configured to
     * find a new unique name for clashing filenames.
     *
     * @param parentNodeRef the parent node
     * @param fileName      the original fileName
     * @return a new file name
     */
    private String findUniqueName(NodeRef parentNodeRef, String fileName)
    {
        int counter = 1;
        String tmpFilename;
        NodeRef existingFile;
        do
        {
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex == 0)
            {
                // File didn't have a proper 'name' instead it
                // had just a suffix and started with a ".", create "1.txt"
                tmpFilename = counter + fileName;
            }
            else if (dotIndex > 0)
            {
                // Filename contained ".", create "fileName-1.txt"
                tmpFilename = fileName.substring(0, dotIndex) + "-" + counter + fileName.substring(dotIndex);
            }
            else
            {
                // Filename didn't contain a dot at all, create "fileName-1"
                tmpFilename = fileName + "-" + counter;
            }
            existingFile = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, tmpFilename);
            counter++;

        } while (existingFile != null);

        return tmpFilename;
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
        String PREFIX = PARAM_SELECT_PROPERTIES+"/";

        List<QName> result = new ArrayList<>(qnameStrList.size());
        for (String str : qnameStrList)
        {
            if (str.startsWith(PREFIX))
            {
                str = str.substring(PREFIX.length());
            }

            QName name = createQName(str);
            if (!EXCLUDED_PROPS.contains(name))
            {
                result.add(name);
            }
        }
        return result;
    }

}
