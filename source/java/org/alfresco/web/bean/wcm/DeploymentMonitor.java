/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean.wcm;

import java.io.Serializable;

import org.alfresco.service.cmr.avm.deploy.DeploymentCallback;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent;
import org.alfresco.service.cmr.avm.deploy.DeploymentEvent.Type;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;
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

public class DeploymentMonitor implements DeploymentCallback, Serializable
{
   private static final long serialVersionUID = 8167554931073708558L;
   
   private String id;
   private NodeRef website;
   private NodeRef targetServer;
   private String targetServerName;
   private String deployAttemptId;
   private String url;
   private String reason;
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
            String serverName, String deployAttemptId, String url)
   {
      this.id = ID_PREFIX + Long.toString(System.currentTimeMillis()) + this.hashCode();
      this.website = website;
      this.targetServer = server;
      this.snapshotVersion = snapshotVersion;
      this.targetServerName = serverName;
      this.deployAttemptId = deployAttemptId;
      this.url = url;
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
      else if (event.getType().equals(Type.FAILED))
      {
         // if we get the FAILED event the deployment was unsuccessful
         this.successful = false;
         this.finished = true;
         
         this.reason = event.getMessage();
      
         if (logger.isDebugEnabled())
            logger.debug(this.targetServerName + ": ERROR: " + this.reason);
      }
      
      if (logger.isDebugEnabled())
         logger.debug(this.targetServerName + ": " + event.getType() + 
                  " " + event.getDestination());
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
      buffer.append(" url=").append(this.url);
      buffer.append(" reason=").append(this.reason);
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
      buffer.append(Utils.encode(this.targetServerName));
      buffer.append("\" server=\"");
      buffer.append(this.targetServer);
      buffer.append("\" attempt=\"");
      buffer.append(this.deployAttemptId);
      buffer.append("\" finished=\"");
      buffer.append(this.finished);
      buffer.append("\"");
      if (this.url != null)
      {
         buffer.append(" url=\"");
         buffer.append(Utils.encode(this.url));
         buffer.append("\"");
      }
      if (this.reason != null)
      {
         buffer.append(" reason=\"");
         buffer.append(Utils.encode(this.reason));
         buffer.append("\"");
      }
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
    * @return The URL of the server being deployed
    */
   public String getUrl()
   {
      return this.url;
   }
   
   /**
    * @return The reason for the error, null if an error has not occurred
    */
   public String getReason()
   {
      return this.reason;
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
