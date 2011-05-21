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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.repo.forms.processor.node.ContentModelFormProcessor;
import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.util.ParameterCheck;

/**
 * Abstract base class for workflow based form processors.
 * 
 * @since 3.4
 * @author Nick Smith
 *
 * @param <ItemType>
 * @param <PersistType>
 */
public abstract class AbstractWorkflowFormProcessor<ItemType, PersistType> extends ContentModelFormProcessor<ItemType, PersistType>
{
    /** WorkflowService */
    protected WorkflowService workflowService;

    protected BehaviourFilter behaviourFilter;
    
    @Override
    protected void populateForm(Form form, List<String> fields, FormCreationData data)
    {
        super.populateForm(form, fields, data);
    
        // Add package actions to FormData.
        ContentModelItemData<?> itemData = (ContentModelItemData<?>) data.getItemData();
        addPropertyDataIfRequired(WorkflowModel.PROP_PACKAGE_ACTION_GROUP, form, itemData);
        addPropertyDataIfRequired(WorkflowModel.PROP_PACKAGE_ITEM_ACTION_GROUP, form, itemData);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#internalPersist(java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    @Override
    protected PersistType internalPersist(ItemType item, FormData data)
    {
        ContentModelFormPersister<PersistType> persister = makeFormPersister(item);
        for (FieldData fieldData : data) 
        {
            persister.addField(fieldData);
        }
        return persister.persist();
    }
    
    /**
     * @param workflowService the workflowService to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    /**
     * @param behaviourFilter the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
    /*
     * @see org.alfresco.repo.forms.processor.node.NodeFormProcessor#getTypedItem(org.alfresco.repo.forms.Item)
     */
    @Override
    protected ItemType getTypedItem(Item item)
    {
        try
        {
            ParameterCheck.mandatory("item", item);
            String itemId = decodeId(item.getId());
            return getTypedItemForDecodedId(itemId);
        }
        catch (Exception e)
        {
            throw new FormNotFoundException(item, e);
        }
    }

    /**
     * The itemId may be in a URL/Webscript-friendly format. If so it must be converted
     * back to the proper id format.
     * 
     * @param itemId
     */
    private String decodeId(String itemId)
    {
        String decodedId = itemId;
        if (itemId.contains("$") == false)
        {
            decodedId = itemId.replaceFirst("_", Matcher.quoteReplacement("$"));
        }
        return decodedId;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getDefaultIgnoredFields()
     */
    @Override
    protected List<String> getDefaultIgnoredFields()
    {
        List<String> fields = super.getDefaultIgnoredFields();
        
        if (fields == null)
        {
            fields = new ArrayList<String>(20);
        }
        
        // ignore document related properties
        fields.add("cm:name");
        fields.add("cm:owner");
        fields.add("cm:creator");
        fields.add("cm:modifier");
        fields.add("cm:content");
        fields.add("cm:accessed");
        fields.add("cm:modified");
        fields.add("cm:created");
        
        // ignore task properties that shouldn't be directly edited
        fields.add("bpm:package"); 
        fields.add("bpm:pooledActors");
        fields.add("bpm:completedItems");
        fields.add("bpm:completionDate");
        fields.add("bpm:context");
        fields.add("bpm:hiddenTransitions");
        fields.add("bpm:reassignable");
        fields.add("bpm:startDate");
        fields.add("bpm:packageActionGroup");
        fields.add("bpm:packageItemActionGroup");
        fields.add("bpm:outcome");
        fields.add("bpm:taskId");
        
        return fields;
    }
    
    /**
     * Returns an implementation of {@link ContentModelFormPersister} which is
     * used to accumulate all the changes specified in the {@link Form} and then persist them.
     * 
     * @param item
     * @return
     */
    protected abstract ContentModelFormPersister<PersistType> makeFormPersister(ItemType item);

    /**
     * Returns the typed item.
     * @param itemId the decoded item Id.
     * @return
     */
    protected abstract ItemType getTypedItemForDecodedId(String itemId);

}
