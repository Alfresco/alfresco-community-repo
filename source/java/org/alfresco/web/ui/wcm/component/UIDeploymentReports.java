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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.actions.AVMDeployWebsiteAction;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.wcm.sandbox.SandboxConstants;
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
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.UIActions;
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
   // date filters
   public static final String FILTER_DATE_TODAY       = "today";
   public static final String FILTER_DATE_YESTERDAY   = "yesterday";
   public static final String FILTER_DATE_WEEK        = "week";
   public static final String FILTER_DATE_MONTH       = "month";
   public static final String FILTER_DATE_ALL         = "all";
   
   protected String store;
   protected String dateFilter;
   protected Boolean showPrevious;
   protected NodeRef attempt;
   
   private static final String MSG_ATTEMPT_DATE = "deploy_attempt_date";
   private static final String MSG_SERVERS = "deployed_to_servers";
   private static final String MSG_SNAPSHOT = "snapshot";
   private static final String MSG_SELECT_ATTEMPT = "select_deploy_attempt";
   
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
      this.dateFilter = (String)values[2];
      this.showPrevious = (Boolean)values[3];
      this.attempt = (NodeRef)values[4];
   }
   
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[5];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.store;
      values[2] = this.dateFilter;
      values[3] = this.showPrevious;
      values[4] = this.attempt;
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
         
         boolean showPrevious = getShowPrevious();
         if (showPrevious)
         {
            renderPreviousReports(context, out, storeName);
         }
         else
         {
            renderAttempt(context, out, storeName);
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
   
   /**
    * @return The current dateFilter if previous reports are being shown
    */
   public String getDateFilter()
   {
      ValueBinding vb = getValueBinding("dateFilter");
      if (vb != null)
      {
         this.dateFilter = (String)vb.getValue(getFacesContext());
      }
      
      return this.dateFilter;
   }
   
   /**
    * @param value The dateFilter to use when previous reports are being shown
    */
   public void setDateFilter(String value)
   {
      this.dateFilter = value;
   }
   
   /**
    * @return true if the component should show previous reports
    */
   public boolean getShowPrevious()
   {
      ValueBinding vb = getValueBinding("showPrevious");
      if (vb != null)
      {
         this.showPrevious = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.showPrevious == null)
      {
         this.showPrevious = Boolean.FALSE;
      }
      
      return this.showPrevious.booleanValue();
   }
   
   /**
    * @param showPrevious Determines whether previous reports are shown
    */
   public void setShowPrevious(boolean showPrevious)
   {
      this.showPrevious = new Boolean(showPrevious);
   }
   
   /**
    * @return NodeRef of the deploymentattempt to show the details for 
    */
   public NodeRef getAttempt()
   {
      ValueBinding vb = getValueBinding("attempt");
      if (vb != null)
      {
         this.attempt = (NodeRef)vb.getValue(getFacesContext());
      }
      
      return this.attempt;
   }
   
   /**
    * @param value The NodeRef of the deploymentattempt to show the details for
    */
   public void setAttempt(NodeRef value)
   {
      this.attempt = value;
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   @SuppressWarnings("unchecked")
   protected void renderPreviousReports(FacesContext context, ResponseWriter out, String store)
      throws IOException
   {
      NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
      ResourceBundle bundle = Application.getBundle(context);
      
      List<NodeRef> deployAttempts = null;
      String dateFilter = getDateFilter();
      
      if (logger.isDebugEnabled())
         logger.debug("Rendering previous deployment reports for store '" + store +
                  "' using dateFilter: " + dateFilter);
      
      if (dateFilter == null || dateFilter.equals(FILTER_DATE_ALL))
      {
         deployAttempts = DeploymentUtil.findDeploymentAttempts(store);
      }
      else
      {
         // get today's date
         Date toDate = new Date();
         
         // calculate the from date
         Date fromDate;
         if (FILTER_DATE_TODAY.equals(dateFilter))
         {
            fromDate = toDate;
         }
         else if (FILTER_DATE_YESTERDAY.equals(dateFilter))
         {
            fromDate = new Date(toDate.getTime() - (1000L*60L*60L*24L));
            toDate = fromDate;
         }
         else if (FILTER_DATE_WEEK.equals(dateFilter))
         {
            fromDate = new Date(toDate.getTime() - (1000L*60L*60L*24L*7L));
         }
         else if (FILTER_DATE_MONTH.equals(dateFilter))
         {
            fromDate = new Date(toDate.getTime() - (1000L*60L*60L*24L*30L));
         }
         else
         {
            throw new IllegalArgumentException("Unknown date filter mode: " + dateFilter);
         }
         
         // get the filtered list of attempts
         deployAttempts = DeploymentUtil.findDeploymentAttempts(store, fromDate, toDate);
      }
      
      if (deployAttempts.size() > 0)
      {
         out.write("<table class='deployMoreReportsList' cellspacing='3' cellpadding='3' width='100%'>");
         out.write("<tr><th align='left'><nobr>");
         out.write(bundle.getString(MSG_ATTEMPT_DATE));
         out.write("</nobr></th><th align='left'><nobr>");
         out.write(bundle.getString(MSG_SERVERS));
         out.write("</nobr></th><th align='left'><nobr>");
         out.write(bundle.getString(MSG_SNAPSHOT));
         out.write("</th></tr>");
         
         // create a list of DeploymentAttempt objects to be ordered
         List<DeploymentAttempt> orderedAttempts = new ArrayList<DeploymentAttempt>(deployAttempts.size());
         for (NodeRef attempt : deployAttempts)
         {
            Map<QName, Serializable> props = nodeService.getProperties(attempt);
            String attemptId = (String)props.get(WCMAppModel.PROP_DEPLOYATTEMPTID);
            Date attemptTime = (Date)props.get(WCMAppModel.PROP_DEPLOYATTEMPTTIME);
            Integer version = (Integer)props.get(WCMAppModel.PROP_DEPLOYATTEMPTVERSION);
            List<String> servers = (List<String>)props.get(WCMAppModel.PROP_DEPLOYATTEMPTSERVERS);
            StringBuilder buffer = new StringBuilder();
            if (servers != null)
            {
               for (String server : servers)
               {
                  if (buffer.length() != 0)
                  {
                     buffer.append(", ");
                  }
                  
                  buffer.append(Utils.encode(server));
               }
            }
            
            orderedAttempts.add(new DeploymentAttempt(attempt, attemptId, 
                     attemptTime, buffer.toString(), version));
         }
         
         // sort the list of deployment attempts
         QuickSort sorter = new QuickSort(orderedAttempts, "date", false, 
                  IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
         
         for (DeploymentAttempt attempt : orderedAttempts)
         {
            out.write("<tr><td><nobr>");
            Utils.encodeRecursive(context, 
                     aquireViewAttemptAction(context, attempt));
            out.write("</nobr></td><td>");
            out.write(attempt.getServers());
            out.write("</td><td>");
            Integer version = attempt.getVersion();
            if (version != null)
            {
               out.write(version.toString());
               if (version.intValue() == -1)
               {
                  out.write("&nbsp;(");
                  out.write(bundle.getString("current_working_version"));
                  out.write(")");
               }
            }
            out.write("</td></tr>");
         }
         
         out.write("</table>");
      }
      else
      {
         out.write("<div class='deployServersInfo'>");
         out.write(Application.getMessage(context, "no_deploy_attempts"));
         out.write("</div>\n");
      }
   }
   
   protected void renderAttempt(FacesContext context, ResponseWriter out, String store)
      throws IOException
   {
      // get services required
      NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
      ContentService contentService = Repository.getServiceRegistry(context).getContentService();
      AVMService avmService = Repository.getServiceRegistry(context).getAVMService();

      // get the attempt node to show (if any)
      NodeRef attempt = getAttempt();
      if (attempt == null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Rendering last deployment report for store: " + store);
         
         // get the last deployment attempt id, then locate the deploymentattempt node
         PropertyValue val = avmService.getStoreProperty(store, SandboxConstants.PROP_LAST_DEPLOYMENT_ID);
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
            logger.debug("Retrieving deploymentattempt node with attempt id: " + attemptId);
         
         // get the deploymentattempt object
         attempt = DeploymentUtil.findDeploymentAttempt(attemptId);
      }
      
      // if we have an attempt node, render it
      if (attempt != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Rendering deployment reports for attempt: " + attempt);
         
         // render the supporting JavaScript
         out.write("<script type='text/javascript' src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/scripts/ajax/deployment.js'></script>\n");
         
         // iterate through each deployment report
         List<ChildAssociationRef> deployReportRefs = nodeService.getChildAssocs(
                  attempt, WCMAppModel.ASSOC_DEPLOYMENTREPORTS, RegexQNamePattern.MATCH_ALL);
         if (deployReportRefs.size() > 0)
         {
            for (ChildAssociationRef ref : deployReportRefs)
            {
               // render each report
               renderReport(context, out, ref.getChildRef(), nodeService, contentService);
            }
         }
         else
         {
            out.write("<div class='deployInProgress'><img src='");
            out.write(context.getExternalContext().getRequestContextPath());
            out.write("/images/icons/info_icon_large.gif' />&nbsp;");
            out.write(Application.getMessage(context, "no_deploy_reports"));
            out.write("</div>\n");
         }
         
         // add some padding after the panels
         out.write("\n<div style='padding-top:12px;'></div>\n");
      }
   }
   
   protected void renderReport(FacesContext context, ResponseWriter out, NodeRef deploymentReport,
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
      Long serverId = (Long)serverProps.get(ContentModel.PROP_NODE_DBID);
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
      String excludes = (String)serverProps.get(WCMAppModel.PROP_DEPLOYEXCLUDESUSED);
      String failReason = (String)serverProps.get(WCMAppModel.PROP_DEPLOYFAILEDREASON);
      
      String content = "";
      ContentReader reader = contentService.getReader(deploymentReport, ContentModel.PROP_CONTENT);
      if (reader != null)
      {
         content = reader.getContentString();
         if (content != null)
         {
            content = Utils.encode(content);
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
      out.write(Utils.encode(serverName));
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
         out.write(Utils.encode(failReason));
         out.write("</div>");
      }
      
      if (showServerAddress)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server"));
         out.write(":&nbsp;");
         out.write(Utils.encode(server));
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
      out.write(Utils.encode(creator));
      out.write("</div>");
      
      if (username != null)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server_username"));
         out.write(":&nbsp;");
         out.write(Utils.encode(username));
         out.write("</div>");
      }
      
      if (source != null)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server_source_path"));
         out.write(":&nbsp;");
         out.write(Utils.encode(source));
         out.write("</div>");
      }
      
      if (excludes != null)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server_excludes"));
         out.write(":&nbsp;");
         out.write(Utils.encode(excludes));
         out.write("</div>");
      }
      
      if (target != null)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server_target_name"));
         out.write(":&nbsp;");
         out.write(Utils.encode(target));
         out.write("</div>");
      }
      
      if (success.booleanValue() == true && url != null && url.length() > 0)
      {
         out.write("<div style='margin-top: 3px;'>");
         out.write(Application.getMessage(context, "deploy_server_url"));
         out.write(":&nbsp;<a target='new' href='");
         out.write(url);
         out.write("'>");
         out.write(Utils.encode(url));
         out.write("</a></div>");
      }
      
      if (content.length() > 0)
      {
         out.write("<div style='margin-top: 6px;'><img src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/images/icons/collapsed.gif' style='vertical-align: -1px; cursor: pointer;' class='collapsed' onclick=\"Alfresco.toggleDeploymentDetails(this, '");
         out.write(serverId.toString());
         out.write("');\" />&nbsp;");
         out.write(Application.getMessage(context, "details"));
         out.write("</div>\n");
         out.write("<div id='");
         out.write(serverId.toString());
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
   
   @SuppressWarnings("unchecked")
   protected UIActionLink aquireViewAttemptAction(FacesContext context, DeploymentAttempt attempt)
   {
      UIActionLink action = null;
      String actionId = "va_" + attempt.getId();
      
      // try find the action as a child of this component
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            break;
         }
      }
      
      if (action == null)
      {
         // create the action and add as a child component
         javax.faces.application.Application facesApp = context.getApplication();
         action = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
         action.setId(actionId);
         action.setValue(attempt.getDateAsString());
         action.setTooltip(Application.getMessage(context, MSG_SELECT_ATTEMPT));
         action.setShowLink(true);
         action.setActionListener(facesApp.createMethodBinding(
               "#{DialogManager.bean.attemptSelected}", UIActions.ACTION_CLASS_ARGS));
         
         // add attemptRef param
         UIParameter param1 = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
         param1.setId(actionId + "_1");
         param1.setName("attemptRef");
         param1.setValue(attempt.getNodeRef().toString());
         action.getChildren().add(param1);
         
         // add attemptDate param
         UIParameter param2 = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
         param2.setId(actionId + "_2");
         param2.setName("attemptDate");
         param2.setValue(attempt.getDateAsString());
         action.getChildren().add(param2);
         
         this.getChildren().add(action);
      }
      
      return action;
   }
   
   private class DeploymentAttempt
   {
      private NodeRef nodeRef;
      private String id;
      private Date date;
      private String servers;
      private Integer version;
      
      public DeploymentAttempt(NodeRef nodeRef, String id, Date date, 
               String servers, Integer version)
      {
         this.nodeRef = nodeRef;
         this.id = id;
         this.date = date;
         this.servers = servers;
         this.version = version;
      }

      public NodeRef getNodeRef()
      {
         return this.nodeRef;
      }

      public String getId()
      {
         return this.id;
      }

      public String getServers()
      {
         return this.servers;
      }
      
      public Integer getVersion()
      {
         return this.version;
      }
      
      public Date getDate()
      {
         return this.date;
      }
      
      public String getDateAsString()
      {
         // format the date using the default pattern
         return Utils.getDateTimeFormat(FacesContext.getCurrentInstance()).format(this.date);
      }
   }
}
