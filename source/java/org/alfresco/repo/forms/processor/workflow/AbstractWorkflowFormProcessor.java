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

import java.util.List;
import java.util.regex.Matcher;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.repo.forms.processor.node.ContentModelFormProcessor;
import org.alfresco.repo.forms.processor.node.ItemData;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.util.ParameterCheck;

/**
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

    
    /*
     * @see
     * org.alfresco.repo.forms.processor.node.NodeFormProcessor#getTypedItem
     * (org.alfresco.repo.forms.Item)
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
        if (itemId.contains("$")==false)
        {
            decodedId = itemId.replaceFirst("_", Matcher.quoteReplacement("$"));
        }
        return decodedId;
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
