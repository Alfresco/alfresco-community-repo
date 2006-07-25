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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class JsfHelper {

  public static long getId(String parameterName) {
    long value = -1;
    String valueText = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(parameterName);
    try {
      Long id = new Long(valueText);
      value = id.longValue();
    } catch (NumberFormatException e) {
      throw new RuntimeException("couldn't parse '"+parameterName+"'='"+valueText+"' as a long");
    }
    return value;
  }
  
  public static void addMessage(String msg) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(msg));
  }

  public static void setSessionAttribute(String key, Object value) {
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(key, value);
  }

  public static Object getSessionAttribute(String key) {
    return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key);
  }

  public static void removeSessionAttribute(String key) {
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(key);
  }

  public static String getParameter(String name) {
    return (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
  }
  // private static final Log log = LogFactory.getLog(JsfHelper.class);
}
