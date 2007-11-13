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

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
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
   protected NodeRef webProjectRef;
   
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
      this.webProjectRef = (NodeRef)values[1];
   }
   
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.webProjectRef;
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
         
         NodeRef webProject = getValue();
         if (webProject == null)
         {
            throw new IllegalArgumentException("The web project must be specified.");
         }
         
         if (logger.isDebugEnabled())
            logger.debug("Rendering deployment reports for: " + webProject.toString());
         
         // render the supporting JavaScript
         renderScript(context, out);
         
         // iterate through each deployment report
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         ContentService contentService = Repository.getServiceRegistry(context).getContentService();
         List<ChildAssociationRef> deployReportRefs = nodeService.getChildAssocs(
                  this.webProjectRef, WCMAppModel.ASSOC_DEPLOYMENTREPORT, RegexQNamePattern.MATCH_ALL);
         for (ChildAssociationRef ref : deployReportRefs)
         {
            // render each report
            renderReport(context, out, ref.getChildRef(), nodeService, contentService);
         }
         
         // add some padding after the panels
         out.write("\n<div style='padding-top:8px;'></div>\n");
         
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
    * @return The NodeRef representation of the web project to show the deployment reports for 
    */
   public NodeRef getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.webProjectRef = (NodeRef)vb.getValue(getFacesContext());
      }
      
      return this.webProjectRef;
   }
   
   /**
    * @param value The NodeRef representation of the web project to show the deployment reports for 
    */
   public void setValue(NodeRef value)
   {
      this.webProjectRef = value;
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   private void renderScript(FacesContext context, ResponseWriter out)
      throws IOException
   {
      out.write("<script type='text/javascript'>\n");
      out.write("function toggleDeploymentDetails(icon, server) {\n");
      out.write("   var currentState = icon.className;\n");
      out.write("   var detailsDiv = document.getElementById(server + '-deployment-details');\n");
      out.write("   if (currentState == 'collapsed') {\n");
      out.write("      icon.src = '");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/expanded.gif';\n");
      out.write("      icon.className = 'expanded';\n");
      out.write("      if (detailsDiv != null) { detailsDiv.style.display = 'block'; }\n");
      out.write("   } else {\n");
      out.write("      icon.src = '");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/collapsed.gif';\n");
      out.write("      icon.className = 'collapsed';\n");
      out.write("      if (detailsDiv != null) { detailsDiv.style.display = 'none'; }\n");
      out.write("   }\n}</script>\n");
   }
   
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
               context.getExternalContext().getRequestContextPath(), "innerwhite", "white");
      
      // extract the information we need to display
      String server = (String)nodeService.getProperty(deploymentReport, WCMAppModel.PROP_DEPLOYSERVER);
      String creator = (String)nodeService.getProperty(deploymentReport, ContentModel.PROP_CREATOR);
      Date startTime = (Date)nodeService.getProperty(deploymentReport, WCMAppModel.PROP_DEPLOYSTARTTIME);
      String started = null;
      if (startTime != null)
      {
         started = Utils.getDateTimeFormat(context).format(startTime);
      }
      
      Date endTime = (Date)nodeService.getProperty(deploymentReport, WCMAppModel.PROP_DEPLOYENDTIME);
      String finished = null;
      if (endTime != null)
      {
         finished = Utils.getDateTimeFormat(context).format(endTime);
      }
      
      Boolean success = (Boolean)nodeService.getProperty(deploymentReport, WCMAppModel.PROP_DEPLOYSUCCESSFUL);
      if (success == null)
      {
         success = Boolean.FALSE;
      }
      
      String failReason = (String)nodeService.getProperty(deploymentReport, WCMAppModel.PROP_DEPLOYFAILEDREASON);
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
      Serializable snapshotObj = nodeService.getProperty(deploymentReport, WCMAppModel.PROP_DEPLOYVERSION);
      if (snapshotObj != null && snapshotObj instanceof Integer)
      {
         snapshot = (Integer)snapshotObj;
      }
      
      out.write("<table cellspacing='0' cellpadding='2' border='0' width='100%'>");
      out.write("<tr><td width='40' valign='top'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/deploy_server_large.gif' /></td><td>");
      out.write("<div class='mainHeading'>");
      out.write(server);
      out.write("</div><div style='margin-top: 3px;'><img src='");
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
      out.write("<div style='margin-top: 6px;'>");
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
      if (success.booleanValue() == false && failReason != null && failReason.length() > 0)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "reason"));
         out.write(":&nbsp;");
         out.write(failReason);
         out.write("</div>");
      }
      if (content.length() > 0)
      {
         out.write("<div style='margin-top: 6px;'><img src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/images/icons/collapsed.gif' style='vertical-align: -1px; cursor: pointer;' class='collapsed' onclick=\"toggleDeploymentDetails(this, '");
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
               context.getExternalContext().getRequestContextPath(), "innerwhite");
   }
}
