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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.node.ContentModelFormProcessor;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

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
    
    /** Unprotected Node Service */
    private NodeService unprotectedNodeService;

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

    public void setSmallNodeService(NodeService nodeService)
    {
        this.unprotectedNodeService = nodeService;
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
    
    /*
     * @see
     * org.alfresco.repo.forms.processor.node.NodeFormProcessor#internalPersist
     * (java.lang.Object, org.alfresco.repo.forms.FormData)
     */
    @Override
    protected WorkflowInstance internalPersist(WorkflowDefinition workflowDef, final FormData data)
    {
        if (logger.isDebugEnabled()) logger.debug("Persisting form for: " + workflowDef);

        WorkflowInstance workflow = null;
        Map<QName, Serializable> params = new HashMap<QName, Serializable>(8);

        // create a package for the workflow
        NodeRef workflowPackage = this.workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
        
        // TODO: iterate through form data to collect properties, for now
        //       just hardcode the ones we know
        params.put(WorkflowModel.PROP_DESCRIPTION, 
                    (Serializable)data.getFieldData("prop_bpm_workflowDescription").getValue());
        
        // look for assignee and group assignee
        FieldData assigneeField = data.getFieldData("assoc_bpm_assignee_added");
        if (assigneeField != null)
        {
            NodeRef assignee = new NodeRef(assigneeField.getValue().toString());
            ArrayList<NodeRef> assigneeList = new ArrayList<NodeRef>(1);
            assigneeList.add(assignee);
            params.put(WorkflowModel.ASSOC_ASSIGNEE, assigneeList);
        }
        else
        {
            Object groupValue = data.getFieldData("assoc_bpm_groupAssignee_added").getValue();
            NodeRef group = new NodeRef(groupValue.toString());
            ArrayList<NodeRef> groupList = new ArrayList<NodeRef>(1);
            groupList.add(group);
            params.put(QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "groupAssignee"), 
                        groupList);
        }
        
        // add any package items
        Object items = data.getFieldData("assoc_packageItems_added").getValue();
        if (items != null)
        {
            String[] nodeRefs = StringUtils.tokenizeToStringArray(items.toString(), ",");
            for (int x = 0; x < nodeRefs.length; x++)
            {
                NodeRef item = new NodeRef(nodeRefs[x]);
                this.unprotectedNodeService.addChild(workflowPackage, item, 
                            WorkflowModel.ASSOC_PACKAGE_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                            QName.createValidLocalName((String)this.nodeService.getProperty(
                                  item, ContentModel.PROP_NAME))));
            }
        }
        
        // TODO: add any context (this could re-use alf_destination)
        
        // start the workflow to get access to the start task
        WorkflowPath path = this.workflowService.startWorkflow(workflowDef.getId(), params);
        if (path != null)
        {
            // get hold of the workflow instance for returning
            workflow = path.instance;
            
            // extract the start task
            List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1)
            {
                WorkflowTask startTask = tasks.get(0);
              
                if (logger.isDebugEnabled())
                    logger.debug("Found start task:" + startTask);
              
                if (startTask.state == WorkflowTaskState.IN_PROGRESS)
                {
                    // end the start task to trigger the first 'proper'
                    // task in the workflow
                    this.workflowService.endTask(startTask.id, null);
                }
            }
        
            if (logger.isDebugEnabled())
                logger.debug("Started workflow: " + workflowDef.getId());
        }
        
        // return the workflow just started
        return workflow;
    }
    
    /**
     * @param workflowService the workflowService to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
}
