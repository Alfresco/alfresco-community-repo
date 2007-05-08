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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.DeploymentMonitor;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSF component that allows a user to select which servers to deploy a
 * website to and provides monitoring of the deployments selected
 * 
 * @author gavinc
 */
public class UIDeployWebsite extends UIInput
{
   protected NodeRef webProjectRef;
   protected Integer snapshotVersion = -1;
   protected Boolean monitorDeployment;
   protected List<String> monitorIds;
   
   private static Log logger = LogFactory.getLog(UIDeployWebsite.class);
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * Default constructor
    */
   public UIDeployWebsite()
   {
      setRendererType(null);
   }
   
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.DeployWebsite";
   }
   
   @Override
   public void decode(FacesContext context)
   {
      super.decode(context);
      
      Map valuesMap = context.getExternalContext().getRequestParameterValuesMap();
      String[] values = (String[])valuesMap.get(this.getClientId(context));
      
      setSubmittedValue(values);
   }

   @SuppressWarnings("unchecked")
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.webProjectRef = (NodeRef)values[1];
      this.monitorDeployment = (Boolean)values[2];
      this.monitorIds = (List<String>)values[3];
      this.snapshotVersion = (Integer)values[4];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[5];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.webProjectRef;
      values[2] = this.monitorDeployment;
      values[3] = this.monitorIds;
      values[4] = this.snapshotVersion;
      return values;
   }
   
   @SuppressWarnings("unchecked")
   @Override
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
         
         NodeRef webProject = this.getWebsite();
         if (webProject == null)
         {
            throw new IllegalArgumentException("The web project must be specified.");
         }
         
         // add some before the panels
         out.write("\n<div style='padding-top:4px;'></div>\n");
      
         if (this.getMonitor())
         {
            // get the ids of the deployment monitor objects
            List<String> deployMonitorIds = this.getMonitorIds();
            
            if (logger.isDebugEnabled())
            {
               logger.debug("Monitoring deployment of: " + deployMonitorIds);
            }
            
            // TODO: Add support for 'sniffing' the session for deployment monitors
            //       this could be useful when the monitor ids are not known
            
            // render the supporting script required for monitoring
            renderScript(context, out, deployMonitorIds);
            
            // render each server being deployed with an animated icon, the subsequent
            // AJAX callback will update the progress.
            for (String id : deployMonitorIds)
            {
               // try and find the monitor object in the session, if it's not there
               // it has probably completed and been removed
               DeploymentMonitor monitor = (DeploymentMonitor)context.getExternalContext().
                     getSessionMap().get(id);
               if (monitor != null)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Found deployment monitor: " + monitor);

                  renderServer(context, out, monitor.getTargetServer(), false, true, id);
               }
            }
         }
         else
         {
            // get a list of the servers that have been successfully deployed to 
            NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
            int deployingVersion = this.getSnapshotVersion();
            List<String> serversAlreadyDeployed = new ArrayList<String>();
            List<ChildAssociationRef> deployReportRefs = nodeService.getChildAssocs(
                     webProjectRef, WCMAppModel.ASSOC_DEPLOYMENTREPORT, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : deployReportRefs)
            {
               NodeRef report = ref.getChildRef();
               Boolean success = (Boolean)nodeService.getProperty(report, 
                        WCMAppModel.PROP_DEPLOYSUCCESSFUL);
               int deployedVersion = (Integer)nodeService.getProperty(report, 
                        WCMAppModel.PROP_DEPLOYVERSION);
               if (success.booleanValue() && (deployingVersion == deployedVersion))
               {
                  serversAlreadyDeployed.add((String)nodeService.getProperty(report, 
                           WCMAppModel.PROP_DEPLOYSERVER));
               }
            }
            
            // get the list of servers for the user to select from
            List<String> servers = (List<String>)nodeService.getProperty(webProject, 
                     WCMAppModel.PROP_DEPLOYTO);
            
            if (logger.isDebugEnabled())
               logger.debug("Servers available: " + servers + ", servers already deployed: " + 
                        serversAlreadyDeployed);

            // render the list of servers, only pre-select those that have not been 
            // deployed successfully
            for (String server : servers)
            {
               boolean preSelected = !serversAlreadyDeployed.contains(server);
               renderServer(context, out, server, preSelected, false, null);
            }
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
    * @return The NodeRef representation of the web project to show the deployment reports for 
    */
   public NodeRef getWebsite()
   {
      ValueBinding vb = getValueBinding("website");
      if (vb != null)
      {
         this.webProjectRef = (NodeRef)vb.getValue(getFacesContext());
      }
      
      return this.webProjectRef;
   }
   
   /**
    * @param value The NodeRef representation of the web project to show the deployment reports for 
    */
   public void setWebsite(NodeRef value)
   {
      this.webProjectRef = value;
   }
   
   /**
    * @return true if the component should monitor a deployment
    */
   public boolean getMonitor()
   {
      ValueBinding vb = getValueBinding("monitor");
      if (vb != null)
      {
         this.monitorDeployment = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.monitorDeployment == null)
      {
         this.monitorDeployment = Boolean.FALSE;
      }
      
      return this.monitorDeployment.booleanValue();
   }
   
   /**
    * @param monitor Determines whether a deployment should be monitored
    */
   public void setMonitor(boolean monitor)
   {
      this.monitorDeployment = new Boolean(monitor);
   }
   
   /**
    * @return The list of deployment monitor IDs
    */
   @SuppressWarnings("unchecked")
   public List<String> getMonitorIds()
   {
      ValueBinding vb = getValueBinding("monitorIds");
      if (vb != null)
      {
         this.monitorIds = (List<String>)vb.getValue(getFacesContext());
      }
      
      return this.monitorIds;
   }
   
   /**
    * @param monitorIds List of monitor IDs to look for
    */
   public void setMonitorIds(List<String> monitorIds)
   {
      this.monitorIds = monitorIds;
   }
   
   /**
    * @return The version of the snapshot being deployed
    */
   public int getSnapshotVersion()
   {
      ValueBinding vb = getValueBinding("snapshotVersion");
      if (vb != null)
      {
         this.snapshotVersion = (Integer)vb.getValue(getFacesContext());
      }
      
      return this.snapshotVersion;
   }
   
   /**
    * @param snapshotVersion The version of the snapshot being deployed
    */
   public void setSnapshotVersion(int snapshotVersion)
   {
      this.snapshotVersion = snapshotVersion;
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   private void renderScript(FacesContext context, ResponseWriter out, 
            List<String> monitorIds) throws IOException
   {
//      // render supporting Yahoo scripts 
//      Utils.writeYahooScripts(context, out, null);
      
      // create comma separated list of deplyment ids
      StringBuilder ids = new StringBuilder();
      for (int x = 0; x < monitorIds.size(); x++)
      {
         if (x > 0)
         {
            ids.append(",");
         }
         
         String id = monitorIds.get(x);
         ids.append(id);
      }
      
      // determine the polling frequency value
      int pollFreq = AVMUtil.getRemoteDeploymentPollingFrequency() * 1000;

      // render the script to handle the progress monitoring
      out.write("<script type='text/javascript'>\n");
      out.write("Alfresco.DeploymentMonitor = function(ids) {\n");
      out.write("   this.ids = ids;\n");
      out.write("   this.url = '");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/ajax/invoke/DeploymentProgressBean.getStatus?ids=' + this.ids;\n");
      out.write("   this.failedMsg = '");
      out.write(Application.getMessage(context, "deploy_failed"));
      out.write("';\n");
      out.write("   this.successMsg = '");
      out.write(Application.getMessage(context, "deploy_successful"));
      out.write("';\n");
      out.write("}\n");
      out.write("Alfresco.DeploymentMonitor.prototype = {\n");
      out.write("   ids: null,\n");
      out.write("   url: null,\n");
      out.write("   failedMsg: null,\n");
      out.write("   successMsg: null,\n");
      out.write("   retrieveDeploymentStatus: function() {\n");
      out.write("      YAHOO.util.Connect.asyncRequest('GET', this.url,\n");
      out.write("         {\n");
      out.write("            success: this.processResults,\n");
      out.write("            failure: this.handleError,\n");
      out.write("            scope: this\n");
      out.write("         },\n");
      out.write("         null);\n");
      out.write("   },\n");
      out.write("   processResults: function(ajaxResponse) {\n");
      out.write("      var xml = ajaxResponse.responseXML.documentElement;\n");
      out.write("      var statuses = xml.getElementsByTagName('target-server');\n");
      out.write("      var noInProgress = statuses.length;\n");
      out.write("      if (noInProgress > 0) {\n");
      out.write("         for (var x = 0; x < noInProgress; x++) {\n");
      out.write("            var target = statuses[x];\n");
      out.write("            var id = target.getAttribute('id');\n");
      out.write("            var finished = target.getAttribute('finished');\n");
      out.write("            if (finished == 'true') {\n");
      out.write("               var successful = target.getAttribute('successful');\n");
      out.write("               var icon = document.getElementById(id + '_icon');\n");
      out.write("               if (icon != null) {\n");
      out.write("                  var image = (successful == 'true') ? 'successful' : 'failed';\n");
      out.write("                  icon.src = '/alfresco/images/icons/deploy_' + image + '.gif';\n");
      out.write("               }\n");
      out.write("               var text = document.getElementById(id + '_text');\n");
      out.write("               if (text != null) {\n");
      out.write("                  var msg = (successful == 'true') ? this.successMsg : this.failedMsg;\n");
      out.write("                  text.innerHTML = msg;\n");
      out.write("               }\n");
      out.write("            }\n");
      out.write("         }\n");
      out.write("         // there are still outstanding deployments, refresh in a few seconds\n");
      out.write("         setTimeout('Alfresco.monitor.retrieveDeploymentStatus()', ");
      out.write(Integer.toString(pollFreq));
      out.write(");\n");
      out.write("      }\n");
      out.write("   },\n");
      out.write("   handleError: function(ajaxResponse) {\n");
      out.write("      handleErrorYahoo(ajaxResponse.status + ' ' + ajaxResponse.statusText);\n");
      out.write("   }\n");
      out.write("}\n");
      out.write("Alfresco.initDeploymentMonitor = function() {\n");
      out.write("   Alfresco.monitor = new Alfresco.DeploymentMonitor('");
      out.write(ids.toString());
      out.write("');\n");
      out.write("   Alfresco.monitor.retrieveDeploymentStatus();\n");
      out.write("}\n");
      out.write("Alfresco.monitor = null;\n");
      out.write("window.onload = Alfresco.initDeploymentMonitor;\n");
      out.write("</script>\n");
   }
   
   private void renderServer(FacesContext context, ResponseWriter out, String server, 
            boolean selected, boolean monitoring, String monitorId) throws IOException
   {
      String contextPath = context.getExternalContext().getRequestContextPath();
      
      out.write("<table cellspacing='0' cellpadding='0' border='0' width='100%'>");
      out.write("<tr><td width='10'><img src='");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_start.gif' /></td>");
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_bg.gif); background-repeat: repeat-x;'>");
      if (monitoring)
      {
         out.write("<div class='deployPanelStatusIcon'>");
         out.write("<img id='");
         out.write(monitorId);
         out.write("_icon' src='");
         out.write(contextPath);
         out.write("/images/icons/ajax_anim.gif' />");
      }
      else
      {
         out.write("<div class='deployPanelCheckbox'>");
         out.write("<input type='checkbox' name='");
         out.write(this.getClientId(context));
         out.write("' value='");
         out.write(server);
         out.write("'");
         if (selected)
         {
            out.write("checked='checked'");
         }
         out.write(" />");
      }
      out.write("</div></td><td width='2'><img src='");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_separator.gif' /></td>");
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_bg.gif); background-repeat: repeat-x;'>");
      out.write("<div class='deployPanelIcon'>");
      out.write("<img src='");
      out.write(contextPath);
      out.write("/images/icons/deploy_server_large.gif' /></div></td>");
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_bg.gif); background-repeat: repeat-x; width: 100%;'>");
      out.write("<div class='deployPanelServerName'>");
      out.write(server);
      out.write("</div>");
      if (monitoring)
      {
         out.write("<div class='deployPanelServerStatus' id='");
         out.write(monitorId);
         out.write("_text'>");
         out.write(Application.getMessage(context, "deploying"));
         out.write("</div>");
      }
      else if (selected == false)
      {
         out.write("<div class='deployPanelServerStatus'><img src='");
         out.write(contextPath);
         out.write("/images/icons/info_icon.gif' style='vertical-align: -5px;' />&nbsp;");
         out.write(Application.getMessage(context, "deploy_server_not_selected"));
         out.write("</div>");
      }
      out.write("</td><td width='10'><img src='");
      out.write(contextPath);
      out.write("/images/parts/deploy_panel_end.gif' /></td></tr></table>");
      
      // add some padding under each panel
      out.write("\n<div style='padding-top:8px;'></div>\n");
   }
}
