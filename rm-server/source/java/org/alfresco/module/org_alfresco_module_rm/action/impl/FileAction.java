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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Files a record into a particular record folder
 * 
 * @author Roy Wetherall
 */
public class FileAction extends RMActionExecuterAbstractBase
{
    /** Parameter names */
    public static final String PARAM_RECORD_METADATA_ASPECTS = "recordMetadataAspects";
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // Permissions perform the following checks so this action doesn't need to.
        //
        // check the record is within a folder
        // check that the folder we are filing into is not closed
        
        // Get the optional list of record meta-data aspects
        List<QName> recordMetadataAspects = (List<QName>)action.getParameterValue(PARAM_RECORD_METADATA_ASPECTS);
            
    	// Add the record aspect (doesn't matter if it is already present)
    	if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_RECORD) == false)
    	{
    		nodeService.addAspect(actionedUponNodeRef, RecordsManagementModel.ASPECT_RECORD, null);
    	}	
    		
        // Get the records properties
        Map<QName, Serializable> recordProperties = this.nodeService.getProperties(actionedUponNodeRef);

        Calendar fileCalendar = Calendar.getInstance();
        if (recordProperties.get(RecordsManagementModel.PROP_IDENTIFIER) == null)
        {
	        // Calculate the filed date and record identifier	        
	        String year = Integer.toString(fileCalendar.get(Calendar.YEAR));
	        QName nodeDbid = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "node-dbid");
	        String recordId = year + "-" + padString(recordProperties.get(nodeDbid).toString(), 10);        
	        recordProperties.put(RecordsManagementModel.PROP_IDENTIFIER, recordId);
        }
        
        // Update/set the date this record was refiled/filed
        recordProperties.put(RecordsManagementModel.PROP_DATE_FILED, fileCalendar.getTime());

        // Set the record properties
        this.nodeService.setProperties(actionedUponNodeRef, recordProperties);     
        
        // Apply any record meta-data aspects
        if (recordMetadataAspects != null && recordMetadataAspects.size() != 0)
        {
            for (QName aspect : recordMetadataAspects)
            {
                nodeService.addAspect(actionedUponNodeRef, aspect, null);
            }
        }

        // Calculate the review schedule
        VitalRecordDefinition viDef = vitalRecordService.getVitalRecordDefinition(actionedUponNodeRef);
        if (viDef != null && viDef.isEnabled() == true)
        {
	        Date reviewAsOf = viDef.getNextReviewDate();
	        if (reviewAsOf != null)
	        {
	            Map<QName, Serializable> reviewProps = new HashMap<QName, Serializable>(1);
	            reviewProps.put(RecordsManagementModel.PROP_REVIEW_AS_OF, reviewAsOf);
	            
	            if (!nodeService.hasAspect(actionedUponNodeRef, ASPECT_VITAL_RECORD))
	            {
	                this.nodeService.addAspect(actionedUponNodeRef, RecordsManagementModel.ASPECT_VITAL_RECORD, reviewProps);
	            }
	            else
	            {
	                Map<QName, Serializable> props = nodeService.getProperties(actionedUponNodeRef);
	                props.putAll(reviewProps);
	                nodeService.setProperties(actionedUponNodeRef, props);
	            }
	        }
        }

        // Get the disposition instructions for the actioned upon record
        DispositionSchedule di = this.dispositionService.getDispositionSchedule(actionedUponNodeRef);
        
        // Set up the disposition schedule if the dispositions are being managed at the record level
        if (di != null && di.isRecordLevelDisposition() == true)
        {
            // Setup the next disposition action
            updateNextDispositionAction(actionedUponNodeRef);
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {              
        // No parameters
        paramList.add(new ParameterDefinitionImpl(PARAM_RECORD_METADATA_ASPECTS, DataTypeDefinition.QNAME, false, "Record Metadata Aspects", true));
    }

    @Override
    public Set<QName> getProtectedAspects()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(ASPECT_RECORD);
        qnames.add(ASPECT_VITAL_RECORD);
        return qnames;
    }

    @Override
    public Set<QName> getProtectedProperties()
    {
        HashSet<QName> qnames = new HashSet<QName>();
        qnames.add(PROP_DATE_FILED);
        qnames.add(PROP_REVIEW_AS_OF);
        return qnames;
    }

    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
        return true;
    }

}
