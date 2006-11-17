/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.workflow.jbpm;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipInputStream;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.BPMEngine;
import org.alfresco.repo.workflow.TaskComponent;
import org.alfresco.repo.workflow.WorkflowComponent;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.db.GraphSession;
import org.jbpm.db.TaskMgmtSession;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.par.ProcessArchive;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Problem;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.springframework.util.StringUtils;
import org.springmodules.workflow.jbpm31.JbpmCallback;
import org.springmodules.workflow.jbpm31.JbpmTemplate;
import org.xml.sax.InputSource;


/**
 * JBoss JBPM based implementation of:
 * 
 * Workflow Definition Component
 * Workflow Component
 * Task Component
 * 
 * @author davidc
 */
public class JBPMEngine extends BPMEngine
    implements WorkflowComponent, TaskComponent
{
    // Implementation dependencies
    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;
    protected NodeService nodeService;
    protected ServiceRegistry serviceRegistry;
    protected PersonService personService;
    protected JbpmTemplate jbpmTemplate;

    // Note: jBPM query which is not provided out-of-the-box
    // TODO: Check jBPM 3.2 and get this implemented in jBPM
    private final static String COMPLETED_TASKS_QUERY =     
        "select ti " + 
        "from org.jbpm.taskmgmt.exe.TaskInstance as ti " +
        "where ti.actorId = :actorId " +
        "and ti.isOpen = false " +
        "and ti.end is not null";
    
    // Workflow Path Seperators
    private final static String WORKFLOW_PATH_SEPERATOR = "-";
    private final static String WORKFLOW_TOKEN_SEPERATOR = "@";
    
    // I18N labels
    private final static String TITLE_LABEL = "title";
    private final static String DESC_LABEL = "description";
    private final static String DEFAULT_TRANSITION_LABEL = "bpm_businessprocessmodel.transition";    
    
    
    /**
     * Sets the JBPM Template used for accessing JBoss JBPM in the correct context
     * 
     * @param jbpmTemplate
     */
    public void setJBPMTemplate(JbpmTemplate jbpmTemplate)
    {
        this.jbpmTemplate = jbpmTemplate;
    }
    
    /**
     * Sets the Dictionary Service
     * 
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Sets the Namespace Service
     * 
     * @param namespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
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
     * Sets the Person Service
     * 
     * @param personService
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Sets the Service Registry
     *  
     * @param serviceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    
    //
    // Workflow Definition...
    //
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowDefinitionComponent#deployDefinition(java.io.InputStream)
     */
    public WorkflowDeployment deployDefinition(final InputStream workflowDefinition, final String mimetype)
    {
        try
        {
            return (WorkflowDeployment)jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // construct process definition
                    CompiledProcessDefinition compiledDef = compileProcessDefinition(workflowDefinition, mimetype);
                    
                    // deploy the parsed definition
                    context.deployProcessDefinition(compiledDef.def);
                    
                    // return deployed definition
                    WorkflowDeployment workflowDeployment = createWorkflowDeployment(compiledDef);
                    return workflowDeployment;
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to deploy workflow definition", e);
        }
    }

            
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowDefinitionComponent#isDefinitionDeployed(java.io.InputStream, java.lang.String)
     */
    public boolean isDefinitionDeployed(final InputStream workflowDefinition, final String mimetype)
    {
        try
        {
            return (Boolean) jbpmTemplate.execute(new JbpmCallback()
            {
                public Boolean doInJbpm(JbpmContext context)
                {
                    // create process definition from input stream
                    CompiledProcessDefinition processDefinition = compileProcessDefinition(workflowDefinition, mimetype);
                    
                    // retrieve process definition from Alfresco Repository
                    GraphSession graphSession = context.getGraphSession();
                    ProcessDefinition existingDefinition = graphSession.findLatestProcessDefinition(processDefinition.def.getName());
                    return (existingDefinition == null) ? false : true;
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to determine if workflow definition is already deployed", e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowDefinitionComponent#undeployDefinition(java.lang.String)
     */
    public void undeployDefinition(final String workflowDefinitionId)
    {
        try
        {
            jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve process definition
                    GraphSession graphSession = context.getGraphSession();
                    ProcessDefinition processDefinition = getProcessDefinition(graphSession, workflowDefinitionId);
                    
                    // undeploy
                    // NOTE: jBPM deletes all "in-flight" processes too
                    // TODO: Determine if there's a safer undeploy we can expose via the WorkflowService contract
                    graphSession.deleteProcessDefinition(processDefinition);
                    
                    // we're done
                    return null;
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to deploy workflow definition", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowDefinitionComponent#getDefinitions()
     */
    @SuppressWarnings("unchecked")
    public List<WorkflowDefinition> getDefinitions()
    {
        try
        {
            return (List<WorkflowDefinition>)jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    GraphSession graphSession = context.getGraphSession();
                    List<ProcessDefinition> processDefs = (List<ProcessDefinition>)graphSession.findLatestProcessDefinitions();
                    List<WorkflowDefinition> workflowDefs = new ArrayList<WorkflowDefinition>(processDefs.size());
                    for (ProcessDefinition processDef : processDefs)
                    {
                        WorkflowDefinition workflowDef = createWorkflowDefinition(processDef);
                        workflowDefs.add(workflowDef);
                    }
                    return workflowDefs;
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve workflow definitions", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowDefinitionComponent#getDefinitionById(java.lang.String)
     */
    public WorkflowDefinition getDefinitionById(final String workflowDefinitionId)
    {
        try
        {
            return (WorkflowDefinition)jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve process
                    GraphSession graphSession = context.getGraphSession();
                    ProcessDefinition processDefinition = graphSession.getProcessDefinition(getJbpmId(workflowDefinitionId));
                    return processDefinition == null ? null : createWorkflowDefinition(processDefinition);
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve workflow definition '" + workflowDefinitionId + "'", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowComponent#getDefinitionByName(java.lang.String)
     */
    public WorkflowDefinition getDefinitionByName(final String workflowName)
    {
        try
        {
            return (WorkflowDefinition)jbpmTemplate.execute(new JbpmCallback()
            {
                @SuppressWarnings("synthetic-access")
                public Object doInJbpm(JbpmContext context)
                {
                    GraphSession graphSession = context.getGraphSession();
                    ProcessDefinition processDef = graphSession.findLatestProcessDefinition(createLocalId(workflowName));
                    return processDef == null ? null : createWorkflowDefinition(processDef);
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve workflow definition '" + workflowName + "'", e);
        }
    }
    
    /**
     * Gets a jBPM process definition
     * 
     * @param graphSession  jBPM graph session
     * @param workflowDefinitionId  workflow definition id
     * @return  process definition
     */
    private ProcessDefinition getProcessDefinition(GraphSession graphSession, String workflowDefinitionId)
    {
        ProcessDefinition processDefinition = graphSession.getProcessDefinition(getJbpmId(workflowDefinitionId));
        if (processDefinition == null)
        {
            throw new WorkflowException("Workflow definition '" + workflowDefinitionId + "' does not exist");
        }
        return processDefinition;
    }
    
    
    //
    // Workflow Instance Management...
    //

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowComponent#startWorkflow(java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public WorkflowPath startWorkflow(final String workflowDefinitionId, final Map<QName, Serializable> parameters)
    {
        try
        {
            return (WorkflowPath) jbpmTemplate.execute(new JbpmCallback()
            {
                @SuppressWarnings("synthetic-access")
                public Object doInJbpm(JbpmContext context)
                {
                    // initialise jBPM actor (for any processes that wish to record the initiator)
                    String currentUserName = AuthenticationUtil.getCurrentUserName();
                    context.setActorId(currentUserName);

                    // construct a new process
                    GraphSession graphSession = context.getGraphSession();
                    ProcessDefinition processDefinition = getProcessDefinition(graphSession, workflowDefinitionId);
                    ProcessInstance processInstance = new ProcessInstance(processDefinition);
                 
                    // assign initial process context
                    ContextInstance processContext = processInstance.getContextInstance();
                    NodeRef initiatorPerson = mapNameToAuthority(currentUserName);
                    if (initiatorPerson != null)
                    {
                        processContext.setVariable("initiator", new JBPMNode(initiatorPerson, serviceRegistry));
                    }

                    // create the start task if one exists
                    Token token = processInstance.getRootToken();
                    Task startTask = processInstance.getTaskMgmtInstance().getTaskMgmtDefinition().getStartTask();
                    if (startTask != null)
                    {
                        TaskInstance taskInstance = processInstance.getTaskMgmtInstance().createStartTaskInstance();
                        setTaskProperties(taskInstance, parameters);
                        token = taskInstance.getToken();
                    }

                    // Save the process instance along with the task instance
                    context.save(processInstance);
                    return createWorkflowPath(token);
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to start workflow " + workflowDefinitionId, e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowComponent#getActiveWorkflows(java.lang.String)
     */    
    @SuppressWarnings("unchecked")
    public List<WorkflowInstance> getActiveWorkflows(final String workflowDefinitionId)
    {
        try
        {
            return (List<WorkflowInstance>) jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    GraphSession graphSession = context.getGraphSession();
                    List<ProcessInstance> processInstances = graphSession.findProcessInstances(getJbpmId(workflowDefinitionId));
                    List<WorkflowInstance> workflowInstances = new ArrayList<WorkflowInstance>(processInstances.size());
                    for (ProcessInstance processInstance : processInstances)
                    {
                        if (!processInstance.hasEnded())
                        {
                            WorkflowInstance workflowInstance = createWorkflowInstance(processInstance);
                            workflowInstances.add(workflowInstance);
                        }
                    }
                    return workflowInstances;
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve workflow instances for definition '" + workflowDefinitionId + "'", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowComponent#getWorkflowById(java.lang.String)
     */
    public WorkflowInstance getWorkflowById(final String workflowId)
    {
        try
        {
            return (WorkflowInstance) jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve workflow
                    GraphSession graphSession = context.getGraphSession();
                    ProcessInstance processInstance = graphSession.getProcessInstance(getJbpmId(workflowId));
                    return processInstance == null ? null : createWorkflowInstance(processInstance);
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve workflow instance '" + workflowId + "'", e);
        }        
    }
    
    /**
     * Gets a jBPM Process Instance
     * @param graphSession  jBPM graph session
     * @param workflowId  workflow id
     * @return  process instance
     */
    private ProcessInstance getProcessInstance(GraphSession graphSession, String workflowId)
    {
        ProcessInstance processInstance = graphSession.getProcessInstance(getJbpmId(workflowId));
        if (processInstance == null)
        {
            throw new WorkflowException("Workflow instance '" + workflowId + "' does not exist");
        }
        return processInstance;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowComponent#getWorkflowPaths(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<WorkflowPath> getWorkflowPaths(final String workflowId)
    {
        try
        {
            return (List<WorkflowPath>) jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve process instance
                    GraphSession graphSession = context.getGraphSession();
                    ProcessInstance processInstance = getProcessInstance(graphSession, workflowId);
                    
                    // convert jBPM tokens to workflow posisitons
                    List<Token> tokens = processInstance.findAllTokens();
                    List<WorkflowPath> paths = new ArrayList<WorkflowPath>(tokens.size());
                    for (Token token : tokens)
                    {
                        if (!token.hasEnded())
                        {
                            WorkflowPath path = createWorkflowPath(token);
                            paths.add(path);
                        }
                    }
                    
                    return paths;
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve workflow paths for workflow instance '" + workflowId + "'", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowComponent#cancelWorkflow(java.lang.String)
     */
    public WorkflowInstance cancelWorkflow(final String workflowId)
    {
        try
        {
            return (WorkflowInstance) jbpmTemplate.execute(new JbpmCallback()
            {
				@SuppressWarnings("unchecked")
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve and cancel process instance
                    GraphSession graphSession = context.getGraphSession();
                    ProcessInstance processInstance = getProcessInstance(graphSession, workflowId);
                    // TODO: Determine if this is the most appropriate way to cancel workflow...
                    //       It might be useful to record point at which it was cancelled etc
                    WorkflowInstance workflowInstance = createWorkflowInstance(processInstance);
                    
                    // delete the process instance
                    graphSession.deleteProcessInstance(processInstance, true, true, true);
                    workflowInstance.active = false;
                    workflowInstance.endDate = new Date();
                    return workflowInstance; 
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to cancel workflow instance '" + workflowId + "'", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowComponent#signal(java.lang.String, java.lang.String)
     */
    public WorkflowPath signal(final String pathId, final String transition)
    {
        try
        {
            return (WorkflowPath) jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve jBPM token for workflow position
                    GraphSession graphSession = context.getGraphSession();
                    Token token = getWorkflowToken(graphSession, pathId);
                    
                    // signal the transition
                    if (transition == null)
                    {
                        token.signal();
                    }
                    else
                    {
                        Node node = token.getNode();
                        if (!node.hasLeavingTransition(transition))
                        {
                            throw new WorkflowException("Transition '" + transition + "' is invalid for Workflow path '" + pathId + "'");
                        }
                        token.signal(transition);
                    }
                    
                    // save
                    ProcessInstance processInstance = token.getProcessInstance();
                    context.save(processInstance);
                    
                    // return new workflow path
                    return createWorkflowPath(token);
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to signal transition '" + transition + "' from workflow path '" + pathId + "'", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.WorkflowComponent#getTasksForWorkflowPath(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<WorkflowTask> getTasksForWorkflowPath(final String pathId)
    {
        try
        {
            return (List<WorkflowTask>) jbpmTemplate.execute(new JbpmCallback()
            {
                public List<WorkflowTask> doInJbpm(JbpmContext context)
                {
                    // retrieve tasks at specified workflow path
                    GraphSession graphSession = context.getGraphSession();
                    Token token = getWorkflowToken(graphSession, pathId);
                    TaskMgmtSession taskSession = context.getTaskMgmtSession();
                    List<TaskInstance> tasks = taskSession.findTaskInstancesByToken(token.getId());
                    List<WorkflowTask> workflowTasks = new ArrayList<WorkflowTask>(tasks.size());
                    for (TaskInstance task : tasks)
                    {
                        WorkflowTask workflowTask = createWorkflowTask(task);
                        workflowTasks.add(workflowTask);
                    }
                    return workflowTasks;
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve tasks assigned at Workflow path '" + pathId + "'", e);
        }
    }

    
    //
    // Task Management ...
    //
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.TaskComponent#getAssignedTasks(java.lang.String, org.alfresco.service.cmr.workflow.WorkflowTaskState)
     */
    @SuppressWarnings("unchecked")
    public List<WorkflowTask> getAssignedTasks(final String authority, final WorkflowTaskState state)
    {
        try
        {
            return (List<WorkflowTask>) jbpmTemplate.execute(new JbpmCallback()
            {
                public List<WorkflowTask> doInJbpm(JbpmContext context)
                {
                    // retrieve tasks assigned to authority
                    List<TaskInstance> tasks;
                    if (state.equals(WorkflowTaskState.IN_PROGRESS))
                    {
                        TaskMgmtSession taskSession = context.getTaskMgmtSession();
                        tasks = taskSession.findTaskInstances(authority);
                    }
                    else
                    {
                        // Note: This method is not implemented by jBPM
                        tasks = findCompletedTaskInstances(context, authority);
                    }
                    
                    // convert tasks to appropriate service response format 
                    List<WorkflowTask> workflowTasks = new ArrayList<WorkflowTask>(tasks.size());
                    for (TaskInstance task : tasks)
                    {
                        WorkflowTask workflowTask = createWorkflowTask(task);
                        workflowTasks.add(workflowTask);
                    }
                    return workflowTasks;
                }
                
                /**
                 * Gets the completed task list for the specified actor
                 * 
                 * TODO: This method provides a query that's not in JBPM!  Look to have JBPM implement this.
                 * 
                 * @param jbpmContext  the jbpm context
                 * @param actorId  the actor to retrieve tasks for
                 * @return  the tasks
                 */
                private List findCompletedTaskInstances(JbpmContext jbpmContext, String actorId)
                {
                    List result = null;
                    try
                    {
                        Session session = jbpmContext.getSession();
                        Query query = session.createQuery(COMPLETED_TASKS_QUERY);
                        query.setString("actorId", actorId);
                        result = query.list();
                    }
                    catch (Exception e)
                    {
                        throw new JbpmException("Couldn't get completed task instances list for actor '" + actorId + "'", e);
                    }
                    return result;
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve tasks assigned to authority '" + authority + "' in state '" + state + "'", e);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.TaskComponent#getPooledTasks(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public List<WorkflowTask> getPooledTasks(final List<String> authorities)
    {
        try
        {
            return (List<WorkflowTask>) jbpmTemplate.execute(new JbpmCallback()
            {
                public List<WorkflowTask> doInJbpm(JbpmContext context)
                {
                    // retrieve pooled tasks for specified authorities
                    TaskMgmtSession taskSession = context.getTaskMgmtSession();
                    List<TaskInstance> tasks = taskSession.findPooledTaskInstances(authorities);
                    List<WorkflowTask> workflowTasks = new ArrayList<WorkflowTask>(tasks.size());
                    for (TaskInstance task : tasks)
                    {
                        WorkflowTask workflowTask = createWorkflowTask(task);
                        workflowTasks.add(workflowTask);
                    }
                    return workflowTasks;
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve pooled tasks for authorities '" + authorities + "'", e);
        }
    }

    /**
     * Gets a jBPM Task Instance
     * @param taskSession  jBPM task session
     * @param taskId  task id
     * @return  task instance
     */
    private TaskInstance getTaskInstance(TaskMgmtSession taskSession, String taskId)
    {
        TaskInstance taskInstance = taskSession.getTaskInstance(getJbpmId(taskId));
        if (taskInstance == null)
        {
            throw new WorkflowException("Task instance '" + taskId + "' does not exist");
        }
        return taskInstance;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.TaskComponent#updateTask(java.lang.String, java.util.Map, java.util.Map, java.util.Map)
     */
    public WorkflowTask updateTask(final String taskId, final Map<QName, Serializable> properties, final Map<QName, List<NodeRef>> add, final Map<QName, List<NodeRef>> remove)
    {
        try
        {
            return (WorkflowTask) jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve task
                    TaskMgmtSession taskSession = context.getTaskMgmtSession();
                    TaskInstance taskInstance = getTaskInstance(taskSession, taskId);

                    // create properties to set on task instance
                    Map<QName, Serializable> newProperties = properties;
                    if (newProperties == null && (add != null || remove != null))
                    {
                        newProperties = new HashMap<QName, Serializable>(10); 
                    }
                    
                    if (add != null || remove != null)
                    {
                        Map<QName, Serializable> existingProperties = getTaskProperties(taskInstance, false);
                        
                        if (add != null)
                        {
                            // add new associations
                            for (Entry<QName, List<NodeRef>> toAdd : add.entrySet())
                            {
                                // retrieve existing list of noderefs for association
                                List<NodeRef> existingAdd = (List<NodeRef>)newProperties.get(toAdd.getKey());
                                if (existingAdd == null)
                                {
                                    existingAdd = (List<NodeRef>)existingProperties.get(toAdd.getKey());
                                }
    
                                // make the additions
                                if (existingAdd == null)
                                {
                                    newProperties.put(toAdd.getKey(), (Serializable)toAdd.getValue());
                                }
                                else
                                {
                                    for (NodeRef nodeRef : (List<NodeRef>)toAdd.getValue())
                                    {
                                        if (!(existingAdd.contains(nodeRef)))
                                        {
                                            existingAdd.add(nodeRef);
                                        }
                                    }
                                }
                            }
                        }

                        if (remove != null)
                        {
                            // add new associations
                            for (Entry<QName, List<NodeRef>> toRemove: remove.entrySet())
                            {
                                // retrieve existing list of noderefs for association
                                List<NodeRef> existingRemove = (List<NodeRef>)newProperties.get(toRemove.getKey());
                                if (existingRemove == null)
                                {
                                    existingRemove = (List<NodeRef>)existingProperties.get(toRemove.getKey());
                                }
    
                                // make the subtractions
                                if (existingRemove != null)
                                {
                                    for (NodeRef nodeRef : (List<NodeRef>)toRemove.getValue())
                                    {
                                        existingRemove.remove(nodeRef);
                                    }
                                }
                            }
                        }
                    }
                    
                    // update the task
                    if (newProperties != null)
                    {
                        setTaskProperties(taskInstance, newProperties);
                        
                        // save
                        ProcessInstance processInstance = taskInstance.getToken().getProcessInstance();
                        context.save(processInstance);
                    }
                    
                    // note: the ending of a task may not have signalled (i.e. more than one task exists at
                    //       this node)
                    return createWorkflowTask(taskInstance);
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to update workflow task '" + taskId + "'", e);
        }        
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.TaskComponent#startTask(java.lang.String)
     */
    public WorkflowTask startTask(String taskId)
    {
        // TODO:
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.TaskComponent#suspendTask(java.lang.String)
     */
    public WorkflowTask suspendTask(String taskId)
    {
        // TODO:
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.TaskComponent#endTask(java.lang.String, java.lang.String)
     */
    public WorkflowTask endTask(final String taskId, final String transition)
    {
        try
        {
            return (WorkflowTask) jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve task
                    TaskMgmtSession taskSession = context.getTaskMgmtSession();
                    TaskInstance taskInstance = getTaskInstance(taskSession, taskId);

                    // signal the transition on the task
                    if (transition == null)
                    {
                        taskInstance.end();
                    }
                    else
                    {
                        Node node = taskInstance.getToken().getNode();
                        if (node.getLeavingTransition(transition) == null)
                        {
                            throw new WorkflowException("Transition '" + transition + "' is invalid for Workflow task '" + taskId + "'");
                        }
                        taskInstance.end(transition);
                    }
                    
                    // save
                    ProcessInstance processInstance = taskInstance.getToken().getProcessInstance();
                    context.save(processInstance);
                    
                    // note: the ending of a task may not have signalled (i.e. more than one task exists at
                    //       this node)
                    return createWorkflowTask(taskInstance);
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to signal transition '" + transition + "' from workflow task '" + taskId + "'", e);
        }        
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.TaskComponent#getTaskById(java.lang.String)
     */
    public WorkflowTask getTaskById(final String taskId)
    {
        try
        {
            return (WorkflowTask) jbpmTemplate.execute(new JbpmCallback()
            {
                public Object doInJbpm(JbpmContext context)
                {
                    // retrieve task
                    TaskMgmtSession taskSession = context.getTaskMgmtSession();
                    TaskInstance taskInstance = taskSession.getTaskInstance(getJbpmId(taskId));
                    return taskInstance == null ? null : createWorkflowTask(taskInstance);
                }
            });
        }
        catch(JbpmException e)
        {
            throw new WorkflowException("Failed to retrieve task '" + taskId + "'", e);
        }        
    }
    
    
    //
    // Helpers...
    //
    

    /**
     * Process Definition with accompanying problems
     */
    private static class CompiledProcessDefinition
    {
        public CompiledProcessDefinition(ProcessDefinition def, List<Problem> problems)
        {
            this.def = def;
            this.problems = new String[problems.size()];
            int i = 0;
            for (Problem problem : problems)
            {
                this.problems[i++] = problem.toString();
            }
        }
        
        protected ProcessDefinition def;
        protected String[] problems;
    }

    /**
     * JpdlXmlReader with access to problems encountered during compile.
     * 
     * @author davidc
     */
    private static class JBPMEngineJpdlXmlReader extends JpdlXmlReader
    {
        private static final long serialVersionUID = -753730152120696221L;

        public JBPMEngineJpdlXmlReader(InputStream inputStream)
        {
            super(new InputSource(inputStream));
        }

        /**
         * Gets the problems
         * 
         * @return  problems
         */
        public List getProblems()
        {
            return problems;
        }
    }
    
    /**
     * Construct a Process Definition from the provided Process Definition stream
     * 
     * @param workflowDefinition  stream to create process definition from  
     * @param mimetype  mimetype of stream
     * @return  process definition
     */
    @SuppressWarnings("unchecked")
    protected CompiledProcessDefinition compileProcessDefinition(InputStream definitionStream, String mimetype)
    {
        String actualMimetype = (mimetype == null) ? MimetypeMap.MIMETYPE_ZIP : mimetype;
        CompiledProcessDefinition compiledDef = null;
        
        // parse process definition from jBPM process archive file
        
        if (actualMimetype.equals(MimetypeMap.MIMETYPE_ZIP))
        {
            ZipInputStream zipInputStream = null;
            try
            {
                zipInputStream = new ZipInputStream(definitionStream);
                ProcessArchive reader = new ProcessArchive(zipInputStream);
                ProcessDefinition def = reader.parseProcessDefinition();
                compiledDef = new CompiledProcessDefinition(def, reader.getProblems());
            }
            catch(Exception e)
            {
                throw new JbpmException("Failed to parse process definition from jBPM zip archive stream", e);
            }
            finally
            {
                if (zipInputStream != null)
                {
                    try { zipInputStream.close(); } catch(IOException e) {};
                }
            }
        }
        
        // parse process definition from jBPM xml file
        
        else if (actualMimetype.equals(MimetypeMap.MIMETYPE_XML))
        {
            try
            {
                JBPMEngineJpdlXmlReader jpdlReader = new JBPMEngineJpdlXmlReader(definitionStream); 
                ProcessDefinition def = jpdlReader.readProcessDefinition();
                compiledDef = new CompiledProcessDefinition(def, jpdlReader.getProblems());
            }
            catch(Exception e)
            {
                throw new JbpmException("Failed to parse process definition from jBPM xml stream", e);
            }
        }

        return compiledDef;
    }

    /**
     * Gets the Task definition of the specified Task
     * 
     * @param task  the task
     * @return  the task definition
     */
    private TypeDefinition getTaskDefinition(Task task)
    {
        // TODO: Extend jBPM task instance to include dictionary definition qname? 
        QName typeName = QName.createQName(task.getName(), namespaceService);
        TypeDefinition typeDef = dictionaryService.getType(typeName);
        if (typeDef == null)
        {
            typeDef = dictionaryService.getType(task.getStartState() == null ? WorkflowModel.TYPE_WORKFLOW_TASK : WorkflowModel.TYPE_START_TASK);
            if (typeDef == null)
            {
                throw new WorkflowException("Failed to find type definition '" + WorkflowModel.TYPE_WORKFLOW_TASK + "'");
            }
        }
        return typeDef;
    }
    
    /**
     * Convert the specified Type definition to an anonymous Type definition.
     * 
     * This collapses all mandatory aspects into a single Type definition.
     * 
     * @param typeDef  the type definition
     * @return  the anonymous type definition
     */
    private TypeDefinition getAnonymousTaskDefinition(TypeDefinition typeDef)
    {
        List<AspectDefinition> aspects = typeDef.getDefaultAspects();
        List<QName> aspectNames = new ArrayList<QName>(aspects.size());
        getMandatoryAspects(typeDef, aspectNames);
        return dictionaryService.getAnonymousType(typeDef.getName(), aspectNames);
    }

    /**
     * Gets a flattened list of all mandatory aspects for a given class
     * 
     * @param classDef  the class
     * @param aspects  a list to hold the mandatory aspects
     */
    private void getMandatoryAspects(ClassDefinition classDef, List<QName> aspects)
    {
        for (AspectDefinition aspect : classDef.getDefaultAspects())
        {
            QName aspectName = aspect.getName();
            if (!aspects.contains(aspectName))
            {
                aspects.add(aspect.getName());
                getMandatoryAspects(aspect, aspects);
            }
        }
    }
    
    /**
     * Get JBoss JBPM Id from Engine Global Id
     * 
     * @param id  global id
     * @return  JBoss JBPM Id
     */
    protected long getJbpmId(String id)
    {
        try
        {
            String theLong = createLocalId(id);
            return new Long(theLong);
        }
        catch(NumberFormatException e)
        {
            throw new WorkflowException("Format of id '" + id + "' is invalid", e);
        }
    }

    /**
     * Get the JBoss JBPM Token for the Workflow Path
     * 
     * @param session   JBoss JBPM Graph Session
     * @param pathId  workflow path id
     * @return  JBoss JBPM Token
     */
    protected Token getWorkflowToken(GraphSession session, String pathId)
    {
        // extract process id and token path within process
        String[] path = pathId.split(WORKFLOW_PATH_SEPERATOR);
        if (path.length != 2)
        {
            throw new WorkflowException("Invalid workflow path '" + pathId + "'");
        }

        // retrieve jBPM token for workflow position
        ProcessInstance processInstance = getProcessInstance(session, path[0]);
        String tokenId = path[1].replace(WORKFLOW_TOKEN_SEPERATOR, "/");
        Token token = processInstance.findToken(tokenId);
        if (token == null)
        {
            throw new WorkflowException("Workflow path '" + pathId + "' does not exist");
        }
        
        return token;
    }

    /**
     * Sets Properties of Task
     * 
     * @param instance  task instance
     * @param properties  properties to set
     */
    @SuppressWarnings("unchecked")
    protected Map<QName, Serializable> getTaskProperties(TaskInstance instance, boolean localProperties)
    {
    	// retrieve type definition for task
        TypeDefinition taskDef = getAnonymousTaskDefinition(getTaskDefinition(instance.getTask()));
        Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
        Map<QName, AssociationDefinition> taskAssocs = taskDef.getAssociations();
    	
        // map arbitrary task variables
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(10);
        Map<String, Object> vars = (localProperties ? instance.getVariablesLocally() : instance.getVariables());

        for (Entry<String, Object> entry : vars.entrySet())
        {
            String key = entry.getKey();
            QName qname = mapNameToQName(key);

            // add variable, only if part of task definition or locally defined on task
            if (taskProperties.containsKey(qname) || taskAssocs.containsKey(qname) || instance.hasVariableLocally(key))
            {
	            Object value = entry.getValue();
	
	            //
	            // perform data conversions
	            //
	            
	            // Convert Nodes to NodeRefs
	            if (value instanceof JBPMNode)
	            {
	                value = ((JBPMNode)value).getNodeRef();
	            }
	            else if (value instanceof JBPMNodeList)
	            {
	                JBPMNodeList nodes = (JBPMNodeList)value;
	                List<NodeRef> nodeRefs = new ArrayList<NodeRef>(nodes.size());
	                for (JBPMNode node : nodes)
	                {
	                    nodeRefs.add(node.getNodeRef());
	                }
	                value = (Serializable)nodeRefs;
	            }
	            
	            // place task variable in map to return
	            properties.put(qname, (Serializable)value);
            }
        }

        // map jBPM task instance fields to properties
        properties.put(WorkflowModel.PROP_TASK_ID, instance.getId());
        properties.put(WorkflowModel.PROP_DESCRIPTION, instance.getDescription());
        properties.put(WorkflowModel.PROP_START_DATE, instance.getStart());
        properties.put(WorkflowModel.PROP_DUE_DATE, instance.getDueDate());
        properties.put(WorkflowModel.PROP_COMPLETION_DATE, instance.getEnd());
        properties.put(WorkflowModel.PROP_PRIORITY, instance.getPriority());
        properties.put(ContentModel.PROP_CREATED, instance.getCreate());
        properties.put(ContentModel.PROP_OWNER, instance.getActorId());
        
        // map jBPM task instance collections to associations
        Set pooledActors = instance.getPooledActors();
        if (pooledActors != null)
        {
            List<NodeRef> pooledNodeRefs = new ArrayList<NodeRef>(pooledActors.size());
            for (String pooledActor : (Set<String>)pooledActors)
            {
                NodeRef pooledNodeRef = mapNameToAuthority(pooledActor);
                if (pooledNodeRef != null)
                {
                    pooledNodeRefs.add(pooledNodeRef);
                }
            }
            properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, (Serializable)pooledNodeRefs);
        }

        return properties;
    }
    
    /**
     * Sets Properties of Task
     * 
     * @param instance  task instance
     * @param properties  properties to set
     */
    protected void setTaskProperties(TaskInstance instance, Map<QName, Serializable> properties)
    {
        if (properties == null)
        {
            return;
        }

        // establish task definition
        TypeDefinition taskDef = getAnonymousTaskDefinition(getTaskDefinition(instance.getTask()));
        Map<QName, PropertyDefinition> taskProperties = taskDef.getProperties();
        Map<QName, AssociationDefinition> taskAssocs = taskDef.getAssociations();

        // map each parameter to task
        for (Entry<QName, Serializable> entry : properties.entrySet())
        {
            QName key = entry.getKey();
            Serializable value = entry.getValue();
            
            // determine if writing property
            // NOTE: some properties map to fields on jBPM task instance whilst
            //       others are set in the general variable bag on the task
            PropertyDefinition propDef = taskProperties.get(key);
            if (propDef != null)
            {
                if (propDef.isProtected())
                {
                    // NOTE: only write non-protected properties
                    continue;
                }
                
                // convert property value
                if (value instanceof Collection)
                {
                    value = (Serializable)DefaultTypeConverter.INSTANCE.convert(propDef.getDataType(), (Collection)value);
                }
                else
                {
                    value = (Serializable)DefaultTypeConverter.INSTANCE.convert(propDef.getDataType(), value);
                }

                // convert NodeRefs to JBPMNodes
                DataTypeDefinition dataTypeDef = propDef.getDataType();
                if (dataTypeDef.getName().equals(DataTypeDefinition.NODE_REF))
                {
                    value = convertNodeRefs(propDef.isMultiValued(), value);
                }
                
                // map property to specific jBPM task instance field
                if (key.equals(WorkflowModel.PROP_DESCRIPTION))
                {
                    if (value != null && !(value instanceof String))
                    {
                        throw new WorkflowException("Task description '" + value + "' is invalid");
                    }
                    instance.setDescription((String)value);
                    continue;
                }
                if (key.equals(WorkflowModel.PROP_DUE_DATE))
                {
                    if (value != null && !(value instanceof Date))
                    {
                        throw new WorkflowException("Task due date '" + value + "' is invalid");
                    }
                    instance.setDueDate((Date)value);
                    continue;
                }
                else if (key.equals(WorkflowModel.PROP_PRIORITY))
                {
                    if (!(value instanceof Integer))
                    {
                        throw new WorkflowException("Task priority '" + value + "' is invalid");
                    }
                    instance.setPriority((Integer)value);
                    continue;
                }
                else if (key.equals(ContentModel.PROP_OWNER))
                {
                    if (value != null && !(value instanceof String))
                    {
                        throw new WorkflowException("Task owner '" + value + "' is invalid");
                    }
                    instance.setActorId((String)value);
                    continue;
                }
            }
            else
            {
                // determine if writing association
                AssociationDefinition assocDef = taskAssocs.get(key);
                if (assocDef != null)
                {
                    // convert association to JBPMNodes
                    value = convertNodeRefs(assocDef.isTargetMany(), value);
                    
                    // map association to specific jBPM task instance field
                    if (key.equals(WorkflowModel.ASSOC_POOLED_ACTORS))
                    {
                        String[] pooledActors = null;
                        if (value instanceof JBPMNodeList[])
                        {
                            JBPMNodeList actors = (JBPMNodeList)value;
                            pooledActors = new String[actors.size()];
                            int i = 0;
                            for (JBPMNode actor : actors)
                            {
                                pooledActors[i++] = actor.getName();
                            }
                        }
                        else if (value instanceof JBPMNode)
                        {
                            pooledActors = new String[] {((JBPMNode)value).getName()};
                        }
                        else
                        {
                            throw new WorkflowException("Pooled actors value '" + value + "' is invalid");
                        }
                        instance.setPooledActors(pooledActors);
                        continue;
                    }
                    else if (key.equals(WorkflowModel.ASSOC_PACKAGE))
                    {
                        // Attach workflow definition & instance id to Workflow Package in Repository
                        String name = mapQNameToName(key);
                        JBPMNode existingWorkflowPackage = (JBPMNode)instance.getVariable(name);
                        
                        // first check if provided workflow package has already been associated with another workflow instance
                        if (existingWorkflowPackage != null && value != null)
                        {
                            NodeRef newPackageNodeRef = ((JBPMNode)value).getNodeRef();
                            ProcessInstance processInstance = instance.getToken().getProcessInstance();
                            String packageInstanceId = (String)nodeService.getProperty(newPackageNodeRef, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID);
                            if (packageInstanceId != null && packageInstanceId.length() > 0 && (processInstance.getId() == getJbpmId(packageInstanceId)))
                            {
                                String workflowInstanceId = createGlobalId(new Long(processInstance.getId()).toString());
                                throw new WorkflowException("Cannot associate workflow package '" + newPackageNodeRef + "' with workflow instance '" + workflowInstanceId + "' as it's already associated with workflow instance '" + packageInstanceId + "'"); 
                            }
                        }

                        // initialise workflow package
                        if (existingWorkflowPackage == null && value != null)
                        {
                            // initialise workflow package
                            NodeRef newPackageNodeRef = ((JBPMNode)value).getNodeRef();
                            ProcessInstance processInstance = instance.getToken().getProcessInstance();
                            WorkflowInstance workflowInstance = createWorkflowInstance(processInstance);
                            nodeService.setProperty(newPackageNodeRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_ID, workflowInstance.definition.id);
                            nodeService.setProperty(newPackageNodeRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME, workflowInstance.definition.name);
                            nodeService.setProperty(newPackageNodeRef, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID, workflowInstance.id);
                        }
                        
                        // NOTE: Fall-through to allow setting of Workflow Package on Task Instance
                    }
                }
                
                // untyped value, perform minimal conversion
                else
                {
                    if (value instanceof NodeRef)
                    {
                        value = new JBPMNode((NodeRef)value, serviceRegistry);
                    }
                }
            }
            
            // no specific mapping to jBPM task has been established, so place into
            // the generic task variable bag
            String name = mapQNameToName(key);
            instance.setVariableLocally(name, value);
        }
    }

    /**
     * Sets Default Properties of Task
     * 
     * @param instance  task instance
     */
    protected void setDefaultTaskProperties(TaskInstance instance)
    {
        Map<QName, Serializable> existingValues = getTaskProperties(instance, true);
        Map<QName, Serializable> defaultValues = new HashMap<QName, Serializable>();

        // construct an anonymous type that flattens all mandatory aspects
        ClassDefinition classDef = getAnonymousTaskDefinition(getTaskDefinition(instance.getTask()));
        Map<QName, PropertyDefinition> propertyDefs = classDef.getProperties(); 

        // for each property, determine if it has a default value
        for (Map.Entry<QName, PropertyDefinition> entry : propertyDefs.entrySet())
        {
            String defaultValue = entry.getValue().getDefaultValue();
            if (defaultValue != null)
            {
                if (existingValues.get(entry.getKey()) == null)
                {
                    defaultValues.put(entry.getKey(), defaultValue);
                }
            }
        }

        // special case for task description default value
        String description = (String)existingValues.get(WorkflowModel.PROP_DESCRIPTION);
        if (description == null || description.length() == 0)
        {
            description = (String)instance.getContextInstance().getVariable(mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION));
            if (description != null && description.length() > 0)
            {
                defaultValues.put(WorkflowModel.PROP_DESCRIPTION, description);
            }
            else
            {
                WorkflowTask task = createWorkflowTask(instance);
                defaultValues.put(WorkflowModel.PROP_DESCRIPTION, task.title);
            }
        }

        // assign the default values to the task
        if (defaultValues.size() > 0)
        {
            setTaskProperties(instance, defaultValues);
        }
    }

    /**
     * Sets default description for the Task
     * 
     * @param instance  task instance
     */
    public void setDefaultStartTaskDescription(TaskInstance instance)
    {
        String description = instance.getTask().getDescription();
        if (description == null || description.length() == 0)
        {
            description = (String)instance.getContextInstance().getVariable(mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION));
            if (description != null && description.length() > 0)
            {
                Map<QName, Serializable> defaultValues = new HashMap<QName, Serializable>();
                defaultValues.put(WorkflowModel.PROP_DESCRIPTION, description);
                setTaskProperties(instance, defaultValues);
            }
        }
    }

    /**
     * Initialise Workflow Instance properties
     * 
     * @param startTask  start task instance
     */
    protected void setDefaultWorkflowProperties(TaskInstance startTask)
    {
        Map<QName, Serializable> taskProperties = getTaskProperties(startTask, true);
        
        ContextInstance processContext = startTask.getContextInstance();
        String workflowDescriptionName = mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION);
        if (!processContext.hasVariable(workflowDescriptionName))
        {
            processContext.setVariable(workflowDescriptionName, taskProperties.get(WorkflowModel.PROP_WORKFLOW_DESCRIPTION));
        }
        String workflowDueDateName = mapQNameToName(WorkflowModel.PROP_WORKFLOW_DUE_DATE);
        if (!processContext.hasVariable(workflowDueDateName))
        {
            processContext.setVariable(workflowDueDateName, taskProperties.get(WorkflowModel.PROP_WORKFLOW_DUE_DATE));
        }
        String workflowPriorityName = mapQNameToName(WorkflowModel.PROP_WORKFLOW_PRIORITY);
        if (!processContext.hasVariable(workflowPriorityName))
        {
            processContext.setVariable(workflowPriorityName, taskProperties.get(WorkflowModel.PROP_WORKFLOW_PRIORITY));
        }
        String workflowPackageName = mapQNameToName(WorkflowModel.ASSOC_PACKAGE);
        if (!processContext.hasVariable(workflowPackageName))
        {
            Serializable packageNodeRef = taskProperties.get(WorkflowModel.ASSOC_PACKAGE);
            processContext.setVariable(workflowPackageName, convertNodeRefs(packageNodeRef instanceof List, packageNodeRef));
        }
        String workflowContextName = mapQNameToName(WorkflowModel.PROP_CONTEXT);
        if (!processContext.hasVariable(workflowContextName))
        {
            Serializable contextRef = taskProperties.get(WorkflowModel.PROP_CONTEXT);
            processContext.setVariable(workflowContextName, convertNodeRefs(contextRef instanceof List, contextRef));
        }
    }
    
    /**
     * Convert a Repository association to JBPMNodeList or JBPMNode
     * 
     * @param isMany true => force conversion to list
     * @param value  value to convert
     * @return JBPMNodeList or JBPMNode
     */
    private Serializable convertNodeRefs(boolean isMany, Serializable value)
    {
        if (value instanceof NodeRef)
        {
            if (isMany)
            {
                // convert single node ref to list of node refs
                JBPMNodeList values = new JBPMNodeList(); 
                values.add(new JBPMNode((NodeRef)value, serviceRegistry));
                value = (Serializable)values;
            }
            else
            {
                value = new JBPMNode((NodeRef)value, serviceRegistry);
            }
        }
        else if (value instanceof List)
        {
            if (isMany)
            {
                JBPMNodeList values = new JBPMNodeList();
                for (NodeRef nodeRef : (List<NodeRef>)value)
                {
                    values.add(new JBPMNode(nodeRef, serviceRegistry));
                }
                value = (Serializable)values;
            }
            else
            {
                List<NodeRef> nodeRefs = (List<NodeRef>)value;
                value = (nodeRefs.size() == 0 ? null : new JBPMNode(nodeRefs.get(0), serviceRegistry));
            }
        }
        
        return value;
    }
    
    /**
     * Convert authority name to an Alfresco Authority
     * 
     * @param names  the authority names to convert
     * @return  the Alfresco authorities
     */
    private NodeRef mapNameToAuthority(String name)
    {
        NodeRef authority = null;
        if (name != null)
        {
            // TODO: Should this be an exception?
            if (personService.personExists(name))
            {
                authority = personService.getPerson(name);
            }
        }
        return authority;
    }

    /**
     * Map jBPM variable name to QName
     * 
     * @param name  jBPM variable name
     * @return  qname
     */
    private QName mapNameToQName(String name)
    {
        QName qname = null;
        String qnameStr = name.replaceFirst("_", ":");
        try
        {
            qname = QName.createQName(qnameStr, this.namespaceService);
        }
        catch(NamespaceException e)
        {
            qname = QName.createQName(name, this.namespaceService);
        }
        return qname;
    }

    /**
     * Map QName to jBPM variable name
     * 
     * @param name  QName
     * @return  jBPM variable name
     */
    private String mapQNameToName(QName name)
    {
        String nameStr = name.toPrefixString(this.namespaceService);
        return nameStr.replace(':', '_');
    }
    
    /**
     * Get an I18N Label for a workflow item
     * 
     * @param displayId  message resource id lookup
     * @param labelKey  label to lookup (title or description)
     * @param defaultLabel  default value if not found in message resource bundle
     * @return  the label
     */
    private String getLabel(String displayId, String labelKey, String defaultLabel)
    {
        String key = StringUtils.replace(displayId, ":", "_");
        key += "." + labelKey;
        String label = I18NUtil.getMessage(key);
        return (label == null) ? defaultLabel : label;
    }
    

    //
    // Workflow Data Object Creation...
    //
    
    /**
     * Creates a Workflow Path
     * 
     * @param token  JBoss JBPM Token
     * @return  Workflow Path
     */
    protected WorkflowPath createWorkflowPath(Token token)
    {
        WorkflowPath path = new WorkflowPath();
        String tokenId = token.getFullName().replace("/", WORKFLOW_TOKEN_SEPERATOR);
        path.id = createGlobalId(token.getProcessInstance().getId() + WORKFLOW_PATH_SEPERATOR + tokenId);
        path.instance = createWorkflowInstance(token.getProcessInstance());
        path.node = createWorkflowNode(token.getNode());
        path.active = !token.hasEnded();
        return path;
    }
    
    /**
     * Creates a Workflow Node
     * 
     * @param node  JBoss JBPM Node
     * @return   Workflow Node
     */
    @SuppressWarnings("unchecked")
    protected WorkflowNode createWorkflowNode(Node node)
    {
        String processName = node.getProcessDefinition().getName();
        WorkflowNode workflowNode = new WorkflowNode();
        workflowNode.name = node.getName();
        workflowNode.title = getLabel(processName + ".node." + workflowNode.name, TITLE_LABEL, workflowNode.name);
        workflowNode.description = getLabel(processName + ".node." + workflowNode.name, DESC_LABEL, workflowNode.title);
        if (node instanceof HibernateProxy)
        {
            Node realNode = (Node)((HibernateProxy)node).getHibernateLazyInitializer().getImplementation();        
            workflowNode.type = realNode.getClass().getSimpleName();
        }
        else
        {
            workflowNode.type = node.getClass().getSimpleName();
        }
        // TODO: Is there a formal way of determing if task node?
        workflowNode.isTaskNode = workflowNode.type.equals("TaskNode");
        List transitions = node.getLeavingTransitions();
        workflowNode.transitions = new WorkflowTransition[(transitions == null) ? 0 : transitions.size()];
        if (transitions != null)
        {
            int i = 0;
            for (Transition transition : (List<Transition>)transitions)
            {
                workflowNode.transitions[i++] = createWorkflowTransition(transition);
            }
        }
        return workflowNode;
    }
    
    /**
     * Create a Workflow Transition
     * 
     * @param transition  JBoss JBPM Transition
     * @return  Workflow Transition
     */
    protected WorkflowTransition createWorkflowTransition(Transition transition)
    {
        WorkflowTransition workflowTransition = new WorkflowTransition();
        workflowTransition.id = transition.getName();
        Node node = transition.getFrom();
        workflowTransition.isDefault = node.getDefaultLeavingTransition().equals(transition);
        if (workflowTransition.id == null || workflowTransition.id.length() == 0)
        {
            workflowTransition.title = getLabel(DEFAULT_TRANSITION_LABEL, TITLE_LABEL, workflowTransition.id);
            workflowTransition.description = getLabel(DEFAULT_TRANSITION_LABEL, DESC_LABEL, workflowTransition.title);
        }
        else
        {
            String nodeName = node.getName();
            String processName = node.getProcessDefinition().getName();
            workflowTransition.title = getLabel(processName + ".node." + nodeName + ".transition." + workflowTransition.id, TITLE_LABEL, workflowTransition.id);
            workflowTransition.description = getLabel(processName + ".node." + nodeName + ".transition." + workflowTransition.id, DESC_LABEL, workflowTransition.title);
        }
        return workflowTransition;
    }
        
    /**
     * Creates a Workflow Instance
     * 
     * @param instance  JBoss JBPM Process Instance
     * @return  Workflow instance
     */
    protected WorkflowInstance createWorkflowInstance(ProcessInstance instance)
    {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.id = createGlobalId(new Long(instance.getId()).toString());
        workflowInstance.description = (String)instance.getContextInstance().getVariable(mapQNameToName(WorkflowModel.PROP_WORKFLOW_DESCRIPTION));
        workflowInstance.definition = createWorkflowDefinition(instance.getProcessDefinition());
        workflowInstance.active = !instance.hasEnded();
        JBPMNode initiator = (JBPMNode)instance.getContextInstance().getVariable("initiator");
        if (initiator != null)
        {
            workflowInstance.initiator = initiator.getNodeRef(); 
        }
        JBPMNode context = (JBPMNode)instance.getContextInstance().getVariable(mapQNameToName(WorkflowModel.PROP_CONTEXT));
        if (context != null)
        {
            workflowInstance.context = context.getNodeRef(); 
        }
        JBPMNode workflowPackage = (JBPMNode)instance.getContextInstance().getVariable(mapQNameToName(WorkflowModel.ASSOC_PACKAGE));
        if (workflowPackage != null)
        {
            workflowInstance.workflowPackage = workflowPackage.getNodeRef(); 
        }
        workflowInstance.startDate = instance.getStart();
        workflowInstance.endDate = instance.getEnd();
        return workflowInstance;
    }

    /**
     * Creates a Workflow Definition
     * 
     * @param definition  JBoss Process Definition
     * @return  Workflow Definition
     */
    protected WorkflowDefinition createWorkflowDefinition(ProcessDefinition definition)
    {
        final Task startTask = definition.getTaskMgmtDefinition().getStartTask();
        final String name = definition.getName();
        final String title = getLabel(name + ".workflow", TITLE_LABEL, name);
        final String description = getLabel(name + ".workflow", DESC_LABEL, title);
        return new WorkflowDefinition(createGlobalId(new Long(definition.getId()).toString()),
                                      name,
                                      new Integer(definition.getVersion()).toString(),
                                      title,
                                      description,
                                      (startTask != null
                                       ? createWorkflowTaskDefinition(startTask)
                                       : null));
    }
    
    /**
     * Creates a Workflow Task
     * 
     * @param task  JBoss Task Instance
     * @return  Workflow Task
     */
    protected WorkflowTask createWorkflowTask(TaskInstance task)
    {
        String processName = task.getTask().getProcessDefinition().getName();

        WorkflowTask workflowTask = new WorkflowTask();
        workflowTask.id = createGlobalId(new Long(task.getId()).toString());
        workflowTask.name = task.getName();
        workflowTask.path = createWorkflowPath(task.getToken());
        workflowTask.state = getWorkflowTaskState(task);
        workflowTask.definition = createWorkflowTaskDefinition(task.getTask());
        workflowTask.properties = getTaskProperties(task, false);
        workflowTask.title = getLabel(processName + ".task." + workflowTask.name, TITLE_LABEL, null);
        if (workflowTask.title == null)
        {
            workflowTask.title = workflowTask.definition.metadata.getTitle();
            if (workflowTask.title == null)
            {
                workflowTask.title = workflowTask.name;
            }
        }
        workflowTask.description = getLabel(processName + ".task." + workflowTask.name, DESC_LABEL, null);
        if (workflowTask.description == null)
        {
            String description = workflowTask.definition.metadata.getDescription();
            workflowTask.description = (description == null) ? workflowTask.title : description;
        }
        return workflowTask;
    }
 
    /**
     * Creates a Workflow Task Definition
     * 
     * @param task  JBoss JBPM Task
     * @return  Workflow Task Definition
     */
    protected WorkflowTaskDefinition createWorkflowTaskDefinition(Task task)
    {
        WorkflowTaskDefinition taskDef = new WorkflowTaskDefinition();
        taskDef.id = task.getName();
        Node node = (task.getStartState() == null ? task.getTaskNode() : task.getStartState());
        taskDef.node = createWorkflowNode(node);
        taskDef.metadata = getTaskDefinition(task);
        return taskDef;
    }
    
    /**
     * Creates a Workflow Deployment
     * 
     * @param compiledDef  compiled JBPM process definition
     * @return  workflow deployment
     */
    protected WorkflowDeployment createWorkflowDeployment(CompiledProcessDefinition compiledDef)
    {
        WorkflowDeployment deployment = new WorkflowDeployment();
        deployment.definition = createWorkflowDefinition(compiledDef.def);
        deployment.problems = compiledDef.problems;
        return deployment;
    }
    
    /**
     * Get the Workflow Task State for the specified JBoss JBPM Task
     * 
     * @param task  task
     * @return  task state
     */
    protected WorkflowTaskState getWorkflowTaskState(TaskInstance task)
    {
        if (task.hasEnded())
        {
            return WorkflowTaskState.COMPLETED;
        }
        else
        {
            return WorkflowTaskState.IN_PROGRESS;
        }
    }

}
