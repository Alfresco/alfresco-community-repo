/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.repo.workflow;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * An interactive console for Workflows.
 * 
 * @author davidc
 */
public class WorkflowInterpreter
{
    // Service dependencies    
    private WorkflowService workflowService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    private AVMService avmService;
    private AVMSyncService avmSyncService;
    private PersonService personService;
    
    /**
     * The reader for interaction.
     */
    private BufferedReader fIn;

    /**
     * Current context
     */
    private WorkflowDefinition currentWorkflowDef = null;
    private WorkflowPath currentPath = null;
    private String currentDeploy = null;
    
    /**
     * Last command issued
     */
    private String lastCommand = null;
    
    /**
     * Variables
     */
    private Map<QName, Serializable> vars = new HashMap<QName, Serializable>();
    

    /**
     * Main entry point.
     *   
     * Syntax: AVMInteractiveConsole storage (new|old).
     */
    public static void main(String[] args)
    {
        ApplicationContext context = ApplicationContextHelper.getApplicationContext();
        WorkflowInterpreter console = (WorkflowInterpreter)context.getBean("workflowInterpreter");
        AuthenticationUtil.setSystemUserAsCurrentUser();
        console.rep();
        System.exit(0);
    }

    /**
     * Make up a new console.
     */
    public WorkflowInterpreter()
    {
        fIn = new BufferedReader(new InputStreamReader(System.in));
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
     * A Read-Eval-Print loop.
     */
    public void rep()
    {
        while (true)
        {
            System.out.print("ok> ");
            try
            {
                String line = fIn.readLine();
                if (line.equals("exit"))
                {
                    return;
                }
                long startms = System.currentTimeMillis();
                System.out.print(interpretCommand(line));
                System.out.println("" + (System.currentTimeMillis() - startms) + "ms");
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                System.out.println("");
            }
        }
    }

    /**
     * Interpret a single command using the BufferedReader passed in for any data needed.
     * 
     * @param line The unparsed command
     * @return The textual output of the command.
     */
    public String interpretCommand(String line)
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
            return "repeating command " + lastCommand + "\n\n" + interpretCommand(lastCommand);
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
            
            else if (command[1].equals("definitions"))
            {
                List<WorkflowDefinition> defs = workflowService.getDefinitions();
                for (WorkflowDefinition def : defs)
                {
                    out.println("id: " + def.id + " , name: " + def.name + " , title: " + def.title + " , version: " + def.version);
                }
            }
            
            else if (command[1].equals("workflows"))
            {
                if (currentWorkflowDef == null)
                {
                    return "workflow definition not in use.  Enter command use <workflowDefId>.\n";
                }
                List<WorkflowInstance> workflows = workflowService.getActiveWorkflows(currentWorkflowDef.id);
                for (WorkflowInstance workflow : workflows)
                {
                    out.println("id: " + workflow.id + " , desc: " + workflow.description + " , start date: " + workflow.startDate + " , def: " + workflow.definition.title);
                }
            }
            
            else if (command[1].equals("paths"))
            {
                String workflowId = (command.length == 3) ? command[2] : (currentPath == null) ? null : currentPath.instance.id;
                if (workflowId == null)
                {
                    return "Syntax Error.  Workflow Id not specified.\n";
                }
                List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowId);
                for (WorkflowPath path : paths)
                {
                    out.println("path id: " + path.id + " , node: " + path.node.name);
                }
            }
            
            else if (command[1].equals("tasks"))
            {
                String pathId = (command.length == 3) ? command[2] : (currentPath == null) ? null : currentPath.id;
                if (pathId == null)
                {
                    return "Syntax Error.  Path Id not specified.\n";
                }
                List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(pathId);
                for (WorkflowTask task : tasks)
                {
                    out.println("task id: " + task.id + " , name: " + task.name + " , properties: " + task.properties.size());
                }
            }
            
            else if (command[1].equals("transitions"))
            {
                String workflowId = (command.length == 3) ? command[2] : (currentPath == null) ? null : currentPath.instance.id;
                if (workflowId == null)
                {
                    return "Syntax Error.  Workflow Id not specified.\n";
                }
                List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowId);
                for (WorkflowPath path : paths)
                {
                    out.println("path: " + path.id + " , node: " + path.node.name + " , active: " + path.active);
                    List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.id);
                    for (WorkflowTask task : tasks)
                    {
                        out.println(" task id: " + task.id + " , name: " + task.name + " , properties: " + task.properties.size());
                    }
                    for (WorkflowTransition transition : path.node.transitions)
                    {
                        out.println(" transition id: " + ((transition.id == null || transition.id.equals("")) ? "[default]" : transition.id) + " , title: " + transition.title);
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
                    out.println(AuthenticationUtil.getCurrentUserName() + ":");
                    List<WorkflowTask> tasks = workflowService.getAssignedTasks(AuthenticationUtil.getCurrentUserName(), WorkflowTaskState.IN_PROGRESS);
                    for (WorkflowTask task : tasks)
                    {
                        out.println("id: " + task.id + " , name: " + task.name + " , properties: " + task.properties.size() + " , workflow: " + task.path.instance.id + " , path: " + task.path.id);
                    }
                }
                
                else if (command[2].equals("completed"))
                {
                    out.println(AuthenticationUtil.getCurrentUserName() + ":");
                    List<WorkflowTask> tasks = workflowService.getAssignedTasks(AuthenticationUtil.getCurrentUserName(), WorkflowTaskState.COMPLETED);
                    for (WorkflowTask task : tasks)
                    {
                        out.println("id: " + task.id + " , name " + task.name + " , properties: " + task.properties.size() + " , workflow: " + task.path.instance.id + " , path: " + task.path.id);
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
                out.println("id: " + task.id);
                out.println("name: " + task.name);
                out.println("title: " + task.title);
                out.println("description: " + task.description);
                out.println("state: " + task.state);
                out.println("path: " + task.path.id);
                out.println("transitions: " + task.path.node.transitions.length);
                for (WorkflowTransition transition : task.path.node.transitions)
                {
                    out.println(" transition: " + ((transition == null || transition.id.equals("")) ? "[default]" : transition.id) + " , title: " + transition.title + " , desc: " + transition.description);
                }
                out.println("properties: " + task.properties.size());
                for (Map.Entry<QName, Serializable> prop : task.properties.entrySet())
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
                out.println("definition: " + workflow.definition.name);
                out.println("id: " + workflow.id);
                out.println("description: " + workflow.description);
                out.println("active: " + workflow.active);
                out.println("start date: " + workflow.startDate);
                out.println("end date: " + workflow.endDate);
                out.println("initiator: " + workflow.initiator);
                out.println("context: " + workflow.context);
                out.println("package: " + workflow.workflowPackage);
            }
            else
            {
                return "Syntax Error.\n";
            }
        }
        
        else if (command[0].equals("deploy"))
        {
            if (command.length != 2)
            {
                return "Syntax Error.\n";
            }
            ClassPathResource workflowDef = new ClassPathResource(command[1]);
            WorkflowDeployment deployment = workflowService.deployDefinition("jbpm", workflowDef.getInputStream(), MimetypeMap.MIMETYPE_XML);
            WorkflowDefinition def = deployment.definition;
            for (String problem : deployment.problems)
            {
                out.println(problem);
            }
            out.println("deployed definition id: " + def.id + " , name: " + def.name + " , title: " + def.title + " , version: " + def.version);
            currentDeploy = command[1];
            out.print(interpretCommand("use " + def.id));
        }

        else if (command[0].equals("redeploy"))
        {
            if (currentDeploy == null)
            {
                return "nothing to redeploy\n";
            }
            out.print(interpretCommand("deploy " + currentDeploy));
        }
        
        else if (command[0].equals("use"))
        {
            if (command.length == 1)
            {
                out.println("definition: " + ((currentWorkflowDef == null) ? "None" : currentWorkflowDef.id + " , name: " + currentWorkflowDef.title));
                out.println("workflow: " + ((currentPath == null) ? "None" : currentPath.instance.id + " , active: " + currentPath.instance.active));
                out.println("path: " + ((currentPath == null) ? "None" : currentPath.id + " , node: " + currentPath.node.title));
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
                    out.print(interpretCommand("use"));
                }
                
                else if (command[1].equals("workflow"))
                {
                    if (command.length != 3)
                    {
                        return "Syntax Error.\n";
                    }
                    WorkflowInstance instance = workflowService.getWorkflowById(command[2]);
                    currentWorkflowDef = instance.definition;
                    currentPath = workflowService.getWorkflowPaths(instance.id).get(0);
                    out.print(interpretCommand("use"));
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
                AuthenticationUtil.setCurrentUser(command[1]);
            }
            out.println("using user " + AuthenticationUtil.getCurrentUserName());
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
            WorkflowPath path = workflowService.startWorkflow(currentWorkflowDef.id, params);
            out.println("started workflow id: " + path.instance.id + ", path: " + path.id + " , node: " + path.node.name + " , def: " + path.instance.definition.title);
            currentPath = path;
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
                out.println("updated task id: " + command[2] + ", properties: " + task.properties.size());
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
            out.println("signal sent - path id: " + path.id + " , node: " + path.node.name);
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
                out.println("signal sent - path id: " + task.path.id + " , node: " + task.path.node.name);
            }
            else if (command[1].equals("workflow"))
            {
                String workflowId = (command.length == 3) ? command[2] : (currentPath == null) ? null : currentPath.instance.id;
                if (workflowId == null)
                {
                    return "Syntax Error.  Workflow Id not specified.\n";
                }
                workflowService.cancelWorkflow(workflowId);
                out.println("cancelled workflow" + workflowId);
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
                		avmService.createAVMStore("workflow-system");
                		avmService.createDirectory("workflow-system:/", "packages");
                	}

                	// create package (layered to target, if target is specified)
                	String packageName = GUID.generate();
                	String avmSourceIndirection = avmSource.getIndirection();
                	if (avmSourceIndirection != null)
                	{
                    	avmService.createLayeredDirectory(avmSourceIndirection, packagesPath, packageName);
                		List<AVMDifference> diff = avmSyncService.compare(-1, avmSource.getPath(), -1, packagesPath + "/" + packageName);
                        avmSyncService.update(diff, true, true, false, false, null, null);
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
    
    /**
     * Get current user name
     * 
     * @return  user name
     */
    public String getCurrentUserName()
    {
        return AuthenticationUtil.getCurrentUserName();
    }
    
}