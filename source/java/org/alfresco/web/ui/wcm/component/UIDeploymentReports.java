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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.actions.AVMDeployWebsiteAction;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.sandbox.SandboxConstants;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.DeploymentUtil;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

/**
 * JSF component that displays the latest deployment reports for a web project.
 * 
 * @author gavinc
 */
public class UIDeploymentReports extends SelfRenderingComponent
{
   protected String store;
   
   private static Log logger = LogFactory.getLog(UIDeploymentReports.class);
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.DeploymentReports";
   }
   
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.store = (String)values[1];
   }
   
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.store;
      return values;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         String storeName = getValue();
         if (storeName == null)
         {
            throw new IllegalArgumentException("The store must be specified.");
         }
         
         if (logger.isDebugEnabled())
            logger.debug("Rendering deployment reports for store: " + storeName);
         
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         ContentService contentService = Repository.getServiceRegistry(context).getContentService();
         AVMService avmService = Repository.getServiceRegistry(context).getAVMService();
         
         PropertyValue val = avmService.getStoreProperty(storeName, SandboxConstants.PROP_LAST_DEPLOYMENT_ID);
         String attemptId = null;
         
         if (val != null)
         {
            attemptId = val.getStringValue();
         }
         
         if (attemptId == null || attemptId.length() == 0)
         {
            throw new IllegalStateException("Failed to retrieve deployment attempt id");
         }
         
         if (logger.isDebugEnabled())
            logger.debug("Retrieving deployment reports for attempt id: " + attemptId);
         
         // get the deploymentattempt object
         NodeRef attempt = DeploymentUtil.findDeploymentAttempt(attemptId);
         
         if (attempt != null)
         {
            // render the supporting JavaScript
            out.write("<script type='text/javascript' src='");
            out.write(context.getExternalContext().getRequestContextPath());
            out.write("/scripts/ajax/deployment.js'></script>\n");
            
            // iterate through each deployment report
            List<ChildAssociationRef> deployReportRefs = nodeService.getChildAssocs(
                     attempt, WCMAppModel.ASSOC_DEPLOYMENTREPORTS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : deployReportRefs)
            {
               // render each report
               renderReport(context, out, ref.getChildRef(), nodeService, contentService);
            }
            
            // add some padding after the panels
            out.write("\n<div style='padding-top:8px;'></div>\n");
         }
         
         tx.commit();
      }
      catch (Throwable err)
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         throw new RuntimeException(err);
      }
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * @return The store to show the deployment reports for 
    */
   public String getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.store = (String)vb.getValue(getFacesContext());
      }
      
      return this.store;
   }
   
   /**
    * @param value The store to show the deployment reports for 
    */
   public void setValue(String value)
   {
      this.store = value;
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   private void renderReport(FacesContext context, ResponseWriter out, NodeRef deploymentReport,
            NodeService nodeService, ContentService contentService)
      throws IOException
   {
      if (logger.isDebugEnabled())
         logger.debug("Rendering report: " + deploymentReport);
      
      // add some padding before the panel
      out.write("\n<div style='padding-top:8px;'></div>\n");
      
      // start the surrounding panel
      PanelGenerator.generatePanelStart(out, 
               context.getExternalContext().getRequestContextPath(), "lightstorm", "#eaeff2");
      
      // extract the information we need to display
      Map<QName, Serializable> serverProps = nodeService.getProperties(deploymentReport);
      String server = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVER);
      boolean showServerAddress = true;
      String serverName = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERNAMEUSED);
      if (serverName == null || serverName.length() == 0)
      {
         serverName = server;
         showServerAddress = false;
      }
      
      String deployType = WCMAppModel.CONSTRAINT_ALFDEPLOY;
      if (server.startsWith(AVMDeployWebsiteAction.FILE_SERVER_PREFIX))
      {
         deployType = WCMAppModel.CONSTRAINT_FILEDEPLOY;
      }
      
      String creator = (String)serverProps.get(ContentModel.PROP_CREATOR);
      Date startTime = (Date)serverProps.get(WCMAppModel.PROP_DEPLOYSTARTTIME);
      String started = "";
      if (startTime != null)
      {
         started = Utils.getDateTimeFormat(context).format(startTime);
      }
      
      Date endTime = (Date)serverProps.get(WCMAppModel.PROP_DEPLOYENDTIME);
      String finished = "";
      if (endTime != null)
      {
         finished = Utils.getDateTimeFormat(context).format(endTime);
      }
      
      Boolean success = (Boolean)serverProps.get(WCMAppModel.PROP_DEPLOYSUCCESSFUL);
      if (success == null)
      {
         success = Boolean.FALSE;
      }
      
      String url = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERURLUSED);
      String username = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERUSERNAMEUSED);
      String source = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSOURCEPATHUSED);
      String target = (String)serverProps.get(WCMAppModel.PROP_DEPLOYSERVERTARGETUSED);
      String failReason = (String)serverProps.get(WCMAppModel.PROP_DEPLOYFAILEDREASON);
      
      String content = "";
      ContentReader reader = contentService.getReader(deploymentReport, ContentModel.PROP_CONTENT);
      if (reader != null)
      {
         content = reader.getContentString();
         if (content != null)
         {
            content = StringUtils.replace(content, "\r\n", "<br/>");
         }
         else
         {
            content = "";
         }
      }
      
      int snapshot = -1;
      Serializable snapshotObj = serverProps.get(WCMAppModel.PROP_DEPLOYVERSION);
      if (snapshotObj != null && snapshotObj instanceof Integer)
      {
         snapshot = (Integer)snapshotObj;
      }
      
      out.write("<table cellspacing='0' cellpadding='2' border='0' width='100%'>");
      out.write("<tr><td width='40' valign='top'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/deploy_server_");
      out.write(deployType);
      out.write(".gif' /></td><td>");
      out.write("<div class='mainHeading'>");
      out.write(serverName);
      out.write("</div><div style='margin-top: 3px; margin-bottom: 6px;'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/deploy_");
      if (success.booleanValue())
      {
         out.write("successful");
      }
      else
      {
         out.write("failed");
      }
      out.write(".gif' style='vertical-align: -4px;' />&nbsp;&nbsp;");
      if (success.booleanValue())
      {
         out.write(Application.getMessage(context, "deploy_successful"));
      }
      else
      {
         out.write(Application.getMessage(context, "deploy_failed"));
      }
      out.write("</div>");
      
      if (success.booleanValue() == false && failReason != null && failReason.length() > 0)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "reason"));
         out.write(":&nbsp;");
         out.write(failReason);
         out.write("</div>");
      }
      
      if (showServerAddress)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server"));
         out.write(":&nbsp;");
         out.write(server);
         out.write("</div>");
      }
      
      out.write("<div style='margin-top: 3px;'>");
      out.write(Application.getMessage(context, "snapshot"));
      out.write(":&nbsp;");
      out.write(Integer.toString(snapshot));
      out.write("</div>");
      
      out.write("<div style='margin-top: 3px;'>");
      out.write(Application.getMessage(context, "deploy_started"));
      out.write(":&nbsp;");
      out.write(started);
      out.write("</div>");
      
      out.write("<div style='margin-top: 3px;'>");
      out.write(Application.getMessage(context, "deploy_finished"));
      out.write(":&nbsp;");
      out.write(finished);
      out.write("</div>");
      
      out.write("<div style='margin-top: 3px;'>");
      out.write(Application.getMessage(context, "deployed_by"));
      out.write(":&nbsp;");
      out.write(creator);
      out.write("</div>");
      
      if (username != null)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server_username"));
         out.write(":&nbsp;");
         out.write(username);
         out.write("</div>");
      }
      
      if (source != null)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server_source_path"));
         out.write(":&nbsp;");
         out.write(source);
         out.write("</div>");
      }
      
      if (target != null)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server_target_name"));
         out.write(":&nbsp;");
         out.write(target);
         out.write("</div>");
      }
      
      if (success.booleanValue() == true && url != null && url.length() > 0)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server_url"));
         out.write(":&nbsp;<a target='new' href='");
         out.write(url);
         out.write("'>");
         out.write(url);
         out.write("</a></div>");
      }
      
      if (content.length() > 0)
      {
         out.write("<div style='margin-top: 6px;'><img src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/images/icons/collapsed.gif' style='vertical-align: -1px; cursor: pointer;' class='collapsed' onclick=\"Alfresco.toggleDeploymentDetails(this, '");
         out.write(server.replace(':', '_').replace('\\', '_'));
         out.write("');\" />&nbsp;");
         out.write(Application.getMessage(context, "details"));
         out.write("</div>\n");
         out.write("<div id='");
         out.write(server.replace(':', '_').replace('\\', '_'));
         out.write("-deployment-details' style='display: none; border: 1px dotted #eee; margin-left: 14px; margin-top: 4px; padding:3px;'>");
         out.write(content);
         out.write("</div>");
      }
      out.write("\n<div style='padding-top:6px;'></div>\n");
      out.write("</td></tr></table>");
      
      // finish the surrounding panel
      PanelGenerator.generatePanelEnd(out, 
               context.getExternalContext().getRequestContextPath(), "lightstorm");
   }
}
