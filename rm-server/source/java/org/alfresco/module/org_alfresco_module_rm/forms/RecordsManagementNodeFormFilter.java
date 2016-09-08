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
package org.alfresco.module.org_alfresco_module_rm.forms;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ImapModel;
import org.alfresco.module.org_alfresco_module_rm.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.compatibility.CompatibilityModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionScheduleImpl;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.PropertyFieldDefinition;
import org.alfresco.repo.forms.processor.node.FieldUtils;
import org.alfresco.repo.forms.processor.node.FormFieldConstants;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a form processor Filter.
 * <p>
 * The filter ensures that any custom properties defined for the records
 * management type are provided as part of the Form and also assigned to the
 * same field group.
 * </p>
 * 
 * @author Gavin Cornwell
 */
public class RecordsManagementNodeFormFilter extends RecordsManagementFormFilter<NodeRef> implements RecordsManagementModel, DOD5015Model
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RecordsManagementNodeFormFilter.class);

    protected static final String TRANSIENT_DECLARED = "rmDeclared";
    protected static final String TRANSIENT_CATEGORY_ID = "rmCategoryIdentifier";
    protected static final String TRANSIENT_DISPOSITION_INSTRUCTIONS = "rmDispositionInstructions";

    /** Dictionary service */
    protected DictionaryService dictionaryService;
    
    /** Disposition service */
    protected DispositionService dispositionService;    

    /**
     * Sets the data dictionary service
     * 
     * @param dictionaryService The DictionaryService instance
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Sets the disposition service
     *  
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /*
     * @see org.alfresco.repo.forms.processor.Filter#afterGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
     */
    public void afterGenerate(
            NodeRef nodeRef, 
            List<String> fields, 
            List<String> forcedFields, 
            Form form,
            Map<String, Object> context)
    {
        if (rmService.isFilePlanComponent(nodeRef) == true)
        {        
            // add all the custom properties
            addCustomPropertyFieldsToGroup(form, nodeRef);
            
            FilePlanComponentKind kind = rmService.getFilePlanComponentKind(nodeRef);
            if (FilePlanComponentKind.RECORD.equals(kind) == true)
            {
                // add all the record meta-data aspect properties
                addRecordMetadataPropertyFieldsToGroup(form, nodeRef);
                
                // add required transient properties
                addTransientProperties(form, nodeRef);
                
                // add the supplemental marking list property
                forceSupplementalMarkingListProperty(form, nodeRef);
                
                // if the record is the result of an email we need to 'protect' some fields
                if (this.nodeService.hasAspect(nodeRef, ImapModel.ASPECT_IMAP_CONTENT))
                {
                    protectEmailExtractedFields(form, nodeRef);
                }
            }
            else if (FilePlanComponentKind.RECORD_FOLDER.equals(kind) == true)
            {
                // add the supplemental marking list property
                forceSupplementalMarkingListProperty(form, nodeRef);
                
                // add required transient properties
                addTransientProperties(form, nodeRef);
            }
            else if (FilePlanComponentKind.DISPOSITION_SCHEDULE.equals(kind) == true)
            {
                 // use the same mechanism used to determine whether steps can be removed from the
                 // schedule to determine whether the disposition level can be changed i.e. record 
                 // level or folder level.
                 DispositionSchedule schedule = new DispositionScheduleImpl(this.rmServiceRegistry, this.nodeService, nodeRef);
                 if (dispositionService.hasDisposableItems(schedule) == true)
                 {
                     protectRecordLevelDispositionPropertyField(form);
                 }
            }
            
        }
    }
    
    protected void addCustomPropertyFieldsToGroup(Form form, NodeRef nodeRef)
    {
        Set<QName> customisables = rmAdminService.getCustomisable(nodeRef);
        
        // Compatibility support: don't show category properties if node of type series
        QName type = nodeService.getType(nodeRef);
        if (CompatibilityModel.TYPE_RECORD_SERIES.equals(type) == true)
        {
            // remove record category from the list of customisable types to apply to the form
            customisables.remove(TYPE_RECORD_CATEGORY);
        }
        
        for (QName customisable : customisables)
        {
            addPropertyFieldsToGroup(form, rmAdminService.getCustomPropertyDefinitions(customisable), CUSTOM_RM_FIELD_GROUP_ID);
        }
    }
    
    protected void addRecordMetadataPropertyFieldsToGroup(Form form, NodeRef nodeRef)
    {
        Set<QName> aspects = rmService.getRecordMetaDataAspects();
        
        for (QName aspect : aspects)
        {
            if (nodeService.hasAspect(nodeRef, aspect) == true)
            {
                String aspectName = aspect.getPrefixedQName(namespaceService).toPrefixString().replace(":", "-");
                String setId = RM_METADATA_PREFIX + aspectName;
                addPropertyFieldsToGroup(form, dictionaryService.getPropertyDefs(aspect), setId);
            }
        }
    }

    /**
     * Forces the "rmc:supplementalMarkingList" property to be present, if it is
     * already on the given node this method does nothing, otherwise a property
     * field definition is generated for the property.
     * 
     * @param form The Form instance to add the property to
     * @param nodeRef The node the form is being generated for
     */
    protected void forceSupplementalMarkingListProperty(Form form, NodeRef nodeRef)
    {
        if (!this.nodeService.hasAspect(nodeRef, 
                    RecordsManagementCustomModel.ASPECT_SUPPLEMENTAL_MARKING_LIST))
        {
            PropertyDefinition propDef = this.dictionaryService.getProperty(
                        RecordsManagementCustomModel.PROP_SUPPLEMENTAL_MARKING_LIST);
            
            if (propDef != null)
            {
                Field field = FieldUtils.makePropertyField(propDef, null, null, namespaceService, dictionaryService);
                form.addField(field);
            }
            else if (logger.isWarnEnabled())
            {
                logger.warn("Could not add " + 
                            RecordsManagementCustomModel.PROP_SUPPLEMENTAL_MARKING_LIST.getLocalName() +
                            " property as it's definition could not be found");
            }
        }
    }

    
    protected void addTransientProperties(Form form, NodeRef nodeRef)
    {
        if (rmService.isRecord(nodeRef) == true)
        {
            addTransientPropertyField(form, TRANSIENT_DECLARED, DataTypeDefinition.BOOLEAN, rmService.isRecordDeclared(nodeRef));
        }
        
        DispositionSchedule ds = dispositionService.getDispositionSchedule(nodeRef);
        if (ds != null)
        {
            String instructions = ds.getDispositionInstructions();
            if (instructions != null)
            {
                addTransientPropertyField(form, TRANSIENT_DISPOSITION_INSTRUCTIONS, DataTypeDefinition.TEXT, instructions);
            }
            
            NodeRef recordCategory = dispositionService.getAssociatedRecordsManagementContainer(ds);
            if (recordCategory != null)
            {
                String categoryId = (String)nodeService.getProperty(recordCategory, PROP_IDENTIFIER);
                if (categoryId != null)
                {
                    addTransientPropertyField(form, TRANSIENT_CATEGORY_ID, DataTypeDefinition.TEXT, categoryId);
                }
            }
        }        
    }
    
    protected void addTransientPropertyField(Form form, String name, QName type, Object value)
    {
        String dataKeyName = FormFieldConstants.PROP_DATA_PREFIX + name;
        PropertyFieldDefinition declaredField = new PropertyFieldDefinition(name, type.getLocalName());
        declaredField.setLabel(name);
        declaredField.setDescription(name);
        declaredField.setProtectedField(true);
        declaredField.setDataKeyName(dataKeyName);
        form.addFieldDefinition(declaredField);
        form.addData(dataKeyName, value);
    }
    
    /**
     * Marks all the fields that contain data extracted from an email
     * as protected fields.
     * 
     * @param form The Form instance to add the property to
     * @param nodeRef The node the form is being generated for
     */
    protected void protectEmailExtractedFields(Form form, NodeRef nodeRef)
    {
        // iterate round existing fields and set email fields as protected
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        for (FieldDefinition fieldDef : fieldDefs)
        {
            String prefixName = fieldDef.getName();
            
            // check the value of the property, if empty then do not mark property 
            // as read only
            QName qname = QName.createQName(prefixName, namespaceService);
            Serializable value = nodeService.getProperty(nodeRef, qname);
            if (value != null)
            {            
                if (prefixName.equals("cm:title") || 
                    prefixName.equals("cm:author") ||
                    prefixName.equals("rma:originator") ||
                    prefixName.equals("rma:publicationDate") ||
                    prefixName.equals("rma:dateReceived") ||
                    prefixName.equals("rma:address") ||
                    prefixName.equals("rma:otherAddress"))
                {
                    fieldDef.setProtectedField(true);
                }
            }
        }
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("Set email related fields to be protected");
        }
    }
    
    /**
     * Marks the recordLevelDisposition property as protected to disable editing
     * 
     * @param form The Form instance
     */
    protected void protectRecordLevelDispositionPropertyField(Form form)
    {
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        for (FieldDefinition fieldDef : fieldDefs)
        {
            if (fieldDef.getName().equals(RecordsManagementModel.PROP_RECORD_LEVEL_DISPOSITION.toPrefixString(
                        this.namespaceService)))
            {
                fieldDef.setProtectedField(true);
                break;
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Set 'rma:recordLevelDisposition' field to be protected as record folders or records are present");
    }
}
