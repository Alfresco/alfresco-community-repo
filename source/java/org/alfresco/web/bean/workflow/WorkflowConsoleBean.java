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
package org.alfresco.web.bean.workflow;

import org.alfresco.repo.workflow.WorkflowInterpreter;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;


/**
 * Backing bean to support the Workflow Console
 */
public class WorkflowConsoleBean
{
    // command
    private String command = "";
    private String submittedCommand = "none";
    private long duration = 0L;
    private String result = null;
    
    // supporting repository services
    private WorkflowInterpreter workflowInterpreter;

    
    /**
     * @param nodeService  node service
     */
    public void setWorkflowInterpreter(WorkflowInterpreter workflowInterpreter)
    {
        this.workflowInterpreter = workflowInterpreter;
    }

    /**
     * Gets the command result
     * 
     * @return  result
     */
    public String getResult()
    {
        if (result == null)
        {
            interpretCommand("help");
        }
        return result;
    }
    
    /**
     * Sets the command result
     * 
     * @param result
     */
    public void setResult(String result)
    {
        this.result = result;
    }
    
    /**
     * Gets the current query
     * 
     * @return  query statement
     */
    public String getCommand()
    {
        return command;
    }
    
    /**
     * Set the current query
     * 
     * @param query   query statement
     */
    public void setCommand(String command)
    {
        this.command = command;
    }

    /**
     * Gets the submitted command
     * 
     * @return  submitted command
     */
    public String getSubmittedCommand()
    {
        return submittedCommand;
    }
    
    /**
     * Set the current query
     * 
     * @param query   query statement
     */
    public void setSubmittedCommand(String submittedCommand)
    {
        this.submittedCommand = submittedCommand;
    }

    /**
     * Gets the last command duration
     * 
     * @return  command duration
     */
    public long getDuration()
    {
        return duration;
    }
    
    /**
     * Set the current query
     * 
     * @param query   query statement
     */
    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    /**
     * Action to submit command
     * 
     * @return  next action
     */
    public String submitCommand()
    {
        interpretCommand(command);
        return "success";
    }

    /**
     * Gets the current user name
     * 
     * @return  user name
     */
    public String getCurrentUserName()
    {
        return workflowInterpreter.getCurrentUserName();
    }
    
    /**
     * Gets the current workflow definition
     * 
     * @return  workflow definition
     */
    public String getCurrentWorkflowDef()
    {
        WorkflowDefinition def = workflowInterpreter.getCurrentWorkflowDef();
        return (def == null) ? "None" : def.title + " v" + def.version;
    }
    
    /**
     * Interpret workflow console command
     * 
     * @param command  command
     */
    private void interpretCommand(String command)
    {
        try
        {
            long startms = System.currentTimeMillis();
            String result = workflowInterpreter.interpretCommand(command);
            setDuration(System.currentTimeMillis() - startms);
            setResult(result);
            setCommand("");
            setSubmittedCommand(command);
        }
        catch (Exception e)
        {
            setResult(e.toString());
        }
    }
    
}
