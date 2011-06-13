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

package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTimer;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public class WorkflowObjectFactory
{
    private final static String TITLE_LABEL = "title";
    private final static String DESC_LABEL = "description";

    private final WorkflowQNameConverter qNameConverter;
    private final TenantService tenantService;
    private final MessageService messageService;
    private final DictionaryService dictionaryService;
    private final String engineId;
    
    public WorkflowObjectFactory(WorkflowQNameConverter qNameConverter,
            TenantService tenantService,
            MessageService messageService,
            DictionaryService dictionaryService,
            String engineId)
    {
        this.tenantService = tenantService;
        this.messageService = messageService;
        this.dictionaryService = dictionaryService;
        this.engineId = engineId;
        this.qNameConverter = qNameConverter;
    }

    public String buildGlobalId(String localId)
    {
        return BPMEngineRegistry.createGlobalId(engineId, localId);
    }
    
    public String getLocalEngineId(String globalId)
    {
        return BPMEngineRegistry.getLocalId(globalId);
    }
    
    public boolean isGlobalId(String globalId)
    {
        return BPMEngineRegistry.isGlobalId(globalId, engineId);
    }

    /**
     * Create a new {@link WorkflowDeployment}.
     * @param wfDef 
     * @param problems 
     * @return
     */
    public WorkflowDeployment createDeployment(WorkflowDefinition wfDef, String... problems)
    {
        WorkflowDeployment wfDeployment = new WorkflowDeployment(wfDef, problems);
        return wfDeployment;
    }

    /**
     * Create a new {@link WorkflowDefinition}.
     * @param defId 
     * @param defName 
     * @param version 
     * @param defaultTitle 
     * @param startTaskDef 
     * @param processDef
     * @return
     */
    public WorkflowDefinition createDefinition(String defId,
                String defName, int version, 
                String defaultTitle, String defaultDescription,
                WorkflowTaskDefinition startTaskDef)
    {
        checkDomain(defName);
        String actualId = buildGlobalId(defId);
        
        String actualVersion = Integer.toString(version);
        
        String displayId = getProcessKey(defName) + ".workflow";
        String title = getLabel(displayId, TITLE_LABEL, defaultTitle);
        String description = getLabel(displayId, DESC_LABEL, defaultDescription, title);
        return new WorkflowDefinition(
                    actualId, buildGlobalId(defName), actualVersion, title, description, startTaskDef);
    }
    
    public String getWorkflowDefinitionName(String defName)
    {
        String baseName= tenantService.getBaseName(defName);
        String actualName = buildGlobalId(baseName);
        return actualName;
    }
    
    public WorkflowInstance createInstance(String id,
                WorkflowDefinition definition, Map<String, Object> variables,
                boolean isActive, Date startDate, Date endDate)
    {
        checkDomain(definition.getName());
        String actualId = buildGlobalId(id);
        
        String description = (String) getVariable(variables, WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
        
        NodeRef initiator = null;
        ScriptNode initiatorSN= (ScriptNode) getVariable(variables, WorkflowConstants.PROP_INITIATOR);
        if(initiatorSN != null)
        {
            initiator = initiatorSN.getNodeRef();
        }
        
        NodeRef context = getNodeVariable(variables, WorkflowModel.PROP_CONTEXT);
        NodeRef workflowPackage= getNodeVariable(variables, WorkflowModel.ASSOC_PACKAGE);
        
        WorkflowInstance workflowInstance = new WorkflowInstance(
                    actualId, definition, 
                    description, initiator, 
                    workflowPackage, context, 
                    isActive, startDate, endDate);
        
        workflowInstance.priority = (Integer) getVariable(variables, WorkflowModel.PROP_WORKFLOW_PRIORITY);
        Date dueDate = (Date) getVariable(variables, WorkflowModel.PROP_WORKFLOW_DUE_DATE);
        if(dueDate != null)
        {
            workflowInstance.dueDate = dueDate;
        }
        
        return workflowInstance;
    }

    public WorkflowPath createPath(String id, 
                WorkflowInstance wfInstance, WorkflowNode node, 
                boolean isActive)
    {
        String actualId = buildGlobalId(id);
        return new WorkflowPath(actualId, wfInstance, node, isActive);
    }
    
    public WorkflowNode createNode(String name, 
                String definitionName, String defaultTitle,
                String defaultDescription, String type,
                boolean isTaskNode, WorkflowTransition... transitions)
    {
        String displayId = definitionName + ".node."+name;
        String title = getLabel(displayId, TITLE_LABEL, defaultTitle);
        String description = getLabel(displayId, DESC_LABEL, defaultDescription, title);
        return new WorkflowNode(name,
                    title, description, type,
                    isTaskNode, transitions);
    }
    
    public WorkflowTaskDefinition createTaskDefinition(String id, WorkflowNode node, String typeName, boolean isStart)
    {
        TypeDefinition metaData = getTaskTypeDefinition(typeName, isStart);
        if(id == null)
        {
            id = qNameConverter.mapQNameToName(metaData.getName());
        }
        return new WorkflowTaskDefinition(id, node, metaData);
    }
    
    public WorkflowTask createTask (String id,
                WorkflowTaskDefinition taskDef, String name,
                String defaultTitle, String defaultDescription,
                WorkflowTaskState state, WorkflowPath path,
                Map<QName, Serializable> properties)
    {
        String defName = path.getInstance().getDefinition().getName();
        checkDomain(defName);
        String actualId = buildGlobalId(id);
        
        String displayId =  getProcessKey(defName) + ".task." + name;
        TypeDefinition metadata = taskDef.getMetadata();
        String title = getLabel(displayId, TITLE_LABEL, defaultTitle, metadata.getTitle(), name);
        String description = getLabel(displayId, DESC_LABEL, defaultDescription, metadata.getDescription(), title);
        return new WorkflowTask(actualId,
                    taskDef, name, title, description,
                    state, path, properties);
    }
    
    public WorkflowTimer createWorkflowTimer(String id, String name, String error, 
    		Date dueDate, WorkflowPath workflowPath, WorkflowTask workflowTask)
    {
        String actualId = buildGlobalId(id);
        return new WorkflowTimer(actualId, name, workflowPath, workflowTask, dueDate, error);
    }
    
    private String getProcessKey(String defName)
    {
        String processKey = defName;
        if (isGlobalId(defName))
        {
            processKey = getLocalEngineId(defName);
        }
        return tenantService.getBaseName(processKey);
    }
    
    public String getTaskTitle(TypeDefinition typeDefinition, String defName, String defaultTitle, String name)
    {
        String displayId = getProcessKey(defName) + ".task." + name;
        return getLabel(displayId, TITLE_LABEL, defaultTitle, typeDefinition.getTitle(), name);
    }
    
    public String getTaskDescription(TypeDefinition typeDefinition, String defName, String defaultDescription, String title)
    {
        String displayId = getProcessKey(defName) + ".task." + title;
        return getLabel(displayId, DESC_LABEL, defaultDescription, typeDefinition.getTitle(), title);
    }
    

    /**
     * Get an I18N Label for a workflow item
     * 
     * @param displayId  message resource id lookup
     * @param labelKey  label to lookup (title or description)
     * @param defaultLabel  default value if not found in message resource bundle
     * @return  the label
     */
    private String getLabel(String displayId, String labelKey, String... defaults)
    {
        String keyBase = displayId.replace(":", "_");
        String key = keyBase+ "." + labelKey;
        String label = messageService.getMessage(key);
        int i = 0;
        while(label==null && i<defaults.length)
        {
            label = defaults[i];
            i++;
        }
        return label;
    }

    private NodeRef getNodeVariable(Map<String, Object> variables, QName qName)
    {
        Object obj = getVariable(variables, qName);
        if (obj==null)
        {
            return null;
        }
        if(obj instanceof ScriptNode)
        {
            ScriptNode scriptNode  = (ScriptNode) obj;
            return scriptNode.getNodeRef();
        }
        String message = "Variable "+qName+" should be of type ScriptNode but was "+obj.getClass();
        throw new WorkflowException(message);
    }
    
    private Object getVariable(Map<String, Object> variables, QName qName)
    {
        if(variables == null || qName == null)
            return null;
        String varName = qNameConverter.mapQNameToName(qName);
        return variables.get(varName);
    }
    
    private Object getVariable(Map<String, Object> variables, String key)
    {
        if(variables == null || key == null)
            return null;
        return variables.get(key);
    }
    
    /**
     * Throws exception if domain mismatch
     * @param defName
     */
    private void checkDomain(String defName)
    {
        if (tenantService.isEnabled())
        {
            String processKey = defName;
            if (isGlobalId(defName))
            {
                processKey = getLocalEngineId(defName);
            }
            tenantService.checkDomain(processKey);
        }
    }

    /**
     * Returns an anonymous {@link TypeDefinition} for the given name with all
     * the mandatory aspects applied.
     * 
     * @param name
     *            the name of the task definition.
     * @param isStart
     *            is theis a start task?
     * @return the task {@link TypeDefinition}.
     */
    public TypeDefinition getTaskFullTypeDefinition(String name, boolean isStart)
    {
        TypeDefinition typeDef = getTaskTypeDefinition(name, isStart);
        return dictionaryService.getAnonymousType(typeDef.getName());
    }
    
    /**
     * Gets the Task {@link TypeDefinition} for the given name.
     * 
     * @param name the name of the task definition.
     * @param isStart is theis a start task?
     * @return  the task {@link TypeDefinition}.
     */
    public TypeDefinition getTaskTypeDefinition(String name, boolean isStart)
    {
        TypeDefinition typeDef = null;
        if(name!=null)
        {
            QName typeName = qNameConverter.mapNameToQName(name);
            typeDef = dictionaryService.getType(typeName);
        }
        if (typeDef == null)
        {
            QName defaultTypeName = isStart? WorkflowModel.TYPE_START_TASK : WorkflowModel.TYPE_WORKFLOW_TASK;
            typeDef = dictionaryService.getType(defaultTypeName);
            if (typeDef == null)
            {
                String msg = messageService.getMessage("workflow.get.task.definition.metadata.error", name);
                throw new WorkflowException( msg);
            }
        }
        return typeDef;
    }
    
        /**
     * Map QName to jBPM variable name
     * 
     * @param name  QName
     * @return  jBPM variable name
     */
    public String mapQNameToName(QName name)
    {
        return qNameConverter.mapQNameToName(name);
    }
    
    /**
     * Map QName to jBPM variable name
     * 
     * @param name  QName
     * @return  jBPM variable name
     */
    public QName mapNameToQName(String name)
    {
        return qNameConverter.mapNameToQName(name);
    }
    
    public void clearQNameCache()
    {
        qNameConverter.clearCache();
    }
}