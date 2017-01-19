/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.impl.NodesImpl;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.RMNodes;
import org.alfresco.rm.rest.api.model.FileplanComponentNode;
import org.alfresco.rm.rest.api.model.RecordCategoryNode;
import org.alfresco.rm.rest.api.model.RecordFolderNode;
import org.alfresco.rm.rest.api.model.RecordNode;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.servlet.FormData;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Centralizes access to the repository.
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public class RMNodesImpl extends NodesImpl implements RMNodes
{
    private enum RMNodeType
    {
        // Note: ordered
        CATEGORY, RECORD_FOLDER, FILE
    }

    private FilePlanService filePlanService;
    private NodeService nodeService;
    private RecordsManagementServiceRegistry serviceRegistry;
    private DictionaryService dictionaryService;
    private DispositionService dispositionService;
    private CapabilityService capabilityService;
    private FileFolderService fileFolderService;

    public void init()
    {
        super.init();
        this.nodeService = serviceRegistry.getNodeService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.dispositionService = serviceRegistry.getDispositionService();
    }

    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    @Override
    public Node getFolderOrDocument(final NodeRef nodeRef, NodeRef parentNodeRef, QName nodeTypeQName, List<String> includeParam, Map<String, UserInfo> mapUserInfo)
    {
        Node originalNode = super.getFolderOrDocument(nodeRef, parentNodeRef, nodeTypeQName, includeParam, mapUserInfo);

        if(nodeTypeQName == null)
        {
            nodeTypeQName = nodeService.getType(nodeRef);
        }

        RMNodeType type = getType(nodeTypeQName, nodeRef);
        FileplanComponentNode node = null;
        if (mapUserInfo == null)
        {
            mapUserInfo = new HashMap<>(2);
        }

        if (type == null)
        {
            if (filePlanService.isFilePlanComponent(nodeRef))
            {
                node = new FileplanComponentNode(originalNode);
            }
            else
            {
                throw new InvalidParameterException("The provided node is not a fileplan component");
            }
        }
        else
        {
            switch(type)
            {
                case CATEGORY:
                    RecordCategoryNode categoryNode = new RecordCategoryNode(originalNode);
                    if (includeParam.contains(PARAM_INCLUDE_HAS_RETENTION_SCHEDULE))
                    {
                        DispositionSchedule ds = dispositionService.getDispositionSchedule(nodeRef);
                        categoryNode.setHasRetentionSchedule(ds!=null?true:false);
                    }
                    node = categoryNode;
                    break;
                case RECORD_FOLDER:
                    RecordFolderNode rfNode = new RecordFolderNode(originalNode);
                    if (includeParam.contains(PARAM_INCLUDE_IS_CLOSED))
                    {
                        rfNode.setIsClosed((Boolean) nodeService.getProperty(nodeRef, RecordsManagementModel.PROP_IS_CLOSED));
                    }
                    node = rfNode;
                    break;
                case FILE:
                    RecordNode rNode = new RecordNode(originalNode);
                    if (includeParam.contains(PARAM_INCLUDE_IS_COMPLETED))
                    {
                        rNode.setIsCompleted(nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_DECLARED_RECORD));
                    }
                    node = rNode;
                    break;
            }
        }

        if (includeParam.contains(PARAM_INCLUDE_ALLOWABLEOPERATIONS))
        {
            // If the user does not have any of the mapped permissions then "allowableOperations" is not returned (rather than an empty array)
            List<String> allowableOperations = getAllowableOperations(nodeRef, type);
            node.setAllowableOperations((allowableOperations.size() > 0 )? allowableOperations : null);
        }

        return node;
    }

    /**
     * Helper method that generates allowable operation for the provided node
     * @param nodeRef the node to get the allowable operations for
     * @param type the type of the provided nodeRef
     * @return a sublist of [{@link Nodes.OP_DELETE}, {@link Nodes.OP_CREATE}, {@link Nodes.OP_UPDATE}] representing the allowable operations for the provided node
     */
    private List<String> getAllowableOperations(NodeRef nodeRef, RMNodeType type)
    {
        List<String> allowableOperations = new ArrayList<>();

        NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        boolean isFilePlan = nodeRef.equals(filePlan);
        boolean isTransferContainer = nodeRef.equals(filePlanService.getTransferContainer(filePlan));
        boolean isUnfiledContainer = nodeRef.equals(filePlanService.getUnfiledContainer(filePlan));
        boolean isHoldsContainer = nodeRef.equals(filePlanService.getHoldContainer(filePlan)) ;
        boolean isSpecialContainer = isFilePlan || isTransferContainer || isUnfiledContainer || isHoldsContainer;

        // DELETE
        if(!isSpecialContainer &&
                capabilityService.getCapability("Delete").evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            allowableOperations.add(OP_DELETE);
        }

        // CREATE
        if(type != RMNodeType.FILE &&
                !isTransferContainer &&
                capabilityService.getCapability("FillingPermissionOnly").evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            allowableOperations.add(OP_CREATE);
        }

        // UPDATE
        if (capabilityService.getCapability("Update").evaluate(nodeRef) == AccessDecisionVoter.ACCESS_GRANTED)
        {
            allowableOperations.add(OP_UPDATE);
        }

        return allowableOperations;
    }

    @Override
    public NodeRef validateNode(String nodeId)
    {
        ParameterCheck.mandatoryString("nodeId", nodeId);

        if (nodeId.equals(PATH_FILE_PLAN))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                return filePlan;
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(PATH_TRANSFERS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                return filePlanService.getTransferContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(PATH_UNFILED))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                return filePlanService.getUnfiledContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }
        else if (nodeId.equals(PATH_HOLDS))
        {
            NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (filePlan != null)
            {
                return filePlanService.getHoldContainer(filePlan);
            }
            else
            {
                throw new EntityNotFoundException(nodeId);
            }
        }

        return super.validateNode(nodeId);
    }

    private RMNodeType getType(QName typeQName, NodeRef nodeRef)
    {
        // quick check for common types
        if (typeQName.equals(RecordsManagementModel.TYPE_RECORD_FOLDER))
        {
            return RMNodeType.RECORD_FOLDER;
        }
        if (typeQName.equals(RecordsManagementModel.TYPE_RECORD_CATEGORY))
        {
            return RMNodeType.CATEGORY;
        }
        if (typeQName.equals(ContentModel.TYPE_CONTENT))
        {
            return RMNodeType.FILE;
        }

        // check subclasses
        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            return RMNodeType.FILE;
        }
        if (dictionaryService.isSubClass(typeQName, RecordsManagementModel.TYPE_RECORD_FOLDER))
        {
            return RMNodeType.RECORD_FOLDER;
        }

        return null;
    }

    @Override
    protected Pair<Set<QName>, Set<QName>> buildSearchTypesAndIgnoreAspects(QName nodeTypeQName, boolean includeSubTypes, Set<QName> ignoreQNameTypes, Boolean includeFiles, Boolean includeFolders)
    {
        Pair<Set<QName>, Set<QName>> searchTypesAndIgnoreAspects = super.buildSearchTypesAndIgnoreAspects(nodeTypeQName, includeSubTypes, ignoreQNameTypes, includeFiles, includeFolders);
        Set<QName> searchTypeQNames = searchTypesAndIgnoreAspects.getFirst();
        Set<QName> ignoreAspectQNames = searchTypesAndIgnoreAspects.getSecond();

        searchTypeQNames.remove(RecordsManagementModel.TYPE_HOLD_CONTAINER);
        searchTypeQNames.remove(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER);
        searchTypeQNames.remove(RecordsManagementModel.TYPE_TRANSFER_CONTAINER);

        searchTypeQNames.remove(RecordsManagementModel.TYPE_DISPOSITION_SCHEDULE);
        searchTypeQNames.remove(RecordsManagementModel.TYPE_DISPOSITION_ACTION);
        searchTypeQNames.remove(RecordsManagementModel.TYPE_DISPOSITION_ACTION_DEFINITION);

        return new Pair<>(searchTypeQNames, ignoreAspectQNames);
    }

    @Override
    public Node createNode(String parentFolderNodeId, Node nodeInfo, Parameters parameters)
    {
        // create RM path if needed and call the super method with the last element of the created path
        String relativePath = nodeInfo.getRelativePath();

        // Get the type of the node to be created
        String nodeType = nodeInfo.getNodeType();
        if ((nodeType == null) || nodeType.isEmpty())
        {
            throw new InvalidArgumentException("Node type is expected: "+parentFolderNodeId+","+nodeInfo.getName());
        }
        QName nodeTypeQName = createQName(nodeType);

        // Get or create the path
        NodeRef parentNodeRef = getOrCreatePath(parentFolderNodeId, relativePath, nodeTypeQName);

        // Set relative path to null as we pass the last element from the path
        nodeInfo.setRelativePath(null);

        return super.createNode(parentNodeRef.getId(), nodeInfo, parameters);
    }

    @Override
    public Node upload(String parentFolderNodeId, FormData formData, Parameters parameters)
    {
        if (formData == null || !formData.getIsMultiPart())
        {
            throw new InvalidArgumentException("The request content-type is not multipart: "+parentFolderNodeId);
        }

        for (FormData.FormField field : formData.getFields())
        {
            if(field.getName().equalsIgnoreCase("relativepath"))
            {
                // Create the path if it does not exist
                getOrCreatePath(parentFolderNodeId, getStringOrNull(field.getValue()), ContentModel.TYPE_CONTENT);
                break;
            }
        }

        return super.upload(parentFolderNodeId, formData, parameters);
    }

    private String getStringOrNull(String value)
    {
        if (StringUtils.isNotEmpty(value))
        {
            return value.equalsIgnoreCase("null") ? null : value;
        }
        return null;
    }

    @Override
    public NodeRef getOrCreatePath(String parentFolderNodeId, String relativePath, QName nodeTypeQName)
    {
        NodeRef parentNodeRef = validateOrLookupNode(parentFolderNodeId, null);

        if (relativePath == null)
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
        int i = 0;
        for (; i < pathElements.size(); i++)
        {
            final String pathElement = pathElements.get(i);
            final NodeRef contextParentNodeRef = parentNodeRef;
            // Navigation should not check permissions
            NodeRef child = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
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
            parentNodeRef = child;
        }
        if(i == pathElements.size())
        {
            return parentNodeRef;
        }
        else
        {
            pathElements = pathElements.subList(i, pathElements.size());
        }

        /*
         * Starting from the latest existing element create the rest of the elements
         */
        QName parentNodeType = nodeService.getType(parentNodeRef);
        if(dictionaryService.isSubClass(parentNodeType, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER) ||
           dictionaryService.isSubClass(parentNodeType, RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER))
        {
            for (String pathElement : pathElements)
            {
                // Create unfiled record folder
                parentNodeRef = fileFolderService.create(parentNodeRef, pathElement, RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER).getNodeRef();
            }
        }
        else
        {
            /* Outside the unfiled record container the path elements are record categories
             * except the last element which is a record folder if the created node is of type content
             */
            Iterator<String> iterator = pathElements.iterator();
            while(iterator.hasNext())
            {
                String pathElement = iterator.next();

                if(!iterator.hasNext() && dictionaryService.isSubClass(nodeTypeQName, ContentModel.TYPE_CONTENT))
                {
                    // last element, create record folder if the node to be created is content
                    parentNodeRef = fileFolderService.create(parentNodeRef, pathElement, RecordsManagementModel.TYPE_RECORD_FOLDER).getNodeRef();
                }
                else
                {
                    // create record category
                    parentNodeRef = filePlanService.createRecordCategory(parentNodeRef, pathElement);
                }
            }
        }

        return parentNodeRef;
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

}
