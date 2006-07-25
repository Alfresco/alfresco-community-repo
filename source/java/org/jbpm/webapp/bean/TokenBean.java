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

import java.util.Date;

/**
 * Token Bean Implementation.
 * 
 * @author David Loiseau
 */

public class TokenBean {

  long id;
  String name;
  String nodeName;
  String nodeClassName;
  Date start;
  Date end;
  long level;

  public TokenBean(long id, String name, String nodeName, String nodeClassName, Date start, Date end, long level) {

    this.id = id;
    this.name = name;
    this.nodeName = nodeName;
    this.nodeClassName = nodeClassName;
    this.start = start;
    this.end = end;
    this.level = level;
  }

  private String getTypeNameFromClassName(String className) {
    String typeName = "";
    if (className.indexOf(".") > 0) {
      typeName = className.substring(className.lastIndexOf(".") + 1);
    }
    return typeName;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabel() {
    String label = "";
    int i = 1;
    while (i < this.level) {
      label = label + "---";
      i++;
    }
    if (i > 1)
      label = label + " ";
    label = label + this.name;

    return label;
  }

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public Date getEnd() {
    return end;
  }

  public void setEnd(Date end) {
    this.end = end;
  }

  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }

  public String getNodeType() {
    return getTypeNameFromClassName(this.nodeClassName);
  }

  public boolean isSignal() {
    if (this.end == null)
      return true;
    return false;
  }

}
