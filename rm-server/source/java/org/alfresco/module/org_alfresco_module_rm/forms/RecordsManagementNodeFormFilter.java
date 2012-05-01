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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ImapModel;
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
    protected static final String TRANSIENT_RECORD_TYPE = "rmRecordType";
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
    public void afterGenerate(NodeRef nodeRef, List<String> fields, List<String> forcedFields, Form form,
                Map<String, Object> context)
    {
    	// TODO this needs a massive refactor inorder to support any custom type or aspect ....
    	
        // if the node has the RM marker aspect look for the custom properties
        // for the type
        if (this.nodeService.hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT))
        {
            // Make sure any customisable types or aspects present of the node have their properties included
            // in the custom group
            showCustomProperties(nodeRef, form);
            
            if (this.nodeService.hasAspect(nodeRef, ASPECT_RECORD))
            {
                // force the "supplementalMarkingList" property to be present
                forceSupplementalMarkingListProperty(form, nodeRef);
                
                // generate property definitions for the 'transient' properties
                generateDeclaredPropertyField(form, nodeRef);
                generateRecordTypePropertyField(form, nodeRef);
                generateCategoryIdentifierPropertyField(form, nodeRef);
                generateDispositionInstructionsPropertyField(form, nodeRef);
                
                // if the record is the result of an email we need to 'protect' some fields
                if (this.nodeService.hasAspect(nodeRef, ImapModel.ASPECT_IMAP_CONTENT))
                {
                    protectEmailExtractedFields(form, nodeRef);
                }
            }
            else
            {
                QName type = this.nodeService.getType(nodeRef);
                if (TYPE_RECORD_FOLDER.equals(type))
                {
                    // force the "supplementalMarkingList" property to be present
                    forceSupplementalMarkingListProperty(form, nodeRef);
                }
                else if (TYPE_DISPOSITION_SCHEDULE.equals(type))
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
    }
    
    /**
     * Show the custom properties if any are present.
     * 
     * @param nodeRef   node reference
     * @param form      form
     */
    protected void showCustomProperties(NodeRef nodeRef, Form form)
    {
        Set<QName> customClasses = rmAdminService.getCustomisable(nodeRef);
        if (customClasses.isEmpty() == false)
        {
            // add the 'rm-custom' field group
            addCustomRMGroup(form);        
        }        
    }

    /**
     * Adds the Custom RM field group (id 'rm-custom') to all the field
     * definitions representing RM custom properties.
     * 
     * @param form The form holding the field definitions
     */
    protected void addCustomRMGroup(Form form)
    {
        // iterate round existing fields and set group on each custom
        // RM field
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        for (FieldDefinition fieldDef : fieldDefs)
        {
            if (fieldDef.getName().startsWith(RM_CUSTOM_PREFIX) &&
                !fieldDef.getName().equals(PROP_SUPPLEMENTAL_MARKING_LIST.toPrefixString(this.namespaceService)))
            {
                // only add custom RM properties, not associations/references
                if (fieldDef instanceof PropertyFieldDefinition)
                {
                    fieldDef.setGroup(CUSTOM_RM_FIELD_GROUP);

                    if (logger.isDebugEnabled())
                        logger.debug("Added \"" + fieldDef.getName() + "\" to RM custom field group");
                }
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
                Field field = FieldUtils.makePropertyField(propDef, null, null, namespaceService);
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
    
    /**
     * Generates the field definition for the transient <code>rmDeclared</code>
     * property.
     * 
     * @param form The Form instance to add the property to
     * @param nodeRef The node the form is being generated for
     */
    protected void generateDeclaredPropertyField(Form form, NodeRef nodeRef)
    {
        // TODO should this be done using a new FieldProcessor?
        String dataKeyName = FormFieldConstants.PROP_DATA_PREFIX + TRANSIENT_DECLARED;
        PropertyFieldDefinition declaredField = new PropertyFieldDefinition(TRANSIENT_DECLARED,
                    DataTypeDefinition.BOOLEAN.getLocalName());
        declaredField.setLabel(TRANSIENT_DECLARED);
        declaredField.setDescription(TRANSIENT_DECLARED);
        declaredField.setProtectedField(true);
        declaredField.setDataKeyName(dataKeyName);
        form.addFieldDefinition(declaredField);
        form.addData(dataKeyName, this.nodeService.hasAspect(nodeRef, ASPECT_DECLARED_RECORD));
    }

    /**
     * Generates the field definition for the transient
     * <code>rmRecordType</code> property
     * 
     * @param form The Form instance to add the property to
     * @param nodeRef The node the form is being generated for
     */
    protected void generateRecordTypePropertyField(Form form, NodeRef nodeRef)
    {
        String dataKeyName = FormFieldConstants.PROP_DATA_PREFIX + TRANSIENT_RECORD_TYPE;
        PropertyFieldDefinition recordTypeField = new PropertyFieldDefinition(TRANSIENT_RECORD_TYPE,
                    DataTypeDefinition.TEXT.getLocalName());
        recordTypeField.setLabel(TRANSIENT_RECORD_TYPE);
        recordTypeField.setDescription(TRANSIENT_RECORD_TYPE);
        recordTypeField.setProtectedField(true);
        recordTypeField.setDataKeyName(dataKeyName);
        form.addFieldDefinition(recordTypeField);

        // determine what record type value to return, use aspect/type title
        // from model
        String recordType = null;
        QName type = this.nodeService.getType(nodeRef);
        if (TYPE_NON_ELECTRONIC_DOCUMENT.equals(type))
        {
            // get the non-electronic type title
            recordType = dictionaryService.getType(TYPE_NON_ELECTRONIC_DOCUMENT).getTitle();
        }
        else
        {
            // the aspect applied to record determines it's type
            if (nodeService.hasAspect(nodeRef, ASPECT_PDF_RECORD))
            {
                recordType = dictionaryService.getAspect(ASPECT_PDF_RECORD).getTitle();
            }
            else if (nodeService.hasAspect(nodeRef, ASPECT_WEB_RECORD))
            {
                recordType = dictionaryService.getAspect(ASPECT_WEB_RECORD).getTitle();
            }
            else if (nodeService.hasAspect(nodeRef, ASPECT_SCANNED_RECORD))
            {
                recordType = dictionaryService.getAspect(ASPECT_SCANNED_RECORD).getTitle();
            }
            else if (nodeService.hasAspect(nodeRef, ASPECT_DIGITAL_PHOTOGRAPH_RECORD))
            {
                recordType = dictionaryService.getAspect(ASPECT_DIGITAL_PHOTOGRAPH_RECORD).getTitle();
            }
            else
            {
                // no specific aspect applied so default to just "Record"
                recordType = dictionaryService.getAspect(ASPECT_RECORD).getTitle();
            }
        }

        form.addData(dataKeyName, recordType);
    }
    
    /**
     * Generates the field definition for the transient <code>rmCategoryIdentifier</code>
     * property
     * 
     * @param form The Form instance to add the property to
     * @param nodeRef The node the form is being generated for
     */
    protected void generateDispositionInstructionsPropertyField(Form form, NodeRef nodeRef)
    {
        String dataKeyName = FormFieldConstants.PROP_DATA_PREFIX + TRANSIENT_DISPOSITION_INSTRUCTIONS;
        PropertyFieldDefinition dispInstructionsField = new PropertyFieldDefinition(TRANSIENT_DISPOSITION_INSTRUCTIONS,
                    DataTypeDefinition.TEXT.getLocalName());
        dispInstructionsField.setLabel(TRANSIENT_DISPOSITION_INSTRUCTIONS);
        dispInstructionsField.setDescription(TRANSIENT_DISPOSITION_INSTRUCTIONS);
        dispInstructionsField.setProtectedField(true);
        dispInstructionsField.setDataKeyName(dataKeyName);
        form.addFieldDefinition(dispInstructionsField);
        
        // use RMService to get disposition instructions
        DispositionSchedule ds = dispositionService.getDispositionSchedule(nodeRef);
        if (ds != null)
        {
            String instructions = ds.getDispositionInstructions();
            if (instructions != null)
            {
                form.addData(dataKeyName, instructions);
            }
        }
    }
    
    /**
     * Generates the field definition for the transient <code>rmCategoryIdentifier</code>
     * property
     * 
     * @param form The Form instance to add the property to
     * @param nodeRef The node the form is being generated for
     */
    protected void generateCategoryIdentifierPropertyField(Form form, NodeRef nodeRef)
    {
        String dataKeyName = FormFieldConstants.PROP_DATA_PREFIX + TRANSIENT_CATEGORY_ID;
        PropertyFieldDefinition categoryIdField = new PropertyFieldDefinition(TRANSIENT_CATEGORY_ID,
                    DataTypeDefinition.TEXT.getLocalName());
        categoryIdField.setLabel(TRANSIENT_CATEGORY_ID);
        categoryIdField.setDescription(TRANSIENT_CATEGORY_ID);
        categoryIdField.setProtectedField(true);
        categoryIdField.setDataKeyName(dataKeyName);
        form.addFieldDefinition(categoryIdField);
        
        // get the category id from the appropriate parent node
        NodeRef category = getRecordCategory(nodeRef);
        if (category != null)
        {
            String categoryId = (String)nodeService.getProperty(category, PROP_IDENTIFIER);
            if (categoryId != null)
            {
                form.addData(dataKeyName, categoryId);
            }
        }
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
            if (fieldDef.getName().equals("cm:title") || 
                fieldDef.getName().equals("cm:author") ||
                fieldDef.getName().equals("rma:originator") ||
                fieldDef.getName().equals("rma:publicationDate") ||
                fieldDef.getName().equals("rma:dateReceived") ||
                fieldDef.getName().equals("rma:address") ||
                fieldDef.getName().equals("rma:otherAddress"))
            {
                fieldDef.setProtectedField(true);
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Set email related fields to be protected");
    }
    
    /**
     * Marks the recordLevelDisposition property as protected to disable editing
     * 
     * @param form The Form instance
     */
    protected void protectRecordLevelDispositionPropertyField(Form form)
    {
        // iterate round existing fields and set email fields as protected
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
    
    /**
     * Retrieves the record category the given record belongs to or
     * null if the record category can not be found
     * 
     * @param record NodeRef representing the record
     * @return NodeRef of the record's category
     */
    protected NodeRef getRecordCategory(NodeRef record)
    {
        NodeRef result = null;
        
        NodeRef parent = this.nodeService.getPrimaryParent(record).getParentRef();
        if (parent != null)
        {
            QName nodeType = this.nodeService.getType(parent);
            if (this.dictionaryService.isSubClass(nodeType, TYPE_RECORD_CATEGORY))
            {
                result = parent;
            }
            else
            {
                result = getRecordCategory(parent);
            }
        }
        
        return result;
    }
}
