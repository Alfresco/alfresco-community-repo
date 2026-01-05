/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.Group;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinitionImpl;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.AssocChild;
import org.alfresco.rest.api.model.ContentInfo;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.CapabilityModel;
import org.alfresco.rm.rest.api.model.FilePlan;
import org.alfresco.rm.rest.api.model.GroupModel;
import org.alfresco.rm.rest.api.model.HoldModel;
import org.alfresco.rm.rest.api.model.RMNode;
import org.alfresco.rm.rest.api.model.Record;
import org.alfresco.rm.rest.api.model.RecordCategory;
import org.alfresco.rm.rest.api.model.RecordCategoryChild;
import org.alfresco.rm.rest.api.model.RecordFolder;
import org.alfresco.rm.rest.api.model.RetentionPeriod;
import org.alfresco.rm.rest.api.model.RetentionSchedule;
import org.alfresco.rm.rest.api.model.RetentionScheduleActionDefinition;
import org.alfresco.rm.rest.api.model.RetentionSteps;
import org.alfresco.rm.rest.api.model.RoleModel;
import org.alfresco.rm.rest.api.model.Transfer;
import org.alfresco.rm.rest.api.model.TransferChild;
import org.alfresco.rm.rest.api.model.TransferContainer;
import org.alfresco.rm.rest.api.model.UnfiledChild;
import org.alfresco.rm.rest.api.model.UnfiledContainer;
import org.alfresco.rm.rest.api.model.UnfiledContainerChild;
import org.alfresco.rm.rest.api.model.UnfiledRecordFolder;
import org.alfresco.rm.rest.api.model.UnfiledRecordFolderChild;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Utility class containing Alfresco and RM java services required by the API endpoints
 *
 * @author Ana Bozianu
 * @since 2.6
 */
public class ApiNodesModelFactory
{

    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiNodesModelFactory.class);

    // excluded namespaces (aspects, properties, assoc types)
    public static final List<String> EXCLUDED_NS = Arrays.asList(NamespaceService.SYSTEM_MODEL_1_0_URI);

    // excluded aspects
    public static final List<QName> EXCLUDED_ASPECTS = Arrays.asList();

    // excluded properties
    public static final List<QName> EXCLUDED_PROPS = Arrays.asList(
            // top-level minimal info
            ContentModel.PROP_NAME, ContentModel.PROP_MODIFIER, ContentModel.PROP_MODIFIED, ContentModel.PROP_CREATOR,
            ContentModel.PROP_CREATED, ContentModel.PROP_CONTENT,
            // other - TBC
            ContentModel.PROP_INITIAL_VERSION, ContentModel.PROP_AUTO_VERSION_PROPS, ContentModel.PROP_AUTO_VERSION);

    private NodeService nodeService;
    private NamespaceService namespaceService;
    private Nodes nodes;
    private FilePlanComponentsApiUtils apiUtils;
    private PersonService personService;
    private DispositionService dispositionService;
    private ServiceRegistry serviceRegistry;
    private RecordsManagementServiceRegistry services;

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public NamespaceService getNamespaceService()
    {
        return namespaceService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public DispositionService getDispositionService()
    {
        return dispositionService;
    }

    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry services)
    {
        this.services = services;
    }

    /**
     * Helper method that sets the basic information for most of the node types.
     *
     * @param rmNode
     * @param info
     * @param propertyFilter
     * @param isMinimalInfo
     */
    private void mapBasicInfo(RMNode rmNode, FileInfo info, BeanPropertiesFilter propertyFilter, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        if (propertyFilter.isAllowed(RMNode.PARAM_ID))
        {
            rmNode.setNodeRef(info.getNodeRef());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_PARENT_ID))
        {
            rmNode.setParentId(nodeService.getPrimaryParent(info.getNodeRef()).getParentRef());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_NAME))
        {
            rmNode.setName(info.getName());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_NODE_TYPE))
        {
            rmNode.setNodeType(info.getType().toPrefixString(namespaceService));
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_MODIFIED_AT))
        {
            rmNode.setModifiedAt(info.getModifiedDate());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_MODIFIED_BY_USER))
        {
            if (mapUserInfo == null)
            {
                mapUserInfo = new HashMap<>(2);
            }
            UserInfo modifer = Node.lookupUserInfo((String) info.getProperties().get(ContentModel.PROP_MODIFIER), mapUserInfo,
                    personService);
            rmNode.setModifiedByUser(modifer);
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_CREATED_AT))
        {
            rmNode.setCreatedAt(info.getCreatedDate());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_CREATED_BY_USER))
        {
            if (mapUserInfo == null)
            {
                mapUserInfo = new HashMap<>(2);
            }
            UserInfo creator = Node.lookupUserInfo((String) info.getProperties().get(ContentModel.PROP_CREATOR), mapUserInfo,
                    personService);
            rmNode.setCreatedByUser(creator);
        }
        if (!isMinimalInfo && propertyFilter.isAllowed(RMNode.PARAM_ASPECT_NAMES))
        {
            rmNode.setAspectNames(mapFromNodeAspects(nodeService.getAspects(info.getNodeRef())));
        }
        if (!isMinimalInfo && propertyFilter.isAllowed(RMNode.PARAM_PROPERTIES))
        {
            rmNode.setProperties(mapFromNodeProperties(info.getProperties()));
        }
    }

    /**
     * Helper method that sets the optional information for most of the node types.
     *
     * @param rmNode
     * @param info
     * @param includeParam
     * @param isMinimalInfo
     */
    private void mapOptionalInfo(RMNode rmNode, FileInfo info, List<String> includeParam, boolean isMinimalInfo)
    {
        if (includeParam == null || includeParam.isEmpty())
        {
            return;
        }
        if (includeParam.contains(RMNode.PARAM_ALLOWABLE_OPERATIONS))
        {
            rmNode.setAllowableOperations(apiUtils.getAllowableOperations(info.getNodeRef(), info.getType()));
        }
        if (includeParam.contains(RMNode.PARAM_PATH))
        {
            rmNode.setPath(nodes.lookupPathInfo(info.getNodeRef(), null));
        }
        if (isMinimalInfo && includeParam.contains(RMNode.PARAM_ASPECT_NAMES))
        {
            rmNode.setAspectNames(mapFromNodeAspects(nodeService.getAspects(info.getNodeRef())));
        }
        if (isMinimalInfo && includeParam.contains(RMNode.PARAM_PROPERTIES))
        {
            rmNode.setProperties(mapFromNodeProperties(info.getProperties()));
        }
    }

    /**
     * Helper method that sets the information for unfiled child type.
     *
     * @param unfiledChild
     * @param info
     * @param propertyFilter
     */
    private void mapUnfiledChildInfo(UnfiledChild unfiledChild, FileInfo info, BeanPropertiesFilter propertyFilter)
    {
        if (RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER.equals(info.getType()))
        {
            if (propertyFilter.isAllowed(UnfiledChild.PARAM_IS_UNFILED_RECORD_FOLDER))
            {
                unfiledChild.setIsUnfiledRecordFolder(true);
            }
            if (propertyFilter.isAllowed(UnfiledChild.PARAM_IS_RECORD))
            {
                unfiledChild.setIsRecord(false);
            }
        }
        else
        {
            if (propertyFilter.isAllowed(UnfiledChild.PARAM_IS_UNFILED_RECORD_FOLDER))
            {
                unfiledChild.setIsUnfiledRecordFolder(false);
            }
            if (propertyFilter.isAllowed(UnfiledChild.PARAM_IS_RECORD))
            {
                unfiledChild.setIsRecord(true);
            }
        }
    }

    /**
     * Helper method that sets the information for transfer container type.
     *
     * @param transferContainer
     * @param info
     * @param propertyFilter
     */
    private void mapTransferContainerInfo(TransferContainer transferContainer, FileInfo info, Map<String, UserInfo> mapUserInfo, BeanPropertiesFilter propertyFilter, List<String> includeParam, boolean isMinimalInfo)
    {
        if (propertyFilter.isAllowed(RMNode.PARAM_ID))
        {
            transferContainer.setNodeRef(info.getNodeRef());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_PARENT_ID))
        {
            transferContainer.setParentId(nodeService.getPrimaryParent(info.getNodeRef()).getParentRef());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_NAME))
        {
            transferContainer.setName(info.getName());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_NODE_TYPE))
        {
            transferContainer.setNodeType(info.getType().toPrefixString(namespaceService));
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_MODIFIED_AT))
        {
            transferContainer.setModifiedAt(info.getModifiedDate());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_MODIFIED_BY_USER))
        {
            if (mapUserInfo == null)
            {
                mapUserInfo = new HashMap<>(2);
            }
            UserInfo modifer = Node.lookupUserInfo((String) info.getProperties().get(ContentModel.PROP_MODIFIER), mapUserInfo,
                    personService);
            transferContainer.setModifiedByUser(modifer);
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_CREATED_AT))
        {
            transferContainer.setCreatedAt(info.getCreatedDate());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_CREATED_BY_USER))
        {
            if (mapUserInfo == null)
            {
                mapUserInfo = new HashMap<>(2);
            }
            UserInfo creator = Node.lookupUserInfo((String) info.getProperties().get(ContentModel.PROP_CREATOR), mapUserInfo,
                    personService);
            transferContainer.setCreatedByUser(creator);
        }
        if (!isMinimalInfo && propertyFilter.isAllowed(RMNode.PARAM_ASPECT_NAMES))
        {
            transferContainer.setAspectNames(mapFromNodeAspects(nodeService.getAspects(info.getNodeRef())));
        }
        if (!isMinimalInfo && propertyFilter.isAllowed(RMNode.PARAM_PROPERTIES))
        {
            transferContainer.setProperties(mapFromNodeProperties(info.getProperties()));
        }

        // optional parameters
        if (includeParam == null || includeParam.isEmpty())
        {
            return;
        }
        if (includeParam.contains(RMNode.PARAM_ALLOWABLE_OPERATIONS))
        {
            transferContainer.setAllowableOperations(apiUtils.getAllowableOperations(info.getNodeRef(), info.getType()));
        }
        if (isMinimalInfo && includeParam.contains(RMNode.PARAM_ASPECT_NAMES))
        {
            transferContainer.setAspectNames(mapFromNodeAspects(nodeService.getAspects(info.getNodeRef())));
        }
        if (isMinimalInfo && includeParam.contains(RMNode.PARAM_PROPERTIES))
        {
            transferContainer.setProperties(mapFromNodeProperties(info.getProperties()));
        }
    }

    /**
     * Helper method that sets the information for transfer type.
     *
     * @param transfer
     * @param info
     * @param propertyFilter
     */
    private void mapTransferInfo(Transfer transfer, FileInfo info, Map<String, UserInfo> mapUserInfo, BeanPropertiesFilter propertyFilter, List<String> includeParam, boolean isMinimalInfo)
    {
        if (propertyFilter.isAllowed(RMNode.PARAM_ID))
        {
            transfer.setNodeRef(info.getNodeRef());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_PARENT_ID))
        {
            transfer.setParentId(nodeService.getPrimaryParent(info.getNodeRef()).getParentRef());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_NAME))
        {
            transfer.setName(info.getName());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_NODE_TYPE))
        {
            transfer.setNodeType(info.getType().toPrefixString(namespaceService));
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_CREATED_AT))
        {
            transfer.setCreatedAt(info.getCreatedDate());
        }
        if (propertyFilter.isAllowed(RMNode.PARAM_CREATED_BY_USER))
        {
            if (mapUserInfo == null)
            {
                mapUserInfo = new HashMap<>(2);
            }
            UserInfo creator = Node.lookupUserInfo((String) info.getProperties().get(ContentModel.PROP_CREATOR), mapUserInfo,
                    personService);
            transfer.setCreatedByUser(creator);
        }
        if (!isMinimalInfo && propertyFilter.isAllowed(RMNode.PARAM_ASPECT_NAMES))
        {
            transfer.setAspectNames(mapFromNodeAspects(nodeService.getAspects(info.getNodeRef())));
        }
        if (!isMinimalInfo && propertyFilter.isAllowed(RMNode.PARAM_PROPERTIES))
        {
            transfer.setProperties(mapFromNodeProperties(info.getProperties()));
        }

        // optional parameters
        if (isMinimalInfo && includeParam == null || includeParam.isEmpty())
        {
            return;
        }
        if (includeParam.contains(RMNode.PARAM_ALLOWABLE_OPERATIONS))
        {
            transfer.setAllowableOperations(apiUtils.getAllowableOperations(info.getNodeRef(), info.getType()));
        }
        if (isMinimalInfo && includeParam.contains(RMNode.PARAM_ASPECT_NAMES))
        {
            transfer.setAspectNames(mapFromNodeAspects(nodeService.getAspects(info.getNodeRef())));
        }
        if (isMinimalInfo && includeParam.contains(RMNode.PARAM_PROPERTIES))
        {
            transfer.setProperties(mapFromNodeProperties(info.getProperties()));
        }
        if ((!isMinimalInfo && propertyFilter.isAllowed(Transfer.PARAM_TRANSFER_ACCESSION_INDICATOR)) || (isMinimalInfo && includeParam.contains(Transfer.PARAM_TRANSFER_ACCESSION_INDICATOR)))
        {
            transfer.setTransferAccessionIndicator((Boolean) nodeService.getProperty(info.getNodeRef(), RecordsManagementModel.PROP_TRANSFER_ACCESSION_INDICATOR));
        }
        if ((!isMinimalInfo && propertyFilter.isAllowed(Transfer.PARAM_TRANSFER_LOCATION)) || (isMinimalInfo && includeParam.contains(Transfer.PARAM_TRANSFER_LOCATION)))
        {
            transfer.setTransferLocation((String) nodeService.getProperty(info.getNodeRef(), RecordsManagementModel.PROP_TRANSFER_LOCATION));
        }
        if ((!isMinimalInfo && propertyFilter.isAllowed(Transfer.PARAM_TRANSFER_PDF_INDICATOR)) || (isMinimalInfo && includeParam.contains(Transfer.PARAM_TRANSFER_PDF_INDICATOR)))
        {
            transfer.setTransferPDFIndicator((Boolean) nodeService.getProperty(info.getNodeRef(), RecordsManagementModel.PROP_TRANSFER_PDF_INDICATOR));
        }
    }

    /**
     * Helper method that sets the information for transfer child type.
     *
     * @param transferChild
     * @param info
     * @param propertyFilter
     */
    private void mapTransferChildInfo(TransferChild transferChild, FileInfo info, List<String> includeParam, boolean isMinimalInfo)
    {
        if (includeParam == null || includeParam.isEmpty())
        {
            return;
        }
        if (RecordsManagementModel.TYPE_RECORD_FOLDER.equals(info.getType()))
        {
            if (isMinimalInfo && includeParam.contains(TransferChild.PARAM_IS_RECORD_FOLDER))
            {
                transferChild.setIsRecordFolder(true);
            }
            if (isMinimalInfo && includeParam.contains(TransferChild.PARAM_IS_RECORD))
            {
                transferChild.setIsRecord(false);
            }
            if (isMinimalInfo && includeParam.contains(RMNode.PARAM_IS_CLOSED))
            {
                transferChild.setIsClosed((Boolean) nodeService.getProperty(info.getNodeRef(), RecordsManagementModel.PROP_IS_CLOSED));
            }
        }
        else
        {
            if (isMinimalInfo && includeParam.contains(TransferChild.PARAM_IS_RECORD_FOLDER))
            {
                transferChild.setIsRecordFolder(false);
            }
            if (isMinimalInfo && includeParam.contains(TransferChild.PARAM_IS_RECORD))
            {
                transferChild.setIsRecord(true);
            }
            if (isMinimalInfo && includeParam.contains(RMNode.PARAM_IS_CLOSED))
            {
                transferChild.setIsClosed(null);
            }
        }
    }

    /**
     * Helper method that sets the information for record category child type.
     *
     * @param recordCategoryChild
     *            the record category child to set the fields to
     * @param info
     *            info of the record category child
     * @param includeParam
     *            the requested include parameters
     * @param propertyFilter
     */
    private void mapRecordCategoryChildInfo(RecordCategoryChild recordCategoryChild, FileInfo info, List<String> includeParam, BeanPropertiesFilter propertyFilter, boolean isMinimalInfo)
    {
        if (isMinimalInfo && (includeParam == null || includeParam.isEmpty()))
        {
            return;
        }
        if (RecordsManagementModel.TYPE_RECORD_FOLDER.equals(info.getType()))
        {
            if (isRecordFolder(isMinimalInfo, propertyFilter, includeParam))
            {
                recordCategoryChild.setIsRecordFolder(true);
            }
            if (isRecordCategory(isMinimalInfo, propertyFilter, includeParam))
            {
                recordCategoryChild.setIsRecordCategory(false);
            }
            if (isRecordCategoryChildClosed(isMinimalInfo, propertyFilter, includeParam))
            {
                recordCategoryChild.setIsClosed((Boolean) nodeService.getProperty(info.getNodeRef(), RecordsManagementModel.PROP_IS_CLOSED));
            }
            if (includeParam.contains(RMNode.PARAM_HAS_RETENTION_SCHEDULE))
            {
                recordCategoryChild.setHasRetentionSchedule(null);
            }
        }
        else
        {
            if (isRecordFolder(isMinimalInfo, propertyFilter, includeParam))
            {
                recordCategoryChild.setIsRecordFolder(false);
            }
            if (isRecordCategory(isMinimalInfo, propertyFilter, includeParam))
            {
                recordCategoryChild.setIsRecordCategory(true);
            }
            if (includeParam.contains(RMNode.PARAM_HAS_RETENTION_SCHEDULE))
            {
                DispositionSchedule ds = dispositionService.getDispositionSchedule(info.getNodeRef());
                recordCategoryChild.setHasRetentionSchedule(ds != null);
            }
            if (isRecordCategoryChildClosed(isMinimalInfo, propertyFilter, includeParam))
            {
                recordCategoryChild.setIsClosed(null);
            }
        }
    }

    private boolean isRecordCategoryChildClosed(boolean isMinimalInfo, BeanPropertiesFilter propertyFilter, List<String> includeParam)
    {
        return (!isMinimalInfo && propertyFilter.isAllowed(RMNode.PARAM_IS_CLOSED)) || (isMinimalInfo && includeParam.contains(RMNode.PARAM_IS_CLOSED));
    }

    private boolean isRecordCategory(boolean isMinimalInfo, BeanPropertiesFilter propertyFilter, List<String> includeParam)
    {
        return (!isMinimalInfo && propertyFilter.isAllowed(RecordCategoryChild.PARAM_IS_RECORD_CATEGORY)) || (isMinimalInfo && includeParam.contains(RecordCategoryChild.PARAM_IS_RECORD_CATEGORY));
    }

    private boolean isRecordFolder(boolean isMinimalInfo, BeanPropertiesFilter propertyFilter, List<String> includeParam)
    {
        return (!isMinimalInfo && propertyFilter.isAllowed(RecordCategoryChild.PARAM_IS_RECORD_FOLDER)) || (isMinimalInfo && includeParam.contains(RecordCategoryChild.PARAM_IS_RECORD_FOLDER));
    }

    /**
     * Utility method that maps record specific fields
     *
     * @param record
     *            the record to set the fields to
     * @param info
     *            info of the record
     * @param includeParam
     *            the requested include parameters
     */
    private void mapRecordInfo(Record record, FileInfo info, List<String> includeParam)
    {
        if (includeParam == null || includeParam.isEmpty())
        {
            return;
        }
        if (includeParam.contains(Record.PARAM_IS_COMPLETED))
        {
            record.setIsCompleted(nodeService.hasAspect(info.getNodeRef(), RecordsManagementModel.ASPECT_DECLARED_RECORD));
        }
        if (includeParam.contains(Record.PARAM_CONTENT))
        {
            Serializable val = info.getProperties().get(ContentModel.PROP_CONTENT);

            if (val instanceof ContentData)
            {
                ContentData cd = (ContentData) val;
                String mimeType = cd.getMimetype();
                String mimeTypeName = serviceRegistry.getMimetypeService().getDisplaysByMimetype().get(mimeType);
                ContentInfo contentInfo = new ContentInfo(mimeType, mimeTypeName, cd.getSize(), cd.getEncoding());
                record.setContent(contentInfo);
            }
        }
    }

    /**
     * Helper method that converts a set of QName aspects into a list of String aspects
     *
     * @param properties
     */
    private List<String> mapFromNodeAspects(Set<QName> nodeAspects)
    {
        return nodes.mapFromNodeAspects(nodeAspects, EXCLUDED_NS, EXCLUDED_ASPECTS);
    }

    /**
     * Helper method that converts a map of QName properties into a map of String properties
     *
     * @param properties
     * @return a map of String properties
     */
    private Map<String, Object> mapFromNodeProperties(Map<QName, Serializable> properties)
    {
        return nodes.mapFromNodeProperties(properties, new ArrayList<>(), new HashMap<>(), EXCLUDED_NS, EXCLUDED_PROPS);
    }

    /**
     * Utility method that maps associations, applicable only for records
     * 
     * @param rmNode
     * @param info
     * @param includeParam
     */
    private void mapAssociations(RMNode rmNode, FileInfo info, List<String> includeParam)
    {
        if (includeParam.contains(RMNode.PARAM_INCLUDE_ASSOCIATION))
        {
            NodeRef nodeRef = info.getNodeRef();
            ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);

            if ((parentAssocRef == null) || (parentAssocRef.getParentRef() == null)
                    || (!parentAssocRef.getParentRef().equals(rmNode.getParentId())))
            {
                List<ChildAssociationRef> parentAssocRefs = nodeService.getParentAssocs(nodeRef);
                for (ChildAssociationRef pAssocRef : parentAssocRefs)
                {
                    if (pAssocRef.getParentRef().equals(rmNode.getParentId()))
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
                if ((assocTypeQName != null) && (!EXCLUDED_NS.contains(assocTypeQName.getNamespaceURI())))
                {
                    AssocChild childAssoc = new AssocChild(assocTypeQName.toPrefixString(namespaceService), parentAssocRef.isPrimary());

                    rmNode.setAssociation(childAssoc);
                }
            }
        }
    }

    /**
     * Creates an object of type HoldModel
     *
     * @param info
     *            info of the hold
     * @return HoldModel object
     */
    public HoldModel createHoldModel(FileInfo info)
    {
        return new HoldModel(info.getNodeRef().getId(),
                (String) info.getProperties().get(ContentModel.PROP_NAME),
                (String) info.getProperties().get(ContentModel.PROP_DESCRIPTION),
                (String) info.getProperties().get(RecordsManagementModel.PROP_HOLD_REASON));
    }

    public RoleModel createRoleModel(Role role, List<String> assignedUsers, List<String> assignedGroups)
    {
        return new RoleModel(role.getName(),
                role.getDisplayLabel(),
                role.getCapabilities()
                        .stream()
                        .map(this::createCapabilityModel)
                        .sorted(Comparator.comparing(CapabilityModel::name))
                        .toList(),
                role.getRoleGroupName(),
                role.getGroupShortName(),
                assignedUsers,
                assignedGroups);
    }

    public CapabilityModel createCapabilityModel(Capability capability)
    {
        return new CapabilityModel(capability.getName(), capability.getTitle(), capability.getDescription(),
                createGroupModel(capability.getGroup()), capability.getIndex());
    }

    public GroupModel createGroupModel(Group group)
    {
        if (group == null)
        {
            return null;
        }
        return new GroupModel(group.getId(), group.getTitle());
    }

    /**
     * Creates an object of type FilePlan
     *
     * @param info
     *            info of the file plan
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return FilePlan object
     */
    public FilePlan createFilePlan(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo, boolean isMinimalInfo)
    {
        FilePlan filePlan = new FilePlan();
        mapBasicInfo(filePlan, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(filePlan, info, parameters.getInclude(), isMinimalInfo);
        return filePlan;
    }

    /**
     * Creates an object of type RecordCategory
     *
     * @param info
     *            info of the record category
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return RecordCategory object
     */
    public RecordCategory createRecordCategory(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        RecordCategory recordCategory = new RecordCategory();
        mapBasicInfo(recordCategory, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(recordCategory, info, parameters.getInclude(), isMinimalInfo);

        if (parameters.getInclude().contains(RMNode.PARAM_HAS_RETENTION_SCHEDULE))
        {
            DispositionSchedule ds = dispositionService.getDispositionSchedule(info.getNodeRef());
            recordCategory.setHasRetentionSchedule(ds != null);
        }

        return recordCategory;
    }

    /**
     * Creates an object of type RecordCategory
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return RecordCategory object
     */
    public RecordFolder createRecordFolder(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        RecordFolder recordFolder = new RecordFolder();
        mapBasicInfo(recordFolder, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(recordFolder, info, parameters.getInclude(), isMinimalInfo);

        if (parameters.getInclude().contains(RMNode.PARAM_IS_CLOSED))
        {
            recordFolder.setIsClosed((Boolean) nodeService.getProperty(info.getNodeRef(), RecordsManagementModel.PROP_IS_CLOSED));
        }

        return recordFolder;
    }

    /**
     * Creates an object of type UnfiledContainer
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return UnfiledContainer object
     */
    public UnfiledContainer createUnfiledContainer(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        UnfiledContainer unfiledContainer = new UnfiledContainer();
        mapBasicInfo(unfiledContainer, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(unfiledContainer, info, parameters.getInclude(), isMinimalInfo);
        return unfiledContainer;
    }

    /**
     * Creates an object of type TransferContainer
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return UnfiledContainer object
     */
    public TransferContainer createTransferContainer(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        TransferContainer transferContainer = new TransferContainer();
        mapTransferContainerInfo(transferContainer, info, mapUserInfo, parameters.getFilter(), parameters.getInclude(), isMinimalInfo);
        return transferContainer;
    }

    /**
     * Creates an object of type Transfer
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return UnfiledContainer object
     */
    public Transfer createTransfer(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        Transfer transfer = new Transfer();
        mapTransferInfo(transfer, info, mapUserInfo, parameters.getFilter(), parameters.getInclude(), isMinimalInfo);
        return transfer;
    }

    /**
     * Creates an object of type TransferChild
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return UnfiledContainer object
     */
    public TransferChild createTransferChild(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        TransferChild transferChild = new TransferChild();
        mapBasicInfo(transferChild, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(transferChild, info, parameters.getInclude(), isMinimalInfo);
        mapTransferChildInfo(transferChild, info, parameters.getInclude(), isMinimalInfo);
        return transferChild;
    }

    /**
     * Creates an object of type UnfiledContainerChild
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return UnfiledContainerChild object
     */
    public UnfiledContainerChild createUnfiledContainerChild(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        UnfiledContainerChild unfiledContainerChild = new UnfiledContainerChild();
        mapBasicInfo(unfiledContainerChild, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(unfiledContainerChild, info, parameters.getInclude(), isMinimalInfo);
        mapUnfiledChildInfo(unfiledContainerChild, info, parameters.getFilter());
        if (unfiledContainerChild.getIsRecord())
        {
            mapAssociations(unfiledContainerChild, info, parameters.getInclude());
        }
        return unfiledContainerChild;
    }

    /**
     * Creates an object of type UnfiledRecordFolder
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return UnfiledRecordFolder object
     */
    public UnfiledRecordFolder createUnfiledRecordFolder(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        UnfiledRecordFolder unfiledChild = new UnfiledRecordFolder();
        mapBasicInfo(unfiledChild, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(unfiledChild, info, parameters.getInclude(), isMinimalInfo);
        return unfiledChild;
    }

    /**
     * Creates an object of type UnfiledRecordFolderChild
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return UnfiledRecordFolderChild object
     */
    public UnfiledRecordFolderChild createUnfiledRecordFolderChild(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        UnfiledRecordFolderChild unfiledRecordFolderChild = new UnfiledRecordFolderChild();
        mapBasicInfo(unfiledRecordFolderChild, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(unfiledRecordFolderChild, info, parameters.getInclude(), isMinimalInfo);
        mapUnfiledChildInfo(unfiledRecordFolderChild, info, parameters.getFilter());
        if (unfiledRecordFolderChild.getIsRecord())
        {
            mapAssociations(unfiledRecordFolderChild, info, parameters.getInclude());
        }
        return unfiledRecordFolderChild;
    }

    /**
     * Creates an object of type RecordCategoryChild
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return
     */
    public RecordCategoryChild createRecordCategoryChild(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo,
            boolean isMinimalInfo)
    {
        RecordCategoryChild recordCategoryChild = new RecordCategoryChild();
        mapBasicInfo(recordCategoryChild, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(recordCategoryChild, info, parameters.getInclude(), isMinimalInfo);
        mapRecordCategoryChildInfo(recordCategoryChild, info, parameters.getInclude(), parameters.getFilter(), isMinimalInfo);
        return recordCategoryChild;
    }

    /**
     * Create an object of type Record
     *
     * @param info
     * @param parameters
     * @param mapUserInfo
     * @param isMinimalInfo
     * @return
     */
    public Record createRecord(FileInfo info, Parameters parameters, Map<String, UserInfo> mapUserInfo, boolean isMinimalInfo)
    {
        Record record = new Record();
        mapBasicInfo(record, info, parameters.getFilter(), mapUserInfo, isMinimalInfo);
        mapOptionalInfo(record, info, parameters.getInclude(), isMinimalInfo);
        mapRecordInfo(record, info, parameters.getInclude());
        mapAssociations(record, info, parameters.getInclude());
        return record;
    }

    /**
     * Helper method that sets the information for the retention schedule type.
     * 
     * @param dispositionSchedule
     * @return RetentionSchedule
     */
    public RetentionSchedule mapRetentionScheduleData(DispositionSchedule dispositionSchedule)
    {
        RetentionSchedule retentionSchedule = new RetentionSchedule();
        retentionSchedule.setId(dispositionSchedule.getNodeRef().getId());
        if (dispositionSchedule.getNodeRef() != null)
        {
            NodeRef parent = this.nodeService.getPrimaryParent(dispositionSchedule.getNodeRef()).getParentRef();
            retentionSchedule.setParentId(parent.getId());
        }
        retentionSchedule.setInstructions(dispositionSchedule.getDispositionInstructions());
        retentionSchedule.setAuthority(dispositionSchedule.getDispositionAuthority());
        retentionSchedule.setIsRecordLevel(dispositionSchedule.isRecordLevelDisposition());

        boolean unpublishedUpdates = dispositionSchedule.getDispositionActionDefinitions().stream()
                .map(DispositionActionDefinition::getNodeRef)
                .anyMatch(actionDefNodeRef -> nodeService.hasAspect(actionDefNodeRef, RecordsManagementModel.ASPECT_UNPUBLISHED_UPDATE));
        retentionSchedule.setUnpublishedUpdates(unpublishedUpdates);
        return retentionSchedule;
    }

    /**
     * Helper method that sets the information for the retention schedule action definition type.
     * 
     * @param dispositionActionDefinition
     * @return RetentionScheduleActionDefinition
     */
    public RetentionScheduleActionDefinition mapRetentionScheduleActionDefData(DispositionActionDefinition dispositionActionDefinition)
    {
        RetentionScheduleActionDefinition retentionScheduleActionDefinition = new RetentionScheduleActionDefinition();
        // Mapping basic properties
        mapRetentionActionProperties(dispositionActionDefinition, retentionScheduleActionDefinition);
        // Mapping period and period amount
        mapPeriodProperties(dispositionActionDefinition, retentionScheduleActionDefinition);
        // Mapping events properties
        mapEventsProperties(dispositionActionDefinition, retentionScheduleActionDefinition);
        return retentionScheduleActionDefinition;
    }

    /**
     * Helper method that sets core information for the retention schedule action definition type.
     * 
     * @param dispositionActionDefinition
     * @param retentionScheduleActionDefinition
     */
    private void mapRetentionActionProperties(DispositionActionDefinition dispositionActionDefinition, RetentionScheduleActionDefinition retentionScheduleActionDefinition)
    {
        retentionScheduleActionDefinition.setId(dispositionActionDefinition.getId());
        retentionScheduleActionDefinition.setName(dispositionActionDefinition.getName());
        retentionScheduleActionDefinition.setDescription(dispositionActionDefinition.getDescription());
        retentionScheduleActionDefinition.setEligibleOnFirstCompleteEvent(dispositionActionDefinition.eligibleOnFirstCompleteEvent());
        if (nodeService.getProperty(dispositionActionDefinition.getNodeRef(), RecordsManagementModel.PROP_COMBINE_DISPOSITION_STEP_CONDITIONS) != null)
        {
            retentionScheduleActionDefinition.setCombineRetentionStepConditions((Boolean) nodeService.getProperty(dispositionActionDefinition.getNodeRef(), RecordsManagementModel.PROP_COMBINE_DISPOSITION_STEP_CONDITIONS));
        }
        retentionScheduleActionDefinition.setLocation(dispositionActionDefinition.getLocation());
        if (dispositionActionDefinition.getGhostOnDestroy() != null)
        {
            retentionScheduleActionDefinition.setRetainRecordMetadataAfterDestruction(dispositionActionDefinition.getGhostOnDestroy().equals("ghost"));
        }
        retentionScheduleActionDefinition.setIndex(dispositionActionDefinition.getIndex());
    }

    /**
     * Helper method that sets the period-related information for the retention schedule action definition type.
     * 
     * @param dispositionActionDefinition
     * @param retentionScheduleActionDefinition
     */
    private void mapPeriodProperties(DispositionActionDefinition dispositionActionDefinition, RetentionScheduleActionDefinition retentionScheduleActionDefinition)
    {
        if (dispositionActionDefinition.getPeriodProperty() != null)
        {
            retentionScheduleActionDefinition.setPeriodProperty(dispositionActionDefinition.getPeriodProperty().toPrefixString(namespaceService));
        }

        String period = dispositionActionDefinition.getPeriod().toString();
        if (!period.isEmpty())
        {
            // In rest api we are splitting `period` property into `period` and `periodAmount`.
            // so we need to split the period into two properties.
            // ex. period -> 'month|10' so the split properties would be like below
            // period -> 'month'
            // periodAmount -> 10
            String[] periodArray = period.split("\\|");

            if (periodArray.length > 0)
            {
                retentionScheduleActionDefinition.setPeriod(periodArray[0]);
            }

            if (periodArray.length > 1)
            {
                try
                {
                    retentionScheduleActionDefinition.setPeriodAmount(Integer.parseInt(periodArray[1]));
                }
                catch (NumberFormatException numberFormatException)
                {
                    LOGGER.error("Error parsing period amount: {}{}", numberFormatException.getMessage(), periodArray[1], numberFormatException);
                    throw numberFormatException;
                }
            }
        }
    }

    /**
     * Helper method that sets the events information for the retention schedule action definition type.
     * 
     * @param dispositionActionDefinition
     * @param retentionScheduleActionDefinition
     */
    private void mapEventsProperties(DispositionActionDefinition dispositionActionDefinition, RetentionScheduleActionDefinition retentionScheduleActionDefinition)
    {
        List<RecordsManagementEvent> events = dispositionActionDefinition.getEvents();
        if (events != null && !events.isEmpty())
        {
            List<String> eventNames = events.stream()
                    .map(RecordsManagementEvent::getName)
                    .collect(Collectors.toList());
            retentionScheduleActionDefinition.setEvents(eventNames);
        }
    }

    /**
     * Helper method that sets the optional information for the retention schedule type.
     * 
     * @param retentionSchedule
     * @param schedule
     * @param includeParam
     */
    public void mapRetentionScheduleOptionalInfo(RetentionSchedule retentionSchedule, DispositionSchedule schedule, List<String> includeParam)
    {
        if (includeParam != null && !includeParam.isEmpty() && includeParam.contains("actions"))
        {
            List<RetentionScheduleActionDefinition> actions = schedule.getDispositionActionDefinitions().stream()
                    .map(this::mapRetentionScheduleActionDefData)
                    .collect(Collectors.toList());
            retentionSchedule.setActions(actions);
        }
    }

    /**
     * this method is used for creation of retention schedule action definition params
     * 
     * @param nodeInfo
     *            retention schedule action definition
     * @return Map<QName, Serializable>
     */
    public Map<QName, Serializable> createRetentionActionDefinitionParams(RetentionScheduleActionDefinition nodeInfo)
    {
        Map<QName, Serializable> actionDefinitionParams = new HashMap<>();

        String retentionActionName = nodeInfo.getName();

        if (nodeInfo.getName().equals(RetentionSteps.DESTROY_NODE.stepName) ||
                nodeInfo.getName().equals(RetentionSteps.DESTROY_CONTENT.stepName))
        {
            retentionActionName = "destroy";
        }

        actionDefinitionParams.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, retentionActionName);
        actionDefinitionParams.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, nodeInfo.getDescription());
        StringBuilder retentionPeriod = new StringBuilder(nodeInfo.getPeriod()).append("|");

        if (isPeriodAmountApplicable(nodeInfo.getPeriod()))
        {
            retentionPeriod.append(nodeInfo.getPeriodAmount());
        }
        actionDefinitionParams.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, retentionPeriod.toString());
        QName periodProperty = QName.createQName(nodeInfo.getPeriodProperty(), namespaceService);
        actionDefinitionParams.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY, periodProperty);
        actionDefinitionParams.put(RecordsManagementModel.PROP_DISPOSITION_EVENT_COMBINATION,
                nodeInfo.isEligibleOnFirstCompleteEvent());
        boolean combineConditions = nodeInfo.getName().equals(RetentionSteps.ACCESSION.stepName) && nodeInfo.isCombineRetentionStepConditions();
        actionDefinitionParams.put(RecordsManagementModel.PROP_COMBINE_DISPOSITION_STEP_CONDITIONS, combineConditions);

        if (nodeInfo.getLocation() != null && nodeInfo.getName().equals(RetentionSteps.TRANSFER.stepName))
        {
            actionDefinitionParams.put(RecordsManagementModel.PROP_DISPOSITION_LOCATION,
                    nodeInfo.getLocation());
        }
        List<String> inputEvents = nodeInfo.getEvents();
        actionDefinitionParams.put(RecordsManagementModel.PROP_DISPOSITION_EVENT, (Serializable) inputEvents);

        if (RetentionSteps.DESTROY_CONTENT.stepName.equals(nodeInfo.getName()))
        {
            actionDefinitionParams.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_GHOST_ON_DESTROY, "ghost");
        }
        else if (RetentionSteps.DESTROY_NODE.stepName.equals(nodeInfo.getName()))
        {
            actionDefinitionParams.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_GHOST_ON_DESTROY, "delete");
        }
        return actionDefinitionParams;
    }

    /**
     * this method is used retrieve retention schedule action details
     * 
     * @param retentionScheduleNodeRef
     *            nodeRef
     * @return List<DispositionActionDefinition>
     */
    public List<DispositionActionDefinition> getRetentionActions(NodeRef retentionScheduleNodeRef)
    {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(
                retentionScheduleNodeRef,
                RecordsManagementModel.ASSOC_DISPOSITION_ACTION_DEFINITIONS,
                RegexQNamePattern.MATCH_ALL);
        // we are getting disposition action definitions based on retention schedule child association.
        // setting the index value for each action.
        List<DispositionActionDefinition> actions;
        actions = IntStream.range(0, assocs.size())
                .mapToObj(index -> {
                    ChildAssociationRef assoc = assocs.get(index);
                    return new DispositionActionDefinitionImpl(
                            services.getRecordsManagementEventService(),
                            services.getRecordsManagementActionService(),
                            nodeService,
                            assoc.getChildRef(),
                            index);
                })
                .collect(Collectors.toList());
        return actions;
    }

    /**
     * this method is used to check period amount applicable or not for particular period
     * 
     * @param period
     *            period
     * @return boolean
     */
    private boolean isPeriodAmountApplicable(String period)
    {
        // periodAmount property only applicable for following periods
        // day, week, month, quarter, year and duration
        return period.equals(RetentionPeriod.DAY.periodName) || period.equals(RetentionPeriod.MONTH.periodName) || period.equals(RetentionPeriod.QUARTER.periodName)
                || period.equals(RetentionPeriod.WEEK.periodName) || period.equals(RetentionPeriod.XML_DURATION.periodName) || period.equals(RetentionPeriod.YEAR.periodName);
    }
}
