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
package org.alfresco.repo.workflow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.BaseInterpreter;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTimer;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * An interactive console for Workflows.
 * 
 * @author davidc
 */
public class WorkflowInterpreter extends BaseInterpreter
{
    // Service dependencies    
    private WorkflowService workflowService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private AuthorityDAO authorityDAO;
    private AVMService avmService;
    private AVMSyncService avmSyncService;
    private PersonService personService;
    private FileFolderService fileFolderService;
    private TenantService tenantService;
    private DictionaryService dictionaryService;

    /**
     * Current context
     */
    private WorkflowDefinition currentWorkflowDef = null;
    private WorkflowPath currentPath = null;
    private String currentDeployResource = null;
    private String currentDeployEngine = null;


    /**
     * Variables
     */
    private Map<QName, Serializable> vars = new HashMap<QName, Serializable>();
    



    /* (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.getContext().ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        //NOOP
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.getContext().ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
    
    /**
     * @param workflowService The Workflow Service
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    /**
     * @param nodeService The Node Service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param tenantService The Tenant Service
     */  
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * @param dictionaryService dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param avmService  The AVM Service
     */
    public void setAVMService(AVMService avmService)
    {
    	this.avmService = avmService;
    }
    
    /**
     * @param avmSyncService  The AVM Sync Service
     */
    public void setAVMSyncService(AVMSyncService avmSyncService)
    {
    	this.avmSyncService = avmSyncService;
    }

    /**
     * @param namespaceService  namespaceService
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param personService  personService
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * @param transactionService  transactionService
     */
    @Override
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param authorityDAO  authorityDAO
     */
    public void setAuthorityDAO(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }

    /**
     * @param fileFolderService  fileFolderService
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args)
    {
        runMain("workflowInterpreter");
    }

    @Override
    protected boolean hasAuthority(String username)
    {
        // admin can change to any user (via worklow command "user <username>")
        return true;
    }
    
    /**
     * Execute a single command using the BufferedReader passed in for any data needed.
     * 
     * TODO: Use decent parser!
     * 
     * @param line The unparsed command
     * @return The textual output of the command.
     */
    @Override
    protected String executeCommand(String line)
        throws IOException
    {
        String[] command = line.split(" ");
        if (command.length == 0)
        {
            command = new String[1];
            command[0] = line;
        }
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);

        // repeat last command?
        if (command[0].equals("r"))
        {
            if (lastCommand == null)
            {
                return "No command entered yet.";
            }
            return "repeating command " + lastCommand + "\n\n" + executeCommand(lastCommand);
        }
        
        // remember last command
        lastCommand = line;

        // execute command
        if (command[0].equals("help"))
        {
            String helpFile = I18NUtil.getMessage("workflow_console.help");
            ClassPathResource helpResource = new ClassPathResource(helpFile);
            byte[] helpBytes = new byte[500];
            InputStream helpStream = helpResource.getInputStream();
            try
            {
                int read = helpStream.read(helpBytes);
                while (read != -1)
                {
                    bout.write(helpBytes, 0, read);
                    read = helpStream.read(helpBytes);
                }
            }
            finally
            {
                helpStream.close();
            }
        }
        
        else if (command[0].equals("show"))
        {
            if (command.length < 2)
            {
                return "Syntax Error.\n";
            }
            
            else if (command[1].equals("file"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.\n";
                }
                ClassPathResource file = new ClassPathResource(command[2]);
                InputStream fileStream = file.getInputStream();
                byte[] fileBytes = new byte[500];
                try
                {
                    int read = fileStream.read(fileBytes);
                    while (read != -1)
                    {
                        bout.write(fileBytes, 0, read);
                        read = fileStream.read(fileBytes);
                    }
                }
                finally
                {
                    fileStream.close();
                }
                out.println();
            }
            
            else if (command[1].equals("definitions"))
            {
                List<WorkflowDefinition> defs = null; 
                if (command.length == 3)
                {
                    if (command[2].equals("all"))
                    {
                        defs = workflowService.getAllDefinitions();
                    }
                    else
                    {
                        return "Syntax Error.\n";
                    }
                }
                else
                {
                    defs = workflowService.getDefinitions();
                }
                for (WorkflowDefinition def : defs)
                {
                    out.println("id: " + def.getId() + " , name: " + def.getName() + " , title: " + def.getTitle() + " , version: " + def.getVersion());
                }
            }
            
            else if (command[1].equals("workflows"))
            {
                String id = (currentWorkflowDef != null) ? currentWorkflowDef.getId() : null;
                if (id == null && command.length == 2)
                {
                    return "workflow definition not in use.  Enter command 'show workflows all' or 'use <workflowDefId>'.\n";
                }
                if (command.length == 3)
                {
                    if (command[2].equals("all"))
                    {
                        id = "all";
                    }
                    else
                    {
                        return "Syntax Error.\n";
                    }
                }
                
                if ("all".equals(id))
                {
                    for (WorkflowDefinition def : workflowService.getAllDefinitions())
                    {
                        List<WorkflowInstance> workflows = workflowService.getActiveWorkflows(def.getId());
                        for (WorkflowInstance workflow : workflows)
                        {
                            out.println("id: " + workflow.getId() + " , desc: " + workflow.getDescription() + " , start date: " + workflow.getStartDate() + " , def: " + workflow.getDefinition().getName() + " v" + workflow.getDefinition().getVersion());
                        }
                    }
                }
                else
                {
                    List<WorkflowInstance> workflows = workflowService.getActiveWorkflows(id);
                    for (WorkflowInstance workflow : workflows)
                    {
                        out.println("id: " + workflow.getId() + " , desc: " + workflow.getDescription() + " , start date: " + workflow.getStartDate() + " , def: " + workflow.getDefinition().getName());
                    }
                }
            }
            
            else if (command[1].equals("paths"))
            {
                String workflowId = (command.length == 3) ? command[2] : (currentPath == null) ? null : currentPath.getInstance().getId();
                if (workflowId == null)
                {
                    return "Syntax Error.  Workflow Id not specified.\n";
                }
                List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowId);
                for (WorkflowPath path : paths)
                {
                    out.println("path id: " + path.getId() + " , node: " + path.getNode().getName());
                }
            }
            
            else if (command[1].equals("tasks"))
            {
                String pathId = (command.length == 3) ? command[2] : (currentPath == null) ? null : currentPath.getId();
                if (pathId == null)
                {
                    return "Syntax Error.  Path Id not specified.\n";
                }
                List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(pathId);
                for (WorkflowTask task : tasks)
                {
                    out.println("task id: " + task.getId() + " , name: " + task.getName() + " , properties: " + task.getProperties().size());
                }
            }
            
            else if (command[1].equals("transitions"))
            {
                String workflowId = (command.length == 3) ? command[2] : (currentPath == null) ? null : currentPath.getInstance().getId();
                if (workflowId == null)
                {
                    return "Syntax Error.  Workflow Id not specified.\n";
                }
                List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowId);
                if (paths.size() == 0)
                {
                    out.println("no further transitions");
                }
                for (WorkflowPath path : paths)
                {
                    out.println("path: " + path.getId() + " , node: " + path.getNode().getName() + " , active: " + path.isActive());
                    List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
                    for (WorkflowTask task : tasks)
                    {
                        out.println(" task id: " + task.getId() + " , name: " + task.getName() + ", title: " + task.getTitle() + " , desc: " + task.getDescription() + " , properties: " + task.getProperties().size());
                    }
                    for (WorkflowTransition transition : path.getNode().getTransitions())
                    {
                        out.println(" transition id: " + ((transition.getId() == null || transition.getId().equals("")) ? "[default]" : transition.getId()) + " , title: " + transition.getTitle());
                    }
                }
            }
            
            else if (command[1].equals("timers"))
            {
                String id = (currentWorkflowDef != null) ? currentWorkflowDef.getId() : null;
                if (id == null && command.length == 2)
                {
                    return "workflow definition not in use.  Enter command 'show timers all' or 'use <workflowDefId>'.\n";
                }
                if (command.length == 3)
                {
                    if (command[2].equals("all"))
                    {
                        id = "all";
                    }
                    else
                    {
                        return "Syntax Error.\n";
                    }
                }
                
                List<WorkflowTimer> timers = new ArrayList<WorkflowTimer>();
                
                if ("all".equals(id))
                {
                    for (WorkflowDefinition def : workflowService.getAllDefinitions())
                    {
                        List<WorkflowInstance> workflows = workflowService.getActiveWorkflows(def.getId());
                        for (WorkflowInstance workflow : workflows)
                        {
                            timers.addAll(workflowService.getTimers(workflow.getId()));
                        }
                    }
                }
                else
                {
                    List<WorkflowInstance> workflows = workflowService.getActiveWorkflows(id);
                    for (WorkflowInstance workflow : workflows)
                    {
                        timers.addAll(workflowService.getTimers(workflow.getId()));
                    }
                }
                
                for (WorkflowTimer timer : timers)
                {
                    out.print("id: " + timer.getId() + " , name: " + timer.getName() + " , due date: " + timer.getDueDate() + " , path: " + timer.getPath().getId() + " , node: " + timer.getPath().getNode().getName() + " , process: " + timer.getPath().getInstance().getId());
                    if (timer.getTask() != null)
                    {
                        out.print(" , task: " + timer.getTask().getName() + "(" + timer.getTask().getId() + ")");
                    }
                    out.println();
                    if (timer.getError() != null)
                    {
                        out.println("error executing timer id " + timer.getId());
                        out.println(timer.getError());
                    }
                }
            }
            
            else if (command[1].equals("my"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.\n";
                }
                
                if (command[2].equals("tasks"))
                {
                    out.println(AuthenticationUtil.getRunAsUser() + ":");
                    List<WorkflowTask> tasks = workflowService.getAssignedTasks(AuthenticationUtil.getRunAsUser(), WorkflowTaskState.IN_PROGRESS);
                    for (WorkflowTask task : tasks)
                    {
                        out.println("id: " + task.getId() + " , name: " + task.getName() + " , properties: " + task.getProperties().size() + " , workflow: " + task.getPath().getInstance().getId() + " , path: " + task.getPath().getId());
                    }
                }
                
                else if (command[2].equals("completed"))
                {
                    out.println(AuthenticationUtil.getRunAsUser() + ":");
                    List<WorkflowTask> tasks = workflowService.getAssignedTasks(AuthenticationUtil.getRunAsUser(), WorkflowTaskState.COMPLETED);
                    for (WorkflowTask task : tasks)
                    {
                        out.println("id: " + task.getId() + " , name " + task.getName() + " , properties: " + task.getProperties().size() + " , workflow: " + task.getPath().getInstance().getId() + " , path: " + task.getPath().getId());
                    }
                }
                
                else if (command[2].equals("pooled"))
                {
                    out.println(AuthenticationUtil.getRunAsUser() + ":");
                    List<WorkflowTask> tasks = workflowService.getPooledTasks(AuthenticationUtil.getRunAsUser());
                    for (WorkflowTask task : tasks)
                    {
                        out.println("id: " + task.getId() + " , name " + task.getName() + " , properties: " + task.getProperties().size() + " , workflow: " + task.getPath().getInstance().getId() + " , path: " + task.getPath().getId());
                    }
                }

                else
                {
                    return "Syntax Error.\n";
                }
            }
            else
            {
                return "Syntax Error.\n";
            }
        }
        
        else if (command[0].equals("desc"))
        {
            if (command.length < 2)
            {
                return "Syntax Error.\n";
            }
            
            if (command[1].equals("task"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.\n";
                }
                WorkflowTask task = workflowService.getTaskById(command[2]);
                out.println("id: " + task.getId());
                out.println("name: " + task.getName());
                out.println("title: " + task.getTitle());
                out.println("description: " + task.getDescription());
                out.println("state: " + task.getState());
                out.println("path: " + task.getPath().getId());
                out.println("transitions: " + task.getDefinition().getNode().getTransitions().length);
                for (WorkflowTransition transition : task.getDefinition().getNode().getTransitions())
                {
                    out.println(" transition: " + ((transition.getId() == null || transition.getId().equals("")) ? "[default]" : transition.getId()) + " , title: " + transition.getTitle() + " , desc: " + transition.getDescription());
                }
                out.println("properties: " + task.getProperties().size());
                for (Map.Entry<QName, Serializable> prop : task.getProperties().entrySet())
                {
                    out.println(" " + prop.getKey() + " = " + prop.getValue());
                }
            }
            
            else if (command[1].equals("workflow"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.\n";
                }
                WorkflowInstance workflow = workflowService.getWorkflowById(command[2]);
                out.println("definition: " + workflow.getDefinition().getName());
                out.println("id: " + workflow.getId());
                out.println("description: " + workflow.getDescription());
                out.println("active: " + workflow.isActive());
                out.println("start date: " + workflow.getStartDate());
                out.println("end date: " + workflow.getEndDate());
                out.println("initiator: " + workflow.getInitiator());
                out.println("context: " + workflow.getContext());
                out.println("package: " + workflow.getWorkflowPackage());
            }
            
            else if (command[1].equals("path"))
            {
                if (command.length != 3)
                {
                    return "Syntax Error.\n";
                }
                Map<QName, Serializable> properties = workflowService.getPathProperties(command[2]);
                out.println("path: " + command[1]);
                out.println("properties: " + properties.size());
                for (Map.Entry<QName, Serializable> prop : properties.entrySet())
                {
                    out.println(" " + prop.getKey() + " = " + prop.getValue());
                }
            }
            
            else
            {
                return "Syntax Error.\n";
            }
        }
        
        else if (command[0].equals("query"))
        {
            if (command.length < 2)
            {
                return "Syntax Error.\n";
            }
            
            if (command[1].equals("task"))
            {
                // build query
                WorkflowTaskQuery query = new WorkflowTaskQuery();
                Map<QName, Object> taskProps = new HashMap<QName, Object>();
                Map<QName, Object> procProps = new HashMap<QName, Object>();
                
                for (int i = 2; i < command.length; i++)
                {
                    String[] predicate = command[i].split("=");
                    if (predicate.length == 1)
                    {
                        return "Syntax Error.\n";
                    }
                    String[] predicateName = predicate[0].split("\\.");
                    if (predicateName.length == 1)
                    {
                        if (predicate[0].equals("taskId"))
                        {
                            query.setTaskId(predicate[1]);
                        }
                        else if (predicate[0].equals("taskState"))
                        {
                            WorkflowTaskState state = WorkflowTaskState.valueOf(predicate[1]);
                            if (state == null)
                            {
                                return "Syntax Error.  Unknown task state\n";
                            }
                            query.setTaskState(state);
                        }
                        else if (predicate[0].equals("taskName"))
                        {
                            query.setTaskName(QName.createQName(predicate[1], namespaceService));
                        }
                        else if (predicate[0].equals("taskActor"))
                        {
                            query.setActorId(predicate[1]);
                        }
                        else if (predicate[0].equals("processId"))
                        {
                            query.setProcessId(predicate[1]);                            
                        }
                        else if (predicate[0].equals("processName"))
                        {
                            query.setProcessName(QName.createQName(predicate[1], namespaceService));
                        }
                        else if (predicate[0].equals("workflowDefinitionName"))
                        {
                            query.setWorkflowDefinitionName(predicate[1]);
                        }
                        else if (predicate[0].equals("processActive"))
                        {
                            Boolean active = Boolean.valueOf(predicate[1]);
                            query.setActive(active);
                        }
                        else if (predicate[0].equals("orderBy"))
                        {
                            String[] orderBy = predicate[1].split(",");
                            WorkflowTaskQuery.OrderBy[] queryOrderBy = new WorkflowTaskQuery.OrderBy[orderBy.length];
                            for (int iOrderBy = 0; iOrderBy < orderBy.length; iOrderBy++)
                            {
                                queryOrderBy[iOrderBy] = WorkflowTaskQuery.OrderBy.valueOf(orderBy[iOrderBy]);
                                if (queryOrderBy[iOrderBy] == null)
                                {
                                    return "Syntax Error.  Unknown orderBy.\n";
                                }
                            }
                            query.setOrderBy(queryOrderBy);
                        }
                        else
                        {
                            return "Syntax Error.  Unknown query predicate.\n";
                        }
                    }
                    else if (predicateName.length == 2)
                    {
                        if (predicateName[0].equals("task"))
                        {
                            taskProps.put(QName.createQName(predicateName[1], namespaceService), predicate[1]);
                        }
                        else if (predicateName[0].equals("process"))
                        {
                            procProps.put(QName.createQName(predicateName[1], namespaceService), predicate[1]);
                        }
                        else
                        {
                            return "Syntax Error.  Unknown query predicate.\n";
                        }
                    }
                    else
                    {
                        return "Syntax Error.\n";
                    }
                }
                
                if (taskProps.size() > 0)
                {
                    query.setTaskCustomProps(taskProps);
                }
                if (procProps.size() > 0)
                {
                    query.setProcessCustomProps(procProps);
                }
                                
                // execute query
                List<WorkflowTask> tasks = workflowService.queryTasks(query);
                out.println("found " + tasks.size() + " tasks.");
                for (WorkflowTask task : tasks)
                {
                    out.println("task id: " + task.getId() + " , name: " + task.getName() + " , properties: " + task.getProperties().size() + ", process id: " + task.getPath().getInstance());
                }
            }
            
            else
            {
                return "Syntax Error.\n";
            }
        }
        
        else if (command[0].equals("deploy"))
        {
            if (command.length != 3)
            {
                return "Syntax Error.\n";
            }
            ClassPathResource workflowDef = new ClassPathResource(command[2]);
            WorkflowDeployment deployment = workflowService.deployDefinition(command[1], workflowDef.getInputStream(), MimetypeMap.MIMETYPE_XML);
            WorkflowDefinition def = deployment.getDefinition();
            for (String problem : deployment.getProblems())
            {
                out.println(problem);
            }
            out.println("deployed definition id: " + def.getId() + " , name: " + def.getName() + " , title: " + def.getTitle() + " , version: " + def.getVersion());
            currentDeployEngine = command[1];
            currentDeployResource = command[2];
            out.print(executeCommand("use definition " + def.getId()));
        }

        else if (command[0].equals("redeploy"))
        {
            if (currentDeployResource == null)
            {
                return "nothing to redeploy\n";
            }
            out.print(executeCommand("deploy " + currentDeployEngine + " " + currentDeployResource));
        }
        
        else if (command[0].equals("undeploy"))
        {
            if (command.length < 2)
            {
                return "Syntax Error.\n";
            }
            if (command[1].equals("definition"))
            {
                if (command.length == 3)
                {
                    workflowService.undeployDefinition(command[2]);
                    currentWorkflowDef = null;
                    currentPath = null;
                    out.print(executeCommand("show definitions"));
                }
                else if (command.length == 4)
                {
                    if (command[2].equals("name"))
                    {
                        out.print("undeploying...");
                        List<WorkflowDefinition> defs = workflowService.getAllDefinitionsByName(command[3]);
                        for (WorkflowDefinition def: defs)
                        {
                            workflowService.undeployDefinition(def.getId());
                            out.print(" v" + def.getVersion());
                        }
                        out.println("");
                        currentWorkflowDef = null;
                        currentPath = null;
                        out.print(executeCommand("show definitions all"));
                    }
                    else
                    {
                        return "Syntax Error.\n";
                    }
                }
                else
                {
                    return "Syntax Error.\n";
                }
            }
            else
            {
                return "Syntax Error.\n";
            }
        }
        
        else if (command[0].equals("use"))
        {
            if (command.length == 1)
            {
                out.println("definition: " + ((currentWorkflowDef == null) ? "None" : currentWorkflowDef.getId() + " , name: " + currentWorkflowDef.getTitle() + " , version: " + currentWorkflowDef.getVersion()));
                out.println("workflow: " + ((currentPath == null) ? "None" : currentPath.getInstance().getId() + " , active: " + currentPath.getInstance().isActive()));
                out.println("path: " + ((currentPath == null) ? "None" : currentPath.getId() + " , node: " + currentPath.getNode().getTitle()));
            }
            else if (command.length > 1)
            {
                if (command[1].equals("definition"))
                {
                    if (command.length != 3)
                    {
                        return "Syntax Error.\n";
                    }
                    WorkflowDefinition def = workflowService.getDefinitionById(command[2]);
                    if (def == null)
                    {
                        return "Not found.\n";
                    }
                    currentWorkflowDef = def;
                    currentPath = null;
                    out.print(executeCommand("use"));
                }
                
                else if (command[1].equals("workflow"))
                {
                    if (command.length != 3)
                    {
                        return "Syntax Error.\n";
                    }
                    WorkflowInstance instance = workflowService.getWorkflowById(command[2]);
                    currentWorkflowDef = instance.getDefinition();
                    currentPath = workflowService.getWorkflowPaths(instance.getId()).get(0);
                    out.print(executeCommand("use"));
                }
                else
                {
                    return "Syntax Error.\n";
                }
            }            
        }
        
        else if (command[0].equals("user"))
        {
            if (command.length == 2)
            {
                if (tenantService.isEnabled())
                {
                    tenantService.checkDomainUser(command[1]);
                }
                setCurrentUserName(command[1]);
            }
            out.println("using user " + getCurrentUserName());
        }
        
        else if (command[0].equals("start"))
        {
            Map<QName, Serializable> params = new HashMap<QName, Serializable>();
            for (int i = 1; i < command.length; i++)
            {
                String[] param = command[i].split("=");
                QName qname = QName.createQName(param[0], namespaceService);
                if (param.length == 1)
                {
                    if (!vars.containsKey(qname))
                    {
                        return "var " + qname + " not found.\n";
                    }
                    params.put(qname, vars.get(qname));
                }
                else if (param.length == 2)
                {
                    params.put(qname, param[1]);
                }
                else
                {
                    return "Syntax Error.\n";
                }
            }
            if (currentWorkflowDef == null)
            {
                return "Workflow definition not selected.\n";
            }
            setupStartTaskParameters(currentWorkflowDef.getStartTaskDefinition().metadata, params);
            WorkflowPath path = workflowService.startWorkflow(currentWorkflowDef.getId(), params);
            endStartTaskForPath(path);
            out.println("started workflow id: " + path.getInstance().getId() + " , def: " + path.getInstance().getDefinition().getTitle());
            currentPath = path;
            out.print(interpretCommand("show transitions"));
        }
        
        else if (command[0].equals("update"))
        {
            if (command.length < 3)
            {
                return "Syntax Error.\n";
            }
            
            if (command[1].equals("task"))
            {
                if (command.length < 4)
                {
                    return "Syntax Error.\n";
                }
                Map<QName, Serializable> params = new HashMap<QName, Serializable>();
                for (int i = 3; i < command.length; i++)
                {
                    String[] param = command[i].split("=");
                    QName qname = QName.createQName(param[0], namespaceService);
                    if (param.length == 1)
                    {
                        if (!vars.containsKey(qname))
                        {
                            return "var " + qname + " not found.\n";
                        }
                        params.put(qname, vars.get(qname));
                    }
                    else if (param.length == 2)
                    {
                        params.put(qname, param[1]);
                    }
                    else
                    {
                        return "Syntax Error.\n";
                    }
                }
                WorkflowTask task = workflowService.updateTask(command[2], params, null, null);
                out.println("updated task id: " + command[2] + ", properties: " + task.getProperties().size());
            }
            else
            {
                return "Syntax Error.\n";
            }
        }
        
        else if (command[0].equals("signal"))
        {
            if (command.length < 2)
            {
                return "Syntax Error.\n";
            }
            WorkflowPath path = workflowService.signal(command[1], (command.length == 3) ? command[2] : null);
            out.println("signal sent - path id: " + path.getId());
            out.print(interpretCommand("show transitions"));
        }
        
        else if (command[0].equals("event"))
        {
            if (command.length < 3)
            {
                return "Syntax Error.\n";
            }
            WorkflowPath path = workflowService.fireEvent(command[1], command[2]);
            out.println("event " + command[2] + " fired - path id: " + path.getId());
            out.print(interpretCommand("show transitions"));
        }

        else if (command[0].equals("end"))
        {
            if (command.length < 3)
            {
                return "Syntax Error.\n";
            }
            if (command[1].equals("task"))
            {
                WorkflowTask task = workflowService.endTask(command[2], (command.length == 4) ? command[3] : null);
                out.println("signal sent - path id: " + task.getPath().getId());
                out.print(interpretCommand("show transitions"));
            }
            else if (command[1].equals("workflow"))
            {
                String workflowId = (command.length == 3) ? command[2] : (currentPath == null) ? null : currentPath.getInstance().getId();
                if (workflowId == null)
                {
                    return "Syntax Error.  Workflow Id not specified.\n";
                }
                workflowService.cancelWorkflow(workflowId);
                out.println("workflow " + workflowId + " cancelled.");
            }
            else
            {
                return "Syntax Error.\n";
            }
        }

        else if (command[0].equals("delete"))
        {
            if (command.length < 2)
            {
                return "Syntax Error.\n";
            }
            if (command[1].equals("workflow"))
            {
                String workflowId = (command.length == 3) ? command[2] : (currentPath == null) ? null : currentPath.getInstance().getId();
                if (workflowId == null)
                {
                    return "Syntax Error.  Workflow Id not specified.\n";
                }
                workflowService.deleteWorkflow(workflowId);
                out.println("workflow " + workflowId + " deleted.");
            }
            else if (command[1].equals("all"))
            {
                if (command.length < 3)
                {
                    return "Syntax Error.\n";
                }
                if (command[2].equals("workflows"))
                {
                    if (command.length < 4)
                    {
                        return "Enter the command 'delete all workflows imeanit' to really delete all workflows\n";
                    }
                    if (command[3].equals("imeanit"))
                    {
                        for (WorkflowDefinition def : workflowService.getAllDefinitions())
                        {
                            List<WorkflowInstance> workflows = workflowService.getActiveWorkflows(def.getId());
                            for (WorkflowInstance workflow : workflows)
                            {
                                workflowService.deleteWorkflow(workflow.getId());
                                out.println("workflow " + workflow.getId() + " deleted.");
                            }
                        }
                    }
                    else
                    {
                        return "Syntax Error.\n";
                    }
                }
                else
                {
                    return "Syntax Error.\n";
                }
            }
            else
            {
                return "Syntax Error.\n";
            }
        }

        else if (command[0].equals("var"))
        {
            if (command.length == 1)
            {
                for (Map.Entry<QName, Serializable> entry : vars.entrySet())
                {
                    out.println(entry.getKey() + " = " + entry.getValue());
                }
            }
            else if (command.length == 2)
            {
                String[] param = command[1].split("=");
                if (param.length == 0)
                {
                    return "Syntax Error.\n";
                }
                if (param.length == 1)
                {
                    QName qname = QName.createQName(param[0], namespaceService);
                    vars.remove(qname);
                    out.println("deleted var " + qname);
                }
                else if (param.length == 2)
                {
                    boolean multi = false;
                    if (param[0].endsWith("*"))
                    {
                        param[0] = param[0].substring(0, param[0].length() -1);
                        multi = true;
                    }
                    QName qname = QName.createQName(param[0], namespaceService);
                    String[] strValues = param[1].split(",");
                    if (!multi && strValues.length > 1)
                    {
                        return "Syntax Error.\n";
                    }
                    if (!multi)
                    {
                        vars.put(qname, strValues[0]);
                    }
                    else
                    {
                        List<String> values = new ArrayList<String>();
                        for (String strValue : strValues)
                        {
                            values.add(strValue);
                        }
                        vars.put(qname, (Serializable)values);
                    }
                    out.println("set var " + qname + " = " + vars.get(qname));
                }
                else
                {
                    return "Syntax Error.\n";
                }
            }
            else if (command.length == 4)
            {
                if (command[2].equals("person"))
                {
                    boolean multi = false;
                    if (command[1].endsWith("*"))
                    {
                        command[1] = command[1].substring(0, command[1].length() -1);
                        multi = true;
                    }
                    QName qname = QName.createQName(command[1], namespaceService);
                    String[] strValues = command[3].split(",");
                    if (!multi && strValues.length > 1)
                    {
                        return "Syntax Error.\n";
                    }
                    if (!multi)
                    {
                        NodeRef auth = personService.getPerson(strValues[0]);
                        vars.put(qname, auth);
                    }
                    else
                    {
                        List<NodeRef> values = new ArrayList<NodeRef>();
                        for (String strValue : strValues)
                        {
                            NodeRef auth = personService.getPerson(strValue);
                            values.add(auth);
                        }
                        vars.put(qname, (Serializable)values);
                    }
                    out.println("set var " + qname + " = " + vars.get(qname));
                }
                else if (command[2].equals("group"))
                {
                    boolean multi = false;
                    if (command[1].endsWith("*"))
                    {
                        command[1] = command[1].substring(0, command[1].length() -1);
                        multi = true;
                    }
                    QName qname = QName.createQName(command[1], namespaceService);
                    String[] strValues = command[3].split(",");
                    if (!multi && strValues.length > 1)
                    {
                        return "Syntax Error.\n";
                    }
                    if (!multi)
                    {
                        NodeRef auth = authorityDAO.getAuthorityNodeRefOrNull(strValues[0]);
                        if (auth == null)
                        {
                            throw new WorkflowException("Group " + strValues[0] + " does not exist.");
                        }
                        vars.put(qname, auth);
                    }
                    else
                    {
                        List<NodeRef> values = new ArrayList<NodeRef>();
                        for (String strValue : strValues)
                        {
                            NodeRef auth = authorityDAO.getAuthorityNodeRefOrNull(strValue);
                            if (auth == null)
                            {
                                throw new WorkflowException("Group " + strValue + " does not exist.");
                            }
                            values.add(auth);
                        }
                        vars.put(qname, (Serializable)values);
                    }
                    out.println("set var " + qname + " = " + vars.get(qname));
                }
                else if (command[2].equals("avmpackage"))
                {
                	// lookup source folder of changes
                	AVMNodeDescriptor avmSource = avmService.lookup(-1, command[3]);
                	if (avmSource == null || !avmSource.isDirectory())
                	{
                		return command[3] + " must refer to a directory.";
                	}
                	
                	// create container for avm workflow packages
                	String packagesPath = "workflow-system:/packages";
                	AVMNodeDescriptor packagesDesc = avmService.lookup(-1, packagesPath);
                	if (packagesDesc == null)
                	{
                		avmService.createStore("workflow-system");
                		avmService.createDirectory("workflow-system:/", "packages");
                	}

                	// create package (layered to target, if target is specified)
                	String packageName = GUID.generate();
                	String avmSourceIndirection = avmSource.getIndirection();
                	if (avmSourceIndirection != null)
                	{
                    	avmService.createLayeredDirectory(avmSourceIndirection, packagesPath, packageName);
                		List<AVMDifference> diff = avmSyncService.compare(-1, avmSource.getPath(), -1, packagesPath + "/" + packageName, null);
                        avmSyncService.update(diff, null, true, true, false, false, null, null);
                	}
                	else
                	{
                		// copy source folder to package folder
                		avmService.copy(-1, avmSource.getPath(), packagesPath, packageName);
                	}

                	// convert package to workflow package
                	AVMNodeDescriptor packageDesc = avmService.lookup(-1, packagesPath + "/" + packageName);
                	NodeRef packageNodeRef = workflowService.createPackage(AVMNodeConverter.ToNodeRef(-1, packageDesc.getPath()));
                	nodeService.setProperty(packageNodeRef, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, true);
                    QName qname = QName.createQName(command[1], namespaceService);
                	vars.put(qname, packageNodeRef);
                    out.println("set var " + qname + " = " + vars.get(qname));
                }
                else if (command[2].equals("package"))
                {
                    QName qname = QName.createQName(command[1], namespaceService);
                    int number = new Integer(command[3]);
                    NodeRef packageNodeRef = workflowService.createPackage(null);
                    for (int i = 0; i < number; i++)
                    {
                        FileInfo fileInfo = fileFolderService.create(packageNodeRef, "Content" + i, ContentModel.TYPE_CONTENT);
                        ContentWriter writer = fileFolderService.getWriter(fileInfo.getNodeRef());
                        writer.putContent("Content" + i);
                    }
                    vars.put(qname, packageNodeRef);
                    out.println("set var " + qname + " = " + vars.get(qname));
                }
                else
                {
                    return "Syntax Error.\n";
                }
            }
            else
            {
                return "Syntax Error.\n";
            }
        }
        
        else
        {
            return "Syntax Error.\n";
        }
 
        out.flush();
        String retVal = new String(bout.toByteArray());
        out.close();
        return retVal;
    }
    

    /**
     * Get currently used workflow definition
     * 
     * @return  workflow definition
     */
    public WorkflowDefinition getCurrentWorkflowDef()
    {
        return currentWorkflowDef;
    }

    private void setupStartTaskParameters(TypeDefinition typeDef, Map<QName, Serializable> params)
    {
        // build a complete anonymous type for the start task
        List<AspectDefinition> aspects = typeDef.getDefaultAspects();
        List<QName> aspectNames = new ArrayList<QName>(aspects.size());
        getMandatoryAspects(typeDef, aspectNames);
        ClassDefinition startTaskDef = dictionaryService.getAnonymousType(typeDef.getName(), aspectNames);

        // apply default values
        Map<QName, PropertyDefinition> propertyDefs = startTaskDef.getProperties(); 
        for (Map.Entry<QName, PropertyDefinition> entry : propertyDefs.entrySet())
        {
            String defaultValue = entry.getValue().getDefaultValue();

            if (params.get(entry.getKey()) == null)
            {
                if (defaultValue != null)
                {
                    params.put(entry.getKey(), (Serializable)DefaultTypeConverter.INSTANCE.convert(entry.getValue().getDataType(), defaultValue));
                }
            }
            else
            {
                params.put(entry.getKey(), (Serializable)DefaultTypeConverter.INSTANCE.convert(entry.getValue().getDataType(), params.get(entry.getKey())));
            }
        }
        
        if (params.containsKey(WorkflowModel.ASSOC_ASSIGNEE))
        {
            String value = (String)params.get(WorkflowModel.ASSOC_ASSIGNEE);
            ArrayList<NodeRef> assignees = new ArrayList<NodeRef>();
            assignees.add(personService.getPerson(value));
            params.put(WorkflowModel.ASSOC_ASSIGNEE, assignees);
        }
        
        params.put(WorkflowModel.ASSOC_PACKAGE, workflowService.createPackage(null));
    }

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

    private void endStartTaskForPath(WorkflowPath path)
    {
        if (path != null)
        {
           List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
           if (tasks.size() == 1)
           {
              WorkflowTask startTask = tasks.get(0);
              
              if (startTask.state == WorkflowTaskState.IN_PROGRESS)
              {
                 // end the start task to trigger the first 'proper'
                 // task in the workflow
                 this.workflowService.endTask(startTask.id, null);
              }
           }
        }
    }
}