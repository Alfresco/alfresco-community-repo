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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.content.ContentLimitViolationException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropBoolean;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailHelper;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Activities;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.QuickShareLinks;
import org.alfresco.rest.api.model.AssocChild;
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.LockInfo;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.PathInfo.ElementInfo;
import org.alfresco.rest.api.model.QuickShareLink;
import org.alfresco.rest.api.model.UnlockInfo;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.api.nodes.NodeAssocService;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InsufficientStorageException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RequestEntityTooLargeException;
import org.alfresco.rest.framework.core.exceptions.UnsupportedMediaTypeException;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.ContentInfoImpl;
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
import org.alfresco.service.cmr.activities.ActivitiesTransactionListener;
import org.alfresco.service.cmr.activities.ActivityInfo;
import org.alfresco.service.cmr.activities.ActivityPoster;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationExistsException;
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
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Centralises access to file/folder/node services and maps between representations.
 *
 * Note:
 * This class was originally used for returning some basic node info when listing Favourites.
 *
 * It has now been re-purposed and extended to implement the new Nodes (RESTful) API for
 * managing files & folders, as well as custom node types.
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


    private enum Type
    {
        // Note: ordered
        DOCUMENT, FOLDER
    }

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private FileFolderService fileFolderService;
    private NamespaceService namespaceService;
    private PermissionService permissionService;
    private MimetypeService mimetypeService;
    private ContentService contentService;
    private ActionService actionService;
    private VersionService versionService;
    private PersonService personService;
    private OwnableService ownableService;
    private AuthorityService authorityService;
    private ThumbnailService thumbnailService;
    private SiteService siteService;
    private ActivityPoster poster;
    private RetryingTransactionHelper retryingTransactionHelper;
    private NodeAssocService nodeAssocService;
    private LockService lockService;

    private enum Activity_Type
    {
        ADDED, UPDATED, DELETED, DOWNLOADED
    }

    private BehaviourFilter behaviourFilter;

    // note: circular - Nodes/QuickShareLinks currently use each other (albeit for different methods)
    private QuickShareLinks quickShareLinks;

    private Repository repositoryHelper;
    private ServiceRegistry sr;
    private Set<String> defaultIgnoreTypesAndAspects;

    // ignore types/aspects
    private Set<QName> ignoreQNames;

    private ConcurrentHashMap<String,NodeRef> ddCache = new ConcurrentHashMap<>();

    private Set<String> nonAttachContentTypes = Collections.emptySet(); // pre-configured whitelist, eg. images & pdf

    public void setNonAttachContentTypes(Set<String> nonAttachWhiteList)
    {
        this.nonAttachContentTypes = nonAttachWhiteList;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "serviceRegistry", sr);
        PropertyCheck.mandatory(this, "behaviourFilter", behaviourFilter);
        PropertyCheck.mandatory(this, "repositoryHelper", repositoryHelper);
        PropertyCheck.mandatory(this, "quickShareLinks", quickShareLinks);
        PropertyCheck.mandatory(this, "poster", poster);

        this.namespaceService = sr.getNamespaceService();
        this.fileFolderService = sr.getFileFolderService();
        this.nodeService = sr.getNodeService();
        this.permissionService = sr.getPermissionService();
        this.dictionaryService = sr.getDictionaryService();
        this.mimetypeService = sr.getMimetypeService();
        this.contentService = sr.getContentService();
        this.actionService = sr.getActionService();
        this.versionService = sr.getVersionService();
        this.personService = sr.getPersonService();
        this.ownableService = sr.getOwnableService();
        this.authorityService = sr.getAuthorityService();
        this.thumbnailService = sr.getThumbnailService();
        this.siteService =  sr.getSiteService();
        this.retryingTransactionHelper = sr.getRetryingTransactionHelper();
        this.lockService = sr.getLockService();

        if (defaultIgnoreTypesAndAspects != null)
        {
            ignoreQNames = new HashSet<>(defaultIgnoreTypesAndAspects.size());
            for (String type : defaultIgnoreTypesAndAspects)
            {
                ignoreQNames.add(createQName(type));
            }
        }
    }

    public void setServiceRegistry(ServiceRegistry sr)
    {
        this.sr = sr;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    public void setQuickShareLinks(QuickShareLinks quickShareLinks)
    {
        this.quickShareLinks = quickShareLinks;
    }

    public void setIgnoreTypes(Set<String> ignoreTypesAndAspects)
    {
        this.defaultIgnoreTypesAndAspects = ignoreTypesAndAspects;
    }

    public void setPoster(ActivityPoster poster)
    {
        this.poster = poster;
    }

    // Introduces permissions for Node Assoc (see public-rest-context.xml)
    public void setNodeAssocService(NodeAssocService nodeAssocService)
    {
        this.nodeAssocService = nodeAssocService;
    }


    // excluded namespaces (aspects, properties, assoc types)
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

    // list children filtering (via where clause)
    private final static Set<String> LIST_FOLDER_CHILDREN_EQUALS_QUERY_PROPERTIES =
            new HashSet<>(Arrays.asList(new String[] {PARAM_ISFOLDER, PARAM_ISFILE, PARAM_NODETYPE, PARAM_ISPRIMARY, PARAM_ASSOC_TYPE}));

    /*
     * Validates that node exists.
     *
     * Note: assumes workspace://SpacesStore
     */
    @Override
    public NodeRef validateNode(String nodeId)
    {
        //belts-and-braces
        if (nodeId == null)
        {
            throw new InvalidArgumentException("Missing nodeId");
        }

        return validateNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
    }

    @Override
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

    @Override
    public NodeRef validateNode(NodeRef nodeRef)
    {
        if (!nodeService.exists(nodeRef))
        {
            throw new EntityNotFoundException(nodeRef.getId());
        }

        return nodeRef;
    }

    /*
     * Check that nodes exists and matches given expected/excluded type(s).
     */
    @Override
    public boolean nodeMatches(NodeRef nodeRef, Set<QName> expectedTypes, Set<QName> excludedTypes)
    {
        return nodeMatches(nodeRef, expectedTypes, excludedTypes, true);
    }

    @Override
    public boolean isSubClass(NodeRef nodeRef, QName ofClassQName, boolean validateNodeRef)
    {
        if (validateNodeRef)
        {
            nodeRef = validateNode(nodeRef);
        }
        return isSubClass(getNodeType(nodeRef), ofClassQName);
    }

    private boolean nodeMatches(NodeRef nodeRef, Set<QName> expectedTypes, Set<QName> excludedTypes, boolean existsCheck)
    {
        if (existsCheck && (! nodeService.exists(nodeRef)))
        {
            throw new EntityNotFoundException(nodeRef.getId());
        }

        return typeMatches(getNodeType(nodeRef), expectedTypes, excludedTypes);
    }

    private QName getNodeType(NodeRef nodeRef)
    {
        return nodeService.getType(nodeRef);
    }

    private boolean isSubClass(QName className, QName ofClassQName)
    {
        return dictionaryService.isSubClass(className, ofClassQName);
    }

    protected boolean typeMatches(QName type, Set<QName> expectedTypes, Set<QName> excludedTypes)
    {
        if (((expectedTypes != null) && (expectedTypes.size() == 1)) &&
            ((excludedTypes == null) || (excludedTypes.size() == 0)))
        {
            // use isSubClass if checking against single expected type (and no excluded types)
            return isSubClass(type, expectedTypes.iterator().next());
        }

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
    @Override
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
        return getType(getNodeType(nodeRef), nodeRef);
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

        if (isSubClass(typeQName, ContentModel.TYPE_LINK))
        {
            if (isSubClass(typeQName, ApplicationModel.TYPE_FOLDERLINK))
            {
                return Type.FOLDER;
            }
            else if (isSubClass(typeQName, ApplicationModel.TYPE_FILELINK))
            {
                return Type.DOCUMENT;
            }

            NodeRef linkNodeRef = (NodeRef)nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);
            if (linkNodeRef != null)
            {
                try
                {
                    typeQName = getNodeType(linkNodeRef);
                    // drop-through to check type of destination
                    // note: edge-case - if link points to another link then we will return null
                }
                catch (InvalidNodeRefException inre)
                {
                    // ignore
                }
            }
        }

        if (isSubClass(typeQName, ContentModel.TYPE_FOLDER))
        {
            if (! isSubClass(typeQName, ContentModel.TYPE_SYSTEM_FOLDER))
            {
                return Type.FOLDER;
            }
            return null; // unknown
        }
        else if (isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            return Type.DOCUMENT;
        }

        return null; // unknown
    }

    /**
     * @deprecated note: currently required for backwards compat' (Favourites API)
     */
    @Override
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
            throw new InvalidArgumentException("Node is not a file: "+nodeRef.getId());
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
    @Override
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
            throw new InvalidArgumentException("Node is not a folder: "+nodeRef.getId());
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

    public NodeRef validateOrLookupNode(String nodeId, String path)
    {
        NodeRef parentNodeRef;

        if ((nodeId == null) || (nodeId.isEmpty()))
        {
            throw new InvalidArgumentException("Missing nodeId");
        }

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
                throw new InvalidArgumentException("Unexpected - cannot use: " + PATH_MY);
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
            // check that parent is a folder before resolving relative path
            if (! nodeMatches(parentNodeRef, Collections.singleton(ContentModel.TYPE_FOLDER), null, false))
            {
                throw new InvalidArgumentException("NodeId of folder is expected: "+parentNodeRef.getId());
            }

            // resolve path relative to current nodeId
            parentNodeRef = resolveNodeByPath(parentNodeRef, path, true);
        }

        return parentNodeRef;
    }

    protected NodeRef resolveNodeByPath(final NodeRef parentNodeRef, String path, boolean checkForCompanyHome)
    {
        final List<String> pathElements = getPathElements(path);

        if (!pathElements.isEmpty() && checkForCompanyHome)
        {
            /*
            if (nodeService.getRootNode(parentNodeRef.getStoreRef()).equals(parentNodeRef))
            {
                // special case
                NodeRef chNodeRef = repositoryHelper.getCompanyHome();
                String chName = (String) nodeService.getProperty(chNodeRef, ContentModel.PROP_NAME);
                if (chName.equals(pathElements.get(0)))
                {
                    pathElements = pathElements.subList(1, pathElements.size());
                    parentNodeRef = chNodeRef;
                }
            }
            */
        }

        FileInfo fileInfo = null;
        try
        {
            if (!pathElements.isEmpty())
            {
                fileInfo = fileFolderService.resolveNamePath(parentNodeRef, pathElements);
            }
            else
            {
                fileInfo = fileFolderService.getFileInfo(parentNodeRef);
                if (fileInfo == null)
                {
                    throw new EntityNotFoundException(parentNodeRef.getId());
                }
            }
        }
        catch (FileNotFoundException fnfe)
        {
            // convert checked exception
            throw new NotFoundException("The entity with relativePath: " + path + " was not found.");
        }
        catch (AccessDeniedException ade)
        {
            // return 404 instead of 403 (as per security review - uuid vs path)
            throw new NotFoundException("The entity with relativePath: " + path + " was not found.");
        }

        return fileInfo.getNodeRef();
    }

    private List<String> getPathElements(String path)
    {
        final List<String> pathElements = new ArrayList<>();
        if (path != null && path.trim().length() > 0)
        {
            // There is no need to check for leading and trailing "/"
            final StringTokenizer tokenizer = new StringTokenizer(path, "/");
            while (tokenizer.hasMoreTokens())
            {
                pathElements.add(tokenizer.nextToken().trim());
            }
        }
        return pathElements;
    }

    private NodeRef makeFolders(NodeRef parentNodeRef, List<String> pathElements)
    {
        NodeRef currentParentRef = parentNodeRef;
        // just loop and create if necessary
        for (final String element : pathElements)
        {
            final NodeRef contextNodeRef = currentParentRef;
            // does it exist?
            // Navigation should not check permissions
            NodeRef nodeRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork() throws Exception
                {
                    return nodeService.getChildByName(contextNodeRef, ContentModel.ASSOC_CONTAINS, element);
                }
            }, AuthenticationUtil.getSystemUserName());

            if (nodeRef == null)
            {
                try
                {
                    // Checks for create permissions as the fileFolderService is a public service.
                    FileInfo createdFileInfo = fileFolderService.create(currentParentRef, element, ContentModel.TYPE_FOLDER);
                    currentParentRef = createdFileInfo.getNodeRef();
                }
                catch (AccessDeniedException ade)
                {
                    throw new PermissionDeniedException(ade.getMessage());
                }
                catch (FileExistsException fex)
                {
                    // Assume concurrency failure, so retry
                    throw new ConcurrencyFailureException(fex.getMessage());
                }
            }
            else if (!isSubClass(nodeRef, ContentModel.TYPE_FOLDER, false))
            {
                String parentName = (String) nodeService.getProperty(contextNodeRef, ContentModel.PROP_NAME);
                throw new ConstraintViolatedException("Name [" + element + "] already exists in the target parent: " + parentName);
            }
            else
            {
                // it exists
                currentParentRef = nodeRef;
            }
        }
        return currentParentRef;
    }

    @Override
    public Node getFolderOrDocument(String nodeId, Parameters parameters)
    {
        String path = parameters.getParameter(PARAM_RELATIVE_PATH);
        NodeRef nodeRef = validateOrLookupNode(nodeId, path);

        return getFolderOrDocumentFullInfo(nodeRef, null, null, parameters);
    }

    private Node getFolderOrDocumentFullInfo(NodeRef nodeRef, NodeRef parentNodeRef, QName nodeTypeQName, Parameters parameters)
    {
        return getFolderOrDocumentFullInfo(nodeRef, parentNodeRef, nodeTypeQName, parameters, null);
    }

    @Override
    public Node getFolderOrDocumentFullInfo(NodeRef nodeRef, NodeRef parentNodeRef, QName nodeTypeQName, Parameters parameters, Map<String,UserInfo> mapUserInfo)
    {
        List<String> includeParam = new ArrayList<>();
        if (parameters != null)
        {
            includeParam.addAll(parameters.getInclude());
        }

        // Add basic info for single get (above & beyond minimal that is used for listing collections)
        includeParam.add(PARAM_INCLUDE_ASPECTNAMES);
        includeParam.add(PARAM_INCLUDE_PROPERTIES);

        return getFolderOrDocument(nodeRef, parentNodeRef, nodeTypeQName, includeParam, mapUserInfo);
    }

    @Override
    public Node getFolderOrDocument(final NodeRef nodeRef, NodeRef parentNodeRef, QName nodeTypeQName, List<String> includeParam, Map<String, UserInfo> mapUserInfo)
    {
        if (mapUserInfo == null)
        {
            mapUserInfo = new HashMap<>(2);
        }

        if (includeParam == null)
        {
            includeParam = Collections.emptyList();
        }

        Node node;
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        PathInfo pathInfo = null;
        if (includeParam.contains(PARAM_INCLUDE_PATH))
        {
            ChildAssociationRef archivedParentAssoc = (ChildAssociationRef) properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
            pathInfo = lookupPathInfo(nodeRef, archivedParentAssoc);
        }

        if (nodeTypeQName == null)
        {
            nodeTypeQName = getNodeType(nodeRef);
        }

        if (parentNodeRef == null)
        {
            parentNodeRef = getParentNodeRef(nodeRef);
        }

        Type type = getType(nodeTypeQName, nodeRef);

        if (type == null)
        {
            // not direct folder (or file) ...
            // might be sub-type of cm:cmobject (or a cm:link pointing to cm:cmobject or possibly even another cm:link)
            node = new Node(nodeRef, parentNodeRef, properties, mapUserInfo, sr);
            node.setIsFolder(false);
            node.setIsFile(false);
        }
        else if (type.equals(Type.DOCUMENT))
        {
            node = new Document(nodeRef, parentNodeRef, properties, mapUserInfo, sr);
        }
        else if (type.equals(Type.FOLDER))
        {
            node = new Folder(nodeRef, parentNodeRef, properties, mapUserInfo, sr);
        }
        else
        {
            throw new RuntimeException("Unexpected - should not reach here: "+type);
        }

        if (includeParam.size() > 0)
        {
            node.setProperties(mapFromNodeProperties(properties, includeParam, mapUserInfo));
        }

        Set<QName> aspects = null;
        if (includeParam.contains(PARAM_INCLUDE_ASPECTNAMES))
        {
            aspects = nodeService.getAspects(nodeRef);
            node.setAspectNames(mapFromNodeAspects(aspects));
        }

        if (includeParam.contains(PARAM_INCLUDE_ISLINK))
        {
            boolean isLink = isSubClass(nodeTypeQName, ContentModel.TYPE_LINK);
            node.setIsLink(isLink);
        }

        if (includeParam.contains(PARAM_INCLUDE_ISLOCKED))
        {
            boolean isLocked = isLocked(nodeRef, aspects);
            node.setIsLocked(isLocked);
        }

        if (includeParam.contains(PARAM_INCLUDE_ALLOWABLEOPERATIONS))
        {
            // note: refactor when requirements change
            Map<String, String> mapPermsToOps = new HashMap<>(3);
            mapPermsToOps.put(PermissionService.DELETE, OP_DELETE);
            mapPermsToOps.put(PermissionService.ADD_CHILDREN, OP_CREATE);
            mapPermsToOps.put(PermissionService.WRITE, OP_UPDATE);

            List<String> allowableOperations = new ArrayList<>(3);
            for (Entry<String, String> kv : mapPermsToOps.entrySet())
            {
                String perm = kv.getKey();
                String op = kv.getValue();

                if (perm.equals(PermissionService.ADD_CHILDREN) && Type.DOCUMENT.equals(type))
                {
                    // special case: do not return "create" (as an allowable op) for file/content types - note: 'type' can be null
                    continue;
                }
                else if (perm.equals(PermissionService.DELETE) && (isSpecialNode(nodeRef, nodeTypeQName)))
                {
                    // special case: do not return "delete" (as an allowable op) for specific system nodes
                    continue;
                }
                else if (permissionService.hasPermission(nodeRef, perm) == AccessStatus.ALLOWED)
                {
                    allowableOperations.add(op);
                }
            }

            node.setAllowableOperations((allowableOperations.size() > 0 )? allowableOperations : null);
        }

        if (includeParam.contains(PARAM_INCLUDE_ASSOCIATION))
        {
            // Ugh ... can we optimise this and return the actual assoc directly (via FileFolderService/GetChildrenCQ) ?
            ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);

            // note: parentAssocRef.parentRef can be null for -root- node !
            if ((parentAssocRef == null) || (parentAssocRef.getParentRef() == null) || (! parentAssocRef.getParentRef().equals(parentNodeRef)))
            {
                List<ChildAssociationRef> parentAssocRefs = nodeService.getParentAssocs(nodeRef);
                for (ChildAssociationRef pAssocRef : parentAssocRefs)
                {
                    if (pAssocRef.getParentRef().equals(parentNodeRef))
                    {
                        // for now, assume same parent/child cannot appear more than once (due to unique name)
                        parentAssocRef = pAssocRef;
                        break;
                    }
                }
            }

            if (parentAssocRef != null)
            {
                QName assocTypeQName = parentAssocRef.getTypeQName();
                if ((assocTypeQName != null) && (! EXCLUDED_NS.contains(assocTypeQName.getNamespaceURI())))
                {
                    AssocChild childAssoc = new AssocChild(
                            assocTypeQName.toPrefixString(namespaceService),
                            parentAssocRef.isPrimary());

                    node.setAssociation(childAssoc);
                }
            }
        }

        node.setNodeType(nodeTypeQName.toPrefixString(namespaceService));
        node.setPath(pathInfo);

        return node;
    }
    
    protected PathInfo lookupPathInfo(NodeRef nodeRefIn, ChildAssociationRef archivedParentAssoc)
    {

        List<ElementInfo> pathElements = new ArrayList<>();
        Boolean isComplete = Boolean.TRUE;
        final Path nodePath;
        final int pathIndex;

        if (archivedParentAssoc != null)
        {
            if (permissionService.hasPermission(archivedParentAssoc.getParentRef(), PermissionService.READ).equals(AccessStatus.ALLOWED)
                    && nodeService.exists(archivedParentAssoc.getParentRef()))
            {
                nodePath = nodeService.getPath(archivedParentAssoc.getParentRef());
                pathIndex = 1;// 1 => we want to include the given node in the path as well.
            }
            else
            {
                //We can't return a valid path
                return null;
            }
        }
        else
        {
            nodePath = nodeService.getPath(nodeRefIn);
            pathIndex = 2; // 2 => as we don't want to include the given node in the path as well.
        }

        for (int i = nodePath.size() - pathIndex; i >= 0; i--)
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
                        pathElements.add(0, new ElementInfo(childNodeRef.getId(), nameProp.toString()));
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
                throw new InvalidArgumentException("Unknown aspect: " + aspectName);
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
                throw new InvalidArgumentException("Unknown property: " + propName);
            }
        }

        return nodeProps;
    }

    protected Map<String, Object> mapFromNodeProperties(Map<QName, Serializable> nodeProps, List<String> selectParam, Map<String,UserInfo> mapUserInfo)
    {
        List<QName> selectedProperties;

        if ((selectParam.size() == 0) || selectParam.contains(PARAM_INCLUDE_PROPERTIES))
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

    @Override
    public CollectionWithPagingInfo<Node> listChildren(String parentFolderNodeId, Parameters parameters)
    {
        String path = parameters.getParameter(PARAM_RELATIVE_PATH);

        final NodeRef parentNodeRef = validateOrLookupNode(parentFolderNodeId, path);

        final List<String> includeParam = parameters.getInclude();

        // filters
        Boolean includeFolders = null;
        Boolean includeFiles = null;
        Boolean isPrimary = null;
        QName assocTypeQNameParam = null;
        QName filterNodeTypeQName = null;

        // note: for files/folders, include subtypes by default (unless filtering by a specific nodeType - see below)
        boolean filterIncludeSubTypes = true;

        Query q = parameters.getQuery();

        if (q != null)
        {
            // filtering via "where" clause
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(LIST_FOLDER_CHILDREN_EQUALS_QUERY_PROPERTIES, null);
            QueryHelper.walk(q, propertyWalker);

            isPrimary = propertyWalker.getProperty(PARAM_ISPRIMARY, WhereClauseParser.EQUALS, Boolean.class);

            String assocTypeQNameStr = propertyWalker.getProperty(PARAM_ASSOC_TYPE, WhereClauseParser.EQUALS, String.class);
            if (assocTypeQNameStr != null)
            {
                assocTypeQNameParam = getAssocType(assocTypeQNameStr);
            }

            Boolean isFolder = propertyWalker.getProperty(PARAM_ISFOLDER, WhereClauseParser.EQUALS, Boolean.class);
            Boolean isFile = propertyWalker.getProperty(PARAM_ISFILE, WhereClauseParser.EQUALS, Boolean.class);

            if (isFolder != null)
            {
                includeFolders = isFolder;
            }

            if (isFile != null)
            {
                includeFiles = isFile;
            }

            if (Boolean.TRUE.equals(includeFiles) && Boolean.TRUE.equals(includeFolders))
            {
                throw new InvalidArgumentException("Invalid filter (isFile=true and isFolder=true) - a node cannot be both a file and a folder");
            }

            String nodeTypeStr = propertyWalker.getProperty(PARAM_NODETYPE, WhereClauseParser.EQUALS, String.class);
            if ((nodeTypeStr != null) && (! nodeTypeStr.isEmpty()))
            {
                if ((isFile != null) || (isFolder != null))
                {
                    throw new InvalidArgumentException("Invalid filter - nodeType and isFile/isFolder are mutually exclusive");
                }

                Pair<QName, Boolean> pair = parseNodeTypeFilter(nodeTypeStr);
                filterNodeTypeQName = pair.getFirst();
                filterIncludeSubTypes = pair.getSecond();
            }
        }

        List<SortColumn> sortCols = parameters.getSorting();
        List<Pair<QName, Boolean>> sortProps = null;
        if ((sortCols != null) && (sortCols.size() > 0))
        {
            // TODO should we allow isFile in sort (and map to reverse of isFolder) ?
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

        List<FilterProp> filterProps = null;
        if (isPrimary != null)
        {
            filterProps = new ArrayList<>(1);
            filterProps.add(new FilterPropBoolean(GetChildrenCannedQuery.FILTER_QNAME_NODE_IS_PRIMARY, isPrimary));
        }

        Paging paging = parameters.getPaging();

        PagingRequest pagingRequest = Util.getPagingRequest(paging);

        final PagingResults<FileInfo> pagingResults;

        // notes (see also earlier validation checks):
        // - no filtering means any types/sub-types (well, apart from hidden &/or default ignored types - eg. systemfolder, fm types)
        // - node type filtering is mutually exclusive from isFile/isFolder, can optionally also include sub-types
        // - isFile & isFolder cannot both be true
        // - (isFile=false) means any other types/sub-types (other than files)
        // - (isFolder=false) means any other types/sub-types (other than folders)
        // - (isFile=false and isFolder=false) means any other types/sub-types (other than files or folders)

        if (filterNodeTypeQName == null)
        {
            if ((includeFiles == null) && (includeFolders == null))
            {
                // no additional filtering
                filterNodeTypeQName = ContentModel.TYPE_CMOBJECT;
            }
            else if ((includeFiles != null) && (includeFolders != null))
            {
                if ((! includeFiles) && (! includeFolders))
                {
                    // no files or folders
                    filterNodeTypeQName = ContentModel.TYPE_CMOBJECT;
                }
            }
            else if ((includeFiles != null) && (! includeFiles))
            {
                // no files
                filterNodeTypeQName = ContentModel.TYPE_CMOBJECT;
            }
            else if ((includeFolders != null) && (! includeFolders))
            {
                // no folders
                filterNodeTypeQName = ContentModel.TYPE_CMOBJECT;
            }
        }

        Pair<Set<QName>, Set<QName>> pair = buildSearchTypesAndIgnoreAspects(filterNodeTypeQName, filterIncludeSubTypes, ignoreQNames, includeFiles, includeFolders);
        Set<QName> searchTypeQNames = pair.getFirst();
        Set<QName> ignoreAspectQNames = pair.getSecond();

        Set<QName> assocTypeQNames = buildAssocTypes(assocTypeQNameParam);

        // call GetChildrenCannedQuery (via FileFolderService)
        pagingResults = fileFolderService.list(parentNodeRef, assocTypeQNames, searchTypeQNames, ignoreAspectQNames, sortProps, filterProps, pagingRequest);


        final Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        final List<FileInfo> page = pagingResults.getPage();
        List<Node> nodes = new AbstractList<Node>()
        {
            @Override
            public Node get(int index)
            {
                FileInfo fInfo = page.get(index);

                // minimal info by default (unless "include"d otherwise)
                return getFolderOrDocument(fInfo.getNodeRef(), parentNodeRef, fInfo.getType(), includeParam, mapUserInfo);
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        Node sourceEntity = null;
        if (parameters.includeSource())
        {
            sourceEntity = getFolderOrDocumentFullInfo(parentNodeRef, null, null, null, mapUserInfo);
        }

        return CollectionWithPagingInfo.asPaged(paging, nodes, pagingResults.hasMoreItems(), pagingResults.getTotalResultCount().getFirst(), sourceEntity);
    }

    private Pair<QName,Boolean> parseNodeTypeFilter(String nodeTypeStr)
    {
        boolean filterIncludeSubTypes = false; // default nodeType filtering is without subTypes (unless nodeType value is suffixed with ' INCLUDESUBTYPES')

        int idx = nodeTypeStr.lastIndexOf(' ');
        if (idx > 0)
        {
            String suffix = nodeTypeStr.substring(idx);
            if (suffix.equalsIgnoreCase(" "+PARAM_INCLUDE_SUBTYPES))
            {
                filterIncludeSubTypes = true;
                nodeTypeStr = nodeTypeStr.substring(0, idx);
            }
        }

        QName filterNodeTypeQName = createQName(nodeTypeStr);
        if (dictionaryService.getType(filterNodeTypeQName) == null)
        {
            throw new InvalidArgumentException("Unknown filter nodeType: "+nodeTypeStr);
        }

        return new Pair<>(filterNodeTypeQName, filterIncludeSubTypes);
    }

    protected Set<QName> buildAssocTypes(QName assocTypeQName)
    {
        Set<QName> assocTypeQNames = null;
        if (assocTypeQName != null)
        {
            assocTypeQNames = Collections.singleton(assocTypeQName);
        }
        /*
        // TODO review - this works, but reduces from ~100 to ~96 (OOTB)
        // maybe we could post filter (rather than join) - examples: sys:children, sys:lost_found, sys:archivedLink, sys:archiveUserLink
        else
        {
            Collection<QName> qnames = dictionaryService.getAllAssociations();
            assocTypeQNames = new HashSet<>(qnames.size());

            // remove system assoc types
            for (QName qname : qnames)
            {
                if ((!EXCLUDED_NS.contains(qname.getNamespaceURI())))
                {
                    assocTypeQNames.add(qname);
                }
            }
        }
        */
        return assocTypeQNames;
    }

    protected Pair<Set<QName>, Set<QName>> buildSearchTypesAndIgnoreAspects(QName nodeTypeQName, boolean includeSubTypes, Set<QName> ignoreQNameTypes, Boolean includeFiles, Boolean includeFolders)
    {
        Set<QName> searchTypeQNames = new HashSet<>(100);
        Set<QName> ignoreAspectQNames = null;

        if (nodeTypeQName != null)
        {
            // Build a list of (sub-)types
            if (includeSubTypes)
            {
                Collection<QName> qnames = dictionaryService.getSubTypes(nodeTypeQName, true);
                searchTypeQNames.addAll(qnames);
            }
            searchTypeQNames.add(nodeTypeQName);

            // Remove 'system' folders
            if (includeSubTypes)
            {
                Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_SYSTEM_FOLDER, true);
                searchTypeQNames.removeAll(qnames);
            }
            searchTypeQNames.remove(ContentModel.TYPE_SYSTEM_FOLDER);
        }

        if (includeFiles != null)
        {
            if (includeFiles)
            {
                if (includeSubTypes)
                {
                    Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_CONTENT, true);
                    searchTypeQNames.addAll(qnames);
                }
                searchTypeQNames.add(ContentModel.TYPE_CONTENT);
            }
            else
            {
                Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_CONTENT, true);
                searchTypeQNames.removeAll(qnames);
                searchTypeQNames.remove(ContentModel.TYPE_CONTENT);
            }
        }

        if (includeFolders != null)
        {
            if (includeFolders)
            {
                if (includeSubTypes)
                {
                    Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_FOLDER, true);
                    searchTypeQNames.addAll(qnames);
                }
                searchTypeQNames.add(ContentModel.TYPE_FOLDER);

                // Remove 'system' folders
                if (includeSubTypes)
                {
                    Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_SYSTEM_FOLDER, true);
                    searchTypeQNames.removeAll(qnames);
                }
                searchTypeQNames.remove(ContentModel.TYPE_SYSTEM_FOLDER);
            }
            else
            {
                Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_FOLDER, true);
                searchTypeQNames.removeAll(qnames);
                searchTypeQNames.remove(ContentModel.TYPE_FOLDER);
            }
        }

        if (ignoreQNameTypes != null)
        {
            Set<QName> ignoreQNamesNotSearchTypes = new HashSet<>(ignoreQNameTypes);
            ignoreQNamesNotSearchTypes.removeAll(searchTypeQNames);
            ignoreQNamesNotSearchTypes.remove(ContentModel.TYPE_SYSTEM_FOLDER);

            if (ignoreQNamesNotSearchTypes.size() > 0)
            {
                ignoreAspectQNames = getAspectsToIgnore(ignoreQNamesNotSearchTypes);
            }

            searchTypeQNames.removeAll(ignoreQNameTypes);
        }

        return new Pair<>(searchTypeQNames, ignoreAspectQNames);
    }

    private Set<QName> getAspectsToIgnore(Set<QName> ignoreQNames)
    {
        Set<QName> ignoreQNameAspects = new HashSet<>(ignoreQNames.size());
        for (QName qname : ignoreQNames)
        {
            if (dictionaryService.getAspect(qname) != null)
            {
                ignoreQNameAspects.add(qname);
            }
        }
        return ignoreQNameAspects;
    }

    @Override
    public void deleteNode(String nodeId, Parameters parameters)
    {
        NodeRef nodeRef = validateOrLookupNode(nodeId, null);

        if (isSpecialNode(nodeRef, getNodeType(nodeRef)))
        {
            throw new PermissionDeniedException("Cannot delete: " + nodeId);
        }

        // default false (if not provided)
        boolean permanentDelete = Boolean.valueOf(parameters.getParameter(PARAM_PERMANENT));

        if (permanentDelete == true)
        {
            boolean isAdmin = authorityService.hasAdminAuthority();
            if (! isAdmin)
            {
                String owner = ownableService.getOwner(nodeRef);
                if (! AuthenticationUtil.getRunAsUser().equals(owner))
                {
                    // non-owner/non-admin cannot permanently delete (even if they have delete permission)
                    throw new PermissionDeniedException("Non-owner/non-admin cannot permanently delete: " + nodeId);
                }
            }

            // Set as temporary to delete node instead of archiving.
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_TEMPORARY, null);
        }

        final ActivityInfo activityInfo =  getActivityInfo(getParentNodeRef(nodeRef), nodeRef);
        postActivity(Activity_Type.DELETED, activityInfo, true);

        fileFolderService.delete(nodeRef);
    }

    @Override
    public Node createNode(String parentFolderNodeId, Node nodeInfo, Parameters parameters)
    {
        if (nodeInfo.getNodeRef() != null)
        {
            throw new InvalidArgumentException("Unexpected id when trying to create a new node: "+nodeInfo.getNodeRef().getId());
        }

        // check that requested parent node exists and it's type is a (sub-)type of folder
        NodeRef parentNodeRef = validateOrLookupNode(parentFolderNodeId, null);

        // node name - mandatory
        String nodeName = nodeInfo.getName();
        if ((nodeName == null) || nodeName.isEmpty())
        {
            throw new InvalidArgumentException("Node name is expected: "+parentNodeRef.getId());
        }

        // node type - check that requested type is a (sub-) type of cm:object
        String nodeType = nodeInfo.getNodeType();
        if ((nodeType == null) || nodeType.isEmpty())
        {
            throw new InvalidArgumentException("Node type is expected: "+parentNodeRef.getId()+","+nodeName);
        }

        QName nodeTypeQName = createQName(nodeType);

        boolean isContent = isSubClass(nodeTypeQName, ContentModel.TYPE_CONTENT);
        if (! isContent)
        {
            validateCmObject(nodeTypeQName);
        }

        /* RA-834: commented-out since not currently applicable for empty file
        List<ThumbnailDefinition> thumbnailDefs = null;
        String renditionsParam = parameters.getParameter(PARAM_RENDITIONS);
        if (renditionsParam != null)
        {
            if (!isContent)
            {
                throw new InvalidArgumentException("Renditions ['"+renditionsParam+"'] only apply to content types: "+parentNodeRef.getId()+","+nodeName);
            }

            thumbnailDefs = getThumbnailDefs(renditionsParam);
        }
        */

        Map<QName, Serializable> props = new HashMap<>(1);

        if (nodeInfo.getProperties() != null)
        {
            // node properties - set any additional properties
            props = mapToNodeProperties(nodeInfo.getProperties());
        }

        // Optionally, lookup by relative path
        String relativePath = nodeInfo.getRelativePath();
        parentNodeRef = getOrCreatePath(parentNodeRef, relativePath);

        // Existing file/folder name handling
        boolean autoRename = Boolean.valueOf(parameters.getParameter(PARAM_AUTO_RENAME));
        if (autoRename && (isContent || isSubClass(nodeTypeQName, ContentModel.TYPE_FOLDER)))
        {
            NodeRef existingNode = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, nodeName);
            if (existingNode != null)
            {
                // File already exists, find a unique name
                nodeName = findUniqueName(parentNodeRef, nodeName);
            }
        }

        QName assocTypeQName = ContentModel.ASSOC_CONTAINS;
        if ((nodeInfo.getAssociation() != null) && (nodeInfo.getAssociation().getAssocType() != null))
        {
            assocTypeQName = getAssocType(nodeInfo.getAssociation().getAssocType());
        }
        
        Boolean versionMajor = null;
        String str = parameters.getParameter(PARAM_VERSION_MAJOR);
        if (str != null)
        {
            versionMajor = new Boolean(str);
        }
        String versionComment = parameters.getParameter(PARAM_VERSION_COMMENT);

        // Create the node
        NodeRef nodeRef;

        if (isContent)
        {
            // create empty file node - note: currently will be set to default encoding only (UTF-8)
            nodeRef = createNewFile(parentNodeRef, nodeName, nodeTypeQName, null, props, assocTypeQName, parameters, versionMajor, versionComment);
        }
        else
        {
            // create non-content node
            nodeRef = createNodeImpl(parentNodeRef, nodeName, nodeTypeQName, props, assocTypeQName);
        }

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
        
        // eg. to create mandatory assoc(s)

        if (nodeInfo.getTargets() != null)
        {
            addTargets(nodeRef.getId(), nodeInfo.getTargets());
        }

        if (nodeInfo.getSecondaryChildren() != null)
        {
            addChildren(nodeRef.getId(), nodeInfo.getSecondaryChildren());
        }

        Node newNode = getFolderOrDocument(nodeRef.getId(), parameters);

        /* RA-834: commented-out since not currently applicable for empty file
        requestRenditions(thumbnailDefs, newNode); // note: noop for folder
        */

        return newNode;
    }

    private NodeRef getOrCreatePath(NodeRef parentNodeRef, String relativePath)
    {
        if (relativePath != null)
        {
            List<String> pathElements = getPathElements(relativePath);

            // Checks for the presence of, and creates as necessary,
            // the folder structure in the provided path elements list.
            if (!pathElements.isEmpty())
            {
                parentNodeRef = makeFolders(parentNodeRef, pathElements);
            }
        }

        return parentNodeRef;
    }

    public List<AssocChild> addChildren(String parentNodeId, List<AssocChild> entities)
    {
        NodeRef parentNodeRef = validateNode(parentNodeId);

        List<AssocChild> result = new ArrayList<>(entities.size());

        for (AssocChild assoc : entities)
        {
            String childId = assoc.getChildId();
            if (childId == null)
            {
                throw new InvalidArgumentException("Missing childId");
            }

            QName assocTypeQName = getAssocType(assoc.getAssocType());

            try
            {
                NodeRef childNodeRef = validateNode(childId);

                String nodeName = (String)nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);
                QName assocChildQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(nodeName));

                nodeService.addChild(parentNodeRef, childNodeRef, assocTypeQName, assocChildQName);
            }
            catch (AssociationExistsException aee)
            {
                throw new ConstraintViolatedException(aee.getMessage());
            }
            catch (DuplicateChildNodeNameException dcne)
            {
                throw new ConstraintViolatedException(dcne.getMessage());
            }

            result.add(assoc);
        }

        return result;
    }

    public List<AssocTarget> addTargets(String sourceNodeId, List<AssocTarget> entities)
    {
        List<AssocTarget> result = new ArrayList<>(entities.size());

        NodeRef srcNodeRef = validateNode(sourceNodeId);

        for (AssocTarget assoc : entities)
        {
            String targetNodeId = assoc.getTargetId();
            if (targetNodeId == null)
            {
                throw new InvalidArgumentException("Missing targetId");
            }

            String assocTypeStr = assoc.getAssocType();
            QName assocTypeQName = getAssocType(assocTypeStr);
            try
            {
                NodeRef tgtNodeRef = validateNode(targetNodeId);
                nodeAssocService.createAssociation(srcNodeRef, tgtNodeRef, assocTypeQName);
            }
            catch (AssociationExistsException aee)
            {
                throw new ConstraintViolatedException("Node association '"+assocTypeStr+"' already exists from "+sourceNodeId+" to "+targetNodeId);
            }
            catch (IllegalArgumentException iae)
            {
                // note: for now, we assume it is invalid assocType - alternatively, we could attempt to pre-validate via dictionary.getAssociation
                throw new InvalidArgumentException(sourceNodeId+","+assocTypeStr+","+targetNodeId);
            }

            result.add(assoc);
        }
        return result;
    }

    public QName getAssocType(String assocTypeQNameStr)
    {
        return getAssocType(assocTypeQNameStr, true);
    }

    public QName getAssocType(String assocTypeQNameStr, boolean mandatory)
    {
        QName assocType = null;

        if ((assocTypeQNameStr != null) && (! assocTypeQNameStr.isEmpty()))
        {
            assocType = createQName(assocTypeQNameStr);
            if (dictionaryService.getAssociation(assocType) == null)
            {
                throw new InvalidArgumentException("Unknown assocType: " + assocTypeQNameStr);
            }

            if (EXCLUDED_NS.contains(assocType.getNamespaceURI()))
            {
                throw new InvalidArgumentException("Invalid assocType: " + assocTypeQNameStr);
            }
        }

        if (mandatory && (assocType == null))
        {
            throw new InvalidArgumentException("Missing assocType");
        }

        return assocType;
    }


    private NodeRef createNodeImpl(NodeRef parentNodeRef, String nodeName, QName nodeTypeQName, Map<QName, Serializable> props, QName assocTypeQName)
    {
        NodeRef newNode = null;
        if (props == null)
        {
            props = new HashMap<>(1);
        }
        props.put(ContentModel.PROP_NAME, nodeName);

        validatePropValues(props);

        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(nodeName));
        try
        {
            newNode = nodeService.createNode(parentNodeRef, assocTypeQName, assocQName, nodeTypeQName, props).getChildRef();
        }
        catch (DuplicateChildNodeNameException dcne)
        {
            // duplicate - name clash
            throw new ConstraintViolatedException(dcne.getMessage());
        }

        ActivityInfo activityInfo =  getActivityInfo(parentNodeRef, newNode);
        postActivity(Activity_Type.ADDED, activityInfo, false);
        return newNode;
    }

    /**
     * Posts activites based on the activity_type.
     * If the method is called with aSync=true then a TransactionListener is used post the activity
     * afterCommit.  Otherwise the activity posting is done synchronously.
     * @param activity_type
     * @param activityInfo
     * @param aSync
     */
    protected void postActivity(Activity_Type activity_type, ActivityInfo activityInfo, boolean aSync)
    {
        if (activityInfo == null) return; //Nothing to do.

        String activityType = determineActivityType(activity_type, activityInfo.getFileInfo().isFolder());
        if (activityType != null)
        {
            if (aSync)
            {
                ActivitiesTransactionListener txListener = new ActivitiesTransactionListener(activityType, activityInfo,
                        TenantUtil.getCurrentDomain(), Activities.APP_TOOL, Activities.RESTAPI_CLIENT,
                        poster, retryingTransactionHelper);
                AlfrescoTransactionSupport.bindListener(txListener);
            }
            else
            {
                    poster.postFileFolderActivity(activityType, null, TenantUtil.getCurrentDomain(),
                        activityInfo.getSiteId(), activityInfo.getParentNodeRef(), activityInfo.getNodeRef(),
                        activityInfo.getFileName(), Activities.APP_TOOL, Activities.RESTAPI_CLIENT,
                        activityInfo.getFileInfo());
            }
        }
    }

    protected ActivityInfo getActivityInfo(NodeRef parentNodeRef, NodeRef nodeRef)
    {
        SiteInfo siteInfo = siteService.getSite(nodeRef);
        String siteId = (siteInfo != null ? siteInfo.getShortName() : null);
        if(siteId != null && !siteId.equals(""))
        {
            FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef);
            if (fileInfo != null)
            {
                boolean isContent = isSubClass(fileInfo.getType(), ContentModel.TYPE_CONTENT);

                if (fileInfo.isFolder() || isContent)
                {
                    return new ActivityInfo(null, parentNodeRef, siteId, fileInfo);
                }
            }
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Non-site activity, so ignored " + nodeRef);
            }
        }
        return null;
    }

    protected static String determineActivityType(Activity_Type activity_type, boolean isFolder)
    {
        switch (activity_type)
        {
            case DELETED:
                return isFolder ? ActivityType.FOLDER_DELETED:ActivityType.FILE_DELETED;
            case ADDED:
                return isFolder ? ActivityType.FOLDER_ADDED:ActivityType.FILE_ADDED;
            case UPDATED:
                if (!isFolder) return ActivityType.FILE_UPDATED;
                break;
            case DOWNLOADED:
                if (!isFolder) return ActivityPoster.DOWNLOADED;
                break;
        }
        return null;
    }

    // check cm:cmobject (but *not* cm:systemfolder)
    private void validateCmObject(QName nodeTypeQName)
    {
        if (! isSubClass(nodeTypeQName, ContentModel.TYPE_CMOBJECT))
        {
            throw new InvalidArgumentException("Invalid type: " + nodeTypeQName + " - expected (sub-)type of cm:cmobject");
        }

        if (isSubClass(nodeTypeQName, ContentModel.TYPE_SYSTEM_FOLDER))
        {
            throw new InvalidArgumentException("Invalid type: " + nodeTypeQName + " - cannot be (sub-)type of cm:systemfolder");
        }
    }

    // special cases: additional validation of property values (if not done by underlying foundation services)
    private void validatePropValues(Map<QName, Serializable> props)
    {
        String newOwner = (String)props.get(ContentModel.PROP_OWNER);
        if (newOwner != null)
        {
            // validate that user exists
            if (! personService.personExists(newOwner))
            {
                throw new InvalidArgumentException("Unknown owner: "+newOwner);
            }
        }
    }

    // special case: additional node validation (pending common lower-level service support)
    // for blacklist of system nodes that should not be deleted or locked, eg. Company Home, Sites, Data Dictionary
    private boolean isSpecialNode(NodeRef nodeRef, QName type)
    {
        // Check for Company Home, Sites and Data Dictionary (note: must be tenant-aware)

        if (nodeRef.equals(repositoryHelper.getCompanyHome()))
        {
            return true;
        }
        else if (type.equals(SiteModel.TYPE_SITES) || type.equals(SiteModel.TYPE_SITE))
        {
            // note: alternatively, we could inject SiteServiceInternal and use getSitesRoot (or indirectly via node locator)
            return true;
        }
        else
        {
            String tenantDomain = TenantUtil.getCurrentDomain();
            NodeRef ddNodeRef = ddCache.get(tenantDomain);
            if (ddNodeRef == null)
            {
                List<ChildAssociationRef> ddAssocs = nodeService.getChildAssocs(
                        repositoryHelper.getCompanyHome(),
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary"));
                if (ddAssocs.size() == 1)
                {
                    ddNodeRef = ddAssocs.get(0).getChildRef();
                    ddCache.put(tenantDomain, ddNodeRef);
                }
            }

            if (nodeRef.equals(ddNodeRef))
            {
                return true;
            }
        }

        return false;
    }
    
    private boolean isLocked(NodeRef nodeRef, Set<QName> aspects)
    {
        boolean locked = false;
        if (((aspects != null) && aspects.contains(ContentModel.ASPECT_LOCKABLE)) 
           || nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
        {
            locked = lockService.isLocked(nodeRef);
        }

        return locked;
    }

    @Override
    public Node updateNode(String nodeId, Node nodeInfo, Parameters parameters)
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                NodeRef nodeRef = updateNodeImpl(nodeId, nodeInfo, parameters);
                ActivityInfo activityInfo =  getActivityInfo(getParentNodeRef(nodeRef), nodeRef);
                postActivity(Activity_Type.UPDATED, activityInfo, false);
                
                return null;
            }
        }, false, true);

        return retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Node>()
        {
            @Override
            public Node execute() throws Throwable
            {
                return getFolderOrDocument(nodeId, parameters);
            }
        }, false, false);
    }
    
    protected NodeRef updateNodeImpl(String nodeId, Node nodeInfo, Parameters parameters)
    {
        final NodeRef nodeRef = validateNode(nodeId);

        QName nodeTypeQName = getNodeType(nodeRef);

        validateCmObject(nodeTypeQName);

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

            if ((! destNodeTypeQName.equals(nodeTypeQName)) &&
                 isSubClass(destNodeTypeQName, nodeTypeQName) &&
                 (! isSubClass(destNodeTypeQName, ContentModel.TYPE_SYSTEM_FOLDER)))
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
            NodeRef currentParentNodeRef = getParentNodeRef(nodeRef);
            if (currentParentNodeRef == null)
            {
                // implies root (Company Home) hence return 403 here
                throw new PermissionDeniedException();
            }

            if (! currentParentNodeRef.equals(parentNodeRef))
            {
                //moveOrCopy(nodeRef, parentNodeRef, name, false); // not currently supported - client should use explicit POST /move operation instead
                throw new InvalidArgumentException("Cannot update parentId of "+nodeId+" via PUT /nodes/{nodeId}. Please use explicit POST /nodes/{nodeId}/move operation instead");
            }
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
                if (aQName.equals(QuickShareModel.ASPECT_QSHARE))
                {
                    String qSharedId = (String)nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
                    if (qSharedId != null)
                    {
                        // note: for now, go via QuickShareLinks (rather than QuickShareService) to ensure consistent permission checks
                        // alternatively we could disallow (or ignore) "qshare:shared" aspect removal
                        quickShareLinks.delete(qSharedId, null);
                    }
                }

                nodeService.removeAspect(nodeRef, aQName);
            }

            for (QName aQName : aspectsToAdd)
            {
                if (aQName.equals(QuickShareModel.ASPECT_QSHARE))
                {
                    // note: for now, go via QuickShareLinks (rather than QuickShareService) to ensure consistent permission checks
                    // alternatively we could disallow (or ignore) "qshare:shared" aspect addition
                    QuickShareLink qs = new QuickShareLink();
                    qs.setNodeId(nodeRef.getId());
                    quickShareLinks.create(Collections.singletonList(qs), null);
                }

                nodeService.addAspect(nodeRef, aQName, null);
            }
        }

        if (props.size() > 0)
        {
            validatePropValues(props);

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
        
        return nodeRef;
    }

    @Override
    public Node moveOrCopyNode(String sourceNodeId, String targetParentId, String name, Parameters parameters, boolean isCopy)
    {
        if ((sourceNodeId == null) || (sourceNodeId.isEmpty()))
        {
            throw new InvalidArgumentException("Missing sourceNodeId");
        }

        if ((targetParentId == null) || (targetParentId.isEmpty()))
        {
            throw new InvalidArgumentException("Missing targetParentId");
        }

        final NodeRef parentNodeRef = validateOrLookupNode(targetParentId, null);
        final NodeRef sourceNodeRef = validateOrLookupNode(sourceNodeId, null);

        FileInfo fi = moveOrCopyImpl(sourceNodeRef, parentNodeRef, name, isCopy);
        return getFolderOrDocument(fi.getNodeRef().getId(), parameters);
    }

    protected FileInfo moveOrCopyImpl(NodeRef nodeRef, NodeRef parentNodeRef, String name, boolean isCopy)
    {
        String targetParentId = parentNodeRef.getId();

        try
        {
            if (isCopy)
            {
                // copy
                FileInfo newFileInfo = fileFolderService.copy(nodeRef, parentNodeRef, name);
                if (newFileInfo.getNodeRef().equals(nodeRef))
                {
                    // copy did not happen - eg. same parent folder and name (name can be null or same)
                    throw new FileExistsException(nodeRef, "");
                }
                return newFileInfo;
            }
            else
            {
                // move
                if ((! nodeRef.equals(parentNodeRef)) && isSpecialNode(nodeRef, getNodeType(nodeRef)))
                {
                    throw new PermissionDeniedException("Cannot move: "+nodeRef.getId());
                }

                // updating "parentId" means moving primary parent !
                // note: in the future (as and when we support secondary parent/child assocs) we may also
                // wish to select which parent to "move from" (in case where the node resides in multiple locations)
                return fileFolderService.move(nodeRef, parentNodeRef, name);
            }
        }
        catch (InvalidNodeRefException inre)
        {
            throw new EntityNotFoundException(targetParentId);
        }
        catch (FileNotFoundException fnfe)
        {
            // convert checked exception
            throw new EntityNotFoundException(targetParentId);
        }
        catch (FileExistsException fee)
        {
            // duplicate - name clash
            throw new ConstraintViolatedException("Name already exists in target parent: "+name);
        }
        catch (FileFolderServiceImpl.InvalidTypeException ite)
        {
            throw new InvalidArgumentException("Invalid type of target parent: "+targetParentId);
        }
    }

    @Override
    public BinaryResource getContent(String fileNodeId, Parameters parameters, boolean recordActivity)
    {
        final NodeRef nodeRef = validateNode(fileNodeId);
        return getContent(nodeRef, parameters, recordActivity);
    }

    @Override
    public BinaryResource getContent(NodeRef nodeRef, Parameters parameters, boolean recordActivity)
    {
        if (!nodeMatches(nodeRef, Collections.singleton(ContentModel.TYPE_CONTENT), null, false))
        {
            throw new InvalidArgumentException("NodeId of content is expected: " + nodeRef.getId());
        }

        Map<QName, Serializable> nodeProps = nodeService.getProperties(nodeRef);
        ContentData cd = (ContentData) nodeProps.get(ContentModel.PROP_CONTENT);
        String name = (String) nodeProps.get(ContentModel.PROP_NAME);

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
                    logger.warn("Ignored attachment=false for "+nodeRef.getId()+" since "+mimeType+" is not in the whitelist for non-attach content types");
                }
            }
        }
        String attachFileName = (attach ? name : null);

        if (recordActivity)
        {
            final ActivityInfo activityInfo = getActivityInfo(getParentNodeRef(nodeRef), nodeRef);
            postActivity(Activity_Type.DOWNLOADED, activityInfo, true);
        }

        return new NodeBinaryResource(nodeRef, ContentModel.PROP_CONTENT, ci, attachFileName);
    }

    @Override
    public Node updateContent(String fileNodeId, BasicContentInfo contentInfo, InputStream stream, Parameters parameters)
    {
        if (contentInfo.getMimeType().toLowerCase().startsWith("multipart"))
        {
            throw new UnsupportedMediaTypeException("Cannot update using "+contentInfo.getMimeType());
        }

        final NodeRef nodeRef = validateNode(fileNodeId);

        if (! nodeMatches(nodeRef, Collections.singleton(ContentModel.TYPE_CONTENT), null, false))
        {
            throw new InvalidArgumentException("NodeId of content is expected: " + nodeRef.getId());
        }

        Boolean versionMajor = null;
        String str = parameters.getParameter(PARAM_VERSION_MAJOR);
        if (str != null)
        {
            versionMajor = new Boolean(str);
        }
        String versionComment = parameters.getParameter(PARAM_VERSION_COMMENT);

        String fileName = parameters.getParameter(PARAM_NAME);
        if (fileName != null)
        {
            // optionally rename, before updating the content
            nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, fileName);
        }
        else
        {
            fileName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        }
        
        return updateExistingFile(null, nodeRef, fileName, contentInfo, stream, parameters, versionMajor, versionComment);
    }

    private Node updateExistingFile(NodeRef parentNodeRef, NodeRef nodeRef, String fileName, BasicContentInfo contentInfo, InputStream stream, Parameters parameters, Boolean versionMajor, String versionComment)
    {
        boolean isVersioned = versionService.isVersioned(nodeRef);

        behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        try
        {
            writeContent(nodeRef, fileName, stream, true);

            if ((isVersioned) || (versionMajor != null) || (versionComment != null) )
            {
                VersionType versionType = null;
                if (versionMajor != null)
                {
                    versionType = (versionMajor ? VersionType.MAJOR : VersionType.MINOR);
                }
                else
                {
                    // note: it is possible to have versionable aspect but no versions (=> no version label)
                    if ((! isVersioned) || (nodeService.getProperty(nodeRef, ContentModel.PROP_VERSION_LABEL) == null))
                    {
                        versionType = VersionType.MAJOR;
                    }
                    else
                    {
                        versionType = VersionType.MINOR;
                    }
                }

                createVersion(nodeRef, isVersioned, versionType, versionComment);
            }

            ActivityInfo activityInfo =  getActivityInfo(parentNodeRef, nodeRef);
            postActivity(Activity_Type.UPDATED, activityInfo, false);

            extractMetadata(nodeRef);
        }
        finally
        {
            behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
        }

        return getFolderOrDocumentFullInfo(nodeRef, null, null, parameters);
    }

    private void writeContent(NodeRef nodeRef, String fileName, InputStream stream, boolean guessEncoding)
    {
        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

        String mimeType = mimetypeService.guessMimetype(fileName);
        if ((mimeType != null) && (! mimeType.equals(MimetypeMap.MIMETYPE_BINARY)))
        {
            // quick/weak guess based on file extension
            writer.setMimetype(mimeType);
        }
        else
        {
            // stronger guess based on file stream
            writer.guessMimetype(fileName);
        }

        InputStream is = null;

        if (guessEncoding)
        {
            is = new BufferedInputStream(stream);
            is.mark(1024);
            writer.setEncoding(guessEncoding(is, mimeType, false));
            try
            {
                is.reset();
            }
            catch (IOException ioe)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Failed to reset stream after trying to guess encoding: " + ioe.getMessage());
                }
            }
        }
        else
        {
            is = stream;
        }

        writer.putContent(is);
    }

    private String guessEncoding(InputStream in, String mimeType, boolean close)
    {
        String encoding = "UTF-8";
        try
        {
            if (in != null)
            {
                Charset charset = mimetypeService.getContentCharsetFinder().getCharset(in, mimeType);
                encoding = charset.name();
            }
        }
        finally
        {
            try
            {
                if (close && (in != null))
                {
                    in.close();
                }
            }
            catch (IOException ioe)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Failed to close stream after trying to guess encoding: " + ioe.getMessage());
                }
            }
        }
        return encoding;
    }

    protected void createVersion(NodeRef nodeRef, boolean isVersioned, VersionType versionType, String reason)
    {
        if (! isVersioned)
        {
            // Ensure versioning is enabled for the file (autoVersion = true, autoVersionProps = false)
            Map<QName, Serializable> props = new HashMap<>(2);
            props.put(ContentModel.PROP_AUTO_VERSION, true);
            props.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);

            nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, props);
        }

        Map<String, Serializable> versionProperties = new HashMap<>(2);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, versionType);
        if (reason != null)
        {
            versionProperties.put(VersionModel.PROP_DESCRIPTION, reason);
        }

        versionService.createVersion(nodeRef, versionProperties);
    }

    @Override
    public Node upload(String parentFolderNodeId, FormData formData, Parameters parameters)
    {
        if (formData == null || !formData.getIsMultiPart())
        {
            throw new InvalidArgumentException("The request content-type is not multipart: "+parentFolderNodeId);
        }

        NodeRef parentNodeRef = validateOrLookupNode(parentFolderNodeId, null);
        if (!nodeMatches(parentNodeRef, Collections.singleton(ContentModel.TYPE_FOLDER), null, false))
        {
            throw new InvalidArgumentException("NodeId of folder is expected: " + parentNodeRef.getId());
        }

        String fileName = null;
        Content content = null;
        boolean autoRename = false;
        QName nodeTypeQName = ContentModel.TYPE_CONTENT;
        boolean overwrite = false; // If a fileName clashes for a versionable file
        Boolean versionMajor = null;
        String versionComment = null;
        String relativePath = null;
        String renditionNames = null;

        Map<String, Object> qnameStrProps = new HashMap<>();
        Map<QName, Serializable> properties = null;

        for (FormData.FormField field : formData.getFields())
        {
            switch (field.getName().toLowerCase())
            {
                case "name":
                    String str = getStringOrNull(field.getValue());
                    if ((str != null) && (! str.isEmpty()))
                    {
                        fileName = str;
                    }
                    break;

                case "filedata":
                    if (field.getIsFile())
                    {
                        fileName = (fileName != null ? fileName : field.getFilename());
                        content = field.getContent();
                    }
                    break;

                case "autorename":
                    autoRename = Boolean.valueOf(field.getValue());
                    break;

                case "nodetype":
                    nodeTypeQName = createQName(getStringOrNull(field.getValue()));
                    if (! isSubClass(nodeTypeQName, ContentModel.TYPE_CONTENT))
                    {
                        throw new InvalidArgumentException("Can only upload type of cm:content: " + nodeTypeQName);
                    }
                    break;

                case "overwrite":
                    overwrite = Boolean.valueOf(field.getValue());
                    break;

                case "majorversion":
                    versionMajor = Boolean.valueOf(field.getValue());
                    break;

                case "comment":
                    versionComment = getStringOrNull(field.getValue());
                    break;

                case "relativepath":
                    relativePath = getStringOrNull(field.getValue());
                    break;

                case "renditions":
                    renditionNames = getStringOrNull(field.getValue());
                    break;

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
            throw new ConstraintViolatedException("No disk space available");
        }
        // Ensure mandatory file attributes have been located. Need either
        // destination, or site + container or updateNodeRef
        if ((fileName == null) || fileName.isEmpty() || (content == null))
        {
            throw new InvalidArgumentException("Required parameters are missing");
        }

        if (autoRename && overwrite)
        {
            throw new InvalidArgumentException("Both 'overwrite' and 'autoRename' should not be true when uploading a file");
        }

        // if requested, make (get or create) path
        parentNodeRef = getOrCreatePath(parentNodeRef, relativePath);
        final QName assocTypeQName = ContentModel.ASSOC_CONTAINS;
        final Set<String> renditions = getRequestedRenditions(renditionNames);

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
            NodeRef existingFile = nodeService.getChildByName(parentNodeRef, assocTypeQName, fileName);
            if (existingFile != null)
            {
                // File already exists, decide what to do
                if (autoRename)
                {
                    // attempt to find a unique name
                    fileName = findUniqueName(parentNodeRef, fileName);

                    // drop-through !
                }
                else if (overwrite && nodeService.hasAspect(existingFile, ContentModel.ASPECT_VERSIONABLE))
                {
                    // overwrite existing (versionable) file
                    BasicContentInfo contentInfo = new ContentInfoImpl(content.getMimetype(), content.getEncoding(), -1, null);
                    return updateExistingFile(parentNodeRef, existingFile, fileName, contentInfo, content.getInputStream(), parameters, versionMajor, versionComment);
                }
                else
                {
                    // name clash (and no autoRename or overwrite)
                    throw new ConstraintViolatedException(fileName + " already exists.");
                }
            }
            
            // Note: pending REPO-159, we currently auto-enable versioning on new upload (but not when creating empty file)
            if (versionMajor == null)
            {
                versionMajor = true;
            }

            // Create a new file.
            NodeRef nodeRef = createNewFile(parentNodeRef, fileName, nodeTypeQName, content, properties, assocTypeQName, parameters, versionMajor, versionComment);
            
            // Create the response
            final Node fileNode = getFolderOrDocumentFullInfo(nodeRef, parentNodeRef, nodeTypeQName, parameters);

            // RA-1052
            try
            {
                List<ThumbnailDefinition> thumbnailDefs = getThumbnailDefs(renditions);
                requestRenditions(thumbnailDefs, fileNode);
            }
            catch (Exception ex)
            {
                // Note: The log level is not 'error' as it could easily fill out the log file, especially in the Cloud.
                if (logger.isDebugEnabled())
                {
                    // Don't throw the exception as we don't want the the upload to fail, just log it.
                    logger.debug("Asynchronous request to create a rendition upon upload failed: " + ex.getMessage());
                }
            }

            return fileNode;

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
            throw new PermissionDeniedException(ade.getMessage());
        }
        catch (ContentQuotaException cqe)
        {
            throw new InsufficientStorageException();
        }
        catch (ContentLimitViolationException clv)
        {
            throw new RequestEntityTooLargeException(clv.getMessage());
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

    private NodeRef createNewFile(NodeRef parentNodeRef, String fileName, QName nodeType, Content content, Map<QName, Serializable> props, QName assocTypeQName, Parameters params,
                                  Boolean versionMajor, String versionComment)
    {
        NodeRef nodeRef = createNodeImpl(parentNodeRef, fileName, nodeType, props, assocTypeQName);
        
        if (content == null)
        {
            // Write "empty" content
            writeContent(nodeRef, fileName, new ByteArrayInputStream("".getBytes()), false);
        }
        else
        {
            // Write content
            writeContent(nodeRef, fileName, content.getInputStream(), true);
        }
        
        if ((versionMajor != null) || (versionComment != null))
        {
            behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
            try
            {
                // by default, first version is major, unless specified otherwise
                VersionType versionType = VersionType.MAJOR;
                if ((versionMajor != null) && (!versionMajor))
                {
                    versionType = VersionType.MINOR;
                }

                createVersion(nodeRef, false, versionType, versionComment);

                extractMetadata(nodeRef);
            } finally
            {
                behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_VERSIONABLE);
            }
        }

        return nodeRef;
    }

    private String getStringOrNull(String value)
    {
        if (StringUtils.isNotEmpty(value))
        {
            return value.equalsIgnoreCase("null") ? null : value;
        }
        return null;
    }

    private List<ThumbnailDefinition> getThumbnailDefs(Set<String> renditionNames)
    {
        List<ThumbnailDefinition> thumbnailDefs = null;

        if (renditionNames != null)
        {
            // If thumbnail generation has been configured off, then don't bother.
            if (!thumbnailService.getThumbnailsEnabled())
            {
                throw new DisabledServiceException("Thumbnail generation has been disabled.");
            }

            thumbnailDefs = new ArrayList<>(renditionNames.size());
            ThumbnailRegistry registry = thumbnailService.getThumbnailRegistry();
            for (String renditionName : renditionNames)
            {
                // Use the thumbnail registry to get the details of the thumbnail
                ThumbnailDefinition thumbnailDef = registry.getThumbnailDefinition(renditionName);
                if (thumbnailDef == null)
                {
                    throw new NotFoundException(renditionName + " is not registered.");
                }

                thumbnailDefs.add(thumbnailDef);

            }
        }

        return thumbnailDefs;
    }

    private Set<String> getRequestedRenditions(String renditionsParam)
    {
        if (renditionsParam == null)
        {
            return null;
        }

        String[] renditionNames = renditionsParam.split(",");

        // Temporary - pending future improvements to thumbnail service to minimise chance of
        // missing/failed thumbnails (when requested/generated 'concurrently')
        if (renditionNames.length > 1)
        {
            throw new InvalidArgumentException("Please specify one rendition entity id only");
        }

        Set<String> renditions = new LinkedHashSet<>(renditionNames.length);
        for (String name : renditionNames)
        {
            name = name.trim();
            if (!name.isEmpty())
            {
                renditions.add(name.trim());
            }
        }
        return renditions;
    }

    private void requestRenditions(List<ThumbnailDefinition> thumbnailDefs, Node fileNode)
    {
        if (thumbnailDefs != null)
        {
            ThumbnailRegistry registry = thumbnailService.getThumbnailRegistry();
            for (ThumbnailDefinition thumbnailDef : thumbnailDefs)
            {
                NodeRef sourceNodeRef = fileNode.getNodeRef();
                String mimeType = fileNode.getContent().getMimeType();
                long size = fileNode.getContent().getSizeInBytes();

                // Check if anything is currently available to generate thumbnails for the specified mimeType
                if (! registry.isThumbnailDefinitionAvailable(null, mimeType, size, sourceNodeRef, thumbnailDef))
                {
                    throw new InvalidArgumentException("Unable to create thumbnail '" + thumbnailDef.getName() + "' for " +
                            mimeType + " as no transformer is currently available.");
                }

                Action action = ThumbnailHelper.createCreateThumbnailAction(thumbnailDef, sr);

                // Queue async creation of thumbnail
                actionService.executeAction(action, sourceNodeRef, true, true);
            }
        }
    }

    /**
     * Extracts the given node metadata asynchronously.
     *
     *  The overwrite policy controls which if any parts of the document's properties are updated from this.
     */
    private void extractMetadata(NodeRef nodeRef)
    {
        final String actionName = ContentMetadataExtracter.EXECUTOR_NAME;
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
    public QName createQName(String qnameStr)
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
        String PREFIX = PARAM_INCLUDE_PROPERTIES +"/";

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

    @Override
    public Node lock(String nodeId, LockInfo lockInfo, Parameters parameters)
    {
        NodeRef nodeRef = validateOrLookupNode(nodeId, null);

        if (isSpecialNode(nodeRef, getNodeType(nodeRef)))
        {
            throw new PermissionDeniedException("Current user doesn't have permission to lock node " + nodeId);
        }

        lockInfo = validateLockInformation(lockInfo);
        lockService.lock(nodeRef, lockInfo.getMappedType(), lockInfo.getTimeToExpire(), lockInfo.getLifetime(), lockInfo.getIncludeChildren());
        
        return getFolderOrDocument(nodeId, parameters);
    }
    
    private LockInfo validateLockInformation(LockInfo lockInfo)
    {
        // Set default values for the lock details.
        if (lockInfo.getType() == null)
        {
            lockInfo.setType(LockInfo.LockType2.ALLOW_OWNER_CHANGES.name());
        }
        if (lockInfo.getLifetime() == null)
        {
            lockInfo.setLifetime(Lifetime.PERSISTENT.name());
        }
        if (lockInfo.getIncludeChildren() == null)
        {
            lockInfo.setIncludeChildren(false);
        }
        if (lockInfo.getTimeToExpire() == null)
        {
            lockInfo.setTimeToExpire(0);
        }
        return lockInfo;
    }

    @Override
    public Node unlock(String nodeId, UnlockInfo unlockInfo, Parameters parameters)
    {
        NodeRef nodeRef = validateOrLookupNode(nodeId, null);

        if (isSpecialNode(nodeRef, getNodeType(nodeRef)))
        {
            throw new PermissionDeniedException("Current user doesn't have permission to unlock node " + nodeId);
        }

        if (unlockInfo.getIncludeChildren() == null)
        {
            unlockInfo.setIncludeChildren(false);
        }
        if (unlockInfo.getAllowCheckedOut() == null)
        {
            unlockInfo.setAllowCheckedOut(false);
        }
        
        // If there is no lock placed on the node skip the operation.
        if (lockService.getLockStatus(nodeRef) != LockStatus.NO_LOCK)
        {
            if (permissionService.hasPermission(nodeRef, PermissionService.UNLOCK).equals(AccessStatus.ALLOWED))
            {
                lockService.unlock(nodeRef, unlockInfo.getIncludeChildren(), unlockInfo.getAllowCheckedOut());
            }
            else
            {
                throw new PermissionDeniedException("Current user doesn't have permission to unlock node " + nodeId);
            }
        }
        return getFolderOrDocument(nodeId, parameters);
    }

    /**
     * @author Jamal Kaabi-Mofrad
     */
    /*
    private static class ContentInfoWrapper implements BasicContentInfo
    {
        private String mimeType;
        private String encoding;

        public String getEncoding()
        {
            return encoding;
        }

        public String getMimeType()
        {
            return mimeType;
        }

        ContentInfoWrapper(BasicContentInfo basicContentInfo)
        {
            if (basicContentInfo != null)
            {
                this.mimeType = basicContentInfo.getMimeType();
                this.encoding = basicContentInfo.getEncoding();
            }
        }

        ContentInfoWrapper(ContentInfo contentInfo)
        {
            if (contentInfo != null)
            {
                this.mimeType = contentInfo.getMimeType();
                this.encoding = contentInfo.getEncoding();
            }
        }

        ContentInfoWrapper(Content content)
        {
            if (content != null && StringUtils.isNotEmpty(content.getMimetype()))
            {
                try
                {
                    // TODO I think it makes sense to push contentType parsing into org.springframework.extensions.webscripts.servlet.FormData
                    MediaType media = MediaType.parseMediaType(content.getMimetype());
                    this.mimeType = media.getType() + '/' + media.getSubtype();

                    if (media.getCharSet() != null)
                    {
                        this.encoding = media.getCharSet().name();
                    }
                }
                catch (InvalidMediaTypeException ime)
                {
                    throw new InvalidArgumentException(ime.getMessage());
                }
            }
        }
    }
    */
}

