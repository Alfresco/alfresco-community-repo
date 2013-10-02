package org.alfresco.rest.workflow.api.tests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.PublicApiHttpClient;
import org.alfresco.rest.api.tests.client.UserDataService;
import org.alfresco.rest.workflow.api.model.Deployment;
import org.alfresco.rest.workflow.api.model.ProcessDefinition;
import org.alfresco.rest.workflow.api.model.ProcessInfo;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WorkflowApiClient extends PublicApiClient
{
    public static final DateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    private DeploymentsClient deploymentsClient;
    private ProcessDefinitionsClient processDefinitionsClient;
    private ProcessesClient processesClient;
    private TasksClient tasksClient;
    
    public WorkflowApiClient(PublicApiHttpClient client, UserDataService userDataService) {
        super(client, userDataService);
        initWorkflowClients();
    }
    
    public void initWorkflowClients()
    {
        deploymentsClient = new DeploymentsClient();
        processDefinitionsClient = new ProcessDefinitionsClient();
        processesClient = new ProcessesClient();
        tasksClient = new TasksClient();
    }

    public DeploymentsClient deploymentsClient()
    {
        return deploymentsClient;
    }
    

    public ProcessDefinitionsClient processDefinitionsClient()
    {
        return processDefinitionsClient;
    }
    
    public ProcessesClient processesClient()
    {
        return processesClient;
    }
    
    public TasksClient tasksClient()
    {
        return tasksClient;
    }
    
    public class DeploymentsClient extends AbstractProxy
    {
        public ListResponse<Deployment> getDeployments(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("deployments", null, null, null, params, "Failed to get deploymentsClient");
            return DeploymentParser.INSTANCE.parseList(response.getJsonResponse());
        }
        
        public JSONObject getDeploymentsWithRawResponse(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("deployments", null, null, null, params, "Failed to get deploymentsClient");
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }

        public ListResponse<Deployment> getDeployments() throws PublicApiException
        {
            return getDeployments(null);
        }

        /*public ListResponse<Deployment> createNewDeployment(String name, byte[] bytes) throws PublicApiException
        {
            Part[] parts = new Part[]{
                    new StringPart("name", name),
                    new FilePart("file", new ByteArrayPartSource("bytes", bytes))
            };
            RequestEntity requestEntity = new MultipartRequestEntity(parts, null);
            HttpResponse response = create("deployments", null, null, null, requestEntity, "Failed to create new deployment");
            return DeploymentParser.INSTANCE.parseList(response.getJsonResponse());
        }*/

        public Deployment findDeploymentById(String deploymentId) throws PublicApiException
        {
            HttpResponse response = getSingle("deployments", deploymentId, null, null, "Failed to get deployment");
            JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
            return DeploymentParser.INSTANCE.parseEntry(entry);
        }

        public void deleteDeployment(String deploymentId) throws PublicApiException
        {
            remove("deployments", deploymentId, null, null, "Failed to delete deployment");
        }
    }
    
    public class ProcessDefinitionsClient extends AbstractProxy
    {
        public ListResponse<ProcessDefinition> getProcessDefinitions(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("process-definitions", null, null, null, params, "Failed to get process definitions");
            return ProcessDefinitionParser.INSTANCE.parseList(response.getJsonResponse());
        }
        
        public JSONObject getProcessDefinitionsWithRawResponse(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("process-definitions", null, null, null, params, "Failed to get process definitions");
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }

        public ProcessDefinition findProcessDefinitionById(String processDefinitionId) throws PublicApiException
        {
            HttpResponse response = getSingle("process-definitions", processDefinitionId, null, null, "Failed to get process definition");
            JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
            return ProcessDefinitionParser.INSTANCE.parseEntry(entry);
        }
        
        public JSONObject findStartFormModel(String processDefinitionId) throws PublicApiException
        {
            HttpResponse response = getAll("process-definitions", processDefinitionId, "start-form-model", null, null,
                    "Failed to get the start form model of the process definition");
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }
    }

    public class ProcessesClient extends AbstractProxy
    {
        public ProcessInfo createProcess(String body) throws PublicApiException
        {
            HttpResponse response = create("processes", null, null, null, body, "Failed to start new process instance");
            return ProcessesParser.INSTANCE.parseEntry((JSONObject) response.getJsonResponse().get("entry"));
        }

        public ListResponse<ProcessInfo> getProcesses(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("processes", null, null, null, params, "Failed to get process instances");
            return ProcessesParser.INSTANCE.parseList(response.getJsonResponse());
        }
        
        public JSONObject getProcessesJSON(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("processes", null, null, null, params, "Failed to get process instances");
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }

        public ProcessInfo findProcessById(String processInstanceId) throws PublicApiException
        {
            HttpResponse response = getSingle("processes", processInstanceId, null, null, "Failed to find process instance by id");
            JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
            return ProcessesParser.INSTANCE.parseEntry(entry);
        }
        
        public JSONObject getTasks(String processInstanceId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("processes", processInstanceId, "tasks", null, params, "Failed to get task instances of processInstanceId " + processInstanceId);
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }
        
        public JSONObject getActivities(String processInstanceId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("processes", processInstanceId, "activities", null, params, "Failed to get activity instances of processInstanceId " + processInstanceId);
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }
        
        public HttpResponse getImage(String processInstanceId) throws PublicApiException
        {
            HttpResponse response = getSingle("processes", processInstanceId, "image", null, "Failed to get image of processInstanceId " + processInstanceId);
            return response;
        }
        
        public JSONObject findProcessItems(String processInstanceId) throws PublicApiException
        {
            HttpResponse response = getAll("processes", processInstanceId, "items", null, null,
                    "Failed to get the items of the process instance");
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }
        
        public JSONObject getProcessvariables(String processInstanceId) throws PublicApiException
        {
            HttpResponse response = getAll("processes", processInstanceId, "variables", null, null,
                    "Failed to get the variables of the process instance");
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }
        
        public JSONObject createVariables(String processId, JSONArray variables) throws PublicApiException
        {
            HttpResponse response = create("processes", processId, "variables", null, variables.toJSONString(), "Failed to create variables");
            return response.getJsonResponse();
        }
        
        public JSONObject updateVariable(String processId, String variableName, JSONObject variable) throws PublicApiException
        {
            HttpResponse response = update("processes", processId, "variables", variableName, variable.toJSONString(), "Failed to update variable");
            return response.getJsonResponse();
        }
        
        public void deleteVariable(String processId, String variableName) throws PublicApiException
        {
            remove("processes", processId, "variables", variableName, "Failed to delete variable");
        }
        
        public void addProcessItem(String processId, String body) throws PublicApiException
        {
            create("processes", processId, "items", null, body, "Failed to add item");
        }
        
        public void deleteProcessItem(String processId, String itemId) throws PublicApiException
        {
            remove("processes", processId, "items", itemId, "Failed to delete item");
        }
        
        public JSONObject findProcessItem(String processInstanceId, String itemId) throws PublicApiException
        {
            HttpResponse response = getAll("processes", processInstanceId, "items", itemId, null,
                    "Failed to get the item of the process instance");
            JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
            return entry;
        }

        public void deleteProcessById(String processId) throws PublicApiException
        {
            remove("processes", processId, null, null, "Failed to delete process");
        }
    }
    
    public class TasksClient extends AbstractProxy
    {
        public JSONObject findTaskById(String taskId) throws PublicApiException
        {
            HttpResponse response = getSingle("tasks", taskId, null, null, "Failed to get task");
            JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
            return entry;
        }
        
        public JSONObject updateTask(String taskId, JSONObject task, List<String> selectedFields) throws PublicApiException
        {
            String selectedFieldsValue = StringUtils.join(selectedFields, ",");
            Map<String, String> params = new HashMap<String, String>();
            params.put("select", selectedFieldsValue);
            
            HttpResponse response = update("tasks", taskId, null, null, task.toJSONString(), "Failed to update task", params);
            
            JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
            return entry;
        }
        
        public JSONObject findTasks(Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("tasks", null, null, null, params, "Failed to get all tasks");
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }
        
        public JSONObject findTaskCandidates(String taskId) throws PublicApiException
        {
            HttpResponse response = getAll("tasks", taskId, "candidates", null, null, "Failed to get task candidates");
            return response.getJsonResponse();
        }
        
        public JSONObject findTaskVariables(String taskId, Map<String, String> params) throws PublicApiException
        {
            HttpResponse response = getAll("tasks", taskId, "variables", null, params, "Failed to get task variables");
            return response.getJsonResponse();
        }
        
        public JSONObject findTaskVariables(String taskId) throws PublicApiException
        {
            return findTaskVariables(taskId, null);
        }
        
        public JSONObject createTaskVariables(String taskId, JSONArray variables) throws PublicApiException
        {
            HttpResponse response = create("tasks", taskId, "variables", null, variables.toJSONString(), "Failed to create task variables");
            return response.getJsonResponse();
        }
        
        public JSONObject updateTaskVariable(String taskId, String variableName, JSONObject variable) throws PublicApiException
        {
            HttpResponse response = update("tasks", taskId, "variables", variableName, variable.toJSONString(), "Failed to update task variable");
            return response.getJsonResponse();
        }
        
        public void deleteTaskVariable(String taskId, String variableName) throws PublicApiException
        {
            remove("tasks", taskId, "variables", variableName, "Failed to delete task variable");
        }

        public JSONObject findTaskFormModel(String taskId) throws PublicApiException
        {
            HttpResponse response = getAll("tasks", taskId, "task-form-model", null, null, "Failed to get task form model");
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }
        
        public JSONObject findTaskItems(String taskId) throws PublicApiException
        {
            HttpResponse response = getAll("tasks", taskId, "items", null, null,
                    "Failed to get the items of the task");
            JSONObject list = (JSONObject) response.getJsonResponse().get("list");
            return list;
        }
        
        public JSONObject addTaskItem(String taskId, String body) throws PublicApiException
        {
            HttpResponse response = create("tasks", taskId, "items", null, body, "Failed to add item");
            JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
            return entry;
        }
        
        public void deleteTaskItem(String taskId, String itemId) throws PublicApiException
        {
            remove("tasks", taskId, "items", itemId, "Failed to delete item");
        }
        
        public JSONObject findTaskItem(String taskId, String itemId) throws PublicApiException
        {
            HttpResponse response = getAll("tasks", taskId, "items", itemId, null,
                    "Failed to get the item of the task");
            JSONObject entry = (JSONObject) response.getJsonResponse().get("entry");
            return entry;
        }
    }
    
    public static Date parseDate(JSONObject entry, String fieldName) {
        String dateText = (String) entry.get(fieldName);
        if (dateText!=null) {
          try
          {
              return DATE_FORMAT_ISO8601.parse(dateText);
          }
          catch (Exception e)
          {
              throw new RuntimeException("couldn't parse date "+dateText+": "+e.getMessage(), e);
          }
        }
        return null;
      }
}
