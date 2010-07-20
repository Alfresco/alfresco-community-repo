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
import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.processor.node.ItemData;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Temporary FormProcessor implementation that can generate and persist 
 * Form objects for workflow definitions.
 *
 * @author Nick Smith
 */
public class WorkflowFormProcessor extends AbstractWorkflowFormProcessor<WorkflowDefinition, WorkflowInstance>
{
    /** Logger */
    private final static Log logger = LogFactory.getLog(WorkflowFormProcessor.class);
    
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
     * @see org.alfresco.repo.forms.processor.workflow.AbstractWorkflowFormProcessor#makeFormPersister(java.lang.Object)
     */
    @Override
    protected ContentModelFormPersister<WorkflowInstance> makeFormPersister(WorkflowDefinition item)
    {
        ItemData<WorkflowDefinition> itemData = makeItemData(item);
        return new WorkflowFormPersister(itemData, namespaceService, dictionaryService, workflowService, nodeService, logger);
    }
}
