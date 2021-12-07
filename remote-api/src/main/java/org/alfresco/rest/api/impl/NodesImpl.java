/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
import org.alfresco.repo.domain.node.AuditablePropertiesEntity;
import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropBoolean;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rendition2.RenditionDefinition2;
import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2;
import org.alfresco.repo.rendition2.RenditionService2;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Activities;
import org.alfresco.rest.api.ClassDefinitionMapper;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.QuickShareLinks;
import org.alfresco.rest.api.model.AssocChild;
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.model.ClassDefinition;
import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.LockInfo;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.NodePermissions;
import org.alfresco.rest.api.model.PathInfo;
import org.alfresco.rest.api.model.PathInfo.ElementInfo;
import org.alfresco.rest.api.model.QuickShareLink;
import org.alfresco.rest.api.model.UserInfo;
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
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
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
import org.apache.commons.lang3.StringUtils;
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
    private RenditionService2 renditionService2;
    private SiteService siteService;
    private ActivityPoster poster;
    private RetryingTransactionHelper retryingTransactionHelper;
    private LockService lockService;
    private VirtualStore smartStore; // note: remove as part of REPO-1173
    private ClassDefinitionMapper classDefinitionMapper;

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
    private Set<String> defaultPersonLookupProperties;

    // ignore types/aspects
    private Set<QName> ignoreQNames;

    private Set<QName> personLookupProperties = new HashSet<>();

    private ConcurrentHashMap<String,NodeRef> ddCache = new ConcurrentHashMap<>();

    // pre-configured allow list of media/mime types, eg. specific types of images & also pdf
    private Set<String> nonAttachContentTypes = Collections.emptySet(); 

    public void setNonAttachContentTypes(String nonAttachAllowListStr)
    {
        if ((nonAttachAllowListStr != null) && (! nonAttachAllowListStr.isEmpty()))
        {
            nonAttachContentTypes = Set.of(nonAttachAllowListStr.trim().split("\\s*,\\s*"));
        }
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
        this.renditionService2 = sr.getRenditionService2();
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

        if (defaultPersonLookupProperties != null)
        {
            for (String property : defaultPersonLookupProperties)
            {
                personLookupProperties.add(createQName(property));
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

    public void setPersonLookupProperties(Set<String> personLookupProperties) {
      this.defaultPersonLookupProperties = personLookupProperties;
    }

    public void setPoster(ActivityPoster poster)
    {
        this.poster = poster;
    }

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }
    
    public void setClassDefinitionMapper(ClassDefinitionMapper classDefinitionMapper)
    {
        this.classDefinitionMapper = classDefinitionMapper;
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

    public final static Map<String,QName> PARAM_SYNONYMS_QNAME;
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

        PARAM_SYNONYMS_QNAME = Collections.unmodifiableMap(aMap);
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
        node.setDescription((String)properties.get(ContentModel.PROP_DESCRIPTION));
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
        Node node = getFolderOrDocumentFullInfo(nodeRef, null, null, parameters);
        return node;

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
            node.setProperties(mapFromNodeProperties(properties, includeParam, mapUserInfo, EXCLUDED_NS, EXCLUDED_PROPS));
        }

        Set<QName> aspects = null;
        if (includeParam.contains(PARAM_INCLUDE_ASPECTNAMES))
        {
            aspects = nodeService.getAspects(nodeRef);
            node.setAspectNames(mapFromNodeAspects(aspects, EXCLUDED_NS, EXCLUDED_ASPECTS));
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

        if (includeParam.contains(PARAM_INCLUDE_ISFAVORITE))
        {
            boolean isFavorite = isFavorite(nodeRef);
            node.setIsFavorite(isFavorite);
        }

        if (includeParam.contains(PARAM_INCLUDE_ALLOWABLEOPERATIONS))
        {
            // note: refactor when requirements change
            Map<String, String> mapPermsToOps = new HashMap<>(3);
            mapPermsToOps.put(PermissionService.DELETE, OP_DELETE);
            mapPermsToOps.put(PermissionService.ADD_CHILDREN, OP_CREATE);
            mapPermsToOps.put(PermissionService.WRITE, OP_UPDATE);
            mapPermsToOps.put(PermissionService.CHANGE_PERMISSIONS, OP_UPDATE_PERMISSIONS);
            

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

        if (includeParam.contains(PARAM_INCLUDE_PERMISSIONS))
        {
            Boolean inherit = permissionService.getInheritParentPermissions(nodeRef);

            List<NodePermissions.NodePermission> inheritedPerms = new ArrayList<>(5);
            List<NodePermissions.NodePermission> setDirectlyPerms = new ArrayList<>(5);
            Set<String> settablePerms = null;
            boolean allowRetrievePermission = true;

            try
            {
                for (AccessPermission accessPerm : permissionService.getAllSetPermissions(nodeRef))
                {
                    NodePermissions.NodePermission nodePerm = new NodePermissions.NodePermission(accessPerm.getAuthority(), accessPerm.getPermission(), accessPerm.getAccessStatus().toString());
                    if (accessPerm.isSetDirectly())
                    {
                        setDirectlyPerms.add(nodePerm);
                    } else
                    {
                        inheritedPerms.add(nodePerm);
                    }
                }

                settablePerms = permissionService.getSettablePermissions(nodeRef);
            }
            catch (AccessDeniedException ade)
            {
                // ignore - ie. denied access to retrieve permissions, eg. non-admin on root (Company Home)
                allowRetrievePermission = false;
            }

            // If the user does not have read permissions at
            // least on a special node then do not include permissions and
            // returned only node info that he's allowed to see
            if (allowRetrievePermission)
            {
                NodePermissions nodePerms = new NodePermissions(inherit, inheritedPerms, setDirectlyPerms, settablePerms);
                node.setPermissions(nodePerms);
            }
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

        if (includeParam.contains(PARAM_INCLUDE_DEFINITION)) 
        {
            ClassDefinition classDefinition = classDefinitionMapper.fromDictionaryClassDefinition(getTypeDefinition(nodeRef), dictionaryService);
            node.setDefinition(classDefinition);
        }

        node.setNodeType(nodeTypeQName.toPrefixString(namespaceService));
        node.setPath(pathInfo);

        return node;
    }

    private TypeDefinition getTypeDefinition(NodeRef nodeRef)
    {
        QName type = nodeService.getType(nodeRef);
        Set<QName> aspectNames = nodeService.getAspects(nodeRef);
        TypeDefinition typeDefinition = dictionaryService.getAnonymousType(type, aspectNames);
        return typeDefinition;
    }

    @Override
    public PathInfo lookupPathInfo(NodeRef nodeRefIn, ChildAssociationRef archivedParentAssoc)
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
                        String type = getNodeType(childNodeRef).toPrefixString(namespaceService);
                        Set<QName> aspects = nodeService.getAspects(childNodeRef);
                        List<String> aspectNames = mapFromNodeAspects(aspects, EXCLUDED_NS, EXCLUDED_ASPECTS);
                        pathElements.add(0, new ElementInfo(childNodeRef.getId(), nameProp.toString(), type, aspectNames));
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

    public Set<QName> mapToNodeAspects(List<String> aspectNames)
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

    public Map<QName, Serializable> mapToNodeProperties(Map<String, Object> props)
    {
        Map<QName, Serializable> nodeProps = new HashMap<>(props.size());

        for (Entry<String, Object> entry : props.entrySet())
        {
            String propName = entry.getKey();
            QName propQName = createQName(propName);

            PropertyDefinition pd = dictionaryService.getProperty(propQName);
            if (pd != null)
            {
                Serializable value = null;
                if (entry.getValue() != null)
                {
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
    
    public Map<String, Object> mapFromNodeProperties(Map<QName, Serializable> nodeProps, List<String> selectParam, Map<String,UserInfo> mapUserInfo, List<String> excludedNS, List<QName> excludedProps)
    {
        List<QName> selectedProperties;

        if ((selectParam.size() == 0) || selectParam.contains(PARAM_INCLUDE_PROPERTIES))
        {
            // return all properties
            selectedProperties = new ArrayList<>(nodeProps.size());
            for (QName propQName : nodeProps.keySet())
            {
                if ((! excludedNS.contains(propQName.getNamespaceURI())) && (! excludedProps.contains(propQName)))
                {
                    selectedProperties.add(propQName);
                }
            }
        }
        else
        {
            // return selected properties
            selectedProperties = createQNames(selectParam, excludedProps);
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
                    if (personLookupProperties.contains(qName))
                    {
                        value = Node.lookupUserInfo((String) value, mapUserInfo, personService);
                    }

                    // Empty (zero length) string values are considered to be
                    // null values, and will be represented the same as null
                    // values (i.e. by non-existence of the property).
                    if (value != null && value instanceof String && ((String) value).isEmpty())
                    {
                        continue;
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

    public List<String> mapFromNodeAspects(Set<QName> nodeAspects, List<String> excludedNS, List<QName> excludedAspects)
    {
        List<String> aspectNames = new ArrayList<>(nodeAspects.size());

        for (QName aspectQName : nodeAspects)
        {
            if ((! excludedNS.contains(aspectQName.getNamespaceURI())) && (! excludedAspects.contains(aspectQName)))
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

        QName assocTypeQNameParam = null;

        Query q = parameters.getQuery();

        if (q != null)
        {
            // filtering via "where" clause
            MapBasedQueryWalker propertyWalker = createListChildrenQueryWalker();
            QueryHelper.walk(q, propertyWalker);

            String assocTypeQNameStr = propertyWalker.getProperty(PARAM_ASSOC_TYPE, WhereClauseParser.EQUALS, String.class);
            if (assocTypeQNameStr != null)
            {
                assocTypeQNameParam = getAssocType(assocTypeQNameStr);
            }
        }

        List<Pair<QName, Boolean>> sortProps = getListChildrenSortProps(parameters);
        List<FilterProp> filterProps = getListChildrenFilterProps(parameters);

        Paging paging = parameters.getPaging();

        PagingRequest pagingRequest = Util.getPagingRequest(paging);

        final PagingResults<FileInfo> pagingResults;

        Pair<Set<QName>, Set<QName>> pair = buildSearchTypesAndIgnoreAspects(parameters);

        Set<QName> searchTypeQNames = pair.getFirst();
        Set<QName> ignoreAspectQNames = pair.getSecond();

        Set<QName> assocTypeQNames = buildAssocTypes(assocTypeQNameParam);

        // call GetChildrenCannedQuery (via FileFolderService)
        if (((filterProps == null) || (filterProps.size() == 0)) &&
            ((assocTypeQNames == null) || (assocTypeQNames.size() == 0)) &&
            (smartStore.isVirtual(parentNodeRef)|| (smartStore.canVirtualize(parentNodeRef))))
        {
            pagingResults = fileFolderService.list(parentNodeRef, searchTypeQNames, ignoreAspectQNames, sortProps, pagingRequest);
        }
        else
        {
            // TODO smart folders (see REPO-1173)
            pagingResults = fileFolderService.list(parentNodeRef, assocTypeQNames, searchTypeQNames, ignoreAspectQNames, sortProps, filterProps, pagingRequest);
        }

        final Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        final List<FileInfo> page = pagingResults.getPage();
        List<Node> nodes = new AbstractList<Node>()
        {
            @Override
            public Node get(int index)
            {
                FileInfo fInfo = page.get(index);

                // minimal info by default (unless "include"d otherwise)
                // (pass in null as parentNodeRef to force loading of primary
                // parent node as parentId)
                Node node = getFolderOrDocument(fInfo.getNodeRef(), null, fInfo.getType(), includeParam, mapUserInfo);
                if (node.getPath() != null)
                {
                    calculateRelativePath(parentFolderNodeId, node);
                }
                return node;
            }

            private void calculateRelativePath(String parentFolderNodeId, Node node)
            {
                NodeRef rootNodeRef = validateOrLookupNode(parentFolderNodeId, null);
                try
                {
                    // get the path elements
                    List<String> pathInfos = fileFolderService.getNameOnlyPath(rootNodeRef, node.getNodeRef());

                    int sizePathInfos = pathInfos.size();

                    if (sizePathInfos > 1)
                    {
                        // remove the current child
                        pathInfos.remove(sizePathInfos - 1);

                        // build the path string
                        StringBuilder sb = new StringBuilder(pathInfos.size() * 20);
                        for (String fileInfo : pathInfos)
                        {
                            sb.append("/");
                            sb.append(fileInfo);
                        }

                        node.getPath().setRelativePath(sb.toString());
                    }

                }

                catch (FileNotFoundException e)
                {   
                 // NOTE: return null as relativePath
                }
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

    /**
     * Create query walker for <code>listChildren</code>.
     *
     * @return The  created {@link MapBasedQueryWalker}.
     */
    private MapBasedQueryWalker createListChildrenQueryWalker()
    {
        return new MapBasedQueryWalker(LIST_FOLDER_CHILDREN_EQUALS_QUERY_PROPERTIES, null);
    }

    /**
     * <p>Returns a List of filter properties specified by request parameters.</p>
     *
     * @param parameters The {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - filter, sort & paging params (where, orderBy, skipCount, maxItems)
     *        - incFiles, incFolders (both true by default)
     * @return The list of {@link FilterProp}. Can be null.
     */
    protected List<FilterProp> getListChildrenFilterProps(final Parameters parameters)
    {
        List<FilterProp> filterProps = null;
        Query q = parameters.getQuery();
        if (q != null)
        {
            MapBasedQueryWalker propertyWalker = createListChildrenQueryWalker();
            QueryHelper.walk(q, propertyWalker);

            Boolean isPrimary = propertyWalker.getProperty(PARAM_ISPRIMARY, WhereClauseParser.EQUALS, Boolean.class);

            if (isPrimary != null)
            {
                filterProps = new ArrayList<>(1);
                filterProps.add(new FilterPropBoolean(GetChildrenCannedQuery.FILTER_QNAME_NODE_IS_PRIMARY, isPrimary));
            }
        }
        return filterProps;
    }

    /**
     * <p>Returns a List of sort properties specified by the "sorting" request parameter.</p>
     *
     * @param parameters The {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - filter, sort & paging params (where, orderBy, skipCount, maxItems)
     *        - incFiles, incFolders (both true by default)
     * @return The list of <code>Pair&lt;QName, Boolean&gt;</code> sort properties. If no sort parameters are
     *        found defaults to {@link #getListChildrenSortPropsDefault() getListChildrenSortPropsDefault}.
     */
    protected List<Pair<QName, Boolean>> getListChildrenSortProps(final Parameters parameters)
    {
        List<SortColumn> sortCols = parameters.getSorting();
        List<Pair<QName, Boolean>> sortProps;
        if ((sortCols != null) && (sortCols.size() > 0))
        {
            // TODO should we allow isFile in sort (and map to reverse of isFolder) ?
            sortProps = new ArrayList<>(sortCols.size());
            for (SortColumn sortCol : sortCols)
            {
                QName propQname = PARAM_SYNONYMS_QNAME.get(sortCol.column);
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
            sortProps = getListChildrenSortPropsDefault();
        }

        return sortProps;
    }

    /**
     * <p>
     * Returns the default sort order.
     * </p>
     *
     * @return The list of <code>Pair&lt;QName, Boolean&gt;</code> sort
     *         properties.
     */
    protected List<Pair<QName, Boolean>> getListChildrenSortPropsDefault()
    {
        List<Pair<QName, Boolean>> sortProps = new ArrayList<>(
                Arrays.asList(new Pair<>(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, Boolean.FALSE), new Pair<>(ContentModel.PROP_NAME, true)));
        return sortProps;
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

    protected Pair<Set<QName>, Set<QName>> buildSearchTypesAndIgnoreAspects(final Parameters parameters)
    {
        // filters
        Boolean includeFolders = null;
        Boolean includeFiles = null;
        QName filterNodeTypeQName = null;

        // note: for files/folders, include subtypes by default (unless filtering by a specific nodeType - see below)
        boolean filterIncludeSubTypes = true;

        Query q = parameters.getQuery();

        if (q != null)
        {
            // filtering via "where" clause
            MapBasedQueryWalker propertyWalker = createListChildrenQueryWalker();
            QueryHelper.walk(q, propertyWalker);

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

        return buildSearchTypesAndIgnoreAspects(filterNodeTypeQName, filterIncludeSubTypes, ignoreQNames, includeFiles, includeFolders);
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
        validateAspects(nodeInfo.getAspectNames(), EXCLUDED_NS, EXCLUDED_ASPECTS);
        validateProperties(nodeInfo.getProperties(), EXCLUDED_NS,  Arrays.asList());

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
            versionMajor = Boolean.valueOf(str);
        }
        String versioningEnabledStringValue = parameters.getParameter("versioningEnabled");
        if (null != versioningEnabledStringValue)
        {
            boolean versioningEnabled = Boolean.parseBoolean(versioningEnabledStringValue);
            if (versioningEnabled)
            {
                versionMajor = (null != versionMajor) ? versionMajor : true;
            }
            else
            {
                versionMajor = null;
            }
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

        addCustomAspects(nodeRef, nodeInfo.getAspectNames(), EXCLUDED_ASPECTS);

        processNodePermissions(nodeRef, nodeInfo);

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

    public void addCustomAspects(NodeRef nodeRef, List<String> aspectNames, List<QName> excludedAspects)
    {
        if (aspectNames == null)
        {
            return;
        }
        // node aspects - set any additional aspects
        Set<QName> aspectQNames = mapToNodeAspects(aspectNames);
        for (QName aspectQName : aspectQNames)
        {
            if (excludedAspects.contains(aspectQName) || aspectQName.equals(ContentModel.ASPECT_AUDITABLE))
            {
                continue; // ignore
            }

            nodeService.addAspect(nodeRef, aspectQName, null);
        }
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
                nodeService.createAssociation(srcNodeRef, tgtNodeRef, assocTypeQName);
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
     * Posts activities based on the activity_type.
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

    // note: see also org.alfresco.opencmis.ActivityPosterImpl
    protected ActivityInfo getActivityInfo(NodeRef parentNodeRef, NodeRef nodeRef)
    {
        // runAs system, eg. user may not have permission see one or more parents (irrespective of whether in a site context of not)
        SiteInfo siteInfo = AuthenticationUtil.runAs(new RunAsWork<SiteInfo>()
        {
            @Override
            public SiteInfo doWork() throws Exception
            {
                return siteService.getSite(nodeRef);
            }
        }, AuthenticationUtil.getSystemUserName());

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


    /**
     * Check for special case: additional node validation (pending common lower-level service support)
     * for blacklist of system nodes that should not be deleted or locked, eg. Company Home, Sites, Data Dictionary
     *
     * @param nodeRef
     * @param type
     * @return
     */
    protected boolean isSpecialNode(NodeRef nodeRef, QName type)
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
        validateAspects(nodeInfo.getAspectNames(), EXCLUDED_NS, EXCLUDED_ASPECTS);
        validateProperties(nodeInfo.getProperties(), EXCLUDED_NS,  Arrays.asList());

        final NodeRef nodeRef = validateOrLookupNode(nodeId, null);

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
            else if (! destNodeTypeQName.equals(nodeTypeQName))
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
        updateCustomAspects(nodeRef, aspectNames, EXCLUDED_ASPECTS);

        if (props.size() > 0)
        {
            validatePropValues(props);

            try
            {
                handleNodeRename(props, nodeRef);
                // update node properties - note: null will unset the specified property
                nodeService.addProperties(nodeRef, props);
            }
            catch (DuplicateChildNodeNameException dcne)
            {
                throw new ConstraintViolatedException(dcne.getMessage());
            }
        }

        processNodePermissions(nodeRef, nodeInfo);
        
        return nodeRef;
    }

    private void handleNodeRename(Map<QName, Serializable> props, NodeRef nodeRef)
    {
        Serializable nameProp = props.get(ContentModel.PROP_NAME);
        handleNodeRename(nameProp, nodeRef);
    }

    private void handleNodeRename(Serializable nameProp, NodeRef nodeRef)
    {
        if ((nameProp != null))
        {
            String currentName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            String newName = (String) nameProp;
            if (!currentName.equals(newName))
            {
                rename(nodeRef, newName);
            }
        }
    }

    protected void processNodePermissions(NodeRef nodeRef, Node nodeInfo)
    {
        NodePermissions nodePerms = nodeInfo.getPermissions();
        if (nodePerms != null)
        {
            // Cannot set inherited permissions, only direct (locally set) permissions can be set
            if ((nodePerms.getInherited() != null) && (nodePerms.getInherited().size() > 0))
            {
                throw new InvalidArgumentException("Cannot set *inherited* permissions on this node");
            }

            // Check inherit from parent value and if it's changed set the new value
            if (nodePerms.getIsInheritanceEnabled() != null)
            {
                if (nodePerms.getIsInheritanceEnabled() != permissionService.getInheritParentPermissions(nodeRef))
                {
                    permissionService.setInheritParentPermissions(nodeRef, nodePerms.getIsInheritanceEnabled());
                }
            }

            // set direct permissions
            if ((nodePerms.getLocallySet() != null))
            {
                // list of all directly set permissions
                Set<AccessPermission> directPerms = new HashSet<>(5);
                for (AccessPermission accessPerm : permissionService.getAllSetPermissions(nodeRef))
                {
                    if (accessPerm.isSetDirectly()) 
                    {
                        directPerms.add(accessPerm);
                    }
                }

                //
                // replace (or clear) set of direct permissions
                //

                // TODO cleanup the way we replace permissions (ie. add, update and delete)

                // check if same permission is sent more than once
                if (hasDuplicatePermissions(nodePerms.getLocallySet()))
                {
                    throw new InvalidArgumentException("Duplicate node permissions, there is more than one permission with the same authority and name!");
                }
                
                for (NodePermissions.NodePermission nodePerm : nodePerms.getLocallySet())
                {
                    String permName = nodePerm.getName();
                    String authorityId = nodePerm.getAuthorityId();

                    AccessStatus accessStatus = AccessStatus.ALLOWED;
                    if (nodePerm.getAccessStatus() != null)
                    {
                        accessStatus = AccessStatus.valueOf(nodePerm.getAccessStatus());
                    }

                    if (authorityId == null || authorityId.isEmpty())
                    {
                        throw new InvalidArgumentException("Authority Id is expected.");
                    }

                    if (permName == null || permName.isEmpty())
                    {
                        throw new InvalidArgumentException("Permission name is expected.");
                    }

                    if (((!authorityId.equals(PermissionService.ALL_AUTHORITIES) && (!authorityService.authorityExists(authorityId)))))
                    {
                        throw new InvalidArgumentException("Cannot set permissions on this node - unknown authority: " + authorityId);
                    }

                    AccessPermission existing = null;
                    boolean addPerm = true;
                    boolean updatePerm = false;

                    // If the permission already exists but with different access status it will be updated
                    for (AccessPermission accessPerm : directPerms)
                    {
                        if (accessPerm.getAuthority().equals(authorityId) && accessPerm.getPermission().equals(permName))
                        {
                            existing = accessPerm;
                            addPerm = false;

                            if (accessPerm.getAccessStatus() != accessStatus)
                            {
                                updatePerm = true;
                            }
                            break;
                        }
                    }

                    if (existing != null)
                    {
                        // ignore existing permissions
                        directPerms.remove(existing);
                    }

                    if (addPerm || updatePerm)
                    {
                        try
                        {
                            permissionService.setPermission(nodeRef, authorityId, permName, (accessStatus == AccessStatus.ALLOWED));
                        }
                        catch (UnsupportedOperationException e)
                        {
                            throw new InvalidArgumentException("Cannot set permissions on this node - unknown access level: " + permName);
                        }
                    }
                }

                // remove any remaining direct perms
                for (AccessPermission accessPerm : directPerms)
                {
                    permissionService.deletePermission(nodeRef, accessPerm.getAuthority(), accessPerm.getPermission());
                }
            }
        }
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
    
    public void updateCustomAspects(NodeRef nodeRef, List<String> aspectNames, List<QName> excludedAspects)
    {
        if (aspectNames != null)
        {
            // update aspects - note: can be empty (eg. to remove existing aspects+properties) but not cm:auditable, sys:referencable, sys:localized

            Set<QName> aspectQNames = mapToNodeAspects(aspectNames);

            Set<QName> existingAspects = nodeService.getAspects(nodeRef);

            Set<QName> aspectsToAdd = new HashSet<>(3);
            Set<QName> aspectsToRemove = new HashSet<>(3);

            for (QName aspectQName : aspectQNames)
            {
                if (EXCLUDED_NS.contains(aspectQName.getNamespaceURI()) || excludedAspects.contains(aspectQName) || aspectQName.equals(ContentModel.ASPECT_AUDITABLE))
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
                if (EXCLUDED_NS.contains(existingAspect.getNamespaceURI()) || excludedAspects.contains(existingAspect) || existingAspect.equals(ContentModel.ASPECT_AUDITABLE))
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
    }

    private void rename(NodeRef nodeRef, String name)
    {
        try
        {
            fileFolderService.rename(nodeRef, name);
        }
        catch (FileNotFoundException fnfe)
        {
            // convert checked exception
            throw new EntityNotFoundException(nodeRef.getId());
        }
        catch (FileExistsException fee)
        {
            // duplicate - name clash
            throw new ConstraintViolatedException("Name already exists in target parent: " + name);
        }
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
            versionMajor = Boolean.valueOf(str);
        }
        String versionComment = parameters.getParameter(PARAM_VERSION_COMMENT);

        String fileName = parameters.getParameter(PARAM_NAME);
        if (fileName != null)
        {
            handleNodeRename(fileName, nodeRef);
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
        try
        {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

            String mimeType = mimetypeService.guessMimetype(fileName);
            if ((mimeType != null) && (!mimeType.equals(MimetypeMap.MIMETYPE_BINARY)))
            {
                // quick/weak guess based on file extension
                writer.setMimetype(mimeType);
            } else
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
                } catch (IOException ioe)
                {
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("Failed to reset stream after trying to guess encoding: " + ioe.getMessage());
                    }
                }
            } else
            {
                is = stream;
            }

            writer.putContent(is);
        }
        catch (ContentQuotaException cqe)
        {
            throw new InsufficientStorageException();
        }
        catch (ContentLimitViolationException clv)
        {
            throw new RequestEntityTooLargeException(clv.getMessage());
        }
        catch (ContentIOException cioe)
        {
            if (cioe.getCause() instanceof NodeLockedException)
            {
                throw (NodeLockedException)cioe.getCause();
            }
            throw cioe;
        }
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
        if (!isVersioned)
        {
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
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
        boolean versioningEnabled = true;

        Map<String, Object> qnameStrProps = new HashMap<>();
        Map<QName, Serializable> properties = null;
        Map<String, String[]> formDataParameters = formData.getParameters();

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
                case "versioningenabled":
                    String versioningEnabledStringValue = getStringOrNull(field.getValue());
                    if (null != versioningEnabledStringValue)
                    {
                        // MNT-22036 versioningenabled parameter was added to disable versioning of newly created nodes.
                        // The default API mechanism should not be changed/affected.
                        // Versioning is enabled by default when creating a node using form-data.
                        // To preserve this, versioningEnabled value must be 'true' for any given value typo/valuesNotSupported (except case-insensitive 'false')
                        // .equalsIgnoreCase("false") will return true only when the input value is 'false'
                        // !.equalsIgnoreCase("false") will return false only when the input value is 'false'
                        versioningEnabled = !versioningEnabledStringValue.equalsIgnoreCase("false");
                    }
                    break;

                default:
                {
                    final String propName = field.getName();
                    if (propName.indexOf(QName.NAMESPACE_PREFIX) > -1 && !qnameStrProps.containsKey(propName))
                    {
                        String[] fieldValue = formDataParameters.get(propName);
                        if (fieldValue.length > 1)
                        {
                            qnameStrProps.put(propName, Arrays.asList(fieldValue));
                        }
                        else
                        {
                            qnameStrProps.put(propName, fieldValue[0]);
                        }
                    }
                }
            }
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

        validateProperties(qnameStrProps, EXCLUDED_NS,  Arrays.asList());
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
            // MNT-22036 add versioningEnabled property for newly created nodes.
            versionMajor = versioningEnabled ? versionMajor : null;

            // Create a new file.
            NodeRef nodeRef = createNewFile(parentNodeRef, fileName, nodeTypeQName, content, properties, assocTypeQName, parameters, versionMajor, versionComment);
            
            // Create the response
            final Node fileNode = getFolderOrDocumentFullInfo(nodeRef, parentNodeRef, nodeTypeQName, parameters);

            checkRenditionNames(renditions);
            requestRenditions(renditions, fileNode);

            return fileNode;

            // Do not clean formData temp files to allow for retries.
            // Temp files will be deleted later when GC call DiskFileItem#finalize() method or by temp file cleaner.
        }
        catch (AccessDeniedException ade)
        {
            throw new PermissionDeniedException(ade.getMessage());
        }

        /*
         * NOTE: Do not clean formData temp files to allow for retries. It's
         * possible for a temp file to remain if max retry attempts are
         * made, but this is rare, so leave to usual temp file cleanup.
         */
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

    private void checkRenditionNames(Set<String> renditionNames)
    {
        if (renditionNames != null)
        {
            if (!renditionService2.isEnabled())
            {
                throw new DisabledServiceException("Thumbnail generation has been disabled.");
            }

            RenditionDefinitionRegistry2 renditionDefinitionRegistry2 = renditionService2.getRenditionDefinitionRegistry2();
            for (String renditionName : renditionNames)
            {
                RenditionDefinition2 renditionDefinition = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
                if (renditionDefinition == null)
                {
                    throw new NotFoundException(renditionName + " is not registered.");
                }
            }
        }
    }

    static Set<String> getRequestedRenditions(String renditionsParam)
    {
        if (renditionsParam == null)
        {
            return null;
        }

        String[] renditionNames = renditionsParam.split(",");

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

    private void requestRenditions(Set<String> renditionNames, Node fileNode)
    {
        if (renditionNames != null)
        {
            NodeRef sourceNodeRef = fileNode.getNodeRef();
            String mimeType = fileNode.getContent().getMimeType();
            long size = fileNode.getContent().getSizeInBytes();
            RenditionDefinitionRegistry2 renditionDefinitionRegistry2 = renditionService2.getRenditionDefinitionRegistry2();
            Set<String> availableRenditions = renditionDefinitionRegistry2.getRenditionNamesFrom(mimeType, size);

            for (String renditionName : renditionNames)
            {
                // RA-1052 (REPO-47)
                try
                {
                    // File may be to big
                    if (!availableRenditions.contains(renditionName))
                    {
                        throw new InvalidArgumentException("Unable to create thumbnail '" + renditionName + "' for " +
                                mimeType + " as no transformer is currently available.");
                    }

                    renditionService2.render(sourceNodeRef, renditionName);
                }
                catch (Exception ex)
                {
                    // Note: The log level is not 'error' as it could easily fill out the log file.
                    if (logger.isDebugEnabled())
                    {
                        // Don't throw the exception as we don't want the the upload to fail, just log it.
                        logger.debug("Asynchronous request to create a rendition upon upload failed: " + ex.getMessage());
                    }
                }
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
     * @param excludedProps
     * @return a list of {@code QName} objects
     */
    protected List<QName> createQNames(List<String> qnameStrList, List<QName> excludedProps)
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
            if (!excludedProps.contains(name))
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

        if (!nodeMatches(nodeRef, Collections.singleton(ContentModel.TYPE_CONTENT), null, false))
        {
            throw new InvalidArgumentException("Node of type cm:content or a subtype is expected: " + nodeId);
        }

        LockInfo validatedLockInfo = validateLockInformation(lockInfo);
        lockService.lock(nodeRef, validatedLockInfo.getMappedType(), validatedLockInfo.getTimeToExpire(), validatedLockInfo.getLifetime());

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
        if (lockInfo.getTimeToExpire() == null)
        {
            lockInfo.setTimeToExpire(0);
        }
        return lockInfo;
    }

    @Override
    public Node unlock(String nodeId, Parameters parameters)
    {
        NodeRef nodeRef = validateOrLookupNode(nodeId, null);

        if (isSpecialNode(nodeRef, getNodeType(nodeRef)))
        {
            throw new PermissionDeniedException("Current user doesn't have permission to unlock node " + nodeId);
        }
        if (!lockService.isLocked(nodeRef))
        {
            throw new IntegrityException("Can't unlock node " + nodeId + " because it isn't locked", null);
        }

        lockService.unlock(nodeRef);
        return getFolderOrDocument(nodeId, parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, boolean attachment, Long validFor)
    {
        DirectAccessUrl directAccessUrl = contentService.requestContentDirectUrl(nodeRef, ContentModel.PROP_CONTENT, attachment, validFor);
        if (directAccessUrl == null)
        {
            throw new DisabledServiceException("Direct access url isn't available.");
        }
        return directAccessUrl;
    }

    /**
     * Checks if same permission is sent more than once
     * @param locallySetPermissions
     * @return
     */
    private boolean hasDuplicatePermissions(List<NodePermissions.NodePermission> locallySetPermissions)
    {
        boolean duplicate = false;
        if (locallySetPermissions != null)
        {
            HashSet<NodePermissions.NodePermission> temp = new HashSet<>(locallySetPermissions.size());
            for (NodePermissions.NodePermission permission : locallySetPermissions)
            {
                temp.add(permission);
            }
            duplicate = (locallySetPermissions.size() != temp.size());
        }
        return duplicate;
    }
    
    /**
     * 
     * @param node
     */
    private boolean isFavorite(NodeRef node)
    {
        PreferenceService preferenceService = (PreferenceService) sr.getService(ServiceRegistry.PREFERENCE_SERVICE);
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        Map<String, Serializable> preferences = preferenceService.getPreferences(currentUserName);

        for (Serializable nodesFavorites : preferences.values())
        {
            if (nodesFavorites instanceof String)
            {
                StringTokenizer st = new StringTokenizer((String) nodesFavorites, ",");
                while (st.hasMoreTokens())
                {
                    String nodeRefStr = st.nextToken();
                    nodeRefStr = nodeRefStr.trim();

                    if (!NodeRef.isNodeRef((String) nodeRefStr))
                    {
                        continue;
                    }

                    NodeRef nodeRef = new NodeRef((String) nodeRefStr);

                    if (nodeRef.equals(node))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void validateAspects(List<String> aspectNames, List<String> excludedNS, List<QName> excludedAspects)
    {
        if (aspectNames != null && excludedNS != null && excludedAspects != null)
        {
            Set<QName> aspects = mapToNodeAspects(aspectNames);
            aspects.forEach(aspect -> {
                if (excludedNS != null && excludedNS.contains(aspect.getNamespaceURI()))
                {
                    throw new IllegalArgumentException("NameSpace cannot be used by API: " + aspect.toPrefixString());
                }
                if (excludedAspects != null && excludedAspects.contains(aspect))
                {
                    throw new IllegalArgumentException("Cannot be used by API: " + aspect.toPrefixString());
                }
            });
        }
    }

    public void validateProperties(Map<String, Object> properties, List<String> excludedNS, List<QName> excludedProperties)
    {
        if (properties != null)
        {
            Map<QName, Serializable> nodeProps = mapToNodeProperties(properties);
            nodeProps.keySet().forEach(property -> {
                if ((excludedNS != null && excludedNS.contains(property.getNamespaceURI())))
                {
                    throw new IllegalArgumentException("NameSpace cannot be used by API: " + property.toPrefixString());
                }
                if ((excludedProperties != null && excludedProperties.contains(property)) || AuditablePropertiesEntity.getAuditablePropertyQNames().contains(property))
                {
                    throw new IllegalArgumentException("Cannot be used by API: " + property.toPrefixString());
                }
            });
        }
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

    protected NodeService getNodeService()
    {
        return nodeService;
    }

    protected DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    protected FileFolderService getFileFolderService()
    {
        return fileFolderService;
    }

    protected NamespaceService getNamespaceService()
    {
        return namespaceService;
    }

    protected PermissionService getPermissionService()
    {
        return permissionService;
    }

    protected MimetypeService getMimetypeService()
    {
        return mimetypeService;
    }

    protected ContentService getContentService()
    {
        return contentService;
    }

    protected ActionService getActionService()
    {
        return actionService;
    }

    protected VersionService getVersionService()
    {
        return versionService;
    }

    protected PersonService getPersonService()
    {
        return personService;
    }

    protected OwnableService getOwnableService()
    {
        return ownableService;
    }

    protected AuthorityService getAuthorityService()
    {
        return authorityService;
    }

    @Deprecated
    protected ThumbnailService getThumbnailService()
    {
        return thumbnailService;
    }

    protected SiteService getSiteService()
    {
        return siteService;
    }

    protected ActivityPoster getPoster()
    {
        return poster;
    }

    protected RetryingTransactionHelper getRetryingTransactionHelper()
    {
        return retryingTransactionHelper;
    }

    protected LockService getLockService()
    {
        return lockService;
    }

    protected VirtualStore getSmartStore()
    {
        return smartStore;
    }

    protected QuickShareLinks getQuickShareLinks()
    {
        return quickShareLinks;
    }

    protected Repository getRepositoryHelper()
    {
        return repositoryHelper;
    }
}
