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

import javax.faces.context.FacesContext;

import org.jbpm.scheduler.impl.Scheduler;

public class AdminBean {
  
  String deployUrl;
  
  public void deployProcess() {
  }
  
  public void createSchema() {
  }
  
  public void dropSchema() {
  }
  
  public boolean isSchedulerRunning() {
    return getScheduler().isRunning();
  }
  
  public List getSchedulerHistoryLogs() {
    return getScheduler().getSchedulerHistoryLogs();
  }
  
  private Scheduler getScheduler() {
    return (Scheduler) FacesContext.getCurrentInstance()
                                   .getExternalContext()
                                   .getApplicationMap()
                                   .get("scheduler");
  }

  public String getDeployUrl() {
    return deployUrl;
  }
  public void setDeployUrl(String deployUrl) {
    this.deployUrl = deployUrl;
  }
}
