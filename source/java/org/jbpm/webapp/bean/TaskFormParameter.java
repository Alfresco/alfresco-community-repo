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

import java.io.Serializable;

import org.hibernate.Session;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class TaskFormParameter implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String label = null;
  protected String description = null;
  protected Object value = null;
  protected boolean isReadable = true;
  protected boolean isWritable = true;
  protected boolean isRequired = true;
  
  public TaskFormParameter() {
  }
  
  public TaskFormParameter(VariableAccess variableAccess, Object value) {
    this.label = variableAccess.getMappedName();
    this.value = value;
    this.isReadable = variableAccess.isReadable();
    this.isWritable = variableAccess.isWritable();
    this.isRequired = variableAccess.isRequired();
  }

  public TaskFormParameter(TaskFormParameter other) {
    this.label = other.label;
    this.description = other.description;
    this.value = other.value;
    this.isReadable = other.isReadable;
    this.isWritable = other.isWritable;
    this.isRequired = other.isRequired;
  }

  public static TaskFormParameter create(TaskInstance instance, String name, Object value, Session session) {
    TaskFormParameter taskFormParameter = null;
    return taskFormParameter;
  }

  public String toString() {
    return "("+label+","+value+")";
  }
  
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public boolean isReadable() {
    return isReadable;
  }
  public void setReadable(boolean isReadable) {
    this.isReadable = isReadable;
  }
  public boolean isRequired() {
    return isRequired;
  }
  public void setRequired(boolean isRequired) {
    this.isRequired = isRequired;
  }
  public boolean isWritable() {
    return isWritable;
  }
  public boolean isReadOnly() {
     return !isWritable;
  }
  public void setWritable(boolean isWritable) {
    this.isWritable = isWritable;
  }
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  public Object getValue() {
    return value;
  }
  public void setValue(Object value) {
    this.value = value;
  }
}
