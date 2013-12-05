/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.vital;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Vital record service interface implementation.
 *
 * @author Roy Wetherall
 * @since 2.0
 */
public class VitalRecordServiceImpl implements VitalRecordService,
                                               RecordsManagementModel
{
    /** Services */
    private NodeService nodeService;
    private FilePlanService filePlanService;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService#setupVitalRecordDefinition(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void setupVitalRecordDefinition(NodeRef nodeRef)
    {
        // get the current review period value
        Period currentReviewPeriod = (Period)nodeService.getProperty(nodeRef, PROP_REVIEW_PERIOD);
        if (currentReviewPeriod == null ||
            PERIOD_NONE.equals(currentReviewPeriod) == true)
        {
            // get the immediate parent
            NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

            // is the parent a record category
            if (parentRef != null &&
                FilePlanComponentKind.RECORD_CATEGORY.equals(filePlanService.getFilePlanComponentKind(parentRef)) == true)
            {
                // is the child a record category or folder
                FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
                if (kind.equals(FilePlanComponentKind.RECORD_CATEGORY) == true ||
                    kind.equals(FilePlanComponentKind.RECORD_FOLDER) == true)
                {
                    // set the vital record definition values to match that of the parent
                    nodeService.setProperty(nodeRef,
                                            PROP_VITAL_RECORD_INDICATOR,
                                            nodeService.getProperty(parentRef, PROP_VITAL_RECORD_INDICATOR));
                    nodeService.setProperty(nodeRef,
                                            PROP_REVIEW_PERIOD,
                                            nodeService.getProperty(parentRef, PROP_REVIEW_PERIOD));
                }
            }
        }
    }

    /**
     * @see VitalRecordService#initialiseVitalRecord(NodeRef)
     */
    public void initialiseVitalRecord(NodeRef nodeRef)
    {
        // Calculate the review schedule
        VitalRecordDefinition viDef = getVitalRecordDefinition(nodeRef);
        if (viDef != null && viDef.isEnabled() == true)
        {
            Date reviewAsOf = viDef.getNextReviewDate();
            if (reviewAsOf != null)
            {
                Map<QName, Serializable> reviewProps = new HashMap<QName, Serializable>(1);
                reviewProps.put(RecordsManagementModel.PROP_REVIEW_AS_OF, reviewAsOf);

                if (nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD) == false)
                {
                    nodeService.addAspect(nodeRef, RecordsManagementModel.ASPECT_VITAL_RECORD, reviewProps);
                }
                else
                {
                    Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
                    props.putAll(reviewProps);
                    nodeService.setProperties(nodeRef, props);
                }
            }
        }
        else
        {
            // if we are re-filling then remove the vital aspect if it is not longer a vital record
            if (nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD) == true)
            {
                nodeService.removeAspect(nodeRef, ASPECT_VITAL_RECORD);
            }
        }
    }

    /**
     * @see VitalRecordService#getVitalRecordDefinition(NodeRef)
     */
    public VitalRecordDefinition getVitalRecordDefinition(NodeRef nodeRef)
    {
        VitalRecordDefinition result = null;

        FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
        if (FilePlanComponentKind.RECORD.equals(kind) == true)
        {
            result = resolveVitalRecordDefinition(nodeRef);
        }
        else
        {
            if (nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD_DEFINITION) == true)
            {
                result = VitalRecordDefinitionImpl.create(nodeService, nodeRef);
            }
        }

        return result;
    }

    /**
     * Resolves the record vital definition.
     * <p>
     * NOTE:  Currently we only support the resolution of the vital record definition from the
     * primary record parent.  ie the record folder the record was originally filed within.
     * <p>
     * TODO:  Add an algorithm to resolve the correct vital record definition when a record is filed in many
     * record folders.
     *
     * @param record
     * @return VitalRecordDefinition
     */
    private VitalRecordDefinition resolveVitalRecordDefinition(NodeRef record)
    {
        NodeRef parent = nodeService.getPrimaryParent(record).getParentRef();
        return getVitalRecordDefinition(parent);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService#setVitalRecordDefintion(org.alfresco.service.cmr.repository.NodeRef, boolean, org.alfresco.service.cmr.repository.Period)
     */
    @Override
    public VitalRecordDefinition setVitalRecordDefintion(NodeRef nodeRef, boolean enabled, Period reviewPeriod)
    {
        // Check params
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("enabled", enabled);

        // Set the properties (will automatically add the vital record definition aspect)
        nodeService.setProperty(nodeRef, PROP_VITAL_RECORD_INDICATOR, enabled);
        nodeService.setProperty(nodeRef, PROP_REVIEW_PERIOD, reviewPeriod);

        return new VitalRecordDefinitionImpl(enabled, reviewPeriod);
    }

    /**
     * @see VitalRecordService#isVitalRecord(NodeRef)
     */
    public boolean isVitalRecord(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ASPECT_VITAL_RECORD);
    }
}
