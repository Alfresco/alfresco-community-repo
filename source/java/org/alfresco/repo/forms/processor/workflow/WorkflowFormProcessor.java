/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.repo.forms.processor.node.ContentModelFormProcessor;
import org.alfresco.repo.forms.processor.node.ItemData;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Temporary FormProcessor implementation that can generate and persist 
 * Form objects for workflow definitions.
 *
 * @author Nick Smith
 */
public class WorkflowFormProcessor extends ContentModelFormProcessor<WorkflowDefinition, WorkflowInstance>
{
    /** Logger */
    private final static Log logger = LogFactory.getLog(WorkflowFormProcessor.class);
    
    /** WorkflowService */
    private WorkflowService workflowService;

    /** TyepdPropertyValueGetter */
    private TypedPropertyValueGetter valueGetter;

    private DataKeyMatcher keyMatcher;


    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#generateFields(org.alfresco.repo.forms.Form, java.util.List, org.alfresco.repo.forms.processor.FormCreationData)
     */
    @Override
    protected void populateForm(Form form, List<String> fields, FormCreationData data)
    {
        super.populateForm(form, fields, data);

        // Add package actions to FormData.
        ItemData<?> itemData = (ItemData<?>) data.getItemData();
        addPropertyDataIfRequired(WorkflowModel.PROP_PACKAGE_ACTION_GROUP, form, itemData);
        addPropertyDataIfRequired(WorkflowModel.PROP_PACKAGE_ITEM_ACTION_GROUP, form, itemData);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getAssociationValues(java.lang.Object)
     */
    @Override
    protected Map<QName, Serializable> getAssociationValues(WorkflowDefinition item)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getBaseType(java.lang.Object)
     */
    @Override
    protected TypeDefinition getBaseType(WorkflowDefinition item)
    {
        //TODO I'm not sure this is safe as getStartTaskDefinition() is 'optional'.
        WorkflowTaskDefinition startTask = item.getStartTaskDefinition();
        return startTask.getMetadata();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getPropertyValues(java.lang.Object)
     */
    @Override
    protected Map<QName, Serializable> getPropertyValues(WorkflowDefinition item)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getTransientValues(java.lang.Object)
     */
    @Override
    protected Map<String, Object> getTransientValues(WorkflowDefinition item)
    {
        return Collections.<String, Object>singletonMap(
                    PackageItemsFieldProcessor.KEY, Collections.EMPTY_LIST);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemType(java.lang.Object)
     */
    @Override
    protected String getItemType(WorkflowDefinition item)
    {
        return item.name;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemURI(java.lang.Object)
     */
    @Override
    protected String getItemURI(WorkflowDefinition item)
    {
        return "api/workflow-definitions/"+item.id;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getLogger()
     */
    @Override
    protected Log getLogger()
    {
        return logger;
    }
    
    /*
     * @see
     * org.alfresco.repo.forms.processor.node.NodeFormProcessor#getTypedItem
     * (org.alfresco.repo.forms.Item)
     */
    @Override
    protected WorkflowDefinition getTypedItem(Item item)
    {
        try
        {
            String itemId = item.getId();
            return getWorkflowDefinitionForName(itemId);
        }
        catch (Exception e)
        {
            throw new FormNotFoundException(item, e);
        }
    }

    private WorkflowDefinition getWorkflowDefinitionForName(String itemId)
    {
        String workflowDefName = decodeWorkflowDefinitionName(itemId);
        WorkflowDefinition workflowDef = workflowService.getDefinitionByName(workflowDefName);
        if (workflowDef == null) 
        { 
            String msg = "Workflow definition does not exist: " + itemId;
            throw new IllegalArgumentException(msg);
        }
        return workflowDef;
    }

    /**
     * The itemId may be in a URL/Webscript-friendly format. If so it must be converted
     * back to the proper workflow definition name.
     * 
     * @param itemId
     */
    private String decodeWorkflowDefinitionName(String itemId)
    {
        String defName = itemId;
        if (itemId.contains("$")==false)
        {
            defName = itemId.replaceFirst("_", Matcher.quoteReplacement("$"));
        }
        if (itemId.contains(":")==false)
        {
            defName = defName.replaceFirst("_", ":");
        }
        return defName;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#internalPersist(java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    @Override
    protected WorkflowInstance internalPersist(WorkflowDefinition definition, FormData data)
    {
        WorkflowBuilder builder = new WorkflowBuilder(definition, workflowService, nodeService);
        ItemData<WorkflowDefinition> itemData = makeItemData(definition);
        for (FieldData fieldData : data) 
        {
            addFieldToSerialize(builder, itemData, fieldData);
        }
        return builder.build();
    }
    
    private void addFieldToSerialize(WorkflowBuilder builder, ItemData<WorkflowDefinition> itemData, FieldData fieldData)
    {
        String dataKeyName = fieldData.getName();
        DataKeyInfo keyInfo = keyMatcher.match(dataKeyName);
        if (keyInfo == null || 
                    FieldType.TRANSIENT_PROPERTY == keyInfo.getFieldType() )
        {
            if(logger.isDebugEnabled())
                logger.debug("Ignoring unrecognized field: " + dataKeyName);
            return;
        }
        WorkflowDataKeyInfoVisitor visitor = new WorkflowDataKeyInfoVisitor(fieldData.getValue(), builder, itemData);
        keyInfo.visit(visitor);
    }

    /**
     * @param workflowService the workflowService to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#setNamespaceService(org.alfresco.service.namespace.NamespaceService)
     */
    @Override
    public void setNamespaceService(NamespaceService namespaceService)
    {
        super.setNamespaceService(namespaceService);
        this.keyMatcher = new DataKeyMatcher(namespaceService);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#setDictionaryService(org.alfresco.service.cmr.dictionary.DictionaryService)
     */
    @Override
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        super.setDictionaryService(dictionaryService);
        this.valueGetter = new TypedPropertyValueGetter(dictionaryService);
    }
    
    private class WorkflowDataKeyInfoVisitor implements DataKeyInfoVisitor<Void>
    {
        private final Object rawValue;
        private final WorkflowBuilder builder;
        private final ItemData<WorkflowDefinition> itemData;
        
        public WorkflowDataKeyInfoVisitor(Object rawValue, WorkflowBuilder builder,
                    ItemData<WorkflowDefinition> itemData)
        {
            this.rawValue = rawValue;
            this.builder = builder;
            this.itemData = itemData;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.forms.processor.workflow.DataKeyInfoVisitor#visitAssociation(org.alfresco.repo.forms.processor.workflow.DataKeyInfo)
         */
        public Void visitAssociation(DataKeyInfo info)
        {
            QName qName = info.getQName();
            if (rawValue instanceof String)
            {
                Serializable nodes = (Serializable) NodeRef.getNodeRefs((String) rawValue);
                builder.addParameter(qName, nodes);
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.forms.processor.workflow.DataKeyInfoVisitor#visitProperty(org.alfresco.repo.forms.processor.workflow.DataKeyInfo)
         */
        public Void visitProperty(DataKeyInfo info)
        {
            QName qName = info.getQName();
            Serializable propValue = valueGetter.getPropertyValueToPersist(qName, rawValue, itemData);
            builder.addParameter(qName, propValue);
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.forms.processor.workflow.DataKeyInfoVisitor#visitTransientAssociation(org.alfresco.repo.forms.processor.workflow.DataKeyInfo)
         */
        public Void visitTransientAssociation(DataKeyInfo info)
        {
            if(PackageItemsFieldProcessor.KEY.equals(info.getFieldName()))
            {
                if(rawValue instanceof String)
                {
                    builder.addPackageItems((String)rawValue);
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.forms.processor.workflow.DataKeyInfoVisitor#visitTransientProperty(org.alfresco.repo.forms.processor.workflow.DataKeyInfo)
         */
        public Void visitTransientProperty(DataKeyInfo info)
        {
            throw new FormException("This methdo should never be called!");
        }
       
    }
}
