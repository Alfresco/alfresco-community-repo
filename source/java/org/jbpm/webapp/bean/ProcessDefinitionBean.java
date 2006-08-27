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
import java.util.List;
import java.util.ListIterator;

import javax.faces.context.FacesContext;

import org.jbpm.JbpmContext;
import org.jbpm.db.GraphSession;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

/**
 * Process Definition Bean Implementation.
 * 
 * @author David Loiseau
 */

public class ProcessDefinitionBean {

  String name;
  int version;
  long id;
  int instancesCount;

  public ProcessDefinitionBean() {
  }

  public ProcessDefinitionBean(long id) {
    this.id = id;
    initialize();
  }

  public ProcessDefinitionBean(long id, String name, int version, int instancesCount) {
    this.id = id;
    this.name = name;
    this.version = version;
    this.instancesCount = instancesCount;
  }

  private void initialize() {
    JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
    GraphSession graphSession = jbpmContext.getGraphSession();
    ProcessDefinition processDefinition = graphSession.loadProcessDefinition(id);
    this.name = processDefinition.getName();
    this.version = processDefinition.getVersion();
    this.instancesCount = graphSession.findProcessInstances(this.id).size();
  }

  public List getProcessInstances() {

    JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
    GraphSession graphSession = jbpmContext.getGraphSession();

    ArrayList processInstancesList = new ArrayList();

    List listProcessInstance = graphSession.findProcessInstances(this.id);

    if (listProcessInstance.isEmpty() == false) {
      ListIterator listProcessInstances = listProcessInstance.listIterator();
      while (listProcessInstances.hasNext()) {
        ProcessInstance processInstance = (ProcessInstance) listProcessInstances.next();

        processInstancesList.add(new ProcessInstanceBean(processInstance.getId(), processInstance.getStart(), processInstance.getEnd()));
      }
    }

    return processInstancesList;
  }

  public String showProcessInstances() {
    ProcessDefinitionBean processDefinitionBean = new ProcessDefinitionBean();
    processDefinitionBean.setId(this.id);
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("processDefinitionBean", processDefinitionBean);
    return ("processInstances");
  }
  
  public String startProcessInstance() {
	JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
	GraphSession graphSession = jbpmContext.getGraphSession();
	ProcessDefinition processDefinition = graphSession.loadProcessDefinition(getId());
	processDefinition.createInstance();
	return showProcessInstances();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
    this.initialize();
  }

  public int getInstancesCount() {
    return instancesCount;
  }

  public void setInstancesCount(int instancesCount) {
    this.instancesCount = instancesCount;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

}
