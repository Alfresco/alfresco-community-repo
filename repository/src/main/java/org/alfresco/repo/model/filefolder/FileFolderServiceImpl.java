/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.model.filefolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.copy.AbstractBaseCopyService;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.model.filefolder.traitextender.FileFolderServiceExtension;
import org.alfresco.repo.model.filefolder.traitextender.FileFolderServiceTrait;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionCheckedCollection.PermissionCheckedCollectionMixin;
import org.alfresco.repo.security.permissions.PermissionCheckedValue.PermissionCheckedValueMixin;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.model.SubFolderFilter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.AJProxyTrait;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.SearchLanguageConversion;
import org.alfresco.util.registry.NamedObjectRegistry;

/**
 * Implementation of the file/folder-specific service.
 * 
 * @author Derek Hulley
 */
public class FileFolderServiceImpl extends AbstractBaseCopyService implements FileFolderService, Extensible
{
    private static final String CANNED_QUERY_FILEFOLDER_LIST = "fileFolderGetChildrenCannedQueryFactory";

    /** Shallow search for files and folders with a name pattern */
    private static final String XPATH_QUERY_SHALLOW_ALL = "./*" +
            "[like(@cm:name, $cm:name, false)" +
            " and not (subtypeOf('" + ContentModel.TYPE_SYSTEM_FOLDER + "'))" +
            " and (subtypeOf('" + ContentModel.TYPE_FOLDER + "') or subtypeOf('" + ContentModel.TYPE_CONTENT + "')" +
            " or subtypeOf('" + ContentModel.TYPE_LINK + "'))]";

    /** Deep search for files and folders with a name pattern */
    private static final String XPATH_QUERY_DEEP_ALL = ".//*" +
            "[like(@cm:name, $cm:name, false)" +
            " and not (subtypeOf('" + ContentModel.TYPE_SYSTEM_FOLDER + "'))" +
            " and (subtypeOf('" + ContentModel.TYPE_FOLDER + "') or subtypeOf('" + ContentModel.TYPE_CONTENT + "')" +
            " or subtypeOf('" + ContentModel.TYPE_LINK + "'))]";

    /** Deep search for folders with a name pattern */
    private static final String XPATH_QUERY_DEEP_FOLDERS = ".//*" +
            "[like(@cm:name, $cm:name, false)" +
            " and not (subtypeOf('" + ContentModel.TYPE_SYSTEM_FOLDER + "'))" +
            " and (subtypeOf('" + ContentModel.TYPE_FOLDER + "'))]";

    /** Deep search for files with a name pattern */
    private static final String XPATH_QUERY_DEEP_FILES = ".//*" +
            "[like(@cm:name, $cm:name, false)" +
            " and not (subtypeOf('" + ContentModel.TYPE_SYSTEM_FOLDER + "'))" +
            " and (subtypeOf('" + ContentModel.TYPE_CONTENT + "')" +
            " or subtypeOf('" + ContentModel.TYPE_LINK + "'))]";

    private static Log logger = LogFactory.getLog(FileFolderServiceImpl.class);

    private HiddenAspect hiddenAspect;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private CopyService copyService;
    private SearchService searchService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private BehaviourFilter behaviourFilter;
    private NamedObjectRegistry<CannedQueryFactory<NodeRef>> cannedQueryRegistry;

    private boolean preserveAuditableData = true;

    // TODO: Replace this with a more formal means of identifying "system" folders (i.e. aspect or UUID)
    private List<String> systemPaths;

    // default cutoff - applies to list "all" methods
    private int defaultListMaxResults = 5000;

    private final ExtendedTrait<FileFolderServiceTrait> fileFolderTrait;

    /**
     * Default constructor
     */
    public FileFolderServiceImpl()
    {
        super();

        fileFolderTrait = new ExtendedTrait<FileFolderServiceTrait>(AJProxyTrait.create(createFileFolderTrait(), FileFolderServiceTrait.class));
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setCopyService(CopyService copyService)
    {
        this.copyService = copyService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
        this.hiddenAspect = hiddenAspect;
    }

    /**
     * Set the registry of {@link CannedQueryFactory canned queries}
     */
    public void setCannedQueryRegistry(NamedObjectRegistry<CannedQueryFactory<NodeRef>> cannedQueryRegistry)
    {
        this.cannedQueryRegistry = cannedQueryRegistry;
    }

    // TODO: Replace this with a more formal means of identifying "system" folders (i.e. aspect or UUID)
    public void setSystemPaths(List<String> systemPaths)
    {
        this.systemPaths = systemPaths;
    }

    public void setDefaultListMaxResults(int defaultListMaxResults)
    {
        this.defaultListMaxResults = defaultListMaxResults;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setPreserveAuditableData(boolean preserveAuditableData)
    {
        this.preserveAuditableData = preserveAuditableData;
    }

    public boolean isPreserveAuditableData()
    {
        return preserveAuditableData;
    }

    public void init()
    {}

    /**
     * Helper method to convert node reference instances to file info
     * 
     * @param nodeRefs
     *            the node references
     * @return Return a list of file info
     * @throws InvalidTypeException
     *             if the node is not a valid type
     */
    private List<FileInfo> toFileInfo(List<NodeRef> nodeRefs) throws InvalidTypeException
    {
        List<FileInfo> results = new ArrayList<FileInfo>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs)
        {
            try
            {
                FileInfo fileInfo = toFileInfo(nodeRef, true);
                results.add(fileInfo);
            }
            catch (InvalidNodeRefException inre)
            {
                logger.warn("toFileInfo: " + inre);
                // skip
            }
        }
        return results;
    }

    /**
     * Helper method to convert a node reference instance to a file info
     */
    private FileInfo toFileInfo(NodeRef nodeRef, boolean addTranslations) throws InvalidTypeException
    {
        // Get the file attributes
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        // Is it a folder
        QName typeQName = nodeService.getType(nodeRef);

        FileFolderServiceType type = getType(typeQName);

        boolean isFolder = type.equals(FileFolderServiceType.FOLDER);
        boolean isHidden = false;

        Client client = FileFilterMode.getClient();
        if (hiddenAspect.getVisibility(client, nodeRef) == Visibility.HiddenAttribute)
        {
            isHidden = true;
        }

        // Construct the file info and add to the results
        FileInfo fileInfo = new FileInfoImpl(nodeRef, typeQName, isFolder, isHidden, properties);

        // Done
        return fileInfo;
    }

    @Override
    public List<FileInfo> toFileInfoList(List<NodeRef> nodeRefs)
    {
        List<FileInfo> fileInfos = new LinkedList<FileInfo>();
        for (NodeRef nodeRef : nodeRefs)
        {
            // Use service proxy
            FileInfo fileInfo = this.getFileInfo(nodeRef);
            if (fileInfo != null)
            {
                fileInfos.add(fileInfo);
            }
        }
        return fileInfos;
    }

    /**
     * Exception when the type is not a valid File or Folder type
     * 
     * @see ContentModel#TYPE_CONTENT
     * @see ContentModel#TYPE_FOLDER
     * 
     * @author Derek Hulley
     */
    public static class InvalidTypeException extends RuntimeException
    {
        private static final long serialVersionUID = -310101369475434280L;

        public InvalidTypeException(String msg)
        {
            super(msg);
        }
    }

    @Override
    public boolean exists(NodeRef nodeRef)
    {
        return nodeService.exists(nodeRef);
    }

    @Override
    public FileFolderServiceType getType(QName typeQName)
    {
        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER))
        {
            if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_SYSTEM_FOLDER))
            {
                return FileFolderServiceType.SYSTEM_FOLDER;
            }
            return FileFolderServiceType.FOLDER;
        }
        else if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) ||
                dictionaryService.isSubClass(typeQName, ContentModel.TYPE_LINK))
        {
            // it is a regular file
            return FileFolderServiceType.FILE;
        }
        else
        {
            // unhandled type
            return FileFolderServiceType.INVALID;
        }
    }

    @Override
    @Extend(traitAPI = FileFolderServiceTrait.class, extensionAPI = FileFolderServiceExtension.class)
    public List<FileInfo> list(NodeRef contextNodeRef)
    {
        // execute the query
        List<FileInfo> results = listSimple(contextNodeRef, true, true);
        // done
        if (logger.isTraceEnabled())
        {
            logger.trace("List files and folders: \n" +
                    "   context: " + contextNodeRef + "\n" +
                    "   results: " + results);
        }
        return results;
    }

    private PagingResults<FileInfo> getPagingResults(PagingRequest pagingRequest, final CannedQueryResults<NodeRef> results)
    {
        List<NodeRef> nodeRefs = null;
        if (results.getPageCount() > 0)
        {
            nodeRefs = results.getPages().get(0);
        }
        else
        {
            nodeRefs = Collections.emptyList();
        }

        // set total count
        final Pair<Integer, Integer> totalCount;
        if (pagingRequest.getRequestTotalCountMax() > 0)
        {
            totalCount = results.getTotalResultCount();
        }
        else
        {
            totalCount = null;
        }

        final List<FileInfo> nodeInfos = new ArrayList<FileInfo>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs)
        {
            nodeInfos.add(toFileInfo(nodeRef, true));
        }
        PermissionCheckedCollectionMixin.create(nodeInfos, nodeRefs);

        return new PagingResults<FileInfo>() {
            @Override
            public String getQueryExecutionId()
            {
                return results.getQueryExecutionId();
            }

            @Override
            public List<FileInfo> getPage()
            {
                return nodeInfos;
            }

            @Override
            public boolean hasMoreItems()
            {
                return results.hasMoreItems();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return totalCount;
            }
        };
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.model.FileFolderService#list(org.alfresco.service.cmr.repository.NodeRef, boolean, boolean, java.util.Set, org.alfresco.service.cmr.model.PagingSortRequest) */
    @Auditable(parameters = {"contextNodeRef", "files", "folders", "ignoreQNames", "sortProps", "pagingRequest"})
    @Override
    @Extend(traitAPI = FileFolderServiceTrait.class, extensionAPI = FileFolderServiceExtension.class)
    public PagingResults<FileInfo> list(NodeRef contextNodeRef,
            boolean files,
            boolean folders,
            Set<QName> ignoreQNames,
            List<Pair<QName, Boolean>> sortProps,
            PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("contextNodeRef", contextNodeRef);
        ParameterCheck.mandatory("pagingRequest", pagingRequest);

        Pair<Set<QName>, Set<QName>> pair = buildSearchTypesAndIgnoreAspects(files, folders, ignoreQNames);
        Set<QName> searchTypeQNames = pair.getFirst();
        Set<QName> ignoreAspectQNames = pair.getSecond();

        // execute query
        final CannedQueryResults<NodeRef> results = listImpl(contextNodeRef, null, searchTypeQNames, ignoreAspectQNames, sortProps, pagingRequest);
        return getPagingResults(pagingRequest, results);
    }

    @Override
    @Extend(traitAPI = FileFolderServiceTrait.class, extensionAPI = FileFolderServiceExtension.class)
    public PagingResults<FileInfo> list(NodeRef contextNodeRef, boolean files, boolean folders, String pattern, Set<QName> ignoreQNames, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("contextNodeRef", contextNodeRef);
        ParameterCheck.mandatory("pagingRequest", pagingRequest);

        Pair<Set<QName>, Set<QName>> pair = buildSearchTypesAndIgnoreAspects(files, folders, ignoreQNames);
        Set<QName> searchTypeQNames = pair.getFirst();
        Set<QName> ignoreAspectQNames = pair.getSecond();

        // execute query
        final CannedQueryResults<NodeRef> results = listImpl(contextNodeRef, pattern, searchTypeQNames, ignoreAspectQNames, sortProps, pagingRequest);
        return getPagingResults(pagingRequest, results);
    }

    @Override
    @Extend(traitAPI = FileFolderServiceTrait.class, extensionAPI = FileFolderServiceExtension.class)
    public PagingResults<FileInfo> list(NodeRef rootNodeRef, Set<QName> searchTypeQNames, Set<QName> ignoreAspectQNames, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        CannedQueryResults<NodeRef> results = listImpl(rootNodeRef, null, searchTypeQNames, ignoreAspectQNames, sortProps, pagingRequest);
        return getPagingResults(pagingRequest, results);
    }

    @Override
    public PagingResults<FileInfo> list(NodeRef rootNodeRef,
            Set<QName> assocTypeQNames,
            Set<QName> searchTypeQNames,
            Set<QName> ignoreAspectQNames,
            List<Pair<QName, Boolean>> sortProps,
            List<FilterProp> filterProps,
            PagingRequest pagingRequest)
    {
        CannedQueryResults<NodeRef> results = listImpl(rootNodeRef, null, assocTypeQNames, searchTypeQNames, ignoreAspectQNames, sortProps, filterProps, pagingRequest);
        return getPagingResults(pagingRequest, results);
    }

    private CannedQueryResults<NodeRef> listImpl(NodeRef contextNodeRef, boolean files, boolean folders)
    {
        Set<QName> searchTypeQNames = buildSearchTypesAndIgnoreAspects(files, folders, null).getFirst();
        return listImpl(contextNodeRef, searchTypeQNames);
    }

    private CannedQueryResults<NodeRef> listImpl(NodeRef contextNodeRef, Set<QName> searchTypeQNames)
    {
        return listImpl(contextNodeRef, null, searchTypeQNames, null, null, new PagingRequest(defaultListMaxResults, null));
    }

    // note: similar to getChildAssocs(contextNodeRef, searchTypeQNames) but enables paging features, including max items, sorting etc (with permissions per-applied)

    /**
     *
     * @param contextNodeRef
     * @param pattern
     * @param searchTypeQNames
     * @param ignoreAspectQNames
     * @param sortProps
     * @param pagingRequest
     * @return
     */
    private CannedQueryResults<NodeRef> listImpl(NodeRef contextNodeRef, String pattern, Set<QName> searchTypeQNames, Set<QName> ignoreAspectQNames, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        return listImpl(contextNodeRef, pattern, Collections.singleton(ContentModel.ASSOC_CONTAINS), searchTypeQNames, ignoreAspectQNames, sortProps, null, pagingRequest);
    }

    private CannedQueryResults<NodeRef> listImpl(NodeRef contextNodeRef, String pattern, Set<QName> assocTypeQNames, Set<QName> searchTypeQNames, Set<QName> ignoreAspectQNames,
            List<Pair<QName, Boolean>> sortProps, List<FilterProp> filterProps, PagingRequest pagingRequest)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);

        // get canned query
        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory) cannedQueryRegistry.getNamedObject(CANNED_QUERY_FILEFOLDER_LIST);

        GetChildrenCannedQuery cq = (GetChildrenCannedQuery) getChildrenCannedQueryFactory.getCannedQuery(contextNodeRef, pattern, assocTypeQNames, searchTypeQNames, ignoreAspectQNames, filterProps, sortProps, pagingRequest);

        // execute canned query
        CannedQueryResults<NodeRef> results = cq.execute();

        if (start != null)
        {
            int cnt = results.getPagedResultCount();
            int skipCount = pagingRequest.getSkipCount();
            int maxItems = pagingRequest.getMaxItems();
            boolean hasMoreItems = results.hasMoreItems();
            Pair<Integer, Integer> totalCount = (pagingRequest.getRequestTotalCountMax() > 0 ? results.getTotalResultCount() : null);
            int pageNum = (skipCount / maxItems) + 1;

            logger.debug("List: " + cnt + " items in " + (System.currentTimeMillis() - start) + " msecs [pageNum=" + pageNum + ",skip=" + skipCount + ",max=" + maxItems + ",hasMorePages=" + hasMoreItems + ",totalCount=" + totalCount + ",parentNodeRef=" + contextNodeRef + "]");
        }

        return results;
    }

    @Override
    public List<FileInfo> listFiles(NodeRef contextNodeRef)
    {
        // execute the query
        List<FileInfo> results = listSimple(contextNodeRef, true, false);
        // done
        if (logger.isTraceEnabled())
        {
            logger.trace("List files: \n" +
                    "   context: " + contextNodeRef + "\n" +
                    "   results: " + results);
        }
        return results;
    }

    @Override
    public List<FileInfo> listFolders(NodeRef contextNodeRef)
    {
        // execute the query
        List<FileInfo> results = listSimple(contextNodeRef, false, true);
        // done
        if (logger.isTraceEnabled())
        {
            logger.trace("List for folders: \n" +
                    "   context: " + contextNodeRef + "\n" +
                    "   results: " + results);
        }
        return results;
    }

    @Override
    public List<FileInfo> listDeepFolders(NodeRef contextNodeRef, SubFolderFilter filter)
    {
        List<NodeRef> nodeRefs = listSimpleDeep(contextNodeRef, false, true, filter);

        List<FileInfo> results = toFileInfo(nodeRefs);

        // done
        if (logger.isTraceEnabled())
        {
            logger.trace("Deep search for files: \n" +
                    "   context: " + contextNodeRef + "\n" +
                    "   results: " + results.size());
        }
        return results;

    }

    @Override
    public NodeRef getLocalizedSibling(NodeRef nodeRef)
    {
        Locale userLocale = I18NUtil.getLocale();

        String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
        // Work out the base name we are working with
        Pair<String, String> split = getExtension(name, false);
        String base = split.getFirst();
        String ext = split.getSecond();

        NodeRef resultNodeRef = nodeRef;
        // Search for siblings with the same name
        Control resourceHelper = Control.getControl(Control.FORMAT_DEFAULT);
        List<Locale> candidateLocales = resourceHelper.getCandidateLocales(base, userLocale);
        for (Locale candidateLocale : candidateLocales)
        {
            String filename = resourceHelper.toBundleName(base, candidateLocale) + "." + ext;
            // Attempt to find the file
            NodeRef foundNodeRef = searchSimple(parentNodeRef, filename);
            if (foundNodeRef != null) // TODO: Check for read permissions
            {
                resultNodeRef = foundNodeRef;
                break;
            }
        }
        // Done
        return resultNodeRef;
    }

    @Override
    public NodeRef searchSimple(NodeRef contextNodeRef, String name)
    {
        ParameterCheck.mandatory("name", name);
        ParameterCheck.mandatory("contextNodeRef", contextNodeRef);

        NodeRef childNodeRef = nodeService.getChildByName(contextNodeRef, ContentModel.ASSOC_CONTAINS, name);
        if (logger.isTraceEnabled())
        {
            logger.trace(
                    "Simple name search results: \n" +
                            "   parent: " + contextNodeRef + "\n" +
                            "   name: " + name + "\n" +
                            "   result: " + childNodeRef);
        }
        return childNodeRef;
    }

    /**
     * @see #search(NodeRef, String, boolean, boolean, boolean)
     */
    @Override
    @Extend(traitAPI = FileFolderServiceTrait.class, extensionAPI = FileFolderServiceExtension.class)
    public List<FileInfo> search(NodeRef contextNodeRef, String namePattern, boolean includeSubFolders)
    {
        return search(contextNodeRef, namePattern, true, true, includeSubFolders);
    }

    private static final String LUCENE_MULTI_CHAR_WILDCARD = "*";

    /**
     * Full search with all options
     */
    @Override
    @Extend(traitAPI = FileFolderServiceTrait.class, extensionAPI = FileFolderServiceExtension.class)
    public List<FileInfo> search(
            NodeRef contextNodeRef,
            String namePattern,
            boolean fileSearch,
            boolean folderSearch,
            boolean includeSubFolders)
    {
        // get the raw nodeRefs
        List<NodeRef> nodeRefs = searchInternal(contextNodeRef, namePattern, fileSearch, folderSearch, includeSubFolders);

        List<FileInfo> results = toFileInfo(nodeRefs);

        // eliminate unwanted files/folders
        Iterator<FileInfo> iterator = results.iterator();
        while (iterator.hasNext())
        {
            FileInfo file = iterator.next();
            if (file.isFolder() && !folderSearch)
            {
                iterator.remove();
            }
            else if (!file.isFolder() && !fileSearch)
            {
                iterator.remove();
            }
        }
        // done
        if (logger.isTraceEnabled())
        {
            logger.trace("Deep search: \n" +
                    "   context: " + contextNodeRef + "\n" +
                    "   pattern: " + namePattern + "\n" +
                    "   files: " + fileSearch + "\n" +
                    "   folders: " + folderSearch + "\n" +
                    "   deep: " + includeSubFolders + "\n" +
                    "   results: " + results);
        }
        return results;
    }

    /**
     * Performs a full search, but doesn't translate the node references into file info objects. This allows {@link #checkExists(NodeRef, String)} to bypass the retrieval of node properties.
     */
    private List<NodeRef> searchInternal(
            NodeRef contextNodeRef,
            String namePattern,
            boolean fileSearch,
            boolean folderSearch,
            boolean includeSubFolders)
    {
        // shortcut if the search is requesting nothing
        if (!fileSearch && !folderSearch)
        {
            return Collections.emptyList();
        }

        if (namePattern == null)
        {
            namePattern = LUCENE_MULTI_CHAR_WILDCARD; // default to wildcard
        }
        // now check if we can use Lucene to handle this query
        boolean anyName = namePattern.equals(LUCENE_MULTI_CHAR_WILDCARD);

        List<NodeRef> nodeRefs = null;
        if (anyName)
        {
            // This is search for any name
            if (includeSubFolders)
            {
                nodeRefs = listSimpleDeep(contextNodeRef, fileSearch, folderSearch, null);
            }
            else
            {
                nodeRefs = listImpl(contextNodeRef, fileSearch, folderSearch).getPage();
            }
        }
        else
        {
            // TODO - we need to get rid of this xpath stuff
            // if the name pattern is null, then we use the ANY pattern
            QueryParameterDefinition[] params = null;
            if (namePattern != null)
            {
                // the interface specifies the Lucene syntax, so perform a conversion
                namePattern = SearchLanguageConversion.convert(
                        SearchLanguageConversion.DEF_LUCENE,
                        SearchLanguageConversion.DEF_XPATH_LIKE,
                        namePattern);

                params = new QueryParameterDefinition[1];
                params[0] = new QueryParameterDefImpl(
                        ContentModel.PROP_NAME,
                        dictionaryService.getDataType(DataTypeDefinition.TEXT),
                        true,
                        namePattern);
            }
            // determine the correct query to use
            String query = null;
            if (includeSubFolders)
            {
                // this is a deep search
                if (!fileSearch && folderSearch)
                {
                    // This is a folder search only;
                    query = XPATH_QUERY_DEEP_FOLDERS;
                }
                else if (fileSearch && !folderSearch)
                {
                    // This is a folder search only;
                    query = XPATH_QUERY_DEEP_FILES;
                }
                else
                {
                    query = XPATH_QUERY_DEEP_ALL;
                }
            }
            else
            {
                // this is a shallow search
                query = XPATH_QUERY_SHALLOW_ALL;
            }
            // execute the query
            nodeRefs = searchService.selectNodes(
                    contextNodeRef,
                    query,
                    params,
                    namespaceService,
                    false);
        }
        // done
        return nodeRefs;
    }

    private List<FileInfo> listSimple(NodeRef contextNodeRef, boolean files, boolean folders) throws InvalidTypeException
    {
        CannedQueryResults<NodeRef> cq = listImpl(contextNodeRef, files, folders);
        List<NodeRef> nodeRefs = cq.getPage();

        List<FileInfo> results = toFileInfo(nodeRefs);

        // avoid re-applying permissions (for "list" canned queries)
        return PermissionCheckedValueMixin.create(results);
    }

    private Pair<Set<QName>, Set<QName>> buildSearchTypesAndIgnoreAspects(boolean files, boolean folders, Set<QName> ignoreQNameTypes)
    {
        Set<QName> searchTypeQNames = new HashSet<QName>(100);
        Set<QName> ignoreAspectQNames = null;

        // Build a list of file and folder types
        if (folders)
        {
            searchTypeQNames.addAll(buildFolderTypes());
        }
        if (files)
        {
            searchTypeQNames.addAll(buildFileTypes());
        }

        if (ignoreQNameTypes != null)
        {
            Set<QName> ignoreQNamesNotSearchTypes = new HashSet<QName>(ignoreQNameTypes);
            ignoreQNamesNotSearchTypes.removeAll(searchTypeQNames);
            ignoreQNamesNotSearchTypes.remove(ContentModel.TYPE_SYSTEM_FOLDER); // note: not included in buildFolderTypes()

            if (ignoreQNamesNotSearchTypes.size() > 0)
            {
                ignoreAspectQNames = getAspectsToIgnore(ignoreQNamesNotSearchTypes);
            }

            searchTypeQNames.removeAll(ignoreQNameTypes);
        }

        return new Pair<Set<QName>, Set<QName>>(searchTypeQNames, ignoreAspectQNames);
    }

    private Set<QName> getAspectsToIgnore(Set<QName> ignoreQNames)
    {
        Set<QName> ignoreQNameAspects = new HashSet<QName>(ignoreQNames.size());
        for (QName qname : ignoreQNames)
        {
            if (dictionaryService.getAspect(qname) != null)
            {
                ignoreQNameAspects.add(qname);
            }
        }
        return ignoreQNameAspects;
    }

    private Set<QName> buildFolderTypes()
    {
        Set<QName> folderTypeQNames = new HashSet<QName>(50);

        // Build a list of folder types
        Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_FOLDER, true);
        folderTypeQNames.addAll(qnames);
        folderTypeQNames.add(ContentModel.TYPE_FOLDER);

        // Remove 'system' folders
        qnames = dictionaryService.getSubTypes(ContentModel.TYPE_SYSTEM_FOLDER, true);
        folderTypeQNames.removeAll(qnames);
        folderTypeQNames.remove(ContentModel.TYPE_SYSTEM_FOLDER);

        return folderTypeQNames;
    }

    private Set<QName> buildFileTypes()
    {
        Set<QName> fileTypeQNames = new HashSet<QName>(50);

        // Build a list of file types
        Collection<QName> qnames = dictionaryService.getSubTypes(ContentModel.TYPE_CONTENT, true);
        fileTypeQNames.addAll(qnames);
        fileTypeQNames.add(ContentModel.TYPE_CONTENT);
        qnames = dictionaryService.getSubTypes(ContentModel.TYPE_LINK, true);
        fileTypeQNames.addAll(qnames);
        fileTypeQNames.add(ContentModel.TYPE_LINK);

        return fileTypeQNames;
    }

    /**
     * A deep version of listSimple. Which recursively walks down the tree from a given starting point, returning the node refs of files or folders found along the way.
     * <p>
     * The folder filter is called for each sub-folder to determine whether to search in that sub-folder, should a subfolder be excluded then all its chidren are excluded as well.
     * 
     * @param contextNodeRef
     *            the starting point.
     * @param files
     *            return nodes of type files.
     * @param folders
     *            return nodes of type folders.
     * @param folderFilter
     *            filter controls which folders to search. If null then all subfolders are searched.
     * @return list of node references
     */
    /* <p> MER: I've added this rather than changing listSimple to minimise the risk of breaking the existing code. This is a quick performance improvement between using XPath which is awful or adding new methods to the NodeService/DB This is also a dangerous method in that it can return a lot of data and take a long time. */
    private List<NodeRef> listSimpleDeep(NodeRef contextNodeRef, boolean files, boolean folders, SubFolderFilter folderFilter)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("searchSimpleDeep contextNodeRef:" + contextNodeRef);
        }

        // To hold the results.
        List<NodeRef> result = new ArrayList<NodeRef>();

        // Build a list of folder types
        Set<QName> folderTypeQNames = buildFolderTypes();
        Set<QName> fileTypeQNames = (files ? buildFileTypes() : new HashSet<QName>(0));

        if (!folders && !files)
        {
            return Collections.emptyList();

        }

        // Shortcut
        if (folderTypeQNames.size() == 0)
        {
            return Collections.emptyList();
        }

        Stack<NodeRef> toSearch = new Stack<NodeRef>();
        toSearch.push(contextNodeRef);

        // Now we need to walk down the folders.
        while (!toSearch.empty())
        {
            NodeRef currentDir = toSearch.pop();

            List<ChildAssociationRef> folderAssocRefs = nodeService.getChildAssocs(currentDir, folderTypeQNames);

            for (ChildAssociationRef folderRef : folderAssocRefs)
            {
                // We have some child folders
                boolean include = true;
                if (folderFilter != null)
                {
                    include = folderFilter.isEnterSubfolder(folderRef);
                    if (include)
                    {
                        // yes search in these subfolders
                        toSearch.push(folderRef.getChildRef());
                    }
                }
                else
                {
                    // No filter - Add the folders in the currentDir
                    toSearch.push(folderRef.getChildRef());
                }

                if (folders && include)
                {
                    result.add(folderRef.getChildRef());
                }
            }

            if (files)
            {
                // Add the files in the current dir
                List<ChildAssociationRef> fileAssocRefs = nodeService.getChildAssocs(currentDir, fileTypeQNames);
                for (ChildAssociationRef fileRef : fileAssocRefs)
                {
                    result.add(fileRef.getChildRef());
                }
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("searchSimpleDeep finished size:" + result.size());
        }

        // Done
        return result;
    }

    /**
     * @see #move(NodeRef, NodeRef, String)
     */
    @Override
    @Extend(traitAPI = FileFolderServiceTrait.class, extensionAPI = FileFolderServiceExtension.class)
    public FileInfo rename(NodeRef sourceNodeRef, String newName) throws FileExistsException, FileNotFoundException
    {
        return moveOrCopy(sourceNodeRef, null, null, newName, true);
    }

    /**
     * @see #moveOrCopy(NodeRef, NodeRef, NodeRef, String, boolean)
     */
    @Override
    public FileInfo move(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException
    {
        return moveOrCopy(sourceNodeRef, null, targetParentRef, newName, true);
    }

    /**
     * @see #moveOrCopy(NodeRef, NodeRef, NodeRef, String, boolean)
     */
    @Override
    public FileInfo moveFrom(NodeRef sourceNodeRef, NodeRef sourceParentRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException
    {
        return moveOrCopy(sourceNodeRef, sourceParentRef, targetParentRef, newName, true);
    }

    /**
     * @deprecated
     */
    @Override
    public FileInfo move(NodeRef sourceNodeRef, NodeRef sourceParentRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException
    {
        return moveOrCopy(sourceNodeRef, sourceParentRef, targetParentRef, newName, true);
    }

    /**
     * @see #moveOrCopy(NodeRef, NodeRef, NodeRef, String, boolean)
     */
    @Override
    public FileInfo copy(NodeRef sourceNodeRef, NodeRef targetParentRef, String newName) throws FileExistsException, FileNotFoundException
    {
        return moveOrCopy(sourceNodeRef, null, targetParentRef, newName, false);
    }

    /**
     * Implements both move and copy behaviour
     * 
     * @param move
     *            true to move, otherwise false to copy
     */
    private FileInfo moveOrCopy(NodeRef sourceNodeRef, NodeRef sourceParentRef, NodeRef targetParentRef, String newName, boolean move) throws FileExistsException, FileNotFoundException
    {
        // get file/folder in its current state
        FileInfo beforeFileInfo = toFileInfo(sourceNodeRef, true);
        // check the name - null means keep the existing name
        if (newName == null)
        {
            newName = beforeFileInfo.getName();
        }

        boolean nameChanged = (newName.equals(beforeFileInfo.getName()) == false);

        AssociationCopyInfo targetInfo = getAssociationCopyInfo(nodeService, sourceNodeRef, sourceParentRef, newName, nameChanged);
        QName qname = targetInfo.getTargetAssocQName();
        boolean isPrimaryParent = targetInfo.getSourceParentAssoc().isPrimary();
        ChildAssociationRef assocRef = targetInfo.getSourceParentAssoc();

        if (targetParentRef == null)
        {
            targetParentRef = assocRef.getParentRef();
        }

        boolean changedParent = !targetParentRef.equals(assocRef.getParentRef());
        // there is nothing to do if both the name and parent folder haven't changed
        if (!nameChanged && !changedParent)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Doing nothing - neither filename or parent has changed: \n" +
                        "   parent: " + targetParentRef + "\n" +
                        "   before: " + beforeFileInfo + "\n" +
                        "   new name: " + newName);
            }
            return beforeFileInfo;
        }

        QName targetParentType = nodeService.getType(targetParentRef);

        // Fix AWC-1517 & ALF-5569
        QName assocTypeQname = null;
        if (nameChanged && move)
        {
            // if it's a rename use the existing assoc type
            assocTypeQname = assocRef.getTypeQName();
        }
        else
        {
            if (dictionaryService.isSubClass(targetParentType, ContentModel.TYPE_FOLDER))
            {
                assocTypeQname = ContentModel.ASSOC_CONTAINS; // cm:folder -> cm:contains
            }
            else if (dictionaryService.isSubClass(targetParentType, ContentModel.TYPE_CONTAINER))
            {
                assocTypeQname = ContentModel.ASSOC_CHILDREN; // sys:container -> sys:children
            }
            else
            {
                throw new InvalidTypeException("Unexpected type (" + targetParentType + ") for target parent: " + targetParentRef);
            }
        }

        // move or copy
        NodeRef targetNodeRef = null;
        if (move)
        {
            // TODO: Replace this with a more formal means of identifying "system" folders (i.e. aspect or UUID)
            if (!isSystemPath(sourceNodeRef))
            {
                // The cm:name might clash with another node in the target location.
                if (nameChanged)
                {
                    // The name will be changing, so we really need to set the node's name to the new
                    // name. This can't be done at the same time as the move - to avoid incorrect violations
                    // of the name constraints, the cm:name is set to something random and will be reset
                    // to the correct name later.
                    nodeService.setProperty(sourceNodeRef, ContentModel.PROP_NAME, GUID.generate());
                }
                try
                {
                    ChildAssociationRef newAssocRef = null;

                    if (isPrimaryParent)
                    {
                        // move the node so that the association moves as well
                        boolean auditableBehaviorWasDisabled = preserveAuditableData && behaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE);
                        if (auditableBehaviorWasDisabled)
                        {
                            behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
                        }

                        try
                        {
                            newAssocRef = nodeService.moveNode(sourceNodeRef, targetParentRef, assocTypeQname, qname);
                        }
                        finally
                        {
                            if (auditableBehaviorWasDisabled)
                            {
                                behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
                            }
                        }
                    }
                    else
                    {
                        nodeService.removeChild(sourceParentRef, sourceNodeRef);
                        newAssocRef = nodeService.addChild(targetParentRef, sourceNodeRef, assocRef.getTypeQName(), assocRef.getQName());
                    }

                    targetNodeRef = newAssocRef.getChildRef();
                }
                catch (DuplicateChildNodeNameException e)
                {
                    throw new FileExistsException(targetParentRef, newName);
                }
            }
            else
            {
                // system path folders do not need to be moved
                targetNodeRef = sourceNodeRef;
            }
        }
        else
        {
            // Check if during copy top level name will be changed to some new
            String newNameAfterCopy = copyService.getTopLevelNodeNewName(sourceNodeRef, targetParentRef, assocTypeQname, qname);
            if (newNameAfterCopy != null && !newNameAfterCopy.equals(newName))
            {
                newName = newNameAfterCopy;
                qname = QName.createQName(
                        assocRef.getQName().getNamespaceURI(),
                        QName.createValidLocalName(newNameAfterCopy));
            }

            try
            {
                // Copy the node. The cm:name will be dropped and reset later.
                targetNodeRef = copyService.copy(
                        sourceNodeRef,
                        targetParentRef,
                        assocTypeQname,
                        qname,
                        true);
            }
            catch (DuplicateChildNodeNameException e)
            {
                throw new FileExistsException(targetParentRef, newName);
            }
        }

        // Only update the name if it has changed
        String currentName = (String) nodeService.getProperty(targetNodeRef, ContentModel.PROP_NAME);

        // ALF-13949: WorkingCopyAspect intentionally generates new names for all copies of working copies (which no
        // longer have the working copy aspect) so leave these alone after copy
        if (!currentName.equals(newName) && (move || !nodeService.hasAspect(sourceNodeRef, ContentModel.ASPECT_WORKING_COPY)))
        {
            try
            {
                // changed the name property
                nodeService.setProperty(targetNodeRef, ContentModel.PROP_NAME, newName);

                // May need to update the mimetype, to support apps using .tmp files when saving
                ContentData contentData = (ContentData) nodeService.getProperty(targetNodeRef, ContentModel.PROP_CONTENT);

                // Check the newName and oldName extensions.
                // Keep previous mimetype if
                // 1. new extension is empty
                // 2. new extension is '.tmp'
                // 3. extension was not changed,
                //
                // It fixes the ETWOTWO-16 issue.
                String oldExt = getExtension(beforeFileInfo.getName(), true).getSecond();
                String newExt = getExtension(newName, true).getSecond();
                if (contentData != null &&
                        newExt.length() != 0 &&
                        !"tmp".equalsIgnoreCase(newExt) &&
                        !newExt.equalsIgnoreCase(oldExt))
                {
                    String targetMimetype = contentData.getMimetype();
                    final ContentReader reader = contentService.getReader(targetNodeRef, ContentModel.PROP_CONTENT);
                    String newMimetype = mimetypeService.guessMimetype(newName, reader);
                    if (!targetMimetype.equalsIgnoreCase(newMimetype))
                    {
                        contentData = ContentData.setMimetype(contentData, newMimetype);
                        nodeService.setProperty(targetNodeRef, ContentModel.PROP_CONTENT, contentData);
                    }
                }
            }
            catch (DuplicateChildNodeNameException e)
            {
                throw new FileExistsException(targetParentRef, newName);
            }
        }

        // get the details after the operation
        FileInfo afterFileInfo = toFileInfo(targetNodeRef, true);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("" + (move ? "Moved" : "Copied") + " node: \n" +
                    "   parent: " + targetParentRef + "\n" +
                    "   before: " + beforeFileInfo + "\n" +
                    "   after: " + afterFileInfo);
        }
        return afterFileInfo;
    }

    /**
     * Determine if the specified node is a special "system" folder path based node
     * 
     * TODO: Replace this with a more formal means of identifying "system" folders (i.e. aspect or UUID)
     * 
     * @param nodeRef
     *            node to check
     * @return true => system folder path based node
     */
    private boolean isSystemPath(NodeRef nodeRef)
    {
        Path path = nodeService.getPath(nodeRef);
        String prefixedPath = path.toPrefixString(namespaceService);
        return systemPaths.contains(prefixedPath);
    }

    @Override
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName) throws FileExistsException
    {
        return createImpl(parentNodeRef, name, typeQName, null);
    }

    @Override
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName, QName assocQName) throws FileExistsException
    {
        return createImpl(parentNodeRef, name, typeQName, assocQName);
    }

    private FileInfo createImpl(NodeRef parentNodeRef, String name, QName typeQName, QName assocQName) throws FileExistsException
    {
        // set up initial properties
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(11);
        properties.put(ContentModel.PROP_NAME, (Serializable) name);

        // Check the type is valid for file/folder service
        FileFolderServiceType type = getType(typeQName);

        switch (type)
        {
        case SYSTEM_FOLDER:
            throw new InvalidTypeException("System Folders are not handled by this service :" + typeQName);
        case INVALID:
            throw new InvalidTypeException("Type is not handled by this service: " + typeQName);
        case FILE:
        case FOLDER:
        default:
        }

        // create the node
        if (assocQName == null)
        {
            assocQName = QName.createQName(
                    NamespaceService.CONTENT_MODEL_1_0_URI,
                    QName.createValidLocalName(name));
        }
        ChildAssociationRef assocRef = null;
        try
        {
            assocRef = nodeService.createNode(
                    parentNodeRef,
                    ContentModel.ASSOC_CONTAINS,
                    assocQName,
                    typeQName,
                    properties);
        }
        catch (DuplicateChildNodeNameException e)
        {
            throw new FileExistsException(parentNodeRef, name);
        }

        NodeRef nodeRef = assocRef.getChildRef();

        FileInfo fileInfo = toFileInfo(nodeRef, true);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created: \n" +
                    "   parent: " + parentNodeRef + "\n" +
                    "   created: " + fileInfo);
        }
        return fileInfo;
    }

    @Override
    public void delete(NodeRef nodeRef)
    {
        nodeService.deleteNode(nodeRef);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted: \n" +
                    "   node: " + nodeRef);
        }
    }

    /**
     * Checks for the presence of, and creates as necessary, the folder structure in the provided path.
     * <p>
     * An empty path list is not allowed as it would be impossible to necessarily return file info for the parent node - it might not be a folder node.
     * 
     * @param parentNodeRef
     *            the node under which the path will be created
     * @param pathElements
     *            the folder name path to create - may not be empty
     * @param folderTypeQName
     *            the types of nodes to create. This must be a valid subtype of {@link org.alfresco.model.ContentModel#TYPE_FOLDER they folder type}.
     * @return Returns the info of the last folder in the path.
     * @deprecated Use FileFolderUtil.makeFolders rather than directly accessing this implementation class.
     */
    public FileInfo makeFolders(NodeRef parentNodeRef, List<String> pathElements, QName folderTypeQName)
    {
        return FileFolderUtil.makeFolders(this, parentNodeRef, pathElements, folderTypeQName);
    }

    /**
     * Checks for the presence of, and creates as necessary, the folder structure in the provided path.
     * <p>
     * An empty path list is not allowed as it would be impossible to necessarily return file info for the parent node - it might not be a folder node.
     * 
     * @param parentNodeRef
     *            the node under which the path will be created
     * @param pathElements
     *            the folder name path to create - may not be empty
     * @param folderTypeQName
     *            the types of nodes to create. This must be a valid subtype of {@link org.alfresco.model.ContentModel#TYPE_FOLDER they folder type}.
     * @return Returns the info of the last folder in the path.
     * @deprecated Use FileFolderUtil.makeFolders rather than directly accessing this implementation class.
     */
    public static FileInfo makeFolders(FileFolderService service, NodeRef parentNodeRef, List<String> pathElements, QName folderTypeQName)
    {
        return FileFolderUtil.makeFolders(service, parentNodeRef, pathElements, folderTypeQName);
    }

    /**
     * Get the file or folder information from the root down to and including the node provided.
     * <ul>
     * <li>The root node can be of any type and is not included in the path list.</li>
     * <li>Only the primary path is considered. If the target node is not a descendant of the root along purely primary associations, then an exception is generated.</li>
     * <li>If an invalid type is encountered along the path, then an exception is generated.</li>
     * </ul>
     * 
     * @param rootNodeRef
     *            the start of the returned path, or null if the <b>store</b> root node must be assumed.
     * @param nodeRef
     *            a reference to the file or folder
     * @return Returns a list of file/folder infos from the root (excluded) down to and including the destination file or folder
     * @throws FileNotFoundException
     *             if the node could not be found
     */
    @Override
    public List<FileInfo> getNamePath(NodeRef rootNodeRef, NodeRef nodeRef) throws FileNotFoundException
    {
        // check the root
        if (rootNodeRef == null)
        {
            rootNodeRef = nodeService.getRootNode(nodeRef.getStoreRef());
        }
        try
        {
            ArrayList<FileInfo> results = new ArrayList<FileInfo>(10);
            // get the primary path
            Path path = nodeService.getPath(nodeRef);
            // iterate and turn the results into file info objects
            boolean foundRoot = false;
            for (Path.Element element : path)
            {
                // ignore everything down to the root
                Path.ChildAssocElement assocElement = (Path.ChildAssocElement) element;
                final NodeRef childNodeRef = assocElement.getRef().getChildRef();
                if (childNodeRef.equals(rootNodeRef))
                {
                    // just found the root - but we don't put in an entry for it
                    foundRoot = true;
                    continue;
                }
                else if (!foundRoot)
                {
                    // keep looking for the root
                    continue;
                }
                // we found the root and expect to be building the path up
                // Run as system as the user could not have access to all folders in the path, see ALF-13816
                FileInfo pathInfo = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<FileInfo>() {
                    public FileInfo doWork() throws Exception
                    {
                        return toFileInfo(childNodeRef, true);
                    }
                }, AuthenticationUtil.getSystemUserName());

                // we can't append a path element to the results if there is already a (non-folder) file at the tail
                // since this would result in a path anomoly - file's cannot contain other files.
                if (!results.isEmpty() && !results.get(results.size() - 1).isFolder())
                {
                    throw new InvalidTypeException(
                            "File is not the last element in path: files cannot contain other files.");
                }
                results.add(pathInfo);
            }
            // check that we found the root
            if (!foundRoot)
            {
                throw new FileNotFoundException(nodeRef);
            }
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Built name path for node: \n" +
                        "   root: " + rootNodeRef + "\n" +
                        "   node: " + nodeRef + "\n" +
                        "   path: " + results);
            }
            return results;
        }
        catch (InvalidNodeRefException e)
        {
            throw new FileNotFoundException(nodeRef);
        }
    }

    /**
     * Get the file or folder names from the root down to and including the node provided.
     * <ul>
     * <li>The root node can be of any type and is not included in the path list.</li>
     * <li>Only the primary path is considered. If the target node is not a descendant of the root along purely primary associations, then an exception is generated.</li>
     * <li>If an invalid type is encountered along the path, then an exception is generated.</li>
     * </ul>
     * 
     * @param rootNodeRef
     *            the start of the returned path, or null if the <b>store</b> root node must be assumed.
     * @param nodeRef
     *            a reference to the file or folder
     * @return Returns a list of file/folder names from the root (excluded) down to and including the destination file or folder
     * @throws FileNotFoundException
     *             if the node could not be found
     */
    @Override
    public List<String> getNameOnlyPath(NodeRef rootNodeRef, final NodeRef nodeRef) throws FileNotFoundException
    {
        // check the root
        if (rootNodeRef == null)
        {
            rootNodeRef = nodeService.getRootNode(nodeRef.getStoreRef());
        }
        try
        {
            final NodeRef rNodeRef = rootNodeRef;
            final ArrayList<String> results = new ArrayList<String>(10);
            // Run as system as the user could not have access to all folders in the path, see ALF-13816
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
                public Void doWork() throws Exception
                {
                    // get the primary path
                    Path path = nodeService.getPath(nodeRef);
                    // iterate and turn the results into file info objects
                    boolean foundRoot = false;
                    for (Path.Element element : path)
                    {
                        // ignore everything down to the root
                        Path.ChildAssocElement assocElement = (Path.ChildAssocElement) element;
                        final NodeRef childNodeRef = assocElement.getRef().getChildRef();
                        if (childNodeRef.equals(rNodeRef))
                        {
                            // just found the root - but we don't put in an entry for it
                            foundRoot = true;
                            continue;
                        }
                        else if (!foundRoot)
                        {
                            // keep looking for the root
                            continue;
                        }
                        results.add(nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME).toString());
                    }
                    // check that we found the root
                    if (!foundRoot)
                    {
                        throw new FileNotFoundException(nodeRef);
                    }
                    // done
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Built name path for node: \n" +
                                "   root: " + rNodeRef + "\n" +
                                "   node: " + nodeRef + "\n" +
                                "   path: " + results);
                    }
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());

            return results;
        }
        catch (InvalidNodeRefException e)
        {
            throw new FileNotFoundException(nodeRef);
        }
        catch (RuntimeException e)
        {
            // the runAs() is too keen on wrapping everything in an outer RuntimeException - which we don't want.
            if (e.getCause() instanceof FileNotFoundException)
            {
                throw (FileNotFoundException) e.getCause();
            }
            else
                throw e;
        }
    }

    @Override
    public FileInfo resolveNamePath(NodeRef rootNodeRef, List<String> pathElements) throws FileNotFoundException
    {
        return resolveNamePath(rootNodeRef, pathElements, true);
    }

    @Override
    public FileInfo resolveNamePath(NodeRef rootNodeRef, List<String> pathElements, boolean mustExist) throws FileNotFoundException
    {
        if (pathElements.size() == 0)
        {
            throw new IllegalArgumentException("Path elements list is empty");
        }
        // walk the folder tree first
        NodeRef parentNodeRef = rootNodeRef;
        StringBuilder currentPath = new StringBuilder(pathElements.size() << 4);
        int folderCount = pathElements.size() - 1;
        for (int i = 0; i < folderCount; i++)
        {
            String pathElement = pathElements.get(i);
            currentPath.append("/").append(pathElement);
            NodeRef folderNodeRef = searchSimple(parentNodeRef, pathElement);
            if (folderNodeRef == null)
            {
                if (mustExist)
                {
                    throw new FileNotFoundException("Folder not found: " + currentPath + " (in " + rootNodeRef + ")");
                }
                else
                {
                    return null;
                }
            }
            parentNodeRef = folderNodeRef;
        }
        // we have resolved the folder path - resolve the last component
        String pathElement = pathElements.get(pathElements.size() - 1);
        currentPath.append("/").append(pathElement);
        NodeRef fileNodeRef = searchSimple(parentNodeRef, pathElement);
        if (fileNodeRef == null)
        {
            if (mustExist)
            {
                throw new FileNotFoundException("File not found: " + currentPath + " (in " + rootNodeRef + ")");
            }
            else
            {
                return null;
            }
        }
        FileInfo result = getFileInfo(fileNodeRef);
        // found it
        if (logger.isDebugEnabled())
        {
            logger.debug("Resoved path element: \n" +
                    "   root: " + rootNodeRef + "\n" +
                    "   path: " + currentPath + "\n" +
                    "   node: " + result);
        }
        return result;
    }

    @Override
    public FileInfo getFileInfo(NodeRef nodeRef)
    {
        try
        {
            return toFileInfo(nodeRef, true);
        }
        catch (InvalidTypeException e)
        {
            return null;
        }
    }

    @Override
    public ContentReader getReader(NodeRef nodeRef)
    {
        FileInfo fileInfo = toFileInfo(nodeRef, false);
        if (fileInfo.isFolder())
        {
            throw new InvalidTypeException("Unable to get a content reader for a folder: " + fileInfo);
        }
        return contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
    }

    @Override
    public ContentWriter getWriter(NodeRef nodeRef)
    {
        FileInfo fileInfo = toFileInfo(nodeRef, false);
        if (fileInfo.isFolder())
        {
            throw new InvalidTypeException("Unable to get a content writer for a folder: " + fileInfo);
        }
        final ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        // Ensure that a mimetype is set based on the filename (ALF-6560)
        // This has been removed from the create code in 3.4 to prevent insert-update behaviour
        // of the ContentData.
        if (writer.getMimetype() == null)
        {
            final String name = fileInfo.getName();
            writer.guessMimetype(name);
        }
        // Done
        return writer;
    }

    /**
     * Split a filename into the base (part before the '.') and the extension (part after the '.')
     */
    private Pair<String, String> getExtension(String name, boolean useLastDot)
    {
        String ext = "";
        String base = name;
        if (name != null)
        {
            name = name.trim();
            int index = useLastDot ? name.lastIndexOf('.') : name.indexOf('.');
            if (index > -1 && (index < name.length() - 1))
            {
                base = name.substring(0, index);
                ext = name.substring(index + 1);
            }
        }
        return new Pair<String, String>(base, ext);
    }

    @Override
    public void setHidden(NodeRef nodeRef, boolean isHidden)
    {
        int mask = 0;
        boolean allVisible = true;
        Visibility webDavVisibility = isHidden ? Visibility.NotVisible : Visibility.Visible;
        for (Client client : hiddenAspect.getClients())
        {
            Visibility clientVisibility = client == FileFilterMode.getClient() ? webDavVisibility
                    : hiddenAspect
                            .getVisibility(client, nodeRef);
            if (clientVisibility != Visibility.Visible)
            {
                allVisible = false;
            }
            mask |= hiddenAspect.getClientVisibilityMask(client, clientVisibility);
        }
        if (allVisible)
        {
            hiddenAspect.showNode(nodeRef, true);
        }
        else
        {
            hiddenAspect.hideNode(nodeRef, mask, true, true, false);
        }
    }

    @Override
    public boolean isHidden(NodeRef nodeRef)
    {
        return hiddenAspect.getVisibility(FileFilterMode.getClient(), nodeRef) != Visibility.Visible;
    }

    @Override
    public <M extends Trait> ExtendedTrait<M> getTrait(Class<? extends M> traitAPI)
    {
        return (ExtendedTrait<M>) fileFolderTrait;
    }

    private FileFolderServiceTrait createFileFolderTrait()
    {
        return new FileFolderServiceTraitImpl(this);
    }

    public static class FileFolderServiceTraitImpl implements FileFolderServiceTrait
    {
        private FileFolderServiceImpl thisService;

        public FileFolderServiceTraitImpl(FileFolderServiceImpl fileFolderServiceImpl)
        {
            thisService = fileFolderServiceImpl;
        }

        @Override
        public Pair<Set<QName>, Set<QName>> buildSearchTypesAndIgnoreAspects(boolean files, boolean folders,
                Set<QName> ignoreQNameTypes)
        {
            return thisService.buildSearchTypesAndIgnoreAspects(files,
                    folders,
                    ignoreQNameTypes);
        }

        @Override
        public FileInfo createFileInfo(NodeRef nodeRef, QName typeQName, boolean isFolder, boolean isHidden,
                Map<QName, Serializable> properties)
        {
            return new FileInfoImpl(nodeRef,
                    typeQName,
                    isFolder,
                    isHidden,
                    properties);
        }

        @Override
        public FileFolderServiceType getType(QName typeQName)
        {
            return thisService.getType(typeQName);
        }

        @Override
        public List<FileInfo> list(final NodeRef contextNodeRef)
        {

            return thisService.list(contextNodeRef);
        }

        @Override
        public PagingResults<FileInfo> list(final NodeRef contextNodeRef, final boolean files, final boolean folders,
                final String pattern, final Set<QName> ignoreQNames, final List<Pair<QName, Boolean>> sortProps,
                final PagingRequest pagingRequest)
        {
            return thisService.list(contextNodeRef,
                    files,
                    folders,
                    pattern,
                    ignoreQNames,
                    sortProps,
                    pagingRequest);
        }

        @Override
        public PagingResults<FileInfo> list(final NodeRef rootNodeRef, final Set<QName> searchTypeQNames,
                final Set<QName> ignoreAspectQNames, final List<Pair<QName, Boolean>> sortProps,
                final PagingRequest pagingRequest)
        {
            return thisService.list(rootNodeRef,
                    searchTypeQNames,
                    ignoreAspectQNames,
                    sortProps,
                    pagingRequest);
        }

        @Override
        public List<FileInfo> search(final NodeRef contextNodeRef, final String namePattern, final boolean fileSearch,
                final boolean folderSearch, final boolean includeSubFolders)
        {
            return thisService.search(contextNodeRef,
                    namePattern,
                    fileSearch,
                    folderSearch,
                    includeSubFolders);
        }

        @Override
        public FileInfo rename(final NodeRef sourceNodeRef, final String newName) throws FileExistsException, FileNotFoundException
        {

            return thisService.rename(sourceNodeRef, newName);

        }

        public PagingResults<FileInfo> list(NodeRef contextNodeRef,
                boolean files,
                boolean folders,
                Set<QName> ignoreQNames,
                List<Pair<QName, Boolean>> sortProps,
                PagingRequest pagingRequest)
        {
            return thisService.list(contextNodeRef,
                    files,
                    folders,
                    ignoreQNames,
                    sortProps,
                    pagingRequest);
        }
    };
}
