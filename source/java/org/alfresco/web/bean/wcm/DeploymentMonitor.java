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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.bean.wcm;

import java.io.Serializable;

import org.alfresco.service.cmr.avm.deploy.DeploymentCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent.Type;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Object used to monitor the deployment of a snapshot to a remote
 * server. These objects are typically stored in the HTTP session
 * and accessed by an AJAX callback to provide progress feedback
 * for the deployment. 
 * 
 * @author gavinc
 */
@SuppressWarnings("serial")
public class DeploymentMonitor implements DeploymentCallback, Serializable
{
   private String id;
   private NodeRef website;
   private NodeRef targetServer;
   private String targetServerName;
   private String deployAttemptId;
   private int snapshotVersion;
   private boolean started = false;
   private boolean finished = false;
   private boolean successful = false;
   
   private static Log logger = LogFactory.getLog(DeploymentMonitor.class);
   private static final String ID_PREFIX = "_depmon_";
   
   /**
    * Default constructor
    */
   public DeploymentMonitor(NodeRef website, NodeRef server, int snapshotVersion, 
            String serverName, String deployAttemptId)
   {
      this.id = ID_PREFIX + Long.toString(System.currentTimeMillis()) + this.hashCode();
      this.website = website;
      this.targetServer = server;
      this.snapshotVersion = snapshotVersion;
      this.targetServerName = serverName;
      this.deployAttemptId = deployAttemptId;
   }
   
   // ------------------------------------------------------------------------------
   // DeploymentCallback implementation
   
   public void eventOccurred(DeploymentEvent event)
   {
      // we're only interested in the start and end event for the time
      // being, we'll add support for returning item by item progress
      // at a later date.
      if (event.getType().equals(Type.START))
      {
         this.started = true;
      }
      else if (event.getType().equals(Type.END))
      {
         // if we get the END event the deployment was successful
         this.successful = true;
         this.finished = true;
      }
      
      if (logger.isDebugEnabled())
         logger.debug(this.targetServerName + ": " + event.getType() + 
                  " " + event.getDestination());
   }
   
   /**
    * Informs the monitor an error occurred during deployment
    * 
    * @param err The error that caused the deployment to fail
    */
   public void errorOccurred(Throwable err)
   {
      if (logger.isDebugEnabled())
         logger.debug(this.targetServerName + ": ERROR: " + err.getMessage()); 
      
      this.successful = false;
      this.finished = true;
   }
   
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder(super.toString());
      buffer.append(" (id=").append(this.id);
      buffer.append(" website=").append(this.website);
      buffer.append(" targetServer=").append(this.targetServer);
      buffer.append(" targetServerName=").append(this.targetServerName);
      buffer.append(" snapshotVersion=").append(this.snapshotVersion);
      buffer.append(" deployAttemptId=").append(this.deployAttemptId);
      buffer.append(" started=").append(this.started);
      buffer.append(" finished=").append(this.finished);
      buffer.append(" successful=").append(this.successful).append(")");
      return buffer.toString();
   }
   
   public String toXML()
   {
      StringBuilder buffer = new StringBuilder("<target-server id=\"");
      buffer.append(this.getId());
      buffer.append("\" name=\"");
      buffer.append(this.targetServerName);
      buffer.append("\" server=\"");
      buffer.append(this.targetServer);
      buffer.append("\" attempt=\"");
      buffer.append(this.deployAttemptId);
      buffer.append("\" finished=\"");
      buffer.append(this.finished);
      buffer.append("\"");
      if (this.finished)
      {
         buffer.append(" successful=\"");
         buffer.append(this.successful);
         buffer.append("\"");
      }
      buffer.append("/>");
      return buffer.toString();
   }
   
   // ------------------------------------------------------------------------------
   // Getters and setters
   
   /**
    * @return The id for this deployment monitor
    */
   public String getId()
   {
      return this.id;
   }
   
   /**
    * @return The NodeRef representation of the website being deployed
    */
   public NodeRef getWebsite()
   {
      return this.website;
   }
   
   /**
    * @return The target server for this deployment
    */
   public NodeRef getTargetServer()
   {
      return this.targetServer;
   }
   
   /**
    * @return The target server display name for this deployment
    */
   public String getTargetServerName()
   {
      return this.targetServerName;
   }

   /**
    * @return The snapshot version being deployed
    */
   public int getSnapshotVersion()
   {
      return this.snapshotVersion;
   }
   
   /**
    * @return The deploy attempt id for this deployment
    */
   public String getDeployAttemptId()
   {
      return this.deployAttemptId;
   }

   /**
    * @return true if the deployment has started
    */
   public boolean isStarted()
   {
      return this.started;
   }
   
   /**
    * @return true if the deployment has finished
    */
   public boolean isFinished()
   {
      return this.finished;
   }

   /**
    * @return true if the deployment was successful, 
    *         only reliable once isFinished returns true.
    */
   public boolean isSuccessful()
   {
      return this.successful;
   }
}
