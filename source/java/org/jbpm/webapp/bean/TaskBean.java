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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.logging.exe.LoggingInstance;
import org.jbpm.taskmgmt.def.TaskController;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jbpm.taskmgmt.log.TaskAssignLog;

public class TaskBean {

  UserBean userBean = null;
  List taskFormParameters;
  List availableTransitions;
  List availableTransitionItems;
  TaskInstance taskInstance;
  long taskInstanceId;
  DataModel transitions;

//  JbpmContext jbpmContext;
//  GraphSession graphSession;
//  TaskMgmtSession taskMgmtSession;

  // For monitoring purposes
  String name;
  String actorId;
  Date end;

  public TaskBean() {
//    this.jbpmContext = JbpmContext.getCurrentJbpmContext();
//    this.graphSession = jbpmContext.getGraphSession();
//    this.taskMgmtSession = jbpmContext.getTaskMgmtSession();

    // get the parameters from the session
//    this.taskFormParameters = (List) JsfHelper.getSessionAttribute("taskFormParameters");
  }

  public TaskBean(long taskInstanceId, String name, String actorId, Date end) {
      this.taskInstanceId = taskInstanceId;
      this.name = name;
      this.actorId = actorId;
      this.end = end;
    }
  
  public void initialize(TaskInstance taskInstance) {
    this.taskInstance = taskInstance;
    this.taskInstanceId = taskInstance.getId();

    // set the parameters
    this.taskFormParameters = new ArrayList();
    TaskController taskController = taskInstance.getTask().getTaskController();
    if (taskController!=null) {
      List variableAccesses = taskController.getVariableAccesses();
      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        String mappedName = variableAccess.getMappedName();
        Object value = taskInstance.getVariable(mappedName);
        TaskFormParameter tfp = new TaskFormParameter(variableAccess, value);
        taskFormParameters.add(tfp);
      }
    }

    // store the parameters in the session
    //JsfHelper.setSessionAttribute("taskFormParameters", taskFormParameters);

    // get the available transitions
    availableTransitions = null;

    availableTransitions = taskInstance.getAvailableTransitions();
    if ((availableTransitions != null) && (availableTransitions.size() <= 1)) {
      transitions = null;
      availableTransitions = null;
      availableTransitionItems = null;
    } else {
      transitions = new ListDataModel(availableTransitions);
      availableTransitionItems = new ArrayList();
      Iterator iter = availableTransitions.iterator();
      while (iter.hasNext()) {
        Transition transition = (Transition) iter.next();
        SelectItem transitionItem = new SelectItem();
        transitionItem.setValue(transition.getName());
        transitionItem.setLabel(transition.getName());
        transitionItem.setDisabled(false);
        availableTransitionItems.add(transitionItem);
      }
    }

    log.debug("initialized availableTransitions " + availableTransitions);
  }

  public String save() {
    log.debug("saving the task parameters " + taskFormParameters);

    // submit the parameters in the jbpm task controller
    TaskInstance taskInstance = JbpmContext.getCurrentJbpmContext().getTaskMgmtSession().loadTaskInstance(taskInstanceId);

    // collect the parameter values from the values that were updated in the
    // parameters by jsf.
    Iterator iter = taskFormParameters.iterator();
    while (iter.hasNext()) {
      TaskFormParameter taskFormParameter = (TaskFormParameter) iter.next();

      if ((taskFormParameter.isWritable()) && (taskFormParameter.getValue() != null)) {
        log.debug("submitting [" + taskFormParameter.getLabel() + "]=" + taskFormParameter.getValue());
        taskInstance.setVariable(taskFormParameter.getLabel(), taskFormParameter.getValue());
      } else {
        log.debug("ignoring unwritable [" + taskFormParameter.getLabel() + "]");
      }
    }

    // save the process instance and hence the updated task instance variables
    JbpmContext.getCurrentJbpmContext().save(taskInstance);

    // remove the parameters from the session
    //JsfHelper.removeSessionAttribute("taskFormParameters");

    return "home";
  }

  public String saveAndClose() {
    // save
    save();

    TaskInstance taskInstance = JbpmContext.getCurrentJbpmContext().getTaskMgmtSession().loadTaskInstance(taskInstanceId);
    
    // close the task instance
    if (transitions == null)
    {
        taskInstance.end();
    }
    else
    {
        Transition selectedTransition = (Transition)transitions.getRowData();
        taskInstance.end(selectedTransition.getName());
    }
    
    ProcessInstance processInstance = taskInstance.getTaskMgmtInstance().getProcessInstance();
    if (processInstance.hasEnded()) {
      JsfHelper.addMessage("The process has finished.");
    }

    LoggingInstance loggingInstance = processInstance.getLoggingInstance();
    List assignmentLogs = loggingInstance.getLogs(TaskAssignLog.class);

    log.debug("assignmentlogs: " + assignmentLogs);

    if (assignmentLogs.size() == 1) {
      TaskAssignLog taskAssignLog = (TaskAssignLog) assignmentLogs.get(0);
      JsfHelper.addMessage("A new task has been assigned to '" + taskAssignLog.getTaskNewActorId() + "'");

    } else if (assignmentLogs.size() > 1) {
      String msg = "New tasks have been assigned to: ";
      Iterator iter = assignmentLogs.iterator();
      while (iter.hasNext()) {
        TaskAssignLog taskAssignLog = (TaskAssignLog) iter.next();
        msg += taskAssignLog.getActorId();
        if (iter.hasNext())
          msg += ", ";
      }
      msg += ".";
      JsfHelper.addMessage(msg);
    }

    JbpmContext.getCurrentJbpmContext().save(taskInstance);

    return "home";
  }

  public long getTaskInstanceId() {
    return taskInstanceId;
  }
  public void setTaskInstanceId(long taskInstanceId) {
    this.taskInstanceId = taskInstanceId;
  }
  public UserBean getUserBean() {
    return userBean;
  }
  public void setUserBean(UserBean userBean) {
    this.userBean = userBean;
  }
  public List getTaskFormParameters() {
    return taskFormParameters;
  }
  
  public DataModel getTransitions()
  {
      return transitions;
  }
  
  public List getAvailableTransitions() {
    return availableTransitions;
  }
  public void setAvailableTransitions(List availableTransitions) {
    this.availableTransitions = availableTransitions;
  }
  public List getAvailableTransitionItems() {
    return availableTransitionItems;
  }
  public TaskInstance getTaskInstance() {
    return taskInstance;
  }

  private static final Log log = LogFactory.getLog(TaskBean.class);

  public String getActorId() {
    return actorId;
  }

  public void setActorId(String actorId) {
    this.actorId = actorId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public boolean isEnded() {
    if (end == null)
      return true;
    return false;
  }
}
