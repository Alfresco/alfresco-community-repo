/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.opencmis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.opencmis.dictionary.CMISObjectVariant;
import org.alfresco.opencmis.dictionary.FolderTypeDefintionWrapper;
import org.alfresco.opencmis.dictionary.PropertyDefintionWrapper;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.Authorization;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInServiceException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.alfresco.util.TempFileProvider;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
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
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * OpenCMIS service object.
 * 
 * @author florian.mueller
 */
public class AlfrescoCmisService extends AbstractCmisService
{
    private static Log logger = LogFactory.getLog(AlfrescoCmisService.class);

    private static final String MIN_FILTER = "cmis:name,cmis:baseTypeId,cmis:objectTypeId,"
            + "cmis:createdBy,cmis:creationDate,cmis:lastModifiedBy,cmis:lastModificationDate,"
            + "cmis:contentStreamLength,cmis:contentStreamMimeType,cmis:contentStreamFileName,"
            + "cmis:contentStreamId";

    private CMISConnector connector;
    private CallContext context;
    private UserTransaction txn;
    private Map<String, CMISNodeInfo> nodeInfoMap;
    private Map<String, ObjectInfo> objectInfoMap;

    public AlfrescoCmisService(CMISConnector connector)
    {
        this.connector = connector;
        nodeInfoMap = new HashMap<String, CMISNodeInfo>();
        objectInfoMap = new HashMap<String, ObjectInfo>();
    }

    public void beginCall(CallContext context)
    {
        this.context = context;

        AuthenticationUtil.pushAuthentication();

        try
        {
            String currentUser = connector.getAuthenticationService().getCurrentUserName();
            String user = context.getUsername();
            String password = context.getPassword();

            if (currentUser == null)
            {
                Authorization auth = new Authorization(user, password);

                if (auth.isTicket())
                {
                    connector.getAuthenticationService().validate(auth.getTicket());
                } else
                {
                    connector.getAuthenticationService().authenticate(auth.getUserName(), auth.getPasswordCharArray());
                }

            } else if (currentUser.equals(connector.getProxyUser()))
            {
                if (user != null && user.length() > 0)
                {
                    AuthenticationUtil.setFullyAuthenticatedUser(user);
                }
            }
        } catch (AuthenticationException ae)
        {
            throw new CmisPermissionDeniedException(ae.getMessage(), ae);
        }

        // start read-only transaction
        try
        {
            beginReadOnlyTransaction();
        } catch (Exception e)
        {
            AuthenticationUtil.popAuthentication();

            if (e instanceof CmisBaseException)
            {
                throw (CmisBaseException) e;
            } else
            {
                throw new CmisRuntimeException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void close()
    {
        try
        {
            endReadOnlyTransaction();
        } catch (Exception e)
        {
            if (e instanceof CmisBaseException)
            {
                throw (CmisBaseException) e;
            } else
            {
                throw new CmisRuntimeException(e.getMessage(), e);
            }
        } finally
        {
            AuthenticationUtil.popAuthentication();
            context = null;
            nodeInfoMap.clear();
            objectInfoMap.clear();
        }
    }

    /**
     * Begins the embracing read-only transaction.
     */
    protected void beginReadOnlyTransaction()
    {
        txn = null;
        try
        {
            txn = connector.getTransactionService().getNonPropagatingUserTransaction(true);
            txn.begin();
        } catch (Exception e)
        {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Ends embracing read-only transaction.
     */
    protected void endReadOnlyTransaction()
    {
        try
        {
            if (txn != null)
            {
                // there isn't anything to commit, really
                // we just have to end the transaction
                if (txn.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                {
                    txn.rollback();
                } else
                {
                    txn.commit();
                }
                txn = null;
            }
        } catch (Exception e)
        {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    protected CMISNodeInfoImpl createNodeInfo(NodeRef nodeRef)
    {
        CMISNodeInfoImpl result = connector.createNodeInfo(nodeRef);
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
        return Collections.singletonList(connector.getRepositoryInfo());
    }

    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        return connector.getRepositoryInfo();
    }

    @Override
    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
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
            childrenList = connector.getOpenCMISDictionaryService().getBaseTypes();
        } else
        {
            TypeDefinitionWrapper tdw = connector.getOpenCMISDictionaryService().findType(typeId);
            if (tdw == null)
            {
                throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
            }

            childrenList = tdw.getChildren();
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
    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
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
            for (TypeDefinitionWrapper tdw : connector.getOpenCMISDictionaryService().getBaseTypes())
            {
                result.add(getTypesDescendants(d, tdw, includePropertyDefinitions));
            }
        } else
        {
            TypeDefinitionWrapper tdw = connector.getOpenCMISDictionaryService().findType(typeId);
            if (tdw == null)
            {
                throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
            }

            if (tdw.getChildren() != null)
            {
                for (TypeDefinitionWrapper child : tdw.getChildren())
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
    private TypeDefinitionContainer getTypesDescendants(int depth, TypeDefinitionWrapper tdw,
            boolean includePropertyDefinitions)
    {
        TypeDefinitionContainerImpl result = new TypeDefinitionContainerImpl();

        result.setTypeDefinition(tdw.getTypeDefinition(includePropertyDefinitions));

        if (depth != 0)
        {
            if (tdw.getChildren() != null)
            {
                result.setChildren(new ArrayList<TypeDefinitionContainer>());
                for (TypeDefinitionWrapper tdc : tdw.getChildren())
                {
                    result.getChildren().add(
                            getTypesDescendants(depth < 0 ? -1 : depth - 1, tdc, includePropertyDefinitions));
                }
            }
        }

        return result;
    }

    // --- navigation service ---

    /*
     * Lucene based getChildren - deactivated
     */
    public ObjectInFolderList XgetChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // convert BigIntegers to int
        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        int skip = (skipCount == null || skipCount.intValue() < 0 ? 0 : skipCount.intValue());

        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        List<ObjectInFolderData> list = new ArrayList<ObjectInFolderData>();
        result.setObjects(list);

        // get the children references
        NodeRef folderNodeRef = getOrCreateFolderInfo(folderId, "Folder").getNodeRef();

        // lucene part
        QName PARAM_PARENT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "parent");
        DataTypeDefinition nodeRefDataType = connector.getDictionaryService().getDataType(DataTypeDefinition.NODE_REF);

        SearchParameters params = new SearchParameters();
        params.setLanguage(SearchService.LANGUAGE_LUCENE);
        params.addStore(folderNodeRef.getStoreRef());
        QueryParameterDefinition parentDef = new QueryParameterDefImpl(PARAM_PARENT, nodeRefDataType, true,
                folderNodeRef.toString());
        params.addQueryParameterDefinition(parentDef);

        // Build a query for the appropriate types
        StringBuilder query = new StringBuilder(1024).append("+PARENT:\"${cm:parent}\" -ASPECT:\"")
                .append(ContentModel.ASPECT_WORKING_COPY).append("\" +TYPE:(");

        // Include doc type if necessary
        query.append('"').append(ContentModel.TYPE_CONTENT).append('"');
        query.append(" ");
        query.append('"').append(ContentModel.TYPE_FOLDER).append('"');

        // Always exclude system folders
        query.append(") -TYPE:\"").append(ContentModel.TYPE_SYSTEM_FOLDER).append("\"");
        params.setQuery(query.toString());
        // parseOrderBy(orderBy, params);
        ResultSet resultSet = null;

        List<NodeRef> childrenList;
        try
        {
            resultSet = connector.getSearchService().query(params);
            childrenList = resultSet.getNodeRefs();
        } finally
        {
            if (resultSet != null)
                resultSet.close();
        }

        if (max > 0)
        {
            int lastIndex = (max + skip > childrenList.size() ? childrenList.size() : max + skip) - 1;
            for (int i = skip; i <= lastIndex; i++)
            {
                NodeRef child = childrenList.get(i);
                CMISNodeInfo ni = createNodeInfo(child);

                // create a child CMIS object
                ObjectData object = connector.createCMISObject(ni, filter, includeAllowableActions,
                        includeRelationships, renditionFilter, false, false);

                ObjectInFolderDataImpl childData = new ObjectInFolderDataImpl();
                childData.setObject(object);

                // include path segment
                if (includePathSegment)
                {
                    childData.setPathSegment(connector.getName(child));
                }

                // add it
                list.add(childData);
            }
        }

        result.setHasMoreItems(childrenList.size() - skip > result.getObjects().size());
        result.setNumItems(BigInteger.valueOf(childrenList.size()));

        return result;
    }

    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
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
                        logger.debug("Too many sort properties in 'orderBy' - ignore those above max (max="
                                + maxSortProps + ",actual=" + len + ")");
                    }
                    len = maxSortProps;
                }
                for (int i = 0; i < len; i++)
                {
                    String[] sort = parts[i].split(" +");

                    if (sort.length > 0)
                    {
                        PropertyDefintionWrapper propDef = connector.getOpenCMISDictionaryService()
                                .findPropertyByQueryName(sort[0]);
                        if (propDef != null)
                        {
                            QName sortProp = propDef.getPropertyAccessor().getMappedProperty();
                            if (sortProp != null)
                            {
                                boolean sortAsc = ((sort.length == 1) || (sortAsc = (sort[1].equalsIgnoreCase("asc"))));
                                sortProps.add(new Pair<QName, Boolean>(sortProp, sortAsc));
                            } else
                            {
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Ignore sort property '" + sort[0] + " - mapping not found");
                                }
                            }
                        } else
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("Ignore sort property '" + sort[0] + " - query name not found");
                            }
                        }
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
        pageRequest.setRequestTotalCountMax(skip + 10000); // TODO make this
                                                           // optional/configurable
                                                           // - affects whether
                                                           // numItems may be
                                                           // returned

        PagingResults<FileInfo> pageOfNodeInfos = connector.getFileFolderService().list(folderNodeRef, true, true,
                null, sortProps, pageRequest);

        if (max > 0)
        {
            for (FileInfo child : pageOfNodeInfos.getPage())
            {
                try
                {
                    // create a child CMIS object
                    CMISNodeInfo ni = createNodeInfo(child.getNodeRef());
                    ObjectData object = connector.createCMISObject(ni, child, filter, includeAllowableActions,
                            includeRelationships, renditionFilter, false, false);

                    if (context.isObjectInfoRequired())
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
                } catch (InvalidNodeRefException e)
                {
                    // ignore invalid children
                }
            }
        }

        // / has more ?
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

        if (logger.isDebugEnabled())
        {
            logger.debug("getChildren: " + list.size() + " in " + (System.currentTimeMillis() - start) + " msecs");
        }

        return result;
    }

    @Override
    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();

        getDescendantsTree(repositoryId, getOrCreateFolderInfo(folderId, "Folder").getNodeRef(), depth.intValue(),
                filter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, false,
                result);

        return result;
    }

    @Override
    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();

        getDescendantsTree(repositoryId, getOrCreateFolderInfo(folderId, "Folder").getNodeRef(), depth.intValue(),
                filter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, true,
                result);

        return result;
    }

    private void getDescendantsTree(String repositoryId, NodeRef folderNodeRef, int depth, String filter,
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

                // create a child CMIS object
                ObjectInFolderDataImpl object = new ObjectInFolderDataImpl();
                CMISNodeInfo ni = createNodeInfo(child.getChildRef());
                object.setObject(connector.createCMISObject(ni, filter, includeAllowableActions, includeRelationships,
                        renditionFilter, false, false));
                if (context.isObjectInfoRequired())
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
                    getDescendantsTree(repositoryId, child.getChildRef(), depth - 1, filter, includeAllowableActions,
                            includeRelationships, renditionFilter, includePathSegment, foldersOnly,
                            container.getChildren());
                }

                // add it
                list.add(container);
            } catch (InvalidNodeRefException e)
            {
                // ignore invalid children
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

        ObjectData result = connector.createCMISObject(parentInfo, filter, false, IncludeRelationships.NONE,
                CMISConnector.RENDITION_NONE, false, false);
        if (context.isObjectInfoRequired())
        {
            getObjectInfo(repositoryId, parentInfo.getObjectId(), IncludeRelationships.NONE);
        }

        return result;
    }

    @Override
    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        List<ObjectParentData> result = new ArrayList<ObjectParentData>();

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // relationships are not filed
        if (info.isRelationship())
        {
            throw new CmisConstraintException("Relationships are not fileable!");
        }

        if (info.isFolder() && !info.isRootFolder())
        {
            List<CMISNodeInfo> parentInfos = info.getParents();
            if (!parentInfos.isEmpty())
            {
                CMISNodeInfo parentInfo = addNodeInfo(parentInfos.get(0));

                ObjectData object = connector.createCMISObject(parentInfo, filter, includeAllowableActions,
                        includeRelationships, renditionFilter, false, false);
                if (context.isObjectInfoRequired())
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
        } else if (info.isCurrentVersion() || info.isPWC())
        {
            List<CMISNodeInfo> parentInfos = info.getParents();
            for (CMISNodeInfo parentInfo : parentInfos)
            {
                addNodeInfo(parentInfo);

                ObjectData object = connector.createCMISObject(parentInfo, filter, includeAllowableActions,
                        includeRelationships, renditionFilter, false, false);
                if (context.isObjectInfoRequired())
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

        return result;
    }

    @Override
    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        return new ObjectListImpl();
    }

    // --- object service ---

    @Override
    public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
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
            newId = createDocument(repositoryId, properties, folderId, contentStream, versioningState, policies, null,
                    null, extension);
            break;
        case CMIS_FOLDER:
            newId = createFolder(repositoryId, properties, folderId, policies, null, null, extension);
            break;
        case CMIS_POLICY:
            newId = createPolicy(repositoryId, properties, folderId, policies, null, null, extension);
            break;
        }

        // check new object id
        if (newId == null)
        {
            throw new CmisRuntimeException("Creation failed!");
        }

        if (context.isObjectInfoRequired())
        {
            try
            {
                getObjectInfo(repositoryId, newId, "*", IncludeRelationships.NONE);
            } catch (InvalidNodeRefException e)
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
        final String name = connector.getNameProperty(properties);
        final String objectTypeId = connector.getObjectTypeIdProperty(properties);
        final TypeDefinitionWrapper type = connector.getTypeForCreate(objectTypeId, BaseTypeId.CMIS_FOLDER);

        connector.checkChildObjectType(parentInfo, type.getTypeId());

        // run transaction
        endReadOnlyTransaction();
        NodeRef newNodeRef = connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Exception
                    {
                        try
                        {
                            NodeRef nodeRef = connector.getFileFolderService()
                                    .create(parentInfo.getNodeRef(), name, type.getAlfrescoClass()).getNodeRef();

                            connector.setProperties(nodeRef, type, properties, new String[] { PropertyIds.NAME,
                                    PropertyIds.OBJECT_TYPE_ID });
                            connector.applyPolicies(nodeRef, type, policies);
                            connector.applyACL(nodeRef, type, addAces, removeAces);

                            return nodeRef;
                        } catch (FileExistsException fee)
                        {
                            throw new CmisContentAlreadyExistsException("An object with this name already exists!", fee);
                        } catch (IntegrityException ie)
                        {
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();

        return newNodeRef.toString();
    }

    @Override
    public String createDocument(String repositoryId, final Properties properties, String folderId,
            final ContentStream contentStream, final VersioningState versioningState, final List<String> policies,
            final Acl addAces, final Acl removeAces, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get the parent folder node ref
        final CMISNodeInfo parentInfo = getOrCreateFolderInfo(folderId, "Parent folder");

        // get name and type
        final String name = connector.getNameProperty(properties);
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

        if (docType.isVersionable() && (versioningState == VersioningState.NONE))
        {
            throw new CmisConstraintException("This document type is versionable!");
        }

        if (!docType.isVersionable() && (versioningState != VersioningState.NONE))
        {
            throw new CmisConstraintException("This document type is not versionable!");
        }

        // copy stream to temp file
        final File tempFile = copyToTempFile(contentStream);
        final Charset encoding = (tempFile == null ? null : getEncoding(tempFile, contentStream.getMimeType()));

        // run transaction
        endReadOnlyTransaction();
        NodeRef newNodeRef = connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Exception
                    {
                        try
                        {
                            NodeRef nodeRef = connector.getFileFolderService()
                                    .create(parentInfo.getNodeRef(), name, type.getAlfrescoClass()).getNodeRef();

                            connector.setProperties(nodeRef, type, properties, new String[] { PropertyIds.NAME,
                                    PropertyIds.OBJECT_TYPE_ID });
                            connector.applyPolicies(nodeRef, type, policies);
                            connector.applyACL(nodeRef, type, addAces, removeAces);

                            // handle content
                            if (contentStream != null)
                            {
                                // write content
                                ContentWriter writer = connector.getFileFolderService().getWriter(nodeRef);
                                writer.setMimetype(contentStream.getMimeType());
                                writer.setEncoding(encoding.name());
                                writer.putContent(tempFile);
                            }

                            connector.applyVersioningState(nodeRef, versioningState);

                            return nodeRef;
                        } catch (FileExistsException fee)
                        {
                            removeTempFile(tempFile);
                            throw new CmisContentAlreadyExistsException("An object with this name already exists!", fee);
                        } catch (IntegrityException ie)
                        {
                            removeTempFile(tempFile);
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            removeTempFile(tempFile);
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();

        removeTempFile(tempFile);

        return connector.createObjectId(newNodeRef);
    }

    @Override
    public String createDocumentFromSource(String repositoryId, String sourceId, final Properties properties,
            String folderId, final VersioningState versioningState, final List<String> policies, final Acl addAces,
            final Acl removeAces, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get the parent folder node ref
        final CMISNodeInfo parentInfo = getOrCreateFolderInfo(folderId, "Parent folder");

        // get name and type
        final String name = connector.getNameProperty(properties);

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

        final TypeDefinitionWrapper type = info.getType();
        connector.checkChildObjectType(parentInfo, type.getTypeId());

        // run transaction
        endReadOnlyTransaction();
        NodeRef newNodeRef = connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Exception
                    {
                        try
                        {
                            NodeRef newDocumentNodeRef = connector.getFileFolderService()
                                    .copy(sourceNodeRef, parentInfo.getNodeRef(), name).getNodeRef();

                            connector.setProperties(newDocumentNodeRef, type, properties, new String[] {
                                    PropertyIds.NAME, PropertyIds.OBJECT_TYPE_ID });
                            connector.applyPolicies(newDocumentNodeRef, type, policies);
                            connector.applyACL(newDocumentNodeRef, type, addAces, removeAces);
                            connector.applyVersioningState(newDocumentNodeRef, versioningState);

                            return newDocumentNodeRef;
                        } catch (FileExistsException fee)
                        {
                            throw new CmisContentAlreadyExistsException("An object with this name already exists!", fee);
                        } catch (IntegrityException ie)
                        {
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();

        return connector.createObjectId(newNodeRef);
    }

    @Override
    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
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
    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get type
        String objectTypeId = connector.getObjectTypeIdProperty(properties);
        final TypeDefinitionWrapper type = connector.getTypeForCreate(objectTypeId, BaseTypeId.CMIS_RELATIONSHIP);

        // get source object
        String sourceId = connector.getSourceIdProperty(properties);
        CMISNodeInfo sourceInfo = getOrCreateNodeInfo(sourceId, "Source");

        if (!sourceInfo.isVariant(CMISObjectVariant.CURRENT_VERSION) && !sourceInfo.isVariant(CMISObjectVariant.FOLDER))
        {
            throw new CmisInvalidArgumentException("Source is not the latest version of a document or a folder object!");
        }

        final NodeRef sourceNodeRef = sourceInfo.getNodeRef();

        // get target object
        String targetId = connector.getTargetIdProperty(properties);
        CMISNodeInfo targetInfo = getOrCreateNodeInfo(targetId, "Target");

        if (!targetInfo.isVariant(CMISObjectVariant.CURRENT_VERSION) && !targetInfo.isVariant(CMISObjectVariant.FOLDER))
        {
            throw new CmisInvalidArgumentException(
                    "Target is not the latest version of a document or a folder object!!");
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
        endReadOnlyTransaction();
        AssociationRef assocRef = connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<AssociationRef>()
                {
                    public AssociationRef execute() throws Exception
                    {
                        try
                        {
                            return connector.getNodeService().createAssociation(sourceNodeRef, targetNodeRef,
                                    type.getAlfrescoClass());
                        } catch (IntegrityException ie)
                        {
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();

        return CMISConnector.ASSOC_ID_PREFIX + assocRef.getId();
    }

    @Override
    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, final ContentStream contentStream, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        if (!info.isVariant(CMISObjectVariant.CURRENT_VERSION) && !info.isVariant(CMISObjectVariant.PWC))
        {
            throw new CmisStreamNotSupportedException("Content can only be set ondocuments!");
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

        // copy stream to temp file
        final File tempFile = copyToTempFile(contentStream);
        final Charset encoding = getEncoding(tempFile, contentStream.getMimeType());

        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            ContentWriter writer = connector.getFileFolderService().getWriter(nodeRef);
                            writer.setMimetype(contentStream.getMimeType());
                            writer.setEncoding(encoding.name());
                            writer.putContent(tempFile);

                            return null;
                        } catch (IntegrityException ie)
                        {
                            removeTempFile(tempFile);
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            removeTempFile(tempFile);
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();

        removeTempFile(tempFile);

        objectId.setValue(connector.createObjectId(nodeRef));
    }

    @Override
    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
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

        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            connector.getNodeService().setProperty(nodeRef, ContentModel.PROP_CONTENT, null);
                            return null;
                        } catch (IntegrityException ie)
                        {
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();

        objectId.setValue(connector.createObjectId(nodeRef));
    }

    @Override
    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // get object and source and target parent
        CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        final NodeRef nodeRef = info.getCurrentNodeNodeRef();
        final CMISNodeInfo sourceInfo = getOrCreateFolderInfo(sourceFolderId, "Source Folder");
        final CMISNodeInfo targetInfo = getOrCreateFolderInfo(targetFolderId, "Target Folder");

        connector.checkChildObjectType(targetInfo, info.getType().getTypeId());

        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            ChildAssociationRef primaryParentRef = connector.getNodeService().getPrimaryParent(nodeRef);
                            // if this is a primary child node, move it
                            if (primaryParentRef.getParentRef().equals(sourceInfo.getNodeRef()))
                            {
                                connector.getNodeService().moveNode(nodeRef, targetInfo.getNodeRef(),
                                        primaryParentRef.getTypeQName(), primaryParentRef.getQName());
                            } else
                            {
                                // otherwise, reparent it
                                for (ChildAssociationRef parent : connector.getNodeService().getParentAssocs(nodeRef,
                                        ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
                                {
                                    if (parent.getParentRef().equals(sourceInfo.getNodeRef()))
                                    {
                                        connector.getNodeService().removeChildAssociation(parent);
                                        connector.getNodeService().addChild(targetInfo.getNodeRef(), nodeRef,
                                                ContentModel.ASSOC_CONTAINS, parent.getQName());
                                        return null;
                                    }
                                }
                                throw new CMISInvalidArgumentException(
                                        "Document is not a child of the source folder that was specified!");
                            }

                            return null;
                        } catch (IntegrityException ie)
                        {
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();
    }

    @Override
    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            final Properties properties, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        final CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            throw new CmisInvalidArgumentException("Relationship properties cannot be updated!");
        } else
        {
            if (info.isVariant(CMISObjectVariant.VERSION))
            {
                throw new CmisInvalidArgumentException("Document is not the latest version!");
            }

            final NodeRef nodeRef = info.getNodeRef();

            // run transaction
            endReadOnlyTransaction();
            connector.getTransactionService().getRetryingTransactionHelper()
                    .doInTransaction(new RetryingTransactionCallback<Object>()
                    {
                        public Object execute() throws Exception
                        {
                            try
                            {
                                connector.setProperties(nodeRef, info.getType(), properties, new String[0]);
                                return null;
                            } catch (IntegrityException ie)
                            {
                                throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                            } catch (AccessDeniedException ade)
                            {
                                throw new CmisPermissionDeniedException("Permission denied!", ade);
                            }
                        };
                    }, false, true);
            beginReadOnlyTransaction();

            objectId.setValue(connector.createObjectId(nodeRef));

            if (context.isObjectInfoRequired())
            {
                getObjectInfo(repositoryId, objectId.getValue(), "*", IncludeRelationships.NONE);
            }
        }
    }

    @Override
    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension)
    {
        deleteObjectOrCancelCheckOut(repositoryId, objectId, allVersions, extension);
    }

    @Override
    public void deleteObjectOrCancelCheckOut(String repositoryId, final String objectId, final Boolean allVersions,
            ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        final CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // run transaction
        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Boolean>()
                {
                    public Boolean execute() throws Exception
                    {
                        try
                        {
                            // handle relationships
                            if (info.isVariant(CMISObjectVariant.ASSOC))
                            {
                                AssociationRef assocRef = info.getAssociationRef();
                                connector.getNodeService().removeAssociation(assocRef.getSourceRef(),
                                        assocRef.getTargetRef(), assocRef.getTypeQName());
                                return true;
                            }

                            NodeRef nodeRef = info.getNodeRef();

                            // handle PWC
                            if (info.isVariant(CMISObjectVariant.PWC))
                            {
                                connector.getCheckOutCheckInService().cancelCheckout(nodeRef);
                                return true;
                            }

                            // handle folders
                            if (info.isFolder())
                            {
                                if (connector.getNodeService().getChildAssocs(nodeRef).size() > 0)
                                {
                                    throw new CmisConstraintException(
                                            "Could not delete folder with at least one child!");
                                }

                                connector.getNodeService().deleteNode(nodeRef);
                                return true;
                            }

                            // handle versions
                            if (allVersions)
                            {
                                NodeRef workingCopy = connector.getCheckOutCheckInService().getWorkingCopy(nodeRef);
                                if (workingCopy != null)
                                {
                                    connector.getCheckOutCheckInService().cancelCheckout(workingCopy);
                                }
                            } else if (info.isVariant(CMISObjectVariant.VERSION))
                            {
                                Version version = ((CMISNodeInfoImpl) info).getVersion();
                                connector.getVersionService().deleteVersion(nodeRef, version);
                                return true;
                            }

                            if (info.isVariant(CMISObjectVariant.VERSION))
                            {
                                nodeRef = info.getCurrentNodeNodeRef();
                            }

                            // remove not primary parent associations
                            List<ChildAssociationRef> childAssociations = connector.getNodeService().getParentAssocs(
                                    nodeRef);
                            if (childAssociations != null)
                            {
                                for (ChildAssociationRef childAssoc : childAssociations)
                                {
                                    if (!childAssoc.isPrimary())
                                    {
                                        connector.getNodeService().removeChildAssociation(childAssoc);
                                    }
                                }
                            }

                            // attempt to delete the node
                            connector.getNodeService().deleteNode(nodeRef);
                            return true;
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        endReadOnlyTransaction();
    }

    @Override
    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
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

        // run transaction
        endReadOnlyTransaction();
        result.setIds(connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<List<String>>()
                {
                    public List<String> execute() throws Exception
                    {
                        return deleteBranch(folderNodeRef, continueOnFailure);
                    };
                }, false, true));
        beginReadOnlyTransaction();

        return result;
    }

    private List<String> deleteBranch(NodeRef nodeRef, boolean continueOnFailure)
    {
        List<String> result = new ArrayList<String>();

        try
        {
            // remove children
            List<ChildAssociationRef> childrenList = connector.getNodeService().getChildAssocs(nodeRef);
            if (childrenList != null)
            {
                for (ChildAssociationRef child : childrenList)
                {
                    List<String> ftod = deleteBranch(child.getChildRef(), continueOnFailure);
                    if (!ftod.isEmpty())
                    {
                        result.addAll(ftod);
                        if (!continueOnFailure)
                        {
                            return result;
                        }
                    }
                }
            }

            // remove not primary parent associations
            List<ChildAssociationRef> childAssociations = connector.getNodeService().getParentAssocs(nodeRef);
            if (childAssociations != null)
            {
                for (ChildAssociationRef childAssoc : childAssociations)
                {
                    if (!childAssoc.isPrimary())
                    {
                        connector.getNodeService().removeChildAssociation(childAssoc);
                    }
                }
            }

            // attempt to delete the node
            connector.getNodeService().deleteNode(nodeRef);
        } catch (Exception e)
        {
            result.add(nodeRef.toString());
        }

        return result;
    }

    @Override
    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // create a CMIS object
        ObjectData object = connector.createCMISObject(info, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePolicyIds, includeAcl);

        if (context.isObjectInfoRequired())
        {
            getObjectInfo(repositoryId, info.getObjectId(), includeRelationships);
        }

        return object;
    }

    @Override
    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // start at the root node
        NodeRef rootNodeRef = connector.getRootNodeRef();

        if (path.equals("/"))
        {
            return connector.createCMISObject(createNodeInfo(rootNodeRef), filter, includeAllowableActions,
                    includeRelationships, renditionFilter, includePolicyIds, includeAcl);
        } else
        {
            try
            {
                // resolve path and get the node ref
                FileInfo fileInfo = connector.getFileFolderService().resolveNamePath(rootNodeRef,
                        Arrays.asList(path.substring(1).split("/")));

                CMISNodeInfo info = createNodeInfo(fileInfo.getNodeRef());

                ObjectData object = connector.createCMISObject(info, fileInfo, filter, includeAllowableActions,
                        includeRelationships, renditionFilter, includePolicyIds, includeAcl);

                if (context.isObjectInfoRequired())
                {
                    getObjectInfo(repositoryId, info.getObjectId(), includeRelationships);
                }

                return object;
            } catch (FileNotFoundException e)
            {
                throw new CmisObjectNotFoundException("Object not found: " + path);
            }
        }
    }

    @Override
    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        if (context.isObjectInfoRequired())
        {
            getObjectInfo(repositoryId, info.getObjectId(), IncludeRelationships.NONE);
        }

        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            return connector.getAssocProperties(info, filter);
        } else
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
    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
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
        } else
        {
            return connector.getRendtions(info.getNodeRef(), renditionFilter, maxItems, skipCount);
        }
    }

    // --- versioning service ---

    @Override
    public void checkOut(String repositoryId, final Holder<String> objectId, ExtensionsData extension,
            final Holder<Boolean> contentCopied)
    {
        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId.getValue(), "Object");

        // check for current version
        if (!info.isVariant(CMISObjectVariant.CURRENT_VERSION))
        {
            throw new CmisInvalidArgumentException("Only documents can be checked out!");
        }

        // get object
        final NodeRef nodeRef = info.getNodeRef();

        if (!((DocumentTypeDefinition) info.getType().getTypeDefinition(false)).isVersionable())
        {
            throw new CmisConstraintException("Document is not versionable!");
        }

        // check out
        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            NodeRef pwcNodeRef = connector.getCheckOutCheckInService().checkout(nodeRef);
                            objectId.setValue(pwcNodeRef.toString());
                            if (contentCopied != null)
                            {
                                contentCopied.setValue(connector.getFileFolderService().getReader(pwcNodeRef) != null);
                            }
                            return null;
                        } catch (CheckOutCheckInServiceException e)
                        {
                            throw new CmisVersioningException("Check out failed: " + e.getMessage(), e);

                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();
    }

    @Override
    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // only accept a PWC
        if (info.isVariant(CMISObjectVariant.PWC))
        {
            throw new CmisVersioningException("Object is not a PWC!");
        }

        // get object
        final NodeRef nodeRef = info.getNodeRef();

        // cancel check out
        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            connector.getCheckOutCheckInService().cancelCheckout(nodeRef);
                            return null;
                        } catch (CheckOutCheckInServiceException e)
                        {
                            throw new CmisVersioningException("Check out failed: " + e.getMessage(), e);

                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();
    }

    @Override
    public void checkIn(String repositoryId, final Holder<String> objectId, final Boolean major,
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

        // copy stream to temp file
        final File tempFile = copyToTempFile(contentStream);
        final Charset encoding = (tempFile == null ? null : getEncoding(tempFile, contentStream.getMimeType()));

        // check in
        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            // update PWC
                            connector.setProperties(nodeRef, type, properties,
                                    new String[] { PropertyIds.OBJECT_TYPE_ID });
                            connector.applyPolicies(nodeRef, type, policies);
                            connector.applyACL(nodeRef, type, addAces, removeAces);

                            // handle content
                            if (contentStream != null)
                            {
                                // write content
                                ContentWriter writer = connector.getFileFolderService().getWriter(nodeRef);
                                writer.setMimetype(contentStream.getMimeType());
                                writer.setEncoding(encoding.name());
                                writer.putContent(tempFile);
                            }

                            // check aspect
                            if (connector.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) == false)
                            {
                                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                                props.put(ContentModel.PROP_INITIAL_VERSION, false);
                                props.put(ContentModel.PROP_AUTO_VERSION, false);
                                connector.getNodeService().addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, props);
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
                            NodeRef newNodeRef = connector.getCheckOutCheckInService().checkin(nodeRef,
                                    versionProperties);

                            objectId.setValue(connector.createObjectId(newNodeRef));

                            return null;
                        } catch (FileExistsException fee)
                        {
                            removeTempFile(tempFile);
                            throw new CmisContentAlreadyExistsException("An object with this name already exists!", fee);
                        } catch (IntegrityException ie)
                        {
                            removeTempFile(tempFile);
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (CheckOutCheckInServiceException e)
                        {
                            removeTempFile(tempFile);
                            throw new CmisVersioningException("Check out failed: " + e.getMessage(), e);

                        } catch (AccessDeniedException ade)
                        {
                            removeTempFile(tempFile);
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();

        removeTempFile(tempFile);
    }

    @Override
    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
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

            if (context.isObjectInfoRequired())
            {
                getObjectInfo(repositoryId, info.getObjectId(), IncludeRelationships.NONE);
            }
        } else
        {
            if (info.hasPWC())
            {
                CMISNodeInfo pwcInfo = createNodeInfo(connector.getCheckOutCheckInService().getWorkingCopy(nodeRef));

                result.add(connector.createCMISObject(pwcInfo, filter, includeAllowableActions,
                        IncludeRelationships.NONE, CMISConnector.RENDITION_NONE, false, false));

                if (context.isObjectInfoRequired())
                {
                    getObjectInfo(repositoryId, pwcInfo.getObjectId(), IncludeRelationships.NONE);
                }
            }

            // convert the version history
            for (Version version : versionHistory.getAllVersions())
            {
                CMISNodeInfo versionInfo = createNodeInfo(version.getFrozenStateNodeRef());

                result.add(connector.createCMISObject(versionInfo, filter, includeAllowableActions,
                        IncludeRelationships.NONE, CMISConnector.RENDITION_NONE, false, false));

                if (context.isObjectInfoRequired())
                {
                    getObjectInfo(repositoryId, versionInfo.getObjectId(), IncludeRelationships.NONE);
                }
            }
        }

        return result;
    }

    @Override
    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        if (objectId != null)
        {
            // it's an AtomPub call
            versionSeriesId = connector.getCurrentVersionId(objectId);
        }

        // what kind of object is it?
        CMISNodeInfo info = getOrCreateNodeInfo(versionSeriesId, "Version Series");
        CMISNodeInfo versionInfo = createNodeInfo(((CMISNodeInfoImpl) info).getLatestVersionNodeRef(major));

        ObjectData object = connector.createCMISObject(versionInfo, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePolicyIds, includeAcl);

        if (context.isObjectInfoRequired())
        {
            getObjectInfo(repositoryId, info.getObjectId(), includeRelationships);
        }

        return object;
    }

    @Override
    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
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
        } else
        {
            CMISNodeInfo versionInfo = createNodeInfo(((CMISNodeInfoImpl) info).getLatestVersionNodeRef(major));
            addNodeInfo(versionInfo);
            return connector.getNodeProperties(versionInfo, filter);
        }
    }

    // --- multifiling service ---

    @Override
    public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions,
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

        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            connector.getNodeService().addChild(folderInfo.getNodeRef(), nodeRef,
                                    ContentModel.ASSOC_CONTAINS, name);
                            return null;
                        } catch (IntegrityException ie)
                        {
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();
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

        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            connector.getNodeService().removeChild(folderNodeRef, nodeRef);
                            return null;
                        } catch (IntegrityException ie)
                        {
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();
    }

    // --- discovery service ---

    @Override
    public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
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

        return connector.query(statement, includeAllowableActions, includeRelationships, renditionFilter, maxItems,
                skipCount);
    }

    // --- relationship service ---

    @Override
    public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
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
            } else
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

        return connector.getObjectRelationships(info.getNodeRef(), relationshipDirection, typeId, filter,
                includeAllowableActions, maxItems, skipCount);
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
    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        // what kind of object is it?
        getOrCreateNodeInfo(objectId, "Object");

        // policies are not supported -> return empty list
        return Collections.emptyList();
    }

    // --- ACL service ---

    @Override
    public Acl applyAcl(String repositoryId, String objectId, final Acl addAces, final Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension)
    {
        checkRepositoryId(repositoryId);

        if (aclPropagation == AclPropagation.OBJECTONLY)
        {
            throw new CmisInvalidArgumentException("ACL propagation 'objectonly' is not supported!");
        }

        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // relationships don't have ACLs
        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            throw new CmisConstraintException("Relationships are not ACL controllable!");
        }

        final NodeRef nodeRef = info.getCurrentNodeNodeRef();
        final TypeDefinitionWrapper type = info.getType();

        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            connector.applyACL(nodeRef, type, addAces, removeAces);
                            return null;
                        } catch (IntegrityException ie)
                        {
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();

        return connector.getACL(nodeRef, false);
    }

    @Override
    public Acl applyAcl(String repositoryId, String objectId, final Acl aces, AclPropagation aclPropagation)
    {
        checkRepositoryId(repositoryId);

        if (aclPropagation == AclPropagation.OBJECTONLY)
        {
            throw new CmisInvalidArgumentException("ACL propagation 'objectonly' is not supported!");
        }

        CMISNodeInfo info = getOrCreateNodeInfo(objectId, "Object");

        // relationships don't have ACLs
        if (info.isVariant(CMISObjectVariant.ASSOC))
        {
            throw new CmisConstraintException("Relationships are not ACL controllable!");
        }

        final NodeRef nodeRef = info.getCurrentNodeNodeRef();
        final TypeDefinitionWrapper type = info.getType();

        endReadOnlyTransaction();
        connector.getTransactionService().getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Exception
                    {
                        try
                        {
                            connector.applyACL(nodeRef, type, aces);
                            return null;
                        } catch (IntegrityException ie)
                        {
                            throw new CmisConstraintException("Constraint violation: " + ie.getMessage(), ie);
                        } catch (AccessDeniedException ade)
                        {
                            throw new CmisPermissionDeniedException("Permission denied!", ade);
                        }
                    };
                }, false, true);
        beginReadOnlyTransaction();

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

    public ObjectInfo getObjectInfo(String repositoryId, String objectId, IncludeRelationships includeRelationships)
    {
        return getObjectInfo(repositoryId, objectId, null, includeRelationships);
    }

    public ObjectInfo getObjectInfo(String repositoryId, String objectId, String filter,
            IncludeRelationships includeRelationships)
    {
        ObjectInfo info = objectInfoMap.get(objectId);
        if (info == null)
        {
            CMISNodeInfo nodeInfo = getOrCreateNodeInfo(objectId);

            // object info has not been found -> create one
            try
            {
                if (filter == null)
                {
                    filter = MIN_FILTER;
                } else if (!filter.equals("*"))
                {
                    filter = filter + "," + MIN_FILTER;
                }

                // get the object and its info
                ObjectData object = connector.createCMISObject(nodeInfo, filter, false, includeRelationships, null,
                        false, false);

                info = getObjectInfoIntern(repositoryId, object);

                // add object info
                objectInfoMap.put(objectId, info);
            } catch (Exception e)
            {
                e.printStackTrace();
                info = null;
            }
        }
        return info;
    }

    /**
     * Collects the {@link ObjectInfo} about an object.
     * 
     * (Provided by OpenCMIS, but optimized for Alfresco.)
     */
    @Override
    protected ObjectInfo getObjectInfoIntern(String repositoryId, ObjectData object)
    {
        // if the object has no properties, stop here
        if (object.getProperties() == null || object.getProperties().getProperties() == null)
        {
            throw new CmisRuntimeException("No properties!");
        }

        CMISNodeInfo ni = getOrCreateNodeInfo(object.getId());

        ObjectInfoImpl info = new ObjectInfoImpl();

        // general properties
        info.setObject(object);
        info.setId(object.getId());
        info.setName(ni.getName());
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
        } else if (ni.isFolder())
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
        } else if (ni.isDocument())
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
            } else
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
            } else
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
            info.setRenditionInfos(null);
            List<RenditionData> renditions = object.getRenditions();
            if (renditions != null && renditions.size() > 0)
            {
                List<RenditionInfo> renditionInfos = new ArrayList<RenditionInfo>();
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
                info.setRenditionInfos(renditionInfos);
            }

            // relationships
            setRelaionshipsToObjectInfo(object, info);

            // global settings
            info.setHasAcl(true);
            info.setSupportsDescendants(true);
            info.setSupportsFolderTree(true);
        }

        return info;
    }

    private void setRelaionshipsToObjectInfo(ObjectData object, ObjectInfoImpl info)
    {
        info.setRelationshipSourceIds(null);
        info.setRelationshipTargetIds(null);

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
            if (sourceIds.size() > 0)
            {
                info.setRelationshipSourceIds(sourceIds);
            }
            if (targetIds.size() > 0)
            {
                info.setRelationshipTargetIds(targetIds);
            }
        }
    }

    // --------------------------------------------------------

    private void checkRepositoryId(String repositoryId)
    {
        if (!connector.getRepositoryId().equals(repositoryId))
        {
            throw new CmisObjectNotFoundException("Unknown repository '" + repositoryId + "'!");
        }
    }

    private Charset getEncoding(File tempFile, String mimeType)
    {
        Charset encoding = null;

        try
        {
            InputStream tfis = new BufferedInputStream(new FileInputStream(tempFile));
            ContentCharsetFinder charsetFinder = connector.getMimetypeService().getContentCharsetFinder();
            encoding = charsetFinder.getCharset(tfis, mimeType);
            tfis.close();
        } catch (Exception e)
        {
            throw new CmisStorageException("Unable to read content: " + e.getMessage(), e);
        }

        return encoding;
    }

    private File copyToTempFile(ContentStream contentStream)
    {
        File tempFile = null;

        if (contentStream == null)
        {
            return tempFile;
        }

        int bufferSize = 40 * 1014;

        try
        {
            tempFile = TempFileProvider.createTempFile("cmis", "content");
            if (contentStream.getStream() != null)
            {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile), bufferSize);
                InputStream in = new BufferedInputStream(contentStream.getStream(), bufferSize);

                byte[] buffer = new byte[bufferSize];
                int i;
                while ((i = in.read(buffer)) > -1)
                {
                    out.write(buffer, 0, i);
                }

                in.close();
                out.close();
            }
        } catch (Exception e)
        {
            throw new CmisStorageException("Unable to store content: " + e.getMessage(), e);
        }

        return tempFile;
    }

    private void removeTempFile(File tempFile)
    {
        if (tempFile == null)
        {
            return;
        }

        try
        {
            tempFile.delete();
        } catch (Exception e)
        {
            // ignore - file will be removed by TempFileProvider
        }
    }
}
