/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.forms.processor.node.ContentModelItemData;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.QName;

/**
 * FormProcessor implementation that can generate and persist Form objects for workflow definitions.
 *
 * @since 3.4
 * @author Nick Smith
 */
public class WorkflowFormProcessor extends AbstractWorkflowFormProcessor<WorkflowDefinition, WorkflowInstance>
{
    /** Logger */
    private final static Log logger = LogFactory.getLog(WorkflowFormProcessor.class);

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getAssociationValues(java.lang.Object) */
    @Override
    protected Map<QName, Serializable> getAssociationValues(WorkflowDefinition item)
    {
        return null;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getBaseType(java.lang.Object) */
    @Override
    protected TypeDefinition getBaseType(WorkflowDefinition item)
    {
        // TODO: I'm not sure this is safe as getStartTaskDefinition() is 'optional'.
        WorkflowTaskDefinition startTask = item.getStartTaskDefinition();
        return startTask.getMetadata();
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getPropertyValues(java.lang.Object) */
    @Override
    protected Map<QName, Serializable> getPropertyValues(WorkflowDefinition item)
    {
        return null;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getTransientValues(java.lang.Object) */
    @Override
    protected Map<String, Object> getTransientValues(WorkflowDefinition item)
    {
        return Collections.<String, Object> singletonMap(
                PackageItemsFieldProcessor.KEY, Collections.emptyList());
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemType(java.lang.Object) */
    @Override
    protected String getItemType(WorkflowDefinition item)
    {
        return item.getName();
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getItemURI(java.lang.Object) */
    @Override
    protected String getItemURI(WorkflowDefinition item)
    {
        return "api/workflow-definitions/" + item.getId();
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.FilteredFormProcessor#getLogger() */
    @Override
    protected Log getLogger()
    {
        return logger;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.workflow.AbstractWorkflowFormProcessor #getTypedItemForDecodedId(java.lang.String) */
    @Override
    protected WorkflowDefinition getTypedItemForDecodedId(String itemId)
    {
        WorkflowDefinition workflowDef = workflowService.getDefinitionByName(itemId);
        if (workflowDef == null)
        {
            String msg = "Workflow definition does not exist: " + itemId;
            throw new IllegalArgumentException(msg);
        }
        return workflowDef;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.workflow.AbstractWorkflowFormProcessor#makeFormPersister(java.lang.Object) */
    @Override
    protected ContentModelFormPersister<WorkflowInstance> makeFormPersister(WorkflowDefinition item)
    {
        ContentModelItemData<WorkflowDefinition> itemData = makeItemData(item);
        return new WorkflowFormPersister(itemData, namespaceService, dictionaryService, workflowService, nodeService, behaviourFilter, logger);
    }

    /* @see org.alfresco.repo.forms.processor.workflow.AbstractWorkflowFormProcessor#getDefaultIgnoredFields() */
    @Override
    protected List<String> getDefaultIgnoredFields()
    {
        List<String> fields = super.getDefaultIgnoredFields();

        if (fields == null)
        {
            fields = new ArrayList<String>(3);
        }

        // for the workflow form processor also hide the task specific
        // description, due date and priority fields
        fields.add("bpm:description");
        fields.add("bpm:dueDate");
        fields.add("bpm:priority");

        return fields;
    }
}
