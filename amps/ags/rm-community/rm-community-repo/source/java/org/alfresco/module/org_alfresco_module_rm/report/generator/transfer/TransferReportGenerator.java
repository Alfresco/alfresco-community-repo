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

package org.alfresco.module.org_alfresco_module_rm.report.generator.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.report.generator.DeclarativeReportGenerator;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Transfer report generator.
 * 
 * @author Tuna Aksoy
 * @author Roy Wetherall
 * @since 2.2
 */
public class TransferReportGenerator extends DeclarativeReportGenerator
{
    /** dispotion service */
    protected DispositionService dispositionService;
    
    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.report.generator.DeclarativeReportGenerator#generateReportTemplateContext(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected Map<String, Serializable> generateReportTemplateContext(NodeRef reportedUponNodeRef)
    {
        // Get all 'transferred' nodes
        List<TransferNode> transferNodes = getTransferNodes(reportedUponNodeRef);

        // Get the disposition authority
        String dispositionAuthority = getDispositionAuthority(transferNodes);

        // Save to the properties map
        Map<String, Serializable> properties = new HashMap<>(2);
        properties.put("transferNodes", (ArrayList<TransferNode>) transferNodes);
        properties.put("dispositionAuthority", dispositionAuthority);

        return properties;
    }

    /**
     * Returns a list of transfer nodes
     *
     * @param nodeRef The transfer object
     * @return Transfer node list
     */
    private List<TransferNode> getTransferNodes(NodeRef nodeRef)
    {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RecordsManagementModel.ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
        List<TransferNode> transferNodes = new ArrayList<>(assocs.size());
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef childRef = assoc.getChildRef();
            Map<String, Serializable> properties = getTransferNodeProperties(childRef);
            transferNodes.add(new TransferNode(childRef, properties));
        }
        return transferNodes;
    }

    /**
     * Helper method to get the properties of a transfer node
     *
     * @param childRef  Node reference
     * @return Transfer node properties
     */
    private Map<String, Serializable> getTransferNodeProperties(NodeRef childRef)
    {
        Map<String, Serializable> transferNodeProperties = new HashMap<>(6);

        boolean isFolder = dictionaryService.isSubClass(nodeService.getType(childRef), ContentModel.TYPE_FOLDER);
        transferNodeProperties.put("isFolder", isFolder);

        if (isFolder)
        {
            Map<String, Serializable> folderProperties = getFolderProperties(childRef);
            transferNodeProperties.putAll(folderProperties);
        }
        else
        {
            Map<String, Serializable> recordProperties = getRecordProperties(childRef);
            transferNodeProperties.putAll(recordProperties);
        }

        return transferNodeProperties;
    }

    /**
     * Helper method to get the list of records (with their properties) within a folder
     *
     * @param childRef  Node reference of the folder
     * @return List of records within the specified folder
     */
    private List<TransferNode> getRecords(NodeRef childRef)
    {
        List<TransferNode> records = new ArrayList<>(4);
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(childRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef child : assocs)
        {
            NodeRef record = child.getChildRef();
            if (nodeService.hasAspect(record, RecordsManagementModel.ASPECT_RECORD))
            {
                Map<String, Serializable> recordProperties = getRecordProperties(record);
                TransferNode transferNode = new TransferNode(record, recordProperties);
                records.add(transferNode);
            }
        }
        return records;
    }

    /**
     * Helper method to get the common transfer node properties
     *
     * @param nodeRef   Node reference of the transfer node
     * @return  Map of the common transfer node properties
     */
    private Map<String, Serializable> getCommonProperties(NodeRef nodeRef)
    {
        Map<String, Serializable> transferNodeProperties = new HashMap<>(3);

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        String name = (String) properties.get(ContentModel.PROP_NAME);
        String identifier = (String) properties.get(RecordsManagementModel.PROP_IDENTIFIER);

        transferNodeProperties.put("name", name);
        transferNodeProperties.put("identifier", identifier);

        return transferNodeProperties;
    }

    /**
     * Helper method to get the folder specific properties
     *
     * @param folder Node reference of the folder
     * @return Map of the folder specific properties
     */
    private Map<String, Serializable> getFolderProperties(NodeRef folder)
    {
        Map<String, Serializable> transferNodeProperties = new HashMap<>(3);

        Map<String, Serializable> commonProperties = getCommonProperties(folder);
        ArrayList<TransferNode> records = (ArrayList<TransferNode>) getRecords(folder);
        transferNodeProperties.putAll(commonProperties);
        transferNodeProperties.put("records", records);

        return transferNodeProperties;
    }

    /**
     * Helper method to get the record folder properties
     *
     * @param record Node reference of the record
     * @return Map of the record specific properties
     */
    private Map<String, Serializable> getRecordProperties(NodeRef record)
    {
        Map<String, Serializable> transferNodeProperties = new HashMap<>(5);

        Map<QName, Serializable> properties = nodeService.getProperties(record);
        String declaredBy = (String) properties.get(RecordsManagementModel.PROP_DECLARED_BY);
        Date declaredOn = (Date) properties.get(RecordsManagementModel.PROP_DECLARED_AT);
        boolean isDeclared = nodeService.hasAspect(record, RecordsManagementModel.ASPECT_DECLARED_RECORD);

        Map<String, Serializable> commonProperties = getCommonProperties(record);
        transferNodeProperties.putAll(commonProperties);
        transferNodeProperties.put("declaredBy", declaredBy);
        transferNodeProperties.put("declaredOn", declaredOn);
        transferNodeProperties.put("isDeclared", isDeclared);

        return transferNodeProperties;
    }

    /**
     * Gets the disposition authority from the list of the transfer nodes
     *
     * @param transferNodes   The transfer nodes
     * @return Disposition authority
     */
    private String getDispositionAuthority(List<TransferNode> transferNodes)
    {
        // use RMService to get disposition authority
        String dispositionAuthority = null;
        if (transferNodes.size() > 0)
        {
            // use the first transfer item to get to disposition schedule
            NodeRef nodeRef = transferNodes.iterator().next().getNodeRef();
            DispositionSchedule ds = dispositionService.getDispositionSchedule(nodeRef);
            if (ds != null)
            {
                dispositionAuthority = ds.getDispositionAuthority();
            }
        }
        return dispositionAuthority == null ? StringUtils.EMPTY : dispositionAuthority;
    }

}
