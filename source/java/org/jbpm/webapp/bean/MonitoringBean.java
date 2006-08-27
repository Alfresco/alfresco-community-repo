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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;


/**
 * Monitoring Bean Implementation.
 *
 * @author David Loiseau
 */

public class MonitoringBean {

	long processInstanceId;
	String message;
	String variableName;
	String variableValue;
	String variableNameOperator;
	String variableValueOperator;
	ArrayList processInstances;
	
	public String showProcessDefinitions() {
		return "processDefinitions";
	}
	
	public List getProcessDefinitions() {
		
		ArrayList processDefinitionsList = new ArrayList();

		JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
		List processDefinitions = jbpmContext.getGraphSession().findAllProcessDefinitions();

		if (processDefinitions.isEmpty() == false) {
			ListIterator listProcessDefinitions = processDefinitions.listIterator();
			while (listProcessDefinitions.hasNext() ) {
				ProcessDefinition processDefinition = (ProcessDefinition)listProcessDefinitions.next();

				int instancesCount = 0;
				try {
					Connection connection = jbpmContext.getConnection();
					Statement statement = connection.createStatement();
					
					String request = "SELECT COUNT(*) AS instancesCount "
						+ "FROM jbpm_processinstance " 
						+ "WHERE processdefinition_='"
						+ processDefinition.getId() + "'";
					ResultSet resultSet = statement.executeQuery(request);
					resultSet.next();
					instancesCount = resultSet.getInt("instancesCount");
				}
				catch (Exception e) {}

				processDefinitionsList.add(
						new ProcessDefinitionBean(
							processDefinition.getId(),
							processDefinition.getName(),
							processDefinition.getVersion(),
							instancesCount
							));					
			}
		}

		return(processDefinitionsList);
	}
	
	public String inspectInstance() {
		try {
			ProcessInstanceBean processInstanceBean = new ProcessInstanceBean(this.processInstanceId);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("processInstanceBean", processInstanceBean);
			this.message = "";
			return "inspectInstance";
		}
		catch (Exception exception) {
			this.message = "Error for process instance " + this.processInstanceId;
			return "";
		}
	}

	public String showSearchInstances() {
		return("showSearchInstances");
	}
	
	public String searchInstances() {

		long count = 0;
		
        JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();

		try {
			Connection connection = jbpmContext.getConnection();
			Statement statement = connection.createStatement();
			statement.setMaxRows(100);
			
			String request = "SELECT DISTINCT processinstance_, name_, stringvalue_  FROM jbpm_variableinstance " 
						+ "WHERE name_ "
						+ this.variableNameOperator + " '" 
						+ variableName + "' AND stringvalue_ "
						+ this.variableValueOperator + " '" + variableValue + "'";

			ResultSet resultSet = statement.executeQuery(request);
			
			processInstances = new ArrayList();
			
			while (resultSet.next()) {
				processInstances.add(new ProcessInstanceBean(
						resultSet.getLong("processinstance_"),
						resultSet.getString("name_"),
						resultSet.getString("stringvalue_")));
				count++;
			}
			statement.close();
		}
		catch (Exception e) {
			this.message = "Search error " + e.getMessage();
		}
		
		if (count == 1) {
			ProcessInstanceBean processInstanceBean = (ProcessInstanceBean)processInstances.iterator().next();
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("processInstanceBean", processInstanceBean);
			return("inspectInstance");	
		}
		return "";
	}
	
	public List getOperatorsList (){

        ArrayList operatorsList =  new ArrayList();

		SelectItem item = new SelectItem("=", "is equal to");
		operatorsList.add(item);
		item = new SelectItem("like", "is like");
		operatorsList.add(item);			
        return operatorsList;
		
	}
	
	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public boolean isShowProcessInstances() {
		if (processInstances == null) return false;
		if (processInstances.size() == 0) return false;
		return true;
	}

	public ArrayList getProcessInstances() {
		return processInstances;
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

	public String getVariableNameOperator() {
		return variableNameOperator;
	}

	public void setVariableNameOperator(String variableNameOperator) {
		this.variableNameOperator = variableNameOperator;
	}

	public String getVariableValueOperator() {
		return variableValueOperator;
	}

	public void setVariableValueOperator(String variableValueOperator) {
		this.variableValueOperator = variableValueOperator;
	}


}
