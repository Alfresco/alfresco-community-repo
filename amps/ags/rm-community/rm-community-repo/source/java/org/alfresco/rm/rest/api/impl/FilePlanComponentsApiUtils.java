/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api.impl;

import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.acegisecurity.vote.AccessDecisionVoter;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.content.ContentLimitViolationException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl.InvalidTypeException;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropBoolean;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Activities;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InsufficientStorageException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.RequestEntityTooLargeException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.rm.rest.api.RMSites;
import org.alfresco.rm.rest.api.model.RMNode;
import org.alfresco.rm.rest.api.model.RMSite;
import org.alfresco.rm.rest.api.model.TransferContainer;
import org.alfresco.rm.rest.api.model.UploadInfo;
import org.alfresco.service.cmr.activities.ActivityInfo;
import org.alfresco.service.cmr.activities.ActivityPoster;
import org.alfresco.service.cmr.attributes.DuplicateAttributeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.usage.ContentQuotaException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that handles common api endpoint tasks
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public class FilePlanComponentsApiUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchTypesFactory.class);

    public static final String FILE_PLAN_ALIAS = "-filePlan-";
    public static final String TRANSFERS_ALIAS = "-transfers-";
    public static final String UNFILED_ALIAS = "-unfiled-";
    public static final String HOLDS_ALIAS = "-holds-";
    public static final String RM_SITE_ID = "rm";
    public static final List<String> CONTAINERS_FOR_CLASSIFIABLE_CHILDREN_ALIAS = Arrays.asList(
            FILE_PLAN_ALIAS, UNFILED_ALIAS);
    //public static String PARAM_RELATIVE_PATH = "relativePath";

    // excluded properties
    public static final List<QName> TYPES_CAN_CREATE = Arrays.asList(
            RecordsManagementModel.TYPE_FILE_PLAN,
            RecordsManagementModel.TYPE_RECORD_CATEGORY,
            RecordsManagementModel.TYPE_RECORD_FOLDER,
            RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER,
            RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER,
            RecordsManagementModel.TYPE_HOLD_CONTAINER);

    /** RM Nodes API */
    private Nodes nodes;
    private FileFolderService fileFolderService;
    private FilePlanService filePlanService;
    private NodeService nodeService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private DictionaryService dictionaryService;
    private CapabilityService capabilityService;
    private PermissionService permissionService;
    private RecordService recordService;
    private AuthenticationUtil authenticationUtil;
    private ActivityPoster activityPoster;
    private RMSites sites;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }
    
    public void setActivityPoster(ActivityPoster poster)
    {
        this.activityPoster = poster;
    }

    public void setSites(RMSites sites)
    {
        this.sites = sites;
    }

    /**
     * lookup node and validate type
     *
     * @param nodeId
     * @param expectedNodeType
     * @return
     * @throws EntityNotFoundException
     */
    public NodeRef lookupAndValidateNodeType(String nodeId, QName expectedNodeType) throws EntityNotFoundException
    {
        return lookupAndValidateNodeType(nodeId, expectedNodeType, null);
    }

    /**
     * lookup node by id and relative path and validate type
     *
     * @param nodeId
     * @param expectedNodeType
     * @param relativePath
     * @return
     * @throws EntityNotFoundException
     */
    public NodeRef lookupAndValidateNodeType(String nodeId, QName expectedNodeType, String relativePath) throws EntityNotFoundException
    {
        return lookupAndValidateNodeType(nodeId, expectedNodeType, relativePath, false);
    }

    /**
     * lookup node by id and relative path and validate type
     *
     * @param nodeId
     * @param expectedNodeType
     * @param relativePath
     * @return
     * @throws EntityNotFoundException
     */
    public NodeRef lookupAndValidateNodeType(String nodeId, QName expectedNodeType, String relativePath, boolean readOnlyRelativePath) throws EntityNotFoundException
    {
        ParameterCheck.mandatoryString("nodeId", nodeId);
        ParameterCheck.mandatory("expectedNodeType", expectedNodeType);

        NodeRef nodeRef = lookupByPlaceholder(nodeId);

        QName nodeType = nodeService.getType(nodeRef);
        if (!nodeType.equals(expectedNodeType))
        {
            throw new InvalidArgumentException("The given id:'" + nodeId + "' (nodeType:" + nodeType.toString()
            + ") is not valid for this endpoint. Expected nodeType is:" + expectedNodeType.toString());
        }

        if(StringUtils.isNotBlank(relativePath))
        {
            nodeRef = lookupAndValidateRelativePath(nodeRef, relativePath, readOnlyRelativePath, expectedNodeType);
        }
        return nodeRef;
    }

    /**
     * look up node by id and validate node type is suitable container
     *
     * @param nodeId
     * @return
     */
    public NodeRef validateAndLookUpContainerNode(String nodeId, List<String> allowedPlaceholders)
    {
        ParameterCheck.mandatoryString("nodeId", nodeId);

        NodeRef nodeRef = lookupByAllowedPlaceholders(nodeId, allowedPlaceholders);
        QName nodeType = nodeService.getType(nodeRef);
        if(!dictionaryService.isSubClass(nodeType, TYPE_FOLDER))
        {
            throw new InvalidArgumentException("The given id:'" + nodeId + "' (nodeType:" + nodeType.toString()
            + ") is not valid for this endpoint. Expected nodeType is:" + TYPE_FOLDER.toString());
        }

        return nodeRef;
    }
    /**
     * Lookup node by placeholder from allowed placeholder list
     *
     * @param nodeId
     * @param allowedPlaceholders
     * @return NodeRef for corresponding id
     */
    public NodeRef lookupByAllowedPlaceholders(String nodeId, List<String> allowedPlaceholders)
    {
        NodeRef nodeRef;
        if (allowedPlaceholders.contains(nodeId))
        {
            nodeRef = lookupByPlaceholder(nodeId);
        }
        else
        {
            nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        }
        return nodeRef;
    }

    /**
     * Lookup node by placeholder
     *
     * @param nodeId
     * @return NodeRef for corresponding id
     */
    public NodeRef lookupByPlaceholder(String nodeId)
    {
        NodeRef nodeRef;
        if (nodeId.equals(FILE_PLAN_ALIAS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                nodeRef = filePlan;
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(TRANSFERS_ALIAS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                nodeRef = filePlanService.getTransferContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(UNFILED_ALIAS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                nodeRef = filePlanService.getUnfiledContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(HOLDS_ALIAS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                nodeRef = filePlanService.getHoldContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else
        {
            nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        }
        
        return nodeRef;
    }

    /**
     * TODO
     * @param parameters
     * @return
     */
    public List<Pair<QName, Boolean>> getSortProperties(Parameters parameters)
    {
        List<Pair<QName, Boolean>> sortProps = new ArrayList<>();
        sortProps.add(new Pair<>(GetChildrenCannedQuery.SORT_QNAME_NODE_TYPE, true));
        sortProps.add(new Pair<>(ContentModel.PROP_NAME, true));
        return sortProps;
    }

    /**
     * Write content to file
     *
     * @param nodeRef  the node to write the content to
     * @param fileName  the name of the file (used for guessing the file's mimetype)
     * @param stream  the input stream to write
     * @param guessEncoding  whether to guess stream encoding
     */
    public void writeContent(NodeRef nodeRef, String fileName, InputStream stream, boolean guessEncoding)
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
                    if (LOGGER.isWarnEnabled())
                    {
                        LOGGER.warn("Failed to reset stream after trying to guess encoding: " + ioe.getMessage());
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

    /**
     * Helper method that guesses the encoding of a stream of data
     * @param in  the stream to guess the encoding for
     * @param mimeType  the mimetype of the file
     * @param close  if true the stream will be closed at the end
     * @return the stream encoding
     */
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
                if (LOGGER.isWarnEnabled())
                {
                    LOGGER.warn("Failed to close stream after trying to guess encoding: " + ioe.getMessage());
                }
            }
        }
        return encoding;
    }

    /**
     * Helper method that creates a relative path if it doesn't already exist
     * The relative path will be build with nodes of the type specified in nodesType
     * If the relative path already exists the method validates if the last element is of type nodesType
     * The method does not validate the type of parentNodeRef
     *
     * @param parentNodeRef  the first node of the path
     * @param relativePath  a string representing the relative path in the format "Folder1/Folder2/Folder3"
     * @param nodesType  the type of all the containers in the path
     * @return  the last element of the relative path
     */
    public NodeRef lookupAndValidateRelativePath(final NodeRef parentNodeRef, String relativePath, QName nodesType)
    {
        return lookupAndValidateRelativePath(parentNodeRef, relativePath, false, nodesType);
    }

    /**
     * Helper method that creates a relative path if it doesn't already exist and if relative path is not read only.
     * If relative path is read only an exception will be thrown if the provided relative path does not exist.
     * The relative path will be build with nodes of the type specified in nodesType
     * If the relative path already exists the method validates if the last element is of type nodesType
     * The method does not validate the type of parentNodeRef
     *
     * @param parentNodeRef  the first node of the path
     * @param relativePath  a string representing the relative path in the format "Folder1/Folder2/Folder3"
     * @param readOnlyRelativePath the flag that indicates if the relativePath should be created if doesn't exist or not
     * @param nodesType  the type of all the containers in the path
     * @return  the last element of the relative path
     */
    public NodeRef lookupAndValidateRelativePath(final NodeRef parentNodeRef, String relativePath, boolean readOnlyRelativePath, QName nodesType)
    {
        mandatory("parentNodeRef", parentNodeRef);
        mandatory("nodesType", nodesType);
        if (StringUtils.isBlank(relativePath))
        {
            return parentNodeRef;
        }
        List<String> pathElements = getPathElements(relativePath);
        if (pathElements.isEmpty())
        {
            return parentNodeRef;
        }

        /*
         * Get the latest existing path element
         */
        NodeRef lastNodeRef = parentNodeRef;
        int i = 0;
        for (; i < pathElements.size(); i++)
        {
            final String pathElement = pathElements.get(i);
            final NodeRef contextParentNodeRef = lastNodeRef;
            // Navigation should not check permissions
            NodeRef child = authenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork() throws Exception
                {
                    return nodeService.getChildByName(contextParentNodeRef, ContentModel.ASSOC_CONTAINS, pathElement);
                }
            });

            if(child == null)
            {
                break;
            }
            lastNodeRef = child;
        }
        if(i == pathElements.size())
        {
            QName nodeType = nodeService.getType(lastNodeRef);
            if(!nodeType.equals(nodesType))
            {
                throw new InvalidArgumentException("The given id:'"+ parentNodeRef.getId() +"' and the relative path '"+ relativePath + "' reach a node type invalid for this endpoint."
                            + " Expected nodeType is:" + nodesType.toString() + ". Actual nodeType is:" + nodeType);
            }
            return lastNodeRef;
        }
        else
        {
            if(!readOnlyRelativePath)
            {
                pathElements = pathElements.subList(i, pathElements.size());
            }
            else
            {
                throw new NotFoundException("The entity with relativePath: " + relativePath + " was not found.");
            }
        }

        /*
         * Starting from the latest existing element create the rest of the elements
         */
        if(nodesType.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER))
        {
            for (String pathElement : pathElements)
            {
                lastNodeRef = fileFolderService.create(lastNodeRef, pathElement, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER).getNodeRef();
            }
        }
        else if(nodesType.equals(RecordsManagementModel.TYPE_RECORD_CATEGORY))
        {
            for (String pathElement : pathElements)
            {
                lastNodeRef = filePlanService.createRecordCategory(lastNodeRef, pathElement);
            }
        }
        else
        {
            // Throw internal error as this method should not be called for other types
            throw new InvalidArgumentException("Creating relative path of type '" + nodesType + "' not suported for this endpoint");
        }

        return lastNodeRef;
    }

    /**
     * Helper method that parses a string representing a file path and returns a list of element names
     * @param path the file path represented as a string
     * @return a list of file path element names
     */
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
    /**
     * Helper method that converts a map of String properties into a map of QName properties
     * @param properties
     * @return a map of properties
     */
    public Map<QName, Serializable> mapToNodeProperties(Map<String, Object> properties)
    {
        Map<QName, Serializable> response = null;
        if(properties != null)
        {
            response = nodes.mapToNodeProperties(properties);
        }
        return response;
    }

    /**
     * Create an RM node
     *
     * @param parentNodeRef  the parent of the node
     * @param nodeInfo  the node infos to create
     * @param parameters  the object to get the parameters passed into the request
     * @return the new node
     */
    public NodeRef createRMNode(NodeRef parentNodeRef, RMNode nodeInfo, Parameters parameters)
    {
        mandatory("parentNodeRef", parentNodeRef);
        mandatory("nodeInfo", nodeInfo);
        mandatory("parameters", parameters);

        String nodeName = nodeInfo.getName();
        String nodeType = nodeInfo.getNodeType();
        checkNotBlank(RMNode.PARAM_NAME, nodeName);
        checkNotBlank(RMNode.PARAM_NODE_TYPE, nodeType);

        // Create the node
        NodeRef newNodeRef = null;
        boolean autoRename = Boolean.valueOf(parameters.getParameter(RMNode.PARAM_AUTO_RENAME));

        try
        {
            QName typeQName = nodes.createQName(nodeType);

            // Existing file/folder name handling
            if (autoRename)
            {
                NodeRef existingNode = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, nodeName);
                if (existingNode != null)
                {
                    // File already exists, find a unique name
                    nodeName = findUniqueName(parentNodeRef, nodeName);
                }
            }
            newNodeRef = fileFolderService.create(parentNodeRef, nodeName, typeQName).getNodeRef();

            // Set the provided properties if any
            Map<QName, Serializable> qnameProperties = mapToNodeProperties(nodeInfo.getProperties());
            if (qnameProperties != null)
            {
                nodeService.addProperties(newNodeRef, qnameProperties);
            }

            // If electronic record create empty content
            if (!typeQName.equals(RecordsManagementModel.TYPE_NON_ELECTRONIC_DOCUMENT)
                    && dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
            {
                writeContent(newNodeRef, nodeName, new ByteArrayInputStream("".getBytes()), false);
            }

            // Add the provided aspects if any
            List<String> aspectNames = nodeInfo.getAspectNames();
            if (aspectNames != null)
            {
                nodes.addCustomAspects(newNodeRef, aspectNames, ApiNodesModelFactory.EXCLUDED_ASPECTS);
            }
        }
        catch (InvalidTypeException ex)
        {
            throw new InvalidArgumentException("The given type:'" + nodeType + "' is invalid '");
        }
        catch(DuplicateAttributeException ex)
        {
            // This exception can occur when setting a custom identifier that already exists
            throw new IntegrityException(ex.getMessage(), null);
        }

        return newNodeRef;
    }

    /**
     * Upload a record
     *
     * @param parentNodeRef  the parent of the record
     * @param uploadInfo  the infos of the uploaded record
     * @param parameters  the object to get the parameters passed into the request
     * @return the new record
     */
    public NodeRef uploadRecord(NodeRef parentNodeRef, UploadInfo uploadInfo, Parameters parameters)
    {
        mandatory("parentNodeRef", parentNodeRef);
        mandatory("uploadInfo", uploadInfo);
        mandatory("parameters", parameters);

        String nodeName = uploadInfo.getFileName();
        String nodeType = uploadInfo.getNodeType();
        InputStream stream = uploadInfo.getContent().getInputStream();
        mandatory("stream", stream);
        checkNotBlank(RMNode.PARAM_NAME, nodeName);

        // Create the node
        QName typeQName = StringUtils.isBlank(nodeType) ? ContentModel.TYPE_CONTENT : nodes.createQName(nodeType);
        if (!dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            throw new InvalidArgumentException("Can only upload type of cm:content: " + typeQName);
        }

        // Existing file/folder name handling
        boolean autoRename = Boolean.valueOf(parameters.getParameter(RMNode.PARAM_AUTO_RENAME));
        if (autoRename)
        {
            NodeRef existingNode = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, nodeName);
            if (existingNode != null)
            {
                // File already exists, find a unique name
                nodeName = findUniqueName(parentNodeRef, nodeName);
            }
        }

        NodeRef newNodeRef = fileFolderService.create(parentNodeRef, nodeName, typeQName).getNodeRef();

        // Write content
        writeContent(newNodeRef, nodeName, stream, true);

        // Set the provided properties if any
        Map<QName, Serializable> qnameProperties = mapToNodeProperties(uploadInfo.getProperties());
        if (qnameProperties != null)
        {
            nodeService.addProperties(newNodeRef, qnameProperties);
        }

        return newNodeRef;
    }

    /**
     * Returns a List of filter properties specified by request parameters.
     * @param parameters The {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - filter, sort &amp; paging params (where, orderBy, skipCount, maxItems)
     * @return The list of {@link FilterProp}. Can be null.
     */
    public List<FilterProp> getListChildrenFilterProps(Parameters parameters, Set<String> listFolderChildrenEqualsQueryProperties)
    {

        List<FilterProp> filterProps = null;
        Query q = parameters.getQuery();
        if (q != null)
        {
            MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(listFolderChildrenEqualsQueryProperties, null);
            QueryHelper.walk(q, propertyWalker);

            Boolean isPrimary = propertyWalker.getProperty(RMNode.PARAM_ISPRIMARY, WhereClauseParser.EQUALS, Boolean.class);

            if (isPrimary != null)
            {
                filterProps = new ArrayList<>(1);
                filterProps.add(new FilterPropBoolean(GetChildrenCannedQuery.FILTER_QNAME_NODE_IS_PRIMARY, isPrimary));
            }
            Boolean isClosed = propertyWalker.getProperty(RMNode.PARAM_IS_CLOSED, WhereClauseParser.EQUALS, Boolean.class);
            if (isClosed != null)
            {
                filterProps = new ArrayList<>(1);
                filterProps.add(new FilterPropBoolean(RecordsManagementModel.PROP_IS_CLOSED, isClosed));
            }
            //TODO see how we can filter for categories that have retention schedule
//            Boolean hasRetentionSchedule = propertyWalker.getProperty(RMNode.PARAM_HAS_RETENTION_SCHEDULE, WhereClauseParser.EQUALS, Boolean.class);
//            if (hasRetentionSchedule != null)
//            {
//                filterProps = new ArrayList<>(1);
//            }
        }
        return filterProps;
    }

    /**
     * Utility method that updates a node's name and properties
     * @param nodeRef  the node to update
     * @param updateInfo  information to update the record with
     * @param parameters  request parameters
     */
    public void updateNode(NodeRef nodeRef, RMNode updateInfo, Parameters parameters)
    {
        Map<QName, Serializable> props = new HashMap<>(0);

        if (updateInfo.getProperties() != null)
        {
            props = mapToNodeProperties(updateInfo.getProperties());
        }

        String name = updateInfo.getName();
        if ((name != null) && (!name.isEmpty()))
        {
            // update node name if needed
            props.put(ContentModel.PROP_NAME, name);
        }

        try
        {
            // update node properties - note: null will unset the specified property
            nodeService.addProperties(nodeRef, props);
        }
        catch (DuplicateChildNodeNameException dcne)
        {
            throw new ConstraintViolatedException(dcne.getMessage());
        }

        // update aspects
        List<String> aspectNames = updateInfo.getAspectNames();
        nodes.updateCustomAspects(nodeRef, aspectNames, ApiNodesModelFactory.EXCLUDED_ASPECTS);
    }

    /**
     * Validates a record
     *
     * @param recordId  the id of the record to validate
     * @return
     */
    public NodeRef validateRecord(String recordId) throws InvalidArgumentException
    {
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, recordId);
        if(!recordService.isRecord(nodeRef))
        {
            throw new IllegalArgumentException("The given id:'"+ recordId +"' is not valid for this endpoint. This endpoint only supports records.");
        }
        return nodeRef;
    }

    public BinaryResource getContent(NodeRef nodeRef, Parameters parameters, boolean recordActivity)
    {
        return nodes.getContent(nodeRef, parameters, recordActivity);
    }

    /**
     * Utility method that updates a transfer container's name and properties
     *
     * @param nodeRef  the node to update
     * @param transferContainerInfo  information to update the transfer container with
     * @param parameters  request parameters
     */
    public void updateTransferContainer(NodeRef nodeRef, TransferContainer transferContainerInfo, Parameters parameters)
    {
        Map<QName, Serializable> props = new HashMap<>(0);

        if (transferContainerInfo.getProperties() != null)
        {
            props = mapToNodeProperties(transferContainerInfo.getProperties());
        }

        String name = transferContainerInfo.getName();
        if ((name != null) && (!name.isEmpty()))
        {
            // update node name if needed
            props.put(ContentModel.PROP_NAME, name);
        }

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

    /**
     * Helper method that generates allowable operation for the provided node
     * @param nodeRef the node to get the allowable operations for
     * @param typeQName the type of the provided nodeRef
     * @return a sublist of [{@link Nodes#OP_DELETE}, {@link Nodes#OP_CREATE}, {@link Nodes#OP_UPDATE}] representing
     * the allowable operations for the provided node
     */
    protected List<String> getAllowableOperations(NodeRef nodeRef, QName typeQName)
    {
        List<String> allowableOperations = new ArrayList<>();

        boolean isFilePlan =  typeQName.equals(RecordsManagementModel.TYPE_FILE_PLAN);
        boolean isTransferContainer = typeQName.equals(RecordsManagementModel.TYPE_TRANSFER_CONTAINER);
        boolean isUnfiledContainer = typeQName.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
        boolean isHoldsContainer = typeQName.equals(RecordsManagementModel.TYPE_HOLD_CONTAINER);
        boolean isSpecialContainer = isFilePlan || isTransferContainer || isUnfiledContainer || isHoldsContainer;

        // DELETE
        if(!isSpecialContainer &&
                capabilityService.getCapability("Delete").evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            allowableOperations.add(Nodes.OP_DELETE);
        }

        // CREATE
        if(TYPES_CAN_CREATE.contains(typeQName) &&
                capabilityService.getCapability("FillingPermissionOnly").evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            allowableOperations.add(Nodes.OP_CREATE);
        }

        // UPDATE
        if (capabilityService.getCapability("Update").evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            allowableOperations.add(Nodes.OP_UPDATE);
        }

        return allowableOperations;
    }

    /**
     * Helper method to obtain file plan type or null if the rm site does not exist.
     *
     * @return file plan type or null
     */
    public QName getFilePlanType()
    {
        NodeRef filePlanNodeRef = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        if(filePlanNodeRef != null)
        {
            return nodeService.getType(filePlanNodeRef);
        }
        return null;
    }
    /**
     * Posts activities for given fileInfo
     * 
     * @param fileInfo
     * @param parentNodeRef
     * @param activityType
     */
    public void postActivity(FileInfo fileInfo, NodeRef parentNodeRef, String activityType)
    {
        ActivityInfo activityInfo = null;
        RMSite rmSite = sites.getRMSite(RM_SITE_ID);
        if (rmSite != null && !rmSite.getId().equals(""))
        {
            if (fileInfo != null)
            {
                boolean isContent = dictionaryService.isSubClass(fileInfo.getType(), ContentModel.TYPE_CONTENT);

                if (isContent)
                {
                    activityInfo = new ActivityInfo(null, parentNodeRef, RM_SITE_ID, fileInfo);
                }
            }
        }
        else
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Non-site activity, so ignored " + fileInfo.getNodeRef());
            }
        }

        if (activityInfo == null)
            return; // Nothing to do.

        if (activityType != null)
        {
            activityPoster.postFileFolderActivity(activityType, null, TenantUtil.getCurrentDomain(), activityInfo.getSiteId(),
                    activityInfo.getParentNodeRef(), activityInfo.getNodeRef(), activityInfo.getFileName(), Activities.APP_TOOL,
                    Activities.RESTAPI_CLIENT, activityInfo.getFileInfo());
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
}
