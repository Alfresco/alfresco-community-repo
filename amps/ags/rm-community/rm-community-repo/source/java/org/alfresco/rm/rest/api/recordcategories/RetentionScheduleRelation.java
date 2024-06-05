/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.rm.rest.api.recordcategories;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.RetentionSchedule;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import java.util.ArrayList;
import java.util.List;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

@RelationshipResource(name = "retention-schedule", entityResource = RecordCategoriesEntityResource.class, title = "Retention Schedule")
public class RetentionScheduleRelation implements RelationshipResourceAction.Read<RetentionSchedule>,
        RelationshipResourceAction.Create<RetentionSchedule>{

    private FilePlanComponentsApiUtils apiUtils;

    private ApiNodesModelFactory nodesModelFactory;

    /** Disposition service */
    private DispositionService dispositionService;

    /** Node service */
    protected NodeService nodeService;

    @Override
    public List<RetentionSchedule> create(String recordCategoryId, List<RetentionSchedule> nodeInfos, Parameters parameters) {
        checkNotBlank("recordCategoryId", recordCategoryId);
        mandatory("entity", nodeInfos);
        mandatory("parameters", parameters);
        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(recordCategoryId, RecordsManagementModel.TYPE_RECORD_CATEGORY);
        // Create the disposition schedule
        DispositionSchedule dispositionSchedule = dispositionService.createDispositionSchedule(parentNodeRef, null);
        RetentionSchedule retentionSchedule = nodesModelFactory.createRetentionSchedule(dispositionSchedule);
        return List.of(retentionSchedule);
    }

    @Override
    public CollectionWithPagingInfo<RetentionSchedule> readAll(String recordCategoryId, Parameters parameters) {
        checkNotBlank("recordCategoryId", recordCategoryId);
        mandatory("parameters", parameters);
        NodeRef parentNodeRef = apiUtils.lookupAndValidateNodeType(recordCategoryId, RecordsManagementModel.TYPE_RECORD_CATEGORY);
        DispositionSchedule schedule = dispositionService.getDispositionSchedule(parentNodeRef);
        RetentionSchedule retentionSchedule = nodesModelFactory.createRetentionSchedule(schedule);
        List<RetentionSchedule> retentionScheduleList = new ArrayList<>();
        nodesModelFactory.mapRetentionScheduleOptionalInfo(retentionSchedule, schedule, parameters.getInclude());
        retentionScheduleList.add(retentionSchedule);
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), retentionScheduleList, false,
                retentionScheduleList.size());
    }

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
}