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

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.FormCreationData;
import org.alfresco.repo.forms.processor.node.ContentModelFormProcessor;
import org.alfresco.repo.forms.processor.node.ItemData;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowService;

/**
 * @author Nick Smith
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

    protected abstract ContentModelFormPersister<PersistType> makeFormPersister(ItemType item);

}
