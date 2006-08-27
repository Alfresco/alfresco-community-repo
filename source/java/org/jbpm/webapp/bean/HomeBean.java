/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.webapp.bean;

import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.db.GraphSession;
import org.jbpm.db.TaskMgmtSession;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class HomeBean {

  UserBean userBean;
  TaskBean taskBean;
  JbpmConfiguration config;
  DataModel taskInstances;
  DataModel processDefs;
  

  public HomeBean()
  {
  }
  
  public void setJbpmConfiguration(JbpmConfiguration config)
  {
      this.config = config;
  }
  
  public List getTaskInstances()
  {
      JbpmContext xjbpmContext = config.getCurrentJbpmContext();
      JbpmContext jbpmContext = (xjbpmContext == null) ? config.createJbpmContext() : xjbpmContext;

      try
      {
          TaskMgmtSession taskMgmtSession = jbpmContext.getTaskMgmtSession();
          List<TaskInstance> taskInstances = taskMgmtSession.findTaskInstances(userBean.getUserName());
          for (TaskInstance taskInstance : taskInstances)
          {
              taskInstance.getName();
              taskInstance.getTaskMgmtInstance().getTaskMgmtDefinition().getProcessDefinition().getName();
          }
          return taskInstances;
      }
      finally
      {
          if (xjbpmContext == null) jbpmContext.close();
      }
  }

  public DataModel getTaskInstancesModel()
  {
      if (taskInstances == null)
      {
          taskInstances = new ListDataModel(getTaskInstances());
      }
      return taskInstances;
  }
  
  public List getLatestProcessDefinitions()
  {
      JbpmContext xjbpmContext = config.getCurrentJbpmContext();
      JbpmContext jbpmContext = (xjbpmContext == null) ? config.createJbpmContext() : xjbpmContext;
      try
      {
          GraphSession graphSession = jbpmContext.getGraphSession();
          List<ProcessDefinition> procDefs = graphSession.findLatestProcessDefinitions();
          for (ProcessDefinition procDef : procDefs)
          {
              procDef.getName();
              Task startTask = procDef.getTaskMgmtDefinition().getStartTask();
              if (startTask != null)
              {
                  startTask.getName();
              }
          }
          return procDefs;
      }
      finally
      {
          if (xjbpmContext == null) jbpmContext.close();
      }
  }

  public DataModel getLatestProcessDefinitionsModel()
  {
      if (processDefs == null)
      {
          processDefs = new ListDataModel(getLatestProcessDefinitions());
      }
      return processDefs;
  }

  /**
   * selects a task.
   */
  public String selectTaskInstance()
  {
      JbpmContext xjbpmContext = config.getCurrentJbpmContext();
      JbpmContext jbpmContext = (xjbpmContext == null) ? config.createJbpmContext() : xjbpmContext;
      
      try
      {
          // Get the task instance id from request parameter
          TaskInstance selectedTask = (TaskInstance)taskInstances.getRowData();
          long taskInstanceId = selectedTask.getId();
          TaskMgmtSession taskMgmtSession = jbpmContext.getTaskMgmtSession();
          TaskInstance taskInstance = taskMgmtSession.loadTaskInstance(taskInstanceId);
          taskBean.initialize(taskInstance);
          
          return "task";
      }
      finally
      {
          taskInstances = null;
          processDefs = null;
          if (xjbpmContext == null ) jbpmContext.close();
      }
  }

  /**
   * prepares a task form for starting a new process instance.
   */
  public String startProcessInstance()
  {
      JbpmContext xjbpmContext = config.getCurrentJbpmContext();
      JbpmContext jbpmContext = (xjbpmContext == null) ? config.createJbpmContext() : xjbpmContext;
      try
      {
          jbpmContext.setActorId(AuthenticationUtil.getCurrentUserName());
          
          // Get the task instance id from request parameter
          ProcessDefinition selectedProc = (ProcessDefinition)processDefs.getRowData();
          long processDefinitionId = selectedProc.getId();
          GraphSession graphSession = jbpmContext.getGraphSession();
          ProcessDefinition processDefinition = graphSession.loadProcessDefinition(processDefinitionId);

          // create a new process instance to run
          ProcessInstance processInstance = new ProcessInstance(processDefinition);

          // create a new taskinstance for the start task
          Task startTask = processInstance.getTaskMgmtInstance().getTaskMgmtDefinition().getStartTask();
          if (startTask != null)
          {
              TaskInstance taskInstance = processInstance.getTaskMgmtInstance().createStartTaskInstance();
              taskBean.initialize(taskInstance);
          }
          
          // Save the process instance along with the task instance
          jbpmContext.save(processInstance);
          
          // Fill the task backing bean with useful information
          return (startTask == null) ? "home" : "task";
      }
      finally
      {
          if (xjbpmContext == null) jbpmContext.close();
          taskInstances = null;
          processDefs = null;
      }
  }
  
  public UserBean getUserBean() {
    return userBean;
  }
  public void setUserBean(UserBean userBean) {
    this.userBean = userBean;
  }
  public TaskBean getTaskBean() {
    return taskBean;
  }
  public void setTaskBean(TaskBean taskBean) {
    this.taskBean = taskBean;
  }
}
