/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.rm.rest.api.retentionschedule;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.UnprocessableContentException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.RetentionSchedule;
import org.alfresco.rm.rest.api.recordcategories.RecordCategoriesEntityResource;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_DISPOSITION_AUTHORITY;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_DISPOSITION_INSTRUCTIONS;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.PROP_RECORD_LEVEL_DISPOSITION;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_RECORD_CATEGORY;
import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

/**
 * Retention schedule relation is used perform retention schedule operation for a record category.
 */
@RelationshipResource(name = "retention-schedules", entityResource = RecordCategoriesEntityResource.class, title = "Retention Schedule")
public class RetentionScheduleRelation implements RelationshipResourceAction.Read<RetentionSchedule>,
        RelationshipResourceAction.Create<RetentionSchedule>
{

    private FilePlanComponentsApiUtils apiUtils;
    private ApiNodesModelFactory nodesModelFactory;
    private DispositionService dispositionService;
    protected NodeService nodeService;

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setNodesModelFactory(ApiNodesModelFactory nodesModelFactory)
    {
        this.nodesModelFactory = nodesModelFactory;
    }

    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    @WebApiDescription(title="Create a retention schedule for the particular record category using the 'recordCategoryId'")
    public List<RetentionSchedule> create(String recordCategoryId, List<RetentionSchedule> nodeInfos, Parameters parameters)
    {
        checkNotBlank("recordCategoryId", recordCategoryId);
        mandatory("entity", nodeInfos);
        mandatory("parameters", parameters);
        NodeRef parentNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, recordCategoryId);

        if (checkCategoryHasAssocFolder(parentNodeRef) && nodeInfos.get(0).getIsRecordLevel())
        {
            throw new UnprocessableContentException("Record level retention schedule cannot be created for a record category having folder associated.");
        }
        List<RetentionSchedule> result = new ArrayList<>();
        // Create the disposition schedule
        Map<QName, Serializable> dsProps = new HashMap<>();
        dsProps.put(PROP_DISPOSITION_AUTHORITY, nodeInfos.get(0).getAuthority());
        dsProps.put(PROP_DISPOSITION_INSTRUCTIONS, nodeInfos.get(0).getInstructions());
        dsProps.put(PROP_RECORD_LEVEL_DISPOSITION, nodeInfos.get(0).getIsRecordLevel());
        DispositionSchedule dispositionSchedule = dispositionService.createDispositionSchedule(parentNodeRef, dsProps);
        RetentionSchedule retentionSchedule = nodesModelFactory.mapRetentionScheduleData(dispositionSchedule);
        result.add(retentionSchedule);
        return result;
    }

    private boolean checkCategoryHasAssocFolder(NodeRef nodeRef)
    {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        return assocs.stream()
                .map(assoc -> nodeService.getType(assoc.getChildRef()))
                .anyMatch(nodeType -> nodeType.equals(RecordsManagementModel.TYPE_RECORD_FOLDER));
    }

    @Override
    @WebApiDescription(title = "Return a paged list of retention schedule based on the 'recordCategoryId'")
    public CollectionWithPagingInfo<RetentionSchedule> readAll(String recordCategoryId, Parameters parameters)
    {
        checkNotBlank("recordCategoryId", recordCategoryId);
        mandatory("parameters", parameters);
        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(recordCategoryId, TYPE_RECORD_CATEGORY);
        DispositionSchedule schedule = dispositionService.getDispositionSchedule(parentNodeRef);
        RetentionSchedule retentionSchedule = nodesModelFactory.mapRetentionScheduleData(schedule);
        List<RetentionSchedule> retentionScheduleList = new ArrayList<>();
        nodesModelFactory.mapRetentionScheduleOptionalInfo(retentionSchedule, schedule, parameters.getInclude());
        retentionScheduleList.add(retentionSchedule);
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), retentionScheduleList, false,
                retentionScheduleList.size());
    }
}
