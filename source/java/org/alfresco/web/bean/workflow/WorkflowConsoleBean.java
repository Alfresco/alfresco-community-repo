/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.workflow;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.alfresco.repo.workflow.WorkflowInterpreter;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.web.app.servlet.FacesHelper;


/**
 * Backing bean to support the Workflow Console
 */
public class WorkflowConsoleBean implements Serializable
{
    private static final long serialVersionUID = -7531838393180855185L;
    
    // command
    private String command = "";
    private String submittedCommand = "none";
    private long duration = 0L;
    private String result = null;
    
    // supporting repository services
    transient private WorkflowInterpreter workflowInterpreter;

    
    /**
     * @param workflowInterpreter  workflow interpreter
     */
    public void setWorkflowInterpreter(WorkflowInterpreter workflowInterpreter)
    {
        this.workflowInterpreter = workflowInterpreter;
    }
    
    private WorkflowInterpreter getWorkflowInterpreter()
   {
       if (this.workflowInterpreter == null)
       {
          this.workflowInterpreter = (WorkflowInterpreter) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "workflowInterpreter");
       }
       return this.workflowInterpreter;
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
     * Set the current command
     * 
     * @param command   command
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
     * Set the submitted command
     * 
     * @param submittedCommand The submitted command
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
     * Set the duration
     * 
     * @param duration   The duration
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
        return getWorkflowInterpreter().getCurrentUserName();
    }
    
    /**
     * Gets the current workflow definition
     * 
     * @return  workflow definition
     */
    public String getCurrentWorkflowDef()
    {
        WorkflowDefinition def = getWorkflowInterpreter().getCurrentWorkflowDef();
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
            String result = getWorkflowInterpreter().interpretCommand(command);
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
