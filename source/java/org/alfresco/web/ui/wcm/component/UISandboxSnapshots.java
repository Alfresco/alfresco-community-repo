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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.VersionDescriptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.ConstantMethodBinding;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.UIActions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * @author Kevin Roast
 */
public class UISandboxSnapshots extends SelfRenderingComponent
{
   private static final String ACT_SNAPSHOT_PREVIEW = "snapshot_preview";
   private static final String ACT_SNAPSHOT_REVERT = "snapshot_revert";
   private static final String ACT_SNAPSHOT_DEPLOY = "snapshot_deploy";
   
   private static final String REQUEST_SNAPVERSION = "_snapVer";

   private static Log logger = LogFactory.getLog(UISandboxSnapshots.class);
   
   // snapshot date filters
   public static final String FILTER_DATE_ALL    = "all";
   public static final String FILTER_DATE_TODAY  = "today";
   public static final String FILTER_DATE_WEEK   = "week";
   public static final String FILTER_DATE_MONTH  = "month";
   
   private static final String MSG_LABEL = "name";
   private static final String MSG_DESCRIPTION = "description";
   private static final String MSG_DATE = "date";
   private static final String MSG_USERNAME = "username";
   private static final String MSG_VERSION = "version";
   private static final String MSG_STATUS = "status";
   private static final String MSG_ACTIONS = "actions";
   private static final String MSG_LIVE = "live";
   
   /** sandbox to show snapshots for */
   private String value;
   
   /** date filter to use when listing snapshots */
   private String dateFilter;
   
   /** The snapshot version deployed and the state of that deployment */
   private int deployAttemptVersion = -1;
   private String deployStatus;
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.SandboxSnapshots";
   }
   
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.value = (String)values[1];
   }
   
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.value;
      return values;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#getRendersChildren()
    */
   public boolean getRendersChildren()
   {
      return true;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
    */
   public void encodeChildren(FacesContext context) throws IOException
   {
      // the child components are rendered explicitly during the encodeBegin()
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
      
      ResourceBundle bundle = Application.getBundle(context);
      DateFormat df = Utils.getDateTimeFormat(context);
      AVMService avmService = getAVMService(context);
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(FacesContext.getCurrentInstance(), true);
         tx.begin();
         
         String sandbox = getValue();
         if (sandbox == null)
         {
            throw new IllegalArgumentException("Sandbox must be specified.");
         }
         
         // TODO: apply tag style - removed hardcoded
         out.write("<table class='snapshotItemsList' cellspacing=2 cellpadding=1 border=0 width=100%>");
         // header row
         out.write("<tr align=left><th>");
         out.write(bundle.getString(MSG_LABEL));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_DESCRIPTION));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_DATE));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_USERNAME));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_VERSION));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_STATUS));
         out.write("</th><th>");
         out.write(bundle.getString(MSG_ACTIONS));
         out.write("</th></tr>");
         
         // get the list of snapshots we can potentially display
         List<VersionDescriptor> versions;
         String dateFilter = getDateFilter();
         if (dateFilter == null || dateFilter.equals(FILTER_DATE_ALL))
         {
            versions = avmService.getStoreVersions(sandbox);
         }
         else
         {
            Date toDate = new Date();
            Date fromDate;
            if (FILTER_DATE_TODAY.equals(dateFilter))
            {
               final Calendar c = Calendar.getInstance();
               c.setTime(toDate);
               c.set(Calendar.HOUR, 0);
               c.set(Calendar.MINUTE, 0);
               c.set(Calendar.SECOND, 0);
               fromDate = c.getTime();
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
            versions = avmService.getStoreVersions(sandbox, fromDate, toDate);
         }
         
         // determine whether the deploy action should be shown
         boolean showDeployAction = false;
         NodeRef webProjectRef = AVMUtil.getWebProjectNodeFromStore(sandbox);
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         List<String> deployToServers = (List<String>)nodeService.getProperty(webProjectRef, 
               WCMAppModel.PROP_DEPLOYTO);
         if (deployToServers != null && deployToServers.size() > 0)
         {
            showDeployAction = true;
         }
         
         // determine the deployment status for the website
         determineDeploymentStatus(context, webProjectRef, sandbox, nodeService);
         
         Map requestMap = context.getExternalContext().getRequestMap();
         for (int i=versions.size() - 1; i >= 0; i--) // reverse order
         {
            VersionDescriptor item = versions.get(i);
            
            // only display snapshots with a valid tag - others are system generated snapshots
            if (item.getTag() != null && item.getVersionID() != 0)
            {
               int version = item.getVersionID();
               
               out.write("<tr><td>");
               out.write(item.getTag());
               out.write("</td><td>");
               out.write(item.getDescription() != null ? item.getDescription() : "");
               out.write("</td><td>");
               out.write(df.format(new Date(item.getCreateDate())));
               out.write("</td><td>");
               out.write(item.getCreator());
               out.write("</td><td>");
               out.write(Integer.toString(version));
               out.write("</td><td>");
               if (version == this.deployAttemptVersion && this.deployStatus != null)
               {
                  out.write(this.deployStatus);
               }
               else
               {
                  out.write("&nbsp;");
               }
               out.write("</td><td><nobr>");
               
               // create actions for the item
               
               // revert action
               UIActionLink action = findAction(ACT_SNAPSHOT_REVERT, sandbox);
               if (action == null)
               {
                  Map<String, String> params = new HashMap<String, String>(2, 1.0f);
                  params.put("sandbox", sandbox);
                  params.put("version", "#{" + REQUEST_SNAPVERSION + "}");
                  action = createAction(context, sandbox, ACT_SNAPSHOT_REVERT, "/images/icons/revert.gif",
                        "#{AVMBrowseBean.revertSnapshot}", null, null, params);
                  
               }
               requestMap.put(REQUEST_SNAPVERSION, Integer.toString(item.getVersionID()));
               Utils.encodeRecursive(context, action);
               
               // deploy action (if there are deployto servers specified)
               if (showDeployAction)
               {
                  out.write("&nbsp;&nbsp;");
                  action = findAction(ACT_SNAPSHOT_DEPLOY, sandbox);
                  if (action == null)
                  {
                     Map<String, String> params = new HashMap<String, String>(2, 1.0f);
                     params.put("version", "#{" + REQUEST_SNAPVERSION + "}");
                     action = createAction(context, sandbox, ACT_SNAPSHOT_DEPLOY, "/images/icons/deploy.gif",
                           "#{DialogManager.setupParameters}", "dialog:deploySnapshot", null, params);
                  }
                  
                  Utils.encodeRecursive(context, action);
               }
               
               // TODO: restore once preview of a store by version is implemented in vserver
               //out.write("&nbsp;&nbsp;");
               /*Utils.encodeRecursive(context, aquireAction(
                        context, sandbox, ACT_SNAPSHOT_PREVIEW, null,
                        null, null));
               out.write("&nbsp;");*/
               
               requestMap.remove(REQUEST_SNAPVERSION);
               
               out.write("</nobr></td></tr>");
            }
         }
         
         // end table
         out.write("</table>");
         
         tx.commit();
      }
      catch (Throwable err)
      {
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         throw new RuntimeException(err);
      }
   }
   
   /**
    * Aquire a UIActionLink component for the specified action
    * 
    * @param fc               FacesContext
    * @param sandbox          Root sandbox name
    * @param name             Action name - will be used for I18N message lookup
    * @param icon             Icon to display for the action
    * @param actionListener   Actionlistener for the action
    * @param outcome          Navigation outcome for the action
    * 
    * @return UIActionLink component
    */
   private UIActionLink aquireAction(FacesContext fc, String sandbox, String name, String icon,
         String actionListener, String outcome)
   {
      return aquireAction(fc, sandbox, name, icon, actionListener, outcome, null, null);
   }
   
   /**
    * Aquire a UIActionLink component for the specified action
    * 
    * @param fc               FacesContext
    * @param sandbox          Root sandbox name
    * @param name             Action name - will be used for I18N message lookup
    * @param icon             Icon to display for the action
    * @param actionListener   Actionlistener for the action
    * @param outcome          Navigation outcome for the action
    * @param url              HREF URL for the action
    * @param params           Parameters name/values for the action listener args
    * 
    * @return UIActionLink component
    */
   private UIActionLink aquireAction(FacesContext fc, String sandbox, String name, String icon,
         String actionListener, String outcome, String url, Map<String, String> params)
   {
      UIActionLink action = findAction(name, sandbox);
      if (action == null)
      {
         action = createAction(fc, sandbox, name, icon, actionListener, outcome, url, params);
      }
      return action;
   }
   
   /**
    * Locate a child UIActionLink component by name.
    * 
    * @param name       Of the action component to find
    * @param sandbox    Sandbox the action component is tied to
    * 
    * @return UIActionLink component if found, else null if not created yet
    */
   private UIActionLink findAction(String name, String sandbox)
   {
      UIActionLink action = null;
      String actionId = name + '_' + sandbox;
      if (logger.isDebugEnabled())
         logger.debug("Finding action Id: " + actionId);
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            if (logger.isDebugEnabled())
               logger.debug("...found action Id: " + actionId);
            break;
         }
      }
      return action;
   }
   
   /**
    * Create a UIActionLink child component.
    * 
    * @param fc               FacesContext
    * @param sandbox          Root sandbox name
    * @param name             Action name - will be used for I18N message lookup
    * @param icon             Icon to display for the actio n
    * @param actionListener   Actionlistener for the action
    * @param outcome          Navigation outcome for the action
    * @param url              HREF URL for the action
    * @param params           Parameters name/values for the action listener args
    * 
    * @return UIActionLink child component
    */
   private UIActionLink createAction(FacesContext fc, String sandbox, String name, String icon,
         String actionListener, String outcome, String url, Map<String, String> params)
   {
      javax.faces.application.Application facesApp = fc.getApplication();
      UIActionLink control = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
      
      String id = name + '_' + sandbox;
      if (logger.isDebugEnabled())
         logger.debug("...creating action Id: " + id);
      control.setRendererType(UIActions.RENDERER_ACTIONLINK);
      control.setId(id);
      control.setValue(Application.getMessage(fc, name));
      control.setShowLink(icon != null ? false : true);
      control.setImage(icon);
      
      if (actionListener != null)
      {
         control.setActionListener(facesApp.createMethodBinding(
               actionListener, UIActions.ACTION_CLASS_ARGS));
         
         // add sandbox as the default action listener parameter
         if (params == null)
         {
            UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
            param.setId(id + "_1");
            param.setName("sandbox");
            param.setValue(sandbox);
            control.getChildren().add(param);
         }
         else
         {
            // if a specific set of parameters are supplied, then add them instead
            int idIndex = 1;
            for (String key : params.keySet())
            {
               UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
               param.setId(id + '_' + Integer.toString(idIndex++));
               param.setName(key);
               String value = params.get(key);
               if (value.startsWith("#{") == true)
               {
                  ValueBinding vb = facesApp.createValueBinding(value);
                  param.setValueBinding("value", vb);
               }
               else
               {
                  param.setValue(params.get(key));
               }
               control.getChildren().add(param);
            }
         }
      }
      if (outcome != null)
      {
         control.setAction(new ConstantMethodBinding(outcome));
      }
      if (url != null)
      {
         control.setHref(url);
         control.setTarget("new");
      }
      
      this.getChildren().add(control);
      
      return control;
   }
   
   private void determineDeploymentStatus(FacesContext context, NodeRef webProjectRef, 
         String sandbox, NodeService nodeService)
   {
      // work out what status to show against which snapshot
      List<String> selectedServers = (List<String>)nodeService.getProperty(webProjectRef, 
               WCMAppModel.PROP_SELECTEDDEPLOYTO);
      Integer ver = (Integer)nodeService.getProperty(webProjectRef, 
               WCMAppModel.PROP_SELECTEDDEPLOYVERSION);               
      if (ver != null)
      {
         this.deployAttemptVersion = ver.intValue();
      }
               
      if (selectedServers != null && selectedServers.size() > 0)
      {
         // if the 'selecteddeployto' property is set a deployment has been attempted
         List<ChildAssociationRef> deployReportRefs = nodeService.getChildAssocs(
                  webProjectRef, WCMAppModel.ASSOC_DEPLOYMENTREPORT, RegexQNamePattern.MATCH_ALL);
         
         List<String> deployedServers = new ArrayList<String>();
         
         boolean oneOrMoreFailed = false;
         boolean allFailed = true;
         for (ChildAssociationRef ref : deployReportRefs)
         {
            NodeRef report = ref.getChildRef();

            // get the name of the server and the deploy outcome
            String serverName = (String)nodeService.getProperty(report, 
                     WCMAppModel.PROP_DEPLOYSERVER);
            Boolean successful = (Boolean)nodeService.getProperty(report, 
                     WCMAppModel.PROP_DEPLOYSUCCESSFUL);
            
            deployedServers.add(serverName);
            if (successful != null)
            {
               if (successful.booleanValue())
               {
                  allFailed = false;
               }
               else
               {
                  oneOrMoreFailed = true;
               }
            }
         }
         
         // now we have a list of servers that were deployed see if all
         // the selected servers have a report, if not it means a deployment
         // is in progress. If all servers reports are present then 
         // determine the status by the outcomes of from all the reports.
         boolean allPresent = true;
         for (String selectedServer : selectedServers)
         {
            if (deployedServers.contains(selectedServer) == false)
            {
               allPresent = false;
               break;
            }
         }
         
         if (allPresent)
         {
            // get the right status string
            if (allFailed)
            {
               this.deployStatus = Application.getMessage(context, "deploy_status_failed");
            }
            else if (oneOrMoreFailed)
            {
               this.deployStatus = Application.getMessage(context, "deploy_status_partial");
            }
            else
            {
               this.deployStatus = Application.getMessage(context, "deploy_status_live");
            }
         }
         else
         {
            this.deployStatus = Application.getMessage(context, "deploy_status_in_progress");
         }
      }
   }
   
   private AVMService getAVMService(FacesContext fc)
   {
      return (AVMService)FacesContextUtils.getRequiredWebApplicationContext(fc).getBean("AVMLockingAwareService");
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors

   /**
    * Returns the Sandbox to show the snapshots for
    *
    * @return The Sandbox name
    */
   public String getValue()
   {
      ValueBinding vb = getValueBinding("value");
      if (vb != null)
      {
         this.value = (String)vb.getValue(getFacesContext());
      }
      
      return this.value;
   }
   
   /**
    * Sets the Sandbox to show the snapshots for
    *
    * @param value   The Sandbox name
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * @return Returns the date filter.
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
    * @param dateFilter The date filter to set.
    */
   public void setDateFilter(String dateFilter)
   {
      this.dateFilter = dateFilter;
   }
}
