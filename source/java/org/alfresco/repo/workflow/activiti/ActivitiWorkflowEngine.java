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

package org.alfresco.repo.workflow.activiti;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.BPMEngine;
import org.alfresco.repo.workflow.WorkflowAuthorityManager;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowEngine;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowNodeConverter;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery.OrderBy;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTimer;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.collections.Function;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ActivitiWorkflowEngine extends BPMEngine implements WorkflowEngine
{
    // Workflow Component Messages
    private static final String ERR_DEPLOY_WORKFLOW = "activiti.engine.deploy.workflow.error";
    private static final String ERR_IS_WORKFLOW_DEPLOYED = "activiti.engine.is.workflow.deployed.error";
    private static final String ERR_UNDEPLOY_WORKFLOW = "activiti.engine.undeploy.workflow.error";
    private static final String ERR_UNDEPLOY_WORKFLOW_UNEXISTING = "activiti.engine.undeploy.workflow.unexisting.error";
    private static final String ERR_GET_WORKFLOW_DEF = "activiti.engine.get.workflow.definition.error";
    private static final String ERR_GET_WORKFLOW_DEF_BY_ID = "activiti.engine.get.workflow.definition.by.id.error";
    private static final String ERR_GET_WORKFLOW_DEF_BY_NAME = "activiti.engine.get.workflow.definition.by.name.error";
    private static final String ERR_GET_ALL_DEFS_BY_NAME = "activiti.engine.get.all.workflow.definitions.by.name.error";
    private static final String ERR_GET_DEF_IMAGE = "activiti.engine.get.workflow.definition.image.error";
    private static final String ERR_GET_DEF_UNEXISTING_IMAGE = "activiti.engine.get.workflow.definition.unexisting.image.error";
//    private static final String ERR_GET_TASK_DEFS = "activiti.engine.get.task.definitions.error";
//    private static final String ERR_GET_PROCESS_DEF = "activiti.engine.get.process.definition.error";
    private static final String ERR_START_WORKFLOW = "activiti.engine.start.workflow.error";
    private static final String ERR_GET_WORKFLOW_INSTS = "activiti.engine.get.workflows.error";
    private static final String ERR_GET_ACTIVE_WORKFLOW_INSTS = "activiti.engine.get.active.workflows.error";
    private static final String ERR_GET_COMPLETED_WORKFLOW_INSTS = "activiti.engine.get.completed.workflows.error";
//    private static final String ERR_GET_WORKFLOW_INST_BY_ID = "activiti.engine.get.workflow.instance.by.id.error";
//    private static final String ERR_GET_PROCESS_INSTANCE = "activiti.engine.get.process.instance.error";
    private static final String ERR_GET_WORKFLOW_PATHS = "activiti.engine.get.workflow.paths.error";
//    private static final String ERR_GET_PATH_PROPERTIES = "activiti.engine.get.path.properties.error";
    private static final String ERR_CANCEL_WORKFLOW = "activiti.engine.cancel.workflow.error";
    private static final String ERR_CANCEL_UNEXISTING_WORKFLOW = "activiti.engine.cancel.unexisting.workflow.error";
    private static final String ERR_DELETE_WORKFLOW = "activiti.engine.delete.workflow.error";
    private static final String ERR_DELETE_UNEXISTING_WORKFLOW = "activiti.engine.delete.unexisting.workflow.error";
//    private static final String ERR_SIGNAL_TRANSITION = "activiti.engine.signal.transition.error";
    protected static final String ERR_FIRE_EVENT_NOT_SUPPORTED = "activiti.engine.event.unsupported";
//    private static final String ERR_FIRE_EVENT = "activiti.engine.fire.event.error";
    private static final String ERR_GET_TASKS_FOR_PATH = "activiti.engine.get.tasks.for.path.error";
//    private static final String ERR_GET_TASKS_FOR_UNEXISTING_PATH = "activiti.engine.get.tasks.for.unexisting.path.error";
    private static final String ERR_GET_TIMERS = "activiti.engine.get.timers.error";
    protected static final String ERR_FIND_COMPLETED_TASK_INSTS = "activiti.engine.find.completed.task.instances.error";
//    private static final String ERR_COMPILE_PROCESS_DEF_zip = "activiti.engine.compile.process.definition.zip.error";
//    private static final String ERR_COMPILE_PROCESS_DEF_XML = "activiti.engine.compile.process.definition.xml.error";
//    private static final String ERR_COMPILE_PROCESS_DEF_UNSUPPORTED = "activiti.engine.compile.process.definition.unsupported.error";
//    private static final String ERR_GET_activiti_ID = "activiti.engine.get.activiti.id.error";
    private static final String ERR_GET_WORKFLOW_TOKEN_INVALID = "activiti.engine.get.workflow.token.invalid";
    private static final String ERR_GET_WORKFLOW_TOKEN_NULL = "activiti.engine.get.workflow.token.is.null";
    private static final String ERR_GET_COMPANY_HOME_INVALID = "activiti.engine.get.company.home.invalid";
    private static final String ERR_GET_COMPANY_HOME_MULTIPLE = "activiti.engine.get.company.home.multiple";
    
    // Task Component Messages
    private static final String ERR_GET_ASSIGNED_TASKS = "activiti.engine.get.assigned.tasks.error";
    private static final String ERR_GET_POOLED_TASKS = "activiti.engine.get.pooled.tasks.error";
//    private static final String ERR_QUERY_TASKS = "activiti.engine.query.tasks.error";
//    private static final String ERR_GET_TASK_INST = "activiti.engine.get.task.instance.error";
    private static final String ERR_UPDATE_TASK = "activiti.engine.update.task.error";
    private static final String ERR_UPDATE_TASK_UNEXISTING = "activiti.engine.update.task.unexisting.error";
    private static final String ERR_UPDATE_START_TASK = "activiti.engine.update.starttask.illegal.error";
//    private static final String ERR_END_TASK = "activiti.engine.end.task.error";
    private static final String ERR_END_UNEXISTING_TASK = "activiti.engine.end.task.unexisting.error";
    private static final String ERR_GET_TASK_BY_ID = "activiti.engine.get.task.by.id.error";
    private static final String ERR_END_TASK_INVALID_TRANSITION = "activiti.engine.end.task.invalid.transition";

    private final static String WORKFLOW_TOKEN_SEPERATOR = "\\$";
    
    private RepositoryService repoService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private HistoryService historyService;
    private ManagementService managementService;
    private FormService formService;
    private ActivitiUtil activitiUtil;
    
    private NodeService nodeService;
    private SearchService unprotectedSearchService;
    private PersonService personService;
    private ActivitiTypeConverter typeConverter;
    private ActivitiPropertyConverter propertyConverter;
    private WorkflowAuthorityManager authorityManager;
    private WorkflowNodeConverter nodeConverter;
    private WorkflowObjectFactory factory;
    
    private MessageService messageService;
    private TenantService tenantService;
    private NamespaceService namespaceService;
    
    private String companyHomePath;
    private StoreRef companyHomeStore;
    
    
    public ActivitiWorkflowEngine()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
     @Override
     public void afterPropertiesSet() throws Exception
     {
         super.afterPropertiesSet();
         this.repoService = activitiUtil.getRepositoryService();
         this.runtimeService = activitiUtil.getRuntimeService();
         this.taskService = activitiUtil.getTaskService();
         this.formService = activitiUtil.getFormService();
         this.historyService = activitiUtil.getHistoryService();
         this.managementService = activitiUtil.getManagementService();
     }
     
    /**
    * {@inheritDoc}
    */
    public WorkflowInstance cancelWorkflow(String workflowId)
    {
        String localId = createLocalId(workflowId);
        try
        {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(localId).singleResult();
            if(processInstance == null) 
            {
                throw new WorkflowException(messageService.getMessage(ERR_CANCEL_UNEXISTING_WORKFLOW));
            }
            
            // TODO: Cancel VS delete?
            // Delete the process instance
            runtimeService.deleteProcessInstance(processInstance.getId(), WorkflowConstants.PROP_CANCELLED);
            
            // Convert historic process instance
            HistoricProcessInstance deletedInstance = historyService.createHistoricProcessInstanceQuery()
            	.processInstanceId(processInstance.getId())
            	.singleResult();
            WorkflowInstance result =  typeConverter.convert(deletedInstance);
            
            // Delete the historic process instance
            historyService.deleteHistoricProcessInstance(deletedInstance.getId());
            
            return result;
        } 
        catch(ActivitiException ae) 
        {
            String msg = messageService.getMessage(ERR_CANCEL_WORKFLOW);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public WorkflowInstance deleteWorkflow(String workflowId)
    {
        String localId = createLocalId(workflowId);
        try
        {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(localId).singleResult();
            if(processInstance == null) 
            {
                throw new WorkflowException(messageService.getMessage(ERR_DELETE_UNEXISTING_WORKFLOW));
            }
            
            // Delete the process instance
            runtimeService.deleteProcessInstance(processInstance.getId(), ActivitiConstants.DELETE_REASON_DELETED);
            
            // Convert historic process instance
            HistoricProcessInstance deletedInstance = historyService.createHistoricProcessInstanceQuery()
            	.processInstanceId(processInstance.getId())
            	.singleResult();
            WorkflowInstance result =  typeConverter.convert(deletedInstance);
            
            // Delete the historic process instance
            historyService.deleteHistoricProcessInstance(deletedInstance.getId());
            
            return result;
        } 
        catch(ActivitiException ae) 
        {
            String msg = messageService.getMessage(ERR_DELETE_WORKFLOW);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public WorkflowDeployment deployDefinition(InputStream workflowDefinition, String mimetype)
    {
        return deployDefinition(workflowDefinition, mimetype, null);
    }
    
    /**
     * {@inheritDoc}
     */
     public WorkflowDeployment deployDefinition(InputStream workflowDefinition, String mimetype, String name)
     {
         try 
         {
             String resourceName = GUID.generate() + BpmnDeployer.BPMN_RESOURCE_SUFFIX;
             Deployment deployment = repoService.createDeployment()
                 .addInputStream(resourceName, workflowDefinition)
                 .name(name)
                 .deploy();
             
             // No problems can be added to the WorkflowDeployment, warnings are
             // not exposed
             return typeConverter.convert(deployment);
         } 
         catch(ActivitiException ae) 
         {
             String msg = messageService.getMessage(ERR_DEPLOY_WORKFLOW);
             throw new WorkflowException(msg, ae);
         }
     }

    /**
    * {@inheritDoc}
    */
    public WorkflowPath fireEvent(String pathId, String event)
    {
       String message = messageService.getMessage(ERR_FIRE_EVENT_NOT_SUPPORTED);
       throw new WorkflowException(message);
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowInstance> getActiveWorkflows()
    {
        try
        {
            return getWorkflowInstances(null, true);
        }
        catch(ActivitiException ae) 
        {
            String message = messageService.getMessage(ERR_GET_ACTIVE_WORKFLOW_INSTS, "");
            throw new WorkflowException(message, ae);
        }
    }
    
    /**
    * {@inheritDoc}
    */
    @Override
    public List<WorkflowInstance> getCompletedWorkflows()
    {
        try
        {
            return getWorkflowInstances(null, false);
        }
        catch(ActivitiException ae) 
        {
            String message = messageService.getMessage(ERR_GET_COMPLETED_WORKFLOW_INSTS, "");
            throw new WorkflowException(message, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public List<WorkflowInstance> getWorkflows()
    {
        try
        {
            return getWorkflowInstances(null, null);
        }
        catch(ActivitiException ae) 
        {
            String message = messageService.getMessage(ERR_GET_WORKFLOW_INSTS, "");
            throw new WorkflowException(message, ae);
        }
    }
    
    /**
    * {@inheritDoc}
    */
    public List<WorkflowInstance> getActiveWorkflows(String workflowDefinitionId)
    {
        try
        {
            return getWorkflowInstances(workflowDefinitionId, true);
        }
        catch(ActivitiException ae) 
        {
            String message = messageService.getMessage(ERR_GET_ACTIVE_WORKFLOW_INSTS, workflowDefinitionId);
            throw new WorkflowException(message, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowDefinition> getAllDefinitions()
    {
        try 
        {
            List<ProcessDefinition> definitions = repoService.createProcessDefinitionQuery().list();
            return getValidWorkflowDefinitions(definitions);
        } 
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_WORKFLOW_DEF);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowDefinition> getAllDefinitionsByName(String workflowName)
    {
        try 
        {
            String key = factory.getProcessKey(workflowName);
            List<ProcessDefinition> definitions = repoService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .list();
            return typeConverter.convert(definitions);          
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_ALL_DEFS_BY_NAME, workflowName);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowInstance> getCompletedWorkflows(String workflowDefinitionId)
    {
        try
        {
            return getWorkflowInstances(workflowDefinitionId, false);
        }
        catch(ActivitiException ae) 
        {
            String message = messageService.getMessage(ERR_GET_COMPLETED_WORKFLOW_INSTS, workflowDefinitionId);
            throw new WorkflowException(message, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public WorkflowDefinition getDefinitionById(String workflowDefinitionId)
    {
        try
        {
            String localId = createLocalId(workflowDefinitionId);
            ProcessDefinition procDef = repoService.createProcessDefinitionQuery()
                .processDefinitionId(localId )
                .singleResult();
            return typeConverter.convert(procDef);
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_WORKFLOW_DEF_BY_ID, workflowDefinitionId);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public WorkflowDefinition getDefinitionByName(String workflowName)
    {
        try 
        {
            String key =factory.getDomainProcessKey(workflowName);
            ProcessDefinition definition = repoService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .latestVersion()
                .singleResult();
            return typeConverter.convert(definition);
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_WORKFLOW_DEF_BY_NAME, workflowName);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public byte[] getDefinitionImage(String workflowDefinitionId)
    {
       try
       {
           String processDefinitionId = createLocalId(workflowDefinitionId);
           ProcessDefinition processDefinition = repoService.createProcessDefinitionQuery()
               .processDefinitionId(processDefinitionId)
               .singleResult();
           
           if(processDefinition == null)
           {
               throw new WorkflowException(messageService.getMessage(ERR_GET_DEF_UNEXISTING_IMAGE, workflowDefinitionId));
           }
           
           String diagramResourceName = ((ReadOnlyProcessDefinition)processDefinition).getDiagramResourceName();
           if(diagramResourceName != null)
           {
               ByteArrayOutputStream out = new ByteArrayOutputStream();
               InputStream resourceInputStream = repoService.getResourceAsStream(processDefinitionId, diagramResourceName);
               // Write the resource to a ByteArrayOutpurStream
               IOUtils.copy(resourceInputStream, out);
               return out.toByteArray();
           }
           // No image was found for the process definition
           return null;
       }
       catch(IOException ioe)
       {
           String msg = messageService.getMessage(ERR_GET_DEF_IMAGE, workflowDefinitionId);
           throw new WorkflowException(msg, ioe);
       }
       catch(ActivitiException ae)
       {
           String msg = messageService.getMessage(ERR_GET_DEF_IMAGE, workflowDefinitionId);
           throw new WorkflowException(msg, ae);
       }
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowDefinition> getDefinitions()
    {
        try 
        {
            List<ProcessDefinition> definitions = repoService.createProcessDefinitionQuery()
                .latestVersion()
                .list();
            return getValidWorkflowDefinitions(definitions);
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_WORKFLOW_DEF);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public Map<QName, Serializable> getPathProperties(String pathId)
    {
        String executionId = createLocalId(pathId);
        return propertyConverter.getPathProperties(executionId);
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowTaskDefinition> getTaskDefinitions(String workflowDefinitionId)
    {
        List<WorkflowTaskDefinition> defs = new ArrayList<WorkflowTaskDefinition>();
        String processDefinitionId = createLocalId(workflowDefinitionId);
        
        // This should return all task definitions, including the start-task
        ReadOnlyProcessDefinition processDefinition =((RepositoryServiceImpl)repoService).getDeployedProcessDefinition(processDefinitionId);

        String processName = processDefinition.getName();
        factory.checkDomain(processName);
        
        // Process start task definition
        PvmActivity startEvent = processDefinition.getInitial();
        
        String startTaskName = null;
        StartFormData startFormData = formService.getStartFormData(processDefinition.getId());
        if(startFormData != null) 
        {
            startTaskName = startFormData.getFormKey();
        }
        
        // Add start task definition
        defs.add(typeConverter.getTaskDefinition(startEvent, startTaskName, processDefinition.getId()));
        
        // Now, continue through process, finding all user-tasks
        Collection<PvmActivity> taskActivities = findUserTasks(startEvent);
        for(PvmActivity act : taskActivities)
        {
            String formKey = getFormKey(act);
            defs.add(typeConverter.getTaskDefinition(act, formKey, processDefinition.getId()));
        }
        
       return defs;
    }

    private String getFormKey(PvmActivity act)
    {
        if(act instanceof ActivityImpl) 
        {
            ActivityImpl actImpl = (ActivityImpl) act;
            if (actImpl.getActivityBehavior() instanceof UserTaskActivityBehavior)        
            {
            	UserTaskActivityBehavior uta = (UserTaskActivityBehavior) actImpl.getActivityBehavior();
                TaskFormHandler handler = uta.getTaskDefinition().getTaskFormHandler();
                if(handler != null && handler instanceof DefaultTaskFormHandler)
                {
                    // We cast to DefaultTaskFormHandler since we do not configure our own
                    return ((DefaultTaskFormHandler)handler).getFormKey();
                }
                
            }
        }
        return null;
    }
    
    private boolean isReceiveTask(PvmActivity act)
    {
        if(act instanceof ActivityImpl) 
        {
            ActivityImpl actImpl = (ActivityImpl) act;
            return (actImpl.getActivityBehavior() instanceof ReceiveTaskActivityBehavior);        
        }
        return false;
    }

    private Collection<PvmActivity> findUserTasks(PvmActivity startEvent)
    {
        // Use a linked hashmap to get the task defs in the right order
        Map<String, PvmActivity> userTasks = new LinkedHashMap<String, PvmActivity>();

        // Start finding activities recursively
        findUserTasks(startEvent, userTasks);
        
        return userTasks.values();
    }
    
    private boolean isFirstActivity(PvmActivity activity, ReadOnlyProcessDefinition procDef)
    {
        if(procDef.getInitial().getOutgoingTransitions().size() == 1) 
        {
            if (procDef.getInitial().getOutgoingTransitions().get(0).getDestination().equals(activity)) 
            {
                return true;
            }
        }
        return false;
    }

    private void findUserTasks(PvmActivity currentActivity, Map<String, PvmActivity> userTasks)
    {
        // Only process activity if not already present to prevent endless loops
        if(!userTasks.containsKey(currentActivity.getId()))
        {
            if(isUserTask(currentActivity)) 
            {
                userTasks.put(currentActivity.getId(), currentActivity);
            }
            
            // Process outgoing transitions
            if(currentActivity.getOutgoingTransitions() != null)
            {
                for(PvmTransition transition : currentActivity.getOutgoingTransitions())
                {
                    if(transition.getDestination() != null)
                    {
                        findUserTasks(transition.getDestination(), userTasks);
                    }
                }
            }
        }
    }

    private boolean isUserTask(PvmActivity currentActivity)
    {
        // TODO: Validate if this is the best way to find out an activity is a usertask
        String type = (String) currentActivity.getProperty(ActivitiConstants.NODE_TYPE);
        if(type != null && type.equals(ActivitiConstants.USER_TASK_NODE_TYPE))
        {
            return true;
        }
        return false;
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowTask> getTasksForWorkflowPath(String pathId)
    {
        try
        {
            // Extract the Activiti ID from the path
            String executionId = getExecutionIdFromPath(pathId);
            if (executionId == null)
            {
                throw new WorkflowException(messageService.getMessage(ERR_GET_WORKFLOW_TOKEN_INVALID, pathId));
            }

            // Check if the execution exists
            Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
            if (execution == null)
            {
                throw new WorkflowException(messageService.getMessage(ERR_GET_WORKFLOW_TOKEN_NULL, pathId));
            }

            // Check if workflow's start task has been completed. If not, return
            // the virtual task
            // Otherwise, just return the runtime Activiti tasks
            String processInstanceId = execution.getProcessInstanceId();
            ArrayList<WorkflowTask> resultList = new ArrayList<WorkflowTask>();
            if (typeConverter.isStartTaskActive(processInstanceId))
            {
                resultList.add(typeConverter.getVirtualStartTask(processInstanceId, true));
            }
            else
            {
                List<Task> tasks = taskService.createTaskQuery().executionId(executionId).list();
                for (Task task : tasks)
                {
                    resultList.add(typeConverter.convert(task));
                }
            }
            return resultList;
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_TASKS_FOR_PATH, pathId);
            throw new WorkflowException(msg, ae);
        }
    }

    protected String getExecutionIdFromPath(String workflowPath) 
    {
        if(workflowPath != null) 
        {
            String[] parts = workflowPath.split(WORKFLOW_TOKEN_SEPERATOR);
            if(parts.length != 2) 
            {
                throw new WorkflowException(messageService.getMessage(ERR_GET_WORKFLOW_TOKEN_INVALID, workflowPath));
            }
            return parts[1];
        }
        return null;
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowTimer> getTimers(String workflowId)
    {
    	 try 
         {
             List<WorkflowTimer> timers = new ArrayList<WorkflowTimer>();
             
             String processInstanceId = createLocalId(workflowId);
             List<Job> timerJobs = managementService.createJobQuery()
             	.processInstanceId(processInstanceId)
             	.timers()
             	.list();
             
             // Only fetch process-instance when timers are available, to prevent extra unneeded query
             ProcessInstance jobsProcessInstance = null;
             if(timerJobs.size() > 0)
             {
            	 // Reuse the process-instance, is used from WorkflowPath creation
            	 jobsProcessInstance = runtimeService.createProcessInstanceQuery()
              		.processInstanceId(processInstanceId).singleResult();
             }
             
             // Convert the timerJobs to WorkflowTimers
             for(Job job : timerJobs)
             {
            	 Execution jobExecution = runtimeService.createExecutionQuery()
            	 	.executionId(job.getExecutionId()).singleResult();
            	 
            	 WorkflowPath path = typeConverter.convert(jobExecution, jobsProcessInstance);
            	 WorkflowTask workflowTask = getTaskForTimer(job, jobsProcessInstance, jobExecution);
            	 
            	 WorkflowTimer workflowTimer = factory.createWorkflowTimer(job.getId(), job.getId(),
            			 job.getExceptionMessage(), job.getDuedate(), path, workflowTask);
            	 timers.add(workflowTimer);
             }
             
             return timers;
             
         }
         catch (ActivitiException ae)
         {
             String msg = messageService.getMessage(ERR_GET_TIMERS, workflowId);
             throw new WorkflowException(msg, ae);
         }
    }

    private WorkflowTask getTaskForTimer(Job job, ProcessInstance processInstance, Execution jobExecution) 
    {
        if (job instanceof TimerEntity) 
        {
			ReadOnlyProcessDefinition def = activitiUtil.getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
			List<String> activeActivityIds = runtimeService.getActiveActivityIds(jobExecution.getId());
			
			if(activeActivityIds.size() == 1)
			{
				PvmActivity targetActivity = def.findActivity(activeActivityIds.get(0));
				if(targetActivity != null)
				{
					// Only get tasks of active activity is a user-task 
					String activityType = (String) targetActivity.getProperty(ActivitiConstants.NODE_TYPE);
					if(ActivitiConstants.USER_TASK_NODE_TYPE.equals(activityType))
					{
						Task task = taskService.createTaskQuery().executionId(job.getExecutionId()).singleResult();
						return typeConverter.convert(task);
					}
				}
			}
		}
		return null;
	}

	/**
    * {@inheritDoc}
    */
    public WorkflowInstance getWorkflowById(String workflowId)
    {
        try 
        {
            WorkflowInstance instance = null;
            
            String processInstanceId = createLocalId(workflowId);
            ProcessInstance processIntance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
            
            if(processIntance != null) 
            {
                instance = typeConverter.convert(processIntance);
            } 
            else 
            {
                // The process instance can be finished
                HistoricProcessInstance historicInstance = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
                
                if(historicInstance != null) 
                {
                    instance = typeConverter.convert(historicInstance);
                }
            }
            return instance;
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_WORKFLOW_DEF);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowPath> getWorkflowPaths(String workflowId)
    {
        try 
        {           
            String processInstanceId = createLocalId(workflowId);
            
            List<Execution> executions = runtimeService
                .createExecutionQuery()
                .processInstanceId(processInstanceId)
                .list();
            
            return typeConverter.convertExecution(executions);
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_WORKFLOW_PATHS);
              throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowInstance> getWorkflows(String workflowDefinitionId)
    {
        try
        {
            return getWorkflowInstances(workflowDefinitionId, null);
        }
        catch(ActivitiException ae) 
        {
            String message = messageService.getMessage(ERR_GET_WORKFLOW_INSTS, workflowDefinitionId);
            throw new WorkflowException(message, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public boolean isDefinitionDeployed(InputStream workflowDefinition, String mimetype)
    {
        String key = null;
        try
        {
            key = getProcessKey(workflowDefinition);
        }
        catch (Exception e) 
        {
            throw new WorkflowException(e.getMessage(), e);
        }
        
        try 
        {           
            long count = repoService.createProcessDefinitionQuery()
                .processDefinitionKey(key )
                .count();
            return count>0;
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_IS_WORKFLOW_DEPLOYED);
            throw new WorkflowException(msg, ae);
        }
    }

    private String getProcessKey(InputStream workflowDefinition) throws Exception
    {
        try 
        {
            InputSource inputSource = new InputSource(workflowDefinition);
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document document = parser.getDocument();
            NodeList elemnts = document.getElementsByTagName("process");
            if (elemnts.getLength() < 1)
            {
                throw new IllegalArgumentException("The input stream does not contain a process definition!");
            }
            NamedNodeMap attributes = elemnts.item(0).getAttributes();
            Node idAttrib = attributes.getNamedItem("id");
            if (idAttrib == null)
            {
                throw new IllegalAccessError("The process definition does not have an id!");
            }
            String key = idAttrib.getNodeValue();
            return factory.getDomainProcessKey(key);
        }
        finally
        {
            workflowDefinition.close();
        }
    }

    /**
    * {@inheritDoc}
    */
    public WorkflowPath signal(String pathId, String transitionId)
    {
        String execId = createLocalId(pathId);
        Execution oldExecution = activitiUtil.getExecution(execId);
        runtimeService.signal(execId);
        Execution execution = activitiUtil.getExecution(execId);
        if(execution !=null)
        {
            return typeConverter.convert(execution);
        }
        return typeConverter.buildCompletedPath(execId, oldExecution.getProcessInstanceId());
    }

    /**
    * {@inheritDoc}
    */
    public WorkflowPath startWorkflow(String workflowDefinitionId, Map<QName, Serializable> parameters)
    {
        try
        {
            String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
            Authentication.setAuthenticatedUserId(currentUserName);
            
            String processDefId = createLocalId(workflowDefinitionId);
            
            // Set start task properties. This should be done before instance is started, since it's id will be used
            Map<String, Object> variables = propertyConverter.getStartVariables(processDefId, parameters);
            variables.put(WorkflowConstants.PROP_CANCELLED, Boolean.FALSE);
            
            // Add company home
            Object companyHome = nodeConverter.convertNode(getCompanyHome());
            variables.put(WorkflowConstants.PROP_COMPANY_HOME, companyHome);
             
            // Add the initiator
            NodeRef initiator = getPersonNodeRef(currentUserName);
            if (initiator != null)
            {
                variables.put(WorkflowConstants.PROP_INITIATOR, nodeConverter.convertNode(initiator));
                // Also add the initiator home reference, if one exists
                NodeRef initiatorHome = (NodeRef) nodeService.getProperty(initiator, ContentModel.PROP_HOMEFOLDER);
                if (initiatorHome != null)
                {
                    variables.put(WorkflowConstants.PROP_INITIATOR_HOME, nodeConverter.convertNode(initiatorHome));
                }
            }
            
            // Start the process-instance
            ProcessInstance instance = runtimeService.startProcessInstanceById(processDefId, variables);
            if(instance.isEnded()) 
            {
                return typeConverter.buildCompletedPath(instance.getId(), instance.getId());
            } 
            else
            {
                WorkflowPath path = typeConverter.convert((Execution)instance);        
                endStartTaskAutomatically(path, instance);
                return path;
            }
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_START_WORKFLOW, workflowDefinitionId);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
     * @param path
     * @param instance
     */
    private void endStartTaskAutomatically(WorkflowPath path, ProcessInstance instance)
    {
        // Check if StartTask Needs to be ended automatically
        WorkflowDefinition definition = path.getInstance().getDefinition();
        TypeDefinition metadata = definition.getStartTaskDefinition().getMetadata();
        Set<QName> aspects = metadata.getDefaultAspectNames();
        if(aspects.contains(WorkflowModel.ASPECT_END_AUTOMATICALLY))
        {
            String taskId = ActivitiConstants.START_TASK_PREFIX + instance.getId();
            endStartTask(taskId);
        }
    }
    
    /**
    * {@inheritDoc}
    */
    public void undeployDefinition(String workflowDefinitionId)
    {
        try 
        {
            String procDefId = createLocalId(workflowDefinitionId);
            ProcessDefinition procDef = repoService.createProcessDefinitionQuery()
                .processDefinitionId(procDefId)
                .singleResult();
            if (procDef == null)     
            {
                String msg = messageService.getMessage(ERR_UNDEPLOY_WORKFLOW_UNEXISTING, workflowDefinitionId);
                throw new WorkflowException(msg);
            }
            String deploymentId = procDef.getDeploymentId();
            repoService.deleteDeployment(deploymentId);
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_UNDEPLOY_WORKFLOW, workflowDefinitionId);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasWorkflowImage(String workflowInstanceId)
    {
        boolean hasImage = false;
        
        String processInstanceId = createLocalId(workflowInstanceId);
        ExecutionEntity pi = (ExecutionEntity) runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();

        // If the process is finished, there is no diagram available
        if (pi != null)
        {
            // Fetch the process-definition. Not using query API, since the returned
            // processdefinition isn't initialized with all activities
            ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repoService)
                        .getDeployedProcessDefinition(pi.getProcessDefinitionId());

            hasImage = (processDefinition != null && processDefinition.isGraphicalNotationDefined()); 
        }
        
        return hasImage;
    }
    
    /**
     * {@inheritDoc}
     */
    public InputStream getWorkflowImage(String workflowInstanceId)
    {
        String processInstanceId = createLocalId(workflowInstanceId);
        ExecutionEntity pi = (ExecutionEntity) runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();

        // If the process is finished, there is no diagram available
        if (pi != null)
        {
            // Fetch the process-definition. Not using query API, since the returned
            // processdefinition isn't initialized with all activities
            ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repoService)
                        .getDeployedProcessDefinition(pi.getProcessDefinitionId());

            if (processDefinition != null && processDefinition.isGraphicalNotationDefined()) 
            { 
                return ProcessDiagramGenerator
                        .generateDiagram(processDefinition,
                                    ActivitiConstants.PROCESS_INSTANCE_IMAGE_FORMAT,
                                    runtimeService.getActiveActivityIds(processInstanceId)); 
            }
        }
        return null;
    }

    /**
     * Converts the given list of {@link ProcessDefinition}s to a list of {@link WorkflowDefinition}s
     * that have a valid domain.
     * @param definitions
     */
    private List<WorkflowDefinition> getValidWorkflowDefinitions(List<ProcessDefinition> definitions)
    {
        return typeConverter.filterByDomainAndConvert(definitions, new Function<ProcessDefinition, String>()
        {
            public String apply(ProcessDefinition value)
            {
                return value.getKey();
            }
        });
    }
    
    /**
     * Converts the given list of {@link Task}s to a list of {@link WorkflowTask}s
     * that have a valid domain.
     * @param tasks
     */
    private List<WorkflowTask> getValidWorkflowTasks(List<Task> tasks)
    {
        return typeConverter.filterByDomainAndConvert(tasks, new Function<Task, String>()
        {
            public String apply(Task task)
            {
                //TODO This probably isn't very performant!
                String defId = task.getProcessDefinitionId();
                ProcessDefinition definition = repoService.createProcessDefinitionQuery().processDefinitionId(defId)
                        .singleResult();
                return definition.getKey();
            }
        });
    }

    /**
     * Converts the given list of {@link Task}s to a list of {@link WorkflowTask}s
     * that have a valid domain.
     * @param tasks
     */
    private List<WorkflowTask> getValidHistoricTasks(List<HistoricTaskInstance> tasks)
    {
        return typeConverter.filterByDomainAndConvert(tasks, new Function<HistoricTaskInstance, String>()
        {
            public String apply(HistoricTaskInstance task)
            {
                // TODO This probably isn't very performant!
                String defId = task.getProcessDefinitionId();
                ProcessDefinition definition = repoService.createProcessDefinitionQuery().processDefinitionId(defId)
                        .singleResult();
                return definition.getKey();
            }
        });
    }
    
    /**
     * Gets the Company Home
     * 
     * @return company home node ref
     */
    private NodeRef getCompanyHome()
    {
        if (tenantService.isEnabled())
        {
            try
            {
                return tenantService.getRootNode(nodeService, unprotectedSearchService, namespaceService,
                            companyHomePath, nodeService.getRootNode(companyHomeStore));
            }
            catch (RuntimeException re)
            {
                String msg = messageService.getMessage(ERR_GET_COMPANY_HOME_INVALID, companyHomePath);
                throw new IllegalStateException(msg, re);
            }
        }
        else
        {
            List<NodeRef> refs = unprotectedSearchService.selectNodes(nodeService.getRootNode(companyHomeStore),
                        companyHomePath, null, namespaceService, false);
            if (refs.size() != 1)
            {
                String msg = messageService.getMessage(ERR_GET_COMPANY_HOME_MULTIPLE, companyHomePath, refs.size());
                throw new IllegalStateException(msg);
            }
            return refs.get(0);
        }
    }
    
    /**
     * Gets an Alfresco Person reference for the given name.
     * 
     * @param name the person name
     * @return the Alfresco person. Returns null, if no person is found with the
     *         given name.
     */
    private NodeRef getPersonNodeRef(String name)
    {
        NodeRef authority = null;
        if (name != null)
        {
            if (personService.personExists(name))
            {
                authority = personService.getPerson(name);
            }
        }
        return authority;
    }
 
    /**
     * @param propertyConverter the propertyConverter to set
     */
    public void setPropertyConverter(ActivitiPropertyConverter propertyConverter)
    {
        this.propertyConverter = propertyConverter;
    }
    
    /**
     * Sets the Node Service
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the Company Home Path
     * 
     * @param companyHomePath
     */
    public void setCompanyHomePath(String companyHomePath)
    {
        this.companyHomePath = companyHomePath;
    }

    /**
     * Sets the Company Home Store
     * 
     * @param companyHomeStore
     */
    public void setCompanyHomeStore(String companyHomeStore)
    {
        this.companyHomeStore = new StoreRef(companyHomeStore);
    }
    
    /**
     * Set the unprotected search service - so we can find the node ref for
     * company home when folk do not have read access to company home TODO:
     * review use with DC
     * 
     * @param unprotectedSearchService
     */
    public void setUnprotectedSearchService(SearchService unprotectedSearchService)
    {
        this.unprotectedSearchService = unprotectedSearchService;
    }
    
    /**
     * Sets the Person Service
     * 
     * @param personService
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Sets the Authority DAO
    /**
     * @param authorityManager the authorityManager to set
     */
    public void setAuthorityManager(WorkflowAuthorityManager authorityManager)
    {
        this.authorityManager = authorityManager;
    }

    ///////////// Task Component //////////
    
    /**
    * {@inheritDoc}
    */
    public WorkflowTask endTask(String taskId, String transition)
    {
        String localTaskId = createLocalId(taskId);
        // Check if the task is a virtual start task
        if(localTaskId.startsWith(ActivitiConstants.START_TASK_PREFIX))
        {
            return endStartTask(localTaskId);
        }
        
        return endNormalTask(taskId, localTaskId, transition);
    }

    private WorkflowTask endNormalTask(String taskId, String localTaskId, String transition)
    {
        // Retrieve task
        Task task = taskService.createTaskQuery().taskId(localTaskId).singleResult();
        
        if(task == null)
        {
            String msg = messageService.getMessage(ERR_END_UNEXISTING_TASK, taskId);
            throw new WorkflowException(msg);
        }
        setOutcome(task, transition);
        taskService.complete(localTaskId);
        // The task should have a historicTaskInstance
        HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).singleResult();
        return typeConverter.convert(historicTask);
    }

    private void setOutcome(Task task, String transition)
    {
        String outcomeValue = ActivitiConstants.DEFAULT_TRANSITION_NAME;
        HashMap<QName, Serializable> updates = new HashMap<QName, Serializable>();

        boolean isDefaultTransition = transition == null || ActivitiConstants.DEFAULT_TRANSITION_NAME.equals(transition);

        Map<QName, Serializable> properties = propertyConverter.getTaskProperties(task);
        QName outcomePropName = (QName) properties.get(WorkflowModel.PROP_OUTCOME_PROPERTY_NAME);
        if(outcomePropName !=null)
        {
            if(isDefaultTransition == false)
            {
                outcomeValue = transition;
                Serializable transitionValue = propertyConverter.convertValueToPropertyType(task, transition, outcomePropName);
                updates.put(outcomePropName, transitionValue);
            }
            else
            {
                Serializable rawOutcome = properties.get(outcomePropName);
                if(rawOutcome != null)
                {
                    outcomeValue = DefaultTypeConverter.INSTANCE.convert(String.class, rawOutcome);
                }
            }
        }
        else if (isDefaultTransition==false)
        {
            // Only 'Next' is supported as transition.
            String taskId = createGlobalId(task.getId());
            String msg = messageService.getMessage(ERR_END_TASK_INVALID_TRANSITION, transition, taskId, ActivitiConstants.DEFAULT_TRANSITION_NAME);
            throw new WorkflowException(msg);
        }
        updates.put(WorkflowModel.PROP_OUTCOME, outcomeValue);
        propertyConverter.updateTask(task, updates, null, null);
    }

    private WorkflowTask endStartTask(String localTaskId)
    {
        // We don't end a task, we set a variable on the process-instance 
        // to indicate that it's started
        String processInstanceId = localTaskId.replace(ActivitiConstants.START_TASK_PREFIX, "");
        if(false == typeConverter.isStartTaskActive(processInstanceId))
        {
            return typeConverter.getVirtualStartTask(processInstanceId, false);
        }
        
        // Set start task end date on the process
        runtimeService.setVariable(processInstanceId, ActivitiConstants.PROP_START_TASK_END_DATE, new Date());
        
        // Check if the current activity is a signalTask and the first activity in the process,
        // this is a workaround for processes without any task/waitstates that should otherwise end
        // when they are started.
        ProcessInstance processInstance = activitiUtil.getProcessInstance(processInstanceId);
        String currentActivity = ((ExecutionEntity)processInstance).getActivityId();
        
        ReadOnlyProcessDefinition procDef = activitiUtil.getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
        PvmActivity activity = procDef.findActivity(currentActivity);
        if(isReceiveTask(activity) && isFirstActivity(activity, procDef)) 
        {
            // Signal the process to start flowing, beginning from the recieve task
            runtimeService.signal(processInstanceId);
            
            // It's possible the process has ended after signalling the receive task
        }
        // Return virtual start task for the execution, it's safe to use the
        // processInstanceId
        return typeConverter.getVirtualStartTask(processInstanceId, false);
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowTask> getAssignedTasks(String authority, WorkflowTaskState state)
    {
        try
        {
            if(state == WorkflowTaskState.IN_PROGRESS)
            {
                List<Task> tasks = taskService.createTaskQuery()
                    .taskAssignee(authority)
                    .list();
                return typeConverter.convert(tasks);
            }
            else
            {
                List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                    .taskAssignee(authority)
                    .list();
                return typeConverter.convert(historicTasks);
            }
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_ASSIGNED_TASKS);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
    * {@inheritDoc}
    */
    public List<WorkflowTask> getPooledTasks(List<String> authorities)
    {
        try 
        {
            if (authorities != null && authorities.size() > 0) 
            {
                // As an optimisation, we assume the first authority CAN be a user. All the 
                // others are groups to which the user (or group) belongs. This way, we don't have to
                // check for the type of the authority.
                
                String firstAuthority = authorities.get(0);
                // Use a map, can be that a task has multiple candidate-groups, which are inside the list
                // of authorities
                Map<String, Task> resultingTasks = new HashMap<String, Task>();
                if (authorityManager.isUser(firstAuthority))
                {
                    // Candidate user
                    addTasksForCandidateUser(firstAuthority, resultingTasks);
                } 
                else
                {
                    // Candidate group
                    addTasksForCandidateGroup(firstAuthority, resultingTasks);
                }
                for (int i=1; i<authorities.size(); i++) 
                {
                    // All folowing authorities are groups, just add the cadidate-group tasks
                    addTasksForCandidateGroup(authorities.get(i), resultingTasks);
                }
                
                List<Task> tasks = new ArrayList<Task>();
                
                // Only tasks that have NO assignee, should be returned
                for(Task task : resultingTasks.values()) 
                {
                    if(task.getAssignee() == null) 
                    {
                        tasks.add(task);
                    }
                }
                return typeConverter.convert(tasks);
            }
            
            return Collections.emptyList();
        }
        catch(ActivitiException ae)
        {
            String authorityString = null;
            if(authorities != null)
            {
                authorityString = StringUtils.join(authorities.iterator(), ", ");
            }
            String msg = messageService.getMessage(ERR_GET_POOLED_TASKS, authorityString);
            throw new WorkflowException(msg, ae);
        }
    }

    private void addTasksForCandidateGroup(String groupName, Map<String, Task> resultingTasks)
    {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(groupName).list();
        for(Task task : tasks)
        {
            resultingTasks.put(task.getId(), task);
        }
    }

    private void addTasksForCandidateUser(String userName, Map<String, Task> resultingTasks)
    {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(userName).list();
        for(Task task : tasks)
        {
            resultingTasks.put(task.getId(), task);
        }
    }

    /**
    * {@inheritDoc}
    */
    public WorkflowTask getTaskById(String taskId)
    {
        try
        {
            String localId = createLocalId(taskId);
            if(localId.startsWith(ActivitiConstants.START_TASK_PREFIX)) 
            {
                String processInstanceId = localId.replace(ActivitiConstants.START_TASK_PREFIX ,"");
                return typeConverter.getVirtualStartTask(processInstanceId, null);
            } 
            else
            {
                Task task = activitiUtil.getTaskInstance(localId);
                if(task != null) 
                {
                    return typeConverter.convert(task);
                }
                HistoricTaskInstance historicTask = activitiUtil.getHistoricTaskInstance(localId);
                return typeConverter.convert(historicTask);
            }
        }
        catch (ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_GET_TASK_BY_ID);
            throw new WorkflowException(msg, ae);
        }
    }

    /**
     * {@inheritDoc}
     */
     public List<WorkflowTask> queryTasks(WorkflowTaskQuery query)
     {
         ArrayList<WorkflowTask> result = new ArrayList<WorkflowTask>();
         WorkflowTaskState taskState = query.getTaskState();
         if(WorkflowTaskState.COMPLETED.equals(taskState) == false)
         {
             result.addAll(queryRuntimeTasks(query));           
         }       
         
         // Depending on the state, history should be included/excluded as wel
         if(WorkflowTaskState.IN_PROGRESS.equals(taskState) == false)
         {
             result.addAll(queryHistoricTasks(query));
             result.addAll(queryStartTasks(query));
         }
         return result;
     }
     
     private List<WorkflowTask> queryRuntimeTasks(WorkflowTaskQuery query)
     {
        // Runtime-tasks only exist on process-instances that are active
        // so no use in querying runtime tasks if not active
        if (!Boolean.FALSE.equals(query.isActive()))
        {
            // Add task name
            TaskQuery taskQuery = taskService.createTaskQuery();
            if (query.getTaskName() != null)
            {
                // Task 'key' is stored as variable on task
                String formKey = query.getTaskName().toPrefixString(namespaceService);
                taskQuery.taskVariableValueEquals(ActivitiConstants.PROP_TASK_FORM_KEY, formKey);
            }

            if (query.getProcessId() != null)
            {
                String processInstanceId = createLocalId(query.getProcessId());
                taskQuery.processInstanceId(processInstanceId);
            }

            if (query.getProcessName() != null)
            {
                String processName = getProcessNameMTSafe(query.getProcessName());
                taskQuery.processDefinitionKey(processName);
            }
            
            if (query.getWorkflowDefinitionName() != null)
            {
                String processName = factory.getProcessKey(query.getWorkflowDefinitionName());
                taskQuery.processDefinitionKey(processName);
            }

            if (query.getActorId() != null)
            {
                taskQuery.taskAssignee(query.getActorId());
            }

            if (query.getTaskId() != null)
            {
                String taskId = createLocalId(query.getTaskId());
                taskQuery.taskId(taskId);
            }

            // Custom task properties
            if (query.getTaskCustomProps() != null)
            {
                addTaskPropertiesToQuery(query.getTaskCustomProps(), taskQuery);
            }

            if (query.getProcessCustomProps() != null)
            {
                addProcessPropertiesToQuery(query.getProcessCustomProps(), taskQuery);
            }
            // Add ordering
            if (query.getOrderBy() != null)
            {
                WorkflowTaskQuery.OrderBy[] orderBy = query.getOrderBy();
                orderQuery(taskQuery, orderBy);
            }

            List<Task> results;
            int limit = query.getLimit();
            if (limit > 0)
            {
                results = taskQuery.listPage(0, limit);
            }
            else
            {
                results = taskQuery.list();
            }
            return getValidWorkflowTasks(results);
        }
        return new ArrayList<WorkflowTask>();
     }
     
     private void addProcessPropertiesToQuery(
             Map<QName, Object> processCustomProps, TaskQuery taskQuery) 
     {
    	 for(Entry<QName, Object> customProperty : processCustomProps.entrySet()) 
         {
             String name =factory.mapQNameToName(customProperty.getKey());
             taskQuery.processVariableValueEquals(name, customProperty.getValue());
         }
     }
     
     private String getProcessNameMTSafe(QName processNameQName)
     {
         String key = processNameQName.toPrefixString(namespaceService);
         return factory.getProcessKey(key);
     }
     
     private void orderQuery(TaskQuery taskQuery, OrderBy[] orderBy) 
     {
         for (WorkflowTaskQuery.OrderBy orderByPart : orderBy)
         {
             if (orderByPart == WorkflowTaskQuery.OrderBy.TaskActor_Asc)
             {
                 taskQuery.orderByTaskAssignee().asc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskActor_Desc)
             {
                 taskQuery.orderByTaskAssignee().desc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskCreated_Asc)
             {
                 taskQuery.orderByTaskCreateTime().asc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskCreated_Desc)
             {
                 taskQuery.orderByTaskCreateTime().desc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskDue_Asc)
             {
                 // TODO: order by dueDate? It's a task-variable
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskDue_Desc)
             {
                 // TODO: order by duedate? It's a task-variable
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskId_Asc)
             {
                 taskQuery.orderByTaskId().asc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskId_Desc)
             {
                 taskQuery.orderByTaskId().desc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskName_Asc)
             {
                 taskQuery.orderByTaskName().asc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskName_Desc)
             {
                 taskQuery.orderByTaskName().desc();
             }
             // All workflows are active, no need to order on WorkflowTaskQuery.OrderBy.TaskState_Asc
         }
     }
     
     private void orderQuery(HistoricTaskInstanceQuery taskQuery, OrderBy[] orderBy) 
     {
         for (WorkflowTaskQuery.OrderBy orderByPart : orderBy)
         {
             if (orderByPart == WorkflowTaskQuery.OrderBy.TaskActor_Asc)
             {
                 taskQuery.orderByTaskAssignee().asc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskActor_Desc)
             {
                 taskQuery.orderByTaskAssignee().desc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskCreated_Asc)
             {
                 taskQuery.orderByHistoricActivityInstanceStartTime().asc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskCreated_Desc)
             {
                 taskQuery.orderByHistoricActivityInstanceStartTime().desc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskDue_Asc)
             {
                 // TODO: order by dueDate? It's a task-variable
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskDue_Desc)
             {
                 // TODO: order by duedate? It's a task-variable
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskId_Asc)
             {
                 taskQuery.orderByTaskId().asc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskId_Desc)
             {
                 taskQuery.orderByTaskId().desc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskName_Asc)
             {
                 taskQuery.orderByTaskName().asc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskName_Desc)
             {
                 taskQuery.orderByTaskName().desc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskState_Asc)
             {
                 taskQuery.orderByHistoricTaskInstanceEndTime().asc();
             }
             else if (orderByPart == WorkflowTaskQuery.OrderBy.TaskState_Desc)
             {
                 taskQuery.orderByHistoricTaskInstanceEndTime().asc();
             }
         }
     }

     private void addTaskPropertiesToQuery(Map<QName, Object> taskCustomProps, TaskQuery taskQuery)
     {
         for(Entry<QName, Object> customProperty : taskCustomProps.entrySet()) 
         {
             String name =factory.mapQNameToName(customProperty.getKey());
             taskQuery.taskVariableValueEquals(name, customProperty.getValue());
         }
     }

     private List<WorkflowTask> queryHistoricTasks(WorkflowTaskQuery query)
     {
        HistoricTaskInstanceQuery historicQuery = historyService.createHistoricTaskInstanceQuery().finished();

        if (query.getTaskId() != null)
        {
            String taskId = createLocalId(query.getTaskId());
            historicQuery.taskId(taskId);
        }

        if (query.getProcessId() != null)
        {
            String processInstanceId = createLocalId(query.getProcessId());
            historicQuery.processInstanceId(processInstanceId);
        }

        if (query.getTaskName() != null)
        {
            historicQuery.taskDefinitionKey(query.getTaskName().toPrefixString());
        }

        if (query.getActorId() != null)
        {
            historicQuery.taskAssignee(query.getActorId());
        }

        if (query.getProcessName() != null)
        {
            String processName = getProcessNameMTSafe(query.getProcessName());
            historicQuery.processDefinitionKey(processName);
        }
        
        if (query.getWorkflowDefinitionName() != null)
        {
            String processName = factory.getProcessKey(query.getWorkflowDefinitionName());
            historicQuery.processDefinitionKey(processName);
        }

        if (query.getTaskCustomProps() != null)
        {
            addTaskPropertiesToQuery(query.getTaskCustomProps(), historicQuery);
        }

        if (query.getProcessCustomProps() != null)
        {
            addProcessPropertiesToQuery(query.getProcessCustomProps(), historicQuery);
        }

        if (query.isActive() != null)
        {
            if (query.isActive())
            {
                historicQuery.processUnfinished();
            }
            else
            {
                historicQuery.processFinished();
            }
        }

        // Order query
        if (query.getOrderBy() != null)
        {
            orderQuery(historicQuery, query.getOrderBy());
        }

        List<HistoricTaskInstance> results;
        int limit = query.getLimit();
        if (limit > 0)
        {
            results = historicQuery.listPage(0, limit);
        }
        else
        {
            results = historicQuery.list();
        }
        return getValidHistoricTasks(results);
     }
     
     private void addTaskPropertiesToQuery(Map<QName, Object> taskCustomProps, 
                 HistoricTaskInstanceQuery taskQuery) 
     {
    	 for(Entry<QName, Object> customProperty : taskCustomProps.entrySet()) 
    	 {
    		 String name =factory.mapQNameToName(customProperty.getKey());
    		 taskQuery.taskVariableValueEquals(name, customProperty.getValue());
    	 }
     }
     
     private void addProcessPropertiesToQuery(Map<QName, Object> processCustomProps, 
                 HistoricTaskInstanceQuery taskQuery) 
     {
    	 for(Entry<QName, Object> customProperty : processCustomProps.entrySet()) 
    	 {
    		 String name =factory.mapQNameToName(customProperty.getKey());
    		 taskQuery.processVariableValueEquals(name, customProperty.getValue());
    	 }
     }
     
     private List<WorkflowTask> queryStartTasks(WorkflowTaskQuery query)
     {
         List<WorkflowTask> startTasks =  new ArrayList<WorkflowTask>();

         String processInstanceId = null;
         String taskId = query.getTaskId();
         if(taskId != null )
         {
             String localTaskId = createLocalId(taskId);
             if(localTaskId.startsWith(ActivitiConstants.START_TASK_PREFIX))
             processInstanceId = localTaskId.substring(ActivitiConstants.START_TASK_PREFIX.length());
         }
         else
         {
             String processId = query.getProcessId();
             if(processId != null)
             {
                 // Start task for a specific process
                 processInstanceId = createLocalId(processId);
             }
         }
         
         // Only return start-task when a process or task id is set
         if(processInstanceId != null)
         {
             WorkflowTask workflowTask = typeConverter.getVirtualStartTask(processInstanceId, null);
             if(workflowTask != null)
             {
            	boolean startTaskMatches = isStartTaskMatching(workflowTask, query);
            	if(startTaskMatches)
            	{
            		startTasks.add(workflowTask);
            	}
             }
         }
         return startTasks;
     }
         
     
    private boolean isStartTaskMatching(WorkflowTask workflowTask, WorkflowTaskQuery query) 
    {
    	if(query.isActive() != null)
    	{
    		if(query.isActive() && !workflowTask.getPath().isActive()) 
    		{
    			return false;
    		}
    		if(!query.isActive() && workflowTask.getPath().isActive()) 
    		{
    			return false;
    		}
    	}
    	
    	if(query.getActorId() != null && !query.getActorId().equals(workflowTask.getProperties().get(ContentModel.PROP_OWNER)))
    	{
    		return false;
    	}
    	
    	if(query.getProcessCustomProps() != null)
    	{
    		// Get properties for process instance, based on path of start task, which is process-instance
    		Map<QName, Serializable> props = getPathProperties(workflowTask.getPath().getId());
    		if(!checkPropertiesPresent(query.getProcessCustomProps(), props))
    		{
    			return false;
    		}
    	}
    		
		if(query.getProcessId() != null)
		{
			if(!query.getProcessId().equals(workflowTask.getPath().getInstance().getId()))
			{
				return false;
			}
		}
		
		// Query by process name deprecated, but still implemented.
		if(query.getProcessName() != null)
		{
			String processName = factory.mapQNameToName(query.getProcessName());
			if(!processName.equals(workflowTask.getPath().getInstance().getDefinition().getName()))
			{
				return false;
			}
		}
		
		if(query.getWorkflowDefinitionName() != null)
        {
            if(!query.getWorkflowDefinitionName().equals(workflowTask.getPath().getInstance().getDefinition().getName()))
            {
                return false;
            }
        }
    	
		if(query.getTaskCustomProps() != null)
		{
			if(!checkPropertiesPresent(query.getTaskCustomProps(), workflowTask.getProperties()))
    		{
    			return false;
    		}
		}
		
		if(query.getTaskId() != null)
		{
			if(!query.getTaskId().equals(workflowTask.getId()))
			{
				return false;
			}
		}
		
		if(query.getTaskName() != null)
		{
			if(!query.getTaskName().equals(workflowTask.getDefinition().getMetadata().getName()))
			{
				return false;
			}
		}
		
		if(query.getTaskState() != null)
		{
			if(!query.getTaskState().equals(workflowTask.getState()))
			{
				return false;
			}
		}
		
    	// If we fall through, start task matches the query
    	return true;
    }
    
    private boolean checkPropertiesPresent(Map<QName, Object> expectedProperties, Map<QName, Serializable> props)
    {
    	for(Map.Entry<QName, Object> entry : expectedProperties.entrySet())
		{
			if(props.containsKey(entry.getKey())) 
			{
				Object requiredValue = entry.getValue();
				Object actualValue = props.get(entry.getKey());
				
				if(requiredValue != null)
				{
					if(!requiredValue.equals(actualValue))
					{
						return false;
					}
					break;
				}
				else
				{
					if(actualValue != null)
					{
						return false;
					}
					break;
				}
			}
			if(entry.getValue() != null)
			{
				// If variable is not found and required value is non null, start-task doesn't match
				return false;    				
			}
		}
    	
    	return true;
    }
    
    
	/**
    * {@inheritDoc}
    */
    public WorkflowTask getStartTask(String workflowInstanceId)
    {
        String instanceId = createLocalId(workflowInstanceId);
        return typeConverter.getVirtualStartTask(instanceId, null);
    }

    /**
    * {@inheritDoc}
    */
    public WorkflowTask startTask(String taskId)
    {
        throw new UnsupportedOperationException();
    }

    /**
    * {@inheritDoc}
    */
    
    public WorkflowTask suspendTask(String taskId)
    {
        throw new UnsupportedOperationException();
    }

    /**
    * {@inheritDoc}
    */
    public WorkflowTask updateTask(String taskId, Map<QName, Serializable> properties, Map<QName, List<NodeRef>> add,
                Map<QName, List<NodeRef>> remove)
    {
        try
        {
            if(taskId.startsWith(ActivitiConstants.START_TASK_PREFIX))
            {
                // Known limitation, start-tasks cannot be updated
                String msg = messageService.getMessage(ERR_UPDATE_START_TASK, taskId);
                throw new WorkflowException(msg);
            }
            
            Task task = taskService.createTaskQuery().taskId(createLocalId(taskId)).singleResult();
            if(task != null)
            {
                Task updatedTask = propertyConverter.updateTask(task, properties, add, remove);
                return typeConverter.convert(updatedTask);
            }
            else
            {
                String msg = messageService.getMessage(ERR_UPDATE_TASK_UNEXISTING, taskId);
                throw new WorkflowException(msg);
            }            
        }
        catch(ActivitiException ae)
        {
            String msg = messageService.getMessage(ERR_UPDATE_TASK, taskId);
            throw new WorkflowException(msg, ae);
        }
    }
    
    private List<WorkflowInstance> getWorkflowInstances(String workflowDefinitionId, Boolean isActive)
    {
        String processDefId = workflowDefinitionId==null ? null : createLocalId(workflowDefinitionId);
        LinkedList<WorkflowInstance> results = new LinkedList<WorkflowInstance>();
        if(Boolean.FALSE.equals(isActive)==false)
        {
            ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
            if(processDefId!=null)
            {
                query = query.processDefinitionId(processDefId);
            }
            List<ProcessInstance> activeInstances = query.list();
            List<WorkflowInstance> activeResults = typeConverter.convert(activeInstances);
            results.addAll(activeResults);
        }
        if(Boolean.TRUE.equals(isActive)==false)
        {
            HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .finished();
            if(processDefId!=null)
            {
                query = query.processDefinitionId(processDefId);
            }
            List<HistoricProcessInstance> completedInstances = query.list();
            List<WorkflowInstance> completedResults = typeConverter.convert(completedInstances);
            results.addAll(completedResults);
        }
        return results;
    }

    /**
     * @param nodeConverter the nodeConverter to set
     */
    public void setNodeConverter(WorkflowNodeConverter nodeConverter)
    {
        this.nodeConverter = nodeConverter;
    }
    
    
    /**
     * @param factory the factory to set
     */
    public void setFactory(WorkflowObjectFactory factory)
    {
        this.factory = factory;
    }
    
    /**
     * @param messageService the messageService to set
     */
    public void setMessageService(MessageService messageService)
    {
        this.messageService = messageService;
    }
    
    /**
     * @param tenantService the tenantService to set
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * @param typeConverter the typeConverter to set
     */
    public void setTypeConverter(ActivitiTypeConverter typeConverter)
    {
        this.typeConverter = typeConverter;
    }
    
    /**
     * @param activitiUtil the activitiUtil to set
     */
    public void setActivitiUtil(ActivitiUtil activitiUtil)
    {
        this.activitiUtil = activitiUtil;
    }
    
    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
}
