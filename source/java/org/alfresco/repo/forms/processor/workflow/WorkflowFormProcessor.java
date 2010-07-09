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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.AssociationFieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.AssociationFieldDefinition.Direction;
import org.alfresco.repo.forms.processor.node.ContentModelFormProcessor;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
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
 * @author Gavin Cornwell
 */
public class WorkflowFormProcessor extends ContentModelFormProcessor<WorkflowDefinition, WorkflowInstance>
{
    /** Logger */
    private static Log logger = LogFactory.getLog(WorkflowFormProcessor.class);
    
    public static final String TRANSIENT_PACKAGE_ITEMS = "packageItems";
    
    /** workflow service */
    protected WorkflowService workflowService;
    
    protected NodeService unprotectedNodeService;
    
    /**
     * Sets the workflow service
     * 
     * @param workflowService The WorkflowService instance
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    public void setSmallNodeService(NodeService nodeService)
    {
        this.unprotectedNodeService = nodeService;
    }
    
    /*
     * @see
     * org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#getLogger
     * ()
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
        WorkflowDefinition workflowDef = null;
        
        try
        {
            // item id could be the raw workflow definition name or it could be
            // in a URL friendly format
            String workflowDefName = item.getId();
            if (workflowDefName.indexOf("$") == -1)
            {
                // decode the itemId
                workflowDefName = workflowDefName.replace("jbpm_", "jbpm$");
                workflowDefName = workflowDefName.replace('_', ':');
            }
            
            // Extract the workflow definition
            workflowDef = this.workflowService.getDefinitionByName(workflowDefName);

            if (workflowDef == null) 
            { 
                throw new FormNotFoundException(item, 
                            new IllegalArgumentException("Workflow definition does not exist: " + item.getId()));
            }
        }
        catch (WorkflowException we)
        {
            throw new FormNotFoundException(item, we);
        }
        
        // return the type definition object for the requested type
        return workflowDef;
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.FilteredFormProcessor#internalGenerate
     * (java.lang.Object, java.util.List, java.util.List,
     * org.alfresco.repo.forms.Form, java.util.Map)
     */
    @Override
    protected void internalGenerate(WorkflowDefinition workflowDef, List<String> fields,
                List<String> forcedFields, Form form, Map<String, Object> context)
    {
        if (logger.isDebugEnabled()) logger.debug("Generating form for item: " + workflowDef);

        // generate the form for the workflow definition
        form.getItem().setType(workflowDef.name);
        form.getItem().setUrl("/api/workflow-definition/" + workflowDef.id);

        // get the type of the start task for the workflow definition
        TypeDefinition typeDef = workflowDef.getStartTaskDefinition().metadata;
        
        if (fields != null && fields.size() > 0)
        {
            generateSelectedFields(null, typeDef, fields, forcedFields, form);
        }
        else
        {
            // setup field definitions and data
            generateAllPropertyFields(typeDef, form);
            generateAllAssociationFields(typeDef, form);
        }
        
        // add the "packageItems" transient field
        generatePackageItemsTransientField(form);
        
        if (logger.isDebugEnabled()) logger.debug("Generating form: " + form);
    }

    /**
     * Generates the 'packageItems' field
     * 
     * @param form The Form instance to populate
     */
    protected void generatePackageItemsTransientField(Form form)
    {
        // setup basic field info
        AssociationFieldDefinition fieldDef = new AssociationFieldDefinition(TRANSIENT_PACKAGE_ITEMS, 
                    "cm:content", Direction.TARGET);
        fieldDef.setLabel("Items");
        fieldDef.setDescription("Items that are part of the workflow");
        fieldDef.setProtectedField(false);
        fieldDef.setEndpointMandatory(false);
        fieldDef.setEndpointMany(true);

        // define the data key name and set
        fieldDef.setDataKeyName(ASSOC_DATA_PREFIX + TRANSIENT_PACKAGE_ITEMS);

        // add definition to the form
        form.addFieldDefinition(fieldDef);
    }
    
    /**
     * Sets up the field definitions for all the type's properties.
     * 
     * @param typeDef The type being setup
     * @param form The Form instance to populate
     */
    protected void generateAllPropertyFields(TypeDefinition typeDef, Form form)
    {
        // iterate round the property defintions and setup field definition
        Map<QName, PropertyDefinition> propDefs = typeDef.getProperties();
        for (PropertyDefinition propDef : propDefs.values())
        {
            generatePropertyField(propDef, form, this.namespaceService);
        }

        // get all default aspects for the type and iterate round their
        // property definitions too
        List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
        for (AspectDefinition aspect : aspects)
        {
            propDefs = aspect.getProperties();
            for (PropertyDefinition propDef : propDefs.values())
            {
                generatePropertyField(propDef, form, this.namespaceService);
            }
        }
    }

    /**
     * Sets up the field definitions for all the type's associations.
     * 
     * @param typeDef The type being setup
     * @param form The Form instance to populate
     */
    protected void generateAllAssociationFields(TypeDefinition typeDef, Form form)
    {
        // iterate round the association defintions and setup field definition
        Map<QName, AssociationDefinition> assocDefs = typeDef.getAssociations();
        for (AssociationDefinition assocDef : assocDefs.values())
        {
            generateAssociationField(assocDef, form, this.namespaceService);
        }

        // get all default aspects for the type and iterate round their
        // association definitions too
        List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
        for (AspectDefinition aspect : aspects)
        {
            assocDefs = aspect.getAssociations();
            for (AssociationDefinition assocDef : assocDefs.values())
            {
                generateAssociationField(assocDef, form, this.namespaceService);
            }
        }
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
        
        NodeRef assignee = new NodeRef(data.getFieldData("assoc_bpm_assignee_added").getValue().toString());
        ArrayList<NodeRef> assigneeList = new ArrayList<NodeRef>(1);
        assigneeList.add(assignee);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assigneeList);
        
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
}
