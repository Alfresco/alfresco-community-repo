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
package org.alfresco.opencmis;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.opencmis.dictionary.CMISObjectVariant;
import org.alfresco.opencmis.dictionary.FolderTypeDefintionWrapper;
import org.alfresco.opencmis.dictionary.PropertyDefinitionWrapper;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.Authorization;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.CmisEnumHelper;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateObjectIdAndChangeTokenImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.server.RenditionInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.server.RenditionInfo;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.server.shared.QueryStringHttpServletRequestWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.acegisecurity.Authentication;

/**
 * OpenCMIS service implementation
 * 
 * @author florian.mueller
 * @author Derek Hulley
 * @author janv
 *
 * @since 4.0
 */
public class AlfrescoCmisServiceImpl extends AbstractCmisService implements AlfrescoCmisService
{
    private static Log logger = LogFactory.getLog(AlfrescoCmisService.class);

    private static final String MIN_FILTER = "cmis:name,cmis:baseTypeId,cmis:objectTypeId,"
            + "cmis:createdBy,cmis:creationDate,cmis:lastModifiedBy,cmis:lastModificationDate,"
            + "cmis:contentStreamLength,cmis:contentStreamMimeType,cmis:contentStreamFileName,"
            + "cmis:contentStreamId";

    private CMISConnector connector;
    private Authentication authentication;
    private Map<String, CMISNodeInfo> nodeInfoMap;
    private Map<String, ObjectInfo> objectInfoMap;

    public AlfrescoCmisServiceImpl(CMISConnector connector)
    {
        this.connector = connector;
        nodeInfoMap = new HashMap<>();
        objectInfoMap = new HashMap<>();
    }

    @Override
    public void open(CallContext context)
    {
        AlfrescoCmisServiceCall.set(context);
    }
    
    protected CallContext getContext()
    {
    	CallContext context = AlfrescoCmisServiceCall.get();
    	return context;
    }
    
    @Override
    public void close()
    {
        AlfrescoCmisServiceCall.clear();

        // Put these resources on the transactions
        nodeInfoMap.clear();
        objectInfoMap.clear();
    }

    protected CMISNodeInfoImpl createNodeInfo(NodeRef nodeRef)
    {
        return createNodeInfo(nodeRef, null, true);
    }

    protected CMISNodeInfoImpl createNodeInfo(NodeRef nodeRef, VersionHistory versionHistory, boolean checkExists)
    {
        return createNodeInfo(nodeRef, null, null, versionHistory, checkExists);
    }

    protected CMISNodeInfoImpl createNodeInfo(NodeRef nodeRef, QName nodeType, Map<QName,Serializable> nodeProps, VersionHistory versionHistory, boolean checkExists)
    {
        CMISNodeInfoImpl result = connector.createNodeInfo(nodeRef, nodeType, nodeProps, versionHistory, checkExists);
        nodeInfoMap.put(result.getObjectId(), result);

        return result;
    }
    
    protected CMISNodeInfo createNodeInfo(AssociationRef assocRef)
    {
        CMISNodeInfoImpl result = connector.createNodeInfo(assocRef);
        nodeInfoMap.put(result.getObjectId(), result);

        return result;
    }

    protected CMISNodeInfo getOrCreateNodeInfo(String objectId)
    {
        CMISNodeInfo result = nodeInfoMap.get(objectId);
        if (result == null)
        {
            result = connector.createNodeInfo(objectId);
            nodeInfoMap.put(objectId, result);
        }

        return result;
    }

    protected CMISNodeInfo getOrCreateNodeInfo(String objectId, String what)
    {
        CMISNodeInfo result = getOrCreateNodeInfo(objectId);
        if (result instanceof CMISNodeInfoImpl)
        {
            ((CMISNodeInfoImpl) result).checkIfUseful(what);
        }

        return result;
    }

    protected CMISNodeInfo getOrCreateFolderInfo(String folderId, String what)
    {
        CMISNodeInfo result = getOrCreateNodeInfo(folderId);
        if (result instanceof CMISNodeInfoImpl)
        {
            ((CMISNodeInfoImpl) result).checkIfFolder(what);
        }

        return result;
    }

    protected CMISNodeInfo addNodeInfo(CMISNodeInfo info)
    {
        nodeInfoMap.put(info.getObjectId(), info);

        return info;
    }

    // --- repository service ---

    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension)
    {
    	CmisVersion cmisVersion = getContext().getCmisVersion();
        return Collections.singletonList(connector.getRepositoryInfo(cmisVersion));
    }

    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

    	CmisVersion cmisVersion = getContext().getCmisVersion();
        return connector.getRepositoryInfo(cmisVersion);
    }

    @Override
    public TypeDefinitionList getTypeChildren(
            String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // convert BigIntegers to int
        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        int skip = (skipCount == null || skipCount.intValue() < 0 ? 0 : skipCount.intValue());

        // set up the result
        TypeDefinitionListImpl result = new TypeDefinitionListImpl();
        List<TypeDefinition> list = new ArrayList<TypeDefinition>();
        result.setList(list);

        // get the types from the dictionary
        List<TypeDefinitionWrapper> childrenList;
        if (typeId == null)
        {
            childrenList = connector.getOpenCMISDictionaryService().getBaseTypes(true);
        }
        else
        {
        	TypeDefinitionWrapper tdw = connector.getOpenCMISDictionaryService().findType(typeId);
        	if (tdw == null)
        	{
        		throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        	}
        	childrenList = connector.getOpenCMISDictionaryService().getChildren(typeId);
//            childrenList = tdw.getChildren();
        }

        // create result
        if (max > 0)
        {
            int lastIndex = (max + skip > childrenList.size() ? childrenList.size() : max + skip) - 1;
            for (int i = skip; i <= lastIndex; i++)
            {
                list.add(childrenList.get(i).getTypeDefinition(includePropertyDefinitions));
            }
        }

        result.setHasMoreItems(childrenList.size() - skip > result.getList().size());
        result.setNumItems(BigInteger.valueOf(childrenList.size()));

        return result;
    }

    @Override
    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // find the type
        TypeDefinitionWrapper tdw = connector.getOpenCMISDictionaryService().findType(typeId);
        if (tdw == null)
        {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        // return type definition
        return tdw.getTypeDefinition(true);
    }

    @Override
    public List<TypeDefinitionContainer> getTypeDescendants(
            String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();

        // check depth
        int d = (depth == null ? -1 : depth.intValue());
        if (d == 0)
        {
            throw new CmisInvalidArgumentException("Depth must not be 0!");
        }

        if (typeId == null)
        {
            for (TypeDefinitionWrapper tdw : connector.getOpenCMISDictionaryService().getBaseTypes(true))
            {
                result.add(getTypesDescendants(d, tdw, includePropertyDefinitions));
            }
        }
        else
        {
            TypeDefinitionWrapper tdw = connector.getOpenCMISDictionaryService().findType(typeId);
            if (tdw == null)
            {
                throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
            }
        	List<TypeDefinitionWrapper> children = connector.getOpenCMISDictionaryService().getChildren(typeId);
            if(children != null)
            {
                for (TypeDefinitionWrapper child : children)
                {
                    result.add(getTypesDescendants(d, child, includePropertyDefinitions));
                }
            }
        }

        return result;
    }

    /**
     * Gathers the type descendants tree.
     */
    private TypeDefinitionContainer getTypesDescendants(
            int depth, TypeDefinitionWrapper tdw, boolean includePropertyDefinitions)
    {
        TypeDefinitionContainerImpl result = new TypeDefinitionContainerImpl();

        result.setTypeDefinition(tdw.getTypeDefinition(includePropertyDefinitions));

        if (depth != 0)
        {
        	String typeId = tdw.getTypeId();
        	List<TypeDefinitionWrapper> children = connector.getOpenCMISDictionaryService().getChildren(typeId);
            if (children != null)
            {
                result.setChildren(new ArrayList<TypeDefinitionContainer>());
                for (TypeDefinitionWrapper tdc : children)
                {
                    result.getChildren().add(
                            getTypesDescendants(depth < 0 ? -1 : depth - 1, tdc, includePropertyDefinitions));
                }
            }
        }

        return result;
    }

    // --- navigation service ---

    @Override
    public ObjectInFolderList getChildren(
            String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension)
    {
        long start = System.currentTimeMillis();

        checkRepositoryId(repositoryId);

        // convert BigIntegers to int
        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        int skip = (skipCount == null || skipCount.intValue() < 0 ? 0 : skipCount.intValue());

        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        List<ObjectInFolderData> list = new ArrayList<ObjectInFolderData>();
        result.setObjects(list);

        // get the children references
        NodeRef folderNodeRef = getOrCreateFolderInfo(folderId, "Folder").getNodeRef();

        // convert orderBy to sortProps
        List<Pair<QName, Boolean>> sortProps = null;
        if (orderBy != null)
        {
            sortProps = new ArrayList<Pair<QName, Boolean>>(1);

            String[] parts = orderBy.split(",");
            int len = parts.length;
            final int origLen = len;

            if (origLen > 0)
            {
                int maxSortProps = GetChildrenCannedQuery.MAX_FILTER_SORT_PROPS;
                if (len > maxSortProps)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug(
                                "Too many sort properties in 'orderBy' - ignore those above max (max="
                                + maxSortProps + ",actual=" + len + ")");
                    }
                    len = maxSortProps;
                }
                for (int i = 0; i < len; i++)
                {
                    String[] sort = parts[i].split(" +");
                    
                    if (sort.length > 0)
                    {
                        // MNT-10529: Leading spaces result in an empty string value after the split operation. Leading spaces may occur in the 'orderBy' statement when the
                        // elements of statement separated by a comma and a space(s)
                        int index = (sort[0].isEmpty()) ? (1) : (0);

                        Pair<QName, Boolean> sortProp = connector.getSortProperty(sort[index], sort.length > (index + 1) ? sort[index + 1] : null);
                        sortProps.add(sortProp);
                    }
                }
            }

            if (sortProps.size() < origLen)
            {
                logger.warn("Sort properties trimmed - either too many and/or not found: \n" + "   orig:  " + orderBy
                        + "\n" + "   final: " + sortProps);
            }
        }

        PagingRequest pageRequest = new PagingRequest(skip, max, null);
        pageRequest.setRequestTotalCountMax(skip + 10000); // TODO make this optional/configurable
                                                           // - affects whether numItems may be returned 
        Set<QName> typeqnames = new HashSet<QName>();
        List<TypeDefinitionWrapper> allTypes = connector.getOpenCMISDictionaryService().getAllTypes();
        for (TypeDefinitionWrapper type : allTypes)
        {
        	typeqnames.add(type.getAlfrescoClass());
        }
        PagingResults<FileInfo> pageOfNodeInfos = connector.getFileFolderService().list(
        		folderNodeRef, 
        		typeqnames, 
        		null, //ignoreAspectQNames, 
        		sortProps, 
        		pageRequest);

        if (max > 0)
        {
            for (FileInfo child : pageOfNodeInfos.getPage())
            {
                try
                {
                	// TODO this will break the paging if filtering is performed...
                    if(connector.filter(child.getNodeRef()))
                    {
                        continue;
                    }

                    // create a child CMIS object
                    CMISNodeInfo ni = createNodeInfo(child.getNodeRef(), child.getType(), child.getProperties(), null, false); // note: checkExists=false (don't need to check again)

                    // ignore invalid children
                    // Skip non-cmis objects
                    if (ni.getObjectVariant() == CMISObjectVariant.INVALID_ID
                            || ni.getObjectVariant() == CMISObjectVariant.NOT_EXISTING
                            || ni.getObjectVariant() == CMISObjectVariant.NOT_A_CMIS_OBJECT
                            || ni.getObjectVariant() == CMISObjectVariant.PERMISSION_DENIED)
                    {
                        continue;
                    }

                    ObjectData object = connector.createCMISObject(ni, filter, includeAllowableActions,
                            includeRelationships, renditionFilter, false, false/*, getContext().getCmisVersion()*/);

                	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
                    if (isObjectInfoRequired)
                    {
                        getObjectInfo(repositoryId, ni.getObjectId(), includeRelationships);
                    }

                    ObjectInFolderDataImpl childData = new ObjectInFolderDataImpl();
                    childData.setObject(object);

                    // include path segment
                    if (includePathSegment)
                    {
                        childData.setPathSegment(child.getName());
                    }

                    // add it
                    list.add(childData);
                }
                catch (InvalidNodeRefException e)
                {
                    // ignore invalid children
                }
                catch(CmisObjectNotFoundException e)
                {
                    // ignore objects that have not been found (perhaps because their type is unknown to CMIS)
                }
            }
        }

        // has more ?
        result.setHasMoreItems(pageOfNodeInfos.hasMoreItems());

        // total count ?
        Pair<Integer, Integer> totalCounts = pageOfNodeInfos.getTotalResultCount();
        if (totalCounts != null)
        {
            Integer totalCountLower = totalCounts.getFirst();
            Integer totalCountUpper = totalCounts.getSecond();
            if ((totalCountLower != null) && (totalCountLower.equals(totalCountUpper)))
            {
                result.setNumItems(BigInteger.valueOf(totalCountLower));
            }
        }

        logGetObjectsCall("getChildren", start, folderId, list.size(), filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePathSegment, extension, skipCount, maxItems, orderBy, null);

        return result;
    }

    @Override
    public List<ObjectInFolderContainer> getDescendants(
            String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension)
    {
        long start = System.currentTimeMillis();

        checkRepositoryId(repositoryId);

        List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();

        getDescendantsTree(
                repositoryId,
                getOrCreateFolderInfo(folderId, "Folder").getNodeRef(),
                depth.intValue(),
                filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, false,
                result);

        logGetObjectsCall("getDescendants", start, folderId, countDescendantsTree(result), filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePathSegment, extension, null, null, null, depth);

        return result;
    }

    @Override
    public List<ObjectInFolderContainer> getFolderTree(
            String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension)
    {
        long start = System.currentTimeMillis();

        checkRepositoryId(repositoryId);

        List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();

        getDescendantsTree(
                repositoryId,
                getOrCreateFolderInfo(folderId, "Folder").getNodeRef(),
                depth.intValue(),
                filter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, true,
                result);

        logGetObjectsCall("getFolderTree", start, folderId, countDescendantsTree(result), filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePathSegment, extension, null, null, null, depth);

        return result;
    }

    protected void logGetObjectsCall(String methodName, long start, String folderId, int itemCount,
                                     String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
                                     String renditionFilter, Boolean includePathSegment, ExtensionsData extensionsData,
                                     BigInteger skipCount, BigInteger maxItems, String orderBy, BigInteger depth)
    {
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append(methodName).append(": ").append(folderId).append(" - ").append(itemCount)
                    .append(" in ").append(System.currentTimeMillis()-start).append(" ms")
                    .append(" [includeAllowableActions=").append(includeAllowableActions)
                    .append(",includeRelationships=").append(includeRelationships).append(",renditionFilter=").append(renditionFilter);

            if (includePathSegment != null) {
                sb.append((methodName.equals("getObjectParents") ? ",includeRelativePathSegment=" : ",includePathSegment="))
                        .append(includePathSegment);
            }

            if (filter != null) { sb.append(",filter=").append(filter); }
            if (skipCount != null) { sb.append(",skipCount=").append(skipCount); }
            if (maxItems != null) { sb.append(",maxItems=").append(maxItems); }
            if (orderBy != null) { sb.append(",orderBy=").append(orderBy); }
            if (depth != null) { sb.append(",depth=").append(depth); }
            if (extensionsData != null) { sb.append(",extensionsData=").append(extensionsData); }

            sb.append("]");

            logger.debug(sb.toString());
        }
        else if (logger.isInfoEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append(methodName).append(": ").append(folderId).append(" - ").append(itemCount)
                    .append(" in ").append(System.currentTimeMillis()-start).append(" ms");

            logger.info(sb.toString());
        }
    }

    private int countDescendantsTree(List<ObjectInFolderContainer> tree) {
        int c = tree.size();
        for (ObjectInFolderContainer o : tree) {
            c += countDescendantsTree(o.getChildren());
        }
        return c;
    }

    protected void logGetObjectCall(String methodName, long start, String idOrPath,
                                    String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
                                    String renditionFilter, Boolean includePolicyIds, Boolean includeAcl,
                                    Boolean isObjectInfoRequired, ExtensionsData extensionsData)
    {
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append(methodName).append(": ").append(idOrPath).append(" in ").append(System.currentTimeMillis()-start).append(" ms")
                    .append(" [includeAllowableActions=").append(includeAllowableActions)
                    .append(",includeRelationships=").append(includeRelationships).append(",renditionFilter=").append(renditionFilter)
                    .append(",includePolicyIds=").append(includePolicyIds).append(",includeAcl=").append(includeAcl)
                    .append(",isObjectInfoRequired=").append(isObjectInfoRequired);

            if (filter != null) { sb.append(",filter=").append(filter); }
            if (extensionsData != null) { sb.append(",extensionsData=").append(extensionsData); }

            sb.append("]");

            logger.debug(sb.toString());
        }
        else if (logger.isInfoEnabled())
        {
            StringBuilder sb = new StringBuilder();
            sb.append(methodName).append(": ").append(idOrPath).append(" in ").append(System.currentTimeMillis()-start).append(" ms");

            logger.info(sb.toString());
        }
    }

    private void getDescendantsTree(
            String repositoryId, NodeRef folderNodeRef, int depth, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, boolean foldersOnly, List<ObjectInFolderContainer> list)
    {
        // get the children references
        List<ChildAssociationRef> childrenList = connector.getNodeService().getChildAssocs(folderNodeRef);
        for (ChildAssociationRef child : childrenList)
        {
            try
            {
                TypeDefinitionWrapper type = connector.getType(child.getChildRef());
                if (type == null)
                {
                    continue;
                }

                boolean isFolder = (type instanceof FolderTypeDefintionWrapper);

                if (foldersOnly && !isFolder)
                {
                    continue;
                }
                
                if(isFolder && type.getAlfrescoClass().equals(ContentModel.TYPE_SYSTEM_FOLDER))
                {
                    continue;
                }
                
                if(connector.isHidden(child.getChildRef()))
                {
                    continue;
                }

                if(connector.filter(child.getChildRef()))
                {
                    continue;
                }
                
            	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();

                // create a child CMIS object
                ObjectInFolderDataImpl object = new ObjectInFolderDataImpl();
                CMISNodeInfo ni = createNodeInfo(child.getChildRef(), null, false); // note: checkExists=false (don't need to check again)
                object.setObject(connector.createCMISObject(
                        ni, filter, includeAllowableActions, includeRelationships,
                        renditionFilter, false, false));
                if (isObjectInfoRequired)
                {
                    getObjectInfo(repositoryId, ni.getObjectId(), includeRelationships);
                }

                if (includePathSegment)
                {
                    object.setPathSegment(connector.getName(child.getChildRef()));
                }

                // create the container
                ObjectInFolderContainerImpl container = new ObjectInFolderContainerImpl();
                container.setObject(object);

                if ((depth != 1) && isFolder)
                {
                    container.setChildren(new ArrayList<ObjectInFolderContainer>());
                    getDescendantsTree(
                            repositoryId,
                            child.getChildRef(),
                            depth - 1, filter, includeAllowableActions,
                            includeRelationships, renditionFilter, includePathSegment, foldersOnly,
                            container.getChildren());
                }

                // add it
                list.add(container);
            }
            catch (InvalidNodeRefException e)
            {
                // ignore invalid children
            }
            catch(CmisObjectNotFoundException e)
            {
                // ignore objects that have not been found (perhaps because their type is unknown to CMIS)
            }
        }
    }

    @Override
    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get the node ref
        CMISNodeInfo info = getOrCreateFolderInfo(folderId, "Folder");

        // the root folder has no parent
        if (info.isRootFolder())
        {
            throw new CmisInvalidArgumentException("Root folder has no parent!");
        }

        // get the parent
        List<CMISNodeInfo> parentInfos = info.getParents();
        if (parentInfos.isEmpty())
        {
            throw new CmisRuntimeException("Folder has no parent and is not the root folder?!");
        }

        CMISNodeInfo parentInfo = addNodeInfo(parentInfos.get(0));

        ObjectData result = connector.createCMISObject(
                parentInfo, filter, false, IncludeRelationships.NONE,
                CMISConnector.RENDITION_NONE, false, false);
    	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
        if (isObjectInfoRequired)
        {
            getObjectInfo(
                    repositoryId,
                    parentInfo.getObjectId(),
                    IncludeRelationships.NONE);
        }

        return result;
    }

    @Override
    public List<ObjectParentData> getObjectParents(
            String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension)
    {
        long start = System.currentTimeMillis();

        checkRepositoryId(repositoryId);

        List<ObjectParentData> result = new ArrayList<ObjectParentData>();

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // relationships are not filed
        if (info.isRelationship())
        {
            throw new CmisConstraintException("Relationships are not fileable!");
        }

        if (info.isItem() || (info.isFolder() && !info.isRootFolder()))
        {
            List<CMISNodeInfo> parentInfos = info.getParents();
            if (!parentInfos.isEmpty())
            {
                for (CMISNodeInfo parent : parentInfos)
                {
                    CMISNodeInfo parentInfo = addNodeInfo(parent);

                    ObjectData object = connector.createCMISObject(
                            parentInfo, filter, includeAllowableActions,
                            includeRelationships, renditionFilter, false, false);
                    boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
                    if (isObjectInfoRequired)
                    {
                        getObjectInfo(repositoryId, object.getId(), includeRelationships);
                    }

                    ObjectParentDataImpl objectParent = new ObjectParentDataImpl();
                    objectParent.setObject(object);

                    // include relative path segment
                    if (includeRelativePathSegment)
                    {
                        objectParent.setRelativePathSegment(info.getName());
                    }

                    result.add(objectParent);
                }
            }
        }
        else if (info.isCurrentVersion() || info.isPWC())
        {
            List<CMISNodeInfo> parentInfos = info.getParents();
            for (CMISNodeInfo parentInfo : parentInfos)
            {
                addNodeInfo(parentInfo);

                ObjectData object = connector.createCMISObject(
                        parentInfo, filter, includeAllowableActions,
                        includeRelationships, renditionFilter, false, false);
            	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
                if (isObjectInfoRequired)
                {
                    getObjectInfo(repositoryId, object.getId(), includeRelationships);
                }

                ObjectParentDataImpl objectParent = new ObjectParentDataImpl();
                objectParent.setObject(object);

                // include relative path segment
                if (includeRelativePathSegment)
                {
                    objectParent.setRelativePathSegment(info.getName());
                }

                result.add(objectParent);
            }
        }

        logGetObjectsCall("getObjectParents", start, objectId, result.size(), filter, includeAllowableActions, includeRelationships,
                renditionFilter, includeRelativePathSegment, extension, null, null, null, null);

        return result;
    }

    @Override
    public ObjectList getCheckedOutDocs(
            String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension)
    {
        long start = System.currentTimeMillis();

        checkRepositoryId(repositoryId);

        // convert BigIntegers to int
        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        int skip = (skipCount == null || skipCount.intValue() < 0 ? 0 : skipCount.intValue());

        // prepare query
        SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
       
        if (folderId == null)
        {
            params.setQuery("+=cm:workingCopyOwner:\""+AuthenticationUtil.getFullyAuthenticatedUser()+"\"");
            params.addStore(connector.getRootStoreRef());
        }
        else
        {
            CMISNodeInfo folderInfo = getOrCreateFolderInfo(folderId, "Folder");

            params.setQuery("+=cm:workingCopyOwner:\""+AuthenticationUtil.getFullyAuthenticatedUser()+"\" AND +=PARENT:\""+folderInfo.getNodeRef().toString()+"\"");
            params.addStore(folderInfo.getNodeRef().getStoreRef());
        }

        // set up order
        if (orderBy != null)
        {
            String[] parts = orderBy.split(",");
            for (int i = 0; i < parts.length; i++)
            {
                String[] sort = parts[i].split(" +");

                if (sort.length < 1)
                {
                    continue;
                }

                PropertyDefinitionWrapper propDef = connector.getOpenCMISDictionaryService().findPropertyByQueryName(sort[0]);
                if (propDef != null)
                {
                    if (propDef.getPropertyDefinition().isOrderable())
                    {
                        QName sortProp = propDef.getPropertyAccessor().getMappedProperty();
                        if (sortProp != null)
                        {
                            boolean sortAsc = (sort.length == 1) || sort[1].equalsIgnoreCase("asc");
                            params.addSort(propDef.getPropertyLuceneBuilder().getLuceneFieldName(), sortAsc);
                        }
                        else
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Ignore sort property '" + sort[0] + " - mapping not found");
                            }
                        }
                    }
                    else
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Ignore sort property '" + sort[0] + " - not orderable");
                        }
                    }
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Ignore sort property '" + sort[0] + " - query name not found");
                    }
                }
            }
        }

        // execute query
        ResultSet resultSet = null;
        List<NodeRef> nodeRefs;
        try
        {
            resultSet = connector.getSearchService().query(params);
            nodeRefs = resultSet.getNodeRefs();
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }

        // collect results
        ObjectListImpl result = new ObjectListImpl();
        List<ObjectData> list = new ArrayList<ObjectData>();
        result.setObjects(list);

        int skipCounter = skip;
        if (max > 0)
        {
            for (NodeRef nodeRef : nodeRefs)
            {
                // TODO - perhaps filter by path in the query instead?
                if(connector.filter(nodeRef))
                {
                    continue;
                }

                if (skipCounter > 0)
                {
                    skipCounter--;
                    continue;
                }

                if (list.size() == max)
                {
                    break;
                }

                try
                {
                    // create a CMIS object
                    CMISNodeInfo ni = createNodeInfo(nodeRef);
                    ObjectData object = connector.createCMISObject(
                            ni, filter, includeAllowableActions,
                            includeRelationships, renditionFilter, false, false);

                	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
                    if (isObjectInfoRequired)
                    {
                        getObjectInfo(repositoryId, ni.getObjectId(), includeRelationships);
                    }

                    // add it
                    list.add(object);
                }
                catch (InvalidNodeRefException e)
                {
                    // ignore invalid objects
                }
                catch(CmisObjectNotFoundException e)
                {
                    // ignore objects that have not been found (perhaps because their type is unknown to CMIS)
                }
            }
        }

        // has more ?
        result.setHasMoreItems(nodeRefs.size() - skip > list.size());

        logGetObjectsCall("getCheckedOutDocs", start, folderId, list.size(), filter, includeAllowableActions, includeRelationships,
                renditionFilter, null, extension, skipCount, maxItems, orderBy, null);

        return result;
    }

    // --- object service ---

    @Override
    public String create(
            String repositoryId, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // check properties
        if (properties == null || properties.getProperties() == null)
        {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }

        // get the type
        String objectTypeId = connector.getObjectTypeIdProperty(properties);

        // find the type
        TypeDefinitionWrapper type = connector.getOpenCMISDictionaryService().findType(objectTypeId);
        if (type == null)
        {
            throw new CmisInvalidArgumentException("Type '" + objectTypeId + "' is unknown!");
        }

        // create object
        String newId = null;
        switch (type.getBaseTypeId())
        {
        case CMIS_DOCUMENT:
            versioningState = getDocumentDefaultVersioningState(versioningState, type);
            newId = createDocument(repositoryId, properties, folderId, contentStream, versioningState, policies, null,
                    null, extension);
            break;
        case CMIS_FOLDER:
            newId = createFolder(repositoryId, properties, folderId, policies, null, null, extension);
            break;
        case CMIS_POLICY:
            newId = createPolicy(repositoryId, properties, folderId, policies, null, null, extension);
            break;
        case CMIS_ITEM:
            newId = createItem(repositoryId, properties, folderId, policies, null, null, extension);
            break;
        default:
            break;

        }

        // check new object id
        if (newId == null)
        {
            throw new CmisRuntimeException("Creation failed!");
        }

    	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
        if (isObjectInfoRequired)
        {
            try
            {
                getObjectInfo(repositoryId, newId, "*", IncludeRelationships.NONE);
            }
            catch (InvalidNodeRefException e)
            {
                throw new CmisRuntimeException("Creation failed! New object not found!");
            }
        }

        // return the new object id
        return newId;
    }

    @Override
    public String createFolder(String repositoryId, final Properties properties, String folderId,
            final List<String> policies, final Acl addAces, final Acl removeAces, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get the parent folder node ref
        final CMISNodeInfo parentInfo = getOrCreateFolderInfo(folderId, "Folder");

        // get name and type
        final String name = connector.getNameProperty(properties, null);
        final String objectTypeId = connector.getObjectTypeIdProperty(properties);
        final TypeDefinitionWrapper type = connector.getTypeForCreate(objectTypeId, BaseTypeId.CMIS_FOLDER);

        connector.checkChildObjectType(parentInfo, type.getTypeId());

        // run transaction
        FileInfo fileInfo = connector.getFileFolderService().create(
                parentInfo.getNodeRef(), name, type.getAlfrescoClass());
        NodeRef nodeRef = fileInfo.getNodeRef();

        connector.setProperties(nodeRef, type, properties, new String[] { PropertyIds.NAME, PropertyIds.OBJECT_TYPE_ID });
        connector.applyPolicies(nodeRef, type, policies);
        connector.applyACL(nodeRef, type, addAces, removeAces);
        
        connector.getActivityPoster().postFileFolderAdded(nodeRef);

        String objectId = connector.createObjectId(nodeRef);
        return objectId;
    }
    

    @Override
    public String createItem(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) 
    {
    	checkRepositoryId(repositoryId);
    	
        // get the parent folder node ref
        final CMISNodeInfo parentInfo = getOrCreateFolderInfo(folderId, "Folder");

        // get name and type
        final String name = connector.getNameProperty(properties, null);
        final String objectTypeId = connector.getObjectTypeIdProperty(properties);
        final TypeDefinitionWrapper type = connector.getTypeForCreate(objectTypeId, BaseTypeId.CMIS_ITEM);

        connector.checkChildObjectType(parentInfo, type.getTypeId());
        
        /**
         * The above code specifies a folder - so the contents of a folder (as defined by the alfresco model) are 
         * ASSOC cm:contains to a TYPE sys:base
         */
        QName assocQName = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName(name));
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(11);
        props.put(ContentModel.PROP_NAME, (Serializable) assocQName.getLocalName());
        
        ChildAssociationRef newRef = connector.getNodeService().createNode(
        		parentInfo.getNodeRef(), 
        		ContentModel.ASSOC_CONTAINS, 
        		assocQName, 
        		type.getAlfrescoClass(),
        		props);
        
        NodeRef nodeRef = newRef.getChildRef();
        
        connector.setProperties(nodeRef, type, properties, new String[] { PropertyIds.NAME, PropertyIds.OBJECT_TYPE_ID });
        connector.getNodeService().setProperty(nodeRef, ContentModel.PROP_NAME, assocQName.getLocalName());

        connector.applyPolicies(nodeRef, type, policies);
        connector.applyACL(nodeRef, type, addAces, removeAces);

        String objectId = connector.createObjectId(nodeRef);
        return objectId;
    	
    }

    private String parseMimeType(ContentStream contentStream)
    {
    	String mimeType = null;

    	String tmp = contentStream.getMimeType();
    	if(tmp != null)
    	{
    		int idx = tmp.indexOf(";");
    		if(idx != -1)
    		{
    			mimeType = tmp.substring(0, idx).trim();
    		}
    		else
    		{
    			mimeType = tmp;
    		}
    	}

    	return mimeType;
    }

    @Override
    public String createDocument(
            String repositoryId, final Properties properties, String folderId,
            final ContentStream contentStream, VersioningState versioningState, final List<String> policies,
            final Acl addAces, final Acl removeAces, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);
        
        // get the parent folder node ref
        final CMISNodeInfo parentInfo = getOrCreateFolderInfo(folderId, "Parent folder");

        // get name and type
        final String name = connector.getNameProperty(properties, null);
        final String objectTypeId = connector.getObjectTypeIdProperty(properties);
        final TypeDefinitionWrapper type = connector.getTypeForCreate(objectTypeId, BaseTypeId.CMIS_DOCUMENT);
        
        connector.checkChildObjectType(parentInfo, type.getTypeId());

        DocumentTypeDefinition docType = (DocumentTypeDefinition) type.getTypeDefinition(false);

        if ((docType.getContentStreamAllowed() == ContentStreamAllowed.NOTALLOWED) && (contentStream != null))
        {
            throw new CmisConstraintException("This document type does not support content!");
        }

        if ((docType.getContentStreamAllowed() == ContentStreamAllowed.REQUIRED) && (contentStream == null))
        {
            throw new CmisConstraintException("This document type does requires content!");
        }

        if (!docType.isVersionable() && (versioningState != VersioningState.NONE))
        {
            throw new CmisConstraintException("This document type is not versionable!");
        }
        
        versioningState = getDocumentDefaultVersioningState(versioningState, type);

        FileInfo fileInfo = connector.getFileFolderService().create(
                parentInfo.getNodeRef(), name, type.getAlfrescoClass());
        NodeRef nodeRef = fileInfo.getNodeRef();
        connector.setProperties(nodeRef, type, properties, new String[] { PropertyIds.NAME, PropertyIds.OBJECT_TYPE_ID });
        connector.applyPolicies(nodeRef, type, policies);
        connector.applyACL(nodeRef, type, addAces, removeAces);

        // handle content
        if (contentStream != null)
        {
            // write content
            String mimeType = parseMimeType(contentStream);
            String encoding = getEncoding(contentStream.getStream(), mimeType);
            ContentWriter writer = connector.getFileFolderService().getWriter(nodeRef);
            writer.setMimetype(mimeType);
            writer.setEncoding(encoding);
            writer.putContent(contentStream.getStream());
        }

        connector.extractMetadata(nodeRef);

        // generate "doclib" thumbnail asynchronously
        connector.createThumbnails(nodeRef, Collections.singleton("doclib"));

        connector.applyVersioningState(nodeRef, versioningState);

        String objectId = connector.createObjectId(nodeRef);

        connector.getActivityPoster().postFileFolderAdded(nodeRef);

        return objectId;
    }
    


    /**
     * Gets default value of <code>VersioningState</code> based on whether document is versionable or not.
     * 
     * @param versioningState
     * @param type
     * @return <code>VersioningState.MAJOR</code> if versioningState is {@code null} and object is versionable
     *         <code>VersioningState.NONE</code> if versioningState is {@code null} and object is not versionable
     *         versioningState if it's value is not {@code null}
     */
    private VersioningState getDocumentDefaultVersioningState(VersioningState versioningState, TypeDefinitionWrapper type)
    {
        if (versioningState == null)
        {
            DocumentTypeDefinition docType = (DocumentTypeDefinition) type.getTypeDefinition(false);
            versioningState = docType.isVersionable() ? VersioningState.MAJOR : VersioningState.NONE;
        }
        return versioningState;
    }

    @Override
    public String createDocumentFromSource(
            String repositoryId, String sourceId, final Properties properties,
            String folderId, VersioningState versioningState, final List<String> policies, final Acl addAces,
            final Acl removeAces, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get the parent folder node ref
        final CMISNodeInfo parentInfo = getOrCreateFolderInfo(folderId, "Parent folder");

        // get source
        CMISNodeInfo info = getOrCreateNodeInfo(sourceId, "Source");

        // check source
        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            throw new CmisConstraintException("Source object is not a document!");
        }

        final NodeRef sourceNodeRef = info.getNodeRef();
        if (!info.isDocument())
        {
            throw new CmisConstraintException("Source object is not a document!");
        }
        
        // get name and type
        final String name = connector.getNameProperty(properties, info.getName());

        final TypeDefinitionWrapper type = info.getType();
        connector.checkChildObjectType(parentInfo, type.getTypeId());
        
        versioningState = getDocumentDefaultVersioningState(versioningState, type);

        try
        {
            FileInfo fileInfo = connector.getFileFolderService().copy(
                    sourceNodeRef, parentInfo.getNodeRef(), name);
            NodeRef nodeRef = fileInfo.getNodeRef();
            connector.setProperties(nodeRef, type, properties, new String[] {
                    PropertyIds.NAME, PropertyIds.OBJECT_TYPE_ID });
            connector.applyPolicies(nodeRef, type, policies);
            connector.applyACL(nodeRef, type, addAces, removeAces);
            
            connector.extractMetadata(nodeRef);
            connector.createThumbnails(nodeRef, Collections.singleton("doclib"));

            connector.applyVersioningState(nodeRef, versioningState);
            
            connector.getActivityPoster().postFileFolderAdded(nodeRef);
            
            return connector.createObjectId(nodeRef);
        }
        catch (FileNotFoundException e)
        {
            throw new CmisContentAlreadyExistsException("An object with this name already exists!", e);
        }
    }

    @Override
    public String createPolicy(
            String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get the parent folder
        getOrCreateFolderInfo(folderId, "Parent Folder");

        String objectTypeId = connector.getObjectTypeIdProperty(properties);
        connector.getTypeForCreate(objectTypeId, BaseTypeId.CMIS_POLICY);

        // we should never get here - policies are not creatable!
        throw new CmisRuntimeException("Polcies cannot be created!");
    }

    @Override
    public String createRelationship(
            String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get type
        String objectTypeId = connector.getObjectTypeIdProperty(properties);
        final TypeDefinitionWrapper type = connector.getTypeForCreate(objectTypeId, BaseTypeId.CMIS_RELATIONSHIP);

        // get source object
        String sourceId = connector.getSourceIdProperty(properties);
        CMISNodeInfo sourceInfo = getOrCreateNodeInfo(sourceId, "Source");

        if (!sourceInfo.isVariant(CMISObjectVariant.CURRENT_VERSION) && !sourceInfo.isVariant(CMISObjectVariant.FOLDER) && !sourceInfo.isVariant(CMISObjectVariant.ITEM))
        {
            throw new CmisInvalidArgumentException("Source is not the latest version of a document, a folder or an item object!");
        }

        final NodeRef sourceNodeRef = sourceInfo.getNodeRef();

        // get target object
        String targetId = connector.getTargetIdProperty(properties);
        CMISNodeInfo targetInfo = getOrCreateNodeInfo(targetId, "Target");

        if (!targetInfo.isVariant(CMISObjectVariant.CURRENT_VERSION) && !targetInfo.isVariant(CMISObjectVariant.FOLDER) && !targetInfo.isVariant(CMISObjectVariant.ITEM))
        {
            throw new CmisInvalidArgumentException(
                    "Target is not the latest version of a document, a folder or an item object!!");
        }

        final NodeRef targetNodeRef = targetInfo.getNodeRef();

        // check policies and ACLs
        if ((policies != null) && (!policies.isEmpty()))
        {
            throw new CmisConstraintException("Relationships are not policy controllable!");
        }

        if ((addAces != null) && (addAces.getAces() != null) && (!addAces.getAces().isEmpty()))
        {
            throw new CmisConstraintException("Relationships are not ACL controllable!");
        }

        if ((removeAces != null) && (removeAces.getAces() != null) && (!removeAces.getAces().isEmpty()))
        {
            throw new CmisConstraintException("Relationships are not ACL controllable!");
        }

        // create relationship
        // ALF-10085 : disable auditing behaviour for this use case
        boolean wasEnabled = connector.disableBehaviour(ContentModel.ASPECT_AUDITABLE, sourceNodeRef);        // Lasts for txn
        try
        {
            AssociationRef assocRef = connector.getNodeService().createAssociation(
                    sourceNodeRef, targetNodeRef, type.getAlfrescoClass());

            return CMISConnector.ASSOC_ID_PREFIX + assocRef.getId();
        }
        finally
        {
            if(wasEnabled)
            {
                connector.enableBehaviour(ContentModel.ASPECT_AUDITABLE, sourceNodeRef);
            }
        }
    }

    @Override
    public void appendContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ContentStream contentStream, boolean isLastChunk, ExtensionsData extension)
    {
        if ((contentStream == null) || (contentStream.getStream() == null))
        {
            throw new CmisInvalidArgumentException("No content!");
        }

        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");
        NodeRef nodeRef = info.getNodeRef();

        if (((DocumentTypeDefinition) info.getType().getTypeDefinition(false)).getContentStreamAllowed() == ContentStreamAllowed.NOTALLOWED)
        {
            throw new CmisStreamNotSupportedException("Document type doesn't allow content!");
        }

        //ALF-21852 - Separated appendContent and objectId.setValue in two different transactions because
        //after executing appendContent, the new objectId is not visible.
        RetryingTransactionHelper helper = connector.getRetryingTransactionHelper();
        helper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
           public Void execute() throws Throwable
           {
               try
               {
                   connector.appendContent(info, contentStream, isLastChunk);
               }
               catch(IOException e)
               {
                    throw new ContentIOException("", e);
               }
                return null;
           }
        }, false, true);

       String objId = helper.doInTransaction(new RetryingTransactionCallback<String>()
       {
           public String execute() throws Throwable
           {
               return connector.createObjectId(nodeRef);
           }
       }, true, true);

       objectId.setValue(objId);
    }

    @Override
    public void setContentStream(
            String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, final ContentStream contentStream, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        if (!info.isVariant(CMISObjectVariant.CURRENT_VERSION) && !info.isVariant(CMISObjectVariant.PWC))
        {
            throw new CmisStreamNotSupportedException("Content can only be set on private working copies or current versions.");
        }

        final NodeRef nodeRef = info.getNodeRef();

        if (((DocumentTypeDefinition) info.getType().getTypeDefinition(false)).getContentStreamAllowed() == ContentStreamAllowed.NOTALLOWED)
        {
            throw new CmisStreamNotSupportedException("Document type doesn't allow content!");
        }

        boolean existed = connector.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT) != null;
        if (existed && !overwriteFlag)
        {
            throw new CmisContentAlreadyExistsException("Content already exists!");
        }

        if ((contentStream == null) || (contentStream.getStream() == null))
        {
            throw new CmisInvalidArgumentException("No content!");
        }

        //ALF-21852 - Separated setContent and objectId.setValue in two different transactions because
        //after executing setContent, the new objectId is not visible.
        RetryingTransactionHelper helper = connector.getRetryingTransactionHelper();
        helper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                String mimeType = parseMimeType(contentStream);
                String encoding = getEncoding(contentStream.getStream(), mimeType);
                ContentWriter writer = connector.getFileFolderService().getWriter(nodeRef);
                writer.setMimetype(mimeType);
                writer.setEncoding(encoding);
                writer.putContent(contentStream.getStream());
                connector.getActivityPoster().postFileFolderUpdated(info.isFolder(), nodeRef);
                return null;
            }
        }, false, true);

        String objId = helper.doInTransaction(new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                return connector.createObjectId(nodeRef);
            }
        }, true, true);

        objectId.setValue(objId);
    }

    @Override
    public void deleteContentStream(
            String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        if (!info.isVariant(CMISObjectVariant.CURRENT_VERSION) && !info.isVariant(CMISObjectVariant.PWC))
        {
            throw new CmisStreamNotSupportedException("Content can only be deleted from ondocuments!");
        }

        final NodeRef nodeRef = info.getNodeRef();

        if (((DocumentTypeDefinition) info.getType().getTypeDefinition(false)).getContentStreamAllowed() == ContentStreamAllowed.REQUIRED)
        {
            throw new CmisInvalidArgumentException("Document type requires content!");
        }

        //ALF-21852 - Separated deleteContent and objectId.setValue in two different transactions because
        //after executing deleteContent, the new objectId is not visible.
        RetryingTransactionHelper helper = connector.getRetryingTransactionHelper();
        helper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {

                connector.getNodeService().setProperty(nodeRef, ContentModel.PROP_CONTENT, null);

//              connector.createVersion(nodeRef, VersionType.MINOR, "Delete content");

                connector.getActivityPoster().postFileFolderUpdated(info.isFolder(), nodeRef);
                return null;
            }
        }, false, true);

        String objId = helper.doInTransaction(new RetryingTransactionCallback<String>()
        {
            public String execute() throws Throwable
            {
                return connector.createObjectId(nodeRef);
            }
        }, true, true);

        objectId.setValue(objId);
    }

    @Override
    public void moveObject(
            String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get object and source and target parent
        CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        final NodeRef nodeRef = info.getCurrentNodeNodeRef();
        final CMISNodeInfo sourceInfo = getOrCreateFolderInfo(sourceFolderId, "Source Folder");
        final CMISNodeInfo targetInfo = getOrCreateFolderInfo(targetFolderId, "Target Folder");

        connector.checkChildObjectType(targetInfo, info.getType().getTypeId());

        ChildAssociationRef primaryParentRef = connector.getNodeService().getPrimaryParent(nodeRef);
        // if this is a primary child node, move it
        if (primaryParentRef.getParentRef().equals(sourceInfo.getNodeRef()))
        {
            connector.getNodeService().moveNode(
                    nodeRef, targetInfo.getNodeRef(),
                    primaryParentRef.getTypeQName(), primaryParentRef.getQName());
        }
        else
        {
            boolean found = false;
            // otherwise, reparent it
            for (ChildAssociationRef parent : connector.getNodeService().getParentAssocs(
                    nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
            {
                if (parent.getParentRef().equals(sourceInfo.getNodeRef()))
                {
                    connector.getNodeService().removeChildAssociation(parent);
                    connector.getNodeService().addChild(
                            targetInfo.getNodeRef(), nodeRef,
                            ContentModel.ASSOC_CONTAINS, parent.getQName());
                    found = true;
                }
            }
            if (!found)
            {
                throw new IllegalArgumentException(
                        new CmisInvalidArgumentException(
                                "Document is not a child of the source folder that was specified!"));
            }
        }
    }

    @Override
    public void updateProperties(
            String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            final Properties properties, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        final CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            throw new CmisInvalidArgumentException("Relationship properties cannot be updated!");
        }
        else
        {
            if (info.isVariant(CMISObjectVariant.VERSION))
            {
                throw new CmisInvalidArgumentException("Document is not the latest version!");
            }

            final NodeRef nodeRef = info.getNodeRef();

            connector.setProperties(nodeRef, info.getType(), properties, new String[0]);
            
            objectId.setValue(connector.createObjectId(nodeRef));

        	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
            if (isObjectInfoRequired)
            {
                getObjectInfo(repositoryId, objectId.getValue(), "*", IncludeRelationships.NONE);
            }

            connector.getActivityPoster().postFileFolderUpdated(info.isFolder(), nodeRef);
        }
    }

    @Override
    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension)
    {
        deleteObjectOrCancelCheckOut(repositoryId, objectId, allVersions, extension);
    }

    @Override
    public void deleteObjectOrCancelCheckOut(
            String repositoryId, final String objectId, final Boolean allVersions,
            ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        final CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        do
        {
            // handle relationships
            if (info.isVariant(CMISObjectVariant.ASSOC))
            {
                AssociationRef assocRef = info.getAssociationRef();
                connector.getNodeService().removeAssociation(
                        assocRef.getSourceRef(),
                        assocRef.getTargetRef(),
                        assocRef.getTypeQName());
                break;      // Reason for do-while
            }

            NodeRef nodeRef = info.getNodeRef();

            // handle PWC
            if (info.isVariant(CMISObjectVariant.PWC))
            {
                connector.getCheckOutCheckInService().cancelCheckout(nodeRef);
                break;      // Reason for do-while
            }

            // handle folders
            if (info.isFolder())
            {
                // Check if there is at least one child
                if (connector.getNodeService().getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL, 1, false).size() > 0)
                {
                    throw new CmisConstraintException(
                            "Could not delete folder with at least one child!");
                }

                connector.deleteNode(nodeRef, true);
                break;      // Reason for do-while
            }

            if(info.hasPWC())
            {
                // is a checked out document. If a delete, don't allow unless checkout is canceled. If a cancel
                // checkout, not allowed.
                throw new CmisConstraintException(
                "Could not delete/cancel checkout on the original checked out document");
            }

            // handle versions
            if (info.isVariant(CMISObjectVariant.VERSION))
            {
                nodeRef = info.getCurrentNodeNodeRef();
            }

            if (allVersions)
            {
                NodeRef workingCopy = connector.getCheckOutCheckInService().getWorkingCopy(nodeRef);
                if (workingCopy != null)
                {
                    connector.getCheckOutCheckInService().cancelCheckout(workingCopy);
                }
            }
            else if (info.isVariant(CMISObjectVariant.VERSION))
            {
                // Check the DELETE permission since the version service has no restrictions.
                AccessStatus perm = connector.getServiceRegistry().getPermissionService().hasPermission(nodeRef, PermissionService.DELETE);
                if (AccessStatus.ALLOWED != perm)
                {
                    throw new CmisPermissionDeniedException("Cannot delete the node version.");
                }
                Version version = ((CMISNodeInfoImpl) info).getVersion();
                connector.getVersionService().deleteVersion(nodeRef, version);
                break;      // Reason for do-while
            }

            // attempt to delete the node
            if (allVersions)
            {
                connector.deleteNode(nodeRef, true);
            }
            else
            {
                CMISNodeInfoImpl infoImpl = (CMISNodeInfoImpl) info;
                Version version = infoImpl.getVersion();

                if (infoImpl.getVersionHistory().getPredecessor(version) == null)
                {
                    connector.deleteNode(nodeRef, true);
                }
                else
                {
                    AccessStatus perm = connector.getServiceRegistry().getPermissionService().hasPermission(nodeRef, PermissionService.DELETE);
                    if (AccessStatus.ALLOWED != perm)
                    {
                        throw new CmisPermissionDeniedException("Cannot delete the node version.");
                    }
                    connector.getVersionService().deleteVersion(nodeRef, version);
                    // MNT-10032 revert node version to predecessor
                    connector.getVersionService().revert(nodeRef);
                }
            }
        }
        while (false);      // Dodgey, but avoided having to play with too much code during refactor
    }

    @Override
    public FailedToDeleteData deleteTree(
            String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, final Boolean continueOnFailure, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        if (!allVersions)
        {
            throw new CmisInvalidArgumentException("Only allVersions=true supported!");
        }

        if (unfileObjects == UnfileObject.UNFILE)
        {
            throw new CmisInvalidArgumentException("Unfiling not supported!");
        }

        final NodeRef folderNodeRef = getOrCreateFolderInfo(folderId, "Folder").getNodeRef();
        final FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();

        try
        {
            connector.deleteNode(folderNodeRef, true);
        }
        catch (Exception e)
        {
            List<String> ids = new ArrayList<String>();
            ids.add(folderId);
            result.setIds(ids);
        }

        return result;
    }

    @Override
    public ObjectData getObject(
            String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension)
    {
    	long start = System.currentTimeMillis();
    	
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // create a CMIS object
        ObjectData object = connector.createCMISObject(
                info, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePolicyIds, includeAcl);

    	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
        if (isObjectInfoRequired)
        {
            getObjectInfo(repositoryId, info.getObjectId(), includeRelationships);
        }

        logGetObjectCall("getObject", start, objectId, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePolicyIds, includeAcl, isObjectInfoRequired, extension);

        return object;
    }
    
    @Override
    public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(final String repositoryId,
            List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeTokens, final Properties properties,
            final List<String> addSecondaryTypeIds, final List<String> removeSecondaryTypeIds, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);
        
        if(objectIdAndChangeTokens.size() > connector.getBulkMaxItems())
        {
            throw new CmisConstraintException("Bulk update not supported for more than " + connector.getBulkMaxItems() + " objects.");
        }
        // MNT-16376 We need the CMIS call context from this thread to set 
        // to the working threads below, in order to correctly identify 
        // the CMIS version protocol used for this CMIS call
        final CallContext cmisCallContext = AlfrescoCmisServiceCall.get();

        // WorkProvider
        class WorkProvider implements BatchProcessWorkProvider<BulkEntry>
        {
            private final Iterator<BulkUpdateObjectIdAndChangeToken> iterator;
            private final int size;
            private final int batchSize;
            private BulkUpdateContext context;

            public WorkProvider(List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeTokens, BulkUpdateContext context, int batchSize)
            {
                this.iterator = objectIdAndChangeTokens.iterator();
                this.size = objectIdAndChangeTokens.size();
                this.context = context;
                this.batchSize = batchSize;
            }

            @Override
            public synchronized int getTotalEstimatedWorkSize()
            {
                return size;
            }

            @Override
            public synchronized Collection<BulkEntry> getNextWork()
            {
                Collection<BulkEntry> results = new ArrayList<BulkEntry>(batchSize);
                while (results.size() < batchSize && iterator.hasNext())
                {
                    results.add(new BulkEntry(context, iterator.next(), properties, addSecondaryTypeIds, removeSecondaryTypeIds, getContext().isObjectInfoRequired()));
                }
                return results;
            }
        }
        
        BulkUpdateContext context = new BulkUpdateContext(objectIdAndChangeTokens.size());
        RetryingTransactionHelper helper = connector.getRetryingTransactionHelper();
        final String runAsUser = AuthenticationUtil.getRunAsUser();

        // Worker
        BatchProcessWorker<BulkEntry> worker = new BatchProcessWorker<BulkEntry>()
        {
            @Override
            public void process(final BulkEntry entry) throws Throwable
            {
                entry.update();
            }

            public String getIdentifier(BulkEntry entry)
            {
                return entry.getObjectIdAndChangeToken().getId();
            }

            @Override
            public void beforeProcess() throws Throwable
            {
                // Authentication
                AuthenticationUtil.pushAuthentication();
                AuthenticationUtil.setFullyAuthenticatedUser(runAsUser);
                AlfrescoCmisServiceCall.set(cmisCallContext);
            }

            @Override
            public void afterProcess() throws Throwable
            {
                // Clear authentication
                AuthenticationUtil.popAuthentication();
            }
        };

        // Processor
        BatchProcessor<BulkEntry> processor = new BatchProcessor<BulkEntry>(
                "CMISbulkUpdateProperties",
                helper,
                new WorkProvider(objectIdAndChangeTokens, context, connector.getBulkBatchSize()),
                connector.getBulkWorkerThreads(), connector.getBulkBatchSize(),
                null,
                logger, 100);
        processor.process(worker, true);
        
        for(CMISNodeInfo info : context.getSuccesses())
        {
            NodeRef nodeRef = info.getNodeRef();
            connector.getActivityPoster().postFileFolderUpdated(info.isFolder(), nodeRef);
        }

        return context.getChanges();
    }
    
    private class BulkEntry
    {
        private String repositoryId;
        private BulkUpdateContext bulkUpdateContext;

        private BulkUpdateObjectIdAndChangeToken objectIdAndChangeToken;
        private Properties properties;
        private List<String> addSecondaryTypeIds;
        private List<String> removeSecondaryTypeIds;
        private boolean isObjectInfoRequired;

        BulkEntry(BulkUpdateContext bulkUpdateContext, BulkUpdateObjectIdAndChangeToken objectIdAndChangeToken, Properties properties, List<String> addSecondaryTypeIds,
                List<String> removeSecondaryTypeIds, boolean isObjectInfoRequired)
        {
            this.bulkUpdateContext = bulkUpdateContext;
            this.objectIdAndChangeToken = objectIdAndChangeToken;
            this.properties = properties;
            this.addSecondaryTypeIds = addSecondaryTypeIds;
            this.removeSecondaryTypeIds = removeSecondaryTypeIds;
            this.isObjectInfoRequired = isObjectInfoRequired;
        }

        public void update()
        {
            String objectId = objectIdAndChangeToken.getId();
            final CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

            if (!info.isVariant(CMISObjectVariant.ASSOC) && !info.isVariant(CMISObjectVariant.VERSION))
            {
                final NodeRef nodeRef = info.getNodeRef();

                connector.setProperties(nodeRef, info.getType(), properties, new String[0]);

                if (isObjectInfoRequired)
                {
                    getObjectInfo(repositoryId, objectId, "*", IncludeRelationships.NONE);
                }

                connector.addSecondaryTypes(nodeRef, addSecondaryTypeIds);
                connector.removeSecondaryTypes(nodeRef, removeSecondaryTypeIds);

                if (properties.getProperties().size() > 0 || addSecondaryTypeIds.size() > 0 || removeSecondaryTypeIds.size() > 0)
                {
                    bulkUpdateContext.success(info);
                }
            }
        };
        
        public BulkUpdateObjectIdAndChangeToken getObjectIdAndChangeToken()
        {
            return objectIdAndChangeToken;
        }
    };

    private static class BulkUpdateContext
    {
        private Set<CMISNodeInfo> successes;
        
        BulkUpdateContext(int size)
        {
            this.successes = Collections.newSetFromMap(new ConcurrentHashMap<CMISNodeInfo, Boolean>());
        }
        
        void success(CMISNodeInfo info)
        {
            successes.add(info);
        }
        
        Set<CMISNodeInfo> getSuccesses()
        {
            return successes;
        }

        List<BulkUpdateObjectIdAndChangeToken> getChanges()
        {
            List<BulkUpdateObjectIdAndChangeToken> changes = new ArrayList<BulkUpdateObjectIdAndChangeToken>(successes.size());
            for(CMISNodeInfo info : successes)
            {
                BulkUpdateObjectIdAndChangeTokenImpl a = new BulkUpdateObjectIdAndChangeTokenImpl();
                a.setId(info.getObjectId());
//              a.setNewId(info.getObjectId());
                changes.add(a);
            }

            return changes;
        }
    }

    @Override
    public ObjectData getObjectByPath(
            String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension)
    {
        long start = System.currentTimeMillis();

        boolean isObjectInfoRequired = false;

        checkRepositoryId(repositoryId);

        // start at the root node
        NodeRef rootNodeRef = connector.getRootNodeRef();

        ObjectData object;

        if (path.equals("/"))
        {
            object = connector.createCMISObject(createNodeInfo(rootNodeRef), filter, includeAllowableActions,
                                                includeRelationships, renditionFilter, includePolicyIds, includeAcl);
        }
        else
        {
            try
            {
                // resolve path and get the node ref
                FileInfo fileInfo = connector.getFileFolderService().resolveNamePath(
                        rootNodeRef,
                        Arrays.asList(path.substring(1).split("/")));

                if (connector.filter(fileInfo.getNodeRef()))
                {
                    throw new CmisObjectNotFoundException("Object not found: " + path);
                }

                CMISNodeInfo info = createNodeInfo(fileInfo.getNodeRef(), fileInfo.getType(), fileInfo.getProperties(), null, false);

                object = connector.createCMISObject(info, filter, includeAllowableActions,
                        includeRelationships, renditionFilter, includePolicyIds, includeAcl);

                isObjectInfoRequired = getContext().isObjectInfoRequired();
                if (isObjectInfoRequired)
                {
                    getObjectInfo(repositoryId, info.getObjectId(), includeRelationships);
                }

            }
            catch (FileNotFoundException e)
            {
                throw new CmisObjectNotFoundException("Object not found: " + path);
            }
        }

        logGetObjectCall("getObjectByPath", start, path, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePolicyIds, includeAcl, isObjectInfoRequired, extension);

        return object;
    }

    @Override
    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

    	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
        if (isObjectInfoRequired)
        {
            getObjectInfo(repositoryId, info.getObjectId(), IncludeRelationships.NONE);
        }

        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            return connector.getAssocProperties(info, filter);
        }
        else
        {
            return connector.getNodeProperties(info, filter);
        }
    }

    @Override
    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        return connector.getAllowableActions(info);
    }

    @Override
    public ContentStream getContentStream(
            String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // relationships cannot have content
        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            throw new CmisInvalidArgumentException("Object is a relationship and cannot have content!");
        }

        // now get it
        return connector.getContentStream(info, streamId, offset, length);
    }

    @Override
    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            return Collections.emptyList();
        }
        else
        {
            return connector.getRenditions(info.getNodeRef(), renditionFilter, maxItems, skipCount);
        }
    }

    // --- versioning service ---

    @Override
    public void checkOut(
            String repositoryId, final Holder<String> objectId, ExtensionsData extension,
            final Holder<Boolean> contentCopied)
    {
        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        // Check for current version
        if (info.isVariant(CMISObjectVariant.CURRENT_VERSION))
        {
            // Good
        }
        else if (info.isVariant(CMISObjectVariant.VERSION))
        {
            throw new CmisInvalidArgumentException("Can't check out an old version of a document");
        }
        else {   
            throw new CmisInvalidArgumentException("Only documents can be checked out! Object was a " + info.getObjectVariant().toString());
        }

        // get object
        final NodeRef nodeRef = info.getNodeRef();

        if (!((DocumentTypeDefinition) info.getType().getTypeDefinition(false)).isVersionable())
        {
            throw new CmisConstraintException("Document is not versionable!");
        }
        
        // check out
        NodeRef pwcNodeRef = connector.getCheckOutCheckInService().checkout(nodeRef);
        CMISNodeInfo pwcNodeInfo = createNodeInfo(pwcNodeRef);
        objectId.setValue(pwcNodeInfo.getObjectId());

        if (contentCopied != null)
        {
            contentCopied.setValue(connector.getFileFolderService().getReader(pwcNodeRef) != null);
        }
    }

    @Override
    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");
        
        // only accept a PWC
        if (!info.isVariant(CMISObjectVariant.PWC))
        {
            NodeRef nodeRef = info.getNodeRef();
            NodeRef workingCopyNodeRef = connector.getCheckOutCheckInService().getWorkingCopy(nodeRef);
            info = getOrCreateNodeInfo(workingCopyNodeRef.getId());
            if (!info.isVariant(CMISObjectVariant.PWC))
            {
                throw new CmisVersioningException("Object is not a PWC!");
            }
        }

        // get object
        final NodeRef nodeRef = info.getNodeRef();

        // cancel check out
        connector.getCheckOutCheckInService().cancelCheckout(nodeRef);
    }

    @Override
    public void checkIn(
            String repositoryId, final Holder<String> objectId, final Boolean major,
            final Properties properties, final ContentStream contentStream, final String checkinComment,
            final List<String> policies, final Acl addAces, final Acl removeAces, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        // only accept a PWC
        if (!info.isVariant(CMISObjectVariant.PWC))
        {
            throw new CmisVersioningException("Object is not a PWC!");
        }

        // get object
        final NodeRef nodeRef = info.getNodeRef();
        final TypeDefinitionWrapper type = info.getType();

        // check in
        // update PWC
        connector.setProperties(nodeRef, type, properties,
                new String[] { PropertyIds.OBJECT_TYPE_ID });
        connector.applyPolicies(nodeRef, type, policies);
        connector.applyACL(nodeRef, type, addAces, removeAces);

        // handle content
        if (contentStream != null)
        {
            String mimeType = parseMimeType(contentStream);
            String encoding = getEncoding(contentStream.getStream(), mimeType);
            // write content
            ContentWriter writer = connector.getFileFolderService().getWriter(nodeRef);
            writer.setMimetype(mimeType);
            writer.setEncoding(encoding);
            writer.putContent(contentStream.getStream());
        }

        // create version properties
        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(5);
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, major ? VersionType.MAJOR
                : VersionType.MINOR);
        if (checkinComment != null)
        {
            versionProperties.put(VersionModel.PROP_DESCRIPTION, checkinComment);
        }

        // check in
        NodeRef newNodeRef = connector.getCheckOutCheckInService().checkin(nodeRef, versionProperties);

        connector.getActivityPoster().postFileFolderUpdated(info.isFolder(), newNodeRef);

        objectId.setValue(connector.createObjectId(newNodeRef));
    }

    @Override
    public List<ObjectData> getAllVersions(
            String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        if (versionSeriesId == null && objectId != null)
        {
            // it's a browser binding call
            versionSeriesId = connector.getCurrentVersionId(objectId);
        }

        if (versionSeriesId == null)
        {
            throw new CmisInvalidArgumentException("Object Id or Object Series Id must be set!");
        }

        List<ObjectData> result = new ArrayList<ObjectData>();

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(versionSeriesId, "Version Series");

        if (!info.isVariant(CMISObjectVariant.CURRENT_VERSION))
        {
            // the version series id is the id of current version, which is a
            // document
            throw new CmisInvalidArgumentException("Version Series does not exist!");
        }

        // get current version and it's history
        NodeRef nodeRef = info.getNodeRef();
        VersionHistory versionHistory = ((CMISNodeInfoImpl) info).getVersionHistory();

        if (versionHistory == null)
        {
            // add current version
            result.add(connector.createCMISObject(info, filter, includeAllowableActions, IncludeRelationships.NONE,
                    CMISConnector.RENDITION_NONE, false, false));

        	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
            if (isObjectInfoRequired)
            {
                getObjectInfo(repositoryId, info.getObjectId(), IncludeRelationships.NONE);
            }
        }
        else
        {
            if (info.hasPWC())
            {
                CMISNodeInfo pwcInfo = createNodeInfo(connector.getCheckOutCheckInService().getWorkingCopy(nodeRef));

                result.add(
                        connector.createCMISObject(
                                pwcInfo, filter, includeAllowableActions,
                                IncludeRelationships.NONE, CMISConnector.RENDITION_NONE, false, false));

            	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
                if (isObjectInfoRequired)
                {
                    getObjectInfo(repositoryId, pwcInfo.getObjectId(), IncludeRelationships.NONE);
                }
            }

            // convert the version history
            for (Version version : versionHistory.getAllVersions())
            {
                CMISNodeInfo versionInfo = createNodeInfo(version.getFrozenStateNodeRef(), versionHistory, true); // TODO do we need to check existence ?
                // MNT-9557 fix. Replace head version with current node info
                if (versionHistory.getHeadVersion().equals(version))
                {
                    versionInfo = createNodeInfo(nodeRef);
                }

                result.add(
                        connector.createCMISObject(
                                versionInfo, filter, includeAllowableActions,
                                IncludeRelationships.NONE, CMISConnector.RENDITION_NONE, false, false));

            	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
                if (isObjectInfoRequired)
                {
                    getObjectInfo(repositoryId, versionInfo.getObjectId(), IncludeRelationships.NONE);
                }
            }
        }

        return result;
    }

    @Override
    public ObjectData getObjectOfLatestVersion(
            String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension)
    {
        long start = System.currentTimeMillis();

        checkRepositoryId(repositoryId);

        if (objectId != null)
        {
            // it's an AtomPub call
            versionSeriesId = connector.getCurrentVersionId(objectId);
        }

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(versionSeriesId, "Version Series");
        CMISNodeInfo versionInfo = createNodeInfo(((CMISNodeInfoImpl) info).getLatestVersionNodeRef(major));

        ObjectData object = connector.createCMISObject(
                versionInfo, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePolicyIds, includeAcl);

    	boolean isObjectInfoRequired = getContext().isObjectInfoRequired();
        if (isObjectInfoRequired)
        {
            getObjectInfo(repositoryId, info.getObjectId(), includeRelationships);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(objectId).append("-").append(versionSeriesId);

        logGetObjectCall("getObjectOfLatestVersion", start, sb.toString(), filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePolicyIds, includeAcl, isObjectInfoRequired, extension);

        return object;
    }

    @Override
    public Properties getPropertiesOfLatestVersion(
            String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        if (objectId != null)
        {
            // it's an AtomPub call
            versionSeriesId = connector.getCurrentVersionId(objectId);
        }

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(versionSeriesId, "Version Series");

        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            return connector.getAssocProperties(info, filter);
        }
        else
        {
            CMISNodeInfo versionInfo = createNodeInfo(((CMISNodeInfoImpl) info).getLatestVersionNodeRef(major));
            addNodeInfo(versionInfo);
            return connector.getNodeProperties(versionInfo, filter);
        }
    }

    // --- multifiling service ---

    @Override
    public void addObjectToFolder(
            String repositoryId, String objectId, String folderId, Boolean allVersions,
            ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        if (!allVersions)
        {
            throw new CmisInvalidArgumentException("Only allVersions=true supported!");
        }

        // get node ref
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        if (!info.isDocument())
        {
            throw new CmisInvalidArgumentException("Object is not a document!");
        }

        final NodeRef nodeRef = info.getNodeRef();

        // get the folder node ref
        final CMISNodeInfo folderInfo = getOrCreateFolderInfo(folderId, "Folder");

        connector.checkChildObjectType(folderInfo, info.getType().getTypeId());

        final QName name = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName((String) connector.getNodeService().getProperty(nodeRef,
                        ContentModel.PROP_NAME)));

        connector.getNodeService().addChild(
                folderInfo.getNodeRef(), nodeRef, ContentModel.ASSOC_CONTAINS, name);
    }

    @Override
    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get node ref
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        if (!info.isDocument())
        {
            throw new CmisInvalidArgumentException("Object is not a document!");
        }

        final NodeRef nodeRef = info.getNodeRef();

        // get the folder node ref
        final NodeRef folderNodeRef = getOrCreateFolderInfo(folderId, "Folder").getNodeRef();

        // check primary parent
        if (connector.getNodeService().getPrimaryParent(nodeRef).getParentRef().equals(folderNodeRef))
        {
            throw new CmisConstraintException(
                    "Unfiling from primary parent folder is not supported! Use deleteObject() instead.");
        }

        connector.getNodeService().removeChild(folderNodeRef, nodeRef);
    }

    // --- discovery service ---

    @Override
    public ObjectList getContentChanges(
            String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        return connector.getContentChanges(changeLogToken, maxItems);
    }

    @Override
    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        if (searchAllVersions.booleanValue())
        {
            throw new CmisInvalidArgumentException("Search all version is not supported!");
        }

        return connector.query(
                statement, includeAllowableActions, includeRelationships, renditionFilter,
                maxItems, skipCount);
    }

    // --- relationship service ---

    @Override
    public ObjectList getObjectRelationships(
            String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            throw new CmisInvalidArgumentException("Object is a relationship!");
        }

        if (info.isVariant(CMISObjectVariant.VERSION))
        {
            throw new CmisInvalidArgumentException("Object is a document version!");
        }

        // check if the relationship base type is requested
        if (BaseTypeId.CMIS_RELATIONSHIP.value().equals(typeId))
        {
            boolean isrt = (includeSubRelationshipTypes == null ? false : includeSubRelationshipTypes.booleanValue());
            if (isrt)
            {
                // all relationships are a direct subtype of the base type in
                // Alfresco -> remove filter
                typeId = null;
            }
            else
            {
                // there are no relationships of the base type in Alfresco ->
                // return empty list
                ObjectListImpl result = new ObjectListImpl();
                result.setHasMoreItems(false);
                result.setNumItems(BigInteger.ZERO);
                result.setObjects(new ArrayList<ObjectData>());
                return result;
            }
        }

        return connector.getObjectRelationships(
                info.getNodeRef(), relationshipDirection, typeId, filter, includeAllowableActions,
                maxItems, skipCount);
    }

    // --- policy service ---

    @Override
    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        TypeDefinitionWrapper type = info.getType();
        if (type == null)
        {
            throw new CmisObjectNotFoundException("No corresponding type found! Not a CMIS object?");
        }

        connector.applyPolicies(info.getNodeRef(), type, Collections.singletonList(policyId));
    }

    @Override
    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        TypeDefinitionWrapper type = info.getType();
        if (type == null)
        {
            throw new CmisObjectNotFoundException("No corresponding type found! Not a CMIS object?");
        }

        throw new CmisConstraintException("Object is not policy controllable!");
    }

    @Override
    public List<ObjectData> getAppliedPolicies(
            String repositoryId, String objectId, String filter, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        getOrCreateNodeInfo(objectId, "Object");

        // policies are not supported -> return empty list
        return Collections.emptyList();
    }

    // --- ACL service ---

    @Override
    public Acl applyAcl(
            String repositoryId, String objectId, final Acl addAces, final Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // We are spec compliant if we just let it through and the tck will not fail

        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // relationships don't have ACLs
        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            throw new CmisConstraintException("Relationships are not ACL controllable!");
        }

        final NodeRef nodeRef = info.getCurrentNodeNodeRef();
        final TypeDefinitionWrapper type = info.getType();

        connector.applyACL(nodeRef, type, addAces, removeAces);

        return connector.getACL(nodeRef, false);
    }

    @Override
    public Acl applyAcl(String repositoryId, String objectId, final Acl aces, AclPropagation aclPropagation)
    {
        checkRepositoryId(repositoryId);

        // We are spec compliant if we just let it through and the tck will not fail

        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // relationships don't have ACLs
        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            throw new CmisConstraintException("Relationships are not ACL controllable!");
        }

        final NodeRef nodeRef = info.getCurrentNodeNodeRef();
        final TypeDefinitionWrapper type = info.getType();

        connector.applyACL(nodeRef, type, aces);

        return connector.getACL(nodeRef, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // relationships don't have ACLs
        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            return new AccessControlListImpl(Collections.EMPTY_LIST);
        }

        // get the ACL
        return connector.getACL(info.getCurrentNodeNodeRef(), onlyBasicPermissions);
    }

    // --------------------------------------------------------

    /**
     * Collects the {@link ObjectInfo} about an object.
     * 
     * (Provided by OpenCMIS, but optimized for Alfresco.)
     */
    @Override
    public ObjectInfo getObjectInfo(String repositoryId, String objectId)
    {
        return getObjectInfo(repositoryId, objectId, null, IncludeRelationships.BOTH);
    }

    protected ObjectInfo getObjectInfo(String repositoryId, String objectId, IncludeRelationships includeRelationships)
    {
        return getObjectInfo(repositoryId, objectId, null, includeRelationships);
    }

    protected ObjectInfo getObjectInfo(
            String repositoryId, String objectId, String filter,
            IncludeRelationships includeRelationships)
    {
        ObjectInfo info = objectInfoMap.get(objectId);
        if (info == null)
        {
            CMISNodeInfo nodeInfo = getOrCreateNodeInfo(objectId);

            if (nodeInfo.getObjectVariant() == CMISObjectVariant.INVALID_ID
                    || nodeInfo.getObjectVariant() == CMISObjectVariant.NOT_EXISTING
                    || nodeInfo.getObjectVariant() == CMISObjectVariant.NOT_A_CMIS_OBJECT
                    || nodeInfo.getObjectVariant() == CMISObjectVariant.PERMISSION_DENIED)
            {
                info = null;
            } else
            {
                // object info has not been found -> create one
                try
                {
                    if (filter == null)
                    {
                        filter = MIN_FILTER;
                    }
                    else if (!filter.equals("*"))
                    {
                        filter = filter + "," + MIN_FILTER;
                    }

                    // get the object and its info
                    ObjectData object = connector.createCMISObject(
                            nodeInfo, filter, false, includeRelationships, null, false, false);

                    info = getObjectInfoIntern(repositoryId, object);

                    // add object info
                    objectInfoMap.put(objectId, info);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    info = null;
                }
            }
        }
        return info;
    }

    public String getRequestParameterRenditionFilter()
    {
        QueryStringHttpServletRequestWrapper httpServletRequest = getHttpServletRequest();
        String value = httpServletRequest == null ? null : httpServletRequest.getParameter("renditionFilter");
        return value == null ? CMISConnector.RENDITION_NONE : value;
    }

    public IncludeRelationships getRequestParameterIncludeRelationships()
    {
        QueryStringHttpServletRequestWrapper httpServletRequest = getHttpServletRequest();
        String value = httpServletRequest == null ? null : httpServletRequest.getParameter("includeRelationships");
        return value == null ? IncludeRelationships.NONE : CmisEnumHelper.fromValue(value, IncludeRelationships.class);
    }

    private QueryStringHttpServletRequestWrapper getHttpServletRequest()
    {
        CallContext context = getContext();
        return (QueryStringHttpServletRequestWrapper)context.get(CallContext.HTTP_SERVLET_REQUEST);
    }

    /**
     * Collects the {@link ObjectInfo} about an object.
     * 
     * (Provided by OpenCMIS, but optimized for Alfresco.)
     */
    @SuppressWarnings("unchecked")
	@Override
    protected ObjectInfo getObjectInfoIntern(String repositoryId, ObjectData object)
    {
        // if the object has no properties, stop here
        if (object.getProperties() == null || object.getProperties().getProperties() == null)
        {
            throw new CmisRuntimeException("No properties!");
        }

        String objectId = object.getId();
        CMISNodeInfo ni = getOrCreateNodeInfo(objectId);

        ObjectInfoImpl info = new ObjectInfoImpl();

        // general properties
        info.setObject(object);
        info.setId(objectId);
        info.setName(getStringProperty(object, PropertyIds.NAME));
        info.setCreatedBy(getStringProperty(object, PropertyIds.CREATED_BY));
        info.setCreationDate(getDateTimeProperty(object, PropertyIds.CREATION_DATE));
        info.setLastModificationDate(getDateTimeProperty(object, PropertyIds.LAST_MODIFICATION_DATE));
        info.setTypeId(getIdProperty(object, PropertyIds.OBJECT_TYPE_ID));
        info.setBaseType(object.getBaseTypeId());

        if (ni.isRelationship())
        {
            // versioning
            info.setWorkingCopyId(null);
            info.setWorkingCopyOriginalId(null);

            info.setVersionSeriesId(null);
            info.setIsCurrentVersion(true);
            info.setWorkingCopyId(null);
            info.setWorkingCopyOriginalId(null);

            // content
            info.setHasContent(false);
            info.setContentType(null);
            info.setFileName(null);

            // parent
            info.setHasParent(false);

            // policies and relationships
            info.setSupportsRelationships(false);
            info.setSupportsPolicies(false);

            // renditions
            info.setRenditionInfos(null);

            // relationships
            info.setRelationshipSourceIds(null);
            info.setRelationshipTargetIds(null);

            // global settings
            info.setHasAcl(false);
            info.setSupportsDescendants(false);
            info.setSupportsFolderTree(false);
        }
        else if (ni.isFolder())
        {
            // versioning
            info.setWorkingCopyId(null);
            info.setWorkingCopyOriginalId(null);

            info.setVersionSeriesId(null);
            info.setIsCurrentVersion(true);
            info.setWorkingCopyId(null);
            info.setWorkingCopyOriginalId(null);

            // content
            info.setHasContent(false);
            info.setContentType(null);
            info.setFileName(null);

            // parent
            info.setHasParent(!ni.isRootFolder());

            // policies and relationships
            info.setSupportsRelationships(true);
            info.setSupportsPolicies(true);

            // renditions
            info.setRenditionInfos(null);

            // relationships
            setRelaionshipsToObjectInfo(object, info);

            // global settings
            info.setHasAcl(true);
            info.setSupportsDescendants(true);
            info.setSupportsFolderTree(true);
        }
        else if (ni.isDocument())
        {
            // versioning
            info.setWorkingCopyId(null);
            info.setWorkingCopyOriginalId(null);

            info.setVersionSeriesId(ni.getCurrentNodeId());

            if (ni.isPWC())
            {
                info.setIsCurrentVersion(false);
                info.setWorkingCopyId(ni.getObjectId());
                info.setWorkingCopyOriginalId(ni.getCurrentObjectId());
            }
            else
            {
                info.setIsCurrentVersion(ni.isCurrentVersion());

                if (ni.hasPWC())
                {
                    info.setWorkingCopyId(ni.getCurrentNodeId() + CMISConnector.ID_SEPERATOR
                            + CMISConnector.PWC_VERSION_LABEL);
                    info.setWorkingCopyOriginalId(ni.getCurrentObjectId());
                } else
                {
                    info.setWorkingCopyId(null);
                    info.setWorkingCopyOriginalId(null);
                }
            }

            // content
            String fileName = getStringProperty(object, PropertyIds.CONTENT_STREAM_FILE_NAME);
            String mimeType = getStringProperty(object, PropertyIds.CONTENT_STREAM_MIME_TYPE);
            String streamId = getIdProperty(object, PropertyIds.CONTENT_STREAM_ID);
            BigInteger length = getIntegerProperty(object, PropertyIds.CONTENT_STREAM_LENGTH);
            boolean hasContent = fileName != null || mimeType != null || streamId != null || length != null;
            if (hasContent)
            {
                info.setHasContent(hasContent);
                info.setContentType(mimeType);
                info.setFileName(fileName);
            }
            else
            {
                info.setHasContent(false);
                info.setContentType(null);
                info.setFileName(null);
            }

            // parent
            info.setHasParent(ni.isCurrentVersion() || ni.isPWC());

            // policies and relationships
            info.setSupportsRelationships(true);
            info.setSupportsPolicies(true);

            // renditions
            String renditionFilter = getRequestParameterRenditionFilter();
            List<RenditionInfo> renditionInfos = new ArrayList<>();
            CMISNodeInfo nodeInfo = getOrCreateNodeInfo(objectId);
            NodeRef nodeRef = nodeInfo.getNodeRef();
            if (nodeRef != null)
            {
                List<RenditionData> renditions = connector.getRenditions(nodeRef, renditionFilter, null, null);
                for (RenditionData rendition : renditions)
                {
                    RenditionInfoImpl renditionInfo = new RenditionInfoImpl();
                    renditionInfo.setId(rendition.getStreamId());
                    renditionInfo.setKind(rendition.getKind());
                    renditionInfo.setContentType(rendition.getMimeType());
                    renditionInfo.setTitle(rendition.getTitle());
                    renditionInfo.setLength(rendition.getBigLength());
                    renditionInfos.add(renditionInfo);
                }
            }
            info.setRenditionInfos(renditionInfos);

            // relationships
            setRelaionshipsToObjectInfo(object, info);

            // global settings
            info.setHasAcl(true);
            info.setSupportsDescendants(true);
            info.setSupportsFolderTree(true);
        }
        else if (ni.isItem())
        {
        	info.setHasAcl(true);
        	info.setHasContent(false);        	
        }

        return info;
    }

    private void setRelaionshipsToObjectInfo(ObjectData object, ObjectInfoImpl info)
    {
        info.setRelationshipSourceIds(null);
        info.setRelationshipTargetIds(null);

        IncludeRelationships includeRelationships = getRequestParameterIncludeRelationships();
        if (includeRelationships != IncludeRelationships.NONE)
        {
            List<ObjectData> relationships = object.getRelationships();
            if (relationships != null && relationships.size() > 0)
            {
                List<String> sourceIds = new ArrayList<String>();
                List<String> targetIds = new ArrayList<String>();
                for (ObjectData relationship : relationships)
                {
                    String sourceId = getIdProperty(relationship, PropertyIds.SOURCE_ID);
                    String targetId = getIdProperty(relationship, PropertyIds.TARGET_ID);
                    if (object.getId().equals(sourceId))
                    {
                        sourceIds.add(relationship.getId());
                    }
                    if (object.getId().equals(targetId))
                    {
                        targetIds.add(relationship.getId());
                    }
                }
                if (sourceIds.size() > 0 &&
                    (includeRelationships == IncludeRelationships.SOURCE ||
                     includeRelationships == IncludeRelationships.BOTH))
                {
                    info.setRelationshipSourceIds(sourceIds);
                }
                if (targetIds.size() > 0 &&
                    (includeRelationships == IncludeRelationships.TARGET ||
                     includeRelationships == IncludeRelationships.BOTH))
                {
                    info.setRelationshipTargetIds(targetIds);
                }
            }
        }
    }

    // --------------------------------------------------------

    protected void checkRepositoryId(String repositoryId)
    {
        if (!connector.getRepositoryId().equals(repositoryId))
        {
            throw new CmisObjectNotFoundException("Unknown repository '" + repositoryId + "'!");
        }
    }

    /**
     * Inspired from NodesImpl.guessEncoding method.
     * 
     * @param inputStream can be null;
     * @param mimeType can be null;
     * @return the encoding detected. never null;
     */
    private String getEncoding(InputStream inputStream, String mimeType)
    {
        String defaultEncoding = "UTF-8";
        if (inputStream == null)
        {
            return defaultEncoding;
        }

        InputStream tfis = null;
        try
        {
            tfis = new BufferedInputStream(inputStream);
            ContentCharsetFinder charsetFinder = connector.getMimetypeService().getContentCharsetFinder();
            return charsetFinder.getCharset(tfis, mimeType).name();
        }
        catch (Exception e)
        {
            throw new CmisStorageException("Unable to read content: " + e.getMessage(), e);
        }
        finally
        {
            if (tfis != null)
            {
                try
                {
                    tfis.close();
                }
                catch (Exception e)
                {
                    // nothing
                }
            }
        }
    }

    @Override
    public void beforeCall()
    {
        AuthenticationUtil.pushAuthentication();
        if (authentication != null)
        {
            // Use the previously-obtained authentication
            AuthenticationUtil.setFullAuthentication(authentication);
        }
        else
        {
        	CallContext context = getContext();
            if (context == null)
            {
                // Service not opened, yet
                return;
            }
            // Sticky sessions?
            if (connector.openHttpSession())
            {
                // create a session -> set a cookie
                // if the CMIS client supports cookies that might help in clustered environments
                ((HttpServletRequest)context.get(CallContext.HTTP_SERVLET_REQUEST)).getSession();
            }
            
            // Authenticate
            if (authentication != null)
            {
                // We have already authenticated; just reuse the authentication
                AuthenticationUtil.setFullAuthentication(authentication);
            }
            else
            {
                // First check if we already are authenticated
                if (AuthenticationUtil.getFullyAuthenticatedUser() == null)
                {
                    // We have to go to the repo and authenticate
                    String user = context.getUsername();
                    String password = context.getPassword();
                    Authorization auth = new Authorization(user, password);
                    if (auth.isTicket())
                    {
                        connector.getAuthenticationService().validate(auth.getTicket());
                    }
                    else
                    {
                        connector.getAuthenticationService().authenticate(auth.getUserName(), auth.getPasswordCharArray());
                    }
                }
                this.authentication = AuthenticationUtil.getFullAuthentication();
            }
            
//            // TODO: How is the proxy user working.
//            //       Until we know what it is meant to do, it's not available
//            String currentUser = connector.getAuthenticationService().getCurrentUserName();
//            String user = getContext().getUsername();
//            String password = getContext().getPassword();
//            if (currentUser != null && currentUser.equals(connector.getProxyUser()))
//            {
//                if (user != null && user.length() > 0)
//                {
//                    AuthenticationUtil.setFullyAuthenticatedUser(user);
//                }
//            }
        }
    }

    @Override
    public void afterCall()
    {
        AuthenticationUtil.popAuthentication();
    }
}
