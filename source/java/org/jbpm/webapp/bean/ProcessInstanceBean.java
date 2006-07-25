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
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.jbpm.JbpmContext;
import org.jbpm.db.GraphSession;
import org.jbpm.db.TaskMgmtSession;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Process Instance Bean Implementation.
 * 
 * @author David Loiseau
 */

public class ProcessInstanceBean {

  long id;
  String processDefinitionLabel;
  long processDefinitionId;
  Date start;
  Date end;

  ArrayList tokens;
  ArrayList variables;
  ArrayList tasks;
  ArrayList transitions;

  String variableName;
  String variableValue;

  long tokenInstanceId;
  long taskInstanceId;

  public ProcessInstanceBean(long id, Date start, Date end) {
    this.id = id;
    this.start = start;
    this.end = end;
  }

  public ProcessInstanceBean(long id) {
    this.id = id;
    this.initialize();
  }

  public ProcessInstanceBean(long id, String variableName, String variableValue) {
    this.id = id;
    this.variableName = variableName;
    this.variableValue = variableValue;
    this.initialize();
  }

  public String inspectProcessInstance() {
    ProcessInstanceBean processInstanceBean = new ProcessInstanceBean(this.id);
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("processInstanceBean", processInstanceBean);
    return ("inspectInstance");
  }

  public String deleteProcessInstance() {
    JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
    GraphSession graphSession = jbpmContext.getGraphSession();
    graphSession.deleteProcessInstance(this.id);
    return ("deleteInstance");
  }

  private void initialize() {
    JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
    GraphSession graphSession = jbpmContext.getGraphSession();
    ProcessInstance processInstance = graphSession.loadProcessInstance(this.id);
    this.start = processInstance.getStart();
    this.end = processInstance.getEnd();
    this.processDefinitionId = processInstance.getProcessDefinition().getId();
    this.processDefinitionLabel = processInstance.getProcessDefinition().getName() + " (version " + processInstance.getProcessDefinition().getVersion() + ")";

    initializeVariablesList(processInstance);
    initializeTokensList(processInstance);
    initializeTasksList(processInstance);
  }

  private void initializeAvailableTransitions(TaskInstance taskInstance) {

    transitions = new ArrayList();

    if (taskInstance.getAvailableTransitions().isEmpty() == false) {
      Iterator availableTransitionsIterator = taskInstance.getAvailableTransitions().iterator();
      while (availableTransitionsIterator.hasNext()) {
        Transition transition = (Transition) availableTransitionsIterator.next();
        transitions.add(transition);

      }
    }
  }

  private void initializeAvailableTransitions(Token token) {

    transitions = new ArrayList();

    if (token.getNode().getLeavingTransitions().isEmpty() == false) {
      Iterator availableTransitionsIterator = token.getNode().getLeavingTransitions().iterator();
      while (availableTransitionsIterator.hasNext()) {
        Transition transition = (Transition) availableTransitionsIterator.next();
        transitions.add(transition);

      }
    }
  }

  private void initializeVariablesList(ProcessInstance processInstance) {

    // Variables list
    variables = new ArrayList();

    if (processInstance.getContextInstance().getVariables() != null && !processInstance.getContextInstance().getVariables().values().isEmpty()) {
      int mapsize = processInstance.getContextInstance().getVariables().size();
      Iterator variablesIterator = processInstance.getContextInstance().getVariables().entrySet().iterator();
      for (int i = 0; i < mapsize; i++) {
        Entry entry = (Entry) variablesIterator.next();
        variables.add(new VariableBean((String) entry.getKey(), entry.getValue()));
      }
    }

  }

  private void initializeTasksList(ProcessInstance processInstance) {

    // Tasks list
    tasks = new ArrayList();
    if (processInstance.getTaskMgmtInstance().getTaskInstances().isEmpty() == false) {
      Iterator tasksIterator = processInstance.getTaskMgmtInstance().getTaskInstances().iterator();
      while (tasksIterator.hasNext()) {
        TaskInstance taskInstance = (TaskInstance) tasksIterator.next();
        tasks.add(new TaskBean(taskInstance.getId(), taskInstance.getName(), taskInstance.getActorId(), taskInstance.getEnd()));
      }
    }

  }

  private void initializeTokensList(ProcessInstance processInstance) {

    // Tokens list
    Token rootToken = processInstance.getRootToken();

    tokens = new ArrayList();
    this.tokenInstanceId = rootToken.getId();
    this.taskInstanceId = 0;
    tokens.add(new TokenBean(rootToken.getId(), "Root", rootToken.getNode().getName(), rootToken.getNode().getClass().getName(), rootToken.getStart(),
            rootToken.getEnd(), 1));
    try {
      if (rootToken.getChildren().isEmpty() == false) {
        AddChildrenTokensToTokensList(this.tokens, rootToken, 2);
      }
    } catch (Exception exception) {
    }

  }

  /**
   * 
   * Add token childs to the current token beans list
   * 
   * @param tokensList
   *          Current token list to update
   * @param token
   *          Token where are the token childs
   * @param level
   *          Level where is the token: 1 for the root token, 2 for the childs
   *          of the root token, ...
   */
  private void AddChildrenTokensToTokensList(ArrayList tokensList, Token token, long level) {

    Iterator childrenIterator = token.getChildren().values().iterator();
    while (childrenIterator.hasNext()) {
      Token childToken = (Token) childrenIterator.next();
      tokensList.add(new TokenBean(childToken.getId(), childToken.getName(), childToken.getNode().getName(), childToken.getNode().getClass().getName(),
              childToken.getStart(), childToken.getEnd(), level));
      try {
        if (childToken.getChildren().isEmpty() == false) {
          AddChildrenTokensToTokensList(tokensList, childToken, level + 1);
        }
      } catch (Exception exception) {
      }
    }
  }

  public String updateVariable() {

    if (this.variableName != null) {
      JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
      GraphSession graphSession = jbpmContext.getGraphSession();
      ProcessInstance processInstance = graphSession.loadProcessInstance(this.id);
      if (this.variableValue != null) {
        processInstance.getContextInstance().setVariable(this.variableName, this.variableValue);
      } else {
        processInstance.getContextInstance().deleteVariable(this.variableName);
      }
      initializeVariablesList(processInstance);
    }
    return "inspectInstance";
  }

  public String selectToken() {
    this.taskInstanceId = 0;
    this.tokenInstanceId = JsfHelper.getId("tokenInstanceId");
    return "";
  }

  public String selectTask() {
    this.tokenInstanceId = 0;
    this.taskInstanceId = JsfHelper.getId("taskInstanceId");
    return "";
  }

  public String signal() {

    selectToken();

    JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
    GraphSession graphSession = jbpmContext.getGraphSession();

    Token token = graphSession.loadToken(this.tokenInstanceId);

    if (token.getNode().getLeavingTransitions().size() > 1) {
      initializeAvailableTransitions(token);
      return "showTransitions";
    }

    token.signal();

    this.initializeTokensList(token.getProcessInstance());

    return "inspectInstance";
  }

  public String selectTransition() {
    String transitionName;

    transitionName = JsfHelper.getParameter("transitionName");
    ProcessInstance processInstance = null;

    JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
    if (this.taskInstanceId > 0) {
      TaskMgmtSession taskMgmtSession = jbpmContext.getTaskMgmtSession();
      TaskInstance taskInstance = taskMgmtSession.loadTaskInstance(this.taskInstanceId);
      if (transitionName.equals("")) {
        taskInstance.end();
      } else {
        taskInstance.end(transitionName);
      }
      processInstance = taskInstance.getToken().getProcessInstance();
    } else if (this.tokenInstanceId > 0) {
      GraphSession graphSession = jbpmContext.getGraphSession();
      Token token = graphSession.loadToken(this.tokenInstanceId);
      if (transitionName.equals("")) {
        token.signal();
      } else {
        token.signal(transitionName);
      }
      processInstance = token.getProcessInstance();
    }

    jbpmContext.save(processInstance);

    this.initializeTasksList(processInstance);
    this.initializeTokensList(processInstance);

    return "inspectInstance";
  }

  public String endTask() {

    selectTask();

    JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
    TaskMgmtSession taskMgmtSession = jbpmContext.getTaskMgmtSession();

    TaskInstance taskInstance = taskMgmtSession.loadTaskInstance(this.taskInstanceId);

    if (taskInstance.getAvailableTransitions().size() > 1) {
      initializeAvailableTransitions(taskInstance);
      return "showTransitions";
    }

    taskInstance.end();

    ProcessInstance processInstance = taskInstance.getToken().getProcessInstance();
    jbpmContext.save(processInstance);

    this.initializeTasksList(processInstance);
    this.initializeTokensList(processInstance);

    return "inspectInstance";
  }

  // Show all the process instances for a given process definition ID
  public String showProcessInstances() {
    ProcessDefinitionBean processDefinitionBean = new ProcessDefinitionBean(this.processDefinitionId);
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("processDefinitionBean", processDefinitionBean);
    return ("processInstances");
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public ArrayList getTokens() {
    return tokens;
  }

  public void setTokens(ArrayList tokens) {
    this.tokens = tokens;
  }

  public String getProcessDefinitionLabel() {
    return processDefinitionLabel;
  }

  public void setProcessDefinitionLabel(String processDefinitionLabel) {
    this.processDefinitionLabel = processDefinitionLabel;
  }

  public ArrayList getVariables() {
    return variables;
  }

  public ArrayList getTasks() {
    return tasks;
  }

  public ArrayList getTransitions() {
    return transitions;
  }

  public void setVariables(ArrayList variables) {
    this.variables = variables;
  }

  public String getVariableName() {
    return variableName;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public String getVariableValue() {
    return variableValue;
  }

  public void setVariableValue(String variableValue) {
    this.variableValue = variableValue;
  }

  public long getTokenInstanceId() {
    return tokenInstanceId;
  }

  public void setTokenInstanceId(long tokenInstanceId) {
    this.taskInstanceId = 0;
    this.tokenInstanceId = tokenInstanceId;
  }

  public long getTaskInstanceId() {
    return taskInstanceId;
  }

  public void setTaskInstanceId(long taskInstanceId) {
    this.tokenInstanceId = 0;
    this.taskInstanceId = taskInstanceId;
  }

}
